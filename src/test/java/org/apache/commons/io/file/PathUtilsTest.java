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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Tests {@link PathUtils}.
 */
class PathUtilsTest extends AbstractTempDirTest {

    private static final String STRING_FIXTURE = "Hello World";

    static final byte[] BYTE_ARRAY_FIXTURE = STRING_FIXTURE.getBytes(StandardCharsets.UTF_8);

    private static final String PATH_FIXTURE = "NOTICE.txt";

    private void assertCreateOwnerTempFile(final String prefix, final String suffix, final IOConsumer<Path> assertPermissions) throws IOException {
        final Path file = PathUtils.createOwnerTempFile(prefix, suffix);
        try {
            assertTrue(Files.isRegularFile(file));
            assertEquals(0, Files.size(file));
            assertTrue(prefix == null || file.getFileName().toString().startsWith(prefix));
            assertTrue(file.getFileName().toString().endsWith(suffix == null ? ".tmp" : suffix));
            assertPermissions.accept(file);
            Files.write(file, BYTE_ARRAY_FIXTURE);
            assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
        } finally {
            Files.delete(file);
        }
    }

    private void assertOwnerOnlyAcl(final Path file) throws IOException {
        final AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
        assertNotNull(view);
        assertEquals(Collections.singletonList(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(view.getOwner())
                .setPermissions(EnumSet.allOf(AclEntryPermission.class)).build()), view.getAcl());
    }

    private Path current() {
        return PathUtils.current();
    }

    private Long getLastModifiedMillis(final Path file) throws IOException {
        return Files.getLastModifiedTime(file).toMillis();
    }

    private Path getNonExistentPath() {
        return Paths.get("/does not exist/for/certain");
    }

