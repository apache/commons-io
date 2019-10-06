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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.junit.jupiter.api.Test;

/**
 * This is used to test DirectoryWalker for correctness when using Java4 (i.e. no generics).
 *
 * @see DirectoryWalker
 */
@SuppressWarnings({"unchecked", "rawtypes"}) // Java4
public class DirectoryWalkerTestCaseJava4 {

    // Directories
    private static final File current = new File(".");
    private static final File javaDir = new File("src/main/java");
    private static final File orgDir = new File(javaDir, "org");
    private static final File apacheDir = new File(orgDir, "apache");
    private static final File commonsDir = new File(apacheDir, "commons");
    private static final File ioDir = new File(commonsDir, "io");
    private static final File outputDir = new File(ioDir, "output");
    private static final File[] dirs = new File[]{orgDir, apacheDir, commonsDir, ioDir, outputDir};

    // Files
    private static final File filenameUtils = new File(ioDir, "FilenameUtils.java");
    private static final File ioUtils = new File(ioDir, "IOUtils.java");
    private static final File proxyWriter = new File(outputDir, "ProxyWriter.java");
    private static final File nullStream = new File(outputDir, "NullOutputStream.java");
    private static final File[] ioFiles = new File[]{filenameUtils, ioUtils};
    private static final File[] outputFiles = new File[]{proxyWriter, nullStream};

    // Filters
    private static final IOFileFilter dirsFilter = createNameFilter(dirs);
    private static final IOFileFilter iofilesFilter = createNameFilter(ioFiles);
    private static final IOFileFilter outputFilesFilter = createNameFilter(outputFiles);
    private static final IOFileFilter ioDirAndFilesFilter = new OrFileFilter(dirsFilter, iofilesFilter);
    private static final IOFileFilter dirsAndFilesFilter = new OrFileFilter(ioDirAndFilesFilter, outputFilesFilter);

    // Filter to exclude SVN files
    private static final IOFileFilter NOT_SVN = FileFilterUtils.makeSVNAware(null);

    //-----------------------------------------------------------------------

    /**
     * Test Filtering
     */
    @Test
    public void testFilter() {
        final List<File> results = new TestFileFinder(dirsAndFilesFilter, -1).find(javaDir);
        assertEquals(1 + dirs.length + ioFiles.length + outputFiles.length, results.size(), "Result Size");
        assertTrue(results.contains(javaDir), "Start Dir");
        checkContainsFiles("Dir", dirs, results);
        checkContainsFiles("IO File", ioFiles, results);
        checkContainsFiles("Output File", outputFiles, results);
    }

    /**
     * Test Filtering and limit to depth 0
     */
    @Test
    public void testFilterAndLimitA() {
        final List<File> results = new TestFileFinder(NOT_SVN, 0).find(javaDir);
        assertEquals(1, results.size(), "[A] Result Size");
        assertTrue(results.contains(javaDir), "[A] Start Dir");
    }

    /**
     * Test Filtering and limit to depth 1
     */
    @Test
    public void testFilterAndLimitB() {
        final List<File> results = new TestFileFinder(NOT_SVN, 1).find(javaDir);
        assertEquals(2, results.size(), "[B] Result Size");
        assertTrue(results.contains(javaDir), "[B] Start Dir");
        assertTrue(results.contains(orgDir), "[B] Org Dir");
    }

    /**
     * Test Filtering and limit to depth 3
     */
    @Test
    public void testFilterAndLimitC() {
        final List<File> results = new TestFileFinder(NOT_SVN, 3).find(javaDir);
        assertEquals(4, results.size(), "[C] Result Size");
        assertTrue(results.contains(javaDir), "[C] Start Dir");
        assertTrue(results.contains(orgDir), "[C] Org Dir");
        assertTrue(results.contains(apacheDir), "[C] Apache Dir");
        assertTrue(results.contains(commonsDir), "[C] Commons Dir");
    }

