/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.tar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.TestCase;

/**
 * Test case for all tar resources.
 *
 * @todo Find V7 tar and do tests against it
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/09 15:12:23 $
 */
public final class TarTestCase
    extends TestCase
{
    private static final char SP = File.separatorChar;
    private static final String BASE_DATA_NAME = "data.txt";
    private static final String LFN_PART = "a-b-c-d-e-f-g-h-i-j/";
    private static final String LONG_FILE_NAME =
        LFN_PART + LFN_PART + LFN_PART + LFN_PART + LFN_PART + "a";

    private static final String BASEDIR = calcBaseDir();

    private static final File BASEDIR_FILE = new File( BASEDIR );
    private static final File POSIX_TAR_FILE =
        new File( BASEDIR_FILE, "posix.tar" );
    //    private static final File V7_TAR_FILE =
    //        new File( BASEDIR_FILE, "v7.tar" );
    private static final File GNU_TAR_FILE =
        new File( BASEDIR_FILE, "gnu.tar" );
    private static final File DATA_FILE1 =
        new File( BASEDIR_FILE, BASE_DATA_NAME );
    private static final String USER_NAME = "avalon";
    private static final String GROUP_NAME = "excalibur";
    private static final long SIZE = DATA_FILE1.length();
    private static final int GROUP_ID = 0;
    private static final int USER_ID = 0;
    private static final int MODE = 0100000;
    private static final int MOD_TIME = 0;

    public TarTestCase()
    {
        this( "Tar Test Case" );
    }

    public TarTestCase( String name )
    {
        super( name );
    }

    private static String calcBaseDir()
    {
        final String name = TarTestCase.class.getName();
        final int size = name.length();
        final String filename =
            name.substring( 0, size - 11 ).replace( '.', SP );
        return ".." + SP + ".." + SP +
            "src" + SP + "test" + SP + filename + SP;
    }

    public void testReadPosixTar()
        throws Exception
    {
        compareTar( BASE_DATA_NAME, POSIX_TAR_FILE );
    }

    public void testReadGnuTar()
        throws Exception
    {
        compareTar( LONG_FILE_NAME, GNU_TAR_FILE );
    }

    public void testWritePosixTar()
        throws Exception
    {
        //final File temp = new File( BASEDIR_FILE, "posix2.tar" );
        final File temp = File.createTempFile( "delete-me", "tar" );
        final FileOutputStream fileOutput = new FileOutputStream( temp );
        final TarOutputStream output = new TarOutputStream( fileOutput );
        //output.setBufferDebug( true );
        final TarEntry entry = new TarEntry( BASE_DATA_NAME );
        setupEntry( entry );
        output.putNextEntry( entry );

        final FileInputStream fileInput = new FileInputStream( DATA_FILE1 );
        output.copyEntryContents( fileInput );
        output.closeEntry();
        shutdownStream( fileInput );
        shutdownStream( output );
        shutdownStream( fileOutput );

        assertTrue( "Tar files Equal", contentEquals( temp, POSIX_TAR_FILE ) );
        temp.delete();
    }

    public void testWriteGnuTar()
        throws Exception
    {
        //final File temp = new File( BASEDIR_FILE, "gnu2.tar" );
        final File temp = File.createTempFile( "delete-me", "tar" );
        final FileOutputStream fileOutput = new FileOutputStream( temp );
        final TarOutputStream output = new TarOutputStream( fileOutput );
        //output.setBufferDebug( true );
        output.setLongFileMode( TarOutputStream.LONGFILE_GNU );
        final TarEntry entry = new TarEntry( LONG_FILE_NAME );
        setupEntry( entry );
        output.putNextEntry( entry );

        final FileInputStream fileInput = new FileInputStream( DATA_FILE1 );
        output.copyEntryContents( fileInput );
        output.closeEntry();
        shutdownStream( fileInput );
        shutdownStream( output );
        shutdownStream( fileOutput );

        //Have to compare it this way as the contents will differ
        //due to entry created for second part of name
        compareTar( LONG_FILE_NAME, temp );
        temp.delete();
    }

    private void setupEntry( final TarEntry entry )
    {
        entry.setModTime( MOD_TIME );
        entry.setSize( SIZE );
        entry.setUserID( USER_ID );
        entry.setGroupID( GROUP_ID );
        entry.setUserName( USER_NAME );
        entry.setGroupName( GROUP_NAME );
        entry.setMode( MODE );
    }

    private void checkEntry( final TarEntry entry )
    {
        assertEquals( "Entry size", SIZE, entry.getSize() );
        assertEquals( "Entry User ID", USER_ID, entry.getUserID() );
        assertEquals( "Entry Group ID", GROUP_ID, entry.getGroupID() );
        assertEquals( "Entry User name", USER_NAME, entry.getUserName() );
        assertEquals( "Entry group name", GROUP_NAME, entry.getGroupName() );
        assertEquals( "Entry mode", MODE, entry.getMode() );
        assertEquals( "Entry mode", MOD_TIME, entry.getModTime().getTime() / 1000 );
    }

    /**
     * Read tar entry with specified name from tar file1 and compare
     * against data file DATA_FILE1.
     *
     * @param entryName the expected name of entry
     * @param file1 the tar file comparing
     * @throws IOException if an error occurs
     */
    private void compareTar( final String entryName,
                             final File file1 )
        throws IOException
    {
        final FileInputStream fileInput = new FileInputStream( file1 );
        final TarInputStream input = new TarInputStream( fileInput );
        //input.setDebug( true );
        final TarEntry entry = input.getNextEntry();

        assertEquals( "Entry name", entryName, entry.getName() );
        checkEntry( entry );

        final File temp = new File( BASEDIR_FILE, entryName.length() + "data.txt" );//File.createTempFile( "delete-me", "tar" );
        final FileOutputStream output = new FileOutputStream( temp );
        input.copyEntryContents( output );
        shutdownStream( output );

        assertNull( "Next Entry", input.getNextEntry() );

        shutdownStream( input );

        assertTrue( "Data Equals", contentEquals( temp, DATA_FILE1 ) );
        temp.delete();
    }

    /**
     * Compare the contents of two files to determine if they are equal or not.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     */
    private boolean contentEquals( final File file1, final File file2 )
        throws IOException
    {
        final boolean file1Exists = file1.exists();
        if( file1Exists != file2.exists() )
        {
            return false;
        }

        if( !file1Exists )
        {
            // two not existing files are equal
            return true;
        }

        if( file1.isDirectory() || file2.isDirectory() )
        {
            // don't want to compare directory contents
            return false;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try
        {
            input1 = new FileInputStream( file1 );
            input2 = new FileInputStream( file2 );
            return contentEquals( input1, input2 );

        }
        finally
        {
            shutdownStream( input1 );
            shutdownStream( input2 );
        }
    }

    /**
     * Compare the contents of two Streams to determine if they are equal or not.
     *
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the content of the streams are equal or they both don't exist, false otherwise
     */
    private boolean contentEquals( final InputStream input1,
                                   final InputStream input2 )
        throws IOException
    {
        final InputStream bufferedInput1 = new BufferedInputStream( input1 );
        final InputStream bufferedInput2 = new BufferedInputStream( input2 );

        int count = 0;
        int ch = bufferedInput1.read();
        while( -1 != ch )
        {
            final int ch2 = bufferedInput2.read();
            count++;
            if( ch != ch2 )
            {
                System.out.println( "count = " + count );
                System.out.println( "ch2 = " + ch2 );
                System.out.println( "ch = " + ch );
                return false;
            }
            ch = bufferedInput1.read();
        }

        final int ch2 = bufferedInput2.read();
        if( -1 != ch2 )
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private void shutdownStream( final InputStream input )
    {
        if( null == input )
        {
            return;
        }

        try
        {
            input.close();
        }
        catch( final IOException ioe )
        {
        }
    }

    private void shutdownStream( final OutputStream output )
    {
        if( null == output )
        {
            return;
        }

        try
        {
            output.close();
        }
        catch( final IOException ioe )
        {
        }
    }
}
