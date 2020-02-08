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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 *
 */
public class EndianUtilsTest  {

    @Test
    public void testCtor() {
        new EndianUtils();
        // Constructor does not blow up.
    }

    @Test
    public void testEOFException() throws IOException {
        final ByteArrayInputStream input = new ByteArrayInputStream(new byte[] {});
        try {
            EndianUtils.readSwappedDouble(input);
            fail("Expected EOFException");
        } catch (final EOFException e) {
            // expected
        }
    }

    @Test
    public void testSwapShort() {
        assertEquals( (short) 0, EndianUtils.swapShort( (short) 0 ) );
        assertEquals( (short) 0x0201, EndianUtils.swapShort( (short) 0x0102 ) );
        assertEquals( (short) 0xffff, EndianUtils.swapShort( (short) 0xffff ) );
        assertEquals( (short) 0x0102, EndianUtils.swapShort( (short) 0x0201 ) );
    }

    @Test
    public void testSwapInteger() {
        assertEquals( 0, EndianUtils.swapInteger( 0 ) );
        assertEquals( 0x04030201, EndianUtils.swapInteger( 0x01020304 ) );
        assertEquals( 0x01000000, EndianUtils.swapInteger( 0x00000001 ) );
        assertEquals( 0x00000001, EndianUtils.swapInteger( 0x01000000 ) );
        assertEquals( 0x11111111, EndianUtils.swapInteger( 0x11111111 ) );
        assertEquals( 0xabcdef10, EndianUtils.swapInteger( 0x10efcdab ) );
        assertEquals( 0xab, EndianUtils.swapInteger( 0xab000000 ) );
    }

    @Test
    public void testSwapLong() {
        assertEquals( 0, EndianUtils.swapLong( 0 ) );
        assertEquals( 0x0807060504030201L, EndianUtils.swapLong( 0x0102030405060708L ) );
        assertEquals( 0xffffffffffffffffL, EndianUtils.swapLong( 0xffffffffffffffffL ) );
        assertEquals( 0xab, EndianUtils.swapLong( 0xab00000000000000L ) );
    }

    @Test
    public void testSwapFloat() {
        assertEquals( 0.0f, EndianUtils.swapFloat( 0.0f ), 0.0 );
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        final float f2 = Float.intBitsToFloat( 0x04030201 );
        assertEquals( f2, EndianUtils.swapFloat( f1 ), 0.0 );
    }

    @Test
    public void testSwapDouble() {
        assertEquals( 0.0, EndianUtils.swapDouble( 0.0 ), 0.0 );
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        final double d2 = Double.longBitsToDouble( 0x0807060504030201L );
        assertEquals( d2, EndianUtils.swapDouble( d1 ), 0.0 );
    }

    /**
     * Tests all swapXxxx methods for symmetry when going from one endian
     * to another and back again.
     */
    @Test
    public void testSymmetry() {
        assertEquals( (short) 0x0102, EndianUtils.swapShort( EndianUtils.swapShort( (short) 0x0102 ) ) );
        assertEquals( 0x01020304, EndianUtils.swapInteger( EndianUtils.swapInteger( 0x01020304 ) ) );
        assertEquals( 0x0102030405060708L, EndianUtils.swapLong( EndianUtils.swapLong( 0x0102030405060708L ) ) );
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        assertEquals( f1, EndianUtils.swapFloat( EndianUtils.swapFloat( f1 ) ), 0.0 );
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        assertEquals( d1, EndianUtils.swapDouble( EndianUtils.swapDouble( d1 ) ), 0.0 );
    }

    @Test
    public void testReadSwappedShort() throws IOException {
        final byte[] bytes = new byte[] { 0x02, 0x01 };
        assertEquals( 0x0102, EndianUtils.readSwappedShort( bytes, 0 ) );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( 0x0102, EndianUtils.readSwappedShort( input ) );
    }

    @Test
    public void testWriteSwappedShort() throws IOException {
        byte[] bytes = new byte[2];
        EndianUtils.writeSwappedShort( bytes, 0, (short) 0x0102 );
        assertEquals( 0x02, bytes[0] );
        assertEquals( 0x01, bytes[1] );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
        EndianUtils.writeSwappedShort( baos, (short) 0x0102 );
        bytes = baos.toByteArray();
        assertEquals( 0x02, bytes[0] );
        assertEquals( 0x01, bytes[1] );
    }

    @Test
    public void testReadSwappedUnsignedShort() throws IOException {
        final byte[] bytes = new byte[] { 0x02, 0x01 };
        assertEquals( 0x00000102, EndianUtils.readSwappedUnsignedShort( bytes, 0 ) );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( 0x00000102, EndianUtils.readSwappedUnsignedShort( input ) );
    }

    @Test
    public void testReadSwappedInteger() throws IOException {
        final byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        assertEquals( 0x01020304, EndianUtils.readSwappedInteger( bytes, 0 ) );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( 0x01020304, EndianUtils.readSwappedInteger( input ) );
    }

