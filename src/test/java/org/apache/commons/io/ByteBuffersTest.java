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

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ByteBuffers}.
 */
public class ByteBuffersTest {

    /**
     * Tests {@link ByteBuffers#littleEndian(byte[])} with a non-empty array.
     */
    @Test
    void testLittleEndianByteArray() {
        final byte[] array = { 1, 2, 3, 4 };
        final ByteBuffer buffer = ByteBuffers.littleEndian(array);
        assertNotNull(buffer);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
        assertEquals(0, buffer.position());
        assertEquals(array.length, buffer.limit());
        assertEquals(array.length, buffer.capacity());
        assertArrayEquals(array, buffer.array());
        assertEquals(0, buffer.arrayOffset());
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(byte[])} with an empty array.
     */
    @Test
    void testLittleEndianByteArrayEmpty() {
        final byte[] array = {};
        final ByteBuffer buffer = ByteBuffers.littleEndian(array);
        assertNotNull(buffer);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
        assertEquals(0, buffer.capacity());
        assertArrayEquals(array, buffer.array());
    }

    /**
     * Tests that {@link ByteBuffers#littleEndian(byte[])} wraps the given array (same backing array).
     */
    @Test
    void testLittleEndianByteArrayIsSameBackingArray() {
        final byte[] array = { 10, 20, 30 };
        final ByteBuffer buffer = ByteBuffers.littleEndian(array);
        assertSame(array, buffer.array());
    }

    /**
     * Tests that a multi-byte value written to a little-endian buffer from a byte array has correct byte order.
     */
    @Test
    void testLittleEndianByteArrayMultibyteValue() {
        final byte[] array = new byte[4];
        final ByteBuffer buffer = ByteBuffers.littleEndian(array);
        buffer.putInt(0x01020304);
        // In little-endian, least significant byte is first
        assertEquals((byte) 0x04, array[0]);
        assertEquals((byte) 0x03, array[1]);
        assertEquals((byte) 0x02, array[2]);
        assertEquals((byte) 0x01, array[3]);
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(ByteBuffer)} sets order to little-endian and returns the same buffer.
     */
    @Test
    void testLittleEndianByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        // Default order is BIG_ENDIAN
        assertEquals(ByteOrder.BIG_ENDIAN, buffer.order());
        final ByteBuffer result = ByteBuffers.littleEndian(buffer);
        assertSame(buffer, result);
        assertEquals(ByteOrder.LITTLE_ENDIAN, result.order());
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(ByteBuffer)} when the buffer is already little-endian.
     */
    @Test
    void testLittleEndianByteBufferAlreadyLittleEndian() {
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer result = ByteBuffers.littleEndian(buffer);
        assertSame(buffer, result);
        assertEquals(ByteOrder.LITTLE_ENDIAN, result.order());
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(int)} with a positive capacity.
     */
    @Test
    void testLittleEndianInt() {
        final int capacity = 16;
        final ByteBuffer buffer = ByteBuffers.littleEndian(capacity);
        assertNotNull(buffer);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
        assertEquals(capacity, buffer.capacity());
        assertEquals(0, buffer.position());
        assertEquals(capacity, buffer.limit());
        assertEquals(0, buffer.arrayOffset());
        // Each element should be initialized to zero
        for (int i = 0; i < capacity; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests that a multi-byte value written to a little-endian buffer has correct byte order.
     */
    @Test
    void testLittleEndianIntMultibyteValue() {
        final ByteBuffer buffer = ByteBuffers.littleEndian(4);
        buffer.putInt(0x01020304);
        buffer.flip();
        // In little-endian, least significant byte is first
        assertEquals((byte) 0x04, buffer.get(0));
        assertEquals((byte) 0x03, buffer.get(1));
        assertEquals((byte) 0x02, buffer.get(2));
        assertEquals((byte) 0x01, buffer.get(3));
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(int)} with a negative capacity throws {@link IllegalArgumentException}.
     */
    @Test
    void testLittleEndianIntNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> ByteBuffers.littleEndian(-1));
    }

    /**
     * Tests {@link ByteBuffers#littleEndian(int)} with zero capacity.
     */
    @Test
    void testLittleEndianIntZeroCapacity() {
        final ByteBuffer buffer = ByteBuffers.littleEndian(0);
        assertNotNull(buffer);
        assertEquals(ByteOrder.LITTLE_ENDIAN, buffer.order());
        assertEquals(0, buffer.capacity());
    }

}
