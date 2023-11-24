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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link FileWriterWithEncoding}.
 */
public class FileWriterWithEncodingTest {

    @TempDir
    public File temporaryFolder;

    private String defaultEncoding;
    private File file1;
    private File file2;
    private String textContent;
    private final char[] anotherTestContent = {'f', 'z', 'x'};

    @BeforeEach
    public void setUp() throws Exception {
        final File encodingFinder = new File(temporaryFolder, "finder.txt");
        try (OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(encodingFinder.toPath()))) {
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

    private void successfulRun(final FileWriterWithEncoding fw21) throws Exception {
        try (FileWriter fw1 = new FileWriter(file1); // default encoding
            FileWriterWithEncoding fw2 = fw21) {
            writeTestPayload(fw1, fw2);
            checkFile(file1, file2);
        }
        assertTrue(file1.exists());
        assertTrue(file2.exists());
    }

    @Test
    public void testConstructor_File_directory() {
        assertThrows(IOException.class, () -> {
            try (Writer writer = new FileWriterWithEncoding(temporaryFolder, defaultEncoding)) {
                // empty
            }
        });
        assertFalse(file1.exists());
        assertThrows(IOException.class, () -> {
            try (Writer writer = FileWriterWithEncoding.builder().setFile(temporaryFolder).setCharset(defaultEncoding).get()) {
                // empty
            }
        });
        assertFalse(file1.exists());
    }

    @Test
    public void testConstructor_File_encoding_badEncoding() {
        assertThrows(IOException.class, () -> {
            try (Writer writer = new FileWriterWithEncoding(file1, "BAD-ENCODE")) {
                // empty
            }
        });
        assertFalse(file1.exists());
    }

    @Test
    public void testConstructor_File_existingFile_withContent() throws Exception {
        try (FileWriter fw1 = new FileWriter(file1);) {
            fw1.write(textContent);
            fw1.write(65);
        }
        assertEquals(1025, file1.length());

        try (FileWriterWithEncoding fw1 = new FileWriterWithEncoding(file1, defaultEncoding)) {
            fw1.write("ABcd");
        }

        assertEquals(4, file1.length());

        try (FileWriterWithEncoding fw1 = FileWriterWithEncoding.builder().setFile(file1).setCharset(defaultEncoding).get()) {
            fw1.write("ABcd");
        }

        assertEquals(4, file1.length());
    }

    @Test
    public void testConstructor_File_nullFile() {
        assertThrows(NullPointerException.class, () -> {
            try (Writer writer = new FileWriterWithEncoding((File) null, defaultEncoding)) {
                // empty
            }
        });
        assertFalse(file1.exists());
    }

    @Test
    public void testConstructor_fileName_nullFile() {
        assertThrows(NullPointerException.class, () -> {
            try (Writer writer = new FileWriterWithEncoding((String) null, defaultEncoding)) {
                // empty
            }
        });
        assertFalse(file1.exists());
    }

    @Test
    public void testConstructorAppend_File_existingFile_withContent() throws Exception {
        try (FileWriter fw1 = new FileWriter(file1)) {
            fw1.write("ABcd");
        }
        assertEquals(4, file1.length());

        try (FileWriterWithEncoding fw1 = new FileWriterWithEncoding(file1, defaultEncoding, true)) {
            fw1.write("XyZ");
        }

        assertEquals(7, file1.length());

        // @formatter:off
        try (FileWriterWithEncoding fw1 = FileWriterWithEncoding.builder()
                .setFile(file1)
                .setCharset(defaultEncoding)
                .setAppend(true)
                .get()) {
            fw1.write("XyZ");
        }
        // @formatter:on

        assertEquals(10, file1.length());
    }

    @Test
    public void testDifferentEncoding() throws Exception {
        if (Charset.isSupported(StandardCharsets.UTF_16BE.name())) {
            try (FileWriter fw1 = new FileWriter(file1); // default encoding
                FileWriterWithEncoding fw2 = new FileWriterWithEncoding(file2, defaultEncoding)) {
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
        if (Charset.isSupported(StandardCharsets.UTF_16LE.name())) {
            try (FileWriter fw1 = new FileWriter(file1); // default encoding
                FileWriterWithEncoding fw2 = new FileWriterWithEncoding(file2, defaultEncoding)) {
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

    @Test
    public void testSameEncoding_Charset_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2, Charset.defaultCharset())) {
            successfulRun(writer);
        }
        // @formatter:off
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder()
                .setFile(file2)
                .setCharset(Charset.defaultCharset())
                .get()) {
            successfulRun(writer);
        }
        // @formatter:on
    }

    @Test
    public void testSameEncoding_CharsetEncoder_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2, Charset.defaultCharset().newEncoder())) {
            successfulRun(writer);
        }
        // @formatter:off
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder()
                .setFile(file2)
                .setCharsetEncoder(Charset.defaultCharset().newEncoder())
                .get()) {
            successfulRun(writer);
        }
        // @formatter:on
    }

    @Test
    public void testSameEncoding_null_Charset_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2, (Charset) null)) {
            successfulRun(writer);
        }
    }

    @Test
    public void testSameEncoding_null_CharsetEncoder_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2.getPath(), (CharsetEncoder) null)) {
            successfulRun(writer);
        }
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder().setFile(file2.getPath()).get()) {
            successfulRun(writer);
        }
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder().setFile(file2.getPath()).setCharsetEncoder(null).get()) {
            successfulRun(writer);
        }
    }

    @Test
    public void testSameEncoding_null_CharsetName_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2.getPath(), (String) null)) {
            successfulRun(writer);
        }
    }

    @Test
    public void testSameEncoding_string_Charset_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2.getPath(), Charset.defaultCharset())) {
            successfulRun(writer);
        }
        // @formatter:off
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder()
                .setFile(file2.getPath())
                .setCharset(Charset.defaultCharset())
                .get()) {
            successfulRun(writer);
        }
        // @formatter:on
    }

    @Test
    public void testSameEncoding_string_CharsetEncoder_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2.getPath(), Charset.defaultCharset().newEncoder())) {
            successfulRun(writer);
        }
    }

    @Test
    public void testSameEncoding_string_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2, defaultEncoding)) {
            successfulRun(writer);
        }
    }

    @Test
    public void testSameEncoding_string_string_constructor() throws Exception {
        try (FileWriterWithEncoding writer = new FileWriterWithEncoding(file2.getPath(), defaultEncoding)) {
            successfulRun(writer);
        }
        // @formatter:off
        try (FileWriterWithEncoding writer = FileWriterWithEncoding.builder()
                .setFile(file2.getPath())
                .setCharset(defaultEncoding)
                .get()) {
            successfulRun(writer);
        }
        // @formatter:on
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
}
