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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;

/**
 * Tests {@link NullInputStream}.
 */
public class NullInputStreamTest {

    private static final class TestNullInputStream extends NullInputStream {

        TestNullInputStream(final int size) {
            super(size);
        }

        TestNullInputStream(final int size, final boolean markSupported, final boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }

        @Override
        protected int processByte() {
            return (int) getPosition() - 1;
        }

        @Override
        protected void processBytes(final byte[] bytes, final int offset, final int length) {
            final int startPos = (int) getPosition() - length;
            for (int i = offset; i < length; i++) {
                bytes[i] = (byte) (startPos + i);
            }
        }

    }

    /** Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1. */
    private static final String MARK_RESET_NOT_SUPPORTED = "mark/reset not supported";

    @SuppressWarnings("resource")
    @ParameterizedTest
    @MethodSource(AbstractInputStreamTest.ARRAY_LENGTHS_NAME)
    public void testAvailableAfterClose(final int len) throws Exception {
        final InputStream shadow;
        try (InputStream in = new TestNullInputStream(len, false, false)) {
            assertEquals(len, in.available());
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    @ParameterizedTest
    @MethodSource(AbstractInputStreamTest.ARRAY_LENGTHS_NAME)
    public void testAvailableAfterOpen(final int len) throws Exception {
        try (InputStream in = new TestNullInputStream(len, false, false)) {
            assertEquals(len, in.available());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedSingleton() throws Exception {
        assertNotNull(NullInputStream.INSTANCE);
    }

    @Test
    public void testEOFException() throws Exception {
        try (InputStream input = new TestNullInputStream(2, false, true)) {
            assertEquals(0, input.read(), "Read 1");
            assertEquals(1, input.read(), "Read 2");
            assertThrows(EOFException.class, () -> input.read());
        }
    }

    @Test
    public void testMarkAndReset() throws Exception {
        int position = 0;
        final int readLimit = 10;
        try (InputStream input = new TestNullInputStream(100, true, false)) {

            assertTrue(input.markSupported(), "Mark Should be Supported");

            // No Mark
            final IOException noMarkException = assertThrows(IOException.class, input::reset);
            assertEquals("No position has been marked", noMarkException.getMessage(), "No Mark IOException message");

            for (; position < 3; position++) {
                assertEquals(position, input.read(), "Read Before Mark [" + position + "]");
            }

            // Mark
            input.mark(readLimit);

            // Read further
            for (int i = 0; i < 3; i++) {
                assertEquals(position + i, input.read(), "Read After Mark [" + i + "]");
            }

            // Reset
            input.reset();

            // Read From marked position
            for (int i = 0; i < readLimit + 1; i++) {
                assertEquals(position + i, input.read(), "Read After Reset [" + i + "]");
            }

            // Reset after read limit passed
            final IOException resetException = assertThrows(IOException.class, input::reset, "Read limit exceeded, expected IOException");
            assertEquals("Marked position [" + position + "] is no longer valid - passed the read limit [" + readLimit + "]", resetException.getMessage(),
                    "Read limit IOException message");
        }
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        try (InputStream input = new TestNullInputStream(100, false, true)) {
            assertFalse(input.markSupported(), "Mark Should NOT be Supported");

            final UnsupportedOperationException markException = assertThrows(UnsupportedOperationException.class, () -> input.mark(5));
            assertEquals(MARK_RESET_NOT_SUPPORTED, markException.getMessage(), "mark() error message");

            final UnsupportedOperationException resetException = assertThrows(UnsupportedOperationException.class, input::reset);
            assertEquals(MARK_RESET_NOT_SUPPORTED, resetException.getMessage(), "reset() error message");
        }
    }

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        try (InputStream input = new TestNullInputStream(size)) {
            for (int i = 0; i < size; i++) {
                assertEquals(size - i, input.available(), "Check Size [" + i + "]");
                assertEquals(i, input.read(), "Check Value [" + i + "]");
            }
            assertEquals(0, input.available(), "Available after contents all read");

            // Check available is zero after End of file
            assertEquals(-1, input.read(), "End of File");
            assertEquals(0, input.available(), "Available after End of File");

            // Test reading after the end of file
            assertEquals(-1, input.read(), "End of File");

            // Close
            input.close();
            assertEquals(0, input.available(), "Available after close");
        }
    }

    @Test
    public void testReadAfterClose() throws Exception {
        try (InputStream in = new NullInputStream()) {
            assertEquals(0, in.available());
            in.close();
            assertThrows(IOException.class, in::read);
        }
    }

    @ParameterizedTest
    @MethodSource(AbstractInputStreamTest.ARRAY_LENGTHS_NAME)
    public void testReadAfterClose(final int len) throws Exception {
        try (InputStream in = new TestNullInputStream(len, false, false)) {
            assertEquals(len, in.available());
            in.close();
            assertThrows(IOException.class, in::read);
        }
    }

    @Test
    public void testReadByteArray() throws Exception {
        final byte[] bytes = new byte[10];
        try (NullInputStream input = new TestNullInputStream(15)) {

            // Read into array
            final int count1 = input.read(bytes);
            assertEquals(bytes.length, count1, "Read 1");
            for (int i = 0; i < count1; i++) {
                assertEquals(i, bytes[i], "Check Bytes 1");
            }

            // Read into array
            final int count2 = input.read(bytes);
            assertEquals(5, count2, "Read 2");
            for (int i = 0; i < count2; i++) {
                assertEquals(count1 + i, bytes[i], "Check Bytes 2");
            }

            // End of File
            final int count3 = input.read(bytes);
            assertEquals(-1, count3, "Read 3 (EOF)");

            // Test reading after the end of file
            final int count4 = input.read(bytes);
            assertEquals(-1, count4, "Read 4 (EOF)");

            // re-init
            input.init();

            // Read into array using offset & length
            final int offset = 2;
            final int len = 4;
            final int count5 = input.read(bytes, offset, len);
            assertEquals(len, count5, "Read 5");
            for (int i = offset; i < len; i++) {
                assertEquals(i, bytes[i], "Check Bytes 2");
            }
        }
    }

    @Test
    public void testReadByteArrayAfterClose() throws Exception {
        try (InputStream in = new NullInputStream()) {
            assertEquals(0, in.available());
            in.close();
            assertEquals(0, in.read(new byte[0]));
            assertThrows(IOException.class, () -> in.read(new byte[2]));
        }
    }

    @Test
    public void testReadByteArrayIntIntAfterClose() throws Exception {
        try (InputStream in = new NullInputStream()) {
            assertEquals(0, in.available());
            in.close();
            assertEquals(0, in.read(new byte[0], 0, 1));
            assertEquals(0, in.read(new byte[1], 0, 0));
            assertThrows(IOException.class, () -> in.read(new byte[2], 0, 1));
        }
    }

    @Test
    public void testReadByteArrayThrowAtEof() throws Exception {
        final byte[] bytes = new byte[10];
        try (NullInputStream input = new TestNullInputStream(15, true, true)) {

            // Read into array
            final int count1 = input.read(bytes);
            assertEquals(bytes.length, count1, "Read 1");
            for (int i = 0; i < count1; i++) {
                assertEquals(i, bytes[i], "Check Bytes 1");
            }

            // Read into array
            final int count2 = input.read(bytes);
            assertEquals(5, count2, "Read 2");
            for (int i = 0; i < count2; i++) {
                assertEquals(count1 + i, bytes[i], "Check Bytes 2");
            }

            // End of File
            final IOException e1 = assertThrows(EOFException.class, () -> input.read(bytes));
            assertTrue(StringUtils.isNotBlank(e1.getMessage()));

            // Test reading after the end of file
            final IOException e2 = assertThrows(EOFException.class, () -> input.read(bytes));
            assertTrue(StringUtils.isNotBlank(e2.getMessage()));

            // reset by closing
            input.init();

            // Read into array using offset & length
            final int offset = 2;
            final int len = 4;
            final int count5 = input.read(bytes, offset, len);
            assertEquals(len, count5, "Read 5");
            for (int i = offset; i < len; i++) {
                assertEquals(i, bytes[i], "Check Bytes 2");
            }
        }
    }

    @Test
    public void testReadThrowAtEof() throws Exception {
        final int size = 5;
        try (InputStream input = new TestNullInputStream(size, true, true)) {
            for (int i = 0; i < size; i++) {
                assertEquals(size - i, input.available(), "Check Size [" + i + "]");
                assertEquals(i, input.read(), "Check Value [" + i + "]");
            }
            assertEquals(0, input.available(), "Available after contents all read");

            // Check available is zero after End of file
            final IOException e1 = assertThrows(EOFException.class, input::read);
            assertTrue(StringUtils.isNotBlank(e1.getMessage()));

            // Test reading after the end of file
            final IOException e2 = assertThrows(EOFException.class, input::read);
            assertTrue(StringUtils.isNotBlank(e2.getMessage()));

            // Close
            input.close();
            assertEquals(0, input.available(), "Available after close");
        }
    }

    @Test
    public void testSkip() throws Exception {
        try (InputStream input = new TestNullInputStream(10, true, false)) {
            assertEquals(0, input.read(), "Read 1");
            assertEquals(1, input.read(), "Read 2");
            assertEquals(5, input.skip(5), "Skip 1");
            assertEquals(7, input.read(), "Read 3");
            assertEquals(2, input.skip(5), "Skip 2"); // only 2 left to skip
            assertEquals(-1, input.skip(5), "Skip 3 (EOF)"); // End of file
            assertEquals(-1, input.skip(5), "Skip 3 (EOF)"); // End of file
        }
    }

    @Test
    public void testSkipThrowAtEof() throws Exception {
        try (InputStream input = new TestNullInputStream(10, true, true)) {
            assertEquals(0, input.read(), "Read 1");
            assertEquals(1, input.read(), "Read 2");
            assertEquals(5, input.skip(5), "Skip 1");
            assertEquals(7, input.read(), "Read 3");
            assertEquals(2, input.skip(5), "Skip 2"); // only 2 left to skip
            // End of File
            final IOException e1 = assertThrows(EOFException.class, () -> input.skip(5), "Skip 3 (EOF)");
            assertTrue(StringUtils.isNotBlank(e1.getMessage()));

            final IOException e2 = assertThrows(IOException.class, () -> input.skip(5), "Expected IOException for skipping after end of file");
            assertTrue(StringUtils.isNotBlank(e2.getMessage()));
        }
    }
}
