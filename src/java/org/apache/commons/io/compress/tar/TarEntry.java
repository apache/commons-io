/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.tar;

import java.io.File;
import java.util.Date;
import java.util.Locale;

/**
 * This class represents an entry in a Tar archive. It consists of the entry's
 * header, as well as the entry's File. Entries can be instantiated in one of
 * three ways, depending on how they are to be used. <p>
 *
 * TarEntries that are created from the header bytes read from an archive are
 * instantiated with the TarEntry( byte[] ) constructor. These entries will be
 * used when extracting from or listing the contents of an archive. These
 * entries have their header filled in using the header bytes. They also set the
 * File to null, since they reference an archive entry not a file. <p>
 *
 * TarEntries that are created from Files that are to be written into an archive
 * are instantiated with the TarEntry( File ) constructor. These entries have
 * their header filled in using the File's information. They also keep a
 * reference to the File for convenience when writing entries. <p>
 *
 * Finally, TarEntries can be constructed from nothing but a name. This allows
 * the programmer to construct the entry by hand, for instance when only an
 * InputStream is available for writing to the archive, and the header
 * information is constructed from other information. In this case the header
 * fields are set to defaults and the File is set to null. <p>
 *
 * The C structure for a Tar Entry's header is: <pre>
 * struct header {
 * char name[NAMSIZ];
 * char mode[8];
 * char uid[8];
 * char gid[8];
 * char size[12];
 * char mtime[12];
 * char chksum[8];
 * char linkflag;
 * char linkname[NAMSIZ];
 * char magic[8];
 * char uname[TUNMLEN];
 * char gname[TGNMLEN];
 * char devmajor[8];
 * char devminor[8];
 * } header;
 * </pre>
 *
 * @author <a href="mailto:time@ice.com">Timothy Gerard Endres</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/08 22:16:08 $
 * @see TarInputStream
 * @see TarOutputStream
 */
public class TarEntry
{
    /**
     * The length of the name field in a header buffer.
     */
    public static final int NAMELEN = 100;

    /**
     * The entry's modification time.
     */
    private int m_checkSum;

    /**
     * The entry's group name.
     */
    private int m_devMajor;

    /**
     * The entry's major device number.
     */
    private int m_devMinor;

    /**
     * The entry's minor device number.
     */
    private File m_file;

    /**
     * The entry's user id.
     */
    private int m_groupID;

    /**
     * The entry's user name.
     */
    private StringBuffer m_groupName;

    /**
     * The entry's checksum.
     */
    private byte m_linkFlag;

    /**
     * The entry's link flag.
     */
    private StringBuffer m_linkName;

    /**
     * The entry's link name.
     */
    private StringBuffer m_magic;

    /**
     * The entry's size.
     */
    private long m_modTime;

    /**
     * The entry's name.
     */
    private int m_mode;

    private StringBuffer m_name;

    /**
     * The entry's group id.
     */
    private long m_size;

    /**
     * The entry's permission mode.
     */
    private int m_userID;

    /**
     * The entry's magic tag.
     */
    private StringBuffer m_userName;

    /**
     * Construct an entry with only a name. This allows the programmer to
     * construct the entry's header "by hand". File is set to null.
     *
     * @param name the name of the entry
     */
    public TarEntry( final String name )
    {
        this();

        final boolean isDir = name.endsWith( "/" );

        m_name = new StringBuffer( name );
        m_mode = isDir ? 040755 : 0100644;
        m_linkFlag = isDir ? TarConstants.LF_DIR : TarConstants.LF_NORMAL;
        m_modTime = ( new Date() ).getTime() / 1000;
        m_linkName = new StringBuffer( "" );
        m_userName = new StringBuffer( "" );
        m_groupName = new StringBuffer( "" );
    }

    /**
     * Construct an entry with a name an a link flag.
     *
     * @param name Description of Parameter
     * @param linkFlag Description of Parameter
     */
    public TarEntry( final String name, final byte linkFlag )
    {
        this( name );
        m_linkFlag = linkFlag;
    }

