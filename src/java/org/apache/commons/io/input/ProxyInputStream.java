/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Proxy stream which acts as expected, that is it passes the method 
 * calls on to the proxied stream and doesn't change which methods are 
 * being called. It is an alternative base class to FilterInputStream
 * to increase reusability.
 */
public abstract class ProxyInputStream extends FilterInputStream {

    private InputStream proxy;

    /**
     * Constructs a new ProxyInputStream.
     * @param proxy InputStream to delegate to
     */
    public ProxyInputStream(InputStream proxy) {
        super(proxy);
        this.proxy = proxy;
    }

    /** @see java.io.InputStream#read() */
    public int read() throws IOException {
        return this.proxy.read();
    }

    /** @see java.io.InputStream#read(byte[]) */
    public int read(byte[] bts) throws IOException {
        return this.proxy.read(bts);
    }

    /** @see java.io.InputStream#read(byte[], int, int) */
    public int read(byte[] bts, int st, int end) throws IOException {
        return this.proxy.read(bts, st, end);
    }

    /** @see java.io.InputStream#skip(long) */
    public long skip(long ln) throws IOException {
        return this.proxy.skip(ln);
    }

    /** @see java.io.InputStream#available() */
    public int available() throws IOException {
        return this.proxy.available();
    }

    /** @see java.io.InputStream#close() */
    public void close() throws IOException {
        this.proxy.close();
    }

    /** @see java.io.InputStream#mark(int) */
    public synchronized void mark(int idx) {
        this.proxy.mark(idx);
    }

    /** @see java.io.InputStream#reset() */
    public synchronized void reset() throws IOException {
        this.proxy.reset();
    }

    /** @see java.io.InputStream#markSupported() */
    public boolean markSupported() {
        return this.proxy.markSupported();
    }

}
