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

package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link DemuxOutputStream}.
 */
class DemuxOutputStreamTest {

    private static final class TrackingOutputStream extends OutputStream {

        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        private int bulkWriteCalls;

        private int singleByteWriteCalls;

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            bulkWriteCalls++;
            delegate.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            singleByteWriteCalls++;
            delegate.write(b);
        }
    }

    @Test
    void testWriteByteArrayAtDelegatesAsBulkWrite() throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            demux.bindStream(output);
            demux.write(new byte[] { 1, 2, 3, 4 }, 1, 2);
            assertEquals(1, output.bulkWriteCalls);
            assertEquals(0, output.singleByteWriteCalls);
            assertArrayEquals(new byte[] { 2, 3 }, output.delegate.toByteArray());
        }
    }

    @ParameterizedTest
    @CsvSource({ "-1, 1", "0, -1", "4, 0", "3, 1", "2, 2", "2147483647, 1", "1, 2147483647" })
    void testWriteByteArrayAtInvalidRange(final int off, final int len) throws IOException {
        for (final boolean bound : new boolean[] { false, true }) {
            final TrackingOutputStream output = new TrackingOutputStream();
            try (DemuxOutputStream demux = new DemuxOutputStream()) {
                if (bound) {
                    demux.bindStream(output);
                }
                assertThrows(IndexOutOfBoundsException.class, () -> demux.write(new byte[3], off, len));
                assertEquals(0, output.bulkWriteCalls);
                assertEquals(0, output.singleByteWriteCalls);
            }
        }
    }

    @Test
    void testWriteByteArrayAtWithoutBoundStream() throws IOException {
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            assertDoesNotThrow(() -> demux.write(new byte[] { 1, 2, 3 }, 1, 2));
            assertDoesNotThrow(() -> demux.write(new byte[] { 1, 2, 3 }));
            assertDoesNotThrow(() -> demux.write(new byte[0]));
            assertDoesNotThrow(() -> demux.write(new byte[3], 3, 0));
            assertThrows(NullPointerException.class, () -> demux.write(null, 0, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> demux.write(new byte[3], 2, 2));
        }
    }

    @Test
    void testWriteByteArrayDelegatesAsBulkWrite() throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        final byte[] bytes = { 0, (byte) 0x80, (byte) 0xFF };
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            demux.bindStream(output);
            demux.write(bytes);
            assertEquals(1, output.bulkWriteCalls);
            assertEquals(0, output.singleByteWriteCalls);
            assertArrayEquals(bytes, output.delegate.toByteArray());
        }
    }

    @Test
    void testWriteByteArrayEmptyDoesNotDelegate() throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            demux.bindStream(output);
            demux.write(new byte[0]);
            demux.write(new byte[3], 0, 0);
            demux.write(new byte[3], 3, 0);
            assertEquals(0, output.bulkWriteCalls);
            assertEquals(0, output.singleByteWriteCalls);
            assertArrayEquals(new byte[0], output.delegate.toByteArray());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testWriteByteArrayEmptyFinishedDelegate(final boolean wholeArray) throws IOException {
        try (DeflaterOutputStream output = new DeflaterOutputStream(new ByteArrayOutputStream());
                DemuxOutputStream demux = new DemuxOutputStream()) {
            output.finish();
            demux.bindStream(output);
            if (wholeArray) {
                assertDoesNotThrow(() -> demux.write(new byte[0]));
            } else {
                assertDoesNotThrow(() -> demux.write(new byte[3], 3, 0));
            }
        }
    }

    @Test
    void testWriteByteArrayIOException() throws IOException {
        final IOException failure = new IOException("Write failed");
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            demux.bindStream(new OutputStream() {
                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    throw failure;
                }

                @Override
                public void write(final int b) throws IOException {
                    throw failure;
                }
            });
            assertSame(failure, assertThrows(IOException.class, () -> demux.write(new byte[] { 1, 2 })));
            assertSame(failure, assertThrows(IOException.class, () -> demux.write(new byte[] { 1, 2 }, 1, 1)));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testWriteByteArrayNull(final boolean bound) throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            if (bound) {
                demux.bindStream(output);
            }
            assertThrows(NullPointerException.class, () -> demux.write(null));
            assertThrows(NullPointerException.class, () -> demux.write(null, 0, 0));
            assertThrows(NullPointerException.class, () -> demux.write(null, -1, -1));
            assertEquals(0, output.bulkWriteCalls);
            assertEquals(0, output.singleByteWriteCalls);
        }
    }

    @Test
    void testWriteByteArraySubclass() throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream() {
            @Override
            public void write(final int ch) throws IOException {
                super.write(ch + 1);
            }
        }) {
            demux.bindStream(output);
            demux.write(new byte[] { 1, 2 });
            demux.write(new byte[] { 3, 4, 5 }, 1, 2);
            assertEquals(0, output.bulkWriteCalls);
            assertEquals(4, output.singleByteWriteCalls);
            assertArrayEquals(new byte[] { 2, 3, 5, 6 }, output.delegate.toByteArray());
        }
    }

    @Test
    void testWriteByteArraySubclassWithoutBoundStream() throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream() {
            @Override
            public void write(final int ch) {
                output.write(ch);
            }
        }) {
            demux.write(new byte[] { 1, 2 });
            demux.write(new byte[] { 3, 4, 5 }, 1, 2);
            assertArrayEquals(new byte[] { 1, 2, 4, 5 }, output.toByteArray());
        }
    }

    @Test
    void testWriteByteArrayThreadBinding() throws Exception {
        final TrackingOutputStream parentOutput = new TrackingOutputStream();
        final TrackingOutputStream childOutput = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            demux.bindStream(parentOutput);
            final FutureTask<Void> task = new FutureTask<>(() -> {
                demux.write(new byte[] { 1, 2 });
                assertSame(parentOutput, demux.bindStream(childOutput));
                demux.write(new byte[] { 0, 3, 4, 0 }, 1, 2);
                return null;
            });
            final Thread thread = new Thread(task, "DemuxOutputStreamTest-child");
            thread.start();
            task.get(5, TimeUnit.SECONDS);
            demux.write(new byte[] { 5, 6 }, 0, 2);
            assertArrayEquals(new byte[] { 1, 2, 5, 6 }, parentOutput.delegate.toByteArray());
            assertArrayEquals(new byte[] { 3, 4 }, childOutput.delegate.toByteArray());
            assertEquals(2, parentOutput.bulkWriteCalls);
            assertEquals(1, childOutput.bulkWriteCalls);
            assertEquals(0, parentOutput.singleByteWriteCalls);
            assertEquals(0, childOutput.singleByteWriteCalls);
        }
    }

    @Test
    void testWriteByteArrayUnbind() throws IOException {
        final TrackingOutputStream output = new TrackingOutputStream();
        try (DemuxOutputStream demux = new DemuxOutputStream()) {
            assertNull(demux.bindStream(output));
            assertSame(output, demux.bindStream(null));
            demux.write(new byte[] { 1, 2 });
            demux.write(new byte[] { 1, 2 }, 1, 1);
            assertEquals(0, output.bulkWriteCalls);
            assertEquals(0, output.singleByteWriteCalls);
        }
    }
}
