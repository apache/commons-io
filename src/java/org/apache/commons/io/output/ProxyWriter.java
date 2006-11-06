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

import java.io.IOException;
import java.io.FilterWriter;
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

    /** @see java.io.Writer#write(int) */
    public void write(int idx) throws IOException {
        out.write(idx);
    }

    /** @see java.io.Writer#write(char[]) */
    public void write(char[] chr) throws IOException {
        out.write(chr);
    }

    /** @see java.io.Writer#write(char[], int, int) */
    public void write(char[] chr, int st, int end) throws IOException {
        out.write(chr, st, end);
    }

    /** @see java.io.Writer#write(String) */
    public void write(String str) throws IOException {
        out.write(str);
    }

    /** @see java.io.Writer#write(String, int, int) */
    public void write(String str, int st, int end) throws IOException {
        out.write(str, st, end);
    }

    /** @see java.io.Writer#flush() */
    public void flush() throws IOException {
        out.flush();
    }

    /** @see java.io.Writer#close() */
    public void close() throws IOException {
        out.close();
    }

}
