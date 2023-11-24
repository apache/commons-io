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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RegexFileFilter}.
 */
public class RegexFileFilterTest {

    public void assertFiltering(final IOFileFilter filter, final File file, final boolean expected) {
        // Note. This only tests the (File, String) version if the parent of
        // the File passed in is not null
        assertEquals(expected, filter.accept(file), "Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);

        if (file != null && file.getParentFile() != null) {
            assertEquals(expected, filter.accept(file.getParentFile(), file.getName()),
                    "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
            assertEquals(expected, filter.matches(file.toPath()), "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
        } else if (file == null) {
            assertEquals(expected, filter.accept(file), "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null");
            assertEquals(expected, filter.matches(null), "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null");
        }
        // Just don't blow up
        assertNotNull(filter.toString());
    }

    public void assertFiltering(final IOFileFilter filter, final Path path, final boolean expected) {
        // Note. This only tests the (Path, Path) version if the parent of
        // the Path passed in is not null
        final FileVisitResult expectedFileVisitResult = AbstractFileFilter.toDefaultFileVisitResult(expected);
        assertEquals(expectedFileVisitResult, filter.accept(path, null),
                "Filter(Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for " + path);
        assertEquals(expectedFileVisitResult != FileVisitResult.TERMINATE, filter.matches(path),
                "Filter(Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for " + path);

        if (path != null && path.getParent() != null) {
            assertEquals(expectedFileVisitResult, filter.accept(path, null),
                    "Filter(Path, Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for " + path);
        } else if (path == null) {
            assertEquals(expectedFileVisitResult, filter.accept(path, null),
                    "Filter(Path, Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for null");
        }
        // Just don't blow up
        assertNotNull(filter.toString());
    }

    private RegexFileFilter assertSerializable(final RegexFileFilter serializable) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(serializable);
            }
            baos.flush();
            assertTrue(baos.toByteArray().length > 0);
        }
        return serializable;
    }

    @Test
    public void testRegex() throws IOException {
        RegexFileFilter filter = new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$");
        assertSerializable(filter);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test-10.java"), true);
        assertFiltering(filter, new File("test-.java"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("test-10.java").toPath(), true);
        assertFiltering(filter, new File("test-.java").toPath(), false);

        filter = new RegexFileFilter("^[Tt]est.java$");
        assertSerializable(filter);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("test.java").toPath(), true);
        assertFiltering(filter, new File("tEST.java").toPath(), false);

        filter = new RegexFileFilter(Pattern.compile("^test.java$", Pattern.CASE_INSENSITIVE));
        assertSerializable(filter);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("test.java").toPath(), true);
        assertFiltering(filter, new File("tEST.java").toPath(), true);

        filter = new RegexFileFilter("^test.java$", Pattern.CASE_INSENSITIVE);
        assertSerializable(filter);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("test.java").toPath(), true);
        assertFiltering(filter, new File("tEST.java").toPath(), true);

        filter = new RegexFileFilter("^test.java$", IOCase.INSENSITIVE);
        assertSerializable(filter);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("test.java"), true);
        assertFiltering(filter, new File("tEST.java"), true);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("test.java").toPath(), true);
        assertFiltering(filter, new File("tEST.java").toPath(), true);
    }

    @Test
    public void testRegexEdgeCases() {
        assertThrows(NullPointerException.class, () -> assertSerializable(new RegexFileFilter((String) null)));
        assertThrows(NullPointerException.class, () -> assertSerializable(new RegexFileFilter(null, Pattern.CASE_INSENSITIVE)));
        assertThrows(NullPointerException.class, () -> assertSerializable(new RegexFileFilter(null, IOCase.INSENSITIVE)));
        assertThrows(NullPointerException.class, () -> assertSerializable(new RegexFileFilter((java.util.regex.Pattern) null)));
    }

    /**
     * Tests https://issues.apache.org/jira/browse/IO-733.
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegexFileNameOnly() throws IOException {
        final Path path = Paths.get("folder", "Foo.java");
        final String patternStr = "Foo.*";
        assertFiltering(assertSerializable(new RegexFileFilter(patternStr)), path, true);
        assertFiltering(assertSerializable(new RegexFileFilter(Pattern.compile(patternStr), (Function<Path, String> & Serializable) Path::toString)), path,
                false);
        //
        assertFiltering(new RegexFileFilter(Pattern.compile(patternStr), (Function<Path, String> & Serializable) null), path, false);
        assertFiltering(new RegexFileFilter(Pattern.compile(patternStr), (Function<Path, String> & Serializable) p -> null), path, false);
        //
        assertFiltering(assertSerializable(new RegexFileFilter(Pattern.compile(patternStr), (Function<Path, String> & Serializable) null)), path, false);
        assertFiltering(assertSerializable(new RegexFileFilter(Pattern.compile(patternStr), (Function<Path, String> & Serializable) p -> null)), path, false);
    }

}
