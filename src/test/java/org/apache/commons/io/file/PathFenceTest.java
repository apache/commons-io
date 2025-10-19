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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link PathFence}.
 */
public class PathFenceTest {

    private Path createDirectory(final Path tempDir, final String other) throws IOException {
        return Files.createDirectory(tempDir.resolve(other));
    }

    @Test
    void testAbsolutePath(@TempDir final Path fenceRootPath) throws Exception {
        // tempDir is the fence
        final Path childTest = fenceRootPath.resolve("child/file.txt");
        final PathFence fence = PathFence.builder().setRoots(fenceRootPath).get();
        // getPath with an absolute string should be allowed
        final Path childOk = fence.apply(childTest.toString());
        assertEquals(childTest.toAbsolutePath().normalize(), childOk.toAbsolutePath().normalize());
        // check with a Path instance should return the same instance when allowed
        assertSame(childTest, fence.apply(childTest));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ".", "some", "some/relative", "some/relative/path" })
    void testEmpty(final String test) {
        // An empty fence accepts anything
        final PathFence fence = PathFence.builder().get();
        final Path path = Paths.get(test);
        assertEquals(path, fence.apply(test));
        assertSame(path, fence.apply(path));
    }

    @Test
    void testMultipleFencePaths(@TempDir final Path tempDir) throws Exception {
        // The fence is inside tempDir
        final Path fenceRootPath1 = createDirectory(tempDir, "root-one");
        final Path fenceRootPath2 = createDirectory(tempDir, "root-two");
        final PathFence fence = PathFence.builder().setRoots(fenceRootPath1, fenceRootPath2).get();
        // Path under the first path should be allowed
        final Path inPath1 = fenceRootPath1.resolve("a/b.txt");
        assertSame(inPath1, fence.apply(inPath1));
        // Path under the second path should be allowed
        final Path inPath2 = fenceRootPath2.resolve("a/b.txt");
        assertSame(inPath2, fence.apply(inPath2));
    }

    @Test
    void testNormalization(@TempDir final Path tempDir) throws Exception {
        final Path fenceRootPath = createDirectory(tempDir, "root-one");
        final Path weird = fenceRootPath.resolve("subdir/../other.txt");
        final PathFence fence = PathFence.builder().setRoots(fenceRootPath).get();
        // Path contains '..' but after normalization it's still inside the base
        assertSame(weird, fence.apply(weird));
    }

    @Test
    void testOutsideFenceThrows(@TempDir final Path tempDir) throws Exception {
        final Path fenceRootPath = createDirectory(tempDir, "root-one");
        final Path other = createDirectory(tempDir, "other");
        final PathFence fence = PathFence.builder().setRoots(fenceRootPath).get();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fence.apply(other.toString()));
        final String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("not in the fence"), () -> "Expected message to mention fence: " + msg);
        assertTrue(msg.contains(other.toAbsolutePath().toString()), () -> "Expected message to contain the path: " + msg);
    }
}
