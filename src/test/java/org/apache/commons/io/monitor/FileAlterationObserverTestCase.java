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
package org.apache.commons.io.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.jupiter.api.Test;

/**
 * {@link FileAlterationObserver} Test Case.
 */
public class FileAlterationObserverTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     */
    public FileAlterationObserverTestCase() {
        listener = new CollectionFileListener(true);
    }

    /**
     * Test add/remove listeners.
     */
    @Test
    public void testAddRemoveListeners() {
        final FileAlterationObserver observer = new FileAlterationObserver("/foo");
        // Null Listener
        observer.addListener(null);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[1]");
        observer.removeListener(null);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[2]");

        // Add Listener
        final FileAlterationListenerAdaptor listener = new FileAlterationListenerAdaptor();
        observer.addListener(listener);
        final Iterator<FileAlterationListener> it = observer.getListeners().iterator();
        assertTrue(it.hasNext(), "Listeners[3]");
        assertEquals(listener, it.next(), "Added");
        assertFalse(it.hasNext(), "Listeners[4]");

        // Remove Listener
        observer.removeListener(listener);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[5]");
    }

    /**
     * Test toString().
     */
    @Test
    public void testToString() {
        final File file = new File("/foo");

        FileAlterationObserver observer = new FileAlterationObserver(file);
        assertEquals("FileAlterationObserver[file='" + file.getPath() +  "', listeners=0]",
                observer.toString());

        observer = new FileAlterationObserver(file, CanReadFileFilter.CAN_READ);
        assertEquals("FileAlterationObserver[file='" + file.getPath() +  "', CanReadFileFilter, listeners=0]",
                observer.toString());

        assertEquals(file, observer.getDirectory());
  }

    /**
     * Test checkAndNotify() method
     * @throws Exception
     */
    @Test
    public void testDirectory() throws Exception {
        checkAndNotify();
        checkCollectionsEmpty("A");
        final File testDirA = new File(testDir, "test-dir-A");
        final File testDirB = new File(testDir, "test-dir-B");
        final File testDirC = new File(testDir, "test-dir-C");
        testDirA.mkdir();
        testDirB.mkdir();
        testDirC.mkdir();
        final File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.txt")); // filter should ignore this
        final File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        final File testDirBFile1 = touch(new File(testDirB, "B-file1.java"));

        checkAndNotify();
        checkCollectionSizes("B", 3, 0, 0, 4, 0, 0);
        assertTrue(listener.getCreatedDirectories().contains(testDirA), "B testDirA");
        assertTrue(listener.getCreatedDirectories().contains(testDirB), "B testDirB");
        assertTrue(listener.getCreatedDirectories().contains(testDirC), "B testDirC");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile1), "B testDirAFile1");
        assertFalse(listener.getCreatedFiles().contains(testDirAFile2), "B testDirAFile2");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile3), "B testDirAFile3");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile4), "B testDirAFile4");
        assertTrue(listener.getCreatedFiles().contains(testDirBFile1), "B testDirBFile1");

        checkAndNotify();
        checkCollectionsEmpty("C");

        testDirAFile4 = touch(testDirAFile4);
        FileUtils.deleteDirectory(testDirB);
        checkAndNotify();
        checkCollectionSizes("D", 0, 0, 1, 0, 1, 1);
        assertTrue(listener.getDeletedDirectories().contains(testDirB), "D testDirB");
        assertTrue(listener.getChangedFiles().contains(testDirAFile4), "D testDirAFile4");
        assertTrue(listener.getDeletedFiles().contains(testDirBFile1), "D testDirBFile1");

        FileUtils.deleteDirectory(testDir);
        checkAndNotify();
        checkCollectionSizes("E", 0, 0, 2, 0, 0, 3);
        assertTrue(listener.getDeletedDirectories().contains(testDirA), "E testDirA");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile1), "E testDirAFile1");
        assertFalse(listener.getDeletedFiles().contains(testDirAFile2), "E testDirAFile2");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile3), "E testDirAFile3");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile4), "E testDirAFile4");

        testDir.mkdir();
        checkAndNotify();
        checkCollectionsEmpty("F");

        checkAndNotify();
        checkCollectionsEmpty("G");
    }

    /**
     * Test checkAndNotify() creating
     * @throws Exception
     */
    @Test
    public void testFileCreate() throws Exception {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        File testDirAFile1 =       new File(testDirA, "A-file1.java");
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        File testDirAFile3 =       new File(testDirA, "A-file3.java");
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        File testDirAFile5 =       new File(testDirA, "A-file5.java");

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 2, 0, 0);
        assertFalse(listener.getCreatedFiles().contains(testDirAFile1), "B testDirAFile1");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile2), "B testDirAFile2");
        assertFalse(listener.getCreatedFiles().contains(testDirAFile3), "B testDirAFile3");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile4), "B testDirAFile4");
        assertFalse(listener.getCreatedFiles().contains(testDirAFile5), "B testDirAFile5");

        assertFalse(testDirAFile1.exists(), "B testDirAFile1 exists");
        assertTrue(testDirAFile2.exists(), "B testDirAFile2 exists");
        assertFalse(testDirAFile3.exists(), "B testDirAFile3 exists");
        assertTrue(testDirAFile4.exists(), "B testDirAFile4 exists");
        assertFalse(testDirAFile5.exists(), "B testDirAFile5 exists");

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Create file with name < first entry
        testDirAFile1 = touch(testDirAFile1);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 1, 0, 0);
        assertTrue(testDirAFile1.exists(), "D testDirAFile1 exists");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile1), "D testDirAFile1");

        // Create file with name between 2 entries
        testDirAFile3 = touch(testDirAFile3);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 1, 0, 0);
        assertTrue(testDirAFile3.exists(), "E testDirAFile3 exists");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile3), "E testDirAFile3");

        // Create file with name > last entry
        testDirAFile5 = touch(testDirAFile5);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 1, 0, 0);
        assertTrue(testDirAFile5.exists(), "F testDirAFile5 exists");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile5), "F testDirAFile5");
    }

    /**
     * Test checkAndNotify() creating
     * @throws Exception
     */
    @Test
    public void testFileUpdate() throws Exception {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        File testDirAFile5 = touch(new File(testDirA, "A-file5.java"));

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 5, 0, 0);
        assertTrue(listener.getCreatedFiles().contains(testDirAFile1), "B testDirAFile1");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile2), "B testDirAFile2");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile3), "B testDirAFile3");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile4), "B testDirAFile4");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile5), "B testDirAFile5");

        assertTrue(testDirAFile1.exists(), "B testDirAFile1 exists");
        assertTrue(testDirAFile2.exists(), "B testDirAFile2 exists");
        assertTrue(testDirAFile3.exists(), "B testDirAFile3 exists");
        assertTrue(testDirAFile4.exists(), "B testDirAFile4 exists");
        assertTrue(testDirAFile5.exists(), "B testDirAFile5 exists");

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Update first entry
        testDirAFile1 = touch(testDirAFile1);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 0, 1, 0);
        assertTrue(listener.getChangedFiles().contains(testDirAFile1), "D testDirAFile1");

        // Update file with name between 2 entries
        testDirAFile3 = touch(testDirAFile3);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 0, 1, 0);
        assertTrue(listener.getChangedFiles().contains(testDirAFile3), "E testDirAFile3");

        // Update last entry
        testDirAFile5 = touch(testDirAFile5);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 0, 1, 0);
        assertTrue(listener.getChangedFiles().contains(testDirAFile5), "F testDirAFile5");
    }

    /**
     * Test checkAndNotify() deleting
     * @throws Exception
     */
    @Test
    public void testFileDelete() throws Exception {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        final File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        final File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        final File testDirAFile5 = touch(new File(testDirA, "A-file5.java"));

        assertTrue(testDirAFile1.exists(), "B testDirAFile1 exists");
        assertTrue(testDirAFile2.exists(), "B testDirAFile2 exists");
        assertTrue(testDirAFile3.exists(), "B testDirAFile3 exists");
        assertTrue(testDirAFile4.exists(), "B testDirAFile4 exists");
        assertTrue(testDirAFile5.exists(), "B testDirAFile5 exists");

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 5, 0, 0);
        assertTrue(listener.getCreatedFiles().contains(testDirAFile1), "B testDirAFile1");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile2), "B testDirAFile2");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile3), "B testDirAFile3");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile4), "B testDirAFile4");
        assertTrue(listener.getCreatedFiles().contains(testDirAFile5), "B testDirAFile5");

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Delete first entry
        FileUtils.deleteQuietly(testDirAFile1);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 0, 0, 1);
        assertFalse(testDirAFile1.exists(), "D testDirAFile1 exists");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile1), "D testDirAFile1");

        // Delete file with name between 2 entries
        FileUtils.deleteQuietly(testDirAFile3);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 0, 0, 1);
        assertFalse(testDirAFile3.exists(), "E testDirAFile3 exists");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile3), "E testDirAFile3");

        // Delete last entry
        FileUtils.deleteQuietly(testDirAFile5);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 0, 0, 1);
        assertFalse(testDirAFile5.exists(), "F testDirAFile5 exists");
        assertTrue(listener.getDeletedFiles().contains(testDirAFile5), "F testDirAFile5");
    }

    /**
     * Test checkAndNotify() method
     * @throws Exception
     */
    @Test
    public void testObserveSingleFile() throws Exception {
        final File testDirA = new File(testDir, "test-dir-A");
        File testDirAFile1 = new File(testDirA, "A-file1.java");
        testDirA.mkdir();

        final FileFilter nameFilter = FileFilterUtils.nameFileFilter(testDirAFile1.getName());
        createObserver(testDirA, nameFilter);
        checkAndNotify();
        checkCollectionsEmpty("A");
        assertFalse(testDirAFile1.exists(), "A testDirAFile1 exists");

        // Create
        testDirAFile1 = touch(testDirAFile1);
        File testDirAFile2 = touch(new File(testDirA, "A-file2.txt"));  /* filter should ignore */
        File testDirAFile3 = touch(new File(testDirA, "A-file3.java")); /* filter should ignore */
        assertTrue(testDirAFile1.exists(), "B testDirAFile1 exists");
        assertTrue(testDirAFile2.exists(), "B testDirAFile2 exists");
        assertTrue(testDirAFile3.exists(), "B testDirAFile3 exists");
        checkAndNotify();
        checkCollectionSizes("C", 0, 0, 0, 1, 0, 0);
        assertTrue(listener.getCreatedFiles().contains(testDirAFile1), "C created");
        assertFalse(listener.getCreatedFiles().contains(testDirAFile2), "C created");
        assertFalse(listener.getCreatedFiles().contains(testDirAFile3), "C created");

        // Modify
        testDirAFile1 = touch(testDirAFile1);
        testDirAFile2 = touch(testDirAFile2);
        testDirAFile3 = touch(testDirAFile3);
        checkAndNotify();
        checkCollectionSizes("D", 0, 0, 0, 0, 1, 0);
        assertTrue(listener.getChangedFiles().contains(testDirAFile1), "D changed");
        assertFalse(listener.getChangedFiles().contains(testDirAFile2), "D changed");
        assertFalse(listener.getChangedFiles().contains(testDirAFile3), "D changed");

        // Delete
        FileUtils.deleteQuietly(testDirAFile1);
        FileUtils.deleteQuietly(testDirAFile2);
        FileUtils.deleteQuietly(testDirAFile3);
        assertFalse(testDirAFile1.exists(), "E testDirAFile1 exists");
        assertFalse(testDirAFile2.exists(), "E testDirAFile2 exists");
        assertFalse(testDirAFile3.exists(), "E testDirAFile3 exists");
        checkAndNotify();
        checkCollectionSizes("E", 0, 0, 0, 0, 0, 1);
        assertTrue(listener.getDeletedFiles().contains(testDirAFile1), "E deleted");
        assertFalse(listener.getDeletedFiles().contains(testDirAFile2), "E deleted");
        assertFalse(listener.getDeletedFiles().contains(testDirAFile3), "E deleted");
    }

    /**
     * Call {@link FileAlterationObserver#checkAndNotify()}.
     *
     * @throws Exception if an error occurs
     */
    protected void checkAndNotify() throws Exception {
        observer.checkAndNotify();
    }
}
