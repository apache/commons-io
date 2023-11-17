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

package org.apache.commons.io.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsContentEqualsTest {

    @TempDir
    public File temporaryFolder;

    private String getName() {
        return this.getClass().getSimpleName();
    }

    @Test
    public void testDirectoryAndFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertTrue(PathUtils.directoryAndFileContentEquals(null, null));
        assertFalse(PathUtils.directoryAndFileContentEquals(null, path1));
        assertFalse(PathUtils.directoryAndFileContentEquals(path1, null));
        // both don't exist
        assertTrue(PathUtils.directoryAndFileContentEquals(path1, path1));
        assertTrue(PathUtils.directoryAndFileContentEquals(path1, path2));
        assertTrue(PathUtils.directoryAndFileContentEquals(path2, path2));
        assertTrue(PathUtils.directoryAndFileContentEquals(path2, path1));
        // Tree equals true tests
        {
            // Trees of files only that contain the same files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only2");
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
        }
        {
            // Trees of directories containing other directories.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir2");
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
        }
        {
            // Trees of directories containing other directories and files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
        }
        // Tree equals false tests
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/");
            assertFalse(PathUtils.directoryAndFileContentEquals(dir1, dir2));
            assertFalse(PathUtils.directoryAndFileContentEquals(dir2, dir1));
        }
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files");
            assertFalse(PathUtils.directoryAndFileContentEquals(dir1, dir2));
            assertFalse(PathUtils.directoryAndFileContentEquals(dir2, dir1));
        }
    }

    @Test
    public void testDirectoryContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertTrue(PathUtils.directoryContentEquals(null, null));
        assertFalse(PathUtils.directoryContentEquals(null, path1));
        assertFalse(PathUtils.directoryContentEquals(path1, null));
        // both don't exist
        assertTrue(PathUtils.directoryContentEquals(path1, path1));
        assertTrue(PathUtils.directoryContentEquals(path1, path2));
        assertTrue(PathUtils.directoryContentEquals(path2, path2));
        assertTrue(PathUtils.directoryContentEquals(path2, path1));
        // Tree equals true tests
        {
            // Trees of files only that contain the same files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only2");
            assertTrue(PathUtils.directoryContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
        }
        {
            // Trees of directories containing other directories.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir2");
            assertTrue(PathUtils.directoryContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
        }
        {
            // Trees of directories containing other directories and files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            assertTrue(PathUtils.directoryContentEquals(dir1, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
            assertTrue(PathUtils.directoryContentEquals(dir1, dir1));
            assertTrue(PathUtils.directoryContentEquals(dir2, dir2));
        }
        // Tree equals false tests
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/");
            assertFalse(PathUtils.directoryContentEquals(dir1, dir2));
            assertFalse(PathUtils.directoryContentEquals(dir2, dir1));
        }
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files");
            assertFalse(PathUtils.directoryContentEquals(dir1, dir2));
            assertFalse(PathUtils.directoryContentEquals(dir2, dir1));
        }
    }

    @Test
    public void testFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertTrue(PathUtils.fileContentEquals(null, null));
        assertFalse(PathUtils.fileContentEquals(null, path1));
        assertFalse(PathUtils.fileContentEquals(path1, null));
        // both don't exist
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path1));

        // Directories
        assertThrows(IOException.class, () -> PathUtils.fileContentEquals(temporaryFolder.toPath(), temporaryFolder.toPath()));

        // Different files
        final Path objFile1 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object");
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1);

        final Path objFile1b = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object2");
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1b);

        final Path objFile2 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".collection");
        PathUtils.copyFile(getClass().getResource("/java/util/Collection.class"), objFile2);

        assertFalse(PathUtils.fileContentEquals(objFile1, objFile2));
        assertFalse(PathUtils.fileContentEquals(objFile1b, objFile2));
        assertTrue(PathUtils.fileContentEquals(objFile1, objFile1b));

        assertTrue(PathUtils.fileContentEquals(objFile1, objFile1));
        assertTrue(PathUtils.fileContentEquals(objFile1b, objFile1b));
        assertTrue(PathUtils.fileContentEquals(objFile2, objFile2));

        // Equal files
        Files.createFile(path1);
        Files.createFile(path2);
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path2));
    }

    @Test
    public void testFileContentEqualsZip() throws Exception {
        final Path path1 = Paths.get("src/test/resources/org/apache/commons/io/bla.zip");
        final Path path2 = Paths.get("src/test/resources/org/apache/commons/io/bla-copy.zip");
        final Path path3 = Paths.get("src/test/resources/org/apache/commons/io/moby.zip");
        assertTrue(PathUtils.fileContentEquals(path1, path2));
        assertFalse(PathUtils.fileContentEquals(path1, path3));
    }

    @Test
    public void testFileContentEqualsZipFileSystem() throws Exception {
        try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get("src/test/resources/org/apache/commons/io/test-same-size-diff-contents.zip"),
                ClassLoader.getSystemClassLoader())) {
            // Contains one char: A
            final Path path1 = fileSystem.getPath("/test-same-size-diff-contents/A.txt");
            // Contains one char: B
            final Path path2 = fileSystem.getPath("/test-same-size-diff-contents/B.txt");
            assertTrue(Files.exists(path1));
            assertTrue(Files.exists(path2));
            assertTrue(PathUtils.fileContentEquals(path1, path1));
            assertTrue(PathUtils.fileContentEquals(path2, path2));
            assertFalse(PathUtils.fileContentEquals(path1, path2));
        }
    }

}
