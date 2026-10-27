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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileCleaningTracker}.
 */
class FileCleaningTrackerTest extends AbstractTempDirTest {

    private static final long TIMEOUT_MILLIS = 10_000;

    private final CountDownLatch deletionComplete = new CountDownLatch(1);

    private File testFile;
    private Path testPath;

    private FileCleaningTracker fileCleaningTracker;

    private void awaitDeletion() throws InterruptedException {
        assertTrue(deletionComplete.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "Timed out waiting for file deletion");
    }

    private void awaitReaperTermination() throws InterruptedException {
        if (fileCleaningTracker.reaper != null) {
            fileCleaningTracker.reaper.join(TIMEOUT_MILLIS);
            assertFalse(fileCleaningTracker.reaper.isAlive(), "Timed out waiting for the reaper to terminate");
        }
    }

    RandomAccessFile createRandomAccessFile() throws FileNotFoundException {
        return RandomAccessFileMode.READ_WRITE.create(testFile);
    }

    private FileDeleteStrategy deletionStrategy(final FileDeleteStrategy delegate) {
        return new FileDeleteStrategy("Notify " + delegate) {
            @Override
            protected boolean doDelete(final File file) throws IOException {
                try {
                    return delegate.doDelete(file);
                } finally {
                    deletionComplete.countDown();
                }
            }
        };
    }

    private void enqueueTrackers() {
        // Exercise the reaper without depending on garbage collection.
        final Reference<?>[] references;
        synchronized (fileCleaningTracker.trackers) {
            references = fileCleaningTracker.trackers.toArray(new Reference<?>[0]);
        }
        for (final Reference<?> reference : references) {
            reference.clear();
            reference.enqueue();
        }
    }

    private void finishTracking() throws InterruptedException {
        // Joining also waits for deletion and failure recording when testing the default and null strategies.
        enqueueTrackers();
        fileCleaningTracker.exitWhenFinished();
        awaitReaperTermination();
    }

    protected FileCleaningTracker newInstance() {
        return new FileCleaningTracker();
    }

    @BeforeEach
    public void setUp() {
        testFile = new File(tempDirFile, "file-test.txt");
        testPath = testFile.toPath();
        fileCleaningTracker = newInstance();
    }

    private String showFailures() {
        if (fileCleaningTracker.deleteFailures.size() == 1) {
            return "[Delete Failed: " + fileCleaningTracker.deleteFailures.get(0) + "]";
        }
        return "[Delete Failures: " + fileCleaningTracker.deleteFailures.size() + "]";
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        // reset file cleaner class, so as not to break other tests
        /**
         * The following block of code can possibly be removed when the deprecated {@link FileCleaner} is gone. The question is, whether we want to support
         * reuse of {@link FileCleaningTracker} instances, which we should, IMO, not.
         */
        {
            if (fileCleaningTracker != null) {
                finishTracking();
                fileCleaningTracker.refQueue = new ReferenceQueue<>();
                fileCleaningTracker.trackers.clear();
                fileCleaningTracker.deleteFailures.clear();
                fileCleaningTracker.exitWhenFinished = false;
                fileCleaningTracker.reaper = null;
            }
        }
        fileCleaningTracker = null;
    }

    @Test
    void testExitWhenFinishedClearsInterruptBeforeWaitingForPendingTrackers() throws Exception {
        final CountDownLatch firstRemove = new CountDownLatch(1);
        final CountDownLatch secondRemove = new CountDownLatch(1);
        final AtomicInteger calls = new AtomicInteger();
        final AtomicBoolean interruptedOnRetry = new AtomicBoolean();
        fileCleaningTracker.refQueue = new ReferenceQueue<Object>() {
            @Override
            public Reference<?> remove() throws InterruptedException {
                final int call = calls.incrementAndGet();
                if (call == 1) {
                    firstRemove.countDown();
                } else if (call == 2) {
                    interruptedOnRetry.set(Thread.currentThread().isInterrupted());
                    secondRemove.countDown();
                }
                return super.remove();
            }
        };
        TestUtils.createFile(testFile, 16);
        // Keep the marker reachable until the assertions finish; enqueue it explicitly for cleanup.
        fileCleaningTracker.track(testFile, this);
        assertTrue(firstRemove.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "Reaper did not wait for a tracker");
        fileCleaningTracker.exitWhenFinished();
        assertTrue(secondRemove.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "Reaper did not retry after interruption");
        assertFalse(interruptedOnRetry.get(), "Reaper must clear the interrupt before waiting again");
        assertEquals(1, fileCleaningTracker.getTrackCount());
        assertTrue(testFile.exists());
        finishTracking();
        assertFalse(testFile.exists(), showFailures());
        assertTrue(fileCleaningTracker.getDeleteFailures().isEmpty(), showFailures());
    }

