/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/tar/Attic/TarOutputStream.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
 * $Revision: 1.2 $
 * $Date: 2002/07/13 22:37:46 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.io.compress.tar;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The TarOutputStream writes a UNIX tar archive as an OutputStream. Methods are
 * provided to put entries, and then write their contents by writing to this
 * stream using write().
 *
 * @author Timothy Gerard Endres <a href="mailto:time@ice.com">time@ice.com</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/13 22:37:46 $
 * @see TarInputStream
 * @see TarEntry
 */
public class TarOutputStream
    extends FilterOutputStream
{
    /**
     * Flag to indicate that an error should be generated if
     * an attempt is made to write an entry that exceeds the 100 char
     * POSIX limit.
     */
    public static final int LONGFILE_ERROR = 0;

    /**
     * Flag to indicate that entry name should be truncated if
     * an attempt is made to write an entry that exceeds the 100 char
     * POSIX limit.
     */
    public static final int LONGFILE_TRUNCATE = 1;

    /**
     * Flag to indicate that entry name should be formatted
     * according to GNU tar extension if an attempt is made
     * to write an entry that exceeds the 100 char POSIX
     * limit. Note that this makes the jar unreadable by
     * non-GNU tar commands.
     */
    public static final int LONGFILE_GNU = 2;

    private int m_longFileMode = LONGFILE_ERROR;
    private byte[] m_assemBuf;
    private int m_assemLen;
    private TarBuffer m_buffer;
    private int m_currBytes;
    private int m_currSize;

    private byte[] m_oneBuf;
    private byte[] m_recordBuf;

    /**
     * Construct a TarOutputStream using specified input
     * stream and default block and record sizes.
     *
     * @param output stream to create TarOutputStream from
     * @see TarBuffer#DEFAULT_BLOCKSIZE
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    public TarOutputStream( final OutputStream output )
    {
        this( output, TarBuffer.DEFAULT_BLOCKSIZE, TarBuffer.DEFAULT_RECORDSIZE );
    }

    /**
     * Construct a TarOutputStream using specified input
     * stream, block size and default record sizes.
     *
     * @param output stream to create TarOutputStream from
     * @param blockSize the block size
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    public TarOutputStream( final OutputStream output,
                            final int blockSize )
    {
        this( output, blockSize, TarBuffer.DEFAULT_RECORDSIZE );
    }

    /**
     * Construct a TarOutputStream using specified input
     * stream, block size and record sizes.
     *
     * @param output stream to create TarOutputStream from
     * @param blockSize the block size
     * @param recordSize the record size
     */
    public TarOutputStream( final OutputStream output,
                            final int blockSize,
                            final int recordSize )
    {
        super( output );

        m_buffer = new TarBuffer( output, blockSize, recordSize );
        m_assemLen = 0;
        m_assemBuf = new byte[ recordSize ];
        m_recordBuf = new byte[ recordSize ];
        m_oneBuf = new byte[ 1 ];
    }

    /**
     * Sets the debugging flag in this stream's TarBuffer.
     *
     * @param debug The new BufferDebug value
     */
    public void setBufferDebug( boolean debug )
    {
        m_buffer.setDebug( debug );
    }

    /**
     * Set the mode used to work with entrys exceeding
     * 100 chars (and thus break the POSIX standard).
     * Must be one of the LONGFILE_* constants.
     *
     * @param longFileMode the mode
     */
    public void setLongFileMode( final int longFileMode )
    {
        if( LONGFILE_ERROR != longFileMode &&
            LONGFILE_GNU != longFileMode &&
            LONGFILE_TRUNCATE != longFileMode )
        {
            throw new IllegalArgumentException( "longFileMode" );
        }
        m_longFileMode = longFileMode;
    }

    /**
     * Get the record size being used by this stream's TarBuffer.
     *
     * @return The TarBuffer record size.
     */
    public int getRecordSize()
    {
        return m_buffer.getRecordSize();
    }

    /**
     * Ends the TAR archive and closes the underlying OutputStream. This means
     * that finish() is called followed by calling the TarBuffer's close().
     *
     * @exception IOException when an IO error causes operation to fail
     */
    public void close()
        throws IOException
    {
        finish();
        m_buffer.close();
    }

    /**
     * Close an entry. This method MUST be called for all file entries that
     * contain data. The reason is that we must buffer data written to the
     * stream in order to satisfy the buffer's record based writes. Thus, there
     * may be data fragments still being assembled that must be written to the
     * output stream before this entry is closed and the next entry written.
     *
     * @exception IOException when an IO error causes operation to fail
     */
    public void closeEntry()
        throws IOException
    {
        if( m_assemLen > 0 )
        {
            for( int i = m_assemLen; i < m_assemBuf.length; ++i )
            {
                m_assemBuf[ i ] = 0;
            }

            m_buffer.writeRecord( m_assemBuf );

            m_currBytes += m_assemLen;
            m_assemLen = 0;
        }

        if( m_currBytes < m_currSize )
        {
            final String message = "entry closed at '" + m_currBytes +
                "' before the '" + m_currSize +
                "' bytes specified in the header were written";
            throw new IOException( message );
        }
    }

    /**
     * Ends the TAR archive without closing the underlying OutputStream. The
     * result is that the EOF record of nulls is written.
     *
     * @exception IOException when an IO error causes operation to fail
     */
    public void finish()
        throws IOException
    {
        writeEOFRecord();
    }

    /**
     * Put an entry on the output stream. This writes the entry's header record
     * and positions the output stream for writing the contents of the entry.
     * Once this method is called, the stream is ready for calls to write() to
     * write the entry's contents. Once the contents are written, closeEntry()
     * <B>MUST</B> be called to ensure that all buffered data is completely
     * written to the output stream.
     *
     * @param entry The TarEntry to be written to the archive.
     * @exception IOException when an IO error causes operation to fail
     */
    public void putNextEntry( final TarEntry entry )
        throws IOException
    {
        if( entry.getName().length() >= TarEntry.NAMELEN )
        {
            if( m_longFileMode == LONGFILE_GNU )
            {
                // create a TarEntry for the LongLink, the contents
                // of which are the entry's name
                final TarEntry longLinkEntry =
                    new TarEntry( TarConstants.GNU_LONGLINK,
                                  TarConstants.LF_GNUTYPE_LONGNAME );

                longLinkEntry.setSize( entry.getName().length() );
                putNextEntry( longLinkEntry );
                write( entry.getName().getBytes() );
                //write( 0 );
                closeEntry();
            }
            else if( m_longFileMode != LONGFILE_TRUNCATE )
            {
                final String message = "file name '" + entry.getName() +
                    "' is too long ( > " + TarEntry.NAMELEN + " bytes)";
                throw new IOException( message );
            }
        }

        entry.writeEntryHeader( m_recordBuf );
        m_buffer.writeRecord( m_recordBuf );

        m_currBytes = 0;

        if( entry.isDirectory() )
        {
            m_currSize = 0;
        }
        else
        {
            m_currSize = (int)entry.getSize();
        }
    }

    /**
     * Copies the contents of the specified stream into current tar
     * archive entry.
     *
     * @param input The InputStream from which to read entrys data
     * @exception IOException when an IO error causes operation to fail
     */
    public void copyEntryContents( final InputStream input )
        throws IOException
    {
        final byte[] buffer = new byte[ 32 * 1024 ];
        while( true )
        {
            final int numRead = input.read( buffer, 0, buffer.length );
            if( numRead == -1 )
            {
                break;
            }

            write( buffer, 0, numRead );
        }
    }

    /**
     * Writes a byte to the current tar archive entry. This method simply calls
     * read( byte[], int, int ).
     *
     * @param data The byte written.
     * @exception IOException when an IO error causes operation to fail
     */
    public void write( final int data )
        throws IOException
    {
        m_oneBuf[ 0 ] = (byte)data;

        write( m_oneBuf, 0, 1 );
    }

    /**
     * Writes bytes to the current tar archive entry. This method simply calls
     * write( byte[], int, int ).
     *
     * @param buffer The buffer to write to the archive.
     * @exception IOException when an IO error causes operation to fail
     */
    public void write( final byte[] buffer )
        throws IOException
    {
        write( buffer, 0, buffer.length );
    }

    /**
     * Writes bytes to the current tar archive entry. This method is aware of
     * the current entry and will throw an exception if you attempt to write
     * bytes past the length specified for the current entry. The method is also
     * (painfully) aware of the record buffering required by TarBuffer, and
     * manages buffers that are not a multiple of recordsize in length,
     * including assembling records from small buffers.
     *
     * @param buffer The buffer to write to the archive.
     * @param offset The offset in the buffer from which to get bytes.
     * @param count The number of bytes to write.
     * @exception IOException when an IO error causes operation to fail
     */
    public void write( final byte[] buffer,
                       final int offset,
                       final int count )
        throws IOException
    {
        int position = offset;
        int numToWrite = count;
        if( ( m_currBytes + numToWrite ) > m_currSize )
        {
            final String message = "request to write '" + numToWrite +
                "' bytes exceeds size in header of '" + m_currSize + "' bytes";
            throw new IOException( message );
            //
            // We have to deal with assembly!!!
            // The programmer can be writing little 32 byte chunks for all
            // we know, and we must assemble complete records for writing.
            // REVIEW Maybe this should be in TarBuffer? Could that help to
            // eliminate some of the buffer copying.
            //
        }

        if( m_assemLen > 0 )
        {
            if( ( m_assemLen + numToWrite ) >= m_recordBuf.length )
            {
                final int length = m_recordBuf.length - m_assemLen;

                System.arraycopy( m_assemBuf, 0, m_recordBuf, 0,
                                  m_assemLen );
                System.arraycopy( buffer, position, m_recordBuf,
                                  m_assemLen, length );
                m_buffer.writeRecord( m_recordBuf );

                m_currBytes += m_recordBuf.length;
                position += length;
                numToWrite -= length;
                m_assemLen = 0;
            }
            else
            {
                System.arraycopy( buffer, position, m_assemBuf, m_assemLen,
                                  numToWrite );

                position += numToWrite;
                m_assemLen += numToWrite;
                numToWrite -= numToWrite;
            }
        }

        //
        // When we get here we have EITHER:
        // o An empty "assemble" buffer.
        // o No bytes to write (numToWrite == 0)
        //
        while( numToWrite > 0 )
        {
            if( numToWrite < m_recordBuf.length )
            {
                System.arraycopy( buffer, position, m_assemBuf, m_assemLen,
                                  numToWrite );

                m_assemLen += numToWrite;

                break;
            }

            m_buffer.writeRecord( buffer, position );

            int num = m_recordBuf.length;

            m_currBytes += num;
            numToWrite -= num;
            position += num;
        }
    }

    /**
     * Write an EOF (end of archive) record to the tar archive. An EOF record
     * consists of a record of all zeros.
     *
     * @exception IOException when an IO error causes operation to fail
     */
    private void writeEOFRecord()
        throws IOException
    {
        for( int i = 0; i < m_recordBuf.length; ++i )
        {
            m_recordBuf[ i ] = 0;
        }

        m_buffer.writeRecord( m_recordBuf );
    }
}
