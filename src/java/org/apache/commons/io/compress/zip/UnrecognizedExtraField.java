/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/UnrecognizedExtraField.java,v 1.3 2003/10/13 07:03:30 rdonkin Exp $
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
 * Simple placeholder for all those extra fields we don't want to deal with. <p>
 *
 * Assumes local file data and central directory entries are identical - unless
 * told the opposite.</p>
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.3 $
 */
public class UnrecognizedExtraField
    implements ZipExtraField
{
    /**
     * Extra field data in central directory - without Header-ID or length
     * specifier.
     *
     * @since 1.1
     */
    private byte[] m_centralData;

    /**
     * The Header-ID.
     *
     * @since 1.1
     */
    private ZipShort m_headerID;

    /**
     * Extra field data in local file data - without Header-ID or length
     * specifier.
     *
     * @since 1.1
     */
    private byte[] m_localData;

    /**
     * Set the central directory data
     *
     * @param centralData the central directory data
     */
    public void setCentralDirectoryData( final byte[] centralData )
    {
        m_centralData = centralData;
    }

    /**
     * Set the header ID.
     *
     * @param headerID the header ID
     * @deprecated Use setHeaderID() instead
     * @see #setHeaderID(ZipShort)
     */
    public void setHeaderId( final ZipShort headerID )
    {
        setHeaderID( headerID );
    }

    /**
     * Set the header ID.
     *
     * @param headerID the header ID
     */
    public void setHeaderID( final ZipShort headerID )
    {
        m_headerID = headerID;
    }

    /**
     * Set the local file data.
     *
     * @param localData the local file data
     */
    public void setLocalFileDataData( final byte[] localData )
    {
        m_localData = localData;
    }

    /**
     * Get the central directory data.
     *
     * @return the central directory data.
     */
    public byte[] getCentralDirectoryData()
    {
        if( m_centralData != null )
        {
            return m_centralData;
        }
        return getLocalFileDataData();
    }

    /**
     * Get the length of the central directory in bytes.
     *
     * @return the length of the central directory in bytes.
     */
    public ZipShort getCentralDirectoryLength()
    {
        if( m_centralData != null )
        {
            return new ZipShort( m_centralData.length );
        }
        return getLocalFileDataLength();
    }

    /**
     * Get the HeaderID.
     *
     * @return the HeaderID
     * @deprecated Use getHeaderID() instead
     * @see #getHeaderID()
     */
    public ZipShort getHeaderId()
    {
        return m_headerID;
    }

    /**
     * Get the HeaderID.
     *
     * @return the HeaderID
     */
    public ZipShort getHeaderID()
    {
        return m_headerID;
    }

    /**
     * Get the local file data.
     *
     * @return the local file data
     */
    public byte[] getLocalFileDataData()
    {
        return m_localData;
    }

    /**
     * Get the length of local file data in bytes.
     *
     * @return the length of local file data in bytes
     */
    public ZipShort getLocalFileDataLength()
    {
        return new ZipShort( m_localData.length );
    }

    /**
     * Parse LocalFiledata out of supplied buffer.
     *
     * @param buffer the buffer to use
     * @param offset the offset into buffer
     * @param length then length of data
     */
    public void parseFromLocalFileData( final byte[] buffer,
                                        final int offset,
                                        final int length )
    {
        final byte[] fileData = new byte[ length ];
        System.arraycopy( buffer, offset, fileData, 0, length );
        setLocalFileDataData( fileData );
    }
}
