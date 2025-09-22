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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive test suite for SeekableByteChannel implementations. Tests can be run against any SeekableByteChannel implementation by overriding the
 * createChannel() method in a subclass.
 */
abstract class AbstractSeekableByteChannelTest {

    private SeekableByteChannel channel;

    @TempDir
    protected Path tempDir;

    protected Path tempFile;

    private final Random random = new Random(42);

    /**
     * Creates the SeekableByteChannel to test.
     *
     * @return a new SeekableByteChannel.
     * @throws IOException Thrown when the SeekableByteChannel cannot be created.
     */
    protected abstract SeekableByteChannel createChannel() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = tempDir.resolve(getClass().getSimpleName() + ".tmp");
        channel = createChannel();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    void testCloseMultipleTimes() throws IOException {
        channel.close();
        channel.close(); // Should not throw
        assertFalse(channel.isOpen());
    }

    @Test
    void testConcurrentPositionAndSizeQueries() throws IOException {
        final byte[] data = "test data".getBytes();
        channel.write(ByteBuffer.wrap(data));
        final long size = channel.size();
        final long position = channel.position();
        assertEquals(data.length, size);
        assertEquals(data.length, position);
        // These values should be consistent
        assertEquals(channel.size(), size);
        assertEquals(channel.position(), position);
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        channel.close();
        assertFalse(channel.isOpen());
    }

    @Test
    void testIsOpennOnNew() {
        assertTrue(channel.isOpen());
    }

    @Test
    void testPartialWritesAndReads() throws IOException {
        final byte[] data = "0123456789".getBytes();
        // Write in chunks
        channel.write(ByteBuffer.wrap(data, 0, 5));
        channel.write(ByteBuffer.wrap(data, 5, 5));
        assertEquals(10, channel.size());
        // Read back in different chunks
        channel.position(0);
        final ByteBuffer buffer1 = ByteBuffer.allocate(3);
        final ByteBuffer buffer2 = ByteBuffer.allocate(7);
        assertEquals(3, channel.read(buffer1));
        assertEquals(7, channel.read(buffer2));
        assertArrayEquals(Arrays.copyOf(data, 3), buffer1.array());
        assertArrayEquals(Arrays.copyOfRange(data, 3, 10), buffer2.array());
    }

    @Test
    void testPositionBeyondSize() throws IOException {
        channel.write(ByteBuffer.wrap("test".getBytes()));
        channel.position(100);
        assertEquals(100, channel.position());
        assertEquals(4, channel.size()); // Size should not change
    }

    @ParameterizedTest
    @CsvSource({ "0, 0", "5, 5", "10, 10", "100, 100" })
    void testPositionInBounds(final long newPosition, final long expectedPosition) throws IOException {
        // Create file with enough data
        final byte[] data = new byte[200];
        random.nextBytes(data);
        channel.write(ByteBuffer.wrap(data));
        @SuppressWarnings("resource") // returns "this".
        final SeekableByteChannel result = channel.position(newPosition);
        assertSame(channel, result); // Javadoc: "This channel"
        assertEquals(expectedPosition, channel.position());
    }

    @Test
    void testPositionNegative() {
        assertThrows(IllegalArgumentException.class, () -> channel.position(-1));
    }

    @Test
    void testPositionOnClosed() throws IOException {
        channel.close();
        assertThrows(ClosedChannelException.class, () -> channel.position());
        assertThrows(ClosedChannelException.class, () -> channel.position(0));
    }

    @Test
    void testPositionOnNew() throws IOException {
        assertEquals(0, channel.position());
    }

    @Test
    void testRandomAccess() throws IOException {
        final byte[] data = new byte[1000];
        random.nextBytes(data);
        // Write initial data
        channel.write(ByteBuffer.wrap(data));
        // Perform random access operations
        final int[] positions = { 100, 500, 0, 999, 250 };
        for (final int pos : positions) {
            channel.position(pos);
            assertEquals(pos, channel.position());
            final ByteBuffer buffer = ByteBuffer.allocate(1);
            final int read = channel.read(buffer);
            if (pos < data.length) {
                assertEquals(1, read);
                assertEquals(data[pos], buffer.get(0));
            }
        }
    }

    @Test
    void testReadAtEndOfFile() throws IOException {
        channel.write(ByteBuffer.wrap("test".getBytes()));
        // Position is already at end after write
        final ByteBuffer buffer = ByteBuffer.allocate(10);
        final int read = channel.read(buffer);
        assertEquals(-1, read);
    }

    @Test
    void testReadBuffer() throws IOException {
        final byte[] data = "test".getBytes();
        channel.write(ByteBuffer.wrap(data));
        channel.position(0);
        final ByteBuffer buffer = ByteBuffer.allocate(100);
        final int read = channel.read(buffer);
        assertEquals(data.length, read);
        assertEquals(data.length, channel.position());
        // Verify only the expected bytes were read
        final byte[] readData = new byte[read];
        buffer.flip();
        buffer.get(readData);
        assertArrayEquals(data, readData);
    }

    @Test
    void testReadBytes() throws IOException {
        final byte[] data = "Hello, World!".getBytes();
        channel.write(ByteBuffer.wrap(data));
        channel.position(0);
        final ByteBuffer buffer = ByteBuffer.allocate(data.length);
        final int read = channel.read(buffer);
        assertEquals(data.length, read);
        assertArrayEquals(data, buffer.array());
        assertEquals(data.length, channel.position());
    }

