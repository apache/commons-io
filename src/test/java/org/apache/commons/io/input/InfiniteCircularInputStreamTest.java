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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class InfiniteCircularInputStreamTest {

    @Test
    public void should_cycle_bytes() throws IOException {
        final byte[] input = new byte[] { 1, 2 };
        final byte[] expected = new byte[] { 1, 2, 1, 2, 1 };

        assertStreamOutput(input, expected);
    }

    @Test
    public void should_handle_whole_range_of_bytes() throws IOException {
        final int size = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
        final byte[] contentToCycle = new byte[size];
        byte value = Byte.MIN_VALUE;
        for (int i = 0; i < contentToCycle.length; i++) {
            contentToCycle[i] = value++;
        }

        final byte[] expectedOutput = Arrays.copyOf(contentToCycle, size);

        assertStreamOutput(contentToCycle, expectedOutput);
    }

    private void assertStreamOutput(final byte[] toCycle, final byte[] expected) throws IOException {
        final byte[] actual = new byte[expected.length];

        try (InputStream infStream = new InfiniteCircularInputStream(toCycle)) {
            final int actualReadBytes = infStream.read(actual);

            assertArrayEquals(expected, actual);
            assertEquals(expected.length, actualReadBytes);
        }
    }

}
