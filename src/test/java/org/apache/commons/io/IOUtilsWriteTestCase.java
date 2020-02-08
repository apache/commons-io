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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.testtools.TestUtils;
import org.apache.commons.io.testtools.YellOnFlushAndCloseOutputStream;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests for IOUtils write methods.
 *
 * @see IOUtils
 */
@SuppressWarnings("deprecation") // includes tests for deprecated methods
public class IOUtilsWriteTestCase {

    private static final int FILE_SIZE = 1024 * 4 + 1;

    private final byte[] inData = TestUtils.generateTestData(FILE_SIZE);

    // ----------------------------------------------------------------
    // Tests
    // ----------------------------------------------------------------

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_byteArrayToOutputStream() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(inData, out);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_byteArrayToOutputStream_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((byte[]) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_byteArrayToOutputStream_nullStream() throws Exception {
        try {
            IOUtils.write(inData, (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_byteArrayToWriter() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(inData, writer);
        out.off();
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_byteArrayToWriter_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write((byte[]) null, writer);
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_byteArrayToWriter_nullWriter() throws Exception {
        try {
            IOUtils.write(inData, (Writer) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_byteArrayToWriter_Encoding() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(inData, writer, "UTF8");
        out.off();
        writer.flush();

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF8").getBytes("US-ASCII");
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @Test
    public void testWrite_byteArrayToWriter_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(null, writer, "UTF8");
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_byteArrayToWriter_Encoding_nullWriter() throws Exception {
        try {
            IOUtils.write(inData, null, "UTF8");
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testWrite_byteArrayToWriter_Encoding_nullEncoding() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(inData, writer, (String) null);
        out.off();
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charSequenceToOutputStream() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(csq, out);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_charSequenceToOutputStream_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((CharSequence) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charSequenceToOutputStream_nullStream() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));
        try {
            IOUtils.write(csq, (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charSequenceToOutputStream_Encoding() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(csq, out, "UTF16");
        out.off();
        out.flush();

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @Test
    public void testWrite_charSequenceToOutputStream_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((CharSequence) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charSequenceToOutputStream_Encoding_nullStream() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));
        try {
            IOUtils.write(csq, (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testWrite_charSequenceToOutputStream_nullEncoding() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(csq, out, (String) null);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charSequenceToWriter() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(csq, writer);
        out.off();
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_charSequenceToWriter_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write((CharSequence) null, writer);
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charSequenceToWriter_Encoding_nullStream() throws Exception {
        final CharSequence csq = new StringBuilder(new String(inData, "US-ASCII"));
        try {
            IOUtils.write(csq, (Writer) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_stringToOutputStream() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str, out);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_stringToOutputStream_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((String) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_stringToOutputStream_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_stringToOutputStream_Encoding() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str, out, "UTF16");
        out.off();
        out.flush();

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @Test
    public void testWrite_stringToOutputStream_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((String) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_stringToOutputStream_Encoding_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testWrite_stringToOutputStream_nullEncoding() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str, out, (String) null);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_stringToWriter() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(str, writer);
        out.off();
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_stringToWriter_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write((String) null, writer);
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_stringToWriter_Encoding_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (Writer) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charArrayToOutputStream() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_charArrayToOutputStream_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((char[]) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charArrayToOutputStream_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charArrayToOutputStream_Encoding() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out, "UTF16");
        out.off();
        out.flush();

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue(Arrays.equals(inData, bytes), "Content differs");
    }

    @Test
    public void testWrite_charArrayToOutputStream_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write((char[]) null, out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charArrayToOutputStream_Encoding_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testWrite_charArrayToOutputStream_nullEncoding() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out, (String) null);
        out.off();
        out.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrite_charArrayToWriter() throws Exception {
        final String str = new String(inData, "US-ASCII");

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(str.toCharArray(), writer);
        out.off();
        writer.flush();

        assertEquals(inData.length, baout.size(), "Sizes differ");
        assertTrue(Arrays.equals(inData, baout.toByteArray()), "Content differs");
    }

    @Test
    public void testWrite_charArrayToWriter_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write((char[]) null, writer);
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWrite_charArrayToWriter_Encoding_nullStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (Writer) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWriteLines_OutputStream() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(list, "*", out);

        out.off();
        out.flush();

        final String expected = "hello*world**this is**some text*";
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_OutputStream_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(null, "*", out);
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWriteLines_OutputStream_nullSeparator() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(list, null, out);
        out.off();
        out.flush();

        final String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_OutputStream_nullStream() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", (OutputStream) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWriteLines_OutputStream_Encoding() throws Exception {
        final Object[] data = new Object[]{
                "hello\u8364", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(list, "*", out, "UTF-8");

        out.off();
        out.flush();

        final String expected = "hello\u8364*world**this is**some text*";
        final String actual = baout.toString("UTF-8");
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_OutputStream_Encoding_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(null, "*", out, "US-ASCII");
        out.off();
        out.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWriteLines_OutputStream_Encoding_nullSeparator() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(list, null, out, "US-ASCII");
        out.off();
        out.flush();

        final String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_OutputStream_Encoding_nullStream() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", null, "US-ASCII");
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

    @Test
    public void testWriteLines_OutputStream_Encoding_nullEncoding() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        IOUtils.writeLines(list, "*", out, (String) null);

        out.off();
        out.flush();

        final String expected = "hello*world**this is**some text*";
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWriteLines_Writer() throws Exception {
        final Object[] data = new Object[]{
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.writeLines(list, "*", writer);

        out.off();
        writer.flush();

        final String expected = "hello*world**this is**some text*";
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_Writer_nullData() throws Exception {
        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.writeLines(null, "*", writer);
        out.off();
        writer.flush();

        assertEquals(0, baout.size(), "Sizes differ");
    }

    @Test
    public void testWriteLines_Writer_nullSeparator() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);

        final ByteArrayOutputStream baout = new ByteArrayOutputStream();
        @SuppressWarnings("resource") // deliberately not closed
        final YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        final Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.writeLines(list, null, writer);
        out.off();
        writer.flush();

        final String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        final String actual = baout.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteLines_Writer_nullStream() throws Exception {
        final Object[] data = new Object[]{"hello", "world"};
        final List<Object> list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", (Writer) null);
            fail();
        } catch (final NullPointerException ignore) {
        }
    }

}
