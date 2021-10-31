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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.io.function.IOConsumer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests <a href="https://issues.apache.org/jira/browse/IO-751">IO-751</a>.
 * <p>
 * Must be run on macOS or Linux, not Windows.
 * </p>
 */
public class DeleteDirectoryTest {

    @BeforeAll
    public static void beforeAll() throws IOException {
        // This test requires a POSIX file system, so not stock Windows 10.
        Assumptions.assumeTrue(PathUtils.isPosix(PathUtils.current()));
    }

    @TempDir
    public File tempDir;

    private void testDeleteDirectory(final IOConsumer<Path> deleter) throws IOException {
        final Path tempDirPath = tempDir.toPath();

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

        // Delete the test directory with the given implementation
        deleter.accept(testDir);
        // Symlink is gone -- passes
        assertFalse(Files.exists(symLink), symLink::toString);
        // The test file still exists -- passes
        assertTrue(Files.exists(file), file::toString);
        // The permissions of the test file should still be the same -- fails
        assertEquals(permissions, Files.getPosixFilePermissions(file), file::toString);
    }

    @Test
    @Disabled
    public void testDeleteDirectoryWithFileUtils() throws IOException {
        testDeleteDirectory(dir -> FileUtils.deleteDirectory(dir.toFile()));
    }

    @Test
    public void testDeleteDirectoryWithPathUtils() throws IOException {
        testDeleteDirectory(PathUtils::deleteDirectory);
    }

    @Test
    @Disabled
    public void testDeleteDirectoryWithPathUtilsOverrideReadOnly() throws IOException {
        testDeleteDirectory(dir -> PathUtils.deleteDirectory(dir, StandardDeleteOption.OVERRIDE_READ_ONLY));
    }
}
