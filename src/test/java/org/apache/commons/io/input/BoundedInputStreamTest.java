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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.test.CustomIOException;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link BoundedInputStream}.
 */
class BoundedInputStreamTest {

    static Stream<Arguments> testAvailableAfterClose() throws IOException {
        // Case 1: behaves like ByteArrayInputStream — close() is a no-op, available() still returns a value (e.g., 42).
        final InputStream noOpClose = mock(InputStream.class);
        when(noOpClose.available()).thenReturn(42, 42);

        // Case 2: returns 0 after close (Commons memory-backed streams that ignore close but report 0 when exhausted).
        final InputStream returnsZeroAfterClose = mock(InputStream.class);
        when(returnsZeroAfterClose.available()).thenReturn(42, 0);

        // Case 3: throws IOException after close (e.g., FileInputStream-like behavior).
        final InputStream throwsAfterClose = mock(InputStream.class);
        when(throwsAfterClose.available()).thenReturn(42).thenThrow(new IOException("Stream closed"));

        return Stream.of(
                Arguments.of("underlying stream still returns 42 after close", noOpClose, 42),
                Arguments.of("underlying stream returns 0 after close", returnsZeroAfterClose, 42),
                Arguments.of("underlying stream throws IOException after close", throwsAfterClose, 42));
    }

