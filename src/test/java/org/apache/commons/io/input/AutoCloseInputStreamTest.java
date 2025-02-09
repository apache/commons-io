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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AutoCloseInputStream}.
 */
public class AutoCloseInputStreamTest {

    private byte[] data;

    private AutoCloseInputStream stream;

    @SuppressWarnings("deprecation")
    @BeforeEach
    public void setUp() {
        data = new byte[] { 'x', 'y', 'z' };
        stream = new AutoCloseInputStream(new ByteArrayInputStream(data));
    }

    @Test
    public void testAfterReadConsumer() throws Exception {
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = AutoCloseInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream bounded = AutoCloseInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(bounded)).getMessage());
        }
        // @formatter:on
    }

    @Test
    public void testAvailableAfterClose() throws IOException {
        final InputStream shadow;
        try (InputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream(data))) {
            assertEquals(3, inputStream.available());
            shadow = inputStream;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    public void testAvailableAll() throws IOException {
        try (InputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream(data))) {
            assertEquals(3, inputStream.available());
            IOUtils.toByteArray(inputStream);
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testAvailableNull() throws IOException {
        try (InputStream inputStream = new AutoCloseInputStream(null)) {
            assertEquals(0, inputStream.available());
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testBuilderGet() {
        // java.lang.IllegalStateException: origin == null
        assertThrows(IllegalStateException.class, () -> AutoCloseInputStream.builder().get());
    }

    @Test
    public void testClose() throws IOException {
        stream.close();
        assertTrue(stream.isClosed(), "closed");
        assertEquals(-1, stream.read(), "read()");
        assertTrue(stream.isClosed(), "closed");
    }

    @Test
    public void testCloseHandleIOException() throws IOException {
        ProxyInputStreamTest.testCloseHandleIOException(AutoCloseInputStream.builder());
    }

    @Test
    public void testFinalize() throws Throwable {
        stream.finalize();
        assertTrue(stream.isClosed(), "closed");
        assertEquals(-1, stream.read(), "read()");
    }

    @Test
    public void testRead() throws IOException {
        for (final byte element : data) {
            assertEquals(element, stream.read(), "read()");
            assertFalse(stream.isClosed(), "closed");
        }
        assertEquals(-1, stream.read(), "read()");
        assertTrue(stream.isClosed(), "closed");
    }

    @Test
    public void testReadBuffer() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b)) {
            assertFalse(stream.isClosed(), "closed");
            for (int i = 0; i < n; i++) {
                assertEquals(data[total + i], b[i], "read(b)");
            }
            total += n;
        }
        assertEquals(data.length, total, "read(b)");
        assertTrue(stream.isClosed(), "closed");
        assertEquals(-1, stream.read(b), "read(b)");
    }

    @Test
    public void testReadBufferOffsetLength() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b, total, b.length - total)) {
            assertFalse(stream.isClosed(), "closed");
            total += n;
        }
        assertEquals(data.length, total, "read(b, off, len)");
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b[i], "read(b, off, len)");
        }
        assertTrue(stream.isClosed(), "closed");
        assertEquals(-1, stream.read(b, 0, b.length), "read(b, off, len)");
    }

    private void testResetBeforeEnd(final AutoCloseInputStream inputStream) throws IOException {
        inputStream.mark(1);
        assertEquals('1', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        assertEquals('4', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
    }

    @Test
    public void testResetBeforeEndCtor() throws IOException {
        try (AutoCloseInputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream("1234".getBytes()))) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetByteArray() throws IOException {
        try (AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setByteArray("1234".getBytes()).get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetCharSequence() throws IOException {
        try (AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setCharSequence("1234").get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetBeforeEndSetInputStream() throws IOException {
        try (AutoCloseInputStream inputStream = AutoCloseInputStream.builder().setInputStream(new ByteArrayInputStream("1234".getBytes())).get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testrReadAfterClose() throws IOException {
        final InputStream shadow;
        try (InputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream(data))) {
            assertEquals(3, inputStream.available());
            shadow = inputStream;
        }
        assertEquals(IOUtils.EOF, shadow.read());
    }

}
