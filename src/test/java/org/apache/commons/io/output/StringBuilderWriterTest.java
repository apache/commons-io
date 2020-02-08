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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StringBuilderWriter}.
 *
 */
public class StringBuilderWriterTest {
    private static final char[] FOOBAR_CHARS = new char[] {'F', 'o', 'o', 'B', 'a', 'r'};


    @Test
    public void testAppendConstructCapacity() throws IOException {
        try (final Writer writer = new StringBuilderWriter(100)) {
            writer.append("Foo");
            assertEquals("Foo", writer.toString());
        }
    }

    @Test
    public void testAppendConstructStringBuilder() {
        final StringBuilder builder = new StringBuilder("Foo");
        try (final StringBuilderWriter writer = new StringBuilderWriter(builder)) {
            writer.append("Bar");
            assertEquals("FooBar", writer.toString());
            assertSame(builder, writer.getBuilder());
        }
    }

    @Test
    public void testAppendConstructNull() throws IOException {
        try (final Writer writer = new StringBuilderWriter(null)) {
            writer.append("Foo");
            assertEquals("Foo", writer.toString());
        }
    }

    @Test
    public void testAppendChar() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.append('F').append('o').append('o');
            assertEquals("Foo", writer.toString());
        }
    }

    @Test
    public void testAppendCharSequence() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.append("Foo").append("Bar");
            assertEquals("FooBar", writer.toString());
        }
    }

    @Test
    public void testAppendCharSequencePortion() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.append("FooBar", 3, 6).append(new StringBuffer("FooBar"), 0, 3);
            assertEquals("BarFoo", writer.toString());
        }
    }

    @Test
    public void testClose() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            try {
                writer.append("Foo");
                writer.close();
                writer.append("Bar");
            } catch (final Throwable t) {
                fail("Threw: " + t);
            }
            assertEquals("FooBar", writer.toString());
        }
    }

    @Test
    public void testWriteChar() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.write('F');
            assertEquals("F", writer.toString());
            writer.write('o');
            assertEquals("Fo", writer.toString());
            writer.write('o');
            assertEquals("Foo", writer.toString());
        }
    }

    @Test
    public void testWriteCharArray() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.write(new char[] { 'F', 'o', 'o' });
            assertEquals("Foo", writer.toString());
            writer.write(new char[] { 'B', 'a', 'r' });
            assertEquals("FooBar", writer.toString());
        }
    }

    @Test
    public void testWriteCharArrayPortion() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
        writer.write(FOOBAR_CHARS, 3, 3);
        assertEquals("Bar", writer.toString());
        writer.write(FOOBAR_CHARS, 0, 3);
        assertEquals("BarFoo", writer.toString());
        }
    }

    @Test
    public void testWriteString() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.write("Foo");
            assertEquals("Foo", writer.toString());
            writer.write("Bar");
            assertEquals("FooBar", writer.toString());
        }
    }

    @Test
    public void testWriteStringPortion() throws IOException {
        try (final Writer writer = new StringBuilderWriter()) {
            writer.write("FooBar", 3, 3);
            assertEquals("Bar", writer.toString());
            writer.write("FooBar", 0, 3);
            assertEquals("BarFoo", writer.toString());
        }
    }

}