    @Test
    void testExitWhenFinishedTestMatchesField() {
        // Before calling exitWhenFinished()
        assertEquals(fileCleaningTracker.exitWhenFinished, fileCleaningTracker.exitWhenFinishedTest(),
                "exitWhenFinishedTest() must reflect the exitWhenFinished field (before)");
        fileCleaningTracker.exitWhenFinished();
        // After calling exitWhenFinished()
        assertEquals(fileCleaningTracker.exitWhenFinished, fileCleaningTracker.exitWhenFinishedTest(),
                "exitWhenFinishedTest() must reflect the exitWhenFinished field (after)");
    }

    @Test
    void testExitWhenFinishedTestReturnsFalseInitially() {
        assertFalse(fileCleaningTracker.exitWhenFinishedTest(), "exitWhenFinishedTest() should return false before exitWhenFinished() is called");
    }

    @Test
    void testExitWhenFinishedTestReturnsTrueAfterExitWhenFinished() {
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinishedTest(), "exitWhenFinishedTest() should return true after exitWhenFinished() is called");
    }

    @Test
    void testExitWhenFinishedTestWithActiveReaper() throws Exception {
        final String path = testFile.getPath();
        // Start the reaper by tracking a file
        final RandomAccessFile raf = createRandomAccessFile();
        fileCleaningTracker.track(path, raf);
        assertFalse(fileCleaningTracker.exitWhenFinishedTest(), "exitWhenFinishedTest() should return false while reaper is still active");
        assertTrue(fileCleaningTracker.reaper.isAlive(), "Reaper should be alive");
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinishedTest(),
                "exitWhenFinishedTest() should return true after exitWhenFinished() is called with active reaper");
        raf.close();
    }

    @Test
    void testFileCleanerDirectory_ForceStrategy_DoesNotFollowSymbolicLink() throws Exception {
        final Path victimDirectory = Files.createDirectory(tempDirPath.resolve("victim"));
        final Path victimFile = victimDirectory.resolve("important.txt");
        TestUtils.createFile(victimFile, 16);
        final Path trackedDirectory = Files.createDirectory(tempDirPath.resolve("tracked"));
        fileCleaningTracker.track(trackedDirectory, this, FileDeleteStrategy.FORCE);
        Files.delete(trackedDirectory);
        Files.createSymbolicLink(trackedDirectory, victimDirectory);

        finishTracking();

        assertFalse(Files.exists(trackedDirectory, LinkOption.NOFOLLOW_LINKS), showFailures());
        assertTrue(Files.isDirectory(victimDirectory));
        assertEquals(16, Files.size(victimFile));
        assertTrue(fileCleaningTracker.getDeleteFailures().isEmpty(), showFailures());
    }

    @Test
    void testFileCleanerDirectory_ForceStrategy_FileSource() throws Exception {
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 100);
        }
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj, deletionStrategy(FileDeleteStrategy.FORCE));
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        enqueueTrackers();
        awaitDeletion();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(testFile.getPath()).exists(), showFailures());
        assertFalse(testFile.getParentFile().exists(), showFailures());
    }

    @Test
    void testFileCleanerDirectory_ForceStrategy_PathSource() throws Exception {
        if (!Files.exists(testPath.getParent())) {
            throw new IOException("Cannot create file " + testPath + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testPath))) {
            TestUtils.generateTestData(output, 100);
        }
        assertTrue(Files.exists(testPath));
        assertTrue(Files.exists(tempDirPath));

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirPath, obj, deletionStrategy(FileDeleteStrategy.FORCE));
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        enqueueTrackers();
        awaitDeletion();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(Files.exists(testPath), showFailures());
        assertFalse(Files.exists(testPath.getParent()), showFailures());
    }

    @Test
    void testFileCleanerDirectory_NullStrategy() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj, null);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        finishTracking();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertEquals(Collections.singletonList(tempDirFile.getPath()), fileCleaningTracker.getDeleteFailures());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    void testFileCleanerDirectoryFileSource() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        finishTracking();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertEquals(Collections.singletonList(tempDirFile.getPath()), fileCleaningTracker.getDeleteFailures());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    void testFileCleanerDirectoryPathSource() throws Exception {
        TestUtils.createFile(testPath, 100);
        assertTrue(Files.exists(testPath));
        assertTrue(Files.exists(tempDirPath));

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirPath, obj);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        finishTracking();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertEquals(Collections.singletonList(tempDirFile.getPath()), fileCleaningTracker.getDeleteFailures());
        assertTrue(Files.exists(testPath));  // not deleted, as dir not empty
        assertTrue(Files.exists(testPath.getParent()));  // not deleted, as dir not empty
    }

    @Test
    void testFileCleanerExitWhenFinished_NoTrackAfter() {
        assertFalse(fileCleaningTracker.exitWhenFinished);
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);

        final String path = testFile.getPath();
        final Object marker = new Object();

        assertThrows(IllegalStateException.class, () -> fileCleaningTracker.track(path, marker));
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);
    }

    @Test
    void testFileCleanerExitWhenFinished1() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists(), "1-testFile exists: " + testFile);

        // Do NOT used a try-with-resources statement here or the test will fail.
        RandomAccessFile raf = createRandomAccessFile();
        assertTrue(testFile.exists(), "2-testFile exists");

        assertEquals(0, fileCleaningTracker.getTrackCount(), "3-Track Count");
        fileCleaningTracker.track(path, raf);
        assertEquals(1, fileCleaningTracker.getTrackCount(), "4-Track Count");
        assertFalse(fileCleaningTracker.exitWhenFinished, "5-exitWhenFinished");
        assertTrue(fileCleaningTracker.reaper.isAlive(), "6-reaper.isAlive");

        assertFalse(fileCleaningTracker.exitWhenFinished, "7-exitWhenFinished");
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinished, "8-exitWhenFinished");
        assertTrue(fileCleaningTracker.reaper.isAlive(), "9-reaper.isAlive");

        raf.close();
        testFile = null;
        raf = null;

        enqueueTrackers();
        awaitReaperTermination();

        assertEquals(0, fileCleaningTracker.getTrackCount(), "10-Track Count");
        assertFalse(new File(path).exists(), "11-testFile exists " + showFailures());
        assertTrue(fileCleaningTracker.exitWhenFinished, "12-exitWhenFinished");
        assertFalse(fileCleaningTracker.reaper.isAlive(), "13-reaper.isAlive");
    }

    @Test
    void testFileCleanerExitWhenFinished2() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists());
        RandomAccessFile raf = createRandomAccessFile();
        assertTrue(testFile.exists());

        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(path, raf, deletionStrategy(FileDeleteStrategy.NORMAL));
        assertEquals(1, fileCleaningTracker.getTrackCount());
        assertFalse(fileCleaningTracker.exitWhenFinished);
        assertTrue(fileCleaningTracker.reaper.isAlive());

        raf.close();
        testFile = null;
        raf = null;

        enqueueTrackers();
        awaitDeletion();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(path).exists(), showFailures());
        assertFalse(fileCleaningTracker.exitWhenFinished);
        assertTrue(fileCleaningTracker.reaper.isAlive());

        assertFalse(fileCleaningTracker.exitWhenFinished);
        fileCleaningTracker.exitWhenFinished();
        awaitReaperTermination();
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertFalse(fileCleaningTracker.reaper.isAlive());
        assertFalse(Files.exists(Paths.get(path)));
    }

    @Test
    void testFileCleanerExitWhenFinishedFirst() throws Exception {
        assertFalse(fileCleaningTracker.exitWhenFinished);
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);
    }

    @Test
    void testFileCleanerFile() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists());
        RandomAccessFile raf = createRandomAccessFile();
        assertTrue(testFile.exists());

        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(path, raf);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        raf.close();
        testFile = null;
        raf = null;

        finishTracking();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(path).exists(), showFailures());
    }

    @Test
    void testFileCleanerGarbageCollection() throws Exception {
        TestUtils.createFile(testFile, 100);
        trackGarbageCollectableMarker();

        System.gc();
        awaitDeletion();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(testFile.exists(), showFailures());
    }

    @Test
    void testFileCleanerNull() {
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((File) null, new Object()));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((File) null, new Object(), FileDeleteStrategy.NORMAL));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((String) null, new Object()));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((String) null, new Object(), FileDeleteStrategy.NORMAL));
    }

    private void trackGarbageCollectableMarker() {
        // A separate stack frame ensures the marker is unreachable when GC is requested.
        fileCleaningTracker.track(testFile, new Object(), deletionStrategy(FileDeleteStrategy.NORMAL));
    }
}
