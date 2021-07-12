/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.io.input;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * A {@link BufferedReader} that throws {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @see BufferedReader
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public class UncheckedFilterInputStream extends FilterInputStream {

    /**
     * Creates a {@code UncheckedFilterInputStream}.
     *
     * @param inputStream the underlying input stream, or {@code null} if this instance is to be created without an
     *        underlying stream.
     * @return a new UncheckedFilterInputStream.
     */
    public static UncheckedFilterInputStream on(final InputStream inputStream) {
        return new UncheckedFilterInputStream(inputStream);
    }

    /**
     * Creates a {@code UncheckedFilterInputStream}.
     *
     * @param inputStream the underlying input stream, or {@code null} if this instance is to be created without an
     *        underlying stream.
     */
    public UncheckedFilterInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int available() throws UncheckedIOException {
        try {
            return super.available();
        } catch (final IOException e) {
            throw uncheck(e);
        }
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
    public int read(final byte[] b) throws UncheckedIOException {
        try {
            return super.read(b);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws UncheckedIOException {
        try {
            return super.read(b, off, len);
        } catch (final IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public synchronized void reset() throws UncheckedIOException {
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
