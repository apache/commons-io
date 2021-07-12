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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UncheckedFilterReader}.
 */
public class UncheckedBufferedReaderTest {

    private UncheckedBufferedReader ucStringReader;
    private UncheckedBufferedReader ucBrokenReader;
    private IOException exception = new IOException("test exception");

    @SuppressWarnings("resource")
    @BeforeEach
    public void beforeEach() {
        ucStringReader = UncheckedBufferedReader.on(new StringReader("01"));
        exception = new IOException("test exception");
        ucBrokenReader = UncheckedBufferedReader.on(new BrokenReader(exception));
    }

    @Test
    public void testBufferSize() {
        try (UncheckedBufferedReader uncheckedReader = new UncheckedBufferedReader(new StringReader("0123456789"), 2)) {
            assertEquals('0', uncheckedReader.read());
        }
    }

    @Test
    public void testClose() {
        ucStringReader.close();
        assertThrows(UncheckedIOException.class, () -> ucBrokenReader.read());
    }

    @Test
    public void testCloseThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.close()).getCause());
    }

    @Test
    public void testMarkReset() {
        ucStringReader.mark(10);
        final int c = ucStringReader.read();
        ucStringReader.reset();
        assertEquals(c, ucStringReader.read());
    }

    @Test
    public void testMarkThrows() {
        try (UncheckedBufferedReader closedReader = UncheckedBufferedReader.on(ClosedReader.INSTANCE)) {
            closedReader.close();
            assertThrows(UncheckedIOException.class, () -> closedReader.mark(1));
        }
    }

    @Test
    public void testRead() {
        try (final UncheckedBufferedReader uncheckedReader = UncheckedBufferedReader.on(ucStringReader)) {
            assertEquals('0', uncheckedReader.read());
            assertEquals('1', uncheckedReader.read());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
        }
    }

    @Test
    public void testReadCharArray() {
        try (final UncheckedBufferedReader uncheckedReader = UncheckedBufferedReader.on(ucStringReader)) {
            final char[] array = new char[1];
            assertEquals(1, uncheckedReader.read(array));
            assertEquals('0', array[0]);
            array[0] = 0;
            assertEquals(1, uncheckedReader.read(array));
            assertEquals('1', array[0]);
            array[0] = 0;
            assertEquals(IOUtils.EOF, uncheckedReader.read(array));
            assertEquals(0, array[0]);
            assertEquals(IOUtils.EOF, uncheckedReader.read(array));
            assertEquals(0, array[0]);
        }
    }

    @Test
    public void testReadCharArrayIndexed() {
        try (final UncheckedBufferedReader uncheckedReader = UncheckedBufferedReader.on(ucStringReader)) {
            final char[] array = new char[1];
            assertEquals(1, uncheckedReader.read(array, 0, 1));
            assertEquals('0', array[0]);
            array[0] = 0;
            assertEquals(1, uncheckedReader.read(array, 0, 1));
            assertEquals('1', array[0]);
            array[0] = 0;
            assertEquals(IOUtils.EOF, uncheckedReader.read(array, 0, 1));
            assertEquals(0, array[0]);
            assertEquals(IOUtils.EOF, uncheckedReader.read(array, 0, 1));
            assertEquals(0, array[0]);
        }
    }

    @Test
    public void testReadCharArrayIndexedThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.read(new char[1], 0, 1)).getCause());
    }

    @Test
    public void testReadCharArrayThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.read(new char[1])).getCause());
    }

    @Test
    public void testReadCharBuffer() {
        try (final UncheckedBufferedReader uncheckedReader = UncheckedBufferedReader.on(ucStringReader)) {
            final CharBuffer buffer = CharBuffer.wrap(new char[1]);
            assertEquals(1, uncheckedReader.read(buffer));
            buffer.flip();
            assertEquals('0', buffer.charAt(0));
            buffer.put(0, (char) 0);
            assertEquals(1, uncheckedReader.read(buffer));
            buffer.flip();
            assertEquals('1', buffer.charAt(0));
            buffer.put(0, (char) 0);
            assertEquals(IOUtils.EOF, uncheckedReader.read(buffer));
            buffer.flip();
            assertEquals(0, buffer.length());
            assertEquals(0, uncheckedReader.read(buffer));
            buffer.flip();
            assertEquals(0, buffer.length());
        }
    }

    @Test
    public void testReadCharBufferThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.read(CharBuffer.wrap(new char[1]))).getCause());
    }

    @Test
    public void testReadLine() {
        try (final UncheckedBufferedReader uncheckedReader = UncheckedBufferedReader.on(ucStringReader)) {
            assertEquals("01", uncheckedReader.readLine());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
        }
    }

    @Test
    public void testReadLineThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.readLine()).getCause());
    }

    @Test
    public void testReadThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.read()).getCause());
    }

    @Test
    public void testReady() {
        assertTrue(ucStringReader.ready());
    }

    @Test
    public void testReadyThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.ready()).getCause());
    }

    @Test
    public void testResetThrows() {
        try (UncheckedBufferedReader closedReader = UncheckedBufferedReader.on(ClosedReader.INSTANCE)) {
            closedReader.close();
            assertThrows(UncheckedIOException.class, () -> ucBrokenReader.reset());
        }
    }

    @Test
    public void testSkip() {
        assertEquals(1, ucStringReader.skip(1));
    }

    @Test
    public void testSkipThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> ucBrokenReader.skip(1)).getCause());
    }

}
