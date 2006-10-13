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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import junit.framework.TestCase;

/**
 * JUnit Test Case for {@link NullReader}.
 *
 * @version $Id$
 */
public class NullReaderTest extends TestCase {

    /** Constructor */
    public NullReaderTest(String name) {
        super(name);
    }

    /** Set up */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** Tear Down */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test <code>available()</code> method.
     */
    public void testRead() throws Exception {
        int size = 5;
        TestNullReader reader = new TestNullReader(size);
        for (int i = 0; i < size; i++) {
            assertEquals("Check Value [" + i + "]", i, reader.read());
        }

        // Check End of File
        assertEquals("End of File", -1, reader.read());

        // Test reading after the end of file
        try {
            int result = reader.read();
            fail("Should have thrown an IOException, value=[" + result + "]");
        } catch (IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // Close - should reset
        reader.close();
        assertEquals("Available after close", 0, reader.getPosition());
    }

    /**
     * Test <code>read(char[])</code> method.
     */
    public void testReadCharArray() throws Exception {
        char[] chars = new char[10];
        Reader reader = new TestNullReader(15);

        // Read into array
        int count1 = reader.read(chars);
        assertEquals("Read 1", chars.length, count1);
        for (int i = 0; i < count1; i++) {
            assertEquals("Check Chars 1", i, chars[i]);
        }

        // Read into array
        int count2 = reader.read(chars);
        assertEquals("Read 2", 5, count2);
        for (int i = 0; i < count2; i++) {
            assertEquals("Check Chars 2", count1 + i, chars[i]);
        }

        // End of File
        int count3 = reader.read(chars);
        assertEquals("Read 3 (EOF)", -1, count3);

        // Test reading after the end of file
        try {
            int count4 = reader.read(chars);
            fail("Should have thrown an IOException, value=[" + count4 + "]");
        } catch (IOException e) {
            assertEquals("Read after end of file", e.getMessage());
        }

        // reset by closing
        reader.close();
    
        // Read into array using offset & length
        int offset = 2;
        int lth    = 4;
        int count5 = reader.read(chars, offset, lth);
        assertEquals("Read 5", lth, count5);
        for (int i = offset; i < lth; i++) {
            assertEquals("Check Chars 3", i, chars[i]);
        }
    }

    /**
     * Test when configured to throw an EOFException at the end of file
     * (rather than return -1).
     */
    public void testEOFException() throws Exception {
        Reader reader = new TestNullReader(2, false, true);
        assertEquals("Read 1",  0, reader.read());
        assertEquals("Read 2",  1, reader.read());
        try {
            int result = reader.read();
            fail("Should have thrown an EOFException, value=[" + result + "]");
        } catch (EOFException e) {
            // expected
        }
    }

    /**
     * Test <code>mark()</code> and <code>reset()</code> methods.
     */
    public void testMarkAndReset() throws Exception {
        int position = 0;
        int readlimit = 10;
        Reader reader = new TestNullReader(100, true, false);
        
        assertTrue("Mark Should be Supported", reader.markSupported());

        // No Mark
        try {
            reader.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (Exception e) {
            assertEquals("No Mark IOException message",
                         "No position has been marked",
                         e.getMessage());
        }

        for (; position < 3; position++) {
            assertEquals("Read Before Mark [" + position +"]",  position, reader.read());
        }

        // Mark
        reader.mark(readlimit);

        // Read further
        for (int i = 0; i < 3; i++) {
            assertEquals("Read After Mark [" + i +"]",  (position + i), reader.read());
        }

        // Reset
        reader.reset();

        // Read From marked position
        for (int i = 0; i < readlimit + 1; i++) {
            assertEquals("Read After Reset [" + i +"]",  (position + i), reader.read());
        }

        // Reset after read limit passed
        try {
            reader.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (Exception e) {
            assertEquals("Read limit IOException message",
                         "Marked position [" + position
                         + "] is no longer valid - passed the read limit ["
                         + readlimit + "]",
                         e.getMessage());
        }
    }

    /**
     * Test <code>mark()</code> not supported.
     */
    public void testMarkNotSupported() throws Exception {
        Reader reader = new TestNullReader(100, false, true);
        assertFalse("Mark Should NOT be Supported", reader.markSupported());

        try {
            reader.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("mark() error message",  "Mark not supported", e.getMessage());
        }

        try {
            reader.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("reset() error message",  "Mark not supported", e.getMessage());
        }
    }

    /**
     * Test <code>skip()</code> method.
     */
   public void testSkip() throws Exception {
        Reader reader = new TestNullReader(10, true, false);
        assertEquals("Read 1", 0, reader.read());
        assertEquals("Read 2", 1, reader.read());
        assertEquals("Skip 1", 5, reader.skip(5));
        assertEquals("Read 3", 7, reader.read());
        assertEquals("Skip 2", 2, reader.skip(5)); // only 2 left to skip
        assertEquals("Skip 3 (EOF)", -1, reader.skip(5)); // End of file
        try {
            reader.skip(5); //
            fail("Expected IOException for skipping after end of file");
        } catch (Exception e) {
            assertEquals("Skip after EOF IOException message",
                    "Skip after end of file",
                    e.getMessage());
        }
    }


    // ------------- Test NullReader implementation -------------

    private static final class TestNullReader extends NullReader {
        public TestNullReader(int size) {
            super(size);
        }
        public TestNullReader(int size, boolean markSupported, boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }
        protected int processChar() {
            return ((int)getPosition() - 1);
        }
        protected void processChars(char[] chars, int offset, int length) {
            int startPos = (int)getPosition() - length;
            for (int i = offset; i < length; i++) {
                chars[i] = (char)(startPos + i);
            }
        }
        
    }
}
