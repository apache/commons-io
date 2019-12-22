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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for {@link SequenceReader}.
 */
public class SequenceReaderTest {
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
        final Reader reader = new CharSequenceReader("FooBar");
        assertTrue(reader.markSupported());
        reader.close();
    }

    @Test
    public void testSkip() throws IOException {
        final Reader reader = new SequenceReader(new Reader[]{
                new StringReader("Foo"),
                new StringReader("Bar")
        });
        assertEquals(3, reader.skip(3));
        checkRead(reader, "Bar");
        assertEquals(0, reader.skip(3));
    }

    @Test
    public void testRead() throws IOException {
        final Reader reader = new SequenceReader(new Reader[]{
                new StringReader("Foo"),
                new StringReader("Bar")
        });
        assertEquals('F', reader.read());
        assertEquals('o', reader.read());
        assertEquals('o', reader.read());
        assertEquals('B', reader.read());
        assertEquals('a', reader.read());
        assertEquals('r', reader.read());
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read());
        reader.close();
    }

    @Test
    public void testReadList() throws IOException {
        List<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        final Reader reader = new SequenceReader(readers);
        assertEquals('F', reader.read());
        assertEquals('B', reader.read());
        assertEquals(-1, reader.read());
        reader.close();
    }

    @Test
    public void testReadCharArray() throws IOException {
        final Reader reader = new SequenceReader(new Reader[]{
                new StringReader("Foo"),
                new StringReader("Bar")
        });
        char[] chars = new char[2];
        assertEquals(2, reader.read(chars));
        checkArray(new char[]{'F', 'o'}, chars);
        chars = new char[3];
        assertEquals(3, reader.read(chars));
        checkArray(new char[]{'o', 'B', 'a'}, chars);
        chars = new char[3];
        assertEquals(1, reader.read(chars));
        checkArray(new char[]{'r', NONE, NONE}, chars);
        assertEquals(-1, reader.read(chars));
        reader.close();
    }

    @Test
    public void testReadCharArrayPortion() throws IOException {
        final char[] chars = new char[10];
        final Reader reader = new SequenceReader(new Reader[]{
                new StringReader("Foo"),
                new StringReader("Bar")
        });
        assertEquals(3, reader.read(chars, 3, 3));
        checkArray(new char[]{NONE, NONE, NONE, 'F', 'o', 'o'}, chars);
        assertEquals(3, reader.read(chars, 0, 3));
        checkArray(new char[]{'B', 'a', 'r', 'F', 'o', 'o', NONE}, chars);
        assertEquals(-1, reader.read(chars));
        assertThrows(IndexOutOfBoundsException.class, () -> reader.read(chars, 10, 10));
        assertThrows(NullPointerException.class, () -> reader.read(null, 0, 10));
        reader.close();
    }

    private void checkRead(final Reader reader, final String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.charAt(i), (char) reader.read(),
                    "Read[" + i + "] of '" + expected + "'");
        }
    }

    private void checkArray(final char[] expected, final char[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], "Compare[" + i + "]");
        }
    }
}
