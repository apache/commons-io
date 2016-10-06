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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * {@link InputStream} implementation that can read from String, StringBuffer,
 * StringBuilder or CharBuffer.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 *
 * @since 2.2
 */
public class CharSequenceInputStream extends InputStream {

    private static final int BUFFER_SIZE = 2048;

    private static final int NO_MARK = -1;

    private final CharsetEncoder encoder;
    private final CharBuffer cbuf;
    private final ByteBuffer bbuf;

    private int mark_cbuf; // position in cbuf
    private int mark_bbuf; // position in bbuf

    /**
     * Constructor.
     *
     * @param cs the input character sequence
     * @param charset the character set name to use
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character
     */
    public CharSequenceInputStream(final CharSequence cs, final Charset charset, final int bufferSize) {
        super();
        this.encoder = charset.newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // Ensure that buffer is long enough to hold a complete character
        final float maxBytesPerChar = encoder.maxBytesPerChar();
        if (bufferSize < maxBytesPerChar) {
            throw new IllegalArgumentException("Buffer size " + bufferSize + " is less than maxBytesPerChar " +
                    maxBytesPerChar);
        }
        this.bbuf = ByteBuffer.allocate(bufferSize);
        this.bbuf.flip();
        this.cbuf = CharBuffer.wrap(cs);
        this.mark_cbuf = NO_MARK;
        this.mark_bbuf = NO_MARK;
    }

    /**
     * Constructor, calls {@link #CharSequenceInputStream(CharSequence, Charset, int)}.
     *
     * @param cs the input character sequence
     * @param charset the character set name to use
     * @param bufferSize the buffer size to use.
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character
     */
    public CharSequenceInputStream(final CharSequence cs, final String charset, final int bufferSize) {
        this(cs, Charset.forName(charset), bufferSize);
    }

    /**
     * Constructor, calls {@link #CharSequenceInputStream(CharSequence, Charset, int)}
     * with a buffer size of 2048.
     *
     * @param cs the input character sequence
     * @param charset the character set name to use
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character
     */
    public CharSequenceInputStream(final CharSequence cs, final Charset charset) {
        this(cs, charset, BUFFER_SIZE);
    }

    /**
     * Constructor, calls {@link #CharSequenceInputStream(CharSequence, String, int)}
     * with a buffer size of 2048.
     *
     * @param cs the input character sequence
     * @param charset the character set name to use
     * @throws IllegalArgumentException if the buffer is not large enough to hold a complete character
     */
    public CharSequenceInputStream(final CharSequence cs, final String charset) {
        this(cs, charset, BUFFER_SIZE);
    }

    /**
     * Fills the byte output buffer from the input char buffer.
     *
     * @throws CharacterCodingException
     *             an error encoding data
     */
    private void fillBuffer() throws CharacterCodingException {
        this.bbuf.compact();
        final CoderResult result = this.encoder.encode(this.cbuf, this.bbuf, true);
        if (result.isError()) {
            result.throwException();
        }
        this.bbuf.flip();
    }

    @Override
    public int read(final byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("Byte array is null");
        }
        if (len < 0 || (off + len) > b.length) {
            throw new IndexOutOfBoundsException("Array Size=" + b.length +
                    ", offset=" + off + ", length=" + len);
        }
        if (len == 0) {
            return 0; // must return 0 for zero length read
        }
        if (!this.bbuf.hasRemaining() && !this.cbuf.hasRemaining()) {
            return EOF;
        }
        int bytesRead = 0;
        while (len > 0) {
            if (this.bbuf.hasRemaining()) {
                final int chunk = Math.min(this.bbuf.remaining(), len);
                this.bbuf.get(b, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
            } else {
                fillBuffer();
                if (!this.bbuf.hasRemaining() && !this.cbuf.hasRemaining()) {
                    break;
                }
            }
        }
        return bytesRead == 0 && !this.cbuf.hasRemaining() ? EOF : bytesRead;
    }

    @Override
    public int read() throws IOException {
        for (;;) {
            if (this.bbuf.hasRemaining()) {
                return this.bbuf.get() & 0xFF;
            }
            fillBuffer();
            if (!this.bbuf.hasRemaining() && !this.cbuf.hasRemaining()) {
                return EOF;
            }
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        /*
         * This could be made more efficient by using position to skip within the current buffer.
         */
        long skipped = 0;
        while (n > 0 && available() > 0) {
            this.read();
            n--;
            skipped++;
        }
        return skipped;
    }

    /**
     * Return an estimate of the number of bytes remaining in the byte stream.
     * @return the count of bytes that can be read without blocking (or returning EOF).
     *
     * @throws IOException if an error occurs (probably not possible)
     */
    @Override
    public int available() throws IOException {
        // The cached entries are in bbuf; since encoding always creates at least one byte
        // per character, we can add the two to get a better estimate (e.g. if bbuf is empty)
        // Note that the previous implementation (2.4) could return zero even though there were
        // encoded bytes still available.
        return this.bbuf.remaining() + this.cbuf.remaining();
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * {@inheritDoc}
     * @param readlimit max read limit (ignored)
     */
    @Override
    public synchronized void mark(final int readlimit) {
        this.mark_cbuf = this.cbuf.position();
        this.mark_bbuf = this.bbuf.position();
        this.cbuf.mark();
        this.bbuf.mark();
        // It would be nice to be able to use mark & reset on the cbuf and bbuf;
        // however the bbuf is re-used so that won't work
    }

    @Override
    public synchronized void reset() throws IOException {
        /*
         * This is not the most efficient implementation, as it re-encodes from the beginning.
         *
         * Since the bbuf is re-used, in general it's necessary to re-encode the data.
         *
         * It should be possible to apply some optimisations however:
         * + use mark/reset on the cbuf and bbuf. This would only work if the buffer had not been (re)filled since
         * the mark. The code would have to catch InvalidMarkException - does not seem possible to check if mark is
         * valid otherwise. + Try saving the state of the cbuf before each fillBuffer; it might be possible to
         * restart from there.
         */
        if (this.mark_cbuf != NO_MARK) {
            // if cbuf is at 0, we have not started reading anything, so skip re-encoding
            if (this.cbuf.position() != 0) {
                this.encoder.reset();
                this.cbuf.rewind();
                this.bbuf.rewind();
                this.bbuf.limit(0); // rewind does not clear the buffer
                while(this.cbuf.position() < this.mark_cbuf) {
                    this.bbuf.rewind(); // empty the buffer (we only refill when empty during normal processing)
                    this.bbuf.limit(0);
                    fillBuffer();
                }
            }
            if (this.cbuf.position() != this.mark_cbuf) {
                throw new IllegalStateException("Unexpected CharBuffer postion: actual=" + cbuf.position() + " " +
                        "expected=" + this.mark_cbuf);
            }
            this.bbuf.position(this.mark_bbuf);
            this.mark_cbuf = NO_MARK;
            this.mark_bbuf = NO_MARK;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

}
