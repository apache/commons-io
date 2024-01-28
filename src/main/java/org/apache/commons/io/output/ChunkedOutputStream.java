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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * OutputStream which breaks larger output blocks into chunks. Native code may need to copy the input array; if the write buffer is very large this can cause
 * OOME.
 * <p>
 * To build an instance, see {@link Builder}
 * </p>
 *
 * @see Builder
 * @since 2.5
 */
public class ChunkedOutputStream extends FilterOutputStream {

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
     *   .get();
     * }
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UnsynchronizedByteArrayOutputStream s = UnsynchronizedByteArrayOutputStream.builder()
     *   .setBufferSize(8192)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     * @since 2.13.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<ChunkedOutputStream, Builder> {

        /**
         * Builds a new {@link UnsynchronizedByteArrayOutputStream}.
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link #getBufferSize()} (chunk size)</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link OutputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getOutputStream()
         * @see #getBufferSize()
         */
        @Override
        public ChunkedOutputStream get() throws IOException {
            return new ChunkedOutputStream(getOutputStream(), getBufferSize());
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.13.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The maximum chunk size to us when writing data arrays
     */
    private final int chunkSize;

    /**
     * Constructs a new stream that uses a chunk size of {@link IOUtils#DEFAULT_BUFFER_SIZE}.
     *
     * @param stream the stream to wrap
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ChunkedOutputStream(final OutputStream stream) {
        this(stream, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new stream that uses the specified chunk size.
     *
     * @param stream    the stream to wrap
     * @param chunkSize the chunk size to use; must be a positive number.
     * @throws IllegalArgumentException if the chunk size is &lt;= 0
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ChunkedOutputStream(final OutputStream stream, final int chunkSize) {
        super(stream);
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize <= 0");
        }
        this.chunkSize = chunkSize;
    }

    int getChunkSize() {
        return chunkSize;
    }

    /**
     * Writes the data buffer in chunks to the underlying stream
     *
     * @param data      the data to write
     * @param srcOffset the offset
     * @param length    the length of data to write
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final byte[] data, final int srcOffset, final int length) throws IOException {
        int bytes = length;
        int dstOffset = srcOffset;
        while (bytes > 0) {
            final int chunk = Math.min(bytes, chunkSize);
            out.write(data, dstOffset, chunk);
            bytes -= chunk;
            dstOffset += chunk;
        }
    }

}
