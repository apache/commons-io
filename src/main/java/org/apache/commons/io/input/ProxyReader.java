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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * A Proxy stream which acts as expected, that is it passes the method
 * calls on to the proxied stream and doesn't change which methods are
 * being called.
 * <p>
 * It is an alternative base class to FilterReader
 * to increase reusability, because FilterReader changes the
 * methods being called, such as read(char[]) to read(char[], int, int).
 *
 */
public abstract class ProxyReader extends FilterReader {

    /**
     * Constructs a new ProxyReader.
     *
     * @param proxy  the Reader to delegate to
     */
    public ProxyReader(final Reader proxy) {
        super(proxy);
        // the proxy is stored in a protected superclass variable named 'in'
    }

    /**
     * Invokes the delegate's <code>read()</code> method.
     * @return the character read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>read(char[])</code> method.
     * @param chr the buffer to read the characters into
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(final char[] chr) throws IOException {
        try {
            beforeRead(chr != null ? chr.length : 0);
            final int n = in.read(chr);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's <code>read(char[], int, int)</code> method.
     * @param chr the buffer to read the characters into
     * @param st The start offset
     * @param len The number of bytes to read
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>read(CharBuffer)</code> method.
     * @param target the char buffer to read the characters into
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    @Override
    public int read(final CharBuffer target) throws IOException {
        try {
            beforeRead(target != null ? target.length() : 0);
            final int n = in.read(target);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's <code>skip(long)</code> method.
     * @param ln the number of bytes to skip
     * @return the number of bytes to skipped or EOF if the end of stream
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>ready()</code> method.
     * @return true if the stream is ready to be read
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>close()</code> method.
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>mark(int)</code> method.
     * @param idx read ahead limit
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void mark(final int idx) throws IOException {
        try {
            in.mark(idx);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>reset()</code> method.
     * @throws IOException if an I/O error occurs
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
     * Invokes the delegate's <code>markSupported()</code> method.
     * @return true if mark is supported, otherwise false
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
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
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add pre-processing steps also to them.
     *
     * @since 2.0
     * @param n number of chars that the caller asked to be read
     * @throws IOException if the pre-processing fails
     */
    protected void beforeRead(final int n) throws IOException {
    }

    /**
     * Invoked by the read methods after the proxied call has returned
     * successfully. The number of chars returned to the caller (or -1 if
     * the end of stream was reached) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the read methods.
     * The default implementation does nothing.
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or
     * {@link #reset()}. You need to explicitly override those methods if
     * you want to add post-processing steps also to them.
     *
     * @since 2.0
     * @param n number of chars read, or -1 if the end of stream was reached
     * @throws IOException if the post-processing fails
     */
    protected void afterRead(final int n) throws IOException {
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception
     * handling. The default behaviour is to re-throw the exception.
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    protected void handleIOException(final IOException e) throws IOException {
        throw e;
    }

}
