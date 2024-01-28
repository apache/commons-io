/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.commons.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * An unsynchronized version of {@link FilterInputStream}, not thread-safe.
 * <p>
 * Wraps an existing {@link InputStream} and performs some transformation on the input data while it is being read. Transformations can be anything from a
 * simple byte-wise filtering input data to an on-the-fly compression or decompression of the underlying stream. Input streams that wrap another input stream
 * and provide some additional functionality on top of it usually inherit from this class.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * Provenance: Apache Harmony and modified.
 * </p>
 *
 * @see Builder
 * @see FilterInputStream
 * @since 2.12.0
 */
//@NotThreadSafe
public class UnsynchronizedFilterInputStream extends InputStream {

    // @formatter:off
    /**
     * Builds a new {@link UnsynchronizedFilterInputStream}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UnsynchronizedFilterInputStream s = UnsynchronizedFilterInputStream.builder()
     *   .setFile(file)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UnsynchronizedFilterInputStream s = UnsynchronizedFilterInputStream.builder()
     *   .setPath(path)
     *   .get();}
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UnsynchronizedFilterInputStream, Builder> {

        /**
         * Builds a new {@link UnsynchronizedFilterInputStream}.
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
        @SuppressWarnings("resource") // Caller closes.
        @Override
        public UnsynchronizedFilterInputStream get() throws IOException {
            return new UnsynchronizedFilterInputStream(getInputStream());
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
     * The source input stream that is filtered.
     */
    protected volatile InputStream inputStream;

    /**
     * Constructs a new {@code FilterInputStream} with the specified input stream as source.
     *
     * @param inputStream the non-null InputStream to filter reads on.
     */
    UnsynchronizedFilterInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Returns the number of bytes that are available before this stream will block.
     *
     * @return the number of bytes available before blocking.
     * @throws IOException if an error occurs in this stream.
     */
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * Closes this stream. This implementation closes the filtered stream.
     *
     * @throws IOException if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    /**
     * Sets a mark position in this stream. The parameter {@code readLimit} indicates how many bytes can be read before the mark is invalidated. Sending
     * {@code reset()} will reposition this stream back to the marked position, provided that {@code readLimit} has not been surpassed.
     * <p>
     * This implementation sets a mark in the filtered stream.
     *
     * @param readLimit the number of bytes that can be read from this stream before the mark is invalidated.
     * @see #markSupported()
     * @see #reset()
     */
    @SuppressWarnings("sync-override") // by design.
    @Override
    public void mark(final int readLimit) {
        inputStream.mark(readLimit);
    }

    /**
     * Indicates whether this stream supports {@code mark()} and {@code reset()}. This implementation returns whether or not the filtered stream supports
     * marking.
     *
     * @return {@code true} if {@code mark()} and {@code reset()} are supported, {@code false} otherwise.
     * @see #mark(int)
     * @see #reset()
     * @see #skip(long)
     */
    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    /**
     * Reads a single byte from the filtered stream and returns it as an integer in the range from 0 to 255. Returns -1 if the end of this stream has been
     * reached.
     *
     * @return the byte read or -1 if the end of the filtered stream has been reached.
     * @throws IOException if the stream is closed or another IOException occurs.
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    /**
     * Reads bytes from this stream and stores them in the byte array {@code buffer}. Returns the number of bytes actually read or -1 if no bytes were read and
     * the end of this stream was encountered. This implementation reads bytes from the filtered stream.
     *
     * @param buffer the byte array in which to store the read bytes.
     * @return the number of bytes actually read or -1 if the end of the filtered stream has been reached while reading.
     * @throws IOException if this stream is closed or another IOException occurs.
     */
    @Override
    public int read(final byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads at most {@code count} bytes from this stream and stores them in the byte array {@code buffer} starting at {@code offset}. Returns the number of
     * bytes actually read or -1 if no bytes have been read and the end of this stream has been reached. This implementation reads bytes from the filtered
     * stream.
     *
     * @param buffer the byte array in which to store the bytes read.
     * @param offset the initial position in {@code buffer} to store the bytes read from this stream.
     * @param count  the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes actually read or -1 if the end of the filtered stream has been reached while reading.
     * @throws IOException if this stream is closed or another I/O error occurs.
     */
    @Override
    public int read(final byte[] buffer, final int offset, final int count) throws IOException {
        return inputStream.read(buffer, offset, count);
    }

    /**
     * Resets this stream to the last marked location. This implementation resets the target stream.
     *
     * @throws IOException if this stream is already closed, no mark has been set or the mark is no longer valid because more than {@code readLimit} bytes have
     *                     been read since setting the mark.
     * @see #mark(int)
     * @see #markSupported()
     */
    @SuppressWarnings("sync-override") // by design.
    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    /**
     * Skips {@code count} number of bytes in this stream. Subsequent {@code read()}'s will not return these bytes unless {@code reset()} is used. This
     * implementation skips {@code count} number of bytes in the filtered stream.
     *
     * @param count the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException if this stream is closed or another IOException occurs.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public long skip(final long count) throws IOException {
        return inputStream.skip(count);
    }
}
