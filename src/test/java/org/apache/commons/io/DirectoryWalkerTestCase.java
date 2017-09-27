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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.Assert;
import org.junit.Test;

/**
 * This is used to test DirectoryWalker for correctness.
 *
 * @see DirectoryWalker
 *
 */
public class DirectoryWalkerTestCase {

    // Directories
    private static final File current      = new File(".");
    private static final File javaDir      = new File("src/main/java");
    private static final File orgDir       = new File(javaDir, "org");
    private static final File apacheDir    = new File(orgDir, "apache");
    private static final File commonsDir   = new File(apacheDir, "commons");
    private static final File ioDir        = new File(commonsDir, "io");
    private static final File outputDir    = new File(ioDir, "output");
    private static final File[] dirs       = new File[] {orgDir, apacheDir, commonsDir, ioDir, outputDir};

    // Files
    private static final File filenameUtils = new File(ioDir, "FilenameUtils.java");
    private static final File ioUtils       = new File(ioDir, "IOUtils.java");
    private static final File proxyWriter   = new File(outputDir, "ProxyWriter.java");
    private static final File nullStream    = new File(outputDir, "NullOutputStream.java");
    private static final File[] ioFiles     = new File[] {filenameUtils, ioUtils};
    private static final File[] outputFiles = new File[] {proxyWriter, nullStream};

