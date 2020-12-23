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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.NullReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.io.test.ThrowOnCloseInputStream;
import org.apache.commons.io.test.ThrowOnFlushAndCloseOutputStream;
import org.junit.jupiter.api.Test;

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
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        final int count = IOUtils.copy(in, out);

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
        assertEquals(inData.length,count);
    }

    /*
     * Test Copying file > 2GB  - see issue# IO-84
     */
    @Test
    public void testCopy_inputStreamToOutputStream_IO84() throws Exception {
        final long size = (long)Integer.MAX_VALUE + (long)1;
        final InputStream  in  = new NullInputStream(size);
        final OutputStream out = NullOutputStream.NULL_OUTPUT_STREAM;

        // Test copy() method
        assertEquals(-1, IOUtils.copy(in, out));

        // reset the input
        in.close();

        // Test copyLarge() method
        assertEquals(size, IOUtils.copyLarge(in, out), "copyLarge()");
    }

    @Test
    public void testCopy_inputStreamToOutputStream_nullIn() throws Exception {
        final OutputStream out = new ByteArrayOutputStream();
        assertEquals(0, IOUtils.copy((InputStream) null, out));
    }

    @Test
    public void testCopy_inputStreamToOutputStream_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(in, (OutputStream) null));
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
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        final long count = IOUtils.copy(in, out, bufferSize);

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
        assertEquals(inData.length,count);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final ThrowOnFlushAndCloseOutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, StandardCharsets.US_ASCII);

        IOUtils.copy(in, writer); // deliberately testing deprecated method
        out.off();
        writer.flush();

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter_Encoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final ThrowOnFlushAndCloseOutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, StandardCharsets.US_ASCII);

        IOUtils.copy(in, writer, "UTF8");
        out.off();
        writer.flush();

        assertEquals(0, in.available(), "Not all bytes were read");
        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, StandardCharsets.UTF_8).getBytes(StandardCharsets.US_ASCII);
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToWriter_Encoding_nullEncoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final ThrowOnFlushAndCloseOutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, StandardCharsets.US_ASCII);

        IOUtils.copy(in, writer, (String) null);
        out.off();
        writer.flush();

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testCopy_inputStreamToWriter_Encoding_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(null, writer, "UTF8"));
    }

    @Test
    public void testCopy_inputStreamToWriter_Encoding_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(in, null, "UTF8"));
    }

    @SuppressWarnings("deprecation") // deliberately testing deprecated method
    @Test
    public void testCopy_inputStreamToWriter_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy((InputStream) null, writer));
    }

    @SuppressWarnings("deprecation") // deliberately testing deprecated method
    @Test
    public void testCopy_inputStreamToWriter_nullOut() throws Exception {
        final InputStream in = new ByteArrayInputStream(inData);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(in, (Writer) null)); // deliberately testing deprecated method
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToAppendable() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final ThrowOnFlushAndCloseOutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, StandardCharsets.US_ASCII);

        final long count = IOUtils.copy(reader, (Appendable) writer);
        out.off();
        writer.flush();
        assertEquals(inData.length, count, "The number of characters returned by copy is wrong");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testCopy_readerToAppendable_IO84() throws Exception {
        final long size = (long) Integer.MAX_VALUE + (long) 1;
        final Reader reader = new NullReader(size);
        final NullWriter writer = new NullWriter();

        // Test copy() method
        assertEquals(size, IOUtils.copy(reader, (Appendable) writer));

        // reset the input
        reader.close();

        // Test copyLarge() method
        assertEquals(size, IOUtils.copyLarge(reader, writer), "copy()");

    }

    @Test
    public void testCopy_readerToAppendable_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Appendable writer = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy((Reader) null, writer));
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToAppendable_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(reader, (Appendable) null));
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out); // deliberately testing deprecated method
        //Note: this method *does* flush. It is equivalent to:
        //  OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( fin, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;

        // Note: rely on the method to flush
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_Encoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out, "UTF16");
        // note: this method *does* flush.
        // note: we don't flush here; this IOUtils method does it for us

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, StandardCharsets.UTF_16).getBytes(StandardCharsets.US_ASCII);
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_Encoding_nullEncoding() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.copy(reader, out, (String) null);
        // note: this method *does* flush.
        // note: we don't flush here; this IOUtils method does it for us

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testCopy_readerToOutputStream_Encoding_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(null, out, "UTF16"));
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_Encoding_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(reader, null, "UTF16"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCopy_readerToOutputStream_nullIn() throws Exception { // deliberately testing deprecated method
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        assertThrows(NullPointerException.class, () -> IOUtils.copy((Reader) null, out));
    }

    @SuppressWarnings({ "resource", "deprecation" }) // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(reader, (OutputStream) null)); // deliberately testing deprecated method
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final ThrowOnFlushAndCloseOutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, StandardCharsets.US_ASCII);

        final int count = IOUtils.copy(reader, writer);
        out.off();
        writer.flush();
        assertEquals(inData.length, count, "The number of characters returned by copy is wrong");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
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
        assertEquals(size, IOUtils.copyLarge(reader, writer), "copyLarge()");

    }

    @Test
    public void testCopy_readerToWriter_nullIn() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy((Reader) null, writer));
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToWriter_nullOut() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
        assertThrows(NullPointerException.class, () -> IOUtils.copy(reader, (Writer) null));
    }

}
