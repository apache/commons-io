/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/IOUtilsTestCase.java,v 1.6 2003/12/30 15:26:59 jeremias Exp $
 * $Revision: 1.6 $
 * $Date: 2003/12/30 15:26:59 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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

package org.apache.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.testtools.*;

// Note: jdk1.2 dependency

/**
 * This is used to test IOUtils for correctness. The following checks are performed:
 * <ul>
 *   <li>The return must not be null, must be the same type and equals() to the method's second arg</li>
 *   <li>All bytes must have been read from the source (available() == 0)</li>
 *   <li>The source and destination content must be identical (byte-wise comparison check)</li>
 *   <li>The output stream must not have been closed (a byte/char is written to test this, and
 *   subsequent size checked)</li>
 * </ul>
 * Due to interdependencies in IOUtils and IOUtilsTestlet, one bug may cause
 * multiple tests to fail.
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 */
public class IOUtilsTestCase extends FileBasedTestCase {
    /*
     * Note: this is not particularly beautiful code. A better way to check for
     * flush and close status would be to implement "trojan horse" wrapper
     * implementations of the various stream classes, which set a flag when
     * relevant methods are called. (JT)
     */

    private static final int FILE_SIZE = 1024 * 4 + 1;

    private File m_testFile;

    public void setUp()
    {
        try
        {
            getTestDirectory().mkdirs();
            m_testFile = new File( getTestDirectory(), "file2-test.txt" );

            createFile( m_testFile, FILE_SIZE );
        }
        catch( IOException ioe )
        {
            throw new RuntimeException( "Can't run this test because "
                    + "environment could not be built: " + ioe.getMessage());
        }
    }

    public void tearDown()
    {
        try
        {
            FileUtils.deleteDirectory( getTestDirectory() );
        }
        catch( IOException ioe )
        {
            // Ignore, because by this time, it is too late.
        }
    }

    public IOUtilsTestCase( String name )
    {
        super( name );
    }

    /** Assert that the contents of two byte arrays are the same. */
    private void assertEqualContent( byte[] b0, byte[] b1 )
        throws IOException
    {
        assertTrue( "Content not equal according to java.util.Arrays#equals()", Arrays.equals( b0, b1 ) );
    }

    public void testInputStreamToOutputStream()
        throws Exception
    {
        File destination = newFile( "copy1.txt" );
        FileInputStream fin = new FileInputStream( m_testFile );
        try {
            FileOutputStream fout = new FileOutputStream( destination );
            try {
                int count = CopyUtils.copy( fin, fout );
                assertTrue( "Not all bytes were read", fin.available() == 0 );
                assertEquals( "Number of bytes read should equal file size", m_testFile.length(), count );
                fout.flush();

                checkFile( destination, m_testFile );
                checkWrite( fout );
            } finally {
                fout.close();
            }
            deleteFile( destination );
        } finally {
            fin.close();
        }
    }

    public void testInputStreamToWriter()
        throws Exception
    {
        File destination = newFile( "copy2.txt" );
        FileInputStream fin = new FileInputStream( m_testFile );
        try {
            FileWriter fout = new FileWriter( destination );
            try {
                CopyUtils.copy( fin, fout );

                assertTrue( "Not all bytes were read", fin.available() == 0 );
                fout.flush();

                checkFile( destination, m_testFile );
                checkWrite( fout );
            } finally {
                fout.close();
            }
            deleteFile( destination );
        } finally {
             fin.close();
        }
    }

    public void testInputStreamToString()
        throws Exception
    {
        FileInputStream fin = new FileInputStream( m_testFile );
        try {
            String out = IOUtils.toString( fin );
            assertNotNull( out );
            assertTrue( "Not all bytes were read", fin.available() == 0 );
            assertTrue( "Wrong output size: out.length()=" + out.length() +
                        "!=" + FILE_SIZE, out.length() == FILE_SIZE );
        } finally {
            fin.close();
        }
    }

    public void testReaderToOutputStream()
        throws Exception
    {
        File destination = newFile( "copy3.txt" );
        FileReader fin = new FileReader( m_testFile );
        try {
            FileOutputStream fout = new FileOutputStream( destination );
            try {
                CopyUtils.copy( fin, fout );
                //Note: this method *does* flush. It is equivalent to:
                //  OutputStreamWriter _out = new OutputStreamWriter(fout);
                //  CopyUtils.copy( fin, _out, 4096 ); // copy( Reader, Writer, int );
                //  _out.flush();
                //  out = fout;
    
                // Note: rely on the method to flush
                checkFile( destination, m_testFile );
                checkWrite( fout );
            } finally {
                fout.close();
            }
            deleteFile( destination );
        } finally {
            fin.close();
        }
    }