    @Test
    void testReadClosed() throws IOException {
        channel.close();
        final ByteBuffer buffer = ByteBuffer.allocate(10);
        assertThrows(ClosedChannelException.class, () -> channel.read(buffer));
    }

    @Test
    void testReadEmpty() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(10);
        final int read = channel.read(buffer);
        assertEquals(-1, read);
        assertEquals(0, buffer.position());
    }

    @Test
    void testReadNull() {
        assertThrows(NullPointerException.class, () -> channel.read(null));
    }

    @Test
    void testReadSingleByte() throws IOException {
        // Write first
        channel.write(ByteBuffer.wrap(new byte[] { 42 }));
        channel.position(0);
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        final int read = channel.read(buffer);
        assertEquals(1, read);
        assertEquals(42, buffer.get(0));
        assertEquals(1, channel.position());
    }

    @Test
    void testSizeAfterTruncateToLargerSize() throws IOException {
        channel.write(ByteBuffer.wrap("Hello".getBytes()));
        assertEquals(5, channel.size());
        channel.truncate(10);
        assertEquals(5, channel.size()); // Size should remain unchanged
    }

    @Test
    void testSizeAfterWrite() throws IOException {
        assertEquals(0, channel.size());
        channel.write(ByteBuffer.wrap("Hello".getBytes()));
        assertEquals(5, channel.size());
        channel.write(ByteBuffer.wrap(" World".getBytes()));
        assertEquals(11, channel.size());
    }

    @Test
    void testSizeOnClosed() throws IOException {
        channel.close();
        assertThrows(ClosedChannelException.class, () -> channel.size());
    }

    @Test
    void testSizeOnNew() throws IOException {
        assertEquals(0, channel.size());
    }

    @Test
    void testSizeSameOnOverwrite() throws IOException {
        channel.write(ByteBuffer.wrap("Hello World".getBytes()));
        assertEquals(11, channel.size());
        channel.position(6);
        channel.write(ByteBuffer.wrap("Test".getBytes()));
        assertEquals(11, channel.size()); // Size should not change
    }

    @Test
    void testTruncateNegative() {
        assertThrows(IllegalArgumentException.class, () -> channel.truncate(-1));
    }

    @Test
    void testTruncateShrinks() throws IOException {
        channel.write(ByteBuffer.wrap("Hello World".getBytes()));
        assertEquals(11, channel.size());
        channel.truncate(5);
        assertEquals(5, channel.size());
        // Position should be adjusted if it was beyond new size
        if (channel.position() > 5) {
            assertEquals(5, channel.position());
        }
    }

    @Test
    void testWriteBeyondSizeGrows() throws IOException {
        channel.position(100);
        final byte[] data = "test".getBytes();
        channel.write(ByteBuffer.wrap(data));
        assertEquals(104, channel.size());
        assertEquals(104, channel.position());
        // Verify the gap contains zeros (implementation dependent)
        channel.position(0);
        final ByteBuffer buffer = ByteBuffer.allocate(100);
        channel.read(buffer);
        // Most implementations will fill gaps with zeros
        final byte[] expectedGap = new byte[100];
        assertArrayEquals(expectedGap, buffer.array());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 100, 1000, 10000 })
    void testWriteDifferentSizes(final int size) throws IOException {
        final byte[] data = new byte[size];
        random.nextBytes(data);
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int byteCount = channel.write(buffer);
        assertEquals(size, byteCount);
        assertEquals(size, channel.position());
        assertEquals(size, channel.size());
    }
    @Test
    void testWriteEmpty() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(0);
        final int byteCount = channel.write(buffer);
        assertEquals(0, byteCount);
        assertEquals(0, channel.position());
        assertEquals(0, channel.size());
    }
    @Test
    void testWriteNull() {
        assertThrows(NullPointerException.class, () -> channel.write(null));
    }
    @Test
    void testWritePositionReadVerify() throws IOException {
        final byte[] originalData = "Hello, SeekableByteChannel World!".getBytes();
        // Write data
        channel.write(ByteBuffer.wrap(originalData));
        // Seek to beginning
        channel.position(0);
        // Read data back
        final ByteBuffer readBuffer = ByteBuffer.allocate(originalData.length);
        final int bytesRead = channel.read(readBuffer);
        assertEquals(originalData.length, bytesRead);
        assertArrayEquals(originalData, readBuffer.array());
    }

    @Test
    void testWriteSingleByte() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 42);
        buffer.flip();
        final int written = channel.write(buffer);
        assertEquals(1, written);
        assertEquals(1, channel.position());
        assertEquals(1, channel.size());
    }

    @Test
    void testWriteToClosedChannel() throws IOException {
        channel.close();
        final ByteBuffer buffer = ByteBuffer.wrap("test".getBytes());
        assertThrows(ClosedChannelException.class, () -> channel.write(buffer));
    }

    @Test
    void tesWriteBytes() throws IOException {
        final byte[] data = "Hello, World!".getBytes();
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int byteCount = channel.write(buffer);
        assertEquals(data.length, byteCount);
        assertEquals(data.length, channel.position());
        assertEquals(data.length, channel.size());
    }
}
