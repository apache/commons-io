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
 */

package org.apache.commons.io.input;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.function.Uncheck;

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
     * Creates a {@link UncheckedFilterInputStream}.
     *
     * @param inputStream the underlying input stream, or {@code null} if this instance is to be created without an
     *        underlying stream.
     * @return a new UncheckedFilterInputStream.
     */
    public static UncheckedFilterInputStream on(final InputStream inputStream) {
        return new UncheckedFilterInputStream(inputStream);
    }

    /**
     * Creates a {@link UncheckedFilterInputStream}.
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
        return Uncheck.get(super::available);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void close() throws UncheckedIOException {
        Uncheck.run(super::close);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read() throws UncheckedIOException {
        return Uncheck.get(super::read);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final byte[] b) throws UncheckedIOException {
        return Uncheck.apply(super::read, b);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws UncheckedIOException {
        return Uncheck.apply(super::read, b, off, len);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public synchronized void reset() throws UncheckedIOException {
        Uncheck.run(super::reset);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public long skip(final long n) throws UncheckedIOException {
        return Uncheck.apply(super::skip, n);
    }

}
