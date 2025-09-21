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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

/**
 * An in-memory {@link SeekableByteChannel} backed by a growable {@code byte[]} buffer.
 *
 * @since 2.21.0
 */
public class ByteArrayChannel implements SeekableByteChannel {

    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    /**
     * Constructs a channel that wraps the given byte array.
     * <p>
     * The resulting channel will share the given array as its buffer, until a write operation
     * requires a larger capacity.
     * The initial size of the channel is the length of the given array, and the initial position is 0.
     * </p>
     * @param bytes The byte array to wrap; must not be {@code null}.
     * @return A new channel that wraps the given byte array; never {@code null}.
     * @throws NullPointerException If the byte array is {@code null}.
     */
    public static ByteArrayChannel wrap(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        return new ByteArrayChannel(bytes, bytes.length);
    }

    // package-private for testing
    byte[] data;
    private int position;
    private int count;
    private volatile boolean closed;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a channel with the default initial capacity.
     * <p>
     * The initial size is 0, and the initial position is 0.
     * </p>
     */
    public ByteArrayChannel() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs a channel with the given initial capacity.
     * <p>
     * The initial size is 0, and the initial position is 0.
     * </p>
     * @param initialCapacity The initial capacity; must be non-negative.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public ByteArrayChannel(int initialCapacity) {
        this(byteArray(initialCapacity), 0);
    }

    private static byte[] byteArray(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Size must be non-negative");
        }
        return new byte[value];
    }

    private ByteArrayChannel(byte[] data, int count) {
        this.data = data;
        this.position = 0;
        this.count = count;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
        lock.lock();
        try {
            final int remaining = dst.remaining();
            if (remaining == 0) {
                return 0;
            }
            if (position >= count) {
                return -1; // EOF
            }
            final int n = Math.min(count - position, remaining);
            dst.put(data, position, n);
            position += n;
            return n;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        ensureOpen();
        lock.lock();
        try {
            final int remaining = src.remaining();
            if (remaining == 0) {
                return 0;
            }
            final int newPosition = position + remaining;
            ensureCapacity(newPosition);
            src.get(data, position, remaining);
            position = newPosition;
            if (newPosition > count) {
                count = newPosition;
            }
            return remaining;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long position() throws IOException {
        ensureOpen();
        lock.lock();
        try {
            return position;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition < 0L || newPosition > IOUtils.SOFT_MAX_ARRAY_LENGTH) {
            throw new IOException("position must be in range [0, " + IOUtils.SOFT_MAX_ARRAY_LENGTH + "]");
        }
        ensureOpen();
        lock.lock();
        try {
            this.position = (int) newPosition; // allowed to be > count; reads will return -1
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (size < 0L || size > IOUtils.SOFT_MAX_ARRAY_LENGTH) {
            throw new IOException("size must be in range [0, " + IOUtils.SOFT_MAX_ARRAY_LENGTH + "]");
        }
        ensureOpen();
        lock.lock();
        try {
            final int newSize = (int) size;
            if (newSize < count) {
                // shrink logical size; do not allocate
                count = newSize;

            }
            if (newSize < position) {
                position = newSize;
            }
            // if newSize >= count: no effect
            return this;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() {
        closed = true;
    }

    private void ensureOpen() throws ClosedChannelException {
        if (closed) {
            throw new ClosedChannelException();
        }
    }

    private void ensureCapacity(int minCapacity) {
        // Guard against integer overflow and against exceeding the soft maximum.
        // Negative values signal overflow in the (position + remaining) arithmetic.
        if (minCapacity < 0 || minCapacity > IOUtils.SOFT_MAX_ARRAY_LENGTH) {
            throw new OutOfMemoryError("required array size " + minCapacity + " too large");
        }
        // The current buffer is already big enough.
        if (minCapacity <= data.length) {
            return;
        }
        // Increase capacity geometrically (double the current size) to reduce reallocation cost.
        // Always honor the requested minimum; if doubling overflows, use the minimum instead.
        final int newCapacity = Math.max(data.length << 1, minCapacity);
        // If geometric growth overshoots the soft maximum (but still fits in int),
        // clamp to the soft maximum. minCapacity has already been validated to be ≤ soft max.
        data = Arrays.copyOf(data, Math.min(newCapacity, IOUtils.SOFT_MAX_ARRAY_LENGTH));
    }

    /**
     * Returns a copy of the logical contents.
     * @return A copy of the logical contents, never {@code null}.
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(data, count);
    }
}
