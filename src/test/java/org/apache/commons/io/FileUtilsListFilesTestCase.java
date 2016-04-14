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

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.testtools.FileBasedTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for FileUtils.listFiles() methods.
 */
public class FileUtilsListFilesTestCase extends FileBasedTestCase {

    private File getLocalTestDirectory() {
        return new File(getTestDirectory(), "list-files");
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        File dir = getLocalTestDirectory();
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();
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

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        final File dir = getLocalTestDirectory();
        FileUtils.deleteDirectory(dir);
    }

    private Collection<String> filesToFilenames(final Collection<File> files) {
        final Collection<String> filenames = new ArrayList<String>(files.size());
        for (final File file : files) {
            filenames.add(file.getName());
        }
        return filenames;
    }

    private Collection<String> filesToFilenames(final Iterator<File> files) {
        final Collection<String> filenames = new ArrayList<String>();
        while (files.hasNext()) {
            filenames.add(files.next().getName());
        }
        return filenames;
    }

    @Test
    public void testIterateFilesByExtension() throws Exception {
        final String[] extensions = { "xml", "txt" };

        Iterator<File> files = FileUtils.iterateFiles(getLocalTestDirectory(), extensions, false);
        Collection<String> filenames = filesToFilenames(files);
        assertEquals(1, filenames.size());
        assertTrue(filenames.contains("dummy-build.xml"));
        assertFalse(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));

        files = FileUtils.iterateFiles(getLocalTestDirectory(), extensions, true);
        filenames = filesToFilenames(files);
        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("dummy-file.txt"));
        assertFalse(filenames.contains("dummy-index.html"));

        files = FileUtils.iterateFiles(getLocalTestDirectory(), null, false);
        filenames = filesToFilenames(files);
        assertEquals(2, filenames.size());
        assertTrue(filenames.contains("dummy-build.xml"));
        assertTrue(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));
    }

    @Test
    public void testListFilesByExtension() throws Exception {
        final String[] extensions = {"xml", "txt"};

        Collection<File> files = FileUtils.listFiles(getLocalTestDirectory(), extensions, false);
        assertEquals(1, files.size());
        Collection<String> filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertFalse(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));

        files = FileUtils.listFiles(getLocalTestDirectory(), extensions, true);
        filenames = filesToFilenames(files);
        assertEquals(4, filenames.size());
        assertTrue(filenames.contains("dummy-file.txt"));
        assertFalse(filenames.contains("dummy-index.html"));

        files = FileUtils.listFiles(getLocalTestDirectory(), null, false);
        assertEquals(2, files.size());
        filenames = filesToFilenames(files);
        assertTrue(filenames.contains("dummy-build.xml"));
        assertTrue(filenames.contains("README"));
        assertFalse(filenames.contains("dummy-file.txt"));
    }

    @Test
    public void testListFiles() throws Exception {
        Collection<File> files;
        Collection<String> filenames;
        IOFileFilter fileFilter;
        IOFileFilter dirFilter;

        //First, find non-recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, null);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertFalse("'dummy-index.html' shouldn't be found", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        //Second, find recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("CVS"));
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        //Do the same as above but now with the filter coming from FileFilterUtils
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.makeCVSAware(null);
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        //Again with the CVS filter but now with a non-null parameter
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.prefixFileFilter("sub");
        dirFilter = FileFilterUtils.makeCVSAware(dirFilter);
        files = FileUtils.listFiles(getLocalTestDirectory(), fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertTrue("'dummy-build.xml' is missing", filenames.contains("dummy-build.xml"));
        assertTrue("'dummy-index.html' is missing", filenames.contains("dummy-index.html"));
        assertFalse("'Entries' shouldn't be found", filenames.contains("Entries"));

        try {
            FileUtils.listFiles(getLocalTestDirectory(), null, null);
            fail("Expected error about null parameter");
        } catch (final NullPointerException e) {
            // expected
        }
    }


}