    @Test
    public void testWriteSwappedInteger() throws IOException {
        byte[] bytes = new byte[4];
        EndianUtils.writeSwappedInteger( bytes, 0, 0x01020304 );
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        EndianUtils.writeSwappedInteger( baos, 0x01020304 );
        bytes = baos.toByteArray();
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );
    }

    @Test
    public void testReadSwappedUnsignedInteger() throws IOException {
        final byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        assertEquals( 0x0000000001020304L, EndianUtils.readSwappedUnsignedInteger( bytes, 0 ) );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( 0x0000000001020304L, EndianUtils.readSwappedUnsignedInteger( input ) );
    }

    @Test
    public void testReadSwappedLong() throws IOException {
        final byte[] bytes = new byte[] { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        assertEquals( 0x0102030405060708L, EndianUtils.readSwappedLong( bytes, 0 ) );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( 0x0102030405060708L, EndianUtils.readSwappedLong( input ) );
    }

    @Test
    public void testWriteSwappedLong() throws IOException {
        byte[] bytes = new byte[8];
        EndianUtils.writeSwappedLong( bytes, 0, 0x0102030405060708L );
        assertEquals( 0x08, bytes[0] );
        assertEquals( 0x07, bytes[1] );
        assertEquals( 0x06, bytes[2] );
        assertEquals( 0x05, bytes[3] );
        assertEquals( 0x04, bytes[4] );
        assertEquals( 0x03, bytes[5] );
        assertEquals( 0x02, bytes[6] );
        assertEquals( 0x01, bytes[7] );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        EndianUtils.writeSwappedLong( baos, 0x0102030405060708L );
        bytes = baos.toByteArray();
        assertEquals( 0x08, bytes[0] );
        assertEquals( 0x07, bytes[1] );
        assertEquals( 0x06, bytes[2] );
        assertEquals( 0x05, bytes[3] );
        assertEquals( 0x04, bytes[4] );
        assertEquals( 0x03, bytes[5] );
        assertEquals( 0x02, bytes[6] );
        assertEquals( 0x01, bytes[7] );
    }

    @Test
    public void testReadSwappedFloat() throws IOException {
        final byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        final float f2 = EndianUtils.readSwappedFloat( bytes, 0 );
        assertEquals( f1, f2, 0.0 );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( f1, EndianUtils.readSwappedFloat( input ), 0.0 );
    }

    @Test
    public void testWriteSwappedFloat() throws IOException {
        byte[] bytes = new byte[4];
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        EndianUtils.writeSwappedFloat( bytes, 0, f1 );
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        EndianUtils.writeSwappedFloat( baos, f1 );
        bytes = baos.toByteArray();
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );
    }

    @Test
    public void testReadSwappedDouble() throws IOException {
        final byte[] bytes = new byte[] { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        final double d2 = EndianUtils.readSwappedDouble( bytes, 0 );
        assertEquals( d1, d2, 0.0 );

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertEquals( d1, EndianUtils.readSwappedDouble( input ), 0.0 );
    }

    @Test
    public void testWriteSwappedDouble() throws IOException {
        byte[] bytes = new byte[8];
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        EndianUtils.writeSwappedDouble( bytes, 0, d1 );
        assertEquals( 0x08, bytes[0] );
        assertEquals( 0x07, bytes[1] );
        assertEquals( 0x06, bytes[2] );
        assertEquals( 0x05, bytes[3] );
        assertEquals( 0x04, bytes[4] );
        assertEquals( 0x03, bytes[5] );
        assertEquals( 0x02, bytes[6] );
        assertEquals( 0x01, bytes[7] );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        EndianUtils.writeSwappedDouble( baos, d1 );
        bytes = baos.toByteArray();
        assertEquals( 0x08, bytes[0] );
        assertEquals( 0x07, bytes[1] );
        assertEquals( 0x06, bytes[2] );
        assertEquals( 0x05, bytes[3] );
        assertEquals( 0x04, bytes[4] );
        assertEquals( 0x03, bytes[5] );
        assertEquals( 0x02, bytes[6] );
        assertEquals( 0x01, bytes[7] );
    }

    // tests #IO-101
    @Test
    public void testSymmetryOfLong() {

        final double[] tests = new double[] {34.345, -345.5645, 545.12, 10.043, 7.123456789123};
        for (final double test : tests) {

            // testing the real problem
            byte[] buffer = new byte[8];
            final long ln1 = Double.doubleToLongBits( test );
            EndianUtils.writeSwappedLong(buffer, 0, ln1);
            final long ln2 = EndianUtils.readSwappedLong(buffer, 0);
            assertEquals( ln1, ln2 );

            // testing the bug report
            buffer = new byte[8];
            EndianUtils.writeSwappedDouble(buffer, 0, test);
            final double val = EndianUtils.readSwappedDouble(buffer, 0);
            assertEquals( test, val, 0 );
        }
    }

    // tests #IO-117
    @Test
    public void testUnsignedOverrun() throws Exception {
        final byte[] target = new byte[] { 0, 0, 0, (byte)0x80 };
        final long expected = 0x80000000L;

        long actual = EndianUtils.readSwappedUnsignedInteger(target, 0);
        assertEquals(expected, actual, "readSwappedUnsignedInteger(byte[], int) was incorrect");

        final ByteArrayInputStream in = new ByteArrayInputStream(target);
        actual = EndianUtils.readSwappedUnsignedInteger(in);
        assertEquals(expected, actual, "readSwappedUnsignedInteger(InputStream) was incorrect");
    }

}
