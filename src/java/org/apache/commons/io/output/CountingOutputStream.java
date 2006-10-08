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
import java.io.OutputStream;

/**
 * Used in debugging, it counts the number of bytes that pass 
 * through it.
 *
 * @author <a href="mailto:bayard@apache.org">Henri Yandell</a>
 * @version $Id$
 */
public class CountingOutputStream extends ProxyOutputStream {

    private long count;

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
     * <p>
     * <strong>WARNING</strong> This method will return an
     * incorrect count for files over 2GB - use
     * <code>getByteCount()</code> instead.
     *
     * @return the number of bytes accumulated
     * @deprecated use <code>getByteCount()</code> - see issue IO-84
     */
    public int getCount() {
        return (int)getByteCount();
    }

    /** 
      * Set the count back to 0. 
      * <p>
      * <strong>WARNING</strong> This method will return an
      * incorrect count for files over 2GB - use
      * <code>resetByteCount()</code> instead.
      *
      * @return the count previous to resetting.
      * @deprecated use <code>resetByteCount()</code> - see issue IO-84
      */
    public synchronized int resetCount() {
        return (int)resetByteCount();
    }

    /**
     * The number of bytes that have passed through this stream.
     * <p>
     * <strong>N.B.</strong> This method was introduced as an
     * alternative for the <code>getCount()</code> method
     * because that method returns an integer which will result
     * in incorrect count for files over 2GB being returned.
     *
     * @return the number of bytes accumulated
     */
    public long getByteCount() {
        return this.count;
    }

    /** 
     * Set the count back to 0. 
     * <p>
     * <strong>N.B.</strong> This method was introduced as an
     * alternative for the <code>resetCount()</code> method
     * because that method returns an integer which will result
     * in incorrect count for files over 2GB being returned.
     *
     * @return the count previous to resetting.
     */
    public synchronized long resetByteCount() {
        long tmp = this.count;
        this.count = 0;
        return tmp;
    }
}
