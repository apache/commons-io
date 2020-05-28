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

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implements a version of {@link AbstractByteArrayOutputStream}
 * <b>without</b> any concurrent thread safety.
 *
 * @since 2.7
 */
//@NotThreadSafe
public final class UnsynchronizedByteArrayOutputStream extends AbstractByteArrayOutputStream {

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 1024 bytes, though its size increases if necessary.
     */
    public UnsynchronizedByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param size  the initial size
     * @throws IllegalArgumentException if size is negative
     */
    public UnsynchronizedByteArrayOutputStream(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException(
                "Negative initial size: " + size);
        }
        needNewBuffer(size);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        if ((off < 0)
                || (off > b.length)
                || (len < 0)
                || ((off + len) > b.length)
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        writeImpl(b, off, len);
    }

    @Override
    public void write(final int b) {
        writeImpl(b);
    }

    @Override
    public int write(final InputStream in) throws IOException {
        return writeImpl(in);
    }

    @Override
    public int size() {
        return count;
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    @Override
    public void reset() {
        resetImpl();
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        writeToImpl(out);
    }

    /**
     * Fetches entire contents of an <code>InputStream</code> and represent
     * same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for
     * long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toByteArray()}, since it
     * avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input Stream to be fully buffered.
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs
     */
    public static InputStream toBufferedInputStream(final InputStream input)
            throws IOException {
        return toBufferedInputStream(input, 1024);
    }

    /**
     * Fetches entire contents of an <code>InputStream</code> and represent
     * same data as result InputStream.
     * <p>
     * This method is useful where,
     * </p>
     * <ul>
     * <li>Source InputStream is slow.</li>
     * <li>It has network resources associated, so we cannot keep it open for
     * long time.</li>
     * <li>It has network timeout associated.</li>
     * </ul>
     * It can be used in favor of {@link #toByteArray()}, since it
     * avoids unnecessary allocation and copy of byte[].<br>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input Stream to be fully buffered.
     * @param size the initial buffer size
     * @return A fully buffered stream.
     * @throws IOException if an I/O error occurs
     */
    public static InputStream toBufferedInputStream(final InputStream input, final int size)
            throws IOException {
        // It does not matter if a ByteArrayOutputStream is not closed as close() is a no-op
        @SuppressWarnings("resource")
        final UnsynchronizedByteArrayOutputStream output = new UnsynchronizedByteArrayOutputStream(size);
        output.write(input);
        return output.toInputStream();
    }

    @Override
    public InputStream toInputStream() {
        return toInputStreamImpl(UnsynchronizedByteArrayInputStream::new);
    }

    @Override
    public byte[] toByteArray() {
        return toByteArrayImpl();
    }
}
