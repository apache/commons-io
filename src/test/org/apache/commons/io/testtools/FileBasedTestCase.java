/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/testtools/FileBasedTestCase.java,v 1.1 2003/08/21 18:42:53 jeremias Exp $
 * $Revision: 1.1 $
 * $Date: 2003/08/21 18:42:53 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 */
package org.apache.commons.io.testtools;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Base class for testcases doing tests with files.
 * 
 * @author Jeremias Maerki
 */
public abstract class FileBasedTestCase extends TestCase {

    private static File testDir;

    public FileBasedTestCase(String name) {
        super(name);
    }
    
    public static File getTestDirectory() {
        if (testDir == null) {
            testDir = (new File("test/io/")).getAbsoluteFile();
        }
        return testDir;
    }
    
    protected void createFile(final File file, final long size)
            throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file 
                + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
            new BufferedOutputStream(new java.io.FileOutputStream(file));
        try {
            generateTestData(output, size);
        } finally {
            IOUtils.shutdownStream(output);
        }
    }
    
    protected byte[] generateTestData(final long size) {
        try {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            generateTestData(baout, size);
            return baout.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException("This should never happen: " + ioe.getMessage());
        }
    }
    
    protected void generateTestData(final OutputStream out, final long size) 
                throws IOException {
        for (int i = 0; i < size; i++) {
            //output.write((byte)'X');

            // nice varied byte pattern compatible with Readers and Writers
            out.write( (byte)( (i % 127) + 1) );
        }
    }

    protected File newFile(String filename) throws IOException {
        final File destination = new File( getTestDirectory(), filename );
        /*
        assertTrue( filename + "Test output data file shouldn't previously exist",
                    !destination.exists() );
        */
        if (destination.exists()) {
            FileUtils.forceDelete(destination);
        }
        return destination;
    }

    protected void checkFile( final File file, final File referenceFile )
                throws Exception {
        assertTrue( "Check existence of output file", file.exists() );
        assertEqualContent( referenceFile, file );
    }

    /** Assert that the content of two files is the same. */
    private void assertEqualContent( final File f0, final File f1 )
        throws IOException
    {
        /* This doesn't work because the filesize isn't updated until the file
         * is closed.
        assertTrue( "The files " + f0 + " and " + f1 +
                    " have differing file sizes (" + f0.length() +
                    " vs " + f1.length() + ")", ( f0.length() == f1.length() ) );
        */
        final InputStream is0 = new java.io.FileInputStream( f0 );
        try {
            final InputStream is1 = new java.io.FileInputStream( f1 );
            try {
                final byte[] buf0 = new byte[ 1024 ];
                final byte[] buf1 = new byte[ 1024 ];
                int n0 = 0;
                int n1 = 0;

                while( -1 != n0 )
                {
                    n0 = is0.read( buf0 );
                    n1 = is1.read( buf1 );
                    assertTrue( "The files " + f0 + " and " + f1 +
                                " have differing number of bytes available (" + n0 +
                                " vs " + n1 + ")", ( n0 == n1 ) );

                    assertTrue( "The files " + f0 + " and " + f1 +
                                " have different content", Arrays.equals( buf0, buf1 ) );
                }
            } finally {
                is1.close();
            }
        } finally {
            is0.close();
        }
    }

    /** Assert that the content of a file is equal to that in a byte[]. */
    protected void assertEqualContent( final byte[] b0, final File file )
        throws IOException
    {
        final InputStream is = new java.io.FileInputStream( file );
        try {
            byte[] b1 = new byte[ b0.length ];
            int numRead = is.read( b1 );
            assertTrue( "Different number of bytes", numRead == b0.length && is.available() == 0 );
            for( int i = 0;
                 i < numRead;
                 assertTrue( "Byte " + i + " differs (" + b0[ i ] + " != " + b1[ i ] + ")", 
                    b0[ i ] == b1[ i ] ), i++
                );
        } finally {
            is.close();
        }
    }

    protected void checkWrite(final OutputStream output) throws Exception {
        try {
            new java.io.PrintStream(output).write(0);
        } catch (final Throwable t) {
            throw new AssertionFailedError(
                "The copy() method closed the stream "
                    + "when it shouldn't have. "
                    + t.getMessage());
        }
    }

    protected void checkWrite(final Writer output) throws Exception {
        try {
            new java.io.PrintWriter(output).write('a');
        } catch (final Throwable t) {
            throw new AssertionFailedError(
                "The copy() method closed the stream "
                    + "when it shouldn't have. "
                    + t.getMessage());
        }
    }

    protected void deleteFile( final File file )
        throws Exception {
        if (file.exists()) {
            assertTrue("Couldn't delete file: " + file, file.delete());
        }
    }
    

}
