/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests {@link RandomAccessFileOutputStream}.
 */
class RandomAccessFileOutputStreamTest {

    private static final class TrackingRandomAccessFile extends RandomAccessFile {

        private int bulkWriteCalls;

        private int singleByteWriteCalls;

        TrackingRandomAccessFile(final Path path) throws IOException {
            super(path.toFile(), "rw");
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            bulkWriteCalls++;
            super.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            singleByteWriteCalls++;
            super.write(b);
        }
    }

    private static final String EXPECTED = "Put the message in the box";

    /** A temporary folder. */
    @TempDir
    public Path temporaryFolder;

    @Test
    void testClose() throws IOException {
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
    void testFlush() throws IOException {
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
    void testWriteByteArray() throws IOException {
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
    void testWriteByteArrayAt() throws IOException {
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
    void testWriteByteArrayAtCurrentPosition() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("position.txt");
        Files.write(fixturePath, new byte[] { 9, 9, 9, 9, 9 });
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fixturePath.toFile(), "rw");
                RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            randomAccessFile.seek(1);
            os.write(new byte[] { 1, (byte) 0x80, (byte) 0xFF, 4 }, 1, 2);
            assertEquals(3, randomAccessFile.getFilePointer());
        }
        assertArrayEquals(new byte[] { 9, (byte) 0x80, (byte) 0xFF, 9, 9 }, Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteByteArrayAtDelegatesAsBulkWrite() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("bulkSlice.txt");
        try (TrackingRandomAccessFile randomAccessFile = new TrackingRandomAccessFile(fixturePath);
                RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            os.write(new byte[] { 1, 2, 3, 4 }, 1, 2);
            assertEquals(1, randomAccessFile.bulkWriteCalls);
            assertEquals(0, randomAccessFile.singleByteWriteCalls);
            assertEquals(2, randomAccessFile.getFilePointer());
        }
        assertArrayEquals(new byte[] { 2, 3 }, Files.readAllBytes(fixturePath));
    }

    @ParameterizedTest
    @CsvSource({ "-1, 1", "0, -1", "4, 0", "3, 1", "2, 2", "2147483647, 1", "1, 2147483647" })
    void testWriteByteArrayAtInvalidRange(final int off, final int len) throws IOException {
        final Path fixturePath = temporaryFolder.resolve("invalidRange.txt");
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            assertThrows(IndexOutOfBoundsException.class, () -> os.write(new byte[3], off, len));
            assertEquals(0, os.getRandomAccessFile().getFilePointer());
            assertEquals(0, os.getRandomAccessFile().length());
        }
    }

    @Test
    void testWriteByteArrayClosed() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("closed.txt");
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            os.close();
            os.write(new byte[0]);
            os.write(new byte[2], 2, 0);
            assertThrows(IOException.class, () -> os.write(new byte[] { 1, 2 }));
            assertThrows(IOException.class, () -> os.write(new byte[] { 1, 2 }, 1, 1));
        }
        assertArrayEquals(new byte[0], Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteByteArrayDelegatesAsBulkWrite() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("bulkArray.txt");
        final byte[] bytes = { 1, 2, 3, 4 };
        try (TrackingRandomAccessFile randomAccessFile = new TrackingRandomAccessFile(fixturePath);
                RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            os.write(bytes);
            assertEquals(1, randomAccessFile.bulkWriteCalls);
            assertEquals(0, randomAccessFile.singleByteWriteCalls);
            assertEquals(bytes.length, randomAccessFile.getFilePointer());
        }
        assertArrayEquals(bytes, Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteByteArrayEmpty() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("empty.txt");
        final byte[] bytes = { 1, 2, 3 };
        Files.write(fixturePath, bytes);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fixturePath.toFile(), "rw");
                RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            randomAccessFile.seek(1);
            os.write(new byte[0]);
            os.write(bytes, 0, 0);
            os.write(bytes, bytes.length, 0);
            assertEquals(1, randomAccessFile.getFilePointer());
            assertEquals(bytes.length, randomAccessFile.length());
        }
        assertArrayEquals(bytes, Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteByteArrayIOException() throws IOException {
        final IOException failure = new IOException("Write failed");
        final Path fixturePath = temporaryFolder.resolve("failure.txt");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fixturePath.toFile(), "rw") {
            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                throw failure;
            }
        }; RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            assertSame(failure, assertThrows(IOException.class, () -> os.write(new byte[] { 1, 2 })));
            assertSame(failure, assertThrows(IOException.class, () -> os.write(new byte[] { 1, 2 }, 1, 1)));
        }
        assertArrayEquals(new byte[0], Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteByteArrayNull() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("null.txt");
        try (RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setPath(fixturePath).get()) {
            assertThrows(NullPointerException.class, () -> os.write(null));
            assertThrows(NullPointerException.class, () -> os.write(null, 0, 0));
            assertThrows(NullPointerException.class, () -> os.write(null, -1, -1));
            assertEquals(0, os.getRandomAccessFile().getFilePointer());
            assertEquals(0, os.getRandomAccessFile().length());
        }
    }

    @Test
    void testWriteByteArrayReadOnly() throws IOException {
        final Path fixturePath = temporaryFolder.resolve("readOnly.txt");
        final byte[] bytes = { 1, 2, 3 };
        Files.write(fixturePath, bytes);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fixturePath.toFile(), "r");
                RandomAccessFileOutputStream os = RandomAccessFileOutputStream.builder().setRandomAccessFile(randomAccessFile).get()) {
            os.write(new byte[0]);
            os.write(bytes, bytes.length, 0);
            assertThrows(IOException.class, () -> os.write(bytes));
            assertThrows(IOException.class, () -> os.write(bytes, 1, 1));
        }
        assertArrayEquals(bytes, Files.readAllBytes(fixturePath));
    }

    @Test
    void testWriteGet() throws IOException {
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
    void testWriteGetDefault() {
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
    void testWriteGetPathOnly() throws IOException {
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
    void testWriteInt() throws IOException {
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
