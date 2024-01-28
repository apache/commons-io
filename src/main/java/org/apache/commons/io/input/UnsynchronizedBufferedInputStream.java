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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * An unsynchronized version of {@link BufferedInputStream}, not thread-safe.
 * <p>
 * Wraps an existing {@link InputStream} and <em>buffers</em> the input. Expensive interaction with the underlying input stream is minimized, since most
 * (smaller) requests can be satisfied by accessing the buffer alone. The drawback is that some extra space is required to hold the buffer and that copying
 * takes place when filling that buffer, but this is usually outweighed by the performance benefits.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * A typical application pattern for the class looks like this:
 * </p>
 *
 * <pre>
 * UnsynchronizedBufferedInputStream s = new UnsynchronizedBufferedInputStream.Builder().
 *   .setInputStream(new FileInputStream(&quot;file.java&quot;))
 *   .setBufferSize(8192)
 *   .get();
 * </pre>
 * <p>
 * Provenance: Apache Harmony and modified.
 * </p>
 *
 * @see Builder
 * @see BufferedInputStream
 * @since 2.12.0
 */
//@NotThreadSafe
public final class UnsynchronizedBufferedInputStream extends UnsynchronizedFilterInputStream {

    // @formatter:off
    /**
     * Builds a new {@link UnsynchronizedBufferedInputStream}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UnsynchronizedBufferedInputStream s = UnsynchronizedBufferedInputStream.builder()
     *   .setFile(file)
     *   .setBufferSize(8192)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UnsynchronizedBufferedInputStream s = UnsynchronizedBufferedInputStream.builder()
     *   .setPath(path)
     *   .setBufferSize(8192)
     *   .get();}
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UnsynchronizedBufferedInputStream, Builder> {

        /**
         * Builds a new {@link UnsynchronizedBufferedInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link #getBufferSize()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         * @see #getBufferSize()
         */
        @SuppressWarnings("resource") // Caller closes.
        @Override
        public UnsynchronizedBufferedInputStream get() throws IOException {
            return new UnsynchronizedBufferedInputStream(getInputStream(), getBufferSize());
        }

    }

    /**
     * The buffer containing the current bytes read from the target InputStream.
     */
    protected volatile byte[] buffer;

    /**
     * The total number of bytes inside the byte array {@code buffer}.
     */
    protected int count;

    /**
     * The current limit, which when passed, invalidates the current mark.
     */
    protected int markLimit;

    /**
     * The currently marked position. -1 indicates no mark has been set or the mark has been invalidated.
     */
    protected int markPos = IOUtils.EOF;

    /**
     * The current position within the byte array {@code buffer}.
     */
    protected int pos;

    /**
     * Constructs a new {@code BufferedInputStream} on the {@link InputStream} {@code in}. The buffer size is specified by the parameter {@code size} and all
     * reads are now filtered through this stream.
     *
     * @param in   the input stream the buffer reads from.
     * @param size the size of buffer to allocate.
     * @throws IllegalArgumentException if {@code size < 0}.
     */
    private UnsynchronizedBufferedInputStream(final InputStream in, final int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be > 0");
        }
        buffer = new byte[size];
    }

    /**
     * Returns the number of bytes that are available before this stream will block. This method returns the number of bytes available in the buffer plus those
     * available in the source stream.
     *
     * @return the number of bytes available before blocking.
     * @throws IOException if this stream is closed.
     */
    @Override
    public int available() throws IOException {
        final InputStream localIn = inputStream; // 'in' could be invalidated by close()
        if (buffer == null || localIn == null) {
            throw new IOException("Stream is closed");
        }
        return count - pos + localIn.available();
    }

    /**
     * Closes this stream. The source stream is closed and any resources associated with it are released.
     *
     * @throws IOException if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        buffer = null;
        final InputStream localIn = inputStream;
        inputStream = null;
        if (localIn != null) {
            localIn.close();
        }
    }

    private int fillBuffer(final InputStream localIn, byte[] localBuf) throws IOException {
        if (markPos == IOUtils.EOF || pos - markPos >= markLimit) {
            /* Mark position not set or exceeded readLimit */
            final int result = localIn.read(localBuf);
            if (result > 0) {
                markPos = IOUtils.EOF;
                pos = 0;
                count = result;
            }
            return result;
        }
        if (markPos == 0 && markLimit > localBuf.length) {
            /* Increase buffer size to accommodate the readLimit */
            int newLength = localBuf.length * 2;
            if (newLength > markLimit) {
                newLength = markLimit;
            }
            final byte[] newbuf = new byte[newLength];
            System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length);
            // Reassign buffer, which will invalidate any local references
            // FIXME: what if buffer was null?
            localBuf = buffer = newbuf;
        } else if (markPos > 0) {
            System.arraycopy(localBuf, markPos, localBuf, 0, localBuf.length - markPos);
        }
        // Set the new position and mark position
        pos -= markPos;
        count = markPos = 0;
        final int bytesread = localIn.read(localBuf, pos, localBuf.length - pos);
        count = bytesread <= 0 ? pos : pos + bytesread;
        return bytesread;
    }

    byte[] getBuffer() {
        return buffer;
    }

    /**
     * Sets a mark position in this stream. The parameter {@code readLimit} indicates how many bytes can be read before a mark is invalidated. Calling
     * {@code reset()} will reposition the stream back to the marked position if {@code readLimit} has not been surpassed. The underlying buffer may be
     * increased in size to allow {@code readLimit} number of bytes to be supported.
     *
     * @param readLimit the number of bytes that can be read before the mark is invalidated.
     * @see #reset()
     */
    @Override
    public void mark(final int readLimit) {
        markLimit = readLimit;
        markPos = pos;
    }

    /**
     * Indicates whether {@code BufferedInputStream} supports the {@code mark()} and {@code reset()} methods.
     *
     * @return {@code true} for BufferedInputStreams.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the range from 0 to 255. Returns -1 if the end of the source string has been
     * reached. If the internal buffer does not contain any available bytes then it is filled from the source stream and the first byte is returned.
     *
     * @return the byte read or -1 if the end of the source stream has been reached.
     * @throws IOException if this stream is closed or another IOException occurs.
     */
    @Override
    public int read() throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        byte[] localBuf = buffer;
        final InputStream localIn = inputStream;
        if (localBuf == null || localIn == null) {
            throw new IOException("Stream is closed");
        }

        /* Are there buffered bytes available? */
        if (pos >= count && fillBuffer(localIn, localBuf) == IOUtils.EOF) {
            return IOUtils.EOF; /* no, fill buffer */
        }
        // localBuf may have been invalidated by fillbuf
        if (localBuf != buffer) {
            localBuf = buffer;
            if (localBuf == null) {
                throw new IOException("Stream is closed");
            }
        }

        /* Did filling the buffer fail with -1 (EOF)? */
        if (count - pos > 0) {
            return localBuf[pos++] & 0xFF;
        }
        return IOUtils.EOF;
    }

    /**
     * Reads at most {@code length} bytes from this stream and stores them in byte array {@code buffer} starting at offset {@code offset}. Returns the number of
     * bytes actually read or -1 if no bytes were read and the end of the stream was encountered. If all the buffered bytes have been used, a mark has not been
     * set and the requested number of bytes is larger than the receiver's buffer size, this implementation bypasses the buffer and simply places the results
     * directly into {@code buffer}.
     *
     * @param dest the byte array in which to store the bytes read.
     * @param offset the initial position in {@code buffer} to store the bytes read from this stream.
     * @param length the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes actually read or -1 if end of stream.
     * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code length < 0}, or if {@code offset + length} is greater than the size of {@code buffer}.
     * @throws IOException               if the stream is already closed or another IOException occurs.
     */
    @Override
    public int read(final byte[] dest, int offset, final int length) throws IOException {
        // Use local ref since buf may be invalidated by an unsynchronized
        // close()
        byte[] localBuf = buffer;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
        // avoid int overflow
        if (offset > dest.length - length || offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        final InputStream localIn = inputStream;
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }

        int required;
        if (pos < count) {
            /* There are bytes available in the buffer. */
            final int copylength = count - pos >= length ? length : count - pos;
            System.arraycopy(localBuf, pos, dest, offset, copylength);
            pos += copylength;
            if (copylength == length || localIn.available() == 0) {
                return copylength;
            }
            offset += copylength;
            required = length - copylength;
        } else {
            required = length;
        }

        while (true) {
            final int read;
            /*
             * If we're not marked and the required size is greater than the buffer, simply read the bytes directly bypassing the buffer.
             */
            if (markPos == IOUtils.EOF && required >= localBuf.length) {
                read = localIn.read(dest, offset, required);
                if (read == IOUtils.EOF) {
                    return required == length ? IOUtils.EOF : length - required;
                }
            } else {
                if (fillBuffer(localIn, localBuf) == IOUtils.EOF) {
                    return required == length ? IOUtils.EOF : length - required;
                }
                // localBuf may have been invalidated by fillBuffer()
                if (localBuf != buffer) {
                    localBuf = buffer;
                    if (localBuf == null) {
                        throw new IOException("Stream is closed");
                    }
                }

                read = count - pos >= required ? required : count - pos;
                System.arraycopy(localBuf, pos, dest, offset, read);
                pos += read;
            }
            required -= read;
            if (required == 0) {
                return length;
            }
            if (localIn.available() == 0) {
                return length - required;
            }
            offset += read;
        }
    }

    /**
     * Resets this stream to the last marked location.
     *
     * @throws IOException if this stream is closed, no mark has been set or the mark is no longer valid because more than {@code readLimit} bytes have been
     *                     read since setting the mark.
     * @see #mark(int)
     */
    @Override
    public void reset() throws IOException {
        if (buffer == null) {
            throw new IOException("Stream is closed");
        }
        if (IOUtils.EOF == markPos) {
            throw new IOException("Mark has been invalidated");
        }
        pos = markPos;
    }

    /**
     * Skips {@code amount} number of bytes in this stream. Subsequent {@code read()}'s will not return these bytes unless {@code reset()} is used.
     *
     * @param amount the number of bytes to skip. {@code skip} does nothing and returns 0 if {@code amount} is less than zero.
     * @return the number of bytes actually skipped.
     * @throws IOException if this stream is closed or another IOException occurs.
     */
    @Override
    public long skip(final long amount) throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        final byte[] localBuf = buffer;
        final InputStream localIn = inputStream;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
        if (amount < 1) {
            return 0;
        }
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }

        if (count - pos >= amount) {
            // (int count - int pos) here is always an int so amount is also in the int range if the above test is true.
            // We can safely cast to int and avoid static analysis warnings.
            pos += (int) amount;
            return amount;
        }
        int read = count - pos;
        pos = count;

        if (markPos != IOUtils.EOF && amount <= markLimit) {
            if (fillBuffer(localIn, localBuf) == IOUtils.EOF) {
                return read;
            }
            if (count - pos >= amount - read) {
                // (int count - int pos) here is always an int so (amount - read) is also in the int range if the above test is true.
                // We can safely cast to int and avoid static analysis warnings.
                pos += (int) amount - read;
                return amount;
            }
            // Couldn't get all the bytes, skip what we read
            read += count - pos;
            pos = count;
            return read;
        }
        return read + localIn.skip(amount - read);
    }
}
