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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.TestResources;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.collect.Lists;

/**
 * Test for {@link Tailer}.
 */
public class TailerTest {

    private static class NonStandardTailable implements Tailer.Tailable {

        private final File file;

        public NonStandardTailable(final File file) {
            this.file = file;
        }

        @Override
        public Tailer.RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            return new Tailer.RandomAccessResourceBridge() {

                private final RandomAccessFile randomAccessFile = new RandomAccessFile(file, mode);

                @Override
                public void close() throws IOException {
                    randomAccessFile.close();
                }

                @Override
                public long getPointer() throws IOException {
                    return randomAccessFile.getFilePointer();
                }

                @Override
                public int read(final byte[] b) throws IOException {
                    return randomAccessFile.read(b);
                }

                @Override
                public void seek(final long position) throws IOException {
                    randomAccessFile.seek(position);
                }
            };
        }

        @Override
        public boolean isNewer(final FileTime fileTime) throws IOException {
            return FileUtils.isFileNewer(file, fileTime);
        }

        @Override
        public FileTime lastModifiedFileTime() throws IOException {
            return FileUtils.lastModifiedFileTime(file);
        }

        @Override
        public long size() {
            return file.length();
        }
    }

    /**
     * Test {@link TailerListener} implementation.
     */
    private static class TestTailerListener extends TailerListenerAdapter {

        // Must be synchronized because it is written by one thread and read by another
        private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

        private final CountDownLatch latch;

        volatile Exception exception;

        volatile int notFound;

        volatile int rotated;

        volatile int initialized;

        volatile int reachedEndOfFile;

        public TestTailerListener() {
            latch = new CountDownLatch(1);
        }

        public TestTailerListener(final int expectedLines) {
            latch = new CountDownLatch(expectedLines);
        }

        public boolean awaitExpectedLines(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
            return latch.await(timeout, timeUnit);
        }

        public void clear() {
            lines.clear();
        }

        @Override
        public void endOfFileReached() {
            reachedEndOfFile++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileNotFound() {
            notFound++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileRotated() {
            rotated++; // not atomic, but OK because only updated here.
        }

        public List<String> getLines() {
            return lines;
        }

        @Override
        public void handle(final Exception e) {
            exception = e;
        }

        @Override
        public void handle(final String line) {
            lines.add(line);
            latch.countDown();
        }

        @Override
        public void init(final Tailer tailer) {
            initialized++; // not atomic, but OK because only updated here.
        }
    }

    private static final int TEST_BUFFER_SIZE = 1024;

    private static final int TEST_DELAY_MILLIS = 1500;

    @TempDir
    public static File temporaryFolder;

    protected void createFile(final File file, final long size) throws IOException {
        assertTrue(file.getParentFile().exists(), () -> "Cannot create file " + file + " as the parent directory does not exist");
        try (final BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            TestUtils.generateTestData(output, size);
        }

        // try to make sure file is found
        // (to stop continuum occasionally failing)
        RandomAccessFile reader = null;
        try {
            while (reader == null) {
                try {
                    reader = RandomAccessFileMode.READ_ONLY.create(file);
                } catch (final FileNotFoundException ignore) {
                    // ignore
                }
                TestUtils.sleepQuietly(200L);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }
        // sanity checks
        assertTrue(file.exists());
        assertEquals(size, file.length());
    }

    @Test
    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    public void testBufferBreak() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testBufferBreak.txt");
        createFile(file, 0);
        writeString(file, "SBTOURIST\n");

        final TestTailerListener listener = new TestTailerListener();
        try (Tailer tailer = new Tailer(file, listener, delay, false, 1)) {
            final Thread thread = new Thread(tailer);
            thread.start();

            List<String> lines = listener.getLines();
            while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
                lines = listener.getLines();
            }

            listener.clear();
        }
    }

    @Test
    public void testBuilderWithNonStandardTailable() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start-with-reopen-and-buffersize-and-charset.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer.Builder(new NonStandardTailable(file), listener).build()) {
            assertTrue(tailer.getTailable() instanceof NonStandardTailable);
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreate() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreaterWithDelayAndFromStartWithReopen() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start-with-reopen.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener, TEST_DELAY_MILLIS, false, false)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreateWithDelay() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener, TEST_DELAY_MILLIS)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreateWithDelayAndFromStart() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener, TEST_DELAY_MILLIS, false)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreateWithDelayAndFromStartWithBufferSize() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start-with-buffersize.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener, TEST_DELAY_MILLIS, false, TEST_BUFFER_SIZE)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreateWithDelayAndFromStartWithReopenAndBufferSize() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start-with-reopen-and-buffersize.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, listener, TEST_DELAY_MILLIS, false, true, TEST_BUFFER_SIZE)) {
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testCreateWithDelayAndFromStartWithReopenAndBufferSizeAndCharset() throws Exception {
        final File file = new File(temporaryFolder, "tailer-create-with-delay-and-from-start-with-reopen-and-buffersize-and-charset.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = Tailer.create(file, StandardCharsets.UTF_8, listener, TEST_DELAY_MILLIS, false, true, TEST_BUFFER_SIZE)) {
            validateTailer(listener, tailer, file);
        }
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
        try (Tailer tailer = new Tailer(file, listener, delay, false, IOUtils.DEFAULT_BUFFER_SIZE)) {
            final Thread thread = new Thread(tailer);
            thread.setDaemon(true);
            thread.start();
            TestUtils.sleep(idle);
            thread.interrupt();
            TestUtils.sleep(delay + idle);
            assertNotNull(listener.exception, "Missing InterruptedException");
            assertTrue(listener.exception instanceof InterruptedException, "Unexpected Exception: " + listener.exception);
            assertEquals(1, listener.initialized, "Expected init to be called");
            assertTrue(listener.notFound > 0, "fileNotFound should be called");
            assertEquals(0, listener.rotated, "fileRotated should be not be called");
            assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
        }
    }

    @Test
    public void testIO335() throws Exception { // test CR behavior
        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(temporaryFolder, "tailer-testio334.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        try (Tailer tailer = new Tailer(file, listener, delayMillis, false)) {
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
    }

    @Test
    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    public void testLongFile() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testLongFile.txt");
        createFile(file, 0);
        try (final Writer writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND)) {
            for (int i = 0; i < 100000; i++) {
                writer.write("LineLineLineLineLineLineLineLineLineLine\n");
            }
            writer.write("SBTOURIST\n");
        }

        final TestTailerListener listener = new TestTailerListener();
        try (Tailer tailer = new Tailer(file, listener, delay, false)) {

            // final long start = System.currentTimeMillis();

            final Thread thread = new Thread(tailer);
            thread.start();

            List<String> lines = listener.getLines();
            while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
                lines = listener.getLines();
            }
            // System.out.println("Elapsed: " + (System.currentTimeMillis() - start));

            listener.clear();
        }
    }

    @Test
    public void testMultiByteBreak() throws Exception {
        // System.out.println("testMultiByteBreak() Default charset: " + Charset.defaultCharset().displayName());
        final long delay = 50;
        final File origin = TestResources.getFile("test-file-utf8.bin");
        final File file = new File(temporaryFolder, "testMultiByteBreak.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        // Need to use UTF-8 to read & write the file otherwise it can be corrupted (depending on the default charset)
        final Charset charsetUTF8 = StandardCharsets.UTF_8;
        try (Tailer tailer = new Tailer(file, charsetUTF8, listener, delay, false, isWindows, IOUtils.DEFAULT_BUFFER_SIZE)) {
            final Thread thread = new Thread(tailer);
            thread.start();

            try (Writer out = new OutputStreamWriter(Files.newOutputStream(file.toPath()), charsetUTF8);
                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(origin.toPath()), charsetUTF8))) {
                final List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.write(line);
                    out.write("\n");
                    lines.add(line);
                }
                out.close(); // ensure data is written

                final long testDelayMillis = delay * 10;
                TestUtils.sleep(testDelayMillis);
                final List<String> tailerlines = listener.getLines();
                assertEquals(lines.size(), tailerlines.size(), "line count");
                for (int i = 0, len = lines.size(); i < len; i++) {
                    final String expected = lines.get(i);
                    final String actual = tailerlines.get(i);
                    if (!expected.equals(actual)) {
                        fail("Line: " + i + "\nExp: (" + expected.length() + ") " + expected + "\nAct: (" + actual.length() + ") " + actual);
                    }
                }
            }
        }
    }

    @Test
    public void testSimpleConstructor() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelay() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener, TEST_DELAY_MILLIS)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelayAndFromStart() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay-and-from-start.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener, TEST_DELAY_MILLIS, false)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelayAndFromStartWithBufferSize() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay-and-from-start-with-buffersize.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener, TEST_DELAY_MILLIS, false, TEST_BUFFER_SIZE)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelayAndFromStartWithReopen() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay-and-from-start-with-reopen.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener, TEST_DELAY_MILLIS, false, false)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelayAndFromStartWithReopenAndBufferSize() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay-and-from-start-with-reopen-and-buffersize.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, listener, TEST_DELAY_MILLIS, false, true, TEST_BUFFER_SIZE)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testSimpleConstructorWithDelayAndFromStartWithReopenAndBufferSizeAndCharset() throws Exception {
        final File file = new File(temporaryFolder, "tailer-simple-constructor-with-delay-and-from-start-with-reopen-and-buffersize-and-charset.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener(1);
        try (final Tailer tailer = new Tailer(file, StandardCharsets.UTF_8, listener, TEST_DELAY_MILLIS, false, true, TEST_BUFFER_SIZE)) {
            final Thread thread = new Thread(tailer);
            thread.start();
            validateTailer(listener, tailer, file);
        }
    }

    @Test
    public void testStopWithNoFile() throws Exception {
        final File file = new File(temporaryFolder, "nosuchfile");
        assertFalse(file.exists(), "nosuchfile should not exist");
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        try (Tailer tailer = Tailer.create(file, listener, delay, false)) {
            TestUtils.sleep(idle);
        }
        TestUtils.sleep(delay + idle);
        assertNull(listener.exception, "Should not generate Exception");
        assertEquals(1, listener.initialized, "Expected init to be called");
        assertTrue(listener.notFound > 0, "fileNotFound should be called");
        assertEquals(0, listener.rotated, "fileRotated should be not be called");
        assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
    }

    @Test
    public void testStopWithNoFileUsingExecutor() throws Exception {
        final File file = new File(temporaryFolder, "nosuchfile");
        assertFalse(file.exists(), "nosuchfile should not exist");
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        try (Tailer tailer = new Tailer(file, listener, delay, false)) {
            final Executor exec = new ScheduledThreadPoolExecutor(1);
            exec.execute(tailer);
            TestUtils.sleep(idle);
        }
        TestUtils.sleep(delay + idle);
        assertNull(listener.exception, "Should not generate Exception");
        assertEquals(1, listener.initialized, "Expected init to be called");
        assertTrue(listener.notFound > 0, "fileNotFound should be called");
        assertEquals(0, listener.rotated, "fileRotated should be not be called");
        assertEquals(0, listener.reachedEndOfFile, "end of file never reached");
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
        try (Tailer tailer = new Tailer(file, listener, delayMillis, false, isWindows)) {
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
            assertFalse(file.exists(), "File should not exist");
            createFile(file, 0);
            assertTrue(file.exists(), "File should now exist");
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
            assertEquals(1, listener.initialized, "Expected init to be called");
            // assertEquals(0 , listener.notFound, "fileNotFound should not be called"); // there is a window when it might be
            // called
            assertEquals(1, listener.rotated, "fileRotated should be be called");
        }
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
        try (Tailer tailer = new Tailer(file, listener, delayMillis, false, isWindows)) {
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
    }

    @Test
    public void testTailerEof() throws Exception {
        // Create & start the Tailer
        final long delayMillis = 100;
        final File file = new File(temporaryFolder, "tailer2-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        try (Tailer tailer = new Tailer(file, listener, delayMillis, false)) {
            final Thread thread = new Thread(tailer);
            thread.start();

            // Write some lines to the file
            writeString(file, "Line");

            TestUtils.sleep(delayMillis * 2);
            List<String> lines = listener.getLines();
            assertEquals(0, lines.size(), "1 line count");

            writeString(file, " one\n");
            TestUtils.sleep(delayMillis * 4);
            lines = listener.getLines();

            assertEquals(1, lines.size(), "1 line count");
            assertEquals("Line one", lines.get(0), "1 line 1");

            listener.clear();
        }
    }

    private void validateTailer(final TestTailerListener listener, final Tailer tailer, final File file) throws Exception {
        write(file, "foo");
        final int timeout = 30;
        final TimeUnit timeoutUnit = TimeUnit.SECONDS;
        assertTrue(listener.awaitExpectedLines(timeout, timeoutUnit), () -> String.format("await timed out after %s %s", timeout, timeoutUnit));
        assertEquals(listener.getLines(), Lists.newArrayList("foo"), "lines");
    }

    /** Appends lines to a file */
    private void write(final File file, final String... lines) throws Exception {
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND)) {
            for (final String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    /** Appends strings to a file */
    private void writeString(final File file, final String... strings) throws Exception {
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND)) {
            for (final String string : strings) {
                writer.write(string);
            }
        }
    }
}
