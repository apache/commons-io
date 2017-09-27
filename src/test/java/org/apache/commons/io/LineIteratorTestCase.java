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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * This is used to test LineIterator for correctness.
 *
 */
public class LineIteratorTestCase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File getTestDirectory() {
        return temporaryFolder.getRoot();
    }

    private void assertLines(final List<String> lines, final LineIterator iterator) {
        try {
            for (int i = 0; i < lines.size(); i++) {
                final String line = iterator.nextLine();
                assertEquals("nextLine() line " + i, lines.get(i), line);
            }
            assertFalse("No more expected", iterator.hasNext());
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Creates a test file with a specified number of lines.
     *
     * @param file target file
     * @param lineCount number of lines to create
     *
     * @throws IOException If an I/O error occurs
     */
    private List<String> createLinesFile(final File file, final int lineCount) throws IOException {
        final List<String> lines = createStringLines(lineCount);
        FileUtils.writeLines(file, lines);
        return lines;
    }

    /**
     * Creates a test file with a specified number of lines.
     *
     * @param file target file
     * @param encoding the encoding to use while writing the lines
     * @param lineCount number of lines to create
     *
     * @throws IOException If an I/O error occurs
     */
    private List<String> createLinesFile(final File file, final String encoding, final int lineCount) throws IOException {
        final List<String> lines = createStringLines(lineCount);
        FileUtils.writeLines(file, encoding, lines);
        return lines;
    }

    /**
     * Creates String data lines.
     *
     * @param lineCount number of lines to create
     * @return a new lines list.
     */
    private List<String> createStringLines(final int lineCount) {
        final List<String> lines = new ArrayList<>();
        for (int i = 0; i < lineCount; i++) {
            lines.add("LINE " + i);
        }
        return lines;
    }

    @Before
    public void setUp() throws Exception {
        final File dir = getTestDirectory();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();

    }

    // -----------------------------------------------------------------------

    @Test
    public void testConstructor() throws Exception {
        try {
            new LineIterator(null);
            fail();
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testZeroLines() throws Exception {
        doTestFileWithSpecifiedLines(0);
    }

    @Test
    public void testOneLines() throws Exception {
        doTestFileWithSpecifiedLines(1);
    }

    @Test
    public void testTwoLines() throws Exception {
        doTestFileWithSpecifiedLines(2);
    }

    @Test
    public void testThreeLines() throws Exception {
        doTestFileWithSpecifiedLines(3);
    }

    @Test
    public void testMissingFile() throws Exception {
        final File testFile = new File(getTestDirectory(), "dummy-missing-file.txt");

        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(testFile, "UTF-8");
            fail("Expected FileNotFoundException");
        } catch (final FileNotFoundException expected) {
            // ignore, expected result
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    @Test
    public void testValidEncoding() throws Exception {
        final String encoding = "UTF-8";

        final File testFile = new File(getTestDirectory(), "LineIterator-validEncoding.txt");
        createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            int count = 0;
            while (iterator.hasNext()) {
                assertNotNull(iterator.next());
                count++;
            }
            assertEquals(3, count);
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    @Test
    public void testInvalidEncoding() throws Exception {
        final String encoding = "XXXXXXXX";

        final File testFile = new File(getTestDirectory(), "LineIterator-invalidEncoding.txt");
        createLinesFile(testFile, "UTF-8", 3);

        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(testFile, encoding);
            fail("Expected UnsupportedCharsetException");
        } catch (final UnsupportedCharsetException expected) {
            // ignore, expected result
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    @Test
    public void testNextLineOnlyDefaultEncoding() throws Exception {
        final File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile);
        assertLines(lines, iterator);
    }

    @Test
    public void testNextLineOnlyNullEncoding() throws Exception {
        final String encoding = null;

        final File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        assertLines(lines, iterator);
    }

    @Test
    public void testNextLineOnlyUtf8Encoding() throws Exception {
        final String encoding = "UTF-8";

        final File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        assertLines(lines, iterator);
    }

    @Test
    public void testNextOnly() throws Exception {
        final String encoding = null;

        final File testFile = new File(getTestDirectory(), "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            for (int i = 0; i < lines.size(); i++) {
                final String line = iterator.next();
                assertEquals("next() line " + i, lines.get(i), line);
            }
            assertEquals("No more expected", false, iterator.hasNext());
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    @Test
    public void testNextWithException() throws Exception {
        final Reader reader = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() throws IOException {
                throw new IOException("hasNext");
            }
        };
        try {
            new LineIterator(reader).hasNext();
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testCloseEarly() throws Exception {
        final String encoding = "UTF-8";

        final File testFile = new File(getTestDirectory(), "LineIterator-closeEarly.txt");
        createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            // get
            assertNotNull("Line expected", iterator.next());
            assertTrue("More expected", iterator.hasNext());

            // close
            iterator.close();
            assertFalse("No more expected", iterator.hasNext());
            try {
                iterator.next();
                fail();
            } catch (final NoSuchElementException ex) {
                // expected
            }
            try {
                iterator.nextLine();
                fail();
            } catch (final NoSuchElementException ex) {
                // expected
            }

            // try closing again
            iterator.close();
            try {
                iterator.next();
                fail();
            } catch (final NoSuchElementException ex) {
                // expected
            }
            try {
                iterator.nextLine();
                fail();
            } catch (final NoSuchElementException ex) {
                // expected
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    /**
     * Utility method to create and test a file with a specified number of lines.
     *
     * @param lineCount the lines to create in the test file
     *
     * @throws IOException If an I/O error occurs while creating the file
     */
    private void doTestFileWithSpecifiedLines(final int lineCount) throws IOException {
        final String encoding = "UTF-8";

        final String fileName = "LineIterator-" + lineCount + "-test.txt";
        final File testFile = new File(getTestDirectory(), fileName);
        final List<String> lines = createLinesFile(testFile, encoding, lineCount);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        try {
            try {
                iterator.remove();
                fail("Remove is unsupported");
            } catch (final UnsupportedOperationException ex) {
                // expected
            }

            int idx = 0;
            while (iterator.hasNext()) {
                final String line = iterator.next();
                assertEquals("Comparing line " + idx, lines.get(idx), line);
                assertTrue("Exceeded expected idx=" + idx + " size=" + lines.size(), idx < lines.size());
                idx++;
            }
            assertEquals("Line Count doesn't match", idx, lines.size());

            // try calling next() after file processed
            try {
                iterator.next();
                fail("Expected NoSuchElementException");
            } catch (final NoSuchElementException expected) {
                // ignore, expected result
            }
            try {
                iterator.nextLine();
                fail("Expected NoSuchElementException");
            } catch (final NoSuchElementException expected) {
                // ignore, expected result
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

    // -----------------------------------------------------------------------
    @Test
    public void testFilteringFileReader() throws Exception {
        final String encoding = "UTF-8";

        final String fileName = "LineIterator-Filter-test.txt";
        final File testFile = new File(getTestDirectory(), fileName);
        final List<String> lines = createLinesFile(testFile, encoding, 9);

        final Reader reader = new FileReader(testFile);
        this.testFiltering(lines, reader);
    }

    @Test
    public void testFilteringBufferedReader() throws Exception {
        final String encoding = "UTF-8";

        final String fileName = "LineIterator-Filter-test.txt";
        final File testFile = new File(getTestDirectory(), fileName);
        final List<String> lines = createLinesFile(testFile, encoding, 9);

        final Reader reader = new BufferedReader(new FileReader(testFile));
        this.testFiltering(lines, reader);
    }

    private void testFiltering(final List<String> lines, final Reader reader) {
        final LineIterator iterator = new LineIterator(reader) {
            @Override
            protected boolean isValidLine(final String line) {
                final char c = line.charAt(line.length() - 1);
                return (c - 48) % 3 != 1;
            }
        };
        try {
            try {
                iterator.remove();
                fail("Remove is unsupported");
            } catch (final UnsupportedOperationException ex) {
                // expected
            }

            int idx = 0;
            int actualLines = 0;
            while (iterator.hasNext()) {
                final String line = iterator.next();
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
            } catch (final NoSuchElementException expected) {
                // ignore, expected result
            }
            try {
                iterator.nextLine();
                fail("Expected NoSuchElementException");
            } catch (final NoSuchElementException expected) {
                // ignore, expected result
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
    }

}
