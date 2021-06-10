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
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        final Path file = createTestFile(1024 * 1024);
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
        final Path file = createTestFile(1024 * 1024);
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
            assertArrayEquals(expectedData, new byte[] { (byte) b1, (byte) b2 });
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { -5, -1, 0 })
    void testSkipNoop(int amountToSkip) throws IOException {
        // setup
        final Path file = createTestFile(10);
        final byte[] expectedData = Files.readAllBytes(file);
        // test
        try (InputStream is = new MemoryMappedFileInputStream(file)) {
            assertEquals(0, is.skip(amountToSkip));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(expectedData, data);
        }
    }

    @Test
    void testSkipEmpty() throws IOException {
        // setup
        final Path file = createTestFile(0);
        // test
        try (InputStream is = new MemoryMappedFileInputStream(file)) {
            assertEquals(0, is.skip(5));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(new byte[0], data);
        }
    }

    @Test
    void testSkipAtStart() throws IOException {
        // setup
        final Path file = createTestFile(100);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 10)) {
            assertEquals(1, is.skip(1));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(Arrays.copyOfRange(expectedData, 1, expectedData.length), data);
        }
    }

    @Test
    void testSkipInCurrentBuffer() throws IOException {
        // setup
        final Path file = createTestFile(100);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 10)) {
            IOUtils.toByteArray(is, 5);
            assertEquals(3, is.skip(3));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(Arrays.copyOfRange(expectedData, 8, expectedData.length), data);
        }
    }

    @Test
    void testSkipToEndOfCurrentBuffer() throws IOException {
        // setup
        final Path file = createTestFile(100);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 10)) {
            IOUtils.toByteArray(is, 5);
            assertEquals(5, is.skip(5));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(Arrays.copyOfRange(expectedData, 10, expectedData.length), data);
        }
    }

    @Test
    void testSkipOutOfCurrentBuffer() throws IOException {
        // setup
        final Path file = createTestFile(100);
        final byte[] expectedData = Files.readAllBytes(file);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 10)) {
            IOUtils.toByteArray(is, 5);
            assertEquals(6, is.skip(6));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(Arrays.copyOfRange(expectedData, 11, expectedData.length), data);
        }
    }

    @Test
    void testSkipPastEof() throws IOException {
        // setup
        final Path file = createTestFile(100);

        // test
        try (InputStream is = new MemoryMappedFileInputStream(file, 10)) {
            IOUtils.toByteArray(is, 5);
            assertEquals(95, is.skip(96));
            byte[] data = IOUtils.toByteArray(is);
            // verify
            assertArrayEquals(new byte[0], data);
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
