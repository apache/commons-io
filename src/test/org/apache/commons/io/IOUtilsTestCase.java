/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

