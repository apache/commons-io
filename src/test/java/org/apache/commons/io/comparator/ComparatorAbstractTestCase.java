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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base Test case for Comparator implementations.
 */
public abstract class ComparatorAbstractTestCase {

    @TempDir
    public File dir;

    /** comparator instance */
    protected AbstractFileComparator comparator;

    /** reverse comparator instance */
    protected Comparator<File> reverse;

    /** File which compares equal to  "equalFile2" */
    protected File equalFile1;

    /** File which compares equal to  "equalFile1" */
    protected File equalFile2;

    /** File which is less than the "moreFile" */
    protected File lessFile;

    /** File which is more than the "lessFile" */
    protected File moreFile;

    /**
     * Test the comparator.
     */
    @Test
    public void testComparator() {
        assertEquals(0, comparator.compare(equalFile1, equalFile2), "equal");
        assertTrue(comparator.compare(lessFile, moreFile) < 0, "less");
        assertTrue(comparator.compare(moreFile, lessFile) > 0, "more");
    }

    /**
     * Test the comparator reversed.
     */
    @Test
    public void testReverseComparator() {
        assertEquals(0, reverse.compare(equalFile1, equalFile2), "equal");
        assertTrue(reverse.compare(moreFile, lessFile) < 0, "less");
        assertTrue(reverse.compare(lessFile, moreFile) > 0, "more");
    }

    /**
     * Test comparator array sort is null safe.
     */
    @Test
    public void testSortArrayNull() {
        assertNull(comparator.sort((File[])null));
    }

    /**
     * Test the comparator array sort.
     */
    @Test
    public void testSortArray() {
        final File[] files = new File[3];
        files[0] = equalFile1;
        files[1] = moreFile;
        files[2] = lessFile;
        comparator.sort(files);
        assertSame(lessFile, files[0], "equal");
        assertSame(equalFile1, files[1], "less");
        assertSame(moreFile, files[2], "more");
    }

    /**
     * Test the comparator array sort.
     */
    @Test
    public void testSortList() {
        final List<File> files = new ArrayList<>();
        files.add(equalFile1);
        files.add(moreFile);
        files.add(lessFile);
        comparator.sort(files);
        assertSame(lessFile, files.get(0), "equal");
        assertSame(equalFile1, files.get(1), "less");
        assertSame(moreFile, files.get(2), "more");
    }

    /**
     * Test comparator list sort is null safe.
     */
    @Test
    public void testSortListNull() {
        assertNull(comparator.sort((List<File>)null));
    }

    /**
     * Test comparator toString.
     */
    @Test
    public void testToString() {
        assertNotNull(comparator.toString(), "comparator");
        assertTrue(reverse.toString().startsWith("ReverseComparator["), "reverse");
    }
}
