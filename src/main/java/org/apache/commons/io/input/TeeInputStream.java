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
import java.io.OutputStream;

/**
 * InputStream proxy that transparently writes a copy of all bytes read
 * from the proxied stream to a given OutputStream. Using {@link #skip(long)}
 * or {@link #mark(int)}/{@link #reset()} on the stream will result on some
 * bytes from the input stream being skipped or duplicated in the output
 * stream.
 * <p>
 * The proxied input stream is closed when the {@link #close()} method is
 * called on this proxy. You may configure whether the input stream closes the
 * output stream.
 * </p>
 *
 * @since 1.4
 * @see ObservableInputStream
 */
public class TeeInputStream extends ProxyInputStream {

    /**
     * The output stream that will receive a copy of all bytes read from the
     * proxied input stream.
     */
    private final OutputStream branch;

    /**
     * Flag for closing the associated output stream when this stream is closed.
     */
    private final boolean closeBranch;

    /**
     * Creates a TeeInputStream that proxies the given {@link InputStream}
     * and copies all read bytes to the given {@link OutputStream}. The given
     * output stream will not be closed when this stream gets closed.
     *
     * @param input input stream to be proxied
     * @param branch output stream that will receive a copy of all bytes read
     */
    public TeeInputStream(final InputStream input, final OutputStream branch) {
        this(input, branch, false);
    }

    /**
     * Creates a TeeInputStream that proxies the given {@link InputStream}
     * and copies all read bytes to the given {@link OutputStream}. The given
     * output stream will be closed when this stream gets closed if the
     * closeBranch parameter is {@code true}.
     *
     * @param input input stream to be proxied
     * @param branch output stream that will receive a copy of all bytes read
     * @param closeBranch flag for closing also the output stream when this
     *                    stream is closed
     */
    public TeeInputStream(
            final InputStream input, final OutputStream branch, final boolean closeBranch) {
        super(input);
        this.branch = branch;
        this.closeBranch = closeBranch;
    }

    /**
     * Closes the proxied input stream and, if so configured, the associated
     * output stream. An exception thrown from one stream will not prevent
     * closing of the other stream.
     *
     * @throws IOException if either of the streams could not be closed
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (closeBranch) {
                branch.close();
            }
        }
    }

    /**
     * Reads a single byte from the proxied input stream and writes it to
     * the associated output stream.
     *
     * @return next byte from the stream, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written)
     */
    @Override
    public int read() throws IOException {
        final int ch = super.read();
        if (ch != EOF) {
            branch.write(ch);
        }
        return ch;
    }

    /**
     * Reads bytes from the proxied input stream and writes the read bytes
     * to the associated output stream.
     *
     * @param bts byte buffer
     * @param st start offset within the buffer
     * @param end maximum number of bytes to read
     * @return number of bytes read, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written)
     */
    @Override
    public int read(final byte[] bts, final int st, final int end) throws IOException {
        final int n = super.read(bts, st, end);
        if (n != EOF) {
            branch.write(bts, st, n);
        }
        return n;
    }

    /**
     * Reads bytes from the proxied input stream and writes the read bytes
     * to the associated output stream.
     *
     * @param bts byte buffer
     * @return number of bytes read, or -1 if the stream has ended
     * @throws IOException if the stream could not be read (or written)
     */
    @Override
    public int read(final byte[] bts) throws IOException {
        final int n = super.read(bts);
        if (n != EOF) {
            branch.write(bts, 0, n);
        }
        return n;
    }

}
