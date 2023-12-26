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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Reads bytes up to a maximum length, if its count goes above that, it stops.
 * <p>
 * This is useful to wrap ServletInputStreams. The ServletInputStream will block if you try to read content from it that isn't there, because it doesn't know
 * whether the content hasn't arrived yet or whether the content has finished. So, one of these, initialized with the Content-length sent in the
 * ServletInputStream's header, will stop it blocking, providing it's been sent with a correct content length.
 * </p>
 *
 * @since 2.0
 */
public class BoundedInputStream extends FilterInputStream {
    
    // TODO For 3.0, extend CountintInputStream.

    /**
     * Builds a new {@link BoundedInputStream} instance.
     *
     * <h2>Using NIO</h2>
     *
     * <pre>{@code
     * BoundedInputStream s = BoundedInputStream.builder().setPath(Paths.get("MyFile.xml")).setMaxLength(1024).setPropagateClose(false).get();
     * }
     * </pre>
     *
     * <h2>Using IO</h2>
     *
     * <pre>{@code
     * BoundedInputStream s = BoundedInputStream.builder().setFile(new File("MyFile.xml")).setMaxLength(1024).setPropagateClose(false).get();
     * }
     * </pre>
     *
     * @since 2.16.0
     */
    public static class Builder extends AbstractStreamBuilder<BoundedInputStream, Builder> {

        /** The max count of bytes to read. */
        private long maxLength = EOF;

        /** Flag if close should be propagated. */
        private boolean propagateClose = true;

        @SuppressWarnings("resource")
        @Override
        public BoundedInputStream get() throws IOException {
            return new BoundedInputStream(getInputStream(), maxLength, propagateClose);
        }

