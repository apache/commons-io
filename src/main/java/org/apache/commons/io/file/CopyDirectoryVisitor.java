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
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Copies a source directory to a target directory.
 *
 * @since 2.7
 */
public class CopyDirectoryVisitor extends CountingPathVisitor {

    private static final CopyOption[] EMPTY_COPY_OPTIONS = new CopyOption[0];

    private final CopyOption[] copyOptions;
    private final Path sourceDirectory;
    private final Path targetDirectory;

    /**
     * Constructs a new visitor that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     * @param sourceDirectory The source directory
     * @param targetDirectory The target directory
     * @param copyOptions Specifies how the copying should be done.
     */
    public CopyDirectoryVisitor(final PathCounters pathCounter, final Path sourceDirectory, final Path targetDirectory,
            final CopyOption... copyOptions) {
        super(pathCounter);
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        this.copyOptions = copyOptions == null ? EMPTY_COPY_OPTIONS : copyOptions.clone();
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes)
            throws IOException {
        final Path newTargetDir = targetDirectory.resolve(sourceDirectory.relativize(directory));
        if (Files.notExists(newTargetDir)) {
            Files.createDirectory(newTargetDir);
        }
        return super.preVisitDirectory(directory, attributes);
    }

    @Override
    public FileVisitResult visitFile(final Path sourceFile, final BasicFileAttributes attributes) throws IOException {
        final Path targetFile = targetDirectory.resolve(sourceDirectory.relativize(sourceFile));
        Files.copy(sourceFile, targetFile, copyOptions);
        return super.visitFile(targetFile, attributes);
    }

}
