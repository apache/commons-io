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

import junit.framework.TestCase;

/**
 * Basic unit tests for the alternative ByteArrayOutputStream implementation.
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class ByteArrayOutputStreamTestCase extends TestCase {

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

    private static boolean byteCmp(byte[] src, byte[] cmp) {
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
        assertEquals(36, written);
        checkStreams(baout, ref);

        //Another two writes to see if there are any bad effects after toByteArray()
        written = writeData(baout, ref, new int[] {20, 12});
        assertEquals(32, written);
        checkStreams(baout, ref);

        //Now reset the streams        
        baout.reset();
        ref.reset();
        
        //Test again to see if reset() had any bad effects
        written = writeData(baout, ref, new int[] {5, 47, 33, 60, 1, 0, 8});
        assertEquals(155, written);
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
        
        //Make sure that empty ByteArrayOutputStreams really don't create garbage
        //on toByteArray()
        assertSame(new ByteArrayOutputStream().toByteArray(),
            new ByteArrayOutputStream().toByteArray());
    }
}

