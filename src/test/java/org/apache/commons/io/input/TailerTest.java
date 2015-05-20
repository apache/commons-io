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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Tests for {@link Tailer}.
 *
 * @version $Id$
 */
public class TailerTest extends FileBasedTestCase {

    private Tailer tailer;

    public TailerTest(final String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        if (tailer != null) {
            tailer.stop();
            Thread.sleep(1000);
        }
        FileUtils.deleteDirectory(getTestDirectory());
        Thread.sleep(1000);
    }

    public void testLongFile() throws Exception {
        final long delay = 50;

        final File file = new File(getTestDirectory(), "testLongFile.txt");
        createFile(file, 0);
        final Writer writer = new FileWriter(file, true);
        for (int i = 0; i < 100000; i++) {
            writer.write("LineLineLineLineLineLineLineLineLineLine\n");
        }
        writer.write("SBTOURIST\n");
        IOUtils.closeQuietly(writer);

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

    @SuppressWarnings("deprecation") // unavoidable until Java 7
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
        final Charset charsetUTF8 = Charsets.UTF_8;
        tailer = new Tailer(file, charsetUTF8, listener, delay, false, isWindows, 4096);
        final Thread thread = new Thread(tailer);
        thread.start();

        Writer out = new OutputStreamWriter(new FileOutputStream(file), charsetUTF8);
        BufferedReader reader = null;
        try{
            List<String> lines = new ArrayList<String>();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(origin), charsetUTF8));
            String line = null;
            while((line = reader.readLine()) != null){
                out.write(line);
                out.write("\n");
                lines.add(line);
            }
            out.close(); // ensure data is written

           final long testDelayMillis = delay * 10;
           Thread.sleep(testDelayMillis);
           List<String> tailerlines = listener.getLines();
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
        }finally{
            tailer.stop();
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(out);
        }
    }

    public void testTailerEof() throws Exception {
        // Create & start the Tailer
        final long delay = 50;
        final File file = new File(getTestDirectory(), "tailer2-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final Tailer tailer = new Tailer(file, listener, delay, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        final FileWriter writer = null;
        try {
            writeString(file, "Line");

            Thread.sleep(delay * 2);
            List<String> lines = listener.getLines();
            assertEquals("1 line count", 0, lines.size());

            writeString(file, " one\n");
            Thread.sleep(delay * 2);
            lines = listener.getLines();

            assertEquals("1 line count", 1, lines.size());
            assertEquals("1 line 1", "Line one", lines.get(0));

            listener.clear();
        } finally {
            tailer.stop();
            Thread.sleep(delay * 2);
            IOUtils.closeQuietly(writer);
        }
    }

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
        Thread.sleep(testDelayMillis);
        List<String> lines = listener.getLines();
        assertEquals("1 line count", 2, lines.size());
        assertEquals("1 line 1", "Line one", lines.get(0));
        assertEquals("1 line 2", "Line two", lines.get(1));
        listener.clear();

        // Write another line to the file
        write(file, "Line three");
        Thread.sleep(testDelayMillis);
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
        Thread.sleep(testDelayMillis);

        // Write another line
        write(file, "Line four");
        Thread.sleep(testDelayMillis);
        lines = listener.getLines();
        assertEquals("4 line count", 1, lines.size());
        assertEquals("4 line 3", "Line four", lines.get(0));
        listener.clear();

        // Stop
        tailer.stop();
        tailer=null;
        thread.interrupt();
        Thread.sleep(testDelayMillis * 4);
        write(file, "Line five");
        assertEquals("4 line count", 0, listener.getLines().size());
        assertNotNull("Missing InterruptedException", listener.exception);
        assertTrue("Unexpected Exception: " + listener.exception, listener.exception instanceof InterruptedException);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertEquals("fileNotFound should not be called", 0 , listener.notFound);
        assertEquals("fileRotated should be be called", 1 , listener.rotated);
    }

    @Override
    protected void createFile(final File file, final long size)
        throws IOException {
        super.createFile(file, size);

        // try to make sure file is found
        // (to stop continuum occasionally failing)
        RandomAccessFile reader = null;
        try {
            while (reader == null) {
                try {
                    reader = new RandomAccessFile(file.getPath(), "r");
                } catch (final FileNotFoundException e) {
                }
                try {
                    Thread.sleep(200L);
                } catch (final InterruptedException e) {
                    // ignore
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /** Append some lines to a file */
    private void write(final File file, final String... lines) throws Exception {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            for (final String line : lines) {
                writer.write(line + "\n");
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /** Append a string to a file */
    private void writeString(final File file, final String ... strings) throws Exception {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            for (final String string : strings) {
                writer.write(string);
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void testStopWithNoFile() throws Exception {
        final File file = new File(getTestDirectory(),"nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = Tailer.create(file, listener, delay, false);
        Thread.sleep(idle);
        tailer.stop();
        tailer=null;
        Thread.sleep(delay+idle);
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0 , listener.rotated);
    }

    /*
     * Tests [IO-357][Tailer] InterruptedException while the thead is sleeping is silently ignored.
     */
    public void testInterrupt() throws Exception {
        final File file = new File(getTestDirectory(), "nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        final TestTailerListener listener = new TestTailerListener();
        // Use a long delay to try to make sure the test thread calls interrupt() while the tailer thread is sleeping.
        final int delay = 1000;
        final int idle = 50; // allow time for thread to work
        Tailer tailer = new Tailer(file, listener, delay, false, 4096);
        final Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        Thread.sleep(idle);
        thread.interrupt();
        tailer = null;
        Thread.sleep(delay + idle);
        assertNotNull("Missing InterruptedException", listener.exception);
        assertTrue("Unexpected Exception: " + listener.exception, listener.exception instanceof InterruptedException);
        assertEquals("Expected init to be called", 1, listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0, listener.rotated);
    }

    public void testStopWithNoFileUsingExecutor() throws Exception {
        final File file = new File(getTestDirectory(),"nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false);
        final Executor exec = new ScheduledThreadPoolExecutor(1);
        exec.execute(tailer);
        Thread.sleep(idle);
        tailer.stop();
        tailer=null;
        Thread.sleep(delay+idle);
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertTrue("fileNotFound should be called", listener.notFound > 0);
        assertEquals("fileRotated should be not be called", 0 , listener.rotated);
    }

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
        Thread.sleep(testDelayMillis);
        final List<String> lines = listener.getLines();
        assertEquals("line count", 4, lines.size());
        assertEquals("line 1", "CRLF", lines.get(0));
        assertEquals("line 2", "LF", lines.get(1));
        assertEquals("line 3", "CR", lines.get(2));
        assertEquals("line 4", "CRCR\r", lines.get(3));

        // Stop
        tailer.stop();
        tailer=null;
        thread.interrupt();
        Thread.sleep(testDelayMillis);
    }

    /**
     * Test {@link TailerListener} implementation.
     */
    private static class TestTailerListener implements TailerListener {

        // Must be synchronised because it is written by one thread and read by another
        private final List<String> lines = Collections.synchronizedList(new ArrayList<String>());

        volatile Exception exception = null;

        volatile int notFound = 0;

        volatile int rotated = 0;

        volatile int initialised = 0;

        public void handle(final String line) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }

        public void clear() {
            lines.clear();
        }

        public void handle(final Exception e) {
            exception = e;
        }

        public void init(final Tailer tailer) {
            initialised++; // not atomic, but OK because only updated here.
        }

        public void fileNotFound() {
            notFound++; // not atomic, but OK because only updated here.
        }

        public void fileRotated() {
            rotated++; // not atomic, but OK because only updated here.
        }
    }
}
