/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.EndianUtils;

/**
 * DataInput for systems relying on little endian data formats.
 * When read, values will be changed from little endian to big 
 * endian formats for internal usage. 
 * <p>
 * <b>Origin of code: </b>Avalon Excalibur (IO)
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version CVS $Revision$ $Date$
 */
public class SwappedDataInputStream extends ProxyInputStream
    implements DataInput
{

    /**
     * Constructs a SwappedDataInputStream.
     *
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

    /**
     * Not currently supported.
     *
     * @see java.io.DataInput#readLine()
     */
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

    /**
     * Not currently supported. 
     *
     * @see java.io.DataInput#readUTF() 
     */
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
