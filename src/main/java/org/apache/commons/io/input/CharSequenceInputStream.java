/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.charset.CharsetEncoders;
import org.apache.commons.io.function.Uncheck;

/**
 * Implements an {@link InputStream} to read bytes from String, StringBuffer, StringBuilder or CharBuffer,
 * encoded using the specified Charset. The Charset defaults to Charset.defaultCharset().
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * <p>
 * Instances are not thread-safe. If an instance is shared between threads, callers must externally synchronize all access using the same lock.
 * The synchronization of {@link #mark(int)} and {@link #reset()} alone does not make other operations thread-safe.
 * </p>
 *
 * @see Builder
 * @since 2.2
 */
public class CharSequenceInputStream extends InputStream {

    //@formatter:off
    /**
     * Builds a new {@link CharSequenceInputStream}.
     *
     * <p>
     * For example:
     * </p>
     * <h2>Using a Charset</h2>
     * <pre>{@code
     * CharSequenceInputStream s = CharSequenceInputStream.builder()
     *   .setBufferSize(8192)
     *   .setCharSequence("String")
     *   .setCharset(Charset.defaultCharset())
     *   .get();}
     * </pre>
     * <h2>Using a CharsetEncoder</h2>
     * <pre>{@code
     * CharSequenceInputStream s = CharSequenceInputStream.builder()
     *   .setBufferSize(8192)
     *   .setCharSequence("String")
     *   .setCharsetEncoder(Charset.defaultCharset().newEncoder()
     *     .onMalformedInput(CodingErrorAction.REPLACE)
     *     .onUnmappableCharacter(CodingErrorAction.REPLACE))
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.13.0
     */
    //@formatter:on
    public static class Builder extends AbstractStreamBuilder<CharSequenceInputStream, Builder> {

        private CharsetEncoder charsetEncoder = newEncoder(getCharset());

        /**
         * Constructs a new builder of {@link CharSequenceInputStream}.
         */
        public Builder() {
            // empty
        }

        /**
         * Builds a new {@link CharSequenceInputStream}.
         * <p>
         * You must set an aspect that supports {@link #getCharSequence()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getCharSequence()} gets the target aspect.</li>
         * <li>{@link #getBufferSize()}</li>
         * <li>{@link CharsetEncoder}</li>
         * </ul>
         *
         * @return A new instance.
         * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
         * @see #getUnchecked()
         */
        @Override
        public CharSequenceInputStream get() {
            return Uncheck.get(() -> new CharSequenceInputStream(this));
        }

        CharsetEncoder getCharsetEncoder() {
            return charsetEncoder;
        }

        @Override
        public Builder setCharset(final Charset charset) {
            super.setCharset(charset);
            charsetEncoder = newEncoder(getCharset());
            return this;
        }

        /**
         * Sets the charset encoder. Assumes that the caller has configured the encoder.
         *
         * @param newEncoder The charset encoder.
         * @return {@code this} instance.
         * @since 2.13.0
         */
        public Builder setCharsetEncoder(final CharsetEncoder newEncoder) {
            charsetEncoder = CharsetEncoders.toCharsetEncoder(newEncoder, () -> newEncoder(getCharsetDefault()));
            super.setCharset(charsetEncoder.charset());
            return this;
        }

    }

    private static final int NO_MARK = -1;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return A new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private static CharsetEncoder newEncoder(final Charset charset) {
        // @formatter:off
        return Charsets.toCharset(charset).newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // @formatter:on
    }

    private final ByteBuffer bBuf;
    private final CharBuffer cBuf;
    private final CharsetEncoder charsetEncoder;
    private boolean endOfInputDrained; // all chars encoded (endOfInput=true returned UNDERFLOW with no input left)
    private boolean encoderFlushed; // CharsetEncoder.flush() completed (returned UNDERFLOW)
    private boolean closed;
    private long position;
    private long mark = NO_MARK;

    private CharSequenceInputStream(final Builder builder) {
        this.charsetEncoder = builder.charsetEncoder;
        // Ensure that buffer is long enough to hold a complete character
        this.bBuf = ByteBuffer.allocate(ReaderInputStream.checkMinBufferSize(builder.charsetEncoder, builder.getBufferSize()));
        this.bBuf.flip();
        this.cBuf = CharBuffer.wrap(Uncheck.get(() -> builder.getCharSequence()));
        try {
            fillBuffer();
        } catch (final CharacterCodingException ex) {
            // Reset everything without filling the buffer
            // so the same exception can be thrown again later.
            this.charsetEncoder.reset();
            this.endOfInputDrained = false;
            this.encoderFlushed = false;
            this.bBuf.clear();
            this.bBuf.flip();
            this.cBuf.rewind();
        }
    }

