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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test case for {@link LastModifiedFileComparator}.
 */
public class LastModifiedFileComparatorTest extends ComparatorAbstractTestCase {
    @BeforeEach
    public void setUp() throws Exception {
        comparator = (AbstractFileComparator) LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
        reverse = LastModifiedFileComparator.LASTMODIFIED_REVERSE;
        final File olderFile = new File(dir, "older.txt");
        if (!olderFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + olderFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 =
                new BufferedOutputStream(new FileOutputStream(olderFile))) {
            TestUtils.generateTestData(output2, 0);
        }

        final File equalFile = new File(dir, "equal.txt");
        if (!equalFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + equalFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(equalFile))) {
            TestUtils.generateTestData(output1, 0);
        }
        do {
            TestUtils.sleepQuietly(300);
            equalFile.setLastModified(System.currentTimeMillis());
        } while( olderFile.lastModified() == equalFile.lastModified() );

        final File newerFile = new File(dir, "newer.txt");
        if (!newerFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + newerFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(newerFile))){
            TestUtils.generateTestData(output, 0);
        }
        do {
            TestUtils.sleepQuietly(300);
            newerFile.setLastModified(System.currentTimeMillis());
        } while( equalFile.lastModified() == newerFile.lastModified() );
        equalFile1 = equalFile;
        equalFile2 = equalFile;
        lessFile   = olderFile;
        moreFile   = newerFile;
    }

}
