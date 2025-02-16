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

package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ThresholdingOutputStream}. See also the subclass {@link DeferredFileOutputStream}.
 *
 * @see DeferredFileOutputStream
 */
public class ThresholdingOutputStreamTest {

    /**
     * Asserts initial state without changing it.
     *
     * @param out the stream to test.
     * @param expectedThreshold the expected threshold.
     * @param expectedByeCount the expected byte count.
     */
    static void assertThresholdingInitialState(final ThresholdingOutputStream out, final int expectedThreshold, final int expectedByeCount) {
        assertFalse(out.isThresholdExceeded());
        assertEquals(expectedThreshold, out.getThreshold());
        assertEquals(expectedByeCount, out.getByteCount());
    }

    @Test
    public void testResetByteCount() throws IOException {
        final int threshold = 1;
        final AtomicInteger counter = new AtomicInteger();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, tos -> {
            counter.incrementAndGet();
            tos.resetByteCount();
        }, o -> os)) {
            assertThresholdingInitialState(out, threshold, 0);
            assertEquals(0, counter.get());
            out.write('a');
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertEquals(1, counter.get());
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            out.write('a');
            assertEquals(3, counter.get());
        }
    }

    @Test
    public void testResetByteCountBrokenOutputStream() throws IOException {
        final int threshold = 1;
        final AtomicInteger counter = new AtomicInteger();
        final IOException e = assertThrows(IOException.class, () -> {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, tos -> {
                counter.incrementAndGet();
                tos.resetByteCount();
            }, o -> BrokenOutputStream.INSTANCE)) {
                assertThresholdingInitialState(out, threshold, 0);
                assertEquals(0, counter.get());
                assertThrows(IOException.class, () -> out.write('a'));
                assertFalse(out.isThresholdExceeded());
                assertThrows(IOException.class, () -> out.write('a'));
                assertEquals(0, counter.get());
                assertFalse(out.isThresholdExceeded());
                assertThrows(IOException.class, () -> out.write('a'));
                assertThrows(IOException.class, () -> out.write('a'));
                assertEquals(0, counter.get());
            }
        });
        // Should only happen on close
        assertEquals("Broken output stream: close()", e.getMessage());
    }

    @Test
    public void testSetByteCountOutputStream() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean();
        final int initCount = 2;
        final int threshold = 3;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold) {
            {
                setByteCount(initCount);
            }

            @Override
            protected OutputStream getOutputStream() throws IOException {
                return new ByteArrayOutputStream(4);
            }

            @Override
            protected void thresholdReached() throws IOException {
                reached.set(true);
            }
        }) {
            assertThresholdingInitialState(out, threshold, initCount);
            out.write('a');
            assertFalse(reached.get());
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testSetByteCountStream() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean();
        final int initCount = 2;
        final int threshold = 3;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold) {
            {
                setByteCount(initCount);
            }

            @Override
            protected OutputStream getStream() throws IOException {
                return new ByteArrayOutputStream(4);
            }

            @Override
            protected void thresholdReached() throws IOException {
                reached.set(true);
            }
        }) {
            assertThresholdingInitialState(out, threshold, initCount);
            out.write('a');
            assertFalse(reached.get());
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testThresholdIOConsumer() throws IOException {
        final int threshold = 1;
        // Null threshold consumer
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, null,
            os -> new ByteArrayOutputStream(4))) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write('a');
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertTrue(out.isThresholdExceeded());
        }
        // Null output stream function
        final AtomicBoolean reached = new AtomicBoolean();
        reached.set(false);
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, os -> reached.set(true), null)) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write('a');
            assertFalse(reached.get());
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
        }
        // non-null inputs.
        reached.set(false);
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, os -> reached.set(true),
            os -> new ByteArrayOutputStream(4))) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write('a');
            assertFalse(reached.get());
            assertFalse(out.isThresholdExceeded());
            out.write('a');
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testThresholdIOConsumerIOException() throws IOException {
        final int threshold = 1;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, os -> {
            throw new IOException("Threshold reached.");
        }, os -> new ByteArrayOutputStream(4))) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write('a');
            assertFalse(out.isThresholdExceeded());
            assertThrows(IOException.class, () -> out.write('a'));
            assertFalse(out.isThresholdExceeded());
        }
    }

    @Test
    public void testThresholdIOConsumerUncheckedException() throws IOException {
        final int threshold = 1;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold, os -> {
            throw new IllegalStateException("Threshold reached.");
        }, os -> new ByteArrayOutputStream(4))) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write('a');
            assertFalse(out.isThresholdExceeded());
            assertThrows(IllegalStateException.class, () -> out.write('a'));
            assertFalse(out.isThresholdExceeded());
            assertInstanceOf(ByteArrayOutputStream.class, out.getOutputStream());
            assertFalse(out.isThresholdExceeded());
        }
    }

    /**
     * Tests the case where the threshold is negative.
     * The threshold is not reached until something is written to the stream.
     */
    @Test
    public void testThresholdLessThanZero() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean();
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(-1) {
            @Override
            protected void thresholdReached() throws IOException {
                reached.set(true);
            }
        }) {
            assertThresholdingInitialState(out, 0, 0);
            assertFalse(reached.get());
            out.write(89);
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
            assertInstanceOf(NullOutputStream.class, out.getOutputStream());
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testThresholdZero() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean();
        final int threshold = 0;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold) {
            @Override
            protected void thresholdReached() throws IOException {
                reached.set(true);
            }
        }) {
            assertThresholdingInitialState(out, threshold, 0);
            out.write(89);
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
            assertInstanceOf(NullOutputStream.class, out.getOutputStream());
            assertTrue(out.isThresholdExceeded());
        }
    }

    /**
     * Tests the case where no bytes are written.
     * The threshold is not reached until something is written to the stream.
     */
    @Test
    public void testThresholdZeroWrite() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean();
        final int threshold = 7;
        try (ThresholdingOutputStream out = new ThresholdingOutputStream(threshold) {
            @Override
            protected void thresholdReached() throws IOException {
                super.thresholdReached();
                reached.set(true);
            }
        }) {
            assertThresholdingInitialState(out, threshold, 0);
            assertFalse(reached.get());
            out.write(new byte[0]);
            assertFalse(out.isThresholdExceeded());
            assertFalse(reached.get());
            assertInstanceOf(NullOutputStream.class, out.getOutputStream());
            assertFalse(out.isThresholdExceeded());
        }
    }
}