    /**
     * Test Filtering and limit to depth 5
     */
    @Test
    public void testFilterAndLimitD() {
        final List<File> results = new TestFileFinder(dirsAndFilesFilter, 5).find(javaDir);
        assertEquals(1 + dirs.length + ioFiles.length, results.size(), "[D] Result Size");
        assertTrue(results.contains(javaDir), "[D] Start Dir");
        checkContainsFiles("[D] Dir", dirs, results);
        checkContainsFiles("[D] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile1() {
        final List<File> results = new TestFileFinder(dirsFilter, iofilesFilter, -1).find(javaDir);
        assertEquals(1 + dirs.length + ioFiles.length, results.size(), "[DirAndFile1] Result Size");
        assertTrue(results.contains(javaDir), "[DirAndFile1] Start Dir");
        checkContainsFiles("[DirAndFile1] Dir", dirs, results);
        checkContainsFiles("[DirAndFile1] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile2() {
        final List<File> results = new TestFileFinder(null, null, -1).find(javaDir);
        assertTrue(results.size() > 1 + dirs.length + ioFiles.length, "[DirAndFile2] Result Size");
        assertTrue(results.contains(javaDir), "[DirAndFile2] Start Dir");
        checkContainsFiles("[DirAndFile2] Dir", dirs, results);
        checkContainsFiles("[DirAndFile2] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile3() {
        final List<File> results = new TestFileFinder(dirsFilter, null, -1).find(javaDir);
        final List resultDirs = directoriesOnly(results);
        assertEquals(1 + dirs.length, resultDirs.size(), "[DirAndFile3] Result Size");
        assertTrue(results.contains(javaDir), "[DirAndFile3] Start Dir");
        checkContainsFiles("[DirAndFile3] Dir", dirs, resultDirs);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile4() {
        final List<File> results = new TestFileFinder(null, iofilesFilter, -1).find(javaDir);
        final List resultFiles = filesOnly(results);
        assertEquals(ioFiles.length, resultFiles.size(), "[DirAndFile4] Result Size");
        assertTrue(results.contains(javaDir), "[DirAndFile4] Start Dir");
        checkContainsFiles("[DirAndFile4] File", ioFiles, resultFiles);
    }

    /**
     * Test Limiting to current directory
     */
    @Test
    public void testLimitToCurrent() {
        final List<File> results = new TestFileFinder(null, 0).find(current);
        assertEquals(1, results.size(), "Result Size");
        assertTrue(results.contains(new File(".")), "Current Dir");
    }

    /**
     * test an invalid start directory
     */
    @Test
    public void testMissingStartDirectory() {

        // TODO is this what we want with invalid directory?
        final File invalidDir = new File("invalid-dir");
        final List<File> results = new TestFileFinder(null, -1).find(invalidDir);
        assertEquals(1, results.size(), "Result Size");
        assertTrue(results.contains(invalidDir), "Current Dir");

        try {
            new TestFileFinder(null, -1).find(null);
            fail("Null start directory didn't throw Exception");
        } catch (final NullPointerException ignore) {
            // expected result
        }
    }

    /**
     * test an invalid start directory
     */
    @Test
    public void testHandleStartDirectoryFalse() {

        final List<File> results = new TestFalseFileFinder(null, -1).find(current);
        assertEquals(0, results.size(), "Result Size");

    }

    // ------------ Convenience Test Methods ------------------------------------

    /**
     * Check the files in the array are in the results list.
     */
    private void checkContainsFiles(final String prefix, final File[] files, final Collection results) {
        for (int i = 0; i < files.length; i++) {
            assertTrue(results.contains(files[i]), prefix + "[" + i + "] " + files[i]);
        }
    }

    /**
     * Extract the directories.
     */
    private List directoriesOnly(final Collection<File> results) {
        final List list = new ArrayList(results.size());
        for (final File file : results) {
            if (file.isDirectory()) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Extract the files.
     */
    private List filesOnly(final Collection<File> results) {
        final List list = new ArrayList(results.size());
        for (final File file : results) {
            if (file.isFile()) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Create a name filter containing the names of the files
     * in the array.
     */
    private static IOFileFilter createNameFilter(final File[] files) {
        final String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return new NameFileFilter(names);
    }

    /**
     * Test Cancel
     * @throws IOException
     */
    @Test
    public void testCancel() throws IOException {
        String cancelName = null;

        // Cancel on a file
        try {
            cancelName = "DirectoryWalker.java";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals(cancelName, cancel.getFile().getName(), "File:  " + cancelName);
            assertEquals(5, cancel.getDepth(), "Depth: " + cancelName);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals(cancelName, cancel.getFile().getName(), "File:  " + cancelName);
            assertEquals(3, cancel.getDepth(), "Depth: " + cancelName);
        }

        // Suppress CancelException (use same file name as preceding test)
        final List results = new TestCancelWalker(cancelName, true).find(javaDir);
        final File lastFile = (File) results.get(results.size() - 1);
        assertEquals(cancelName, lastFile.getName(), "Suppress:  " + cancelName);
    }

    /**
     * Test Cancel
     * @throws IOException
     */
    @Test
    public void testMultiThreadCancel() throws IOException {
        String cancelName = "DirectoryWalker.java";
        TestMultiThreadCancelWalker walker = new TestMultiThreadCancelWalker(cancelName, false);
        // Cancel on a file
        try {
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            final File last = (File) walker.results.get(walker.results.size() - 1);
            assertEquals(cancelName, last.getName());
            assertEquals(5, cancel.getDepth(), "Depth: " + cancelName);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            walker = new TestMultiThreadCancelWalker(cancelName, false);
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals(cancelName, cancel.getFile().getName(), "File:  " + cancelName);
            assertEquals(3, cancel.getDepth(), "Depth: " + cancelName);
        }

        // Suppress CancelException (use same file name as preceding test)
        walker = new TestMultiThreadCancelWalker(cancelName, true);
        final List results = walker.find(javaDir);
        final File lastFile = (File) results.get(results.size() - 1);
        assertEquals(cancelName, lastFile.getName(), "Suppress:  " + cancelName);

    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    private static class TestFileFinder extends DirectoryWalker {

        protected TestFileFinder(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        protected TestFileFinder(final IOFileFilter dirFilter, final IOFileFilter fileFilter, final int depthLimit) {
            super(dirFilter, fileFilter, depthLimit);
        }

        /**
         * find files.
         */
        protected List<File> find(final File startDirectory) {
            final List<File> results = new ArrayList<>();
            try {
                walk(startDirectory, results);
            } catch (final IOException ex) {
                fail(ex.toString());
            }
            return results;
        }

        /**
         * Handles a directory end by adding the File to the result set.
         */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection results) {
            results.add(directory);
        }

        /**
         * Handles a file by adding the File to the result set.
         */
        @Override
        protected void handleFile(final File file, final int depth, final Collection results) {
            results.add(file);
        }
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that always returns false
     * from handleDirectoryStart()
     */
    private static class TestFalseFileFinder extends TestFileFinder {

        protected TestFalseFileFinder(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        /**
         * Always returns false.
         */
        @Override
        protected boolean handleDirectory(final File directory, final int depth, final Collection results) {
            return false;
        }
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    static class TestCancelWalker extends DirectoryWalker {
        private final String cancelFileName;
        private final boolean suppressCancel;

        TestCancelWalker(final String cancelFileName, final boolean suppressCancel) {
            super();
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /**
         * find files.
         */
        protected List find(final File startDirectory) throws IOException {
            final List results = new ArrayList();
            walk(startDirectory, results);
            return results;
        }

        /**
         * Handles a directory end by adding the File to the result set.
         */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection results) throws IOException {
            results.add(directory);
            if (cancelFileName.equals(directory.getName())) {
                throw new CancelException(directory, depth);
            }
        }

        /**
         * Handles a file by adding the File to the result set.
         */
        @Override
        protected void handleFile(final File file, final int depth, final Collection results) throws IOException {
            results.add(file);
            if (cancelFileName.equals(file.getName())) {
                throw new CancelException(file, depth);
            }
        }

        /**
         * Handles Cancel.
         */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection results,
                                       final CancelException cancel) throws IOException {
            if (!suppressCancel) {
                super.handleCancelled(startDirectory, results, cancel);
            }
        }
    }

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    static class TestMultiThreadCancelWalker extends DirectoryWalker {
        private final String cancelFileName;
        private final boolean suppressCancel;
        private boolean cancelled;
        public List results;

        TestMultiThreadCancelWalker(final String cancelFileName, final boolean suppressCancel) {
            super();
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /**
         * find files.
         */
        protected List find(final File startDirectory) throws IOException {
            results = new ArrayList();
            walk(startDirectory, results);
            return results;
        }

        /**
         * Handles a directory end by adding the File to the result set.
         */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection results) throws IOException {
            results.add(directory);
            assertFalse(cancelled);
            if (cancelFileName.equals(directory.getName())) {
                cancelled = true;
            }
        }

        /**
         * Handles a file by adding the File to the result set.
         */
        @Override
        protected void handleFile(final File file, final int depth, final Collection results) throws IOException {
            results.add(file);
            assertFalse(cancelled);
            if (cancelFileName.equals(file.getName())) {
                cancelled = true;
            }
        }

        /**
         * Handles Cancelled.
         */
        @Override
        protected boolean handleIsCancelled(final File file, final int depth, final Collection results) throws IOException {
            return cancelled;
        }

        /**
         * Handles Cancel.
         */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection results,
                                       final CancelException cancel) throws IOException {
            if (!suppressCancel) {
                super.handleCancelled(startDirectory, results, cancel);
            }
        }
    }

}
