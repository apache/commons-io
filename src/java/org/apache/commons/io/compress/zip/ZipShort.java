/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/ZipShort.java,v 1.3 2003/10/13 07:03:30 rdonkin Exp $
 * $Revision: 1.3 $
 * $Date: 2003/10/13 07:03:30 $
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
 *
 */
package org.apache.commons.io.compress.zip;

/**
 * Utility class that represents a two byte integer with conversion rules for
 * the big endian byte order of ZIP files.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.3 $
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
