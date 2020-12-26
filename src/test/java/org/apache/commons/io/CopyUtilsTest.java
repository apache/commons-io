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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.io.test.ThrowOnCloseInputStream;
import org.apache.commons.io.test.ThrowOnFlushAndCloseOutputStream;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // these are test cases for the deprecated CopyUtils

/**
 * JUnit tests for CopyUtils.
 *
 * @see CopyUtils
 */
public class CopyUtilsTest {

    /*
     * NOTE this is not particularly beautiful code. A better way to check for
     * flush and close status would be to implement "trojan horse" wrapper
     * implementations of the various stream classes, which set a flag when
     * relevant methods are called. (JT)
     */

    private static final int FILE_SIZE = 1024 * 4 + 1;

    private final byte[] inData = TestUtils.generateTestData(FILE_SIZE);

    // ----------------------------------------------------------------
    // Tests
    // ----------------------------------------------------------------

    @Test
    public void testCtor() {
        new CopyUtils();
        // Nothing to assert, the constructor is public and does not blow up.
    }

    @Test
    public void copy_byteArrayToOutputStream() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        CopyUtils.copy(inData, out);

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void copy_byteArrayToWriter() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);
        final Writer writer = new java.io.OutputStreamWriter(out, StandardCharsets.US_ASCII);

        CopyUtils.copy(inData, writer);
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testCopy_byteArrayToWriterWithEncoding() throws Exception {
        final String inDataStr = "data";
        final String charsetName = "UTF-8";
        final StringWriter writer = new StringWriter();
        CopyUtils.copy(inDataStr.getBytes(charsetName), writer, charsetName);
        assertEquals(inDataStr, writer.toString());
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_inputStreamToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        final int count = CopyUtils.copy(in, out);

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
        assertEquals(inData.length, count);
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void copy_inputStreamToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);
        final Writer writer = new java.io.OutputStreamWriter(out, StandardCharsets.US_ASCII);

        CopyUtils.copy(in, writer);
        writer.flush();

        assertEquals(0, in.available(), "Not all bytes were read");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void copy_inputStreamToWriterWithEncoding() throws Exception {
        final String inDataStr = "data";
        final String charsetName = "UTF-8";
        final StringWriter writer = new StringWriter();
        CopyUtils.copy(new ByteArrayInputStream(inDataStr.getBytes(charsetName)), writer, charsetName);
        assertEquals(inDataStr, writer.toString());
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void testCopy_readerToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new java.io.InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        CopyUtils.copy(reader, out);
        //Note: this method *does* flush. It is equivalent to:
        //  OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( fin, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;

        // Note: rely on the method to flush
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @SuppressWarnings("resource") // 'in' is deliberately not closed
    @Test
    public void copy_readerToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new ThrowOnCloseInputStream(in);
        final Reader reader = new java.io.InputStreamReader(in, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);
        final Writer writer = new java.io.OutputStreamWriter(out, StandardCharsets.US_ASCII);

        final int count = CopyUtils.copy(reader, writer);
        writer.flush();
        assertEquals(inData.length, count, "The number of characters returned by copy is wrong");
        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void copy_stringToOutputStream() throws Exception {
        final String str = new String(inData, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);

        CopyUtils.copy(str, out);
        //Note: this method *does* flush. It is equivalent to:
        //  OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;
        // note: we don't flush here; this IOUtils method does it for us

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void copy_stringToWriter() throws Exception {
        final String str = new String(inData, StandardCharsets.US_ASCII);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final OutputStream out = new ThrowOnFlushAndCloseOutputStream(baout, false, true);
        final Writer writer = new java.io.OutputStreamWriter(out, StandardCharsets.US_ASCII);

        CopyUtils.copy(str, writer);
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

} // CopyUtilsTest
