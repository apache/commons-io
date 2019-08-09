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
package org.apache.commons.io.input.buffer;

import java.util.Objects;

/**
 * A buffer, which doesn't need reallocation of byte arrays, because it
 * reuses a single byte array. This works particularly well, if reading
 * from the buffer takes place at the same time than writing to. Such is the
 * case, for example, when using the buffer within a filtering input stream,
 * like the {@link CircularBufferInputStream}.
 */
public class CircularByteBuffer {
    private final byte[] buffer;
    private int startOffset, endOffset, currentNumberOfBytes;

    /**
     * Creates a new instance with the given buffer size.
     *
     * @param pSize the size of buffer to create
     */
    public CircularByteBuffer(final int pSize) {
        buffer = new byte[pSize];
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }

    /**
     * Creates a new instance with a reasonable default buffer size (8192).
     */
    public CircularByteBuffer() {
        this(8192);
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
     * @param pBuffer The byte array, where to add bytes.
     * @param pOffset The offset, where to store bytes in the byte array.
     * @param pLength The number of bytes to return.
     * @throws NullPointerException     The byte array {@code pBuffer} is null.
     * @throws IllegalArgumentException Either of {@code pOffset}, or {@code pLength} is negative,
     *                                  or the length of the byte array {@code pBuffer} is too small.
     * @throws IllegalStateException    The buffer doesn't hold the given number
     *                                  of bytes. Use {@link #getCurrentNumberOfBytes()} to prevent this
     *                                  exception.
     */
    public void read(final byte[] pBuffer, final int pOffset, final int pLength) {
        Objects.requireNonNull(pBuffer);
        if (pOffset < 0 || pOffset >= pBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + pOffset);
        }
        if (pLength < 0 || pLength > buffer.length) {
            throw new IllegalArgumentException("Invalid length: " + pLength);
        }
        if (pOffset + pLength > pBuffer.length) {
            throw new IllegalArgumentException("The supplied byte array contains only "
                    + pBuffer.length + " bytes, but offset, and length would require "
                    + (pOffset + pLength - 1));
        }
        if (currentNumberOfBytes < pLength) {
            throw new IllegalStateException("Currently, there are only " + currentNumberOfBytes
                    + "in the buffer, not " + pLength);
        }
        int offset = pOffset;
        for (int i = 0; i < pLength; i++) {
            pBuffer[offset++] = buffer[startOffset];
            --currentNumberOfBytes;
            if (++startOffset == buffer.length) {
                startOffset = 0;
            }
        }
    }

    /**
     * Adds a new byte to the buffer, which will eventually be returned by following
     * invocations of {@link #read()}.
     *
     * @param pByte The byte, which is being added to the buffer.
     * @throws IllegalStateException The buffer is full. Use {@link #hasSpace()},
     *                               or {@link #getSpace()}, to prevent this exception.
     */
    public void add(final byte pByte) {
        if (currentNumberOfBytes >= buffer.length) {
            throw new IllegalStateException("No space available");
        }
        buffer[endOffset] = pByte;
        ++currentNumberOfBytes;
        if (++endOffset == buffer.length) {
            endOffset = 0;
        }
    }

    /**
     * Returns, whether the next bytes in the buffer are exactly those, given by
     * {@code pBuffer}, {@code pOffset}, and {@code pLength}. No bytes are being
     * removed from the buffer. If the result is true, then the following invocations
     * of {@link #read()} are guaranteed to return exactly those bytes.
     *
     * @param pBuffer the buffer to compare against
     * @param pOffset start offset
     * @param pLength length to compare
     * @return True, if the next invocations of {@link #read()} will return the
     * bytes at offsets {@code pOffset}+0, {@code pOffset}+1, ...,
     * {@code pOffset}+{@code pLength}-1 of byte array {@code pBuffer}.
     * @throws IllegalArgumentException Either of {@code pOffset}, or {@code pLength} is negative.
     * @throws NullPointerException     The byte array {@code pBuffer} is null.
     */
    public boolean peek(final byte[] pBuffer, final int pOffset, final int pLength) {
        Objects.requireNonNull(pBuffer, "Buffer");
        if (pOffset < 0 || pOffset >= pBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + pOffset);
        }
        if (pLength < 0 || pLength > buffer.length) {
            throw new IllegalArgumentException("Invalid length: " + pLength);
        }
        if (pLength < currentNumberOfBytes) {
            return false;
        }
        int offset = startOffset;
        for (int i = 0; i < pLength; i++) {
            if (buffer[offset] != pBuffer[i + pOffset]) {
                return false;
            }
            if (++offset == buffer.length) {
                offset = 0;
            }
        }
        return true;
    }

    /**
     * Adds the given bytes to the buffer. This is the same as invoking {@link #add(byte)}
     * for the bytes at offsets {@code pOffset}+0, {@code pOffset}+1, ...,
     * {@code pOffset}+{@code pLength}-1 of byte array {@code pBuffer}.
     *
     * @param pBuffer the buffer to copy
     * @param pOffset start offset
     * @param pLength length to copy
     * @throws IllegalStateException    The buffer doesn't have sufficient space. Use
     *                                  {@link #getSpace()} to prevent this exception.
     * @throws IllegalArgumentException Either of {@code pOffset}, or {@code pLength} is negative.
     * @throws NullPointerException     The byte array {@code pBuffer} is null.
     */
    public void add(final byte[] pBuffer, final int pOffset, final int pLength) {
        Objects.requireNonNull(pBuffer, "Buffer");
        if (pOffset < 0 || pOffset >= pBuffer.length) {
            throw new IllegalArgumentException("Invalid offset: " + pOffset);
        }
        if (pLength < 0) {
            throw new IllegalArgumentException("Invalid length: " + pLength);
        }
        if (currentNumberOfBytes + pLength > buffer.length) {
            throw new IllegalStateException("No space available");
        }
        for (int i = 0; i < pLength; i++) {
            buffer[endOffset] = pBuffer[pOffset + i];
            if (++endOffset == buffer.length) {
                endOffset = 0;
            }
        }
        currentNumberOfBytes += pLength;
    }

    /**
     * Returns, whether there is currently room for a single byte in the buffer.
     * Same as {@link #hasSpace(int) hasSpace(1)}.
     *
     * @return true if there is space for a byte
     * @see #hasSpace(int)
     * @see #getSpace()
     */
    public boolean hasSpace() {
        return currentNumberOfBytes < buffer.length;
    }

    /**
     * Returns, whether there is currently room for the given number of bytes in the buffer.
     *
     * @param pBytes the byte count
     * @return true if there is space for the given number of bytes
     * @see #hasSpace()
     * @see #getSpace()
     */
    public boolean hasSpace(final int pBytes) {
        return currentNumberOfBytes + pBytes <= buffer.length;
    }

    /**
     * Returns, whether the buffer is currently holding, at least, a single byte.
     *
     * @return true if the buffer is not empty
     */
    public boolean hasBytes() {
        return currentNumberOfBytes > 0;
    }

    /**
     * Returns the number of bytes, that can currently be added to the buffer.
     *
     * @return the number of bytes that can be added
     */
    public int getSpace() {
        return buffer.length - currentNumberOfBytes;
    }

    /**
     * Returns the number of bytes, that are currently present in the buffer.
     *
     * @return the number of bytes
     */
    public int getCurrentNumberOfBytes() {
        return currentNumberOfBytes;
    }

    /**
     * Removes all bytes from the buffer.
     */
    public void clear() {
        startOffset = 0;
        endOffset = 0;
        currentNumberOfBytes = 0;
    }
}
