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

import static org.apache.commons.io.file.CounterAssertions.assertCounts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Tests {@link DeletingPathVisitor}.
 */
class DeletingPathVisitorTest extends AbstractTempDirTest {

    private static final String ARGS = "org.apache.commons.io.file.TestArguments#";

    private void applyDeleteEmptyDirectory(final DeletingPathVisitor visitor) throws IOException {
        Files.walkFileTree(tempDirPath, visitor);
        assertCounts(1, 0, 0, visitor);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testDeleteDirectoryWithOtherAttributes(final boolean skip) throws IOException {
        final Path directory = Files.createDirectory(tempDirPath.resolve("junction"));
        final BasicFileAttributes attributes = mock(BasicFileAttributes.class);
        when(attributes.isDirectory()).thenReturn(true);
        when(attributes.isOther()).thenReturn(true);
        final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(), skip ? new String[] {"junction"} : new String[0]);
        assertEquals(FileVisitResult.SKIP_SUBTREE, visitor.preVisitDirectory(directory, attributes));
        assertEquals(skip, Files.exists(directory));
        assertCounts(skip ? 0 : 1, 0, 0, visitor);
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "deletingPathVisitors")
    void testDeleteEmptyDirectory(final DeletingPathVisitor visitor) throws IOException {
        applyDeleteEmptyDirectory(visitor);
        // This will throw if not empty.
        PathUtils.deleteIfExists(tempDirPath);
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "pathCounters")
    void testDeleteEmptyDirectoryNullCtorArg(final PathCounters pathCounters) throws IOException {
        applyDeleteEmptyDirectory(new DeletingPathVisitor(pathCounters, (String[]) null));
        // This will throw if not empty.
        PathUtils.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "deletingPathVisitors")
    void testDeleteFolders1FileSize0(final DeletingPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"), tempDirPath);
        assertCounts(1, 1, 0, PathUtils.visitFileTree(visitor, tempDirPath));
        // This will throw if not empty.
        PathUtils.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "deletingPathVisitors")
    void testDeleteFolders1FileSize1(final DeletingPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDirPath);
        assertCounts(1, 1, 1, PathUtils.visitFileTree(visitor, tempDirPath));
        // This will throw if not empty.
        PathUtils.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1 but skip that file.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "pathCounters")
    void testDeleteFolders1FileSize1Skip(final PathCounters pathCounters) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDirPath);
        final String skipFileName = "file-size-1.bin";
        final CountingPathVisitor visitor = new DeletingPathVisitor(pathCounters, skipFileName);
        assertCounts(1, 1, 1, PathUtils.visitFileTree(visitor, tempDirPath));
        final Path skippedFile = tempDirPath.resolve(skipFileName);
        assertTrue(Files.exists(skippedFile));
        Files.delete(skippedFile);
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource(ARGS + "deletingPathVisitors")
    void testDeleteFolders2FileSize2(final DeletingPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"), tempDirPath);
        assertCounts(3, 2, 2, PathUtils.visitFileTree(visitor, tempDirPath));
        // This will throw if not empty.
        PathUtils.deleteIfExists(tempDirPath);
    }

    @Test
    void testEqualsHashCode() {
        final DeletingPathVisitor visitor0 = DeletingPathVisitor.withLongCounters();
        final DeletingPathVisitor visitor1 = DeletingPathVisitor.withLongCounters();
        assertEquals(visitor0, visitor0);
        assertEquals(visitor0, visitor1);
        assertEquals(visitor1, visitor0);
        assertEquals(visitor0.hashCode(), visitor0.hashCode());
        assertEquals(visitor0.hashCode(), visitor1.hashCode());
        assertEquals(visitor1.hashCode(), visitor0.hashCode());
        visitor0.getPathCounters().getByteCounter().increment();
        assertEquals(visitor0, visitor0);
        assertNotEquals(visitor0, visitor1);
        assertNotEquals(visitor1, visitor0);
        assertEquals(visitor0.hashCode(), visitor0.hashCode());
        assertNotEquals(visitor0.hashCode(), visitor1.hashCode());
        assertNotEquals(visitor1.hashCode(), visitor0.hashCode());
    }

    /**
     * Tests https://issues.apache.org/jira/browse/IO-850
     */
    @Test
    void testIO850DirectoriesAndFiles() throws IOException {
        final Path rootDir = Files.createDirectory(managedTempDirPath.resolve("IO850"));
        createTempSymbolicLinkedRelativeDir(rootDir);
        final Path targetDir = rootDir.resolve(SUB_DIR);
        final Path symlinkDir = rootDir.resolve(SYMLINKED_DIR);
        Files.write(targetDir.resolve("file0.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
        final Path subDir0 = Files.createDirectory(targetDir.resolve("subDir0"));
        Files.write(subDir0.resolve("file1.txt"), "Hello".getBytes(StandardCharsets.UTF_8));
        final DeletingPathVisitor visitor = DeletingPathVisitor.withLongCounters();
        Files.walkFileTree(rootDir, visitor);
        assertFalse(Files.exists(targetDir));
        assertFalse(Files.exists(symlinkDir));
        assertFalse(Files.exists(rootDir));
        assertTrue(visitor.getPathCounters().getDirectoryCounter().get() > 0);
        assertTrue(visitor.getPathCounters().getFileCounter().get() > 0);
    }

    /**
     * Tests https://issues.apache.org/jira/browse/IO-850
     */
    @Test
    void testIO850DirectoriesOnly() throws IOException {
        final Path rootDir = Files.createDirectory(managedTempDirPath.resolve("IO850"));
        createTempSymbolicLinkedRelativeDir(rootDir);
        final Path targetDir = rootDir.resolve(SUB_DIR);
        final Path symlinkDir = rootDir.resolve(SYMLINKED_DIR);
        final DeletingPathVisitor visitor = DeletingPathVisitor.withLongCounters();
        Files.walkFileTree(rootDir, visitor);
        assertFalse(Files.exists(targetDir));
        assertFalse(Files.exists(symlinkDir));
        assertFalse(Files.exists(rootDir));
        assertTrue(visitor.getPathCounters().getDirectoryCounter().get() > 0);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    @EnabledOnOs(OS.WINDOWS)
    void testOverrideReadOnlyDosSymbolicLink(final boolean followLinks, final boolean dangling) throws IOException {
        final Path target = managedTempDirPath.resolve("target");
        if (!dangling) {
            Files.write(target, new byte[] {1});
        }
        final Path link = Files.createSymbolicLink(tempDirPath.resolve("link"), target);
        final DosFileAttributeView linkDos = Files.getFileAttributeView(link, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        try {
            linkDos.setReadOnly(true);
            assertTrue(linkDos.readAttributes().isReadOnly());
            // Unlike Jimfs, Windows enforces the link's own read-only attribute during deletion.
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                    followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray(),
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(tempDirPath, visitor);
            assertFalse(Files.exists(tempDirPath, LinkOption.NOFOLLOW_LINKS));
            assertEquals(!dangling, Files.exists(target));
        } finally {
            if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
                linkDos.setReadOnly(false);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testOverrideReadOnlyDosSymbolicLinkTarget(final boolean followLinks, final boolean directory) throws IOException {
        // Exercise DOS attributes even when the host file system only supports POSIX attributes.
        final Configuration configuration = Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build();
        try (FileSystem fileSystem = Jimfs.newFileSystem(configuration)) {
            final Path root = Files.createDirectory(fileSystem.getPath("C:\\deleteMe"));
            final Path target = fileSystem.getPath("C:\\target");
            if (directory) {
                Files.createDirectory(target);
                Files.write(target.resolve("content"), new byte[] {1});
            } else {
                Files.write(target, new byte[] {1});
            }
            final DosFileAttributeView dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
            dos.setReadOnly(true);
            Files.createSymbolicLink(root.resolve("link"), target);
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                    followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray(),
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(root, visitor);
            assertFalse(Files.exists(root, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.exists(target));
            assertTrue(dos.readAttributes().isReadOnly());
            if (directory) {
                assertTrue(Files.exists(target.resolve("content")));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testOverrideReadOnlyRegularFile(final boolean followLinks) throws IOException {
        final Path file = Files.write(tempDirPath.resolve("file"), new byte[] {1});
        final DosFileAttributeView dos = Files.getFileAttributeView(file, DosFileAttributeView.class);
        final Set<PosixFilePermission> permissions = dos == null ? Files.getPosixFilePermissions(tempDirPath) : null;
        try {
            if (dos != null) {
                dos.setReadOnly(true);
            } else {
                Files.setPosixFilePermissions(tempDirPath, PosixFilePermissions.fromString("r-x------"));
            }
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                    followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray(),
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(tempDirPath, visitor);
            assertFalse(Files.exists(tempDirPath, LinkOption.NOFOLLOW_LINKS));
            assertCounts(1, 1, 1, visitor);
        } finally {
            if (dos != null && Files.exists(file)) {
                dos.setReadOnly(false);
            } else if (dos == null && Files.exists(tempDirPath)) {
                Files.setPosixFilePermissions(tempDirPath, permissions);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testOverrideReadOnlyRelativeSymbolicLink(final boolean followLinks, final boolean dangling) throws IOException {
        final Configuration configuration = Configuration.unix().toBuilder().setAttributeViews("basic", "posix").build();
        try (FileSystem fileSystem = Jimfs.newFileSystem(configuration)) {
            final Path target = fileSystem.getPath("target");
            if (!dangling) {
                Files.write(target, new byte[] {1});
            }
            final Path link = Files.createSymbolicLink(fileSystem.getPath("link"), target);
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                    followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray(),
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(link, visitor);
            assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
            assertEquals(!dangling, Files.exists(target));
            assertEquals(1, visitor.getPathCounters().getFileCounter().get());
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testOverrideReadOnlySymbolicLinkParent(final boolean followLinks, final boolean dangling) throws IOException {
        assumeTrue(Files.getFileAttributeView(tempDirPath, PosixFileAttributeView.class) != null);
        final Path target = managedTempDirPath.resolve("target");
        if (!dangling) {
            Files.write(target, new byte[] {1});
        }
        Files.createSymbolicLink(tempDirPath.resolve("link"), target);
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(tempDirPath);
        try {
            Files.setPosixFilePermissions(tempDirPath, PosixFilePermissions.fromString("r-x------"));
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                    followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray(),
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(tempDirPath, visitor);
            assertFalse(Files.exists(tempDirPath, LinkOption.NOFOLLOW_LINKS));
            assertEquals(!dangling, Files.exists(target));
        } finally {
            if (Files.exists(tempDirPath)) {
                Files.setPosixFilePermissions(tempDirPath, permissions);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void testOverrideReadOnlySymbolicLinkTarget(final boolean followLinks, final boolean directory) throws IOException {
        final Path target = directory ? Files.createDirectory(managedTempDirPath.resolve("target"))
                : Files.write(managedTempDirPath.resolve("target"), new byte[] {1});
        final Path content = directory ? Files.write(target.resolve("content"), new byte[] {1}) : target;
        final DosFileAttributeView dos = Files.getFileAttributeView(target, DosFileAttributeView.class);
        final Set<PosixFilePermission> permissions = dos == null ? Files.getPosixFilePermissions(target) : null;
        try {
            if (dos != null) {
                dos.setReadOnly(true);
            } else {
                Files.setPosixFilePermissions(target, PosixFilePermissions.fromString(directory ? "r-x------" : "r--------"));
            }
            Files.createSymbolicLink(tempDirPath.resolve("link"), target);
            final LinkOption[] options = followLinks ? new LinkOption[0] : PathUtils.noFollowLinkOptionArray();
            final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(), options,
                    new DeleteOption[] {StandardDeleteOption.OVERRIDE_READ_ONLY});
            Files.walkFileTree(tempDirPath, visitor);
            assertFalse(Files.exists(tempDirPath, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.exists(content));
            if (dos != null) {
                assertTrue(dos.readAttributes().isReadOnly());
            } else {
                assertEquals(PosixFilePermissions.fromString(directory ? "r-x------" : "r--------"), Files.getPosixFilePermissions(target));
            }
            assertEquals(1, visitor.getPathCounters().getFileCounter().get());
            assertEquals(1, visitor.getPathCounters().getDirectoryCounter().get());
        } finally {
            if (dos != null) {
                dos.setReadOnly(false);
            } else {
                Files.setPosixFilePermissions(target, permissions);
            }
        }
    }
}
