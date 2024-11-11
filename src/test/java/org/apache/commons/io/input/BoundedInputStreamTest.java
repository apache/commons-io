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

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link BoundedInputStream}.
 */
public class BoundedInputStreamTest {

    private void compare(final String message, final byte[] expected, final byte[] actual) {
        assertEquals(expected.length, actual.length, () -> message + " (array length equals check)");
        final MutableInt mi = new MutableInt();
        for (int i = 0; i < expected.length; i++) {
            mi.setValue(i);
            assertEquals(expected[i], actual[i], () -> message + " byte[" + mi + "]");
        }
    }

    @Test
    public void testAfterReadConsumer() throws Exception {
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setMaxCount(hello.length)
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setMaxCount(hello.length)
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(bounded)).getMessage());
        }
        // @formatter:on
    }

    @SuppressWarnings("resource")
    @Test
    public void testAvailableAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = BoundedInputStream.builder().setCharSequence("Hi").get()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    public void testAvailableAfterOpen() throws Exception {
        try (InputStream in = BoundedInputStream.builder().setCharSequence("Hi").get()) {
            assertTrue(in.available() > 0);
        }
    }

    @Test
    public void testBuilderGet() {
        // java.lang.IllegalStateException: origin == null
        assertThrows(IllegalStateException.class, () -> BoundedInputStream.builder().get());
    }

    @Test
    public void testCloseHandleIOException() throws IOException {
        ProxyInputStreamTest.testCloseHandleIOException(BoundedInputStream.builder());
    }

    @ParameterizedTest
    @ValueSource(longs = { -100, -1, 0, 1, 2, 4, 8, 16, 32, 64 })
    public void testCounts(final long startCount) throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final long actualStart = startCount < 0 ? 0 : startCount;
        // limit = length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setCount(startCount)
                .setMaxCount(helloWorld.length).get()) {
            assertTrue(bounded.markSupported());
            assertEquals(helloWorld.length, bounded.getMaxCount());
            assertEquals(helloWorld.length, bounded.getMaxLength());
            assertEquals(actualStart, bounded.getCount());
            assertEquals(Math.max(0, bounded.getMaxCount() - actualStart), bounded.getRemaining());
            assertEquals(Math.max(0, bounded.getMaxLength() - actualStart), bounded.getRemaining());
            int readCount = 0;
            for (int i = 0; i < helloWorld.length; i++) {
                final byte expectedCh = bounded.getRemaining() > 0 ? helloWorld[i] : EOF;
                final int actualCh = bounded.read();
                assertEquals(expectedCh, actualCh, "limit = length byte[" + i + "]");
                if (actualCh != EOF) {
                    readCount++;
                }
                assertEquals(helloWorld.length, bounded.getMaxCount());
                assertEquals(helloWorld.length, bounded.getMaxLength());
                assertEquals(actualStart + readCount, bounded.getCount(), "i=" + i);
                assertEquals(Math.max(0, bounded.getMaxCount() - (readCount + actualStart)), bounded.getRemaining());
                assertEquals(Math.max(0, bounded.getMaxLength() - (readCount + actualStart)), bounded.getRemaining());
            }
            assertEquals(-1, bounded.read(), "limit = length end");
            assertEquals(helloWorld.length, bounded.getMaxLength());
            assertEquals(readCount + actualStart, bounded.getCount());
            assertEquals(0, bounded.getRemaining());
            assertEquals(0, bounded.available());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit > length
        final int maxCountP1 = helloWorld.length + 1;
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setCount(startCount)
                .setMaxCount(maxCountP1).get()) {
            assertTrue(bounded.markSupported());
            assertEquals(maxCountP1, bounded.getMaxLength());
            assertEquals(actualStart, bounded.getCount());
            assertEquals(Math.max(0, bounded.getMaxCount() - actualStart), bounded.getRemaining());
            assertEquals(Math.max(0, bounded.getMaxLength() - actualStart), bounded.getRemaining());
            int readCount = 0;
            for (int i = 0; i < helloWorld.length; i++) {
                final byte expectedCh = bounded.getRemaining() > 0 ? helloWorld[i] : EOF;
                final int actualCh = bounded.read();
                assertEquals(expectedCh, actualCh, "limit = length byte[" + i + "]");
                if (actualCh != EOF) {
                    readCount++;
                }
                assertEquals(maxCountP1, bounded.getMaxCount());
                assertEquals(maxCountP1, bounded.getMaxLength());
                assertEquals(actualStart + readCount, bounded.getCount(), "i=" + i);
                assertEquals(Math.max(0, bounded.getMaxCount() - (readCount + actualStart)), bounded.getRemaining());
                assertEquals(Math.max(0, bounded.getMaxLength() - (readCount + actualStart)), bounded.getRemaining());
            }
            assertEquals(-1, bounded.read(), "limit > length end");
            assertEquals(0, bounded.available());
            assertEquals(maxCountP1, bounded.getMaxLength());
            assertEquals(readCount + actualStart, bounded.getCount());
            assertEquals(Math.max(0, maxCountP1 - bounded.getCount()), bounded.getRemaining());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit < length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(hello.length).get()) {
            assertTrue(bounded.markSupported());
            assertEquals(hello.length, bounded.getMaxLength());
            assertEquals(0, bounded.getCount());
            assertEquals(bounded.getMaxLength(), bounded.getRemaining());
            int readCount = 0;
            for (int i = 0; i < hello.length; i++) {
                assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
                readCount++;
                assertEquals(hello.length, bounded.getMaxLength());
                assertEquals(readCount, bounded.getCount());
                assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            }
            assertEquals(-1, bounded.read(), "limit < length end");
            assertEquals(0, bounded.available());
            assertEquals(hello.length, bounded.getMaxLength());
            assertEquals(readCount, bounded.getCount());
            assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }

    @Test
    public void testMarkReset() throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final int helloWorldLen = helloWorld.length;
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final byte[] world = " World".getBytes(StandardCharsets.UTF_8);
        final int helloLen = hello.length;
        // limit = -1
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).get()) {
            assertTrue(bounded.markSupported());
            bounded.mark(0);
            compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = -1", hello, IOUtils.toByteArray(bounded, helloLen));
            bounded.mark(helloWorldLen);
            compare("limit = -1", world, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit = -1", world, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit = 0
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(0).get()) {
            assertTrue(bounded.markSupported());
            bounded.mark(0);
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            bounded.mark(helloWorldLen);
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit = length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length).get()) {
            assertTrue(bounded.markSupported());
            bounded.mark(0);
            compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = length", hello, IOUtils.toByteArray(bounded, helloLen));
            bounded.mark(helloWorldLen);
            compare("limit = length", world, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit = length", world, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit > length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length + 1).get()) {
            assertTrue(bounded.markSupported());
            bounded.mark(0);
            compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit > length", hello, IOUtils.toByteArray(bounded, helloLen));
            bounded.mark(helloWorldLen);
            compare("limit > length", world, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit > length", world, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit < length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length - (hello.length + 1)).get()) {
            assertTrue(bounded.markSupported());
            bounded.mark(0);
            compare("limit < length", hello, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit < length", hello, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit < length", hello, IOUtils.toByteArray(bounded, helloLen));
            bounded.mark(helloWorldLen);
            compare("limit < length", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            bounded.reset();
            compare("limit < length", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }

    @Test
    public void testOnMaxCountConsumer() throws Exception {
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setMaxCount(hello.length)
                .setOnMaxCount(null) // should not blow up
                .setOnMaxCount((m, c) -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setMaxCount(hello.length)
                .setOnMaxCount((m, c) -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(bounded)).getMessage());
        }
        // @formatter:on
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testOnMaxLength() throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final AtomicBoolean boolRef = new AtomicBoolean();
        // limit = length
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length)
                .setOnMaxCount((m, c) -> boolRef.set(true))
                .get()) {
            assertTrue(bounded.markSupported());
            assertEquals(helloWorld.length, bounded.getMaxCount());
            assertEquals(helloWorld.length, bounded.getMaxLength());
            assertEquals(0, bounded.getCount());
            assertEquals(bounded.getMaxCount(), bounded.getRemaining());
            assertEquals(bounded.getMaxLength(), bounded.getRemaining());
            assertFalse(boolRef.get());
            int readCount = 0;
            for (int i = 0; i < helloWorld.length; i++) {
                assertEquals(helloWorld[i], bounded.read(), "limit = length byte[" + i + "]");
                readCount++;
                assertEquals(helloWorld.length, bounded.getMaxCount());
                assertEquals(helloWorld.length, bounded.getMaxLength());
                assertEquals(readCount, bounded.getCount());
                assertEquals(bounded.getMaxCount() - readCount, bounded.getRemaining());
                assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            }
            assertEquals(-1, bounded.read(), "limit = length end");
            assertEquals(0, bounded.available());
            assertEquals(helloWorld.length, bounded.getMaxLength());
            assertEquals(readCount, bounded.getCount());
            assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            assertTrue(boolRef.get());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit > length
        boolRef.set(false);
        final int length2 = helloWorld.length + 1;
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(length2)
                .setOnMaxCount((m, c) -> boolRef.set(true))
                .get()) {
            assertTrue(bounded.markSupported());
            assertEquals(length2, bounded.getMaxLength());
            assertEquals(0, bounded.getCount());
            assertEquals(bounded.getMaxLength(), bounded.getRemaining());
            assertFalse(boolRef.get());
            int readCount = 0;
            for (int i = 0; i < helloWorld.length; i++) {
                assertEquals(helloWorld[i], bounded.read(), "limit > length byte[" + i + "]");
                readCount++;
                assertEquals(length2, bounded.getMaxLength());
                assertEquals(readCount, bounded.getCount());
                assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            }
            assertEquals(0, bounded.available());
            assertEquals(-1, bounded.read(), "limit > length end");
            assertEquals(length2, bounded.getMaxLength());
            assertEquals(readCount, bounded.getCount());
            assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            assertFalse(boolRef.get());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit < length
        boolRef.set(false);
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(hello.length)
                .setOnMaxCount((m, c) -> boolRef.set(true))
                .get()) {
            assertTrue(bounded.markSupported());
            assertEquals(hello.length, bounded.getMaxLength());
            assertEquals(0, bounded.getCount());
            assertEquals(bounded.getMaxLength(), bounded.getRemaining());
            assertFalse(boolRef.get());
            int readCount = 0;
            for (int i = 0; i < hello.length; i++) {
                assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
                readCount++;
                assertEquals(hello.length, bounded.getMaxLength());
                assertEquals(readCount, bounded.getCount());
                assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            }
            assertEquals(-1, bounded.read(), "limit < length end");
            assertEquals(hello.length, bounded.getMaxLength());
            assertEquals(readCount, bounded.getCount());
            assertEquals(bounded.getMaxLength() - readCount, bounded.getRemaining());
            assertTrue(boolRef.get());
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPublicConstructors() throws IOException {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        try (ByteArrayInputStream baos = new ByteArrayInputStream(helloWorld);
                BoundedInputStream inputStream = new BoundedInputStream(baos)) {
            assertSame(baos, inputStream.unwrap());
        }
        final long maxCount = 2;
        try (ByteArrayInputStream baos = new ByteArrayInputStream(helloWorld);
                BoundedInputStream inputStream = new BoundedInputStream(baos, maxCount)) {
            assertSame(baos, inputStream.unwrap());
            assertSame(maxCount, inputStream.getMaxCount());
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testReadAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = BoundedInputStream.builder().setCharSequence("Hi").get()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(IOUtils.EOF, shadow.read());
    }

    @Test
    public void testReadArray() throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).get()) {
            assertTrue(bounded.markSupported());
            compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(0).get()) {
            assertTrue(bounded.markSupported());
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length).get()) {
            assertTrue(bounded.markSupported());
            compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length + 1).get()) {
            assertTrue(bounded.markSupported());
            compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length - 6).get()) {
            assertTrue(bounded.markSupported());
            compare("limit < length", hello, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testReadSingle() throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        // limit = length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(helloWorld.length)
                .get()) {
            assertTrue(bounded.markSupported());
            for (int i = 0; i < helloWorld.length; i++) {
                assertEquals(helloWorld[i], bounded.read(), "limit = length byte[" + i + "]");
            }
            assertEquals(-1, bounded.read(), "limit = length end");
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit > length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(helloWorld.length + 1)
                .get()) {
            assertTrue(bounded.markSupported());
            for (int i = 0; i < helloWorld.length; i++) {
                assertEquals(helloWorld[i], bounded.read(), "limit > length byte[" + i + "]");
            }
            assertEquals(-1, bounded.read(), "limit > length end");
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit < length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(hello.length).get()) {
            assertTrue(bounded.markSupported());
            for (int i = 0; i < hello.length; i++) {
                assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
            }
            assertEquals(-1, bounded.read(), "limit < length end");
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }

    @Test
    public void testReset() throws Exception {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        // limit = -1
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).get()) {
            assertTrue(bounded.markSupported());
            bounded.reset();
            compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit = 0
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld)).setMaxCount(0).get()) {
            assertTrue(bounded.markSupported());
            bounded.reset();
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = 0", IOUtils.EMPTY_BYTE_ARRAY, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit = length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length).get()) {
            assertTrue(bounded.markSupported());
            bounded.reset();
            compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit > length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length + 1).get()) {
            assertTrue(bounded.markSupported());
            bounded.reset();
            compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
        // limit < length
        try (BoundedInputStream bounded = BoundedInputStream.builder().setInputStream(new ByteArrayInputStream(helloWorld))
                .setMaxCount(helloWorld.length - 6).get()) {
            assertTrue(bounded.markSupported());
            bounded.reset();
            compare("limit < length", hello, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
            // again
            bounded.reset();
            compare("limit < length", hello, IOUtils.toByteArray(bounded));
            // should be invariant
            assertTrue(bounded.markSupported());
        }
    }
}
