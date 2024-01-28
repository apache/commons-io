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
package org.apache.commons.io.output;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;

/**
 * Implements a version of {@link AbstractByteArrayOutputStream} <b>without</b> any concurrent thread safety.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @since 2.7
 */
//@NotThreadSafe
public final class UnsynchronizedByteArrayOutputStream extends AbstractByteArrayOutputStream {

    // @formatter:off
    /**
     * Builds a new {@link UnsynchronizedByteArrayOutputStream}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayOutputStream s = UnsynchronizedByteArrayOutputStream.builder()
     *   .setBufferSize(8192)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayOutputStream s = UnsynchronizedByteArrayOutputStream.builder()
     *   .setBufferSize(8192)
     *   .get();}
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UnsynchronizedByteArrayOutputStream, Builder> {

        /**
         * Builds a new {@link UnsynchronizedByteArrayOutputStream}.
         *
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getBufferSize()}</li>
         * </ul>
         *
         * @return a new instance.
         * @see AbstractOrigin#getByteArray()
         */
        @Override
        public UnsynchronizedByteArrayOutputStream get() {
            return new UnsynchronizedByteArrayOutputStream(getBufferSize());
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fetches entire contents of an {@link InputStream} and represent same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toByteArray()}, since it avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     *
     * @param input Stream to be fully buffered.
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream toBufferedInputStream(final InputStream input) throws IOException {
        return toBufferedInputStream(input, DEFAULT_SIZE);
    }

    /**
     * Fetches entire contents of an {@link InputStream} and represent same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toByteArray()}, since it avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     *
     * @param input Stream to be fully buffered.
     * @param size the initial buffer size
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     */
    public static InputStream toBufferedInputStream(final InputStream input, final int size) throws IOException {
        // It does not matter if a ByteArrayOutputStream is not closed as close() is a no-op
        try (UnsynchronizedByteArrayOutputStream output = builder().setBufferSize(size).get()) {
            output.write(input);
            return output.toInputStream();
        }
    }

    /**
     * Constructs a new byte array output stream. The buffer capacity is initially
     *
     * {@value AbstractByteArrayOutputStream#DEFAULT_SIZE} bytes, though its size increases if necessary.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public UnsynchronizedByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new byte array output stream, with a buffer capacity of the specified size, in bytes.
     *
     * @param size the initial size
     * @throws IllegalArgumentException if size is negative
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}. Will be private in 3.0.0.
     */
    @Deprecated
    public UnsynchronizedByteArrayOutputStream(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        needNewBuffer(size);
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    @Override
    public void reset() {
        resetImpl();
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public byte[] toByteArray() {
        return toByteArrayImpl();
    }

    @Override
    public InputStream toInputStream() {
        // @formatter:off
        return toInputStream((buffer, offset, length) -> Uncheck
                .get(() -> UnsynchronizedByteArrayInputStream.builder()
                        .setByteArray(buffer)
                        .setOffset(offset)
                        .setLength(length)
                        .get()));
        // @formatter:on
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException(String.format("offset=%,d, length=%,d", off, len));
        }
        if (len == 0) {
            return;
        }
        writeImpl(b, off, len);
    }

    @Override
    public int write(final InputStream in) throws IOException {
        return writeImpl(in);
    }

    @Override
    public void write(final int b) {
        writeImpl(b);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        writeToImpl(out);
    }
}
