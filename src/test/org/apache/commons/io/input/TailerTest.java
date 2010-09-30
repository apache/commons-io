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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Tests for {@link Tailer}.
 *
 * @version $Id$
 */
public class TailerTest extends FileBasedTestCase {

    public TailerTest(String name) {
        super(name);
    }

    public void testTailer() throws Exception {

        // Create & start the Tailer
        long delay = 50;
        File file = new File(getTestDirectory(), "tailer1-test.txt");
        createFile(file, 0);
        TestTailerListener listener = new TestTailerListener();
        Tailer tailer = start(file, listener, delay, false);

        // Write some lines to the file
        write(file, "Line one", "Line two");
        Thread.sleep(delay * 2);
        List<String> lines = listener.getLines();
        assertEquals("1 line count", 2, lines.size());
        assertEquals("1 line 1", "Line one", lines.get(0));
        assertEquals("1 line 2", "Line two", lines.get(1));
        listener.clear();

        // Write another line to the file
        write(file, "Line three");
        Thread.sleep(delay * 2);
        lines = listener.getLines();
        assertEquals("2 line count", 1, lines.size());
        assertEquals("2 line 3", "Line three", lines.get(0));
        listener.clear();

        // Check file does actually have all the lines
        lines = FileUtils.readLines(file);
        assertEquals("3 line count", 3, lines.size());
        assertEquals("3 line 1", "Line one", lines.get(0));
        assertEquals("3 line 2", "Line two", lines.get(1));
        assertEquals("3 line 3", "Line three", lines.get(2));

        // Delete & re-create
        file.delete();
        createFile(file, 0);
        Thread.sleep(delay * 2);

        // Write another line
        write(file, "Line four");
        Thread.sleep(delay * 2);
        lines = listener.getLines();
        assertEquals("4 line count", 1, lines.size());
        assertEquals("4 line 3", "Line four", lines.get(0));
        listener.clear();

        // Stop
        tailer.stop();
        Thread.sleep(delay * 2);
        write(file, "Line five");
        assertEquals("4 line count", 0, listener.getLines().size());
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertEquals("fileNotFound should not be called", 0 , listener.notFound);
        assertEquals("fileRotated should be be called", 1 , listener.rotated);
    }

    /** Start a tailer */
    private Tailer start(File file, TailerListener listener, long delay, boolean end) {
        Tailer tailer = new Tailer(file, listener, delay, end);
        Thread thread = new Thread(tailer);
        thread.start();
        return tailer;
    }

    /** Append some lines to a file */
    private void write(File file, String... lines) throws Exception {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void testStopWithNoFile() throws Exception {
        File file = new File(getTestDirectory(),"nosuchfile");
        assertFalse("nosuchfile should not exist", file.exists());
        TestTailerListener listener = new TestTailerListener();
        int delay = 100;
        int idle = 50; // allow time for thread to work
        Tailer tailer = start(file, listener, delay, false);
        Thread.sleep(idle);
        tailer.stop();
        Thread.sleep(delay+idle);
        assertNull("Should not generate Exception", listener.exception);
        assertEquals("Expected init to be called", 1 , listener.initialised);
        assertEquals("fileNotFound should be called", 1 , listener.notFound);
        assertEquals("fileRotated should be not be called", 0 , listener.rotated);
    }

    /**
     * Test {@link TailerListener} implementation.
     */
    private static class TestTailerListener implements TailerListener {

        private final List<String> lines = new ArrayList<String>();

        volatile Exception exception = null;
        
        volatile int notFound = 0;

        volatile int rotated = 0;
        
        volatile int initialised = 0;

        public void handle(String line) {
            lines.add(line);
        }
        public List<String> getLines() {
            return lines;
        }
        public void clear() {
            lines.clear();
        }
        public void handle(Exception e) {
            exception = e;
        }
        public void init(Tailer tailer) {
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
