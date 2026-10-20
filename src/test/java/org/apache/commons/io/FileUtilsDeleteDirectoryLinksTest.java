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

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

/**
 * Tests link handling during directory deletion.
 */
class FileUtilsDeleteDirectoryLinksTest {

    /**
     * Runs deletion in a child JVM so tests can observe deletion at exit.
     */
    public static class DeleteOnExitMain {

        /**
         * Schedules the given entry for deletion.
         *
         * @param args the path to delete.
         * @throws IOException if scheduling fails.
         */
        public static void main(final String[] args) throws IOException {
            FileUtils.forceDeleteOnExit(new File(args[0]));
        }
    }

    @TempDir
    Path tempDir;

    private void deleteOnExit(final Path path) throws Exception {
        final String executable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        final String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        final Process process = new ProcessBuilder(executable, "-cp", classPath, DeleteOnExitMain.class.getName(), path.toString()).redirectErrorStream(true)
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Child JVM did not exit");
            assertEquals(0, process.exitValue());
        } finally {
            process.destroyForcibly();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testAttributeReadFailure(final boolean onExit) throws IOException {
        final Path directory = Files.createDirectory(tempDir.resolve("directory"));
        final File directoryFile = spy(directory.toFile());
        final IOException failure = new IOException("Cannot read attributes");
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)).thenThrow(failure);
            assertSame(failure, assertThrows(IOException.class, () -> {
                if (onExit) {
                    FileUtils.forceDeleteOnExit(directoryFile);
                } else {
                    FileUtils.deleteDirectory(directoryFile);
                }
            }));
            files.verify(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
            files.verifyNoMoreInteractions();
        }
        verify(directoryFile, never()).listFiles((FileFilter) null);
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.MAC })
    void testDanglingSymbolicLinkIsRetained() throws IOException {
        final Path link = Files.createSymbolicLink(tempDir.resolve("link"), tempDir.resolve("missing"));
        FileUtils.deleteDirectory(link.toFile());
        assertTrue(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    void testDirectoryOnExit() throws Exception {
        final Path directory = Files.createDirectory(tempDir.resolve("directory"));
        Files.createFile(directory.resolve("file"));
        deleteOnExit(directory);
        assertFalse(Files.exists(directory));
    }

    @ParameterizedTest
    @CsvSource({ "false, false", "false, true", "true, false", "true, true" })
    void testDirectoryWithOtherAttributes(final boolean onExit, final boolean directoryAttribute) throws IOException {
        final Path directory = Files.createDirectory(tempDir.resolve("directory"));
        final File directoryFile = spy(directory.toFile());
        final Path sentinel = Files.write(directory.resolve("sentinel"), new byte[] { 1 });
        final BasicFileAttributes attributes = mock(BasicFileAttributes.class);
        when(attributes.isDirectory()).thenReturn(directoryAttribute);
        when(attributes.isOther()).thenReturn(true);
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)).thenReturn(attributes);
            if (onExit) {
                FileUtils.forceDeleteOnExit(directoryFile);
            } else {
                FileUtils.deleteDirectory(directoryFile);
                files.verify(() -> Files.delete(directory));
            }
            files.verify(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
            files.verifyNoMoreInteractions();
        }
        verify(directoryFile, never()).listFiles((FileFilter) null);
        assertEquals(1, Files.size(sentinel));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testDisappearedBeforeAttributeRead(final boolean onExit) throws IOException {
        final Path directory = Files.createDirectory(tempDir.resolve("directory"));
        final File directoryFile = spy(directory.toFile());
        final NoSuchFileException missing = new NoSuchFileException(directory.toString());
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)).thenThrow(missing);
            files.when(() -> Files.delete(directory)).thenThrow(missing);
            if (onExit) {
                assertDoesNotThrow(() -> FileUtils.forceDeleteOnExit(directoryFile));
            } else {
                assertSame(missing, assertThrows(NoSuchFileException.class, () -> FileUtils.deleteDirectory(directoryFile)));
                files.verify(() -> Files.delete(directory));
            }
            files.verify(() -> Files.readAttributes(directory, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
            files.verifyNoMoreInteractions();
        }
        verify(directoryFile, never()).listFiles((FileFilter) null);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @EnabledOnOs(OS.WINDOWS)
    void testJunction(final boolean onExit) throws Exception {
        testJunction(onExit, false);
    }

    private void testJunction(final boolean onExit, final boolean nested) throws Exception {
        final Path target = Files.createDirectory(tempDir.resolve("target"));
        final Path sentinel = Files.write(target.resolve("sentinel"), new byte[] { 1 });
        final Path root = Files.createDirectory(tempDir.resolve("root"));
        final Path junction = root.resolve("junction");
        final Process process = new ProcessBuilder("cmd", "/c", "mklink", "/J", junction.toString(), target.toString()).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "mklink did not exit");
            assertEquals(0, process.exitValue());
            final Path path = nested ? root : junction;
            if (onExit) {
                deleteOnExit(path);
            } else {
                FileUtils.deleteDirectory(path.toFile());
            }
            assertFalse(Files.exists(junction, LinkOption.NOFOLLOW_LINKS));
            assertTrue(Files.isDirectory(target));
            assertEquals(1, Files.size(sentinel));
        } finally {
            process.destroyForcibly();
            // Unlink directly so even a failing test never recursively cleans the target through this junction.
            Files.deleteIfExists(junction);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @EnabledOnOs(OS.WINDOWS)
    void testNestedJunction(final boolean onExit) throws Exception {
        testJunction(onExit, true);
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.MAC })
    void testRejectsFifo() throws Exception {
        final Path fifo = tempDir.resolve("fifo");
        final Process process = new ProcessBuilder("mkfifo", fifo.toString()).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "mkfifo did not exit");
            assertEquals(0, process.exitValue());
            assertThrows(IllegalArgumentException.class, () -> FileUtils.deleteDirectory(fifo.toFile()));
            assertTrue(Files.exists(fifo, LinkOption.NOFOLLOW_LINKS));
        } finally {
            process.destroyForcibly();
            Files.deleteIfExists(fifo);
        }
    }

    @Test
    @EnabledOnOs({ OS.LINUX, OS.MAC })
    void testSymbolicLinkOnExit() throws Exception {
        final Path target = Files.createDirectory(tempDir.resolve("target"));
        final Path sentinel = Files.write(target.resolve("sentinel"), new byte[] { 1 });
        final Path link = Files.createSymbolicLink(tempDir.resolve("link"), target);
        deleteOnExit(link);
        assertFalse(Files.exists(link, LinkOption.NOFOLLOW_LINKS));
        assertEquals(1, Files.size(sentinel));
    }
}
