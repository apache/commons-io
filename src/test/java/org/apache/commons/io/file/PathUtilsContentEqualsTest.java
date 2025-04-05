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
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsContentEqualsTest {

    static Configuration[] testConfigurations() {
        // @formatter:off
        return new Configuration[] {
                Configuration.osX().toBuilder().setWorkingDirectory("/").build(),
                Configuration.unix().toBuilder().setWorkingDirectory("/").build(),
                Configuration.windows().toBuilder().setWorkingDirectory("C:\\").build()
        };
        // @formatter:on
    }

    @TempDir
    public File temporaryFolder;

    private void assertContentEquals(final FileSystem fileSystem1, final FileSystem fileSystem2) throws IOException {
        assertTrue(PathUtils.contentEquals(fileSystem1, fileSystem2));
        assertTrue(PathUtils.contentEquals(fileSystem2, fileSystem1));
        assertTrue(PathUtils.contentEquals(fileSystem1, fileSystem1));
        assertTrue(PathUtils.contentEquals(fileSystem2, fileSystem2));
    }

    private void assertContentNotEquals(final FileSystem fileSystem1, final FileSystem fileSystem2) throws IOException {
        assertFalse(PathUtils.contentEquals(fileSystem1, fileSystem2));
        assertFalse(PathUtils.contentEquals(fileSystem2, fileSystem1));
        assertTrue(PathUtils.contentEquals(fileSystem1, fileSystem1));
        assertTrue(PathUtils.contentEquals(fileSystem2, fileSystem2));
    }

    private void assertDirectoryAndFileContentEquals(final Path dir1, final Path dir2) throws IOException {
        assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir2));
        assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
        assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir1));
        assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
    }

    private void assertDirectoryAndFileContentNotEquals(final Path dir1, final Path dir2) throws IOException {
        assertFalse(PathUtils.directoryAndFileContentEquals(dir1, dir2));
        assertFalse(PathUtils.directoryAndFileContentEquals(dir2, dir1));
        assertTrue(PathUtils.directoryAndFileContentEquals(dir1, dir1));
        assertTrue(PathUtils.directoryAndFileContentEquals(dir2, dir2));
    }

    private void assertFileContentEquals(final Path path1, final Path path2) throws IOException {
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path1));
    }

    private void assertFileContentNotEquals(final Path path1, final Path path2) throws IOException {
        assertFalse(PathUtils.fileContentEquals(path1, path2));
        assertFalse(PathUtils.fileContentEquals(path2, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path2, path2));
    }

    private String getName() {
        return this.getClass().getSimpleName();
    }

    @ParameterizedTest
    @MethodSource("testConfigurations")
    public void testContentEqualsFileSystemsMemVsMem(final Configuration configuration) throws Exception {
        final Path refDir = Paths.get("src/test/resources/dir-equals-tests");
        try (FileSystem fileSystem1 = Jimfs.newFileSystem(configuration);
                FileSystem fileSystem2 = Jimfs.newFileSystem(configuration)) {
            final Path fsDir1 = fileSystem1.getPath(refDir.getFileName().toString());
            final Path fsDir2 = fileSystem2.getPath(refDir.getFileName().toString());
            assertTrue(PathUtils.copyDirectory(refDir, fsDir1).getByteCounter().get() > 0);
            assertTrue(PathUtils.copyDirectory(refDir, fsDir2).getByteCounter().get() > 0);
            assertContentEquals(fileSystem1, fileSystem2);
        }
    }

    @ParameterizedTest
    @MethodSource("testConfigurations")
    public void testContentEqualsFileSystemsMemVsZip(final Configuration configuration) throws Exception {
        final Path refDir = Paths.get("src/test/resources/dir-equals-tests");
        try (FileSystem fileSystem1 = Jimfs.newFileSystem(configuration);
                FileSystem fileSystem2 = FileSystems.newFileSystem(refDir.resolveSibling(refDir.getFileName() + ".zip"), null)) {
            final Path fsDir1 = fileSystem1.getPath(refDir.getFileName().toString());
            final PathCounters copyDirectory = PathUtils.copyDirectory(refDir, fsDir1);
            assertTrue(copyDirectory.getByteCounter().get() > 0);
            assertContentEquals(fileSystem1, fileSystem2);
        }
    }

    @Test
    public void testContentEqualsFileSystemsZipVsZip() throws Exception {
        final Path zipPath = Paths.get("src/test/resources/dir-equals-tests.zip");
        final Path zipCopy = temporaryFolder.toPath().resolve("copy2.zip");
        Files.copy(zipPath, zipCopy, StandardCopyOption.REPLACE_EXISTING);
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipPath, null); FileSystem fileSystem2 = FileSystems.newFileSystem(zipCopy, null)) {
            assertContentEquals(fileSystem1, fileSystem2);
        }
        final Path emptyZip = Paths.get("src/test/resources/org/apache/commons/io/empty.zip");
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(emptyZip, null); FileSystem fileSystem2 = FileSystems.newFileSystem(emptyZip, null)) {
            assertContentEquals(fileSystem1, fileSystem2);
        }
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipCopy, null); FileSystem fileSystem2 = FileSystems.newFileSystem(emptyZip, null)) {
            assertContentNotEquals(fileSystem1, fileSystem2);
        }
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipPath, null); FileSystem fileSystem2 = FileSystems.newFileSystem(emptyZip, null)) {
            assertContentNotEquals(fileSystem1, fileSystem2);
        }
    }

    @Test
    public void testDirectoryAndFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertDirectoryAndFileContentEquals(null, null);
        assertDirectoryAndFileContentNotEquals(path1, null);
        // both don't exist
        assertDirectoryAndFileContentEquals(path1, path2);
        // Tree equals true tests
        {
            // Trees of files only that contain the same files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only2");
            assertDirectoryAndFileContentEquals(dir1, dir2);
        }
        {
            // Trees of directories containing other directories.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir2");
            assertDirectoryAndFileContentEquals(dir1, dir2);
        }
        {
            // Trees of directories containing other directories and files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            assertDirectoryAndFileContentEquals(dir1, dir2);
        }
        // Tree equals false tests
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/");
            assertDirectoryAndFileContentNotEquals(dir1, dir2);
        }
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files");
            assertDirectoryAndFileContentNotEquals(dir1, dir2);
        }
    }

    /**
     * Tests IO-872 PathUtils.directoryAndFileContentEquals doesn't work across FileSystems.
     *
     * @throws Exception on test failure.
     */
    @Test
    public void testDirectoryAndFileContentEqualsDifferentFileSystemsFileVsZip() throws Exception {
        final Path dir1 = Paths.get("src/test/resources/dir-equals-tests");
        try (FileSystem fileSystem = FileSystems.newFileSystem(dir1.resolveSibling(dir1.getFileName() + ".zip"), null)) {
            final Path dir2 = fileSystem.getPath("/dir-equals-tests");
            // WindowsPath, UnixPath, and ZipPath equals() methods always return false if the argument is not of the same instance as itself.
            assertDirectoryAndFileContentEquals(dir1, dir2);
        }
    }

    /**
     * Tests IO-872 PathUtils.directoryAndFileContentEquals doesn't work across FileSystems.
     *
     * @throws Exception on test failure.
     */
    @Test
    public void testDirectoryAndFileContentEqualsDifferentFileSystemsZipVsZip() throws Exception {
        final Path zipPath = Paths.get("src/test/resources/dir-equals-tests.zip");
        final Path zipCopy = temporaryFolder.toPath().resolve("copy1.zip");
        Files.copy(zipPath, zipCopy, StandardCopyOption.REPLACE_EXISTING);
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipPath, null);
                FileSystem fileSystem2 = FileSystems.newFileSystem(zipCopy, null)) {
            final Path dir1 = fileSystem1.getPath("/dir-equals-tests");
            final Path dir2 = fileSystem2.getPath("/dir-equals-tests");
            // WindowsPath, UnixPath, and ZipPath equals() methods always return false if the argument is not of the same instance as itself.
            assertDirectoryAndFileContentEquals(dir1, dir2);
        }
    }

    /**
     * Tests IO-872 PathUtils.directoryAndFileContentEquals doesn't work across FileSystems.
     *
     * @throws Exception on test failure.
     */
    @Test
    public void testDirectoryAndFileContentEqualsDifferentFileSystemsZipVsZipEmpty() throws Exception {
        final Path zipPath = Paths.get("src/test/resources/dir-equals-tests.zip");
        final Path zipCopy = temporaryFolder.toPath().resolve("copy1.zip");
        final Path emptyZip = Paths.get("src/test/resources/org/apache/commons/io/empty.zip");
        Files.copy(zipPath, zipCopy, StandardCopyOption.REPLACE_EXISTING);
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipPath, null);
                FileSystem fileSystem2 = FileSystems.newFileSystem(emptyZip, null)) {
            final Path dir1 = fileSystem1.getPath("/dir-equals-tests");
            final Path dir2 = fileSystem2.getPath("/");
            // WindowsPath, UnixPath, and ZipPath equals() methods always return false if the argument is not of the same instance as itself.
            assertDirectoryAndFileContentNotEquals(dir1, dir2);
        }
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(zipPath, null);
                FileSystem fileSystem2 = FileSystems.newFileSystem(emptyZip, null)) {
            final Path dir1 = fileSystem1.getPath("/dir-equals-tests");
            final Path dir2 = fileSystem2.getRootDirectories().iterator().next();
            // WindowsPath, UnixPath, and ZipPath equals() methods always return false if the argument is not of the same instance as itself.
            assertDirectoryAndFileContentNotEquals(dir1, dir2);
        }
        Files.copy(emptyZip, zipCopy, StandardCopyOption.REPLACE_EXISTING);
        try (FileSystem fileSystem1 = FileSystems.newFileSystem(emptyZip, null);
                FileSystem fileSystem2 = FileSystems.newFileSystem(zipCopy, null)) {
            final Path dir1 = fileSystem1.getPath("/");
            final Path dir2 = fileSystem2.getPath("/");
            // WindowsPath, UnixPath, and ZipPath equals() methods always return false if the argument is not of the same instance as itself.
            assertDirectoryAndFileContentEquals(dir1, dir2);
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
        assertFileContentNotEquals(path1, null);
        // both don't exist
        assertFileContentEquals(path1, path2);
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
        assertFileContentEquals(path1, path2);
    }

    @Test
    public void testFileContentEqualsZip() throws Exception {
        final Path path1 = Paths.get("src/test/resources/org/apache/commons/io/bla.zip");
        final Path path2 = Paths.get("src/test/resources/org/apache/commons/io/bla-copy.zip");
        // moby.zip is from https://issues.apache.org/jira/browse/COMPRESS-93
        final Path path3 = Paths.get("src/test/resources/org/apache/commons/io/moby.zip");
        assertFileContentEquals(path1, path2);
        assertFileContentNotEquals(path1, path3);
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
            assertFileContentNotEquals(path1, path2);
        }
    }

}
