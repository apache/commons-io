/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

/**
 * Utility class that represents a two byte integer with conversion rules for
 * the big endian byte order of ZIP files.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.1 $
 */
public final class ZipShort
    implements Cloneable
{
    private int m_value;

    /**
     * Create instance from a number.
     *
     * @param value Description of Parameter
     * @since 1.1
     */
    public ZipShort( int value )
    {
        this.m_value = value;
    }

    /**
     * Create instance from bytes.
     *
     * @param bytes Description of Parameter
     * @since 1.1
     */
    public ZipShort( byte[] bytes )
    {
        this( bytes, 0 );
    }

    /**
     * Create instance from the two bytes starting at offset.
     *
     * @param bytes Description of Parameter
     * @param offset Description of Parameter
     * @since 1.1
     */
    public ZipShort( byte[] bytes, int offset )
    {
        m_value = ( bytes[ offset + 1 ] << 8 ) & 0xFF00;
        m_value += ( bytes[ offset ] & 0xFF );
    }

    /**
     * Get value as two bytes in big endian byte order.
     *
     * @return The Bytes value
     * @since 1.1
     */
    public byte[] getBytes()
    {
        byte[] result = new byte[ 2 ];
        result[ 0 ] = (byte)( m_value & 0xFF );
        result[ 1 ] = (byte)( ( m_value & 0xFF00 ) >> 8 );
        return result;
    }

    /**
     * Get value as Java int.
     *
     * @return The Value value
     * @since 1.1
     */
    public int getValue()
    {
        return m_value;
    }

    /**
     * Override to make two instances with same value equal.
     *
     * @param o Description of Parameter
     * @return Description of the Returned Value
     * @since 1.1
     */
    public boolean equals( Object o )
    {
        if( o == null || !( o instanceof ZipShort ) )
        {
            return false;
        }
        return m_value == ( (ZipShort)o ).getValue();
    }

    /**
     * Override to make two instances with same value equal.
     *
     * @return Description of the Returned Value
     * @since 1.1
     */
    public int hashCode()
    {
        return m_value;
    }
}
