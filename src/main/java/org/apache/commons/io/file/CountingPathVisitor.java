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
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SymbolicLinkFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.function.IOBiFunction;

/**
 * Counts files, directories, and sizes, as a visit proceeds.
 *
 * @since 2.7
 */
public class CountingPathVisitor extends SimplePathVisitor {

    static final String[] EMPTY_STRING_ARRAY = {};

    static IOFileFilter defaultDirFilter() {
        return TrueFileFilter.INSTANCE;
    }

    static IOFileFilter defaultFileFilter() {
        return new SymbolicLinkFileFilter(FileVisitResult.TERMINATE, FileVisitResult.CONTINUE);
    }

    /**
     * Constructs a new instance configured with a {@link BigInteger} {@link PathCounters}.
     *
     * @return a new instance configured with a {@link BigInteger} {@link PathCounters}.
     */
    public static CountingPathVisitor withBigIntegerCounters() {
        return new CountingPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Constructs a new instance configured with a {@code long} {@link PathCounters}.
     *
     * @return a new instance configured with a {@code long} {@link PathCounters}.
     */
    public static CountingPathVisitor withLongCounters() {
        return new CountingPathVisitor(Counters.longPathCounters());
    }

    private final PathCounters pathCounters;
    private final PathFilter fileFilter;
    private final PathFilter dirFilter;

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     */
    public CountingPathVisitor(final PathCounters pathCounter) {
        this(pathCounter, defaultFileFilter(), defaultDirFilter());
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     * @param fileFilter Filters which files to count.
     * @param dirFilter Filters which directories to count.
     * @since 2.9.0
     */
    public CountingPathVisitor(final PathCounters pathCounter, final PathFilter fileFilter, final PathFilter dirFilter) {
        this.pathCounters = Objects.requireNonNull(pathCounter, "pathCounter");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.dirFilter = Objects.requireNonNull(dirFilter, "dirFilter");
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     * @param fileFilter Filters which files to count.
     * @param dirFilter Filters which directories to count.
     * @param visitFileFailed Called on {@link #visitFileFailed(Path, IOException)}.
     * @since 2.12.0
     */
    public CountingPathVisitor(final PathCounters pathCounter, final PathFilter fileFilter, final PathFilter dirFilter,
        final IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed) {
        super(visitFileFailed);
        this.pathCounters = Objects.requireNonNull(pathCounter, "pathCounter");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.dirFilter = Objects.requireNonNull(dirFilter, "dirFilter");
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CountingPathVisitor)) {
            return false;
        }
        final CountingPathVisitor other = (CountingPathVisitor) obj;
        return Objects.equals(pathCounters, other.pathCounters);
    }

    /**
     * Gets the visitation counts.
     *
     * @return the visitation counts.
     */
    public PathCounters getPathCounters() {
        return pathCounters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathCounters);
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        updateDirCounter(dir, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        final FileVisitResult accept = dirFilter.accept(dir, attributes);
        return accept != FileVisitResult.CONTINUE ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        return pathCounters.toString();
    }

    /**
     * Updates the counter for visiting the given directory.
     *
     * @param dir the visited directory.
     * @param exc Encountered exception.
     * @since 2.9.0
     */
    protected void updateDirCounter(final Path dir, final IOException exc) {
        pathCounters.getDirectoryCounter().increment();
    }

    /**
     * Updates the counters for visiting the given file.
     *
     * @param file the visited file.
     * @param attributes the visited file attributes.
     */
    protected void updateFileCounters(final Path file, final BasicFileAttributes attributes) {
        pathCounters.getFileCounter().increment();
        pathCounters.getByteCounter().add(attributes.size());
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        // Note: A file can be a symbolic link to a directory.
        if (Files.exists(file) && fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE) {
            updateFileCounters(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

}
