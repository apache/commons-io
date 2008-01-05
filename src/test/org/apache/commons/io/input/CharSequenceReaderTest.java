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

import java.io.IOException;
import java.io.Reader;

import junit.framework.TestCase;

/**
 * Test case for {@link CharSequenceReader}.
 *
 * @version $Revision$ $Date$
 */
public class CharSequenceReaderTest extends TestCase {
    private static final char NONE = (new char[1])[0];

    /**
     * Contruct a new test case.
     * @param name The name of the test
     */
    public CharSequenceReaderTest(String name) {
        super(name);
    }

    /** Test {@link Reader#close()}. */
    public void testClose() throws IOException {
        Reader reader = new CharSequenceReader("FooBar");
        checkRead(reader, "Foo");
        reader.close();
        checkRead(reader, "Foo");
    }

    /** Test {@link Reader#markSupported()}. */
    public void testMarkSupported() throws IOException {
        Reader reader = new CharSequenceReader("FooBar");
        assertTrue(reader.markSupported());
    }

    /** Test {@link Reader#mark(int)}. */
    public void testMark() throws IOException {
        Reader reader = new CharSequenceReader("FooBar");
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

    /** Test {@link Reader#skip(int)}. */
    public void testSkip() throws IOException {
        Reader reader = new CharSequenceReader("FooBar");
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

    /** Test {@link Reader#read()}. */
    public void testRead() throws IOException {
        Reader reader = new CharSequenceReader("Foo");
        assertEquals('F', reader.read());
        assertEquals('o', reader.read());
        assertEquals('o', reader.read());
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read());
    }

    /** Test {@link Reader#read(char[])}. */
    public void testReadCharArray() throws IOException {
        Reader reader = new CharSequenceReader("FooBar");
        char[] chars = new char[2];
        assertEquals(2, reader.read(chars));
        checkArray(new char[] {'F', 'o'}, chars);
        chars = new char[3];
        assertEquals(3, reader.read(chars));
        checkArray(new char[] {'o', 'B', 'a'}, chars);
        chars = new char[3];
        assertEquals(1, reader.read(chars));
        checkArray(new char[] {'r', NONE, NONE}, chars);
        assertEquals(-1, reader.read(chars));
    }

    /** Test {@link Reader#read(char[], int, int)}. */
    public void testReadCharArrayPortion() throws IOException {
        char[] chars = new char[10];
        Reader reader = new CharSequenceReader("FooBar");
        assertEquals(3, reader.read(chars, 3, 3));
        checkArray(new char[] {NONE, NONE, NONE, 'F', 'o', 'o'}, chars);
        assertEquals(3, reader.read(chars, 0, 3));
        checkArray(new char[] {'B', 'a', 'r', 'F', 'o', 'o', NONE}, chars);
        assertEquals(-1, reader.read(chars));
    }

    private void checkRead(Reader reader, String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertEquals("Read[" + i + "] of '" + expected + "'", 
                    (char)expected.charAt(i), (char)reader.read());
        }
    }
    private void checkArray(char[] expected, char[] actual) throws IOException {
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Compare[" +i + "]", expected[i], actual[i]);
        }
    }
}
