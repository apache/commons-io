/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/compress/zip/Attic/AsiExtraField.java,v 1.2 2002/07/13 22:37:46 nicolaken Exp $
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
package org.apache.commons.io.compress.zip;

import java.util.zip.CRC32;
import java.util.zip.ZipException;

/**
 * Adds Unix file permission and UID/GID fields as well as symbolic link
 * handling. <p>
 *
 * This class uses the ASi extra field in the format: <pre>
 *         Value         Size            Description
 *         -----         ----            -----------
 * (Unix3) 0x756e        Short           tag for this extra block type
 *         TSize         Short           total data size for this block
 *         CRC           Long            CRC-32 of the remaining data
 *         Mode          Short           file permissions
 *         SizDev        Long            symlink'd size OR major/minor dev num
 *         UID           Short           user ID
 *         GID           Short           group ID
 *         (var.)        variable        symbolic link filename
 * </pre> taken from appnote.iz (Info-ZIP note, 981119) found at <a
 * href="ftp://ftp.uu.net/pub/archiving/zip/doc/">
 * ftp://ftp.uu.net/pub/archiving/zip/doc/</a> </p> <p>
 *
 * Short is two bytes and Long is four bytes in big endian byte and word order,
 * device numbers are currently not supported.</p>
 *
 * @author <a href="stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision: 1.2 $
 */
