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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DeletingFileVisitor}.
 */
public class DeletingFileVisitorTest {

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
     * Tests an empty folder.
     */
    @Test
    public void testEmptyDirectory() throws IOException {
        testEmptyDirectory(new DeletingFileVisitor());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    private void testEmptyDirectory(final CountingFileVisitor visitor) throws IOException {
        Files.walkFileTree(tempDirectory, visitor);
        Assertions.assertEquals(1, visitor.getDirectoryCount());
        Assertions.assertEquals(0, visitor.getFileCount());
        Assertions.assertEquals(0, visitor.getByteCount());
    }

    /**
     * Tests an empty folder.
     */
    @Test
    public void testEmptyDirectoryNullCtorArg() throws IOException {
        testEmptyDirectory(new DeletingFileVisitor((String[]) null));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    public void testFolders1FileSize0() throws IOException {
        FileUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0").toFile(),
                tempDirectory.toFile());
        final CountingFileVisitor visitor = new DeletingFileVisitor();
        Files.walkFileTree(tempDirectory, visitor);
        Assertions.assertEquals(1, visitor.getDirectoryCount());
        Assertions.assertEquals(1, visitor.getFileCount());
        Assertions.assertEquals(0, visitor.getByteCount());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testFolders1FileSize1() throws IOException {
        FileUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1").toFile(),
                tempDirectory.toFile());
        final CountingFileVisitor visitor = new DeletingFileVisitor();
        Files.walkFileTree(tempDirectory, visitor);
        Assertions.assertEquals(1, visitor.getDirectoryCount());
        Assertions.assertEquals(1, visitor.getFileCount());
        Assertions.assertEquals(1, visitor.getByteCount());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }

    /**
     * Tests a directory with one file of size 1 but skip that file.
     */
    @Test
    public void testFolders1FileSize1Skip() throws IOException {
        FileUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1").toFile(),
                tempDirectory.toFile());
        final String skipFileName = "file-size-1.bin";
        final CountingFileVisitor visitor = new DeletingFileVisitor(skipFileName);
        Files.walkFileTree(tempDirectory, visitor);
        Assertions.assertEquals(1, visitor.getDirectoryCount());
        Assertions.assertEquals(1, visitor.getFileCount());
        Assertions.assertEquals(1, visitor.getByteCount());
        final Path skippedFile = tempDirectory.resolve(skipFileName);
        Assertions.assertTrue(Files.exists(skippedFile));
        Files.delete(skippedFile);
    }

    /**
     * Tests a directory with two subdirectorys, each containing one file of size 1.
     */
    @Test
    public void testFolders2FileSize2() throws IOException {
        FileUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2").toFile(),
                tempDirectory.toFile());
        final CountingFileVisitor visitor = new DeletingFileVisitor();
        Files.walkFileTree(tempDirectory, visitor);
        Assertions.assertEquals(3, visitor.getDirectoryCount());
        Assertions.assertEquals(2, visitor.getFileCount());
        Assertions.assertEquals(2, visitor.getByteCount());
        // This will throw if not empty.
        Files.deleteIfExists(tempDirectory);
    }
}
