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
package org.apache.commons.io.comparator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.testtools.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link SizeFileComparator}.
 */
public class SizeFileComparatorTest extends ComparatorAbstractTestCase {

    private File smallerDir;
    private File largerDir;
    private File smallerFile;
    private File largerFile;

    /** @see junit.framework.TestCase#setUp() */
    @Before
    public void setUp() throws Exception {
        comparator = (AbstractFileComparator) SizeFileComparator.SIZE_COMPARATOR;
        reverse = SizeFileComparator.SIZE_REVERSE;
        final File dir = getTestDirectory();
        smallerDir = new File(dir, "smallerdir");
        largerDir = new File(dir, "largerdir");
        smallerFile = new File(smallerDir, "smaller.txt");
        final File equalFile = new File(dir, "equal.txt");
        largerFile = new File(largerDir, "larger.txt");
        smallerDir.mkdir();
        largerDir.mkdir();
        if (!smallerFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + smallerFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output2 =
                new BufferedOutputStream(new FileOutputStream(smallerFile));
        try {
            TestUtils.generateTestData(output2, (long) 32);
        } finally {
            IOUtils.closeQuietly(output2);
        }
        if (!equalFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + equalFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(equalFile));
        try {
            TestUtils.generateTestData(output1, (long) 48);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!largerFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + largerFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(largerFile));
        try {
            TestUtils.generateTestData(output, (long) 64);
        } finally {
            IOUtils.closeQuietly(output);
        }
        equalFile1 = equalFile;
        equalFile2 = equalFile;
        lessFile   = smallerFile;
        moreFile   = largerFile;
    }

    /**
     * Test a file which doesn't exist.
     */
    @Test
    public void testNonexistantFile() {
        final File nonexistantFile = new File(new File("."), "nonexistant.txt");
        assertFalse(nonexistantFile.exists());
        assertTrue("less",  comparator.compare(nonexistantFile, moreFile) < 0);
    }

    /**
     * Test a file which doesn't exist.
     */
    @Test
    public void testCompareDirectorySizes() {
        assertEquals("sumDirectoryContents=false", 0, comparator.compare(smallerDir, largerDir));
        assertEquals("less", -1, SizeFileComparator.SIZE_SUMDIR_COMPARATOR.compare(smallerDir, largerDir));
        assertEquals("less", 1,  SizeFileComparator.SIZE_SUMDIR_REVERSE.compare(smallerDir, largerDir));
    }
}
