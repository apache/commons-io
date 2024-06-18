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
package org.apache.commons.io.input.buffer;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

/**
 * Implements a buffered input stream, which is internally based on a {@link CircularByteBuffer}. Unlike the
 * {@link BufferedInputStream}, this one doesn't need to reallocate byte arrays internally.
 *
 * @since 2.7
 */
public class CircularBufferInputStream extends FilterInputStream {

    /** Internal buffer. */
    protected final CircularByteBuffer buffer;

    /** Internal buffer size. */
    protected final int bufferSize;

    /** Whether we've seen the input stream EOF. */
    private boolean eof;

    /**
     * Constructs a new instance, which filters the given input stream, and uses a reasonable default buffer size
     * ({@link IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @param inputStream The input stream, which is being buffered.
     */
    public CircularBufferInputStream(final InputStream inputStream) {
        this(inputStream, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance, which filters the given input stream, and uses the given buffer size.
     *
     * @param inputStream The input stream, which is being buffered.
     * @param bufferSize The size of the {@link CircularByteBuffer}, which is used internally.
     */
    @SuppressWarnings("resource") // Caller closes InputStream
    public CircularBufferInputStream(final InputStream inputStream, final int bufferSize) {
        super(Objects.requireNonNull(inputStream, "inputStream"));
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Illegal bufferSize: " + bufferSize);
        }
        this.buffer = new CircularByteBuffer(bufferSize);
        this.bufferSize = bufferSize;
        this.eof = false;
    }

    @Override
    public void close() throws IOException {
        super.close();
        eof = true;
        buffer.clear();
    }

    /**
     * Fills the buffer with the contents of the input stream.
     *
     * @throws IOException in case of an error while reading from the input stream.
     */
    protected void fillBuffer() throws IOException {
        if (eof) {
            return;
        }
        int space = buffer.getSpace();
        final byte[] buf = IOUtils.byteArray(space);
        while (space > 0) {
            final int res = in.read(buf, 0, space);
            if (res == EOF) {
                eof = true;
                return;
            }
            if (res > 0) {
                buffer.add(buf, 0, res);
                space -= res;
            }
        }
    }

    /**
     * Fills the buffer from the input stream until the given number of bytes have been added to the buffer.
     *
     * @param count number of byte to fill into the buffer
     * @return true if the buffer has bytes
     * @throws IOException in case of an error while reading from the input stream.
     */
    protected boolean haveBytes(final int count) throws IOException {
        if (buffer.getCurrentNumberOfBytes() < count) {
            fillBuffer();
        }
        return buffer.hasBytes();
    }

    @Override
    public int read() throws IOException {
        if (!haveBytes(1)) {
            return EOF;
        }
        return buffer.read() & 0xFF; // return unsigned byte
    }

    @Override
    public int read(final byte[] targetBuffer, final int offset, final int length) throws IOException {
        Objects.requireNonNull(targetBuffer, "targetBuffer");
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative");
        }
        if (!haveBytes(length)) {
            return EOF;
        }
        final int result = Math.min(length, buffer.getCurrentNumberOfBytes());
        for (int i = 0; i < result; i++) {
            targetBuffer[offset + i] = buffer.read();
        }
        return result;
    }
}
