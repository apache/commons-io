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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;

import org.apache.commons.io.TestResources;
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

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        checkRead(subReader, "Foo");
        subReader.close();
        checkRead(subReader, "Foo");
    }

    @Test
    public void testReady() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        assertTrue(reader.ready());
        reader.skip(3);
        assertTrue(reader.ready());
        checkRead(reader, "Bar");
        assertFalse(reader.ready());
        reader.reset();
        assertTrue(reader.ready());
        reader.skip(2);
        assertTrue(reader.ready());
        reader.skip(10);
        assertFalse(reader.ready());
        reader.close();
        assertTrue(reader.ready());
        reader.skip(20);
        assertFalse(reader.ready());

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        assertTrue(subReader.ready());
        subReader.skip(3);
        assertTrue(subReader.ready());
        checkRead(subReader, "Bar");
        assertFalse(subReader.ready());
        subReader.reset();
        assertTrue(subReader.ready());
        subReader.skip(2);
        assertTrue(subReader.ready());
        subReader.skip(10);
        assertFalse(subReader.ready());
        subReader.close();
        assertTrue(subReader.ready());
        subReader.skip(20);
        assertFalse(subReader.ready());
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
        try (final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7)) {
            checkRead(subReader, "Foo");
            subReader.mark(0);
            checkRead(subReader, "Bar");
            subReader.reset();
            checkRead(subReader, "Bar");
            subReader.close();
            checkRead(subReader, "Foo");
            subReader.reset();
            checkRead(subReader, "Foo");
        }
    }

    @Test
    public void testSkip() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        assertEquals(3, reader.skip(3));
        checkRead(reader, "Bar");
        assertEquals(0, reader.skip(3));
        reader.reset();
        assertEquals(2, reader.skip(2));
        assertEquals(4, reader.skip(10));
        assertEquals(0, reader.skip(1));
        reader.close();
        assertEquals(6, reader.skip(20));
        assertEquals(-1, reader.read());

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        assertEquals(3, subReader.skip(3));
        checkRead(subReader, "Bar");
        assertEquals(0, subReader.skip(3));
        subReader.reset();
        assertEquals(2, subReader.skip(2));
        assertEquals(4, subReader.skip(10));
        assertEquals(0, subReader.skip(1));
        subReader.close();
        assertEquals(6, subReader.skip(20));
        assertEquals(-1, subReader.read());
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
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
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
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
            char[] chars = new char[2];
            assertEquals(2, reader.read(chars));
            checkArray(new char[] { 'o', 'o' }, chars);
            chars = new char[3];
            assertEquals(2, reader.read(chars));
            checkArray(new char[] { 'B', 'a', NONE }, chars);
            chars = new char[3];
            assertEquals(-1, reader.read(chars));
            checkArray(new char[] { NONE, NONE, NONE }, chars);
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
        Arrays.fill(chars, NONE);
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
            assertEquals(2, reader.read(chars, 3, 2));
            checkArray(new char[] { NONE, NONE, NONE, 'o', 'o', NONE }, chars);
            assertEquals(2, reader.read(chars, 0, 3));
            checkArray(new char[] { 'B', 'a', NONE, 'o', 'o', NONE }, chars);
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

    @Test
    public void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new CharSequenceReader("FooBar", -1, 6),
                "Expected exception not thrown for negative start.");
        assertThrows(IllegalArgumentException.class, () -> new CharSequenceReader("FooBar", 1, 0),
                "Expected exception not thrown for end before start.");
    }

    @Test
    @SuppressWarnings("resource") // don't really need to close CharSequenceReader here
    public void testToString() {
        assertEquals("FooBar", new CharSequenceReader("FooBar").toString());
        assertEquals("FooBar", new CharSequenceReader("xFooBarx", 1, 7).toString());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        /*
         * File CharSequenceReader.bin contains a CharSequenceReader that was serialized before
         * the start and end fields were added. Its CharSequence is "FooBar".
         * This part of the test will test that adding the fields does not break any existing
         * serialized CharSequenceReaders.
         */
        try (ObjectInputStream ois = new ObjectInputStream(TestResources.getInputStream("CharSequenceReader.bin"))) {
            final CharSequenceReader reader = (CharSequenceReader) ois.readObject();
            assertEquals('F', reader.read());
            assertEquals('o', reader.read());
            assertEquals('o', reader.read());
            assertEquals('B', reader.read());
            assertEquals('a', reader.read());
            assertEquals('r', reader.read());
            assertEquals(-1, reader.read());
            assertEquals(-1, reader.read());
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            final CharSequenceReader reader = new CharSequenceReader("xFooBarx", 1, 7);
            oos.writeObject(reader);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            final CharSequenceReader reader = (CharSequenceReader) ois.readObject();
            assertEquals('F', reader.read());
            assertEquals('o', reader.read());
            assertEquals('o', reader.read());
            assertEquals('B', reader.read());
            assertEquals('a', reader.read());
            assertEquals('r', reader.read());
            assertEquals(-1, reader.read());
            assertEquals(-1, reader.read());
            reader.reset();
            assertEquals('F', reader.read());
            assertEquals('o', reader.read());
            assertEquals('o', reader.read());
            assertEquals('B', reader.read());
            assertEquals('a', reader.read());
            assertEquals('r', reader.read());
            assertEquals(-1, reader.read());
            assertEquals(-1, reader.read());
        }
    }
}
