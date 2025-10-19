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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Provides services for test subclasses.
 */
public abstract class AbstractTempDirTest {

    protected static final String SUB_DIR = "subdir";
    protected static final String SYMLINKED_DIR = "symlinked-dir";

    /**
     * Creates directory test fixtures in the given directory {@code rootDir}.
     * <ol>
     * <li>{@code rootDir/subdir}</li>
     * <li>{@code rootDir/symlinked-dir} -> {@code rootDir/subdir}</li>
     * </ol>
     * @param rootDir Root for directory entries.
     * @return Path for {@code tempDirPath/subdir}.
     * @throws IOException if an I/O error occurs or the parent directory does not exist.
     */
    protected static Path createTempSymbolicLinkedRelativeDir(final Path rootDir) throws IOException {
        final Path targetDir = rootDir.resolve(SUB_DIR);
        final Path symlinkDir = rootDir.resolve(SYMLINKED_DIR);
        Files.createDirectory(targetDir);
        return Files.createSymbolicLink(symlinkDir, targetDir);
    }

    /**
     * A temporary directory managed by JUnit.
     */
    @TempDir
    public Path managedTempDirPath;

    /**
     * A File version of this test's Path object.
     */
    public File tempDirFile;

    /**
     * A temporary directory managed by each test so we can optionally fiddle with its permissions independently.
     */
    public Path tempDirPath;

    @BeforeEach
    public void beforeEachCreateTempDirs() throws IOException {
        tempDirPath = Files.createTempDirectory(managedTempDirPath, getClass().getSimpleName());
        tempDirFile = tempDirPath.toFile();
    }

    @SuppressWarnings("resource") // no FileSystem allocation
    protected final boolean isPosixFilePermissionsSupported() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}
