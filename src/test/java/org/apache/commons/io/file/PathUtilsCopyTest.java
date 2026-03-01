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

package org.apache.commons.io.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

/**
 * Tests {@link PathUtils}.
 */
class PathUtilsCopyTest extends AbstractTempDirTest {

    private static final String TEST_JAR_NAME = "test.jar";

    private static final String TEST_JAR_PATH = "src/test/resources/org/apache/commons/io/test.jar";

    private FileSystem openArchive(final Path p, final boolean createNew) throws IOException {
        if (createNew) {
            final Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            final URI fileUri = p.toAbsolutePath().toUri();
            final URI uri = URI.create("jar:" + fileUri.toASCIIString());
            return FileSystems.newFileSystem(uri, env, null);
        }
        return FileSystems.newFileSystem(p, (ClassLoader) null);
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (FileSystem archive = openArchive(archivePath, false)) {
            // relative jar -> absolute dir
            Path sourceDir = archive.getPath("dir1");
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("f1")));
            // absolute jar -> absolute dir
            sourceDir = archive.getPath("/next");
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("dir")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
        try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            // absolute dir -> relative jar
            Path targetDir = archive.getPath("target");
            Files.createDirectory(targetDir);
            final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2").toAbsolutePath();
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
            // absolute dir -> absolute jar
            targetDir = archive.getPath("/");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (FileSystem archive = openArchive(archivePath, false); FileSystem targetArchive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            final Path targetDir = targetArchive.getPath("targetDir");
            Files.createDirectory(targetDir);
            // relative jar -> relative dir
            Path sourceDir = archive.getPath("next");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dir")));
            // absolute jar -> relative dir
            sourceDir = archive.getPath("/dir1");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("f1")));
        }
    }

    @Test
    void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
        try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
            // relative dir -> relative jar
            Path targetDir = archive.getPath("target");
            Files.createDirectory(targetDir);
            final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
            // relative dir -> absolute jar
            targetDir = archive.getPath("/");
            PathUtils.copyDirectory(sourceDir, targetDir);
            assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
        }
    }

    /**
     * Source tree:
     * <pre>
     *
     * source/
     *   dir/
     *     file
     *     symlink-to-file
     *   symlink-to-dir
     * </pre>
     */
    @Disabled
    @Test
    void testCopyDirectoryPreservesSymlinks(@TempDir final Path tempDir) throws Exception {
        final Path sourceDir = Files.createDirectory(tempDir.resolve("source"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        final Path dirLink = Files.createSymbolicLink(sourceDir.resolve("link-to-dir"), dir);
        assertTrue(Files.exists(dirLink));
        final Path file = Files.createFile(dir.resolve("file"));
        final Path fileLink = Files.createSymbolicLink(dir.resolve("link-to-file"), file);
        assertTrue(Files.exists(fileLink));
        final Path targetDir = tempDir.resolve("target");
        PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        final Path copyOfDir = targetDir.resolve("dir");
        assertTrue(Files.exists(copyOfDir));
        final Path copyOfDirLink = targetDir.resolve("link-to-dir");
        assertTrue(Files.exists(copyOfDirLink));
        assertTrue(Files.isSymbolicLink(copyOfDirLink));
        final Path copyOfFileLink = copyOfDir.resolve("link-to-file");
        assertTrue(Files.exists(copyOfFileLink));
        assertTrue(Files.isSymbolicLink(copyOfFileLink));
    }

    @Test
    void testCopyFile() throws IOException {
        final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
        final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    @Test
    void testCopyFileTwoFileSystem() throws IOException {
        try (FileSystem archive = openArchive(Paths.get(TEST_JAR_PATH), false)) {
            final Path sourceFile = archive.getPath("next/dir/test.log");
            final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.size(sourceFile), Files.size(targetFile));
        }
    }

    @Test
    void testCopyURL() throws IOException {
        final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
        final URL url = new URL("file:///" + FilenameUtils.getPath(sourceFile.toAbsolutePath().toString()) + sourceFile.getFileName());
        final Path targetFile = PathUtils.copyFileToDirectory(url, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    /**
     * Illustrates how copy with {@link LinkOption#NOFOLLOW_LINKS} preserves relative symlinks to directories.
     * This simulates to the behavior of Linux {@code cp -r}.
     * Given the source directory structure:
     * <pre>{@code
     * user@host:/tmp$ tree source/
     * source/
     * ├── dir1
     * │   └── symlink -> ../dir2
     * └── dir2
     * }</pre>
     * When doing {@code user@host:/tmp$ cp -r source target}, then the resulting target directory structure is:
     * <pre>{@code
     * user@host:/tmp$ tree target/
     * target/
     * ├── dir1
     * │   └── symlink -> ../dir2
     * └── dir2
     * }</pre>
     */
    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesRelativeSymbolicLinkToDir() throws Exception {
        // Given
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir1 = Files.createDirectory(sourceDir.resolve("dir1"));
        final Path dir2 = Files.createDirectory(sourceDir.resolve("dir2"));
        // source/dir1/symlink -> ../dir2
        Files.createSymbolicLink(dir1.resolve("symlink"), dir1.relativize(dir2));
        final Path targetDir = tempDirPath.resolve("target");
        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        // Then
        // assertEquals(0L, pathCounters.getByteCounter().get());
        assertEquals(3L, pathCounters.getDirectoryCounter().get());
        // Verify that symlink with NOFOLLOW_LINKS counts as file
        assertEquals(1L, pathCounters.getFileCounter().get());
        final Path copyOfDir2 = targetDir.resolve("dir2");
        final Path copyOfRelativeSymlinkToDir2 = targetDir.resolve("dir1").resolve("symlink");
        assertTrue(Files.isSymbolicLink(copyOfRelativeSymlinkToDir2));
        assertTrue(Files.isDirectory(copyOfRelativeSymlinkToDir2));
        // Verify that target/dir1/symlink resolves to /tmp/target/dir2
        assertEquals(copyOfDir2.toRealPath(), copyOfRelativeSymlinkToDir2.toRealPath());
    }

    /**
     * Illustrates how copy with {@link LinkOption#NOFOLLOW_LINKS} preserves absolute symlinks to directories.
     * This simulates to the behavior of Linux {@code cp -r}.
     * Given the source directory structure:
     * <pre>{@code
     * user@host:/tmp$ tree source/ external/
     * source/
     * └── dir
     *     └── symlink -> /tmp/external
     * external/
     * }</pre>
     * When doing {@code user@host:/tmp$ cp -r source target}, then the resulting target directory structure is:
     * <pre>{@code
     * user@host:/tmp$ tree target/
     * target/
     * └── dir
     *     └── symlink -> /tmp/external
     * }</pre>
     */
    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesAbsoluteSymbolicLinkToDir() throws Exception {
        // Given
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path externalDir = Files.createDirectory(tempDirPath.resolve("external"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        // source/dir/symlink -> /tmp/external
        Files.createSymbolicLink(dir.resolve("symlink"), externalDir.toAbsolutePath());
        final Path targetDir = tempDirPath.resolve("target");
        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        // Then
        // assertEquals(0L, pathCounters.getByteCounter().get());
        assertEquals(2L, pathCounters.getDirectoryCounter().get());
        // Verify that symlink with NOFOLLOW_LINKS counts as file
        assertEquals(1L, pathCounters.getFileCounter().get());
        final Path copyOfAbsoluteSymlinkToDir = targetDir.resolve("dir").resolve("symlink");
        assertTrue(Files.isSymbolicLink(copyOfAbsoluteSymlinkToDir));
        assertTrue(Files.isDirectory(copyOfAbsoluteSymlinkToDir));
        // Verify that target/dir/symlink resolves to /tmp/external
        assertEquals(externalDir.toRealPath(), copyOfAbsoluteSymlinkToDir.toRealPath());
    }

    /**
     * Illustrates how copy with {@link LinkOption#NOFOLLOW_LINKS} preserves relative symlinks to files.
     * This simulates to the behavior of Linux {@code cp -r}.
     * Given the source directory structure:
     * <pre>{@code
     * user@host:/tmp$ tree source/
     * source/
     * ├── dir
     * │   └── symlink -> ../file
     * └── file
     * }</pre>
     * When doing {@code user@host:/tmp$ cp -r source target}, then the resulting target directory structure is:
     * <pre>{@code
     * user@host:/tmp$ tree target/
     * target/
     * ├── dir
     * │   └── symlink -> ../file
     * └── file
     * }</pre>
     */
    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesRelativeSymbolicLinkToFile() throws Exception {
        // Given
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        final Path file = Files.write(sourceDir.resolve("file"), PathUtilsTest.BYTE_ARRAY_FIXTURE);
        // source/dir/symlink -> ../file
        Files.createSymbolicLink(dir.resolve("symlink"), dir.relativize(file));
        final Path targetDir = tempDirPath.resolve("target");
        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        // Then
        // assertEquals(11L, pathCounters.getByteCounter().get());
        assertEquals(2L, pathCounters.getDirectoryCounter().get());
        // Verify that file + symlink with NOFOLLOW_LINKS counts as 2 files
        assertEquals(2L, pathCounters.getFileCounter().get());
        final Path copyOfFile = targetDir.resolve("file");
        final Path copyOfRelativeSymlinkToFile = targetDir.resolve("dir").resolve("symlink");
        assertTrue(Files.isSymbolicLink(copyOfRelativeSymlinkToFile));
        assertTrue(Files.isRegularFile(copyOfRelativeSymlinkToFile));
        // Verify that /tmp/target/dir/symlink resolves to /tmp/target/file
        assertEquals(copyOfFile.toRealPath(), copyOfRelativeSymlinkToFile.toRealPath());
    }

    /**
     * Illustrates how copy with {@link LinkOption#NOFOLLOW_LINKS} preserves relative symlinks to files.
     * This simulates to the behavior of Linux {@code cp -r}.
     * Given the source directory structure:
     * <pre>{@code
     * user@host:/tmp$ tree source/
     * source/
     * ├── dir
     * │   └── symlink -> ../file
     * └── file
     * }</pre>
     * When doing {@code user@host:/tmp$ cp -r source target}, then the resulting target directory structure is:
     * <pre>{@code
     * user@host:/tmp$ tree target/
     * target/
     * ├── dir
     * │   └── symlink -> ../file
     * └── file
     * }</pre>
     */
    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesAbsoluteSymbolicLinkToFile() throws Exception {
        // Given
        final Path externalDir = Files.createDirectory(tempDirPath.resolve("external"));
        final Path file = Files.write(externalDir.resolve("file"), PathUtilsTest.BYTE_ARRAY_FIXTURE);
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        // source/dir/symlink -> /tmp/file
        Files.createSymbolicLink(dir.resolve("symlink"), file.toAbsolutePath());
        final Path targetDir = tempDirPath.resolve("target");
        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        // Then
        // assertEquals(0L, pathCounters.getByteCounter().get());
        assertEquals(2L, pathCounters.getDirectoryCounter().get());
        assertEquals(1L, pathCounters.getFileCounter().get());
        final Path copyOfAbsoluteSymlinkToFile = targetDir.resolve("dir").resolve("symlink");
        assertTrue(Files.isSymbolicLink(copyOfAbsoluteSymlinkToFile));
        assertTrue(Files.isRegularFile(copyOfAbsoluteSymlinkToFile));
        // Verify that /tmp/target/dir/symlink resolves to /tmp/source/file
        assertEquals(file.toRealPath(), copyOfAbsoluteSymlinkToFile.toRealPath());
    }

    @Test
    void testCopyDirectoryCyclicSymbolicLink() throws Exception {
        // sourceDir = tempDirPath/source
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        // dir1 = tempDirPath/source/dir1
        final Path dir1 = Files.createDirectory(sourceDir.resolve("dir1"));
        // dir2 = tempDirPath/source/dir1/dir2
        final Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
        // link = tempDirPath/source/dir1/dir2/cyclic-symlink
        // target = ..
        Files.createSymbolicLink(dir2.resolve("cyclic-symlink"), dir2.relativize(dir1));
        final Path targetDir = tempDirPath.resolve("target");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir));
        final Path copyOfDir2 = targetDir.resolve("dir1").resolve("dir2");
        assertTrue(Files.exists(copyOfDir2));
        assertTrue(Files.isDirectory(copyOfDir2));
        assertTrue(Files.exists(copyOfDir2.resolve("cyclic-symlink")));
    }

    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesCyclicSymbolicLink() throws Exception {
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir1 = Files.createDirectory(sourceDir.resolve("dir1"));
        final Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
        Files.createSymbolicLink(dir2.resolve("cyclic-symlink"), dir2.relativize(dir1));
        final Path targetDir = tempDirPath.resolve("target");
        PathUtils.copyDirectory(sourceDir, targetDir, LinkOption.NOFOLLOW_LINKS);
        assertTrue(Files.exists(targetDir));
        final Path copyOfDir1 = targetDir.resolve("dir1");
        final Path copyOfDir2 = copyOfDir1.resolve("dir2");
        assertTrue(Files.exists(copyOfDir2));
        assertTrue(Files.isDirectory(copyOfDir2));
        final Path copyOfCyclicSymlink = copyOfDir2.resolve("cyclic-symlink");
        assertTrue(Files.exists(copyOfCyclicSymlink));
        assertEquals(copyOfDir1.toRealPath(), copyOfCyclicSymlink.toRealPath());
    }

    @ParameterizedTest
    @ArgumentsSource(CopyOptionsArgumentsProvider.class)
    void testCopyDirectoryIgnoresBrokenSymbolicLink(final CopyOption... copyOptions) throws Exception {
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        Files.createSymbolicLink(dir.resolve("broken-symlink"), dir.relativize(sourceDir.resolve("file")));
        final Path targetDir = tempDirPath.resolve("target");
        PathUtils.copyDirectory(sourceDir, targetDir, copyOptions);
        assertTrue(Files.exists(targetDir));
        final Path copyOfDir = targetDir.resolve("dir");
        assertTrue(Files.exists(copyOfDir));
        assertTrue(Files.isDirectory(copyOfDir));
        assertFalse(Files.exists(copyOfDir.resolve("broken-symlink")));
    }

    private static class CopyOptionsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of((Object) new CopyOption[0]),
                    Arguments.of((Object) new CopyOption[] { LinkOption.NOFOLLOW_LINKS })
            );
        }
    }

    @Test
    void testCopyDirectoryFollowsAbsoluteSymbolicLinkToDirectory() throws Exception {
        // Given
        final Path externalDir = Files.createDirectory(tempDirPath.resolve("external"));
        final Path dir1 = Files.createDirectory(externalDir.resolve("dir1"));
        final Path file2 = Files.write(dir1.resolve("file2"), PathUtilsTest.BYTE_ARRAY_FIXTURE);
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir3 = Files.createDirectory(sourceDir.resolve("dir3"));
        final Path file4 = Files.write(dir3.resolve("file4"), PathUtilsTest.BYTE_ARRAY_FIXTURE);
        Files.createSymbolicLink(sourceDir.resolve("symlink1"), dir1.toAbsolutePath());
        Files.createSymbolicLink(sourceDir.resolve("symlink2"), sourceDir.relativize(file2));
        Files.createSymbolicLink(sourceDir.resolve("symlink3"), sourceDir.relativize(dir3));
        Files.createSymbolicLink(dir3.resolve("symlink4"), file4.toAbsolutePath());
        final Path targetDir = tempDirPath.resolve("target");
        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir);
        // Then
        // 6 * 11 bytes == 66:
        // file2
        // file4
        // symlink2 -> file2
        // symlink4 -> file4
        // symlink1 -> dir1 containing file2
        // symlink3 -> dir3 containing file4
        assertTrue(66L <= pathCounters.getByteCounter().get());
        assertEquals(2L, pathCounters.getDirectoryCounter().get());
        assertEquals(5L, pathCounters.getFileCounter().get());
        assertTrue(Files.exists(targetDir.resolve("dir3").resolve("file4")));
        assertTrue(Files.exists(targetDir.resolve("dir3").resolve("symlink4")));
    }
}
