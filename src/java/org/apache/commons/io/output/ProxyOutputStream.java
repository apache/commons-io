/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.IOException;
import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * 
 * A Proxy stream which acts as expected, that is it passes the method 
 * calls on to the proxied stream and doesn't change which methods are 
 * being called. It is an alternative base class to FilterOutputStream
 * to increase reusability.
 */
public class ProxyOutputStream extends FilterOutputStream {

    private OutputStream proxy;

    /**
     * Constructs a new ProxyOutputStream.
     * @param proxy OutputStream to delegate to
     */
    public ProxyOutputStream(OutputStream proxy) {
        super(proxy);
        this.proxy = proxy;
    }

    /** @see java.io.OutputStream#write(int) */
    public void write(int idx) throws IOException {
        this.proxy.write(idx);
    }

    /** @see java.io.OutputStream#write(byte[]) */
    public void write(byte[] bts) throws IOException {
        this.proxy.write(bts);
    }

    /** @see java.io.OutputStream#write(byte[], int, int) */
    public void write(byte[] bts, int st, int end) throws IOException {
        this.proxy.write(bts, st, end);
    }

    /** @see java.io.OutputStream#flush() */
    public void flush() throws IOException {
        this.proxy.flush();
    }

    /** @see java.io.OutputStream#close() */
    public void close() throws IOException {
        this.proxy.close();
    }

}
