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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.function.Consumers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests FileUtils.listFiles() methods.
 */
public class FileUtilsListFilesTest {

    @TempDir
    public File temporaryFolder;

    private Collection<String> filesToFilenames(final Collection<File> files) {
        return files.stream().map(File::getName).collect(Collectors.toList());
    }

    private Collection<String> filesToFilenames(final Iterator<File> files) {
        final Collection<String> fileNames = new ArrayList<>();
        // Iterator.forEachRemaining() closes the underlying stream.
        files.forEachRemaining(f -> fileNames.add(f.getName()));
        return fileNames;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    public void setUp() throws Exception {
        File dir = temporaryFolder;
        File file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "README");
        FileUtils.touch(file);

        dir = new File(dir, "subdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "dummy-readme.txt");
        FileUtils.touch(file);

        dir = new File(dir, "subsubdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-file.txt");
        FileUtils.touch(file);
        file = new File(dir, "dummy-index.html");
        FileUtils.touch(file);

        dir = dir.getParentFile();
        dir = new File(dir, "CVS");
        dir.mkdirs();
        file = new File(dir, "Entries");
        FileUtils.touch(file);
        file = new File(dir, "Repository");
        FileUtils.touch(file);
    }

    @Test
    public void testIterateFilesByExtension() {
        final String[] extensions = { "xml", "txt" };

        Iterator<File> files = FileUtils.iterateFiles(temporaryFolder, extensions, false);
        try {
            Collection<String> filenames = filesToFilenames(files);
            assertEquals(1, filenames.size());
            assertTrue(filenames.contains("dummy-build.xml"));
            assertFalse(filenames.contains("README"));
            assertFalse(filenames.contains("dummy-file.txt"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }

        try {
            files = FileUtils.iterateFiles(temporaryFolder, extensions, true);
            Collection<String> filenames = filesToFilenames(files);
            assertEquals(4, filenames.size());
            assertTrue(filenames.contains("dummy-file.txt"));
            assertFalse(filenames.contains("dummy-index.html"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }

        files = FileUtils.iterateFiles(temporaryFolder, null, false);
        try {
            Collection<String> filenames = filesToFilenames(files);
            assertEquals(2, filenames.size());
            assertTrue(filenames.contains("dummy-build.xml"));
            assertTrue(filenames.contains("README"));
            assertFalse(filenames.contains("dummy-file.txt"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }
    }

    @Test
    public void testListFiles() {
        Collection<File> files;
        Collection<String> filenames;
        IOFileFilter fileFilter;
        IOFileFilter dirFilter;

        // First, find non-recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        files = FileUtils.listFiles(temporaryFolder, fileFilter, null);
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertFalse(filenames.contains("dummy-index.html"), "'dummy-index.html' shouldn't be found");
        assertFalse(filenames.contains("Entries"), "'Entries' shouldn't be found");

        // Second, find recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("CVS"));
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(filenames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(filenames.contains("Entries"), "'Entries' shouldn't be found");

        // Do the same as above but now with the filter coming from FileFilterUtils
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.makeCVSAware(null);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(filenames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(filenames.contains("Entries"), "'Entries' shouldn't be found");

        // Again with the CVS filter but now with a non-null parameter
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.prefixFileFilter("sub");
        dirFilter = FileFilterUtils.makeCVSAware(dirFilter);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(filenames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(filenames.contains("Entries"), "'Entries' shouldn't be found");

        assertThrows(NullPointerException.class, () -> FileUtils.listFiles(temporaryFolder, null, null));
    }

    @Test
    public void testListFilesByExtension() {
        final String[] extensions = {"xml", "txt"};

        Collection<File> files = FileUtils.listFiles(temporaryFolder, extensions, false);
        assertEquals(1, files.size());
        Collection<String> filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertFalse(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));

        files = FileUtils.listFiles(temporaryFolder, extensions, true);
        filenames = filesToFilenames(files);
        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("dummy-file.txt"));
        assertFalse(filenames.contains("dummy-index.html"));

        files = FileUtils.listFiles(temporaryFolder, null, false);
        assertEquals(2, files.size());
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertTrue(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));
    }


}
