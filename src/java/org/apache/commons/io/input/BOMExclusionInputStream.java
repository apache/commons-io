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

import java.io.IOException;
import java.io.InputStream;

/**
 *  This class is used to wrap a UTF8-encoded stream that includes an encoded
 *  Byte Order Mark (BOM, 0xFEFF encoded as 0xEF 0xBB 0xBF) as its first bytes.
 *  Such streams are produced by various Microsoft applications. This class
 *  will automatically skip these bytes and return the subsequent byte as the
 *  first byte in the stream.
 *  <p>
 *  If the first byte in the stream is 0xEF, this class will attempt to read
 *  the next two bytes. Results are undefined if the stream does not contain
 *  UTF-8 encoded data, as these next two bytes may not exist.
 *
 * @version $Revision$ $Date$
 * @since Commons IO 2.0
 */
public class BOMExclusionInputStream extends ProxyInputStream {
    private int[] firstBytes;
    private int fbLength;
    private int fbIndex;
    private boolean markedAtStart;

    /**
     * Constructs a new BOM Exclusion InputStream.
     * @param delegate the InputStream to delegate to
     */
    public BOMExclusionInputStream(InputStream delegate) {
        super(delegate);
    }

    /**
     * This method reads and either preserves or skips the first bytes in the
     * stream. It behaves like the single-byte <code>read()</code> method,
     * either returning a valid byte or -1 to indicate that the initial bytes
     * have been processed already.
     * @return the byte read (excluding BOM) or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    private int readFirstBytes() throws IOException {
        if (firstBytes == null) {
            firstBytes = new int[3];
            int b0 = in.read();
            if ((b0 < 0) || (b0 != 0xEF)) {
                return b0;
            }

            int b1 = in.read();
            int b2 = in.read();
            if ((b1 == 0xBB) && (b2 == 0xBF)) {
                return in.read();
            }

            // if the stream isn't valid UTF-8, this is where things get weird
            firstBytes[fbLength++] = b0;
            firstBytes[fbLength++] = b1;
            firstBytes[fbLength++] = b2;
        }

        return (fbIndex < fbLength) ? firstBytes[fbIndex++] : -1;
    }

    //----------------------------------------------------------------------------
    //  Implementation of InputStream
    //----------------------------------------------------------------------------

    /**
     * Invokes the delegate's <code>read()</code> method, skipping BOM.
     * @return the byte read (excluding BOM) or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        int b = readFirstBytes();
        return (b >= 0) ? b : in.read();
    }

    /**
     * Invokes the delegate's <code>read(byte[], int, int)</code> method, skipping BOM.
     * @param buf the buffer to read the bytes into
     * @param off The start offset
     * @param len The number of bytes to read (excluding BOM)
     * @return the number of bytes read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int firstCount = 0;
        int b = 0;
        while ((len > 0) && (b >= 0)) {
            b = readFirstBytes();
            if (b >= 0) {
                buf[off++] = (byte) (b & 0xFF);
                len--;
                firstCount++;
            }
        }
        int secondCount = in.read(buf, off, len);
        return (secondCount < 0) ? firstCount : firstCount + secondCount;
    }

    /**
     * Invokes the delegate's <code>read(byte[])</code> method, skipping BOM.
     * @param buf the buffer to read the bytes into
     * @return the number of bytes read (excluding BOM)
     * or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Invokes the delegate's <code>mark(int)</code> method.
     * @param readlimit read ahead limit
     */
    @Override
    public synchronized void mark(int readlimit) {
        markedAtStart = (firstBytes == null);
        in.mark(readlimit);
    }

    /**
     * Invokes the delegate's <code>reset()</code> method.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void reset() throws IOException {
        if (markedAtStart) {
            firstBytes = null;
        }

        in.reset();
    }

    /**
     * Invokes the delegate's <code>skip(long)</code> method, skipping BOM.
     * @param n the number of bytes to skip
     * @return the number of bytes to skipped or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public long skip(long n) throws IOException {
        while ((n > 0) && (readFirstBytes() >= 0)) {
            n--;
        }
        return in.skip(n);
    }
}
