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
package org.apache.commons.io.input.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CircularByteBuffer}.
 */
public class CircularByteBufferTest {

    @Test
    public void testAddByteSmallestBuffer() {
        final CircularByteBuffer cbb = new CircularByteBuffer(1);
        cbb.add((byte) 1);
        assertEquals(1, cbb.read());
        cbb.add((byte) 2);
        assertEquals(2, cbb.read());
    }

    @Test
    public void testAddInvalidOffset() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        assertThrows(IllegalArgumentException.class, () -> cbb.add(new byte[] { 1, 2, 3 }, -1, 3));
    }

    @Test
    public void testAddNegativeLength() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final byte[] targetBuffer = { 1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> cbb.add(targetBuffer, 0, -1));
    }

    @Test
    public void testAddNullBuffer() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        assertThrows(NullPointerException.class, () -> cbb.add(null, 0, 3));
    }

    /**
     * Tests for add function with 3 arguments of type byte[], int and int.
     */
    @Test
    public void testAddValidData() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final int length = 3;
        cbb.add(new byte[] { 3, 6, 9 }, 0, length);
        assertEquals(length, cbb.getCurrentNumberOfBytes());
    }

    @Test
    public void testClear() {
       final byte[] data = { 1, 2, 3 };
       final CircularByteBuffer buffer = new CircularByteBuffer(10);
       assertEquals(0, buffer.getCurrentNumberOfBytes());
       assertFalse(buffer.hasBytes());

       buffer.add(data, 0, data.length);
       assertEquals(3, buffer.getCurrentNumberOfBytes());
       assertEquals(7, buffer.getSpace());
       assertTrue(buffer.hasBytes());
       assertTrue(buffer.hasSpace());

       buffer.clear();
       assertEquals(0, buffer.getCurrentNumberOfBytes());
       assertEquals(10, buffer.getSpace());
       assertFalse(buffer.hasBytes());
       assertTrue(buffer.hasSpace());
    }

    @Test
    public void testHasSpace() {
        final CircularByteBuffer cbb = new CircularByteBuffer(1);
        assertTrue(cbb.hasSpace());
        cbb.add((byte) 1);
        assertFalse(cbb.hasSpace());
        assertEquals(1, cbb.read());
        assertTrue(cbb.hasSpace());
        cbb.add((byte) 2);
        assertFalse(cbb.hasSpace());
        assertEquals(2, cbb.read());
        assertTrue(cbb.hasSpace());
    }

    @Test
    public void testHasSpaceInt() {
        final CircularByteBuffer cbb = new CircularByteBuffer(1);
        assertTrue(cbb.hasSpace(1));
        cbb.add((byte) 1);
        assertFalse(cbb.hasSpace(1));
        assertEquals(1, cbb.read());
        assertTrue(cbb.hasSpace(1));
        cbb.add((byte) 2);
        assertFalse(cbb.hasSpace(1));
        assertEquals(2, cbb.read());
        assertTrue(cbb.hasSpace(1));
    }

    @Test
    public void testPeekWithExcessiveLength() {
        assertFalse(new CircularByteBuffer().peek(new byte[] { 1, 3, 5, 7, 9 }, 0, 6));
    }

    @Test
    public void testPeekWithInvalidOffset() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cbb.peek(new byte[] { 2, 4, 6, 8, 10 }, -1, 5));
        assertEquals("Illegal offset: -1", e.getMessage());
    }

    @Test
    public void testPeekWithNegativeLength() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cbb.peek(new byte[] { 1, 4, 3 }, 0, -1));
        assertEquals("Illegal length: -1", e.getMessage());
    }

    // Tests for peek function
    @Test
    public void testPeekWithValidArguments() {
        assertFalse(new CircularByteBuffer().peek(new byte[] { 5, 10, 15, 20, 25 }, 0, 5));
    }

    @Test
    public void testReadByteArray() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final String string = "0123456789";
        final byte[] bytesIn = string.getBytes(StandardCharsets.UTF_8);
        cbb.add(bytesIn, 0, 10);
        final byte[] bytesOut = new byte[10];
        cbb.read(bytesOut, 0, 10);
        assertEquals(string, new String(bytesOut, StandardCharsets.UTF_8));
    }

    @Test
    public void testReadByteArrayIllegalArgumentException() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final byte[] bytesOut = new byte[10];
        // targetOffset < 0
        assertThrows(IllegalArgumentException.class, () -> cbb.read(bytesOut, -1, 10));
        // targetOffset >= targetBuffer.length
        assertThrows(IllegalArgumentException.class, () -> cbb.read(bytesOut, 0, bytesOut.length + 1));
    }
}
