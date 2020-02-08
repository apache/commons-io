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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CharSequenceReader}.
 *
 */
public class CharSequenceReaderTest {
    private static final char NONE = (new char[1])[0];

    @Test
    public void testClose() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        checkRead(reader, "Foo");
        reader.close();
        checkRead(reader, "Foo");
    }

    @Test
    public void testMarkSupported() throws Exception {
        try (final Reader reader = new CharSequenceReader("FooBar")) {
            assertTrue(reader.markSupported());
        }
    }

    @Test
    public void testMark() throws IOException {
        try (final Reader reader = new CharSequenceReader("FooBar")) {
            checkRead(reader, "Foo");
            reader.mark(0);
            checkRead(reader, "Bar");
            reader.reset();
            checkRead(reader, "Bar");
            reader.close();
            checkRead(reader, "Foo");
            reader.reset();
            checkRead(reader, "Foo");
        }
    }

    @Test
    public void testSkip() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        assertEquals(3, reader.skip(3));
        checkRead(reader, "Bar");
        assertEquals(-1, reader.skip(3));
        reader.reset();
        assertEquals(2, reader.skip(2));
        assertEquals(4, reader.skip(10));
        assertEquals(-1, reader.skip(1));
        reader.close();
        assertEquals(6, reader.skip(20));
        assertEquals(-1, reader.read());
    }

    @Test
    public void testRead() throws IOException {
        final String value = "Foo";
        testRead(value);
        testRead(new StringBuilder(value));
        testRead(new StringBuffer(value));
        testRead(CharBuffer.wrap(value));
    }

    private void testRead(final CharSequence charSequence) throws IOException {
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            assertEquals('F', reader.read());
            assertEquals('o', reader.read());
            assertEquals('o', reader.read());
            assertEquals(-1, reader.read());
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testReadCharArray() throws IOException {
        final String value = "FooBar";
        testReadCharArray(value);
        testReadCharArray(new StringBuilder(value));
        testReadCharArray(new StringBuffer(value));
        testReadCharArray(CharBuffer.wrap(value));
    }

    private void testReadCharArray(final CharSequence charSequence) throws IOException {
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            char[] chars = new char[2];
            assertEquals(2, reader.read(chars));
            checkArray(new char[] { 'F', 'o' }, chars);
            chars = new char[3];
            assertEquals(3, reader.read(chars));
            checkArray(new char[] { 'o', 'B', 'a' }, chars);
            chars = new char[3];
            assertEquals(1, reader.read(chars));
            checkArray(new char[] { 'r', NONE, NONE }, chars);
            assertEquals(-1, reader.read(chars));
        }
    }

    @Test
    public void testReadCharArrayPortion() throws IOException {
        final String value = "FooBar";
        testReadCharArrayPortion(value);
        testReadCharArrayPortion(new StringBuilder(value));
        testReadCharArrayPortion(new StringBuffer(value));
        testReadCharArrayPortion(CharBuffer.wrap(value));
    }

    private void testReadCharArrayPortion(final CharSequence charSequence) throws IOException {
        final char[] chars = new char[10];
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            assertEquals(3, reader.read(chars, 3, 3));
            checkArray(new char[] { NONE, NONE, NONE, 'F', 'o', 'o' }, chars);
            assertEquals(3, reader.read(chars, 0, 3));
            checkArray(new char[] { 'B', 'a', 'r', 'F', 'o', 'o', NONE }, chars);
            assertEquals(-1, reader.read(chars));
        }
    }

    private void checkRead(final Reader reader, final String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.charAt(i), (char)reader.read(), "Read[" + i + "] of '" + expected + "'");
        }
    }

    private void checkArray(final char[] expected, final char[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], "Compare[" +i + "]");
        }
    }
}
