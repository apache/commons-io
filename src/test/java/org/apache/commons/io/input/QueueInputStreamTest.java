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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.QueueOutputStream;
import org.apache.commons.io.output.QueueOutputStreamTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Stopwatch;

/**
 * Test {@link QueueInputStream}.
 *
 * @see QueueOutputStreamTest
 */
public class QueueInputStreamTest {

    public static Stream<Arguments> inputData() {
        // @formatter:off
        return Stream.of(Arguments.of(""),
                Arguments.of("1"),
                Arguments.of("12"),
                Arguments.of("1234"),
                Arguments.of("12345678"),
                Arguments.of(StringUtils.repeat("A", 4095)),
                Arguments.of(StringUtils.repeat("A", 4096)),
                Arguments.of(StringUtils.repeat("A", 4097)),
                Arguments.of(StringUtils.repeat("A", 8191)),
                Arguments.of(StringUtils.repeat("A", 8192)),
                Arguments.of(StringUtils.repeat("A", 8193)),
                Arguments.of(StringUtils.repeat("A", 8192 * 4)));
        // @formatter:on
    }

    @TestFactory
    public DynamicTest[] bulkReadErrorHandlingTests() {
        final QueueInputStream queueInputStream = new QueueInputStream();
        return new DynamicTest[] {
                dynamicTest("Offset too big", () ->
                        assertThrows(IndexOutOfBoundsException.class, () ->
                                queueInputStream.read(EMPTY_BYTE_ARRAY, 1, 0))),

                dynamicTest("Offset negative", () ->
                        assertThrows(IndexOutOfBoundsException.class, () ->
                                queueInputStream.read(EMPTY_BYTE_ARRAY, -1, 0))),

                dynamicTest("Length too big", () ->
                        assertThrows(IndexOutOfBoundsException.class, () ->
                                queueInputStream.read(EMPTY_BYTE_ARRAY, 0, 1))),

                dynamicTest("Length negative", () ->
                        assertThrows(IndexOutOfBoundsException.class, () ->
                                queueInputStream.read(EMPTY_BYTE_ARRAY, 0, -1))),
        };
    }

    private int defaultBufferSize() {
        return 8192;
    }

