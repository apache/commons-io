/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link FileAlterationObserver} to verify that when a {@link FileAlterationListener} throws an exception, other listeners are still notified.
 */
class FileAlterationObserverWithListenerExceptionTest {

    /**
     * Extended CollectionFileListener that tracks the number of times onStart and onStop are called.
     */
    private static final class CollectionFileListenerWithStartStopCount extends CollectionFileListener {

        private static final long serialVersionUID = 1L;

        private int startedCount;

        private int stoppedCount;

        CollectionFileListenerWithStartStopCount(final boolean clearOnStart) {
            super(clearOnStart);
        }

        public int getStartedCount() {
            return startedCount;
        }

        public int getStoppedCount() {
            return stoppedCount;
        }

        @Override
        public void onStart(final FileAlterationObserver observer) {
            super.onStart(observer);
            startedCount++;
        }

        @Override
        public void onStop(final FileAlterationObserver observer) {
            super.onStop(observer);
            stoppedCount++;
        }
    }

    /**
     * A listener that throws an exception during onStart.
     */
    private static final class FailingListener extends FileAlterationListenerAdaptor {

        private int onStartCount;

        public int getOnStartCount() {
            return onStartCount;
        }

        @Override
        public void onStart(final FileAlterationObserver observer) {
            onStartCount++;
            throw new RuntimeException("onStart");
        }
    }

    /**
     * A listener that throws an exception during onDirectoryChange.
     */
    private static final class FailingOnDirectoryChangeListener extends FileAlterationListenerAdaptor {

        @Override
        public void onDirectoryChange(final File directory) {
            throw new RuntimeException("onDirectoryChange");
        }
    }

    /**
     * A listener that throws an exception during onDirectoryCreate.
     */
    private static final class FailingOnDirectoryCreateListener extends FileAlterationListenerAdaptor {

        @Override
        public void onDirectoryCreate(final File directory) {
            throw new RuntimeException("onDirectoryCreate");
        }
    }

    /**
     * A listener that throws an exception during onDirectoryDelete.
     */
    private static final class FailingOnDirectoryDeleteListener extends FileAlterationListenerAdaptor {

        @Override
        public void onDirectoryDelete(final File directory) {
            throw new RuntimeException("onDirectoryDelete");
        }
    }

