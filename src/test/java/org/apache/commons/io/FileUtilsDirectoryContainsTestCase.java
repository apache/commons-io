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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This class ensure the correctness of {@link FileUtils#directoryContains(File,File)}.
 *
 * @see FileUtils#directoryContains(File, File)
 * @since 2.2
 */
public class FileUtilsDirectoryContainsTestCase {

    private File directory1;
    private File directory2;
    private File directory3;
    private File file1;
    private File file1ByRelativeDirectory2;
    private File file2;
    private File file2ByRelativeDirectory1;
    private File file3;

    @TempDir
    public File top;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    public void setUp() throws Exception {
        directory1 = new File(top, "directory1");
        directory2 = new File(top, "directory2");
        directory3 = new File(directory2, "directory3");

        directory1.mkdir();
        directory2.mkdir();
        directory3.mkdir();

        file1 = new File(directory1, "file1");
        file2 = new File(directory2, "file2");
        file3 = new File(top, "file3");

        // Tests case with relative path
        file1ByRelativeDirectory2 = new File(top, "directory2/../directory1/file1");
        file2ByRelativeDirectory1 = new File(top, "directory1/../directory2/file2");

        FileUtils.touch(file1);
        FileUtils.touch(file2);
        FileUtils.touch(file3);
    }

    @Test
    public void testCanonicalPath() throws IOException {
        assertTrue(FileUtils.directoryContains(directory1, file1ByRelativeDirectory2));
        assertTrue(FileUtils.directoryContains(directory2, file2ByRelativeDirectory1));

        assertFalse(FileUtils.directoryContains(directory1, file2ByRelativeDirectory1));
        assertFalse(FileUtils.directoryContains(directory2, file1ByRelativeDirectory2));
    }

    @Test
    public void testDirectoryContainsDirectory() throws IOException {
        assertTrue(FileUtils.directoryContains(top, directory1));
        assertTrue(FileUtils.directoryContains(top, directory2));
        assertTrue(FileUtils.directoryContains(top, directory3));
        assertTrue(FileUtils.directoryContains(directory2, directory3));
    }

    @Test
    public void testDirectoryContainsFile() throws IOException {
        assertTrue(FileUtils.directoryContains(directory1, file1));
        assertTrue(FileUtils.directoryContains(directory2, file2));
    }

    @Test
    public void testDirectoryDoesNotContainFile() throws IOException {
        assertFalse(FileUtils.directoryContains(directory1, file2));
        assertFalse(FileUtils.directoryContains(directory2, file1));

        assertFalse(FileUtils.directoryContains(directory1, file3));
        assertFalse(FileUtils.directoryContains(directory2, file3));
    }

    @Test
    public void testDirectoryDoesNotContainsDirectory() throws IOException {
        assertFalse(FileUtils.directoryContains(directory1, top));
        assertFalse(FileUtils.directoryContains(directory2, top));
        assertFalse(FileUtils.directoryContains(directory3, top));
        assertFalse(FileUtils.directoryContains(directory3, directory2));
    }

    @Test
    public void testDirectoryDoesNotExist() throws IOException {
        final File dir = new File("DOESNOTEXIST");
        assertFalse(dir.exists());
        try {
            assertFalse(FileUtils.directoryContains(dir, file1));
            fail("Expected " + IllegalArgumentException.class.getName());
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSameFile() throws IOException {
        try {
            assertTrue(FileUtils.directoryContains(file1, file1));
            fail("Expected " + IllegalArgumentException.class.getName());
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIO466() throws IOException {
            final File fooFile = new File(directory1.getParent(), "directory1.txt");
            assertFalse(FileUtils.directoryContains(directory1, fooFile));
    }

    @Test
    public void testFileDoesNotExist() throws IOException {
        assertFalse(FileUtils.directoryContains(top, null));
        final File file = new File("DOESNOTEXIST");
        assertFalse(file.exists());
        assertFalse(FileUtils.directoryContains(top, file));
    }

    /**
     * Test to demonstrate a file which does not exist returns false
     * @throws IOException If an I/O error occurs
     */
    @Test
    public void testFileDoesNotExistBug() throws IOException {
        final File file = new File(top, "DOESNOTEXIST");
        assertTrue(top.exists(), "Check directory exists");
        assertFalse(file.exists(), "Check file does not exist");
        assertFalse(FileUtils.directoryContains(top, file), "Direcory does not contain unrealized file");
    }

    @Test
    public void testUnrealizedContainment() throws IOException {
        final File dir = new File("DOESNOTEXIST");
        final File file = new File(dir, "DOESNOTEXIST2");
        assertFalse(dir.exists());
        assertFalse(file.exists());
        try {
            assertTrue(FileUtils.directoryContains(dir, file));
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }
}