    /**
     * Runs a native macOS command for ACL regression tests.
     *
     * @param command The executable and its arguments.
     * @return The merged standard output and standard error, decoded as UTF-8.
     * @throws Exception If starting, waiting for, or reading from the command fails.
     * @throws AssertionError If waiting times out or the command exits with a nonzero status.
     */
    private String runMacOsCommand(final String... command) throws Exception {
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Native command did not exit");
            final String output;
            try (InputStream input = process.getInputStream()) {
                output = IOUtils.toString(input, StandardCharsets.UTF_8);
            }
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
            process.getOutputStream().close();
            process.getErrorStream().close();
        }
    }

    private void setLastModifiedMillis(final Path file, final long millis) throws IOException {
        Files.setLastModifiedTime(file, FileTime.fromMillis(millis));
    }

    /**
     * Tests {@link PathUtils#clear(Path)} by writing known non-zero bytes to a temporary file, calling {@code clear()}, and verifying that every byte in the
     * file is {@code 0} and that the file length is unchanged. Also verifies the returned {@link Path} is the same instance.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testClear() throws IOException {
        final Path file = tempDirPath.resolve("testClear.bin");
        final byte[] originalData = { 1, 2, 3, 4, 5, 6, 7, 8 };
        Files.write(file, originalData);
        assertEquals(originalData.length, Files.size(file)); // Sanity check
        // clear() should zero all bytes and return the same Path
        final Path echoRaf = PathUtils.clear(file);
        assertSame(file, echoRaf);
        final byte[] result = Files.readAllBytes(file);
        assertEquals(originalData.length, result.length, "File length must be unchanged after clear()");
        assertArrayEquals(new byte[originalData.length], result, "All bytes should be 0 after clear()");
    }

    /**
     * Tests {@link PathUtils#clearIfExists(Path)} when the path exists: verifies that all bytes are zeroed, the file length is unchanged, and the returned
     * {@link Path} is the same instance.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testClearIfExistsExistingFile() throws IOException {
        final Path file = tempDirPath.resolve("testClearIfExists.bin");
        final byte[] originalData = { 1, 2, 3, 4, 5, 6, 7, 8 };
        Files.write(file, originalData);
        assertEquals(originalData.length, Files.size(file)); // Sanity check
        // clearIfExists() should zero all bytes and return the same Path
        final Path returned = PathUtils.clearIfExists(file);
        assertSame(file, returned, "clearIfExists() should return the given path when it exists");
        final byte[] result = Files.readAllBytes(file);
        assertEquals(originalData.length, result.length, "File length must be unchanged after clearIfExists()");
        assertArrayEquals(new byte[originalData.length], result, "All bytes should be 0 after clearIfExists()");
    }

    /**
     * Tests {@link PathUtils#clearIfExists(Path)} when the path does not exist: verifies the method is a no-op and returns the given (non-existing) path.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testClearIfExistsNonExistentFile() throws IOException {
        final Path nonExistent = tempDirPath.resolve("does-not-exist.bin");
        assertFalse(Files.exists(nonExistent)); // Sanity check
        final Path returned = PathUtils.clearIfExists(nonExistent);
        assertSame(nonExistent, returned, "clearIfExists() should return the given path when it does not exist");
        assertFalse(Files.exists(nonExistent), "clearIfExists() must not create the file when it does not exist");
    }

    /**
     * Tests {@link PathUtils#clearIfExists(Path)} with a {@code null} path: verifies the method returns {@code null} without throwing an exception.
     *
     * @throws IOException Thrown on a test failure.
     */
    @Test
    void testClearIfExistsNull() throws IOException {
        assertNull(PathUtils.clearIfExists(null));
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
    void testCreateDirectoriesSymlinkInCurrentDirectory() throws IOException {
        final Path fixture = Files.createTempDirectory(current().toAbsolutePath().normalize(), "path-utils-");
        final Path link = fixture.resolveSibling(fixture.getFileName() + "-link");
        try {
            final Path target = fixture.getFileName().resolve("target");
            Files.createSymbolicLink(link, target);
            assertEquals(target, PathUtils.createParentDirectories(link.getFileName().resolve("file.txt"), PathUtils.NULL_LINK_OPTION));
            assertTrue(Files.isDirectory(fixture.resolve("target")));
        } finally {
            Files.deleteIfExists(link);
            PathUtils.deleteDirectory(fixture);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows resolves parent traversal differently from POSIX systems")
    void testCreateDirectoriesSymlinkParentTraversal(final boolean absoluteTarget) throws IOException {
        final Path fixture = Files.createTempDirectory(current().toAbsolutePath().normalize(), "path-utils-");
        try {
            final Path linkDir = Files.createDirectories(fixture.resolve("linkDir"));
            final Path nested = Files.createDirectories(linkDir.resolve("other/nested"));
            final Path targetDir = Files.createDirectories(linkDir.resolve(fixture.getFileName()));
            Files.createSymbolicLink(targetDir.resolve("hop"), nested);
            final Path relativeTarget = fixture.getFileName().resolve("hop/../target");
            final Path target = absoluteTarget ? linkDir.resolve(relativeTarget) : relativeTarget;
            final Path link = Files.createSymbolicLink(linkDir.resolve("link"), target);
            assertEquals(linkDir.resolve(relativeTarget), PathUtils.createParentDirectories(link.resolve("file.txt"), PathUtils.NULL_LINK_OPTION));
            assertTrue(Files.isDirectory(linkDir.resolve("other/target")));
            assertFalse(Files.exists(targetDir.resolve("target")));
            assertFalse(Files.exists(fixture.resolve("target")));
        } finally {
            PathUtils.deleteDirectory(fixture);
        }
    }

    @ParameterizedTest
    @CsvSource({ "false,false", "false,true", "true,false", "true,true" })
    void testCreateDirectoriesSymlinkRelativeTarget(final boolean existingTarget, final boolean relativeInput) throws IOException {
        // Keep both the intended target and any incorrectly CWD-relative target inside a unique, owned fixture.
        final Path fixture = Files.createTempDirectory(current().toAbsolutePath().normalize(), "path-utils-");
        try {
            final Path linkDir = Files.createDirectories(fixture.resolve("linkDir"));
            final Path target = fixture.getFileName().resolve("target");
            final Path expected = linkDir.resolve(target);
            if (existingTarget) {
                Files.createDirectories(expected);
            }
            final Path link = Files.createSymbolicLink(linkDir.resolve("link"), target);
            final Path input = relativeInput ? current().toAbsolutePath().normalize().relativize(link) : link;
            final Path parent = PathUtils.createParentDirectories(input.resolve("file.txt"), PathUtils.NULL_LINK_OPTION);
            // Files.createDirectories() may return an absolute path when it creates missing ancestors.
            assertEquals(expected.toAbsolutePath(), parent.toAbsolutePath());
            assertTrue(Files.isDirectory(expected));
            assertFalse(Files.exists(fixture.resolve("target")));
        } finally {
            PathUtils.deleteDirectory(fixture);
        }
    }

    @Test
    void testCreateDirectoriesSymlinkRelativeTargetNoFollow() throws IOException {
        final Path link = Files.createSymbolicLink(tempDirPath.resolve("link"), Paths.get("target"));
        assertEquals(link, PathUtils.createParentDirectories(link.resolve("file.txt")));
        assertEquals(link, PathUtils.createParentDirectories(link.resolve("file.txt"), LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(tempDirPath.resolve("target")));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testCreateOwnerTempFileAclMustBeAtomic(final boolean posix) throws IOException {
        // Jimfs exposes ACLs but does not support setting them as initial creation attributes for files or directories.
        final String[] views = posix ? new String[] { "owner", "acl", "posix" } : new String[] { "owner", "acl" };
        final Configuration configuration = Configuration.windows().toBuilder().setAttributeViews("basic", views).build();
        try (FileSystem fileSystem = Jimfs.newFileSystem(configuration)) {
            final Path directory = Files.createDirectory(fileSystem.getPath("C:\\temp"));
            assertThrows(UnsupportedOperationException.class, () -> PathUtils.createOwnerTempFile(directory, "owner-", ".tmp"));
            assertTrue(PathUtils.isEmptyDirectory(directory));
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testCreateOwnerTempFileDoesNotInheritAcl() throws IOException {
        assumeTrue(Files.getFileStore(tempDirPath).supportsFileAttributeView(AclFileAttributeView.class));
        final AclFileAttributeView parentView = Files.getFileAttributeView(tempDirPath, AclFileAttributeView.class);
        final UserPrincipal owner = parentView.getOwner();
        // Reuse a real principal so this also works with localized Windows account names.
        final List<AclEntry> originalAcl = parentView.getAcl();
        final UserPrincipal other = originalAcl.stream().map(AclEntry::principal).filter(principal -> !owner.equals(principal)).findFirst().orElse(null);
        assumeTrue(other != null);
        final AclEntry ownerEntry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class)).setFlags(AclEntryFlag.FILE_INHERIT).build();
        final AclEntry otherEntry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(other)
                .setPermissions(AclEntryPermission.READ_DATA).setFlags(AclEntryFlag.FILE_INHERIT).build();
        try {
            parentView.setAcl(Arrays.asList(ownerEntry, otherEntry));
            final Path inherited = Files.createTempFile(tempDirPath, "inherited-", ".tmp");
            assertTrue(PathUtils.getAclEntryList(inherited).stream().anyMatch(entry -> entry.type() == AclEntryType.ALLOW
                    && entry.principal().equals(other) && entry.permissions().contains(AclEntryPermission.READ_DATA)));
            final Path file;
            try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
                files.when(() -> Files.createTempFile(any(Path.class), any(String.class), any(String.class), any(FileAttribute.class)))
                        .thenAnswer(invocation -> {
                            final Path created = (Path) invocation.callRealMethod();
                            // Check before the implementation replaces the file ACL; checking only the returned file misses an access window.
                            assertTrue(PathUtils.getAclEntryList(created).isEmpty());
                            assertOwnerOnlyAcl(created.getParent());
                            return created;
                        });
                file = PathUtils.createOwnerTempFile(tempDirPath, "owner-", ".tmp");
                files.verify(() -> Files.createTempFile(any(Path.class), any(String.class), any(String.class), any(FileAttribute.class)));
            }
            assertOwnerOnlyAcl(file);
            Files.write(file, BYTE_ARRAY_FIXTURE);
            assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
            Files.delete(file);
        } finally {
            parentView.setAcl(originalAcl);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "file_inherit", "file_inherit,directory_inherit" })
    @EnabledOnOs(OS.MAC)
    void testCreateOwnerTempFileDoesNotInheritMacOsAcl(final String inheritance) throws Exception {
        final Path directory = Files.createDirectory(tempDirPath.resolve("ACL test ' ; &"));
        runMacOsCommand("/bin/chmod", "+a", "everyone allow read,write,execute," + inheritance, directory.toString());
        final Path inherited = Files.createTempFile(directory, "inherited-", ".tmp",
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        assertTrue(runMacOsCommand("/bin/ls", "-lde", inherited.toString()).contains("inherited allow read,write,execute"));
        Files.delete(inherited);
        final Path file;
        try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.createTempFile(any(Path.class), any(String.class), any(String.class), any(FileAttribute.class)))
                    .thenAnswer(invocation -> {
                        final Path created = (Path) invocation.callRealMethod();
                        // Check at creation, before any subsequent operation could strip an inherited ACL from the file.
                        assertEquals(1, runMacOsCommand("/bin/ls", "-lde", created.toString()).split("\\R").length);
                        assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(created.getParent()));
                        return created;
                    });
            file = PathUtils.createOwnerTempFile(directory, "owner- ' ; &", " .tmp");
            files.verify(() -> Files.createTempFile(any(Path.class), any(String.class), any(String.class), any(FileAttribute.class)));
        }
        try {
            assertEquals(directory, file.getParent());
            assertEquals(0, Files.size(file));
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file));
            // ls -e emits additional lines for ACL entries; the new file must have none.
            assertEquals(1, runMacOsCommand("/bin/ls", "-lde", file.toString()).split("\\R").length);
            Files.write(file, BYTE_ARRAY_FIXTURE);
            assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
        } finally {
            Files.delete(file);
        }
        assertTrue(PathUtils.isEmptyDirectory(directory));
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testCreateOwnerTempFileMacOsCleansUpOnInvalidName() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> PathUtils.createOwnerTempFile(tempDirPath, "invalid/", ".tmp"));
        assertTrue(PathUtils.isEmptyDirectory(tempDirPath));
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testCreateOwnerTempFileMacOsDoesNotReplaceExistingFile() throws IOException {
        final Path[] existingFile = new Path[1];
        try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.createLink(any(Path.class), any(Path.class))).thenAnswer(invocation -> {
                existingFile[0] = Files.write(invocation.getArgument(0), BYTE_ARRAY_FIXTURE);
                return invocation.callRealMethod();
            });
            assertThrows(FileAlreadyExistsException.class, () -> PathUtils.createOwnerTempFile(tempDirPath, "owner-", ".tmp"));
        }
        assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(existingFile[0]));
        Files.delete(existingFile[0]);
        assertTrue(PathUtils.isEmptyDirectory(tempDirPath));
    }

    @ParameterizedTest
    @ValueSource(strings = { "start", "exit", "timeout", "interrupt" })
    @EnabledOnOs(OS.MAC)
    void testCreateOwnerTempFileMacOsNativeFailure(final String failureMode) throws Exception {
        final Process process = mock(Process.class);
        when(process.waitFor(30, TimeUnit.SECONDS)).thenAnswer(invocation -> {
            if ("interrupt".equals(failureMode)) {
                throw new InterruptedException("native ACL removal interrupted");
            }
            return !"timeout".equals(failureMode);
        });
        when(process.exitValue()).thenReturn(1);
        final IOException startFailure = new IOException("native ACL command unavailable");
        try (MockedConstruction<ProcessBuilder> commands = mockConstruction(ProcessBuilder.class, withSettings().defaultAnswer(RETURNS_SELF),
                (builder, context) -> when(builder.start()).thenAnswer(invocation -> {
                    final String[] command = (String[]) context.arguments().get(0);
                    assertEquals("/bin/chmod", command[0]);
                    assertEquals("-N", command[1]);
                    // The native operation must run before there is a file an observer could open.
                    assertTrue(PathUtils.isEmptyDirectory(Paths.get(command[2])));
                    if ("start".equals(failureMode)) {
                        throw startFailure;
                    }
                    return process;
                }))) {
            final IOException failure = assertThrows(IOException.class, () -> PathUtils.createOwnerTempFile(tempDirPath, "owner-", ".tmp"));
            if ("start".equals(failureMode)) {
                assertSame(startFailure, failure);
            } else {
                verify(process).destroyForcibly();
            }
            if ("interrupt".equals(failureMode)) {
                assertTrue(failure instanceof InterruptedIOException);
                assertTrue(Thread.currentThread().isInterrupted());
            }
            assertEquals(1, commands.constructed().size());
            assertTrue(PathUtils.isEmptyDirectory(tempDirPath));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testCreateOwnerTempFileNonDefaultPosixProvider() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("basic", "owner", "posix").build())) {
            final Path directory = Files.createDirectory(fileSystem.getPath("/temp"));
            final Path file = PathUtils.createOwnerTempFile(directory, "owner-", ".tmp");
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file));
            Files.write(file, BYTE_ARRAY_FIXTURE);
            assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(file));
        }
    }

    @ParameterizedTest
    @CsvSource({ "owner-,.bin", "owner-,", ",.bin", "," })
    @DisabledOnOs(OS.WINDOWS)
    void testCreateOwnerTempFilePosix(final String prefix, final String suffix) throws IOException {
        assumeTrue(Files.getFileStore(PathUtils.getTempDirectory()).supportsFileAttributeView(PosixFileAttributeView.class));
        assertCreateOwnerTempFile(prefix, suffix,
                file -> assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file)));
    }

    @Test
    @EnabledOnOs({ OS.MAC, OS.WINDOWS })
    void testCreateOwnerTempFileRemovesPublishedFileOnFailure() throws IOException {
        final IOException failure = new IOException("Could not remove the staging file");
        try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.delete(any(Path.class))).thenThrow(failure);
            assertSame(failure, assertThrows(IOException.class, () -> PathUtils.createOwnerTempFile(tempDirPath, "owner-", ".tmp")));
        }
        assertTrue(PathUtils.isEmptyDirectory(tempDirPath));
    }

    @ParameterizedTest
    @CsvSource({ "owner-,.bin", "owner-,", ",.bin", "," })
    @EnabledOnOs(OS.WINDOWS)
    void testCreateOwnerTempFileWindows(final String prefix, final String suffix) throws IOException {
        assumeTrue(Files.getFileStore(PathUtils.getTempDirectory()).supportsFileAttributeView(AclFileAttributeView.class));
        assertCreateOwnerTempFile(prefix, suffix, this::assertOwnerOnlyAcl);
    }

    @Test
    void testCreateOwnerTempFileWithoutPermissionSupport() throws IOException {
        final Configuration configuration = Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build();
        try (FileSystem fileSystem = Jimfs.newFileSystem(configuration)) {
            final Path directory = Files.createDirectory(fileSystem.getPath("C:\\temp"));
            assertThrows(UnsupportedOperationException.class, () -> PathUtils.createOwnerTempFile(directory, "owner-", ".tmp"));
            assertTrue(PathUtils.isEmptyDirectory(directory));
        }
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
    }

    @Test
    void testGetBaseNamePathCornerCases() {
        assertNull(PathUtils.getBaseName((Path) null));
        assertEquals("foo", PathUtils.getBaseName(Paths.get("foo.")));
        assertEquals("", PathUtils.getBaseName(Paths.get("bar/.foo")));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testGetBaseNamePathWindowsRoot() {
        assertNull(PathUtils.getBaseName(Paths.get("C:\\")));
    }

    @Test
    @DisabledOnOs(OS.MAC)
    void testGetDosFileAttributeView() {
        // dir
        final DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(current());
        final Path path = Paths.get("this-file-does-not-exist-at.all");
        assertFalse(Files.exists(path));
        assertNotNull(dosFileAttributeView);
        // missing file
        assertNotNull(PathUtils.getDosFileAttributeView(path));
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testGetDosFileAttributeViewMac() {
        assertNull(PathUtils.getDosFileAttributeView(current()));
        final Path path = Paths.get("this-file-does-not-exist-at.all");
        assertFalse(Files.exists(path));
        assertNull(PathUtils.getDosFileAttributeView(path));
    }

    @Test
    void testGetDosFileAttributeViewNull() {
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
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testGetExtensionWithColon() {
        assertEquals("txt", PathUtils.getExtension(Paths.get("foo.exe:bar.txt")));
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
    void testNewOutputStreamSymlinkRelativeTarget() throws IOException {
        final Path fixture = Files.createTempDirectory(current().toAbsolutePath().normalize(), "path-utils-");
        try {
            final Path linkDir = Files.createDirectories(fixture.resolve("linkDir"));
            final Path target = fixture.getFileName().resolve("target");
            final Path link = Files.createSymbolicLink(linkDir.resolve("link"), target);
            try (OutputStream output = PathUtils.newOutputStream(link.resolve("file.txt"), false)) {
                output.write(BYTE_ARRAY_FIXTURE);
            }
            assertArrayEquals(BYTE_ARRAY_FIXTURE, Files.readAllBytes(linkDir.resolve(target).resolve("file.txt")));
            assertFalse(Files.exists(fixture.resolve("target")));
        } finally {
            PathUtils.deleteDirectory(fixture);
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
    void testSetOwnerOnlyAcl() throws IOException {
        final Configuration configuration = Configuration.windows().toBuilder().setAttributeViews("basic", "owner", "acl").build();
        try (FileSystem fileSystem = Jimfs.newFileSystem(configuration)) {
            final Path file = Files.createFile(fileSystem.getPath("C:\\owner.tmp"));
            final AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
            final UserPrincipal other = fileSystem.getUserPrincipalLookupService().lookupPrincipalByName("other");
            view.setAcl(Collections.singletonList(AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(other)
                    .setPermissions(AclEntryPermission.READ_DATA).build()));
            assertSame(file, PathUtils.setOwnerOnlyAcl(file));
            assertOwnerOnlyAcl(file);
        }
    }

    @Test
    void testSetOwnerOnlyAclDeletesFileOnFailure() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.windows())) {
            final Path file = Files.createFile(fileSystem.getPath("C:\\owner.tmp"));
            assertThrows(IOException.class, () -> PathUtils.setOwnerOnlyAcl(file));
            assertFalse(Files.exists(file));
        }
    }

    @Test
    void testSetOwnerOnlyAclPreservesCleanupFailure() throws IOException {
        final Path file;
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.windows())) {
            file = Files.createFile(fileSystem.getPath("C:\\owner.tmp"));
        }
        final ClosedFileSystemException exception = assertThrows(ClosedFileSystemException.class, () -> PathUtils.setOwnerOnlyAcl(file));
        assertEquals(1, exception.getSuppressed().length);
        assertTrue(exception.getSuppressed()[0] instanceof ClosedFileSystemException);
    }

    @Test
    void testSetOwnerOnlyAclRejectsUnretainedPermissions() throws IOException {
        final Path file = Files.createFile(tempDirPath.resolve("owner.tmp"));
        final AclFileAttributeView view = mock(AclFileAttributeView.class);
        when(view.getOwner()).thenReturn(Files.getOwner(file));
        when(view.getAcl()).thenReturn(Collections.emptyList());
        try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.getFileAttributeView(file, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS)).thenReturn(view);
            assertThrows(IOException.class, () -> PathUtils.setOwnerOnlyAcl(file));
        }
        assertFalse(Files.exists(file));
    }

    @Test
    @DisabledOnOs(OS.LINUX)
    void testSetReadOnlyFile() throws IOException {
        final Path resolved = tempDirPath.resolve("testSetReadOnlyFile.txt");
        // Ask now, as we are allowed before editing parent permissions.
        final boolean isPosix = PathUtils.isPosix(tempDirPath);

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
        testTouch(PathUtils::touch);
    }

    private void testTouch(IOConsumer<Path> toucher) throws IOException {
        assertThrows(NullPointerException.class, () -> PathUtils.touch(null));
        final Path file = managedTempDirPath.resolve("touch.txt");
        Files.deleteIfExists(file);
        assertFalse(Files.exists(file), "Bad test: test file still exists");
        toucher.accept(file);
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
        toucher.accept(file);
        assertEquals(1, Files.size(file), "FileUtils.touch() didn't empty the file.");
        assertNotEquals(y2k, getLastModifiedMillis(file), "FileUtils.touch() changed lastModified");
        final int delta = 3000;
        assertTrue(getLastModifiedMillis(file) >= nowMillis - delta, "FileUtils.touch() changed lastModified to more than now-3s");
        assertTrue(getLastModifiedMillis(file) <= nowMillis + delta, "FileUtils.touch() changed lastModified to less than now+3s");
    }

    @Test
    void testTouchFileTime() throws IOException {
        testTouch(p -> PathUtils.touch(p, FileTimes.now()));

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

}
