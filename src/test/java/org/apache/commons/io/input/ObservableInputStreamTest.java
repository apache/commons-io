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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ObservableInputStream.Observer;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ObservableInputStream}.
 */
class ObservableInputStreamTest {

    private static final class DataViewObserver extends MethodCountObserver {
        private byte[] buffer;
        private int lastValue = -1;
        private int length = -1;
        private int offset = -1;

        @Override
        public void data(final byte[] buffer, final int offset, final int length) throws IOException {
            this.buffer = buffer;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void data(final int value) throws IOException {
            super.data(value);
            lastValue = value;
        }
    }

    private static final class LengthObserver extends Observer {
        private long total;

        @Override
        public void data(final byte[] buffer, final int offset, final int length) throws IOException {
            this.total += length;
        }

        @Override
        public void data(final int value) throws IOException {
            total++;
        }

        public long getTotal() {
            return total;
        }
    }

    private static class MethodCountObserver extends Observer {
        private long closedCount;
        private long dataBufferCount;
        private long dataCount;
        private long errorCount;
        private long finishedCount;

        @Override
        public void closed() throws IOException {
            closedCount++;
        }

        @Override
        public void data(final byte[] buffer, final int offset, final int length) throws IOException {
            dataBufferCount++;
        }

        @Override
        public void data(final int value) throws IOException {
            dataCount++;
        }

        @Override
        public void error(final IOException exception) throws IOException {
            errorCount++;
        }

        @Override
        public void finished() throws IOException {
            finishedCount++;
        }

        public long getClosedCount() {
            return closedCount;
        }

        public long getDataBufferCount() {
            return dataBufferCount;
        }

        public long getDataCount() {
            return dataCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public long getFinishedCount() {
            return finishedCount;
        }

    }

    private ObservableInputStream brokenObservableInputStream() {
        return new ObservableInputStream(BrokenInputStream.INSTANCE);
    }

    private InputStream createInputStream() {
        final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        return createInputStream(new ByteArrayInputStream(buffer));
    }

    private ObservableInputStream createInputStream(final InputStream origin) {
        return new ObservableInputStream(origin);
    }

    @Test
    void testAfterReadConsumer() throws Exception {
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = new ObservableInputStream.Builder()
                .setCharSequence("Hi")
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream bounded = new ObservableInputStream.Builder()
                .setCharSequence("Hi")
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
    void testAvailableAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = createInputStream()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    void testAvailableAfterOpen() throws Exception {
        try (InputStream in = createInputStream()) {
            assertTrue(in.available() > 0);
            assertNotEquals(IOUtils.EOF, in.read());
            assertTrue(in.available() > 0);
        }
    }

    @Test
    void testBrokenInputStreamRead() throws IOException {
        try (ObservableInputStream ois = brokenObservableInputStream()) {
            assertThrows(IOException.class, ois::read);
        }
    }

    @Test
    void testBrokenInputStreamReadBuffer() throws IOException {
        try (ObservableInputStream ois = brokenObservableInputStream()) {
            assertThrows(IOException.class, () -> ois.read(new byte[1]));
        }
    }

    @Test
    void testBrokenInputStreamReadSubBuffer() throws IOException {
        try (ObservableInputStream ois = brokenObservableInputStream()) {
            assertThrows(IOException.class, () -> ois.read(new byte[2], 0, 1));
        }
    }

    /**
     * Tests that {@link Observer#data(int)} is called.
     */
    @Test
    void testDataByteCalled_add() throws Exception {
        final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        final DataViewObserver lko = new DataViewObserver();
        try (ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer))) {
            assertEquals(-1, lko.lastValue);
            ois.read();
            assertEquals(-1, lko.lastValue);
            assertEquals(0, lko.getFinishedCount());
            assertEquals(0, lko.getClosedCount());
            ois.add(lko);
            for (int i = 1; i < buffer.length; i++) {
                final int result = ois.read();
                assertEquals((byte) result, buffer[i]);
                assertEquals(result, lko.lastValue);
                assertEquals(0, lko.getFinishedCount());
                assertEquals(0, lko.getClosedCount());
            }
            final int result = ois.read();
            assertEquals(-1, result);
            assertEquals(1, lko.getFinishedCount());
            assertEquals(0, lko.getClosedCount());
            ois.close();
            assertEquals(1, lko.getFinishedCount());
            assertEquals(1, lko.getClosedCount());
        }
    }

