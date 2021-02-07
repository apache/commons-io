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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.io.IOUtils.EOF;

/**
 * This is the base class for implementing an output stream in which the data
 * is written into a byte array. The buffer automatically grows as data
 * is written to it.
 * <p>
 * The data can be retrieved using {@code toByteArray()} and
 * {@code toString()}.
 * Closing an {@code AbstractByteArrayOutputStream} has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an {@code IOException}.
 * </p>
 * <p>
 * This is the base for an alternative implementation of the
 * {@link java.io.ByteArrayOutputStream} class. The original implementation
 * only allocates 32 bytes at the beginning. As this class is designed for
 * heavy duty it starts at {@value #DEFAULT_SIZE} bytes. In contrast to the original it doesn't
 * reallocate the whole memory block but allocates additional buffers. This
 * way no buffers need to be garbage collected and the contents don't have
 * to be copied to the new buffer. This class is designed to behave exactly
 * like the original. The only exception is the deprecated
 * {@link java.io.ByteArrayOutputStream#toString(int)} method that has been
 * ignored.
 * </p>
 *
 * @since 2.7
 */
public abstract class AbstractByteArrayOutputStream extends OutputStream {

    static final int DEFAULT_SIZE = 1024;

    /** The list of buffers, which grows and never reduces. */
    private final List<byte[]> buffers = new ArrayList<>();
    /** The index of the current buffer. */
    private int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    private int filledBufferSum;
    /** The current buffer. */
    private byte[] currentBuffer;
    /** The total count of bytes written. */
    protected int count;
    /** Flag to indicate if the buffers can be reused after reset */
    private boolean reuseBuffers = true;

