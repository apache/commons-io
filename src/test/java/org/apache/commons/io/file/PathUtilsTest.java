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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsTest extends AbstractTempDirTest {

    private static final String STRING_FIXTURE = "Hello World";

    private static final byte[] BYTE_ARRAY_FIXTURE = STRING_FIXTURE.getBytes(StandardCharsets.UTF_8);

    private static final String TEST_JAR_NAME = "test.jar";

    private static final String TEST_JAR_PATH = "src/test/resources/org/apache/commons/io/test.jar";

    private static final String PATH_FIXTURE = "NOTICE.txt";

    /**
     * Creates directory test fixtures.
     * <ol>
     * <li>tempDirPath/subdir</li>
     * <li>tempDirPath/symlinked-dir -> tempDirPath/subdir</li>
     * </ol>
     *
     * @return Path to tempDirPath/subdir
     * @throws IOException if an I/O error occurs or the parent directory does not exist.
     */
    private Path createTempSymlinkedRelativeDir() throws IOException {
        final Path targetDir = tempDirPath.resolve("subdir");
        final Path symlinkDir = tempDirPath.resolve("symlinked-dir");
        Files.createDirectory(targetDir);
        Files.createSymbolicLink(symlinkDir, targetDir);
        return symlinkDir;
    }

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
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("f1")));

            // absolute jar -> absolute dir
            sourceDir = archive.getPath("/next");
            PathUtils.copyDirectory(sourceDir, tempDirPath);
            assertTrue(Files.exists(tempDirPath.resolve("dir")));
        }
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
        try (final FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
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
        try (final FileSystem archive = openArchive(archivePath, false);
            final FileSystem targetArchive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
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
        try (final FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
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
        final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }

    @Test
    public void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDirPath.getParent(), PathUtils.createParentDirectories(tempDirPath));
    }

    @SuppressWarnings("resource") // FileSystems.getDefault() is a singleton
    @Test
    public void testCreateDirectoriesForRoots() throws IOException {
        for (final Path path : FileSystems.getDefault().getRootDirectories()) {
            final Path parent = path.getParent();
            assertNull(parent);
            assertEquals(parent, PathUtils.createParentDirectories(path));
        }
    }

    @Test
    public void testCreateDirectoriesForRootsLinkOptionNull() throws IOException {
        for (final File f : File.listRoots()) {
            final Path path = f.toPath();
            assertEquals(path.getParent(), PathUtils.createParentDirectories(path, (LinkOption) null));
        }
    }

    @Test
    public void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDirPath, PathUtils.createParentDirectories(tempDirPath.resolve("child")));
    }

    @Test
    public void testCreateDirectoriesSymlink() throws IOException {
        final Path symlinkedDir = createTempSymlinkedRelativeDir();
        final String leafDirName = "child";
        final Path newDirFollowed = PathUtils.createParentDirectories(symlinkedDir.resolve(leafDirName), PathUtils.NULL_LINK_OPTION);
        assertEquals(Files.readSymbolicLink(symlinkedDir), newDirFollowed);
    }

    @Test
    public void testCreateDirectoriesSymlinkClashing() throws IOException {
        final Path symlinkedDir = createTempSymlinkedRelativeDir();
        assertThrowsExactly(FileAlreadyExistsException.class, () -> PathUtils.createParentDirectories(symlinkedDir.resolve("child")));
    }

    @Test
    public void testGetTempDirectory() {
        final Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        assertEquals(tempDirectory, PathUtils.getTempDirectory());
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertFalse(PathUtils.isDirectory(null));

        assertTrue(PathUtils.isDirectory(tempDirPath));
        final Path testFile1 = Files.createTempFile(tempDirPath, "prefix", null);
        assertFalse(PathUtils.isDirectory(testFile1));

        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        Files.delete(tempDir);
        assertFalse(PathUtils.isDirectory(tempDir));
    }

    @Test
    public void testIsPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(PathUtils.current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.isPosix(PathUtils.current()));
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertFalse(PathUtils.isRegularFile(null));

        assertFalse(PathUtils.isRegularFile(tempDirPath));
        final Path testFile1 = Files.createTempFile(tempDirPath, "prefix", null);
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
    public void testNewOutputStreamNewFileInsideExistingSymlinkedDir() throws IOException {
        final Path symlinkDir = createTempSymlinkedRelativeDir();
        final Path file = symlinkDir.resolve("test.txt");
        try (OutputStream outputStream = PathUtils.newOutputStream(file, new LinkOption[] {})) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, null)) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, true)) {
            // empty
        }
        try (OutputStream outputStream = PathUtils.newOutputStream(file, false)) {
            // empty
        }
    }

    @Test
    public void testReadAttributesPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(PathUtils.current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.readAttributes(PathUtils.current(), PosixFileAttributes.class) != null);
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
        final Path resolved = tempDirPath.resolve("testSetReadOnlyFile.txt");
        // Ask now, as we are allowed before editing parent permissions.
        final boolean isPosix = PathUtils.isPosix(tempDirPath);

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
        assertTrue(Files.isReadable(resolved), "isReadable");
        assertTrue(Files.isWritable(resolved), "isWritable");
        // Again, shouldn't blow up.
        PathUtils.setReadOnly(resolved, false);
        assertTrue(Files.isReadable(resolved), "isReadable");
        assertTrue(Files.isWritable(resolved), "isWritable");
        //
        assertEquals(regularFile, Files.isReadable(resolved));
        assertEquals(executable, Files.isExecutable(resolved));
        assertEquals(hidden, Files.isHidden(resolved));
        assertEquals(directory, Files.isDirectory(resolved));
        assertEquals(symbolicLink, Files.isSymbolicLink(resolved));
        // Test B
        PathUtils.setReadOnly(resolved, true);
        if (isPosix) {
            // On POSIX, now that the parent is not WX, the file is not readable.
            assertFalse(Files.isReadable(resolved), "isReadable");
        } else {
            assertTrue(Files.isReadable(resolved), "isReadable");
        }
        assertFalse(Files.isWritable(resolved), "isWritable");
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(resolved);
        if (dosFileAttributeView != null) {
            assertTrue(dosFileAttributeView.readAttributes().isReadOnly());
        }
        if (isPosix) {
            assertFalse(Files.isReadable(resolved));
        } else {
            assertEquals(regularFile, Files.isReadable(resolved));
        }
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
        final Path file = tempDirPath.resolve("write.txt");
        PathUtils.writeString(file, "Hello /u1234", StandardCharsets.UTF_8);
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    /**
     * Tests newOutputStream() here and don't use Files.write obviously.
     */
    private Path writeToNewOutputStream(final boolean append) throws IOException {
        final Path file = tempDirPath.resolve("test1.txt");
        try (OutputStream os = PathUtils.newOutputStream(file, append)) {
            os.write(BYTE_ARRAY_FIXTURE);
        }
        return file;
    }

}
