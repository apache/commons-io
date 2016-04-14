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
package org.apache.commons.io.monitor;

import org.apache.commons.io.testtools.TestUtils;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link FileAlterationMonitor} Test Case.
 */
public class FileAlterationMonitorTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     */
    public FileAlterationMonitorTestCase() {
        testDirName = "test-monitor";
        listener = new CollectionFileListener(false);
    }

    /**
     * Test default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        final FileAlterationMonitor monitor = new FileAlterationMonitor();
        assertEquals("Interval", 10000, monitor.getInterval());
    }

    /**
     * Test add/remove observers.
     */
    @Test
    public void testAddRemoveObservers() {
        FileAlterationObserver[] observers = null;
        FileAlterationMonitor monitor = null;

        // Null Observers
        monitor = new FileAlterationMonitor(123, observers);
        assertEquals("Interval", 123, monitor.getInterval());
        assertFalse("Observers[1]", monitor.getObservers().iterator().hasNext());

        // Null Observer
        observers = new FileAlterationObserver[1]; // observer is null
        monitor = new FileAlterationMonitor(456, observers);
        assertFalse("Observers[2]", monitor.getObservers().iterator().hasNext());

        // Null Observer
        monitor.addObserver(null);
        assertFalse("Observers[3]", monitor.getObservers().iterator().hasNext());
        monitor.removeObserver(null);

        // Add Observer
        final FileAlterationObserver observer = new FileAlterationObserver("foo");
        monitor.addObserver(observer);
        final Iterator<FileAlterationObserver> it = monitor.getObservers().iterator();
        assertTrue("Observers[4]", it.hasNext());
        assertEquals("Added", observer, it.next());
        assertFalse("Observers[5]", it.hasNext());

        // Remove Observer
        monitor.removeObserver(observer);
        assertFalse("Observers[6]", monitor.getObservers().iterator().hasNext());
    }

    /**
     * Test checkAndNotify() method
     */
    @Test
    public void testMonitor() {
        try {
            final long interval = 100;
            listener.clear();
            final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
            assertEquals("Interval", interval, monitor.getInterval());
            monitor.start();

            try {
                monitor.start(); // try and start again
            } catch (final IllegalStateException e) {
                // expected result, monitor already running
            }

            // Create a File
            checkCollectionsEmpty("A");
            File file1 = touch(new File(testDir, "file1.java"));
            checkFile("Create", file1, listener.getCreatedFiles());
            listener.clear();

            // Update a file
            checkCollectionsEmpty("B");
            file1 = touch(file1);
            checkFile("Update", file1, listener.getChangedFiles());
            listener.clear();

            // Delete a file
            checkCollectionsEmpty("C");
            file1.delete();
            checkFile("Delete", file1, listener.getDeletedFiles());
            listener.clear();

            // Stop monitoring
            monitor.stop();

            try {
                monitor.stop(); // try and stop again
            } catch (final IllegalStateException e) {
                // expected result, monitor already stopped
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Threw " + e);
        }
    }

    /**
     * Test using a thread factory.
     */
    @Test
    public void testThreadFactory() {
        try {
            final long interval = 100;
            listener.clear();
            final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
            monitor.setThreadFactory(Executors.defaultThreadFactory());
            assertEquals("Interval", interval, monitor.getInterval());
            monitor.start();

            // Create a File
            checkCollectionsEmpty("A");
            final File file2 = touch(new File(testDir, "file2.java"));
            checkFile("Create", file2, listener.getCreatedFiles());
            listener.clear();

            // Delete a file
            checkCollectionsEmpty("B");
            file2.delete();
            checkFile("Delete", file2, listener.getDeletedFiles());
            listener.clear();

            // Stop monitoring
            monitor.stop();

        } catch (final Exception e) {
            e.printStackTrace();
            fail("Threw " + e);
        }
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFile(final String label, final File file, final Collection<File> files) {
        for (int i = 0; i < 20; i++) {
            if (files.contains(file)) {
                return; // found, test passes
            }
            TestUtils.sleepQuietly(pauseTime);
        }
        fail(label + " " + file + " not found");
    }
}
