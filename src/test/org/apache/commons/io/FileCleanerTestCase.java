/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * @version $Id: FileCleanerTestCase.java,v 1.1 2004/03/18 06:04:14 martinc Exp $

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

    /**
     *  Test the FileCleaner implementation.
     */
    public void testFileCleaner() throws Exception {
        String path = testFile.getPath();

        assertFalse("File does not exist", testFile.exists());
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertTrue("File exists", testFile.exists());

        assertTrue("No files tracked", FileCleaner.getTrackCount() == 0);
        FileCleaner.track(path, r);
        assertTrue("One file tracked", FileCleaner.getTrackCount() == 1);

        r.close();
        testFile = null;
        r = null;

        while (FileCleaner.getTrackCount() != 0) {
            System.gc();
        }

        assertTrue("No files tracked", FileCleaner.getTrackCount() == 0);
        assertFalse("File does not exist", new File(path).exists());
    }
}
