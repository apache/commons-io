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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

/**
 * A {@link FilterWriter} that throws {@link UncheckedIOException} instead of {@link IOException}.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 *
 * @see Builder
 * @see FilterWriter
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public final class UncheckedFilterWriter extends FilterWriter {

    // @formatter:off
    /**
     * Builds a new {@link UncheckedFilterWriter}.
     *
     * <p>
     * Using File IO:
     * </p>
     * <pre>{@code
     * UncheckedFilterWriter s = UncheckedFilterWriter.builder()
     *   .setFile(file)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     * <pre>{@code
     * UncheckedFilterWriter s = UncheckedFilterWriter.builder()
     *   .setPath(path)
     *   .get();}
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<UncheckedFilterWriter, Builder> {

        /**
         * Builds a new {@link UncheckedFilterWriter}.
         * <p>
         * You must set input that supports {@link #getWriter()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getWriter()}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a Writer.
         * @see #getWriter()
         */
        @SuppressWarnings("resource")
        @Override
        public UncheckedFilterWriter get() throws IOException {
            return new UncheckedFilterWriter(getWriter());
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
     * Constructs a new filtered writer.
     *
     * @param writer a Writer object providing the underlying stream.
     * @throws NullPointerException if {@code writer} is {@code null}.
     */
    private UncheckedFilterWriter(final Writer writer) {
        super(writer);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(final char c) throws UncheckedIOException {
        return Uncheck.apply(super::append, c);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(final CharSequence csq) throws UncheckedIOException {
        return Uncheck.apply(super::append, csq);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws UncheckedIOException {
        return Uncheck.apply(super::append, csq, start, end);
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
    public void flush() throws UncheckedIOException {
        Uncheck.run(super::flush);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final char[] cbuf) throws UncheckedIOException {
        Uncheck.accept(super::write, cbuf);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws UncheckedIOException {
        Uncheck.accept(super::write, cbuf, off, len);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final int c) throws UncheckedIOException {
        Uncheck.accept(super::write, c);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final String str) throws UncheckedIOException {
        Uncheck.accept(super::write, str);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final String str, final int off, final int len) throws UncheckedIOException {
        Uncheck.accept(super::write, str, off, len);
    }

}
