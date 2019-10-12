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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsIsEmptyTest {

    private static final Path DIR_SIZE_1 = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1");

    private static final Path FILE_SIZE_0 = Paths
            .get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0/file-size-0.bin");

    private static final Path FILE_SIZE_1 = Paths
            .get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");

    @Test
    public void testIsEmpty() throws IOException {
        Assertions.assertTrue(PathUtils.isEmpty(FILE_SIZE_0));
        Assertions.assertFalse(PathUtils.isEmpty(FILE_SIZE_1));
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            Assertions.assertTrue(PathUtils.isEmpty(tempDir));
        } finally {
            Files.delete(tempDir);
        }
        Assertions.assertFalse(PathUtils.isEmpty(DIR_SIZE_1));
    }

    @Test
    public void testisEmptyDirectory() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            Assertions.assertTrue(PathUtils.isEmptyDirectory(tempDir));
        } finally {
            Files.delete(tempDir);
        }
        Assertions.assertFalse(PathUtils.isEmptyDirectory(DIR_SIZE_1));
    }

    @Test
    public void testisEmptyFile() throws IOException {
        Assertions.assertTrue(PathUtils.isEmptyFile(FILE_SIZE_0));
        Assertions.assertFalse(PathUtils.isEmptyFile(FILE_SIZE_1));
    }
}
