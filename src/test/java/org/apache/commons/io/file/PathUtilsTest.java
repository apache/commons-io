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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

/**
 * Tests {@link PathUtils}.
 */
class PathUtilsTest extends AbstractTempDirTest {

    private static final String STRING_FIXTURE = "Hello World";

    private static final byte[] BYTE_ARRAY_FIXTURE = STRING_FIXTURE.getBytes(StandardCharsets.UTF_8);

    private static final String PATH_FIXTURE = "NOTICE.txt";

    private Path current() {
        return PathUtils.current();
    }

    private Long getLastModifiedMillis(final Path file) throws IOException {
        return Files.getLastModifiedTime(file).toMillis();
    }

    private Path getNonExistentPath() {
        return Paths.get("/does not exist/for/certain");
    }

    private void setLastModifiedMillis(final Path file, final long millis) throws IOException {
        Files.setLastModifiedTime(file, FileTime.fromMillis(millis));
    }

    @Test
    void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDirPath.getParent(), PathUtils.createParentDirectories(tempDirPath));
    }

    @SuppressWarnings("resource") // FileSystems.getDefault() is a singleton
    @Test
    void testCreateDirectoriesForRoots() throws IOException {
        for (final Path path : FileSystems.getDefault().getRootDirectories()) {
            final Path parent = path.getParent();
            assertNull(parent);
            assertEquals(parent, PathUtils.createParentDirectories(path));
        }
    }

    @Test
    void testCreateDirectoriesForRootsLinkOptionNull() throws IOException {
        for (final File f : File.listRoots()) {
            final Path path = f.toPath();
            assertEquals(path.getParent(), PathUtils.createParentDirectories(path, (LinkOption) null));
        }
    }

    @Test
    void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDirPath, PathUtils.createParentDirectories(tempDirPath.resolve("child")));
    }

    @Test
    void testCreateDirectoriesSymlink() throws IOException {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
        final String leafDirName = "child";
        final Path newDirFollowed = PathUtils.createParentDirectories(symlinkedDir.resolve(leafDirName), PathUtils.NULL_LINK_OPTION);
        assertEquals(Files.readSymbolicLink(symlinkedDir), newDirFollowed);
    }

    @Test
    void testCreateDirectoriesSymlinkClashing() throws IOException {
        final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
        assertEquals(symlinkedDir, PathUtils.createParentDirectories(symlinkedDir.resolve("child")));
    }

    @Test
    void testGetBaseNamePathBaseCases() {
        assertEquals("bar", PathUtils.getBaseName(Paths.get("a/b/c/bar.foo")));
        assertEquals("foo", PathUtils.getBaseName(Paths.get("foo")));
        assertEquals("", PathUtils.getBaseName(Paths.get("")));
        assertEquals("", PathUtils.getBaseName(Paths.get(".")));
        for (final File f : File.listRoots()) {
            assertNull(PathUtils.getBaseName(f.toPath()));
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            assertNull(PathUtils.getBaseName(Paths.get("C:\\")));
        }
    }

    @Test
    void testGetBaseNamePathCornerCases() {
        assertNull(PathUtils.getBaseName((Path) null));
        assertEquals("foo", PathUtils.getBaseName(Paths.get("foo.")));
        assertEquals("", PathUtils.getBaseName(Paths.get("bar/.foo")));
    }

    @Test
    void testGetDosFileAttributeView() {
        // dir
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(current());
        final Path path = Paths.get("this-file-does-not-exist-at.all");
        assertFalse(Files.exists(path));
        if (SystemUtils.IS_OS_MAC) {
            assertNull(dosFileAttributeView);
            // missing file
            assertNull(PathUtils.getDosFileAttributeView(path));
        } else {
            assertNotNull(dosFileAttributeView);
            // missing file
            assertNotNull(PathUtils.getDosFileAttributeView(path));
        }
        // null
        assertThrows(NullPointerException.class, () -> PathUtils.getDosFileAttributeView(null));
    }

    @Test
    void testGetExtension() {
        assertNull(PathUtils.getExtension(null));
        assertEquals("ext", PathUtils.getExtension(Paths.get("file.ext")));
        assertEquals("", PathUtils.getExtension(Paths.get("README")));
        assertEquals("com", PathUtils.getExtension(Paths.get("domain.dot.com")));
        assertEquals("jpeg", PathUtils.getExtension(Paths.get("image.jpeg")));
        assertEquals("", PathUtils.getExtension(Paths.get("a.b/c")));
        assertEquals("txt", PathUtils.getExtension(Paths.get("a.b/c.txt")));
        assertEquals("", PathUtils.getExtension(Paths.get("a/b/c")));
        assertEquals("", PathUtils.getExtension(Paths.get("a.b\\c")));
        assertEquals("txt", PathUtils.getExtension(Paths.get("a.b\\c.txt")));
        assertEquals("", PathUtils.getExtension(Paths.get("a\\b\\c")));
        assertEquals("", PathUtils.getExtension(Paths.get("C:\\temp\\foo.bar\\README")));
        assertEquals("ext", PathUtils.getExtension(Paths.get("../filename.ext")));

        if (File.separatorChar != '\\') {
            // Upwards compatibility:
            assertEquals("txt", PathUtils.getExtension(Paths.get("foo.exe:bar.txt")));
        }
    }

    @Test
    void testGetFileName() {
        assertNull(PathUtils.getFileName(null, null));
        assertNull(PathUtils.getFileName(null, Path::toString));
        assertNull(PathUtils.getFileName(Paths.get("/"), Path::toString));
        assertNull(PathUtils.getFileName(Paths.get("/"), Path::toString));
        assertEquals("", PathUtils.getFileName(Paths.get(""), Path::toString));
        assertEquals("a", PathUtils.getFileName(Paths.get("a"), Path::toString));
        assertEquals("a", PathUtils.getFileName(Paths.get("p", "a"), Path::toString));
    }

    @Test
    void testGetFileNameString() {
        assertNull(PathUtils.getFileNameString(Paths.get("/")));
        assertEquals("", PathUtils.getFileNameString(Paths.get("")));
        assertEquals("a", PathUtils.getFileNameString(Paths.get("a")));
        assertEquals("a", PathUtils.getFileNameString(Paths.get("p", "a")));
    }

    @Test
    void testGetLastModifiedFileTime_File_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toFile()));
    }

    @Test
    void testGetLastModifiedFileTime_Path_Absent() throws IOException {
        assertNull(PathUtils.getLastModifiedFileTime(getNonExistentPath()));
    }

    @Test
    void testGetLastModifiedFileTime_Path_FileTime_Absent() throws IOException {
        final FileTime fromMillis = FileTime.fromMillis(0);
        assertEquals(fromMillis, PathUtils.getLastModifiedFileTime(getNonExistentPath(), fromMillis));
    }

    @Test
    void testGetLastModifiedFileTime_Path_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current()));
    }

    @Test
    void testGetLastModifiedFileTime_URI_Present() throws IOException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toUri()));
    }

    @Test
    void testGetLastModifiedFileTime_URL_Present() throws IOException, URISyntaxException {
        assertNotNull(PathUtils.getLastModifiedFileTime(current().toUri().toURL()));
    }

    @Test
    void testGetPath() {
        final String validKey = "user.dir";
        final Path value = Paths.get(System.getProperty(validKey));
        assertEquals(value, PathUtils.getPath(validKey, null));
        assertEquals(value, PathUtils.getPath(validKey, validKey));
        final String invalidKey = "this property key does not exist";
        assertEquals(value, PathUtils.getPath(invalidKey, value.toString()));
        assertNull(PathUtils.getPath(invalidKey, null));
        assertEquals(value, PathUtils.getPath(null, value.toString()));
        assertEquals(value, PathUtils.getPath("", value.toString()));
    }

    @Test
    void testGetTempDirectory() {
        final Path tempDirectory = Paths.get(SystemProperties.getJavaIoTmpdir());
        assertEquals(tempDirectory, PathUtils.getTempDirectory());
    }

    @Test
    void testIsDirectory() throws IOException {
        assertFalse(PathUtils.isDirectory(null));

        assertTrue(PathUtils.isDirectory(tempDirPath));
        try (TempFile testFile1 = TempFile.create(tempDirPath, "prefix", null)) {
            assertFalse(PathUtils.isDirectory(testFile1.get()));

            Path ref = null;
            try (TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
                ref = tempDir.get();
                assertTrue(PathUtils.isDirectory(tempDir.get()));
            }
            assertFalse(PathUtils.isDirectory(ref));
        }
    }

    @Test
    void testIsPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.isPosix(current()));
    }

    @Test
    void testIsPosixAbsentFile() {
        assertFalse(PathUtils.isPosix(Paths.get("ImNotHereAtAllEver.never")));
        assertFalse(PathUtils.isPosix(null));
    }

    @Test
    void testIsRegularFile() throws IOException {
        assertFalse(PathUtils.isRegularFile(null));

        assertFalse(PathUtils.isRegularFile(tempDirPath));
        try (TempFile testFile1 = TempFile.create(tempDirPath, "prefix", null)) {
            assertTrue(PathUtils.isRegularFile(testFile1.get()));

            Files.delete(testFile1.get());
            assertFalse(PathUtils.isRegularFile(testFile1.get()));
        }
    }

    @Test
    void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (DirectoryStream<Path> stream = PathUtils.newDirectoryStream(current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, PathUtils.getFileNameString(path));
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void testNewOutputStreamExistingFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
        testNewOutputStreamNewFile(false);
    }

    @Test
    void testNewOutputStreamExistingFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
        final Path file = writeToNewOutputStream(true);
        assertArrayEquals(ArrayUtils.addAll(BYTE_ARRAY_FIXTURE, BYTE_ARRAY_FIXTURE), Files.readAllBytes(file));
    }

    void testNewOutputStreamNewFile(final boolean append) throws IOException {
        final Path file = writeToNewOutputStream(append);
        assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
    }

    @Test
    void testNewOutputStreamNewFileAppendFalse() throws IOException {
        testNewOutputStreamNewFile(false);
    }

    @Test
    void testNewOutputStreamNewFileAppendTrue() throws IOException {
        testNewOutputStreamNewFile(true);
    }

    @Test
    void testNewOutputStreamNewFileInsideExistingSymlinkedDir() throws IOException {
        final Path symlinkDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
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
    void testReadAttributesPosix() throws IOException {
        boolean isPosix;
        try {
            Files.getPosixFilePermissions(current());
            isPosix = true;
        } catch (final UnsupportedOperationException e) {
            isPosix = false;
        }
        assertEquals(isPosix, PathUtils.readAttributes(current(), PosixFileAttributes.class) != null);
    }

    @Test
    void testReadStringEmptyFile() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-empty.bin");
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(StringUtils.EMPTY, PathUtils.readString(path, null));
    }

    @Test
    void testReadStringSimpleUtf8() throws IOException {
        final Path path = Paths.get("src/test/resources/org/apache/commons/io/test-file-simple-utf8.bin");
        final String expected = "ABC\r\n";
        assertEquals(expected, PathUtils.readString(path, StandardCharsets.UTF_8));
        assertEquals(expected, PathUtils.readString(path, null));
    }

    @Test
    void testSetReadOnlyFile() throws IOException {
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
    void testSetReadOnlyFileAbsent() {
        assertThrows(IOException.class, () -> PathUtils.setReadOnly(Paths.get("does-not-exist-at-all-ever-never"), true));
    }

    @Test
    void testTouch() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtils.touch(null));

        final Path file = managedTempDirPath.resolve("touch.txt");
        Files.deleteIfExists(file);
        assertFalse(Files.exists(file), "Bad test: test file still exists");
        PathUtils.touch(file);
        assertTrue(Files.exists(file), "touch() created file");
        try (OutputStream out = Files.newOutputStream(file)) {
            assertEquals(0, Files.size(file), "Created empty file.");
            out.write(0);
        }
        assertEquals(1, Files.size(file), "Wrote one byte to file");
        final long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        setLastModifiedMillis(file, y2k); // 0L fails on Win98
        assertEquals(y2k, getLastModifiedMillis(file), "Bad test: set lastModified set incorrect value");
        final long nowMillis = System.currentTimeMillis();
        PathUtils.touch(file);
        assertEquals(1, Files.size(file), "FileUtils.touch() didn't empty the file.");
        assertNotEquals(y2k, getLastModifiedMillis(file), "FileUtils.touch() changed lastModified");
        final int delta = 3000;
        assertTrue(getLastModifiedMillis(file) >= nowMillis - delta, "FileUtils.touch() changed lastModified to more than now-3s");
        assertTrue(getLastModifiedMillis(file) <= nowMillis + delta, "FileUtils.touch() changed lastModified to less than now+3s");
    }

    @Test
    void testWriteStringToFile1() throws Exception {
        final Path file = tempDirPath.resolve("write.txt");
        PathUtils.writeString(file, "Hello \u1234", StandardCharsets.UTF_8);
        final byte[] text = "Hello \u1234".getBytes(StandardCharsets.UTF_8);
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
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, NOFOLLOW_LINKS);

        // Then
        assertEquals(0L, pathCounters.getByteCounter().get());
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
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, NOFOLLOW_LINKS);

        // Then
        assertEquals(0L, pathCounters.getByteCounter().get());
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
        final Path file = Files.write(sourceDir.resolve("file"), BYTE_ARRAY_FIXTURE);
        // source/dir/symlink -> ../file
        Files.createSymbolicLink(dir.resolve("symlink"), dir.relativize(file));
        final Path targetDir = tempDirPath.resolve("target");

        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, NOFOLLOW_LINKS);

        // Then
        assertEquals(11L, pathCounters.getByteCounter().get());
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
        final Path file = Files.write(externalDir.resolve("file"), BYTE_ARRAY_FIXTURE);
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir = Files.createDirectory(sourceDir.resolve("dir"));
        // source/dir/symlink -> /tmp/file
        Files.createSymbolicLink(dir.resolve("symlink"), file.toAbsolutePath());
        final Path targetDir = tempDirPath.resolve("target");

        // When
        final PathCounters pathCounters = PathUtils.copyDirectory(sourceDir, targetDir, NOFOLLOW_LINKS);

        // Then
        assertEquals(0L, pathCounters.getByteCounter().get());
        assertEquals(2L, pathCounters.getDirectoryCounter().get());
        assertEquals(1L, pathCounters.getFileCounter().get());
        final Path copyOfAbsoluteSymlinkToFile = targetDir.resolve("dir").resolve("symlink");
        assertTrue(Files.isSymbolicLink(copyOfAbsoluteSymlinkToFile));
        assertTrue(Files.isRegularFile(copyOfAbsoluteSymlinkToFile));
        // Verify that /tmp/target/dir/symlink resolves to /tmp/source/file
        assertEquals(file.toRealPath(), copyOfAbsoluteSymlinkToFile.toRealPath());
    }

    @Test
    void testCopyDirectoryThrowsOnCyclicSymbolicLink() throws Exception {
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir1 = Files.createDirectory(sourceDir.resolve("dir1"));
        final Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
        Files.createSymbolicLink(dir2.resolve("cyclic-symlink"), dir2.relativize(dir1));
        final Path targetDir = tempDirPath.resolve("target");

        assertThrows(FileSystemLoopException.class, () -> PathUtils.copyDirectory(sourceDir, targetDir));

        assertTrue(Files.exists(targetDir));
        final Path copyOfDir2 = targetDir.resolve("dir1").resolve("dir2");
        assertTrue(Files.exists(copyOfDir2));
        assertTrue(Files.isDirectory(copyOfDir2));
        assertFalse(Files.exists(copyOfDir2.resolve("cyclic-symlink")));
    }

    @Test
    void testCopyDirectoryWithNoFollowLinksPreservesCyclicSymbolicLink() throws Exception {
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir1 = Files.createDirectory(sourceDir.resolve("dir1"));
        final Path dir2 = Files.createDirectory(dir1.resolve("dir2"));
        Files.createSymbolicLink(dir2.resolve("cyclic-symlink"), dir2.relativize(dir1));
        final Path targetDir = tempDirPath.resolve("target");

        PathUtils.copyDirectory(sourceDir, targetDir, NOFOLLOW_LINKS);

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
    void testCopyDirectoryIgnoresBrokenSymbolicLink(CopyOption... copyOptions) throws Exception {
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
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of((Object) new CopyOption[0]),
                    Arguments.of((Object) new CopyOption[] { NOFOLLOW_LINKS })
            );
        }
    }

    @Test
    void testCopyDirectoryFollowsAbsoluteSymbolicLinkToDirectory() throws Exception {
        // Given
        final Path externalDir = Files.createDirectory(tempDirPath.resolve("external"));
        final Path dir1 = Files.createDirectory(externalDir.resolve("dir1"));
        final Path file2 = Files.write(dir1.resolve("file2"), BYTE_ARRAY_FIXTURE);
        final Path sourceDir = Files.createDirectory(tempDirPath.resolve("source"));
        final Path dir3 = Files.createDirectory(sourceDir.resolve("dir3"));
        final Path file4 = Files.write(dir3.resolve("file4"), BYTE_ARRAY_FIXTURE);
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
        assertEquals(66L, pathCounters.getByteCounter().get());
        assertEquals(4L, pathCounters.getDirectoryCounter().get());
        assertEquals(6L, pathCounters.getFileCounter().get());
        assertTrue(Files.exists(targetDir.resolve("dir3").resolve("file4")));
        assertTrue(Files.exists(targetDir.resolve("dir3").resolve("symlink4")));
    }
}
