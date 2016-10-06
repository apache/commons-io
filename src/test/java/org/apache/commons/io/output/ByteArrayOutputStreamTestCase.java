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

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Basic unit tests for the alternative ByteArrayOutputStream implementation.
 */
public class ByteArrayOutputStreamTestCase {

    private static final byte[] DATA;

    static {
        DATA = new byte[64];
        for (byte i = 0; i < 64; i++) {
            DATA[i] = i;
        }
    }

    private int writeData(final ByteArrayOutputStream baout,
                final java.io.ByteArrayOutputStream ref,
                final int count) {
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

    private int writeData(final ByteArrayOutputStream baout,
                final java.io.ByteArrayOutputStream ref,
                final int[] instructions) {
        int written = 0;
        for (final int instruction : instructions) {
            written += writeData(baout, ref, instruction);
        }
        return written;
    }

    private static boolean byteCmp(final byte[] src, final byte[] cmp) {
        for (int i = 0; i < cmp.length; i++) {
            if (src[i] != cmp[i]) {
                return false;
            }
        }
        return true;
    }

    private void checkByteArrays(final byte[] expected, final byte[] actual) {
        if (expected.length != actual.length) {
            fail("Resulting byte arrays are not equally long");
        }
        if (!byteCmp(expected, actual)) {
            fail("Resulting byte arrays are not equal");
        }
    }

    private void checkStreams(
            final ByteArrayOutputStream actual,
            final java.io.ByteArrayOutputStream expected) {
        assertEquals("Sizes are not equal", expected.size(), actual.size());
        final byte[] buf = actual.toByteArray();
        final byte[] refbuf = expected.toByteArray();
        checkByteArrays(buf, refbuf);
    }

    @Test
    public void testToInputStream() throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream();

        //Write 8224 bytes
        writeData(baout, ref, 32);
        for(int i=0;i<128;i++) {
            writeData(baout, ref, 64);
        }

        //Get data before more writes
        InputStream in = baout.toInputStream();
        byte refData[] = ref.toByteArray();

        //Write some more data
        writeData(baout, ref, new int[] { 2, 4, 8, 16 });

        //Check original data
        byte baoutData[] = IOUtils.toByteArray(in);
        assertEquals(8224, baoutData.length);
        checkByteArrays(refData, baoutData);

        //Check all data written
        baoutData = IOUtils.toByteArray(baout.toInputStream());
        refData = ref.toByteArray();
        assertEquals(8254, baoutData.length);
        checkByteArrays(refData, baoutData);
        baout.close();
        in.close();
    }

    @Test
    public void testToInputStreamWithReset() throws IOException {
        //Make sure reset() do not destroy InputStream returned from toInputStream()
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream();

        //Write 8224 bytes
        writeData(baout, ref, 32);
        for(int i=0;i<128;i++) {
            writeData(baout, ref, 64);
        }

        //Get data before reset
        InputStream in = baout.toInputStream();
        byte refData[] = ref.toByteArray();

        //Reset and write some new data
        baout.reset();
        ref.reset();
        writeData(baout, ref, new int[] { 2, 4, 8, 16 });

        //Check original data
        byte baoutData[] = IOUtils.toByteArray(in);
        assertEquals(8224, baoutData.length);
        checkByteArrays(refData, baoutData);

        //Check new data written after reset
        baoutData = IOUtils.toByteArray(baout.toInputStream());
        refData = ref.toByteArray();
        assertEquals(30, baoutData.length);
        checkByteArrays(refData, baoutData);
        baout.close();
        in.close();
    }

    @Test
    public void testStream() throws Exception {
        int written;

        //The ByteArrayOutputStream is initialized with 32 bytes to match
        //the original more closely for this test.
        final ByteArrayOutputStream baout = new ByteArrayOutputStream(32);
        final java.io.ByteArrayOutputStream ref = new java.io.ByteArrayOutputStream();

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

        //Test the readFrom(InputStream) method
        baout.reset();
        written = baout.write(new ByteArrayInputStream(ref.toByteArray()));
        assertEquals(155, written);
        checkStreams(baout, ref);

        //Write the commons Byte[]OutputStream to a java.io.Byte[]OutputStream
        //and vice-versa to test the writeTo() method.
        final ByteArrayOutputStream baout1 = new ByteArrayOutputStream(32);
        ref.writeTo(baout1);
        final java.io.ByteArrayOutputStream ref1 = new java.io.ByteArrayOutputStream();
        baout.writeTo(ref1);
        checkStreams(baout1, ref1);

        //Testing toString(String)
        final String baoutString = baout.toString("ASCII");
        final String refString = ref.toString("ASCII");
        assertEquals("ASCII decoded String must be equal", refString, baoutString);

        //Make sure that empty ByteArrayOutputStreams really don't create garbage
        //on toByteArray()
        final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        assertSame(baos1.toByteArray(), baos2.toByteArray());
        baos1.close();
        baos2.close();
        baout.close();
        baout1.close();
    }
}

