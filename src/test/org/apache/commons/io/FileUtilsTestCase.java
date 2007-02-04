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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.filefilter.WildcardFilter;
import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test FileUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Jim Harrington
 * @version $Id$
 * @see FileUtils
 */
public class FileUtilsTestCase extends FileBasedTestCase {

    // Test data

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;
    
    /** Delay in milliseconds to make sure test for "last modified date" are accurate */
    //private static final int LAST_MODIFIED_DELAY = 600;

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

    //-----------------------------------------------------------------------
    public void test_openInputStream_exists() throws Exception {
        File file = new File(getTestDirectory(), "test.txt");
        createLineBasedFile(file, new String[] {"Hello"});
        FileInputStream in = null;
        try {
            in = FileUtils.openInputStream(file);
            assertEquals('H', in.read());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void test_openInputStream_existsButIsDirectory() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        directory.mkdirs();
        FileInputStream in = null;
        try {
            in = FileUtils.openInputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void test_openInputStream_notExists() throws Exception {
        File directory = new File(getTestDirectory(), "test.txt");
        FileInputStream in = null;
        try {
            in = FileUtils.openInputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    //-----------------------------------------------------------------------
    void openOutputStream_noParent(boolean createFile) throws Exception {
        File file = new File("test.txt");
        assertNull(file.getParentFile());
        try {
            if (createFile) {
            createLineBasedFile(file, new String[]{"Hello"});}
            FileOutputStream out = null;
            try {
                out = FileUtils.openOutputStream(file);
                out.write(0);
            } finally {
                IOUtils.closeQuietly(out);
            }
            assertEquals(true, file.exists());
        } finally {
            if (file.delete() == false) {
                file.deleteOnExit();
            }
        }
    }

    public void test_openOutputStream_noParentCreateFile() throws Exception {
        openOutputStream_noParent(true);
    }

    public void test_openOutputStream_noParentNoFile() throws Exception {
        openOutputStream_noParent(false);
    }


    public void test_openOutputStream_exists() throws Exception {
        File file = new File(getTestDirectory(), "test.txt");
        createLineBasedFile(file, new String[] {"Hello"});
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(file);
            out.write(0);
        } finally {
            IOUtils.closeQuietly(out);
        }
        assertEquals(true, file.exists());
    }

    public void test_openOutputStream_existsButIsDirectory() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        directory.mkdirs();
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void test_openOutputStream_notExists() throws Exception {
        File file = new File(getTestDirectory(), "a/test.txt");
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(file);
            out.write(0);
        } finally {
            IOUtils.closeQuietly(out);
        }
        assertEquals(true, file.exists());
    }

    public void test_openOutputStream_notExistsCannotCreate() throws Exception {
        // according to Wikipedia, most filing systems have a 256 limit on filename
        String longStr =
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz";  // 300 chars
        File file = new File(getTestDirectory(), "a/" + longStr + "/test.txt");
        FileOutputStream out = null;
        try {
            out = FileUtils.openOutputStream(file);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    //-----------------------------------------------------------------------
    // byteCountToDisplaySize
    public void testByteCountToDisplaySize() {
        assertEquals(FileUtils.byteCountToDisplaySize(0), "0 bytes");
        assertEquals(FileUtils.byteCountToDisplaySize(1024), "1 KB");
        assertEquals(FileUtils.byteCountToDisplaySize(1024 * 1024), "1 MB");
        assertEquals(
            FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024),
            "1 GB");
    }

    //-----------------------------------------------------------------------
    public void testToFile1() throws Exception {
        URL url = new URL("file", null, "a/b/c/file.txt");
        File file = FileUtils.toFile(url);
        assertEquals(true, file.toString().indexOf("file.txt") >= 0);
    }

    public void testToFile2() throws Exception {
        URL url = new URL("file", null, "a/b/c/file%20n%61me.tx%74");
        File file = FileUtils.toFile(url);
        assertEquals(true, file.toString().indexOf("file name.txt") >= 0);
    }

    public void testToFile3() throws Exception {
        assertEquals(null, FileUtils.toFile((URL) null));
        assertEquals(null, FileUtils.toFile(new URL("http://jakarta.apache.org")));
    }

    public void testToFile4() throws Exception {
        URL url = new URL("file", null, "a/b/c/file%2Xn%61me.txt");
        try {
            FileUtils.toFile(url);
            fail();
        }  catch (IllegalArgumentException ex) {}
    }

    // toFiles

    public void testToFiles1() throws Exception {
        URL[] urls = new URL[] {
            new URL("file", null, "file1.txt"),
            new URL("file", null, "file2.txt"),
        };
        File[] files = FileUtils.toFiles(urls);
        
        assertEquals(urls.length, files.length);
        assertEquals("File: " + files[0], true, files[0].toString().indexOf("file1.txt") >= 0);
        assertEquals("File: " + files[1], true, files[1].toString().indexOf("file2.txt") >= 0);
    }

    public void testToFiles2() throws Exception {
        URL[] urls = new URL[] {
            new URL("file", null, "file1.txt"),
            null,
        };
        File[] files = FileUtils.toFiles(urls);
        
        assertEquals(urls.length, files.length);
        assertEquals("File: " + files[0], true, files[0].toString().indexOf("file1.txt") >= 0);
        assertEquals("File: " + files[1], null, files[1]);
    }

    public void testToFiles3() throws Exception {
        URL[] urls = null;
        File[] files = FileUtils.toFiles(urls);
        
        assertEquals(0, files.length);
    }

    public void testToFiles4() throws Exception {
        URL[] urls = new URL[] {
            new URL("file", null, "file1.txt"),
            new URL("http", "jakarta.apache.org", "file1.txt"),
        };
        try {
            FileUtils.toFiles(urls);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    // toURLs

    public void testToURLs1() throws Exception {
        File[] files = new File[] {
            new File(getTestDirectory(), "file1.txt"),
            new File(getTestDirectory(), "file2.txt"),
        };
        URL[] urls = FileUtils.toURLs(files);
        
        assertEquals(files.length, urls.length);
        assertEquals(true, urls[0].toExternalForm().startsWith("file:"));
        assertEquals(true, urls[0].toExternalForm().indexOf("file1.txt") >= 0);
        assertEquals(true, urls[1].toExternalForm().startsWith("file:"));
        assertEquals(true, urls[1].toExternalForm().indexOf("file2.txt") >= 0);
    }

//    public void testToURLs2() throws Exception {
//        File[] files = new File[] {
//            new File(getTestDirectory(), "file1.txt"),
//            null,
//        };
//        URL[] urls = FileUtils.toURLs(files);
//        
//        assertEquals(files.length, urls.length);
//        assertEquals(true, urls[0].toExternalForm().startsWith("file:"));
//        assertEquals(true, urls[0].toExternalForm().indexOf("file1.txt") > 0);
//        assertEquals(null, urls[1]);
//    }
//
//    public void testToURLs3() throws Exception {
//        File[] files = null;
//        URL[] urls = FileUtils.toURLs(files);
//        
//        assertEquals(0, urls.length);
//    }

    // contentEquals

    public void testContentEquals() throws Exception {
        // Non-existent files
        File file = new File(getTestDirectory(), getName());
        File file2 = new File(getTestDirectory(), getName() + "2");
        // both don't  exist
        assertTrue(FileUtils.contentEquals(file, file));
        assertTrue(FileUtils.contentEquals(file, file2));
        assertTrue(FileUtils.contentEquals(file2, file2));
        assertTrue(FileUtils.contentEquals(file2, file));

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

        File objFile1b =
            new File(getTestDirectory(), getName() + ".object2");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
            getClass().getResource("/java/lang/Object.class"),
            objFile1b);

        File objFile2 =
            new File(getTestDirectory(), getName() + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(
            getClass().getResource("/java/util/Collection.class"),
            objFile2);

        assertEquals(false, FileUtils.contentEquals(objFile1, objFile2));
        assertEquals(false, FileUtils.contentEquals(objFile1b, objFile2));
        assertEquals(true, FileUtils.contentEquals(objFile1, objFile1b));

        assertEquals(true, FileUtils.contentEquals(objFile1, objFile1));
        assertEquals(true, FileUtils.contentEquals(objFile1b, objFile1b));
        assertEquals(true, FileUtils.contentEquals(objFile2, objFile2));

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertEquals(true, FileUtils.contentEquals(file, file));
        assertEquals(true, FileUtils.contentEquals(file, file2));
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

    // isFileNewer / isFileOlder
    public void testIsFileNewerOlder() throws Exception {
        File reference   = new File(getTestDirectory(), "FileUtils-reference.txt");
        File oldFile     = new File(getTestDirectory(), "FileUtils-old.txt");
        File newFile     = new File(getTestDirectory(), "FileUtils-new.txt");
        File invalidFile = new File(getTestDirectory(), "FileUtils-invalid-file.txt");

        // Create Files
        createFile(oldFile, 0);

        do {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // ignore
            }
            createFile(reference, 0);
        } while( oldFile.lastModified() == reference.lastModified() );

        Date date = new Date();
        long now = date.getTime();

        do {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                // ignore
            }
            createFile(newFile, 0);
        } while( reference.lastModified() == newFile.lastModified() );

        // Test isFileNewer()
        assertFalse("Old File - Newer - File", FileUtils.isFileNewer(oldFile, reference));
        assertFalse("Old File - Newer - Date", FileUtils.isFileNewer(oldFile, date));
        assertFalse("Old File - Newer - Mili", FileUtils.isFileNewer(oldFile, now));
        assertTrue("New File - Newer - File", FileUtils.isFileNewer(newFile, reference));
        assertTrue("New File - Newer - Date", FileUtils.isFileNewer(newFile, date));
        assertTrue("New File - Newer - Mili", FileUtils.isFileNewer(newFile, now));
        assertFalse("Invalid - Newer - File", FileUtils.isFileNewer(invalidFile, reference));
        
        // Test isFileOlder()
        assertTrue("Old File - Older - File", FileUtils.isFileOlder(oldFile, reference));
        assertTrue("Old File - Older - Date", FileUtils.isFileOlder(oldFile, date));
        assertTrue("Old File - Older - Mili", FileUtils.isFileOlder(oldFile, now));
        assertFalse("New File - Older - File", FileUtils.isFileOlder(newFile, reference));
        assertFalse("New File - Older - Date", FileUtils.isFileOlder(newFile, date));
        assertFalse("New File - Older - Mili", FileUtils.isFileOlder(newFile, now));
        assertFalse("Invalid - Older - File", FileUtils.isFileOlder(invalidFile, reference));
        
        
        // ----- Test isFileNewer() exceptions -----
        // Null File
        try {
            FileUtils.isFileNewer(null, now);
            fail("Newer Null, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Null reference File
        try {
            FileUtils.isFileNewer(oldFile, (File)null);
            fail("Newer Null reference, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Invalid reference File
        try {
            FileUtils.isFileNewer(oldFile, invalidFile);
            fail("Newer invalid reference, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Null reference Date
        try {
            FileUtils.isFileNewer(oldFile, (Date)null);
            fail("Newer Null date, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }


        // ----- Test isFileOlder() exceptions -----
        // Null File
        try {
            FileUtils.isFileOlder(null, now);
            fail("Older Null, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Null reference File
        try {
            FileUtils.isFileOlder(oldFile, (File)null);
            fail("Older Null reference, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Invalid reference File
        try {
            FileUtils.isFileOlder(oldFile, invalidFile);
            fail("Older invalid reference, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }
        
        // Null reference Date
        try {
            FileUtils.isFileOlder(oldFile, (Date)null);
            fail("Older Null date, expected IllegalArgumentExcepion");
        } catch (IllegalArgumentException expected) {
            // expected result
        }

    }

//    // TODO Remove after debugging
//    private void log(Object obj) {
//        System.out.println(
//            FileUtilsTestCase.class +" " + getName() + " " + obj);
//    }

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
        
        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if 
        //the lastModified date is not ok
        
        FileUtils.copyFile(testFile1, destination);
        assertTrue("Check Exist", destination.exists());
        assertTrue("Check Full copy", destination.length() == testFile2Size);
        /* disabled: Thread.sleep doesn't work reliably for this case
        assertTrue("Check last modified date preserved", 
            testFile1.lastModified() == destination.lastModified());*/
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

    public void testCopyDirectoryToDirectory_NonExistingDest() throws Exception {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = getTestDirectory();
        File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        File actualDestDir = new File(destDir, srcDir.getName());
        
        FileUtils.copyDirectoryToDirectory(srcDir, destDir);
        
        assertTrue("Check exists", destDir.exists());
        assertTrue("Check exists", actualDestDir.exists());
        assertEquals("Check size", FileUtils.sizeOfDirectory(srcDir), FileUtils.sizeOfDirectory(actualDestDir));
        assertEquals(true, new File(actualDestDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    public void testCopyDirectoryToNonExistingDest() throws Exception {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = getTestDirectory();
        File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        
        FileUtils.copyDirectory(srcDir, destDir);
        
        assertTrue("Check exists", destDir.exists());
        assertEquals("Check size", FileUtils.sizeOfDirectory(srcDir), FileUtils.sizeOfDirectory(destDir));
        assertEquals(true, new File(destDir, "sub/A.txt").exists());
        FileUtils.deleteDirectory(destDir);
    }

    public void testCopyDirectoryToExistingDest() throws Exception {
        createFile(testFile1, 1234);
        createFile(testFile2, 4321);
        File srcDir = getTestDirectory();
        File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        destDir.mkdirs();
        
        FileUtils.copyDirectory(srcDir, destDir);
        
        assertEquals(FileUtils.sizeOfDirectory(srcDir), FileUtils.sizeOfDirectory(destDir));
        assertEquals(true, new File(destDir, "sub/A.txt").exists());
    }

    public void testCopyDirectoryErrors() throws Exception {
        try {
            FileUtils.copyDirectory(null, null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            FileUtils.copyDirectory(new File("a"), null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            FileUtils.copyDirectory(null, new File("a"));
            fail();
        } catch (NullPointerException ex) {}
        try {
            FileUtils.copyDirectory(new File("doesnt-exist"), new File("a"));
            fail();
        } catch (IOException ex) {}
        try {
            FileUtils.copyDirectory(testFile1, new File("a"));
            fail();
        } catch (IOException ex) {}
        try {
            FileUtils.copyDirectory(getTestDirectory(), testFile1);
            fail();
        } catch (IOException ex) {}
        try {
            FileUtils.copyDirectory(getTestDirectory(), getTestDirectory());
            fail();
        } catch (IOException ex) {}
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
        File testDirectory = getTestDirectory();
        FileUtils.forceDelete(testDirectory.getParentFile());
        assertTrue(
            "Check No Exist",
            !testDirectory.getParentFile().exists());
    }

    /**
     *  Test the FileUtils implementation.
     */
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

        FileUtils.writeStringToFile(file2, filename, "UTF-8");
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        String file2contents = FileUtils.readFileToString(file2, "UTF-8");
        assertTrue(
            "Second file's contents correct",
            filename.equals(file2contents));

        assertTrue(file2.delete());
        
        String contents = FileUtils.readFileToString(new File(filename), "UTF-8");
        assertTrue("FileUtils.fileRead()", contents.equals("This is a test"));

    }

    public void testTouch() throws IOException {
        File file = new File(getTestDirectory(), "touch.txt") ;
        if (file.exists()) {
            file.delete();
        }
        assertTrue("Bad test: test file still exists", !file.exists());
        FileUtils.touch(file);
        assertTrue("FileUtils.touch() created file", file.exists());
        FileOutputStream out = new FileOutputStream(file) ;
        assertEquals("Created empty file.", 0, file.length());
        out.write(0) ;
        out.close();
        assertEquals("Wrote one byte to file", 1, file.length());
        long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        boolean res = file.setLastModified(y2k);  // 0L fails on Win98
        assertEquals("Bad test: set lastModified failed", true, res);
        assertEquals("Bad test: set lastModified set incorrect value", y2k, file.lastModified());
        long now = System.currentTimeMillis();
        FileUtils.touch(file) ;
        assertEquals("FileUtils.touch() didn't empty the file.", 1, file.length());
        assertEquals("FileUtils.touch() changed lastModified", false, y2k == file.lastModified());
        assertEquals("FileUtils.touch() changed lastModified to more than now-3s", true, file.lastModified() >= (now - 3000));
        assertEquals("FileUtils.touch() changed lastModified to less than now+3s", true, file.lastModified() <= (now + 3000));
    }

    public void testListFiles() throws Exception {
        File srcDir = getTestDirectory();
        File subDir = new File(srcDir, "list_test" );
        subDir.mkdir();

        String[] fileNames = {"a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"};
        int[] fileSizes = {123, 234, 345, 456, 678, 789};

        for (int i = 0; i < fileNames.length; ++i) {
            File theFile = new File(subDir, fileNames[i]);
            createFile(theFile, fileSizes[i]);
        }

        Collection files = FileUtils.listFiles(subDir,
                                               new WildcardFilter("*.*"),
                                               new WildcardFilter("*"));

        int count = files.size();
        Object[] fileObjs = files.toArray();

        assertEquals(files.size(), fileNames.length);

        Map foundFileNames = new HashMap();

        for (int i = 0; i < count; ++i) {
            boolean found = false;
            for(int j = 0; (( !found ) && (j < fileNames.length)); ++j) {
                if ( fileNames[j].equals(((File) fileObjs[i]).getName())) {
                    foundFileNames.put(fileNames[j], fileNames[j]);
                    found = true;
                }
            }
        }

        assertEquals(foundFileNames.size(), fileNames.length);

        subDir.delete();
    }

    public void testIterateFiles() throws Exception {
        File srcDir = getTestDirectory();
        File subDir = new File(srcDir, "list_test" );
        subDir.mkdir();

        String[] fileNames = {"a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"};
        int[] fileSizes = {123, 234, 345, 456, 678, 789};

        for (int i = 0; i < fileNames.length; ++i) {
            File theFile = new File(subDir, fileNames[i]);
            createFile(theFile, fileSizes[i]);
        }

        Iterator files = FileUtils.iterateFiles(subDir,
                                                new WildcardFilter("*.*"),
                                                new WildcardFilter("*"));

        Map foundFileNames = new HashMap();

        while (files.hasNext()) {
            boolean found = false;
            String fileName = ((File) files.next()).getName();

            for (int j = 0; (( !found ) && (j < fileNames.length)); ++j) {
                if ( fileNames[j].equals(fileName)) {
                    foundFileNames.put(fileNames[j], fileNames[j]);
                    found = true;
                }
            }
        }

        assertEquals(foundFileNames.size(), fileNames.length);

        subDir.delete();
    }

    public void testReadFileToString() throws Exception {
        File file = new File(getTestDirectory(), "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes("UTF8");
        out.write(text);
        out.close();
        
        String data = FileUtils.readFileToString(file, "UTF8");
        assertEquals("Hello /u1234", data);
    }

    public void testReadFileToByteArray() throws Exception {
        File file = new File(getTestDirectory(), "read.txt");
        FileOutputStream out = new FileOutputStream(file);
        out.write(11);
        out.write(21);
        out.write(31);
        out.close();
        
        byte[] data = FileUtils.readFileToByteArray(file);
        assertEquals(3, data.length);
        assertEquals(11, data[0]);
        assertEquals(21, data[1]);
        assertEquals(31, data[2]);
    }

    public void testReadLines() throws Exception {
        File file = newFile("lines.txt");
        try {
            String[] data = new String[] {"hello", "/u1234", "", "this is", "some text"};
            createLineBasedFile(file, data);
            
            List lines = FileUtils.readLines(file, "UTF-8");
            assertEquals(Arrays.asList(data), lines);
        } finally {
            deleteFile(file);
        }
    }

    public void testWriteStringToFile1() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", "UTF8");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    public void testWriteStringToFile2() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", null);
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    public void testWriteByteArrayToFile() throws Exception {
        File file = new File(getTestDirectory(), "write.obj");
        byte[] data = new byte[] {11, 21, 31};
        FileUtils.writeByteArrayToFile(file, data);
        assertEqualContent(data, file);
    }

    public void testWriteLines_4arg() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        File file = newFile("lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, "*");
        
        String expected = "hello*world**this is**some text*";
        String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    public void testWriteLines_4arg_Writer_nullData() throws Exception {
        File file = newFile("lines.txt");
        FileUtils.writeLines(file, "US-ASCII", (List) null, "*");
        
        assertEquals("Sizes differ", 0, file.length());
    }

    public void testWriteLines_4arg_nullSeparator() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        File file = newFile("lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, null);
        
        String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR +
            IOUtils.LINE_SEPARATOR + "this is" + IOUtils.LINE_SEPARATOR +
            IOUtils.LINE_SEPARATOR + "some text" + IOUtils.LINE_SEPARATOR;
        String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    public void testWriteLines_3arg_nullSeparator() throws Exception {
        Object[] data = new Object[] {
            "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        List list = Arrays.asList(data);
        
        File file = newFile("lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list);
        
        String expected = "hello" + IOUtils.LINE_SEPARATOR + "world" + IOUtils.LINE_SEPARATOR +
            IOUtils.LINE_SEPARATOR + "this is" + IOUtils.LINE_SEPARATOR +
            IOUtils.LINE_SEPARATOR + "some text" + IOUtils.LINE_SEPARATOR;
        String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertEquals(expected, actual);
    }

    //-----------------------------------------------------------------------
    public void testChecksumCRC32() throws Exception {
        // create a test file
        String text = "Imagination is more important than knowledge - Einstein";
        File file = new File(getTestDirectory(), "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");
        
        // compute the expected checksum
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes("US-ASCII"), 0, text.length());
        long expectedValue = expectedChecksum.getValue();
        
        // compute the checksum of the file
        long resultValue = FileUtils.checksumCRC32(file);
        
        assertEquals(expectedValue, resultValue);
    }

    public void testChecksum() throws Exception {
        // create a test file
        String text = "Imagination is more important than knowledge - Einstein";
        File file = new File(getTestDirectory(), "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");
        
        // compute the expected checksum
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes("US-ASCII"), 0, text.length());
        long expectedValue = expectedChecksum.getValue();
        
        // compute the checksum of the file
        Checksum testChecksum = new CRC32();
        Checksum resultChecksum = FileUtils.checksum(file, testChecksum);
        long resultValue = resultChecksum.getValue();
        
        assertSame(testChecksum, resultChecksum);
        assertEquals(expectedValue, resultValue);
    }

    public void testChecksumOnNullFile() throws Exception {
        try {
            FileUtils.checksum((File) null, new CRC32());
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testChecksumOnNullChecksum() throws Exception {
        // create a test file
        String text = "Imagination is more important than knowledge - Einstein";
        File file = new File(getTestDirectory(), "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");
        try {
            FileUtils.checksum(file, (Checksum) null);
            fail();
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testChecksumOnDirectory() throws Exception {
        try {
            FileUtils.checksum(new File("."), new CRC32());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testChecksumDouble() throws Exception {
        // create a test file
        String text1 = "Imagination is more important than knowledge - Einstein";
        File file1 = new File(getTestDirectory(), "checksum-test.txt");
        FileUtils.writeStringToFile(file1, text1, "US-ASCII");
        
        // create a second test file
        String text2 = "To be or not to be - Shakespeare";
        File file2 = new File(getTestDirectory(), "checksum-test2.txt");
        FileUtils.writeStringToFile(file2, text2, "US-ASCII");
        
        // compute the expected checksum
        Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text1.getBytes("US-ASCII"), 0, text1.length());
        expectedChecksum.update(text2.getBytes("US-ASCII"), 0, text2.length());
        long expectedValue = expectedChecksum.getValue();
        
        // compute the checksum of the file
        Checksum testChecksum = new CRC32();
        FileUtils.checksum(file1, testChecksum);
        FileUtils.checksum(file2, testChecksum);
        long resultValue = testChecksum.getValue();
        
        assertEquals(expectedValue, resultValue);
    }

}
