/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/FilenameUtilsTestCase.java,v 1.3 2003/12/30 07:00:03 bayard Exp $
 * $Revision: 1.3 $
 * $Date: 2003/12/30 07:00:03 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.testtools.FileBasedTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This is used to test FilenameUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @version $Id: FilenameUtilsTestCase.java,v 1.3 2003/12/30 07:00:03 bayard Exp $
 * @see FilenameUtils
 */
public class FilenameUtilsTestCase extends FileBasedTestCase {

    // Test data

    /**
     * Size of test directory.
     */
    private static int TEST_DIRECTORY_SIZE = 0;

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
