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
     * Returns whether the next bytes in the buffer exactly match {@code sourceBuffer}. This is equivalent to
     * {@link #peek(byte[], int, int)} with {@code offset} == 0, and {@code length} == {@code sourceBuffer.length}.
     *
     * <p>
     * This method does not perform a prefix or starts-with check; for example, peeking {@code "Some"} against a stream
     * containing {@code "Some text buffer"} with the default buffer size returns {@code false}. This method does not consume bytes from the stream.
     * </p>
     *
     * @param sourceBuffer The buffer to compare against.
     * @return true if the buffered bytes after any refill exactly match {@code sourceBuffer}, including its length.
     * @throws IOException Refilling the buffer failed.
     */
    public boolean peek(final byte[] sourceBuffer) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        return peek(sourceBuffer, 0, sourceBuffer.length);
    }

    /**
     * Returns whether the next bytes in the buffer exactly match {@code length} bytes from {@code sourceBuffer}, starting at
     * {@code offset}. This method does not consume bytes from the stream.
     *
     * <p>
     * The requested range must match all bytes currently buffered, not necessarily all bytes remaining in the underlying stream.
     * If fewer than {@code length} bytes are buffered, this method refills the buffer up to its capacity or the end of the stream
     * before comparing. It does not refill when at least {@code length} bytes are already buffered.
     * </p>
     *
     * <p>
     * The source array may be larger than the buffer capacity; only {@code length} is limited by that capacity.
     * If the offset and length are otherwise valid but {@code offset + length} exceeds {@code sourceBuffer.length}, this method returns false.
     * </p>
     *
     * @param sourceBuffer The buffer to compare against.
     * @param offset The start offset in {@code sourceBuffer}.
     * @param length The length to compare.
     * @return true if the buffer after any refill holds exactly {@code length} bytes and they match the requested range from {@code sourceBuffer}.
     * @throws IllegalArgumentException The offset is outside the source array, or the length is negative or exceeds the buffer capacity.
     * @throws NullPointerException The source array is null.
     * @throws IOException Thrown if there is a problem calling fillBuffer().
     */
    public boolean peek(final byte[] sourceBuffer, final int offset, final int length) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        if (length > bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + length
                + " bytes exceeds buffer size of " + bufferSize + " bytes");
        }
        if (buffer.getCurrentNumberOfBytes() < length) {
            fillBuffer();
        }
        return buffer.peek(sourceBuffer, offset, length);
    }
}
