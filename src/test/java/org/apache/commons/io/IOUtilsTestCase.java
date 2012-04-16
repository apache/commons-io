/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.testtools.FileBasedTestCase;
import org.junit.Assert;

/**
 * This is used to test IOUtils for correctness. The following checks are performed:
 * <ul>
 * <li>The return must not be null, must be the same type and equals() to the method's second arg</li>
 * <li>All bytes must have been read from the source (available() == 0)</li>
 * <li>The source and destination content must be identical (byte-wise comparison check)</li>
 * <li>The output stream must not have been closed (a byte/char is written to test this, and subsequent size checked)</li>
 * </ul>
 * Due to interdependencies in IOUtils and IOUtilsTestlet, one bug may cause multiple tests to fail.
 */
public class IOUtilsTestCase extends FileBasedTestCase {

    /** Determine if this is windows. */
    private static final boolean WINDOWS = File.separatorChar == '\\';
    /*
     * Note: this is not particularly beautiful code. A better way to check for flush and close status would be to
     * implement "trojan horse" wrapper implementations of the various stream classes, which set a flag when relevant
     * methods are called. (JT)
     */

    private static final int FILE_SIZE = 1024 * 4 + 1;

    private File m_testFile;

    @Override
    public void setUp() {
        try {
            getTestDirectory().mkdirs();
            m_testFile = new File(getTestDirectory(), "file2-test.txt");

            createFile(m_testFile, FILE_SIZE);
        } catch (IOException ioe) {
            throw new RuntimeException("Can't run this test because the environment could not be built: "
                    + ioe.getMessage());
        }
        // Create and init a byte array as input data
        iarr = new byte[200];
        Arrays.fill( iarr, (byte)-1);
        for( int i=0; i< 80; i++){
            iarr[i] = (byte) i;
        }
        carr = new char[200];
        Arrays.fill( carr, (char)-1);
        for( int i=0; i< 80; i++){
            carr[i] = (char) i;
        }
    }

