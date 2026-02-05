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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
