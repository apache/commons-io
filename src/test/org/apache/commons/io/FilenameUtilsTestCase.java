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
 * @version $Id: FilenameUtilsTestCase.java,v 1.5 2004/02/23 05:02:25 bayard Exp $
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

    // mkdir

    public void testMkdir() {
        File dir = new File(getTestDirectory(), "testdir");
        FilenameUtils.mkdir(dir.getAbsolutePath());
        dir.deleteOnExit();
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

        assertEquals("/a/c", FilenameUtils.catPath("/a/b", "c"));
        assertEquals("/a/d", FilenameUtils.catPath("/a/b/c", "../d"));
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
            int idx = sb.indexOf(lookFor);
            if (idx < 0) {
                break;
            }
            sb.replace(idx, idx + lookFor.length(), replaceWith);
        }
        return sb.toString();
    }

    public void testGetExtension() {
        String[][] tests = { { "filename.ext", "ext" }, {
                "README", "" }, {
                "domain.dot.com", "com" }, {
                "image.jpeg", "jpeg" }
        };
        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FilenameUtils.getExtension(tests[i][0]));
            //assertEquals(tests[i][1], FilenameUtils.extension(tests[i][0]));
        }
    }

    /* TODO: Reenable this test */
    public void DISABLED__testGetExtensionWithPaths() {
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
        String[][] tests = { { "filename.ext", "filename" }, {
                "first.second.third.ext", "first.second.third" }, {
                "README", "README" }, {
                "domain.dot.com", "domain.dot" }, {
                "image.jpeg", "image" }
        };

        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FilenameUtils.removeExtension(tests[i][0]));
            //assertEquals(tests[i][1], FilenameUtils.basename(tests[i][0]));
        }
    }

    /* TODO: Reenable this test */
    public void DISABLED__testRemoveExtensionWithPaths() {
        String[][] testsWithPaths =
            { { "/tmp/foo/filename.ext", "filename" }, {
                "C:\\temp\\foo\\filename.ext", "filename" }, {
                "/tmp/foo.bar/filename.ext", "filename" }, {
                "C:\\temp\\foo.bar\\filename.ext", "filename" }, {
                "/tmp/foo.bar/README", "README" }, {
                "C:\\temp\\foo.bar\\README", "README" }, {
                "../filename.ext", "filename" }
        };

        for (int i = 0; i < testsWithPaths.length; i++) {
            assertEquals(
                testsWithPaths[i][1],
                FilenameUtils.removeExtension(testsWithPaths[i][0]));
            //assertEquals(testsWithPaths[i][1], FilenameUtils.basename(testsWithPaths[i][0]));
        }
    }

}
