/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/FileUtilsTestCase.java,v 1.2 2003/08/21 18:56:12 jeremias Exp $
 * $Revision: 1.2 $
 * $Date: 2003/08/21 18:56:12 $
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.testtools.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This is used to test FileUtils for correctness.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 */
public final class FileUtilsTestCase extends FileBasedTestCase {

    // Test data
    private static final int FILE1_SIZE = 1;
    private static final int FILE2_SIZE = 1024 * 4 + 1;

    private final File m_testFile1;
    private final File m_testFile2;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FileUtilsTestCase.class);
    }

    public FileUtilsTestCase(final String name) throws IOException {
        super(name);

        m_testFile1 = new File(getTestDirectory(), "file1-test.txt");
        m_testFile2 = new File(getTestDirectory(), "file1a-test.txt");
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
        createFile(m_testFile1, FILE1_SIZE);
        createFile(m_testFile2, FILE2_SIZE);
        FileUtils.deleteDirectory(getTestDirectory());
        getTestDirectory().mkdirs();
        createFile(m_testFile1, FILE1_SIZE);
        createFile(m_testFile2, FILE2_SIZE);
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    public void testCopyFile1() throws Exception {
        final File destination = new File(getTestDirectory(), "copy1.txt");
        FileUtils.copyFile(m_testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == FILE1_SIZE);
    }

    public void testCopyFile2() throws Exception {
        final File destination = new File(getTestDirectory(), "copy2.txt");
        FileUtils.copyFile(m_testFile2, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == FILE2_SIZE);
    }

    public void testForceDeleteAFile1() throws Exception {
        final File destination = new File(getTestDirectory(), "copy1.txt");
        destination.createNewFile();
        assertTrue("Copy1.txt doesn't exist to delete", destination.exists());
        FileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    public void testForceDeleteAFile2() throws Exception {
        final File destination = new File(getTestDirectory(), "copy2.txt");
        destination.createNewFile();
        assertTrue("Copy2.txt doesn't exist to delete", destination.exists());
        FileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    public void testCopyFile1ToDir() throws Exception {
        final File directory = new File(getTestDirectory(), "subdir");
        if (!directory.exists())
            directory.mkdirs();
        final File destination = new File(directory, m_testFile1.getName());
        FileUtils.copyFileToDirectory(m_testFile1, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == FILE1_SIZE);
    }

    public void testCopyFile2ToDir() throws Exception {
        final File directory = new File(getTestDirectory(), "subdir");
        if (!directory.exists())
            directory.mkdirs();
        final File destination = new File(directory, m_testFile2.getName());
        FileUtils.copyFileToDirectory(m_testFile2, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == FILE2_SIZE);
    }

    public void testForceDeleteDir() throws Exception {
        FileUtils.forceDelete(getTestDirectory().getParentFile());
        assertTrue("Check No Exist", !getTestDirectory().getParentFile().exists());
    }

    public void testResolveFileDotDot() throws Exception {
        final File file = FileUtils.resolveFile(getTestDirectory(), "..");
        assertEquals(
            "Check .. operator",
            file,
            getTestDirectory().getParentFile());
    }

    public void testResolveFileDot() throws Exception {
        final File file = FileUtils.resolveFile(getTestDirectory(), ".");
        assertEquals("Check . operator", file, getTestDirectory());
    }

    public void testNormalize() throws Exception {
        final String[] src =
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

        final String[] dest =
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
                FileUtils.normalize(src[i]));
        }
    }

    private String replaceAll(String text, String lookFor, String replaceWith) {
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

    /**
     *  Test the FileUtils implementation.
     */
    /// Used to exist as IOTestCase class
    public void testFileUtils() throws Exception {
        // Loads file from classpath
        final String path = "/test.txt";
        final URL url = this.getClass().getResource(path);
        assertNotNull(path + " was not found.", url);

        String filename = url.getFile();
        //The following line applies a fix for spaces in a path
        filename = replaceAll(filename, "%20", " ");
        final String filename2 = "test2.txt";

        assertTrue(
            "test.txt extension == \"txt\"",
            FileUtils.getExtension(filename).equals("txt"));

        assertTrue("Test file does not exist: " + filename, FileUtils.fileExists(filename));

        assertTrue(
            "Second test file does not exist",
            !FileUtils.fileExists(filename2));

        FileUtils.fileWrite(filename2, filename);
        assertTrue("Second file was written", FileUtils.fileExists(filename2));

        final String file2contents = FileUtils.fileRead(filename2);
        assertTrue(
            "Second file's contents correct",
            FileUtils.fileRead(filename2).equals(file2contents));

        FileUtils.fileDelete(filename2);
        assertTrue(
            "Second test file does not exist",
            !FileUtils.fileExists(filename2));

        final String contents = FileUtils.fileRead(filename);
        assertTrue("FileUtils.fileRead()", contents.equals("This is a test"));

    }

    public void testGetExtension() {
        final String[][] tests = {
            {"filename.ext", "ext"},
            {"README", ""},
            {"domain.dot.com", "com"},
            {"image.jpeg", "jpeg"}};
        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FileUtils.getExtension(tests[i][0]));
            //assertEquals(tests[i][1], FileUtils.extension(tests[i][0]));
        }
    }
    
    /* TODO: Reenable this test */
    public void DISABLED__testGetExtensionWithPaths() {
        final String[][] testsWithPaths = {
            {"/tmp/foo/filename.ext", "ext"},
            {"C:\\temp\\foo\\filename.ext", "ext"},
            {"/tmp/foo.bar/filename.ext", "ext"},
            {"C:\\temp\\foo.bar\\filename.ext", "ext"},
            {"/tmp/foo.bar/README", ""},
            {"C:\\temp\\foo.bar\\README", ""},
            {"../filename.ext", "ext"}};
        for (int i = 0; i < testsWithPaths.length; i++) {
            assertEquals(testsWithPaths[i][1], FileUtils.getExtension(testsWithPaths[i][0]));
            //assertEquals(testsWithPaths[i][1], FileUtils.extension(testsWithPaths[i][0]));
        }
    }

    public void testRemoveExtension() {
        final String[][] tests = {
            {"filename.ext", "filename"},
            {"first.second.third.ext", "first.second.third"},
            {"README", "README"},
            {"domain.dot.com", "domain.dot"},
            {"image.jpeg", "image"}};
                                
        for (int i = 0; i < tests.length; i++) {
            assertEquals(tests[i][1], FileUtils.removeExtension(tests[i][0]));
            //assertEquals(tests[i][1], FileUtils.basename(tests[i][0]));
        }
    }
    
    /* TODO: Reenable this test */
    public void DISABLED__testRemoveExtensionWithPaths() {
        final String[][] testsWithPaths = {
            {"/tmp/foo/filename.ext", "filename"},
            {"C:\\temp\\foo\\filename.ext", "filename"},
            {"/tmp/foo.bar/filename.ext", "filename"},
            {"C:\\temp\\foo.bar\\filename.ext", "filename"},
            {"/tmp/foo.bar/README", "README"},
            {"C:\\temp\\foo.bar\\README", "README"},
            {"../filename.ext", "filename"}};

        for (int i = 0; i < testsWithPaths.length; i++) {
            assertEquals(testsWithPaths[i][1], FileUtils.removeExtension(testsWithPaths[i][0]));
            //assertEquals(testsWithPaths[i][1], FileUtils.basename(testsWithPaths[i][0]));
        }
    }

}

