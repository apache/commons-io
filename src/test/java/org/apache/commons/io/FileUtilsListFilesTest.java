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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.function.Uncheck;
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
        file = new File(dir, "dummy-indexhtml");
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
            final Collection<String> fileNames = toFileNames(files);
            assertEquals(1, fileNames.size());
            assertTrue(fileNames.contains("dummy-build.xml"));
            assertFalse(fileNames.contains("README"));
            assertFalse(fileNames.contains("dummy-file.txt"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }

        try {
            files = FileUtils.iterateFiles(temporaryFolder, extensions, true);
            final Collection<String> fileNames = toFileNames(files);
            assertEquals(4, fileNames.size());
            assertTrue(fileNames.contains("dummy-file.txt"));
            assertFalse(fileNames.contains("dummy-index.html"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }

        files = FileUtils.iterateFiles(temporaryFolder, null, false);
        try {
            final Collection<String> fileNames = toFileNames(files);
            assertEquals(2, fileNames.size());
            assertTrue(fileNames.contains("dummy-build.xml"));
            assertTrue(fileNames.contains("README"));
            assertFalse(fileNames.contains("dummy-file.txt"));
        } finally {
            // Backstop in case filesToFilenames() failure.
            files.forEachRemaining(Consumers.nop());
        }
    }

    @Test
    public void testListFiles() {
        Collection<File> files;
        Collection<String> fileNames;
        IOFileFilter fileFilter;
        IOFileFilter dirFilter;
        //
        // First, find non-recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        files = FileUtils.listFiles(temporaryFolder, fileFilter, null);
        fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertFalse(fileNames.contains("dummy-index.html"), "'dummy-index.html' shouldn't be found");
        assertFalse(fileNames.contains("Entries"), "'Entries' shouldn't be found");
        //
        // Second, find recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("CVS"));
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(fileNames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(fileNames.contains("Entries"), "'Entries' shouldn't be found");
        //
        // Do the same as above but now with the filter coming from FileFilterUtils
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.makeCVSAware(null);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(fileNames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(fileNames.contains("Entries"), "'Entries' shouldn't be found");
        //
        // Again with the CVS filter but now with a non-null parameter
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.prefixFileFilter("sub");
        dirFilter = FileFilterUtils.makeCVSAware(dirFilter);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"), "'dummy-build.xml' is missing");
        assertTrue(fileNames.contains("dummy-index.html"), "'dummy-index.html' is missing");
        assertFalse(fileNames.contains("Entries"), "'Entries' shouldn't be found");
        // Edge case
        assertThrows(NullPointerException.class, () -> FileUtils.listFiles(temporaryFolder, null, null));
    }

    @Test
    public void testListFilesByExtension() {
        final String[] extensions = {"xml", "txt"};

        Collection<File> files = FileUtils.listFiles(temporaryFolder, extensions, false);
        assertEquals(1, files.size());
        Collection<String> fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"));
        assertFalse(fileNames.contains("README"));
        assertFalse(fileNames.contains("dummy-file.txt"));

        files = FileUtils.listFiles(temporaryFolder, extensions, true);
        fileNames = toFileNames(files);
        assertEquals(4, fileNames.size(), fileNames::toString);
        assertTrue(fileNames.contains("dummy-file.txt"));
        assertFalse(fileNames.contains("dummy-index.html"));

        files = FileUtils.listFiles(temporaryFolder, null, false);
        assertEquals(2, files.size(), files::toString);
        fileNames = toFileNames(files);
        assertTrue(fileNames.contains("dummy-build.xml"));
        assertTrue(fileNames.contains("README"));
        assertFalse(fileNames.contains("dummy-file.txt"));

        final File directory = new File(temporaryFolder, "subdir1/subsubdir1");
        files = FileUtils.listFiles(directory, new String[] { "html" }, false);
        fileNames = toFileNames(files);
        assertFalse(files.isEmpty(), directory::toString);
        assertTrue(fileNames.contains("dummy-index.html"));
        assertFalse(fileNames.contains("dummy-indexhtml"));
        files = FileUtils.listFiles(temporaryFolder, new String[] { "html" }, true);
        fileNames = toFileNames(files);
        assertFalse(files.isEmpty(), temporaryFolder::toString);
        assertTrue(fileNames.contains("dummy-index.html"));
        assertFalse(fileNames.contains("dummy-indexhtml"));
    }

    @Test
    public void testListFilesMissing() {
        assertTrue(FileUtils.listFiles(new File(temporaryFolder, "dir/does/not/exist/at/all"), null, false).isEmpty());
    }

    @Test
    public void testListFilesWithDeletion() throws IOException {
        final String[] extensions = {"xml", "txt"};
        final List<File> list;
        final File xFile = new File(temporaryFolder, "x.xml");
        if (!xFile.createNewFile()) {
            fail("could not create test file: " + xFile);
        }
        final Collection<File> files = FileUtils.listFiles(temporaryFolder, extensions, true);
        assertEquals(5, files.size());
        try (Stream<File> stream = Uncheck.get(() -> FileUtils.streamFiles(temporaryFolder, true, extensions))) {
            assertTrue(xFile.delete());
            list = stream.collect(Collectors.toList());
            assertFalse(list.contains(xFile), list::toString);
        }
        assertEquals(4, list.size());
    }

    /**
     * Tests <a href="https://issues.apache.org/jira/browse/IO-856">IO-856</a> ListFiles should not fail on vanishing files.
     */
    @Test
    public void testListFilesWithDeletionThreaded() throws ExecutionException, InterruptedException {
        // test for IO-856
        // create random directory in tmp, create the directory if it does not exist
        final Path tempPath = PathUtils.getTempDirectory().resolve("IO-856");
        final File tempDir = tempPath.toFile();
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            fail("Could not create file path: " + tempDir.getAbsolutePath());
        }
        final int waitTime = 10_000;
        final int maxFiles = 500;
        final byte[] bytes = "TEST".getBytes(StandardCharsets.UTF_8);
        final CompletableFuture<Void> c1 = CompletableFuture.runAsync(() -> {
            final long endTime = System.currentTimeMillis() + waitTime;
            int count = 0;
            while (System.currentTimeMillis() < endTime && count < maxFiles) {
                final File file = new File(tempDir.getAbsolutePath(), UUID.randomUUID() + ".deletetester");
                file.deleteOnExit();
                try {
                    Files.write(file.toPath(), bytes);
                    count++;
                } catch (final Exception e) {
                    fail("Could not create test file: '" + file.getAbsolutePath() + "': " + e, e);
                }
                if (!file.delete()) {
                    fail("Could not delete test file: '" + file.getAbsolutePath() + "'");
                }
            }
            // System.out.printf("Created %,d%n", count);
        });
        final CompletableFuture<Void> c2 = CompletableFuture.runAsync(() -> {
            final long endTime = System.currentTimeMillis() + waitTime;
            int max = 0;
            try {
                while (System.currentTimeMillis() < endTime) {
                    final Collection<File> files = FileUtils.listFiles(tempDir, new String[] { ".deletetester" }, false);
                    assertNotNull(files);
                    max = Math.max(max, files.size());
                }
            } catch (final Exception e) {
                System.out.printf("List size max %,d%n", max);
                fail("IO-856 test failure: " + e, e);
                // The exception can be hidden.
                e.printStackTrace();
            }
            // System.out.printf("List size max %,d%n", max);
        });
        // wait for the threads to finish
        c1.get();
        c2.get();
    }

    private Collection<String> toFileNames(final Collection<File> files) {
        return files.stream().map(File::getName).collect(Collectors.toList());
    }

    /**
     * Consumes and closes the underlying stream.
     *
     * @param files The iterator to consume.
     * @return a new collection.
     */
    private Collection<String> toFileNames(final Iterator<File> files) {
        final Collection<String> fileNames = new ArrayList<>();
        // Iterator.forEachRemaining() closes the underlying stream.
        files.forEachRemaining(f -> fileNames.add(f.getName()));
        return fileNames;
    }

}