    private void doTestReadLineByLine(final String inputData, final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final String[] lines = inputData.split("\n");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
            for (final String line : lines) {
                outputStream.write(line.getBytes(UTF_8));
                outputStream.write('\n');

                final String actualLine = reader.readLine();
                assertEquals(line, actualLine);
            }
        }
    }

    private String readUnbuffered(final InputStream inputStream) throws IOException {
        return readUnbuffered(inputStream, Integer.MAX_VALUE);
    }

    private String readUnbuffered(final InputStream inputStream, final int maxBytes) throws IOException {
        if (maxBytes == 0) {
            return "";
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int n = -1;
        while ((n = inputStream.read()) != -1) {
            byteArrayOutputStream.write(n);
            if (byteArrayOutputStream.size() >= maxBytes) {
                break;
            }
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
    }

    @SuppressWarnings("resource")
    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testAvailableAfterClose(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        final InputStream shadow;
        try (InputStream inputStream = new QueueInputStream(queue)) {
            shadow = inputStream;
        }
        assertEquals(0, shadow.available());
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testAvailableAfterOpen(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (InputStream inputStream = new QueueInputStream(queue)) {
            // Always 0 because read() blocks.
            assertEquals(0, inputStream.available());
            IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            assertEquals(0, inputStream.available());
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testBufferedReads(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new QueueInputStream(queue));
             QueueOutputStream outputStream = new QueueOutputStream(queue)) {
            outputStream.write(inputData.getBytes(StandardCharsets.UTF_8));
            final String actualData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            assertEquals(inputData, actualData);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testBufferedReadWrite(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new QueueInputStream(queue));
                BufferedOutputStream outputStream = new BufferedOutputStream(new QueueOutputStream(queue), defaultBufferSize())) {
            outputStream.write(inputData.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            final String dataCopy = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            assertEquals(inputData, dataCopy);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testBufferedWrites(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (QueueInputStream inputStream = new QueueInputStream(queue);
                BufferedOutputStream outputStream = new BufferedOutputStream(new QueueOutputStream(queue), defaultBufferSize())) {
            outputStream.write(inputData.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            final String actualData = readUnbuffered(inputStream);
            assertEquals(inputData, actualData);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testBulkReadWaiting(final String inputData) throws IOException {
        assumeFalse(inputData.isEmpty());

        final CountDownLatch onPollLatch = new CountDownLatch(1);
        final CountDownLatch afterWriteLatch = new CountDownLatch(1);
        final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>() {
            @Override
            public Integer poll(final long timeout, final TimeUnit unit) throws InterruptedException {
                onPollLatch.countDown();
                afterWriteLatch.await(1, TimeUnit.HOURS);
                return super.poll(timeout, unit);
            }
        };

        // Simulate scenario where there is not data immediately available when bulk reading and QueueInputStream has to
        // wait.
        try (QueueInputStream queueInputStream = QueueInputStream.builder()
                .setBlockingQueue(queue)
                .setTimeout(Duration.ofHours(1))
                .get()) {
            final QueueOutputStream queueOutputStream = queueInputStream.newQueueOutputStream();
            final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    onPollLatch.await(1, TimeUnit.HOURS);
                    queueOutputStream.write(inputData.getBytes(UTF_8));
                    afterWriteLatch.countDown();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });

            final byte[] data = new byte[inputData.length()];
            final int read = queueInputStream.read(data, 0, data.length);
            assertEquals(inputData.length(), read);
            final String outputData = new String(data, 0, read, StandardCharsets.UTF_8);
            assertEquals(inputData, outputData);
            assertDoesNotThrow(() -> future.get());
        }
    }

    @Test
    void testBulkReadZeroLength() {
        final QueueInputStream queueInputStream = new QueueInputStream();
        final int read = queueInputStream.read(EMPTY_BYTE_ARRAY, 0, 0);
        assertEquals(0, read);
    }

    @Test
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> QueueInputStream.builder().setTimeout(Duration.ofMillis(-1)).get(), "waitTime must not be negative");
    }

    @SuppressWarnings("resource")
    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testReadAfterClose(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        final InputStream shadow;
        try (InputStream inputStream = new QueueInputStream(queue)) {
            shadow = inputStream;
        }
        assertEquals(IOUtils.EOF, shadow.read());
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testReadLineByLineFile(final String inputData) throws IOException {
        final Path tempFile = Files.createTempFile(getClass().getSimpleName(), ".txt");
        try (InputStream inputStream = Files.newInputStream(tempFile);
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            doTestReadLineByLine(inputData, inputStream, outputStream);
        } finally {
            Files.delete(tempFile);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testReadLineByLineQueue(final String inputData) throws IOException {
        final String[] lines = inputData.split("\n");
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (QueueInputStream inputStream = QueueInputStream.builder()
                .setBlockingQueue(queue)
                .setTimeout(Duration.ofHours(1))
                .get();
             QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {

            doTestReadLineByLine(inputData, inputStream, outputStream);
        }
    }

    @Test
    void testResetArguments() throws IOException {
        try (QueueInputStream queueInputStream = QueueInputStream.builder().setTimeout(null).get()) {
            assertEquals(Duration.ZERO, queueInputStream.getTimeout());
            assertEquals(0, queueInputStream.getBlockingQueue().size());
        }
        try (QueueInputStream queueInputStream = QueueInputStream.builder().setBlockingQueue(null).get()) {
            assertEquals(Duration.ZERO, queueInputStream.getTimeout());
            assertEquals(0, queueInputStream.getBlockingQueue().size());
        }
    }

    @Test
    @DisplayName("If read is interrupted while waiting, then exception is thrown")
    void testTimeoutInterrupted() throws Exception {
        try (QueueInputStream inputStream = QueueInputStream.builder().setTimeout(Duration.ofMinutes(2)).get();
                QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {

            // read in a background thread
            final AtomicBoolean result = new AtomicBoolean();
            final CountDownLatch latch = new CountDownLatch(1);
            final Thread thread = new Thread(() -> {
                // when thread is interrupted, verify ...
                assertThrows(IllegalStateException.class, () -> readUnbuffered(inputStream, 3));
                assertTrue(Thread.currentThread().isInterrupted());
                result.set(true);
                latch.countDown();
            });
            thread.setDaemon(true);
            thread.start();

            // interrupt and check that verification completed
            thread.interrupt();
            latch.await(500, TimeUnit.MILLISECONDS);
            assertTrue(result.get());
        }
    }

    @Test
    @DisplayName("If data is not available in queue, then read will wait until wait time elapses")
    void testTimeoutUnavailableData() throws IOException {
        try (QueueInputStream inputStream = QueueInputStream.builder().setTimeout(Duration.ofMillis(500)).get();
                QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            final String actualData = assertTimeout(Duration.ofSeconds(1), () -> readUnbuffered(inputStream, 3));
            stopwatch.stop();
            assertEquals("", actualData);

            assertTrue(stopwatch.elapsed(TimeUnit.MILLISECONDS) >= 500, () -> stopwatch.toString());
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testUnbufferedReadWrite(final String inputData) throws IOException {
        try (QueueInputStream inputStream = new QueueInputStream();
                QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {
            writeUnbuffered(outputStream, inputData);
            final String actualData = readUnbuffered(inputStream);
            assertEquals(inputData, actualData);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    void testUnbufferedReadWriteWithTimeout(final String inputData) throws IOException {
        final Duration timeout = Duration.ofMinutes(2);
        try (QueueInputStream inputStream = QueueInputStream.builder().setTimeout(timeout).get();
                QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {
            assertEquals(timeout, inputStream.getTimeout());
            writeUnbuffered(outputStream, inputData);
            final String actualData = assertTimeout(Duration.ofSeconds(1), () -> readUnbuffered(inputStream, inputData.length()));
            assertEquals(inputData, actualData);
        }
    }

    private void writeUnbuffered(final QueueOutputStream outputStream, final String inputData) throws IOException {
        final byte[] bytes = inputData.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes, 0, bytes.length);
    }
}
