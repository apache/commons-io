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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ThresholdingOutputStream}.
 */
public class ThresholdingOutputStreamTest {

    @Test
    public void testThresholdLessThanZero() throws IOException {
        try (final ThresholdingOutputStream out = new ThresholdingOutputStream(-1)) {
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testThresholdZero() throws IOException {
        final AtomicBoolean reached = new AtomicBoolean(false);
        try (final ThresholdingOutputStream out = new ThresholdingOutputStream(0) {
            @Override
            protected void thresholdReached() throws IOException {
                reached.set(true);
            }
        }) {
            assertFalse(out.isThresholdExceeded());
            out.write(89);
            assertTrue(reached.get());
            assertTrue(out.isThresholdExceeded());
        }
    }

    @Test
    public void testSetByteCount_OutputStream() throws Exception {
        final AtomicBoolean reached = new AtomicBoolean(false);
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(3) {
            {
                setByteCount(2);
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
            tos.write('a');
            assertFalse(reached.get());
            tos.write('a');
            assertTrue(reached.get());
        }
    }

    @Test
    public void testSetByteCount_Stream() throws Exception {
        final AtomicBoolean reached = new AtomicBoolean(false);
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(3) {
            {
                setByteCount(2);
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
            tos.write('a');
            assertFalse(reached.get());
            tos.write('a');
            assertTrue(reached.get());
        }
    }

    @Test
    public void testThresholdIOConsumer() throws Exception {
        final AtomicBoolean reached = new AtomicBoolean();
        // Null threshold consumer
        reached.set(false);
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(1, null,
            os -> new ByteArrayOutputStream(4))) {
            tos.write('a');
            assertFalse(reached.get());
            tos.write('a');
            assertFalse(reached.get());
        }
        // Null output stream function
        reached.set(false);
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(1, os -> reached.set(true), null)) {
            tos.write('a');
            assertFalse(reached.get());
            tos.write('a');
            assertTrue(reached.get());
        }
        // non-null inputs.
        reached.set(false);
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(1, os -> reached.set(true),
            os -> new ByteArrayOutputStream(4))) {
            tos.write('a');
            assertFalse(reached.get());
            tos.write('a');
            assertTrue(reached.get());
        }
    }

    @Test
    public void testThresholdIOConsumerIOException() throws Exception {
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(1, os -> {
            throw new IOException("Threshold reached.");
        }, os -> new ByteArrayOutputStream(4))) {
            tos.write('a');
            assertThrows(IOException.class, () -> tos.write('a'));
        }
    }

    @Test
    public void testThresholdIOConsumerUncheckedException() throws Exception {
        try (ThresholdingOutputStream tos = new ThresholdingOutputStream(1, os -> {
            throw new IllegalStateException("Threshold reached.");
        }, os -> new ByteArrayOutputStream(4))) {
            tos.write('a');
            assertThrows(IllegalStateException.class, () -> tos.write('a'));
        }
    }
}