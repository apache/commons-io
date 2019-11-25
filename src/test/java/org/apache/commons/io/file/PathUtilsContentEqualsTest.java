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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PathUtilsContentEqualsTest {

    @TempDir
    public File temporaryFolder;

    private String getName() {
        return this.getClass().getSimpleName();
    }

    @Test
    public void testFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertTrue(PathUtils.fileContentEquals(null, null));
        assertFalse(PathUtils.fileContentEquals(null, path1));
        assertFalse(PathUtils.fileContentEquals(path1, null));
        // both don't exist
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path2));
        assertTrue(PathUtils.fileContentEquals(path2, path1));

        // Directories
        try {
            PathUtils.fileContentEquals(temporaryFolder.toPath(), temporaryFolder.toPath());
            fail("Comparing directories should fail with an IOException");
        } catch (final IOException ioe) {
            // expected
        }

        // Different files
        final Path objFile1 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object");
        objFile1.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1);

        final Path objFile1b = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object2");
        objFile1b.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1b);

        final Path objFile2 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".collection");
        objFile2.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/util/Collection.class"), objFile2);

        assertFalse(PathUtils.fileContentEquals(objFile1, objFile2));
        assertFalse(PathUtils.fileContentEquals(objFile1b, objFile2));
        assertTrue(PathUtils.fileContentEquals(objFile1, objFile1b));

        assertTrue(PathUtils.fileContentEquals(objFile1, objFile1));
        assertTrue(PathUtils.fileContentEquals(objFile1b, objFile1b));
        assertTrue(PathUtils.fileContentEquals(objFile2, objFile2));

        // Equal files
        Files.createFile(path1);
        Files.createFile(path2);
        assertTrue(PathUtils.fileContentEquals(path1, path1));
        assertTrue(PathUtils.fileContentEquals(path1, path2));
    }

}
