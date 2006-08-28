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
package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test LineIterator for correctness.
 *
 * @author Niall Pemberton
 * @author Stephen Colebourne
 * @version $Id$
 */
public class LineIteratorTestCase extends FileBasedTestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(LineIteratorTestCase.class);
    }

    public LineIteratorTestCase(String name) throws IOException {
        super(name);
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        File dir = getTestDirectory();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();

    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    //-----------------------------------------------------------------------
    /**
     * Test constructor.
     */
    public void testConstructor() throws Exception {
        try {
            new LineIterator((Reader) null);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    /**
     * Test a file with no lines.
     */
    public void testZeroLines() throws Exception {
        doTestFileWithSpecifiedLines(0);
    }

    /**
     * Test a file with 1 line.
     */
    public void testOneLines() throws Exception {
        doTestFileWithSpecifiedLines(1);
    }

    /**
     * Test a file with 2 lines.
     */
    public void testTwoLines() throws Exception {
        doTestFileWithSpecifiedLines(2);
    }

    /**
     * Test a file with 3 lines.
     */
    public void testThreeLines() throws Exception {
        doTestFileWithSpecifiedLines(3);
    }

    /**
     * Test a missing File.
     */
    public void testMissingFile() throws Exception {
        File testFile = new File(getTestDirectory(), "dummy-missing-file.txt");
        
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(testFile, "UTF-8");
            fail("Expected FileNotFoundException");
        } catch(FileNotFoundException expected) {
            // ignore, expected result
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Test a file with a Valid encoding.
     */
    public void testValidEncoding() throws Exception {
        String encoding = "UTF-8";
        
        File testFile = new File(getTestDirectory(), "LineIterator-validEncoding.txt");
        createFile(testFile, encoding, 3);
        
        LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            int count = 0;
            while (iterator.hasNext()) {
                assertTrue(iterator.next() instanceof String);
                count++;
            }
            assertEquals(3, count);
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Test a file with an Invalid encoding.
     */
    public void testInvalidEncoding() throws Exception {
        String encoding = "XXXXXXXX";
        
        File testFile = new File(getTestDirectory(), "LineIterator-invalidEncoding.txt");
        createFile(testFile, "UTF-8", 3);
        
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(testFile, encoding);
            fail("Expected UnsupportedEncodingException");
        } catch(UnsupportedEncodingException expected) {
            // ignore, expected result
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Test the iterator using only the next() method.
     */
    public void testNextOnly() throws Exception {
        String encoding = null;
        
        File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        List lines = createFile(testFile, encoding, 3);
        
        LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            for (int i = 0; i < lines.size(); i++) {
                String line = (String)iterator.next();
                assertEquals("next() line " + i, lines.get(i), line);
            }
            assertEquals("No more expected", false, iterator.hasNext());
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Test the iterator using only the nextLine() method.
     */
    public void testNextLineOnly() throws Exception {
        String encoding = null;
        
        File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        List lines = createFile(testFile, encoding, 3);
        
        LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            for (int i = 0; i < lines.size(); i++) {
                String line = iterator.nextLine();
                assertEquals("nextLine() line " + i, lines.get(i), line);
            }
            assertFalse("No more expected", iterator.hasNext());
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Test closing the iterator before all the file has been
     * processed.
     */
    public void testCloseEarly() throws Exception {
        String encoding = "UTF-8";
        
        File testFile = new File(getTestDirectory(), "LineIterator-closeEarly.txt");
        createFile(testFile, encoding, 3);
        
        LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            // get
            assertTrue("Line expected", iterator.next() instanceof String);
            assertTrue("More expected", iterator.hasNext());
    
            // close
            iterator.close();
            assertFalse("No more expected", iterator.hasNext());
            try {
                iterator.next();
                fail();
            } catch (NoSuchElementException ex) {
                // expected
            }
            try {
                iterator.nextLine();
                fail();
            } catch (NoSuchElementException ex) {
                // expected
            }
    
            // try closing again
            iterator.close();
            try {
                iterator.next();
                fail();
            } catch (NoSuchElementException ex) {
                // expected
            }
            try {
                iterator.nextLine();
                fail();
            } catch (NoSuchElementException ex) {
                // expected
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Utility method to create and test a file with a specified
     * number of lines.
     */
    private void doTestFileWithSpecifiedLines(int lineCount) throws Exception {
        String encoding = "UTF-8";
        
        String fileName = "LineIterator-" + lineCount + "-test.txt";
        File testFile = new File(getTestDirectory(), fileName);
        List lines = createFile(testFile, encoding, lineCount);
        
        LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            try {
                iterator.remove();
                fail("Remove is unsupported");
            } catch (UnsupportedOperationException ex) {
                // expected
            }
    
            int idx = 0;
            while (iterator.hasNext()) {
                String line = (String)iterator.next();
                assertEquals("Comparing line " + idx, lines.get(idx), line);
                assertTrue("Exceeded expected idx=" + idx + " size=" + lines.size(), idx < lines.size());
                idx++;
            }
            assertEquals("Line Count doesn't match", idx, lines.size());
    
            // try calling next() after file processed
            try {
                iterator.next();
                fail("Expected NoSuchElementException");
            } catch (NoSuchElementException expected) {
                // ignore, expected result
            }
            try {
                iterator.nextLine();
                fail("Expected NoSuchElementException");
            } catch (NoSuchElementException expected) {
                // ignore, expected result
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Utility method to create a test file with a specified
     * number of lines.
     */
    private List createFile(File file, String encoding, int lineCount) throws Exception {
        List lines = new ArrayList();
        for (int i = 0; i < lineCount; i++) {
            lines.add("LINE " + i);
        }
        FileUtils.writeLines(file, encoding, lines);
        return lines;
    }

    //-----------------------------------------------------------------------
    public void testFiltering() throws Exception {
        String encoding = "UTF-8";
        
        String fileName = "LineIterator-Filter-test.txt";
        File testFile = new File(getTestDirectory(), fileName);
        List lines = createFile(testFile, encoding, 9);
        
        Reader reader = new FileReader(testFile);
        LineIterator iterator = new LineIterator(reader) {
            protected boolean isValidLine(String line) {
                char c = line.charAt(line.length() - 1);
                return ((c - 48) % 3 != 1);
            }
        };
        try {
            try {
                iterator.remove();
                fail("Remove is unsupported");
            } catch (UnsupportedOperationException ex) {
                // expected
            }
            
            int idx = 0;
            int actualLines = 0;
            while (iterator.hasNext()) {
                String line = (String) iterator.next();
                actualLines++;
                assertEquals("Comparing line " + idx, lines.get(idx), line);
                assertTrue("Exceeded expected idx=" + idx + " size=" + lines.size(), idx < lines.size());
                idx++;
                if (idx % 3 == 1) {
                    idx++;
                }
            }
            assertEquals("Line Count doesn't match", 9, lines.size());
            assertEquals("Line Count doesn't match", 9, idx);
            assertEquals("Line Count doesn't match", 6, actualLines);
    
            // try calling next() after file processed
            try {
                iterator.next();
                fail("Expected NoSuchElementException");
            } catch (NoSuchElementException expected) {
                // ignore, expected result
            }
            try {
                iterator.nextLine();
                fail("Expected NoSuchElementException");
            } catch (NoSuchElementException expected) {
                // ignore, expected result
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

}
