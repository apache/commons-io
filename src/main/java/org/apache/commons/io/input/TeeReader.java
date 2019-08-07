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
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * Reader proxy that transparently writes a copy of all characters read from the proxied reader to a given Reader. Using
 * {@link #skip(long)} or {@link #mark(int)}/{@link #reset()} on the reader will result on some characters from the
 * reader being skipped or duplicated in the writer.
 * <p>
 * The proxied reader is closed when the {@link #close()} method is called on this proxy. You may configure whether the
 * reader closes the writer.
 * </p>
 *
 * @since 2.7
 */
public class TeeReader extends ProxyReader {

    /**
     * The writer that will receive a copy of all characters read from the proxied reader.
     */
    private final Writer branch;

    /**
     * Flag for closing the associated writer when this reader is closed.
     */
    private final boolean closeBranch;

    /**
     * Creates a TeeReader that proxies the given {@link Reader} and copies all read characters to the given
     * {@link Writer}. The given writer will not be closed when this reader gets closed.
     *
     * @param input  reader to be proxied
     * @param branch writer that will receive a copy of all characters read
     */
    public TeeReader(final Reader input, final Writer branch) {
        this(input, branch, false);
    }

    /**
     * Creates a TeeReader that proxies the given {@link Reader} and copies all read characters to the given
     * {@link Writer}. The given writer will be closed when this reader gets closed if the closeBranch parameter is
     * {@code true}.
     *
     * @param input       reader to be proxied
     * @param branch      writer that will receive a copy of all characters read
     * @param closeBranch flag for closing also the writer when this reader is closed
     */
    public TeeReader(final Reader input, final Writer branch, final boolean closeBranch) {
        super(input);
        this.branch = branch;
        this.closeBranch = closeBranch;
    }

    /**
     * Closes the proxied reader and, if so configured, the associated writer. An exception thrown from the reader will
     * not prevent closing of the writer.
     *
     * @throws IOException if either the reader or writer could not be closed
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
     * Reads a single chracter from the proxied reader and writes it to the associated writer.
     *
     * @return next character from the reader, or -1 if the reader has ended
     * @throws IOException if the reader could not be read (or written)
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
     * Reads characters from the proxied reader and writes the read characters to the associated writer.
     *
     * @param chr character buffer
     * @return number of characters read, or -1 if the reader has ended
     * @throws IOException if the reader could not be read (or written)
     */
    @Override
    public int read(final char[] chr) throws IOException {
        final int n = super.read(chr);
        if (n != EOF) {
            branch.write(chr, 0, n);
        }
        return n;
    }

    /**
     * Reads characters from the proxied reader and writes the read characters to the associated writer.
     *
     * @param chr character buffer
     * @param st  start offset within the buffer
     * @param end maximum number of characters to read
     * @return number of characters read, or -1 if the reader has ended
     * @throws IOException if the reader could not be read (or written)
     */
    @Override
    public int read(final char[] chr, final int st, final int end) throws IOException {
        final int n = super.read(chr, st, end);
        if (n != EOF) {
            branch.write(chr, st, n);
        }
        return n;
    }

    /**
     * Reads characters from the proxied reader and writes the read characters to the associated writer.
     *
     * @param target character buffer
     * @return number of characters read, or -1 if the reader has ended
     * @throws IOException if the reader could not be read (or written)
     */
    @Override
    public int read(final CharBuffer target) throws IOException {
        final int originalPosition = target.position();
        final int n = super.read(target);
        if (n != EOF) {
            // Appending can only be done after resetting the CharBuffer to the
            // right position and limit.
            final int newPosition = target.position();
            final int newLimit = target.limit();
            try {
                target.position(originalPosition).limit(newPosition);
                branch.append(target);
            } finally {
                // Reset the CharBuffer as if the appending never happened.
                target.position(newPosition).limit(newLimit);
            }
        }
        return n;
    }

}
