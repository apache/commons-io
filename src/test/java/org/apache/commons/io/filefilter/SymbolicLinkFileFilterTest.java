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

package org.apache.commons.io.filefilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SymbolicLinkFileFilter}.
 */
public class SymbolicLinkFileFilterTest {

    public static final String TARGET_SHORT_NAME = "SLFF_Target";
    public static final String TARGET_EXT = ".txt";
    public static final String TARGET_NAME = TARGET_SHORT_NAME + TARGET_EXT;
    public static final String DIRECTORY_NAME = "SLFF_TargetDirectory";
    public static final String DIRECTORY_LINK_NAME = "SLFF_LinkDirectory";
    public static final String MISSING = "Missing";

    private static File testTargetFile; // hard file
    private static Path testTargetPath; // hard file Path
    private static File parentDirectoryFile; // System Temp directory
    private static File testLinkFile; // symbolic link to hard file
    private static String linkName; // Name of link file
    private static Path testLinkPath; // symbolic link to hard file Path
    private static File targetDirFile; //
    private static Path targetDirPath; // hard directory Path
    private static Path testLinkDirPath; // symbolic link to hardDirectory
    private static File testLinkDirFile;
    private static File missingFileFile; // non-existent file
    private static Path missingFilePath; // non-existent file
    private static SymbolicLinkFileFilter filter;

    // Mock filter for testing on Windows.
    private static SymbolicLinkFileFilter createMockFilter() {
        return new SymbolicLinkFileFilter() {

            private static final long serialVersionUID = 1L;

            @Override
            boolean isSymbolicLink(final Path filePath) {
                return filePath.toFile().exists() && filePath.toString().contains("Link"); // Mock test
            }
        };
    }

    private static Path createMockSymbolicLink(final Path link, final Path target) throws IOException {
        return Files.createFile(link);
    }

    private static Path createRealSymbolicLink(final Path link, final Path target) throws IOException {
        Files.deleteIfExists(link);
        return Files.createSymbolicLink(link, target);
    }

    @AfterAll
    static void tearDown() {
        // Fortunately, delete() doesn't throw an exception if the file doesn't exist.
        testLinkDirFile.delete();
        targetDirFile.delete();
        testLinkFile.delete();
        testTargetFile.delete();
    }

    /**
     * <p>
     * Unit test setup creates a hard file, a symbolic link to the hard file, a hard directory, and a symbolic link to that directory. All are created in the
     * temp directory
     * </p>
     * <p>
     * Unit test teardown deletes all four of these files.
     * </p>
     *
     * @throws IOException If it fails to create the temporary files
     */
    @BeforeAll
    static void testSetup() throws IOException {
        final IOBiFunction<Path, Path, Path> symbolicLinkCreator;

        // We can't create symbolic links on Windows without admin privileges,
        // so iff that's our OS, we mock them.
        if (SystemUtils.IS_OS_WINDOWS) {
            symbolicLinkCreator = SymbolicLinkFileFilterTest::createMockSymbolicLink;
            filter = createMockFilter();
        } else {
            symbolicLinkCreator = SymbolicLinkFileFilterTest::createRealSymbolicLink;
            filter = SymbolicLinkFileFilter.INSTANCE;
        }

        testTargetFile = File.createTempFile(TARGET_SHORT_NAME, TARGET_EXT);
        testTargetPath = testTargetFile.toPath();
        parentDirectoryFile = testTargetFile.getParentFile();
        // parent directory
        final Path parentDirectoryPath = parentDirectoryFile.toPath();
        linkName = "SLFF_LinkTo" + testTargetFile.getName();
        testLinkPath = symbolicLinkCreator.apply(parentDirectoryPath.resolve(linkName), testTargetPath);
        testLinkFile = testLinkPath.toFile();
        targetDirPath = Files.createDirectories(parentDirectoryPath.resolve(DIRECTORY_NAME));
        targetDirFile = targetDirPath.toFile();
        testLinkDirPath = symbolicLinkCreator.apply(parentDirectoryPath.resolve(DIRECTORY_LINK_NAME), targetDirPath);
        testLinkDirFile = testLinkDirPath.toFile();
        missingFileFile = new File(parentDirectoryPath.toFile(), MISSING);
        missingFilePath = missingFileFile.toPath();
    }

    @Test
    public void testFileFilter_HardDirectory() {
        assertFalse(filter.accept(targetDirFile));
    }

    @Test
    public void testFileFilter_HardFile() {
        assertFalse(filter.accept(testTargetFile));
    }

    @Test
    public void testFileFilter_Link() {
        assertTrue(filter.accept(testLinkFile));
    }

    @Test
    public void testFileFilter_missingFile() {
        assertFalse(filter.accept(missingFileFile));
    }

    @Test
    public void testFileFilter_PathLink() {
        assertTrue(filter.accept(testLinkDirFile));
    }

    @Test
    public void testFileNameFilter_HardDirectory() {
        assertFalse(filter.accept(parentDirectoryFile, DIRECTORY_NAME));
    }

    @Test
    public void testFileNameFilter_HardFile() {
        assertFalse(filter.accept(parentDirectoryFile, TARGET_NAME));
    }

    @Test
    public void testFileNameFilter_Link() {
        assertTrue(filter.accept(parentDirectoryFile, linkName));
    }

    @Test
    public void testFileNameFilter_missingFile() {
        assertFalse(filter.accept(parentDirectoryFile, MISSING));
    }

    @Test
    public void testFileNameFilter_PathLink() {
        assertTrue(filter.accept(parentDirectoryFile, DIRECTORY_LINK_NAME));
    }

    @Test
    public void testPathFilter_HardDirectory() {
        assertEquals(FileVisitResult.TERMINATE, filter.accept(targetDirPath, null));
        assertFalse(filter.matches(targetDirPath));
    }

    @Test
    public void testPathFilter_HardFile() {
        assertEquals(FileVisitResult.TERMINATE, filter.accept(testTargetPath, null));
        assertFalse(filter.matches(testTargetPath));
    }

    @Test
    public void testPathFilter_Link() {
        assertEquals(FileVisitResult.CONTINUE, filter.accept(testLinkPath, null));
        assertTrue(filter.matches(testLinkPath));

    }

    @Test
    public void testPathFilter_missingFile() {
        assertEquals(FileVisitResult.TERMINATE, filter.accept(missingFilePath, null));
        assertFalse(filter.matches(missingFilePath));
    }

    @Test
    public void testPathFilter_PathLink() {
        assertEquals(FileVisitResult.CONTINUE, filter.accept(testLinkDirPath, null));
        assertTrue(filter.matches(testLinkDirPath));
    }

    @Test
    public void testSymbolicLinkFileFilter() {
        assertEquals(FileVisitResult.TERMINATE, SymbolicLinkFileFilter.INSTANCE.accept(PathUtils.current(), null));
        assertFalse(filter.matches(PathUtils.current()));
    }
}
