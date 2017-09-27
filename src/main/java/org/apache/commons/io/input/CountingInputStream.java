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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;

/**
 * A decorating input stream that counts the number of bytes that have passed
 * through the stream so far.
 * <p>
 * A typical use case would be during debugging, to ensure that data is being
 * read as expected.
 *
 */
public class CountingInputStream extends ProxyInputStream {

    /** The count of bytes that have passed. */
    private long count;

    /**
     * Constructs a new CountingInputStream.
     *
     * @param in  the InputStream to delegate to
     */
    public CountingInputStream(final InputStream in) {
        super(in);
    }

    //-----------------------------------------------------------------------

    /**
     * Skips the stream over the specified number of bytes, adding the skipped
     * amount to the count.
     *
     * @param length  the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error occurs
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public synchronized long skip(final long length) throws IOException {
        final long skip = super.skip(length);
        this.count += skip;
        return skip;
    }

    /**
     * Adds the number of read bytes to the count.
     *
     * @param n number of bytes read, or -1 if no more bytes are available
     * @since 2.0
     */
    @Override
    protected synchronized void afterRead(final int n) {
        if (n != EOF) {
            this.count += n;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * The number of bytes that have passed through this stream.
     * <p>
     * NOTE: From v1.3 this method throws an ArithmeticException if the
     * count is greater than can be expressed by an <code>int</code>.
     * See {@link #getByteCount()} for a method using a <code>long</code>.
     *
     * @return the number of bytes accumulated
     * @throws ArithmeticException if the byte count is too large
     */
    public int getCount() {
        final long result = getByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int) result;
    }

    /**
     * Set the byte count back to 0.
     * <p>
     * NOTE: From v1.3 this method throws an ArithmeticException if the
     * count is greater than can be expressed by an <code>int</code>.
     * See {@link #resetByteCount()} for a method using a <code>long</code>.
     *
     * @return the count previous to resetting
     * @throws ArithmeticException if the byte count is too large
     */
    public int resetCount() {
        final long result = resetByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int) result;
    }

    /**
     * The number of bytes that have passed through this stream.
     * <p>
     * NOTE: This method is an alternative for <code>getCount()</code>
     * and was added because that method returns an integer which will
     * result in incorrect count for files over 2GB.
     *
     * @return the number of bytes accumulated
     * @since 1.3
     */
    public synchronized long getByteCount() {
        return this.count;
    }

    /**
     * Set the byte count back to 0.
     * <p>
     * NOTE: This method is an alternative for <code>resetCount()</code>
     * and was added because that method returns an integer which will
     * result in incorrect count for files over 2GB.
     *
     * @return the count previous to resetting
     * @since 1.3
     */
    public synchronized long resetByteCount() {
        final long tmp = this.count;
        this.count = 0;
        return tmp;
    }

}
