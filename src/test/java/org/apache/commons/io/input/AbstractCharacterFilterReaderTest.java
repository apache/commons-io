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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractCharacterFilterReaderTest {

    private static Reader createChunkedReader(final String input, final int chunkSize) {
        return new FilterReader(new StringReader(input)) {
            @Override
            public int read(final char[] buffer, final int offset, final int length) throws IOException {
                return super.read(buffer, offset, Math.min(length, chunkSize));
            }
        };
    }

    private static Reader createFailingReader(final String prefix, final IOException failure) {
        return new FilterReader(new StringReader(prefix)) {
            @Override
            public int read(final char[] buffer, final int offset, final int length) throws IOException {
                final int count = super.read(buffer, offset, length);
                if (count == EOF) {
                    throw failure;
                }
                return count;
            }
        };
    }

    private static List<String> inputs() {
        // Every string of up to five characters over one discarded and two retained characters.
        final List<String> inputs = new ArrayList<>();
        List<String> level = Arrays.asList("");
        inputs.addAll(level);
        for (int length = 1; length <= 5; length++) {
            final List<String> next = new ArrayList<>();
            for (final String prefix : level) {
                for (final char ch : new char[] {'x', 'a', 'b'}) {
                    next.add(prefix + ch);
                }
            }
            inputs.addAll(next);
            level = next;
        }
        return inputs;
    }

    private Reader createReader(final Reader input, final boolean filterSet) {
        return filterSet ? new CharacterSetFilterReader(input, Integer.valueOf('x')) : new CharacterFilterReader(input, 'x');
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testInterleavedReadAndSkip(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxbxcxdxex"), filterSet)) {
            assertEquals('a', reader.read());
            assertEquals(1, reader.skip(1));
            final char[] buffer = new char[1];
            assertEquals(1, reader.read(buffer));
            assertEquals('c', buffer[0]);
            assertEquals(1, reader.skip(1));
            assertEquals('e', reader.read());
            assertEquals(0, reader.skip(1));
            assertEquals(EOF, reader.read(buffer));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testMarkResetAfterSkip(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxbxc"), filterSet)) {
            reader.mark(6);
            assertEquals(2, reader.skip(2));
            assertEquals('c', reader.read());
            reader.reset();
            assertEquals("abc", IOUtils.toString(reader));
        }
    }

    @Test
    void testNoFilter() throws IOException {
        try (Reader reader = new AbstractCharacterFilterReader(new StringReader("xab")) { }) {
            assertEquals(1, reader.skip(1));
            assertEquals("ab", IOUtils.toString(reader));
        }
        try (Reader reader = new CharacterFilterReader(new StringReader("xab"), (IntPredicate) null)) {
            final char[] buffer = new char[2];
            assertEquals(2, reader.read(buffer));
            assertArrayEquals(new char[] {'x', 'a'}, buffer);
            assertEquals(1, reader.skip(1));
            assertEquals(EOF, reader.read());
        }
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
    void testReadCharBufferSlice(final boolean filterSet) throws IOException {
        final char[] chars = {'-', '-', '-', '-', '-'};
        final CharBuffer buffer = CharBuffer.wrap(chars, 1, 3).slice();
        buffer.position(1);
        try (Reader reader = createReader(new StringReader("xxab"), filterSet)) {
            assertEquals(2, reader.read(buffer));
            assertEquals(3, buffer.position());
            assertEquals(3, buffer.limit());
            assertArrayEquals(new char[] {'-', '-', 'a', 'b', '-'}, chars);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadClosed(final boolean filterSet) throws IOException {
        final Reader reader = createReader(new StringReader("xa"), filterSet);
        reader.close();
        assertThrows(IOException.class, reader::read);
        assertThrows(IOException.class, () -> reader.read(new char[2]));
        assertThrows(IOException.class, () -> reader.read(new char[2], 1, 0));
        assertThrows(IOException.class, () -> reader.read(CharBuffer.allocate(2)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadCombinations(final boolean filterSet) throws IOException {
        for (final String input : inputs()) {
            final String expected = input.replace("x", "");
            for (final int chunkSize : new int[] {1, 2, 3, 8}) {
                for (final int length : new int[] {1, 2, 3, 8}) {
                    for (final int offset : new int[] {0, 2}) {
                        final String context = "input=" + input + ", chunkSize=" + chunkSize + ", length=" + length + ", offset=" + offset;
                        try (Reader reader = createReader(createChunkedReader(input, chunkSize), filterSet)) {
                            final char[] buffer = new char[offset + length + 1];
                            final StringBuilder actual = new StringBuilder();
                            int count;
                            do {
                                Arrays.fill(buffer, '-');
                                count = reader.read(buffer, offset, length);
                                for (int i = 0; i < offset; i++) {
                                    assertEquals('-', buffer[i], context);
                                }
                                assertEquals('-', buffer[offset + length], context);
                                if (count != EOF) {
                                    assertTrue(count > 0 && count <= length, context);
                                    actual.append(buffer, offset, count);
                                    assertTrue(actual.length() <= expected.length(), context);
                                }
                            } while (count != EOF);
                            assertEquals(expected, actual.toString(), context);
                            assertEquals(EOF, reader.read(buffer, offset, length), context);
                        }
                    }
                }
            }
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
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 4, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, -1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, 1, Integer.MAX_VALUE));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(buffer, Integer.MAX_VALUE, 1));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 1));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 0));
            assertEquals('a', reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadIOException(final boolean filterSet) throws IOException {
        for (final String prefix : new String[] {"", "xxxx"}) {
            final IOException failure = new IOException("read failure");
            try (Reader reader = createReader(createFailingReader(prefix, failure), filterSet)) {
                assertSame(failure, assertThrows(IOException.class, () -> reader.read(new char[2])));
            }
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
    void testReadNestedFilters(final boolean filterSet) throws IOException {
        for (final boolean innerFilterSet : new boolean[] {false, true}) {
            final Reader input = new StringReader("xxyyxaybxycxy");
            final Reader inner = innerFilterSet ? new CharacterSetFilterReader(input, Integer.valueOf('y')) : new CharacterFilterReader(input, 'y');
            try (Reader reader = createReader(inner, filterSet)) {
                final char[] buffer = new char[2];
                final StringBuilder actual = new StringBuilder();
                int count;
                while ((count = reader.read(buffer)) != EOF) {
                    assertTrue(count > 0);
                    actual.append(buffer, 0, count);
                }
                assertEquals("abc", actual.toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadReturnsBeforeLaterIOException(final boolean filterSet) throws IOException {
        final IOException failure = new IOException("read failure");
        try (Reader reader = createReader(createFailingReader("xa", failure), filterSet)) {
            final char[] buffer = new char[4];
            assertEquals(1, reader.read(buffer));
            assertEquals('a', buffer[0]);
            assertSame(failure, assertThrows(IOException.class, () -> reader.read(buffer)));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadShortDelegate(final boolean filterSet) throws IOException {
        final Reader input = createChunkedReader("xxxab", 1);
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
    void testReadUnicode(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(createChunkedReader("x\u0000x\uFFFFx\uD83D\uDE00x", 1), filterSet)) {
            assertEquals("\u0000\uFFFF\uD83D\uDE00", IOUtils.toString(reader));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testReadZeroLength(final boolean filterSet) throws IOException {
        final StringReader input = new StringReader("xxa");
        try (Reader reader = createReader(input, filterSet)) {
            final char[] buffer = {'-', '-'};
            assertEquals(0, reader.read(buffer, 1, 0));
            assertEquals(0, reader.read(buffer, buffer.length, 0));
            assertEquals(0, reader.read(new char[0]));
            assertEquals(0, reader.read(CharBuffer.allocate(0)));
            assertArrayEquals(new char[] {'-', '-'}, buffer);
            assertEquals('x', input.read());
            assertEquals('a', reader.read());
            assertEquals(EOF, reader.read());
            assertEquals(0, reader.read(buffer, 1, 0));
            assertEquals(0, reader.read(new char[0]));
            assertEquals(0, reader.read(CharBuffer.allocate(0)));
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
    void testSkipClosed(final boolean filterSet) throws IOException {
        final Reader reader = createReader(new StringReader("xa"), filterSet);
        reader.close();
        assertThrows(IOException.class, () -> reader.skip(0));
        assertThrows(IOException.class, () -> reader.skip(1));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipCombinations(final boolean filterSet) throws IOException {
        for (final String input : inputs()) {
            final String expected = input.replace("x", "");
            for (final int chunkSize : new int[] {1, 2, 3, 8}) {
                for (int count = 0; count <= expected.length() + 1; count++) {
                    final String context = "input=" + input + ", chunkSize=" + chunkSize + ", count=" + count;
                    try (Reader reader = createReader(createChunkedReader(input, chunkSize), filterSet)) {
                        final int skipped = Math.min(count, expected.length());
                        assertEquals(skipped, reader.skip(count), context);
                        assertEquals(expected.substring(skipped), IOUtils.toString(reader), context);
                    }
                }
            }
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
    void testSkipIOException(final boolean filterSet) throws IOException {
        for (final String prefix : new String[] {"", "xxxx", "xax"}) {
            final IOException failure = new IOException("skip failure");
            try (Reader reader = createReader(createFailingReader(prefix, failure), filterSet)) {
                assertSame(failure, assertThrows(IOException.class, () -> reader.skip(2)));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipLarge(final boolean filterSet) throws IOException {
        for (final int count : new int[] {IOUtils.DEFAULT_BUFFER_SIZE - 1, IOUtils.DEFAULT_BUFFER_SIZE, IOUtils.DEFAULT_BUFFER_SIZE + 1,
            2 * IOUtils.DEFAULT_BUFFER_SIZE + 1}) {
            final StringBuilder input = new StringBuilder();
            for (int i = 0; i < count; i++) {
                input.append("xa");
            }
            input.append("xbc");
            try (Reader reader = createReader(new StringReader(input.toString()), filterSet)) {
                assertEquals(count, reader.skip(count));
                assertEquals("bc", IOUtils.toString(reader));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipNegative(final boolean filterSet) throws IOException {
        try (Reader reader = createReader(new StringReader("xaxb"), filterSet)) {
            assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
            assertThrows(IllegalArgumentException.class, () -> reader.skip(Long.MIN_VALUE));
            assertEquals('a', reader.read());
            assertThrows(IllegalArgumentException.class, () -> reader.skip(-1));
            assertThrows(IllegalArgumentException.class, () -> reader.skip(Long.MIN_VALUE));
            assertEquals('b', reader.read());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipNestedFilters(final boolean filterSet) throws IOException {
        for (final boolean innerFilterSet : new boolean[] {false, true}) {
            final Reader input = new StringReader("xxyyxaybxycxy");
            final Reader inner = innerFilterSet ? new CharacterSetFilterReader(input, Integer.valueOf('y')) : new CharacterFilterReader(input, 'y');
            try (Reader reader = createReader(inner, filterSet)) {
                assertEquals(2, reader.skip(2));
                assertEquals('c', reader.read());
                assertEquals(0, reader.skip(1));
            }
        }
    }

    @Test
    void testSkipOverriddenFilter() throws IOException {
        try (Reader reader = new AbstractCharacterFilterReader(new StringReader("xaxbx")) {
            @Override
            protected boolean filter(final int ch) {
                return ch == 'x';
            }
        }) {
            assertEquals(1, reader.skip(1));
            assertEquals("b", IOUtils.toString(reader));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipPositiveDoesNotDelegate(final boolean filterSet) throws IOException {
        final Reader input = new FilterReader(new StringReader("xaxbxc")) {
            @Override
            public long skip(final long n) {
                throw new AssertionError("Positive skips must read and filter the input.");
            }
        };
        try (Reader reader = createReader(input, filterSet)) {
            assertEquals(2, reader.skip(2));
            assertEquals('c', reader.read());
        }
    }

    @Test
    void testSkipPredicate() throws IOException {
        final StringBuilder evaluated = new StringBuilder();
        try (Reader reader = new CharacterFilterReader(new StringReader("xaxbxc"), ch -> {
            assertTrue(ch != EOF);
            evaluated.append((char) ch);
            return ch == 'x';
        })) {
            assertEquals(0, reader.skip(0));
            assertEquals("", evaluated.toString());
            assertEquals(2, reader.skip(2));
            assertEquals("xaxb", evaluated.toString());
            assertEquals("c", IOUtils.toString(reader));
            assertEquals("xaxbxc", evaluated.toString());
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
            assertEquals(EOF, reader.read());
            assertEquals(0, reader.skip(0));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSkipZeroDelegates(final boolean filterSet) throws IOException {
        final IOException failure = new IOException("zero skip failure");
        final Reader input = new FilterReader(new StringReader("xa")) {
            @Override
            public long skip(final long n) throws IOException {
                throw failure;
            }
        };
        try (Reader reader = createReader(input, filterSet)) {
            assertSame(failure, assertThrows(IOException.class, () -> reader.skip(0)));
            assertEquals('x', input.read());
        }
    }
}
