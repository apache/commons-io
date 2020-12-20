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

import static org.apache.commons.io.test.TestUtils.checkFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that the encoding is actually set and used.
 *
 */
public class FileWriterWithEncodingTest {

    @TempDir
    public File temporaryFolder;

    private String defaultEncoding;
    private File file1;
    private File file2;
    private String textContent;
    private final char[] anotherTestContent = new char[]{'f', 'z', 'x'};

    @BeforeEach
    public void setUp() throws Exception {
        final File encodingFinder = new File(temporaryFolder, "finder.txt");
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(encodingFinder))) {
            defaultEncoding = out.getEncoding();
        }
        file1 = new File(temporaryFolder, "testfile1.txt");
        file2 = new File(temporaryFolder, "testfile2.txt");
        final char[] arr = new char[1024];
        final char[] chars = "ABCDEFGHIJKLMNOPQabcdefgihklmnopq".toCharArray();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = chars[i % chars.length];
        }
        textContent = new String(arr);
    }

    @Test
    public void sameEncoding_string_constructor() throws Exception {
        successfulRun(new FileWriterWithEncoding(file2, defaultEncoding));
    }

    @Test
    public void sameEncoding_string_string_constructor() throws Exception {
        successfulRun(new FileWriterWithEncoding(file2.getPath(), defaultEncoding));
    }

    @Test
    public void sameEncoding_Charset_constructor() throws Exception {
        successfulRun(new FileWriterWithEncoding(file2, Charset.defaultCharset()));
    }

    @Test
    public void sameEncoding_string_Charset_constructor() throws Exception {
        successfulRun(new FileWriterWithEncoding(file2.getPath(), Charset.defaultCharset()));
    }

    @Test
    public void sameEncoding_CharsetEncoder_constructor() throws Exception {
        final CharsetEncoder enc = Charset.defaultCharset().newEncoder();
        successfulRun(new FileWriterWithEncoding(file2, enc));
    }

    @Test
    public void sameEncoding_string_CharsetEncoder_constructor() throws Exception {
        final CharsetEncoder enc = Charset.defaultCharset().newEncoder();
        successfulRun(new FileWriterWithEncoding(file2.getPath(), enc));
    }

    private void successfulRun(final FileWriterWithEncoding fw21) throws Exception {
        try (
            FileWriter fw1 = new FileWriter(file1);  // default encoding
            FileWriterWithEncoding fw2 = fw21
        ){
            writeTestPayload(fw1, fw2);
            checkFile(file1, file2);
        }
        assertTrue(file1.exists());
        assertTrue(file2.exists());
    }

    @Test
    public void testDifferentEncoding() throws Exception {
        if (Charset.isSupported("UTF-16BE")) {
            try (
                FileWriter fw1 = new FileWriter(file1);  // default encoding
                FileWriterWithEncoding fw2 = new FileWriterWithEncoding(file2, defaultEncoding)
            ){
                writeTestPayload(fw1, fw2);
                try {
                    checkFile(file1, file2);
                    fail();
                } catch (final AssertionError ex) {
                    // success
                }

            }
            assertTrue(file1.exists());
            assertTrue(file2.exists());
        }
        if (Charset.isSupported("UTF-16LE")) {
            try (
                FileWriter fw1 = new FileWriter(file1);  // default encoding
                FileWriterWithEncoding fw2 = new FileWriterWithEncoding(file2, defaultEncoding)
            ){
                writeTestPayload(fw1, fw2);
                try {
                    checkFile(file1, file2);
                    fail();
                } catch (final AssertionError ex) {
                    // success
                }

            }
            assertTrue(file1.exists());
            assertTrue(file2.exists());
        }
    }

    private void writeTestPayload(final FileWriter fw1, final FileWriterWithEncoding fw2) throws IOException {
        assertTrue(file1.exists());
        assertTrue(file2.exists());

        fw1.write(textContent);
        fw2.write(textContent);
        fw1.write(65);
        fw2.write(65);
        fw1.write(anotherTestContent);
        fw2.write(anotherTestContent);
        fw1.write(anotherTestContent, 1, 2);
        fw2.write(anotherTestContent, 1, 2);
        fw1.write("CAFE", 1, 2);
        fw2.write("CAFE", 1, 2);

        fw1.flush();
        fw2.flush();
    }

    @Test
    public void constructor_File_encoding_badEncoding() {
        assertThrows(IOException.class, () -> {
            try (
                Writer writer = new FileWriterWithEncoding(file1, "BAD-ENCODE")
            ){ }
         });
        assertFalse(file1.exists());
    }

    @Test
    public void constructor_File_directory() {
        assertThrows(IOException.class, () -> {
            try (
                Writer writer = new FileWriterWithEncoding(temporaryFolder, defaultEncoding)
            ){ }
         });
        assertFalse(file1.exists());
    }

    @Test
    public void constructor_File_nullFile() {
        assertThrows(NullPointerException.class, () -> {
            try (
                Writer writer = new FileWriterWithEncoding((File) null, defaultEncoding)
            ){ }
         });
        assertFalse(file1.exists());
    }

    @Test
    public void constructor_fileName_nullFile() {
        assertThrows(NullPointerException.class, () -> {
            try (
                Writer writer = new FileWriterWithEncoding((String) null, defaultEncoding)
            ){ }
         });
        assertFalse(file1.exists());
    }

    @Test
    public void sameEncoding_null_Charset_constructor() throws Exception {
        try {
            successfulRun(new FileWriterWithEncoding(file2, (Charset) null));
            fail();
        } catch (final NullPointerException ignore) {

        }
    }
}
