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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Streams data from a {@link RandomAccessFile} starting at its current position.
 * <p>
 * To build an instance, see {@link Builder}.
 * </p>
 * @since 2.8.0
 */
public class RandomAccessFileInputStream extends InputStream {

    /**
     * Builds a new {@link RandomAccessFileInputStream} instance.
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
     * @since 2.12.0
     */
    public static class Builder extends AbstractStreamBuilder<RandomAccessFileInputStream, Builder> {

        private RandomAccessFile randomAccessFile;
        private boolean closeOnClose;

        /**
         * Constructs a new instance.
         * <p>
         * This builder use the aspects RandomAccessFile or File, and closeOnClose. Only set one of RandomAccessFile or an origin that can be converted to a
         * File.
         * </p>
         * <p>
         * If RandomAccessFile is not set, then you must provide an origin that can be converted to a File by this builder, otherwise, this call will throw an
         * {@link UnsupportedOperationException}.
         * </p>
         *
         * @return a new instance.
         * @throws IllegalStateException if both RandomAccessFile and origin are set.
         * @throws UnsupportedOperationException if the origin cannot provide a File.
         * @see AbstractOrigin#getFile()
         */
        @SuppressWarnings("resource") // Caller closes depending on settings
        @Override
        public RandomAccessFileInputStream get() throws IOException {
            if (randomAccessFile == null && getOrigin() == null) {
                throw new UnsupportedOperationException("Neither RandomAccessFile nor origin is set.");
            }
            if (randomAccessFile != null && getOrigin() != null) {
                throw new IllegalStateException(String.format("Only set one of RandomAccessFile (%s) or origin (%s)", randomAccessFile, getOrigin()));
            }

            if (randomAccessFile != null) {
                return new RandomAccessFileInputStream(randomAccessFile, closeOnClose);
            }

            return new RandomAccessFileInputStream(RandomAccessFileMode.READ_ONLY.create(getOrigin().getFile()), closeOnClose);
        }

        /**
         * Sets whether to close the underlying file when this stream is closed.
         *
         * @param closeOnClose Whether to close the underlying file when this stream is closed.
         * @return this
         */
        public Builder setCloseOnClose(final boolean closeOnClose) {
            this.closeOnClose = closeOnClose;
            return this;
        }

        /**
         * Sets the RandomAccessFile to stream.
         *
         * @param randomAccessFile the RandomAccessFile to stream.
         * @return this
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

    private final boolean closeOnClose;
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
     * @param closeOnClose Whether to close the underlying file when this stream is closed.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public RandomAccessFileInputStream(final RandomAccessFile file, final boolean closeOnClose) {
        this.randomAccessFile = Objects.requireNonNull(file, "file");
        this.closeOnClose = closeOnClose;
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
        return randomAccessFile.length() - randomAccessFile.getFilePointer();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (closeOnClose) {
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
        return closeOnClose;
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
