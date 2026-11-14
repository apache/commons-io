/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractCharacterFilterReaderTest {

    private Reader createReader(final Reader input, final boolean filterSet) {
        return filterSet ? new CharacterSetFilterReader(input, Integer.valueOf('x')) : new CharacterFilterReader(input, 'x');
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadAfterFilteredChunk(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xxxa"), filterSet)) {
            final char[] buffer = {'-', '-', '-', '-'};
            assertEquals(1, reader.read(buffer, 1, 2));
            assertEquals('a', buffer[1]);
            assertEquals('-', buffer[0]);
            assertEquals('-', buffer[3]);
            assertEquals(EOF, reader.read(buffer, 1, 2));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadAllFiltered(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xxx"), filterSet)) {
            assertEquals(EOF, reader.read(new char[2]));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadCharBuffer(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xxxa"), filterSet)) {
            final CharBuffer buffer = CharBuffer.allocate(2);
            assertEquals(1, reader.read(buffer));
            buffer.flip();
            assertEquals("a", buffer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadEmpty(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader(""), filterSet)) {
            assertEquals(EOF, reader.read(new char[2]));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadInvalidRange(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("a"), filterSet)) {
            final char[] buffer = new char[3];
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, -1, 1));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 2, 2));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 1));
            assertEquals('a', reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadMixedChunk(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxbxx"), filterSet)) {
            final char[] buffer = new char[6];
            assertEquals(2, reader.read(buffer));
            assertEquals("ab", new String(buffer, 0, 2));
            assertEquals(EOF, reader.read(buffer));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadShortDelegate(final boolean filterSet) throws IOException {
        final Reader input = new FilterReader(new StringReader("xxxab")) {
            @Override
            public int read(final char[] buffer, final int offset, final int length) throws IOException {
                return super.read(buffer, offset, Math.min(length, 1));
            }
        };
        try (Reader reader = createReader(input, filterSet)) {
            final char[] buffer = new char[2];
            assertEquals(1, reader.read(buffer));
            assertEquals('a', buffer[0]);
            assertEquals(1, reader.read(buffer));
            assertEquals('b', buffer[0]);
            assertEquals(EOF, reader.read(buffer));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadZeroLength(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xxa"), filterSet)) {
            final char[] buffer = {'-', '-'};
            assertEquals(0, reader.read(buffer, 1, 0));
            assertEquals('-', buffer[1]);
            assertEquals('a', reader.read());
            assertEquals(EOF, reader.read());
            assertEquals(0, reader.read(buffer, 1, 0));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipAllFiltered(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xxx"), filterSet)) {
            assertEquals(0, reader.skip(1));
            assertEquals(EOF, reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipBeyondEof(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxbxx"), filterSet)) {
            assertEquals(2, reader.skip(Long.MAX_VALUE));
            assertEquals(EOF, reader.read());
            assertEquals(0, reader.skip(1));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipCountsRetainedCharacters(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxb"), filterSet)) {
            assertEquals(1, reader.skip(1));
            assertEquals('b', reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipClosed(final boolean filterSet) throws IOException {
        final Reader reader = createReader(new StringReader("xa"), filterSet);
        reader.close();
        assertThrows(IOException.class, () -> reader.skip(0));
        assertThrows(IOException.class, () -> reader.skip(1));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipNegative(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("a"), filterSet)) {
            assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
            assertThrows(IllegalArgumentException.class, () -> reader.skip(Long.MIN_VALUE));
            assertEquals('a', reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipZero(final boolean filterSet) throws IOException {
        final StringReader input = new StringReader("xa");
        try (Reader reader = createReader(input, filterSet)) {
            assertEquals(0, reader.skip(0));
            assertEquals('x', input.read());
            assertEquals('a', reader.read());
        }
    }
}
