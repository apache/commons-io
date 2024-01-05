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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UnsynchronizedFilterInputStream}.
 * <p>
 * Provenance: Apache Harmony and modified.
 * </p>
 */
public class UnsynchronizedFilterInputStreamTest {

    public static final String DATA = StringUtils.repeat("This is a test.", 500);

    private Path fileName;

    private InputStream is;

    byte[] ibuf = new byte[4096];

    /**
     * Sets up the fixture, for example, open a network connection. This method is called before a test is executed.
     *
     * @throws IOException Thrown on test failure.
     */
    @SuppressWarnings("resource") // See @AfterEach tearDown() method
    @BeforeEach
    protected void setUp() throws IOException {
        fileName = Files.createTempFile(getClass().getSimpleName(), ".tst");
        Files.write(fileName, DATA.getBytes(StandardCharsets.UTF_8));
        is = UnsynchronizedFilterInputStream.builder().setInputStream(Files.newInputStream(fileName)).get();
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
     * Tests java.io.FilterInputStream#available()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_available() throws IOException {
        assertEquals(DATA.length(), is.available(), "Returned incorrect number of available bytes");
    }

    /**
     * Tests java.io.FilterInputStream#close()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_close() throws IOException {
        is.close();
        assertThrows(IOException.class, () -> is.read(), "Able to read from closed stream");
    }

    /**
     * Tests java.io.FilterInputStream#mark(int)
     */
    @Test
    public void test_markI() {
        assertTrue(true, "Mark not supported by parent InputStream");
    }

    /**
     * Tests java.io.FilterInputStream#markSupported()
     */
    @Test
    public void test_markSupported() {
        assertTrue(!is.markSupported(), "markSupported returned true");
    }

    /**
     * Tests java.io.FilterInputStream#read()
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read() throws IOException {
        final int c = is.read();
        assertEquals(DATA.charAt(0), c, "read returned incorrect char");
    }

    /**
     * Tests java.io.FilterInputStream#read(byte[])
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read$B() throws IOException {
        final byte[] buf1 = new byte[100];
        assertEquals(buf1.length, is.read(buf1));
        assertTrue(new String(buf1, 0, buf1.length, StandardCharsets.UTF_8).equals(DATA.substring(0, 100)), "Failed to read correct data");
    }

    /**
     * Tests java.io.FilterInputStream#read(byte[], int, int)
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_read$BII() throws IOException {
        final byte[] buf1 = new byte[100];
        is.skip(3000);
        is.mark(1000);
        is.read(buf1, 0, buf1.length);
        assertTrue(new String(buf1, 0, buf1.length, StandardCharsets.UTF_8).equals(DATA.substring(3000, 3100)), "Failed to read correct data");
    }

    /**
     * Tests java.io.FilterInputStream#reset()
     */
    @Test
    public void test_reset() {
        assertThrows(IOException.class, () -> is.reset(), "should throw IOException");

    }

    /**
     * Tests java.io.FilterInputStream#skip(long)
     *
     * @throws IOException Thrown on test failure.
     */
    @Test
    public void test_skipJ() throws IOException {
        final byte[] buf1 = new byte[10];
        is.skip(1000);
        is.read(buf1, 0, buf1.length);
        assertTrue(new String(buf1, 0, buf1.length, StandardCharsets.UTF_8).equals(DATA.substring(1000, 1010)), "Failed to skip to correct position");
    }
}
