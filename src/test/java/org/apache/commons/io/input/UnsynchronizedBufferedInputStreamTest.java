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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
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

    private BufferedInputStream is;

    private InputStream isFile;

    byte[] ibuf = new byte[BUFFER_SIZE];

    /**
     * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
     *
     * @throws IOException Thrown on test failure.
     */
    @BeforeEach
    protected void setUp() throws IOException {
        fileName = Files.createTempFile(getClass().getSimpleName(), ".tst");
        Files.write(fileName, DATA.getBytes("UTF-8"));

        isFile = Files.newInputStream(fileName);
        is = new BufferedInputStream(isFile);
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
     * Tests java.io.BufferedInputStream#available()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_available() throws IOException {
        assertTrue(is.available() == DATA.length(), "Returned incorrect number of available bytes");

        // Test that a closed stream throws an IOE for available()
        final BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(new byte[] { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' }));
        final int available = bis.available();
        bis.close();
        assertTrue(available != 0);

        assertThrows(IOException.class, () -> bis.available(), "Expected test to throw IOE.");
    }

    /**
     * Tests java.io.BufferedInputStream#close()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_close() throws IOException {
        new BufferedInputStream(isFile).close();

        // regression for HARMONY-667
        try (BufferedInputStream buf = new BufferedInputStream(null, 5)) {
            // closes
        }

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
            final BufferedInputStream bufin = new BufferedInputStream(in);
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
     * Tests java.io.BufferedInputStream(InputStream)
     */
    @Test
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        try (BufferedInputStream str = new BufferedInputStream(null)) {
            assertThrows(IOException.class, () -> str.read(), "Expected an IOException");
        }
    }

    /*
     * Tests java.io.BufferedInputStream(InputStream)
     */
    @Test
    public void test_ConstructorLjava_io_InputStreamI() throws IOException {
        try (BufferedInputStream str = new BufferedInputStream(null, 1)) {
            assertThrows(IOException.class, () -> str.read(), "Expected an IOException");
        }

        // Test for method java.io.BufferedInputStream(java.io.InputStream, int)

        // Create buffer with exact size of file
        is = new BufferedInputStream(isFile, this.DATA.length());
        // Ensure buffer gets filled by evaluating one read
        is.read();
        // Close underlying FileInputStream, all but 1 buffered bytes should
        // still be available.
        isFile.close();
        // Read the remaining buffered characters, no IOException should
        // occur.
        is.skip(this.DATA.length() - 2);
        is.read();
        // is.read should now throw an exception because it will have to be filled.
        assertThrows(IOException.class, () -> is.read());

        assertThrows(NullPointerException.class, () -> UnsynchronizedBufferedInputStream.builder().setInputStream(null).setBufferSize(100).get());
    }

    /**
     * Tests java.io.BufferedInputStream#mark(int)
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
        InputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12);
        in.skip(6);
        in.mark(14);
        in.read(new byte[14], 0, 14);
        in.reset();
        assertTrue(in.read() == 6 && in.read() == 7, "Wrong bytes");

        in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12);
        in.skip(6);
        in.mark(8);
        in.skip(7);
        in.reset();
        assertTrue(in.read() == 6 && in.read() == 7, "Wrong bytes 2");

        BufferedInputStream buf = new BufferedInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
        buf.mark(3);
        bytes = new byte[3];
        int result = buf.read(bytes);
        assertEquals(3, result);
        assertEquals(0, bytes[0], "Assert 0:");
        assertEquals(1, bytes[1], "Assert 1:");
        assertEquals(2, bytes[2], "Assert 2:");
        assertEquals(3, buf.read(), "Assert 3:");

        buf = new BufferedInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
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

        buf = new BufferedInputStream(new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4 }), 2);
        buf.mark(Integer.MAX_VALUE);
        buf.read();
        buf.close();
    }

    /**
     * Tests java.io.BufferedInputStream#markSupported()
     */
    @Test
    public void test_markSupported() {
        assertTrue(is.markSupported(), "markSupported returned incorrect value");
    }

    /**
     * Tests java.io.BufferedInputStream#read()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read() throws IOException {
        final InputStreamReader isr = new InputStreamReader(is);
        final int c = isr.read();
        assertTrue(c == DATA.charAt(0), "read returned incorrect char");

        final byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        final InputStream in = new BufferedInputStream(new ByteArrayInputStream(bytes), 12);
        assertEquals(0, in.read(), "Wrong initial byte"); // Fill the buffer
        final byte[] buf = new byte[14];
        in.read(buf, 0, 14); // Read greater than the buffer
        assertTrue(new String(buf, 0, 14).equals(new String(bytes, 1, 14)), "Wrong block read data");
        assertEquals(15, in.read(), "Wrong bytes"); // Check next byte
    }

    /**
     * Tests java.io.BufferedInputStream#read(byte[], int, int)
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

        try (BufferedInputStream bufin = new BufferedInputStream(new InputStream() {
            int size = 2, pos = 0;

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
        })) {
            bufin.read();
            final int result = bufin.read(new byte[2], 0, 2);
            assertTrue(result == 1, () -> "Incorrect result: " + result);
        }
    }

    /**
     * Tests java.io.BufferedInputStream#read(byte[], int, int)
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read$BII_Exception() throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(null);
        assertThrows(NullPointerException.class, () -> bis.read(null, -1, -1));

        assertThrows(IndexOutOfBoundsException.class, () -> bis.read(new byte[0], -1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bis.read(new byte[0], 1, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bis.read(new byte[0], 1, 1));

        bis.close();

        assertThrows(IOException.class, () -> bis.read(null, -1, -1));
    }

    /**
     * Tests java.io.BufferedInputStream#reset()
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

        final BufferedInputStream bIn = new BufferedInputStream(new ByteArrayInputStream("1234567890".getBytes()));
        bIn.mark(10);
        for (int i = 0; i < 11; i++) {
            bIn.read();
        }
        bIn.reset();
    }

    /**
     * Tests java.io.BufferedInputStream#reset()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset_Exception() throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(null);

        // throws IOException with message "Mark has been invalidated"
        assertThrows(IOException.class, () -> bis.reset());

        // does not throw IOException
        bis.mark(1);
        bis.reset();

        bis.close();

        // throws IOException with message "stream is closed"
        assertThrows(IOException.class, () -> bis.reset());
    }

    /**
     * Tests java.io.BufferedInputStream#reset()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset_scenario1() throws IOException {
        final byte[] input = "12345678900".getBytes();
        final BufferedInputStream buffis = new BufferedInputStream(new ByteArrayInputStream(input));
        buffis.read();
        buffis.mark(5);
        buffis.skip(5);
        buffis.reset();
    }

    /**
     * Tests java.io.BufferedInputStream#reset()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_reset_scenario2() throws IOException {
        final byte[] input = "12345678900".getBytes();
        final BufferedInputStream buffis = new BufferedInputStream(new ByteArrayInputStream(input));
        buffis.mark(5);
        buffis.skip(6);
        buffis.reset();
    }

    /**
     * Tests java.io.BufferedInputStream#skip(long)
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_skip_NullInputStream() throws IOException {
        try (BufferedInputStream buf = new BufferedInputStream(null, 5)) {
            assertEquals(0, buf.skip(0));
        }
    }

    /**
     * Tests java.io.BufferedInputStream#skip(long)
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

        // regression for HARMONY-667
        try (BufferedInputStream buf = new BufferedInputStream(null, 5)) {
            assertThrows(IOException.class, () -> buf.skip(10));
        }
    }
}
