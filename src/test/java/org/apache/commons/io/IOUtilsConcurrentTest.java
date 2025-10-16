/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.input.ChecksumInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link IOUtils} methods in a concurrent environment.
 */
class IOUtilsConcurrentTest {

    private static class ChecksumReader extends Reader {
        private final CRC32 checksum;
        private final long expectedChecksumValue;
        private final Reader reader;

        ChecksumReader(Reader reader, long expectedChecksumValue) {
            this.reader = reader;
            this.checksum = new CRC32();
            this.expectedChecksumValue = expectedChecksumValue;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        public long getValue() {
            return checksum.getValue();
        }

        @Override
        public int read() throws IOException {
            return super.read();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            final int n = reader.read(cbuf, off, len);
            if (n > 0) {
                final byte[] bytes = new String(cbuf, off, n).getBytes(Charset.defaultCharset());
                checksum.update(bytes, 0, bytes.length);
            }
            if (n == -1) {
                final long actual = checksum.getValue();
                if (actual != expectedChecksumValue) {
                    throw new IOException("Checksum mismatch: expected " + expectedChecksumValue + " but got " + actual);
                }
            }
            return n;
        }
    }

    /**
     * Test data for InputStream tests.
     */
    private static final byte[][] BYTE_DATA;
    /**
     * Checksum values for {@link #BYTE_DATA}.
     */
    private static final long[] BYTE_DATA_CHECKSUM;
    /**
     * Number of runs per thread (to increase the chance of collisions).
     */
    private static final int RUNS_PER_THREAD = 16;
    /**
     * Size of test data.
     */
    private static final int SIZE = IOUtils.DEFAULT_BUFFER_SIZE;
    /**
     * Test data for Reader tests.
     */
    private static final String[] STRING_DATA;
    /**
     * Checksum values for {@link #STRING_DATA}.
     */
    private static final long[] STRING_DATA_CHECKSUM;
    /**
     * Number of threads to use.
     */
    private static final int THREAD_COUNT = 16;
    /**
     * Number of data variants (to increase the chance of collisions).
     */
    private static final int VARIANTS = 16;

    static {
        final Checksum checksum = new CRC32();
        // Byte data
        BYTE_DATA = new byte[VARIANTS][];
        BYTE_DATA_CHECKSUM = new long[VARIANTS];
        for (int variant = 0; variant < VARIANTS; variant++) {
            final byte[] data = new byte[SIZE];
            for (int i = 0; i < SIZE; i++) {
                data[i] = (byte) ((i + variant) % 256);
            }
            BYTE_DATA[variant] = data;
            checksum.reset();
            checksum.update(data, 0 , data.length);
            BYTE_DATA_CHECKSUM[variant] = checksum.getValue();
        }
        // Char data
        final char[] cdata = new char[SIZE];
        STRING_DATA = new String[VARIANTS];
        STRING_DATA_CHECKSUM = new long[VARIANTS];
        for (int variant = 0; variant < VARIANTS; variant++) {
            for (int i = 0; i < SIZE; i++) {
                cdata[i] = (char) ((i + variant) % Character.MAX_VALUE);
            }
            STRING_DATA[variant] = new String(cdata);
            checksum.reset();
            final byte[] bytes = STRING_DATA[variant].getBytes(Charset.defaultCharset());
            checksum.update(bytes, 0, bytes.length);
            STRING_DATA_CHECKSUM[variant] = checksum.getValue();
        }
    }

    static Stream<IOConsumer<InputStream>> testConcurrentInputStreamTasks() {
        return Stream.of(
                IOUtils::consume,
                in -> IOUtils.skip(in, Long.MAX_VALUE),
                in -> IOUtils.skipFully(in, SIZE),
                IOUtils::toByteArray,
                in -> IOUtils.toByteArray(in, SIZE),
                in -> IOUtils.toByteArray(in, SIZE, 512)
        );
    }

    static Stream<IOConsumer<Reader>> testConcurrentReaderTasks() {
        return Stream.of(
                IOUtils::consume,
                reader -> IOUtils.skip(reader, Long.MAX_VALUE),
                reader -> IOUtils.skipFully(reader, SIZE),
                reader -> IOUtils.toByteArray(reader, Charset.defaultCharset())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testConcurrentInputStreamTasks(IOConsumer<InputStream> consumer) throws InterruptedException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            final List<Future<Void>> futures = IntStream.range(0, THREAD_COUNT * RUNS_PER_THREAD)
                    .<Future<Void>>mapToObj(i -> threadPool.submit(() -> {
                        try (InputStream in = ChecksumInputStream
                                .builder()
                                .setByteArray(BYTE_DATA[i % VARIANTS])
                                .setChecksum(new CRC32())
                                .setExpectedChecksumValue(BYTE_DATA_CHECKSUM[i % VARIANTS])
                                .get()) {
                            consumer.accept(in);
                        }
                        return null;
                    })).collect(Collectors.toList());
            futures.forEach(f -> assertDoesNotThrow(() -> f.get()));
        } finally {
            threadPool.shutdownNow();
        }
    }

    @ParameterizedTest
    @MethodSource
    void testConcurrentReaderTasks(IOConsumer<Reader> consumer) throws InterruptedException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            final List<Future<Void>> futures = IntStream.range(0, THREAD_COUNT * RUNS_PER_THREAD)
                    .<Future<Void>>mapToObj(i -> threadPool.submit(() -> {
                        try (Reader reader = new ChecksumReader(new StringReader(STRING_DATA[i % VARIANTS]), STRING_DATA_CHECKSUM[i % VARIANTS])) {
                            consumer.accept(reader);
                        }
                        return null;
                    })).collect(Collectors.toList());
            futures.forEach(f -> assertDoesNotThrow(() -> f.get()));
        } finally {
            threadPool.shutdownNow();
        }
    }
}
