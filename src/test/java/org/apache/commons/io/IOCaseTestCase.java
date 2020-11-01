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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

/**
 * This is used to test IOCase for correctness.
 *
 */
public class IOCaseTestCase {

    private static final boolean WINDOWS = File.separatorChar == '\\';

    //-----------------------------------------------------------------------
    @Test
    public void test_forName() throws Exception {
        assertEquals(IOCase.SENSITIVE, IOCase.forName("Sensitive"));
        assertEquals(IOCase.INSENSITIVE, IOCase.forName("Insensitive"));
        assertEquals(IOCase.SYSTEM, IOCase.forName("System"));
        try {
            IOCase.forName("Blah");
            fail();
        } catch (final IllegalArgumentException ignore) {}
        try {
            IOCase.forName(null);
            fail();
        } catch (final IllegalArgumentException ignore) {}
    }

    @Test
    public void test_serialization() throws Exception {
        assertSame(IOCase.SENSITIVE, serialize(IOCase.SENSITIVE));
        assertSame(IOCase.INSENSITIVE, serialize(IOCase.INSENSITIVE));
        assertSame(IOCase.SYSTEM, serialize(IOCase.SYSTEM));
    }

    @Test
    public void test_getName() throws Exception {
        assertEquals("Sensitive", IOCase.SENSITIVE.getName());
        assertEquals("Insensitive", IOCase.INSENSITIVE.getName());
        assertEquals("System", IOCase.SYSTEM.getName());
    }

    @Test
    public void test_toString() throws Exception {
        assertEquals("Sensitive", IOCase.SENSITIVE.toString());
        assertEquals("Insensitive", IOCase.INSENSITIVE.toString());
        assertEquals("System", IOCase.SYSTEM.toString());
    }

