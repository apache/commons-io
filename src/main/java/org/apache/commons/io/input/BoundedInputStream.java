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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

//@formatter:off
/**
 * Reads bytes up to a maximum count and stops once reached.
 * <p>
 * To build an instance, see {@link AbstractBuilder}.
 * </p>
 * <p>
 * By default, a {@link BoundedInputStream} is <em>unbound</em>; so make sure to call {@link AbstractBuilder#setMaxCount(long)}.
 * </p>
 * <p>
 * You can find out how many bytes this stream has seen so far by calling {@link BoundedInputStream#getCount()}. This value reflects bytes read and skipped.
 * </p>
 * <h2>Using a ServletInputStream</h2>
 * <p>
 * A {@code ServletInputStream} can block if you try to read content that isn't there
 * because it doesn't know whether the content hasn't arrived yet or whether the content has finished. Initialize an {@link BoundedInputStream} with the
 * {@code Content-Length} sent in the {@code ServletInputStream}'s header, this stop it from blocking, providing it's been sent with a correct content
 * length in the first place.
 * </p>
 * <h2>Using NIO</h2>
 * <pre>{@code
 * BoundedInputStream s = BoundedInputStream.builder()
 *   .setPath(Paths.get("MyFile.xml"))
 *   .setMaxCount(1024)
 *   .setPropagateClose(false)
 *   .get();
 * }
 * </pre>
 * <h2>Using IO</h2>
 * <pre>{@code
 * BoundedInputStream s = BoundedInputStream.builder()
 *   .setFile(new File("MyFile.xml"))
 *   .setMaxCount(1024)
 *   .setPropagateClose(false)
 *   .get();
 * }
 * </pre>
 * <h2>Counting Bytes</h2>
 * <p>You can set the running count when building, which is most useful when starting from another stream:
 * <pre>{@code
 * InputStream in = ...;
 * BoundedInputStream s = BoundedInputStream.builder()
 *   .setInputStream(in)
 *   .setCount(12)
 *   .setMaxCount(1024)
 *   .setPropagateClose(false)
 *   .get();
 * }
 * </pre>
 * @see Builder
 * @since 2.0
 */
//@formatter:on
public class BoundedInputStream extends ProxyInputStream {

    /**
     * For subclassing builders from {@link BoundedInputStream} subclassses.
     *
     * @param <T> The subclass.
     */
    static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractStreamBuilder<BoundedInputStream, T> {

        /** The current count of bytes counted. */
        private long count;

        /** The max count of bytes to read. */
        private long maxCount = EOF;

        /** Flag if {@link #close()} should be propagated, {@code true} by default. */
        private boolean propagateClose = true;

        long getCount() {
            return count;
        }

        long getMaxCount() {
            return maxCount;
        }

        boolean isPropagateClose() {
            return propagateClose;
        }

        /**
         * Sets the current number of bytes counted.
         * <p>
         * Useful when building from another stream to carry forward a read count.
         * </p>
         * <p>
         * Default is {@code 0}, negative means 0.
         * </p>
         *
         * @param count The current number of bytes counted.
         * @return {@code this} instance.
         */
        public T setCount(final long count) {
            this.count = Math.max(0, count);
            return asThis();
        }

        /**
         * Sets the maximum number of bytes to return.
         * <p>
         * Default is {@value IOUtils#EOF}, negative means unbound.
         * </p>
         *
         * @param maxCount The maximum number of bytes to return.
         * @return {@code this} instance.
         */
        public T setMaxCount(final long maxCount) {
            this.maxCount = Math.max(EOF, maxCount);
            return asThis();
        }

        /**
         * Sets whether the {@link #close()} method should propagate to the underling {@link InputStream}.
         * <p>
         * Default is {@code true}.
         * </p>
         *
         * @param propagateClose {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if
         *                       it does not.
         * @return {@code this} instance.
         */
        public T setPropagateClose(final boolean propagateClose) {
            this.propagateClose = propagateClose;
            return asThis();
        }

    }

    //@formatter:off
    /**
     * Builds a new {@link BoundedInputStream}.
     * <p>
     * By default, a {@link BoundedInputStream} is <em>unbound</em>; so make sure to call {@link AbstractBuilder#setMaxCount(long)}.
     * </p>
     * <p>
     * You can find out how many bytes this stream has seen so far by calling {@link BoundedInputStream#getCount()}. This value reflects bytes read and skipped.
     * </p>
     * <h2>Using a ServletInputStream</h2>
     * <p>
     * A {@code ServletInputStream} can block if you try to read content that isn't there
     * because it doesn't know whether the content hasn't arrived yet or whether the content has finished. Initialize an {@link BoundedInputStream} with the
     * {@code Content-Length} sent in the {@code ServletInputStream}'s header, this stop it from blocking, providing it's been sent with a correct content
     * length in the first place.
     * </p>
     * <h2>Using NIO</h2>
     * <pre>{@code
     * BoundedInputStream s = BoundedInputStream.builder()
     *   .setPath(Paths.get("MyFile.xml"))
     *   .setMaxCount(1024)
     *   .setPropagateClose(false)
     *   .get();
     * }
     * </pre>
     * <h2>Using IO</h2>
     * <pre>{@code
     * BoundedInputStream s = BoundedInputStream.builder()
     *   .setFile(new File("MyFile.xml"))
     *   .setMaxCount(1024)
     *   .setPropagateClose(false)
     *   .get();
     * }
     * </pre>
     * <h2>Counting Bytes</h2>
     * <p>You can set the running count when building, which is most useful when starting from another stream:
     * <pre>{@code
     * InputStream in = ...;
     * BoundedInputStream s = BoundedInputStream.builder()
     *   .setInputStream(in)
     *   .setCount(12)
     *   .setMaxCount(1024)
     *   .setPropagateClose(false)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     * @since 2.16.0
     */
    //@formatter:on
    public static class Builder extends AbstractBuilder<Builder> {

