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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Deletes files but not directories as a visit proceeds.
 *
 * @since 2.7
 */
public class CleaningPathVisitor extends CountingPathVisitor {

    /**
     * Constructs a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return A new instance configured with a BigInteger {@link PathCounters}.
     */
    public static CountingPathVisitor withBigIntegerCounters() {
        return new CleaningPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Constructs a new instance configured with a long {@link PathCounters}.
     *
     * @return A new instance configured with a long {@link PathCounters}.
     */
    public static CountingPathVisitor withLongCounters() {
        return new CleaningPathVisitor(Counters.longPathCounters());
    }

    private final String[] skip;
    private final boolean overrideReadOnly;

    /**
     * Constructs a instance that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     * @param deleteOption How deletion is handled.
     * @param skip The files to skip deleting.
     * @since 2.8.0
     */
    public CleaningPathVisitor(final PathCounters pathCounter, final DeleteOption[] deleteOption, final String... skip) {
        super(pathCounter);
        final String[] temp = skip != null ? skip.clone() : EMPTY_STRING_ARRAY;
        Arrays.sort(temp);
        this.skip = temp;
        this.overrideReadOnly = StandardDeleteOption.overrideReadOnly(deleteOption);
    }

    /**
     * Constructs a new instance that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     * @param skip The files to skip deleting.
     */
    public CleaningPathVisitor(final PathCounters pathCounter, final String... skip) {
        this(pathCounter, PathUtils.EMPTY_DELETE_OPTION_ARRAY, skip);
    }

    /**
     * Returns true to process the given path, false if not.
     *
     * @param path The path to test.
     * @return true to process the given path, false if not.
     */
    private boolean accept(final Path path) {
        return Arrays.binarySearch(skip, PathUtils.getFileNameString(path)) < 0;
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
        final CleaningPathVisitor other = (CleaningPathVisitor) obj;
        return overrideReadOnly == other.overrideReadOnly && Arrays.equals(skip, other.skip);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(skip);
        result = prime * result + Objects.hash(overrideReadOnly);
        return result;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        super.preVisitDirectory(dir, attributes);
        return accept(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        // Files.deleteIfExists() never follows links, so use LinkOption.NOFOLLOW_LINKS in other calls to Files.
        if (accept(file) && Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            final Path parent = file.getParent();
            final PosixFileAttributes parentAttributes = overrideReadOnly ? PathUtils.readPosixFileAttributes(parent, LinkOption.NOFOLLOW_LINKS) : null;
            boolean fileWasReadOnly = false;
            Throwable failure = null;
            try {
                if (overrideReadOnly) {
                    fileWasReadOnly = PathUtils.isReadOnly(file, LinkOption.NOFOLLOW_LINKS);
                    PathUtils.setReadOnly(file, false, LinkOption.NOFOLLOW_LINKS);
                }
                Files.deleteIfExists(file);
            } catch (final IOException | RuntimeException | Error e) {
                failure = e;
                throw e;
            } finally {
                Throwable restorationFailure = null;
                try {
                    if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && fileWasReadOnly) {
                        PathUtils.setReadOnly(file, true, LinkOption.NOFOLLOW_LINKS);
                    }
                } catch (final IOException | RuntimeException | Error e) {
                    restorationFailure = e;
                }
                try {
                    if (parentAttributes != null) {
                        Files.setPosixFilePermissions(parent, parentAttributes.permissions());
                    }
                } catch (final IOException | RuntimeException | Error e) {
                    if (restorationFailure == null) {
                        restorationFailure = e;
                    } else if (restorationFailure != e) {
                        restorationFailure.addSuppressed(e);
                    }
                }
                if (restorationFailure != null) {
                    if (failure == null) {
                        throwRestorationFailure(restorationFailure);
                    }
                    if (failure != restorationFailure) {
                        failure.addSuppressed(restorationFailure);
                    }
                }
            }
        }
        updateFileCounters(file, attributes);
        return FileVisitResult.CONTINUE;
    }

    private static void throwRestorationFailure(final Throwable failure) throws IOException {
        if (failure instanceof IOException) {
            throw (IOException) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        throw new IOException(failure);
    }
}
