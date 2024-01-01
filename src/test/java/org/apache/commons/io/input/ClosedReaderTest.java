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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ClosedReader}.
 */
public class ClosedReaderTest {

    private void assertEof(final Reader reader) throws IOException {
        assertEquals(EOF, reader.read(), "read()");
    }

    @Test
    public void testRead() throws IOException {
        try (Reader reader = new ClosedReader()) {
            assertEof(reader);
        }
    }

    @Test
    public void testReadArray() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(EOF, reader.read(new char[4096]));
            assertEquals(EOF, reader.read(new char[1]));
            assertEquals(EOF, reader.read(new char[0]));
        }
    }

    @Test
    public void testReadArrayIndex() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(EOF, reader.read(CharBuffer.wrap(new char[4096])));
            assertEquals(EOF, reader.read(CharBuffer.wrap(new char[1])));
            assertEquals(EOF, reader.read(CharBuffer.wrap(new char[0])));
        }
    }

    @Test
    public void testReadCharBuffer() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(EOF, reader.read(new char[4096]));
            assertEquals(EOF, reader.read(new char[1]));
            assertEquals(EOF, reader.read(new char[0]));
        }
    }

    @Test
    public void testSingleton() throws Exception {
        try (@SuppressWarnings("deprecation")
        Reader reader = ClosedReader.CLOSED_READER) {
            assertEof(reader);
        }
        try (Reader reader = ClosedReader.INSTANCE) {
            assertEof(reader);
        }
    }

    @Test
    public void testSkip() throws Exception {
        try (Reader reader = new ClosedReader()) {
            assertEquals(0, reader.skip(4096));
            assertEquals(0, reader.skip(1));
            assertEquals(0, reader.skip(0));
        }
    }

}