public class AsiExtraField
    implements ZipExtraField, UnixStat, Cloneable
{
    private static final ZipShort HEADER_ID = new ZipShort( 0x756E );

    /**
     * Standard Unix stat(2) file mode.
     *
     * @since 1.1
     */
    private int m_mode;

    /**
     * User ID.
     *
     * @since 1.1
     */
    private int m_uid;

    /**
     * Group ID.
     *
     * @since 1.1
     */
    private int m_gid;

    /**
     * File this entry points to, if it is a symbolic link. <p>
     *
     * empty string - if entry is not a symbolic link.</p>
     *
     * @since 1.1
     */
    private String m_link = "";

    /**
     * Is this an entry for a directory?
     *
     * @since 1.1
     */
    private boolean m_dirFlag;

    /**
     * Instance used to calculate checksums.
     *
     * @since 1.1
     */
    private CRC32 m_crc = new CRC32();

    /**
     * Indicate whether this entry is a directory.
     *
     * @param dirFlag The new Directory value
     * @since 1.1
     */
    public void setDirectory( final boolean dirFlag )
    {
        m_dirFlag = dirFlag;
        m_mode = getMode( m_mode );
    }

    /**
     * Set the group id.
     *
     * @param gid The new GroupId value
     * @since 1.1
     */
    public void setGroupId( int gid )
    {
        m_gid = gid;
    }

    /**
     * Indicate that this entry is a symbolic link to the given filename.
     *
     * @param name Name of the file this entry links to, empty String if it is
     *      not a symbolic link.
     * @since 1.1
     */
    public void setLinkedFile( final String name )
    {
        m_link = name;
        m_mode = getMode( m_mode );
    }

    /**
     * File mode of this file.
     *
     * @param mode The new Mode value
     * @since 1.1
     */
    public void setMode( final int mode )
    {
        m_mode = getMode( mode );
    }

    /**
     * Set the user id.
     *
     * @param uid The new UserId value
     * @since 1.1
     * @deprecated Use setUserID(int)
     * @see #setUserID(int)
     */
    public void setUserId( final int uid )
    {
        m_uid = uid;
    }

    /**
     * Set the user id.
     *
     * @param uid The new UserId value
     */
    public void setUserID( final int uid )
    {
        m_uid = uid;
    }

    /**
     * Delegate to local file data.
     *
     * @return The CentralDirectoryData value
     * @since 1.1
     */
    public byte[] getCentralDirectoryData()
    {
        return getLocalFileDataData();
    }

    /**
     * Delegate to local file data.
     *
     * @return The CentralDirectoryLength value
     * @since 1.1
     */
    public ZipShort getCentralDirectoryLength()
    {
        return getLocalFileDataLength();
    }

    /**
     * Get the group id.
     *
     * @return The GroupId value
     * @since 1.1
     */
    public int getGroupID()
    {
        return m_gid;
    }

    /**
     * Get the group id.
     *
     * @return The GroupId value
     * @since 1.1
     * @deprecated Use getGroupID() instead
     * @see #getGroupID()
     */
    public int getGroupId()
    {
        return m_gid;
    }

    /**
     * The Header-ID.
     *
     * @return The HeaderId value
     * @since 1.1
     */
    public ZipShort getHeaderID()
    {
        return HEADER_ID;
    }

    /**
     * Name of linked file
     *
     * @return name of the file this entry links to if it is a symbolic link,
     *      the empty string otherwise.
     * @since 1.1
     */
    public String getLinkedFile()
    {
        return m_link;
    }

    /**
     * The actual data to put into local file data - without Header-ID or length
     * specifier.
     *
     * @return The LocalFileDataData value
     * @since 1.1
     */
    public byte[] getLocalFileDataData()
    {
        // CRC will be added later
        byte[] data = new byte[ getLocalFileDataLength().getValue() - 4 ];
        System.arraycopy( ( new ZipShort( getMode() ) ).getBytes(), 0, data, 0, 2 );

        byte[] linkArray = getLinkedFile().getBytes();
        System.arraycopy( ( new ZipLong( linkArray.length ) ).getBytes(),
                          0, data, 2, 4 );

        System.arraycopy( ( new ZipShort( getUserID() ) ).getBytes(),
                          0, data, 6, 2 );
        System.arraycopy( ( new ZipShort( getGroupID() ) ).getBytes(),
                          0, data, 8, 2 );

        System.arraycopy( linkArray, 0, data, 10, linkArray.length );

        m_crc.reset();
        m_crc.update( data );
        long checksum = m_crc.getValue();

        byte[] result = new byte[ data.length + 4 ];
        System.arraycopy( ( new ZipLong( checksum ) ).getBytes(), 0, result, 0, 4 );
        System.arraycopy( data, 0, result, 4, data.length );
        return result;
    }

    /**
     * Length of the extra field in the local file data - without Header-ID or
     * length specifier.
     *
     * @return The LocalFileDataLength value
     * @since 1.1
     */
    public ZipShort getLocalFileDataLength()
    {
        return new ZipShort( 4 + // CRC
                             2 + // Mode
                             4 + // SizDev
                             2 + // UID
                             2 + // GID
                             getLinkedFile().getBytes().length );
    }

    /**
     * File mode of this file.
     *
     * @return The Mode value
     * @since 1.1
     */
    public int getMode()
    {
        return m_mode;
    }

    /**
     * Get the user id.
     *
     * @return The UserId value
     * @since 1.1
     * @deprecated Use getUserID()
     * @see #getUserID()
     */
    public int getUserId()
    {
        return m_uid;
    }

    /**
     * Get the user id.
     *
     * @return The UserID value
     */
    public int getUserID()
    {
        return m_uid;
    }

    /**
     * Is this entry a directory?
     *
     * @return The Directory value
     * @since 1.1
     */
    public boolean isDirectory()
    {
        return m_dirFlag && !isLink();
    }

    /**
     * Is this entry a symbolic link?
     *
     * @return The Link value
     * @since 1.1
     */
    public boolean isLink()
    {
        return getLinkedFile().length() != 0;
    }

    /**
     * Populate data from this array as if it was in local file data.
     *
     * @param buffer the buffer
     * @param offset the offset into buffer
     * @param length the length of data in buffer
     * @throws ZipException on error
     * @since 1.1
     */
    public void parseFromLocalFileData( final byte[] buffer,
                                        final int offset,
                                        final int length )
        throws ZipException
    {

        long givenChecksum = ( new ZipLong( buffer, offset ) ).getValue();
        byte[] tmp = new byte[ length - 4 ];
        System.arraycopy( buffer, offset + 4, tmp, 0, length - 4 );
        m_crc.reset();
        m_crc.update( tmp );
        long realChecksum = m_crc.getValue();
        if( givenChecksum != realChecksum )
        {
            throw new ZipException( "bad CRC checksum " + Long.toHexString( givenChecksum ) +
                                    " instead of " + Long.toHexString( realChecksum ) );
        }

        int newMode = ( new ZipShort( tmp, 0 ) ).getValue();
        byte[] linkArray = new byte[ (int)( new ZipLong( tmp, 2 ) ).getValue() ];
        m_uid = ( new ZipShort( tmp, 6 ) ).getValue();
        m_gid = ( new ZipShort( tmp, 8 ) ).getValue();

        if( linkArray.length == 0 )
        {
            m_link = "";
        }
        else
        {
            System.arraycopy( tmp, 10, linkArray, 0, linkArray.length );
            m_link = new String( linkArray );
        }
        setDirectory( ( newMode & DIR_FLAG ) != 0 );
        setMode( newMode );
    }

    /**
     * Get the file mode for given permissions with the correct file type.
     *
     * @param mode Description of Parameter
     * @return The Mode value
     * @since 1.1
     */
    protected int getMode( final int mode )
    {
        int type = FILE_FLAG;
        if( isLink() )
        {
            type = LINK_FLAG;
        }
        else if( isDirectory() )
        {
            type = DIR_FLAG;
        }
        return type | ( mode & PERM_MASK );
    }
}