    /**
     * Tests that {@link Observer#data(int)} is called.
     */
    @Test
    void testDataByteCalled_ctor() throws Exception {
        final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        final DataViewObserver lko = new DataViewObserver();
        try (ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer), lko)) {
            assertEquals(-1, lko.lastValue);
            ois.read();
            assertNotEquals(-1, lko.lastValue);
            assertEquals(0, lko.getFinishedCount());
            assertEquals(0, lko.getClosedCount());
            for (int i = 1; i < buffer.length; i++) {
                final int result = ois.read();
                assertEquals((byte) result, buffer[i]);
                assertEquals(result, lko.lastValue);
                assertEquals(0, lko.getFinishedCount());
                assertEquals(0, lko.getClosedCount());
            }
            final int result = ois.read();
            assertEquals(-1, result);
            assertEquals(1, lko.getFinishedCount());
            assertEquals(0, lko.getClosedCount());
            ois.close();
            assertEquals(1, lko.getFinishedCount());
            assertEquals(1, lko.getClosedCount());
        }
    }

    /**
     * Tests that {@link Observer#data(byte[],int,int)} is called.
     */
    @Test
    void testDataBytesCalled() throws Exception {
        final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObservableInputStream ois = createInputStream(bais)) {
            final DataViewObserver observer = new DataViewObserver();
            final byte[] readBuffer = new byte[23];
            assertNull(observer.buffer);
            ois.read(readBuffer);
            assertNull(observer.buffer);
            ois.add(observer);
            for (;;) {
                if (bais.available() >= 2048) {
                    final int result = ois.read(readBuffer);
                    if (result == -1) {
                        ois.close();
                        break;
                    }
                    assertEquals(readBuffer, observer.buffer);
                    assertEquals(0, observer.offset);
                    assertEquals(readBuffer.length, observer.length);
                } else {
                    final int res = Math.min(11, bais.available());
                    final int result = ois.read(readBuffer, 1, 11);
                    if (result == -1) {
                        ois.close();
                        break;
                    }
                    assertEquals(readBuffer, observer.buffer);
                    assertEquals(1, observer.offset);
                    assertEquals(res, observer.length);
                }
            }
        }
    }

    @Test
    void testGetObservers0() throws IOException {
        try (ObservableInputStream ois = new ObservableInputStream(new NullInputStream())) {
            assertTrue(ois.getObservers().isEmpty());
        }
    }

    @Test
    void testGetObservers1() throws IOException {
        final DataViewObserver observer0 = new DataViewObserver();
        try (ObservableInputStream ois = new ObservableInputStream(new NullInputStream(), observer0)) {
            assertEquals(observer0, ois.getObservers().get(0));
        }
    }

    @Test
    void testGetObserversOrder() throws IOException {
        final DataViewObserver observer0 = new DataViewObserver();
        final DataViewObserver observer1 = new DataViewObserver();
        try (ObservableInputStream ois = new ObservableInputStream(new NullInputStream(), observer0, observer1)) {
            assertEquals(observer0, ois.getObservers().get(0));
            assertEquals(observer1, ois.getObservers().get(1));
        }
    }

    private void testNotificationCallbacks(final int bufferSize) throws IOException {
        final byte[] buffer = IOUtils.byteArray();
        final LengthObserver lengthObserver = new LengthObserver();
        final MethodCountObserver methodCountObserver = new MethodCountObserver();
        try (ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer), lengthObserver, methodCountObserver)) {
            assertEquals(IOUtils.DEFAULT_BUFFER_SIZE, IOUtils.copy(ois, NullOutputStream.INSTANCE, bufferSize));
        }
        assertEquals(IOUtils.DEFAULT_BUFFER_SIZE, lengthObserver.getTotal());
        assertEquals(1, methodCountObserver.getClosedCount());
        assertEquals(1, methodCountObserver.getFinishedCount());
        assertEquals(0, methodCountObserver.getErrorCount());
        assertEquals(0, methodCountObserver.getDataCount());
        assertEquals(buffer.length / bufferSize, methodCountObserver.getDataBufferCount());
    }

    @Test
    void testNotificationCallbacksBufferSize1() throws Exception {
        testNotificationCallbacks(1);
    }

    @Test
    void testNotificationCallbacksBufferSize2() throws Exception {
        testNotificationCallbacks(2);
    }

    @Test
    void testNotificationCallbacksBufferSizeDefault() throws Exception {
        testNotificationCallbacks(IOUtils.DEFAULT_BUFFER_SIZE);
    }

    @Test
    void testReadAfterClose_ByteArrayInputStream() throws Exception {
        try (InputStream in = createInputStream()) {
            in.close();
            assertNotEquals(IOUtils.EOF, in.read());
        }
    }

    @SuppressWarnings("resource")
    @Test
    void testReadAfterClose_ChannelInputStream() throws Exception {
        try (InputStream in = createInputStream(Files.newInputStream(Paths.get("src/test/resources/org/apache/commons/io/abitmorethan16k.txt")))) {
            in.close();
            // ChannelInputStream throws when closed
            assertThrows(IOException.class, in::read);
        }
    }

}
