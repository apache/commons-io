/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/java/org/apache/commons/io/output/ByteArrayOutputStream.java,v 1.1 2003/04/04 14:03:25 bayard Exp $
 * $Revision: 1.1 $
 * $Date: 2003/04/04 14:03:25 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *    permission of the Apache Group.
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
 *
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
 * @version $Id: ByteArrayOutputStream.java,v 1.1 2003/04/04 14:03:25 bayard Exp $
 */
public class ByteArrayOutputStream extends OutputStream {

    private static final boolean DEBUG = false;

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

    private int getCurrentCapacity() {
        return filledBufferSum + currentBuffer.length;
    }
    
    private void needNewBuffer(int newcount) {
        if (DEBUG) System.out.println("Need new buffer: newcount=" + newcount
                + " curBufIdx=" + currentBufferIndex
                + " buffers=" + buffers.size());
        if (currentBufferIndex < buffers.size() - 1) {
            filledBufferSum += currentBuffer.length;
            
            currentBufferIndex++;
            currentBuffer = getBuffer(currentBufferIndex);
            if (DEBUG) System.out.println("-->Recycling old buffer: size=" 
                        + currentBuffer.length);
        } else {
            final int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                    currentBuffer.length << 1, 
                    newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }
            
            if (DEBUG) System.out.println("-->Adding new buffer: size=" + newBufferSize);
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
        if (DEBUG) System.out.println("------------------write("+len+" bytes)");
        int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            if (DEBUG) System.out.println("Writing " + part 
                    + " bytes at pos " + inBufferPos + " of buffer "
                    + currentBuffer + " len=" + currentBuffer.length
                    + " idx=" + currentBufferIndex);
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
            final byte[] buf = getBuffer(i);
            final int c = Math.min(buf.length, remaining);
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
        final byte newbuf[] = new byte[count];
        for (int i = 0; i < buffers.size(); i++) {
            final byte[] buf = getBuffer(i);
            final int c = Math.min(buf.length, remaining);
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
