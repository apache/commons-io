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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ByteOrderMark}.
 */
public class ByteOrderMarkTest {

    private static final ByteOrderMark TEST_BOM_1 = new ByteOrderMark("test1", 1);
    private static final ByteOrderMark TEST_BOM_2 = new ByteOrderMark("test2", 1, 2);
    private static final ByteOrderMark TEST_BOM_3 = new ByteOrderMark("test3", 1, 2, 3);

    /** Tests that {@link ByteOrderMark#getCharsetName()} can be loaded as a {@link java.nio.charset.Charset} as advertised. */
    @Test
    public void testConstantCharsetNames() {
        assertNotNull(Charset.forName(ByteOrderMark.UTF_8.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_16BE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_16LE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_32BE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_32LE.getCharsetName()));
    }

    /** Tests Exceptions */
    @Test
    public void testConstructorExceptions() {
        assertThrows(NullPointerException.class, () -> new ByteOrderMark(null, 1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> new ByteOrderMark("", 1, 2, 3));
        assertThrows(NullPointerException.class, () -> new ByteOrderMark("a", (int[]) null));
        assertThrows(IllegalArgumentException.class, () -> new ByteOrderMark("b"));
    }

    /** Tests {@link ByteOrderMark#equals(Object)} */
    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEquals() {
        assertEquals(ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16BE);
        assertEquals(ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16LE);
        assertEquals(ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32BE);
        assertEquals(ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32LE);
        assertEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_8);

        assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE);
        assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE);
        assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_32BE);
        assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_32LE);

        assertEquals(TEST_BOM_1, TEST_BOM_1, "test1 equals");
        assertEquals(TEST_BOM_2, TEST_BOM_2, "test2 equals");
        assertEquals(TEST_BOM_3, TEST_BOM_3, "test3 equals");

        assertNotEquals(TEST_BOM_1, new Object(), "Object not equal");
        assertNotEquals(TEST_BOM_1, new ByteOrderMark("1a", 2), "test1-1 not equal");
        assertNotEquals(TEST_BOM_1, new ByteOrderMark("1b", 1, 2), "test1-2 not test2");
        assertNotEquals(TEST_BOM_2, new ByteOrderMark("2", 1, 1), "test2 not equal");
        assertNotEquals(TEST_BOM_3, new ByteOrderMark("3", 1, 2, 4), "test3 not equal");
    }

    /** Tests {@link ByteOrderMark#getBytes()} */
    @Test
    public void testGetBytes() {
        assertArrayEquals(TEST_BOM_1.getBytes(), new byte[] { (byte) 1 }, "test1 bytes");
        TEST_BOM_1.getBytes()[0] = 2;
        assertArrayEquals(TEST_BOM_1.getBytes(), new byte[] { (byte) 1 }, "test1 bytes");
        assertArrayEquals(TEST_BOM_2.getBytes(), new byte[] { (byte) 1, (byte) 2 }, "test1 bytes");
        assertArrayEquals(TEST_BOM_3.getBytes(), new byte[] { (byte) 1, (byte) 2, (byte) 3 }, "test1 bytes");
    }

    /** Tests {@link ByteOrderMark#getCharsetName()} */
    @Test
    public void testGetCharsetName() {
        assertEquals("test1", TEST_BOM_1.getCharsetName(), "test1 name");
        assertEquals("test2", TEST_BOM_2.getCharsetName(), "test2 name");
        assertEquals("test3", TEST_BOM_3.getCharsetName(), "test3 name");
    }

    /** Tests {@link ByteOrderMark#get(int)} */
    @Test
    public void testGetInt() {
        assertEquals(1, TEST_BOM_1.get(0), "test1 get(0)");
        assertEquals(1, TEST_BOM_2.get(0), "test2 get(0)");
        assertEquals(2, TEST_BOM_2.get(1), "test2 get(1)");
        assertEquals(1, TEST_BOM_3.get(0), "test3 get(0)");
        assertEquals(2, TEST_BOM_3.get(1), "test3 get(1)");
        assertEquals(3, TEST_BOM_3.get(2), "test3 get(2)");
    }

    /** Tests {@link ByteOrderMark#hashCode()} */
    @Test
    public void testHashCode() {
        final int bomClassHash = ByteOrderMark.class.hashCode();
        assertEquals(bomClassHash + 1, TEST_BOM_1.hashCode(), "hash test1 ");
        assertEquals(bomClassHash + 3, TEST_BOM_2.hashCode(), "hash test2 ");
        assertEquals(bomClassHash + 6, TEST_BOM_3.hashCode(), "hash test3 ");
    }

    /** Tests {@link ByteOrderMark#length()} */
    @Test
    public void testLength() {
        assertEquals(1, TEST_BOM_1.length(), "test1 length");
        assertEquals(2, TEST_BOM_2.length(), "test2 length");
        assertEquals(3, TEST_BOM_3.length(), "test3 length");
    }

    @Test
    public void testMatches() {
        assertTrue(ByteOrderMark.UTF_16BE.matches(ByteOrderMark.UTF_16BE.getRawBytes()));
        assertTrue(ByteOrderMark.UTF_16LE.matches(ByteOrderMark.UTF_16LE.getRawBytes()));
        assertTrue(ByteOrderMark.UTF_32BE.matches(ByteOrderMark.UTF_32BE.getRawBytes()));
        assertTrue(ByteOrderMark.UTF_16BE.matches(ByteOrderMark.UTF_16BE.getRawBytes()));
        assertTrue(ByteOrderMark.UTF_8.matches(ByteOrderMark.UTF_8.getRawBytes()));

        assertTrue(TEST_BOM_1.matches(TEST_BOM_1.getRawBytes()));
        assertTrue(TEST_BOM_2.matches(TEST_BOM_2.getRawBytes()));
        assertTrue(TEST_BOM_3.matches(TEST_BOM_3.getRawBytes()));

        assertFalse(TEST_BOM_1.matches(new ByteOrderMark("1a", 2).getRawBytes()));
        assertTrue(TEST_BOM_1.matches(new ByteOrderMark("1b", 1, 2).getRawBytes()));
        assertFalse(TEST_BOM_2.matches(new ByteOrderMark("2", 1, 1).getRawBytes()));
        assertFalse(TEST_BOM_3.matches(new ByteOrderMark("3", 1, 2, 4).getRawBytes()));
    }

    /** Tests {@link ByteOrderMark#toString()} */
    @Test
    public void testToString() {
        assertEquals("ByteOrderMark[test1: 0x1]", TEST_BOM_1.toString(), "test1 ");
        assertEquals("ByteOrderMark[test2: 0x1,0x2]", TEST_BOM_2.toString(), "test2 ");
        assertEquals("ByteOrderMark[test3: 0x1,0x2,0x3]", TEST_BOM_3.toString(), "test3 ");
    }
}
