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
 * @author Stephen Colebourne
 * @version $Id$
 */
public abstract class ProxyReader extends FilterReader {

    /**
     * Constructs a new ProxyReader.
     * 
     * @param proxy  the Reader to delegate to
     */
    public ProxyReader(Reader proxy) {
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
            return in.read();
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    /**
     * Invokes the delegate's <code>read(char[])</code> method.
     * @param chr the buffer to read the characters into
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(char[] chr) throws IOException {
        try {
            return in.read(chr);
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    /**
     * Invokes the delegate's <code>read(char[], int, int)</code> method.
     * @param chr the buffer to read the characters into
     * @param st The start offset
     * @param end The number of bytes to read
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(char[] chr, int st, int end) throws IOException {
        try {
            return in.read(chr, st, end);
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    /**
     * Invokes the delegate's <code>read(CharBuffer)</code> method.
     * @param target the char buffer to read the characters into
     * @return the number of characters read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     * @since IO 2.0
     */
    @Override
    public int read(CharBuffer target) throws IOException {
        try {
            return in.read(target);
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    /**
     * Invokes the delegate's <code>skip(long)</code> method.
     * @param ln the number of bytes to skip
     * @return the number of bytes to skipped or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public long skip(long ln) throws IOException {
        try {
            return in.skip(ln);
        } catch (IOException e) {
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>mark(int)</code> method.
     * @param idx read ahead limit
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void mark(int idx) throws IOException {
        try {
            in.mark(idx);
        } catch (IOException e) {
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
        } catch (IOException e) {
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
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception
     * handling. The default behaviour is to re-throw the exception.
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs
     * @since Commons IO 2.0
     */
    protected void handleIOException(IOException e) throws IOException {
        throw e;
    }

}
