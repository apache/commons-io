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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Tests restoration after failed read-only overrides.
 */
class ReadOnlyDeletionTest {

    @Test
    void testAttributeReadFailureDoesNotClearReadOnly() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = Files.createFile(fs.getPath("C:\\file.txt"));
            Files.setAttribute(file, "dos:readonly", true);
            final LinkOption[] options = PathUtils.noFollowLinkOptionArray();
            final IOException readFailure = new AccessDeniedException("Cannot read attributes");
            final IOException deleteFailure = new IOException("Cannot delete file");
            try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
                files.when(() -> Files.readAttributes(file, DosFileAttributes.class, options)).thenThrow(readFailure);
                assertSame(readFailure, assertThrows(IOException.class, () -> PathUtils.withReadOnlyOverride(file, options, true, () -> {
                    throw deleteFailure;
                })));
            }
            assertTrue(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
        }
    }

    @Test
    void testAttributeReadFailureUsesPosixWithoutChangingDosState() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("basic", "owner", "posix", "dos").build())) {
            final Path parent = Files.createDirectory(fs.getPath("/parent"));
            final Path file = Files.createFile(parent.resolve("file"));
            final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("r-x------");
            Files.setPosixFilePermissions(parent, permissions);
            final LinkOption[] options = PathUtils.noFollowLinkOptionArray();
            final IOException deleteFailure = new AccessDeniedException(file.toString());
            try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
                files.when(() -> Files.deleteIfExists(file)).thenThrow(deleteFailure);
                Files.createFile(file);
                Files.setAttribute(file, "dos:readonly", true);
                files.when(() -> Files.readAttributes(file, DosFileAttributes.class, options)).thenThrow(new IOException("Cannot read DOS attributes"));
                assertSame(deleteFailure, assertThrows(IOException.class, () -> PathUtils.deleteFile(file, options, StandardDeleteOption.OVERRIDE_READ_ONLY)));
            }
            assertTrue(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
            assertEquals(permissions, Files.getPosixFilePermissions(parent));
        }
    }

    @Test
    void testDeletingVisitorRetainsPosixTraversalPermissions(@TempDir final Path tempDir) throws IOException {
        assumeTrue(Files.getFileAttributeView(tempDir, PosixFileAttributeView.class) != null);
        // Exercise the POSIX fallback, not a provider that supports DOS attributes.
        assumeTrue(Files.getFileAttributeView(tempDir, DosFileAttributeView.class) == null);
        final Path root = Files.createDirectory(tempDir.resolve("root"));
        final Path child = Files.createDirectory(root.resolve("child"));
        final Path first = Files.createFile(root.resolve("first"));
        final Path second = Files.createFile(child.resolve("second"));
        final DeletingPathVisitor visitor = new DeletingPathVisitor(Counters.longPathCounters(),
                new DeleteOption[] { StandardDeleteOption.OVERRIDE_READ_ONLY });
        Files.setPosixFilePermissions(root, PosixFilePermissions.fromString("r-x------"));
        try {
            assumeTrue(!Files.isWritable(root), "Requires enforced POSIX permissions");
            // Fix traversal order: deleting the first file enables deletion of the later child directory.
            visitor.visitFile(first, Files.readAttributes(first, BasicFileAttributes.class));
            visitor.visitFile(second, Files.readAttributes(second, BasicFileAttributes.class));
            visitor.postVisitDirectory(child, null);
            assertFalse(Files.exists(child));
        } finally {
            Files.setPosixFilePermissions(root, PosixFilePermissions.fromString("rwx------"));
        }
    }

    @Test
    void testDosOverrideAndRestorationFailure() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = Files.createFile(fs.getPath("C:\\file.txt"));
            Files.setAttribute(file, "dos:readonly", true);
            final IOException failure = new IOException("delete failed");
            assertSame(failure, assertThrows(IOException.class, () -> PathUtils.withReadOnlyOverride(file, PathUtils.noFollowLinkOptionArray(), true, () -> {
                assertFalse(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
                fs.close();
                throw failure;
            })));
            assertEquals(1, failure.getSuppressed().length);
            assertTrue(failure.getSuppressed()[0] instanceof java.nio.file.ClosedFileSystemException);
        }
    }

    @ParameterizedTest
    @CsvSource({ "path,true", "path,false", "delete,true", "delete,false", "clean,true", "clean,false" })
    void testFailedDeletion(final String operation, final boolean readOnly) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = fs.getPath("C:\\file.txt");
            Files.createFile(file);
            final IOException failure = new AccessDeniedException(file.toString());
            final AtomicInteger attempts = new AtomicInteger();
            try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
                files.when(() -> Files.deleteIfExists(file)).thenAnswer(invocation -> {
                    final int attempt = attempts.incrementAndGet();
                    // Only PathUtils first attempts deletion without overriding the attribute.
                    if (!"path".equals(operation) || attempt > 1) {
                        assertFalse(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
                    }
                    throw failure;
                });
                // CALLS_REAL_METHODS executes deleteIfExists while setting up the stub.
                Files.createFile(file);
                Files.setAttribute(file, "dos:readonly", readOnly);
                assertSame(failure, assertThrows(IOException.class, () -> {
                    if ("path".equals(operation)) {
                        PathUtils.deleteFile(file, StandardDeleteOption.OVERRIDE_READ_ONLY);
                    } else if ("delete".equals(operation)) {
                        Files.walkFileTree(file,
                                new DeletingPathVisitor(Counters.longPathCounters(), new DeleteOption[] { StandardDeleteOption.OVERRIDE_READ_ONLY }));
                    } else {
                        Files.walkFileTree(file,
                                new CleaningPathVisitor(Counters.longPathCounters(), new DeleteOption[] { StandardDeleteOption.OVERRIDE_READ_ONLY }));
                    }
                }));
                assertTrue(Files.exists(file));
                assertEquals(readOnly, Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
                assertEquals("path".equals(operation) ? 2 : 1, attempts.get());
            }
        }
    }

    @Test
    void testMissingFileAfterFailure() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = Files.createFile(fs.getPath("C:\\file.txt"));
            Files.setAttribute(file, "dos:readonly", true);
            final IOException failure = new IOException("delete failed");
            assertSame(failure, assertThrows(IOException.class, () -> PathUtils.withReadOnlyOverride(file, PathUtils.noFollowLinkOptionArray(), true, () -> {
                Files.delete(file);
                throw failure;
            })));
            assertEquals(0, failure.getSuppressed().length);
        }
    }

    @Test
    void testNoOverride() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = Files.createFile(fs.getPath("C:\\file.txt"));
            Files.setAttribute(file, "dos:readonly", true);
            final IOException failure = new IOException("delete failed");
            assertSame(failure, assertThrows(IOException.class, () -> PathUtils.withReadOnlyOverride(file, PathUtils.noFollowLinkOptionArray(), false, () -> {
                assertTrue(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
                throw failure;
            })));
            assertTrue(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
        }
    }

    @Test
    void testPosixPermissionsUnchangedOnFailure() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("basic", "owner", "posix").build())) {
            final Path parent = Files.createDirectory(fs.getPath("/parent"));
            final Path file = Files.createFile(parent.resolve("file"));
            final Set<PosixFilePermission> filePermissions = PosixFilePermissions.fromString("---------");
            final Set<PosixFilePermission> parentPermissions = PosixFilePermissions.fromString("r-x------");
            Files.setPosixFilePermissions(file, filePermissions);
            Files.setPosixFilePermissions(parent, parentPermissions);
            final IOException failure = new IOException("delete failed");
            try (MockedStatic<Files> files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
                files.when(() -> Files.deleteIfExists(file)).thenThrow(new AccessDeniedException(file.toString())).thenAnswer(invocation -> {
                    assertTrue(Files.getPosixFilePermissions(parent).contains(PosixFilePermission.OWNER_WRITE));
                    throw failure;
                });
                // Recreate the file removed during CALLS_REAL_METHODS stubbing.
                Files.createFile(file);
                Files.setPosixFilePermissions(file, filePermissions);
                assertSame(failure, assertThrows(IOException.class, () -> PathUtils.deleteFile(file, StandardDeleteOption.OVERRIDE_READ_ONLY)));
            }
            assertEquals(filePermissions, Files.getPosixFilePermissions(file));
            assertEquals(parentPermissions, Files.getPosixFilePermissions(parent));
        }
    }

    @Test
    void testSuccessfulDosDeletion() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows().toBuilder().setAttributeViews("basic", "dos").build())) {
            final Path file = Files.createFile(fs.getPath("C:\\file.txt"));
            Files.setAttribute(file, "dos:readonly", true);
            assertTrue(PathUtils.withReadOnlyOverride(file, PathUtils.noFollowLinkOptionArray(), true, () -> {
                assertFalse(Files.readAttributes(file, DosFileAttributes.class).isReadOnly());
                return Files.deleteIfExists(file);
            }));
            assertFalse(Files.exists(file));
        }
    }
}
