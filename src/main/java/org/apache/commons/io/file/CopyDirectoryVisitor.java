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
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Copies a source directory to a target directory.
 *
 * @since 2.7
 */
public class CopyDirectoryVisitor extends CountingPathVisitor {

    private static CopyOption[] toCopyOption(final CopyOption... copyOptions) {
        return copyOptions == null ? PathUtils.EMPTY_COPY_OPTIONS : copyOptions.clone();
    }

    private final CopyOption[] copyOptions;
    private final Path sourceDirectory;
    private final Path targetDirectory;

    /**
     * Constructs an instance that copies all files.
     *
     * @param pathCounter How to count visits.
     * @param sourceDirectory The source directory
     * @param targetDirectory The target directory
     * @param copyOptions Specifies how the copying should be done.
     */
    public CopyDirectoryVisitor(final PathCounters pathCounter, final Path sourceDirectory, final Path targetDirectory, final CopyOption... copyOptions) {
        super(pathCounter);
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        this.copyOptions = toCopyOption(copyOptions);
    }

    /**
     * Constructs an instance that copies files matching the given file and directory filters.
     *
     * @param pathCounter How to count visits.
     * @param fileFilter How to filter file paths.
     * @param dirFilter How to filter directory paths.
     * @param sourceDirectory The source directory
     * @param targetDirectory The target directory
     * @param copyOptions Specifies how the copying should be done.
     * @since 2.9.0
     */
    public CopyDirectoryVisitor(final PathCounters pathCounter, final PathFilter fileFilter, final PathFilter dirFilter, final Path sourceDirectory,
        final Path targetDirectory, final CopyOption... copyOptions) {
        super(pathCounter, fileFilter, dirFilter);
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        this.copyOptions = toCopyOption(copyOptions);
    }

    /**
     * Copies the sourceFile to the targetFile.
     *
     * @param sourceFile the source file.
     * @param targetFile the target file.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    protected void copy(final Path sourceFile, final Path targetFile) throws IOException {
        Files.copy(sourceFile, targetFile, copyOptions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CopyDirectoryVisitor other = (CopyDirectoryVisitor) obj;
        return Arrays.equals(copyOptions, other.copyOptions) && Objects.equals(sourceDirectory, other.sourceDirectory)
            && Objects.equals(targetDirectory, other.targetDirectory);
    }

    /**
     * Gets the copy options.
     *
     * @return the copy options.
     * @since 2.8.0
     */
    public CopyOption[] getCopyOptions() {
        return copyOptions.clone();
    }

    /**
     * Gets the source directory.
     *
     * @return the source directory.
     * @since 2.8.0
     */
    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Gets the target directory.
     *
     * @return the target directory.
     * @since 2.8.0
     */
    public Path getTargetDirectory() {
        return targetDirectory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(copyOptions);
        return prime * result + Objects.hash(sourceDirectory, targetDirectory);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes)
        throws IOException {
        final Path newTargetDir = resolveRelativeAsString(directory);
        if (Files.notExists(newTargetDir)) {
            Files.createDirectory(newTargetDir);
        }
        return super.preVisitDirectory(directory, attributes);
    }

    /**
     * Relativizes against {@code sourceDirectory}, then resolves against {@code targetDirectory}.
     * <p>
     * We call {@link Path#toString()} on the relativized value because we cannot use paths from different FileSystems which throws
     * {@link ProviderMismatchException}. Special care is taken to handle differences in file system separators.
     * </p>
     *
     * @param directory the directory to relativize.
     * @return a new path, relativized against sourceDirectory, then resolved against targetDirectory.
     */
    private Path resolveRelativeAsString(final Path directory) {
        return PathUtils.resolve(targetDirectory, sourceDirectory.relativize(directory));
    }

    @Override
    public FileVisitResult visitFile(final Path sourceFile, final BasicFileAttributes attributes) throws IOException {
        final Path targetFile = resolveRelativeAsString(sourceFile);
        if (accept(sourceFile, attributes)) {
            copy(sourceFile, targetFile);
            updateFileCounters(targetFile, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

}
