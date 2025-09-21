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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ByteArrayChannelTest {

    private static final byte[] testData = "Some data".getBytes(UTF_8);

    private static byte[] getTestData() {
        return testData.clone();
    }

    static Stream<Arguments> testConstructor() {
        return Stream.of(
                Arguments.of((IOSupplier<ByteArrayChannel>) ByteArrayChannel::new, EMPTY_BYTE_ARRAY, 32),
                Arguments.of((IOSupplier<ByteArrayChannel>) () -> new ByteArrayChannel(8), EMPTY_BYTE_ARRAY, 8),
                Arguments.of((IOSupplier<ByteArrayChannel>) () -> new ByteArrayChannel(16), EMPTY_BYTE_ARRAY, 16),
                Arguments.of(
                        (IOSupplier<ByteArrayChannel>) () -> ByteArrayChannel.wrap(EMPTY_BYTE_ARRAY), EMPTY_BYTE_ARRAY, 0),
                Arguments.of((IOSupplier<ByteArrayChannel>) () -> ByteArrayChannel.wrap(getTestData()), getTestData(), testData.length));
    }

    @ParameterizedTest
    @MethodSource
    void testConstructor(IOSupplier<ByteArrayChannel> supplier, byte[] expected, int capacity) throws IOException {
        try (ByteArrayChannel channel = supplier.get()) {
            assertEquals(0, channel.position());
            assertEquals(expected.length, channel.size());
            assertEquals(capacity, channel.data.length);
            assertArrayEquals(expected, channel.toByteArray());
        }
    }

    @Test
    void testConstructorInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new ByteArrayChannel(-1));
        assertThrows(NullPointerException.class, () -> ByteArrayChannel.wrap(null));
    }

    @Test
    void testCloseIdempotent() {
        final ByteArrayChannel channel = new ByteArrayChannel();
        channel.close();
        assertFalse(channel.isOpen());
        channel.close();
        assertFalse(channel.isOpen());
    }

    static Stream<IOConsumer<ByteArrayChannel>> testThrowsAfterClose() {
        return Stream.of(
                channel -> channel.read(ByteBuffer.allocate(1)),
                channel -> channel.write(ByteBuffer.allocate(1)),
                ByteArrayChannel::position,
                channel -> channel.position(0),
                ByteArrayChannel::size,
                channel -> channel.truncate(0));
    }

    @ParameterizedTest
    @MethodSource
    void testThrowsAfterClose(IOConsumer<ByteArrayChannel> consumer) {
        final ByteArrayChannel channel = new ByteArrayChannel();
        channel.close();
        assertThrows(ClosedChannelException.class, () -> consumer.accept(channel));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 9, 15})
    void testPosition(long expected) throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            assertEquals(0, channel.position(), "initial position");
            channel.position(expected);
            assertEquals(expected, channel.position(), "set position");
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, Integer.MAX_VALUE - 7, Integer.MAX_VALUE + 1L})
    void testPositionInvalid(long position) {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            assertThrows(IOException.class, () -> channel.position(position), "position " + position);
        }
    }

    static Stream<Arguments> testRead() {
        return Stream.of(
                Arguments.of(0, 0, "", 0),
                Arguments.of(0, 4, "Some", 4),
                Arguments.of(5, 9, "data", 4),
                Arguments.of(0, 9, "Some data", 9),
                // buffer larger than data
                Arguments.of(0, 10, "Some data", 9),
                // offset beyond end
                Arguments.of(10, 10, "", -1));
    }

    @ParameterizedTest
    @MethodSource
    void testRead(int offset, int bufferSize, String expected, int expectedRead) throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            channel.position(offset);
            final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            final int read = channel.read(buffer);
            assertEquals(expectedRead, read, "read");
            assertEquals(expected, new String(buffer.array(), 0, Math.max(0, read), UTF_8), "data");
            assertEquals(offset + Math.max(0, expectedRead), channel.position(), "position");
        }
    }

    @Test
    void testMultipleRead() throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            final ByteBuffer buffer = ByteBuffer.allocate(4);
            int read = channel.read(buffer);
            assertEquals(4, read, "first read");
            assertEquals("Some", new String(buffer.array(), 0, read, UTF_8), "first data");
            assertEquals(4, channel.position(), "first position");

            buffer.clear();
            read = channel.read(buffer);
            assertEquals(4, read, "second read");
            assertEquals(" dat", new String(buffer.array(), 0, read, UTF_8), "second data");
            assertEquals(8, channel.position(), "second position");

            buffer.clear();
            read = channel.read(buffer);
            assertEquals(1, read, "third read");
            assertEquals("a", new String(buffer.array(), 0, read, UTF_8), "third data");
            assertEquals(9, channel.position(), "third position");

            buffer.clear();
            read = channel.read(buffer);
            assertEquals(-1, read, "fourth read");
            assertEquals(9, channel.position(), "fourth position");
        }
    }

    static Stream<Arguments> testWrite() {
        return Stream.of(
                Arguments.of(1, "", 0, "Some data"),
                Arguments.of(0, "More", 4, "More data"),
                Arguments.of(5, "doll", 4, "Some doll"),
                // extend
                Arguments.of(9, "!", 1, "Some data!"),
                // offset beyond end
                Arguments.of(12, "!!!", 3, "Some data\0\0\0!!!"));
    }

    @ParameterizedTest
    @MethodSource
    void testWrite(int offset, String toWrite, int expectedWritten, String expectedData) throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            channel.position(offset);
            final ByteBuffer buffer = ByteBuffer.wrap(toWrite.getBytes(UTF_8));
            final int written = channel.write(buffer);
            assertEquals(expectedWritten, written, "written");
            assertEquals(expectedData, new String(channel.toByteArray(), UTF_8), "data");
            assertEquals(offset + expectedWritten, channel.position(), "position");
        }
    }

    @Test
    void testSize() throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            assertEquals(testData.length, channel.size(), "size");
            channel.position(testData.length + 10);
            assertEquals(testData.length, channel.size(), "size after position beyond end");
            channel.write(ByteBuffer.wrap("More".getBytes(UTF_8)));
            assertEquals(testData.length + 10 + 4, channel.size(), "size after write beyond end");
        }
    }

    static Stream<Arguments> testTruncate() {
        return Stream.of(
                Arguments.of(0, 0, ""),
                Arguments.of(0, 4, ""),
                Arguments.of(4, 0, "Some"),
                Arguments.of(4, 5, "Some"),
                Arguments.of(9, 0, "Some data"),
                Arguments.of(9, 10, "Some data"),
                // extend - no effect
                Arguments.of(15, 0, "Some data"),
                Arguments.of(15, 20, "Some data"));
    }

    @ParameterizedTest
    @MethodSource
    void testTruncate(int size, int initialPosition, String expectedData) throws Exception {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            channel.position(initialPosition);
            channel.truncate(size);
            assertEquals(expectedData, new String(channel.toByteArray(), UTF_8), "data");
            // Size changes only if size < initial size
            assertEquals(Math.min(size, testData.length), channel.size(), "size");
            // Position changes only if size < initial position
            assertEquals(Math.min(size, initialPosition), channel.position(), "position");
        }
    }

    @Test
    void testTruncateInvalid() {
        try (ByteArrayChannel channel = ByteArrayChannel.wrap(getTestData())) {
            assertThrows(IOException.class, () -> channel.truncate(-1));
            assertThrows(IOException.class, () -> channel.truncate((long) Integer.MAX_VALUE + 1));
        }
    }
}
