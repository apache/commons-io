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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Tailer}.
 *
 */
public class TailerTest {

    @TempDir
    public static File temporaryFolder;

    private Tailer tailer;

    @AfterEach
    public void tearDown() throws Exception {
        if (tailer != null) {
            tailer.stop();
        }
    }

    @Test
    public void testLongFile() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testLongFile.txt");
        createFile(file, 0);
        try (final Writer writer = new FileWriter(file, true)) {
            for (int i = 0; i < 100000; i++) {
                writer.write("LineLineLineLineLineLineLineLineLineLine\n");
            }
            writer.write("SBTOURIST\n");
        }

        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false);

        final long start = System.currentTimeMillis();

        final Thread thread = new Thread(tailer);
        thread.start();

        List<String> lines = listener.getLines();
        while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
            lines = listener.getLines();
        }
        // System.out.println("Elapsed: " + (System.currentTimeMillis() - start));

        listener.clear();
    }

    @Test
    public void testBufferBreak() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testBufferBreak.txt");
        createFile(file, 0);
        writeString(file, "SBTOURIST\n");

        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false, 1);

        final Thread thread = new Thread(tailer);
        thread.start();

        List<String> lines = listener.getLines();
        while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
            lines = listener.getLines();
        }

        listener.clear();
    }

    @Test
    public void testMultiByteBreak() throws Exception {
        // System.out.println("testMultiByteBreak() Default charset: " + Charset.defaultCharset().displayName());
        final long delay = 50;
        final File origin = new File(this.getClass().getResource("/test-file-utf8.bin").toURI());
        final File file = new File(temporaryFolder, "testMultiByteBreak.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        // Need to use UTF-8 to read & write the file otherwise it can be corrupted (depending on the default charset)
        final Charset charsetUTF8 = StandardCharsets.UTF_8;
        tailer = new Tailer(file, charsetUTF8, listener, delay, false, isWindows, 4096);
        final Thread thread = new Thread(tailer);
        thread.start();

        try (Writer out = new OutputStreamWriter(new FileOutputStream(file), charsetUTF8);
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(origin), charsetUTF8))) {
            final List<String> lines = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null){
                out.write(line);
                out.write("\n");
                lines.add(line);
            }
            out.close(); // ensure data is written

           final long testDelayMillis = delay * 10;
           TestUtils.sleep(testDelayMillis);
           final List<String> tailerlines = listener.getLines();
           assertEquals(lines.size(), tailerlines.size(), "line count");
           for(int i = 0,len = lines.size();i<len;i++){
               final String expected = lines.get(i);
               final String actual = tailerlines.get(i);
               if (!expected.equals(actual)) {
                   fail("Line: " + i
                           + "\nExp: (" + expected.length() + ") " + expected
                           + "\nAct: (" + actual.length() + ") "+ actual);
               }
           }
        }
    }

    @Test
    public void testTailerEof() throws Exception {
        // Create & start the Tailer
        final long delay = 50;
        final File file = new File(temporaryFolder, "tailer2-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        writeString(file, "Line");

        TestUtils.sleep(delay * 2);
        List<String> lines = listener.getLines();
        assertEquals(0, lines.size(), "1 line count");

        writeString(file, " one\n");
        TestUtils.sleep(delay * 2);
        lines = listener.getLines();

        assertEquals(1, lines.size(), "1 line count");
        assertEquals("Line one", lines.get(0), "1 line 1");

        listener.clear();
    }

    @Test
    public void testTailer() throws Exception {

        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(temporaryFolder, "tailer1-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        tailer = new Tailer(file, listener, delayMillis, false, isWindows);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        write(file, "Line one", "Line two");
        final long testDelayMillis = delayMillis * 10;
        TestUtils.sleep(testDelayMillis);
        List<String> lines = listener.getLines();
        assertEquals(2, lines.size(), "1 line count");
        assertEquals("Line one", lines.get(0), "1 line 1");
        assertEquals("Line two", lines.get(1), "1 line 2");
        listener.clear();

        // Write another line to the file
        write(file, "Line three");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertEquals(1, lines.size(), "2 line count");
        assertEquals("Line three", lines.get(0), "2 line 3");
        listener.clear();

        // Check file does actually have all the lines
        lines = FileUtils.readLines(file, "UTF-8");
        assertEquals(3, lines.size(), "3 line count");
        assertEquals("Line one", lines.get(0), "3 line 1");
        assertEquals("Line two", lines.get(1), "3 line 2");
        assertEquals("Line three", lines.get(2), "3 line 3");

        // Delete & re-create
        file.delete();
        final boolean exists = file.exists();
        assertFalse(exists, "File should not exist");
        createFile(file, 0);
        TestUtils.sleep(testDelayMillis);

        // Write another line
        write(file, "Line four");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertEquals(1, lines.size(), "4 line count");
        assertEquals("Line four", lines.get(0), "4 line 3");
        listener.clear();

        // Stop
        thread.interrupt();
        TestUtils.sleep(testDelayMillis * 4);
        write(file, "Line five");
        assertEquals(0, listener.getLines().size(), "4 line count");
        assertNotNull(listener.exception, "Missing InterruptedException");
        assertTrue(listener.exception instanceof InterruptedException, "Unexpected Exception: " + listener.exception);
        assertEquals(1 , listener.initialised, "Expected init to be called");
        assertEquals(0 , listener.notFound, "fileNotFound should not be called");
        assertEquals(1 , listener.rotated, "fileRotated should be be called");
    }

    @Test
    public void testTailerEndOfFileReached() throws Exception {
        // Create & start the Tailer
        final long delayMillis = 50;
        final long testDelayMillis = delayMillis * 10;
        final File file = new File(temporaryFolder, "tailer-eof-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        tailer = new Tailer(file, listener, delayMillis, false, isWindows);
        final Thread thread = new Thread(tailer);
        thread.start();

        // write a few lines
        write(file, "line1", "line2", "line3");
        TestUtils.sleep(testDelayMillis);

        // write a few lines
        write(file, "line4", "line5", "line6");
        TestUtils.sleep(testDelayMillis);

        // write a few lines
        write(file, "line7", "line8", "line9");
        TestUtils.sleep(testDelayMillis);

        // May be > 3 times due to underlying OS behavior wrt streams
        assertTrue(listener.reachedEndOfFile >= 3, "end of file reached at least 3 times");
    }

    protected void createFile(final File file, final long size)
        throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(file))) {
            TestUtils.generateTestData(output, size);
        }

        // try to make sure file is found
        // (to stop continuum occasionally failing)
        RandomAccessFile reader = null;
        try {
            while (reader == null) {
                try {
                    reader = new RandomAccessFile(file.getPath(), "r");
                } catch (final FileNotFoundException ignore) {
                }
                try {
                    TestUtils.sleep(200L);
                } catch (final InterruptedException ignore) {
                    // ignore
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /** Append some lines to a file */
    private void write(final File file, final String... lines) throws Exception {
        try (FileWriter writer = new FileWriter(file, true)) {
            for (final String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    /** Append a string to a file */
    private void writeString(final File file, final String ... strings) throws Exception {
        try (FileWriter writer = new FileWriter(file, true)) {
            for (final String string : strings) {
                writer.write(string);
            }
        }
    }

    @Test
    public void testStopWithNoFile() throws Exception {
        final File file = new File(temporaryFolder,"nosuchfile");
        assertFalse(file.exists(), "nosuchfile should not exist");
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = Tailer.create(file, listener, delay, false);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertNull(listener.exception, "Should not generate Exception");
        assertEquals(1 , listener.initialised, "Expected init to be called");
        assertTrue(listener.notFound > 0, "fileNotFound should be called");
        assertEquals(0 , listener.rotated, "fileRotated should be not be called");
        assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
    }

    /*
     * Tests [IO-357][Tailer] InterruptedException while the thead is sleeping is silently ignored.
     */
    @Test
    public void testInterrupt() throws Exception {
        final File file = new File(temporaryFolder, "nosuchfile");
        assertFalse(file.exists(), "nosuchfile should not exist");
        final TestTailerListener listener = new TestTailerListener();
        // Use a long delay to try to make sure the test thread calls interrupt() while the tailer thread is sleeping.
        final int delay = 1000;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false, 4096);
        final Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        TestUtils.sleep(idle);
        thread.interrupt();
        TestUtils.sleep(delay + idle);
        assertNotNull(listener.exception, "Missing InterruptedException");
        assertTrue(listener.exception instanceof InterruptedException, "Unexpected Exception: " + listener.exception);
        assertEquals(1, listener.initialised, "Expected init to be called");
        assertTrue(listener.notFound > 0, "fileNotFound should be called");
        assertEquals(0, listener.rotated, "fileRotated should be not be called");
        assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
    }

    @Test
    public void testStopWithNoFileUsingExecutor() throws Exception {
        final File file = new File(temporaryFolder,"nosuchfile");
        assertFalse(file.exists(), "nosuchfile should not exist");
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false);
        final Executor exec = new ScheduledThreadPoolExecutor(1);
        exec.execute(tailer);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertNull(listener.exception, "Should not generate Exception");
        assertEquals(1 , listener.initialised, "Expected init to be called");
        assertTrue(listener.notFound > 0, "fileNotFound should be called");
        assertEquals(0 , listener.rotated, "fileRotated should be not be called");
        assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
    }

    @Test
    public void testIO335() throws Exception { // test CR behavior
        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(temporaryFolder, "tailer-testio334.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delayMillis, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        writeString(file, "CRLF\r\n", "LF\n", "CR\r", "CRCR\r\r", "trail");
        final long testDelayMillis = delayMillis * 10;
        TestUtils.sleep(testDelayMillis);
        final List<String> lines = listener.getLines();
        assertEquals(4, lines.size(), "line count");
        assertEquals("CRLF", lines.get(0), "line 1");
        assertEquals("LF", lines.get(1), "line 2");
        assertEquals("CR", lines.get(2), "line 3");
        assertEquals("CRCR\r", lines.get(3), "line 4");
    }

    /**
     * Test {@link TailerListener} implementation.
     */
    private static class TestTailerListener extends TailerListenerAdapter {

        // Must be synchronised because it is written by one thread and read by another
        private final List<String> lines = Collections.synchronizedList(new ArrayList<String>());

        volatile Exception exception = null;

        volatile int notFound = 0;

        volatile int rotated = 0;

        volatile int initialised = 0;

        volatile int reachedEndOfFile = 0;

        @Override
        public void handle(final String line) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }

        public void clear() {
            lines.clear();
        }

        @Override
        public void handle(final Exception e) {
            exception = e;
        }

        @Override
        public void init(final Tailer tailer) {
            initialised++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileNotFound() {
            notFound++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileRotated() {
            rotated++; // not atomic, but OK because only updated here.
        }

        @Override
        public void endOfFileReached() {
            reachedEndOfFile++; // not atomic, but OK because only updated here.
        }
    }
}
