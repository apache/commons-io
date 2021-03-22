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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MemoryMappedFileInputStreamTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanup() {
        // Run the garbage collector to clean up memory mapped buffers,
        // otherwise the temporary files won't be able to be removed when running on
        // Windows.
        System.gc();
    }

    @Test
    void testEmptyFile() throws IOException {
        // setup
        final Path file = createTestFile(0);
        // test
        try (InputStream is = new MemoryMappedFileInputStream(file)) {
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(new byte[0], data);
        }
    }

    @Test
    void testSmallFile() throws IOException {
        // setup
        final Path file = createTestFile(100);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file)) {
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(expectedData, data);
        }
    }

    @Test
    void testLargerFile() throws IOException {
        // setup
        final Path file = createTestFile(5 * 1024 * 1024);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file)) {
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(expectedData, data);
        }
    }

    @Test
    void testAlternateBufferSize() throws IOException {
        // setup
        final Path file = createTestFile(1 * 1024 * 1024);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 1024)) {
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(expectedData, data);
        }
    }

    @Test
    void testReadAfterClose() throws IOException {
        // setup
        final Path file = createTestFile(1 * 1024 * 1024);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 1024)) {
            is.close();
            // verify
            Assertions.assertThrows(IOException.class, () -> IOUtils.toByteArray(is));
        }
    }

    @Test
    void testReadSingleByte() throws IOException {
        // setup
        final Path file = createTestFile(2);
        final byte[] expectedData = Files.readAllBytes(file);
        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 1024)) {
            int b1 = is.read();
            int b2 = is.read();
            assertEquals(-1, is.read());
            // verify
            assertArrayEquals(expectedData, new byte[] {(byte) b1, (byte) b2});
        }
    }

    private Path createTestFile(final int size) throws IOException {
        Path file = Files.createTempFile(tempDir, null, null);
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file))) {
            Files.write(file, RandomUtils.nextBytes(size));
        }
        return file;
    }

}
