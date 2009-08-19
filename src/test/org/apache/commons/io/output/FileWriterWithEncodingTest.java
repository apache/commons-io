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
package org.apache.commons.io.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Tests that the encoding is actually set and used.
 *
 * @version $Revision$ $Date$
 */
public class FileWriterWithEncodingTest extends FileBasedTestCase {

    private String defaultEncoding;
    private File file1;
    private File file2;
    private String textContent;

    public FileWriterWithEncodingTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        File encodingFinder = new File(getTestDirectory(), "finder.txt");
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(encodingFinder));
            defaultEncoding = out.getEncoding();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
        }
        file1 = new File(getTestDirectory(), "testfile1.txt");
        file2 = new File(getTestDirectory(), "testfile2.txt");
        char[] arr = new char[1024];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (char) i;
        }
        textContent = new String(arr);
    }

    @Override
    public void tearDown() {
        defaultEncoding = null;
        file1.delete();
        file2.delete();
        textContent = null;
    }

    //-----------------------------------------------------------------------
    public void testSameEncoding() throws Exception {
        FileWriter fw1 = null;
        FileWriterWithEncoding fw2 = null;
        try {
            fw1 = new FileWriter(file1);  // default encoding
            fw2 = new FileWriterWithEncoding(file2, defaultEncoding);
            assertEquals(true, file1.exists());
            assertEquals(true, file2.exists());
            
            fw1.write(textContent);
            fw2.write(textContent);
            
            fw1.flush();
            fw2.flush();
            checkFile(file1, file2);
            
        } finally {
            IOUtils.closeQuietly(fw1);
            IOUtils.closeQuietly(fw2);
        }
        assertEquals(true, file1.exists());
        assertEquals(true, file2.exists());
    }

    public void testDifferentEncoding() throws Exception {
        Map map = Charset.availableCharsets();
        if (map.containsKey("UTF-16BE")) {
            FileWriter fw1 = null;
            FileWriterWithEncoding fw2 = null;
            try {
                fw1 = new FileWriter(file1);  // default encoding
                fw2 = new FileWriterWithEncoding(file2, defaultEncoding);
                assertEquals(true, file1.exists());
                assertEquals(true, file2.exists());
                
                fw1.write(textContent);
                fw2.write(textContent);
                
                fw1.flush();
                fw2.flush();
                try {
                    checkFile(file1, file2);
                    fail();
                } catch (AssertionFailedError ex) {
                    // success
                }
                
            } finally {
                IOUtils.closeQuietly(fw1);
                IOUtils.closeQuietly(fw2);
            }
            assertEquals(true, file1.exists());
            assertEquals(true, file2.exists());
        }
        if (map.containsKey("UTF-16LE")) {
            FileWriter fw1 = null;
            FileWriterWithEncoding fw2 = null;
            try {
                fw1 = new FileWriter(file1);  // default encoding
                fw2 = new FileWriterWithEncoding(file2, defaultEncoding);
                assertEquals(true, file1.exists());
                assertEquals(true, file2.exists());
                
                fw1.write(textContent);
                fw2.write(textContent);
                
                fw1.flush();
                fw2.flush();
                try {
                    checkFile(file1, file2);
                    fail();
                } catch (AssertionFailedError ex) {
                    // success
                }
                
            } finally {
                IOUtils.closeQuietly(fw1);
                IOUtils.closeQuietly(fw2);
            }
            assertEquals(true, file1.exists());
            assertEquals(true, file2.exists());
        }
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_encoding_badEncoding() throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriterWithEncoding(file1, "BAD-ENCODE");
            fail();
        } catch (IOException ex) {
            // expected
            assertEquals(false, file1.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file1.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_directory() throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriterWithEncoding(getTestDirectory(), defaultEncoding);
            fail();
        } catch (IOException ex) {
            // expected
            assertEquals(false, file1.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file1.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_File_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriterWithEncoding((File) null, defaultEncoding);
            fail();
        } catch (NullPointerException ex) {
            // expected
            assertEquals(false, file1.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file1.exists());
    }

    //-----------------------------------------------------------------------
    public void testConstructor_fileName_nullFile() throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriterWithEncoding((String) null, defaultEncoding);
            fail();
        } catch (NullPointerException ex) {
            // expected
            assertEquals(false, file1.exists());
        } finally {
            IOUtils.closeQuietly(writer);
        }
        assertEquals(false, file1.exists());
    }

}
