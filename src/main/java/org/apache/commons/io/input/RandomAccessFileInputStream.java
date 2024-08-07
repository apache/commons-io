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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Streams data from a {@link RandomAccessFile} starting at its current position.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @since 2.8.0
 */
public class RandomAccessFileInputStream extends AbstractInputStream {

    // @formatter:off
    /**
     * Builds a new {@link RandomAccessFileInputStream}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * RandomAccessFileInputStream s = RandomAccessFileInputStream.builder()
     *   .setPath(path)
     *   .setCloseOnClose(true)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<RandomAccessFileInputStream, Builder> {

        private RandomAccessFile randomAccessFile;
        private boolean propagateClose;

        /**
         * Builds a new {@link RandomAccessFileInputStream}.
         * <p>
         * You must set input that supports {@link RandomAccessFile} or {@link File}, otherwise, this method throws an exception. Only set one of
         * RandomAccessFile or an origin that can be converted to a File.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link RandomAccessFile}</li>
         * <li>{@link File}</li>
         * <li>closeOnClose</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws IllegalStateException         if both RandomAccessFile and origin are set.
         * @throws UnsupportedOperationException if the origin cannot be converted to a {@link File}.
         * @see AbstractOrigin#getFile()
         */
        @SuppressWarnings("resource") // Caller closes depending on settings
        @Override
        public RandomAccessFileInputStream get() throws IOException {
            if (randomAccessFile != null) {
                if (getOrigin() != null) {
                    throw new IllegalStateException(String.format("Only set one of RandomAccessFile (%s) or origin (%s)", randomAccessFile, getOrigin()));
                }
                return new RandomAccessFileInputStream(randomAccessFile, propagateClose);
            }
            return new RandomAccessFileInputStream(RandomAccessFileMode.READ_ONLY.create(checkOrigin().getFile()), propagateClose);
        }

        /**
         * Sets whether to close the underlying file when this stream is closed.
         *
         * @param propagateClose Whether to close the underlying file when this stream is closed.
         * @return {@code this} instance.
         */
        public Builder setCloseOnClose(final boolean propagateClose) {
            this.propagateClose = propagateClose;
            return this;
        }

        /**
         * Sets the RandomAccessFile to stream.
         *
         * @param randomAccessFile the RandomAccessFile to stream.
         * @return {@code this} instance.
         */
        public Builder setRandomAccessFile(final RandomAccessFile randomAccessFile) {
            this.randomAccessFile = randomAccessFile;
            return this;
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

    private final boolean propagateClose;
    private final RandomAccessFile randomAccessFile;

    /**
     * Constructs a new instance configured to leave the underlying file open when this stream is closed.
     *
     * @param file The file to stream.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public RandomAccessFileInputStream(final RandomAccessFile file) {
        this(file, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param file         The file to stream.
     * @param propagateClose Whether to close the underlying file when this stream is closed.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public RandomAccessFileInputStream(final RandomAccessFile file, final boolean propagateClose) {
        this.randomAccessFile = Objects.requireNonNull(file, "file");
        this.propagateClose = propagateClose;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream.
     *
     * If there are more than {@link Integer#MAX_VALUE} bytes available, return {@link Integer#MAX_VALUE}.
     *
     * @return An estimate of the number of bytes that can be read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        final long avail = availableLong();
        if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) avail;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from this input stream.
     *
     * @return The number of bytes that can be read.
     * @throws IOException If an I/O error occurs.
     */
    public long availableLong() throws IOException {
        return isClosed() ? 0 : randomAccessFile.length() - randomAccessFile.getFilePointer();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (propagateClose) {
            randomAccessFile.close();
        }
    }

    /**
     * Gets the underlying file.
     *
     * @return the underlying file.
     */
    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    /**
     * Returns whether to close the underlying file when this stream is closed.
     *
     * @return Whether to close the underlying file when this stream is closed.
     */
    public boolean isCloseOnClose() {
        return propagateClose;
    }

    @Override
    public int read() throws IOException {
        return randomAccessFile.read();
    }

    @Override
    public int read(final byte[] bytes) throws IOException {
        return randomAccessFile.read(bytes);
    }

    @Override
    public int read(final byte[] bytes, final int offset, final int length) throws IOException {
        return randomAccessFile.read(bytes, offset, length);
    }

    @Override
    public long skip(final long skipCount) throws IOException {
        if (skipCount <= 0) {
            return 0;
        }
        final long filePointer = randomAccessFile.getFilePointer();
        final long fileLength = randomAccessFile.length();
        if (filePointer >= fileLength) {
            return 0;
        }
        final long targetPos = filePointer + skipCount;
        final long newPos = targetPos > fileLength ? fileLength - 1 : targetPos;
        if (newPos > 0) {
            randomAccessFile.seek(newPos);
        }
        return randomAccessFile.getFilePointer() - filePointer;
    }
}
