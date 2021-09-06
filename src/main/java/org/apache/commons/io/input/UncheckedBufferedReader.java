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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;

/**
 * A {@link BufferedReader} that throws {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @see BufferedReader
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public class UncheckedBufferedReader extends BufferedReader {

    /**
     * Creates a new buffered reader.
     *
     * @param reader a Reader object providing the underlying stream.
     * @return a new UncheckedBufferedReader.
     * @throws NullPointerException if {@code reader} is {@code null}.
     */
    public static UncheckedBufferedReader on(final Reader reader) {
        return new UncheckedBufferedReader(reader);
    }

    /**
     * Creates a buffering character-input stream that uses a default-sized input buffer.
     *
     * @param reader A Reader
     */
    public UncheckedBufferedReader(final Reader reader) {
        super(reader);
    }

    /**
     * Creates a buffering character-input stream that uses an input buffer of the specified size.
     *
     * @param reader     A Reader
     * @param bufferSize Input-buffer size
     *
     * @throws IllegalArgumentException If {@code bufferSize <= 0}
     */
    public UncheckedBufferedReader(final Reader reader, final int bufferSize) {
        super(reader, bufferSize);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void close() throws UncheckedIOException {
        try {
            super.close();
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void mark(final int readAheadLimit) throws UncheckedIOException {
        try {
            super.mark(readAheadLimit);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read() throws UncheckedIOException {
        try {
            return super.read();
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final char[] cbuf) throws UncheckedIOException {
        try {
            return super.read(cbuf);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws UncheckedIOException {
        try {
            return super.read(cbuf, off, len);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final CharBuffer target) throws UncheckedIOException {
        try {
            return super.read(target);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public String readLine() throws UncheckedIOException {
        try {
            return super.readLine();
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public boolean ready() throws UncheckedIOException {
        try {
            return super.ready();
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void reset() throws UncheckedIOException {
        try {
            super.reset();
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public long skip(final long n) throws UncheckedIOException {
        try {
            return super.skip(n);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    private UncheckedIOException uncheck(final IOException e) {
        return new UncheckedIOException(e);
    }

}
