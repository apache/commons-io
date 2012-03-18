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

import junit.framework.TestCase;

/**
 * Test {@link ProxyWriter}. 
 *
 * @version $Id$
 */
public class ProxyWriterTest extends TestCase {

    public ProxyWriterTest(String name) {
        super(name);
    }

    /** Test Appending a CharSequence */
    public void testAppendCharSequence() {
        StringBuilderWriter writer = new StringBuilderWriter();
        ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.append("ABC");
        } catch(Exception e) {
            fail("Appending CharSequence threw " + e);
        }
        assertEquals("ABC", writer.toString());
        
    }

    /** Test Writing a String */
    public void testWriteString() {
        StringBuilderWriter writer = new StringBuilderWriter();
        ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC");
        } catch(Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("ABC", writer.toString());
        
    }

    /** Test Writing a Partial String */
    public void testWriteStringPartial() {
        StringBuilderWriter writer = new StringBuilderWriter();
        ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write("ABC", 1, 2);
        } catch(Exception e) {
            fail("Writing String threw " + e);
        }
        assertEquals("BC", writer.toString());
        
    }

    /** Test Writing a Char array */
    public void testWriteCharArray() {
        StringBuilderWriter writer = new StringBuilderWriter();
        ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[] {'A', 'B', 'C'});
        } catch(Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("ABC", writer.toString());
        
    }

    /** Test Writing a Partial Char array */
    public void testWriteCharArrayPartial() {
        StringBuilderWriter writer = new StringBuilderWriter();
        ProxyWriter proxy = new ProxyWriter(writer);
        try {
            proxy.write(new char[] {'A', 'B', 'C'}, 1, 2);
        } catch(Exception e) {
            fail("Writing char[] threw " + e);
        }
        assertEquals("BC", writer.toString());
        
    }
    
    /** Test writing Null String */
    public void testNullString() {

        ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((String)null);
        } catch(Exception e) {
            fail("Writing null String threw " + e);
        }

        try {
            proxy.write((String)null, 0, 0);
        } catch(Exception e) {
            fail("Writing null String threw " + e);
        }
    }

    /** Test writing Null Char array */
    public void testNullCharArray() {

        ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.write((char[])null);
        } catch(Exception e) {
            fail("Writing null char[] threw " + e);
        }

        try {
            proxy.write((char[])null, 0, 0);
        } catch(Exception e) {
            fail("Writing null char[] threw " + e);
        }
    }

    /** Test appending Null CharSequence */
    public void testNullCharSequencec() {

        ProxyWriter proxy = new ProxyWriter(new NullWriter());

        try {
            proxy.append((String)null);
        } catch(Exception e) {
            fail("Appending null CharSequence threw " + e);
        }
    }

}
