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

import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsCountingTest {

    /**
     * Tests an empty folder.
     */
    @Test
    public void testCountEmptyFolder() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final PathCounters pathCounts = PathUtils.countDirectory(tempDir);
            assertCounts(1, 0, 0, pathCounts);
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    public void testCountFolders1FileSize0() throws IOException {
        final PathCounters pathCounts = PathUtils
                .countDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"));
        assertCounts(1, 1, 0, pathCounts);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    public void testCountFolders1FileSize1() throws IOException {
        final PathCounters visitor = PathUtils
                .countDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"));
        assertCounts(1, 1, 1, visitor);
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @Test
    public void testCountFolders2FileSize2() throws IOException {
        final PathCounters pathCounts = PathUtils
                .countDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"));
        assertCounts(3, 2, 2, pathCounts);
    }
}
