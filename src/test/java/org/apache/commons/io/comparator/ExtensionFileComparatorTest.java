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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ExtensionFileComparator}.
 */
public class ExtensionFileComparatorTest extends ComparatorAbstractTestCase {


    @BeforeEach
    public void setUp() throws Exception {
        comparator = (AbstractFileComparator) ExtensionFileComparator.EXTENSION_COMPARATOR;
        reverse = ExtensionFileComparator.EXTENSION_REVERSE;
        equalFile1 = new File("abc.foo");
        equalFile2 = new File("def.foo");
        lessFile   = new File("abc.abc");
        moreFile   = new File("abc.xyz");
    }

    /** Test case sensitivity */
    @Test
    public void testCaseSensitivity() {
        final File file3 = new File("abc.FOO");
        final Comparator<File> sensitive = new ExtensionFileComparator(null); /* test null as well */
        assertTrue(sensitive.compare(equalFile1, equalFile2) == 0, "sensitive file1 & file2 = 0");
        assertTrue(sensitive.compare(equalFile1, file3) > 0, "sensitive file1 & file3 > 0");
        assertTrue(sensitive.compare(equalFile1, lessFile) > 0, "sensitive file1 & less  > 0");

        final Comparator<File> insensitive = ExtensionFileComparator.EXTENSION_INSENSITIVE_COMPARATOR;
        assertTrue(insensitive.compare(equalFile1, equalFile2) == 0, "insensitive file1 & file2 = 0");
        assertTrue(insensitive.compare(equalFile1, file3) == 0, "insensitive file1 & file3 = 0");
        assertTrue(insensitive.compare(equalFile1, lessFile) > 0, "insensitive file1 & file4 > 0");
        assertTrue(insensitive.compare(file3, lessFile) > 0, "insensitive file3 & less  > 0");
    }
}
