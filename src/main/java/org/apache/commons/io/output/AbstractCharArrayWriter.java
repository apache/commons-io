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
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.input.ClosedReader;
import org.apache.commons.io.input.SequenceReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.io.IOUtils.EOF;

/**
 * This is the base class for implementing a writer in which the data
 * is written into a char array. The buffer automatically grows as data
 * is written to it.
 * <p>
 * The data can be retrieved using {@code toString()}.
 * Closing an {@code AbstractCharArrayWriter} has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an {@code IOException}.
 * </p>
 * <p>
 * This is the base for an alternative implementation of the
 * {@link java.io.CharArrayWriter} class. The original implementation
 * only allocates 32 bytes at the beginning. It is also a good alternative to
 * {@link java.io.StringWriter} class.
 *
 * As this class is designed for
 * heavy duty it starts at {@value #DEFAULT_SIZE} bytes. In contrast to the original it doesn't
 * reallocate the whole memory block but allocates additional buffers. This
 * way no buffers need to be garbage collected and the contents don't have
 * to be copied to the new buffer. This class is designed to behave exactly
 * like the original.
 * </p>
 *
 * @since 2.9
 */
public abstract class AbstractCharArrayWriter extends Writer {

    static final int DEFAULT_SIZE = 1024;

    /** The list of buffers, which grows and never reduces. */
    private final List<char[]> buffers = new ArrayList<>();
    /** The index of the current buffer. */
    private int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    private int filledBufferSum;
    /** The current buffer. */
    private char[] currentBuffer;
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
            currentBuffer = IOUtils.charArray(newBufferSize);
            buffers.add(currentBuffer);
        }
    }

    /**
     * Writes the chars to the char array.
     * @param c the chars to write
     * @param off The start offset
     * @param len The number of chars to write
     */
    @Override
    public abstract void write(final char[] c, final int off, final int len);

    /**
     * Writes the chars to the char array.
     * @param c the chars to write
     * @param off The start offset
     * @param len The number of chars to write
     */
    protected void writeImpl(final char[] c, final int off, final int len) {
        final int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            final int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(c, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        count = newcount;
    }

    /**
     * Write a char to char array.
     * @param c the char to write
     */
    @Override
    public abstract void write(final int c);

    /**
     * Write a char to char array.
     * @param c the char to write
     */
    protected void writeImpl(final int c) {
        int inBufferPos = count - filledBufferSum;
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(count + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (char) c;
        count++;
    }


    /**
     * Writes the entire contents of the specified reader to this
     * char stream. Chars from the reader are read directly into the
     * internal buffers of this stream.
     *
     * @param r the reader to read from
     * @return total number of chars read from the reader
     *         (and written to this stream)
     * @throws IOException if an I/O error occurs while reading the reader
     */
    public abstract int write(final Reader r) throws IOException;

    /**
     * Writes the entire contents of the specified reader to this
     * char stream. Chars from the reader are read directly into the
     * internal buffers of this stream.
     *
     * @param r the reader to read from
     * @return total number of chars read from the reader
     *         (and written to this stream)
     * @throws IOException if an I/O error occurs while reading the reader
     */
    protected int writeImpl(final Reader r) throws IOException {
        int readCount = 0;
        int inBufferPos = count - filledBufferSum;
        int n = r.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != EOF) {
            readCount += n;
            inBufferPos += n;
            count += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = r.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    /**
     * Returns the current size of the char array.
     *
     * @return the current size of the char array
     */
    public abstract int size();

    /**
     * @see java.io.CharArrayWriter#reset()
     */
    public abstract void reset();

    /**
     * @see java.io.CharArrayWriter#reset()
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
     * Closing a {@code Writer} has no effect. The methods in
     * this class can be called after the writer has been closed without
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
     * Writes the entire contents of this writer to the
     * specified writer.
     *
     * @param out  the writer to write to
     * @throws IOException if an I/O error occurs, such as if the writer is closed
     */
    public abstract void writeTo(final Writer out) throws IOException;

    /**
     * Writes the entire contents of this char stream to the
     * specified writer.
     *
     * @param out  the writer to write to
     * @throws IOException if an I/O error occurs, such as if the stream is closed
     */
    protected void writeToImpl(final Writer out) throws IOException {
        int remaining = count;
        for (final char[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * Gets the current contents of this char stream as a Reader. The
     * returned stream is backed by buffers of {@code this} stream,
     * avoiding memory allocation and copy, thus saving space and time.<br>
     *
     * @return the current contents of this writer.
     */
    public abstract Reader toReader();

    /**
     * Gets the current contents of this byte stream as a Input Stream. The
     * returned stream is backed by buffers of {@code this} stream,
     * avoiding memory allocation and copy, thus saving space and time.<br>
     *
     * @param <T> the type of the InputStream which makes up
     *            the {@link SequenceInputStream}.
     * @param readerConstructor A constructor for a Reader which makes
     *                     up the {@link SequenceReader}.
     *
     * @return the current contents of this output stream.
     */
    @SuppressWarnings("resource") // The result InputStream MUST be managed by the call site.
    protected <T extends Reader> Reader toReader(
            final ReaderConstructor<T> readerConstructor) {
        int remaining = count;
        if (remaining == 0) {
            return ClosedReader.CLOSED_READER;
        }
        final List<T> list = new ArrayList<>(buffers.size());
        for (final char[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            list.add(readerConstructor.construct(buf, 0, c));
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        reuseBuffers = false;
        return new SequenceReader(list);
    }

    /**
     * Constructor for a Reader subclass.
     *
     * @param <T> the type of the InputStream.
     */
    @FunctionalInterface
    protected interface ReaderConstructor<T extends Reader> {

        /**
         * Construct a Reader subclass.
         *
         * @param buf the buffer
         * @param offset the offset into the buffer
         * @param length the length of the buffer
         *
         * @return the Reader subclass.
         */
        T construct(final char[] buf, final int offset, final int length);
    }

    /**
     * Gets the current contents of this byte stream as a byte array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a byte array
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    public abstract char[] toCharArray();

    /**
     * Gets the current contents of this byte stream as a byte array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a byte array
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    protected char[] toCharArrayImpl() {
        int remaining = count;
        if (remaining == 0) {
            return new char[0];
        }
        final char[] newbuf = IOUtils.charArray(remaining);
        int pos = 0;
        for (final char[] buf : buffers) {
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

    @Override
    public void flush() throws IOException {
    }

    @Override
    public String toString() {
        return new String(toCharArrayImpl());
    }
}
