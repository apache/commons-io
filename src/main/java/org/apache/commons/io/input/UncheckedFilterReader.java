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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

/**
 * A {@link FilterReader} that throws {@link UncheckedIOException} instead of {@link IOException}.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @see FilterReader
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public final class UncheckedFilterReader extends FilterReader {

    // @formatter:off
    /**
     * Builds a new {@link UncheckedFilterReader}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UncheckedFilterReader s = UncheckedFilterReader.builder()
     *   .setFile(file)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UncheckedFilterReader s = UncheckedFilterReader.builder()
     *   .setPath(path)
     *   .get();}
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UncheckedFilterReader, Builder> {

        /**
         * Builds a new {@link UncheckedFilterReader}.
         * <p>
         * You must set input that supports {@link #getReader()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getReader()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a Reader.
         * @throws IllegalStateException if the {@code origin} is {@code null}.
         * @see #getReader()
         */
        @Override
        public UncheckedFilterReader get() {
            // This an unchecked class, so this method is as well.
            return Uncheck.get(() -> new UncheckedFilterReader(getReader()));
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs a new filtered reader.
     *
     * @param reader a Reader object providing the underlying stream.
     * @throws NullPointerException if {@code reader} is {@code null}.
     */
    private UncheckedFilterReader(final Reader reader) {
        super(reader);
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
    public void mark(final int readAheadLimit) throws UncheckedIOException {
        Uncheck.accept(super::mark, readAheadLimit);
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
    public int read(final char[] cbuf) throws UncheckedIOException {
        return Uncheck.apply(super::read, cbuf);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws UncheckedIOException {
        return Uncheck.apply(super::read, cbuf, off, len);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public int read(final CharBuffer target) throws UncheckedIOException {
        return Uncheck.apply(super::read, target);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public boolean ready() throws UncheckedIOException {
        return Uncheck.get(super::ready);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void reset() throws UncheckedIOException {
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
