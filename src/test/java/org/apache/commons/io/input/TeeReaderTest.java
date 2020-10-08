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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.test.ThrowOnCloseReader;
import org.apache.commons.io.test.ThrowOnCloseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TeeReader}.
 */
public class TeeReaderTest  {

    private StringBuilderWriter output;

    private Reader tee;

    @BeforeEach
    public void setUp() throws Exception {
        final Reader input = new CharSequenceReader("abc");
        output = new StringBuilderWriter();
        tee = new TeeReader(input, output);
    }

    /**
     * Tests that the main {@code Reader} is closed when closing the branch {@code Writer} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    public void testCloseBranchIOException() throws Exception {
        final StringReader goodR = mock(StringReader.class);
        final Writer badW = new ThrowOnCloseWriter();

        final TeeReader nonClosingTr = new TeeReader(goodR, badW, false);
        nonClosingTr.close();
        verify(goodR).close();

        final TeeReader closingTr = new TeeReader(goodR, badW, true);
        try {
            closingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodR, times(2)).close();
        }
    }

    /**
     * Tests that the branch {@code Writer} is closed when closing the main {@code Reader} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    public void testCloseMainIOException() throws IOException {
        final Reader badR = new ThrowOnCloseReader();
        final StringWriter goodW = mock(StringWriter.class);

        final TeeReader nonClosingTr = new TeeReader(badR, goodW, false);
        try {
            nonClosingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodW, never()).close();
        }

        final TeeReader closingTr = new TeeReader(badR, goodW, true);
        try {
            closingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            //Assert.assertTrue(goodW.closed);
            verify(goodW).close();
        }
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

    @Test
    public void testReadEverything() throws Exception {
        assertEquals('a', tee.read());
        assertEquals('b', tee.read());
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("abc", output.toString());
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

}