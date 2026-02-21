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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link FilenameUtils}.
 */
class FilenameUtilsTest {

    private static final String DRIVE_C = "C:";

    private static final String SEP = "" + File.separatorChar;

    private static final boolean WINDOWS = File.separatorChar == '\\';

    @TempDir
    public Path temporaryFolder;

    private Path testFile1;
    private Path testFile2;

    private int testFile1Size;
    private int testFile2Size;

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = Files.createTempFile(temporaryFolder, "test", "1");
        testFile2 = Files.createTempFile(temporaryFolder, "test", "2");

        testFile1Size = (int) Files.size(testFile1);
        testFile2Size = (int) Files.size(testFile2);
        if (!Files.exists(testFile1.getParent())) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output3 =
                new BufferedOutputStream(Files.newOutputStream(testFile1))) {
            TestUtils.generateTestData(output3, testFile1Size);
        }
        if (!Files.exists(testFile2.getParent())) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output2 =
                new BufferedOutputStream(Files.newOutputStream(testFile2))) {
            TestUtils.generateTestData(output2, testFile2Size);
        }
        if (!Files.exists(testFile1.getParent())) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output1 =
                new BufferedOutputStream(Files.newOutputStream(testFile1))) {
            TestUtils.generateTestData(output1, testFile1Size);
        }
        if (!Files.exists(testFile2.getParent())) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (BufferedOutputStream output =
                new BufferedOutputStream(Files.newOutputStream(testFile2))) {
            TestUtils.generateTestData(output, testFile2Size);
        }
    }

    @Test
    void testConcat() {
        assertNull(FilenameUtils.concat("", null));
        assertNull(FilenameUtils.concat(null, null));
        assertNull(FilenameUtils.concat(null, ""));
        assertNull(FilenameUtils.concat(null, "a"));
        assertEquals(SEP + "a", FilenameUtils.concat(null, "/a"));

        assertNull(FilenameUtils.concat("", ":")); // invalid prefix
        assertNull(FilenameUtils.concat(":", "")); // invalid prefix

        assertEquals("f" + SEP, FilenameUtils.concat("", "f/"));
        assertEquals("f", FilenameUtils.concat("", "f"));
        assertEquals("a" + SEP + "f" + SEP, FilenameUtils.concat("a/", "f/"));
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a", "f"));
        assertEquals("a" + SEP + "b" + SEP + "f" + SEP, FilenameUtils.concat("a/b/", "f/"));
        assertEquals("a" + SEP + "b" + SEP + "f", FilenameUtils.concat("a/b", "f"));

        assertEquals("a" + SEP + "f" + SEP, FilenameUtils.concat("a/b/", "../f/"));
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a/b", "../f"));
        assertEquals("a" + SEP + "c" + SEP + "g" + SEP, FilenameUtils.concat("a/b/../c/", "f/../g/"));
        assertEquals("a" + SEP + "c" + SEP + "g", FilenameUtils.concat("a/b/../c", "f/../g"));

        assertEquals("a" + SEP + "c.txt" + SEP + "f", FilenameUtils.concat("a/c.txt", "f"));

        assertEquals(SEP + "f" + SEP, FilenameUtils.concat("", "/f/"));
        assertEquals(SEP + "f", FilenameUtils.concat("", "/f"));
        assertEquals(SEP + "f" + SEP, FilenameUtils.concat("a/", "/f/"));
        assertEquals(SEP + "f", FilenameUtils.concat("a", "/f"));

        assertEquals(SEP + "c" + SEP + "d", FilenameUtils.concat("a/b/", "/c/d"));
        assertEquals("C:c" + SEP + "d", FilenameUtils.concat("a/b/", "C:c/d"));
        assertEquals(DRIVE_C + SEP + "c" + SEP + "d", FilenameUtils.concat("a/b/", "C:/c/d"));
        assertEquals("~" + SEP + "c" + SEP + "d", FilenameUtils.concat("a/b/", "~/c/d"));
        assertEquals("~user" + SEP + "c" + SEP + "d", FilenameUtils.concat("a/b/", "~user/c/d"));
        assertEquals("~" + SEP, FilenameUtils.concat("a/b/", "~"));
        assertEquals("~user" + SEP, FilenameUtils.concat("a/b/", "~user"));
    }

    @Test
    void testDirectoryContains() {
        assertTrue(FilenameUtils.directoryContains("/foo", "/foo/bar"));
        assertTrue(FilenameUtils.directoryContains("/foo/", "/foo/bar"));
        assertTrue(FilenameUtils.directoryContains("C:\\foo", "C:\\foo\\bar"));
        assertTrue(FilenameUtils.directoryContains("C:\\foo\\", "C:\\foo\\bar"));

        assertFalse(FilenameUtils.directoryContains("/foo", "/foo"));
        assertFalse(FilenameUtils.directoryContains("/foo", "/foobar"));
        assertFalse(FilenameUtils.directoryContains("C:\\foo", "C:\\foobar"));
        assertFalse(FilenameUtils.directoryContains("/foo", null));
        assertFalse(FilenameUtils.directoryContains("", ""));
        assertFalse(FilenameUtils.directoryContains("", "/foo"));
        assertFalse(FilenameUtils.directoryContains("/foo", ""));
    }

    @Test
    void testEquals() {
        assertTrue(FilenameUtils.equals(null, null));
        assertFalse(FilenameUtils.equals(null, ""));
        assertFalse(FilenameUtils.equals("", null));
        assertTrue(FilenameUtils.equals("", ""));
        assertTrue(FilenameUtils.equals("file.txt", "file.txt"));
        assertFalse(FilenameUtils.equals("file.txt", "FILE.TXT"));
        assertFalse(FilenameUtils.equals("a\\b\\file.txt", "a/b/file.txt"));
    }

    @Test
    void testEquals_fullControl() {
        assertFalse(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SENSITIVE));
        assertTrue(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.INSENSITIVE));
        assertEquals(WINDOWS, FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SYSTEM));
        assertFalse(FilenameUtils.equals("file.txt", "FILE.TXT", true, null));
    }

    @Test
    void testEqualsNormalized() {
        assertTrue(FilenameUtils.equalsNormalized(null, null));
        assertFalse(FilenameUtils.equalsNormalized(null, ""));
        assertFalse(FilenameUtils.equalsNormalized("", null));
        assertTrue(FilenameUtils.equalsNormalized("", ""));
        assertTrue(FilenameUtils.equalsNormalized("file.txt", "file.txt"));
        assertFalse(FilenameUtils.equalsNormalized("file.txt", "FILE.TXT"));
        assertTrue(FilenameUtils.equalsNormalized("a\\b\\file.txt", "a/b/file.txt"));
        assertFalse(FilenameUtils.equalsNormalized("a/b/", "a/b"));
    }

    /**
     * Test for https://issues.apache.org/jira/browse/IO-128
     */
    @Test
    void testEqualsNormalizedError_IO_128() {
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "file.txt"));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("file.txt", "//file.txt"));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "//file.txt"));
    }

    @Test
    void testEqualsNormalizedOnSystem() {
        assertTrue(FilenameUtils.equalsNormalizedOnSystem(null, null));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem(null, ""));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("", null));
        assertTrue(FilenameUtils.equalsNormalizedOnSystem("", ""));
        assertTrue(FilenameUtils.equalsNormalizedOnSystem("file.txt", "file.txt"));
        assertEquals(WINDOWS, FilenameUtils.equalsNormalizedOnSystem("file.txt", "FILE.TXT"));
        assertTrue(FilenameUtils.equalsNormalizedOnSystem("a\\b\\file.txt", "a/b/file.txt"));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("a/b/", "a/b"));
        assertFalse(FilenameUtils.equalsNormalizedOnSystem("//a.html", "//ab.html"));
    }

    @Test
    void testEqualsOnSystem() {
        assertTrue(FilenameUtils.equalsOnSystem(null, null));
        assertFalse(FilenameUtils.equalsOnSystem(null, ""));
        assertFalse(FilenameUtils.equalsOnSystem("", null));
        assertTrue(FilenameUtils.equalsOnSystem("", ""));
        assertTrue(FilenameUtils.equalsOnSystem("file.txt", "file.txt"));
        assertEquals(WINDOWS, FilenameUtils.equalsOnSystem("file.txt", "FILE.TXT"));
        assertFalse(FilenameUtils.equalsOnSystem("a\\b\\file.txt", "a/b/file.txt"));
    }

    @Test
    void testGetBaseName() {
        assertNull(FilenameUtils.getBaseName(null));
        assertEquals("noseparator", FilenameUtils.getBaseName("noseparator.inthispath"));
        assertEquals("c", FilenameUtils.getBaseName("a/b/c.txt"));
        assertEquals("c", FilenameUtils.getBaseName("a/b/c"));
        assertEquals("", FilenameUtils.getBaseName("a/b/c/"));
        assertEquals("c", FilenameUtils.getBaseName("a\\b\\c"));
        assertEquals("file.txt", FilenameUtils.getBaseName("file.txt.bak"));
    }

    @Test
    void testGetBaseName_with_null_character() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getBaseName("fil\u0000e.txt.bak"));
    }

    @Test
    void testGetExtension() {
        assertNull(FilenameUtils.getExtension(null));
        assertEquals("ext", FilenameUtils.getExtension("file.ext"));
        assertEquals("", FilenameUtils.getExtension("README"));
        assertEquals("com", FilenameUtils.getExtension("domain.dot.com"));
        assertEquals("jpeg", FilenameUtils.getExtension("image.jpeg"));
        assertEquals("", FilenameUtils.getExtension("a.b/c"));
        assertEquals("txt", FilenameUtils.getExtension("a.b/c.txt"));
        assertEquals("", FilenameUtils.getExtension("a/b/c"));
        assertEquals("", FilenameUtils.getExtension("a.b\\c"));
        assertEquals("txt", FilenameUtils.getExtension("a.b\\c.txt"));
        assertEquals("", FilenameUtils.getExtension("a\\b\\c"));
        assertEquals("", FilenameUtils.getExtension("C:\\temp\\foo.bar\\README"));
        assertEquals("ext", FilenameUtils.getExtension("../filename.ext"));

        if (FilenameUtils.isSystemWindows()) {
            // Special case handling for NTFS ADS names
            final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getExtension("foo.exe:bar.txt"));
            assertEquals("NTFS ADS separator (':') in file name is forbidden.", e.getMessage());
        } else {
            // Upwards compatibility:
            assertEquals("txt", FilenameUtils.getExtension("foo.exe:bar.txt"));
        }
    }

    @Test
    void testGetFullPath() {
        assertNull(FilenameUtils.getFullPath(null));
        assertEquals("", FilenameUtils.getFullPath("noseparator.inthispath"));
        assertEquals("a/b/", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getFullPath("a/b/c"));
        assertEquals("a/b/c/", FilenameUtils.getFullPath("a/b/c/"));
        assertEquals("a\\b\\", FilenameUtils.getFullPath("a\\b\\c"));

        assertNull(FilenameUtils.getFullPath(":"));
        assertNull(FilenameUtils.getFullPath("1:/a/b/c.txt"));
        assertNull(FilenameUtils.getFullPath("1:"));
        assertNull(FilenameUtils.getFullPath("1:a"));
        assertNull(FilenameUtils.getFullPath("///a/b/c.txt"));
        assertNull(FilenameUtils.getFullPath("//a"));

        assertEquals("", FilenameUtils.getFullPath(""));

        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals(DRIVE_C, FilenameUtils.getFullPath(DRIVE_C));
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals("", FilenameUtils.getFullPath(DRIVE_C));
        }

        assertEquals("C:/", FilenameUtils.getFullPath("C:/"));
        assertEquals("//server/", FilenameUtils.getFullPath("//server/"));
        assertEquals("~/", FilenameUtils.getFullPath("~"));
        assertEquals("~/", FilenameUtils.getFullPath("~/"));
        assertEquals("~user/", FilenameUtils.getFullPath("~user"));
        assertEquals("~user/", FilenameUtils.getFullPath("~user/"));

        assertEquals("a/b/", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("/a/b/", FilenameUtils.getFullPath("/a/b/c.txt"));
        assertEquals(DRIVE_C, FilenameUtils.getFullPath("C:a"));
        assertEquals("C:a/b/", FilenameUtils.getFullPath("C:a/b/c.txt"));
        assertEquals("C:/a/b/", FilenameUtils.getFullPath("C:/a/b/c.txt"));
        assertEquals("//server/a/b/", FilenameUtils.getFullPath("//server/a/b/c.txt"));
        assertEquals("~/a/b/", FilenameUtils.getFullPath("~/a/b/c.txt"));
        assertEquals("~user/a/b/", FilenameUtils.getFullPath("~user/a/b/c.txt"));
    }

    @Test
    void testGetFullPathNoEndSeparator() {
        assertNull(FilenameUtils.getFullPathNoEndSeparator(null));
        assertEquals("", FilenameUtils.getFullPathNoEndSeparator("noseparator.inthispath"));
        assertEquals("a/b", FilenameUtils.getFullPathNoEndSeparator("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getFullPathNoEndSeparator("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getFullPathNoEndSeparator("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getFullPathNoEndSeparator("a\\b\\c"));

        assertNull(FilenameUtils.getFullPathNoEndSeparator(":"));
        assertNull(FilenameUtils.getFullPathNoEndSeparator("1:/a/b/c.txt"));
        assertNull(FilenameUtils.getFullPathNoEndSeparator("1:"));
        assertNull(FilenameUtils.getFullPathNoEndSeparator("1:a"));
        assertNull(FilenameUtils.getFullPathNoEndSeparator("///a/b/c.txt"));
        assertNull(FilenameUtils.getFullPathNoEndSeparator("//a"));

        assertEquals("", FilenameUtils.getFullPathNoEndSeparator(""));

        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals(DRIVE_C, FilenameUtils.getFullPathNoEndSeparator(DRIVE_C));
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals("", FilenameUtils.getFullPathNoEndSeparator(DRIVE_C));
        }

        assertEquals("C:/", FilenameUtils.getFullPathNoEndSeparator("C:/"));
        assertEquals("//server/", FilenameUtils.getFullPathNoEndSeparator("//server/"));
        assertEquals("~", FilenameUtils.getFullPathNoEndSeparator("~"));
        assertEquals("~/", FilenameUtils.getFullPathNoEndSeparator("~/"));
        assertEquals("~user", FilenameUtils.getFullPathNoEndSeparator("~user"));
        assertEquals("~user/", FilenameUtils.getFullPathNoEndSeparator("~user/"));

        assertEquals("a/b", FilenameUtils.getFullPathNoEndSeparator("a/b/c.txt"));
        assertEquals("/a/b", FilenameUtils.getFullPathNoEndSeparator("/a/b/c.txt"));
        assertEquals(DRIVE_C, FilenameUtils.getFullPathNoEndSeparator("C:a"));
        assertEquals("C:a/b", FilenameUtils.getFullPathNoEndSeparator("C:a/b/c.txt"));
        assertEquals("C:/a/b", FilenameUtils.getFullPathNoEndSeparator("C:/a/b/c.txt"));
        assertEquals("//server/a/b", FilenameUtils.getFullPathNoEndSeparator("//server/a/b/c.txt"));
        assertEquals("~/a/b", FilenameUtils.getFullPathNoEndSeparator("~/a/b/c.txt"));
        assertEquals("~user/a/b", FilenameUtils.getFullPathNoEndSeparator("~user/a/b/c.txt"));
    }

    /**
     * Test for https://issues.apache.org/jira/browse/IO-248
     */
    @Test
    void testGetFullPathNoEndSeparator_IO_248() {
        // Test single separator
        assertEquals("/", FilenameUtils.getFullPathNoEndSeparator("/"));
        assertEquals("\\", FilenameUtils.getFullPathNoEndSeparator("\\"));
        // Test one level directory
        assertEquals("/", FilenameUtils.getFullPathNoEndSeparator("/abc"));
        assertEquals("\\", FilenameUtils.getFullPathNoEndSeparator("\\abc"));
        // Test one level directory
        assertEquals("/abc", FilenameUtils.getFullPathNoEndSeparator("/abc/xyz"));
        assertEquals("\\abc", FilenameUtils.getFullPathNoEndSeparator("\\abc\\xyz"));
    }

    /**
     * Test for https://issues.apache.org/jira/browse/IO-771
     */
    @Test
    @Disabled
    void testGetFullPathNoEndSeparator_IO_771() {
        // IO-771
        // On macOS while in the target folder in jshell:
        // new java.io.File("X:\\path\\subfolder").getAbsolutePath()
        // ==> "/Users/garygregory/git/commons/commons-lang/target/X:\\path\\subfolder"
        assertEquals("/Users/garygregory/git/commons/commons-lang/target/X:\\path",
                FilenameUtils.getFullPathNoEndSeparator("/Users/garygregory/git/commons/commons-lang/target/X:\\path\\subfolder"));
        assertEquals("X:\\path", FilenameUtils.getFullPathNoEndSeparator("X:\\path\\subfolder"));
        assertEquals("/Users/garygregory/git/commons/commons-lang/target/X:\\\\path",
                FilenameUtils.getFullPathNoEndSeparator("/Users/garygregory/git/commons/commons-lang/target/X:\\\\path\\\\subfolder"));
        assertEquals("/Users/garygregory/git/commons/commons-lang/target/X:\\\\\\path",
                FilenameUtils.getFullPathNoEndSeparator("/Users/garygregory/git/commons/commons-lang/target/X:\\\\path\\\\\\subfolder\\\\"));
        assertEquals("/Users/garygregory/git/commons/commons-lang/target/X:\\\\\\path",
                FilenameUtils.getFullPathNoEndSeparator("/Users/garygregory/git/commons/commons-lang/target/X:\\\\path\\\\\\subfolder"));
    }

    @Test
    void testGetName() {
        assertNull(FilenameUtils.getName(null));
        assertEquals("noseparator.inthispath", FilenameUtils.getName("noseparator.inthispath"));
        assertEquals("c.txt", FilenameUtils.getName("a/b/c.txt"));
        assertEquals("c", FilenameUtils.getName("a/b/c"));
        assertEquals("", FilenameUtils.getName("a/b/c/"));
        assertEquals("c", FilenameUtils.getName("a\\b\\c"));
    }

    @Test
    void testGetPath() {
        assertNull(FilenameUtils.getPath(null));
        assertEquals("", FilenameUtils.getPath("noseparator.inthispath"));
        assertEquals("", FilenameUtils.getPath("/noseparator.inthispath"));
        assertEquals("", FilenameUtils.getPath("\\noseparator.inthispath"));
        assertEquals("a/b/", FilenameUtils.getPath("a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("a/b/c"));
        assertEquals("a/b/c/", FilenameUtils.getPath("a/b/c/"));
        assertEquals("a\\b\\", FilenameUtils.getPath("a\\b\\c"));

        assertNull(FilenameUtils.getPath(":"));
        assertNull(FilenameUtils.getPath("1:/a/b/c.txt"));
        assertNull(FilenameUtils.getPath("1:"));
        assertNull(FilenameUtils.getPath("1:a"));
        assertNull(FilenameUtils.getPath("///a/b/c.txt"));
        assertNull(FilenameUtils.getPath("//a"));

        assertEquals("", FilenameUtils.getPath(""));
        assertEquals("", FilenameUtils.getPath(DRIVE_C));
        assertEquals("", FilenameUtils.getPath("C:/"));
        assertEquals("", FilenameUtils.getPath("//server/"));
        assertEquals("", FilenameUtils.getPath("~"));
        assertEquals("", FilenameUtils.getPath("~/"));
        assertEquals("", FilenameUtils.getPath("~user"));
        assertEquals("", FilenameUtils.getPath("~user/"));

        assertEquals("a/b/", FilenameUtils.getPath("a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("/a/b/c.txt"));
        assertEquals("", FilenameUtils.getPath("C:a"));
        assertEquals("a/b/", FilenameUtils.getPath("C:a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("C:/a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("//server/a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("~/a/b/c.txt"));
        assertEquals("a/b/", FilenameUtils.getPath("~user/a/b/c.txt"));
    }

    @Test
    void testGetPath_with_null_character() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getPath("~user/a/\u0000b/c.txt"));
    }

    @Test
    void testGetPathNoEndSeparator() {
        assertNull(FilenameUtils.getPath(null));
        assertEquals("", FilenameUtils.getPath("noseparator.inthispath"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("/noseparator.inthispath"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("\\noseparator.inthispath"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getPathNoEndSeparator("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getPathNoEndSeparator("a\\b\\c"));

        assertNull(FilenameUtils.getPathNoEndSeparator(":"));
        assertNull(FilenameUtils.getPathNoEndSeparator("1:/a/b/c.txt"));
        assertNull(FilenameUtils.getPathNoEndSeparator("1:"));
        assertNull(FilenameUtils.getPathNoEndSeparator("1:a"));
        assertNull(FilenameUtils.getPathNoEndSeparator("///a/b/c.txt"));
        assertNull(FilenameUtils.getPathNoEndSeparator("//a"));

        assertEquals("", FilenameUtils.getPathNoEndSeparator(""));
        assertEquals("", FilenameUtils.getPathNoEndSeparator(DRIVE_C));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("C:/"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("//server/"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("~"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("~/"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("~user"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("~user/"));

        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("/a/b/c.txt"));
        assertEquals("", FilenameUtils.getPathNoEndSeparator("C:a"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("C:a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("C:/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("//server/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("~/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPathNoEndSeparator("~user/a/b/c.txt"));
    }

    @Test
    void testGetPathNoEndSeparator_with_null_character() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getPathNoEndSeparator("~user/a\u0000/b/c.txt"));
    }

    @Test
    void testGetPrefix() {
        assertNull(FilenameUtils.getPrefix(null));
        assertNull(FilenameUtils.getPrefix(":"));
        assertNull(FilenameUtils.getPrefix("1:\\a\\b\\c.txt"));
        assertNull(FilenameUtils.getPrefix("1:"));
        assertNull(FilenameUtils.getPrefix("1:a"));
        assertNull(FilenameUtils.getPrefix("\\\\\\a\\b\\c.txt"));
        assertNull(FilenameUtils.getPrefix("\\\\a"));

        assertEquals("", FilenameUtils.getPrefix(""));
        assertEquals("\\", FilenameUtils.getPrefix("\\"));

        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals(DRIVE_C, FilenameUtils.getPrefix(DRIVE_C));
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals("", FilenameUtils.getPrefix(DRIVE_C));
        }

        assertEquals("C:\\", FilenameUtils.getPrefix("C:\\"));
        assertEquals("//server/", FilenameUtils.getPrefix("//server/"));
        assertEquals("~/", FilenameUtils.getPrefix("~"));
        assertEquals("~/", FilenameUtils.getPrefix("~/"));
        assertEquals("~user/", FilenameUtils.getPrefix("~user"));
        assertEquals("~user/", FilenameUtils.getPrefix("~user/"));

        assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
        assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
        assertEquals("C:\\", FilenameUtils.getPrefix("C:\\a\\b\\c.txt"));
        assertEquals("\\\\server\\", FilenameUtils.getPrefix("\\\\server\\a\\b\\c.txt"));

        assertEquals("", FilenameUtils.getPrefix("a/b/c.txt"));
        assertEquals("/", FilenameUtils.getPrefix("/a/b/c.txt"));
        assertEquals("C:/", FilenameUtils.getPrefix("C:/a/b/c.txt"));
        assertEquals("//server/", FilenameUtils.getPrefix("//server/a/b/c.txt"));
        assertEquals("~/", FilenameUtils.getPrefix("~/a/b/c.txt"));
        assertEquals("~user/", FilenameUtils.getPrefix("~user/a/b/c.txt"));

        assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
        assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
        assertEquals("~\\", FilenameUtils.getPrefix("~\\a\\b\\c.txt"));
        assertEquals("~user\\", FilenameUtils.getPrefix("~user\\a\\b\\c.txt"));
    }

    @Test
    void testGetPrefix_with_null_character() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getPrefix("~u\u0000ser\\a\\b\\c.txt"));
    }

    @Test
    void testGetPrefixLength() {
        assertEquals(-1, FilenameUtils.getPrefixLength(null));
        assertEquals(-1, FilenameUtils.getPrefixLength(":"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:a"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\a"));

        assertEquals(0, FilenameUtils.getPrefixLength(""));
        assertEquals(1, FilenameUtils.getPrefixLength("\\"));

        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals(2, FilenameUtils.getPrefixLength(DRIVE_C));
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals(0, FilenameUtils.getPrefixLength(DRIVE_C));
        }

        assertEquals(3, FilenameUtils.getPrefixLength("C:\\"));
        assertEquals(9, FilenameUtils.getPrefixLength("//server/"));
        assertEquals(2, FilenameUtils.getPrefixLength("~"));
        assertEquals(2, FilenameUtils.getPrefixLength("~/"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user/"));

        assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("C:a\\b\\c.txt"));
        assertEquals(3, FilenameUtils.getPrefixLength("C:\\a\\b\\c.txt"));
        assertEquals(9, FilenameUtils.getPrefixLength("\\\\server\\a\\b\\c.txt"));

        assertEquals(0, FilenameUtils.getPrefixLength("a/b/c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("/a/b/c.txt"));
        assertEquals(3, FilenameUtils.getPrefixLength("C:/a/b/c.txt"));
        assertEquals(9, FilenameUtils.getPrefixLength("//server/a/b/c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("~/a/b/c.txt"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user/a/b/c.txt"));

        assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("~\\a\\b\\c.txt"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user\\a\\b\\c.txt"));

        assertEquals(9, FilenameUtils.getPrefixLength("//server/a/b/c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("///a/b/c.txt"));

        assertEquals(1, FilenameUtils.getPrefixLength("/:foo"));
        assertEquals(1, FilenameUtils.getPrefixLength("/:/"));
        assertEquals(1, FilenameUtils.getPrefixLength("/:::::::.txt"));

        assertEquals(12, FilenameUtils.getPrefixLength("\\\\127.0.0.1\\a\\b\\c.txt"));
        assertEquals(6, FilenameUtils.getPrefixLength("\\\\::1\\a\\b\\c.txt"));
        assertEquals(21, FilenameUtils.getPrefixLength("\\\\server.example.org\\a\\b\\c.txt"));
        assertEquals(10, FilenameUtils.getPrefixLength("\\\\server.\\a\\b\\c.txt"));

        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\-server\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\.\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\..\\a\\b\\c.txt"));
    }

    @Test
    void testIndexOfExtension() {
        assertEquals(-1, FilenameUtils.indexOfExtension(null));
        assertEquals(-1, FilenameUtils.indexOfExtension("file"));
        assertEquals(4, FilenameUtils.indexOfExtension("file.txt"));
        assertEquals(13, FilenameUtils.indexOfExtension("a.txt/b.txt/c.txt"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b\\c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b.notextension/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b.notextension\\c"));

        if (FilenameUtils.isSystemWindows()) {
            // Special case handling for NTFS ADS names
            final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> FilenameUtils.indexOfExtension("foo.exe:bar.txt"));
            assertEquals("NTFS ADS separator (':') in file name is forbidden.", e.getMessage());
        } else {
            // Upwards compatibility on other systems
            assertEquals(11, FilenameUtils.indexOfExtension("foo.exe:bar.txt"));
        }

    }

    @Test
    void testIndexOfLastSeparator() {
        assertEquals(-1, FilenameUtils.indexOfLastSeparator(null));
        assertEquals(-1, FilenameUtils.indexOfLastSeparator("noseparator.inthispath"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a/b/c"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a\\b\\c"));
    }

    @Test
    void testInjectionFailure() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getName("a\\b\\\u0000c"));
    }

    @Test
    void testIsExtension() {
        assertFalse(FilenameUtils.isExtension(null, (String) null));
        assertFalse(FilenameUtils.isExtension("file.txt", (String) null));
        assertTrue(FilenameUtils.isExtension("file", (String) null));
        assertFalse(FilenameUtils.isExtension("file.txt", ""));
        assertTrue(FilenameUtils.isExtension("file", ""));
        assertTrue(FilenameUtils.isExtension("file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("file.txt", "rtf"));

        assertFalse(FilenameUtils.isExtension("a/b/file.txt", (String) null));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", ""));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", "rtf"));

        assertFalse(FilenameUtils.isExtension("a.b/file.txt", (String) null));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", ""));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", "rtf"));

        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", (String) null));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", ""));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", "rtf"));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", (String) null));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", ""));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "rtf"));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "TXT"));
    }

    @Test
    void testIsExtension_injection() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.isExtension("a.b\\fi\u0000le.txt", "TXT"));
    }

    @Test
    void testIsExtensionArray() {
        assertFalse(FilenameUtils.isExtension(null, (String[]) null));
        assertFalse(FilenameUtils.isExtension("file.txt", (String[]) null));
        assertTrue(FilenameUtils.isExtension("file", (String[]) null));
        assertFalse(FilenameUtils.isExtension("file.txt"));
        assertTrue(FilenameUtils.isExtension("file.txt", new String[]{"txt"}));
        assertFalse(FilenameUtils.isExtension("file.txt", new String[]{"rtf"}));
        assertTrue(FilenameUtils.isExtension("file", "rtf", ""));
        assertTrue(FilenameUtils.isExtension("file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a/b/file.txt", (String[]) null));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt"));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", new String[]{"txt"}));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", new String[]{"rtf"}));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a.b/file.txt", (String[]) null));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt"));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", new String[]{"txt"}));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", new String[]{"rtf"}));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", (String[]) null));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt"));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", new String[]{"txt"}));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", new String[]{"rtf"}));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", (String[]) null));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt"));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"txt"}));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"rtf"}));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"TXT"}));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "TXT", "RTF"));
    }

    @Test
    void testIsExtensionCollection() {
        assertFalse(FilenameUtils.isExtension(null, (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("file.txt", (Collection<String>) null));
        assertTrue(FilenameUtils.isExtension("file", (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("file.txt", new ArrayList<>()));
        assertTrue(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("txt"))));
        assertFalse(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("rtf"))));
        assertTrue(FilenameUtils.isExtension("file", new ArrayList<>(Arrays.asList("rtf", ""))));
        assertTrue(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("rtf", "txt"))));

        assertFalse(FilenameUtils.isExtension("a/b/file.txt", (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>()));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("txt"))));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("rtf"))));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("rtf", "txt"))));

        assertFalse(FilenameUtils.isExtension("a.b/file.txt", (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>()));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("txt"))));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("rtf"))));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("rtf", "txt"))));

        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>()));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("txt"))));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("rtf"))));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("rtf", "txt"))));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", (Collection<String>) null));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>()));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("txt"))));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("rtf"))));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("rtf", "txt"))));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("TXT"))));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("TXT", "RTF"))));
    }

    @Test
    void testIsExtensionVarArgs() {
        assertTrue(FilenameUtils.isExtension("file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("file.txt", "rtf"));
        assertTrue(FilenameUtils.isExtension("file", "rtf", ""));
        assertTrue(FilenameUtils.isExtension("file.txt", "rtf", "txt"));

        assertTrue(FilenameUtils.isExtension("a/b/file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a/b/file.txt", "rtf"));
        assertTrue(FilenameUtils.isExtension("a/b/file.txt", "rtf", "txt"));

        assertTrue(FilenameUtils.isExtension("a.b/file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a.b/file.txt", "rtf"));
        assertTrue(FilenameUtils.isExtension("a.b/file.txt", "rtf", "txt"));

        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a\\b\\file.txt", "rtf"));
        assertTrue(FilenameUtils.isExtension("a\\b\\file.txt", "rtf", "txt"));

        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", "txt"));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "rtf"));
        assertTrue(FilenameUtils.isExtension("a.b\\file.txt", "rtf", "txt"));

        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "TXT"));
        assertFalse(FilenameUtils.isExtension("a.b\\file.txt", "TXT", "RTF"));
    }

    @Test
    void testNormalize() {
        assertNull(FilenameUtils.normalize(null));
        assertNull(FilenameUtils.normalize(":"));
        assertNull(FilenameUtils.normalize("1:\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("1:"));
        assertNull(FilenameUtils.normalize("1:a"));
        assertNull(FilenameUtils.normalize("\\\\\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\a"));

        assertEquals("a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("a\\b/c.txt"));
        assertEquals("" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\a\\b/c.txt"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("C:\\a\\b/c.txt"));
        assertEquals("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\server\\a\\b/c.txt"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("~\\a\\b/c.txt"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("~user\\a\\b/c.txt"));

        assertEquals("a" + SEP + "c", FilenameUtils.normalize("a/b/../c"));
        assertEquals("c", FilenameUtils.normalize("a/b/../../c"));
        assertEquals("c" + SEP, FilenameUtils.normalize("a/b/../../c/"));
        assertNull(FilenameUtils.normalize("a/b/../../../c"));
        assertEquals("a" + SEP, FilenameUtils.normalize("a/b/.."));
        assertEquals("a" + SEP, FilenameUtils.normalize("a/b/../"));
        assertEquals("", FilenameUtils.normalize("a/b/../.."));
        assertEquals("", FilenameUtils.normalize("a/b/../../"));
        assertNull(FilenameUtils.normalize("a/b/../../.."));
        assertEquals("a" + SEP + "d", FilenameUtils.normalize("a/b/../c/../d"));
        assertEquals("a" + SEP + "d" + SEP, FilenameUtils.normalize("a/b/../c/../d/"));
        assertEquals("a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("a/b//d"));
        assertEquals("a" + SEP + "b" + SEP, FilenameUtils.normalize("a/b/././."));
        assertEquals("a" + SEP + "b" + SEP, FilenameUtils.normalize("a/b/./././"));
        assertEquals("a" + SEP, FilenameUtils.normalize("./a/"));
        assertEquals("a", FilenameUtils.normalize("./a"));
        assertEquals("", FilenameUtils.normalize("./"));
        assertEquals("", FilenameUtils.normalize("."));
        assertNull(FilenameUtils.normalize("../a"));
        assertNull(FilenameUtils.normalize(".."));
        assertEquals("", FilenameUtils.normalize(""));

        assertEquals(SEP + "a", FilenameUtils.normalize("/a"));
        assertEquals(SEP + "a" + SEP, FilenameUtils.normalize("/a/"));
        assertEquals(SEP + "a" + SEP + "c", FilenameUtils.normalize("/a/b/../c"));
        assertEquals(SEP + "c", FilenameUtils.normalize("/a/b/../../c"));
        assertNull(FilenameUtils.normalize("/a/b/../../../c"));
        assertEquals(SEP + "a" + SEP, FilenameUtils.normalize("/a/b/.."));
        assertEquals(SEP + "", FilenameUtils.normalize("/a/b/../.."));
        assertNull(FilenameUtils.normalize("/a/b/../../.."));
        assertEquals(SEP + "a" + SEP + "d", FilenameUtils.normalize("/a/b/../c/../d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("/a/b//d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP, FilenameUtils.normalize("/a/b/././."));
        assertEquals(SEP + "a", FilenameUtils.normalize("/./a"));
        assertEquals(SEP + "", FilenameUtils.normalize("/./"));
        assertEquals(SEP + "", FilenameUtils.normalize("/."));
        assertNull(FilenameUtils.normalize("/../a"));
        assertNull(FilenameUtils.normalize("/.."));
        assertEquals(SEP + "", FilenameUtils.normalize("/"));

        assertEquals("~" + SEP + "a", FilenameUtils.normalize("~/a"));
        assertEquals("~" + SEP + "a" + SEP, FilenameUtils.normalize("~/a/"));
        assertEquals("~" + SEP + "a" + SEP + "c", FilenameUtils.normalize("~/a/b/../c"));
        assertEquals("~" + SEP + "c", FilenameUtils.normalize("~/a/b/../../c"));
        assertNull(FilenameUtils.normalize("~/a/b/../../../c"));
        assertEquals("~" + SEP + "a" + SEP, FilenameUtils.normalize("~/a/b/.."));
        assertEquals("~" + SEP + "", FilenameUtils.normalize("~/a/b/../.."));
        assertNull(FilenameUtils.normalize("~/a/b/../../.."));
        assertEquals("~" + SEP + "a" + SEP + "d", FilenameUtils.normalize("~/a/b/../c/../d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("~/a/b//d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP, FilenameUtils.normalize("~/a/b/././."));
        assertEquals("~" + SEP + "a", FilenameUtils.normalize("~/./a"));
        assertEquals("~" + SEP, FilenameUtils.normalize("~/./"));
        assertEquals("~" + SEP, FilenameUtils.normalize("~/."));
        assertNull(FilenameUtils.normalize("~/../a"));
        assertNull(FilenameUtils.normalize("~/.."));
        assertEquals("~" + SEP, FilenameUtils.normalize("~/"));
        assertEquals("~" + SEP, FilenameUtils.normalize("~"));

        assertEquals("~user" + SEP + "a", FilenameUtils.normalize("~user/a"));
        assertEquals("~user" + SEP + "a" + SEP, FilenameUtils.normalize("~user/a/"));
        assertEquals("~user" + SEP + "a" + SEP + "c", FilenameUtils.normalize("~user/a/b/../c"));
        assertEquals("~user" + SEP + "c", FilenameUtils.normalize("~user/a/b/../../c"));
        assertNull(FilenameUtils.normalize("~user/a/b/../../../c"));
        assertEquals("~user" + SEP + "a" + SEP, FilenameUtils.normalize("~user/a/b/.."));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/a/b/../.."));
        assertNull(FilenameUtils.normalize("~user/a/b/../../.."));
        assertEquals("~user" + SEP + "a" + SEP + "d", FilenameUtils.normalize("~user/a/b/../c/../d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("~user/a/b//d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP, FilenameUtils.normalize("~user/a/b/././."));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalize("~user/./a"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/./"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/."));
        assertNull(FilenameUtils.normalize("~user/../a"));
        assertNull(FilenameUtils.normalize("~user/.."));
        assertEquals("~user" + SEP, FilenameUtils.normalize("~user/"));
        assertEquals("~user" + SEP, FilenameUtils.normalize("~user"));

        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalize("C:/a"));
        assertEquals(DRIVE_C + SEP + "a" + SEP, FilenameUtils.normalize("C:/a/"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "c", FilenameUtils.normalize("C:/a/b/../c"));
        assertEquals(DRIVE_C + SEP + "c", FilenameUtils.normalize("C:/a/b/../../c"));
        assertNull(FilenameUtils.normalize("C:/a/b/../../../c"));
        assertEquals(DRIVE_C + SEP + "a" + SEP, FilenameUtils.normalize("C:/a/b/.."));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalize("C:/a/b/../.."));
        assertNull(FilenameUtils.normalize("C:/a/b/../../.."));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "d", FilenameUtils.normalize("C:/a/b/../c/../d"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("C:/a/b//d"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP, FilenameUtils.normalize("C:/a/b/././."));
        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalize("C:/./a"));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalize("C:/./"));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalize("C:/."));
        assertNull(FilenameUtils.normalize("C:/../a"));
        assertNull(FilenameUtils.normalize("C:/.."));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalize("C:/"));

        assertEquals(DRIVE_C + "a", FilenameUtils.normalize("C:a"));
        assertEquals(DRIVE_C + "a" + SEP, FilenameUtils.normalize("C:a/"));
        assertEquals(DRIVE_C + "a" + SEP + "c", FilenameUtils.normalize("C:a/b/../c"));
        assertEquals(DRIVE_C + "c", FilenameUtils.normalize("C:a/b/../../c"));
        assertNull(FilenameUtils.normalize("C:a/b/../../../c"));
        assertEquals(DRIVE_C + "a" + SEP, FilenameUtils.normalize("C:a/b/.."));
        assertEquals(DRIVE_C + "", FilenameUtils.normalize("C:a/b/../.."));
        assertNull(FilenameUtils.normalize("C:a/b/../../.."));
        assertEquals(DRIVE_C + "a" + SEP + "d", FilenameUtils.normalize("C:a/b/../c/../d"));
        assertEquals(DRIVE_C + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("C:a/b//d"));
        assertEquals(DRIVE_C + "a" + SEP + "b" + SEP, FilenameUtils.normalize("C:a/b/././."));
        assertEquals(DRIVE_C + "a", FilenameUtils.normalize("C:./a"));
        assertEquals(DRIVE_C + "", FilenameUtils.normalize("C:./"));
        assertEquals(DRIVE_C + "", FilenameUtils.normalize("C:."));
        assertNull(FilenameUtils.normalize("C:../a"));
        assertNull(FilenameUtils.normalize("C:.."));
        assertEquals(DRIVE_C + "", FilenameUtils.normalize(DRIVE_C));

        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalize("//server/a"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP, FilenameUtils.normalize("//server/a/"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "c", FilenameUtils.normalize("//server/a/b/../c"));
        assertEquals(SEP + SEP + "server" + SEP + "c", FilenameUtils.normalize("//server/a/b/../../c"));
        assertNull(FilenameUtils.normalize("//server/a/b/../../../c"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP, FilenameUtils.normalize("//server/a/b/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/a/b/../.."));
        assertNull(FilenameUtils.normalize("//server/a/b/../../.."));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "d", FilenameUtils.normalize("//server/a/b/../c/../d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("//server/a/b//d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP, FilenameUtils.normalize("//server/a/b/././."));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalize("//server/./a"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/./"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/."));
        assertNull(FilenameUtils.normalize("//server/../a"));
        assertNull(FilenameUtils.normalize("//server/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/"));

        assertEquals(SEP + SEP + "127.0.0.1" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\127.0.0.1\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "::1" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\::1\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "1::" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\1::\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "server.example.org" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\server.example.org\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "server.sub.example.org" + SEP + "a" + SEP + "b" + SEP + "c.txt",
            FilenameUtils.normalize("\\\\server.sub.example.org\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "server." + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\server.\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "1::127.0.0.1" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\1::127.0.0.1\\a\\b\\c.txt"));

        // not valid IPv4 addresses but technically a valid "reg-name"s according to RFC1034
        assertEquals(SEP + SEP + "127.0.0.256" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\127.0.0.256\\a\\b\\c.txt"));
        assertEquals(SEP + SEP + "127.0.0.01" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\127.0.0.01\\a\\b\\c.txt"));

        assertNull(FilenameUtils.normalize("\\\\-server\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\.\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\..\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\127.0..1\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\::1::2\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\:1\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\1:\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\1:2:3:4:5:6:7:8:9\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\g:2:3:4:5:6:7:8\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\1ffff:2:3:4:5:6:7:8\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalize("\\\\1:2\\a\\b\\c.txt"));
        // IO-556
        assertNull(FilenameUtils.normalize("//../foo"));
        assertNull(FilenameUtils.normalize("\\\\..\\foo"));
    }

    /**
     */
    @Test
    void testNormalize_with_null_character() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.normalize("a\\b/c\u0000.txt"));
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.normalize("\u0000a\\b/c.txt"));
    }

    @Test
    void testNormalizeFromJavadoc() {
        // Examples from Javadoc
        assertEquals(SEP + "foo" + SEP, FilenameUtils.normalize("/foo//"));
        assertEquals(SEP + "foo" + SEP, FilenameUtils.normalize(SEP + "foo" + SEP + "." + SEP));
        assertEquals(SEP + "bar", FilenameUtils.normalize(SEP + "foo" + SEP + ".." + SEP + "bar"));
        assertEquals(SEP + "bar" + SEP, FilenameUtils.normalize(SEP + "foo" + SEP + ".." + SEP + "bar" + SEP));
        assertEquals(SEP + "baz", FilenameUtils.normalize(SEP + "foo" + SEP + ".." + SEP + "bar" + SEP + ".." + SEP + "baz"));
        assertEquals(SEP + SEP + "foo" + SEP + "bar", FilenameUtils.normalize("//foo//./bar"));
        assertNull(FilenameUtils.normalize(SEP + ".." + SEP));
        assertNull(FilenameUtils.normalize(".." + SEP + "foo"));
        assertEquals("foo" + SEP, FilenameUtils.normalize("foo" + SEP + "bar" + SEP + ".."));
        assertNull(FilenameUtils.normalize("foo" + SEP + ".." + SEP + ".." + SEP + "bar"));
        assertEquals("bar", FilenameUtils.normalize("foo" + SEP + ".." + SEP + "bar"));
        assertEquals(SEP + SEP + "server" + SEP + "bar", FilenameUtils.normalize(SEP + SEP + "server" + SEP + "foo" + SEP + ".." + SEP + "bar"));
        assertNull(FilenameUtils.normalize(SEP + SEP + "server" + SEP + ".." + SEP + "bar"));
        assertEquals(DRIVE_C + SEP + "bar", FilenameUtils.normalize(DRIVE_C + SEP + "foo" + SEP + ".." + SEP + "bar"));
        assertNull(FilenameUtils.normalize(DRIVE_C + SEP + ".." + SEP + "bar"));
        assertEquals("~" + SEP + "bar" + SEP, FilenameUtils.normalize("~" + SEP + "foo" + SEP + ".." + SEP + "bar" + SEP));
        assertNull(FilenameUtils.normalize("~" + SEP + ".." + SEP + "bar"));

        assertEquals(SEP + SEP + "foo" + SEP + "bar", FilenameUtils.normalize("//foo//./bar"));
        assertEquals(SEP + SEP + "foo" + SEP + "bar", FilenameUtils.normalize("\\\\foo\\\\.\\bar"));
    }

    @Test
    void testNormalizeNoEndSeparator() {
        assertNull(FilenameUtils.normalizeNoEndSeparator(null));
        assertNull(FilenameUtils.normalizeNoEndSeparator(":"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("1:\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("1:"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("1:a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("\\\\\\a\\b\\c.txt"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("\\\\a"));

        assertEquals("a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("a\\b/c.txt"));
        assertEquals("" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("\\a\\b/c.txt"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("C:\\a\\b/c.txt"));
        assertEquals("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("\\\\server\\a\\b/c.txt"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("~\\a\\b/c.txt"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("~user\\a\\b/c.txt"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalizeNoEndSeparator("C:\\\\a\\\\b\\\\c.txt"));

        assertEquals("a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("a/b/../c"));
        assertEquals("c", FilenameUtils.normalizeNoEndSeparator("a/b/../../c"));
        assertEquals("c", FilenameUtils.normalizeNoEndSeparator("a/b/../../c/"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("a/b/../../../c"));
        assertEquals("a", FilenameUtils.normalizeNoEndSeparator("a/b/.."));
        assertEquals("a", FilenameUtils.normalizeNoEndSeparator("a/b/../"));
        assertEquals("", FilenameUtils.normalizeNoEndSeparator("a/b/../.."));
        assertEquals("", FilenameUtils.normalizeNoEndSeparator("a/b/../../"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("a/b/../../.."));
        assertEquals("a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("a/b/../c/../d"));
        assertEquals("a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("a/b/../c/../d/"));
        assertEquals("a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("a/b//d"));
        assertEquals("a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("a/b/././."));
        assertEquals("a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("a/b/./././"));
        assertEquals("a", FilenameUtils.normalizeNoEndSeparator("./a/"));
        assertEquals("a", FilenameUtils.normalizeNoEndSeparator("./a"));
        assertEquals("", FilenameUtils.normalizeNoEndSeparator("./"));
        assertEquals("", FilenameUtils.normalizeNoEndSeparator("."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator(".."));
        assertEquals("", FilenameUtils.normalizeNoEndSeparator(""));

        assertEquals(SEP + "a", FilenameUtils.normalizeNoEndSeparator("/a"));
        assertEquals(SEP + "a", FilenameUtils.normalizeNoEndSeparator("/a/"));
        assertEquals(SEP + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("/a/b/../c"));
        assertEquals(SEP + "c", FilenameUtils.normalizeNoEndSeparator("/a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("/a/b/../../../c"));
        assertEquals(SEP + "a", FilenameUtils.normalizeNoEndSeparator("/a/b/.."));
        assertEquals(SEP + "", FilenameUtils.normalizeNoEndSeparator("/a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("/a/b/../../.."));
        assertEquals(SEP + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("/a/b/../c/../d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("/a/b//d"));
        assertEquals(SEP + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("/a/b/././."));
        assertEquals(SEP + "a", FilenameUtils.normalizeNoEndSeparator("/./a"));
        assertEquals(SEP + "", FilenameUtils.normalizeNoEndSeparator("/./"));
        assertEquals(SEP + "", FilenameUtils.normalizeNoEndSeparator("/."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("/../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("/.."));
        assertEquals(SEP + "", FilenameUtils.normalizeNoEndSeparator("/"));

        assertEquals("~" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~/a"));
        assertEquals("~" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~/a/"));
        assertEquals("~" + SEP + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("~/a/b/../c"));
        assertEquals("~" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("~/a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~/a/b/../../../c"));
        assertEquals("~" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~/a/b/.."));
        assertEquals("~" + SEP + "", FilenameUtils.normalizeNoEndSeparator("~/a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~/a/b/../../.."));
        assertEquals("~" + SEP + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("~/a/b/../c/../d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("~/a/b//d"));
        assertEquals("~" + SEP + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("~/a/b/././."));
        assertEquals("~" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~/./a"));
        assertEquals("~" + SEP, FilenameUtils.normalizeNoEndSeparator("~/./"));
        assertEquals("~" + SEP, FilenameUtils.normalizeNoEndSeparator("~/."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~/../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~/.."));
        assertEquals("~" + SEP, FilenameUtils.normalizeNoEndSeparator("~/"));
        assertEquals("~" + SEP, FilenameUtils.normalizeNoEndSeparator("~"));

        assertEquals("~user" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~user/a"));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~user/a/"));
        assertEquals("~user" + SEP + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("~user/a/b/../c"));
        assertEquals("~user" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../../c"));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~user/a/b/.."));
        assertEquals("~user" + SEP + "", FilenameUtils.normalizeNoEndSeparator("~user/a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../.."));
        assertEquals("~user" + SEP + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("~user/a/b/../c/../d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("~user/a/b//d"));
        assertEquals("~user" + SEP + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("~user/a/b/././."));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("~user/./a"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalizeNoEndSeparator("~user/./"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalizeNoEndSeparator("~user/."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~user/../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("~user/.."));
        assertEquals("~user" + SEP, FilenameUtils.normalizeNoEndSeparator("~user/"));
        assertEquals("~user" + SEP, FilenameUtils.normalizeNoEndSeparator("~user"));

        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalizeNoEndSeparator("C:/a"));
        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalizeNoEndSeparator("C:/a/"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("C:/a/b/../c"));
        assertEquals(DRIVE_C + SEP + "c", FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../../c"));
        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalizeNoEndSeparator("C:/a/b/.."));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalizeNoEndSeparator("C:/a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../.."));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("C:/a/b/../c/../d"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("C:/a/b//d"));
        assertEquals(DRIVE_C + SEP + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("C:/a/b/././."));
        assertEquals(DRIVE_C + SEP + "a", FilenameUtils.normalizeNoEndSeparator("C:/./a"));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalizeNoEndSeparator("C:/./"));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalizeNoEndSeparator("C:/."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:/../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:/.."));
        assertEquals(DRIVE_C + SEP + "", FilenameUtils.normalizeNoEndSeparator("C:/"));

        assertEquals(DRIVE_C + "a", FilenameUtils.normalizeNoEndSeparator("C:a"));
        assertEquals(DRIVE_C + "a", FilenameUtils.normalizeNoEndSeparator("C:a/"));
        assertEquals(DRIVE_C + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("C:a/b/../c"));
        assertEquals(DRIVE_C + "c", FilenameUtils.normalizeNoEndSeparator("C:a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:a/b/../../../c"));
        assertEquals(DRIVE_C + "a", FilenameUtils.normalizeNoEndSeparator("C:a/b/.."));
        assertEquals(DRIVE_C + "", FilenameUtils.normalizeNoEndSeparator("C:a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:a/b/../../.."));
        assertEquals(DRIVE_C + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("C:a/b/../c/../d"));
        assertEquals(DRIVE_C + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("C:a/b//d"));
        assertEquals(DRIVE_C + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("C:a/b/././."));
        assertEquals(DRIVE_C + "a", FilenameUtils.normalizeNoEndSeparator("C:./a"));
        assertEquals(DRIVE_C + "", FilenameUtils.normalizeNoEndSeparator("C:./"));
        assertEquals(DRIVE_C + "", FilenameUtils.normalizeNoEndSeparator("C:."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("C:.."));
        assertEquals(DRIVE_C + "", FilenameUtils.normalizeNoEndSeparator(DRIVE_C));

        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("//server/a"));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("//server/a/"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("//server/a/b/../c"));
        assertEquals(SEP + SEP + "server" + SEP + "c", FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../c"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../../c"));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("//server/a/b/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalizeNoEndSeparator("//server/a/b/../.."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../.."));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("//server/a/b/../c/../d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalizeNoEndSeparator("//server/a/b//d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b", FilenameUtils.normalizeNoEndSeparator("//server/a/b/././."));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalizeNoEndSeparator("//server/./a"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalizeNoEndSeparator("//server/./"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalizeNoEndSeparator("//server/."));
        assertNull(FilenameUtils.normalizeNoEndSeparator("//server/../a"));
        assertNull(FilenameUtils.normalizeNoEndSeparator("//server/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalizeNoEndSeparator("//server/"));
    }

    @Test
    void testNormalizeNoEndSeparatorUnixWin() {

        // Normalize (Unix Separator)
        assertEquals("/a/c", FilenameUtils.normalizeNoEndSeparator("/a/b/../c/", true));
        assertEquals("/a/c", FilenameUtils.normalizeNoEndSeparator("\\a\\b\\..\\c\\", true));

        // Normalize (Windows Separator)
        assertEquals("\\a\\c", FilenameUtils.normalizeNoEndSeparator("/a/b/../c/", false));
        assertEquals("\\a\\c", FilenameUtils.normalizeNoEndSeparator("\\a\\b\\..\\c\\", false));
    }

    @Test
    void testNormalizeUnixWin() {

        // Normalize (Unix Separator)
        assertEquals("/a/c/", FilenameUtils.normalize("/a/b/../c/", true));
        assertEquals("/a/c/", FilenameUtils.normalize("\\a\\b\\..\\c\\", true));

        // Normalize (Windows Separator)
        assertEquals("\\a\\c\\", FilenameUtils.normalize("/a/b/../c/", false));
        assertEquals("\\a\\c\\", FilenameUtils.normalize("\\a\\b\\..\\c\\", false));
    }

    @Test
    void testRemoveExtension() {
        assertNull(FilenameUtils.removeExtension(null));
        assertEquals("file", FilenameUtils.removeExtension("file.ext"));
        assertEquals("README", FilenameUtils.removeExtension("README"));
        assertEquals("domain.dot", FilenameUtils.removeExtension("domain.dot.com"));
        assertEquals("image", FilenameUtils.removeExtension("image.jpeg"));
        assertEquals("a.b/c", FilenameUtils.removeExtension("a.b/c"));
        assertEquals("a.b/c", FilenameUtils.removeExtension("a.b/c.txt"));
        assertEquals("a/b/c", FilenameUtils.removeExtension("a/b/c"));
        assertEquals("a.b\\c", FilenameUtils.removeExtension("a.b\\c"));
        assertEquals("a.b\\c", FilenameUtils.removeExtension("a.b\\c.txt"));
        assertEquals("a\\b\\c", FilenameUtils.removeExtension("a\\b\\c"));
        assertEquals("C:\\temp\\foo.bar\\README", FilenameUtils.removeExtension("C:\\temp\\foo.bar\\README"));
        assertEquals("../filename", FilenameUtils.removeExtension("../filename.ext"));
    }

    @Test
    void testSeparatorsToSystem() {
        if (WINDOWS) {
            assertNull(FilenameUtils.separatorsToSystem(null));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToSystem("\\a\\b\\c.txt"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b/c"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToSystem("D:/a/b/c"));
        } else {
            assertNull(FilenameUtils.separatorsToSystem(null));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("/a/b/c.txt", FilenameUtils.separatorsToSystem("/a/b/c.txt"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b\\c"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("D:/a/b/c", FilenameUtils.separatorsToSystem("D:\\a\\b\\c"));
        }
    }

    @Test
    void testSeparatorsToUnix() {
        assertNull(FilenameUtils.separatorsToUnix(null));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b/c"));
        assertEquals("/a/b/c.txt", FilenameUtils.separatorsToUnix("/a/b/c.txt"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b\\c"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("\\a\\b\\c"));
        assertEquals("D:/a/b/c", FilenameUtils.separatorsToUnix("D:\\a\\b\\c"));
    }

    @Test
    void testSeparatorsToWindows() {
        assertNull(FilenameUtils.separatorsToWindows(null));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b\\c"));
        assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToWindows("\\a\\b\\c.txt"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b/c"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("/a/b/c"));
        assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToWindows("D:/a/b/c"));
    }
}
