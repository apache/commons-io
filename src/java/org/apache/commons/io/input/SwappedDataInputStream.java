package org.apache.commons.io.input;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.EndianUtils;

/**
 * DataInput for systems relying on little endian data formats.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version CVS $Revision: 1.2 $ $Date: 2003/07/25 07:51:26 $
 * @since 4.0
 */
public class SwappedDataInputStream
    implements DataInput
{
    //The underlying input stream
    private InputStream m_input;

    public SwappedDataInputStream( final InputStream input )
    {
        m_input = input;
    }

    public boolean readBoolean()
        throws IOException, EOFException
    {
        return ( 0 == readByte() );
    }

    public byte readByte()
        throws IOException, EOFException
    {
        return (byte)m_input.read();
    }

    public char readChar()
        throws IOException, EOFException
    {
        return (char)readShort();
    }

    public double readDouble()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedDouble( m_input );
    }

    public float readFloat()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedFloat( m_input );
    }

    public void readFully( final byte[] data )
        throws IOException, EOFException
    {
        readFully( data, 0, data.length );
    }

    public void readFully( final byte[] data, final int offset, final int length )
        throws IOException, EOFException
    {
        int remaining = length;

        while( remaining > 0 )
        {
            final int location = offset + ( length - remaining );
            final int count = read( data, location, remaining );

            if( -1 == count )
            {
                throw new EOFException();
            }

            remaining -= count;
        }
    }

    public int readInt()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedInteger( m_input );
    }

    public String readLine()
        throws IOException, EOFException
    {
        throw new IOException( "Operation not supported" );
    }

    public long readLong()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedLong( m_input );
    }

    public short readShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedShort( m_input );
    }

    public int readUnsignedByte()
        throws IOException, EOFException
    {
        return m_input.read();
    }

    public int readUnsignedShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedUnsignedShort( m_input );
    }

    public String readUTF()
        throws IOException, EOFException
    {
        throw new IOException( "Operation not supported" );
    }

    public int skipBytes( final int count )
        throws IOException, EOFException
    {
        return (int)m_input.skip( count );
    }

    public int available()
        throws IOException, EOFException
    {
        return m_input.available();
    }

    public void close()
        throws IOException, EOFException
    {
        m_input.close();
    }

    public int read()
        throws IOException, EOFException
    {
        return m_input.read();
    }

    public int read( final byte[] data )
        throws IOException, EOFException
    {
        return read( data, 0, data.length );
    }

    public int read( final byte[] data, final int offset, final int length )
        throws IOException, EOFException
    {
        return m_input.read( data, offset, length );
    }

    public long skip( final long count )
        throws IOException, EOFException
    {
        return m_input.skip( count );
    }

    public void mark( final int readLimit )
    {
        m_input.mark( readLimit );
    }

    public boolean markSupported()
    {
        return m_input.markSupported();
    }

    public void reset()
        throws IOException
    {
        m_input.reset();
    }
}
