/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

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
 * @version $Id: IOUtilsWriteTestCase.java,v 1.1 2004/08/13 23:39:01 scolebourne Exp $
 * @see IOUtils
 */
public class IOUtilsWriteTestCase extends FileBasedTestCase {

    /*
     * NOTE this is not particularly beautiful code. A better way to check for
     * flush and close status would be to implement "trojan horse" wrapper
     * implementations of the various stream classes, which set a flag when
     * relevant methods are called. (JT)
     */

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
        String str = new String(inData, "US-ASCII");
        
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
        String str = new String(inData, "US-ASCII");
        
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
        String str = new String(inData, "US-ASCII");
        
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
        String str = new String(inData, "US-ASCII");
        
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
        String str = new String(inData, "US-ASCII");
        
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
        String str = new String(inData, "US-ASCII");
        
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

}
