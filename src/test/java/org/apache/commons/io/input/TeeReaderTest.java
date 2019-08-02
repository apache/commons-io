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

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Test Case for {@link TeeReader}.
 */
public class TeeReaderTest  {

    private Reader tee;

    private StringBuilderWriter output;

    @Before
    public void setUp() throws Exception {
        final Reader input = new CharSequenceReader("abc");
        output = new StringBuilderWriter();
        tee = new TeeReader(input, output);
    }

    @Test
    public void testReadNothing() throws Exception {
        assertEquals("", output.toString());
    }

    @Test
    public void testReadOneChar() throws Exception {
        assertEquals('a', tee.read());
        assertEquals("a", output.toString());
    }

    @Test
    public void testReadEverything() throws Exception {
        assertEquals('a', tee.read());
        assertEquals('b', tee.read());
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("abc", output.toString());
    }

    @Test
    public void testReadToArray() throws Exception {
        final char[] buffer = new char[8];
        assertEquals(3, tee.read(buffer));
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals('c', buffer[2]);
        assertEquals(-1, tee.read(buffer));
        assertEquals("abc", output.toString());
    }

    @Test
    public void testReadToArrayWithOffset() throws Exception {
        final char[] buffer = new char[8];
        assertEquals(3, tee.read(buffer, 4, 4));
        assertEquals('a', buffer[4]);
        assertEquals('b', buffer[5]);
        assertEquals('c', buffer[6]);
        assertEquals(-1, tee.read(buffer, 4, 4));
        assertEquals("abc", output.toString());
    }

    @Test
    public void testReadToCharBuffer() throws Exception {
        final CharBuffer buffer = CharBuffer.allocate(8);
        buffer.position(1);
        assertEquals(3, tee.read(buffer));
        assertEquals(4, buffer.position());
        buffer.flip();
        buffer.position(1);
        assertEquals('a', buffer.charAt(0));
        assertEquals('b', buffer.charAt(1));
        assertEquals('c', buffer.charAt(2));
        assertEquals(-1, tee.read(buffer));
        assertEquals("abc", output.toString());
    }

    @Test
    public void testSkip() throws Exception {
        assertEquals('a', tee.read());
        assertEquals(1, tee.skip(1));
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("ac", output.toString());
    }

    @Test
    public void testMarkReset() throws Exception {
        assertEquals('a', tee.read());
        tee.mark(1);
        assertEquals('b', tee.read());
        tee.reset();
        assertEquals('b', tee.read());
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("abbc", output.toString());
    }

}
