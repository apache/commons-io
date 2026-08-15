/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.monitor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link FileEntry}.
 */
class FileEntryTest {

    @TempDir
    protected File testDir;

    @Test
    void testConstructor() {
        final FileEntry current = new FileEntry(FileUtils.current());
        assertEquals(FileUtils.current(), current.getFile());
        assertEquals(FileUtils.current().getName(), current.getName());
        assertEquals(FileUtils.current().getParentFile(), current.getParent());
        assertEquals(0, current.getLevel());
    }

    @Test
    void testConstructorNull() {
        assertThrows(NullPointerException.class, () -> new FileEntry(null));
        assertThrows(NullPointerException.class, () -> new FileEntry(null, null));
        final FileEntry current = new FileEntry(FileUtils.current());
        assertThrows(NullPointerException.class, () -> new FileEntry(current, null));
    }

    @Test
    void testEqualsAndHashCode() {
        final FileEntry fe1 = new FileEntry(FileUtils.current());
        final FileEntry fe2 = new FileEntry(FileUtils.current());
        // Default FileEntry objects should not be equal (no equals() override)
        assertNotEquals(fe1, fe2, "Different FileEntry instances should not be equal by default");
        assertNotEquals(fe1.hashCode(), fe2.hashCode(), "hashCode may differ for different instances");
    }

