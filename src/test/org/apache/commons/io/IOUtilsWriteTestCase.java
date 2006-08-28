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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.testtools.FileBasedTestCase;
import org.apache.commons.io.testtools.YellOnFlushAndCloseOutputStream;

/**
 * JUnit tests for IOUtils write methods.
 * 
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @author Jeremias Maerki
 * @author Stephen Colebourne
 * @version $Id$
 * @see IOUtils
 */
public class IOUtilsWriteTestCase extends FileBasedTestCase {

    private static final int FILE_SIZE = 1024 * 4 + 1;


    private byte[] inData = generateTestData(FILE_SIZE);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(IOUtilsWriteTestCase.class);
    }

    public IOUtilsWriteTestCase(String testName) {
        super(testName);
    }

    // ----------------------------------------------------------------
    // Setup
    // ----------------------------------------------------------------

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    // ----------------------------------------------------------------
    // Tests
    // ----------------------------------------------------------------

    //-----------------------------------------------------------------------
    public void testWrite_byteArrayToOutputStream() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write(inData, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_byteArrayToOutputStream_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write((byte[]) null, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_byteArrayToOutputStream_nullStream() throws Exception {
        try {
            IOUtils.write(inData, (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWrite_byteArrayToWriter() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write(inData, writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_byteArrayToWriter_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write((byte[]) null, writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_byteArrayToWriter_nullWriter() throws Exception {
        try {
            IOUtils.write(inData, (Writer) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWrite_byteArrayToWriter_Encoding() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write(inData, writer, "UTF8");
        out.off();
        writer.flush();

        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF8").getBytes("US-ASCII");
        assertTrue("Content differs", Arrays.equals(inData, bytes));
    }

    public void testWrite_byteArrayToWriter_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write((byte[]) null, writer, "UTF8");
        out.off();
        writer.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_byteArrayToWriter_Encoding_nullWriter() throws Exception {
        try {
            IOUtils.write(inData, (Writer) null, "UTF8");
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testWrite_byteArrayToWriter_Encoding_nullEncoding() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write(inData, writer, null);
        out.off();
        writer.flush();
        
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    //-----------------------------------------------------------------------
    public void testWrite_stringToOutputStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write(str, out);
        out.off();
        out.flush();
        
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_stringToOutputStream_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write((String) null, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_stringToOutputStream_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWrite_stringToOutputStream_Encoding() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str, out, "UTF16");
        out.off();
        out.flush();
        
        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue("Content differs", Arrays.equals(inData, bytes));
    }

    public void testWrite_stringToOutputStream_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write((String) null, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_stringToOutputStream_Encoding_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testWrite_stringToOutputStream_nullEncoding() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str, out, null);
        out.off();
        out.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    //-----------------------------------------------------------------------
    public void testWrite_stringToWriter() throws Exception {
        String str = new String(inData, "US-ASCII");

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(str, writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_stringToWriter_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write((String) null, writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_stringToWriter_Encoding_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str, (Writer) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWrite_charArrayToOutputStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_charArrayToOutputStream_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write((char[]) null, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_charArrayToOutputStream_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWrite_charArrayToOutputStream_Encoding() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out, "UTF16");
        out.off();
        out.flush();
        
        byte[] bytes = baout.toByteArray();
        bytes = new String(bytes, "UTF16").getBytes("US-ASCII");
        assertTrue("Content differs", Arrays.equals(inData, bytes));
    }

    public void testWrite_charArrayToOutputStream_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        
        IOUtils.write((char[]) null, out);
        out.off();
        out.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_charArrayToOutputStream_Encoding_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testWrite_charArrayToOutputStream_nullEncoding() throws Exception {
        String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);

        IOUtils.write(str.toCharArray(), out, null);
        out.off();
        out.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    //-----------------------------------------------------------------------
    public void testWrite_charArrayToWriter() throws Exception {
        String str = new String(inData, "US-ASCII");

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");

        IOUtils.write(str.toCharArray(), writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testWrite_charArrayToWriter_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.write((char[]) null, writer);
        out.off();
        writer.flush();

        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWrite_charArrayToWriter_Encoding_nullStream() throws Exception {
        String str = new String(inData, "US-ASCII");
        try {
            IOUtils.write(str.toCharArray(), (Writer) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWriteLines_OutputStream() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines(list, "*", out);
        
        out.off();
        out.flush();
        
        String expected = "hello*world**this is**some text*";
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    public void testWriteLines_OutputStream_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines((List) null, "*", out);
        out.off();
        out.flush();
        
        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWriteLines_OutputStream_nullSeparator() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
            
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines(list, (String) null, out);
        out.off();
        out.flush();
        
        String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    public void testWriteLines_OutputStream_nullStream() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", (OutputStream) null);
            fail();
        } catch (NullPointerException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testWriteLines_OutputStream_Encoding() throws Exception {
        Object[] data = new Object[] {
            "hello\u8364", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines(list, "*", out, "UTF-8");
        
        out.off();
        out.flush();
        
        String expected = "hello\u8364*world**this is**some text*";
        String actual = baout.toString("UTF-8");
        assertEquals(expected, actual);
    }

    public void testWriteLines_OutputStream_Encoding_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines((List) null, "*", out, "US-ASCII");
        out.off();
        out.flush();
        
        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWriteLines_OutputStream_Encoding_nullSeparator() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
            
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines(list, (String) null, out, "US-ASCII");
        out.off();
        out.flush();
        
        String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    public void testWriteLines_OutputStream_Encoding_nullStream() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", (OutputStream) null, "US-ASCII");
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testWriteLines_OutputStream_Encoding_nullEncoding() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        IOUtils.writeLines(list, "*", out, null);
        
        out.off();
        out.flush();
        
        String expected = "hello*world**this is**some text*";
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    //-----------------------------------------------------------------------
    public void testWriteLines_Writer() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.writeLines(list, "*", writer);
        
        out.off();
        writer.flush();
        
        String expected = "hello*world**this is**some text*";
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    public void testWriteLines_Writer_nullData() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.writeLines((List) null, "*", writer);
        out.off();
        writer.flush();
        
        assertEquals("Sizes differ", 0, baout.size());
    }

    public void testWriteLines_Writer_nullSeparator() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
            
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        YellOnFlushAndCloseOutputStream out = new YellOnFlushAndCloseOutputStream(baout, true, true);
        Writer writer = new OutputStreamWriter(baout, "US-ASCII");
        
        IOUtils.writeLines(list, (String) null, writer);
        out.off();
        writer.flush();
        
        String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR;
        String actual = baout.toString();
        assertEquals(expected, actual);
    }

    public void testWriteLines_Writer_nullStream() throws Exception {
        Object[] data = new Object[] {"hello", "world"};
        List list = Arrays.asList(data);
        try {
            IOUtils.writeLines(list, "*", (Writer) null);
            fail();
        } catch (NullPointerException ex) {}
    }

}
