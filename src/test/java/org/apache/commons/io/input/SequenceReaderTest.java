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

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SequenceReader}.
 */
public class SequenceReaderTest {

    private static final char NUL = 0;

    private void checkArray(final char[] expected, final char[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], "Compare[" + i + "]");
        }
    }

    private void checkRead(final Reader reader, final String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.charAt(i), (char) reader.read(), "Read[" + i + "] of '" + expected + "'");
        }
    }

    private void checkReadEof(final Reader reader) throws IOException {
        for (int i = 0; i < 10; i++) {
            assertEquals(-1, reader.read());
        }
    }

    @Test
    public void testAutoClose() throws IOException {
        try (Reader reader = new SequenceReader(new CharSequenceReader("FooBar"))) {
            checkRead(reader, "Foo");
            reader.close();
            checkReadEof(reader);
        }
    }

    @Test
    public void testClose() throws IOException {
        final Reader reader = new SequenceReader(new CharSequenceReader("FooBar"));
        checkRead(reader, "Foo");
        reader.close();
        checkReadEof(reader);
    }

    @Test
    public void testCloseReaders() {
        final AtomicBoolean closeEmptyReader = new AtomicBoolean(false);
        final Reader emptyReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (closeEmptyReader.get()) {
                    throw new IOException("emptyReader already closed");
                }

                return EOF;
            }

            @Override
            public void close() throws IOException {
                closeEmptyReader.set(true);
            }
        };

        final AtomicBoolean closeReader1 = new AtomicBoolean(false);
        final Reader reader1 = new Reader() {
            private final char[] content = new char[]{'A'};
            private int position = 0;

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (closeReader1.get()) {
                    throw new IOException("reader1 already closed");
                }

                if (off < 0) {
                    throw new IndexOutOfBoundsException("off is negative");
                } else if (len < 0) {
                    throw new IndexOutOfBoundsException("len is negative");
                } else if (len > cbuf.length - off) {
                    throw new IndexOutOfBoundsException("len is greater than cbuf.length - off");
                }

                if (position > 0) {
                    return EOF;
                }

                cbuf[off] = content[0];
                position++;
                return 1;
            }

            @Override
            public void close() throws IOException {
                closeReader1.set(true);
            }
        };

        try (SequenceReader sequenceReader = new SequenceReader(reader1, emptyReader)) {
            assertEquals('A', sequenceReader.read());
            assertEquals(EOF, sequenceReader.read());
        } catch (IOException e) {
            fail("No IOException expected");
        } finally {
            assertTrue(closeEmptyReader.get());
            assertTrue(closeReader1.get());
        }
    }

    @Test
    public void testMarkSupported() throws Exception {
        try (Reader reader = new SequenceReader()) {
            assertFalse(reader.markSupported());
        }
    }

    @Test
    public void testRead() throws IOException {
        try (Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertEquals('F', reader.read());
            assertEquals('o', reader.read());
            assertEquals('o', reader.read());
            assertEquals('B', reader.read());
            assertEquals('a', reader.read());
            assertEquals('r', reader.read());
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadCharArray() throws IOException {
        try (Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            char[] chars = new char[2];
            assertEquals(2, reader.read(chars));
            checkArray(new char[] { 'F', 'o' }, chars);
            chars = new char[3];
            assertEquals(3, reader.read(chars));
            checkArray(new char[] { 'o', 'B', 'a' }, chars);
            chars = new char[3];
            assertEquals(1, reader.read(chars));
            checkArray(new char[] { 'r', NUL, NUL }, chars);
            assertEquals(-1, reader.read(chars));
        }
    }

    @Test
    public void testReadCharArrayPortion() throws IOException {
        final char[] chars = new char[10];
        try (Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertEquals(3, reader.read(chars, 3, 3));
            checkArray(new char[] { NUL, NUL, NUL, 'F', 'o', 'o' }, chars);
            assertEquals(3, reader.read(chars, 0, 3));
            checkArray(new char[] { 'B', 'a', 'r', 'F', 'o', 'o', NUL }, chars);
            assertEquals(-1, reader.read(chars));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(chars, 10, 10));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 10));
        }
    }

    @Test
    public void testReadClosedReader() throws IOException {
        @SuppressWarnings("resource")
        final Reader reader = new SequenceReader(new CharSequenceReader("FooBar"));
        reader.close();
        checkReadEof(reader);
    }

    @Test
    public void testReadCollection() throws IOException {
        final Collection<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        try (Reader reader = new SequenceReader(readers)) {
            assertEquals('F', reader.read());
            assertEquals('B', reader.read());
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadIterable() throws IOException {
        final Collection<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        final Iterable<Reader> iterable = readers;
        try (Reader reader = new SequenceReader(iterable)) {
            assertEquals('F', reader.read());
            assertEquals('B', reader.read());
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadLength0Readers() throws IOException {
        try (Reader reader = new SequenceReader(new StringReader(StringUtils.EMPTY),
            new StringReader(StringUtils.EMPTY), new StringReader(StringUtils.EMPTY))) {
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadLength1Readers() throws IOException {
        try (Reader reader = new SequenceReader(
        // @formatter:off
            new StringReader("0"),
            new StringReader("1"),
            new StringReader("2"),
            new StringReader("3"))) {
            // @formatter:on
            assertEquals('0', reader.read());
            assertEquals('1', reader.read());
            assertEquals('2', reader.read());
            assertEquals('3', reader.read());
        }
    }

    @Test
    public void testReadList() throws IOException {
        final List<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        try (Reader reader = new SequenceReader(readers)) {
            assertEquals('F', reader.read());
            assertEquals('B', reader.read());
            checkReadEof(reader);
        }
    }

    @Test
    public void testSkip() throws IOException {
        try (Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertEquals(3, reader.skip(3));
            checkRead(reader, "Bar");
            assertEquals(0, reader.skip(3));
        }
    }
}