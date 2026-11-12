/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link ReadAheadInputStream}. This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was called
 * {@code ReadAheadInputStreamSuite}.
 */
class ReadAheadInputStreamTest extends AbstractInputStreamTest {

    @SuppressWarnings("resource")
    @BeforeEach
    void setUpInputStreams() throws IOException {
        inputStreams = new InputStream[] {
                // Tests equal and aligned buffers of wrapped an outer stream.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 8 * 1024), 8 * 1024),
                // Tests aligned buffers, wrapped bigger than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 3 * 1024), 2 * 1024),
                // Tests aligned buffers, wrapped smaller than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 2 * 1024), 3 * 1024),
                // Tests unaligned buffers, wrapped bigger than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 321), 123),
                // Tests unaligned buffers, wrapped smaller than outer.
                new ReadAheadInputStream(new BufferedFileChannelInputStream(InputPath, 123), 321),
                //
                // Tests equal and aligned buffers of wrapped an outer stream.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 8 * 1024)).setBufferSize(8 * 1024).get(),
                // Tests aligned buffers, wrapped bigger than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 3 * 1024)).setBufferSize(2 * 1024).get(),
                // Tests aligned buffers, wrapped smaller than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 2 * 1024)).setBufferSize(3 * 1024).get(),
                // Tests unaligned buffers, wrapped bigger than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 321)).setBufferSize(123).get(),
                // Tests unaligned buffers, wrapped smaller than outer.
                ReadAheadInputStream.builder().setInputStream(new BufferedFileChannelInputStream(InputPath, 123)).setBufferSize(321).get(),
                ReadAheadInputStream.builder().setPath(InputPath).setOpenOptions(StandardOpenOption.READ).get() };
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    synchronized void testCloseInterrupt() throws IOException, InterruptedException {
        try (ReadAheadInputStream inputStream = ReadAheadInputStream.builder()
                // @formatter:off
                .setPath(InputPath)
                .get()) {
                // @formatter:on
            final ReadAheadInputStream spy = spy(inputStream);
            when(spy.shutdownAwait()).thenThrow(InterruptedException.class);
            Thread.currentThread().interrupt();
            assertInstanceOf(InterruptedException.class, assertThrows(InterruptedIOException.class, spy::close).getCause());
            assertTrue(Thread.interrupted());
        }
    }

    @ParameterizedTest
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @ValueSource(booleans = { false, true })
    void testClosePlusExecutorService(final boolean waitForReadAhead) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService externalExecutor = Executors.newSingleThreadExecutor();
        final CountDownLatch readStarted = new CountDownLatch(1);
        final CountDownLatch continueRead = new CountDownLatch(1);
        // We use an outer try-with-resources for only the test fixture instead of combining it with the ReadAheadInputStream allocation.
        try (FileInputStream inputStream = new FileInputStream("src/test/resources/org/apache/commons/io/FileUtilsTestDataLF.bin") {

            private int readCount;

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                if (waitForReadAhead && ++readCount == 2) {
                    readStarted.countDown();
                    try {
                        continueRead.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw Input.toInterruptedIOException(e);
                    }
                }
                return super.read(b, off, len);
            }
        }) {
            try {
                try (ReadAheadInputStream rais = ReadAheadInputStream.builder().setInputStream(inputStream).setBufferSize(1)
                        .setExecutorService(externalExecutor).get()) {
                    assertEquals('1', rais.read());
                    if (waitForReadAhead) {
                        assertTrue(readStarted.await(30, TimeUnit.SECONDS), "Read-ahead did not start");
                    }
                }
                assertFalse(externalExecutor.isShutdown(), "Closing the stream must not shut down the caller's executor");
                continueRead.countDown();
                // A running read-ahead task closes the underlying stream after its read completes.
                // This single-thread executor runs the barrier only after that task has finished.
                externalExecutor.submit(() -> null).get(30, TimeUnit.SECONDS);
                // The underlying FileInputStream should be closed since ReadAheadInputStream is a FilterInputStream.
                assertThrows(IOException.class, inputStream::available);
                assertThrows(IOException.class, inputStream::read);
                // The caller remains responsible for shutting down the executor.
            } finally {
                continueRead.countDown();
                // TODO ExecutorService implements AutoCloseable in Java 19+.
                externalExecutor.shutdown();
            }
        }
    }

}
