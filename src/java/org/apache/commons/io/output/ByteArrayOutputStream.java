/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * This class implements an output stream in which the data is 
 * written into a byte array. The buffer automatically grows as data 
 * is written to it.
 * <p> 
 * The data can be retrieved using <code>toByteArray()</code> and
 * <code>toString()</code>.
 * <p>
 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an <tt>IOException</tt>.
 * <p>
 * This is an alternative implementation of the java.io.ByteArrayOutputStream
 * class. The original implementation only allocates 32 bytes at the beginning.
 * As this class is designed for heavy duty it starts at 1024 bytes. In contrast
 * to the original it doesn't reallocate the whole memory block but allocates
 * additional buffers. This way no buffers need to be garbage collected and
 * the contents don't have to be copied to the new buffer. This class is
 * designed to behave exactly like the original. The only exception is the
 * deprecated toString(int) method that has been ignored.
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: ByteArrayOutputStream.java,v 1.8 2004/04/24 19:24:09 jeremias Exp $
 */
public class ByteArrayOutputStream extends OutputStream {

    private List buffers = new java.util.ArrayList();
    private int currentBufferIndex;
    private int filledBufferSum;
    private byte[] currentBuffer;
    private int count;

    /**
     * Creates a new byte array output stream. The buffer capacity is 
     * initially 1024 bytes, though its size increases if necessary. 
     */
    public ByteArrayOutputStream() {
        this(1024);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of 
     * the specified size, in bytes. 
     *
     * @param size the initial size.
     * @exception IllegalArgumentException if size is negative.
     */
    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException(
                "Negative initial size: " + size);
        }
        needNewBuffer(size);
    }

    private byte[] getBuffer(int index) {
        return (byte[])buffers.get(index);
    }

    private void needNewBuffer(int newcount) {
        if (currentBufferIndex < buffers.size() - 1) {
            //Recycling old buffer
            filledBufferSum += currentBuffer.length;
            
            currentBufferIndex++;
            currentBuffer = getBuffer(currentBufferIndex);
        } else {
            //Creating new buffer
            int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                    currentBuffer.length << 1, 
                    newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }
            
            currentBufferIndex++;
            currentBuffer = new byte[newBufferSize];
            buffers.add(currentBuffer);
        }
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public synchronized void write(byte[] b, int off, int len) {
        if ((off < 0) 
                || (off > b.length) 
                || (len < 0) 
                || ((off + len) > b.length) 
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        count = newcount;
    }

    /**
     * Calls the write(byte[]) method.
     *
     * @see java.io.OutputStream#write(int)
     */
    public synchronized void write(int b) {
        write(new byte[] {(byte)b}, 0, 1);
    }

    /**
     * @see java.io.ByteArrayOutputStream#size()
     */
    public int size() {
        return count;
    }

    /**
     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * @throws IOException in case an I/O error occurs
     */
    public void close() throws IOException {
        //nop
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    public synchronized void reset() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        currentBuffer = getBuffer(currentBufferIndex);
    }
    
    /**
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        int remaining = count;
        for (int i = 0; i < buffers.size(); i++) {
            byte[] buf = getBuffer(i);
            int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    public synchronized byte toByteArray()[] {
        int remaining = count;
        int pos = 0;
        byte newbuf[] = new byte[count];
        for (int i = 0; i < buffers.size(); i++) {
            byte[] buf = getBuffer(i);
            int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        return newbuf;
    }

    /**
     * @see java.io.ByteArrayOutputStream#toString()
     */
    public String toString() {
        return new String(toByteArray());
    }

    /**
     * @see java.io.ByteArrayOutputStream#toString(String)
     */
    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(toByteArray(), enc);
    }

}
