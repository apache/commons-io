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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TempDirectory}.
 */
public class TempDirectoryTest {

    @SuppressWarnings("resource")
    @Test
    public void testCreatePath() throws IOException {
        final TempDirectory ref;
        try (TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
            ref = tempDir;
            assertTrue(FileUtils.isEmptyDirectory(tempDir.toFile()));
        }
        assertFalse(Files.exists(ref.get()));

        // Fails with a ProviderMismatchException because the Windows FS uses "instanceof sun.nio.fs.WindowsPath".
        // WindowsPath is a class, not an interface we can proxy.
        // assertFalse(Files.exists(ref));
    }

    @SuppressWarnings("resource")
    @Test
    public void testCreateString() throws IOException {
        final TempDirectory ref;
        try (TempDirectory tempDir = TempDirectory.create(Paths.get("target"), getClass().getCanonicalName())) {
            ref = tempDir;
            assertTrue(FileUtils.isEmptyDirectory(tempDir.toFile()));
        }
        assertFalse(Files.exists(ref.get()));
    }

}
