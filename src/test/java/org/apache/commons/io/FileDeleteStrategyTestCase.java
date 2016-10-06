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

import org.apache.commons.io.testtools.FileBasedTestCase;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for FileDeleteStrategy.
 *
 * @version $Id$
 * @see FileDeleteStrategy
 */
public class FileDeleteStrategyTestCase extends FileBasedTestCase {

    //-----------------------------------------------------------------------
    @Test
    public void testDeleteNormal() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        if (!subFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + subFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(subFile));
        try {
            TestUtils.generateTestData(output, (long) 16);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertTrue(subDir.exists());
        assertTrue(subFile.exists());
        // delete dir
        try {
            FileDeleteStrategy.NORMAL.delete(subDir);
            fail();
        } catch (final IOException ex) {
            // expected
        }
        assertTrue(subDir.exists());
        assertTrue(subFile.exists());
        // delete file
        FileDeleteStrategy.NORMAL.delete(subFile);
        assertTrue(subDir.exists());
        assertFalse(subFile.exists());
        // delete dir
        FileDeleteStrategy.NORMAL.delete(subDir);
        assertFalse(subDir.exists());
        // delete dir
        FileDeleteStrategy.NORMAL.delete(subDir);  // no error
        assertFalse(subDir.exists());
    }

    @Test
    public void testDeleteQuietlyNormal() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        if (!subFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + subFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(subFile));
        try {
            TestUtils.generateTestData(output, (long) 16);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertTrue(subDir.exists());
        assertTrue(subFile.exists());
        // delete dir
        assertFalse(FileDeleteStrategy.NORMAL.deleteQuietly(subDir));
        assertTrue(subDir.exists());
        assertTrue(subFile.exists());
        // delete file
        assertTrue(FileDeleteStrategy.NORMAL.deleteQuietly(subFile));
        assertTrue(subDir.exists());
        assertFalse(subFile.exists());
        // delete dir
        assertTrue(FileDeleteStrategy.NORMAL.deleteQuietly(subDir));
        assertFalse(subDir.exists());
        // delete dir
        assertTrue(FileDeleteStrategy.NORMAL.deleteQuietly(subDir));  // no error
        assertFalse(subDir.exists());
    }

    @Test
    public void testDeleteForce() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        if (!subFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + subFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(subFile));
        try {
            TestUtils.generateTestData(output, (long) 16);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertTrue(subDir.exists());
        assertTrue(subFile.exists());
        // delete dir
        FileDeleteStrategy.FORCE.delete(subDir);
        assertFalse(subDir.exists());
        assertFalse(subFile.exists());
        // delete dir
        FileDeleteStrategy.FORCE.delete(subDir);  // no error
        assertFalse(subDir.exists());
    }

    @Test
    public void testDeleteNull() throws Exception {
        try {
            FileDeleteStrategy.NORMAL.delete(null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        assertTrue(FileDeleteStrategy.NORMAL.deleteQuietly(null));
    }

    @Test
    public void testToString() {
        assertEquals("FileDeleteStrategy[Normal]", FileDeleteStrategy.NORMAL.toString());
        assertEquals("FileDeleteStrategy[Force]", FileDeleteStrategy.FORCE.toString());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

}
