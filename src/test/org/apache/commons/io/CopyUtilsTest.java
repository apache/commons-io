/* ====================================================================
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
 */
package org.apache.commons.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.testtools.YellOnCloseInputStream;
import org.apache.commons.io.testtools.YellOnFlushAndCloseOutputStream;
import org.apache.commons.io.testtools.FileBasedTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * JUnit tests for CopyUtils.
 * 
 * @author Jeff Turner
 * @author Matthew Hawthorne
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: CopyUtilsTest.java,v 1.2 2003/10/13 07:06:04 rdonkin Exp $
 * @see CopyUtils
 */
public class CopyUtilsTest extends FileBasedTestCase {

    /*
     * NOTE this is not particularly beautiful code. A better way to check for
     * flush and close status would be to implement "trojan horse" wrapper
     * implementations of the various stream classes, which set a flag when
     * relevant methods are called. (JT)
     */

    private final int FILE_SIZE = 1024 * 4 + 1;


    private byte[] inData = generateTestData(FILE_SIZE);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(CopyUtilsTest.class);
    }

    public CopyUtilsTest(String testName) {
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

    public void testCopy_byteArrayToOutputStream() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        CopyUtils.copy(inData, out);

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_byteArrayToWriter() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        Writer writer = new java.io.OutputStreamWriter(baout, "US-ASCII");
        
        CopyUtils.copy(inData, writer);
        writer.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_inputStreamToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        int count = CopyUtils.copy(in, out);
        
        assertTrue("Not all bytes were read", in.available() == 0);
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_inputStreamToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        Writer writer = new java.io.OutputStreamWriter(baout, "US-ASCII");
        
        CopyUtils.copy(in, writer);
        writer.flush();

        assertTrue("Not all bytes were read", in.available() == 0);
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_readerToOutputStream() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        Reader reader = new java.io.InputStreamReader(in, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        
        CopyUtils.copy(reader, out);
        //Note: this method *does* flush. It is equivalent to:
        //  final OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( fin, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;

        // Note: rely on the method to flush
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_readerToWriter() throws Exception {
        InputStream in = new ByteArrayInputStream(inData);
        in = new YellOnCloseInputStream(in);
        Reader reader = new java.io.InputStreamReader(in, "US-ASCII");

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        Writer writer = new java.io.OutputStreamWriter(baout, "US-ASCII");

        int count = CopyUtils.copy(reader, writer);
        writer.flush();
        assertEquals(
            "The number of characters returned by copy is wrong",
            inData.length,
            count);
        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_stringToOutputStream() throws Exception {
        final String str = new String(inData, "US-ASCII");
        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);

        CopyUtils.copy(str, out);
        //Note: this method *does* flush. It is equivalent to:
        //  final OutputStreamWriter _out = new OutputStreamWriter(fout);
        //  IOUtils.copy( str, _out, 4096 ); // copy( Reader, Writer, int );
        //  _out.flush();
        //  out = fout;
        // note: we don't flush here; this IOUtils method does it for us

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

    public void testCopy_stringToWriter() throws Exception {
        final String str = new String(inData, "US-ASCII");

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new YellOnFlushAndCloseOutputStream(baout, false, true);
        Writer writer = new java.io.OutputStreamWriter(baout, "US-ASCII");

        CopyUtils.copy(str, writer);
        writer.flush();

        assertEquals("Sizes differ", inData.length, baout.size());
        assertTrue("Content differs", Arrays.equals(inData, baout.toByteArray()));
    }

} // CopyUtilsTest
