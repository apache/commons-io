/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//io/src/test/org/apache/commons/io/FileUtilsTestCase.java,v 1.10 2003/12/30 15:26:59 jeremias Exp $
 * $Revision: 1.10 $
 * $Date: 2003/12/30 15:26:59 $
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
 * This is used to test FileUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @version $Id: FileUtilsTestCase.java,v 1.10 2003/12/30 15:26:59 jeremias Exp $
 * @see FileUtils
 */
public class FileUtilsTestCase extends FileBasedTestCase {

    // Test data

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    private File testFile1;
    private File testFile2;

    private static int testFile1Size;
    private static int testFile2Size;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FileUtilsTestCase.class);
    }

    public FileUtilsTestCase(String name) throws IOException {
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

    // byteCountToDisplaySize

    public void testByteCountToDisplaySize() {
        assertEquals(FileUtils.byteCountToDisplaySize(0), "0 bytes");
        assertEquals(FileUtils.byteCountToDisplaySize(1024), "1 KB");
        assertEquals(FileUtils.byteCountToDisplaySize(1024 * 1024), "1 MB");
        assertEquals(
            FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024),
            "1 GB");
    }

    // waitFor

    public void testWaitFor() {
        FileUtils.waitFor(new File(""), -1);

        FileUtils.waitFor(new File(""), 2);
    }

    // toURL

    public void testToURLs() throws Exception {
        File[] files = new File[] { new File("file1"), new File("file2")};

        URL[] urls = FileUtils.toURLs(files);

        // Path separator causes equality tests to fail
        //assertEquals(urls[0].getFile(), File.separator + files[0].getAbsolutePath());
        //assertEquals(urls[1].getFile(), File.separator + files[1].getAbsolutePath());

    }

    // contentEquals

    public void testContentEquals() throws Exception {
        // Non-existent files
        File file = new File(getTestDirectory(), getName());
        assertTrue(FileUtils.contentEquals(file, file));

        // Directories
        try {
            FileUtils.contentEquals(getTestDirectory(), getTestDirectory());
            fail("Comparing directories should fail with an IOException");
        } catch (IOException ioe) {
            //expected
        }

        // Different files
        File objFile1 =
            new File(getTestDirectory(), getName() + ".object");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
            getClass().getResource("/java/lang/Object.class"),
            objFile1);

        File objFile2 =
            new File(getTestDirectory(), getName() + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(
            getClass().getResource("/java/util/Collection.class"),
            objFile2);

        assertTrue(
            "Files should not be equal.",
            !FileUtils.contentEquals(objFile1, objFile2));

        // Equal files
        file.createNewFile();
        assertTrue(FileUtils.contentEquals(file, file));
    }

    // copyURLToFile

    public void testCopyURLToFile() throws Exception {
        // Creates file
        File file = new File(getTestDirectory(), getName());
        file.deleteOnExit();

        // Loads resource
        String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file);

        // Tests that resuorce was copied correctly
        FileInputStream fis = new FileInputStream(file);
        try {
            assertTrue(
                "Content is not equal.",
                IOUtils.contentEquals(
                    getClass().getResourceAsStream(resourceName),
                    fis));
        } finally {
            fis.close();
        }
    }

    // forceMkdir

    public void testForceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(getTestDirectory());

        // Creates test file
        File testFile = new File(getTestDirectory(), getName());
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertTrue("Test file does not exist.", testFile.exists());

        // Tests with existing file
        try {
            FileUtils.forceMkdir(testFile);
            fail("Exception expected.");
        } catch (IOException ex) {}

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertTrue("Directory was not created.", testFile.exists());
    }

    // sizeOfDirectory

    public void testSizeOfDirectory() throws Exception {
        File file = new File(getTestDirectory(), getName());

        // Non-existent file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (IllegalArgumentException ex) {}

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (IllegalArgumentException ex) {}

        // Existing directory
        file.delete();
        file.mkdir();

        assertEquals(
            "Unexpected directory size",
            TEST_DIRECTORY_SIZE,
            FileUtils.sizeOfDirectory(file));
    }

    // isFileNewer

    // TODO Finish test
    public void XtestIsFileNewer() {}

    // TODO Remove after debugging
    private void log(Object obj) {
        System.out.println(
            FileUtilsTestCase.class +" " + getName() + " " + obj);
    }

    // copyFile

    public void testCopyFile1() throws Exception {
        File destination = new File(getTestDirectory(), "copy1.txt");
        FileUtils.copyFile(testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile1Size);
    }

    public void testCopyFile2() throws Exception {
        File destination = new File(getTestDirectory(), "copy2.txt");
        FileUtils.copyFile(testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
    }

    // forceDelete

    public void testForceDeleteAFile1() throws Exception {
        File destination = new File(getTestDirectory(), "copy1.txt");
        destination.createNewFile();
        assertTrue("Copy1.txt doesn't exist to delete", destination.exists());
        FileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    public void testForceDeleteAFile2() throws Exception {
        File destination = new File(getTestDirectory(), "copy2.txt");
        destination.createNewFile();
        assertTrue("Copy2.txt doesn't exist to delete", destination.exists());
        FileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    // copyFileToDirectory

    public void testCopyFile1ToDir() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        if (!directory.exists())
            directory.mkdirs();
        File destination = new File(directory, testFile1.getName());
        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile1Size);
    }

    public void testCopyFile2ToDir() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        if (!directory.exists())
            directory.mkdirs();
        File destination = new File(directory, testFile1.getName());
        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
    }

    // forceDelete

    public void testForceDeleteDir() throws Exception {
        FileUtils.forceDelete(getTestDirectory().getParentFile());
        assertTrue(
            "Check No Exist",
            !getTestDirectory().getParentFile().exists());
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

    /**
     *  Test the FileUtils implementation.
     */
    // Used to exist as IOTestCase class
    public void testFileUtils() throws Exception {
        // Loads file from classpath
        String path = "/test.txt";
        URL url = this.getClass().getResource(path);
        assertNotNull(path + " was not found.", url);

        String filename = url.getFile();
        //The following line applies a fix for spaces in a path
        filename = replaceAll(filename, "%20", " ");
        String filename2 = "test2.txt";

        assertTrue(
            "test.txt extension == \"txt\"",
            FilenameUtils.getExtension(filename).equals("txt"));

        assertTrue(
            "Test file does not exist: " + filename,
            FilenameUtils.fileExists(filename));

        assertTrue(
            "Second test file does not exist",
            !FilenameUtils.fileExists(filename2));

        FileUtils.writeStringToFile(new File(filename2), filename, "UTF-8");
        assertTrue("Second file was written", FilenameUtils.fileExists(filename2));

        String file2contents = FileUtils.readFileToString(new File(filename2), "UTF-8");
        assertTrue(
            "Second file's contents correct",
            FileUtils.readFileToString(new File(filename2), "UTF-8").equals(file2contents));

        FilenameUtils.fileDelete(filename2);
        assertTrue(
            "Second test file does not exist",
            !FilenameUtils.fileExists(filename2));

        String contents = FileUtils.readFileToString(new File(filename), "UTF-8");
        assertTrue("FileUtils.fileRead()", contents.equals("This is a test"));

    }

}