    @Override
    public void tearDown() {
        carr = null;
        iarr = null;
        try {
            FileUtils.deleteDirectory(getTestDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Could not clear up " + getTestDirectory() + ": " + e);
        }
    }

    public IOUtilsTestCase(String name) {
        super(name);
    }

    public void testCloseQuietlyNullSelector() {
        Selector selector = null;
        IOUtils.closeQuietly(selector);
    }

    public void testCloseableCloseQuietlyOnException() {
        IOUtils.closeQuietly(new Closeable() {            
            public void close() throws IOException {
                throw new IOException();
            }
        });
    }

    public void testSocketCloseQuietlyOnException() {
        IOUtils.closeQuietly(new Socket() {            
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        });
    }

    public void testServerSocketCloseQuietlyOnException() throws IOException {
        IOUtils.closeQuietly(new ServerSocket() {            
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        });
    }

    public void testSocketCloseQuietly() {
        IOUtils.closeQuietly((Socket) null);
        IOUtils.closeQuietly(new Socket());
    }

    public void testServerSocketCloseQuietly() throws IOException {
        IOUtils.closeQuietly((ServerSocket) null);
        IOUtils.closeQuietly(new ServerSocket());
    }

    public void testCloseQuietlySelector() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(selector);
        }
    }

    public void testCloseQuietlySelectorIOException() {
        Selector selector = new SelectorAdapter() {
            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
        IOUtils.closeQuietly(selector);
    }

    public void testCloseQuietlySelectorTwice() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(selector);
            IOUtils.closeQuietly(selector);
        }
    }

    // -----------------------------------------------------------------------
    public void testConstants() throws Exception {
        assertEquals('/', IOUtils.DIR_SEPARATOR_UNIX);
        assertEquals('\\', IOUtils.DIR_SEPARATOR_WINDOWS);
        assertEquals("\n", IOUtils.LINE_SEPARATOR_UNIX);
        assertEquals("\r\n", IOUtils.LINE_SEPARATOR_WINDOWS);
        if (WINDOWS) {
            assertEquals('\\', IOUtils.DIR_SEPARATOR);
            assertEquals("\r\n", IOUtils.LINE_SEPARATOR);
        } else {
            assertEquals('/', IOUtils.DIR_SEPARATOR);
            assertEquals("\n", IOUtils.LINE_SEPARATOR);
        }
    }

    // -----------------------------------------------------------------------
    /** Assert that the contents of two byte arrays are the same. */
    private void assertEqualContent(byte[] b0, byte[] b1) {
        assertTrue("Content not equal according to java.util.Arrays#equals()", Arrays.equals(b0, b1));
    }

    public void testInputStreamToString() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals("Not all bytes were read", 0, fin.available());
            assertEquals("Wrong output size", FILE_SIZE, out.length());
        } finally {
            fin.close();
        }
    }

    public void testReaderToString() throws Exception {
        FileReader fin = new FileReader(m_testFile);
        try {
            String out = IOUtils.toString(fin);
            assertNotNull(out);
            assertEquals("Wrong output size", FILE_SIZE, out.length());
        } finally {
            fin.close();
        }
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testStringToOutputStream() throws Exception {
        File destination = newFile("copy5.txt");
        FileReader fin = new FileReader(m_testFile);
        String str;
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        } finally {
            fin.close();
        }

        FileOutputStream fout = new FileOutputStream(destination);
        try {
            CopyUtils.copy(str, fout);
            // Note: this method *does* flush. It is equivalent to:
            // OutputStreamWriter _out = new OutputStreamWriter(fout);
            // CopyUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
            // _out.flush();
            // out = fout;
            // note: we don't flush here; this IOUtils method does it for us

            checkFile(destination, m_testFile);
            checkWrite(fout);
        } finally {
            fout.close();
        }
        deleteFile(destination);
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testStringToWriter() throws Exception {
        File destination = newFile("copy6.txt");
        FileReader fin = new FileReader(m_testFile);
        String str;
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            str = IOUtils.toString(fin);
        } finally {
            fin.close();
        }

        FileWriter fout = new FileWriter(destination);
        try {
            CopyUtils.copy(str, fout);
            fout.flush();

            checkFile(destination, m_testFile);
            checkWrite(fout);
        } finally {
            fout.close();
        }
        deleteFile(destination);
    }

    public void testToByteArray_Reader() throws IOException {
        final String charsetName = "UTF-8";
        final byte[] expecteds = charsetName.getBytes(charsetName);
        byte[] actuals = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expecteds)));
        Assert.assertArrayEquals(expecteds, actuals);
        actuals = IOUtils.toByteArray(new InputStreamReader(new ByteArrayInputStream(expecteds)), charsetName);
        Assert.assertArrayEquals(expecteds, actuals);
    }
    
    public void testInputStreamToByteArray() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            byte[] out = IOUtils.toByteArray(fin);
            assertNotNull(out);
            assertEquals("Not all bytes were read", 0, fin.available());
            assertEquals("Wrong output size", FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fin.close();
        }
    }

    public void testInputStreamToByteArray_Size() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            byte[] out = IOUtils.toByteArray(fin, m_testFile.length());
            assertNotNull(out);
            assertEquals("Not all bytes were read", 0, fin.available());
            assertEquals("Wrong output size: out.length=" + out.length + "!=" + FILE_SIZE, FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fin.close();
        }
    }

    public void testInputStreamToByteArray_NegativeSize() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);

        try {
            IOUtils.toByteArray(fin, -1);
            fail("IllegalArgumentException excepted");
        } catch (IllegalArgumentException exc) {
            assertTrue("Exception message does not start with \"Size must be equal or greater than zero\"", exc
                    .getMessage().startsWith("Size must be equal or greater than zero"));
        } finally {
            fin.close();
        }

    }

    public void testInputStreamToByteArray_ZeroSize() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);

        try {
            byte[] out = IOUtils.toByteArray(fin, 0);
            assertNotNull("Out cannot be null", out);
            assertEquals("Out length must be 0", 0, out.length);
        } finally {
            fin.close();
        }
    }

    public void testInputStreamToByteArray_IllegalSize() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);

        try {
            IOUtils.toByteArray(fin, m_testFile.length() + 1);
            fail("IOException excepted");
        } catch (IOException exc) {
            assertTrue("Exception message does not start with \"Unexpected readed size\"",
                    exc.getMessage().startsWith("Unexpected readed size"));
        } finally {
            fin.close();
        }

    }

    public void testInputStreamToByteArray_LongSize() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);

        try {
            IOUtils.toByteArray(fin, (long) Integer.MAX_VALUE + 1);
            fail("IOException excepted");
        } catch (IllegalArgumentException exc) {
            assertTrue("Exception message does not start with \"Size cannot be greater than Integer max value\"", exc
                    .getMessage().startsWith("Size cannot be greater than Integer max value"));
        } finally {
            fin.close();
        }

    }

    public void testInputStreamToBufferedInputStream() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            InputStream in = IOUtils.toBufferedInputStream(fin);
            byte[] out = IOUtils.toByteArray(in);
            assertNotNull(out);
            assertEquals("Not all bytes were read", 0, fin.available());
            assertEquals("Wrong output size", FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fin.close();
        }
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testStringToByteArray() throws Exception {
        FileReader fin = new FileReader(m_testFile);
        try {
            // Create our String. Rely on testReaderToString() to make sure this is valid.
            String str = IOUtils.toString(fin);

            byte[] out = IOUtils.toByteArray(str);
            assertEqualContent(str.getBytes(), out);
        } finally {
            fin.close();
        }
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testByteArrayToWriter() throws Exception {
        File destination = newFile("copy7.txt");
        FileInputStream fin = new FileInputStream(m_testFile);
        byte[] in;
        try {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        } finally {
            fin.close();
        }

        FileWriter fout = new FileWriter(destination);
        try {
            CopyUtils.copy(in, fout);
            fout.flush();
            checkFile(destination, m_testFile);
            checkWrite(fout);
        } finally {
            fout.close();
        }
        deleteFile(destination);
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testByteArrayToString() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            byte[] in = IOUtils.toByteArray(fin);
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            String str = IOUtils.toString(in);
            assertEqualContent(in, str.getBytes());
        } finally {
            fin.close();
        }
    }

    public void testToByteArrayFromURI() throws Exception {
        URI url = m_testFile.toURI();
        byte[] actual = IOUtils.toByteArray(url);
        Assert.assertEquals(FILE_SIZE, actual.length);
    }

    public void testToByteArrayFromURL() throws Exception {
        URL url = m_testFile.toURI().toURL();
        byte[] actual = IOUtils.toByteArray(url);
        Assert.assertEquals(FILE_SIZE, actual.length);
    }

    public void testToByteArrayFromURLConnection() throws Exception {
        URLConnection urlConn = m_testFile.toURI().toURL().openConnection();
        byte[] actual;
        try {
            actual = IOUtils.toByteArray(urlConn);
        } finally {
            IOUtils.close(urlConn);
        }
        Assert.assertEquals(FILE_SIZE, actual.length);
    }

    /**
     * Test for {@link IOUtils#toInputStream(CharSequence)} and {@link IOUtils#toInputStream(CharSequence, String)}.
     * Note, this test utilizes on {@link IOUtils#toByteArray(java.io.InputStream)} and so relies on
     * {@link #testInputStreamToByteArray()} to ensure this method functions correctly.
     * 
     * @throws Exception
     *             on error
     */
    public void testCharSequenceToInputStream() throws Exception {
        CharSequence csq = new StringBuilder("Abc123Xyz!");
        InputStream inStream = IOUtils.toInputStream(csq);
        byte[] bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(), bytes);
        inStream = IOUtils.toInputStream(csq, (String) null);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes(), bytes);
        inStream = IOUtils.toInputStream(csq, "UTF-8");
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(csq.toString().getBytes("UTF-8"), bytes);
    }

    /**
     * Test for {@link IOUtils#toInputStream(String)} and {@link IOUtils#toInputStream(String, String)}. Note, this test
     * utilizes on {@link IOUtils#toByteArray(java.io.InputStream)} and so relies on
     * {@link #testInputStreamToByteArray()} to ensure this method functions correctly.
     * 
     * @throws Exception
     *             on error
     */
    public void testStringToInputStream() throws Exception {
        String str = "Abc123Xyz!";
        InputStream inStream = IOUtils.toInputStream(str);
        byte[] bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(), bytes);
        inStream = IOUtils.toInputStream(str, (String) null);
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes(), bytes);
        inStream = IOUtils.toInputStream(str, "UTF-8");
        bytes = IOUtils.toByteArray(inStream);
        assertEqualContent(str.getBytes("UTF-8"), bytes);
    }

    @SuppressWarnings("deprecation")
    // testing deprecated method
    public void testByteArrayToOutputStream() throws Exception {
        File destination = newFile("copy8.txt");
        FileInputStream fin = new FileInputStream(m_testFile);
        byte[] in;
        try {
            // Create our byte[]. Rely on testInputStreamToByteArray() to make sure this is valid.
            in = IOUtils.toByteArray(fin);
        } finally {
            fin.close();
        }

        FileOutputStream fout = new FileOutputStream(destination);
        try {
            CopyUtils.copy(in, fout);

            fout.flush();

            checkFile(destination, m_testFile);
            checkWrite(fout);
        } finally {
            fout.close();
        }
        deleteFile(destination);
    }

    public void testInputStreamToCharArray() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            char[] out = IOUtils.toCharArray(fin);
            assertNotNull(out);
            assertEquals("Not all chars were read", 0, fin.available());
            assertEquals("Wrong output size", FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fin.close();
        }
    }

    public void testInputStreamToCharArrayWithEncoding() throws Exception {
        FileInputStream fin = new FileInputStream(m_testFile);
        try {
            char[] out = IOUtils.toCharArray(fin, "UTF-8");
            assertNotNull(out);
            assertEquals("Not all chars were read", 0, fin.available());
            assertEquals("Wrong output size", FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fin.close();
        }
    }

    public void testReaderToCharArray() throws Exception {
        FileReader fr = new FileReader(m_testFile);
        try {
            char[] out = IOUtils.toCharArray(fr);
            assertNotNull(out);
            assertEquals("Wrong output size", FILE_SIZE, out.length);
            assertEqualContent(out, m_testFile);
        } finally {
            fr.close();
        }
    }

    // -----------------------------------------------------------------------
    public void testReadLines_InputStream() throws Exception {
        File file = newFile("lines.txt");
        InputStream in = null;
        try {
            String[] data = new String[] { "hello", "world", "", "this is", "some text" };
            createLineBasedFile(file, data);

            in = new FileInputStream(file);
            List<String> lines = IOUtils.readLines(in);
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            deleteFile(file);
        }
    }

    // -----------------------------------------------------------------------
    public void testReadLines_InputStream_String() throws Exception {
        File file = newFile("lines.txt");
        InputStream in = null;
        try {
            String[] data = new String[] { "hello", "/u1234", "", "this is", "some text" };
            createLineBasedFile(file, data);

            in = new FileInputStream(file);
            List<String> lines = IOUtils.readLines(in, "UTF-8");
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            deleteFile(file);
        }
    }

    // -----------------------------------------------------------------------
    public void testReadLines_Reader() throws Exception {
        File file = newFile("lines.txt");
        Reader in = null;
        try {
            String[] data = new String[] { "hello", "/u1234", "", "this is", "some text" };
            createLineBasedFile(file, data);

            in = new InputStreamReader(new FileInputStream(file));
            List<String> lines = IOUtils.readLines(in);
            assertEquals(Arrays.asList(data), lines);
            assertEquals(-1, in.read());
        } finally {
            IOUtils.closeQuietly(in);
            deleteFile(file);
        }
    }

    public void testSkipStream() throws Exception {
        final int size = 1027;

        InputStream input = new ByteArrayInputStream(new byte[size]);
        try {
            IOUtils.skipFully(input, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        IOUtils.skipFully(input, 0);
        IOUtils.skipFully(input, size - 1);
        try {
            IOUtils.skipFully(input, 2);
            fail("Should have failed with IOException");
        } catch (IOException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);

    }

    public void testSkipReader() throws Exception {
        final int size = 1027;

        Reader input = new CharArrayReader(new char[size]);
        IOUtils.skipFully(input, 0);
        IOUtils.skipFully(input, size - 3);
        try {
            IOUtils.skipFully(input, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            IOUtils.skipFully(input, 5);
            fail("Should have failed with IOException");
        } catch (IOException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);
    }

    public void testSkipFileReader() throws Exception {
        FileReader in = new FileReader(m_testFile);
        try {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        } finally {
            in.close();
        }
    }

    public void testSkipFileInput() throws Exception {
        InputStream in = new FileInputStream(m_testFile);
        try {
            assertEquals(FILE_SIZE - 10, IOUtils.skip(in, FILE_SIZE - 10));
            assertEquals(10, IOUtils.skip(in, 20));
            assertEquals(0, IOUtils.skip(in, 10));
        } finally {
            in.close();
        }
    }

    private void testURIToString(String encoding) throws Exception {
        URI url = m_testFile.toURI();
        String out = IOUtils.toString(url, encoding);
        assertNotNull(out);
        assertEquals("Wrong output size", FILE_SIZE, out.length());
    }

    public void testURIToStringNoEncoding() throws Exception {
        URI url = m_testFile.toURI();
        String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals("Wrong output size", FILE_SIZE, out.length());
    }

    public void testURIToStringNullEncoding() throws Exception {
        testURIToString(null);
    }

    public void testURIToStringUsAciiEncoding() throws Exception {
        testURIToString("US-ASCII");
    }

    private void testURLToString(String encoding) throws Exception {
        URL url = m_testFile.toURI().toURL();
        String out = IOUtils.toString(url, encoding);
        assertNotNull(out);
        assertEquals("Wrong output size", FILE_SIZE, out.length());
    }

    public void testURLToStringNoEncoding() throws Exception {
        URL url = m_testFile.toURI().toURL();
        String out = IOUtils.toString(url);
        assertNotNull(out);
        assertEquals("Wrong output size", FILE_SIZE, out.length());
    }

    public void testURLToStringNullEncoding() throws Exception {
        testURLToString(null);
    }

    public void testURLToStringUsAciiEncoding() throws Exception {
        testURLToString("US-ASCII");
    }

    public void testContentEqualsIgnoreEOL() throws Exception {
        Reader r1;
        Reader r2;

        r1 = new CharArrayReader("".toCharArray());
        r2 = new CharArrayReader("".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("1".toCharArray());
        r2 = new CharArrayReader("1".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("1".toCharArray());
        r2 = new CharArrayReader("2".toCharArray());
        assertFalse(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("123\rabc".toCharArray());
        r2 = new CharArrayReader("123\nabc".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));

        r1 = new CharArrayReader("321".toCharArray());
        r2 = new CharArrayReader("321\r\n".toCharArray());
        assertTrue(IOUtils.contentEqualsIgnoreEOL(r1, r2));
    }

    public void testContentEqualsReaderReader() throws Exception {
        assertTrue(IOUtils.contentEquals(new StringReader(""), new StringReader("")));
        assertTrue(IOUtils.contentEquals(new BufferedReader(new StringReader("")), new BufferedReader(new StringReader(""))));
        assertTrue(IOUtils.contentEquals(new StringReader("ABC"), new StringReader("ABC")));
        assertFalse(IOUtils.contentEquals(new StringReader("ABCD"), new StringReader("ABC")));
        assertFalse(IOUtils.contentEquals(new StringReader("ABC"), new StringReader("ABCD")));
    }

    public void testReadStream() throws Exception {
        final int size = 1027;

        byte[] buffer = new byte[size];

        InputStream input = new ByteArrayInputStream(new byte[size]);
        try {
            IOUtils.readFully(input, buffer, 0, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        IOUtils.readFully(input, buffer, 0, 0);
        IOUtils.readFully(input, buffer, 0, size - 1);
        try {
            IOUtils.readFully(input, buffer, 0, 2);
            fail("Should have failed with EOFxception");
        } catch (EOFException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);

    }

    public void testReadReader() throws Exception {
        final int size = 1027;

        char[] buffer = new char[size];

        Reader input = new CharArrayReader(new char[size]);
        IOUtils.readFully(input, buffer, 0, 0);
        IOUtils.readFully(input, buffer, 0, size - 3);
        try {
            IOUtils.readFully(input, buffer, 0, -1);
            fail("Should have failed with IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            IOUtils.readFully(input, buffer, 0, 5);
            fail("Should have failed with EOFException");
        } catch (EOFException expected) {
            // expected
        }
        IOUtils.closeQuietly(input);
    }
    
    public void testReadReaderWithOffset() throws Exception {
        Reader reader = new StringReader("abcd1234");
        char[] buffer = "wx00000000".toCharArray();
        IOUtils.readFully(reader, buffer, 2, 8);
        assertEquals("wxabcd1234", new String(buffer));
        IOUtils.closeQuietly(reader);
    }
    
    public void testReadStreamWithOffset() throws Exception {
        byte[] bytes = "abcd1234".getBytes("UTF-8");
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        byte[] buffer = "wx00000000".getBytes("UTF-8");
        IOUtils.readFully(stream, buffer, 2, 8);
        assertEquals("wxabcd1234", new String(buffer, 0, buffer.length, "UTF-8"));
        IOUtils.closeQuietly(stream);
    }

    // Tests from IO-305
    
    private byte[] iarr = null;
    
    public void testNoSkip() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream( iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method 
            assertEquals(100, IOUtils.copyLarge( is, os, 0, 100));
            byte[] oarr = os.toByteArray();
            
            // check that output length is correct
            assertEquals( 100, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals( -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testSkip() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream( iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method 
            assertEquals(100, IOUtils.copyLarge( is, os, 10, 100));
            byte[] oarr = os.toByteArray();
            
            // check that output length is correct
            assertEquals( 100, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 11, oarr[1] );
            assertEquals( 79, oarr[69] );
            assertEquals( -1, oarr[70] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testSkipInvalid() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream( iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method 
            IOUtils.copyLarge( is, os, 1000, 100);
            fail( "Should have thrown EOFException");
        }
        catch( EOFException eofe){
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testFullLength() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream( iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method 
            assertEquals(200, IOUtils.copyLarge( is, os, 0, -1));
            byte[] oarr = os.toByteArray();
            
            // check that output length is correct
            assertEquals( 200, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals( -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testExtraLength() throws IOException {
        ByteArrayInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            // Create streams
            is = new ByteArrayInputStream( iarr);
            os = new ByteArrayOutputStream();

            // Test our copy method
            // for extra length, it reads till EOF
            assertEquals(200, IOUtils.copyLarge( is, os, 0, 2000));
            byte[] oarr = os.toByteArray();
            
            // check that output length is correct
            assertEquals( 200, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals( -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    private char[] carr = null;

    public void testCharNoSkip() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader( carr);
            os = new CharArrayWriter();

            // Test our copy method 
            assertEquals(100, IOUtils.copyLarge( is, os, 0, 100));
            char[] oarr = os.toCharArray();
            
            // check that output length is correct
            assertEquals( 100, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals((char) -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testCharSkip() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader( carr);
            os = new CharArrayWriter();

            // Test our copy method 
            assertEquals(100, IOUtils.copyLarge( is, os, 10, 100));
            char[] oarr = os.toCharArray();
            
            // check that output length is correct
            assertEquals( 100, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 11, oarr[1] );
            assertEquals( 79, oarr[69] );
            assertEquals((char) -1, oarr[70] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testCharSkipInvalid() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader( carr);
            os = new CharArrayWriter();

            // Test our copy method 
            IOUtils.copyLarge( is, os, 1000, 100);
            fail( "Should have thrown EOFException");
        }
        catch( EOFException eofe){
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testCharFullLength() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader( carr);
            os = new CharArrayWriter();

            // Test our copy method 
            assertEquals(200, IOUtils.copyLarge( is, os, 0, -1));
            char[] oarr = os.toCharArray();
            
            // check that output length is correct
            assertEquals( 200, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals((char) -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public void testCharExtraLength() throws IOException {
        CharArrayReader is = null;
        CharArrayWriter os = null;
        try {
            // Create streams
            is = new CharArrayReader( carr);
            os = new CharArrayWriter();

            // Test our copy method
            // for extra length, it reads till EOF
            assertEquals(200, IOUtils.copyLarge( is, os, 0, 2000));
            char[] oarr = os.toCharArray();
            
            // check that output length is correct
            assertEquals( 200, oarr.length );
            // check that output data corresponds to input data
            assertEquals( 1, oarr[1] );
            assertEquals( 79, oarr[79] );
            assertEquals((char) -1, oarr[80] );
            
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }
}
