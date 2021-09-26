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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * A {@link FilterOutputStream} that throws {@link UncheckedIOException} instead of {@link UncheckedIOException}.
 *
 * @see FilterOutputStream
 * @see UncheckedIOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public class UncheckedFilterOutputStream extends FilterOutputStream {

    /**
     * Creates a new instance.
     *
     * @param outputStream an OutputStream object providing the underlying stream.
     * @return a new UncheckedFilterOutputStream.
     */
    public static UncheckedFilterOutputStream on(final OutputStream outputStream) {
        return new UncheckedFilterOutputStream(outputStream);
    }

    /**
     * Creates an output stream filter built on top of the specified underlying output stream.
     *
     * @param outputStream the underlying output stream, or {@code null} if this instance is to be created without an
     *        underlying stream.
     */
    public UncheckedFilterOutputStream(final OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void close() throws UncheckedIOException {
        try {
            super.close();
        } catch (final IOException e) {
            uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void flush() throws UncheckedIOException {
        try {
            super.flush();
        } catch (final IOException e) {
            uncheck(e);
        }
    }

    private void uncheck(final IOException e) {
        throw new UncheckedIOException(e);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final byte[] b) throws UncheckedIOException {
        try {
            super.write(b);
        } catch (final IOException e) {
            uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws UncheckedIOException {
        try {
            super.write(b, off, len);
        } catch (final IOException e) {
            uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(final int b) throws UncheckedIOException {
        try {
            super.write(b);
        } catch (final IOException e) {
            uncheck(e);
        }
    }

}