    // Filters
    private static final IOFileFilter dirsFilter        = createNameFilter(dirs);
    private static final IOFileFilter iofilesFilter     = createNameFilter(ioFiles);
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
        assertEquals("Result Size", 1 + dirs.length + ioFiles.length + outputFiles.length, results.size());
        assertTrue("Start Dir", results.contains(javaDir));
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
        assertEquals("[A] Result Size", 1, results.size());
        assertTrue("[A] Start Dir",   results.contains(javaDir));
    }

    /**
     * Test Filtering and limit to depth 1
     */
    @Test
    public void testFilterAndLimitB() {
        final List<File> results = new TestFileFinder(NOT_SVN, 1).find(javaDir);
        assertEquals("[B] Result Size", 2, results.size());
        assertTrue("[B] Start Dir",   results.contains(javaDir));
        assertTrue("[B] Org Dir",     results.contains(orgDir));
    }

    /**
     * Test Filtering and limit to depth 3
     */
    @Test
    public void testFilterAndLimitC() {
        final List<File> results = new TestFileFinder(NOT_SVN, 3).find(javaDir);
        assertEquals("[C] Result Size", 4, results.size());
        assertTrue("[C] Start Dir",   results.contains(javaDir));
        assertTrue("[C] Org Dir",     results.contains(orgDir));
        assertTrue("[C] Apache Dir",  results.contains(apacheDir));
        assertTrue("[C] Commons Dir", results.contains(commonsDir));
    }

    /**
     * Test Filtering and limit to depth 5
     */
    @Test
    public void testFilterAndLimitD() {
        final List<File> results = new TestFileFinder(dirsAndFilesFilter, 5).find(javaDir);
        assertEquals("[D] Result Size", 1 + dirs.length + ioFiles.length, results.size());
        assertTrue("[D] Start Dir", results.contains(javaDir));
        checkContainsFiles("[D] Dir", dirs, results);
        checkContainsFiles("[D] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile1() {
        final List<File> results = new TestFileFinder(dirsFilter, iofilesFilter, -1).find(javaDir);
        assertEquals("[DirAndFile1] Result Size", 1 + dirs.length + ioFiles.length, results.size());
        assertTrue("[DirAndFile1] Start Dir", results.contains(javaDir));
        checkContainsFiles("[DirAndFile1] Dir", dirs, results);
        checkContainsFiles("[DirAndFile1] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile2() {
        final List<File> results = new TestFileFinder(null, null, -1).find(javaDir);
        assertTrue("[DirAndFile2] Result Size", results.size() > 1 + dirs.length + ioFiles.length);
        assertTrue("[DirAndFile2] Start Dir", results.contains(javaDir));
        checkContainsFiles("[DirAndFile2] Dir", dirs, results);
        checkContainsFiles("[DirAndFile2] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile3() {
        final List<File> results = new TestFileFinder(dirsFilter, null, -1).find(javaDir);
        final List<File> resultDirs = directoriesOnly(results);
        assertEquals("[DirAndFile3] Result Size", 1 + dirs.length, resultDirs.size());
        assertTrue("[DirAndFile3] Start Dir", results.contains(javaDir));
        checkContainsFiles("[DirAndFile3] Dir", dirs, resultDirs);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile4() {
        final List<File> results = new TestFileFinder(null, iofilesFilter, -1).find(javaDir);
        final List<File> resultFiles = filesOnly(results);
        assertEquals("[DirAndFile4] Result Size", ioFiles.length, resultFiles.size());
        assertTrue("[DirAndFile4] Start Dir", results.contains(javaDir));
        checkContainsFiles("[DirAndFile4] File", ioFiles, resultFiles);
    }

    /**
     * Test Limiting to current directory
     */
    @Test
    public void testLimitToCurrent() {
        final List<File> results = new TestFileFinder(null, 0).find(current);
        assertEquals("Result Size", 1, results.size());
        assertTrue("Current Dir", results.contains(new File(".")));
    }

    /**
     * test an invalid start directory
     */
    @Test
    public void testMissingStartDirectory() {

        // TODO is this what we want with invalid directory?
        final File invalidDir = new File("invalid-dir");
        final List<File> results = new TestFileFinder(null, -1).find(invalidDir);
        assertEquals("Result Size", 1, results.size());
        assertTrue("Current Dir", results.contains(invalidDir));

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
        assertEquals("Result Size", 0, results.size());

    }

    // ------------ Convenience Test Methods ------------------------------------

    /**
     * Check the files in the array are in the results list.
     */
    private void checkContainsFiles(final String prefix, final File[] files, final Collection<File> results) {
        for (int i = 0; i < files.length; i++) {
            assertTrue(prefix + "["+i+"] " + files[i], results.contains(files[i]));
        }
    }

    private void checkContainsString(final String prefix, final File[] files, final Collection<String> results) {
        for (int i = 0; i < files.length; i++) {
            assertTrue(prefix + "["+i+"] " + files[i], results.contains(files[i].toString()));
        }
    }

    /**
     * Extract the directories.
     */
    private List<File> directoriesOnly(final Collection<File> results) {
        final List<File> list = new ArrayList<>(results.size());
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
    private List<File> filesOnly(final Collection<File> results) {
        final List<File> list = new ArrayList<>(results.size());
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
     */
    @Test
    public void testCancel() {
        String cancelName = null;

        // Cancel on a file
        try {
            cancelName = "DirectoryWalker.java";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals("File:  " + cancelName,   cancelName, cancel.getFile().getName());
            assertEquals("Depth: " + cancelName,  5, cancel.getDepth());
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals("File:  " + cancelName,   cancelName, cancel.getFile().getName());
            assertEquals("Depth: " + cancelName,  3, cancel.getDepth());
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Suppress CancelException (use same file name as preceding test)
        try {
            final List<File> results = new TestCancelWalker(cancelName, true).find(javaDir);
            final File lastFile = results.get(results.size() - 1);
            assertEquals("Suppress:  " + cancelName,   cancelName, lastFile.getName());
        } catch(final IOException ex) {
            fail("Suppress threw " + ex);
        }

    }

    /**
     * Test Cancel
     */
    @Test
    public void testMultiThreadCancel() {
        String cancelName = "DirectoryWalker.java";
        TestMultiThreadCancelWalker walker = new TestMultiThreadCancelWalker(cancelName, false);
        // Cancel on a file
        try {
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            final File last = walker.results.get(walker.results.size() - 1);
            assertEquals(cancelName, last.getName());
            assertEquals("Depth: " + cancelName,  5, cancel.getDepth());
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            walker = new TestMultiThreadCancelWalker(cancelName, false);
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertEquals("File:  " + cancelName,   cancelName, cancel.getFile().getName());
            assertEquals("Depth: " + cancelName,  3, cancel.getDepth());
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Suppress CancelException (use same file name as preceding test)
        try {
            walker = new TestMultiThreadCancelWalker(cancelName, true);
            final List<File> results = walker.find(javaDir);
            final File lastFile = results.get(results.size() - 1);
            assertEquals("Suppress:  " + cancelName, cancelName, lastFile.getName());
        } catch(final IOException ex) {
            fail("Suppress threw " + ex);
        }

    }

    /**
     * Test Filtering
     */
    @Test
    public void testFilterString() {
        final List<String> results = new TestFileFinderString(dirsAndFilesFilter, -1).find(javaDir);
        assertEquals("Result Size", outputFiles.length + ioFiles.length, results.size());
        checkContainsString("IO File", ioFiles, results);
        checkContainsString("Output File", outputFiles, results);
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    private static class TestFileFinder extends DirectoryWalker<File> {

        protected TestFileFinder(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        protected TestFileFinder(final IOFileFilter dirFilter, final IOFileFilter fileFilter, final int depthLimit) {
            super(dirFilter, fileFilter, depthLimit);
        }

        /** find files. */
        protected List<File> find(final File startDirectory) {
           final List<File> results = new ArrayList<>();
           try {
               walk(startDirectory, results);
           } catch(final IOException ex) {
               Assert.fail(ex.toString());
           }
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) {
            results.add(directory);
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) {
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

        /** Always returns false. */
        @Override
        protected boolean handleDirectory(final File directory, final int depth, final Collection<File> results) {
            return false;
        }
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    static class TestCancelWalker extends DirectoryWalker<File> {
        private final String cancelFileName;
        private final boolean suppressCancel;

        TestCancelWalker(final String cancelFileName,final boolean suppressCancel) {
            super();
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /** find files. */
        protected List<File> find(final File startDirectory) throws IOException {
           final List<File> results = new ArrayList<>();
           walk(startDirectory, results);
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) throws IOException {
            results.add(directory);
            if (cancelFileName.equals(directory.getName())) {
                throw new CancelException(directory, depth);
            }
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
            if (cancelFileName.equals(file.getName())) {
                throw new CancelException(file, depth);
            }
        }

        /** Handles Cancel. */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection<File> results,
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
    static class TestMultiThreadCancelWalker extends DirectoryWalker<File> {
        private final String cancelFileName;
        private final boolean suppressCancel;
        private boolean cancelled;
        public List<File> results;

        TestMultiThreadCancelWalker(final String cancelFileName, final boolean suppressCancel) {
            super();
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /** find files. */
        protected List<File> find(final File startDirectory) throws IOException {
           results = new ArrayList<>();
           walk(startDirectory, results);
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) throws IOException {
            results.add(directory);
            assertFalse(cancelled);
            if (cancelFileName.equals(directory.getName())) {
                cancelled = true;
            }
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
            assertFalse(cancelled);
            if (cancelFileName.equals(file.getName())) {
                cancelled = true;
            }
        }

        /** Handles Cancelled. */
        @Override
        protected boolean handleIsCancelled(final File file, final int depth, final Collection<File> results) throws IOException {
            return cancelled;
        }

        /** Handles Cancel. */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection<File> results,
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
    private static class TestFileFinderString extends DirectoryWalker<String> {

        protected TestFileFinderString(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        /** find files. */
        protected List<String> find(final File startDirectory) {
           final List<String> results = new ArrayList<>();
           try {
               walk(startDirectory, results);
           } catch(final IOException ex) {
               Assert.fail(ex.toString());
           }
           return results;
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<String> results) {
            results.add(file.toString());
        }
    }

}
