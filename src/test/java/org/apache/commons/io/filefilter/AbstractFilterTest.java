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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOCase;
import org.junit.jupiter.api.io.TempDir;

/**
 * Used to test FileFilterUtils.
 */
public class AbstractFilterTest {

    /**
     * The subversion directory name.
     */
    static final String SVN_DIR_NAME = ".svn";

    static final boolean WINDOWS = File.separatorChar == '\\';

    @TempDir
    public File temporaryFolder;

    void assertFiltering(final IOFileFilter filter, final File file, final boolean expected) {
        // Note. This only tests the (File, String) version if the parent of
        // the File passed in is not null
        assertEquals(expected, filter.accept(file), "Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);

        if (file != null && file.getParentFile() != null) {
            assertEquals(expected, filter.accept(file.getParentFile(), file.getName()),
                "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
        } else if (file == null) {
            assertEquals(expected, filter.accept(file), "Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null");
        }
        assertNotNull(filter.toString());
    }

    void assertFiltering(final IOFileFilter filter, final Path path, final boolean expected) {
        // Note. This only tests the (Path, Path) version if the parent of
        // the File passed in is not null
        final FileVisitResult expectedFileVisitResult = AbstractFileFilter.toDefaultFileVisitResult(expected);
        assertEquals(expectedFileVisitResult, filter.accept(path, null),
            "Filter(Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for " + path);

        if (path != null && path.getParent() != null) {
            assertEquals(expectedFileVisitResult, filter.accept(path, null),
                "Filter(Path, Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for " + path);
        } else if (path == null) {
            assertEquals(expectedFileVisitResult, filter.accept(path, null),
                "Filter(Path, Path) " + filter.getClass().getName() + " not " + expectedFileVisitResult + " for null");
        }
        assertNotNull(filter.toString());
    }

    void assertFooBarFileFiltering(IOFileFilter filter) {
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("fred").toPath(), false);

        filter = new NameFileFilter(new String[] {"foo", "bar"}, IOCase.SENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), false);
        assertFiltering(filter, new File("BAR"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), false);
        assertFiltering(filter, new File("BAR").toPath(), false);

        filter = new NameFileFilter(new String[] {"foo", "bar"}, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), true);
        assertFiltering(filter, new File("BAR"), true);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), true);
        assertFiltering(filter, new File("BAR").toPath(), true);

        filter = new NameFileFilter(new String[] {"foo", "bar"}, IOCase.SYSTEM);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), WINDOWS);
        assertFiltering(filter, new File("BAR"), WINDOWS);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), WINDOWS);
        assertFiltering(filter, new File("BAR").toPath(), WINDOWS);

        filter = new NameFileFilter(new String[] {"foo", "bar"}, null);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("FOO"), false);
        assertFiltering(filter, new File("BAR"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), false);
        assertFiltering(filter, new File("BAR").toPath(), false);

        // repeat for a List
        final java.util.ArrayList<String> list = new java.util.ArrayList<>();
        list.add("foo");
        list.add("bar");
        filter = new NameFileFilter(list);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("bar"), true);
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("bar").toPath(), true);
        assertFiltering(filter, new File("fred").toPath(), false);

        filter = new NameFileFilter("foo");
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("FOO"), false); // case-sensitive
        assertFiltering(filter, new File("barfoo"), false);
        assertFiltering(filter, new File("foobar"), false);
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), false); // case-sensitive
        assertFiltering(filter, new File("barfoo").toPath(), false);
        assertFiltering(filter, new File("foobar").toPath(), false);
        assertFiltering(filter, new File("fred").toPath(), false);

        // FileFilterUtils.nameFileFilter(String, IOCase) tests
        filter = FileFilterUtils.nameFileFilter("foo", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("foo"), true);
        assertFiltering(filter, new File("FOO"), true); // case-insensitive
        assertFiltering(filter, new File("barfoo"), false);
        assertFiltering(filter, new File("foobar"), false);
        assertFiltering(filter, new File("fred"), false);
        assertFiltering(filter, new File("foo").toPath(), true);
        assertFiltering(filter, new File("FOO").toPath(), true); // case-insensitive
        assertFiltering(filter, new File("barfoo").toPath(), false);
        assertFiltering(filter, new File("foobar").toPath(), false);
        assertFiltering(filter, new File("fred").toPath(), false);
    }

    boolean equalsLastModified(final File left, final File right) throws IOException {
        return Files.getLastModifiedTime(left.toPath()).equals(Files.getLastModifiedTime(right.toPath()));
    }

}
