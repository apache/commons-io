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
package org.apache.commons.io.filefilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;
import org.junit.jupiter.api.Test;

/**
 * Used to test RegexFileFilterUtils.
 */
public class RegexFileFilterTestCase {

    public void assertFiltering(final IOFileFilter filter, final File file, final boolean expected) throws Exception {
        // Note. This only tests the (File, String) version if the parent of
        //       the File passed in is not null
        assertEquals(expected, filter.accept(file),
                "Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);

        if (file != null && file.getParentFile() != null) {
            assertEquals(expected, filter.accept(file.getParentFile(), file.getName()),
                    "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
        } else if (file == null) {
            assertEquals(expected, filter.accept(file),
                    "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null");
        }
    }

    @Test
    public void testRegex() throws Exception {
        IOFileFilter filter = new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$");
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test-10.java"), true);
        assertFiltering(filter, new File("test-.java"), false);

        filter = new RegexFileFilter("^[Tt]est.java$");
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), false);

        filter = new RegexFileFilter(Pattern.compile("^test.java$", Pattern.CASE_INSENSITIVE));
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);

        filter = new RegexFileFilter("^test.java$", Pattern.CASE_INSENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);

        filter = new RegexFileFilter("^test.java$", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);

        try {
            new RegexFileFilter((String)null);
            fail();
        } catch (final IllegalArgumentException ignore) {
            // expected
        }

        try {
            new RegexFileFilter(null, Pattern.CASE_INSENSITIVE);
            fail();
        } catch (final IllegalArgumentException ignore) {
            // expected
        }

        try {
            new RegexFileFilter(null, IOCase.INSENSITIVE);
            fail();
        } catch (final IllegalArgumentException ignore) {
            // expected
        }

        try {
            new RegexFileFilter((java.util.regex.Pattern)null);
            fail();
        } catch (final IllegalArgumentException ignore) {
            // expected
        }
    }

}
