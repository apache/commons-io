/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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
package org.apache.commons.io.input;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.EndianUtils;

/**
 * DataInput for systems relying on little endian data formats.
 *
 * <p><b>Origin of code: </b>Avalon Excalibur (IO)</p>
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version CVS $Revision: 1.6 $ $Date: 2003/12/30 06:56:22 $
 */
public class SwappedDataInputStream extends ProxyInputStream
    implements DataInput
{

    /**
     * Constructs a SwappedDataInputStream.
     * @param input InputStream to read from
     */
    public SwappedDataInputStream( InputStream input )
    {
        super( input );
    }

    /** @see java.io.DataInput#readBoolean() */
    public boolean readBoolean()
        throws IOException, EOFException
    {
        return ( 0 == readByte() );
    }

    /** @see java.io.DataInput#readByte() */
    public byte readByte()
        throws IOException, EOFException
    {
        return (byte)in.read();
    }

    /** @see java.io.DataInput#readChar() */
    public char readChar()
        throws IOException, EOFException
    {
        return (char)readShort();
    }

    /** @see java.io.DataInput#readDouble() */
    public double readDouble()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedDouble( in );
    }

    /** @see java.io.DataInput#readFloat() */
    public float readFloat()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedFloat( in );
    }

    /** @see java.io.DataInput#readFully(byte[]) */
    public void readFully( byte[] data )
        throws IOException, EOFException
    {
        readFully( data, 0, data.length );
    }

    /** @see java.io.DataInput#readFully(byte[], int, int) */
    public void readFully( byte[] data, int offset, int length )
        throws IOException, EOFException
    {
        int remaining = length;

        while( remaining > 0 )
        {
            int location = offset + ( length - remaining );
            int count = read( data, location, remaining );

            if( -1 == count )
            {
                throw new EOFException();
            }

            remaining -= count;
        }
    }

    /** @see java.io.DataInput#readInt() */
    public int readInt()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedInteger( in );
    }

    /** @see java.io.DataInput#readLine() */
    public String readLine()
        throws IOException, EOFException
    {
        throw new UnsupportedOperationException( 
                "Operation not supported: readLine()" );
    }

    /** @see java.io.DataInput#readLong() */
    public long readLong()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedLong( in );
    }

    /** @see java.io.DataInput#readShort() */
    public short readShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedShort( in );
    }

    /** @see java.io.DataInput#readUnsignedByte() */
    public int readUnsignedByte()
        throws IOException, EOFException
    {
        return in.read();
    }

    /** @see java.io.DataInput#readUnsignedShort() */
    public int readUnsignedShort()
        throws IOException, EOFException
    {
        return EndianUtils.readSwappedUnsignedShort( in );
    }

    /** @see java.io.DataInput#readUTF() */
    public String readUTF()
        throws IOException, EOFException
    {
        throw new UnsupportedOperationException( 
                "Operation not supported: readUTF()" );
    }

    /** @see java.io.DataInput#skipBytes(int) */
    public int skipBytes( int count )
        throws IOException, EOFException
    {
        return (int)in.skip( count );
    }

}
