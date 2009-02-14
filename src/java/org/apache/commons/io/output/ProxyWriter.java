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
 * @author Stephen Colebourne
 * @version $Id$
 */
public class ProxyWriter extends FilterWriter {

    /**
     * Constructs a new ProxyWriter.
     * 
     * @param proxy  the Writer to delegate to
     */
    public ProxyWriter(Writer proxy) {
        super(proxy);
        // the proxy is stored in a protected superclass variable named 'out'
    }

    /**
     * Invokes the delegate's <code>append(char)</code> method.
     * @param c The character to write
     * @return this writer
     * @throws IOException if an I/O error occurs
     * @since IO 2.0
     */
    @Override
    public Writer append(char c) throws IOException {
        try {
            out.append(c);
        } catch (IOException e) {
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
     * @since IO 2.0
     */
    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        try {
            out.append(csq, start, end);
        } catch (IOException e) {
            handleIOException(e);
        }
        return this;
    }

    /**
     * Invokes the delegate's <code>append(CharSequence)</code> method.
     * @param csq The character sequence to write
     * @return this writer
     * @throws IOException if an I/O error occurs
     * @since IO 2.0
     */
    @Override
    public Writer append(CharSequence csq) throws IOException {
        try {
            out.append(csq);
        } catch (IOException e) {
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
    public void write(int idx) throws IOException {
        try {
            out.write(idx);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(char[])</code> method.
     * @param chr the characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(char[] chr) throws IOException {
        try {
            out.write(chr);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(char[], int, int)</code> method.
     * @param chr the characters to write
     * @param st The start offset
     * @param end The number of characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(char[] chr, int st, int end) throws IOException {
        try {
            out.write(chr, st, end);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(String)</code> method.
     * @param str the string to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(String str) throws IOException {
        try {
            out.write(str);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(String)</code> method.
     * @param str the string to write
     * @param st The start offset
     * @param end The number of characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(String str, int st, int end) throws IOException {
        try {
            out.write(str, st, end);
        } catch (IOException e) {
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            handleIOException(e);
        }
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
