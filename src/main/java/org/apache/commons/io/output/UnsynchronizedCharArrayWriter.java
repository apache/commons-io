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
package org.apache.commons.io.output;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Implements a version of {@link AbstractCharArrayWriter} <b>without</b> any concurrent thread safety.
 *
 * @since 2.10.0
 */
//@NotThreadSafe
public final class UnsynchronizedCharArrayWriter extends AbstractCharArrayWriter {

    /**
     * Creates a new char array writer. The buffer capacity is initially
     * {@value AbstractCharArrayWriter#DEFAULT_SIZE} chars, though its size increases if necessary.
     */
    public UnsynchronizedCharArrayWriter() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new string writer, with a buffer capacity of the specified size, in chars.
     *
     * @param size the initial size
     * @throws IllegalArgumentException if size is negative
     */
    public UnsynchronizedCharArrayWriter(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        needNewBuffer(size);
    }

    @Override
    public void write(final char[] b, final int off, final int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException(String.format("offset=%,d, length=%,d", off, len));
        }
        if (len == 0) {
            return;
        }
        writeImpl(b, off, len);
    }

    @Override
    public void write(final int b) {
        writeImpl(b);
    }

    @Override
    public int write(final Reader in) throws IOException {
        return writeImpl(in);
    }

    @Override
    public int size() {
        return count;
    }

    /**
     * @see java.io.CharArrayWriter#reset()
     */
    @Override
    public void reset() {
        resetImpl();
    }

    @Override
    public void writeTo(final Writer out) throws IOException {
        writeToImpl(out);
    }

    /**
     * Fetches entire contents of an {@code Reader} and represent same data as result Reader.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source Reader is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toString()}, since it avoids unnecessary allocation and copy of char[].<br>
     * This method buffers the input internally, so there is no need to use a {@code BufferedReader}.
     *
     * @param input Stream to be fully buffered.
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     */
    public static Reader toBufferedReader(final Reader input) throws IOException {
        return toBufferedReader(input, DEFAULT_SIZE);
    }

    /**
     * Fetches entire contents of an {@code Reader} and represent same data as result Reader.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source Reader is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toString()}, since it avoids unnecessary allocation and copy of char[].<br>
     * This method buffers the input internally, so there is no need to use a {@code BufferedReader}.
     *
     * @param input Stream to be fully buffered.
     * @param size the initial buffer size
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs.
     */
    public static Reader toBufferedReader(final Reader input, final int size) throws IOException {
        // It does not matter if a StringWriter is not closed as close() is a no-op
        try (final UnsynchronizedCharArrayWriter output = new UnsynchronizedCharArrayWriter(size)) {
            output.write(input);
            return output.toReader();
        }
    }

    @Override
    public Reader toReader() {
        return toReader(CharArrayReader::new);
    }

    @Override
    public char[] toCharArray() {
        return toCharArrayImpl();
    }
}
