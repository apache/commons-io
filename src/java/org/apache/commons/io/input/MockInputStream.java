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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A mock {@link InputStream} for testing purposes.
 * <p>
 * This implementation provides a light weight mock
 * object for testing with an {@link InputStream}
 * where the contents don't matter.
 * <p>
 * One use case would be for testing the handling of
 * large {@link InputStream} as it can emulate that
 * scenario without the overhead of actually processing
 * large numbers of bytes - significantly speeding up
 * test execution times.
 * <p>
 * Alternatively, if some kind of data is required as part
 * of a test the <code>processByte()</code> and
 * <code>processBytes()</code> methods can be implemented to generate
 * test data.
 *
 *
 * @since Commons IO 1.3
 * @version $Revision$
 */
public class MockInputStream extends InputStream {

    private long size;
    private long position;
    private long mark = -1;
    private long readlimit;
    private boolean eof;
    private boolean throwEofException;
    private boolean markSupported;

    /**
     * Create a mock {@link InputStream} of the specified size.
     *
     * @param size The size of the mock input stream.
     */
    public MockInputStream(long size) {
       this(size, true, false);
    }

    /**
     * Create a mock {@link InputStream} of the specified
     * size and option settings.
     *
     * @param size The size of the mock input stream.
     * @param markSupported Whether this instance will support
     * the <code>mark()</code> functionality.
     * @param throwEofException Whether this implementation
     * will throw an {@link EOFException} or return -1 when the
     * end of file is reached.
     */
    public MockInputStream(long size, boolean markSupported, boolean throwEofException) {
       this.size = size;
       this.markSupported = markSupported;
       this.throwEofException = throwEofException;
    }

    /**
     * Return the current position.
     *
     * @return the current position.
     */
    public long getPosition() {
        return position;
    }

    /**
     * Return the size of this Mock {@link InputStream}
     *
     * @return the size of the mock input stream.
     */
    public long getSize() {
        return size;
    }

    /**
     * Return the number of bytes that can be read.
     *
     * @return The number of bytes that can be read.
     */
    public int available() {
        long avail = size - position;
        if (avail <= 0) {
            return 0;
        } else if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int)avail;
        }
    }

    /**
     * Close this inputstream - resets the internal state to
     * the initial values.
     *
     * @throws IOException If an error occurs.
     */
    public void close() throws IOException {
        eof = false;
        position = 0;
        mark = -1;
    }

    /**
     * Mark the current position.
     *
     * @param readlimit The number of bytes before this marked position
     * is invalid.
     * @throws UnsupportedOperationException if mark is not supported.
     */
    public synchronized void mark(int readlimit) {
        if (!markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        mark = position;
        this.readlimit = readlimit;
    }

    /**
     * Indicates whether <i>mark</i> is supported.
     *
     * @return Whether <i>mark</i> is supported or not.
     */
    public boolean markSupported() {
        return markSupported;
    }

    /**
     * Read a byte.
     *
     * @return Either The byte value returned by <code>processByte()</code>
     * or <code>-1</code> if the end of file has been reached and
     * <code>throwEofException</code> is set to <code>false</code>.
     * @throws EOFException if the end of file is reached and
     * <code>throwEofException</code> is set to <code>true</code>.
     * @throws IOException if trying to read past the end of file.
     */
    public int read() throws IOException {
        if (eof) {
            throw new IOException("Read after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position++;
        return processByte();
    }

    /**
     * Read some bytes into the specified array.
     *
     * @param bytes The byte array to read into
     * @return The number of bytes read or <code>-1</code>
     * if the end of file has been reached and
     * <code>throwEofException</code> is set to <code>false</code>.
     * @throws EOFException if the end of file is reached and
     * <code>throwEofException</code> is set to <code>true</code>.
     * @throws IOException if trying to read past the end of file.
     */
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    /**
     * Read the specified number bytes into an array.
     *
     * @param bytes The byte array to read into.
     * @param offset The offset to start reading bytes into.
     * @param length The number of bytes to read.
     * @return The number of bytes read or <code>-1</code>
     * if the end of file has been reached and
     * <code>throwEofException</code> is set to <code>false</code>.
     * @throws EOFException if the end of file is reached and
     * <code>throwEofException</code> is set to <code>true</code>.
     * @throws IOException if trying to read past the end of file.
     */
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (eof) {
            throw new IOException("Read after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position += length;
        int returnLength = length;
        if (position > size) {
            returnLength = length - (int)(position - size);
            position = size;
        }
        processBytes(bytes, offset, returnLength);
        return returnLength;
    }

    /**
     * Reset the stream to the point when mark was last called.
     *
     * @throws UnsupportedOperationException if mark is not supported.
     * @throws IOException If no position has been marked
     * or the read limit has been exceed since the last position was
     * marked.
     */
    public synchronized void reset() throws IOException {
        if (!markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        if (mark < 0) {
            throw new IOException("No position has been marked");
        }
        if (position > (mark + readlimit)) {
            throw new IOException("Marked position [" + mark +
                    "] is no longer valid - passed the read limit [" +
                    readlimit + "]");
        }
        position = mark;
        eof = false;
    }

    /**
     * Skip a specified number of bytes.
     *
     * @param numberOfBytes The number of bytes to skip.
     * @return The number of bytes skipped or <code>-1</code>
     * if the end of file has been reached and
     * <code>throwEofException</code> is set to <code>false</code>.
     * @throws EOFException if the end of file is reached and
     * <code>throwEofException</code> is set to <code>true</code>.
     * @throws IOException if trying to read past the end of file.
     */
    public long skip(long numberOfBytes) throws IOException {
        if (eof) {
            throw new IOException("Skip after end of file");
        }
        if (position == size) {
            return doEndOfFile();
        }
        position += numberOfBytes;
        long returnLength = numberOfBytes;
        if (position > size) {
            returnLength = numberOfBytes - (position - size);
            position = size;
        }
        return returnLength;
    }

    /**
     * Return a byte value for the  <code>read()</code> method.
     * <p>
     * <strong>N.B.</strong> This implementation returns
     * zero.
     *
     * @return This implementation always returns zero.
     */
    protected int processByte() {
        return 0;
    }

    /**
     * Process the bytes for the <code>read(byte[], offset, length)</code>
     * method.
     * <p>
     * <strong>N.B.</strong> This implementation leaves the byte
     * array unchanged.
     *
     * @param bytes The byte array
     * @param offset The offset to start at.
     * @param length The number of bytes.
     */
    protected void processBytes(byte[] bytes, int offset, int length) {
    }

    /**
     * Handle End of File.
     *
     * @return <code>-1</code> if <code>throwEofException</code> is
     * set to <code>false</code>
     * @throws EOFException if <code>throwEofException</code> is set
     * to <code>true</code>.
     */
    private int doEndOfFile() throws EOFException {
        eof = true;
        if (throwEofException) {
            throw new EOFException();
        }
        return -1;
    }
}