    @Test
    public void test_isCaseSensitive() throws Exception {
        assertTrue(IOCase.SENSITIVE.isCaseSensitive());
        assertFalse(IOCase.INSENSITIVE.isCaseSensitive());
        assertEquals(!WINDOWS, IOCase.SYSTEM.isCaseSensitive());
    }
    //-----------------------------------------------------------------------
    @Test
    public void test_checkCompare_functionality() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkCompareTo("ABC", "") > 0);
        assertTrue(IOCase.SENSITIVE.checkCompareTo("", "ABC") < 0);
        assertTrue(IOCase.SENSITIVE.checkCompareTo("ABC", "DEF") < 0);
        assertTrue(IOCase.SENSITIVE.checkCompareTo("DEF", "ABC") > 0);
        assertEquals(0, IOCase.SENSITIVE.checkCompareTo("ABC", "ABC"));
        assertEquals(0, IOCase.SENSITIVE.checkCompareTo("", ""));

        try {
            IOCase.SENSITIVE.checkCompareTo("ABC", null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkCompareTo(null, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkCompareTo(null, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkCompare_case() throws Exception {
        assertEquals(0, IOCase.SENSITIVE.checkCompareTo("ABC", "ABC"));
        assertTrue(IOCase.SENSITIVE.checkCompareTo("ABC", "abc") < 0);
        assertTrue(IOCase.SENSITIVE.checkCompareTo("abc", "ABC") > 0);

        assertEquals(0, IOCase.INSENSITIVE.checkCompareTo("ABC", "ABC"));
        assertEquals(0, IOCase.INSENSITIVE.checkCompareTo("ABC", "abc"));
        assertEquals(0, IOCase.INSENSITIVE.checkCompareTo("abc", "ABC"));

        assertEquals(0, IOCase.SYSTEM.checkCompareTo("ABC", "ABC"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkCompareTo("ABC", "abc") == 0);
        assertEquals(WINDOWS, IOCase.SYSTEM.checkCompareTo("abc", "ABC") == 0);
    }


    //-----------------------------------------------------------------------
    @Test
    public void test_checkEquals_functionality() throws Exception {
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", ""));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "A"));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "AB"));
        assertTrue(IOCase.SENSITIVE.checkEquals("ABC", "ABC"));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "BC"));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "C"));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "ABCD"));
        assertFalse(IOCase.SENSITIVE.checkEquals("", "ABC"));
        assertTrue(IOCase.SENSITIVE.checkEquals("", ""));

        try {
            IOCase.SENSITIVE.checkEquals("ABC", null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkEquals_case() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkEquals("ABC", "ABC"));
        assertFalse(IOCase.SENSITIVE.checkEquals("ABC", "Abc"));

        assertTrue(IOCase.INSENSITIVE.checkEquals("ABC", "ABC"));
        assertTrue(IOCase.INSENSITIVE.checkEquals("ABC", "Abc"));

        assertTrue(IOCase.SYSTEM.checkEquals("ABC", "ABC"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkEquals("ABC", "Abc"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkStartsWith_functionality() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkStartsWith("ABC", ""));
        assertTrue(IOCase.SENSITIVE.checkStartsWith("ABC", "A"));
        assertTrue(IOCase.SENSITIVE.checkStartsWith("ABC", "AB"));
        assertTrue(IOCase.SENSITIVE.checkStartsWith("ABC", "ABC"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith("ABC", "BC"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith("ABC", "C"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith("ABC", "ABCD"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith("", "ABC"));
        assertTrue(IOCase.SENSITIVE.checkStartsWith("", ""));

        assertFalse(IOCase.SENSITIVE.checkStartsWith("ABC", null));
        assertFalse(IOCase.SENSITIVE.checkStartsWith(null, "ABC"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith(null, null));
    }

    @Test
    public void test_checkStartsWith_case() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkStartsWith("ABC", "AB"));
        assertFalse(IOCase.SENSITIVE.checkStartsWith("ABC", "Ab"));

        assertTrue(IOCase.INSENSITIVE.checkStartsWith("ABC", "AB"));
        assertTrue(IOCase.INSENSITIVE.checkStartsWith("ABC", "Ab"));

        assertTrue(IOCase.SYSTEM.checkStartsWith("ABC", "AB"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkStartsWith("ABC", "Ab"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkEndsWith_functionality() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkEndsWith("ABC", ""));
        assertFalse(IOCase.SENSITIVE.checkEndsWith("ABC", "A"));
        assertFalse(IOCase.SENSITIVE.checkEndsWith("ABC", "AB"));
        assertTrue(IOCase.SENSITIVE.checkEndsWith("ABC", "ABC"));
        assertTrue(IOCase.SENSITIVE.checkEndsWith("ABC", "BC"));
        assertTrue(IOCase.SENSITIVE.checkEndsWith("ABC", "C"));
        assertFalse(IOCase.SENSITIVE.checkEndsWith("ABC", "ABCD"));
        assertFalse(IOCase.SENSITIVE.checkEndsWith("", "ABC"));
        assertTrue(IOCase.SENSITIVE.checkEndsWith("", ""));

        assertFalse(IOCase.SENSITIVE.checkEndsWith("ABC", null));
        assertFalse(IOCase.SENSITIVE.checkEndsWith(null, "ABC"));
        assertFalse(IOCase.SENSITIVE.checkEndsWith(null, null));
    }

    @Test
    public void test_checkEndsWith_case() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkEndsWith("ABC", "BC"));
        assertFalse(IOCase.SENSITIVE.checkEndsWith("ABC", "Bc"));

        assertTrue(IOCase.INSENSITIVE.checkEndsWith("ABC", "BC"));
        assertTrue(IOCase.INSENSITIVE.checkEndsWith("ABC", "Bc"));

        assertTrue(IOCase.SYSTEM.checkEndsWith("ABC", "BC"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkEndsWith("ABC", "Bc"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkIndexOf_functionality() throws Exception {

        // start
        assertEquals(0,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "A"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "A"));
        assertEquals(0,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "AB"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "AB"));
        assertEquals(0,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "ABC"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "ABC"));

        // middle
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "D"));
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "D"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "D"));
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DE"));
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "DE"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "DE"));
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DEF"));
        assertEquals(3,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "DEF"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "DEF"));

        // end
        assertEquals(9,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "J"));
        assertEquals(9,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "J"));
        assertEquals(9,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 9, "J"));
        assertEquals(8,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "IJ"));
        assertEquals(8,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "IJ"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 9, "IJ"));
        assertEquals(7,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 6, "HIJ"));
        assertEquals(7,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 7, "HIJ"));
        assertEquals(-1,  IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "HIJ"));

        // not found
        assertEquals(-1,   IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DED"));

        // too long
        assertEquals(-1,   IOCase.SENSITIVE.checkIndexOf("DEF", 0, "ABCDEFGHIJ"));

        try {
            IOCase.SENSITIVE.checkIndexOf("ABC", 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkIndexOf(null, 0, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkIndexOf(null, 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkIndexOf_case() throws Exception {
        assertEquals(1,  IOCase.SENSITIVE.checkIndexOf("ABC", 0, "BC"));
        assertEquals(-1, IOCase.SENSITIVE.checkIndexOf("ABC", 0, "Bc"));

        assertEquals(1, IOCase.INSENSITIVE.checkIndexOf("ABC", 0, "BC"));
        assertEquals(1, IOCase.INSENSITIVE.checkIndexOf("ABC", 0, "Bc"));

        assertEquals(1, IOCase.SYSTEM.checkIndexOf("ABC", 0, "BC"));
        assertEquals(WINDOWS ? 1 : -1, IOCase.SYSTEM.checkIndexOf("ABC", 0, "Bc"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkRegionMatches_functionality() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, ""));
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "A"));
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABC"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "BC"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "C"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABCD"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("", 0, "ABC"));
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("", 0, ""));

        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, ""));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "A"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "AB"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABC"));
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "BC"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "C"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABCD"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("", 1, "ABC"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("", 1, ""));

        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 1, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkRegionMatches_case() throws Exception {
        assertTrue(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertFalse(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "Ab"));

        assertTrue(IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertTrue(IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "Ab"));

        assertTrue(IOCase.SYSTEM.checkRegionMatches("ABC", 0, "AB"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkRegionMatches("ABC", 0, "Ab"));
    }

    //-----------------------------------------------------------------------
    private IOCase serialize(final IOCase value) throws Exception {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(value);
        out.flush();
        out.close();

        final ByteArrayInputStream bufin = new ByteArrayInputStream(buf.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bufin);
        return (IOCase) in.readObject();
    }

}
