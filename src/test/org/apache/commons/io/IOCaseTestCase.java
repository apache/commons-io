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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test IOCase for correctness.
 *
 * @author Stephen Colebourne
 * @version $Id$
 */
public class IOCaseTestCase extends FileBasedTestCase {

    private static final boolean WINDOWS = (File.separatorChar == '\\');

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(IOCaseTestCase.class);
    }

    public IOCaseTestCase(String name) throws IOException {
        super(name);
    }

    protected void setUp() throws Exception {

    }

    protected void tearDown() throws Exception {
    }

    //-----------------------------------------------------------------------
    public void test_forName() throws Exception {
        assertEquals(IOCase.SENSITIVE, IOCase.forName("Sensitive"));
        assertEquals(IOCase.INSENSITIVE, IOCase.forName("Insensitive"));
        assertEquals(IOCase.SYSTEM, IOCase.forName("System"));
        try {
            IOCase.forName("Blah");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            IOCase.forName(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void test_serialization() throws Exception {
        assertSame(IOCase.SENSITIVE, serialize(IOCase.SENSITIVE));
        assertSame(IOCase.INSENSITIVE, serialize(IOCase.INSENSITIVE));
        assertSame(IOCase.SYSTEM, serialize(IOCase.SYSTEM));
    }

    public void test_getName() throws Exception {
        assertEquals("Sensitive", IOCase.SENSITIVE.getName());
        assertEquals("Insensitive", IOCase.INSENSITIVE.getName());
        assertEquals("System", IOCase.SYSTEM.getName());
    }

    public void test_toString() throws Exception {
        assertEquals("Sensitive", IOCase.SENSITIVE.toString());
        assertEquals("Insensitive", IOCase.INSENSITIVE.toString());
        assertEquals("System", IOCase.SYSTEM.toString());
    }

    public void test_isCaseSensitive() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.isCaseSensitive());
        assertEquals(false, IOCase.INSENSITIVE.isCaseSensitive());
        assertEquals(!WINDOWS, IOCase.SYSTEM.isCaseSensitive());
    }

    //-----------------------------------------------------------------------
    public void test_checkEquals_functionality() throws Exception {
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", ""));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "A"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "AB"));
        assertEquals(true, IOCase.SENSITIVE.checkEquals("ABC", "ABC"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "BC"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "C"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "ABCD"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("", "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkEquals("", ""));
        
        try {
            IOCase.SENSITIVE.checkEquals("ABC", null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, "ABC");
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void test_checkEquals_case() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkEquals("ABC", "ABC"));
        assertEquals(false, IOCase.SENSITIVE.checkEquals("ABC", "Abc"));
        
        assertEquals(true, IOCase.INSENSITIVE.checkEquals("ABC", "ABC"));
        assertEquals(true, IOCase.INSENSITIVE.checkEquals("ABC", "Abc"));
        
        assertEquals(true, IOCase.SYSTEM.checkEquals("ABC", "ABC"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkEquals("ABC", "Abc"));
    }

    //-----------------------------------------------------------------------
    public void test_checkStartsWith_functionality() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("ABC", ""));
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("ABC", "A"));
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("ABC", "AB"));
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("ABC", "ABC"));
        assertEquals(false, IOCase.SENSITIVE.checkStartsWith("ABC", "BC"));
        assertEquals(false, IOCase.SENSITIVE.checkStartsWith("ABC", "C"));
        assertEquals(false, IOCase.SENSITIVE.checkStartsWith("ABC", "ABCD"));
        assertEquals(false, IOCase.SENSITIVE.checkStartsWith("", "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("", ""));
        
        try {
            IOCase.SENSITIVE.checkStartsWith("ABC", null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkStartsWith(null, "ABC");
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkStartsWith(null, null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void test_checkStartsWith_case() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkStartsWith("ABC", "AB"));
        assertEquals(false, IOCase.SENSITIVE.checkStartsWith("ABC", "Ab"));
        
        assertEquals(true, IOCase.INSENSITIVE.checkStartsWith("ABC", "AB"));
        assertEquals(true, IOCase.INSENSITIVE.checkStartsWith("ABC", "Ab"));
        
        assertEquals(true, IOCase.SYSTEM.checkStartsWith("ABC", "AB"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkStartsWith("ABC", "Ab"));
    }

    //-----------------------------------------------------------------------
    public void test_checkEndsWith_functionality() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("ABC", ""));
        assertEquals(false, IOCase.SENSITIVE.checkEndsWith("ABC", "A"));
        assertEquals(false, IOCase.SENSITIVE.checkEndsWith("ABC", "AB"));
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("ABC", "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("ABC", "BC"));
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("ABC", "C"));
        assertEquals(false, IOCase.SENSITIVE.checkEndsWith("ABC", "ABCD"));
        assertEquals(false, IOCase.SENSITIVE.checkEndsWith("", "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("", ""));
        
        try {
            IOCase.SENSITIVE.checkEndsWith("ABC", null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkEndsWith(null, "ABC");
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkEndsWith(null, null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void test_checkEndsWith_case() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkEndsWith("ABC", "BC"));
        assertEquals(false, IOCase.SENSITIVE.checkEndsWith("ABC", "Bc"));
        
        assertEquals(true, IOCase.INSENSITIVE.checkEndsWith("ABC", "BC"));
        assertEquals(true, IOCase.INSENSITIVE.checkEndsWith("ABC", "Bc"));
        
        assertEquals(true, IOCase.SYSTEM.checkEndsWith("ABC", "BC"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkEndsWith("ABC", "Bc"));
    }

    //-----------------------------------------------------------------------
    public void test_checkRegionMatches_functionality() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, ""));
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "A"));
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABC"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "BC"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "C"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABCD"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("", 0, "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("", 0, ""));
        
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, ""));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "A"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "AB"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABC"));
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "BC"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "C"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABCD"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("", 1, "ABC"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("", 1, ""));
        
        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 0, null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, "ABC");
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 1, null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, "ABC");
            fail();
        } catch (NullPointerException ex) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void test_checkRegionMatches_case() throws Exception {
        assertEquals(true, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertEquals(false, IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "Ab"));
        
        assertEquals(true, IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "AB"));
        assertEquals(true, IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "Ab"));
        
        assertEquals(true, IOCase.SYSTEM.checkRegionMatches("ABC", 0, "AB"));
        assertEquals(WINDOWS, IOCase.SYSTEM.checkRegionMatches("ABC", 0, "Ab"));
    }

    //-----------------------------------------------------------------------
    private IOCase serialize(IOCase value) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(value);
        out.flush();
        out.close();

        ByteArrayInputStream bufin = new ByteArrayInputStream(buf.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bufin);
        return (IOCase) in.readObject();
    }

}
