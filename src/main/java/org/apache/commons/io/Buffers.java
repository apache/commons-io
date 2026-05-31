/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.io;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * Helps use {@link Buffer} instances.
 *
 * @since 2.23.0
 */
public final class Buffers {

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer, or null.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     * @throws UnsupportedOperationException Thrown if the given buffer is not one of {@link CharBuffer}, {@link ByteBuffer}, {@link DoubleBuffer},
     *                                       {@link FloatBuffer}, {@link IntBuffer}, {@link LongBuffer}, {@link ShortBuffer}, or null.
     */
    public static Buffer clear(final Buffer buffer) {
        if (buffer instanceof CharBuffer) {
            return clear((CharBuffer) buffer);
        }
        if (buffer instanceof ByteBuffer) {
            return clear((ByteBuffer) buffer);
        }
        if (buffer instanceof DoubleBuffer) {
            return clear((DoubleBuffer) buffer);
        }
        if (buffer instanceof FloatBuffer) {
            return clear((FloatBuffer) buffer);
        }
        if (buffer instanceof IntBuffer) {
            return clear((IntBuffer) buffer);
        }
        if (buffer instanceof LongBuffer) {
            return clear((LongBuffer) buffer);
        }
        if (buffer instanceof ShortBuffer) {
            return clear((ShortBuffer) buffer);
        }
        if (buffer == null) {
            return null;
        }
        throw new UnsupportedOperationException(buffer.getClass().getCanonicalName());
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static ByteBuffer clear(final ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), (byte) 0);
        } else {
            final byte[] zeros = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static CharBuffer clear(final CharBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), (char) 0);
        } else {
            final char[] zeros = new char[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static DoubleBuffer clear(final DoubleBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), 0);
        } else {
            final double[] zeros = new double[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static FloatBuffer clear(final FloatBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), 0);
        } else {
            final float[] zeros = new float[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static IntBuffer clear(final IntBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), 0);
        } else {
            final int[] zeros = new int[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static LongBuffer clear(final LongBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), 0);
        } else {
            final long[] zeros = new long[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * Clears this buffer by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is discarded.
     *
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     * @throws ReadOnlyBufferException If the buffer is read-only.
     */
    public static ShortBuffer clear(final ShortBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        if (clearBuffer(buffer).hasArray()) {
            Arrays.fill(buffer.array(), (short) 0);
        } else {
            final short[] zeros = new short[IOUtils.DEFAULT_BUFFER_SIZE];
            while (buffer.hasRemaining()) {
                buffer.put(zeros, 0, Math.min(buffer.remaining(), zeros.length));
            }
        }
        return clearBuffer(buffer);
    }

    /**
     * A better typed version of {@link Buffer#clear()}.
     * <p>
     * Clears this buffer. The position is set to zero, the limit is set to the capacity, and the mark is discarded.
     * </p>
     * <p>
     * Invoke this method before using a sequence of channel-read or <em>put</em> operations to fill this buffer. For example:
     * </p>
     *
     * <pre>
     * buf.clear(); // Prepare buffer for reading
     * in.read(buf); // Read data
     * </pre>
     *
     * <p>
     * This method does not actually erase the data in the buffer, but it is named as if it did because it will most often be used in situations in which that
     * might as well be the case.
     * </p>
     *
     * @param <B> A Buffer subclass.
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     */
    private static <B extends Buffer> B clearBuffer(final B buffer) {
        buffer.clear();
        return buffer;
    }

    /**
     * Clears the given <em>direct</em> buffer by filling it with zeros and resetting the position to zero. The limit is set to the capacity of the buffer.
     * <p>
     * If the given buffer is a not direct buffer, nothing happens to that buffer.
     * </p>
     *
     * @param <B>    A Buffer subclass.
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     */
    public static <B extends Buffer> B clearDirect(final B buffer) {
        if (buffer != null && buffer.isDirect()) {
            clear(buffer);
        }
        return buffer;
    }

    /**
     * Clears the given <em>writable</em> buffer by filling it with zeros and resetting the position to zero. The limit is set to the capacity of the buffer.
     * <p>
     * If the buffer is read-only, then nothing happens to that buffer.
     * </p>
     *
     * @param <B>    A Buffer subclass.
     * @param buffer The buffer to clear, may be null.
     * @return The given buffer.
     */
    public static <B extends Buffer> B clearWritable(final B buffer) {
        if (buffer != null && !buffer.isReadOnly()) {
            clear(buffer);
        }
        return buffer;
    }

    /**
     * No instances.
     */
    private Buffers() {
        // empty.
    }
}
