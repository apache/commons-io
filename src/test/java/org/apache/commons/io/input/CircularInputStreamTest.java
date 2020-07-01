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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CircularInputStream}.
 */
public class CircularInputStreamTest {

    private void assertStreamOutput(final byte[] toCycle, final byte[] expected) throws IOException {
        final byte[] actual = new byte[expected.length];

        try (InputStream infStream = createInputStream(toCycle, -1)) {
            final int actualReadBytes = infStream.read(actual);

            assertArrayEquals(expected, actual);
            assertEquals(expected.length, actualReadBytes);
        }
    }

    private InputStream createInputStream(final byte[] repeatContent, final long targetByteCount) {
        return new CircularInputStream(repeatContent, targetByteCount);
    }

    @Test
    public void testContainsEofInputSize0() {
        assertThrows(IllegalArgumentException.class, () -> createInputStream(new byte[] { -1 }, 0));
    }

    @Test
    public void testCount0() throws IOException {
        try (InputStream in = createInputStream(new byte[] { 1, 2 }, 0)) {
            assertEquals(IOUtils.EOF, in.read());
        }
    }

    @Test
    public void testCount0InputSize0() {
        assertThrows(IllegalArgumentException.class, () -> createInputStream(new byte[] {}, 0));
    }

    @Test
    public void testCount0InputSize1() throws IOException {
        try (InputStream in = createInputStream(new byte[] { 1 }, 0)) {
            assertEquals(IOUtils.EOF, in.read());
        }
    }

    @Test
    public void testCount1InputSize1() throws IOException {
        try (InputStream in = createInputStream(new byte[] { 1 }, 1)) {
            assertEquals(1, in.read());
            assertEquals(IOUtils.EOF, in.read());
        }
    }

    @Test
    public void testCycleBytes() throws IOException {
        final byte[] input = new byte[] { 1, 2 };
        final byte[] expected = new byte[] { 1, 2, 1, 2, 1 };

        assertStreamOutput(input, expected);
    }

    @Test
    public void testNullInputSize0() {
        assertThrows(NullPointerException.class, () -> createInputStream(null, 0));
    }

    @Test
    public void testWholeRangeOfBytes() throws IOException {
        final int size = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
        final byte[] contentToCycle = new byte[size];
        byte value = Byte.MIN_VALUE;
        for (int i = 0; i < contentToCycle.length; i++) {
            contentToCycle[i] = value == IOUtils.EOF ? 0 : value;
            value++;
        }

        final byte[] expectedOutput = Arrays.copyOf(contentToCycle, size);

        assertStreamOutput(contentToCycle, expectedOutput);
    }

}
