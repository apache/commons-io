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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsTest extends TestArguments {

    private static final String STRING_FIXTURE = "Hello World";

    private static final byte[] BYTE_ARRAY_FIXTURE = STRING_FIXTURE.getBytes(StandardCharsets.UTF_8);

    private static final String TEST_JAR_NAME = "test.jar";

    private static final String TEST_JAR_PATH = "src/test/resources/org/apache/commons/io/test.jar";

    private static final String PATH_FIXTURE = "NOTICE.txt";

    /**
     * A temporary directory managed by JUnit.
     */
    @TempDir
    public Path tempDir;

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
    public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (final FileSystem archive = openArchive(archivePath, false)) {
            // relative jar -> absolute dir
            Path sourceDir = archive.getPath("dir1");
            PathUtils.copyDirectory(sourceDir, tempDir);
            assertTrue(Files.exists(tempDir.resolve("f1")));

            // absolute jar -> absolute dir
            sourceDir = archive.getPath("/next");
            PathUtils.copyDirectory(sourceDir, tempDir);
            assertTrue(Files.exists(tempDir.resolve("dir")));
        }
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
        try (final FileSystem archive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
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
    public void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
        final Path archivePath = Paths.get(TEST_JAR_PATH);
        try (final FileSystem archive = openArchive(archivePath, false); final FileSystem targetArchive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
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
    public void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
        try (final FileSystem archive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
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

    @Test
    public void testCopyFile() throws IOException {
        final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
        final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDir);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    @Test
    public void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDir.getParent(), PathUtils.createParentDirectories(tempDir));
    }

    @Test
    public void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDir, PathUtils.createParentDirectories(tempDir.resolve("child")));
    }

    @Test
    public void testGetTempDirectory() {
        final Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        assertEquals(tempDirectory, PathUtils.getTempDirectory());
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertFalse(PathUtils.isDirectory(null));

        assertTrue(PathUtils.isDirectory(tempDir));
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertFalse(PathUtils.isDirectory(testFile1));

        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        Files.delete(tempDir);
        assertFalse(PathUtils.isDirectory(tempDir));
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertFalse(PathUtils.isRegularFile(null));

        assertFalse(PathUtils.isRegularFile(tempDir));
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertTrue(PathUtils.isRegularFile(testFile1));

        Files.delete(testFile1);
        assertFalse(PathUtils.isRegularFile(testFile1));
    }

    @Test
    public void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (final DirectoryStream<Path> stream = PathUtils.newDirectoryStream(PathUtils.current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, path.getFileName().toString());
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    public void testNewOutputStreamExistingFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
        testNewOutputStreamNewFile(false);
    }

    @Test
    public void testNewOutputStreamExistingFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
        final Path file = writeToNewOutputStream(true);
        assertArrayEquals(ArrayUtils.addAll(BYTE_ARRAY_FIXTURE, BYTE_ARRAY_FIXTURE), Files.readAllBytes(file));
    }

    public void testNewOutputStreamNewFile(final boolean append) throws IOException {
        final Path file = writeToNewOutputStream(append);
        assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
    }

    @Test
    public void testNewOutputStreamNewFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
    }

    @Test
    public void testNewOutputStreamNewFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
    }

    @Test
    public void testReadStringEmptyFile() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty.bin");
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, null));
    }

    @Test
    public void testReadStringSimpleUtf8() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-simple-utf8.bin");
        final String expected = "ABC\r\n";
        assertEquals(expected, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(expected, PathUtils.readString(path, null));
    }

    @Test
    public void testSetReadOnlyFile() throws IOException {
        final Path resolved = tempDir.resolve("testSetReadOnlyFile.txt");

        // TEMP HACK
        assumeFalse(SystemUtils.IS_OS_LINUX);

        PathUtils.writeString(resolved, "test", StandardCharsets.UTF_8);
        final boolean readable = Files.isReadable(resolved);
        final boolean writable = Files.isWritable(resolved);
        final boolean regularFile = Files.isRegularFile(resolved);
        final boolean executable = Files.isExecutable(resolved);
        final boolean hidden = Files.isHidden(resolved);
        final boolean directory = Files.isDirectory(resolved);
        final boolean symbolicLink = Files.isSymbolicLink(resolved);
        // Sanity checks
        assertTrue(readable);
        assertTrue(writable);
        // Test A
        PathUtils.setReadOnly(resolved, false);
        assertTrue(Files.isReadable(resolved));
        assertTrue(Files.isWritable(resolved));
        assertEquals(regularFile, Files.isReadable(resolved));
        assertEquals(executable, Files.isExecutable(resolved));
        assertEquals(hidden, Files.isHidden(resolved));
        assertEquals(directory, Files.isDirectory(resolved));
        assertEquals(symbolicLink, Files.isSymbolicLink(resolved));
        // Test B
        PathUtils.setReadOnly(resolved, true);
        assertTrue(Files.isReadable(resolved));
        assertFalse(Files.isWritable(resolved));
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(resolved);
        if (dosFileAttributeView != null) {
            assertTrue(dosFileAttributeView.readAttributes().isReadOnly());
        }
        final PosixFileAttributeView posixFileAttributeView = PathUtils.getPosixFileAttributeView(resolved);
        if (posixFileAttributeView != null) {
            // Not Windows
            final Set<PosixFilePermission> permissions = posixFileAttributeView.readAttributes().permissions();
            assertFalse(permissions.contains(PosixFilePermission.GROUP_WRITE), permissions::toString);
            assertFalse(permissions.contains(PosixFilePermission.OTHERS_WRITE), permissions::toString);
            assertFalse(permissions.contains(PosixFilePermission.OWNER_WRITE), permissions::toString);
        }
        assertEquals(regularFile, Files.isReadable(resolved));
        assertEquals(executable, Files.isExecutable(resolved));
        assertEquals(hidden, Files.isHidden(resolved));
        assertEquals(directory, Files.isDirectory(resolved));
        assertEquals(symbolicLink, Files.isSymbolicLink(resolved));
        //
        PathUtils.setReadOnly(resolved, false);
        PathUtils.deleteFile(resolved);
    }

    @Test
    public void testWriteStringToFile1() throws Exception {
        final Path file = tempDir.resolve("write.txt");
        PathUtils.writeString(file, "Hello /u1234", StandardCharsets.UTF_8);
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    /**
     * Tests newOutputStream() here and don't use Files.write obviously.
     */
    private Path writeToNewOutputStream(final boolean append) throws IOException {
        final Path file = tempDir.resolve("test1.txt");
        try (OutputStream os = PathUtils.newOutputStream(file, append)) {
            os.write(BYTE_ARRAY_FIXTURE);
        }
        return file;
    }

}
