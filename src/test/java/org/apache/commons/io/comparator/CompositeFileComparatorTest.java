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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.testtools.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CompositeFileComparator}.
 */
public class CompositeFileComparatorTest extends ComparatorAbstractTestCase {

    @BeforeEach
    public void setUp() throws Exception {
        comparator = new CompositeFileComparator(
                new AbstractFileComparator[] {
                    (AbstractFileComparator) SizeFileComparator.SIZE_COMPARATOR,
                    (AbstractFileComparator) ExtensionFileComparator.EXTENSION_COMPARATOR});
        reverse = new ReverseComparator(comparator);
        lessFile   = new File(dir, "xyz.txt");
        equalFile1 = new File(dir, "foo.txt");
        equalFile2 = new File(dir, "bar.txt");
        moreFile   = new File(dir, "foo.xyz");
        if (!lessFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + lessFile
                    + " as the parent directory does not exist");
        }

        try (final BufferedOutputStream output3 =
                new BufferedOutputStream(new FileOutputStream(lessFile))) {
            TestUtils.generateTestData(output3, 32);
        }
        if (!equalFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + equalFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 =
                new BufferedOutputStream(new FileOutputStream(equalFile1))) {
            TestUtils.generateTestData(output2, 48);
        }
        if (!equalFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + equalFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(equalFile2))) {
            TestUtils.generateTestData(output1, 48);
        }
        if (!moreFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + moreFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(moreFile))) {
            TestUtils.generateTestData(output, 48);
        }
    }

    /**
     * Test Constructor with null Iterable
     */
    @Test
    public void constructorIterable_order() {
        final List<Comparator<File>> list = new ArrayList<>();
        list.add(SizeFileComparator.SIZE_COMPARATOR);
        list.add(ExtensionFileComparator.EXTENSION_COMPARATOR);
        final Comparator<File> c = new CompositeFileComparator(list);

        assertEquals(0, c.compare(equalFile1, equalFile2), "equal");
        assertTrue(c.compare(lessFile, moreFile) < 0, "less");
        assertTrue(c.compare(moreFile, lessFile) > 0, "more");
    }

    /**
     * Test Constructor with null Iterable
     */
    @Test
    public void constructorIterable_Null() {
        final Comparator<File> c = new CompositeFileComparator((Iterable<Comparator<File>>)null);
        assertEquals(0, c.compare(lessFile, moreFile), "less,more");
        assertEquals(0, c.compare(moreFile, lessFile), "more,less");
        assertEquals("CompositeFileComparator{}", c.toString(), "toString");
    }

    /**
     * Test Constructor with null array
     */
    @Test
    public void constructorArray_Null() {
        final Comparator<File> c = new CompositeFileComparator((Comparator<File>[])null);
        assertEquals(0, c.compare(lessFile, moreFile), "less,more");
        assertEquals(0, c.compare(moreFile, lessFile), "more,less");
        assertEquals("CompositeFileComparator{}", c.toString(), "toString");
    }
}
