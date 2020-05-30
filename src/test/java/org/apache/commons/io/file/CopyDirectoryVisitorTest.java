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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link CountingPathVisitor}.
 */
public class CopyDirectoryVisitorTest extends TestArguments {

    private Path targetDir;

    @AfterEach
    public void afterEach() throws IOException {
        PathUtils.deleteDirectory(targetDir);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        targetDir = Files.createTempDirectory(getClass().getCanonicalName() + "-target");
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryEmptyFolder(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Files.createTempDirectory(getClass().getSimpleName());
        try {
            assertCounts(1, 0, 0,
                    PathUtils.visitFileTree(new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING), sourceDir));
        } finally {
            Files.deleteIfExists(sourceDir);
        }
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders1FileSize0(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0");
        assertCounts(1, 1, 0, PathUtils.visitFileTree(
                new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING),
                sourceDir));
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders1FileSize1(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1");
        assertCounts(1, 1, 1, PathUtils.visitFileTree(
                new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING),
                sourceDir));
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders2FileSize2(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
        assertCounts(3, 2, 2, PathUtils.visitFileTree(
                new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING),
                sourceDir));
    }

}
