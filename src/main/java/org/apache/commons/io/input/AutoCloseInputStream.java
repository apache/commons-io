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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;

/**
 * Proxy stream that closes and discards the underlying stream as soon as the end of input has been reached or when the stream is explicitly closed. Not even a
 * reference to the underlying stream is kept after it has been closed, so any allocated in-memory buffers can be freed even if the client application still
 * keeps a reference to the proxy stream.
 * <p>
 * This class is typically used to release any resources related to an open stream as soon as possible even if the client application (by not explicitly closing
 * the stream when no longer needed) or the underlying stream (by not releasing resources once the last byte has been read) do not do that.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @since 1.4
 * @see Builder
 */
public class AutoCloseInputStream extends ProxyInputStream {

    // @formatter:off
    /**
     * Builds a new {@link AutoCloseInputStream} instance.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * AutoCloseInputStream s = AutoCloseInputStream.builder()
     *   .setPath(path)
     *   .get();}
     * </pre>
     * <pre>{@code
     * AutoCloseInputStream s = AutoCloseInputStream.builder()
     *   .setInputStream(inputStream)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.13.0
     */
    // @formatter:on
    public static class Builder extends AbstractBuilder<AutoCloseInputStream, Builder> {

        /**
         * Builds a new {@link AutoCloseInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         */
        @Override
        public AutoCloseInputStream get() throws IOException {
            return new AutoCloseInputStream(this);
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs an automatically closing proxy for the given input stream.
     *
     * @param in underlying input stream
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @SuppressWarnings("resource") // ClosedInputStream.nonNull() doesn't allocate
    @Deprecated
    public AutoCloseInputStream(final InputStream in) {
        super(ClosedInputStream.ifNull(in));
    }

    private AutoCloseInputStream(final Builder builder) throws IOException {
        super(builder);
    }

    /**
     * Automatically closes the stream if the end of stream was reached.
     *
     * @param n number of bytes read, or -1 if no more bytes are available
     * @throws IOException if the stream could not be closed
     * @since 2.0
     */
    @Override
    protected void afterRead(final int n) throws IOException {
        if (n == EOF) {
            close();
        }
        super.afterRead(n);
    }

    /**
     * Closes the underlying input stream and replaces the reference to it with a {@link ClosedInputStream} instance.
     * <p>
     * This method is automatically called by the read methods when the end of input has been reached.
     * </p>
     * <p>
     * Note that it is safe to call this method any number of times. The original underlying input stream is closed and discarded only once when this method is
     * first called.
     * </p>
     *
     * @throws IOException if the underlying input stream cannot be closed
     */
    @Override
    public void close() throws IOException {
        super.close();
        in = ClosedInputStream.INSTANCE;
    }

    /**
     * Ensures that the stream is closed before it gets garbage-collected. As mentioned in {@link #close()}, this is a no-op if the stream has already been
     * closed.
     *
     * @throws Throwable if an error occurs
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
