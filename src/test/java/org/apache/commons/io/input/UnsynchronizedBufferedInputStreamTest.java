/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream.Builder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UnsynchronizedBufferedInputStream}.
 * <p>
 * Provenance: Apache Harmony and modified.
 * </p>
 */
public class UnsynchronizedBufferedInputStreamTest {

    private static final int BUFFER_SIZE = 4096;

    public static final String DATA = StringUtils.repeat("This is a test.", 500);

    Path fileName;

    private UnsynchronizedBufferedInputStream is;

    private InputStream isFile;

    byte[] ibuf = new byte[BUFFER_SIZE];

    private Builder builder() {
        return new UnsynchronizedBufferedInputStream.Builder();
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
     *
     * @throws IOException Thrown on test failure.
     */
    @BeforeEach
    protected void setUp() throws IOException {
        fileName = Files.createTempFile(getClass().getSimpleName(), ".tst");
        Files.write(fileName, DATA.getBytes(StandardCharsets.UTF_8));

        isFile = Files.newInputStream(fileName);
        is = builder().setInputStream(isFile).get();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
     *
     * @throws IOException Thrown on test failure.
     */
    @AfterEach
    protected void tearDown() throws IOException {
        IOUtils.closeQuietly(is);
        Files.deleteIfExists(fileName);
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#available()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_available() throws IOException {
        assertEquals(DATA.length(), is.available(), "Returned incorrect number of available bytes");

        // Test that a closed stream throws an IOE for available()
        final UnsynchronizedBufferedInputStream bis = builder()
                .setInputStream(new ByteArrayInputStream(new byte[] { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' })).get();
        final int available = bis.available();
        bis.close();
        assertTrue(available != 0);

        assertThrows(IOException.class, () -> bis.available(), "Expected test to throw IOE.");
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#close()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_close() throws IOException {
        builder().setInputStream(isFile).get().close();

        try (InputStream in = new InputStream() {
            Object lock = new Object();

            @Override
            public void close() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public int read() {
                return 1;
            }

            @Override
            public int read(final byte[] buf, final int offset, final int length) {
                synchronized (lock) {
                    try {
                        lock.wait(3000);
                    } catch (final InterruptedException e) {
                        // Ignore
                    }
                }
                return 1;
            }
        }) {
            final UnsynchronizedBufferedInputStream bufin = builder().setInputStream(in).get();
            final Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    bufin.close();
                } catch (final Exception e) {
                    // Ignored
                }
            });
            thread.start();
            assertThrows(IOException.class, () -> bufin.read(new byte[100], 0, 99), "Should throw IOException");
        }
    }

    /*
     * Tests {@link UnsynchronizedBufferedInputStream#Builder()}.
     */
    @Test
    public void test_ConstructorLjava_io_InputStream() {
        assertThrows(NullPointerException.class, () -> builder().setInputStream(null).get());
    }

    /*
     * Tests {@link UnsynchronizedBufferedInputStream#Builder()}.
     */
    @Test
    public void test_ConstructorLjava_io_InputStreamI() throws IOException {
        assertThrows(NullPointerException.class, () -> builder().setInputStream(null).setBufferSize(1).get());

        // Test for method UnsynchronizedBufferedInputStream(InputStream, int)

        // Create buffer with exact size of file
        is = builder().setInputStream(isFile).setBufferSize(DATA.length()).get();
        // Ensure buffer gets filled by evaluating one read
        is.read();
        // Close underlying FileInputStream, all but 1 buffered bytes should
        // still be available.
        isFile.close();
        // Read the remaining buffered characters, no IOException should
        // occur.
        is.skip(DATA.length() - 2);
        is.read();
        // is.read should now throw an exception because it will have to be filled.
        assertThrows(IOException.class, () -> is.read());

        assertThrows(NullPointerException.class, () -> builder().setInputStream(null).setBufferSize(100).get());
        assertThrows(NullPointerException.class, () -> builder().setInputStream(null));
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#mark(int)}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_markI() throws IOException {
        final byte[] buf1 = new byte[100];
        final byte[] buf2 = new byte[100];
        is.skip(3000);
        is.mark(1000);
        is.read(buf1, 0, buf1.length);
        is.reset();
        is.read(buf2, 0, buf2.length);
        is.reset();
        assertTrue(new String(buf1, 0, buf1.length).equals(new String(buf2, 0, buf2.length)), "Failed to mark correct position");

        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        InputStream in = builder().setInputStream(new ByteArrayInputStream(bytes)).setBufferSize(12).get();
        in.skip(6);
        in.mark(14);
        in.read(new byte[14], 0, 14);
        in.reset();
        assertTrue(in.read() == 6 && in.read() == 7, "Wrong bytes");

        in = builder().setInputStream(new ByteArrayInputStream(bytes)).setBufferSize(12).get();
        in.skip(6);
        in.mark(8);
        in.skip(7);
        in.reset();
        assertTrue(in.read() == 6 && in.read() == 7, "Wrong bytes 2");

        UnsynchronizedBufferedInputStream buf = builder().setInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 })).setBufferSize(2).get();
        buf.mark(3);
        bytes = new byte[3];
        int result = buf.read(bytes);
        assertEquals(3, result);
        assertEquals(0, bytes[0], "Assert 0:");
        assertEquals(1, bytes[1], "Assert 1:");
        assertEquals(2, bytes[2], "Assert 2:");
        assertEquals(3, buf.read(), "Assert 3:");

