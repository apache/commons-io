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

import static org.apache.commons.io.file.CounterAssertions.assertCounts;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DeletingPathVisitor}.
 */
public class PathUtilsDeleteFileTest extends AbstractTempDirTest {

    @Test
    public void testDeleteBrokenSymbolicLink() throws IOException {
        assumeFalse(SystemUtils.IS_OS_WINDOWS);
        final Path missingFile = tempDirPath.resolve("missing.txt");
        final Path brokenLink = tempDirPath.resolve("broken.txt");
        Files.createSymbolicLink(brokenLink, missingFile);
        assertTrue(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(missingFile, LinkOption.NOFOLLOW_LINKS));
        PathUtils.deleteFile(brokenLink);
        assertFalse(Files.exists(brokenLink, LinkOption.NOFOLLOW_LINKS), "Symbolic link not removed");
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    public void testDeleteFileDirectory1FileSize0() throws IOException {
        final String fileName = "file-size-0.bin";
        PathUtils.copyFileToDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0/" + fileName), tempDirPath);
        assertCounts(0, 1, 0, PathUtils.deleteFile(tempDirPath.resolve(fileName)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testDeleteFileDirectory1FileSize1() throws IOException {
        final String fileName = "file-size-1.bin";
        PathUtils.copyFileToDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName), tempDirPath);
        assertCounts(0, 1, 1, PathUtils.deleteFile(tempDirPath.resolve(fileName)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a file that does not exist.
     */
    @Test
    public void testDeleteFileDoesNotExist() throws IOException {
        testDeleteFileEmpty(PathUtils.deleteFile(tempDirPath.resolve("file-does-not-exist.bin")));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    private void testDeleteFileEmpty(final PathCounters pathCounts) {
        assertCounts(0, 0, 0, pathCounts);
    }

    /**
     * Tests an empty folder.
     */
    @Test
    public void testDeleteFileEmptyDirectory() throws IOException {
        Assertions.assertThrows(NoSuchFileException.class, () -> testDeleteFileEmpty(PathUtils.deleteFile(tempDirPath)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testDeleteReadOnlyFileDirectory1FileSize1() throws IOException {
        final String fileName = "file-size-1.bin";
        PathUtils.copyFileToDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName), tempDirPath);
        final Path resolved = tempDirPath.resolve(fileName);
        PathUtils.setReadOnly(resolved, true);
        if (SystemUtils.IS_OS_WINDOWS) {
            // Fails on Windows's Ubuntu subsystem.
            assertFalse(Files.isWritable(resolved));
            assertThrows(IOException.class, () -> PathUtils.deleteFile(resolved));
        }
        assertCounts(0, 1, 1, PathUtils.deleteFile(resolved, StandardDeleteOption.OVERRIDE_READ_ONLY));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testSetReadOnlyFileDirectory1FileSize1() throws IOException {
        final String fileName = "file-size-1.bin";
        PathUtils.copyFileToDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName), tempDirPath);
        final Path resolved = tempDirPath.resolve(fileName);
        PathUtils.setReadOnly(resolved, true);
        if (SystemUtils.IS_OS_WINDOWS) {
            // Fails on Windows's Ubuntu subsystem.
            assertFalse(Files.isWritable(resolved));
            assertThrows(IOException.class, () -> PathUtils.deleteFile(resolved));
        }
        PathUtils.setReadOnly(resolved, false);
        PathUtils.deleteFile(resolved);
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }
}
