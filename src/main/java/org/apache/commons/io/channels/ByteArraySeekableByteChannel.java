/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

/**
 * A {@link SeekableByteChannel} implementation backed by a byte array.
 * <p>
 * When used for writing, the internal buffer grows to accommodate incoming data. The natural size limit is the value of {@link IOUtils#SOFT_MAX_ARRAY_LENGTH}
 * and it's not possible to {@link #position(long) set the position} or {@link #truncate(long) truncate} to a value bigger than that. The raw internal buffer is
 * accessed via {@link ByteArraySeekableByteChannel#array()}.
 * </p>
 *
 * @since 2.19.0
 */
public class ByteArraySeekableByteChannel implements SeekableByteChannel {

    private static final int RESIZE_LIMIT = Integer.MAX_VALUE >> 1;
    private byte[] data;
    private final AtomicBoolean closed = new AtomicBoolean();
    private int position;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a new instance using a default empty buffer.
     */
    public ByteArraySeekableByteChannel() {
        this(0);
    }

    /**
     * Constructs a new instance from a byte array.
     *
     * @param data input data or pre-allocated array.
     */
    public ByteArraySeekableByteChannel(final byte[] data) {
        this.data = data;
        this.size = data.length;
    }

    /**
     * Constructs a new instance from a size of storage to be allocated.
     *
     * @param size size of internal buffer to allocate, in bytes.
     */
    public ByteArraySeekableByteChannel(final int size) {
        this(new byte[size]);
    }

    /**
     * Gets the raw byte array backing this channel, <em>this is not a copy</em>.
     * <p>
     * NOTE: The returned buffer is not aligned with containing data, use {@link #size()} to obtain the size of data stored in the buffer.
     * </p>
     *
     * @return internal byte array.
     */
    public byte[] array() {
        return data;
    }

    private void checkOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    private int checkRange(final long newSize, final String method) {
        if (newSize < 0L || newSize > IOUtils.SOFT_MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException(String.format("%s must be in range [0..%,d]: %,d", method, IOUtils.SOFT_MAX_ARRAY_LENGTH, newSize));
        }
        return (int) newSize;
    }

    @Override
    public void close() {
        closed.set(true);
    }

    /**
     * Like {@link #size()} but never throws {@link ClosedChannelException}.
     *
     * @return See {@link #size()}.
     */
    public long getSize() {
        return size;
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public long position() throws ClosedChannelException {
        checkOpen();
        lock.lock();
        try {
            return position;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SeekableByteChannel position(final long newPosition) throws IOException {
        checkOpen();
        final int intPos = checkRange(newPosition, "position()");
        lock.lock();
        try {
            position = intPos;
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public int read(final ByteBuffer buf) throws IOException {
        checkOpen();
        lock.lock();
        try {
            int wanted = buf.remaining();
            final int possible = size - position;
            if (possible <= 0) {
                return IOUtils.EOF;
            }
            if (wanted > possible) {
                wanted = possible;
            }
            buf.put(data, position, wanted);
            position += wanted;
            return wanted;
        } finally {
            lock.unlock();
        }
    }

    private void resize(final int newLength) {
        int len = data.length;
        if (len <= 0) {
            len = 1;
        }
        if (newLength < RESIZE_LIMIT) {
            while (len < newLength) {
                len <<= 1;
            }
        } else { // avoid overflow
            len = newLength;
        }
        data = Arrays.copyOf(data, len);
    }

    @Override
    public long size() throws ClosedChannelException {
        checkOpen();
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SeekableByteChannel truncate(final long newSize) throws ClosedChannelException {
        checkOpen();
        final int intSize = checkRange(newSize, "truncate()");
        lock.lock();
        try {
            if (size > intSize) {
                size = intSize;
            }
            if (position > intSize) {
                position = intSize;
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    @Override
    public int write(final ByteBuffer b) throws IOException {
        checkOpen();
        lock.lock();
        try {
            int wanted = b.remaining();
            final int possibleWithoutResize = size - position;
            if (wanted > possibleWithoutResize) {
                final int newSize = position + wanted;
                if (newSize < 0) { // overflow
                    resize(IOUtils.SOFT_MAX_ARRAY_LENGTH);
                    wanted = IOUtils.SOFT_MAX_ARRAY_LENGTH - position;
                } else {
                    resize(newSize);
                }
            }
            b.get(data, position, wanted);
            position += wanted;
            if (size < position) {
                size = position;
            }
            return wanted;
        } finally {
            lock.unlock();
        }
    }
}
