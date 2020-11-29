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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsTest extends TestArguments {

    private static final String PATH_FIXTURE = "NOTICE.txt";

    /**
     * A temporary directory managed by JUnit.
     */
    @TempDir
    public Path tempDir;

    @Test
    public void testCopyFile() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final Path sourceFile = Paths
                .get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
            final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDir);
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.size(sourceFile), Files.size(targetFile));
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCreateDirectoriesAlreadyExists() throws IOException {
        assertEquals(tempDir.getParent(), PathUtils.createParentDirectories(tempDir));
    }

    @Test
    public void testCreateDirectoriesNew() throws IOException {
        assertEquals(tempDir, PathUtils.createParentDirectories(tempDir.resolve("child")));
    }

    @Test
    public void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (final DirectoryStream<Path> stream = PathUtils.newDirectoryStream(PathUtils.current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, path.getFileName().toString());
            assertFalse(iterator.hasNext());
        }
    }

}
