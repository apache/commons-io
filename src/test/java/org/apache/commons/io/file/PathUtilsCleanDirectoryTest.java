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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link DeletingPathVisitor}.
 */
class PathUtilsCleanDirectoryTest {

    @TempDir
    private Path tempDir;

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    void testCleanDirectory1FileSize0() throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"), tempDir);
        assertCounts(1, 1, 0, PathUtils.cleanDirectory(tempDir));
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    void testCleanDirectory1FileSize1() throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDir);
        assertCounts(1, 1, 1, PathUtils.cleanDirectory(tempDir));
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @Test
    void testCleanDirectory2FileSize2() throws IOException {
        PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"), tempDir);
        assertCounts(3, 2, 2, PathUtils.cleanDirectory(tempDir));
    }

    /**
     * Tests an empty folder.
     */
    @Test
    void testCleanEmptyDirectory() throws IOException {
        assertCounts(1, 0, 0, PathUtils.cleanDirectory(tempDir));
    }
}
