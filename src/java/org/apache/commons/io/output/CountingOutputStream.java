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
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used in debugging, it counts the number of bytes that pass 
 * through it.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @version $Id: CountingOutputStream.java,v 1.5 2004/02/23 04:40:29 bayard Exp $
 */
public class CountingOutputStream extends ProxyOutputStream {

    private int count;

    /**
     * Constructs a CountingOutputStream.
     * @param out the OutputStream to write to
     */
    public CountingOutputStream( OutputStream out ) {
        super(out);
    }

    /** @see java.io.OutputStream#write(byte[]) */
    public void write(byte[] b) throws IOException {
        count += b.length;
        super.write(b);
    }

    /** @see java.io.OutputStream#write(byte[], int, int) */
    public void write(byte[] b, int off, int len) throws IOException {
        count += len;
        super.write(b, off, len);
    }

    /** @see java.io.OutputStream#write(int) */
    public void write(int b) throws IOException {
        count++;
        super.write(b);
    }

    /**
     * The number of bytes that have passed through this stream.
     * @return the number of bytes accumulated
     */
    public int getCount() {
        return this.count;
    }

}
