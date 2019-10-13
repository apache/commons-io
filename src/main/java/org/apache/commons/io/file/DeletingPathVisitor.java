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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Deletes files and directories as a visit proceeds.
 *
 * @since 2.7
 */
public class DeletingPathVisitor extends CountingPathVisitor {


    /**
     * Creates a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return a new instance configured with a BigInteger {@link PathCounters}.
     */
    public static DeletingPathVisitor withBigIntegerCounters() {
        return new DeletingPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Creates a new instance configured with a long {@link PathCounters}.
     *
     * @return a new instance configured with a long {@link PathCounters}.
     */
    public static DeletingPathVisitor withLongCounters() {
        return new DeletingPathVisitor(Counters.longPathCounters());
    }

    private final String[] skip;

    /**
     * Constructs a new visitor that deletes files except for the files and directories explicitly given.
     *
     * @param pathCounter How to count visits.
     *
     * @param skip The files to skip deleting.
     */
    public DeletingPathVisitor(final PathCounters pathCounter, final String... skip) {
        super(pathCounter);
        final String[] temp = skip != null ? skip.clone() : EMPTY_STRING_ARRAY;
        Arrays.sort(temp);
        this.skip = temp;
    }

    /**
     * Returns true to process the given path, false if not.
     *
     * @param path the path to test.
     * @return true to process the given path, false if not.
     */
    private boolean accept(final Path path) {
        return Arrays.binarySearch(skip, path.getFileName().toString()) < 0;
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
        if (accept(file) && Files.exists(file)) {
            Files.deleteIfExists(file);
        }
        updateFileCounters(file, attrs);
        return FileVisitResult.CONTINUE;
    }
}