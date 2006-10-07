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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Test for FileDeleteStrategy.
 *
 * @version $Id: DirectoryWalkerTestCase.java 438218 2006-08-29 21:15:11 +0000 (Tue, 29 Aug 2006) scolebourne $
 * @see FileDeleteStrategy
 */
public class FileDeleteStrategyTestCase extends FileBasedTestCase {

    public static Test suite() {
        return new TestSuite(FileDeleteStrategyTestCase.class);
    }

    public FileDeleteStrategyTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    public void testDeleteNormal() throws Exception {
        File baseDir = getTestDirectory();
        File subDir = new File(baseDir, "test");
        assertEquals(true, subDir.mkdir());
        File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);
        
        assertEquals(true, subDir.exists());
        assertEquals(true, subFile.exists());
        // delete dir
        try {
            FileDeleteStrategy.NORMAL.delete(subDir);
            fail();
        } catch (IOException ex) {
            // expected
        }
        assertEquals(true, subDir.exists());
        assertEquals(true, subFile.exists());
        // delete file
        FileDeleteStrategy.NORMAL.delete(subFile);
        assertEquals(true, subDir.exists());
        assertEquals(false, subFile.exists());
        // delete dir
        FileDeleteStrategy.NORMAL.delete(subDir);
        assertEquals(false, subDir.exists());
        // delete dir
        FileDeleteStrategy.NORMAL.delete(subDir);  // no error
        assertEquals(false, subDir.exists());
    }

    public void testDeleteQuietlyNormal() throws Exception {
        File baseDir = getTestDirectory();
        File subDir = new File(baseDir, "test");
        assertEquals(true, subDir.mkdir());
        File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);
        
        assertEquals(true, subDir.exists());
        assertEquals(true, subFile.exists());
        // delete dir
        assertEquals(false, FileDeleteStrategy.NORMAL.deleteQuietly(subDir));
        assertEquals(true, subDir.exists());
        assertEquals(true, subFile.exists());
        // delete file
        assertEquals(true, FileDeleteStrategy.NORMAL.deleteQuietly(subFile));
        assertEquals(true, subDir.exists());
        assertEquals(false, subFile.exists());
        // delete dir
        assertEquals(true, FileDeleteStrategy.NORMAL.deleteQuietly(subDir));
        assertEquals(false, subDir.exists());
        // delete dir
        assertEquals(true, FileDeleteStrategy.NORMAL.deleteQuietly(subDir));  // no error
        assertEquals(false, subDir.exists());
    }

    public void testDeleteForce() throws Exception {
        File baseDir = getTestDirectory();
        File subDir = new File(baseDir, "test");
        assertEquals(true, subDir.mkdir());
        File subFile = new File(subDir, "a.txt");
        createFile(subFile, 16);
        
        assertEquals(true, subDir.exists());
        assertEquals(true, subFile.exists());
        // delete dir
        FileDeleteStrategy.FORCE.delete(subDir);
        assertEquals(false, subDir.exists());
        assertEquals(false, subFile.exists());
        // delete dir
        FileDeleteStrategy.FORCE.delete(subDir);  // no error
        assertEquals(false, subDir.exists());
    }

    public void testDeleteNull() throws Exception {
        try {
            FileDeleteStrategy.NORMAL.delete((File) null);
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
        assertEquals(true, FileDeleteStrategy.NORMAL.deleteQuietly((File) null));
    }

    public void testToString() {
        assertEquals("FileDeleteStrategy[Normal]", FileDeleteStrategy.NORMAL.toString());
        assertEquals("FileDeleteStrategy[Force]", FileDeleteStrategy.FORCE.toString());
    }

}