        /**
         * Sets the maximum number of bytes to return.
         * <p>
         * Default is {@value IOUtils#EOF}.
         * </p>
         *
         * @param maxLength The maximum number of bytes to return.
         * @return this.
         */
        public Builder setMaxLength(final long maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        /**
         * Sets whether the {@link #close()} method should propagate to the underling {@link InputStream}.
         * <p>
         * Default is true.
         * </p>
         *
         * @param propagateClose {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if
         *                       it does not.
         * @return this.
         */
        public Builder setPropagateClose(final boolean propagateClose) {
            this.propagateClose = propagateClose;
            return this;
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.16.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The max count of bytes to read. */
    private final long maxCount;

    /** The count of bytes read. */
    private long count;

    /** The marked position. */
    private long mark = EOF;

    /**
     * Flag if close should be propagated.
     *
     * TODO Make final in 3.0.
     */
    private boolean propagateClose = true;

    /**
     * Constructs a new {@link BoundedInputStream} that wraps the given input stream and is unlimited.
     *
     * @param in The wrapped input stream.
     * @deprecated Use {@link Builder#get()}.
     */
    @Deprecated
    public BoundedInputStream(final InputStream in) {
        this(in, EOF);
    }

    /**
     * Constructs a new {@link BoundedInputStream} that wraps the given input stream and limits it to a certain size.
     *
     * @param inputStream The wrapped input stream.
     * @param maxLength   The maximum number of bytes to return.
     * @deprecated Use {@link Builder#get()}.
     */
    @Deprecated
    public BoundedInputStream(final InputStream inputStream, final long maxLength) {
        // Some badly designed methods - e.g. the servlet API - overload length
        // such that "-1" means stream finished
        super(inputStream);
        this.maxCount = maxLength;
    }

    /**
     * Constructs a new {@link BoundedInputStream} that wraps the given input stream and limits it to a certain size.
     *
     * @param inputStream    The wrapped input stream.
     * @param maxLength      The maximum number of bytes to return.
     * @param propagateClose {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if it
     *                       does not.
     */
    private BoundedInputStream(final InputStream inputStream, final long maxLength, final boolean propagateClose) {
        // Some badly designed methods - e.g. the servlet API - overload length
        // such that "-1" means stream finished
        super(inputStream);
        this.maxCount = maxLength;
        this.propagateClose = propagateClose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        if (isMaxLength()) {
            onMaxLength(maxCount, count);
            return 0;
        }
        return in.available();
    }

    /**
     * Invokes the delegate's {@code close()} method if {@link #isPropagateClose()} is {@code true}.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (propagateClose) {
            in.close();
        }
    }

    /**
     * Gets the count of bytes read.
     *
     * @return The count of bytes read.
     * @since 2.12.0
     */
    public long getCount() {
        return count;
    }

    /**
     * Gets the max count of bytes to read.
     *
     * @return The max count of bytes to read.
     * @since 2.12.0
     */
    public long getMaxLength() {
        return maxCount;
    }

    /**
     * Gets how many bytes remain to read.
     *
     * @return bytes how many bytes remain to read.
     * @since 2.16.0
     */
    public long getRemaining() {
        return getMaxLength() - getCount();
    }

    private boolean isMaxLength() {
        return maxCount >= 0 && count >= maxCount;
    }

    /**
     * Tests whether the {@link #close()} method should propagate to the underling {@link InputStream}.
     *
     * @return {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if it does not.
     */
    public boolean isPropagateClose() {
        return propagateClose;
    }

    /**
     * Invokes the delegate's {@code mark(int)} method.
     *
     * @param readLimit read ahead limit
     */
    @Override
    public synchronized void mark(final int readLimit) {
        in.mark(readLimit);
        mark = count;
    }

    /**
     * Invokes the delegate's {@code markSupported()} method.
     *
     * @return true if mark is supported, otherwise false
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * A caller has caused a request that would cross the {@code maxLength} boundary.
     *
     * @param maxLength The max count of bytes to read.
     * @param count     The count of bytes read.
     * @throws IOException Subclasses may throw.
     * @since 2.12.0
     */
    @SuppressWarnings("unused")
    protected void onMaxLength(final long maxLength, final long count) throws IOException {
        // for subclasses
    }

    /**
     * Invokes the delegate's {@code read()} method if the current position is less than the limit.
     *
     * @return the byte read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        if (isMaxLength()) {
            onMaxLength(maxCount, count);
            return EOF;
        }
        final int result = in.read();
        if (result != EOF) {
            count++;
        }
        return result;
    }

    /**
     * Invokes the delegate's {@code read(byte[])} method.
     *
     * @param b the buffer to read the bytes into
     * @return the number of bytes read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    /**
     * Invokes the delegate's {@code read(byte[], int, int)} method.
     *
     * @param b   the buffer to read the bytes into
     * @param off The start offset
     * @param len The number of bytes to read
     * @return the number of bytes read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (isMaxLength()) {
            onMaxLength(maxCount, count);
            return EOF;
        }
        final long maxRead = maxCount >= 0 ? Math.min(len, maxCount - count) : len;
        final int bytesRead = in.read(b, off, (int) maxRead);

        if (bytesRead == EOF) {
            return EOF;
        }

        count += bytesRead;
        return bytesRead;
    }

    /**
     * Invokes the delegate's {@code reset()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
        count = mark;
    }

    /**
     * Sets whether the {@link #close()} method should propagate to the underling {@link InputStream}.
     *
     * @param propagateClose {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if it
     *                       does not.
     * @deprecated Use {@link Builder#setPropagateClose(boolean)}.
     */
    @Deprecated
    public void setPropagateClose(final boolean propagateClose) {
        this.propagateClose = propagateClose;
    }

    /**
     * Invokes the delegate's {@code skip(long)} method.
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        final long toSkip = maxCount >= 0 ? Math.min(n, maxCount - count) : n;
        final long skippedBytes = in.skip(toSkip);
        count += skippedBytes;
        return skippedBytes;
    }

    /**
     * Invokes the delegate's {@code toString()} method.
     *
     * @return the delegate's {@code toString()}
     */
    @Override
    public String toString() {
        return in.toString();
    }
}
