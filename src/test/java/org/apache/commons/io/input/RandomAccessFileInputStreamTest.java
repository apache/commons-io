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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RandomAccessFileInputStream}.
 */
public class RandomAccessFileInputStreamTest {

    private static final String DATA_FILE_NAME = "src/test/resources/org/apache/commons/io/test-file-iso8859-1.bin";
    private static final Path DATA_PATH = Paths.get(DATA_FILE_NAME);
    private static final int DATA_FILE_LEN = 1430;

    private RandomAccessFile createRandomAccessFile() throws FileNotFoundException {
        return RandomAccessFileMode.READ_ONLY.create(DATA_FILE_NAME);
    }

    @SuppressWarnings("resource")
    @Test
    public void testAvailableAfterClose() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            inputStream.close();
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testAvailableAfterOpen() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            assertEquals(DATA_FILE_LEN, inputStream.available());
        }
    }

    @Test
    public void testAvailableLong() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            assertEquals(DATA_FILE_LEN, inputStream.availableLong());
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testBuilderFile() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder().setFile(new File(DATA_FILE_NAME)).get()) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @Test
    public void testBuilderGet() {
        // java.lang.IllegalStateException: origin == null
        assertThrows(IllegalStateException.class, () -> RandomAccessFileInputStream.builder().get());
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testBuilderPath() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder().setPath(DATA_PATH).get()) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testBuilderPathOpenOptions() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder().setPath(DATA_PATH).setOpenOptions(StandardOpenOption.READ)
                    .get()) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testBuilderRandomAccessFile() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder().setRandomAccessFile(file).get()) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testConstructorCloseOnCloseFalse() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                    .setRandomAccessFile(createRandomAccessFile())
                    .get()) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @Test
    public void testCopy() throws IOException {
        // @formatter:off
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            // @formatter:on
            // A Test Line.
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                // 0 and 12
                assertEquals(12, inputStream.copy(0, 12, baos));
                assertArrayEquals("A Test Line.".getBytes(StandardCharsets.ISO_8859_1), baos.toByteArray());
                // 0 and 1
                baos.reset();
                assertEquals(1, inputStream.copy(0, 1, baos));
                assertArrayEquals("A".getBytes(StandardCharsets.ISO_8859_1), baos.toByteArray());
                // 11 and 1
                baos.reset();
                assertEquals(1, inputStream.copy(11, 1, baos));
                assertArrayEquals(".".getBytes(StandardCharsets.ISO_8859_1), baos.toByteArray());
                // 1 and 10
                baos.reset();
                assertEquals(10, inputStream.copy(1, 10, baos));
                assertArrayEquals(" Test Line".getBytes(StandardCharsets.ISO_8859_1), baos.toByteArray());
                // next
                assertEquals('.', inputStream.read());
            }
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testDeprecatedConstructorCloseOnCloseTrue() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file, true)) {
                assertTrue(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            assertThrows(IOException.class, file::read);
        }
    }

    @SuppressWarnings("resource") // instance variable access
    @Test
    public void testDeprecatedConstructorRandomAccessFile() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file)) {
                assertFalse(inputStream.isCloseOnClose());
                assertNotEquals(-1, inputStream.getRandomAccessFile().read());
            }
            file.read();
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstructors() throws IOException {
        try (RandomAccessFile randomAccessFile = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(randomAccessFile)) {
                assertFalse(inputStream.isCloseOnClose());
                assertEquals(randomAccessFile, inputStream.getRandomAccessFile());
            }
            try (RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(randomAccessFile, true)) {
                assertTrue(inputStream.isCloseOnClose());
                assertEquals(randomAccessFile, inputStream.getRandomAccessFile());
            }
            try (RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(randomAccessFile, false)) {
                assertFalse(inputStream.isCloseOnClose());
                assertEquals(randomAccessFile, inputStream.getRandomAccessFile());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstructorsNull() {
        assertThrows(NullPointerException.class, () -> new RandomAccessFileInputStream(null));
        assertThrows(NullPointerException.class, () -> new RandomAccessFileInputStream(null, true));
        assertThrows(NullPointerException.class, () -> new RandomAccessFileInputStream(null, false));
    }

    @Test
    public void testGetters() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                    .setRandomAccessFile(file)
                    .setCloseOnClose(true)
                    .get()) {
                assertEquals(file, inputStream.getRandomAccessFile());
                assertTrue(inputStream.isCloseOnClose());
            }
        }
    }

    @Test
    public void testRead() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            // A Test Line.
            assertEquals('A', inputStream.read());
            assertEquals(' ', inputStream.read());
            assertEquals('T', inputStream.read());
            assertEquals('e', inputStream.read());
            assertEquals('s', inputStream.read());
            assertEquals('t', inputStream.read());
            assertEquals(' ', inputStream.read());
            assertEquals('L', inputStream.read());
            assertEquals('i', inputStream.read());
            assertEquals('n', inputStream.read());
            assertEquals('e', inputStream.read());
            assertEquals('.', inputStream.read());
            assertEquals(DATA_FILE_LEN - 12, inputStream.available());
            assertEquals(DATA_FILE_LEN - 12, inputStream.availableLong());
        }
    }

    @Test
    public void testReadAfterClose() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            inputStream.close();
            assertThrows(IOException.class, inputStream::read);
        }
    }

    @Test
    public void testReadByteArray() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            // A Test Line.
            final int dataLen = 12;
            final byte[] buffer = new byte[dataLen];
            assertEquals(dataLen, inputStream.read(buffer));
            assertArrayEquals("A Test Line.".getBytes(StandardCharsets.ISO_8859_1), buffer);
            //
            assertEquals(DATA_FILE_LEN - dataLen, inputStream.available());
            assertEquals(DATA_FILE_LEN - dataLen, inputStream.availableLong());
        }
    }

    @Test
    public void testReadByteArrayBounds() throws IOException {
        try (RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                .setRandomAccessFile(createRandomAccessFile())
                .setCloseOnClose(true)
                .get()) {
            // A Test Line.
            final int dataLen = 12;
            final byte[] buffer = new byte[dataLen];
            assertEquals(dataLen, inputStream.read(buffer, 0, dataLen));
            assertArrayEquals("A Test Line.".getBytes(StandardCharsets.ISO_8859_1), buffer);
            //
            assertEquals(DATA_FILE_LEN - dataLen, inputStream.available());
            assertEquals(DATA_FILE_LEN - dataLen, inputStream.availableLong());
        }
    }

    @Test
    public void testSkip() throws IOException {

        try (RandomAccessFile file = createRandomAccessFile();
             RandomAccessFileInputStream inputStream = RandomAccessFileInputStream.builder()
                    .setRandomAccessFile(file)
                    .get()) {
            assertEquals(0, inputStream.skip(-1));
            assertEquals(0, inputStream.skip(Integer.MIN_VALUE));
            assertEquals(0, inputStream.skip(0));
            // A Test Line.
            assertEquals('A', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            assertEquals('T', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            assertEquals('s', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            assertEquals(' ', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            assertEquals('i', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            assertEquals('e', inputStream.read());
            assertEquals(1, inputStream.skip(1));
            //
            assertEquals(DATA_FILE_LEN - 12, inputStream.available());
            assertEquals(DATA_FILE_LEN - 12, inputStream.availableLong());
            assertEquals(10, inputStream.skip(10));
            assertEquals(DATA_FILE_LEN - 22, inputStream.availableLong());
            //
            final long avail = inputStream.availableLong();
            assertEquals(avail, inputStream.skip(inputStream.availableLong()));
            // At EOF
            assertEquals(DATA_FILE_LEN, file.length());
            assertEquals(DATA_FILE_LEN, file.getFilePointer());
            //
            assertEquals(0, inputStream.skip(1));
            assertEquals(0, inputStream.skip(1000000000000L));
        }
    }
}
