/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipException;

/**
 * Reimplementation of {@link java.util.zip.ZipOutputStream
 * java.util.zip.ZipOutputStream} that does handle the extended functionality of
 * this package, especially internal/external file attributes and extra fields
 * with different layouts for local file data and central directory entries. <p>
 *
 * This implementation will use a Data Descriptor to store size and CRC
 * information for DEFLATED entries, this means, you don't need to calculate
 * them yourself. Unfortunately this is not possible for the STORED method, here
 * setting the CRC and uncompressed size information is required before {@link
 * #putNextEntry putNextEntry} will be called.</p>
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.1 $
 */
public class ZipOutputStream
    extends DeflaterOutputStream
{
    /**
     * Helper, a 0 as ZipShort.
     *
     * @since 1.1
     */
    private static final byte[] ZERO = {0, 0};

    /**
     * Helper, a 0 as ZipLong.
     *
     * @since 1.1
     */
    private static final byte[] LZERO = {0, 0, 0, 0};

    /**
     * Compression method for deflated entries.
     *
     * @since 1.1
     */
    public static final int DEFLATED = ZipEntry.DEFLATED;

    /**
     * Compression method for deflated entries.
     *
     * @since 1.1
     */
    public static final int STORED = ZipEntry.STORED;

    /*
     * Various ZIP constants
     */
    /**
     * local file header signature
     *
     * @since 1.1
     */
    protected static final ZipLong LFH_SIG = new ZipLong( 0X04034B50L );
    /**
     * data descriptor signature
     *
     * @since 1.1
     */
    protected static final ZipLong DD_SIG = new ZipLong( 0X08074B50L );
    /**
     * central file header signature
     *
     * @since 1.1
     */
    protected static final ZipLong CFH_SIG = new ZipLong( 0X02014B50L );
    /**
     * end of central dir signature
     *
     * @since 1.1
     */
    protected static final ZipLong EOCD_SIG = new ZipLong( 0X06054B50L );

    /**
     * Smallest date/time ZIP can handle.
     *
     * @since 1.1
     */
    private static final ZipLong DOS_TIME_MIN = new ZipLong( 0x00002100L );

    /**
     * The file comment.
     *
     * @since 1.1
     */
    private String m_comment = "";

    /**
     * Compression level for next entry.
     *
     * @since 1.1
     */
    private int m_level = Deflater.DEFAULT_COMPRESSION;

    /**
     * Default compression method for next entry.
     *
     * @since 1.1
     */
    private int m_method = DEFLATED;

    /**
     * List of ZipEntries written so far.
     *
     * @since 1.1
     */
    private final ArrayList m_entries = new ArrayList();

    /**
     * CRC instance to avoid parsing DEFLATED data twice.
     *
     * @since 1.1
     */
    private final CRC32 m_crc = new CRC32();

    /**
     * Count the bytes written to out.
     *
     * @since 1.1
     */
    private long m_written;

    /**
     * Data for current entry started here.
     *
     * @since 1.1
     */
    private long m_dataStart;

    /**
     * Start of central directory.
     *
     * @since 1.1
     */
    private ZipLong m_cdOffset = new ZipLong( 0 );

    /**
     * Length of central directory.
     *
     * @since 1.1
     */
    private ZipLong m_cdLength = new ZipLong( 0 );

    /**
     * Holds the offsets of the LFH starts for each entry
     *
     * @since 1.1
     */
    private final Hashtable m_offsets = new Hashtable();

    /**
     * The encoding to use for filenames and the file comment. <p>
     *
     * For a list of possible values see <a
     * href="http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html">
     * http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html
     * </a>. Defaults to the platform's default character encoding.</p>
     *
     * @since 1.3
     */
    private String m_encoding;

    /**
     * Current entry.
     *
     * @since 1.1
     */
    private ZipEntry m_entry;

    /**
     * Creates a new ZIP OutputStream filtering the underlying stream.
     *
     * @param output the output stream to write to
     * @since 1.1
     */
    public ZipOutputStream( final OutputStream output )
    {
        super( output, new Deflater( Deflater.DEFAULT_COMPRESSION, true ) );
    }

    /**
     * Convert a Date object to a DOS date/time field. <p>
     *
     * Stolen from InfoZip's <code>fileio.c</code></p>
     *
     * @param time Description of Parameter
     * @return Description of the Returned Value
     * @since 1.1
     */
    protected static ZipLong toDosTime( Date time )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime( time );
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        if( year < 1980 )
        {
            return DOS_TIME_MIN;
        }
        long value = ( ( year - 1980 ) << 25 )
            | ( month << 21 )
            | ( cal.get(Calendar.DAY_OF_MONTH) << 16 )
            | ( cal.get(Calendar.HOUR_OF_DAY) << 11 )
            | ( cal.get(Calendar.MINUTE) << 5 )
            | ( cal.get(Calendar.SECOND) >> 1 );

        byte[] result = new byte[ 4 ];
        result[ 0 ] = (byte)( ( value & 0xFF ) );
        result[ 1 ] = (byte)( ( value & 0xFF00 ) >> 8 );
        result[ 2 ] = (byte)( ( value & 0xFF0000 ) >> 16 );
        result[ 3 ] = (byte)( ( value & 0xFF000000l ) >> 24 );
        return new ZipLong( result );
    }

    /**
     * Set the file comment.
     *
     * @param comment The new Comment value
     * @since 1.1
     */
    public void setComment( String comment )
    {
        m_comment = comment;
    }

    /**
     * The encoding to use for filenames and the file comment. <p>
     *
     * For a list of possible values see <a
     * href="http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html">
     * http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html
     * </a>. Defaults to the platform's default character encoding.</p>
     *
     * @param encoding The new Encoding value
     * @since 1.3
     */
    public void setEncoding( String encoding )
    {
        m_encoding = encoding;
    }

    /**
     * Sets the compression level for subsequent entries. <p>
     *
     * Default is Deflater.DEFAULT_COMPRESSION.</p>
     *
     * @param level The new Level value
     * @since 1.1
     */
    public void setLevel( int level )
    {
        m_level = level;
    }

    /**
     * Sets the default compression method for subsequent entries. <p>
     *
     * Default is DEFLATED.</p>
     *
     * @param method The new Method value
     * @since 1.1
     */
    public void setMethod( final int method )
    {
        m_method = method;
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return null if using the platform's default character encoding.
     * @since 1.3
     */
    public String getEncoding()
    {
        return m_encoding;
    }

    /**
     * Writes all necessary data for this entry.
     *
     * @throws IOException if an IO failure causes operation to fail
     * @since 1.1
     */
    public void closeEntry()
        throws IOException
    {
        if( m_entry == null )
        {
            return;
        }

        long realCrc = m_crc.getValue();
        m_crc.reset();

        if( m_entry.getMethod() == DEFLATED )
        {
            def.finish();
            while( !def.finished() )
            {
                deflate();
            }

            m_entry.setSize( def.getTotalIn() );
            m_entry.setComprSize( def.getTotalOut() );
            m_entry.setCrc( realCrc );

            def.reset();

            m_written += m_entry.getCompressedSize();
        }
        else
        {
            if( m_entry.getCrc() != realCrc )
            {
                throw new ZipException( "bad CRC checksum for entry "
                                        + m_entry.getName() + ": "
                                        + Long.toHexString( m_entry.getCrc() )
                                        + " instead of "
                                        + Long.toHexString( realCrc ) );
            }

            if( m_entry.getSize() != m_written - m_dataStart )
            {
                throw new ZipException( "bad size for entry "
                                        + m_entry.getName() + ": "
                                        + m_entry.getSize()
                                        + " instead of "
                                        + ( m_written - m_dataStart ) );
            }

        }

        writeDataDescriptor( m_entry );
        m_entry = null;
    }

    /*
     * Found out by experiment, that DeflaterOutputStream.close()
     * will call finish() - so we don't need to override close
     * ourselves.
     */
    /**
     * Finishs writing the contents and closes this as well as the underlying
     * stream.
     *
     * @throws IOException if an IO failure causes operation to fail
     * @since 1.1
     */
    public void finish()
        throws IOException
    {
        closeEntry();
        m_cdOffset = new ZipLong( m_written );
        final int size = m_entries.size();
        for( int i = 0; i < size; i++ )
        {
            final ZipEntry entry = (ZipEntry)m_entries.get( i );
            writeCentralFileHeader( entry );
        }
        m_cdLength = new ZipLong( m_written - m_cdOffset.getValue() );
        writeCentralDirectoryEnd();
        m_offsets.clear();
        m_entries.clear();
    }

    /**
     * Begin writing next entry.
     *
     * @param entry the entry
     * @throws IOException if an IO failure causes operation to fail
     * @since 1.1
     */
    public void putNextEntry( final ZipEntry entry )
        throws IOException
    {
        closeEntry();

        m_entry = entry;
        m_entries.add( m_entry );

        if( m_entry.getMethod() == -1 )
        {// not specified
            m_entry.setMethod( m_method );
        }

        if( m_entry.getTime() == -1 )
        {// not specified
            m_entry.setTime( System.currentTimeMillis() );
        }

        if( m_entry.getMethod() == STORED )
        {
            if( m_entry.getSize() == -1 )
            {
                throw new ZipException( "uncompressed size is required for STORED method" );
            }
            if( m_entry.getCrc() == -1 )
            {
                throw new ZipException( "crc checksum is required for STORED method" );
            }
            m_entry.setComprSize( m_entry.getSize() );
        }
        else
        {
            def.setLevel( m_level );
        }
        writeLocalFileHeader( m_entry );
    }

    /**
     * Writes bytes to ZIP entry. <p>
     *
     * Override is necessary to support STORED entries, as well as calculationg
     * CRC automatically for DEFLATED entries.</p>
     *
     * @param buffer the buffer to write to
     * @param offset the offset to write to
     * @param length the length of data to write
     * @exception IOException if an IO error causes operation to fail
     */
    public void write( final byte[] buffer,
                       final int offset,
                       final int length )
        throws IOException
    {
        if( m_entry.getMethod() == DEFLATED )
        {
            super.write( buffer, offset, length );
        }
        else
        {
            out.write( buffer, offset, length );
            m_written += length;
        }
        m_crc.update( buffer, offset, length );
    }

    /**
     * Retrieve the bytes for the given String in the encoding set for this
     * Stream.
     *
     * @param name the name to decode
     * @return the bytes for string
     * @exception ZipException if fail to retrieve bytes for specified string
     * @since 1.3
     */
    protected byte[] getBytes( String name )
        throws ZipException
    {
        if( m_encoding == null )
        {
            return name.getBytes();
        }
        else
        {
            try
            {
                return name.getBytes( m_encoding );
            }
            catch( UnsupportedEncodingException uee )
            {
                throw new ZipException( uee.getMessage() );
            }
        }
    }

    /**
     * Writes the &quot;End of central dir record&quot;
     *
     * @exception IOException when an IO erro causes operation to fail
     * @since 1.1
     */
    protected void writeCentralDirectoryEnd()
        throws IOException
    {
        out.write( EOCD_SIG.getBytes() );

        // disk numbers
        out.write( ZERO );
        out.write( ZERO );

        // number of entries
        byte[] num = ( new ZipShort( m_entries.size() ) ).getBytes();
        out.write( num );
        out.write( num );

        // length and location of CD
        out.write( m_cdLength.getBytes() );
        out.write( m_cdOffset.getBytes() );

        // ZIP file comment
        byte[] data = getBytes( m_comment );
        out.write( ( new ZipShort( data.length ) ).getBytes() );
        out.write( data );
    }

    /**
     * Writes the central file header entry
     *
     * @param entry the zip entry
     * @throws IOException when an IO error causes operation to fail
     * @since 1.1
     */
    protected void writeCentralFileHeader( final ZipEntry entry )
        throws IOException
    {
        out.write( CFH_SIG.getBytes() );
        m_written += 4;

        // version made by
        out.write( ( new ZipShort( 20 ) ).getBytes() );
        m_written += 2;

        // version needed to extract
        // general purpose bit flag
        if( entry.getMethod() == DEFLATED )
        {
            // requires version 2 as we are going to store length info
            // in the data descriptor
            out.write( ( new ZipShort( 20 ) ).getBytes() );

            // bit3 set to signal, we use a data descriptor
            out.write( ( new ZipShort( 8 ) ).getBytes() );
        }
        else
        {
            out.write( ( new ZipShort( 10 ) ).getBytes() );
            out.write( ZERO );
        }
        m_written += 4;

        // compression method
        out.write( ( new ZipShort( entry.getMethod() ) ).getBytes() );
        m_written += 2;

        // last mod. time and date
        out.write( toDosTime( new Date( entry.getTime() ) ).getBytes() );
        m_written += 4;

        // CRC
        // compressed length
        // uncompressed length
        out.write( ( new ZipLong( entry.getCrc() ) ).getBytes() );
        out.write( ( new ZipLong( entry.getCompressedSize() ) ).getBytes() );
        out.write( ( new ZipLong( entry.getSize() ) ).getBytes() );
        m_written += 12;

        // file name length
        byte[] name = getBytes( entry.getName() );
        out.write( ( new ZipShort( name.length ) ).getBytes() );
        m_written += 2;

        // extra field length
        byte[] extra = entry.getCentralDirectoryExtra();
        out.write( ( new ZipShort( extra.length ) ).getBytes() );
        m_written += 2;

        // file comment length
        String comm = entry.getComment();
        if( comm == null )
        {
            comm = "";
        }
        byte[] comment = getBytes( comm );
        out.write( ( new ZipShort( comment.length ) ).getBytes() );
        m_written += 2;

        // disk number start
        out.write( ZERO );
        m_written += 2;

        // internal file attributes
        out.write( ( new ZipShort( entry.getInternalAttributes() ) ).getBytes() );
        m_written += 2;

        // external file attributes
        out.write( ( new ZipLong( entry.getExternalAttributes() ) ).getBytes() );
        m_written += 4;

        // relative offset of LFH
        out.write( ( (ZipLong)m_offsets.get( entry ) ).getBytes() );
        m_written += 4;

        // file name
        out.write( name );
        m_written += name.length;

        // extra field
        out.write( extra );
        m_written += extra.length;

        // file comment
        out.write( comment );
        m_written += comment.length;
    }

    /**
     * Writes the data descriptor entry
     *
     * @param ze Description of Parameter
     * @throws IOException if an IO failure causes operation to fail
     * @since 1.1
     */
    protected void writeDataDescriptor( ZipEntry ze )
        throws IOException
    {
        if( ze.getMethod() != DEFLATED )
        {
            return;
        }
        out.write( DD_SIG.getBytes() );
        out.write( ( new ZipLong( m_entry.getCrc() ) ).getBytes() );
        out.write( ( new ZipLong( m_entry.getCompressedSize() ) ).getBytes() );
        out.write( ( new ZipLong( m_entry.getSize() ) ).getBytes() );
        m_written += 16;
    }

    /**
     * Writes the local file header entry
     *
     * @param entry the zip entry
     * @exception IOException when an IO error causes operation to fail
     * @since 1.1
     */
    protected void writeLocalFileHeader( final ZipEntry entry )
        throws IOException
    {
        m_offsets.put( entry, new ZipLong( m_written ) );

        out.write( LFH_SIG.getBytes() );
        m_written += 4;

        // version needed to extract
        // general purpose bit flag
        if( entry.getMethod() == DEFLATED )
        {
            // requires version 2 as we are going to store length info
            // in the data descriptor
            out.write( ( new ZipShort( 20 ) ).getBytes() );

            // bit3 set to signal, we use a data descriptor
            out.write( ( new ZipShort( 8 ) ).getBytes() );
        }
        else
        {
            out.write( ( new ZipShort( 10 ) ).getBytes() );
            out.write( ZERO );
        }
        m_written += 4;

        // compression method
        out.write( ( new ZipShort( entry.getMethod() ) ).getBytes() );
        m_written += 2;

        // last mod. time and date
        out.write( toDosTime( new Date( entry.getTime() ) ).getBytes() );
        m_written += 4;

        // CRC
        // compressed length
        // uncompressed length
        if( entry.getMethod() == DEFLATED )
        {
            out.write( LZERO );
            out.write( LZERO );
            out.write( LZERO );
        }
        else
        {
            out.write( ( new ZipLong( entry.getCrc() ) ).getBytes() );
            out.write( ( new ZipLong( entry.getSize() ) ).getBytes() );
            out.write( ( new ZipLong( entry.getSize() ) ).getBytes() );
        }
        m_written += 12;

        // file name length
        byte[] name = getBytes( entry.getName() );
        out.write( ( new ZipShort( name.length ) ).getBytes() );
        m_written += 2;

        // extra field length
        byte[] extra = entry.getLocalFileDataExtra();
        out.write( ( new ZipShort( extra.length ) ).getBytes() );
        m_written += 2;

        // file name
        out.write( name );
        m_written += name.length;

        // extra field
        out.write( extra );
        m_written += extra.length;

        m_dataStart = m_written;
    }

}
