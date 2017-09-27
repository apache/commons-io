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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.testtools.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * This is used to test {@link FileCleaningTracker} for correctness.
 *
 * @see FileCleaningTracker
 */
public class FileCleaningTrackerTestCase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File getTestDirectory() {
        return temporaryFolder.getRoot();
    }

    protected FileCleaningTracker newInstance() {
        return new FileCleaningTracker();
    }

    private File testFile;
    private FileCleaningTracker theInstance;

    @Before
    public void setUp() throws Exception {
        testFile = new File(getTestDirectory(), "file-test.txt");
        theInstance = newInstance();
    }

    @After
    public void tearDown() throws Exception {

        // reset file cleaner class, so as not to break other tests

        /**
         * The following block of code can possibly be removed when the deprecated {@link FileCleaner} is gone. The
         * question is, whether we want to support reuse of {@link FileCleaningTracker} instances, which we should, IMO,
         * not.
         */
        {
            if (theInstance != null) {
                theInstance.q = new ReferenceQueue<>();
                theInstance.trackers.clear();
                theInstance.deleteFailures.clear();
                theInstance.exitWhenFinished = false;
                theInstance.reaper = null;
            }
        }

        theInstance = null;
    }

    //-----------------------------------------------------------------------
    @Test
    public void testFileCleanerFile() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists());
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertTrue(testFile.exists());

        assertEquals(0, theInstance.getTrackCount());
        theInstance.track(path, r);
        assertEquals(1, theInstance.getTrackCount());

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals(0, theInstance.getTrackCount());
        assertEquals(showFailures(), false, new File(path).exists());
    }

    @Test
    public void testFileCleanerDirectory() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(getTestDirectory().exists());

        Object obj = new Object();
        assertEquals(0, theInstance.getTrackCount());
        theInstance.track(getTestDirectory(), obj);
        assertEquals(1, theInstance.getTrackCount());

        obj = null;

        waitUntilTrackCount();

        assertEquals(0, theInstance.getTrackCount());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectory_NullStrategy() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertTrue(testFile.exists());
        assertTrue(getTestDirectory().exists());

        Object obj = new Object();
        assertEquals(0, theInstance.getTrackCount());
        theInstance.track(getTestDirectory(), obj, null);
        assertEquals(1, theInstance.getTrackCount());

        obj = null;

        waitUntilTrackCount();

        assertEquals(0, theInstance.getTrackCount());
        assertTrue(testFile.exists());  // not deleted, as dir not empty
        assertTrue(testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectory_ForceStrategy() throws Exception {
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile))) {
            TestUtils.generateTestData(output, 100);
        }
        assertTrue(testFile.exists());
        assertTrue(getTestDirectory().exists());

        Object obj = new Object();
        assertEquals(0, theInstance.getTrackCount());
        theInstance.track(getTestDirectory(), obj, FileDeleteStrategy.FORCE);
        assertEquals(1, theInstance.getTrackCount());

        obj = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(testFile.getParentFile());

        assertEquals(0, theInstance.getTrackCount());
        assertEquals(showFailures(), false, new File(testFile.getPath()).exists());
        assertEquals(showFailures(), false, testFile.getParentFile().exists());
    }

    @Test
    public void testFileCleanerNull() throws Exception {
        try {
            theInstance.track((File) null, new Object());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((File) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((String) null, new Object());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((String) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testFileCleanerExitWhenFinishedFirst() throws Exception {
        assertFalse(theInstance.exitWhenFinished);
        theInstance.exitWhenFinished();
        assertTrue(theInstance.exitWhenFinished);
        assertEquals(null, theInstance.reaper);

        waitUntilTrackCount();

        assertEquals(0, theInstance.getTrackCount());
        assertTrue(theInstance.exitWhenFinished);
        assertEquals(null, theInstance.reaper);
    }

    @Test
    public void testFileCleanerExitWhenFinished_NoTrackAfter() throws Exception {
        assertFalse(theInstance.exitWhenFinished);
        theInstance.exitWhenFinished();
        assertTrue(theInstance.exitWhenFinished);
        assertEquals(null, theInstance.reaper);

        final String path = testFile.getPath();
        final Object marker = new Object();
        try {
            theInstance.track(path, marker);
            fail();
        } catch (final IllegalStateException ex) {
            // expected
        }
        assertTrue(theInstance.exitWhenFinished);
        assertEquals(null, theInstance.reaper);
    }

    @Test
    public void testFileCleanerExitWhenFinished1() throws Exception {
        final String path = testFile.getPath();

        assertEquals("1-testFile exists: " + testFile, false, testFile.exists());
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertEquals("2-testFile exists", true, testFile.exists());

        assertEquals("3-Track Count", 0, theInstance.getTrackCount());
        theInstance.track(path, r);
        assertEquals("4-Track Count", 1, theInstance.getTrackCount());
        assertEquals("5-exitWhenFinished", false, theInstance.exitWhenFinished);
        assertEquals("6-reaper.isAlive", true, theInstance.reaper.isAlive());

        assertEquals("7-exitWhenFinished", false, theInstance.exitWhenFinished);
        theInstance.exitWhenFinished();
        assertEquals("8-exitWhenFinished", true, theInstance.exitWhenFinished);
        assertEquals("9-reaper.isAlive", true, theInstance.reaper.isAlive());

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals("10-Track Count", 0, theInstance.getTrackCount());
        assertEquals("11-testFile exists " + showFailures(), false, new File(path).exists());
        assertEquals("12-exitWhenFinished", true, theInstance.exitWhenFinished);
        assertEquals("13-reaper.isAlive", false, theInstance.reaper.isAlive());
    }

    @Test
    public void testFileCleanerExitWhenFinished2() throws Exception {
        final String path = testFile.getPath();

        assertFalse(testFile.exists());
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertTrue(testFile.exists());

        assertEquals(0, theInstance.getTrackCount());
        theInstance.track(path, r);
        assertEquals(1, theInstance.getTrackCount());
        assertFalse(theInstance.exitWhenFinished);
        assertTrue(theInstance.reaper.isAlive());

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertEquals(0, theInstance.getTrackCount());
        assertEquals(showFailures(), false, new File(path).exists());
        assertFalse(theInstance.exitWhenFinished);
        assertTrue(theInstance.reaper.isAlive());

        assertFalse(theInstance.exitWhenFinished);
        theInstance.exitWhenFinished();
        for (int i = 0; i < 20 && theInstance.reaper.isAlive(); i++) {
            TestUtils.sleep(500L);  // allow reaper thread to die
        }
        assertTrue(theInstance.exitWhenFinished);
        assertFalse(theInstance.reaper.isAlive());
    }

    //-----------------------------------------------------------------------
    private void pauseForDeleteToComplete(File file) {
        int count = 0;
        while(file.exists() && count++ < 40) {
            try {
                TestUtils.sleep(500L);
            } catch (final InterruptedException ignore) {
            }
            file = new File(file.getPath());
        }
    }
    private String showFailures() throws Exception {
        if (theInstance.deleteFailures.size() == 1) {
            return "[Delete Failed: " + theInstance.deleteFailures.get(0) + "]";
        } else {
            return "[Delete Failures: " + theInstance.deleteFailures.size() + "]";
        }
    }

    private void waitUntilTrackCount() throws Exception {
        System.gc();
        TestUtils.sleep(500);
        int count = 0;
        while(theInstance.getTrackCount() != 0 && count++ < 5) {
            List<String> list = new ArrayList<>();
            try {
                long i = 0;
                while (theInstance.getTrackCount() != 0) {
                    list.add("A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String " + (i++));
                }
            } catch (final Throwable ignored) {
            }
            list = null;
            System.gc();
            TestUtils.sleep(1000);
        }
        if (theInstance.getTrackCount() != 0) {
            throw new IllegalStateException("Your JVM is not releasing References, try running the testcase with less memory (-Xmx)");
        }

    }
}
