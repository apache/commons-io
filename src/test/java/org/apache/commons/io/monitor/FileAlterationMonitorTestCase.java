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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.testtools.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * {@link FileAlterationMonitor} Test Case.
 */
public class FileAlterationMonitorTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     */
    public FileAlterationMonitorTestCase() {
        listener = new CollectionFileListener(false);
    }

    /**
     * Test default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        final FileAlterationMonitor monitor = new FileAlterationMonitor();
        assertEquals(10000, monitor.getInterval(), "Interval");
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
        assertEquals(123, monitor.getInterval(), "Interval");
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[1]");

        // Null Observer
        observers = new FileAlterationObserver[1]; // observer is null
        monitor = new FileAlterationMonitor(456, observers);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[2]");

        // Null Observer
        monitor.addObserver(null);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[3]");
        monitor.removeObserver(null);

        // Add Observer
        final FileAlterationObserver observer = new FileAlterationObserver("foo");
        monitor.addObserver(observer);
        final Iterator<FileAlterationObserver> it = monitor.getObservers().iterator();
        assertTrue(it.hasNext(), "Observers[4]");
        assertEquals(observer, it.next(), "Added");
        assertFalse(it.hasNext(), "Observers[5]");

        // Remove Observer
        monitor.removeObserver(observer);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[6]");
    }

    /**
     * Test checkAndNotify() method
     * @throws Exception
     */
    @Test
    public void testMonitor() throws Exception {
        final long interval = 100;
        listener.clear();
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        assertEquals(interval, monitor.getInterval(), "Interval");
        monitor.start();

        try {
            monitor.start(); // try and start again
            fail("Expected IllegalStateException");
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
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException e) {
            // expected result, monitor already stopped
        }
    }

    /**
     * Test using a thread factory.
     * @throws Exception
     */
    @Test
    public void testThreadFactory() throws Exception {
        final long interval = 100;
        listener.clear();
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.setThreadFactory(Executors.defaultThreadFactory());
        assertEquals(interval, monitor.getInterval(), "Interval");
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

    /**
     * Test case for IO-535
     *
     * Verify that {@link FileAlterationMonitor#stop()} stops the created thread
     */
    @Test
    public void testStopWhileWaitingForNextInterval() throws Exception {
        final Collection<Thread> createdThreads = new ArrayList<>(1);
        final ThreadFactory threadFactory = new ThreadFactory() {
            private final ThreadFactory delegate = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = delegate.newThread(r);
                thread.setDaemon(true); //do not leak threads if the test fails
                createdThreads.add(thread);
                return thread;
            }
        };

        final FileAlterationMonitor monitor = new FileAlterationMonitor(1_000);
        monitor.setThreadFactory(threadFactory);

        monitor.start();
        assertFalse(createdThreads.isEmpty());

        Thread.sleep(10); // wait until the watcher thread enters Thread.sleep()
        monitor.stop(100);

        for (final Thread thread : createdThreads) {
            assertFalse(thread.isAlive(), "The FileAlterationMonitor did not stop the threads it created.");
        }
    }
}
