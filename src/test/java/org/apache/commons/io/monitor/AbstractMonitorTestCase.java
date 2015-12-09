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
import java.io.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.After;
import org.junit.Before;

import static org.apache.commons.io.testtools.TestUtils.sleepQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@link FileAlterationObserver} Test Case.
 */
public abstract class AbstractMonitorTestCase  {

    /** File observer */
    protected FileAlterationObserver observer;

    /** Listener which collects file changes */
    protected CollectionFileListener listener;

    /** Test diretory name */
    protected String testDirName = null;

    /** Directory for test files */
    protected File testDir;

    /** Time in milliseconds to pause in tests */
    protected long pauseTime = 100L;

    @Before
    public void setUp() throws Exception {
        testDir = new File(new File("."), testDirName);
        if (testDir.exists()) {
            FileUtils.cleanDirectory(testDir);
        } else {
            testDir.mkdir();
        }

        final IOFileFilter files = FileFilterUtils.fileFileFilter();
        final IOFileFilter javaSuffix = FileFilterUtils.suffixFileFilter(".java");
        final IOFileFilter fileFilter = FileFilterUtils.and(files, javaSuffix);

        final IOFileFilter directories = FileFilterUtils.directoryFileFilter();
        final IOFileFilter visible = HiddenFileFilter.VISIBLE;
        final IOFileFilter dirFilter = FileFilterUtils.and(directories, visible);

        final IOFileFilter filter = FileFilterUtils.or(dirFilter, fileFilter);

        createObserver(testDir, filter);
    }

    /**
     * Create a {@link FileAlterationObserver}.
     *
     * @param file The directory to observe
     * @param fileFilter The file filter to apply
     */
    protected void createObserver(final File file, final FileFilter fileFilter) {
        observer = new FileAlterationObserver(file, fileFilter);
        observer.addListener(listener);
        observer.addListener(new FileAlterationListenerAdaptor());
        try {
            observer.initialize();
        } catch (final Exception e) {
            fail("Observer init() threw " + e);
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(testDir);
    }

    /**
     * Check all the Collections are empty
     *
     * @param label the label to use for this check
     */
    protected void checkCollectionsEmpty(final String label) {
        checkCollectionSizes("EMPTY-" + label, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Check all the Collections have the expected sizes.
     *
     * @param label the label to use for this check
     * @param dirCreate expected number of dirs created
     * @param dirChange expected number of dirs changed
     * @param dirDelete expected number of dirs deleted
     * @param fileCreate expected number of files created
     * @param fileChange expected number of files changed
     * @param fileDelete expected number of files deleted
     */
    protected void checkCollectionSizes(String label, 
                                        final int dirCreate, 
                                        final int dirChange, 
                                        final int dirDelete, 
                                        final int fileCreate, 
                                        final int fileChange, 
                                        final int fileDelete) {
        label = label + "[" + listener.getCreatedDirectories().size() +
                        " " + listener.getChangedDirectories().size() +
                        " " + listener.getDeletedDirectories().size() +
                        " " + listener.getCreatedFiles().size() +
                        " " + listener.getChangedFiles().size() +
                        " " + listener.getDeletedFiles().size() + "]";
        assertEquals(label + ": No. of directories created",  dirCreate,  listener.getCreatedDirectories().size());
        assertEquals(label + ": No. of directories changed",  dirChange,  listener.getChangedDirectories().size());
        assertEquals(label + ": No. of directories deleted",  dirDelete,  listener.getDeletedDirectories().size());
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
    protected File touch(File file) {
        final long lastModified = file.exists() ? file.lastModified() : 0;
        try {
            FileUtils.touch(file);
            file = new File(file.getParent(), file.getName());
            while (lastModified == file.lastModified()) {
                sleepQuietly(pauseTime);
                FileUtils.touch(file);
                file = new File(file.getParent(), file.getName());
            }
        } catch (final Exception e) {
            fail("Touching " + file + ": " + e);
        }
        sleepQuietly(pauseTime);
        return file;
    }

}
