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

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import junit.framework.TestCase;

/**
 * {@link FilesystemMonitor} Test Case.
 */
public class FileSystemMonitorTestCase extends TestCase {

    /** Filesystem observer */
    protected FilesystemObserver observer;

    /** Listener which collects file changes */
    protected CollectionFilesystemListener listener = new CollectionFilesystemListener(false);

    /** Directory for test files */
    protected File testDir;

    /**
     * Construct a new test case.
     *
     * @param name The name of the test
     */
    public FileSystemMonitorTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        testDir = new File(new File("."), "test-observer");
        if (testDir.exists()) {
            FileUtils.cleanDirectory(testDir);
        } else {
            testDir.mkdir();
        }

        IOFileFilter files = FileFilterUtils.fileFileFilter();
        IOFileFilter javaSuffix = FileFilterUtils.suffixFileFilter(".java");
        IOFileFilter fileFilter = FileFilterUtils.and(files, javaSuffix);
        
        IOFileFilter directories = FileFilterUtils.directoryFileFilter();
        IOFileFilter visible = HiddenFileFilter.VISIBLE;
        IOFileFilter dirFilter = FileFilterUtils.and(directories, visible);

        IOFileFilter filter = FileFilterUtils.or(dirFilter, fileFilter);

        // Create the observer
        observer = new FilesystemObserver(testDir, filter);
        observer.addListener(listener);
        try {
            observer.initialize();
        } catch (Exception e) {
            fail("Observer init() threw " + e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(testDir);
    }

    /**
     * Test checkAndNotify() method
     */
    public void testMonitor() {
        try {
            long interval = 100;
            FilesystemMonitor monitor = new FilesystemMonitor(interval, observer);
            monitor.start();

            // Create a File
            checkCollectionsEmpty("A");
            File file1 = touch(new File(testDir, "file1.java"));
            checkFile("Create", file1, interval, listener.getCreatedFiles());
            listener.clear();

            // Update a file
            checkCollectionsEmpty("B");
            file1 = touch(file1);
            checkFile("Update", file1, interval, listener.getChangedFiles());
            listener.clear();

            // Delete a file
            checkCollectionsEmpty("C");
            file1.delete();
            checkFile("Delete", file1, interval, listener.getDeletedFiles());
            listener.clear();

            // Stop monitoring
            monitor.stop();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Threw " + e);
        }
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFile(String label, File file, long interval, Collection<File> files) throws Exception {
        for (int i = 0; i < 5; i++) {
            Thread.sleep(interval);
            if (files.contains(file)) {
                return; // found, test passes
            }
        }
        fail(label + " " + file + " not found");
    }

    /**
     * Check all the Collections are empty
     */
    private void checkCollectionsEmpty(String label) {
        checkDirectoryCounts("EMPTY-" + label, 0, 0, 0);
        checkFileCounts("EMPTY-" + label, 0, 0, 0);
    }

    /**
     * Check all the Directory Collections have the expected sizes.
     */
    private void checkDirectoryCounts(String label, int dirCreate, int dirChange, int dirDelete) {
        label = label + "[" + listener.getCreatedDirectories().size() +
                        " " + listener.getChangedDirectories().size() +
                        " " + listener.getDeletedDirectories().size() + "]";
        assertEquals(label + ": No. of directories created",  dirCreate,  listener.getCreatedDirectories().size());
        assertEquals(label + ": No. of directories changed",  dirChange,  listener.getChangedDirectories().size());
        assertEquals(label + ": No. of directories deleted",  dirDelete,  listener.getDeletedDirectories().size());
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFileCounts(String label, int fileCreate, int fileChange, int fileDelete) {
        label = label + "[" + listener.getCreatedFiles().size() +
                        " " + listener.getChangedFiles().size() +
                        " " + listener.getDeletedFiles().size() + "]";
        assertEquals(label + ": No. of files created", fileCreate, listener.getCreatedFiles().size());
        assertEquals(label + ": No. of files changed", fileChange, listener.getChangedFiles().size());
        assertEquals(label + ": No. of files deleted", fileDelete, listener.getDeletedFiles().size());
    }

    /**
     * Either creates a file if it doesn't exist or updates the last modified date/time
     * if it does.
     *
     * @param file The file to touch
     * @return The file
     */
    private File touch(File file) {
        long lastModified = file.exists() ? file.lastModified() : 0;
        try {
            FileUtils.touch(file);
            file = new File(file.getParent(), file.getName());
            while (lastModified == file.lastModified()) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException ie) {
                    // ignore
                }
                FileUtils.touch(file);
                file = new File(file.getParent(), file.getName());
            }
        } catch (Exception e) {
            fail("Touching " + file +": " + e);
        }
        return file;
    }
}
