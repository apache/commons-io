package org.apache.commons.io;

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility code for dealing with different endian systems.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version CVS $Revision: 1.1 $ $Date: 2002/07/08 22:14:46 $
 * @since 4.0
 */
public final class EndianUtil
{
    public static final int SIZEOF_BYTE = 1;
    public static final int SIZEOF_SHORT = 2;
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_LONG = 8;

    public static short swapShort( final short value )
    {
        return (short)( ( ( ( value >> 0 ) & 0xff ) << 8 ) +
            ( ( ( value >> 8 ) & 0xff ) << 0 ) );
    }

    public static int swapInteger( final int value )
    {
        return
            ( ( ( value >> 0 ) & 0xff ) << 24 ) +
            ( ( ( value >> 8 ) & 0xff ) << 16 ) +
            ( ( ( value >> 16 ) & 0xff ) << 8 ) +
            ( ( ( value >> 24 ) & 0xff ) << 0 );
    }

    public static long swapLong( final long value )
    {
        return
            ( ( ( value >> 0 ) & 0xff ) << 56 ) +
            ( ( ( value >> 8 ) & 0xff ) << 48 ) +
            ( ( ( value >> 16 ) & 0xff ) << 40 ) +
            ( ( ( value >> 24 ) & 0xff ) << 32 ) +
            ( ( ( value >> 32 ) & 0xff ) << 24 ) +
            ( ( ( value >> 40 ) & 0xff ) << 16 ) +
            ( ( ( value >> 48 ) & 0xff ) << 8 ) +
            ( ( ( value >> 56 ) & 0xff ) << 0 );
    }

    public static float swapFloat( final float value )
    {
        return Float.intBitsToFloat( swapInteger( Float.floatToIntBits( value ) ) );
    }

    public static double swapDouble( final double value )
    {
        return Double.longBitsToDouble( swapLong( Double.doubleToLongBits( value ) ) );
    }

    public static void writeSwappedShort( final byte[] data, final int offset, final int value )
    {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
    }

    public static short readSwappedShort( final byte[] data, final int offset )
    {
        return (short)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }

    public static int readSwappedUnsignedShort( final byte[] data, final int offset )
    {
        return (int)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }

    public static void writeSwappedInteger( final byte[] data, final int offset, final int value )
    {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
        data[ offset + 2 ] = (byte)( ( value >> 16 ) & 0xff );
        data[ offset + 3 ] = (byte)( ( value >> 24 ) & 0xff );
    }

