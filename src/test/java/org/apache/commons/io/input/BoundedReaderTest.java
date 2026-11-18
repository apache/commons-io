/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.TempFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link BoundedReader}.
 */
class BoundedReaderTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final String STRING_END_NO_EOL = "0\n1\n2";

    private static final String STRING_END_EOL = "0\n1\n2\n";

    private final Reader bufReader1 = new BufferedReader(new StringReader("01234567890"));

    private final Reader bufReader0 = new BufferedReader(new StringReader("01"));

    @Test
    void testCloseTest() throws IOException {
        final AtomicBoolean closed = new AtomicBoolean();
        try (Reader sr = new BufferedReader(new StringReader("01234567890")) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        }) {

            try (BoundedReader mr = new BoundedReader(sr, 3)) {
                // nothing
            }
        }
        assertTrue(closed.get());
    }

    private void testLineNumberReader(final Reader source) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new BoundedReader(source, 10_000_000))) {
            while (reader.readLine() != null) {
                // noop
            }
        }
    }

    void testLineNumberReaderAndFileReaderLastLine(final String data) throws IOException {
        try (TempFile path = TempFile.create(getClass().getSimpleName(), ".txt")) {
            final File file = path.toFile();
            FileUtils.write(file, data, StandardCharsets.ISO_8859_1);
            try (Reader source = Files.newBufferedReader(file.toPath())) {
                testLineNumberReader(source);
            }
        }
    }

    @Test
    void testLineNumberReaderAndFileReaderLastLineEolNo() {
        assertTimeout(TIMEOUT, () -> testLineNumberReaderAndFileReaderLastLine(STRING_END_NO_EOL));
    }

    @Test
    void testLineNumberReaderAndFileReaderLastLineEolYes() {
        assertTimeout(TIMEOUT, () -> testLineNumberReaderAndFileReaderLastLine(STRING_END_EOL));
    }

    @Test
    void testLineNumberReaderAndStringReaderLastLineEolNo() {
        assertTimeout(TIMEOUT, () -> testLineNumberReader(new StringReader(STRING_END_NO_EOL)));
    }

    @Test
    void testLineNumberReaderAndStringReaderLastLineEolYes() {
        assertTimeout(TIMEOUT, () -> testLineNumberReader(new StringReader(STRING_END_EOL)));
    }

    @Test
    void testMarkReset() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.mark(3);
            mr.read();
            mr.read();
            mr.read();
            mr.reset();
            mr.read();
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 5 })
    void testMarkResetAfterInitialRead(final int offset) throws IOException {
        try (BoundedReader mr = new BoundedReader(new BufferedReader(new StringReader("01234567890"), 2), 10)) {
            for (int i = 0; i < offset; i++) {
                assertEquals('0' + i, mr.read());
            }
            mr.mark(2);
            for (int i = 0; i < 2; i++) {
                assertEquals('0' + offset, mr.read());
                assertEquals('1' + offset, mr.read());
                assertEquals(-1, mr.read());
                mr.reset();
            }
        }
    }

    @Test
    void testMarkResetAfterInitialSkip() throws IOException {
        try (BoundedReader mr = new BoundedReader(new BufferedReader(new StringReader("01234567890"), 2), 10)) {
            assertEquals(5, mr.skip(5));
            mr.mark(2);
            final char[] cbuf = new char[4];
            assertEquals(2, mr.read(cbuf, 0, cbuf.length));
            assertEquals('5', cbuf[0]);
            assertEquals('6', cbuf[1]);
            assertEquals(-1, mr.read());
            mr.reset();
            assertEquals('5', mr.read());
        }
    }

    @Test
    void testMarkResetFromOffset1() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.mark(3);
            mr.read();
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
            mr.reset();
            mr.mark(1);
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testMarkResetMarkMore() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.mark(4);
            mr.read();
            mr.read();
            mr.read();
            mr.reset();
            mr.read();
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testMarkResetWithMarkOutsideBoundedReaderMax() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.mark(4);
            mr.read();
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testMarkResetWithMarkOutsideBoundedReaderMaxAndInitialOffset() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            assertEquals('0', mr.read());
            mr.mark(3);
            for (int i = 0; i < 2; i++) {
                assertEquals('1', mr.read());
                assertEquals('2', mr.read());
                assertEquals(-1, mr.read());
                mr.reset();
            }
        }
    }

    @Test
    void testReadAfterSkipWithinReadAheadLimit() throws IOException {
        try (BoundedReader mr = new BoundedReader(new BufferedReader(new StringReader("01234567890"), 2), 10)) {
            assertEquals('0', mr.read());
            mr.mark(4);
            assertEquals(2, mr.skip(2));
            assertEquals('3', mr.read());
            assertEquals('4', mr.read());
            assertEquals(-1, mr.read());
            mr.reset();
            assertEquals('1', mr.read());
        }
    }

    @Test
    void testReadBytesEOF() {
        assertTimeout(TIMEOUT, () -> {
            final BoundedReader mr = new BoundedReader(bufReader1, 3);
            try (BufferedReader br = new BufferedReader(mr)) {
                br.readLine();
                br.readLine();
            }
        });
    }

    @Test
    void testReadMulti() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            final char[] cbuf = new char[4];
            Arrays.fill(cbuf, 'X');
            final int read = mr.read(cbuf, 0, 4);
            assertEquals(3, read);
            assertEquals('0', cbuf[0]);
            assertEquals('1', cbuf[1]);
            assertEquals('2', cbuf[2]);
            assertEquals('X', cbuf[3]);
        }
    }

    @Test
    void testReadMultiWithOffset() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            final char[] cbuf = new char[4];
            Arrays.fill(cbuf, 'X');
            final int read = mr.read(cbuf, 1, 2);
            assertEquals(2, read);
            assertEquals('X', cbuf[0]);
            assertEquals('0', cbuf[1]);
            assertEquals('1', cbuf[2]);
            assertEquals('X', cbuf[3]);
        }
    }

    @Test
    void testReadTillEnd() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.read();
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testShortReader() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader0, 3)) {
            mr.read();
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testSkipDoesNotExceedBound() throws IOException {
        try (BoundedReader mr = new BoundedReader(new StringReader("01234567890"), 3)) {
            assertEquals(3, mr.skip(100));
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testSkipDoesNotOverflowCharsRead() throws IOException {
        try (BoundedReader mr = new BoundedReader(new StringReader("01234567890"), 3)) {
            assertEquals(3, mr.skip(Long.MAX_VALUE));
            assertEquals(-1, mr.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 5 })
    void testSkipIgnoresReadAheadLimit(final int offset) throws IOException {
        try (BoundedReader mr = new BoundedReader(new StringReader("01234567890"), 10)) {
            assertEquals(offset, mr.skip(offset));
            mr.mark(2);
            assertEquals(3, mr.skip(3));
            assertEquals(7 - offset, mr.skip(Long.MAX_VALUE));
            assertEquals(0, mr.skip(1));
            mr.reset();
            assertEquals('0' + offset, mr.read());
        }
    }

    @Test
    void testSkipTest() throws IOException {
        try (BoundedReader mr = new BoundedReader(bufReader1, 3)) {
            mr.skip(2);
            mr.read();
            assertEquals(-1, mr.read());
        }
    }

    @Test
    void testSkipUsesActualSkippedCount() throws IOException {
        try (BoundedReader mr = new BoundedReader(new FilterReader(new StringReader("01234567890")) {
            @Override
            public long skip(final long n) throws IOException {
                return super.skip(Math.min(n, 2));
            }
        }, 5)) {
            assertEquals(2, mr.skip(5));
            assertEquals('2', mr.read());
        }
    }

    @Test
    void testSkipWithMarkUsesActualSkippedCount() throws IOException {
        try (BoundedReader mr = new BoundedReader(new FilterReader(new StringReader("01234567890")) {
            @Override
            public long skip(final long n) throws IOException {
                return super.skip(Math.min(n, 2));
            }
        }, 5)) {
            mr.mark(1);
            assertEquals(2, mr.skip(4));
            assertEquals(2, mr.skip(4));
            assertEquals(1, mr.skip(4));
            assertEquals(0, mr.skip(1));
            mr.reset();
            assertEquals('0', mr.read());
        }
    }

    @Test
    void testSkipWithZeroReadAheadLimit() throws IOException {
        try (BoundedReader mr = new BoundedReader(new StringReader("01234567890"), 3)) {
            mr.mark(0);
            assertEquals(2, mr.skip(2));
            assertEquals(1, mr.skip(2));
            assertEquals(0, mr.skip(1));
        }
    }
}
