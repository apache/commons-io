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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DeletingPathFileVisitor}.
 */
public class PathUtilsDeleteFileTest {

    private Path tempDirectory;

    @AfterEach
    public void afterEach() throws IOException {
        // backstop
        if (Files.exists(tempDirectory) && PathUtils.isEmptyDirectory(tempDirectory)) {
            Files.deleteIfExists(tempDirectory);
        }
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        tempDirectory = Files.createTempDirectory(getClass().getCanonicalName());
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    public void testDeleteFileDirectory1FileSize0() throws IOException {
        final String fileName = "file-size-0.bin";
        FileUtils.copyFileToDirectory(
                Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0/" + fileName).toFile(),
                tempDirectory.toFile());
        final PathCounts pathCounts = PathUtils.deleteFile(tempDirectory.resolve(fileName));
        Assertions.assertEquals(0, pathCounts.getDirectoryCount());
        Assertions.assertEquals(1, pathCounts.getFileCount());
        Assertions.assertEquals(0, pathCounts.getByteCount());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testDeleteFileDirectory1FileSize1() throws IOException {
        final String fileName = "file-size-1.bin";
        FileUtils.copyFileToDirectory(
                Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName).toFile(),
                tempDirectory.toFile());
        final PathCounts pathCounts = PathUtils.deleteFile(tempDirectory.resolve(fileName));
        Assertions.assertEquals(0, pathCounts.getDirectoryCount());
        Assertions.assertEquals(1, pathCounts.getFileCount());
        Assertions.assertEquals(1, pathCounts.getByteCount());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    /**
     * Tests a file that does not exist.
     */
    @Test
    public void testDeleteFileDoesNotExist() throws IOException {
        testDeleteFileEmpty(PathUtils.deleteFile(tempDirectory.resolve("file-does-not-exist.bin")));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    private void testDeleteFileEmpty(final PathCounts pathCounts) throws IOException {
        Assertions.assertEquals(0, pathCounts.getDirectoryCount(), "getDirectoryCount()");
        Assertions.assertEquals(0, pathCounts.getFileCount(), "getFileCount()");
        Assertions.assertEquals(0, pathCounts.getByteCount(), "getByteCount()");
    }

    /**
     * Tests an empty folder.
     */
    @Test
    public void testDeleteFileEmptyDirectory() throws IOException {
        Assertions.assertThrows(NotDirectoryException.class,
                () -> testDeleteFileEmpty(PathUtils.deleteFile(tempDirectory)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }
}
