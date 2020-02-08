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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * Implementation of a buffered input stream, which is internally based on the
 * {@link CircularByteBuffer}. Unlike the {@link java.io.BufferedInputStream}, this one
 * doesn't need to reallocate byte arrays internally.
 */
public class CircularBufferInputStream extends InputStream {
    protected final InputStream in;
    protected final CircularByteBuffer buffer;
    protected final int bufferSize;
    private boolean eofSeen;

    /**
     * Creates a new instance, which filters the given input stream, and
     * uses the given buffer size.
     *
     * @param pIn         The input stream, which is being buffered.
     * @param pBufferSize The size of the {@link CircularByteBuffer}, which is
     *                    used internally.
     */
    public CircularBufferInputStream(final InputStream pIn, final int pBufferSize) {
        Objects.requireNonNull(pIn, "InputStream");
        if (pBufferSize <= 0) {
            throw new IllegalArgumentException("Invalid buffer size: " + pBufferSize);
        }
        in = pIn;
        buffer = new CircularByteBuffer(pBufferSize);
        bufferSize = pBufferSize;
        eofSeen = false;
    }

    /**
     * Creates a new instance, which filters the given input stream, and
     * uses a reasonable default buffer size (8192).
     *
     * @param pIn The input stream, which is being buffered.
     */
    public CircularBufferInputStream(final InputStream pIn) {
        this(pIn, 8192);
    }

    /**
     * Fills the buffer with the contents of the input stream.
     *
     * @throws IOException in case of an error while reading from the input stream.
     */
    protected void fillBuffer() throws IOException {
        if (eofSeen) {
            return;
        }
        int space = buffer.getSpace();
        final byte[] buf = new byte[space];
        while (space > 0) {
            final int res = in.read(buf, 0, space);
            if (res == -1) {
                eofSeen = true;
                return;
            } else if (res > 0) {
                buffer.add(buf, 0, res);
                space -= res;
            }
        }
    }

    /**
     * Fills the buffer from the input stream until the given number of bytes have been added to the buffer.
     *
     * @param pNumber number of byte to fill into the buffer
     * @return true if the buffer has bytes
     * @throws IOException in case of an error while reading from the input stream.
     */
    protected boolean haveBytes(final int pNumber) throws IOException {
        if (buffer.getCurrentNumberOfBytes() < pNumber) {
            fillBuffer();
        }
        return buffer.hasBytes();
    }

    @Override
    public int read() throws IOException {
        if (!haveBytes(1)) {
            return -1;
        }
        return buffer.read();
    }

    @Override
    public int read(final byte[] pBuffer) throws IOException {
        return read(pBuffer, 0, pBuffer.length);
    }

    @Override
    public int read(final byte[] pBuffer, final int pOffset, final int pLength) throws IOException {
        Objects.requireNonNull(pBuffer, "Buffer");
        if (pOffset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        if (pLength < 0) {
            throw new IllegalArgumentException("Length must not be negative");
        }
        if (!haveBytes(pLength)) {
            return -1;
        }
        final int result = Math.min(pLength, buffer.getCurrentNumberOfBytes());
        for (int i = 0; i < result; i++) {
            pBuffer[pOffset + i] = buffer.read();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        in.close();
        eofSeen = true;
        buffer.clear();
    }
}