    public void testReaderToWriter()
        throws Exception
    {
        File destination = newFile( "copy4.txt" );
        FileReader fin = new FileReader( m_testFile );
        try {
            FileWriter fout = new FileWriter( destination );
            try {
                int count = CopyUtils.copy( fin, fout );
                assertEquals( "The number of characters returned by copy is wrong", m_testFile.length(), count);

                fout.flush();
                checkFile( destination, m_testFile );
                checkWrite( fout );
            } finally {
                fout.close();
            }
            deleteFile( destination );
        } finally {
            fin.close();
        }
    }

    public void testReaderToString()
        throws Exception
    {
        FileReader fin = new FileReader( m_testFile );
        try {
            String out = IOUtils.toString( fin );
            assertNotNull( out );
            assertTrue( "Wrong output size: out.length()=" +
                        out.length() + "!=" + FILE_SIZE,
                        out.length() == FILE_SIZE );
        } finally {
            fin.close();
        }
    }

    public void testStringToOutputStream()
        throws Exception
    {
        File destination = newFile( "copy5.txt" );
        FileReader fin = new FileReader( m_testFile );
        String str;
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString( fin );
        } finally {
            fin.close();
        }
        
        FileOutputStream fout = new FileOutputStream( destination );
        try {
            CopyUtils.copy( str, fout );
            //Note: this method *does* flush. It is equivalent to:
            //  OutputStreamWriter _out = new OutputStreamWriter(fout);
            //  CopyUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
            //  _out.flush();
            //  out = fout;
            // note: we don't flush here; this IOUtils method does it for us

            checkFile( destination, m_testFile );
            checkWrite( fout );
        } finally {
            fout.close();
        }
        deleteFile( destination );
    }

    public void testStringToWriter()
        throws Exception
    {
        File destination = newFile( "copy6.txt" );
        FileReader fin = new FileReader( m_testFile );
        String str;
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString( fin );
        } finally {
            fin.close();
        }
        
        FileWriter fout = new FileWriter( destination );
        try {
            CopyUtils.copy( str, fout );
            fout.flush();

            checkFile( destination, m_testFile );
            checkWrite( fout );
        } finally {
            fout.close();
        }
        deleteFile( destination );
    }

    public void testInputStreamToByteArray()
        throws Exception
    {
        FileInputStream fin = new FileInputStream( m_testFile );
        try {
            byte[] out = IOUtils.toByteArray( fin );
            assertNotNull( out );
            assertTrue( "Not all bytes were read", fin.available() == 0 );
            assertTrue( "Wrong output size: out.length=" + out.length +
                        "!=" + FILE_SIZE, out.length == FILE_SIZE );
            assertEqualContent( out, m_testFile );
        } finally {
            fin.close();
        }
    }

    public void testStringToByteArray()
        throws Exception
    {
        FileReader fin = new FileReader( m_testFile );
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            String str = IOUtils.toString( fin );

            byte[] out = IOUtils.toByteArray( str );
            assertEqualContent( str.getBytes(), out );
        } finally {
            fin.close();
        }
    }

    public void testByteArrayToWriter()
        throws Exception
    {
        File destination = newFile( "copy7.txt" );
        FileInputStream fin = new FileInputStream( m_testFile );
        byte[] in;
        try {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray( fin );
        } finally {
            fin.close();
        }

        FileWriter fout = new FileWriter( destination );
        try {
            CopyUtils.copy( in, fout );
            fout.flush();
            checkFile( destination, m_testFile );
            checkWrite( fout );
        } finally {
            fout.close();
        }
        deleteFile( destination );
    }

    public void testByteArrayToString()
        throws Exception
    {
        FileInputStream fin = new FileInputStream( m_testFile );
        try {
            byte[] in = IOUtils.toByteArray( fin );
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            String str = IOUtils.toString( in );
            assertEqualContent( in, str.getBytes() );
        } finally {
            fin.close();
        }
    }

    public void testByteArrayToOutputStream()
        throws Exception
    {
        File destination = newFile( "copy8.txt" );
        FileInputStream fin = new FileInputStream( m_testFile );
        byte[] in;
        try {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray( fin );
        } finally {
            fin.close();
        }

        FileOutputStream fout = new FileOutputStream( destination );
        try {
            CopyUtils.copy( in, fout );

            fout.flush();

            checkFile( destination, m_testFile );
            checkWrite( fout );
        } finally {
            fout.close();
        }
        deleteFile( destination );
    }


}