    /**
     * A listener that throws an exception during onFileChange.
     */
    private static final class FailingOnFileChangeListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileChange(final File file) {
            throw new RuntimeException("onFileChange");
        }
    }

    /**
     * A listener that throws an exception during onFileCreate.
     */
    private static final class FailingOnFileCreateListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(final File file) {
            throw new RuntimeException("onFileCreate");
        }
    }

    /**
     * A listener that throws an exception during onFileDelete.
     */
    private static final class FailingOnFileDeleteListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileDelete(final File file) {
            throw new RuntimeException("onFileDelete");
        }
    }

    /**
     * A listener that throws an exception during onStop.
     */
    private static final class FailingOnStopListener extends FileAlterationListenerAdaptor {

        @Override
        public void onStop(final FileAlterationObserver observer) {
            throw new RuntimeException("onStop");
        }
    }

    private static final long SLEEP_MILLIS = 110;

    /** Directory for test files */
    @TempDir
    File testDir;

    /** Listener which collects file changes */
    private CollectionFileListenerWithStartStopCount listener;

    /**
     * Sets up test fixtures
     */
    @BeforeEach
    void setUp() {
        listener = new CollectionFileListenerWithStartStopCount(true);
    }

    /**
     * Test that when one listener throws an exception during directory modification, other listeners still receive directory modification notifications.
     */
    @Test
    void testListenerExceptionDuringDirectoryChange() throws Exception {
        final File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onDirectoryChange
        final FailingOnDirectoryChangeListener failingOnDirectoryChangeListener = new FailingOnDirectoryChangeListener();
        observer.addListener(failingOnDirectoryChangeListener);
        // Add a working listener
        observer.addListener(listener);
        // First notification to register the directory
        observer.initialize();
        observer.checkAndNotify();
        // Clear collections
        listener.clear();
        // Modify the directory
        Thread.sleep(SLEEP_MILLIS);
        PathUtils.touch(testDirA.toPath(), FileTimes.plusSeconds(FileTimes.now(), 120));
        // Second notification
        observer.checkAndNotify();
        // Verify working listener received the directory change event
        assertEquals(1, listener.getChangedDirectories().size());
        assertTrue(listener.getChangedDirectories().contains(testDirA));
    }

    /**
     * Test that when one listener throws an exception during directory creation, other listeners still receive directory creation notifications.
     */
    @Test
    void testListenerExceptionDuringDirectoryCreate() throws Exception {
        final File testDirA = new File(testDir, "test-dir-A");
        testDirA.delete();
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onDirectoryCreate
        final FailingOnDirectoryCreateListener failingOnDirectoryCreateListener = new FailingOnDirectoryCreateListener();
        observer.addListener(failingOnDirectoryCreateListener);
        // Add a working listener
        observer.addListener(listener);
        observer.initialize();
        testDirA.mkdir();
        observer.checkAndNotify();
        // Verify working listener received the directory create event
        assertEquals(1, listener.getCreatedDirectories().size());
        assertTrue(listener.getCreatedDirectories().contains(testDirA));
    }

    /**
     * Test that when one listener throws an exception during directory deletion, other listeners still receive directory deletion notifications.
     */
    @Test
    void testListenerExceptionDuringDirectoryDelete() throws Exception {
        final File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onDirectoryDelete
        final FailingOnDirectoryDeleteListener failingOnDirectoryDeleteListener = new FailingOnDirectoryDeleteListener();
        observer.addListener(failingOnDirectoryDeleteListener);
        // Add a working listener
        observer.addListener(listener);
        // First notification to register the directory
        observer.initialize();
        observer.checkAndNotify();
        // Clear collections
        listener.clear();
        // Delete the directory
        FileUtils.deleteQuietly(testDirA);
        // Second notification
        observer.checkAndNotify();
        // Verify working listener received the directory delete event
        assertEquals(1, listener.getDeletedDirectories().size());
        assertTrue(listener.getDeletedDirectories().contains(testDirA));
    }

    /**
     * Test that when one listener throws an exception during file modification, other listeners still receive file modification notifications.
     */
    @Test
    void testListenerExceptionDuringFileChange() throws Exception {
        final File testFile = new File(testDir, "test-file.java");
        testFile.createNewFile();
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onFileChange
        final FailingOnFileChangeListener failingOnFileChangeListener = new FailingOnFileChangeListener();
        observer.addListener(failingOnFileChangeListener);
        // Add a working listener
        observer.addListener(listener);
        // First notification to register the file
        observer.initialize();
        observer.checkAndNotify();
        // Clear collections
        listener.clear();
        Thread.sleep(SLEEP_MILLIS);
        PathUtils.touch(testFile.toPath(), FileTimes.plusSeconds(FileTimes.now(), 120));
        observer.checkAndNotify();
        // Verify working listener received the file change event
        assertEquals(1, listener.getChangedFiles().size());
        assertTrue(listener.getChangedFiles().contains(testFile));
    }

    /**
     * Test that when one listener throws an exception during file creation, other listeners still receive file creation notifications.
     */
    @Test
    void testListenerExceptionDuringFileCreate() throws Exception {
        final File testFile = new File(testDir, "test-file.java");
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onFileCreate
        final FailingOnFileCreateListener failingOnFileCreateListener = new FailingOnFileCreateListener();
        observer.addListener(failingOnFileCreateListener);
        // Add a working listener
        observer.addListener(listener);
        observer.initialize();
        testFile.createNewFile();
        observer.checkAndNotify();
        // Verify working listener received the file create event
        assertEquals(1, listener.getCreatedFiles().size());
        assertTrue(listener.getCreatedFiles().contains(testFile));
    }

    /**
     * Test that when one listener throws an exception during file deletion, other listeners still receive file deletion notifications.
     */
    @Test
    void testListenerExceptionDuringFileDelete() throws Exception {
        final File testFile = new File(testDir, "test-file.java");
        testFile.createNewFile();
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).get();
        // Add a listener that throws an exception during onFileDelete
        final FailingOnFileDeleteListener failingOnFileDeleteListener = new FailingOnFileDeleteListener();
        observer.addListener(failingOnFileDeleteListener);
        // Add a working listener
        observer.addListener(listener);
        // First notification to register the file
        observer.initialize();
        observer.checkAndNotify();
        // Clear collections
        listener.clear();
        // Delete the file
        FileUtils.deleteQuietly(testFile);
        // Second notification
        observer.checkAndNotify();
        // Verify working listener received the file delete event
        assertEquals(1, listener.getDeletedFiles().size());
        assertTrue(listener.getDeletedFiles().contains(testFile));
    }

    /**
     * Test that when one listener throws an exception during onStart, other listeners still receive onStart notifications.
     */
    @Test
    void testListenerExceptionDuringOnStart() throws Exception {
        final IOFileFilter files = FileFilterUtils.fileFileFilter();
        final IOFileFilter javaSuffix = FileFilterUtils.suffixFileFilter(".java");
        final IOFileFilter fileFilter = FileFilterUtils.and(files, javaSuffix);
        final IOFileFilter directories = FileFilterUtils.directoryFileFilter();
        final IOFileFilter visible = HiddenFileFilter.VISIBLE;
        final IOFileFilter dirFilter = FileFilterUtils.and(directories, visible);
        final IOFileFilter filter = FileFilterUtils.or(dirFilter, fileFilter);
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).setFileFilter(filter).get();
        // Add a listener that throws an exception during onStart
        final FailingListener failingListener = new FailingListener();
        observer.addListener(failingListener);
        // Add a working listener
        observer.addListener(listener);
        observer.initialize();
        observer.checkAndNotify();
        // Verify working listener received onStart
        assertTrue(failingListener.getOnStartCount() > 0);
        assertEquals(1, listener.getStartedCount());
    }

    /**
     * Test that when one listener throws an exception during onStop, other listeners still receive onStop notifications.
     */
    @Test
    void testListenerExceptionDuringOnStop() throws Exception {
        final IOFileFilter files = FileFilterUtils.fileFileFilter();
        final IOFileFilter javaSuffix = FileFilterUtils.suffixFileFilter(".java");
        final IOFileFilter fileFilter = FileFilterUtils.and(files, javaSuffix);
        final IOFileFilter directories = FileFilterUtils.directoryFileFilter();
        final IOFileFilter visible = HiddenFileFilter.VISIBLE;
        final IOFileFilter dirFilter = FileFilterUtils.and(directories, visible);
        final IOFileFilter filter = FileFilterUtils.or(dirFilter, fileFilter);
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(testDir).setFileFilter(filter).get();
        // Add a listener that throws an exception during onStop
        final FailingOnStopListener failingOnStopListener = new FailingOnStopListener();
        observer.addListener(failingOnStopListener);
        // Add a working listener
        observer.addListener(listener);
        observer.initialize();
        observer.checkAndNotify();
        // Verify working listener received full cycle (onStart and onStop)
        assertEquals(1, listener.getStartedCount());
        assertEquals(1, listener.getStoppedCount());
    }
}