    /**
     * Construct an entry for a file. File is set to file, and the header is
     * constructed from information from the file.
     *
     * @param file The file that the entry represents.
     */
    public TarEntry( final File file )
    {
        this();

        m_file = file;

        String name = file.getPath();

        // Strip off drive letters!
        final String osName =
            System.getProperty( "os.name" ).toLowerCase( Locale.US );
        if( -1 != osName.indexOf( "netware" ) )
        {
            if( name.length() > 2 )
            {
                final char ch1 = name.charAt( 0 );
                final char ch2 = name.charAt( 1 );

                if( ch2 == ':' &&
                    ( ( ch1 >= 'a' && ch1 <= 'z' ) ||
                    ( ch1 >= 'A' && ch1 <= 'Z' ) ) )
                {
                    name = name.substring( 2 );
                }
            }
        }
        else if( -1 != osName.indexOf( "netware" ) )
        {
            final int colon = name.indexOf( ':' );
            if( colon != -1 )
            {
                name = name.substring( colon + 1 );
            }
        }

        name = name.replace( File.separatorChar, '/' );

        // No absolute pathnames
        // Windows (and Posix?) paths can start with "\\NetworkDrive\",
        // so we loop on starting /'s.
        while( name.startsWith( "/" ) )
        {
            name = name.substring( 1 );
        }

        m_linkName = new StringBuffer( "" );
        m_name = new StringBuffer( name );

        if( file.isDirectory() )
        {
            m_mode = 040755;
            m_linkFlag = TarConstants.LF_DIR;

            if( m_name.charAt( m_name.length() - 1 ) != '/' )
            {
                m_name.append( "/" );
            }
        }
        else
        {
            m_mode = 0100644;
            m_linkFlag = TarConstants.LF_NORMAL;
        }

        m_size = file.length();
        m_modTime = file.lastModified() / 1000;
        m_checkSum = 0;
        m_devMajor = 0;
        m_devMinor = 0;
    }

    /**
     * Construct an entry from an archive's header bytes. File is set to null.
     *
     * @param header The header bytes from a tar archive entry.
     */
    public TarEntry( final byte[] header )
    {
        this();
        parseTarHeader( header );
    }

