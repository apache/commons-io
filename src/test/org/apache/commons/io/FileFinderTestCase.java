/*
 * Copyright 2005-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;

public class FileFinderTestCase extends TestCase {

    // Directories
    private static final File javaDir      = new File("src/java");
    private static final File orgDir       = new File(javaDir, "org");
    private static final File apacheDir    = new File(orgDir, "apache");
    private static final File commonsDir   = new File(apacheDir, "commons");
    private static final File ioDir        = new File(commonsDir, "io");
    private static final File outputDir    = new File(ioDir, "output");
    private static final File[] dirs       = new File[] {orgDir, apacheDir, commonsDir, ioDir, outputDir};

    // Files
    private static final File copyUtils     = new File(ioDir, "CopyUtils.java");
    private static final File ioUtils       = new File(ioDir, "IOUtils.java");
    private static final File proxyWriter   = new File(outputDir, "ProxyWriter.java");
    private static final File nullStream    = new File(outputDir, "NullOutputStream.java");
    private static final File[] ioFiles     = new File[] {copyUtils, ioUtils};
    private static final File[] outputFiles = new File[] {proxyWriter, nullStream};
    
    // Filters
    private static final IOFileFilter dirsFilter        = createNameFilter(dirs);
    private static final IOFileFilter iofilesFilter     = createNameFilter(ioFiles);
    private static final IOFileFilter outputFilesFilter = createNameFilter(outputFiles);
    private static final IOFileFilter ioDirAndFilesFilter = new OrFileFilter(dirsFilter, iofilesFilter);
    private static final IOFileFilter dirsAndFilesFilter = new OrFileFilter(ioDirAndFilesFilter, outputFilesFilter);

    // Filter to exclude SVN files
    private static final IOFileFilter NOT_SVN = FileFilterUtils.makeSVNAware(null);

    public static Test suite() {
        return new TestSuite(FileFinderTestCase.class);
    }

    /** Construct the TestCase using the name */
    public FileFinderTestCase(String name) {
        super(name);
    }

    /** Set Up */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** Tear Down */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //-----------------------------------------------------------------------

    /**
     * Test Filtering
     */
    public void testFilter() {
        List results = new FileFinder(dirsAndFilesFilter).find(javaDir);
        assertEquals("Result Size", (1 + dirs.length + ioFiles.length + outputFiles.length), results.size());
        assertTrue("Start Dir", results.contains(javaDir));
        checkContainsFiles("Dir", dirs, results);
        checkContainsFiles("IO File", ioFiles, results);
        checkContainsFiles("Output File", outputFiles, results);
    }

    /**
     * Test Filtering and limit to depth 0
     */
    public void testFilterAndLimitA() {
        List results = new FileFinder(NOT_SVN, 0).find(javaDir);
        assertEquals("[A] Result Size", 1, results.size());
        assertTrue("[A] Start Dir",   results.contains(javaDir));
    }

    /**
     * Test Filtering and limit to depth 1
     */
    public void testFilterAndLimitB() {
        List results = new FileFinder(NOT_SVN, 1).find(javaDir);
        assertEquals("[B] Result Size", 2, results.size());
        assertTrue("[B] Start Dir",   results.contains(javaDir));
        assertTrue("[B] Org Dir",     results.contains(orgDir));
    }

    /**
     * Test Filtering and limit to depth 3
     */
    public void testFilterAndLimitC() {
        List results = new FileFinder(NOT_SVN, 3).find(javaDir);
        assertEquals("[A] Result Size", 4, results.size());
        assertTrue("[A] Start Dir",   results.contains(javaDir));
        assertTrue("[A] Org Dir",     results.contains(orgDir));
        assertTrue("[A] Apache Dir",  results.contains(apacheDir));
        assertTrue("[A] Commons Dir", results.contains(commonsDir));
    }

    /**
     * Test Filtering and limit to depth 5
     */
    public void testFilterAndLimitD() {
        List results = new FileFinder(dirsAndFilesFilter, 5).find(javaDir);
        assertEquals("[D] Result Size", (1 + dirs.length + ioFiles.length), results.size());
        assertTrue("[D] Start Dir", results.contains(javaDir));
        checkContainsFiles("[D] Dir", dirs, results);
        checkContainsFiles("[D] File", ioFiles, results);
    }

    /**
     * Test Limiting to current directory
     */
    public void testLimitToCurrent() {
        List results = new FileFinder(0).find();
        assertEquals("Result Size", 1, results.size());
        assertTrue("Current Dir", results.contains(new File(".")));
    }

    /**
     * test an invalid start directory
     */
    public void testMissingStartDirectory() {
        try {
            FileFinder.ALL_FILES.find(new File("invalid-dir"));
            fail("Invalid start directory didn't throw Exception");
        } catch (IllegalArgumentException ignore) {
            // expected result
        }
        try {
            FileFinder.ALL_FILES.find(null);
            fail("Null start directory didn't throw Exception");
        } catch (IllegalArgumentException ignore) {
            // expected result
        }
    }

    /**
     * Check the files in the array are in the results list.
     */
    private void checkContainsFiles(String prefix, File[] files, List results) {
        for (int i = 0; i < files.length; i++) {
            assertTrue(prefix + "["+i+"] " + files[i], results.contains(files[i]));
        }
    }

    /**
     * Create an name filter containg the names of the files
     * in the array.
     */
    private static IOFileFilter createNameFilter(File[] files) {
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return new NameFileFilter(names);
    }

}
