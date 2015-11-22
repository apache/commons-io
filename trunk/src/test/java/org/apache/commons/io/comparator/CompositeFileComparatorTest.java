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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Test case for {@link CompositeFileComparator}.
 */
public class CompositeFileComparatorTest extends ComparatorAbstractTestCase {

    /**
     * Construct a new test case with the specified name.
     *
     * @param name Name of the test
     */
    public CompositeFileComparatorTest(final String name) {
        super(name);
    }

    /** @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        comparator = new CompositeFileComparator(
                new AbstractFileComparator[] {
                    (AbstractFileComparator) SizeFileComparator.SIZE_COMPARATOR,
                    (AbstractFileComparator) ExtensionFileComparator.EXTENSION_COMPARATOR});
        reverse = new ReverseComparator(comparator);
        final File dir = getTestDirectory();
        lessFile   = new File(dir, "xyz.txt");
        equalFile1 = new File(dir, "foo.txt");
        equalFile2 = new File(dir, "bar.txt");
        moreFile   = new File(dir, "foo.xyz");
        createFile(lessFile,   32);
        createFile(equalFile1, 48);
        createFile(equalFile2, 48);
        createFile(moreFile,   48);
    }

    /**
     * Test Constructor with null Iterable
     */
    public void testConstructorIterable() {
        final List<Comparator<File>> list = new ArrayList<Comparator<File>>();
        list.add(SizeFileComparator.SIZE_COMPARATOR);
        list.add(ExtensionFileComparator.EXTENSION_COMPARATOR);
        final Comparator<File> c = new CompositeFileComparator(list);

        assertEquals("equal", 0, c.compare(equalFile1, equalFile2));
        assertTrue("less",  c.compare(lessFile, moreFile) < 0);
        assertTrue("more",  c.compare(moreFile, lessFile) > 0);
    }

    /**
     * Test Constructor with null Iterable
     */
    public void testConstructorIterableNull() {
        final Comparator<File> c = new CompositeFileComparator((Iterable<Comparator<File>>)null);
        assertEquals("less,more", 0, c.compare(lessFile, moreFile));
        assertEquals("more,less", 0, c.compare(moreFile, lessFile));
        assertEquals("toString", "CompositeFileComparator{}", c.toString());
    }

    /**
     * Test Constructor with null array
     */
    public void testConstructorArrayNull() {
        final Comparator<File> c = new CompositeFileComparator((Comparator<File>[])null);
        assertEquals("less,more", 0, c.compare(lessFile, moreFile));
        assertEquals("more,less", 0, c.compare(moreFile, lessFile));
        assertEquals("toString", "CompositeFileComparator{}", c.toString());
    }
}
