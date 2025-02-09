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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link NullReader}.
 */
public class NullReaderTest {

    private static final class TestNullReader extends NullReader {
        TestNullReader(final int size) {
            super(size);
        }

        TestNullReader(final int size, final boolean markSupported, final boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }

        @Override
        protected int processChar() {
            return (int) getPosition() - 1;
        }

        @Override
        protected void processChars(final char[] chars, final int offset, final int length) {
            final int startPos = (int) getPosition() - length;
            for (int i = offset; i < length; i++) {
                chars[i] = (char) (startPos + i);
            }
        }

    }

    // Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1.
    private static final String MARK_RESET_NOT_SUPPORTED = "mark/reset not supported";

    @Test
    public void testEOFException() throws Exception {
        try (Reader reader = new TestNullReader(2, false, true)) {
            assertEquals(0, reader.read(), "Read 1");
            assertEquals(1, reader.read(), "Read 2");
            assertThrows(EOFException.class, () -> reader.read());
        }
    }

    @Test
    public void testMarkAndReset() throws Exception {
        int position = 0;
        final int readLimit = 10;
        try (Reader reader = new TestNullReader(100, true, false)) {

            assertTrue(reader.markSupported(), "Mark Should be Supported");

            // No Mark
            final IOException resetException = assertThrows(IOException.class, reader::reset);
            assertEquals("No position has been marked", resetException.getMessage(), "No Mark IOException message");

            for (; position < 3; position++) {
                assertEquals(position, reader.read(), "Read Before Mark [" + position + "]");
            }

            // Mark
            reader.mark(readLimit);

            // Read further
            for (int i = 0; i < 3; i++) {
                assertEquals(position + i, reader.read(), "Read After Mark [" + i + "]");
            }

            // Reset
            reader.reset();

            // Read From marked position
            for (int i = 0; i < readLimit + 1; i++) {
                assertEquals(position + i, reader.read(), "Read After Reset [" + i + "]");
            }

            // Reset after read limit passed
            final IOException e = assertThrows(IOException.class, reader::reset);
            assertEquals("Marked position [" + position + "] is no longer valid - passed the read limit [" + readLimit + "]", e.getMessage(),
                    "Read limit IOException message");
        }
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        final Reader reader = new TestNullReader(100, false, true);
        assertFalse(reader.markSupported(), "Mark Should NOT be Supported");

        try {
            reader.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals(MARK_RESET_NOT_SUPPORTED, e.getMessage(), "mark() error message");
        }

        try {
            reader.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals(MARK_RESET_NOT_SUPPORTED, e.getMessage(), "reset() error message");
        }
        reader.close();
    }

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        final TestNullReader reader = new TestNullReader(size);
        for (int i = 0; i < size; i++) {
            assertEquals(i, reader.read(), "Check Value [" + i + "]");
        }

        // Check End of File
        assertEquals(-1, reader.read(), "End of File");

        // Test reading after the end of file
        try {
            final int result = reader.read();
            fail("Should have thrown an IOException, value=[" + result + "]");
        } catch (final IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // Close - should reset
        reader.close();
        assertEquals(0, reader.getPosition(), "Available after close");
    }

    @Test
    public void testReadCharArray() throws Exception {
        final char[] chars = new char[10];
        final Reader reader = new TestNullReader(15);

        // Read into array
        final int count1 = reader.read(chars);
        assertEquals(chars.length, count1, "Read 1");
        for (int i = 0; i < count1; i++) {
            assertEquals(i, chars[i], "Check Chars 1");
        }

        // Read into array
        final int count2 = reader.read(chars);
        assertEquals(5, count2, "Read 2");
        for (int i = 0; i < count2; i++) {
            assertEquals(count1 + i, chars[i], "Check Chars 2");
        }

        // End of File
        final int count3 = reader.read(chars);
        assertEquals(-1, count3, "Read 3 (EOF)");

        // Test reading after the end of file
        try {
            final int count4 = reader.read(chars);
            fail("Should have thrown an IOException, value=[" + count4 + "]");
        } catch (final IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // reset by closing
        reader.close();

        // Read into array using offset & length
        final int offset = 2;
        final int lth    = 4;
        final int count5 = reader.read(chars, offset, lth);
        assertEquals(lth, count5, "Read 5");
        for (int i = offset; i < lth; i++) {
            assertEquals(i, chars[i], "Check Chars 3");
        }
    }

    @Test
    public void testSkip() throws Exception {
        try (Reader reader = new TestNullReader(10, true, false)) {
            assertEquals(0, reader.read(), "Read 1");
            assertEquals(1, reader.read(), "Read 2");
            assertEquals(5, reader.skip(5), "Skip 1");
            assertEquals(7, reader.read(), "Read 3");
            assertEquals(2, reader.skip(5), "Skip 2"); // only 2 left to skip
            assertEquals(-1, reader.skip(5), "Skip 3 (EOF)"); // End of file

            final IOException e = assertThrows(IOException.class, () -> reader.skip(5));
            assertEquals("Skip after end of file", e.getMessage(), "Skip after EOF IOException message");
        }
    }
}
