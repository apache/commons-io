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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
 * @version $Id: FileUtilsTestCase.java,v 1.18 2004/04/23 22:47:39 jeremias Exp $
 * @see FileUtils
 */
public class FileUtilsTestCase extends FileBasedTestCase {

    // Test data

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;
    
    /** Delay in milliseconds to make sure test for "last modified date" are accurate */
    private static final int LAST_MODIFIED_DELAY = 600;

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
        //TODO Maybe test copy to itself like for copyFile()
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
        
        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFile(testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile1Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved", 
            testFile1.lastModified() == destination.lastModified());*/  
    }

    public void testCopyFile2() throws Exception {
        File destination = new File(getTestDirectory(), "copy2.txt");
        
        Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFile(testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
        assertTrue("Check last modified date preserved", 
            testFile1.lastModified() == destination.lastModified());    
    }
    
    public void testCopyToSelf() throws Exception {
        File destination = new File(getTestDirectory(), "copy3.txt");
        //Prepare a test file
        FileUtils.copyFile(testFile1, destination);
        
        try {
            FileUtils.copyFile(destination, destination);
            fail("file copy to self should not be possible");
        } catch (IOException ioe) {
            //we want the exception, copy to self should be illegal
        }
    }

    public void testCopyFile2WithoutFileDatePreservation() throws Exception {
        File destination = new File(getTestDirectory(), "copy2.txt");
        
        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFile(testFile1, destination, false);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date modified", 
            testFile1.lastModified() != destination.lastModified());*/    
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
        
        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile1Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved", 
            testFile1.lastModified() == destination.lastModified());*/
            
        try {
            FileUtils.copyFileToDirectory(destination, directory);
            fail("Should not be able to copy a file into the same directory as itself");    
        } catch (IOException ioe) {
            //we want that, cannot copy to the same directory as the original file
        }
    }

    public void testCopyFile2ToDir() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        if (!directory.exists())
            directory.mkdirs();
        File destination = new File(directory, testFile1.getName());
        
        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFileToDirectory(testFile1, directory);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved", 
            testFile1.lastModified() == destination.lastModified());*/    
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
            int idx = sb.toString().indexOf(lookFor);
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
        File file1 = new File(getTestDirectory(), "test.txt");
        String filename = file1.getAbsolutePath();
        
        //Create test file on-the-fly (used to be in CVS)
        OutputStream out = new java.io.FileOutputStream(file1);
        try {
            out.write("This is a test".getBytes("UTF-8"));
        } finally {
            out.close();
        }
        
        File file2 = new File(getTestDirectory(), "test2.txt");
        String filename2 = file2.getAbsolutePath();

//1.0 These lines commented out as FilenameUtils not in 1.0
//1.0        assertTrue(
//1.0            "test.txt extension == \"txt\"",
//1.0            FilenameUtils.getExtension(filename).equals("txt"));

//1.0        assertTrue(
//1.0            "Test file does not exist: " + filename,
//1.0            FilenameUtils.fileExists(filename));

//1.0        assertTrue(
//1.0            "Second test file does not exist",
//1.0            !FilenameUtils.fileExists(filename2));

        FileUtils.writeStringToFile(file2, filename, "UTF-8");
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        String file2contents = FileUtils.readFileToString(file2, "UTF-8");
        assertTrue(
            "Second file's contents correct",
            filename.equals(file2contents));

        assertTrue(file2.delete());
        
//1.0        FilenameUtils.fileDelete(filename2);
//1.0        assertTrue(
//1.0            "Second test file does not exist",
//1.0            !FilenameUtils.fileExists(filename2));

        String contents = FileUtils.readFileToString(new File(filename), "UTF-8");
        assertTrue("FileUtils.fileRead()", contents.equals("This is a test"));

    }

}
