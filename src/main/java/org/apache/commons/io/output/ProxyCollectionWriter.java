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
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

/**
 * A Proxy stream collection which acts as expected, that is it passes the method calls on to the proxied streams and
 * doesn't change which methods are being called. It is an alternative base class to {@link FilterWriter} and
 * {@link FilterCollectionWriter} to increase reusability, because FilterWriter changes the methods being called, such
 * as {@code write(char[])} to {@code write(char[], int, int)} and {@code write(String)} to
 * {@code write(String, int, int)}. This is in contrast to {@link ProxyWriter} which is backed by a single
 * {@link Writer}.
 *
 * @since 2.7
 */
public class ProxyCollectionWriter extends FilterCollectionWriter {

    /**
     * Constructs a new proxy collection writer.
     *
     * @param writers Writers object to provide the underlying targets.
     */
    public ProxyCollectionWriter(final Collection<Writer> writers) {
        super(writers);
    }

    /**
     * Constructs a new proxy collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    public ProxyCollectionWriter(final Writer... writers) {
        super(writers);
    }

    /**
     * Invoked by the write methods after the proxied call has returned successfully. The number of chars written (1 for
     * the {@link #write(int)} method, buffer length for {@link #write(char[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing functionality without having to override all
     * the write methods. The default implementation does nothing.
     * </p>
     *
     * @param n number of chars written
     * @throws IOException if the post-processing fails
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void afterWrite(final int n) throws IOException {
        // noop
    }

    /**
     * Invokes the delegates' {@code append(char)} methods.
     *
     * @param c The character to write
     * @return this writer
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    @SuppressWarnings("resource") // Fluent API.
    @Override
    public Writer append(final char c) throws IOException {
        try {
            beforeWrite(1);
            super.append(c);
            afterWrite(1);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegates' {@code append(CharSequence)} methods.
     *
     * @param csq The character sequence to write
     * @return this writer
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("resource") // Fluent API.
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        try {
            final int len = IOUtils.length(csq);
            beforeWrite(len);
            super.append(csq);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegates' {@code append(CharSequence, int, int)} methods.
     *
     * @param csq   The character sequence to write
     * @param start The index of the first character to write
     * @param end   The index of the first character to write (exclusive)
     * @return this writer
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("resource") // Fluent API.
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        try {
            beforeWrite(end - start);
            super.append(csq, start, end);
            afterWrite(end - start);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invoked by the write methods before the call is proxied. The number of chars to be written (1 for the
     * {@link #write(int)} method, buffer length for {@link #write(char[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing functionality without having to override all the
     * write methods. The default implementation does nothing.
     * </p>
     *
     * @param n number of chars to be written
     * @throws IOException if the pre-processing fails
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void beforeWrite(final int n) throws IOException {
        // noop
    }

    /**
     * Invokes the delegate's {@code close()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code flush()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        try {
            super.flush();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception handling. The default behavior is to re-throw the
     * exception.
     * </p>
     *
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs.
     */
    protected void handleIOException(final IOException e) throws IOException {
        throw e;
    }

    /**
     * Invokes the delegate's {@code write(char[])} method.
     *
     * @param cbuf the characters to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final char[] cbuf) throws IOException {
        try {
            final int len = IOUtils.length(cbuf);
            beforeWrite(len);
            super.write(cbuf);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(char[], int, int)} method.
     *
     * @param cbuf the characters to write
     * @param off  The start offset
     * @param len  The number of characters to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        try {
            beforeWrite(len);
            super.write(cbuf, off, len);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(int)} method.
     *
     * @param c the character to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final int c) throws IOException {
        try {
            beforeWrite(1);
            super.write(c);
            afterWrite(1);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(String)} method.
     *
     * @param str the string to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final String str) throws IOException {
        try {
            final int len = IOUtils.length(str);
            beforeWrite(len);
            super.write(str);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's {@code write(String)} method.
     *
     * @param str the string to write
     * @param off The start offset
     * @param len The number of characters to write
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        try {
            beforeWrite(len);
            super.write(str, off, len);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

}
