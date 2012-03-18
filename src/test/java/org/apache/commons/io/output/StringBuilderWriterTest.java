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
import java.io.Writer;

import junit.framework.TestCase;

/**
 * Test case for {@link StringBuilderWriter}.
 *
 * @version $Id$
 */
public class StringBuilderWriterTest extends TestCase {
    private static final char[] FOOBAR_CHARS = new char[] {'F', 'o', 'o', 'B', 'a', 'r'};

    /**
     * Contruct a new test case.
     * @param name The name of the test
     */
    public StringBuilderWriterTest(String name) {
        super(name);
    }

    /** Test {@link StringBuilderWriter} constructor. */
    public void testAppendConstructCapacity() throws IOException {
        Writer writer = new StringBuilderWriter(100);
        writer.append("Foo");
        assertEquals("Foo", writer.toString());
    }

    /** Test {@link StringBuilderWriter} constructor. */
    public void testAppendConstructStringBuilder() {
        StringBuilder builder = new StringBuilder("Foo");
        StringBuilderWriter writer = new StringBuilderWriter(builder);
        writer.append("Bar");
        assertEquals("FooBar", writer.toString());
        assertSame(builder, writer.getBuilder());
    }

    /** Test {@link StringBuilderWriter} constructor. */
    public void testAppendConstructNull() throws IOException {
        Writer writer = new StringBuilderWriter((StringBuilder)null);
        writer.append("Foo");
        assertEquals("Foo", writer.toString());
    }

    /** Test {@link Writer#append(char)}. */
    public void testAppendChar() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.append('F').append('o').append('o');
        assertEquals("Foo", writer.toString());
    }

    /** Test {@link Writer#append(CharSequence)}. */
    public void testAppendCharSequence() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.append("Foo").append("Bar");
        assertEquals("FooBar", writer.toString());
    }

    /** Test {@link Writer#append(CharSequence, int, int)}. */
    public void testAppendCharSequencePortion() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.append("FooBar", 3, 6).append(new StringBuffer("FooBar"), 0, 3);
        assertEquals("BarFoo", writer.toString());
    }

    /** Test {@link Writer#close()}. */
    public void testClose() {
        Writer writer = new StringBuilderWriter();
        try {
            writer.append("Foo");
            writer.close();
            writer.append("Bar");
        } catch (Throwable t) {
            fail("Threw: " + t);
        }
        assertEquals("FooBar", writer.toString());
    }

    /** Test {@link Writer#write(int)}. */
    public void testWriteChar() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.write('F');
        assertEquals("F", writer.toString());
        writer.write('o');
        assertEquals("Fo", writer.toString());
        writer.write('o');
        assertEquals("Foo", writer.toString());
    }

    /** Test {@link Writer#write(char[])}. */
    public void testWriteCharArray() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.write(new char[] {'F', 'o', 'o'});
        assertEquals("Foo", writer.toString());
        writer.write(new char[] {'B', 'a', 'r'});
        assertEquals("FooBar", writer.toString());
    }

    /** Test {@link Writer#write(char[], int, int)}. */
    public void testWriteCharArrayPortion() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.write(FOOBAR_CHARS, 3, 3);
        assertEquals("Bar", writer.toString());
        writer.write(FOOBAR_CHARS, 0, 3);
        assertEquals("BarFoo", writer.toString());
    }

    /** Test {@link Writer#write(String)}. */
    public void testWriteString() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.write("Foo");
        assertEquals("Foo", writer.toString());
        writer.write("Bar");
        assertEquals("FooBar", writer.toString());
    }

    /** Test {@link Writer#write(String, int, int)}. */
    public void testWriteStringPortion() throws IOException {
        Writer writer = new StringBuilderWriter();
        writer.write("FooBar", 3, 3);
        assertEquals("Bar", writer.toString());
        writer.write("FooBar", 0, 3);
        assertEquals("BarFoo", writer.toString());
    }

}
