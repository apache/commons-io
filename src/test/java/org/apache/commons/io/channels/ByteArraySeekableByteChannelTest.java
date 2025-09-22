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

package org.apache.commons.io.channels;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A sanity test to make sure {@link AbstractSeekableByteChannelTest} works for files.
 */
public class ByteArraySeekableByteChannelTest extends AbstractSeekableByteChannelTest {

    @Override
    protected SeekableByteChannel createChannel() throws IOException {
        return new ByteArraySeekableByteChannel();
    }

    private static final byte[] testData = "Some data".getBytes(StandardCharsets.UTF_8);

    private static byte[] getTestData() {
        return testData.clone();
    }

    static Stream<Arguments> testConstructor() {
        return Stream.of(
                Arguments.of(
                        (IOSupplier<ByteArraySeekableByteChannel>) ByteArraySeekableByteChannel::new,
                        EMPTY_BYTE_ARRAY,
                        0),
                Arguments.of(
                        (IOSupplier<ByteArraySeekableByteChannel>) () -> new ByteArraySeekableByteChannel(8),
                        EMPTY_BYTE_ARRAY,
                        8),
                Arguments.of(
                        (IOSupplier<ByteArraySeekableByteChannel>) () -> new ByteArraySeekableByteChannel(16),
                        EMPTY_BYTE_ARRAY,
                        16),
                Arguments.of(
                        (IOSupplier<ByteArraySeekableByteChannel>)
                                () -> ByteArraySeekableByteChannel.wrap(EMPTY_BYTE_ARRAY),
                        EMPTY_BYTE_ARRAY,
                        0),
                Arguments.of(
                        (IOSupplier<ByteArraySeekableByteChannel>)
                                () -> ByteArraySeekableByteChannel.wrap(getTestData()),
                        getTestData(),
                        testData.length));
    }

    @ParameterizedTest
    @MethodSource
    void testConstructor(IOSupplier<ByteArraySeekableByteChannel> supplier, byte[] expected, int capacity) throws IOException {
        try (ByteArraySeekableByteChannel channel = supplier.get()) {
            assertEquals(0, channel.position());
            assertEquals(expected.length, channel.size());
            assertEquals(capacity, channel.array().length);
            assertArrayEquals(expected, channel.toByteArray());
        }
    }

    @Test
    void testConstructorInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new ByteArraySeekableByteChannel(-1));
        assertThrows(NullPointerException.class, () -> ByteArraySeekableByteChannel.wrap(null));
    }

    static Stream<Arguments> testShouldResizeWhenWritingMoreDataThanCapacity() {
        return Stream.of(
                // Resize from 0
                Arguments.of(EMPTY_BYTE_ARRAY, 1),
                // Resize less than double
                Arguments.of(new byte[8], 1),
                // Resize more that double
                Arguments.of(new byte[8], 20));
    }

    @ParameterizedTest
    @MethodSource
    void testShouldResizeWhenWritingMoreDataThanCapacity(byte[] data, int wanted) throws IOException {
        try (ByteArraySeekableByteChannel c = ByteArraySeekableByteChannel.wrap(data)) {
            c.position(data.length);
            final ByteBuffer inData = ByteBuffer.wrap(new byte[wanted]);
            final int writeCount = c.write(inData);
            assertEquals(wanted, writeCount);
            assertTrue(c.array().length >= data.length + wanted, "Capacity not increased sufficiently");
        }
    }

}
