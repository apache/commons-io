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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link Tailer}.
 *
 */
public class TailerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File getTestDirectory() {
        return temporaryFolder.getRoot();
    }

    private Tailer tailer;

    @After
    public void tearDown() throws Exception {
        if (tailer != null) {
            tailer.stop();
        }
    }

    @Test
    public void testLongFile() throws Exception {
        final long delay = 50;

        final File file = new File(getTestDirectory(), "testLongFile.txt");
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
        System.out.println("Elapsed: " + (System.currentTimeMillis() - start));

        listener.clear();
    }

    @Test
    public void testBufferBreak() throws Exception {
        final long delay = 50;

        final File file = new File(getTestDirectory(), "testBufferBreak.txt");
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
        System.out.println("testMultiByteBreak() Default charset: "+Charset.defaultCharset().displayName());
        final long delay = 50;
        final File origin = new File(this.getClass().getResource("/test-file-utf8.bin").toURI());
        final File file = new File(getTestDirectory(), "testMultiByteBreak.txt");
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
           assertEquals("line count",lines.size(),tailerlines.size());
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
        final File file = new File(getTestDirectory(), "tailer2-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        writeString(file, "Line");

        TestUtils.sleep(delay * 2);
        List<String> lines = listener.getLines();
        assertEquals("1 line count", 0, lines.size());

        writeString(file, " one\n");
        TestUtils.sleep(delay * 2);
        lines = listener.getLines();

        assertEquals("1 line count", 1, lines.size());
        assertEquals("1 line 1", "Line one", lines.get(0));

        listener.clear();
    }

    @Test
    public void testTailer() throws Exception {

        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(getTestDirectory(), "tailer1-test.txt");
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
        assertEquals("1 line count", 2, lines.size());
        assertEquals("1 line 1", "Line one", lines.get(0));
        assertEquals("1 line 2", "Line two", lines.get(1));
        listener.clear();

        // Write another line to the file
        write(file, "Line three");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertEquals("2 line count", 1, lines.size());
        assertEquals("2 line 3", "Line three", lines.get(0));
        listener.clear();

        // Check file does actually have all the lines
        lines = FileUtils.readLines(file, "UTF-8");
        assertEquals("3 line count", 3, lines.size());
        assertEquals("3 line 1", "Line one", lines.get(0));
        assertEquals("3 line 2", "Line two", lines.get(1));
        assertEquals("3 line 3", "Line three", lines.get(2));

        // Delete & re-create
        file.delete();
        final boolean exists = file.exists();
        assertFalse("File should not exist", exists);
        createFile(file, 0);
        TestUtils.sleep(testDelayMillis);

        // Write another line
        write(file, "Line four");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertEquals("4 line count", 1, lines.size());
        assertEquals("4 line 3", "Line four", lines.get(0));
        listener.clear();

        // Stop
        thread.interrupt();
        TestUtils.sleep(testDelayMillis * 4);
        write(file, "Line five");
        assertEquals("4 line count", 0, listener.getLines().size());
        assertNotNull("Missing InterruptedException", listener.exception);
        assertTrue("Unexpected Exception: " + listener.exception, listener.exception instanceof InterruptedException);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertEquals("fileNotFound should not be called", 0 , listener.notFound);
        assertEquals("fileRotated should be be called", 1 , listener.rotated);
    }

    @Test
    public void testTailerEndOfFileReached() throws Exception {
        // Create & start the Tailer
        final long delayMillis = 50;
        final long testDelayMillis = delayMillis * 10;
        final File file = new File(getTestDirectory(), "tailer-eof-test.txt");
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

        // May be > 3 times due to underlying OS behaviour wrt streams
        assertTrue("end of file reached at least 3 times", listener.reachedEndOfFile >= 3);
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
        final File file = new File(getTestDirectory(),"nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = Tailer.create(file, listener, delay, false);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0 , listener.rotated);
        assertEquals("end of file never reached", 0, listener.reachedEndOfFile);
    }

    /*
     * Tests [IO-357][Tailer] InterruptedException while the thead is sleeping is silently ignored.
     */
    @Test
    public void testInterrupt() throws Exception {
        final File file = new File(getTestDirectory(), "nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
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
        assertNotNull("Missing InterruptedException", listener.exception);
        assertTrue("Unexpected Exception: " + listener.exception, listener.exception instanceof InterruptedException);
        assertEquals("Expected init to be called", 1, listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0, listener.rotated);
        assertEquals("end of file never reached", 0, listener.reachedEndOfFile);
    }

    @Test
    public void testStopWithNoFileUsingExecutor() throws Exception {
        final File file = new File(getTestDirectory(),"nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false);
        final Executor exec = new ScheduledThreadPoolExecutor(1);
        exec.execute(tailer);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0 , listener.rotated);
        assertEquals("end of file never reached", 0, listener.reachedEndOfFile);
    }

    @Test
    public void testIO335() throws Exception { // test CR behaviour
        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(getTestDirectory(), "tailer-testio334.txt");
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
        assertEquals("line count", 4, lines.size());
        assertEquals("line 1", "CRLF", lines.get(0));
        assertEquals("line 2", "LF", lines.get(1));
        assertEquals("line 3", "CR", lines.get(2));
        assertEquals("line 4", "CRCR\r", lines.get(3));
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
