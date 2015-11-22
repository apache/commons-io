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

import java.io.File;

/**
 * Test case for {@link SizeFileComparator}.
 */
public class SizeFileComparatorTest extends ComparatorAbstractTestCase {

    private File smallerDir;
    private File largerDir;
    private File smallerFile;
    private File largerFile;

    /**
     * Construct a new test case with the specified name.
     *
     * @param name Name of the test
     */
    public SizeFileComparatorTest(final String name) {
        super(name);
    }

    /** @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
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
        createFile(smallerFile, 32);
        createFile(equalFile, 48);
        createFile(largerFile, 64);
        equalFile1 = equalFile;
        equalFile2 = equalFile;
        lessFile   = smallerFile;
        moreFile   = largerFile;
    }

    /**
     * Test a file which doesn't exist.
     */
    public void testNonexistantFile() {
        final File nonexistantFile = new File(new File("."), "nonexistant.txt");
        assertFalse(nonexistantFile.exists());
        assertTrue("less",  comparator.compare(nonexistantFile, moreFile) < 0);
    }

    /**
     * Test a file which doesn't exist.
     */
    public void testCompareDirectorySizes() {
        assertEquals("sumDirectoryContents=false", 0, comparator.compare(smallerDir, largerDir));
        assertEquals("less", -1, SizeFileComparator.SIZE_SUMDIR_COMPARATOR.compare(smallerDir, largerDir));
        assertEquals("less", 1,  SizeFileComparator.SIZE_SUMDIR_REVERSE.compare(smallerDir, largerDir));
    }
}
