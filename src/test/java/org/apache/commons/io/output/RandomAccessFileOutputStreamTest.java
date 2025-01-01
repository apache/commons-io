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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link RandomAccessFileOutputStream}.
 */
public class RandomAccessFileOutputStreamTest {

    private static final String EXPECTED = "Put the message in the box";

    /** A temporary folder. */
    @TempDir
    public Path temporaryFolder;

    @Test
    public void testClose() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteInt.txt");
        final Charset charset = StandardCharsets.US_ASCII;
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            os.write(EXPECTED.getBytes(charset));
            os.close();
        }
        assertEquals(EXPECTED, new String(Files.readAllBytes(fixturePath), charset));
        // @formatter:on
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            validateState(os);
        }
    }

    @Test
    public void testFlush() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteInt.txt");
        final Charset charset = StandardCharsets.US_ASCII;
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            final byte[] bytes = EXPECTED.getBytes(charset);
            for (final byte element : bytes) {
                os.write(element);
                os.flush();
            }
        }
        assertEquals(EXPECTED, new String(Files.readAllBytes(fixturePath), charset));
        // @formatter:on
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            validateState(os);
        }
    }

    @Test
    public void testWriteByteArray() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteInt.txt");
        final Charset charset = StandardCharsets.US_ASCII;
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            os.write(EXPECTED.getBytes(charset));
        }
        assertEquals(EXPECTED, new String(Files.readAllBytes(fixturePath), charset));
        // @formatter:on
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            validateState(os);
        }
    }

    @Test
    public void testWriteByteArrayAt() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteInt.txt");
        final Charset charset = StandardCharsets.US_ASCII;
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            os.write(EXPECTED.getBytes(charset), 1, EXPECTED.length() - 2);
        }
        assertEquals(EXPECTED.subSequence(1, EXPECTED.length() - 1), new String(Files.readAllBytes(fixturePath), charset));
        // @formatter:on
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            validateState(os);
        }
    }

    @Test
    public void testWriteGet() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteGet.txt");
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            validateState(os);
        }
        // @formatter:on
    }

    @Test
    public void testWriteGetDefault() throws IOException {
        assertThrows(IllegalStateException.class, () -> {
            try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().get()) {
                validateState(os);
            }
        });
    }

    /**
     * Tests that the default OpenOption is WRITE.
     *
     * @throws IOException Thrown when the test fails.
     */
    @Test
    public void testWriteGetPathOnly() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteGet.txt");
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .get()) {
            validateState(os);
        }
        // @formatter:on
    }

    @Test
    public void testWriteInt() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("testWriteInt.txt");
        final Charset charset = StandardCharsets.US_ASCII;
        // @formatter:off
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder()
                .setPath(fixturePath)
                .setOpenOptions(StandardOpenOption.WRITE)
                .get()) {
            validateState(os);
            final byte[] bytes = EXPECTED.getBytes(charset);
            for (final byte element : bytes) {
                os.write(element);
            }
        }
        assertEquals(EXPECTED, new String(Files.readAllBytes(fixturePath), charset));
        // @formatter:on
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            validateState(os);
        }
    }

    @SuppressWarnings("resource")
    private void validateState(final RandomAccessFileOutputStream os) throws IOException {
        final RandomAccessFile randomAccessFile = os.getRandomAccessFile();
        assertNotNull(randomAccessFile);
        assertNotNull(randomAccessFile.getFD());
    }

}
