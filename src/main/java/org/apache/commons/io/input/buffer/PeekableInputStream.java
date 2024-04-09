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

import org.apache.commons.io.IOUtils;

/**
 * Implements a buffered input stream, which allows to peek into the buffers first bytes. This comes in handy when
 * manually implementing scanners, lexers, parsers, and the like.
 *
 * @since 2.7
 */
public class PeekableInputStream extends CircularBufferInputStream {

    /**
     * Constructs a new instance, which filters the given input stream, and uses a reasonable default buffer size ({@link IOUtils#DEFAULT_BUFFER_SIZE}).
     *
     * @param inputStream The input stream, which is being buffered.
     */
    public PeekableInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Constructs a new instance, which filters the given input stream, and uses the given buffer size.
     *
     * @param inputStream The input stream, which is being buffered.
     * @param bufferSize The size of the {@link CircularByteBuffer}, which is used internally.
     */
    public PeekableInputStream(final InputStream inputStream, final int bufferSize) {
        super(inputStream, bufferSize);
    }

    /**
     * Returns whether the next bytes in the buffer are as given by {@code sourceBuffer}. This is equivalent to
     * {@link #peek(byte[], int, int)} with {@code offset} == 0, and {@code length} == {@code sourceBuffer.length}
     *
     * @param sourceBuffer the buffer to compare against
     * @return true if the next bytes are as given
     * @throws IOException Refilling the buffer failed.
     */
    public boolean peek(final byte[] sourceBuffer) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        return peek(sourceBuffer, 0, sourceBuffer.length);
    }

    /**
     * Returns whether the next bytes in the buffer are as given by {@code sourceBuffer}, {code offset}, and
     * {@code length}.
     *
     * @param sourceBuffer the buffer to compare against
     * @param offset the start offset
     * @param length the length to compare
     * @return true if the next bytes in the buffer are as given
     * @throws IOException if there is a problem calling fillBuffer()
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        if (sourceBuffer.length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + sourceBuffer.length
                + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < sourceBuffer.length) {
            fillBuffer();
        }
        return buffer.peek(sourceBuffer, offset, length);
    }
}