    public static int readSwappedInteger( final byte[] data, final int offset )
    {
        return (int)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
            ( ( data[ offset + 2 ] & 0xff ) << 16 ) +
            ( ( data[ offset + 3 ] & 0xff ) << 24 ) );
    }

    public static long readSwappedUnsignedInteger( final byte[] data, final int offset )
    {
        return (long)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
            ( ( data[ offset + 2 ] & 0xff ) << 16 ) +
            ( ( data[ offset + 3 ] & 0xff ) << 24 ) );
    }

    public static void writeSwappedLong( final byte[] data, final int offset, final long value )
    {
        data[ offset + 0 ] = (byte)( ( value >> 0 ) & 0xff );
        data[ offset + 1 ] = (byte)( ( value >> 8 ) & 0xff );
        data[ offset + 2 ] = (byte)( ( value >> 16 ) & 0xff );
        data[ offset + 3 ] = (byte)( ( value >> 24 ) & 0xff );
        data[ offset + 4 ] = (byte)( ( value >> 32 ) & 0xff );
        data[ offset + 5 ] = (byte)( ( value >> 40 ) & 0xff );
        data[ offset + 6 ] = (byte)( ( value >> 48 ) & 0xff );
        data[ offset + 7 ] = (byte)( ( value >> 56 ) & 0xff );
    }

    public static long readSwappedLong( final byte[] data, final int offset )
    {
        return (long)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
            ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
            ( ( data[ offset + 2 ] & 0xff ) << 16 ) +
            ( ( data[ offset + 3 ] & 0xff ) << 24 ) +
            ( ( data[ offset + 4 ] & 0xff ) << 32 ) +
            ( ( data[ offset + 5 ] & 0xff ) << 40 ) +
            ( ( data[ offset + 6 ] & 0xff ) << 48 ) +
            ( ( data[ offset + 7 ] & 0xff ) << 56 ) );
    }

    public static void writeSwappedFloat( final byte[] data, final int offset, final float value )
    {
        writeSwappedInteger( data, offset, Float.floatToIntBits( value ) );
    }

    public static float readSwappedFloat( final byte[] data, final int offset )
    {
        return Float.intBitsToFloat( readSwappedInteger( data, offset ) );
    }

    public static void writeSwappedDouble( final byte[] data, final int offset, final double value )
    {
        writeSwappedLong( data, offset, Double.doubleToLongBits( value ) );
    }

    public static double readSwappedDouble( final byte[] data, final int offset )
    {
        return Double.longBitsToDouble( readSwappedLong( data, offset ) );
    }

    //////////////////////////////////////////////////////////////////////
    //
    //  The following haven't been fully tested yet - unit tests coming soon!!!
    //
    //////////////////////////////////////////////////////////////////////
    public static void writeSwappedShort( final OutputStream output, final int value )
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
    }

    public static short readSwappedShort( final InputStream input )
        throws IOException
    {
        return (short)( ( ( read( input ) & 0xff ) << 0 ) +
            ( ( read( input ) & 0xff ) << 8 ) );
    }

    public static int readSwappedUnsignedShort( final InputStream input )
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );

        return (int)( ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) );
    }

    public static void writeSwappedInteger( final OutputStream output, final int value )
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
        output.write( (byte)( ( value >> 16 ) & 0xff ) );
        output.write( (byte)( ( value >> 24 ) & 0xff ) );
    }

    public static int readSwappedInteger( final InputStream input )
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );
        final int value3 = read( input );
        final int value4 = read( input );

        return (int)( ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) +
            ( ( value3 & 0xff ) << 16 ) +
            ( ( value4 & 0xff ) << 24 ) );
    }

    public static long readSwappedUnsignedInteger( final InputStream input )
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );
        final int value3 = read( input );
        final int value4 = read( input );

        return (long)( ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) +
            ( ( value3 & 0xff ) << 16 ) +
            ( ( value4 & 0xff ) << 24 ) );
    }

    public static void writeSwappedLong( final OutputStream output, final long value )
        throws IOException
    {
        output.write( (byte)( ( value >> 0 ) & 0xff ) );
        output.write( (byte)( ( value >> 8 ) & 0xff ) );
        output.write( (byte)( ( value >> 16 ) & 0xff ) );
        output.write( (byte)( ( value >> 24 ) & 0xff ) );
        output.write( (byte)( ( value >> 32 ) & 0xff ) );
        output.write( (byte)( ( value >> 40 ) & 0xff ) );
        output.write( (byte)( ( value >> 48 ) & 0xff ) );
        output.write( (byte)( ( value >> 56 ) & 0xff ) );
    }

    public static long readSwappedLong( final InputStream input )
        throws IOException
    {
        final int value1 = read( input );
        final int value2 = read( input );
        final int value3 = read( input );
        final int value4 = read( input );
        final int value5 = read( input );
        final int value6 = read( input );
        final int value7 = read( input );
        final int value8 = read( input );

        return (long)( ( ( value1 & 0xff ) << 0 ) +
            ( ( value2 & 0xff ) << 8 ) +
            ( ( value3 & 0xff ) << 16 ) +
            ( ( value4 & 0xff ) << 24 ) +
            ( ( value5 & 0xff ) << 32 ) +
            ( ( value6 & 0xff ) << 40 ) +
            ( ( value7 & 0xff ) << 48 ) +
            ( ( value8 & 0xff ) << 56 ) );
    }

    public static void writeSwappedFloat( final OutputStream output, final float value )
        throws IOException
    {
        writeSwappedInteger( output, Float.floatToIntBits( value ) );
    }

    public static float readSwappedFloat( final InputStream input )
        throws IOException
    {
        return Float.intBitsToFloat( readSwappedInteger( input ) );
    }

    public static void writeSwappedDouble( final OutputStream output, final double value )
        throws IOException
    {
        writeSwappedLong( output, Double.doubleToLongBits( value ) );
    }

    public static double readSwappedDouble( final InputStream input )
        throws IOException
    {
        return Double.longBitsToDouble( readSwappedLong( input ) );
    }

    private static int read( final InputStream input )
        throws IOException
    {
        final int value = input.read();

        if( -1 == value )
        {
            throw new EOFException( "Unexpected EOF reached" );
        }

        return value;
    }
}