        /**
         * Builds a new {@link BoundedInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>maxCount</li>
         * <li>propagateClose</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         */
        @SuppressWarnings("resource")
        @Override
        public BoundedInputStream get() throws IOException {
            return new BoundedInputStream(getInputStream(), getCount(), getMaxCount(), isPropagateClose());
        }

    }

    /**
     * Constructs a new {@link AbstractBuilder}.
     *
     * @return a new {@link AbstractBuilder}.
     * @since 2.16.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The current count of bytes counted. */
    private long count;

    /** The current mark. */
    private long mark;

    /** The max count of bytes to read. */
    private final long maxCount;

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
     * @deprecated Use {@link AbstractBuilder#get()}.
     */
    @Deprecated
    public BoundedInputStream(final InputStream in) {
        this(in, EOF);
    }

    /**
     * Constructs a new {@link BoundedInputStream} that wraps the given input stream and limits it to a certain size.
     *
     * @param inputStream The wrapped input stream.
     * @param maxCount    The maximum number of bytes to return.
     * @deprecated Use {@link AbstractBuilder#get()}.
     */
    @Deprecated
    public BoundedInputStream(final InputStream inputStream, final long maxCount) {
        // Some badly designed methods - e.g. the Servlet API - overload length
        // such that "-1" means stream finished
        this(inputStream, 0, maxCount, true);
    }

    /**
     * Constructs a new {@link BoundedInputStream} that wraps the given input stream and limits it to a certain size.
     *
     * @param inputStream    The wrapped input stream.
     * @param count          The current number of bytes read.
     * @param maxCount       The maximum number of bytes to return.
     * @param propagateClose {@code true} if calling {@link #close()} propagates to the {@code close()} method of the underlying stream or {@code false} if it
     *                       does not.
     */
    BoundedInputStream(final InputStream inputStream, final long count, final long maxCount, final boolean propagateClose) {
        // Some badly designed methods - e.g. the Servlet API - overload length
        // such that "-1" means stream finished
        // Can't throw because we start from an InputStream.
        super(inputStream);
        this.count = count;
        this.maxCount = maxCount;
        this.propagateClose = propagateClose;
    }

    /**
     * Adds the number of read bytes to the count.
     *
     * @param n number of bytes read, or -1 if no more bytes are available
     * @throws IOException Not thrown here but subclasses may throw.
     * @since 2.0
     */
    @Override
    protected synchronized void afterRead(final int n) throws IOException {
        if (n != EOF) {
            count += n;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int available() throws IOException {
        if (isMaxCount()) {
            onMaxLength(maxCount, getCount());
            return 0;
        }
        return in.available();
    }

    /**
     * Invokes the delegate's {@link InputStream#close()} method if {@link #isPropagateClose()} is {@code true}.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        if (propagateClose) {
            super.close();
        }
    }

    /**
     * Gets the count of bytes read.
     *
     * @return The count of bytes read.
     * @since 2.12.0
     */
    public synchronized long getCount() {
        return count;
    }

    /**
     * Gets the max count of bytes to read.
     *
     * @return The max count of bytes to read.
     * @since 2.16.0
     */
    public long getMaxCount() {
        return maxCount;
    }

    /**
     * Gets the max count of bytes to read.
     *
     * @return The max count of bytes to read.
     * @since 2.12.0
     * @deprecated Use {@link #getMaxCount()}.
     */
    @Deprecated
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
        return Math.max(0, getMaxCount() - getCount());
    }

    private boolean isMaxCount() {
        return maxCount >= 0 && getCount() >= maxCount;
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
     * Invokes the delegate's {@link InputStream#mark(int)} method.
     *
     * @param readLimit read ahead limit
     */
    @Override
    public synchronized void mark(final int readLimit) {
        in.mark(readLimit);
        mark = count;
    }

    /**
     * Invokes the delegate's {@link InputStream#markSupported()} method.
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
     * Invokes the delegate's {@link InputStream#read()} method if the current position is less than the limit.
     *
     * @return the byte read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        if (isMaxCount()) {
            onMaxLength(maxCount, getCount());
            return EOF;
        }
        return super.read();
    }

    /**
     * Invokes the delegate's {@link InputStream#read(byte[])} method.
     *
     * @param b the buffer to read the bytes into
     * @return the number of bytes read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Invokes the delegate's {@link InputStream#read(byte[], int, int)} method.
     *
     * @param b   the buffer to read the bytes into
     * @param off The start offset
     * @param len The number of bytes to read
     * @return the number of bytes read or -1 if the end of stream or the limit has been reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (isMaxCount()) {
            onMaxLength(maxCount, getCount());
            return EOF;
        }
        return super.read(b, off, (int) toReadLen(len));
    }

    /**
     * Invokes the delegate's {@link InputStream#reset()} method.
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
     * @deprecated Use {@link AbstractBuilder#setPropagateClose(boolean)}.
     */
    @Deprecated
    public void setPropagateClose(final boolean propagateClose) {
        this.propagateClose = propagateClose;
    }

    /**
     * Invokes the delegate's {@link InputStream#skip(long)} method.
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized long skip(final long n) throws IOException {
        final long skip = super.skip(toReadLen(n));
        count += skip;
        return skip;
    }

    private long toReadLen(final long len) {
        return maxCount >= 0 ? Math.min(len, maxCount - getCount()) : len;
    }

    /**
     * Invokes the delegate's {@link InputStream#toString()} method.
     *
     * @return the delegate's {@link InputStream#toString()}
     */
    @Override
    public String toString() {
        return in.toString();
    }
}
