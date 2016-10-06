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

import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit Test Case for {@link NullInputStream}.
 *
 * @version $Id$
 */
public class NullInputStreamTest {

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        final InputStream input = new TestNullInputStream(size);
        for (int i = 0; i < size; i++) {
            assertEquals("Check Size [" + i + "]", size - i, input.available());
            assertEquals("Check Value [" + i + "]", i, input.read());
        }
        assertEquals("Available after contents all read", 0, input.available());

        // Check availbale is zero after End of file
        assertEquals("End of File", -1, input.read());
        assertEquals("Available after End of File", 0, input.available());

        // Test reading after the end of file
        try {
            final int result = input.read();
            fail("Should have thrown an IOException, byte=[" + result + "]");
        } catch (final IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // Close - should reset
        input.close();
        assertEquals("Available after close", size, input.available());
    }

    @Test
    public void testReadByteArray() throws Exception {
        final byte[] bytes = new byte[10];
        final InputStream input = new TestNullInputStream(15);

        // Read into array
        final int count1 = input.read(bytes);
        assertEquals("Read 1", bytes.length, count1);
        for (int i = 0; i < count1; i++) {
            assertEquals("Check Bytes 1", i, bytes[i]);
        }

        // Read into array
        final int count2 = input.read(bytes);
        assertEquals("Read 2", 5, count2);
        for (int i = 0; i < count2; i++) {
            assertEquals("Check Bytes 2", count1 + i, bytes[i]);
        }

        // End of File
        final int count3 = input.read(bytes);
        assertEquals("Read 3 (EOF)", -1, count3);

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
        assertEquals("Read 5", lth, count5);
        for (int i = offset; i < lth; i++) {
            assertEquals("Check Bytes 2", i, bytes[i]);
        }
    }

    @Test
    public void testEOFException() throws Exception {
        final InputStream input = new TestNullInputStream(2, false, true);
        assertEquals("Read 1",  0, input.read());
        assertEquals("Read 2",  1, input.read());
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

        assertTrue("Mark Should be Supported", input.markSupported());

        // No Mark
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertEquals("No Mark IOException message",
                         "No position has been marked",
                         e.getMessage());
        }

        for (; position < 3; position++) {
            assertEquals("Read Before Mark [" + position +"]",  position, input.read());
        }

        // Mark
        input.mark(readlimit);

        // Read further
        for (int i = 0; i < 3; i++) {
            assertEquals("Read After Mark [" + i +"]",  position + i, input.read());
        }

        // Reset
        input.reset();

        // Read From marked position
        for (int i = 0; i < readlimit + 1; i++) {
            assertEquals("Read After Reset [" + i +"]",  position + i, input.read());
        }

        // Reset after read limit passed
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertEquals("Read limit IOException message",
                         "Marked position [" + position
                         + "] is no longer valid - passed the read limit ["
                         + readlimit + "]",
                         e.getMessage());
        }
        input.close();
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        final InputStream input = new TestNullInputStream(100, false, true);
        assertFalse("Mark Should NOT be Supported", input.markSupported());

        try {
            input.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals("mark() error message",  "Mark not supported", e.getMessage());
        }

        try {
            input.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertEquals("reset() error message",  "Mark not supported", e.getMessage());
        }
        input.close();
    }

    @Test
    public void testSkip() throws Exception {
        final InputStream input = new TestNullInputStream(10, true, false);
        assertEquals("Read 1", 0, input.read());
        assertEquals("Read 2", 1, input.read());
        assertEquals("Skip 1", 5, input.skip(5));
        assertEquals("Read 3", 7, input.read());
        assertEquals("Skip 2", 2, input.skip(5)); // only 2 left to skip
        assertEquals("Skip 3 (EOF)", -1, input.skip(5)); // End of file
        try {
            input.skip(5); //
            fail("Expected IOException for skipping after end of file");
        } catch (final IOException e) {
            assertEquals("Skip after EOF IOException message",
                    "Skip after end of file",
                    e.getMessage());
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
