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

/**
 * A Proxy stream which acts as expected, that is it passes the method
 * calls on to the proxied stream and doesn't change which methods are
 * being called. It is an alternative base class to FilterWriter
 * to increase reusability, because FilterWriter changes the
 * methods being called, such as write(char[]) to write(char[], int, int)
 * and write(String) to write(String, int, int).
 *
 */
public class ProxyWriter extends FilterWriter {

    /**
     * Constructs a new ProxyWriter.
     *
     * @param proxy  the Writer to delegate to
     */
    public ProxyWriter(final Writer proxy) {
        super(proxy);
        // the proxy is stored in a protected superclass variable named 'out'
    }

    /**
     * Invokes the delegate's <code>append(char)</code> method.
     * @param c The character to write
     * @return this writer
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    @Override
    public Writer append(final char c) throws IOException {
        try {
            beforeWrite(1);
            out.append(c);
            afterWrite(1);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegate's <code>append(CharSequence, int, int)</code> method.
     * @param csq The character sequence to write
     * @param start The index of the first character to write
     * @param end  The index of the first character to write (exclusive)
     * @return this writer
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        try {
            beforeWrite(end - start);
            out.append(csq, start, end);
            afterWrite(end - start);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegate's <code>append(CharSequence)</code> method.
     * @param csq The character sequence to write
     * @return this writer
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        try {
            int len = 0;
            if (csq != null) {
                len = csq.length();
            }

            beforeWrite(len);
            out.append(csq);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegate's <code>write(int)</code> method.
     * @param idx the character to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final int idx) throws IOException {
        try {
            beforeWrite(1);
            out.write(idx);
            afterWrite(1);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(char[])</code> method.
     * @param chr the characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final char[] chr) throws IOException {
        try {
            int len = 0;
            if (chr != null) {
                len = chr.length;
            }

            beforeWrite(len);
            out.write(chr);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(char[], int, int)</code> method.
     * @param chr the characters to write
     * @param st The start offset
     * @param len The number of characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final char[] chr, final int st, final int len) throws IOException {
        try {
            beforeWrite(len);
            out.write(chr, st, len);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(String)</code> method.
     * @param str the string to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final String str) throws IOException {
        try {
            int len = 0;
            if (str != null) {
                len = str.length();
            }

            beforeWrite(len);
            out.write(str);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(String)</code> method.
     * @param str the string to write
     * @param st The start offset
     * @param len The number of characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final String str, final int st, final int len) throws IOException {
        try {
            beforeWrite(len);
            out.write(str, st, len);
            afterWrite(len);
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>flush()</code> method.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>close()</code> method.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            out.close();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invoked by the write methods before the call is proxied. The number
     * of chars to be written (1 for the {@link #write(int)} method, buffer
     * length for {@link #write(char[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing
     * functionality without having to override all the write methods.
     * The default implementation does nothing.
     *
     * @since 2.0
     * @param n number of chars to be written
     * @throws IOException if the pre-processing fails
     */
    protected void beforeWrite(final int n) throws IOException {
    }

    /**
     * Invoked by the write methods after the proxied call has returned
     * successfully. The number of chars written (1 for the
     * {@link #write(int)} method, buffer length for {@link #write(char[])},
     * etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing
     * functionality without having to override all the write methods.
     * The default implementation does nothing.
     *
     * @since 2.0
     * @param n number of chars written
     * @throws IOException if the post-processing fails
     */
    protected void afterWrite(final int n) throws IOException {
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
