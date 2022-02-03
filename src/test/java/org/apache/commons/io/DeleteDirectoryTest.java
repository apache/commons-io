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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.io.function.IOConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests <a href="https://issues.apache.org/jira/browse/IO-751">IO-751</a>.
 * <p>
 * Must be run on a POSIX file system, macOS or Linux, disbled on Windows.
 * </p>
 */
@DisabledOnOs(OS.WINDOWS)
public class DeleteDirectoryTest extends AbstractTempDirTest {

    private void testDeleteDirectory(final IOConsumer<Path> deleter) throws IOException {
        // Create a test file
        final String contents = "Hello!";
        final Path file = tempDirPath.resolve("file.txt");
        final Charset charset = StandardCharsets.UTF_8;
        PathUtils.writeString(file, contents, charset);
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
        // Sanity check: Owner has write permission on the new file
        assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE), permissions::toString);

        // Create a test directory
        final Path testDir = tempDirPath.resolve("dir");
        Files.createDirectory(testDir);

        // Inside the test directory, create a symlink to the test file
        final Path symLink = testDir.resolve("symlink.txt");
        Files.createSymbolicLink(symLink, file);
        // Sanity check: The symlink really points to the test file
        assertEquals(contents, PathUtils.readString(symLink, charset));

        // Delete the test directory using the given implementation
        deleter.accept(testDir);
        // Symlink is gone -- passes
        assertFalse(Files.exists(symLink), symLink::toString);
        // The test file still exists -- passes
        assertTrue(Files.exists(file), file::toString);
        // The permissions of the test file should still be the same
        assertEquals(permissions, Files.getPosixFilePermissions(file), file::toString);
    }

    @Test
    public void testDeleteDirectoryWithFileUtils() throws IOException {
        testDeleteDirectory(dir -> FileUtils.deleteDirectory(dir.toFile()));
    }

    @Test
    public void testDeleteDirectoryWithPathUtils() throws IOException {
        testDeleteDirectory(PathUtils::deleteDirectory);
    }

    @Test
    public void testDeleteDirectoryWithPathUtilsOverrideReadOnly() throws IOException {
        testDeleteDirectory(dir -> PathUtils.deleteDirectory(dir, StandardDeleteOption.OVERRIDE_READ_ONLY));
    }

    @Test
    @DisabledOnOs(OS.LINUX) // TODO
    public void testDeleteFileCheckParentAccess() throws IOException {
        // Create a test directory
        final Path testDir = tempDirPath.resolve("dir");
        Files.createDirectory(testDir);

        // Create a test file
        final Path file = testDir.resolve("file.txt");
        final Charset charset = StandardCharsets.UTF_8;
        PathUtils.writeString(file, "Hello!", charset);

        // A file is RO in POSIX if the parent is not W and not E.
        PathUtils.setReadOnly(file, true);
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(testDir);
        assertFalse(Files.isWritable(testDir),
                () -> String.format("Parent directory '%s' of '%s' should NOT be Writable, permissions are %s ", testDir, file, permissions));
        assertFalse(Files.isExecutable(testDir),
                () -> String.format("Parent directory '%s' of '%s' should NOT be Executable, permissions are %s ", testDir, file, permissions));

        assertThrows(IOException.class, () -> PathUtils.delete(file));
        // Nothing happened, we're not even allowed to test attributes, so the file seems deleted, but it is not.

        PathUtils.delete(file, StandardDeleteOption.OVERRIDE_READ_ONLY);

        assertFalse(Files.exists(file));

        assertEquals(permissions, Files.getPosixFilePermissions(testDir), testDir::toString);
        assertFalse(Files.isWritable(testDir));
        assertFalse(Files.isExecutable(testDir));
    }
}