        buf = builder().setInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 })).setBufferSize(2).get();
        buf.mark(3);
        bytes = new byte[4];
        result = buf.read(bytes);
        assertEquals(4, result);
        assertEquals(0, bytes[0], "Assert 4:");
        assertEquals(1, bytes[1], "Assert 5:");
        assertEquals(2, bytes[2], "Assert 6:");
        assertEquals(3, bytes[3], "Assert 7:");
        assertEquals(4, buf.read(), "Assert 8:");
        assertEquals(-1, buf.read(), "Assert 9:");

        buf = builder().setInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 })).setBufferSize(2).get();
        buf.mark(Integer.MAX_VALUE);
        buf.read();
        buf.close();
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#markSupported()}.
     */
    @Test
    public void test_markSupported() {
        assertTrue(is.markSupported(), "markSupported returned incorrect value");
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#read()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read() throws IOException {
        final InputStreamReader isr = new InputStreamReader(is);
        final int c = isr.read();
        assertEquals(DATA.charAt(0), c, "read returned incorrect char");

        final byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        final InputStream in = builder().setInputStream(new ByteArrayInputStream(bytes)).setBufferSize(12).get();
        assertEquals(0, in.read(), "Wrong initial byte"); // Fill the buffer
        final byte[] buf = new byte[14];
        in.read(buf, 0, 14); // Read greater than the buffer
        assertTrue(new String(buf, 0, 14).equals(new String(bytes, 1, 14)), "Wrong block read data");
        assertEquals(15, in.read(), "Wrong bytes"); // Check next byte
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#read(byte[], int, int)}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read$BII() throws IOException {
        final byte[] buf1 = new byte[100];
        is.skip(3000);
        is.mark(1000);
        is.read(buf1, 0, buf1.length);
        assertTrue(new String(buf1, 0, buf1.length).equals(DATA.substring(3000, 3100)), "Failed to read correct data");

        try (UnsynchronizedBufferedInputStream bufin = builder().setInputStream(new InputStream() {
            int size = 2;
            int pos;

            byte[] contents = new byte[size];

            @Override
            public int available() {
                return size - pos;
            }

            @Override
            public int read() throws IOException {
                if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
                return contents[pos++];
            }

            @Override
            public int read(final byte[] buf, final int off, final int len) throws IOException {
                if (pos >= size) {
                    throw new IOException("Read past end of data");
                }
                int toRead = len;
                if (toRead > available()) {
                    toRead = available();
                }
                System.arraycopy(contents, pos, buf, off, toRead);
                pos += toRead;
                return toRead;
            }
        }).get()) {
            bufin.read();
            final int result = bufin.read(new byte[2], 0, 2);
            assertEquals(1, result, () -> "Incorrect result: " + result);
        }
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#reset()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset() throws IOException {
        final byte[] buf1 = new byte[10];
        final byte[] buf2 = new byte[10];
        is.mark(2000);
        is.read(buf1, 0, 10);
        is.reset();
        is.read(buf2, 0, 10);
        is.reset();
        assertTrue(new String(buf1, 0, buf1.length).equals(new String(buf2, 0, buf2.length)), "Reset failed");

        final UnsynchronizedBufferedInputStream bIn = builder().setInputStream(new ByteArrayInputStream("1234567890".getBytes())).get();
        bIn.mark(10);
        for (int i = 0; i < 11; i++) {
            bIn.read();
        }
        bIn.reset();
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#reset()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset_scenario1() throws IOException {
        final byte[] input = "12345678900".getBytes();
        final UnsynchronizedBufferedInputStream bufin = builder().setInputStream(new ByteArrayInputStream(input)).get();
        bufin.read();
        bufin.mark(5);
        bufin.skip(5);
        bufin.reset();
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#reset()}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset_scenario2() throws IOException {
        final byte[] input = "12345678900".getBytes();
        final UnsynchronizedBufferedInputStream bufin = builder().setInputStream(new ByteArrayInputStream(input)).get();
        bufin.mark(5);
        bufin.skip(6);
        bufin.reset();
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#skip(long)}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_skip_NullInputStream() throws IOException {
        assertThrows(NullPointerException.class, () -> builder().setInputStream(null).setBufferSize(5).get());
    }

    /**
     * Tests {@link UnsynchronizedBufferedInputStream#skip(long)}.
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_skipJ() throws IOException {
        final byte[] buf1 = new byte[10];
        is.mark(2000);
        is.skip(1000);
        is.read(buf1, 0, buf1.length);
        is.reset();
        assertTrue(new String(buf1, 0, buf1.length).equals(DATA.substring(1000, 1010)), "Failed to skip to correct position");
    }
}
