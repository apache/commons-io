/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.commons.io.IOUtils;

/**
 * A reader proxy which delegates to the wrapped reader.
 * <p>
 * It is an alternative base class to FilterReader
 * to increase reusability, because FilterReader changes the
 * methods being called, such as read(char[]) to read(char[], int, int).
 * </p>
 */
public abstract class ProxyReader extends FilterReader {

    /**
     * Constructs a new ProxyReader.
     *
     * @param delegate  the Reader to delegate to.
     */
    public ProxyReader(final Reader delegate) {
        // the delegate is stored in a protected superclass variable named 'in'
        super(delegate);
    }

    /**
     * Invoked by the read methods after the proxied call has returned
     * successfully. The number of chars returned to the caller (or -1 if
     * the end of stream was reached) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     * </p>
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add post-processing steps also to them.
     * </p>
     *
     * @param n number of chars read, or -1 if the end of stream was reached.
     * @throws IOException if the post-processing fails.
     * @since 2.0
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void afterRead(final int n) throws IOException {
        // noop
    }

    /**
     * Invoked by the read methods before the call is proxied. The number
     * of chars that the caller wanted to read (1 for the {@link #read()}
     * method, buffer length for {@link #read(char[])}, etc.) is given as
     * an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     * </p>
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add pre-processing steps also to them.
     * </p>
     *
     * @param n number of chars that the caller asked to be read.
     * @throws IOException if the pre-processing fails.
     * @since 2.0
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    protected void beforeRead(final int n) throws IOException {
        // noop
    }

    /**
     * Closes the stream and releases any system resources associated with it by invoking the delegate's {@link Reader#close()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        try {
            in.close();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception
     * handling. The default behavior is to re-throw the exception.
     * </p>
     *
     * @param e The IOException thrown.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    protected void handleIOException(final IOException e) throws IOException {
        throw e;
    }

    /**
     * Marks the present position in the stream by invoking the delegate's {@link Reader#mark(int)} method.
     *
     * @param readAheadLimit read ahead limit.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void mark(final int readAheadLimit) throws IOException {
        try {
            in.mark(readAheadLimit);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Tests whether this stream supports the {@link Reader#mark(int)} operation by invoking the delegate's {@link Reader#markSupported()} method.
     *
     * @return true if mark is supported, otherwise false.
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Reads characters into an array by invoking the delegate's {@link Reader#read()} method.
     *
     * @return The character read or -1 if the end of stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        try {
            beforeRead(1);
            final int c = in.read();
            afterRead(c != EOF ? 1 : EOF);
            return c;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's {@code read(char[])} method.
     *
     * @param chr the buffer to read the characters into.
     * @return The number of characters read or -1 if the end of stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final char[] chr) throws IOException {
        try {
            beforeRead(IOUtils.length(chr));
            final int n = in.read(chr);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Reads characters into a portion of an array by invoking the delegate's {@link Reader#read(char[], int, int)} method.
     *
     * @param chr the buffer to read the characters into.
     * @param st The start offset.
     * @param len The number of bytes to read.
     * @return The number of characters read or -1 if the end of stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final char[] chr, final int st, final int len) throws IOException {
        try {
            beforeRead(len);
            final int n = in.read(chr, st, len);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Attempts to read characters into the specified character buffer by invoking the delegate's {@link Reader#read(CharBuffer)} method.
     *
     * @param target the char buffer to read the characters into.
     * @return The number of characters read or -1 if the end of stream.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    @Override
    public int read(final CharBuffer target) throws IOException {
        try {
            beforeRead(IOUtils.length(target));
            final int n = in.read(target);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Tells whether this stream is ready to be read by invoking the delegate's {@link Reader#ready()} method.
     *
     * @return true if the stream is ready to be read.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        try {
            return in.ready();
        } catch (final IOException e) {
            handleIOException(e);
            return false;
        }
    }

    /**
     * Resets the stream by invoking the delegate's {@link Reader#reset()} method.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Sets the underlying reader.
     * <p>
     * Use with caution.
     * </p>
     *
     * @param in The input stream to set in {@code java.io.Reader#in}.
     * @return {@code this} instance.
     * @since 2.22.0
     */
    public ProxyReader setReference(final Reader in) {
        this.in = in;
        return this;
    }


    /**
     * Skips characters by invoking the delegate's {@link Reader#skip(long)} method.
     *
     * @param ln the number of bytes to skip.
     * @return The number of bytes to skipped or EOF if the end of stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long skip(final long ln) throws IOException {
        try {
            return in.skip(ln);
        } catch (final IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    /**
     * Unwraps this instance by returning the underlying {@link Reader}.
     * <p>
     * Use with caution.
     * </p>
     *
     * @return The underlying {@link Reader}.
     * @since 2.22.0
     */
    public Reader unwrap() {
        return in;
    }

}