    /**
     * Construct an empty entry and prepares the header values.
     */
    private TarEntry()
    {
        m_magic = new StringBuffer( TarConstants.TMAGIC );
        m_name = new StringBuffer();
        m_linkName = new StringBuffer();

        String user = System.getProperty( "user.name", "" );
        if( user.length() > 31 )
        {
            user = user.substring( 0, 31 );
        }

        m_userName = new StringBuffer( user );
        m_groupName = new StringBuffer( "" );
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     */
    public void setGroupID( final int groupId )
    {
        m_groupID = groupId;
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     * @deprecated Use setGroupID() instead
     * @see #setGroupID(int)
     */
    public void setGroupId( final int groupId )
    {
        m_groupID = groupId;
    }

    /**
     * Set this entry's group name.
     *
     * @param groupName This entry's new group name.
     */
    public void setGroupName( final String groupName )
    {
        m_groupName = new StringBuffer( groupName );
    }

    /**
     * Set this entry's modification time. The parameter passed to this method
     * is in "Java time".
     *
     * @param time This entry's new modification time.
     */
    public void setModTime( final long time )
    {
        m_modTime = time / 1000;
    }

    /**
     * Set this entry's modification time.
     *
     * @param time This entry's new modification time.
     */
    public void setModTime( final Date time )
    {
        m_modTime = time.getTime() / 1000;
    }

    /**
     * Set the mode for this entry
     *
     * @param mode The new Mode value
     */
    public void setMode( final int mode )
    {
        m_mode = mode;
    }

    /**
     * Set this entry's name.
     *
     * @param name This entry's new name.
     */
    public void setName( final String name )
    {
        m_name = new StringBuffer( name );
    }

    /**
     * Set this entry's file size.
     *
     * @param size This entry's new file size.
     */
    public void setSize( final long size )
    {
        m_size = size;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     */
    public void setUserID( final int userId )
    {
        m_userID = userId;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     * @deprecated Use setUserID() instead
     * @see #setUserID(int)
     */
    public void setUserId( final int userId )
    {
        m_userID = userId;
    }

    /**
     * Set this entry's user name.
     *
     * @param userName This entry's new user name.
     */
    public void setUserName( final String userName )
    {
        m_userName = new StringBuffer( userName );
    }

    /**
     * If this entry represents a file, and the file is a directory, return an
     * array of TarEntries for this entry's children.
     *
     * @return An array of TarEntry's for this entry's children.
     */
    public TarEntry[] getDirectoryEntries()
    {
        if( null == m_file || !m_file.isDirectory() )
        {
            return new TarEntry[ 0 ];
        }

        final String[] list = m_file.list();
        final TarEntry[] result = new TarEntry[ list.length ];

        for( int i = 0; i < list.length; ++i )
        {
            result[ i ] = new TarEntry( new File( m_file, list[ i ] ) );
        }

        return result;
    }

    /**
     * Get this entry's file.
     *
     * @return This entry's file.
     */
    public File getFile()
    {
        return m_file;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     * @deprecated Use getGroupID() instead
     * @see #getGroupID()
     */
    public int getGroupId()
    {
        return m_groupID;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     */
    public int getGroupID()
    {
        return m_groupID;
    }

    /**
     * Get this entry's group name.
     *
     * @return This entry's group name.
     */
    public String getGroupName()
    {
        return m_groupName.toString();
    }

    /**
     * Set this entry's modification time.
     *
     * @return The ModTime value
     */
    public Date getModTime()
    {
        return new Date( m_modTime * 1000 );
    }

    /**
     * Get this entry's mode.
     *
     * @return This entry's mode.
     */
    public int getMode()
    {
        return m_mode;
    }

    /**
     * Get this entry's name.
     *
     * @return This entry's name.
     */
    public String getName()
    {
        return m_name.toString();
    }

    /**
     * Get this entry's file size.
     *
     * @return This entry's file size.
     */
    public long getSize()
    {
        return m_size;
    }

    /**
     * Get this entry's checksum.
     *
     * @return This entry's checksum.
     */
    public int getCheckSum()
    {
        return m_checkSum;
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     * @deprecated Use getUserID() instead
     * @see #getUserID()
     */
    public int getUserId()
    {
        return m_userID;
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     */
    public int getUserID()
    {
        return m_userID;
    }

    /**
     * Get this entry's user name.
     *
     * @return This entry's user name.
     */
    public String getUserName()
    {
        return m_userName.toString();
    }

    /**
     * Determine if the given entry is a descendant of this entry. Descendancy
     * is determined by the name of the descendant starting with this entry's
     * name.
     *
     * @param desc Entry to be checked as a descendent of
     * @return True if entry is a descendant of
     */
    public boolean isDescendent( final TarEntry desc )
    {
        return desc.getName().startsWith( getName() );
    }

    /**
     * Return whether or not this entry represents a directory.
     *
     * @return True if this entry is a directory.
     */
    public boolean isDirectory()
    {
        if( m_file != null )
        {
            return m_file.isDirectory();
        }

        if( m_linkFlag == TarConstants.LF_DIR )
        {
            return true;
        }

        if( getName().endsWith( "/" ) )
        {
            return true;
        }

        return false;
    }

    /**
     * Indicate if this entry is a GNU long name block
     *
     * @return true if this is a long name extension provided by GNU tar
     */
    public boolean isGNULongNameEntry()
    {
        return m_linkFlag == TarConstants.LF_GNUTYPE_LONGNAME &&
            m_name.toString().equals( TarConstants.GNU_LONGLINK );
    }

    /**
     * Determine if the two entries are equal. Equality is determined by the
     * header names being equal.
     *
     * @param other Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals( final TarEntry other )
    {
        return getName().equals( other.getName() );
    }

    /**
     * Parse an entry's header information from a header buffer.
     *
     * @param header The tar entry header buffer to get information from.
     */
    private void parseTarHeader( final byte[] header )
    {
        int offset = 0;

        m_name = TarUtils.parseName( header, offset, NAMELEN );
        offset += NAMELEN;
        m_mode = (int)TarUtils.parseOctal( header, offset, TarConstants.MODELEN );
        offset += TarConstants.MODELEN;
        m_userID = (int)TarUtils.parseOctal( header, offset, TarConstants.UIDLEN );
        offset += TarConstants.UIDLEN;
        m_groupID = (int)TarUtils.parseOctal( header, offset, TarConstants.GIDLEN );
        offset += TarConstants.GIDLEN;
        m_size = TarUtils.parseOctal( header, offset, TarConstants.SIZELEN );
        offset += TarConstants.SIZELEN;
        m_modTime = TarUtils.parseOctal( header, offset, TarConstants.MODTIMELEN );
        offset += TarConstants.MODTIMELEN;
        m_checkSum = (int)TarUtils.parseOctal( header, offset, TarConstants.CHKSUMLEN );
        offset += TarConstants.CHKSUMLEN;
        m_linkFlag = header[ offset++ ];
        m_linkName = TarUtils.parseName( header, offset, NAMELEN );
        offset += NAMELEN;
        m_magic = TarUtils.parseName( header, offset, TarConstants.MAGICLEN );
        offset += TarConstants.MAGICLEN;
        m_userName = TarUtils.parseName( header, offset, TarConstants.UNAMELEN );
        offset += TarConstants.UNAMELEN;
        m_groupName = TarUtils.parseName( header, offset, TarConstants.GNAMELEN );
        offset += TarConstants.GNAMELEN;
        m_devMajor = (int)TarUtils.parseOctal( header, offset, TarConstants.DEVLEN );
        offset += TarConstants.DEVLEN;
        m_devMinor = (int)TarUtils.parseOctal( header, offset, TarConstants.DEVLEN );
    }

    /**
     * Write an entry's header information to a header buffer.
     *
     * @param buffer The tar entry header buffer to fill in.
     */
    public void writeEntryHeader( final byte[] buffer )
    {
        int offset = 0;

        offset = TarUtils.getNameBytes( m_name, buffer, offset, NAMELEN );
        offset = TarUtils.getOctalBytes( m_mode, buffer, offset, TarConstants.MODELEN );
        offset = TarUtils.getOctalBytes( m_userID, buffer, offset, TarConstants.UIDLEN );
        offset = TarUtils.getOctalBytes( m_groupID, buffer, offset, TarConstants.GIDLEN );
        offset = TarUtils.getLongOctalBytes( m_size, buffer, offset, TarConstants.SIZELEN );
        offset = TarUtils.getLongOctalBytes( m_modTime, buffer, offset, TarConstants.MODTIMELEN );

        final int checkSumOffset = offset;
        for( int i = 0; i < TarConstants.CHKSUMLEN; ++i )
        {
            buffer[ offset++ ] = (byte)' ';
        }

        buffer[ offset++ ] = m_linkFlag;
        offset = TarUtils.getNameBytes( m_linkName, buffer, offset, NAMELEN );
        offset = TarUtils.getNameBytes( m_magic, buffer, offset, TarConstants.MAGICLEN );
        offset = TarUtils.getNameBytes( m_userName, buffer, offset, TarConstants.UNAMELEN );
        offset = TarUtils.getNameBytes( m_groupName, buffer, offset, TarConstants.GNAMELEN );
        offset = TarUtils.getOctalBytes( m_devMajor, buffer, offset, TarConstants.DEVLEN );
        offset = TarUtils.getOctalBytes( m_devMinor, buffer, offset, TarConstants.DEVLEN );

        while( offset < buffer.length )
        {
            buffer[ offset++ ] = 0;
        }

        final long checkSum = TarUtils.computeCheckSum( buffer );
        TarUtils.getCheckSumOctalBytes( checkSum, buffer, checkSumOffset, TarConstants.CHKSUMLEN );
    }
}
