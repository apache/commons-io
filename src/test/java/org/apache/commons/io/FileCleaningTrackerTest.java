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
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileCleaningTracker}.
 */
public class FileCleaningTrackerTest extends AbstractTempDirTest {

    private File testFile;
    private Path testPath;

    private FileCleaningTracker fileCleaningTracker;

    RandomAccessFile createRandomAccessFile() throws FileNotFoundException {
        return RandomAccessFileMode.READ_WRITE.create(testFile);
    }

    protected FileCleaningTracker newInstance() {
        return new FileCleaningTracker();
    }

    private void pauseForDeleteToComplete(File file) {
        int count = 0;
        while (file.exists() && count++ < 40) {
            TestUtils.sleepQuietly(500L);
            file = new File(file.getPath());
        }
    }

    private void pauseForDeleteToComplete(Path file) {
        int count = 0;
        while (Files.exists(file) && count++ < 40) {
            TestUtils.sleepQuietly(500L);
            file = Paths.get(file.toAbsolutePath().toString());
        }
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
    public void tearDown() {

        // reset file cleaner class, so as not to break other tests

        /**
         * The following block of code can possibly be removed when the deprecated {@link FileCleaner} is gone. The
         * question is, whether we want to support reuse of {@link FileCleaningTracker} instances, which we should, IMO,
         * not.
         */
        {
            if (fileCleaningTracker != null) {
                fileCleaningTracker.q = new ReferenceQueue<>();
                fileCleaningTracker.trackers.clear();
                fileCleaningTracker.deleteFailures.clear();
                fileCleaningTracker.exitWhenFinished = false;
                fileCleaningTracker.reaper = null;
            }
        }

        fileCleaningTracker = null;
    }

    @Test
    public void testFileCleanerDirectory_ForceStrategy_FileSource() throws Exception {
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile.toPath()))) {
            TestUtils.generateTestData(output, 100);
        }
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj, FileDeleteStrategy.FORCE);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(testFile.getParentFile());

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(testFile.getPath()).exists(), showFailures());
        assertFalse(testFile.getParentFile().exists(), showFailures());
    }

    @Test
    public void testFileCleanerDirectory_ForceStrategy_PathSource() throws Exception {
        if (!Files.exists(testPath.getParent())) {
            throw new IOException("Cannot create file " + testPath
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testPath))) {
            TestUtils.generateTestData(output, 100);
        }
        assertTrue(Files.exists(testPath));
        assertTrue(Files.exists(tempDirPath));

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirPath, obj, FileDeleteStrategy.FORCE);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(testPath.getParent());

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(Files.exists(testPath), showFailures());
        assertFalse(Files.exists(testPath.getParent()), showFailures());
    }

    @Test
    public void testFileCleanerDirectory_NullStrategy() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj, null);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        waitUntilTrackCount();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectoryFileSource() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(tempDirFile.exists());

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirFile, obj);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        waitUntilTrackCount();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectoryPathSource() throws Exception {
        TestUtils.createFile(testPath, 100);
        assertTrue(Files.exists(testPath));
        assertTrue(Files.exists(tempDirPath));

        Object obj = new Object();
        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(tempDirPath, obj);
        assertEquals(1, fileCleaningTracker.getTrackCount());

        obj = null;

        waitUntilTrackCount();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertTrue(Files.exists(testPath));  // not deleted, as dir not empty
        assertTrue(Files.exists(testPath.getParent()));  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerExitWhenFinished_NoTrackAfter() {
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
    public void testFileCleanerExitWhenFinished1() throws Exception {
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

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals(0, fileCleaningTracker.getTrackCount(), "10-Track Count");
        assertFalse(new File(path).exists(), "11-testFile exists " + showFailures());
        assertTrue(fileCleaningTracker.exitWhenFinished, "12-exitWhenFinished");
        assertFalse(fileCleaningTracker.reaper.isAlive(), "13-reaper.isAlive");
    }

    @Test
    public void testFileCleanerExitWhenFinished2() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists());
        RandomAccessFile r = createRandomAccessFile();
        assertTrue(testFile.exists());

        assertEquals(0, fileCleaningTracker.getTrackCount());
        fileCleaningTracker.track(path, r);
        assertEquals(1, fileCleaningTracker.getTrackCount());
        assertFalse(fileCleaningTracker.exitWhenFinished);
        assertTrue(fileCleaningTracker.reaper.isAlive());

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(path).exists(), showFailures());
        assertFalse(fileCleaningTracker.exitWhenFinished);
        assertTrue(fileCleaningTracker.reaper.isAlive());

        assertFalse(fileCleaningTracker.exitWhenFinished);
        fileCleaningTracker.exitWhenFinished();
        for (int i = 0; i < 20 && fileCleaningTracker.reaper.isAlive(); i++) {
            TestUtils.sleep(500L);  // allow reaper thread to die
        }
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertFalse(fileCleaningTracker.reaper.isAlive());
    }

    @Test
    public void testFileCleanerExitWhenFinishedFirst() throws Exception {
        assertFalse(fileCleaningTracker.exitWhenFinished);
        fileCleaningTracker.exitWhenFinished();
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);

        waitUntilTrackCount();

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertTrue(fileCleaningTracker.exitWhenFinished);
        assertNull(fileCleaningTracker.reaper);
    }

    @Test
    public void testFileCleanerFile() throws Exception {
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

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals(0, fileCleaningTracker.getTrackCount());
        assertFalse(new File(path).exists(), showFailures());
    }
    @Test
    public void testFileCleanerNull() {
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((File) null, new Object()));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((File) null, new Object(), FileDeleteStrategy.NORMAL));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((String) null, new Object()));
        assertThrows(NullPointerException.class, () -> fileCleaningTracker.track((String) null, new Object(), FileDeleteStrategy.NORMAL));
    }

    private void waitUntilTrackCount() throws Exception {
        System.gc();
        TestUtils.sleep(500);
        int count = 0;
        while (fileCleaningTracker.getTrackCount() != 0 && count++ < 5) {
            List<String> list = new ArrayList<>();
            try {
                long i = 0;
                while (fileCleaningTracker.getTrackCount() != 0) {
                    list.add(
                        "A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String "
                            + i++);
                }
            } catch (final Throwable ignored) {
            }
            list = null;
            System.gc();
            TestUtils.sleep(1000);
        }
        if (fileCleaningTracker.getTrackCount() != 0) {
            throw new IllegalStateException("Your JVM is not releasing References, try running the test with less memory (-Xmx)");
        }

    }
}
