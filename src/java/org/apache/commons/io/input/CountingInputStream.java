/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used in debugging, it counts the number of bytes that pass 
 * through it.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @version $Id: CountingInputStream.java,v 1.8 2004/02/23 04:38:52 bayard Exp $
 */
public class CountingInputStream extends ProxyInputStream {

    private int count;

    /**
     * Constructs a new CountingInputStream.
     * @param in InputStream to delegate to
     */
    public CountingInputStream( InputStream in ) {
        super(in);
    }

    /**
     * Increases the count by super.read(b)'s return count
     * 
     * @see java.io.InputStream#read(byte[]) 
     */
    public int read(byte[] b) throws IOException {
        int found = super.read(b);
        this.count += found;
        return found;
    }

    /**
     * Increases the count by super.read(b, off, len)'s return count
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int found = super.read(b, off, len);
        this.count += found;
        return found;
    }

    /**
     * Increases the count by 1. 
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        this.count++;
        return super.read();
    }

    /**
     * The number of bytes that have passed through this stream.
     *
     * @return the number of bytes accumulated
     */
    public int getCount() {
        return this.count;
    }

}
