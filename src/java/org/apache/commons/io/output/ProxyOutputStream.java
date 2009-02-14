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

/**
 * A Proxy stream which acts as expected, that is it passes the method 
 * calls on to the proxied stream and doesn't change which methods are 
 * being called. It is an alternative base class to FilterOutputStream
 * to increase reusability.
 * 
 * @author Stephen Colebourne
 * @version $Id$
 */
public class ProxyOutputStream extends FilterOutputStream {

    /**
     * Constructs a new ProxyOutputStream.
     * 
     * @param proxy  the OutputStream to delegate to
     */
    public ProxyOutputStream(OutputStream proxy) {
        super(proxy);
        // the proxy is stored in a protected superclass variable named 'out'
    }

    /**
     * Invokes the delegate's <code>write(int)</code> method.
     * @param idx the byte to write
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
     * Invokes the delegate's <code>write(byte[])</code> method.
     * @param bts the bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts) throws IOException {
        try {
            out.write(bts);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>write(byte[])</code> method.
     * @param bts the bytes to write
     * @param st The start offset
     * @param end The number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(byte[] bts, int st, int end) throws IOException {
        try {
            out.write(bts, st, end);
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
