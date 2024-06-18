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
package org.apache.commons.io.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * This is an alternative to {@link ByteArrayInputStream} which removes the synchronization overhead for non-concurrent access; as such this class is
 * not thread-safe.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @see ByteArrayInputStream
 * @since 2.7
 */
//@NotThreadSafe
public class UnsynchronizedByteArrayInputStream extends InputStream {

    // @formatter:off
    /**
     * Builds a new {@link UnsynchronizedByteArrayInputStream}.
     *
     * <p>
     * Using a Byte Array:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayInputStream s = UnsynchronizedByteArrayInputStream.builder()
     *   .setByteArray(byteArray)
     *   .setOffset(0)
     *   .setLength(byteArray.length)
     *   .get();
     * }
     * </pre>
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayInputStream s = UnsynchronizedByteArrayInputStream.builder()
     *   .setFile(file)
     *   .setOffset(0)
     *   .setLength(byteArray.length)
     *   .get();
     * }
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayInputStream s = UnsynchronizedByteArrayInputStream.builder()
     *   .setPath(path)
     *   .setOffset(0)
     *   .setLength(byteArray.length)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UnsynchronizedByteArrayInputStream, Builder> {

        private int offset;
        private int length;

        /**
         * Builds a new {@link UnsynchronizedByteArrayInputStream}.
         * <p>
         * You must set input that supports {@code byte[]} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@code byte[]}</li>
         * <li>offset</li>
         * <li>length</li>
         * </ul>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a byte[].
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @see AbstractOrigin#getByteArray()
         */
        @Override
        public UnsynchronizedByteArrayInputStream get() throws IOException {
            return new UnsynchronizedByteArrayInputStream(checkOrigin().getByteArray(), offset, length);
        }

        @Override
        public Builder setByteArray(final byte[] origin) {
            length = Objects.requireNonNull(origin, "origin").length;
            return super.setByteArray(origin);
        }

        /**
         * Sets the length.
         *
         * @param length Must be greater or equal to 0.
         * @return {@code this} instance.
         */
        public Builder setLength(final int length) {
            if (length < 0) {
                throw new IllegalArgumentException("length cannot be negative");
            }
            this.length = length;
            return this;
        }

        /**
         * Sets the offset.
         *
         * @param offset Must be greater or equal to 0.
         * @return {@code this} instance.
         */
        public Builder setOffset(final int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset cannot be negative");
            }
            this.offset = offset;
            return this;
        }

    }

    /**
     * The end of stream marker.
     */
    public static final int END_OF_STREAM = -1;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private static int minPosLen(final byte[] data, final int defaultValue) {
        requireNonNegative(defaultValue, "defaultValue");
        return Math.min(defaultValue, data.length > 0 ? data.length : defaultValue);
    }

    private static int requireNonNegative(final int value, final String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
        return value;
    }

    /**
     * The underlying data buffer.
     */
    private final byte[] data;

    /**
     * End Of Data.
     *
     * Similar to data.length, i.e. the last readable offset + 1.
     */
    private final int eod;

    /**
     * Current offset in the data buffer.
     */
    private int offset;

    /**
     * The current mark (if any).
     */
    private int markedOffset;

    /**
     * Constructs a new byte array input stream.
     *
     * @param data the buffer
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public UnsynchronizedByteArrayInputStream(final byte[] data) {
        this(data, data.length, 0, 0);
    }

    /**
     * Constructs a new byte array input stream.
     *
     * @param data   the buffer
     * @param offset the offset into the buffer
     *
     * @throws IllegalArgumentException if the offset is less than zero
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public UnsynchronizedByteArrayInputStream(final byte[] data, final int offset) {
        this(data, data.length, Math.min(requireNonNegative(offset, "offset"), minPosLen(data, offset)), minPosLen(data, offset));
    }

    /**
     * Constructs a new byte array input stream.
     *
     * @param data   the buffer
     * @param offset the offset into the buffer
     * @param length the length of the buffer
     *
     * @throws IllegalArgumentException if the offset or length less than zero
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public UnsynchronizedByteArrayInputStream(final byte[] data, final int offset, final int length) {
        requireNonNegative(offset, "offset");
        requireNonNegative(length, "length");
        this.data = Objects.requireNonNull(data, "data");
        this.eod = Math.min(minPosLen(data, offset) + length, data.length);
        this.offset = minPosLen(data, offset);
        this.markedOffset = minPosLen(data, offset);
    }

    private UnsynchronizedByteArrayInputStream(final byte[] data, final int eod, final int offset, final int markedOffset) {
        this.data = Objects.requireNonNull(data, "data");
        this.eod = eod;
        this.offset = offset;
        this.markedOffset = markedOffset;
    }

    @Override
    public int available() {
        return offset < eod ? eod - offset : 0;
    }

    @SuppressWarnings("sync-override")
    @Override
    public void mark(final int readLimit) {
        this.markedOffset = this.offset;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() {
        return offset < eod ? data[offset++] & 0xff : END_OF_STREAM;
    }

    @Override
    public int read(final byte[] dest) {
        Objects.requireNonNull(dest, "dest");
        return read(dest, 0, dest.length);
    }

    @Override
    public int read(final byte[] dest, final int off, final int len) {
        Objects.requireNonNull(dest, "dest");
        if (off < 0 || len < 0 || off + len > dest.length) {
            throw new IndexOutOfBoundsException();
        }

        if (offset >= eod) {
            return END_OF_STREAM;
        }

        int actualLen = eod - offset;
        if (len < actualLen) {
            actualLen = len;
        }
        if (actualLen <= 0) {
            return 0;
        }
        System.arraycopy(data, offset, dest, off, actualLen);
        offset += actualLen;
        return actualLen;
    }

    @SuppressWarnings("sync-override")
    @Override
    public void reset() {
        this.offset = this.markedOffset;
    }

    @Override
    public long skip(final long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Skipping backward is not supported");
        }

        long actualSkip = eod - offset;
        if (n < actualSkip) {
            actualSkip = n;
        }

        offset = Math.addExact(offset, Math.toIntExact(n));
        return actualSkip;
    }
}
