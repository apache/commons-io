/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.zip;

/**
 * Simple placeholder for all those extra fields we don't want to deal with. <p>
 *
 * Assumes local file data and central directory entries are identical - unless
 * told the opposite.</p>
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.1 $
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
