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
 * Implementation of a buffered input stream, which allows to peek into
 * the buffers first bytes. This comes in handy when manually implementing
 * scanners, lexers, parsers, or the like.
 */
public class PeekableInputStream extends CircularBufferInputStream {
    /**
     * Creates a new instance, which filters the given input stream, and
     * uses the given buffer size.
     *
     * @param pIn         The input stream, which is being buffered.
     * @param pBufferSize The size of the {@link CircularByteBuffer}, which is
     *                    used internally.
     */
    public PeekableInputStream(final InputStream pIn, final int pBufferSize) {
        super(pIn, pBufferSize);
    }

    /**
     * Creates a new instance, which filters the given input stream, and
     * uses a reasonable default buffer size (8192).
     *
     * @param pIn The input stream, which is being buffered.
     */
    public PeekableInputStream(final InputStream pIn) {
        super(pIn);
    }

    /**
     * Returns, whether the next bytes in the buffer are as given by
     * {@code pBuffer}. This is equivalent to {@link #peek(byte[], int, int)}
     * with {@code pOffset} == 0, and {@code pLength} == {@code pBuffer.length}
     *
     * @param pBuffer the buffer to compare against
     * @return true if the next bytes are as given
     * @throws IOException Refilling the buffer failed.
     */
    public boolean peek(final byte[] pBuffer) throws IOException {
        Objects.requireNonNull(pBuffer, "Buffer");
        if (pBuffer.length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + pBuffer.length
                    + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < pBuffer.length) {
            fillBuffer();
        }
        return buffer.peek(pBuffer, 0, pBuffer.length);
    }

    /**
     * Returns, whether the next bytes in the buffer are as given by
     * {@code pBuffer}, {code pOffset}, and {@code pLength}.
     *
     * @param pBuffer the buffer to compare against
     * @param pOffset the start offset
     * @param pLength the length to compare
     * @return true if the next bytes in the buffer are as given
     * @throws IOException if there is a problem calling fillBuffer()
     */
    public boolean peek(final byte[] pBuffer, final int pOffset, final int pLength) throws IOException {
        Objects.requireNonNull(pBuffer, "Buffer");
        if (pBuffer.length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + pBuffer.length
                    + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < pBuffer.length) {
            fillBuffer();
        }
        return buffer.peek(pBuffer, pOffset, pLength);
    }
}