    static Stream<Arguments> testAvailableUpperLimit() {
        final byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);
        return Stream.of(
                // Limited by maxCount
                Arguments.of(new ByteArrayInputStream(helloWorld), helloWorld.length - 1, helloWorld.length - 1, 0),
                // Limited by data length
                Arguments.of(new ByteArrayInputStream(helloWorld), helloWorld.length + 1, helloWorld.length, 0),
                // Limited by Integer.MAX_VALUE
                Arguments.of(
                        new NullInputStream(Long.MAX_VALUE), Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    static Stream<Arguments> testReadAfterClose() throws IOException {
        // Case 1: no-op close (ByteArrayInputStream-like): read() still returns a value after close
        final InputStream noOpClose = mock(InputStream.class);
        when(noOpClose.read()).thenReturn(42);

        // Case 2: returns EOF (-1) after close
        final InputStream returnsEofAfterClose = mock(InputStream.class);
        when(returnsEofAfterClose.read()).thenReturn(IOUtils.EOF);

        // Case 3: throws IOException after close (FileInputStream-like)
        final InputStream throwsAfterClose = mock(InputStream.class);
        final IOException closed = new IOException("Stream closed");
        when(throwsAfterClose.read()).thenThrow(closed);

        return Stream.of(
                Arguments.of("underlying stream still reads data after close", noOpClose, 42),
                Arguments.of("underlying stream returns EOF after close", returnsEofAfterClose, IOUtils.EOF),
                Arguments.of("underlying stream throws IOException after close", throwsAfterClose, closed));
    }

    static Stream<Arguments> testRemaining() {
        return Stream.of(
                // Unbounded: any negative maxCount is treated as "no limit".
                Arguments.of("unbounded (EOF constant)", IOUtils.EOF, Long.MAX_VALUE),
                Arguments.of("unbounded (arbitrary negative)", Long.MIN_VALUE, Long.MAX_VALUE),

                // Bounded: remaining equals the configured limit, regardless of underlying data size.
                Arguments.of("bounded (zero)", 0L, 0L),
                Arguments.of("bounded (small)", 1024L, 1024L),
                Arguments.of("bounded (Integer.MAX_VALUE)", Integer.MAX_VALUE, (long) Integer.MAX_VALUE),

                // Bounded but extremely large: still not 'unbounded'.
                Arguments.of("bounded (Long.MAX_VALUE)", Long.MAX_VALUE, Long.MAX_VALUE));
    }

    private void compare(final String message, final byte[] expected, final byte[] actual) {
        assertEquals(expected.length, actual.length, () -> message + " (array length equals check)");
        final MutableInt mi = new MutableInt();
        for (int i = 0; i < expected.length; i++) {
            mi.setValue(i);
            assertEquals(expected[i], actual[i], () -> message + " byte[" + mi + "]");
        }
    }

    @Test
    void testAfterReadConsumer() throws Exception {
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

    @ParameterizedTest(name = "{index} — {0}")
    @MethodSource
    void testAvailableAfterClose(String caseName, InputStream delegate, int expectedBeforeClose)
            throws Exception {
        final InputStream shadow;
        try (InputStream in = BoundedInputStream.builder()
                .setInputStream(delegate)
                .setPropagateClose(true)
                .get()) {
            // Before close: pass-through behavior
            assertEquals(expectedBeforeClose, in.available(), caseName + " (before close)");
            shadow = in; // keep reference to call after close
        }
        // Verify the underlying stream was closed
        verify(delegate, times(1)).close();
        // After close: behavior depends on the underlying stream
        assertEquals(0, shadow.available(), caseName + " (after close)");
        // Interactions: available called only once before close.
        verify(delegate, times(1)).available();
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource
    void testAvailableUpperLimit(InputStream input, long maxCount, int expectedBeforeSkip, int expectedAfterSkip)
            throws Exception {
        try (BoundedInputStream bounded = BoundedInputStream.builder()
                .setInputStream(input)
                .setMaxCount(maxCount)
                .get()) {
            assertEquals(
                    expectedBeforeSkip, bounded.available(), "available should be limited by maxCount and data length");
            IOUtils.skip(bounded, expectedBeforeSkip);
            assertEquals(
                    expectedAfterSkip,
                    bounded.available(),
                    "after skipping available should be limited by maxCount and data length");
        }
    }

    @Test
    void testBuilderGet() {
        // java.lang.IllegalStateException: origin == null
        assertThrows(IllegalStateException.class, () -> BoundedInputStream.builder().get());
    }

    @Test
    void testCloseHandleIOException() throws IOException {
        ProxyInputStreamTest.testCloseHandleIOException(BoundedInputStream.builder());
    }

    @ParameterizedTest
    @ValueSource(longs = { -100, -1, 0, 1, 2, 4, 8, 16, 32, 64 })
    void testCounts(final long startCount) throws Exception {
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
    void testMarkReset() throws Exception {
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
    void testOnMaxCountConsumer() throws Exception {
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
    void testOnMaxLength() throws Exception {
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
    void testPublicConstructors() throws IOException {
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

    @ParameterizedTest(name = "{index} — {0}")
    @MethodSource("testReadAfterClose")
    void testReadAfterClose(
            String caseName,
            InputStream delegate,
            Object expectedAfterClose // Integer (value) or IOException (expected thrown)
            ) throws Exception {

        final InputStream bounded;
        try (InputStream in = BoundedInputStream.builder()
                .setInputStream(delegate)
                .setPropagateClose(true)
                .get()) {
            bounded = in; // call read() only after close
        }

        // Underlying stream should be closed exactly once
        verify(delegate, times(1)).close();

        if (expectedAfterClose instanceof Integer) {
            assertEquals(expectedAfterClose, bounded.read(), caseName + " (after close)");
        } else if (expectedAfterClose instanceof IOException) {
            final IOException actual = assertThrows(IOException.class, bounded::read, caseName + " (after close)");
            // verify it's the exact instance we configured
            assertSame(expectedAfterClose, actual, caseName + " (exception instance)");
        } else {
            fail("Unexpected expectedAfterClose type: " + expectedAfterClose);
        }

        // We only performed one read() (after close)
        verify(delegate, times(1)).read();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void testReadArray() throws Exception {
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

    @Test
    void testReadSingle() throws Exception {
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

    @ParameterizedTest(name = "{index}: {0} -> initial remaining {2}")
    @MethodSource
    void testRemaining(final String caseName, final long maxCount, final long expectedInitialRemaining)
            throws Exception {
        final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8); // 11 bytes

        try (BoundedInputStream in = BoundedInputStream.builder()
                .setByteArray(data)
                .setMaxCount(maxCount)
                .get()) {
            // Initial remaining respects the imposed limit (or is Long.MAX_VALUE if unbounded).
            assertEquals(expectedInitialRemaining, in.getRemaining(), caseName + " (initial)");

            // Skip more than the data length to exercise both bounded and unbounded paths.
            final long skipped = IOUtils.skip(in, 42);

            // For unbounded streams (EOF == -1), remaining stays the same.
            // For bounded, it decreases by 'skipped'.
            final long expectedAfterSkip =
                    in.getMaxCount() == IOUtils.EOF ? expectedInitialRemaining : expectedInitialRemaining - skipped;

            assertEquals(expectedAfterSkip, in.getRemaining(), caseName + " (after skip)");
        }
    }

    @Test
    void testReset() throws Exception {
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
