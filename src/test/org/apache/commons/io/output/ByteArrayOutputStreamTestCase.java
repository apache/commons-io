/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/output/ByteArrayOutputStreamTestCase.java,v 1.1 2003/04/04 14:03:26 bayard Exp $
 * $Revision: 1.1 $
 * $Date: 2003/04/04 14:03:26 $
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
import java.util.Random;

import junit.framework.TestCase;

/**
 * Basic unit tests for the alternative ByteArrayOutputStream implementation.
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public final class ByteArrayOutputStreamTestCase extends TestCase {

    private static final byte[] DATA;
    
    static {
        DATA = new byte[64];
        for (byte i = 0; i < 64; i++) {
            DATA[i] = i;
        }
    }

    public ByteArrayOutputStreamTestCase(String name) {
        super(name);
    }

    private int writeData(ByteArrayOutputStream baout, 
                java.io.ByteArrayOutputStream ref,
                int count) throws IOException {
        if (count > DATA.length) {
            throw new IllegalArgumentException("Requesting too many bytes");
        }
        if (count == 0) {
            baout.write(100);
            ref.write(100);
            return 1;
        } else {
            baout.write(DATA, 0, count);
            ref.write(DATA, 0, count);
            return count;
        }
    }
    
    private int writeData(ByteArrayOutputStream baout, 
                java.io.ByteArrayOutputStream ref, 
                int[] instructions) throws IOException {
        int written = 0;
        for (int i = 0; i < instructions.length; i++) {
            written += writeData(baout, ref, instructions[i]);
        }
        return written;
    }

    private static final boolean byteCmp(byte[] src, byte[] cmp) {
        for (int i = 0; i < cmp.length; i++) {
            if (src[i] != cmp[i]) {
                return false;
            }
        }
        return true;
    }

    private void checkByteArrays(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            fail("Resulting byte arrays are not equally long");
        }
        if (!byteCmp(expected, actual)) {
            fail("Resulting byte arrays are not equal");
        }
    }

    private void checkStreams(
            ByteArrayOutputStream actual,
            java.io.ByteArrayOutputStream expected) {
        assertEquals("Sizes are not equal", expected.size(), actual.size());
        byte[] buf = actual.toByteArray();
        byte[] refbuf = expected.toByteArray();
        checkByteArrays(buf, refbuf);
    }
              
    public void testStream() throws Exception {
        int written;
        
        //The ByteArrayOutputStream is initialized with 32 bytes to match
        //the original more closely for this test.
        ByteArrayOutputStream baout = new ByteArrayOutputStream(32);
        java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream();
        
        //First three writes
        written = writeData(baout, ref, new int[] {4, 10, 22});
        checkStreams(baout, ref);

        //Another two writes to see if there are any bad effects after toByteArray()
        written = writeData(baout, ref, new int[] {20, 12});
        checkStreams(baout, ref);

        //Now reset the streams        
        baout.reset();
        ref.reset();
        
        //Test again to see if reset() had any bad effects
        written = writeData(baout, ref, new int[] {5, 47, 33, 60, 1, 0, 8});
        checkStreams(baout, ref);
        
        //Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream 
        //and vice-versa to test the writeTo() method.
        ByteArrayOutputStream baout1 = new ByteArrayOutputStream(32);
        ref.writeTo(baout1);
        java.io.ByteArrayOutputStream ref1 = new java.io.ByteArrayOutputStream();
        baout.writeTo(ref1);
        checkStreams(baout1, ref1);
        
        //Testing toString(String)
        String baoutString = baout.toString("ASCII");
        String refString = ref.toString("ASCII");
        assertEquals("ASCII decoded String must be equal", refString, baoutString);
    }
}

