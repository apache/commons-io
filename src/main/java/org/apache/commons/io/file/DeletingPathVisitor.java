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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Deletes files and directories as a visit proceeds.
 *
 * @since 2.7
 */
public class DeletingPathVisitor extends CountingPathVisitor {

    /**
     * Constructs a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return a new instance configured with a BigInteger {@link PathCounters}.
     */
    public static DeletingPathVisitor withBigIntegerCounters() {
        return new DeletingPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Constructs a new instance configured with a long {@link PathCounters}.
     *
     * @return a new instance configured with a long {@link PathCounters}.
     */
    public static DeletingPathVisitor withLongCounters() {
        return new DeletingPathVisitor(Counters.longPathCounters());
    }

    private final String[] skip;
    private final boolean overrideReadOnly;
    private final LinkOption[] linkOptions;

    /**
     * Constructs a new visitor that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     * @param deleteOption How deletion is handled.
     * @param skip The files to skip deleting.
     * @since 2.8.0
     */
    public DeletingPathVisitor(final PathCounters pathCounter, final DeleteOption[] deleteOption, final String... skip) {
        this(pathCounter, PathUtils.noFollowLinkOptionArray(), deleteOption, skip);
    }

    /**
     * Constructs a new visitor that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     * @param linkOptions How symbolic links are handled.
     * @param deleteOption How deletion is handled.
     * @param skip The files to skip deleting.
     * @since 2.9.0
     */
    public DeletingPathVisitor(final PathCounters pathCounter, final LinkOption[] linkOptions, final DeleteOption[] deleteOption, final String... skip) {
        super(pathCounter);
        final String[] temp = skip != null ? skip.clone() : EMPTY_STRING_ARRAY;
        Arrays.sort(temp);
        this.skip = temp;
        this.overrideReadOnly = StandardDeleteOption.overrideReadOnly(deleteOption);
        // TODO Files.deleteIfExists() never follows links, so use LinkOption.NOFOLLOW_LINKS in other calls to Files.
        this.linkOptions = linkOptions == null ? PathUtils.noFollowLinkOptionArray() : linkOptions.clone();
    }

    /**
     * Constructs a new visitor that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     *
     * @param skip The files to skip deleting.
     */
    public DeletingPathVisitor(final PathCounters pathCounter, final String... skip) {
        this(pathCounter, PathUtils.EMPTY_DELETE_OPTION_ARRAY, skip);
    }

    /**
     * Returns true to process the given path, false if not.
     *
     * @param path the path to test.
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
        final DeletingPathVisitor other = (DeletingPathVisitor) obj;
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
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        if (PathUtils.isEmptyDirectory(dir)) {
            Files.deleteIfExists(dir);
        }
        return super.postVisitDirectory(dir, exc);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        super.preVisitDirectory(dir, attrs);
        return accept(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        if (accept(file)) {
            // delete files and valid links, respecting linkOptions
            if (Files.exists(file, linkOptions)) {
                if (overrideReadOnly) {
                    PathUtils.setReadOnly(file, false, linkOptions);
                }
                Files.deleteIfExists(file);
            }
            // invalid links will survive previous delete, different approach needed:
            if (Files.isSymbolicLink(file)) {
                try {
                    // deleteIfExists does not work for this case
                    Files.delete(file);
                } catch (final NoSuchFileException ignored) {
                    // ignore
                }
            }
        }
        updateFileCounters(file, attrs);
        return FileVisitResult.CONTINUE;
    }
}