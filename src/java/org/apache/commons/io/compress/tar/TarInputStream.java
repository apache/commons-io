/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/tar/Attic/TarInputStream.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The TarInputStream reads a UNIX tar archive as an InputStream. methods are
 * provided to position at each successive entry in the archive, and the read
 * each entry as a normal input stream using read().
 *
 * @author <a href="mailto:time@ice.com">Timothy Gerard Endres</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/13 22:37:46 $
 * @see TarInputStream
 * @see TarEntry
 */
public class TarInputStream
    extends FilterInputStream
{
    private TarBuffer m_buffer;
    private TarEntry m_currEntry;
    private boolean m_debug;
    private int m_entryOffset;
    private int m_entrySize;
    private boolean m_hasHitEOF;
    private byte[] m_oneBuf;
    private byte[] m_readBuf;

    /**
     * Construct a TarInputStream using specified input
     * stream and default block and record sizes.
     *
     * @param input stream to create TarInputStream from
     * @see TarBuffer#DEFAULT_BLOCKSIZE
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    public TarInputStream( final InputStream input )
    {
        this( input, TarBuffer.DEFAULT_BLOCKSIZE, TarBuffer.DEFAULT_RECORDSIZE );
    }

    /**
     * Construct a TarInputStream using specified input
     * stream, block size and default record sizes.
     *
     * @param input stream to create TarInputStream from
     * @param blockSize the block size to use
     * @see TarBuffer#DEFAULT_RECORDSIZE
     */
    public TarInputStream( final InputStream input,
                           final int blockSize )
    {
        this( input, blockSize, TarBuffer.DEFAULT_RECORDSIZE );
    }

    /**
     * Construct a TarInputStream using specified input
     * stream, block size and record sizes.
     *
     * @param input stream to create TarInputStream from
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     */
    public TarInputStream( final InputStream input,
                           final int blockSize,
                           final int recordSize )
    {
        super( input );

        m_buffer = new TarBuffer( input, blockSize, recordSize );
        m_oneBuf = new byte[ 1 ];
    }

    /**
     * Sets the debugging flag.
     *
     * @param debug The new Debug value
     */
    public void setDebug( final boolean debug )
    {
        m_debug = debug;
        m_buffer.setDebug( debug );
    }

    /**
     * Get the next entry in this tar archive. This will skip over any remaining
     * data in the current entry, if there is one, and place the input stream at
     * the header of the next entry, and read the header and instantiate a new
     * TarEntry from the header bytes and return that entry. If there are no
     * more entries in the archive, null will be returned to indicate that the
     * end of the archive has been reached.
     *
     * @return The next TarEntry in the archive, or null.
     * @exception IOException Description of Exception
     */
    public TarEntry getNextEntry()
        throws IOException
    {
        if( m_hasHitEOF )
        {
            return null;
        }

        if( m_currEntry != null )
        {
            final int numToSkip = m_entrySize - m_entryOffset;

            if( m_debug )
            {
                final String message = "TarInputStream: SKIP currENTRY '" +
                    m_currEntry.getName() + "' SZ " + m_entrySize +
                    " OFF " + m_entryOffset + "  skipping " + numToSkip + " bytes";
                debug( message );
            }

            if( numToSkip > 0 )
            {
                skip( numToSkip );
            }

            m_readBuf = null;
        }

        final byte[] headerBuf = m_buffer.readRecord();
        if( headerBuf == null )
        {
            if( m_debug )
            {
                debug( "READ NULL RECORD" );
            }
            m_hasHitEOF = true;
        }
        else if( m_buffer.isEOFRecord( headerBuf ) )
        {
            if( m_debug )
            {
                debug( "READ EOF RECORD" );
            }
            m_hasHitEOF = true;
        }

        if( m_hasHitEOF )
        {
            m_currEntry = null;
        }
        else
        {
            m_currEntry = new TarEntry( headerBuf );

            if( !( headerBuf[ 257 ] == 'u' && headerBuf[ 258 ] == 's' &&
                headerBuf[ 259 ] == 't' && headerBuf[ 260 ] == 'a' &&
                headerBuf[ 261 ] == 'r' ) )
            {
                //Must be v7Format
            }

            if( m_debug )
            {
                final String message = "TarInputStream: SET CURRENTRY '" +
                    m_currEntry.getName() + "' size = " + m_currEntry.getSize();
                debug( message );
            }

            m_entryOffset = 0;

            // REVIEW How do we resolve this discrepancy?!
            m_entrySize = (int)m_currEntry.getSize();
        }

        if( null != m_currEntry && m_currEntry.isGNULongNameEntry() )
        {
            // read in the name
            final StringBuffer longName = new StringBuffer();
            final byte[] buffer = new byte[ 256 ];
            int length = 0;
            while( ( length = read( buffer ) ) >= 0 )
            {
                final String str = new String( buffer, 0, length );
                longName.append( str );
            }
            getNextEntry();
            m_currEntry.setName( longName.toString() );
        }

        return m_currEntry;
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
     * Get the available data that can be read from the current entry in the
     * archive. This does not indicate how much data is left in the entire
     * archive, only in the current entry. This value is determined from the
     * entry's size header field and the amount of data already read from the
     * current entry.
     *
     * @return The number of available bytes for the current entry.
     * @exception IOException when an IO error causes operation to fail
     */
    public int available()
        throws IOException
    {
        return m_entrySize - m_entryOffset;
    }

    /**
     * Closes this stream. Calls the TarBuffer's close() method.
     *
     * @exception IOException when an IO error causes operation to fail
     */
    public void close()
        throws IOException
    {
        m_buffer.close();
    }

    /**
     * Copies the contents of the current tar archive entry directly into an
     * output stream.
     *
     * @param output The OutputStream into which to write the entry's data.
     * @exception IOException when an IO error causes operation to fail
     */
    public void copyEntryContents( final OutputStream output )
        throws IOException
    {
        final byte[] buffer = new byte[ 32 * 1024 ];
        while( true )
        {
            final int numRead = read( buffer, 0, buffer.length );
            if( numRead == -1 )
            {
                break;
            }

            output.write( buffer, 0, numRead );
        }
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     *
     * @param markLimit The limit to mark.
     */
    public void mark( int markLimit )
    {
    }

    /**
     * Since we do not support marking just yet, we return false.
     *
     * @return False.
     */
    public boolean markSupported()
    {
        return false;
    }

    /**
     * Reads a byte from the current tar archive entry. This method simply calls
     * read( byte[], int, int ).
     *
     * @return The byte read, or -1 at EOF.
     * @exception IOException when an IO error causes operation to fail
     */
    public int read()
        throws IOException
    {
        final int num = read( m_oneBuf, 0, 1 );
        if( num == -1 )
        {
            return num;
        }
        else
        {
            return (int)m_oneBuf[ 0 ];
        }
    }

    /**
     * Reads bytes from the current tar archive entry. This method simply calls
     * read( byte[], int, int ).
     *
     * @param buffer The buffer into which to place bytes read.
     * @return The number of bytes read, or -1 at EOF.
     * @exception IOException when an IO error causes operation to fail
     */
    public int read( final byte[] buffer )
        throws IOException
    {
        return read( buffer, 0, buffer.length );
    }

    /**
     * Reads bytes from the current tar archive entry. This method is aware of
     * the boundaries of the current entry in the archive and will deal with
     * them as if they were this stream's start and EOF.
     *
     * @param buffer The buffer into which to place bytes read.
     * @param offset The offset at which to place bytes read.
     * @param count The number of bytes to read.
     * @return The number of bytes read, or -1 at EOF.
     * @exception IOException when an IO error causes operation to fail
     */
    public int read( final byte[] buffer,
                     final int offset,
                     final int count )
        throws IOException
    {
        int position = offset;
        int numToRead = count;
        int totalRead = 0;

        if( m_entryOffset >= m_entrySize )
        {
            return -1;
        }

        if( ( numToRead + m_entryOffset ) > m_entrySize )
        {
            numToRead = ( m_entrySize - m_entryOffset );
        }

        if( null != m_readBuf )
        {
            final int size =
                ( numToRead > m_readBuf.length ) ? m_readBuf.length : numToRead;

            System.arraycopy( m_readBuf, 0, buffer, position, size );

            if( size >= m_readBuf.length )
            {
                m_readBuf = null;
            }
            else
            {
                final int newLength = m_readBuf.length - size;
                final byte[] newBuffer = new byte[ newLength ];

                System.arraycopy( m_readBuf, size, newBuffer, 0, newLength );

                m_readBuf = newBuffer;
            }

            totalRead += size;
            numToRead -= size;
            position += size;
        }

        while( numToRead > 0 )
        {
            final byte[] rec = m_buffer.readRecord();
            if( null == rec )
            {
                // Unexpected EOF!
                final String message =
                    "unexpected EOF with " + numToRead + " bytes unread";
                throw new IOException( message );
            }

            int size = numToRead;
            final int recordLength = rec.length;

            if( recordLength > size )
            {
                System.arraycopy( rec, 0, buffer, position, size );

                m_readBuf = new byte[ recordLength - size ];

                System.arraycopy( rec, size, m_readBuf, 0, recordLength - size );
            }
            else
            {
                size = recordLength;

                System.arraycopy( rec, 0, buffer, position, recordLength );
            }

            totalRead += size;
            numToRead -= size;
            position += size;
        }

        m_entryOffset += totalRead;

        return totalRead;
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     */
    public void reset()
    {
    }

    /**
     * Skip bytes in the input buffer. This skips bytes in the current entry's
     * data, not the entire archive, and will stop at the end of the current
     * entry's data if the number to skip extends beyond that point.
     *
     * @param numToSkip The number of bytes to skip.
     * @exception IOException when an IO error causes operation to fail
     */
    public void skip( final int numToSkip )
        throws IOException
    {
        // REVIEW
        // This is horribly inefficient, but it ensures that we
        // properly skip over bytes via the TarBuffer...
        //
        final byte[] skipBuf = new byte[ 8 * 1024 ];
        int num = numToSkip;
        while( num > 0 )
        {
            final int count = ( num > skipBuf.length ) ? skipBuf.length : num;
            final int numRead = read( skipBuf, 0, count );
            if( numRead == -1 )
            {
                break;
            }

            num -= numRead;
        }
    }

    /**
     * Utility method to do debugging.
     * Capable of being overidden in sub-classes.
     *
     * @param message the message to use in debugging
     */
    protected void debug( final String message )
    {
        if( m_debug )
        {
            System.err.println( message );
        }
    }
}