    /**
     * Makes a new buffer available either by allocating
     * a new one or re-cycling an existing one.
     *
     * @param newcount  the size of the buffer if one is created
     */
    protected void needNewBuffer(final int newcount) {
        if (currentBufferIndex < buffers.size() - 1) {
            //Recycling old buffer
            filledBufferSum += currentBuffer.length;

            currentBufferIndex++;
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //Creating new buffer
            final int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                    currentBuffer.length << 1,
                    newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }

            currentBufferIndex++;
            currentBuffer = IOUtils.byteArray(newBufferSize);
            buffers.add(currentBuffer);
        }
    }

    /**
     * Writes the bytes to the byte array.
     * @param b the bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    @Override
    public abstract void write(final byte[] b, final int off, final int len);

    /**
     * Writes the bytes to the byte array.
     * @param b the bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    protected void writeImpl(final byte[] b, final int off, final int len) {
        final int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            final int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        count = newcount;
    }

    /**
     * Write a byte to byte array.
     * @param b the byte to write
     */
    @Override
    public abstract void write(final int b);

    /**
     * Write a byte to byte array.
     * @param b the byte to write
     */
    protected void writeImpl(final int b) {
        int inBufferPos = count - filledBufferSum;
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(count + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (byte) b;
        count++;
    }


    /**
     * Writes the entire contents of the specified input stream to this
     * byte stream. Bytes from the input stream are read directly into the
     * internal buffers of this streams.
     *
     * @param in the input stream to read from
     * @return total number of bytes read from the input stream
     *         (and written to this stream)
     * @throws IOException if an I/O error occurs while reading the input stream
     * @since 1.4
     */
    public abstract int write(final InputStream in) throws IOException;

    /**
     * Writes the entire contents of the specified input stream to this
     * byte stream. Bytes from the input stream are read directly into the
     * internal buffers of this streams.
     *
     * @param in the input stream to read from
     * @return total number of bytes read from the input stream
     *         (and written to this stream)
     * @throws IOException if an I/O error occurs while reading the input stream
     * @since 2.7
     */
    protected int writeImpl(final InputStream in) throws IOException {
        int readCount = 0;
        int inBufferPos = count - filledBufferSum;
        int n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != EOF) {
            readCount += n;
            inBufferPos += n;
            count += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    /**
     * Returns the current size of the byte array.
     *
     * @return the current size of the byte array
     */
    public abstract int size();

    /**
     * Closing a {@code ByteArrayOutputStream} has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an {@code IOException}.
     *
     * @throws IOException never (this method should not declare this exception
     * but it has to now due to backwards compatibility)
     */
    @Override
    public void close() throws IOException {
        //nop
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    public abstract void reset();

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    protected void resetImpl() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        if (reuseBuffers) {
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //Throw away old buffers
            currentBuffer = null;
            final int size = buffers.get(0).length;
            buffers.clear();
            needNewBuffer(size);
            reuseBuffers = true;
        }
    }

    /**
     * Writes the entire contents of this byte stream to the
     * specified output stream.
     *
     * @param out  the output stream to write to
     * @throws IOException if an I/O error occurs, such as if the stream is closed
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    public abstract void writeTo(final OutputStream out) throws IOException;

    /**
     * Writes the entire contents of this byte stream to the
     * specified output stream.
     *
     * @param out  the output stream to write to
     * @throws IOException if an I/O error occurs, such as if the stream is closed
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    protected void writeToImpl(final OutputStream out) throws IOException {
        int remaining = count;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * Gets the current contents of this byte stream as a Input Stream. The
     * returned stream is backed by buffers of {@code this} stream,
     * avoiding memory allocation and copy, thus saving space and time.<br>
     *
     * @return the current contents of this output stream.
     * @see java.io.ByteArrayOutputStream#toByteArray()
     * @see #reset()
     * @since 2.5
     */
    public abstract InputStream toInputStream();

    /**
     * Gets the current contents of this byte stream as a Input Stream. The
     * returned stream is backed by buffers of {@code this} stream,
     * avoiding memory allocation and copy, thus saving space and time.<br>
     *
     * @param <T> the type of the InputStream which makes up
     *            the {@link SequenceInputStream}.
     * @param isConstructor A constructor for an InputStream which makes
     *                     up the {@link SequenceInputStream}.
     *
     * @return the current contents of this output stream.
     * @see java.io.ByteArrayOutputStream#toByteArray()
     * @see #reset()
     * @since 2.7
     */
    @SuppressWarnings("resource") // The result InputStream MUST be managed by the call site.
    protected <T extends InputStream> InputStream toInputStream(
            final InputStreamConstructor<T> isConstructor) {
        int remaining = count;
        if (remaining == 0) {
            return ClosedInputStream.CLOSED_INPUT_STREAM;
        }
        final List<T> list = new ArrayList<>(buffers.size());
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            list.add(isConstructor.construct(buf, 0, c));
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        reuseBuffers = false;
        return new SequenceInputStream(Collections.enumeration(list));
    }

    /**
     * Constructor for an InputStream subclass.
     *
     * @param <T> the type of the InputStream.
     */
    @FunctionalInterface
    protected interface InputStreamConstructor<T extends InputStream> {

        /**
         * Construct an InputStream subclass.
         *
         * @param buf the buffer
         * @param offset the offset into the buffer
         * @param length the length of the buffer
         *
         * @return the InputStream subclass.
         */
        T construct(final byte[] buf, final int offset, final int length);
    }

    /**
     * Gets the current contents of this byte stream as a byte array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a byte array
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    public abstract byte[] toByteArray();

    /**
     * Gets the current contents of this byte stream as a byte array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a byte array
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    protected byte[] toByteArrayImpl() {
        int remaining = count;
        if (remaining == 0) {
            return IOUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] newbuf = IOUtils.byteArray(remaining);
        int pos = 0;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        return newbuf;
    }

    /**
     * Gets the current contents of this byte stream as a string
     * using the platform default charset.
     * @return the contents of the byte array as a String
     * @see java.io.ByteArrayOutputStream#toString()
     * @deprecated 2.5 use {@link #toString(String)} instead
     */
    @Override
    @Deprecated
    public String toString() {
        // make explicit the use of the default charset
        return new String(toByteArray(), Charset.defaultCharset());
    }

    /**
     * Gets the current contents of this byte stream as a string
     * using the specified encoding.
     *
     * @param enc  the name of the character encoding
     * @return the string converted from the byte array
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @see java.io.ByteArrayOutputStream#toString(String)
     */
    public String toString(final String enc) throws UnsupportedEncodingException {
        return new String(toByteArray(), enc);
    }

    /**
     * Gets the current contents of this byte stream as a string
     * using the specified encoding.
     *
     * @param charset  the character encoding
     * @return the string converted from the byte array
     * @see java.io.ByteArrayOutputStream#toString(String)
     * @since 2.5
     */
    public String toString(final Charset charset) {
        return new String(toByteArray(), charset);
    }

}
