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
 * Test case for {@link LastModifiedFileComparator}.
 */
public class LastModifiedFileComparatorTest extends ComparatorAbstractTestCase {

    /**
     * Construct a new test case with the specified name.
     *
     * @param name Name of the test
     */
    public LastModifiedFileComparatorTest(final String name) {
        super(name);
    }

    /** @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        comparator = (AbstractFileComparator) LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
        reverse = LastModifiedFileComparator.LASTMODIFIED_REVERSE;
        final File dir = getTestDirectory();
        final File olderFile = new File(dir, "older.txt");
        createFile(olderFile, 0);

        final File equalFile = new File(dir, "equal.txt");
        createFile(equalFile, 0);
        do {
            try { 
                Thread.sleep(300);
            } catch(final InterruptedException ie) {
                // ignore
            }
            equalFile.setLastModified(System.currentTimeMillis());
        } while( olderFile.lastModified() == equalFile.lastModified() );

        final File newerFile = new File(dir, "newer.txt");
        createFile(newerFile, 0);
        do {
            try { 
                Thread.sleep(300);
            } catch(final InterruptedException ie) {
                // ignore
            }
            newerFile.setLastModified(System.currentTimeMillis());
        } while( equalFile.lastModified() == newerFile.lastModified() );
        equalFile1 = equalFile;
        equalFile2 = equalFile;
        lessFile   = olderFile;
        moreFile   = newerFile;
    }

}
