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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Buffers}.
 */
public class BuffersTest {

    private static final int CAPACITY = 8;

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link ByteBuffer}.
     */
    @Test
    void testClearBufferDispatchByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link CharBuffer}.
     */
    @Test
    void testClearBufferDispatchCharBuffer() {
        final CharBuffer buffer = CharBuffer.allocate(CAPACITY);
        buffer.put(new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link DoubleBuffer}.
     */
    @Test
    void testClearBufferDispatchDoubleBuffer() {
        final DoubleBuffer buffer = DoubleBuffer.allocate(CAPACITY);
        buffer.put(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0 });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link FloatBuffer}.
     */
    @Test
    void testClearBufferDispatchFloatBuffer() {
        final FloatBuffer buffer = FloatBuffer.allocate(CAPACITY);
        buffer.put(new float[] { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0f, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with an {@link IntBuffer}.
     */
    @Test
    void testClearBufferDispatchIntBuffer() {
        final IntBuffer buffer = IntBuffer.allocate(CAPACITY);
        buffer.put(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link LongBuffer}.
     */
    @Test
    void testClearBufferDispatchLongBuffer() {
        final LongBuffer buffer = LongBuffer.allocate(CAPACITY);
        buffer.put(new long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0L, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with a {@link ShortBuffer}.
     */
    @Test
    void testClearBufferDispatchShortBuffer() {
        final ShortBuffer buffer = ShortBuffer.allocate(CAPACITY);
        buffer.put(new short[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final Buffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, buffer.position());
        assertEquals(CAPACITY, buffer.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((short) 0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(Buffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearBufferNull() {
        assertNull(Buffers.clear((Buffer) null));
    }

    /**
     * Tests {@link Buffers#clear(ByteBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearByteBufferArrayBacked() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final ByteBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(ByteBuffer)} with a direct buffer.
     */
    @Test
    void testClearByteBufferDirect() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((byte) (i + 1));
        }
        final ByteBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(ByteBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearByteBufferNull() {
        assertNull(Buffers.clear((ByteBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(ByteBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearByteBufferReturnsSameInstance() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clear(CharBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearCharBufferArrayBacked() {
        final CharBuffer buffer = CharBuffer.allocate(CAPACITY);
        buffer.put(new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' });
        final CharBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(CharBuffer)} with a direct (view) buffer.
     */
    @Test
    void testClearCharBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Character.BYTES);
        final CharBuffer buffer = byteBuffer.asCharBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((char) (i + 1));
        }
        final CharBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(CharBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearCharBufferNull() {
        assertNull(Buffers.clear((CharBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(CharBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearCharBufferReturnsSameInstance() {
        final CharBuffer buffer = CharBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clearDirect(Buffer)} with a direct {@link ByteBuffer}.
     */
    @Test
    void testClearDirectWithDirectByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((byte) (i + 1));
        }
        final Buffer result = Buffers.clearDirect(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearDirect(Buffer)} with a direct {@link IntBuffer}.
     */
    @Test
    void testClearDirectWithDirectIntBuffer() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Integer.BYTES);
        final IntBuffer buffer = byteBuffer.asIntBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i + 1);
        }
        final Buffer result = Buffers.clearDirect(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearDirect(Buffer)} with a non-direct {@link ByteBuffer}.
     */
    @Test
    void testClearDirectWithNonDirectByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((byte) (i + 1));
        }
        buffer.flip();
        final Buffer result = Buffers.clearDirect(buffer);
        assertSame(buffer, result);
        // Position and limit should be unchanged (still flipped state)
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        // Content is unchanged
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((byte) (i + 1), buffer.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearDirect(Buffer)} with {@code null}.
     */
    @Test
    void testClearDirectWithNull() {
        assertNull(Buffers.clearDirect(null));
    }

    /**
     * Tests {@link Buffers#clear(DoubleBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearDoubleBufferArrayBacked() {
        final DoubleBuffer buffer = DoubleBuffer.allocate(CAPACITY);
        buffer.put(new double[] { 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8 });
        final DoubleBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(DoubleBuffer)} with a direct (view) buffer .
     */
    @Test
    void testClearDoubleBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Double.BYTES);
        final DoubleBuffer buffer = byteBuffer.asDoubleBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i + 1.0);
        }
        final DoubleBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(DoubleBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearDoubleBufferNull() {
        assertNull(Buffers.clear((DoubleBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(DoubleBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearDoubleBufferReturnsSameInstance() {
        final DoubleBuffer buffer = DoubleBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clear(FloatBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearFloatBufferArrayBacked() {
        final FloatBuffer buffer = FloatBuffer.allocate(CAPACITY);
        buffer.put(new float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f });
        final FloatBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0f, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(FloatBuffer)} with a direct (view) buffer.
     */
    @Test
    void testClearFloatBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Float.BYTES);
        final FloatBuffer buffer = byteBuffer.asFloatBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i + 1.0f);
        }
        final FloatBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0f, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(FloatBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearFloatBufferNull() {
        assertNull(Buffers.clear((FloatBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(FloatBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearFloatBufferReturnsSameInstance() {
        final FloatBuffer buffer = FloatBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clear(IntBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearIntBufferArrayBacked() {
        final IntBuffer buffer = IntBuffer.allocate(CAPACITY);
        buffer.put(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final IntBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(IntBuffer)} with a direct (view) buffer.
     */
    @Test
    void testClearIntBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Integer.BYTES);
        final IntBuffer buffer = byteBuffer.asIntBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i + 1);
        }
        final IntBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(IntBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearIntBufferNull() {
        assertNull(Buffers.clear((IntBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(IntBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearIntBufferReturnsSameInstance() {
        final IntBuffer buffer = IntBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clear(LongBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearLongBufferArrayBacked() {
        final LongBuffer buffer = LongBuffer.allocate(CAPACITY);
        buffer.put(new long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L });
        final LongBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0L, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(LongBuffer)} with a direct (view) buffer.
     */
    @Test
    void testClearLongBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Long.BYTES);
        final LongBuffer buffer = byteBuffer.asLongBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i + 1L);
        }
        final LongBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0L, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(LongBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearLongBufferNull() {
        assertNull(Buffers.clear((LongBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(LongBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearLongBufferReturnsSameInstance() {
        final LongBuffer buffer = LongBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clear(ShortBuffer)} with an array-backed buffer.
     */
    @Test
    void testClearShortBufferArrayBacked() {
        final ShortBuffer buffer = ShortBuffer.allocate(CAPACITY);
        buffer.put(new short[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final ShortBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((short) 0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(ShortBuffer)} with a direct (view) buffer.
     */
    @Test
    void testClearShortBufferDirect() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY * Short.BYTES);
        final ShortBuffer buffer = byteBuffer.asShortBuffer();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((short) (i + 1));
        }
        final ShortBuffer result = Buffers.clear(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((short) 0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clear(ShortBuffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearShortBufferNull() {
        assertNull(Buffers.clear((ShortBuffer) null));
    }

    /**
     * Tests {@link Buffers#clear(ShortBuffer)} returns the same buffer instance.
     */
    @Test
    void testClearShortBufferReturnsSameInstance() {
        final ShortBuffer buffer = ShortBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clear(buffer));
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link ByteBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableByteBufferReadOnly() {
        final ByteBuffer writable = ByteBuffer.allocate(CAPACITY);
        writable.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        writable.flip();
        final ByteBuffer readOnly = writable.asReadOnlyBuffer();
        final ByteBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        // Content and position should be unchanged
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((byte) (i + 1), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link ByteBuffer} zeros all bytes and resets position.
     */
    @Test
    void testClearWritableByteBufferWritable() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final ByteBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((byte) 0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link CharBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableCharBufferReadOnly() {
        final CharBuffer writable = CharBuffer.allocate(CAPACITY);
        writable.put(new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' });
        writable.flip();
        final CharBuffer readOnly = writable.asReadOnlyBuffer();
        final CharBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((char) ('a' + i), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link CharBuffer} zeros all chars.
     */
    @Test
    void testClearWritableCharBufferWritable() {
        final CharBuffer buffer = CharBuffer.allocate(CAPACITY);
        buffer.put(new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' });
        final CharBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((char) 0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a direct writable {@link ByteBuffer} zeros all bytes.
     */
    @Test
    void testClearWritableDirectByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put((byte) (i + 1));
        }
        final ByteBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((byte) 0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link DoubleBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableDoubleBufferReadOnly() {
        final DoubleBuffer writable = DoubleBuffer.allocate(CAPACITY);
        writable.put(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0 });
        writable.flip();
        final DoubleBuffer readOnly = writable.asReadOnlyBuffer();
        final DoubleBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((double) (i + 1), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link DoubleBuffer} zeros all doubles.
     */
    @Test
    void testClearWritableDoubleBufferWritable() {
        final DoubleBuffer buffer = DoubleBuffer.allocate(CAPACITY);
        buffer.put(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0 });
        final DoubleBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link FloatBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableFloatBufferReadOnly() {
        final FloatBuffer writable = FloatBuffer.allocate(CAPACITY);
        writable.put(new float[] { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f });
        writable.flip();
        final FloatBuffer readOnly = writable.asReadOnlyBuffer();
        final FloatBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((float) (i + 1), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link FloatBuffer} zeros all floats.
     */
    @Test
    void testClearWritableFloatBufferWritable() {
        final FloatBuffer buffer = FloatBuffer.allocate(CAPACITY);
        buffer.put(new float[] { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f });
        final FloatBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0.0f, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link IntBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableIntBufferReadOnly() {
        final IntBuffer writable = IntBuffer.allocate(CAPACITY);
        writable.put(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        writable.flip();
        final IntBuffer readOnly = writable.asReadOnlyBuffer();
        final IntBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(i + 1, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link IntBuffer} zeros all ints.
     */
    @Test
    void testClearWritableIntBufferWritable() {
        final IntBuffer buffer = IntBuffer.allocate(CAPACITY);
        buffer.put(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final IntBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link LongBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableLongBufferReadOnly() {
        final LongBuffer writable = LongBuffer.allocate(CAPACITY);
        writable.put(new long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L });
        writable.flip();
        final LongBuffer readOnly = writable.asReadOnlyBuffer();
        final LongBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((long) (i + 1), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link LongBuffer} zeros all longs.
     */
    @Test
    void testClearWritableLongBufferWritable() {
        final LongBuffer buffer = LongBuffer.allocate(CAPACITY);
        buffer.put(new long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L });
        final LongBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(0L, result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with {@code null} returns {@code null}.
     */
    @Test
    void testClearWritableNull() {
        assertNull(Buffers.clearWritable(null));
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} returns the same buffer instance for a writable buffer.
     */
    @Test
    void testClearWritableReturnsSameInstance() {
        final ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        assertSame(buffer, Buffers.clearWritable(buffer));
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a read-only {@link ShortBuffer} leaves it unchanged.
     */
    @Test
    void testClearWritableShortBufferReadOnly() {
        final ShortBuffer writable = ShortBuffer.allocate(CAPACITY);
        writable.put(new short[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        writable.flip();
        final ShortBuffer readOnly = writable.asReadOnlyBuffer();
        final ShortBuffer result = Buffers.clearWritable(readOnly);
        assertSame(readOnly, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((short) (i + 1), result.get(i));
        }
    }

    /**
     * Tests {@link Buffers#clearWritable(Buffer)} with a writable {@link ShortBuffer} zeros all shorts.
     */
    @Test
    void testClearWritableShortBufferWritable() {
        final ShortBuffer buffer = ShortBuffer.allocate(CAPACITY);
        buffer.put(new short[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        final ShortBuffer result = Buffers.clearWritable(buffer);
        assertSame(buffer, result);
        assertEquals(0, result.position());
        assertEquals(CAPACITY, result.limit());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals((short) 0, result.get(i));
        }
    }

}
