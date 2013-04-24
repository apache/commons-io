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

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Test for FileDeleteStrategy.
 *
 * @version $Id$
 * @see FileDeleteStrategy
 */
public class FileDeleteStrategyTestCase extends FileBasedTestCase {

    public FileDeleteStrategyTestCase(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    public void testDeleteNormal() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);

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

    public void testDeleteQuietlyNormal() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);

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

    public void testDeleteForce() throws Exception {
        final File baseDir = getTestDirectory();
        final File subDir = new File(baseDir, "test");
        assertTrue(subDir.mkdir());
        final File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);

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

    public void testDeleteNull() throws Exception {
        try {
            FileDeleteStrategy.NORMAL.delete((File) null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        assertTrue(FileDeleteStrategy.NORMAL.deleteQuietly((File) null));
    }

    public void testToString() {
        assertEquals("FileDeleteStrategy[Normal]", FileDeleteStrategy.NORMAL.toString());
        assertEquals("FileDeleteStrategy[Force]", FileDeleteStrategy.FORCE.toString());
    }

}
