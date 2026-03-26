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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A sanity test to make sure {@link AbstractSeekableByteChannelTest} works for files.
 */
public class ByteArraySeekableByteChannelTest extends AbstractSeekableByteChannelTest {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String STRING = "Some data";
    private static final byte[] BYTE_ARRAY = STRING.getBytes(CHARSET);

    static Stream<Arguments> testConstructor() {
        return Stream.of(
                Arguments.of((IOSupplier<ByteArraySeekableByteChannel>) ByteArraySeekableByteChannel::new, EMPTY_BYTE_ARRAY, IOUtils.DEFAULT_BUFFER_SIZE),
                Arguments.of((IOSupplier<ByteArraySeekableByteChannel>) () -> new ByteArraySeekableByteChannel(8), EMPTY_BYTE_ARRAY, 8),
                Arguments.of((IOSupplier<ByteArraySeekableByteChannel>) () -> new ByteArraySeekableByteChannel(16), EMPTY_BYTE_ARRAY, 16),
                Arguments.of((IOSupplier<ByteArraySeekableByteChannel>) () -> ByteArraySeekableByteChannel.wrap(EMPTY_BYTE_ARRAY), EMPTY_BYTE_ARRAY, 0),
                Arguments.of((IOSupplier<ByteArraySeekableByteChannel>) () -> ByteArraySeekableByteChannel.wrap(BYTE_ARRAY), BYTE_ARRAY, BYTE_ARRAY.length));
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

    @Override
    protected SeekableByteChannel createChannel() throws IOException {
        return new ByteArraySeekableByteChannel();
    }

    @Test
    void testBuilderDefaultConstructor() throws IOException {
        try (ByteArraySeekableByteChannel channel = new ByteArraySeekableByteChannel.Builder().get()) {
            assertEquals(0, channel.position());
            assertEquals(0, channel.size());
            assertEquals(0, channel.array().length);
        }
    }

    @Test
    void testBuilderDefaultMethod() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder().get()) {
            assertEquals(0, channel.position());
            assertEquals(0, channel.size());
            assertEquals(0, channel.array().length);
        }
    }

    @Test
    void testBuilderReadOnly() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setByteArray(BYTE_ARRAY)
                .setOpenOptions(StandardOpenOption.READ)
                .get()) {
            assertEquals(0, channel.position());
            assertEquals(9, channel.size());
            assertEquals(9, channel.array().length);
            assertThrows(NonWritableChannelException.class, () -> channel.write(ByteBuffer.wrap(BYTE_ARRAY)));
            assertThrows(NonWritableChannelException.class, () -> channel.truncate(0));
        }
    }

    private void testBuilderReadWrite(final ByteArraySeekableByteChannel channel) throws ClosedChannelException, IOException {
        assertEquals(0, channel.position());
        assertEquals(9, channel.size());
        assertEquals(9, channel.array().length);
        channel.truncate(0);
        assertEquals(0, channel.position());
        channel.write(ByteBuffer.wrap(BYTE_ARRAY));
        assertEquals(9, channel.size());
        assertEquals(9, channel.array().length);
    }

    @Test
    void testBuilderReadWriteExplict() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setByteArray(BYTE_ARRAY)
                .setOpenOptions(StandardOpenOption.READ, StandardOpenOption.WRITE)
                .get()) {
            testBuilderReadWrite(channel);
        }
    }

    @Test
    void testBuilderReadWriteImplicit() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setByteArray(BYTE_ARRAY)
                .get()) {
            testBuilderReadWrite(channel);
        }
    }

    @Test
    void testBuilderSetByteArray() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setByteArray(BYTE_ARRAY)
                .get()) {
            assertEquals(0, channel.position());
            assertEquals(9, channel.size());
            assertEquals(9, channel.array().length);
        }
    }

    @Test
    void testBuilderSetByteArrayEmpty() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setByteArray(ArrayUtils.EMPTY_BYTE_ARRAY)
                .get()) {
            assertEquals(0, channel.position());
            assertEquals(0, channel.size());
            assertEquals(0, channel.array().length);
        }
    }

    @Test
    void testBuilderSetCharSequence() throws IOException {
        try (ByteArraySeekableByteChannel channel = ByteArraySeekableByteChannel.builder()
                .setCharSequence(STRING)
                .setCharset(CHARSET)
                .get()) {
            assertEquals(0, channel.position());
            assertEquals(9, channel.size());
            assertEquals(9, channel.array().length);
        }
    }

    @ParameterizedTest
    @MethodSource
    void testConstructor(final IOSupplier<ByteArraySeekableByteChannel> supplier, final byte[] expected, final int capacity) throws IOException {
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

    @Test
    void testPositionBeyondSizeReadWrite() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        channel.position(channel.size() + 1);
        assertEquals(channel.size() + 1, channel.position());
        assertEquals(-1, channel.read(buffer));
        channel.position(Integer.MAX_VALUE + 1L);
        assertEquals(Integer.MAX_VALUE + 1L, channel.position());
        assertEquals(-1, channel.read(buffer));
        // ByteArraySeekableByteChannel has a hard boundary at Integer.MAX_VALUE, files don't.
        assertThrows(IOException.class, () -> channel.write(buffer));
        assertThrows(IllegalArgumentException.class, () -> channel.position(-1));
        assertThrows(IllegalArgumentException.class, () -> channel.position(Integer.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> channel.position(Long.MIN_VALUE));
    }

    @ParameterizedTest
    @MethodSource
    void testShouldResizeWhenWritingMoreDataThanCapacity(final byte[] data, final int wanted) throws IOException {
        try (ByteArraySeekableByteChannel c = ByteArraySeekableByteChannel.wrap(data)) {
            c.position(data.length);
            final ByteBuffer inData = ByteBuffer.wrap(new byte[wanted]);
            final int writeCount = c.write(inData);
            assertEquals(wanted, writeCount);
            assertTrue(c.array().length >= data.length + wanted, "Capacity not increased sufficiently");
        }
    }

}
