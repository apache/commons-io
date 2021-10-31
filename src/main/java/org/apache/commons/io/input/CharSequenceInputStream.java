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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

/**
 * Implements an {@link InputStream} to read from String, StringBuffer, StringBuilder or CharBuffer.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 * </p>
 * <p>
 * Instances of {@code CharSequenceInputStream} are not thread safe.
 * </p>
 *
 * @since 2.2
 */
public class CharSequenceInputStream extends InputStream {

    private static final int BUFFER_SIZE = 2048;

    // Delegates to ReaderInputStream to avoid having to implement CharsetEncoder logic here as well
    private final ReaderInputStream delegateStream;
    /**
     * Used for buffering after {@link #mark(int)} has been called, and for reading after
     * {@link #reset()} has been called.
     */
    private ByteBuffer markBuffer;
    /** Determines whether currently reading from {@link #markBuffer}. */
    private boolean isReadingFromBuffer;
    /** Whether mark is set and in case {@link #isReadingFromBuffer} {@code = false}, should be writing to buffer. */
    private boolean hasMark;

    /**
     * Constructs a new instance with a buffer size of 2048.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     */
    public CharSequenceInputStream(final CharSequence cs, final Charset charset) {
        this(cs, charset, BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     */
    public CharSequenceInputStream(final CharSequence cs, final Charset charset, final int bufferSize) {
        Objects.requireNonNull(cs);
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be >= 1");
        }
        // TODO: bufferSize is unused

        // @formatter:off
        CharsetEncoder charsetEncoder = charset.newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // @formatter:on
        this.delegateStream = new ReaderInputStream(cs, charsetEncoder);
        this.markBuffer = null;
        this.isReadingFromBuffer = false;
        this.hasMark = false;
    }

    /**
     * Constructs a new instance with a buffer size of 2048.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     */
    public CharSequenceInputStream(final CharSequence cs, final String charset) {
        this(cs, charset, BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs the input character sequence.
     * @param charset the character set name to use.
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     */
    public CharSequenceInputStream(final CharSequence cs, final String charset, final int bufferSize) {
        this(cs, Charset.forName(charset), bufferSize);
    }

    /**
     * Return an estimate of the number of bytes remaining in the byte stream.
     * @return the count of bytes that can be read without blocking (or returning EOF).
     *
     * @throws IOException if an error occurs (probably not possible).
     */
    @Override
    public int available() throws IOException {
        int availableMarkBytes = 0;
        if (isReadingFromBuffer) {
            availableMarkBytes = markBuffer.remaining();
        }
        return availableMarkBytes + delegateStream.available();
    }

    @Override
    public void close() throws IOException {
        // noop
    }

    @Override
    public synchronized void mark(final int readLimit) {
        if (readLimit <= 0) {
            hasMark = false;
            if (!isReadingFromBuffer) {
                markBuffer = null;
            }
        } else {
            hasMark = true;

            // When currently reading from buffer, keep using it for reading, but make sure
            // that it has in total enough space to satisfy readLimit
            if (isReadingFromBuffer) {
                if (markBuffer.capacity() >= readLimit) {
                    markBuffer.compact();
                    markBuffer.flip();
                } else {
                    ByteBuffer oldBuffer = markBuffer;
                    markBuffer = ByteBuffer.allocate(readLimit);
                    markBuffer.put(oldBuffer);
                    markBuffer.flip();
                }
            } else {
                markBuffer = ByteBuffer.allocate(readLimit);
            }
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    private void checkReadAllFromBuffer() {
        if (!markBuffer.hasRemaining()) {
            isReadingFromBuffer = false;
            if (hasMark) {
                // If has mark continue writing after last read position
                int oldLimit = markBuffer.limit();
                markBuffer.limit(markBuffer.capacity());
                markBuffer.position(oldLimit);
            } else {
                // Buffer is not reused for buffering marked bytes, can clear it
                markBuffer = null;
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (isReadingFromBuffer) {
            int b = markBuffer.get();
            checkReadAllFromBuffer();
            return b;
        }

        int b = delegateStream.read();
        if (b != -1 && hasMark) {
            if (markBuffer.hasRemaining()) {
                markBuffer.put((byte) b);
            } else {
                // Read past the mark readLimit
                markBuffer = null;
                hasMark = false;
            }
        }
        return b;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] array, int off, int len) throws IOException {
        Objects.requireNonNull(array, "array");
        if (len < 0 || (off + len) > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + off + ", length=" + len);
        }
        if (len == 0) {
            return 0; // must return 0 for zero length read
        }

        if (isReadingFromBuffer) {
            int bytesRead = Math.min(markBuffer.remaining(), len);
            markBuffer.get(array, off, bytesRead);
            checkReadAllFromBuffer();
            return bytesRead;
        }

        int bytesRead = delegateStream.read(array, off, len);
        if (bytesRead != -1 && hasMark) {
            if (bytesRead <= markBuffer.remaining()) {
                markBuffer.put(array, off, len);
            } else {
                // Read past the mark readLimit
                markBuffer = null;
                hasMark = false;
            }
        }
        return bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (!hasMark) {
            throw new IOException("No mark exists");
        }

        if (isReadingFromBuffer) {
            // Currently still reading from buffer, so just reset to mark position
            markBuffer.position(0);
        } else if (markBuffer.position() > 0) {
            markBuffer.flip();
            isReadingFromBuffer = true;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }

        if (isReadingFromBuffer) {
            int toSkip = (int) Math.min(markBuffer.remaining(), n);
            markBuffer.position(markBuffer.position() + toSkip);
            checkReadAllFromBuffer();
            return toSkip;
        } else if (hasMark) {
            if (markBuffer.hasRemaining()) {
                // Directly read into markBuffer array
                int bytesRead = read(markBuffer.array(), markBuffer.position(), markBuffer.remaining());
                markBuffer.position(markBuffer.position() + bytesRead);
                return bytesRead;
            } else {
                // Read past the mark readLimit
                markBuffer = null;
                hasMark = false;

                // Call read() to make sure at least 1 byte is skipped; skip() might not skip any
                read();
                n--; // reduce for byte skipped by read()
                return delegateStream.skip(n);
            }
        } else {
            return delegateStream.skip(n);
        }
    }
}
