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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FlushShieldOutputStream}.
 */
class FlushShieldOutputStreamTest {

    /** Tracks whether the underlying stream's close() was called. */
    private AtomicBoolean closed;

    /** Tracks whether the underlying stream's flush() was called. */
    private AtomicBoolean flushed;

    /** The stream under test. */
    private FlushShieldOutputStream shielded;

    /** The underlying byte-array-backed stream used as the delegate. */
    private ByteArrayOutputStream target;

    @BeforeEach
    void setUp() {
        flushed = new AtomicBoolean();
        closed = new AtomicBoolean();
        target = new ByteArrayOutputStream() {

            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }

            @Override
            public void flush() throws IOException {
                flushed.set(true);
                super.flush();
            }
        };
        shielded = new FlushShieldOutputStream(target);
    }

    @Test
    void testBuilderGet() throws IOException {
        assertNotNull(FlushShieldOutputStream.builder().setOutputStream(target).get());
    }

    @Test
    void testBuilderWithOutputStream() throws IOException {
        try (FlushShieldOutputStream built = FlushShieldOutputStream.builder().setOutputStream(target).get()) {
            assertNotNull(built);
            assertInstanceOf(FlushShieldOutputStream.class, built);
            // flush must be shielded for builder-created instances too
            built.flush();
            assertFalse(flushed.get(), "flush() via builder instance must NOT reach the underlying stream.");
            // writes must still work
            built.write('B');
            assertEquals(1, target.size());
            assertEquals('B', target.toByteArray()[0]);
        }
    }

    @Test
    void testCloseReachesUnderlying() throws IOException {
        assertFalse(closed.get(), "close should not have been called yet.");
        shielded.close();
        assertTrue(closed.get(), "close() must reach the underlying stream.");
    }

    @Test
    void testFlushCanBeCalledMultipleTimes() throws IOException {
        shielded.flush();
        shielded.flush();
        shielded.flush();
        assertFalse(flushed.get(), "repeated flush() calls must NOT reach the underlying stream.");
    }

    @Test
    void testFlushDoesNotDelegateToUnderlying() throws IOException {
        assertFalse(flushed.get(), "flush should not have been called yet.");
        shielded.flush();
        assertFalse(flushed.get(), "flush() must NOT reach the underlying stream.");
    }

    @Test
    void testWriteByteArray() throws IOException {
        final byte[] data = { 'H', 'i' };
        shielded.write(data);
        assertEquals(2, target.size());
        assertArrayEquals(data, target.toByteArray());
    }

    @Test
    void testWriteByteArrayWithOffset() throws IOException {
        final byte[] data = { 'X', 'Y', 'Z' };
        shielded.write(data, 1, 2);
        assertEquals(2, target.size());
        assertArrayEquals(new byte[] { 'Y', 'Z' }, target.toByteArray());
    }

    @Test
    void testWriteInt() throws IOException {
        shielded.write('A');
        assertEquals(1, target.size());
        assertEquals('A', target.toByteArray()[0]);
    }
}
