/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.io.compress.bzip2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.TestCase;

/**
 * A test the stress tested the BZip implementation to verify
 * that it behaves correctly.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/09 15:12:23 $
 */
public class BzipTestCase
    extends TestCase
{
    private static final byte[] HEADER = new byte[]{(byte)'B', (byte)'Z'};

    public BzipTestCase( final String name )
    {
        super( name );
    }

    public void testBzipOutputStream()
        throws Exception
    {
        final InputStream input = getInputStream( "asf-logo-huge.tar" );
        final File outputFile = getOutputFile( ".tar.bz2" );
        final OutputStream output = new FileOutputStream( outputFile );
        final CBZip2OutputStream packedOutput = getPackedOutput( output );
        copy( input, packedOutput );
        shutdownStream( input );
        shutdownStream( packedOutput );
        shutdownStream( output );
        compareContents( "asf-logo-huge.tar.bz2", outputFile );
        forceDelete( outputFile );
    }

    private void forceDelete( final File outputFile ) throws IOException
    {
        if( !outputFile.delete() )
        {
            final String message = "File " + outputFile + " unable to be deleted.";
            throw new IOException( message );
        }
    }

    public void testBzipInputStream()
        throws Exception
    {
        final InputStream input = getInputStream( "asf-logo-huge.tar.bz2" );
        final File outputFile = getOutputFile( ".tar" );
        final OutputStream output = new FileOutputStream( outputFile );
        final CBZip2InputStream packedInput = getPackedInput( input );
        copy( packedInput, output );
        shutdownStream( input );
        shutdownStream( packedInput );
        shutdownStream( output );
        compareContents( "asf-logo-huge.tar", outputFile );
        forceDelete( outputFile );
    }

    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     */
    private void copy( final InputStream input,
                       final OutputStream output )
        throws IOException
    {
        final byte[] buffer = new byte[ 8024 ];
        int n = 0;
        while( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }

    private void compareContents( final String initial, final File generated )
        throws Exception
    {
        final InputStream input1 = getInputStream( initial );
        final InputStream input2 = new FileInputStream( generated );
        final boolean test = contentEquals( input1, input2 );
        shutdownStream( input1 );
        shutdownStream( input2 );
        assertTrue( "Contents of " + initial + " matches generated version " + generated, test );
    }

    private CBZip2InputStream getPackedInput( final InputStream input )
        throws IOException
    {
        final int b1 = input.read();
        final int b2 = input.read();
        assertEquals( "Equal header byte1", b1, 'B' );
        assertEquals( "Equal header byte2", b2, 'Z' );
        return new CBZip2InputStream( input );
    }

    private CBZip2OutputStream getPackedOutput( final OutputStream output )
        throws IOException
    {
        output.write( HEADER );
        return new CBZip2OutputStream( output );
    }

    private File getOutputFile( final String postfix )
        throws IOException
    {
        final File cwd = new File( "." );
        return File.createTempFile( "ant-test", postfix, cwd );
    }

    private InputStream getInputStream( final String resource )
        throws Exception
    {
        final String filename = ".." + File.separator + ".." + File.separator +
            "src" + File.separator + "test" + File.separator +
            getClass().getName().replace( '.', File.separatorChar );
        final String path = getPath( filename );
        final File input = new File( path, resource );
        return new FileInputStream( input );
        //final ClassLoader loader = getClass().getClassLoader();
        //return loader.getResourceAsStream( resource );
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

        int ch = bufferedInput1.read();
        while( -1 != ch )
        {
            final int ch2 = bufferedInput2.read();
            if( ch != ch2 )
            {
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

    private String getPath( final String filepath )
    {
        final int index = filepath.lastIndexOf( File.separatorChar );
        if( -1 == index )
        {
            return "";
        }
        else
        {
            return filepath.substring( 0, index );
        }
    }

    /**
     * Unconditionally close an <code>OutputStream</code>.
     * Equivalent to {@link java.io.OutputStream#close()}, except any exceptions will be ignored.
     * @param output A (possibly null) OutputStream
     */
    private static void shutdownStream( final OutputStream output )
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

    /**
     * Unconditionally close an <code>InputStream</code>.
     * Equivalent to {@link java.io.InputStream#close()}, except any exceptions will be ignored.
     * @param input A (possibly null) InputStream
     */
    private static void shutdownStream( final InputStream input )
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
}
