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
package org.apache.commons.io.input.buffer;

import java.util.Objects;

import org.apache.commons.io.IOUtils;

/**
 * A buffer, which doesn't need reallocation of byte arrays, because it
 * reuses a single byte array. This works particularly well, if reading
 * from the buffer takes place at the same time than writing to. Such is the
 * case, for example, when using the buffer within a filtering input stream,
 * like the {@link CircularBufferInputStream}.
 *
 * @since 2.7
 */
public class CircularByteBuffer {

    private final byte[] buffer;
    private int startOffset;
    private int endOffset;
    private int currentNumberOfBytes;

    /**
     * Constructs a new instance with a reasonable default buffer size ({@link IOUtils#DEFAULT_BUFFER_SIZE}).
     */
    public CircularByteBuffer() {
        this(IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance with the given buffer size.
     *
     * @param size the size of buffer to create
     */
    public CircularByteBuffer(final int size) {
        buffer = IOUtils.byteArray(size);
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

    /**
     * Adds a new byte to the buffer, which will eventually be returned by following
     * invocations of {@link #read()}.
     *
     * @param value The byte, which is being added to the buffer.
     * @throws IllegalStateException The buffer is full. Use {@link #hasSpace()},
     *                               or {@link #getSpace()}, to prevent this exception.
     */
    public void add(final byte value) {
        if (currentNumberOfBytes >= buffer.length) {
            throw new IllegalStateException("No space available");
        }
        buffer[endOffset] = value;
        ++currentNumberOfBytes;
        if (++endOffset == buffer.length) {
            endOffset = 0;
        }
    }

    /**
     * Adds the given bytes to the buffer. This is the same as invoking {@link #add(byte)}
     * for the bytes at offsets {@code offset+0}, {@code offset+1}, ...,
     * {@code offset+length-1} of byte array {@code targetBuffer}.
     *
     * @param targetBuffer the buffer to copy
     * @param offset start offset
     * @param length length to copy
     * @throws IllegalStateException    The buffer doesn't have sufficient space. Use
     *                                  {@link #getSpace()} to prevent this exception.
     * @throws IllegalArgumentException Either of {@code offset}, or {@code length} is negative.
     * @throws NullPointerException     The byte array {@code targetBuffer} is null.
     */
    public void add(final byte[] targetBuffer, final int offset, final int length) {
        Objects.requireNonNull(targetBuffer, "Buffer");
        if (offset < 0 || offset >= targetBuffer.length) {
            throw new IllegalArgumentException("Illegal offset: " + offset);
        }
        if (length < 0) {
            throw new IllegalArgumentException("Illegal length: " + length);
        }
        if (currentNumberOfBytes + length > buffer.length) {
            throw new IllegalStateException("No space available");
        }
        for (int i = 0; i < length; i++) {
            buffer[endOffset] = targetBuffer[offset + i];
            if (++endOffset == buffer.length) {
                endOffset = 0;
            }
        }
        currentNumberOfBytes += length;
    }

    /**
     * Removes all bytes from the buffer.
     */
    public void clear() {
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

    /**
     * Gets the number of bytes, that are currently present in the buffer.
     *
     * @return the number of bytes
     */
    public int getCurrentNumberOfBytes() {
        return currentNumberOfBytes;
    }

    /**
     * Gets the number of bytes, that can currently be added to the buffer.
     *
     * @return the number of bytes that can be added
     */
    public int getSpace() {
        return buffer.length - currentNumberOfBytes;
    }

    /**
     * Tests whether the buffer is currently holding at least a single byte.
     *
     * @return true whether the buffer is currently holding at least a single byte.
     */
    public boolean hasBytes() {
        return currentNumberOfBytes > 0;
    }

    /**
     * Tests whether there is currently room for a single byte in the buffer.
     * Same as {@link #hasSpace(int) hasSpace(1)}.
     *
     * @return true whether there is currently room for a single byte in the buffer.
     * @see #hasSpace(int)
     * @see #getSpace()
     */
    public boolean hasSpace() {
        return currentNumberOfBytes < buffer.length;
    }

    /**
     * Tests whether there is currently room for the given number of bytes in the buffer.
     *
     * @param count the byte count
     * @return true whether there is currently room for the given number of bytes in the buffer.
     * @see #hasSpace()
     * @see #getSpace()
     */
    public boolean hasSpace(final int count) {
        return currentNumberOfBytes + count <= buffer.length;
    }

    /**
     * Returns, whether the next bytes in the buffer are exactly those, given by
     * {@code sourceBuffer}, {@code offset}, and {@code length}. No bytes are being
     * removed from the buffer. If the result is true, then the following invocations
     * of {@link #read()} are guaranteed to return exactly those bytes.
     *
     * @param sourceBuffer the buffer to compare against
     * @param offset start offset
     * @param length length to compare
     * @return True, if the next invocations of {@link #read()} will return the
     * bytes at offsets {@code sourceBuffer}+0, {@code offset}+1, ...,
     * {@code offset}+{@code length}-1 of byte array {@code sourceBuffer}.
     * @throws IllegalArgumentException Either of {@code sourceBuffer}, or {@code length} is negative.
     * @throws NullPointerException     The byte array {@code sourceBuffer} is null.
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) {
        Objects.requireNonNull(sourceBuffer, "Buffer");
        if (offset < 0 || offset >= sourceBuffer.length) {
            throw new IllegalArgumentException("Illegal offset: " + offset);
        }
        if (length < 0 || length > buffer.length) {
            throw new IllegalArgumentException("Illegal length: " + length);
        }
        if (length < currentNumberOfBytes) {
            return false;
        }
        int localOffset = startOffset;
        for (int i = 0; i < length; i++) {
            if (buffer[localOffset] != sourceBuffer[i + offset]) {
                return false;
            }
            if (++localOffset == buffer.length) {
                localOffset = 0;
            }
        }
        return true;
    }

    /**
     * Returns the next byte from the buffer, removing it at the same time, so
     * that following invocations won't return it again.
     *
     * @return The byte, which is being returned.
     * @throws IllegalStateException The buffer is empty. Use {@link #hasBytes()},
     *                               or {@link #getCurrentNumberOfBytes()}, to prevent this exception.
     */
    public byte read() {
        if (currentNumberOfBytes <= 0) {
            throw new IllegalStateException("No bytes available.");
        }
        final byte b = buffer[startOffset];
        --currentNumberOfBytes;
        if (++startOffset == buffer.length) {
            startOffset = 0;
        }
        return b;
    }

    /**
     * Returns the given number of bytes from the buffer by storing them in
     * the given byte array at the given offset.
     *
     * @param targetBuffer The byte array, where to add bytes.
     * @param targetOffset The offset, where to store bytes in the byte array.
     * @param length The number of bytes to return.
     * @throws NullPointerException     The byte array {@code targetBuffer} is null.
     * @throws IllegalArgumentException Either of {@code targetOffset}, or {@code length} is negative,
     *                                  or the length of the byte array {@code targetBuffer} is too small.
     * @throws IllegalStateException    The buffer doesn't hold the given number
     *                                  of bytes. Use {@link #getCurrentNumberOfBytes()} to prevent this
     *                                  exception.
     */
    public void read(final byte[] targetBuffer, final int targetOffset, final int length) {
        Objects.requireNonNull(targetBuffer, "targetBuffer");
        if (targetOffset < 0 || targetOffset >= targetBuffer.length) {
            throw new IllegalArgumentException("Illegal offset: " + targetOffset);
        }
        if (length < 0 || length > buffer.length) {
            throw new IllegalArgumentException("Illegal length: " + length);
        }
        if (targetOffset + length > targetBuffer.length) {
            throw new IllegalArgumentException("The supplied byte array contains only "
                    + targetBuffer.length + " bytes, but offset, and length would require "
                    + (targetOffset + length - 1));
        }
        if (currentNumberOfBytes < length) {
            throw new IllegalStateException("Currently, there are only " + currentNumberOfBytes
                    + "in the buffer, not " + length);
        }
        int offset = targetOffset;
        for (int i = 0; i < length; i++) {
            targetBuffer[offset++] = buffer[startOffset];
            --currentNumberOfBytes;
            if (++startOffset == buffer.length) {
                startOffset = 0;
            }
        }
    }
}
