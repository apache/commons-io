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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ClosedReader}.
 */
class ClosedReaderTest {

    private void assertEof(final Reader reader) throws IOException {
        assertEquals(EOF, reader.read(), "read()");
    }

    @Test
    void testRead() throws IOException {
        try (Reader reader = new ClosedReader()) {
            assertEof(reader);
        }
    }

    @Test
    void testReadArray() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(EOF, reader.read(new char[4096]));
            assertEquals(EOF, reader.read(new char[1]));
            assertEquals(0, reader.read(new char[0]));
            assertThrows(NullPointerException.class, () -> reader.read((char[]) null));
        }
    }

    @Test
    void testReadArrayIndex() throws Exception {
        try (Reader reader = new ClosedReader()) {
            final char[] cbuf = new char[4096];
            assertEquals(EOF, reader.read(cbuf, 0, 2048));
            assertEquals(EOF, reader.read(cbuf, 2048, 2048));
            assertEquals(0, reader.read(cbuf, 4096, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(cbuf, -1, 1));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(cbuf, 0, 4097));
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(cbuf, 1, -1));

            assertEquals(EOF, reader.read(new char[1]));
            assertEquals(0, reader.read(new char[0]));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 0));
        }
    }

    @Test
    void testReadCharBuffer() throws Exception {
        try (Reader reader = new ClosedReader()) {
            final CharBuffer charBuffer = CharBuffer.wrap(new char[4096]);
            assertEquals(EOF, reader.read(charBuffer));
            charBuffer.position(4096);
            assertEquals(0, reader.read(charBuffer));

            assertEquals(EOF, reader.read(CharBuffer.wrap(new char[1])));
            assertEquals(0, reader.read(CharBuffer.wrap(new char[0])));
            assertThrows(NullPointerException.class, () -> reader.read((CharBuffer) null));
        }
    }

    @Test
    void testSingleton() throws Exception {
        try (@SuppressWarnings("deprecation")
        Reader reader = ClosedReader.CLOSED_READER) {
            assertEof(reader);
        }
        try (Reader reader = ClosedReader.INSTANCE) {
            assertEof(reader);
        }
    }

    @Test
    void testSkip() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(0, reader.skip(4096));
            assertEquals(0, reader.skip(1));
            assertEquals(0, reader.skip(0));
        }
    }

}
