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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link DeletingPathVisitor}.
 */
public class CleaningPathVisitorTest extends TestArguments {

    private Path tempDir;

    @AfterEach
    public void afterEach() throws IOException {
        // temp dir should still exist since we are cleaning and not deleting.
        assertTrue(Files.exists(tempDir));
        // backstop
        if (Files.exists(tempDir) && PathUtils.isEmptyDirectory(tempDir)) {
            Files.deleteIfExists(tempDir);
        }
    }

    private void applyCleanEmptyDirectory(final CleaningPathVisitor visitor) throws IOException {
        Files.walkFileTree(tempDir, visitor);
        assertCounts(1, 0, 0, visitor);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        tempDir = Files.createTempDirectory(getClass().getCanonicalName());
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("cleaningPathVisitors")
    public void testCleanEmptyDirectory(final CleaningPathVisitor visitor) throws IOException {
        applyCleanEmptyDirectory(visitor);
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCleanEmptyDirectoryNullCtorArg(final PathCounters pathCounters) throws IOException {
        applyCleanEmptyDirectory(new CleaningPathVisitor(pathCounters, (String[]) null));
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("cleaningPathVisitors")
    public void testCleanFolders1FileSize0(final CleaningPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"), tempDir);
        assertCounts(1, 1, 0, PathUtils.visitFileTree(visitor, tempDir));
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("cleaningPathVisitors")
    public void testCleanFolders1FileSize1(final CleaningPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDir);
        assertCounts(1, 1, 1, PathUtils.visitFileTree(visitor, tempDir));
    }

    /**
     * Tests a directory with one file of size 1 but skip that file.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCleanFolders1FileSize1Skip(final PathCounters pathCounters) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDir);
        final String skipFileName = "file-size-1.bin";
        final CountingPathVisitor visitor = new CleaningPathVisitor(pathCounters, skipFileName);
        assertCounts(1, 1, 1, PathUtils.visitFileTree(visitor, tempDir));
        final Path skippedFile = tempDir.resolve(skipFileName);
        Assertions.assertTrue(Files.exists(skippedFile));
        Files.delete(skippedFile);
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("cleaningPathVisitors")
    public void testCleanFolders2FileSize2(final CleaningPathVisitor visitor) throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"), tempDir);
        assertCounts(3, 2, 2, PathUtils.visitFileTree(visitor, tempDir));
    }
}
