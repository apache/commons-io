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
import java.util.Comparator;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * Base Test case for Comparator implementations.
 */
public abstract class ComparatorAbstractTestCase extends FileBasedTestCase {

    /** comparator instance */
    protected Comparator comparator;

    /** reverse comparator instance */
    protected Comparator reverse;

    /** File which compares equal to  "equalFile2" */
    protected File equalFile1;

    /** File which compares equal to  "equalFile1" */
    protected File equalFile2;

    /** File which is less than the "moreFile" */
    protected File lessFile;

    /** File which is more than the "lessFile" */
    protected File moreFile;

    /**
     * Construct a new test case with the specified name
     * @param name Name of the test
     */
    public ComparatorAbstractTestCase(String name) {
        super(name);
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        comparator = DefaultFileComparator.DEFAULT_COMPARATOR;
        reverse = DefaultFileComparator.DEFAULT_REVERSE;
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        comparator = null;
        reverse = null;
        equalFile1 = null;
        equalFile2 = null;
        lessFile = null;
        moreFile = null;
    }

    /**
     * Test the comparator.
     */
    public void testComparator() {
        assertTrue("equal", comparator.compare(equalFile1, equalFile2) == 0);
        assertTrue("less",  comparator.compare(lessFile, moreFile) < 0);
        assertTrue("more",  comparator.compare(moreFile, lessFile) > 0);
    }

    /**
     * Test the comparator reversed.
     */
    public void testReverseComparator() {
        assertTrue("equal", reverse.compare(equalFile1, equalFile2) == 0);
        assertTrue("less",  reverse.compare(moreFile, lessFile) < 0);
        assertTrue("more",  reverse.compare(lessFile, moreFile) > 0);
    }
}