    /**
     * Constructs a new instance with a buffer size of {@link IOUtils#DEFAULT_BUFFER_SIZE}.
     *
     * @param cs The input character sequence.
     * @param charset The character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final Charset charset) {
        this(cs, charset, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs The input character sequence.
     * @param charset The character set name to use, null maps to the default Charset.
     * @param bufferSize The buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final Charset charset, final int bufferSize) {
        this(builder().setCharSequence(cs).setCharset(charset).setBufferSize(bufferSize));
    }

    /**
     * Constructs a new instance with a buffer size of {@link IOUtils#DEFAULT_BUFFER_SIZE}.
     *
     * @param cs The input character sequence.
     * @param charset The character set name to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final String charset) {
        this(cs, charset, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cs The input character sequence.
     * @param charset The character set name to use, null maps to the default Charset.
     * @param bufferSize The buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public CharSequenceInputStream(final CharSequence cs, final String charset, final int bufferSize) {
        this(cs, Charsets.toCharset(charset), bufferSize);
    }

    /**
     * Gets a lower bound on the number of bytes remaining in the byte stream.
     *
     * @return The count of bytes that can be read without blocking (or returning EOF).
     * @throws IOException Thrown if an error occurs (probably not possible).
     */
    @Override
    public int available() throws IOException {
        return this.bBuf.remaining();
    }

    /**
     * Closes this stream.
     *
     * <p>
     * Once closed, {@link #available()} returns 0, reads return EOF (except zero-length reads, which return 0), and {@link #skip(long)} returns 0.
     * Calls to {@link #mark(int)} and {@link #reset()} have no effect.
     * </p>
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        bBuf.position(bBuf.limit());
        closed = true;
    }

    /**
     * Fills the byte output buffer from the input char buffer.
     * <p>
     * Once all input characters have been encoded, this method flushes the encoder so that any final bytes a stateful
     * charset requires (for example the shift-back sequence of the ISO-2022 family) are emitted, matching
     * {@link java.io.OutputStreamWriter} and {@link ReaderInputStream}.
     * </p>
     *
     * @throws CharacterCodingException
     *             an error encoding data.
     */
    private void fillBuffer() throws CharacterCodingException {
        this.bBuf.compact();
        if (!endOfInputDrained) {
            final CoderResult result = this.charsetEncoder.encode(this.cBuf, this.bBuf, true);
            if (result.isError()) {
                result.throwException();
            }
            if (result.isUnderflow() && !this.cBuf.hasRemaining()) {
                endOfInputDrained = true;
            }
        }
        if (endOfInputDrained && !encoderFlushed) {
            final CoderResult result = this.charsetEncoder.flush(this.bBuf);
            if (result.isError()) {
                result.throwException();
            }
            if (result.isUnderflow()) {
                encoderFlushed = true;
            }
            // on OVERFLOW, the next call flushes the remainder.
        }
        this.bBuf.flip();
    }

    /**
     * Tests whether the encoder has consumed all input and completed flushing.
     *
     * <p>
     * Buffered bytes may still remain to be read. An open stream reaches EOF only when encoding is complete and the byte buffer is empty.
     * </p>
     *
     * @return whether no additional encoded bytes can be produced.
     */
    private boolean encodingComplete() {
        return !cBuf.hasRemaining() && endOfInputDrained && encoderFlushed;
    }

    /**
     * Gets the CharsetEncoder.
     *
     * @return The CharsetEncoder.
     */
    CharsetEncoder getCharsetEncoder() {
        return charsetEncoder;
    }

    /**
     * {@inheritDoc}
     *
     * @param readLimit max read limit (ignored).
     */
    @Override
    public synchronized void mark(final int readLimit) {
        if (!closed) {
            mark = position;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            return EOF;
        }
        for (;;) {
            if (this.bBuf.hasRemaining()) {
                position++;
                return this.bBuf.get() & 0xFF;
            }
            fillBuffer();
            if (!this.bBuf.hasRemaining() && encodingComplete()) {
                return EOF;
            }
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] array, int off, int len) throws IOException {
        IOUtils.checkFromIndexSize(array, off, len);
        if (len == 0) {
            return 0; // must return 0 for zero length read
        }
        if (closed) {
            return EOF;
        }
        if (!this.bBuf.hasRemaining() && encodingComplete()) {
            return EOF;
        }
        int bytesRead = 0;
        while (len > 0) {
            if (this.bBuf.hasRemaining()) {
                final int chunk = Math.min(this.bBuf.remaining(), len);
                this.bBuf.get(array, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
                position += chunk;
            } else {
                fillBuffer();
                if (!this.bBuf.hasRemaining() && encodingComplete()) {
                    break;
                }
            }
        }
        return bytesRead == 0 && encodingComplete() ? EOF : bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (closed) {
            return;
        }
        if (mark != NO_MARK) {
            // Replay to the byte offset: multiple buffers can share the same character position while the encoder is flushing.
            final long markedPosition = mark;
            charsetEncoder.reset();
            endOfInputDrained = false;
            encoderFlushed = false;
            cBuf.rewind();
            bBuf.clear();
            bBuf.flip();
            position = 0;
            // Fill even at offset zero to restore the initial buffer and its availability.
            fillBuffer();
            if (skipBytes(markedPosition) != markedPosition) {
                throw new IOException("Cannot restore marked byte position: " + markedPosition);
            }
        }
        mark(0);
    }

    @Override
    public long skip(final long n) throws IOException {
        return closed ? 0 : skipBytes(n);
    }

    private long skipBytes(long n) throws IOException {
        long skipped = 0;
        while (n > 0) {
            if (this.bBuf.hasRemaining()) {
                final int chunk = (int) Math.min(n, bBuf.remaining());
                bBuf.position(bBuf.position() + chunk);
                n -= chunk;
                skipped += chunk;
                position += chunk;
            } else {
                fillBuffer();
                if (!this.bBuf.hasRemaining() && encodingComplete()) {
                    break;
                }
            }
        }
        return skipped;
    }

}
