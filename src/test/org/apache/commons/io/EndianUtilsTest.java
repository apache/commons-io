/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/EndianUtilsTest.java,v 1.8 2004/01/02 06:44:26 bayard Exp $
 * $Revision: 1.8 $
 * $Date: 2004/01/02 06:44:26 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
package org.apache.commons.io;

import junit.framework.TestCase;

/**
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision: 1.8 $ $Date: 2004/01/02 06:44:26 $
 */
public class EndianUtilsTest extends TestCase {

    public EndianUtilsTest(String name) {
        super(name);
    }

    public void testSwapShort() {
        assertEquals( (short) 0, EndianUtils.swapShort( (short) 0 ) );
        assertEquals( (short) 0x0201, EndianUtils.swapShort( (short) 0x0102 ) );
        assertEquals( (short) 0xffff, EndianUtils.swapShort( (short) 0xffff ) );
        assertEquals( (short) 0x0102, EndianUtils.swapShort( (short) 0x0201 ) );
    }

    public void testSwapInteger() {
        assertEquals( 0, EndianUtils.swapInteger( 0 ) );
        assertEquals( 0x04030201, EndianUtils.swapInteger( 0x01020304 ) );
        assertEquals( 0x01000000, EndianUtils.swapInteger( 0x00000001 ) );
        assertEquals( 0x00000001, EndianUtils.swapInteger( 0x01000000 ) );
        assertEquals( 0x11111111, EndianUtils.swapInteger( 0x11111111 ) );
        assertEquals( 0xabcdef10, EndianUtils.swapInteger( 0x10efcdab ) );
        assertEquals( 0xab, EndianUtils.swapInteger( 0xab000000 ) );
    }

    public void testSwapLong() {
        assertEquals( 0, EndianUtils.swapLong( 0 ) );
        assertEquals( 0x0807060504030201L, EndianUtils.swapLong( 0x0102030405060708L ) );
        assertEquals( 0xffffffffffffffffL, EndianUtils.swapLong( 0xffffffffffffffffL ) );
        assertEquals( 0xab, EndianUtils.swapLong( 0xab00000000000000L ) );
    }

    public void testSwapFloat() {
        assertEquals( 0.0f, EndianUtils.swapFloat( 0.0f ), 0.0 );
        float f1 = Float.intBitsToFloat( 0x01020304 );
        float f2 = Float.intBitsToFloat( 0x04030201 );
        assertEquals( f2, EndianUtils.swapFloat( f1 ), 0.0 );
    }

    public void testSwapDouble() {
        assertEquals( 0.0, EndianUtils.swapDouble( 0.0 ), 0.0 );
        double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        double d2 = Double.longBitsToDouble( 0x0807060504030201L );
        assertEquals( d2, EndianUtils.swapDouble( d1 ), 0.0 );
    }

    /**
     * Tests all swapXxxx methods for symmetry when going from one endian 
     * to another and back again. 
     */
    public void testSymmetry() {
        assertEquals( (short) 0x0102, EndianUtils.swapShort( EndianUtils.swapShort( (short) 0x0102 ) ) );
        assertEquals( 0x01020304, EndianUtils.swapInteger( EndianUtils.swapInteger( 0x01020304 ) ) );
        assertEquals( 0x0102030405060708L, EndianUtils.swapLong( EndianUtils.swapLong( 0x0102030405060708L ) ) );
        float f1 = Float.intBitsToFloat( 0x01020304 );
        assertEquals( f1, EndianUtils.swapFloat( EndianUtils.swapFloat( f1 ) ), 0.0 );
        double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        assertEquals( d1, EndianUtils.swapDouble( EndianUtils.swapDouble( d1 ) ), 0.0 );
    }

    public void testReadSwappedShort() {
        byte[] bytes = new byte[] { 0x02, 0x01 };
        assertEquals( 0x0102, EndianUtils.readSwappedShort( bytes, 0 ) );
    }

    public void testWriteSwappedShort() {
        byte[] bytes = new byte[2];
        EndianUtils.writeSwappedShort( bytes, 0, (short) 0x0102 );
        assertEquals( 0x02, bytes[0] );
        assertEquals( 0x01, bytes[1] );
    }

    public void testReadSwappedUnsignedShort() {
        byte[] bytes = new byte[] { 0x02, 0x01 };
        assertEquals( 0x00000102, EndianUtils.readSwappedUnsignedShort( bytes, 0 ) );
    }

    public void testReadSwappedInteger() {
        byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        int ln = EndianUtils.readSwappedInteger( bytes, 0 );
        assertEquals( 0x01020304, EndianUtils.readSwappedInteger( bytes, 0 ) );
    }

    public void testWriteSwappedInteger() {
        byte[] bytes = new byte[4];
        EndianUtils.writeSwappedInteger( bytes, 0, 0x01020304 );
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );
    }

    public void testReadSwappedUnsignedInteger() {
        byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        assertEquals( 0x0000000001020304L, EndianUtils.readSwappedUnsignedInteger( bytes, 0 ) );
    }

    public void testReadSwappedLong() {
        byte[] bytes = new byte[] { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        assertEquals( 0x0102030405060708L, EndianUtils.readSwappedLong( bytes, 0 ) );
    }

    public void testWriteSwappedLong() {
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
    }

    public void testReadSwappedFloat() {
        byte[] bytes = new byte[] { 0x04, 0x03, 0x02, 0x01 };
        float f1 = Float.intBitsToFloat( 0x01020304 );
        float f2 = EndianUtils.readSwappedFloat( bytes, 0 );
        assertEquals( f1, f2, 0.0 );
    }

    public void testWriteSwappedFloat() {
        byte[] bytes = new byte[4];
        float f1 = Float.intBitsToFloat( 0x01020304 );
        EndianUtils.writeSwappedFloat( bytes, 0, f1 );
        assertEquals( 0x04, bytes[0] );
        assertEquals( 0x03, bytes[1] );
        assertEquals( 0x02, bytes[2] );
        assertEquals( 0x01, bytes[3] );
    }

    public void testReadSwappedDouble() {
        byte[] bytes = new byte[] { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        double d2 = EndianUtils.readSwappedDouble( bytes, 0 );
        assertEquals( d1, d2, 0.0 );
    }

    public void testWriteSwappedDouble() {
        byte[] bytes = new byte[8];
        double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        EndianUtils.writeSwappedDouble( bytes, 0, d1 );
        assertEquals( 0x08, bytes[0] );
        assertEquals( 0x07, bytes[1] );
        assertEquals( 0x06, bytes[2] );
        assertEquals( 0x05, bytes[3] );
        assertEquals( 0x04, bytes[4] );
        assertEquals( 0x03, bytes[5] );
        assertEquals( 0x02, bytes[6] );
        assertEquals( 0x01, bytes[7] );
    }

    /*
    // TODO:
    // All readSwappedXxxx(InputStream)
    // All writeSwappedXxxx(OutputStream, xxxx)
    */

}
