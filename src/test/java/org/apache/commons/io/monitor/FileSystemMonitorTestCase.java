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

/**
 * {@link FilesystemMonitor} Test Case.
 */
public class FileSystemMonitorTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     * @param name The name of the test
     */
    public FileSystemMonitorTestCase(String name) {
        super(name);
        testDirName = "test-monitor";
    }

    @Override
    protected void setUp() throws Exception {
        listener = new CollectionFilesystemListener(false);
        super.setUp();
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

        } catch (Exception e) {
            e.printStackTrace();
            fail("Threw " + e);
        }
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFile(String label, File file, Collection<File> files) {
        for (int i = 0; i < 20; i++) {
            if (files.contains(file)) {
                return; // found, test passes
            }
            sleepHandleInterruped(pauseTime);
        }
        fail(label + " " + file + " not found");
    }
}
