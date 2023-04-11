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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.QueueOutputStream;
import org.apache.commons.io.output.QueueOutputStreamTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test {@link QueueInputStream}.
 *
 * @see {@link QueueOutputStreamTest}
 */
public class QueueInputStreamTest {

    public static Stream<Arguments> inputData() {
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
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    public void bufferedReads(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new QueueInputStream(queue));
                final QueueOutputStream outputStream = new QueueOutputStream(queue)) {
            outputStream.write(inputData.getBytes(UTF_8));
            final String actualData = IOUtils.toString(inputStream, UTF_8);
            assertEquals(inputData, actualData);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    public void bufferedReadWrite(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (BufferedInputStream inputStream = new BufferedInputStream(new QueueInputStream(queue));
                final BufferedOutputStream outputStream = new BufferedOutputStream(new QueueOutputStream(queue), defaultBufferSize())) {
            outputStream.write(inputData.getBytes(UTF_8));
            outputStream.flush();
            final String dataCopy = IOUtils.toString(inputStream, UTF_8);
            assertEquals(inputData, dataCopy);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    public void bufferedWrites(final String inputData) throws IOException {
        final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        try (QueueInputStream inputStream = new QueueInputStream(queue);
                final BufferedOutputStream outputStream = new BufferedOutputStream(new QueueOutputStream(queue), defaultBufferSize())) {
            outputStream.write(inputData.getBytes(UTF_8));
            outputStream.flush();
            final String actualData = readUnbuffered(inputStream);
            assertEquals(inputData, actualData);
        }
    }

    private int defaultBufferSize() {
        return 8192;
    }

    private String readUnbuffered(final InputStream inputStream) throws IOException {
        return readUnbuffered(inputStream, Integer.MAX_VALUE);
    }

    private String readUnbuffered(final InputStream inputStream, final int maxBytes) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int numBytesRead = 0; numBytesRead < maxBytes; numBytesRead++) {
            final int valueRead = inputStream.read();
            if (valueRead == -1 || Thread.currentThread().isInterrupted()) {
                break;
            }
            byteArrayOutputStream.write(valueRead);
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    public void testNullArgument() {
        assertThrows(NullPointerException.class, () -> new QueueInputStream(null), "queue is required");
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    public void unbufferedReadWrite(final String inputData) throws IOException {
        try (QueueInputStream inputStream = new QueueInputStream();
                final QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {
            writeUnbuffered(outputStream, inputData);
            final String actualData = readUnbuffered(inputStream);
            assertEquals(inputData, actualData);
        }
    }

    @ParameterizedTest(name = "inputData={0}")
    @MethodSource("inputData")
    public void blockingReadWrite(final String inputData) throws Exception {
        if (inputData.isEmpty()) {
            return;
        }
        
        final Duration timeout = Duration.ofMillis(500);
        try (QueueInputStream inputStream = new QueueInputStream(new LinkedBlockingQueue<>(), true);
                final QueueOutputStream outputStream = inputStream.newQueueOutputStream()) {

            assertNull(readInBackgroundThread(inputStream, inputData.length(), timeout));

            writeUnbuffered(outputStream, inputData);
            assertEquals(inputData, readInBackgroundThread(inputStream, inputData.length(), timeout));

            assertNull(readInBackgroundThread(inputStream, inputData.length(), timeout));
        }
    }

    private String readInBackgroundThread(final QueueInputStream inputStream, final int maxBytes,
        final Duration timeout) throws InterruptedException {
        final BlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();
        final CountDownLatch exitedSignal = new CountDownLatch(1);
        final Thread thread = new Thread(() -> {
            try {
                outputQueue.add(readUnbuffered(inputStream, maxBytes));
            } catch (final IOException e) {
                outputQueue.add(ExceptionUtils.getStackTrace(e));
            }
            exitedSignal.countDown();
        });
        thread.setDaemon(true);
        thread.setName(getClass().getSimpleName());
        thread.start();

        final String output = outputQueue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        thread.interrupt();
        exitedSignal.await();
        return output;
    }

    private void writeUnbuffered(final QueueOutputStream outputStream, final String inputData) throws IOException {
        final byte[] bytes = inputData.getBytes(UTF_8);
        outputStream.write(bytes, 0, bytes.length);
    }
}