    @Test
    void testGetChildren() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertArrayEquals(FileEntry.EMPTY_ARRAY, fe.getChildren(), "Default children should be EMPTY_ARRAY");
    }

    @Test
    void testGetChildrenWithChildren() {
        final FileEntry parent = new FileEntry(FileUtils.current());
        final FileEntry child = parent.newChildInstance(new File("child.txt"));
        parent.setChildren(child);
        assertArrayEquals(new FileEntry[] { child }, parent.getChildren());
    }

    @Test
    void testGetChildrenWithMultipleChildren() {
        final FileEntry parent = new FileEntry(FileUtils.current());
        final FileEntry child1 = parent.newChildInstance(new File("child1.txt"));
        final FileEntry child2 = parent.newChildInstance(new File("child2.txt"));
        parent.setChildren(child1, child2);
        final FileEntry[] children = parent.getChildren();
        assertEquals(2, children.length);
        assertEquals(child1, children[0]);
        assertEquals(child2, children[1]);
    }

    @Test
    void testGetFile() {
        final File expectedFile = FileUtils.current();
        final FileEntry fe = new FileEntry(expectedFile);
        assertEquals(expectedFile, fe.getFile());
    }

    @Test
    void testGetLastModified() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(0, fe.getLastModified(), "Default lastModified should be 0 (epoch)");
    }

    @Test
    void testGetLastModifiedFileTime() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(FileTimes.EPOCH, fe.getLastModifiedFileTime(), "Default lastModifiedFileTime should be EPOCH");
    }

    @Test
    void testGetLength() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(0, fe.getLength(), "Default length should be 0");
    }

    @Test
    void testGetLevel() {
        // Test root entry (no parent)
        final FileEntry root = new FileEntry(FileUtils.current());
        assertEquals(0, root.getLevel(), "Root entry should have level 0");
        // Test child entry (has parent)
        final FileEntry child = root.newChildInstance(new File("child.txt"));
        assertEquals(1, child.getLevel(), "Child entry should have level 1");
        // Test grandchild entry (has parent with level 1)
        final FileEntry grandchild = child.newChildInstance(new File("grandchild.txt"));
        assertEquals(2, grandchild.getLevel(), "Grandchild entry should have level 2");
    }

    @Test
    void testGetName() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(FileUtils.current().getName(), fe.getName());
    }

    @Test
    void testGetParent() {
        // Test root entry (no parent)
        final FileEntry root = new FileEntry(FileUtils.current());
        assertEquals(null, root.getParent(), "Root entry should have null parent");
        // Test child entry (has parent)
        final FileEntry child = root.newChildInstance(new File("child.txt"));
        assertEquals(root, child.getParent(), "Child entry should have root as parent");
    }

    @Test
    void testIsDirectory() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertFalse(fe.isDirectory(), "Default isDirectory should be false");
        // Test setting directory flag
        fe.setDirectory(true);
        assertTrue(fe.isDirectory(), "isDirectory should return true after setDirectory(true)");
    }

    @Test
    void testIsExists() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertFalse(fe.isExists(), "Default isExists should be false");
        // Test setting exists flag
        fe.setExists(true);
        assertTrue(fe.isExists(), "isExists should return true after setExists(true)");
    }

    @Test
    void testNewChildInstance() {
        final FileEntry parent = new FileEntry(FileUtils.current());
        final File childFile = new File("child.txt");
        final FileEntry child = parent.newChildInstance(childFile);
        assertEquals(childFile, child.getFile());
        assertEquals(parent, child.getParent());
        assertEquals(childFile.getName(), child.getName());
        assertEquals(1, child.getLevel());
        assertFalse(child.isDirectory());
        assertFalse(child.isExists());
        assertEquals(0, child.getLength());
        assertEquals(0, child.getLastModified());
    }

    @Test
    void testNewChildInstance_EmptyArrayReturned() {
        final FileEntry parent = new FileEntry(FileUtils.current());
        final FileEntry[] children = parent.getChildren();
        assertSame(FileEntry.EMPTY_ARRAY, children, "getChildren() should return the shared EMPTY_ARRAY");
    }

    @Test
    void testRefresh_Directory() throws IOException {
        final File testDirFile = Files.createDirectory(testDir.toPath().resolve("testDir")).toFile();
        final FileEntry fe = new FileEntry(testDirFile);
        assertTrue(fe.refresh(testDirFile), "refresh() should return true for directory");
        assertTrue(fe.isExists(), "Directory should exist after refresh");
        assertTrue(fe.isDirectory(), "Directory should have isDirectory=true after refresh");
        assertEquals(0, fe.getLength(), "Directory should have length=0 after refresh");
    }

    @Test
    void testRefresh_ExistingFile() throws Exception {
        final File testFile = Files.createFile(testDir.toPath().resolve("test.txt")).toFile();
        final FileEntry fe = new FileEntry(testFile);
        // Initial state - entry was just created, refresh should pick up actual file state
        assertTrue(fe.refresh(testFile), "refresh() should return true for new file state");
        assertTrue(fe.isExists(), "File should exist after refresh");
        assertTrue(fe.getLastModifiedFileTime() != null);
        // Modify the file and refresh again
        Thread.sleep(100); // Ensure time difference
        Files.write(testFile.toPath(), "content".getBytes());
        assertTrue(fe.refresh(testFile), "refresh() should return true for modified file");
    }

    @Test
    void testRefresh_NoChange() throws IOException {
        final File testFile = Files.createFile(testDir.toPath().resolve("test.txt")).toFile();
        final FileEntry fe = new FileEntry(testFile);
        // Refresh once to capture initial state
        fe.refresh(testFile);
        // Refresh again immediately (no changes)
        assertFalse(fe.refresh(testFile), "refresh() should return false when no changes");
    }

    @Test
    void testRefresh_NonExistingFile() throws IOException {
        final File testFile = new File(testDir, "nonExisting.txt");
        final FileEntry fe = new FileEntry(testFile);
        // Refresh for non-existing file
        assertFalse(fe.refresh(testFile), "refresh() should return false for non-existing file");
        assertFalse(fe.isExists(), "Non-existing file should have isExists=false after refresh");
        assertFalse(fe.isDirectory(), "Non-existing file should have isDirectory=false after refresh");
        assertEquals(0, fe.getLength(), "Non-existing file should have length=0 after refresh");
        assertEquals(FileTimes.EPOCH, fe.getLastModifiedFileTime(), "Non-existing file should have epoch as lastModified after refresh");
    }

    @Test
    void testSerializable() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertArrayEquals(fe.getChildren(), SerializationUtils.roundtrip(fe).getChildren());
        assertEquals(fe.getClass(), SerializationUtils.roundtrip(fe).getClass());
        assertEquals(fe.getFile(), SerializationUtils.roundtrip(fe).getFile());
        assertEquals(fe.getLastModified(), SerializationUtils.roundtrip(fe).getLastModified());
        assertEquals(fe.getLastModifiedFileTime(), SerializationUtils.roundtrip(fe).getLastModifiedFileTime());
        assertEquals(fe.getLength(), SerializationUtils.roundtrip(fe).getLength());
        assertEquals(fe.getLevel(), SerializationUtils.roundtrip(fe).getLevel());
        assertEquals(fe.getName(), SerializationUtils.roundtrip(fe).getName());
        assertEquals(fe.getParent(), SerializationUtils.roundtrip(fe).getParent());
    }

    @Test
    void testSetChildren() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        // Test setting children with single child
        final FileEntry child = fe.newChildInstance(new File("child.txt"));
        fe.setChildren(child);
        assertArrayEquals(new FileEntry[] { child }, fe.getChildren());
        // Test setting children with multiple children
        final FileEntry child2 = fe.newChildInstance(new File("child2.txt"));
        fe.setChildren(child, child2);
        assertArrayEquals(new FileEntry[] { child, child2 }, fe.getChildren());
        // Test setting children to null (should become EMPTY_ARRAY)
        fe.setChildren(null);
        assertArrayEquals(FileEntry.EMPTY_ARRAY, fe.getChildren(), "setChildren(null) should set EMPTY_ARRAY");
        // Test setting empty children array
        fe.setChildren();
        assertArrayEquals(FileEntry.EMPTY_ARRAY, fe.getChildren(), "setChildren() should set EMPTY_ARRAY");
    }

    @Test
    void testSetDirectory() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertFalse(fe.isDirectory(), "Default isDirectory should be false");
        fe.setDirectory(true);
        assertTrue(fe.isDirectory(), "isDirectory should return true after setDirectory(true)");
        fe.setDirectory(false);
        assertFalse(fe.isDirectory(), "isDirectory should return false after setDirectory(false)");
    }

    @Test
    void testSetExists() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertFalse(fe.isExists(), "Default isExists should be false");
        fe.setExists(true);
        assertTrue(fe.isExists(), "isExists should return true after setExists(true)");
        fe.setExists(false);
        assertFalse(fe.isExists(), "isExists should return false after setExists(false)");
    }

    @Test
    void testSetLastModifiedFileTime() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(FileTimes.EPOCH, fe.getLastModifiedFileTime(), "Default lastModifiedFileTime should be EPOCH");
        final FileTime newTime = FileTime.fromMillis(1234567890L);
        fe.setLastModified(newTime);
        assertEquals(newTime, fe.getLastModifiedFileTime(), "lastModifiedFileTime should be updated");
        assertEquals(1234567890L, fe.getLastModified(), "getLastModified() should return the set value in millis");
        // Test setting to null should throw
        assertThrows(NullPointerException.class, () -> fe.setLastModified((FileTime) null));
    }

    @Test
    void testSetLastModifiedMillis() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(0, fe.getLastModified(), "Default lastModified should be 0");
        fe.setLastModified(1234567890L);
        assertEquals(1234567890L, fe.getLastModified(), "getLastModified() should return the set value");
        assertEquals(FileTime.fromMillis(1234567890L), fe.getLastModifiedFileTime(), "getLastModifiedFileTime() should match");
    }

    @Test
    void testSetLength() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals(0, fe.getLength(), "Default length should be 0");
        fe.setLength(1024);
        assertEquals(1024, fe.getLength(), "Length should be updated");
        fe.setLength(0);
        assertEquals(0, fe.getLength(), "Length should be settable to 0");
        fe.setLength(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, fe.getLength(), "Length should handle Long.MAX_VALUE");
    }

    @Test
    void testSetName() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        fe.setName("test");
        assertEquals("test", fe.getName());
        assertThrows(NullPointerException.class, () -> fe.setName(null));
    }

    @Test
    void testToString() {
        final FileEntry fe = new FileEntry(FileUtils.current());
        assertEquals("FileEntry[" + fe.getFile() + "]", fe.toString());
    }

    @Test
    void testToString_WithFile() {
        final File testFile = new File("/path/to/test.txt");
        final FileEntry fe = new FileEntry(testFile);
        assertEquals("FileEntry[/path/to/test.txt]", fe.toString());
    }
}
