/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
import java.io.IOException;

import org.apache.commons.io.testtools.FileBasedTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This is used to test FilenameUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @author Martin Cooper
 * @version $Id: FilenameUtilsTestCase.java,v 1.15 2004/10/30 22:43:21 scolebourne Exp $
 * @see FilenameUtils
 */
public class FilenameUtilsTestCase extends FileBasedTestCase {

    private File testFile1;
    private File testFile2;

    private static int testFile1Size;
    private static int testFile2Size;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FilenameUtilsTestCase.class);
    }

    public FilenameUtilsTestCase(String name) throws IOException {
        super(name);

        testFile1 = new File(getTestDirectory(), "file1-test.txt");
        testFile2 = new File(getTestDirectory(), "file1a-test.txt");

        testFile1Size = (int)testFile1.length();
        testFile2Size = (int)testFile2.length();
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
        FileUtils.deleteDirectory(getTestDirectory());
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    // removePath

    public void testRemovePath() {
        String fileName =
            FilenameUtils.removePath(
                new File(getTestDirectory(), getName()).getAbsolutePath());
        assertEquals(getName(), fileName);
    }

    // getPath

    public void testGetPath() {
        String fileName =
            FilenameUtils.getPath(
                new File(getTestDirectory(), getName()).getAbsolutePath());
        assertEquals(getTestDirectory().getAbsolutePath(), fileName);
    }

    // catPath

    public void testCatPath() {
        // TODO StringIndexOutOfBoundsException thrown if file doesn't contain slash.
        // Is this acceptable?
        //assertEquals("", FilenameUtils.catPath("a", "b"));

        assertEquals("/a" + File.separator + "c", FilenameUtils.catPath("/a/b", "c"));
        assertEquals("/a" + File.separator + "d", FilenameUtils.catPath("/a/b/c", "../d"));
        assertEquals("C:\\a" + File.separator + "c", FilenameUtils.catPath("C:\\a\\b", "c"));
        assertEquals("C:\\a" + File.separator + "d", FilenameUtils.catPath("C:\\a\\b\\c", "../d"));
    }

    // resolveFile

    public void testResolveFileDotDot() throws Exception {
        File file = FilenameUtils.resolveFile(getTestDirectory(), "..");
        assertEquals(
            "Check .. operator",
            file,
            getTestDirectory().getParentFile());
    }

    public void testResolveFileDot() throws Exception {
        File file = FilenameUtils.resolveFile(getTestDirectory(), ".");
        assertEquals("Check . operator", file, getTestDirectory());
    }

    // normalize

    public void testNormalize() throws Exception {
        String[] src =
            {
                "",
                "/",
                "///",
                "/foo",
                "/foo//",
                "/./",
                "/foo/./",
                "/foo/./bar",
                "/foo/../bar",
                "/foo/../bar/../baz",
                "/foo/bar/../../baz",
                "/././",
                "/foo/./../bar",
                "/foo/.././bar/",
                "//foo//./bar",
                "/../",
                "/foo/../../" };

        String[] dest =
            {
                "",
                "/",
                "/",
                "/foo",
                "/foo/",
                "/",
                "/foo/",
                "/foo/bar",
                "/bar",
                "/baz",
                "/baz",
                "/",
                "/bar",
                "/bar/",
                "/foo/bar",
                null,
                null };

        assertEquals("Oops, test writer goofed", src.length, dest.length);

        for (int i = 0; i < src.length; i++) {
            assertEquals(
                "Check if '" + src[i] + "' normalized to '" + dest[i] + "'",
                dest[i],
                FilenameUtils.normalize(src[i]));
        }
    }

    private String replaceAll(
        String text,
        String lookFor,
        String replaceWith) {
        StringBuffer sb = new StringBuffer(text);
        while (true) {
            int idx = sb.toString().indexOf(lookFor);
            if (idx < 0) {
                break;
            }
            sb.replace(idx, idx + lookFor.length(), replaceWith);
        }
        return sb.toString();
    }

    public void testGetExtension() {
        String[][] tests = {
                { "filename.ext", "ext" }, 
                { "README", "" }, 
                { "domain.dot.com", "com" }, 
                { "image.jpeg", "jpeg" },
                { "a.b/c", "" },
                { "a.b/c.txt", "txt" },
                { "a/b/c", "" },
        };
        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FilenameUtils.getExtension(tests[i][0]));
            //assertEquals(tests[i][1], FilenameUtils.extension(tests[i][0]));
        }
    }

    public void testGetExtensionWithPaths() {
        String[][] testsWithPaths =
            { { "/tmp/foo/filename.ext", "ext" }, {
                "C:\\temp\\foo\\filename.ext", "ext" }, {
                "/tmp/foo.bar/filename.ext", "ext" }, {
                "C:\\temp\\foo.bar\\filename.ext", "ext" }, {
                "/tmp/foo.bar/README", "" }, {
                "C:\\temp\\foo.bar\\README", "" }, {
                "../filename.ext", "ext" }
        };
        for (int i = 0; i < testsWithPaths.length; i++) {
            assertEquals(
                testsWithPaths[i][1],
                FilenameUtils.getExtension(testsWithPaths[i][0]));
            //assertEquals(testsWithPaths[i][1], FilenameUtils.extension(testsWithPaths[i][0]));
        }
    }

    public void testRemoveExtension() {
        String[][] tests = { 
                { "filename.ext", "filename" }, 
                { "first.second.third.ext", "first.second.third" }, 
                { "README", "README" }, 
                { "domain.dot.com", "domain.dot" }, 
                { "image.jpeg", "image" },
                { "a.b/c", "a.b/c" },
                { "a.b/c.txt", "a.b/c" },
                { "a/b/c", "a/b/c" },
        };

        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FilenameUtils.removeExtension(tests[i][0]));
            //assertEquals(tests[i][1], FilenameUtils.basename(tests[i][0]));
        }
    }

    public void testRemoveExtensionWithPaths() {
        String[][] testsWithPaths =
            { { "/tmp/foo/filename.ext", "/tmp/foo/filename" }, {
                "C:\\temp\\foo\\filename.ext", "C:\\temp\\foo\\filename" }, {
                "/tmp/foo.bar/filename.ext", "/tmp/foo.bar/filename" }, {
                "C:\\temp\\foo.bar\\filename.ext", "C:\\temp\\foo.bar\\filename" }, {
                "/tmp/foo.bar/README", "/tmp/foo.bar/README" }, {
                "C:\\temp\\foo.bar\\README", "C:\\temp\\foo.bar\\README" }, {
                "../filename.ext", "../filename" }
        };

        for (int i = 0; i < testsWithPaths.length; i++) {
            assertEquals(
                testsWithPaths[i][1],
                FilenameUtils.removeExtension(testsWithPaths[i][0]));
            //assertEquals(testsWithPaths[i][1], FilenameUtils.basename(testsWithPaths[i][0]));
        }
    }

    //-----------------------------------------------------------------------
    public void testSeparatorsToUnix() {
        assertEquals(null, FilenameUtils.separatorsToUnix(null));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b/c"));
        assertEquals("/a/b/c.txt", FilenameUtils.separatorsToUnix("/a/b/c.txt"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b\\c"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("\\a\\b\\c"));
        assertEquals("D:/a/b/c", FilenameUtils.separatorsToUnix("D:\\a\\b\\c"));
    }

    public void testSeparatorsToWindows() {
        assertEquals(null, FilenameUtils.separatorsToWindows(null));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b\\c"));
        assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToWindows("\\a\\b\\c.txt"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b/c"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("/a/b/c"));
        assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToWindows("D:/a/b/c"));
    }

    public void testSeparatorsToSystem() {
        if (File.separatorChar == '/') {
            assertEquals(null, FilenameUtils.separatorsToSystem(null));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("/a/b/c.txt", FilenameUtils.separatorsToSystem("/a/b/c.txt"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b\\c"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("D:/a/b/c", FilenameUtils.separatorsToSystem("D:\\a\\b\\c"));
        } else {
            assertEquals(null, FilenameUtils.separatorsToSystem(null));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToSystem("\\a\\b\\c.txt"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b/c"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToSystem("D:/a/b/c"));
        }
    }

    //-----------------------------------------------------------------------
    public void testIndexOfLastSeparator() {
        assertEquals(-1, FilenameUtils.indexOfLastSeparator(null));
        assertEquals(-1, FilenameUtils.indexOfLastSeparator("noseperator.inthispath"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a/b/c"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a\\b\\c"));
    }

    public void testIndexOfExtension() {
        assertEquals(-1, FilenameUtils.indexOfExtension(null));
        assertEquals(-1, FilenameUtils.indexOfExtension("file"));
        assertEquals(4, FilenameUtils.indexOfExtension("file.txt"));
        assertEquals(13, FilenameUtils.indexOfExtension("a.txt/b.txt/c.txt"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b\\c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b.notextension/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b.notextension\\c"));
    }

}
