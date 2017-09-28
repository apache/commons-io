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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.NullReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.testtools.TestUtils;
import org.apache.commons.io.testtools.YellOnCloseInputStream;
import org.apache.commons.io.testtools.YellOnFlushAndCloseOutputStream;
import org.junit.Test;

/**
 * JUnit tests for IOUtils copy methods.
 *
 * @see IOUtils
 */
public class IOUtilsCopyTestCase {

    /*
     * NOTE this is not particularly beautiful code. A better way to check for
     * flush and close status would be to implement "trojan horse" wrapper
     * implementations of the various stream classes, which set a flag when
     * relevant methods are called. (JT)
     */

    private static final int FILE_SIZE = 1024 * 4 + 1;


    private final byte[] inData = TestUtils.generateTestData(FILE_SIZE);

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        final int count = IOUtils.copy(in, out);

        assertEquals("Not all bytes were read", 0, in.available());
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
        assertEquals(inData.length,count);
    }

    @Test
    public void testCopy_inputStreamToOutputStreamWithBufferSize() throws Exception {
        testCopy_inputStreamToOutputStreamWithBufferSize(1);
        testCopy_inputStreamToOutputStreamWithBufferSize(2);
        testCopy_inputStreamToOutputStreamWithBufferSize(4);
        testCopy_inputStreamToOutputStreamWithBufferSize(8);
        testCopy_inputStreamToOutputStreamWithBufferSize(16);
        testCopy_inputStreamToOutputStreamWithBufferSize(32);
        testCopy_inputStreamToOutputStreamWithBufferSize(64);
        testCopy_inputStreamToOutputStreamWithBufferSize(128);
        testCopy_inputStreamToOutputStreamWithBufferSize(256);
        testCopy_inputStreamToOutputStreamWithBufferSize(512);
        testCopy_inputStreamToOutputStreamWithBufferSize(1024);
        testCopy_inputStreamToOutputStreamWithBufferSize(2048);
        testCopy_inputStreamToOutputStreamWithBufferSize(4096);
        testCopy_inputStreamToOutputStreamWithBufferSize(8192);
        testCopy_inputStreamToOutputStreamWithBufferSize(16384);
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    private void testCopy_inputStreamToOutputStreamWithBufferSize(final int bufferSize) throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        final long count = IOUtils.copy(in, out, bufferSize);

        assertEquals("Not all bytes were read", 0, in.available());
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
        assertEquals(inData.length,count);
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToOutputStream_nullIn() throws Exception {
        final OutputStream out = new ByteArrayOutputStream();
        IOUtils.copy((InputStream) null, out);
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToOutputStream_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        IOUtils.copy(in, (OutputStream) null);
    }

    /*
     * Test Copying file > 2GB  - see issue# IO-84
     */
    @Test
    public void testCopy_inputStreamToOutputStream_IO84() throws Exception {
        final long size = (long)Integer.MAX_VALUE + (long)1;
        final InputStream  in  = new NullInputStream(size);
        final OutputStream out = new NullOutputStream();

        // Test copy() method
        assertEquals(-1, IOUtils.copy(in, out));

        // reset the input
        in.close();

        // Test copyLarge() method
        assertEquals("copyLarge()", size, IOUtils.copyLarge(in, out));
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.copy(in, writer); // deliberately testing deprecated method
        out.off();
        writer.flush();

        assertEquals("Not all bytes were read", 0, in.available());
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    @SuppressWarnings("deprecation") // deliberately testing deprecated method
    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToWriter_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, "US-ASCII");
        IOUtils.copy((InputStream) null, writer);
    }

    @SuppressWarnings("deprecation") // deliberately testing deprecated method
    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToWriter_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        IOUtils.copy(in, (Writer) null); // deliberately testing deprecated method
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter_Encoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.copy(in, writer, "UTF8");
        out.off();
        writer.flush();

        assertEquals("Not all bytes were read", 0, in.available());
        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF8").getBytes("US-ASCII");
        assertTrue("Content differs", Arrays.equals(inData, bytes));
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToWriter_Encoding_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, "US-ASCII");
        IOUtils.copy(null, writer, "UTF8");
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_inputStreamToWriter_Encoding_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        IOUtils.copy(in, null, "UTF8");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter_Encoding_nullEncoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.copy(in, writer, (String) null);
        out.off();
        writer.flush();

        assertEquals("Not all bytes were read", 0, in.available());
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out); // deliberately testing deprecated method
        //Note: this method *does* flush. It is equivalent to:
        //  OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( fin, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;

        // Note: rely on the method to flush
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    @SuppressWarnings("deprecation")
    @Test(expected = NullPointerException.class)
    public void testCopy_readerToOutputStream_nullIn() throws Exception { // deliberately testing deprecated method
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        IOUtils.copy((Reader) null, out);
    }

    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test(expected = NullPointerException.class)
    public void testCopy_readerToOutputStream_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");
        IOUtils.copy(reader, (OutputStream) null); // deliberately testing deprecated method
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_Encoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out, "UTF16");
        // note: this method *does* flush.
        // note: we don't flush here; this IOUtils method does it for us

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue("Content differs", Arrays.equals(inData, bytes));
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_readerToOutputStream_Encoding_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        IOUtils.copy(null, out, "UTF16");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test(expected = NullPointerException.class)
    public void testCopy_readerToOutputStream_Encoding_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");
        IOUtils.copy(reader, null, "UTF16");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_Encoding_nullEncoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out, (String) null);
        // note: this method *does* flush.
        // note: we don't flush here; this IOUtils method does it for us

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        final int count = IOUtils.copy(reader, writer);
        out.off();
        writer.flush();
        assertEquals("The number of characters returned by copy is wrong", inData.length, count);
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    @Test(expected = NullPointerException.class)
    public void testCopy_readerToWriter_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, "US-ASCII");
        IOUtils.copy((Reader) null, writer);
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test(expected = NullPointerException.class)
    public void testCopy_readerToWriter_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, "US-ASCII");
        IOUtils.copy(reader, (Writer) null);
    }

    /*
     * Test Copying file > 2GB  - see issue# IO-84
     */
    @Test
    public void testCopy_readerToWriter_IO84() throws Exception {
        final long size = (long)Integer.MAX_VALUE + (long)1;
        final Reader reader = new NullReader(size);
        final Writer writer = new NullWriter();

        // Test copy() method
        assertEquals(-1, IOUtils.copy(reader, writer));

        // reset the input
        reader.close();

        // Test copyLarge() method
        assertEquals("copyLarge()", size, IOUtils.copyLarge(reader, writer));

    }

}
