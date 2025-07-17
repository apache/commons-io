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
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DeletingPathVisitor}.
 */
class PathUtilsDeleteTest extends AbstractTempDirTest {

    @Test
    void testDeleteDirectory1FileSize0() throws IOException {
        final String fileName = "file-size-0.bin";
        FileUtils.copyFileToDirectory(
            Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0/" + fileName).toFile(),
            tempDirPath.toFile());
        assertCounts(0, 1, 0, PathUtils.delete(tempDirPath.resolve(fileName)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    private void testDeleteDirectory1FileSize0(final DeleteOption... options) throws IOException {
        final String fileName = "file-size-0.bin";
        FileUtils.copyFileToDirectory(
            Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0/" + fileName).toFile(),
            tempDirPath.toFile());
        assertCounts(0, 1, 0, PathUtils.delete(tempDirPath.resolve(fileName), options));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    void testDeleteDirectory1FileSize0ForceOff() throws IOException {
        testDeleteDirectory1FileSize0();
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @Test
    void testDeleteDirectory1FileSize0ForceOn() throws IOException {
        testDeleteDirectory1FileSize0();
    }

    @Test
    void testDeleteDirectory1FileSize0NoOption() throws IOException {
        testDeleteDirectory1FileSize0(PathUtils.EMPTY_DELETE_OPTION_ARRAY);
    }

    @Test
    void testDeleteDirectory1FileSize0OverrideReadonly() throws IOException {
        testDeleteDirectory1FileSize0(StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

    @Test
    void testDeleteDirectory1FileSize1() throws IOException {
        final String fileName = "file-size-1.bin";
        FileUtils.copyFileToDirectory(
            Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName).toFile(),
            tempDirPath.toFile());
        assertCounts(0, 1, 1, PathUtils.delete(tempDirPath.resolve(fileName)));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    private void testDeleteDirectory1FileSize1(final DeleteOption... options) throws IOException {
        // TODO Setup the test to use LinkOption.
        final String fileName = "file-size-1.bin";
        FileUtils.copyFileToDirectory(
            Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/" + fileName).toFile(),
            tempDirPath.toFile());
        assertCounts(0, 1, 1, PathUtils.delete(tempDirPath.resolve(fileName), options));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    void testDeleteDirectory1FileSize1ForceOff() throws IOException {
        testDeleteDirectory1FileSize1();
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @Test
    void testDeleteDirectory1FileSize1ForceOn() throws IOException {
        testDeleteDirectory1FileSize1();
    }

    @Test
    void testDeleteDirectory1FileSize1NoOption() throws IOException {
        testDeleteDirectory1FileSize1(PathUtils.EMPTY_DELETE_OPTION_ARRAY);
    }

    @Test
    void testDeleteDirectory1FileSize1OverrideReadOnly() throws IOException {
        testDeleteDirectory1FileSize1(StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

    /**
     * Tests an empty folder.
     */
    @Test
    void testDeleteEmptyDirectory() throws IOException {
        testDeleteEmptyDirectory(PathUtils.delete(tempDirPath));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    /**
     * Tests an empty folder.
     */
    private void testDeleteEmptyDirectory(final DeleteOption... options) throws IOException {
        testDeleteEmptyDirectory(PathUtils.delete(tempDirPath, options));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }

    private void testDeleteEmptyDirectory(final PathCounters pathCounts) {
        assertCounts(1, 0, 0, pathCounts);
    }

    /**
     * Tests an empty folder.
     */
    @Test
    void testDeleteEmptyDirectoryForceOff() throws IOException {
        testDeleteEmptyDirectory();
    }

    /**
     * Tests an empty folder.
     */
    @Test
    void testDeleteEmptyDirectoryForceOn() throws IOException {
        testDeleteEmptyDirectory();
    }

    @Test
    void testDeleteEmptyDirectoryNoOption() throws IOException {
        testDeleteEmptyDirectory(PathUtils.EMPTY_DELETE_OPTION_ARRAY);
    }

    @Test
    void testDeleteEmptyDirectoryOverrideReadOnly() throws IOException {
        testDeleteEmptyDirectory(StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

    /**
     * Tests a file that does not exist.
     */
    @Test
    void testDeleteFileDoesNotExist() throws IOException {
        assertCounts(0, 0, 0, PathUtils.deleteFile(tempDirPath.resolve("file-does-not-exist.bin")));
        // This will throw if not empty.
        Files.deleteIfExists(tempDirPath);
    }
}
