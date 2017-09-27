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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;


/**
 * Test for {@link ByteOrderMark}.
 *
 */
public class ByteOrderMarkTestCase  {

    private static final ByteOrderMark TEST_BOM_1 = new ByteOrderMark("test1", 1);
    private static final ByteOrderMark TEST_BOM_2 = new ByteOrderMark("test2", 1, 2);
    private static final ByteOrderMark TEST_BOM_3 = new ByteOrderMark("test3", 1, 2, 3);

    /** Test {@link ByteOrderMark#getCharsetName()} */
    @Test
    public void charsetName() {
        assertEquals("test1 name", "test1", TEST_BOM_1.getCharsetName());
        assertEquals("test2 name", "test2", TEST_BOM_2.getCharsetName());
        assertEquals("test3 name", "test3", TEST_BOM_3.getCharsetName());
    }

    /** Tests that {@link ByteOrderMark#getCharsetName()} can be loaded as a {@link java.nio.charset.Charset} as advertised. */
    @Test
    public void constantCharsetNames() {
        assertNotNull(Charset.forName(ByteOrderMark.UTF_8.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_16BE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_16LE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_32BE.getCharsetName()));
        assertNotNull(Charset.forName(ByteOrderMark.UTF_32LE.getCharsetName()));
    }

    /** Test {@link ByteOrderMark#length()} */
    @Test
    public void testLength() {        assertEquals("test1 length", 1, TEST_BOM_1.length());
        assertEquals("test2 length", 2, TEST_BOM_2.length());
        assertEquals("test3 length", 3, TEST_BOM_3.length());
    }

    /** Test {@link ByteOrderMark#get(int)} */
    @Test
    public void get() {
        assertEquals("test1 get(0)", 1, TEST_BOM_1.get(0));
        assertEquals("test2 get(0)", 1, TEST_BOM_2.get(0));
        assertEquals("test2 get(1)", 2, TEST_BOM_2.get(1));
        assertEquals("test3 get(0)", 1, TEST_BOM_3.get(0));
        assertEquals("test3 get(1)", 2, TEST_BOM_3.get(1));
        assertEquals("test3 get(2)", 3, TEST_BOM_3.get(2));
    }

    /** Test {@link ByteOrderMark#getBytes()} */
    @Test
    public void getBytes() {
        assertTrue("test1 bytes", Arrays.equals(TEST_BOM_1.getBytes(), new byte[] {(byte)1}));
        assertTrue("test1 bytes", Arrays.equals(TEST_BOM_2.getBytes(), new byte[] {(byte)1, (byte)2}));
        assertTrue("test1 bytes", Arrays.equals(TEST_BOM_3.getBytes(), new byte[] {(byte)1, (byte)2, (byte)3}));
    }

    /** Test {@link ByteOrderMark#equals(Object)} */
    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEquals() {
        assertTrue("test1 equals", TEST_BOM_1.equals(TEST_BOM_1));
        assertTrue("test2 equals", TEST_BOM_2.equals(TEST_BOM_2));
        assertTrue("test3 equals", TEST_BOM_3.equals(TEST_BOM_3));

        assertFalse("Object not equal",  TEST_BOM_1.equals(new Object()));
        assertFalse("test1-1 not equal", TEST_BOM_1.equals(new ByteOrderMark("1a", 2)));
        assertFalse("test1-2 not test2", TEST_BOM_1.equals(new ByteOrderMark("1b", 1, 2)));
        assertFalse("test2 not equal", TEST_BOM_2.equals(new ByteOrderMark("2", 1, 1)));
        assertFalse("test3 not equal", TEST_BOM_3.equals(new ByteOrderMark("3", 1, 2, 4)));
    }

    /** Test {@link ByteOrderMark#hashCode()} */
    @Test
    public void testHashCode() {
        final int bomClassHash = ByteOrderMark.class.hashCode();
        assertEquals("hash test1 ", bomClassHash + 1,  TEST_BOM_1.hashCode());
        assertEquals("hash test2 ", bomClassHash + 3,  TEST_BOM_2.hashCode());
        assertEquals("hash test3 ", bomClassHash + 6,  TEST_BOM_3.hashCode());
    }

    /** Test Errors */
    @Test
    public void errors() {
        try {
            new ByteOrderMark(null, 1,2,3);
            fail("null charset name, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("", 1,2,3);
            fail("no charset name, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("a", (int[])null);
            fail("null bytes, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("b");
            fail("empty bytes, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    /** Test {@link ByteOrderMark#toString()} */
    @Test
    public void testToString() {
        assertEquals("test1 ", "ByteOrderMark[test1: 0x1]",          TEST_BOM_1.toString());
        assertEquals("test2 ", "ByteOrderMark[test2: 0x1,0x2]",      TEST_BOM_2.toString());
        assertEquals("test3 ", "ByteOrderMark[test3: 0x1,0x2,0x3]",  TEST_BOM_3.toString());
    }
}
