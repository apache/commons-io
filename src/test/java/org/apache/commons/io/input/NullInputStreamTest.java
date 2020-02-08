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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link NullInputStream}.
 *
 */
public class NullInputStreamTest {

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        final InputStream input = new TestNullInputStream(size);
        for (int i = 0; i < size; i++) {
            assertEquals(size - i, input.available(), "Check Size [" + i + "]");
            assertEquals(i, input.read(), "Check Value [" + i + "]");
        }
        assertEquals(0, input.available(), "Available after contents all read");

        // Check availbale is zero after End of file
        assertEquals(-1, input.read(), "End of File");
        assertEquals(0, input.available(), "Available after End of File");

        // Test reading after the end of file
        try {
            final int result = input.read();
            fail("Should have thrown an IOException, byte=[" + result + "]");
        } catch (final IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // Close - should reset
        input.close();
        assertEquals(size, input.available(), "Available after close");
    }

    @Test
    public void testReadByteArray() throws Exception {
        final byte[] bytes = new byte[10];
        final InputStream input = new TestNullInputStream(15);

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
        try {
            final int count4 = input.read(bytes);
            fail("Should have thrown an IOException, byte=[" + count4 + "]");
        } catch (final IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // reset by closing
        input.close();

        // Read into array using offset & length
        final int offset = 2;
        final int lth    = 4;
        final int count5 = input.read(bytes, offset, lth);
        assertEquals(lth, count5, "Read 5");
        for (int i = offset; i < lth; i++) {
            assertEquals(i, bytes[i], "Check Bytes 2");
        }
    }

    @Test
    public void testEOFException() throws Exception {
        final InputStream input = new TestNullInputStream(2, false, true);
        assertEquals(0, input.read(), "Read 1");
        assertEquals(1, input.read(), "Read 2");
        try {
            final int result = input.read();
            fail("Should have thrown an EOFException, byte=[" + result + "]");
        } catch (final EOFException e) {
            // expected
        }
        input.close();
    }

    @Test
    public void testMarkAndReset() throws Exception {
        int position = 0;
        final int readlimit = 10;
        final InputStream input = new TestNullInputStream(100, true, false);

        assertTrue(input.markSupported(), "Mark Should be Supported");

        // No Mark
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertEquals("No position has been marked", e.getMessage(), "No Mark IOException message");
        }

        for (; position < 3; position++) {
            assertEquals(position, input.read(), "Read Before Mark [" + position +"]");
        }

        // Mark
        input.mark(readlimit);

        // Read further
        for (int i = 0; i < 3; i++) {
            assertEquals(position + i, input.read(), "Read After Mark [" + i +"]");
        }

        // Reset
        input.reset();

        // Read From marked position
        for (int i = 0; i < readlimit + 1; i++) {
            assertEquals(position + i, input.read(), "Read After Reset [" + i +"]");
        }

        // Reset after read limit passed
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertEquals("Marked position [" + position
                         + "] is no longer valid - passed the read limit ["
                         + readlimit + "]",
                         e.getMessage(),
                         "Read limit IOException message");
        }
        input.close();
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        final InputStream input = new TestNullInputStream(100, false, true);
        assertFalse(input.markSupported(), "Mark Should NOT be Supported");

        try {
            input.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals("Mark not supported", e.getMessage(), "mark() error message");
        }

        try {
            input.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals("Mark not supported", e.getMessage(), "reset() error message");
        }
        input.close();
    }

    @Test
    public void testSkip() throws Exception {
        final InputStream input = new TestNullInputStream(10, true, false);
        assertEquals(0, input.read(), "Read 1");
        assertEquals(1, input.read(), "Read 2");
        assertEquals(5, input.skip(5), "Skip 1");
        assertEquals(7, input.read(), "Read 3");
        assertEquals(2, input.skip(5), "Skip 2"); // only 2 left to skip
        assertEquals(-1, input.skip(5), "Skip 3 (EOF)"); // End of file
        try {
            input.skip(5); //
            fail("Expected IOException for skipping after end of file");
        } catch (final IOException e) {
            assertEquals("Skip after end of file", e.getMessage(), "Skip after EOF IOException message");
        }
        input.close();
    }


    // ------------- Test NullInputStream implementation -------------

    private static final class TestNullInputStream extends NullInputStream {
        public TestNullInputStream(final int size) {
            super(size);
        }
        public TestNullInputStream(final int size, final boolean markSupported, final boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }
        @Override
        protected int processByte() {
            return (int)getPosition() - 1;
        }
        @Override
        protected void processBytes(final byte[] bytes, final int offset, final int length) {
            final int startPos = (int)getPosition() - length;
            for (int i = offset; i < length; i++) {
                bytes[i] = (byte)(startPos + i);
            }
        }

    }
}
