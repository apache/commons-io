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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class PathUtilsTest extends TestArguments {

    @Test
    public void testCopyFile() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
            final Path targetFile = PathUtils.copyFileToDirectory(
                    sourceFile, tempDir);
            assertTrue(Files.exists(targetFile));
            assertEquals(Files.size(sourceFile), Files.size(targetFile));
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

}
