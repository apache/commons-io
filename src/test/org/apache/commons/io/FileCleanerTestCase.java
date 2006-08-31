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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test FileCleaner for correctness.
 *
 * @author Noel Bergman
 * @author Martin Cooper
 *
 * @version $Id$

 * @see FileCleaner
 */
public class FileCleanerTestCase extends FileBasedTestCase {

    private File testFile;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FileCleanerTestCase.class);
    }

    public FileCleanerTestCase(String name) throws IOException {
        super(name);

        testFile = new File(getTestDirectory(), "file-test.txt");
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    //-----------------------------------------------------------------------
    public void testFileCleanerFile() throws Exception {
        String path = testFile.getPath();
        
        assertEquals(false, testFile.exists());
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertEquals(true, testFile.exists());
        
        assertEquals(0, FileCleaner.getTrackCount());
        FileCleaner.track(path, r);
        assertEquals(1, FileCleaner.getTrackCount());
        
        r.close();
        testFile = null;
        r = null;
        while (FileCleaner.getTrackCount() != 0) {
            System.gc();
        }
        
        assertEquals(0, FileCleaner.getTrackCount());
        assertEquals(false, new File(path).exists());
    }

    public void testFileCleanerDirectory() throws Exception {
        createFile(testFile, 100);
        assertEquals(true, testFile.exists());
        assertEquals(true, getTestDirectory().exists());
        
        Object obj = new Object();
        assertEquals(0, FileCleaner.getTrackCount());
        FileCleaner.track(getTestDirectory(), obj);
        assertEquals(1, FileCleaner.getTrackCount());
        
        obj = null;
        while (FileCleaner.getTrackCount() != 0) {
            System.gc();
        }
        
        assertEquals(0, FileCleaner.getTrackCount());
        assertEquals(true, testFile.exists());  // not deleted, as dir not empty
        assertEquals(true, testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    public void testFileCleanerDirectory_NullStrategy() throws Exception {
        createFile(testFile, 100);
        assertEquals(true, testFile.exists());
        assertEquals(true, getTestDirectory().exists());
        
        Object obj = new Object();
        assertEquals(0, FileCleaner.getTrackCount());
        FileCleaner.track(getTestDirectory(), obj, (FileDeleteStrategy) null);
        assertEquals(1, FileCleaner.getTrackCount());
        
        obj = null;
        while (FileCleaner.getTrackCount() != 0) {
            System.gc();
        }
        
        assertEquals(0, FileCleaner.getTrackCount());
        assertEquals(true, testFile.exists());  // not deleted, as dir not empty
        assertEquals(true, testFile.getParentFile().exists());  // not deleted, as dir not empty
    }

    public void testFileCleanerDirectory_ForceStrategy() throws Exception {
        createFile(testFile, 100);
        assertEquals(true, testFile.exists());
        assertEquals(true, getTestDirectory().exists());
        
        Object obj = new Object();
        assertEquals(0, FileCleaner.getTrackCount());
        FileCleaner.track(getTestDirectory(), obj, FileDeleteStrategy.FORCE);
        assertEquals(1, FileCleaner.getTrackCount());
        
        obj = null;
        while (FileCleaner.getTrackCount() != 0) {
            System.gc();
        }
        
        assertEquals(0, FileCleaner.getTrackCount());
        assertEquals(false, testFile.exists());
        assertEquals(false, testFile.getParentFile().exists());
    }

    public void testFileCleanerNull() throws Exception {
        try {
            FileCleaner.track((File) null, new Object());
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            FileCleaner.track((File) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            FileCleaner.track((String) null, new Object());
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
        try {
            FileCleaner.track((String) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
    }

}
