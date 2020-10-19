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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Accumulates normalized paths during visitation.
 * <p>
 * Use with care on large file trees as each visited Path element is remembered.
 * </p>
 * <h2>Example</h2>
 *
 * <pre>
 * Path dir = Paths.get("");
 * // We are interested in files older than one day
 * long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new AgeFileFilter(cutoff));
 * //
 * // Walk one dir
 * Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getFileList());
 * //
 * visitor.getPathCounters().reset();
 * //
 * // Walk dir tree
 * Files.walkFileTree(dir, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getDirList());
 * System.out.println(visitor.getFileList());
 * </pre>
 *
 * @since 2.7
 */
public class AccumulatorPathVisitor extends CountingPathVisitor {

    /**
     * Creates a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return a new instance configured with a BigInteger {@link PathCounters}.
     */
    public static AccumulatorPathVisitor withBigIntegerCounters() {
        return new AccumulatorPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Creates a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @param fileFilter Filters files to accumulate and count.
     * @param dirFilter Filters directories to accumulate and count.
     * @return a new instance configured with a long {@link PathCounters}.
     * @since 2.9.0
     */
    public static AccumulatorPathVisitor withBigIntegerCounters(final PathFilter fileFilter,
        final PathFilter dirFilter) {
        return new AccumulatorPathVisitor(Counters.bigIntegerPathCounters(), fileFilter, dirFilter);
    }

    /**
     * Creates a new instance configured with a long {@link PathCounters}.
     *
     * @return a new instance configured with a long {@link PathCounters}.
     */
    public static AccumulatorPathVisitor withLongCounters() {
        return new AccumulatorPathVisitor(Counters.longPathCounters());
    }

    /**
     * Creates a new instance configured with a long {@link PathCounters}.
     *
     * @param fileFilter Filters files to accumulate and count.
     * @param dirFilter Filters directories to accumulate and count.
     * @return a new instance configured with a long {@link PathCounters}.
     * @since 2.9.0
     */
    public static AccumulatorPathVisitor withLongCounters(final PathFilter fileFilter, final PathFilter dirFilter) {
        return new AccumulatorPathVisitor(Counters.longPathCounters(), fileFilter, dirFilter);
    }

    private final List<Path> dirList = new ArrayList<>();

    private final List<Path> fileList = new ArrayList<>();

    /**
     * Constructs a new instance.
     *
     * @since 2.9.0
     */
    public AccumulatorPathVisitor() {
        super(Counters.noopPathCounters());
    }

    /**
     * Constructs a new instance that counts file system elements.
     *
     * @param pathCounter How to count path visits.
     */
    public AccumulatorPathVisitor(final PathCounters pathCounter) {
        super(pathCounter);
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     * @param fileFilter Filters which files to count.
     * @param dirFilter Filters which directories to count.
     * @since 2.9.0
     */
    public AccumulatorPathVisitor(final PathCounters pathCounter, final PathFilter fileFilter,
        final PathFilter dirFilter) {
        super(pathCounter, fileFilter, dirFilter);
    }

    private void add(final List<Path> list, final Path dir) {
        list.add(dir.normalize());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AccumulatorPathVisitor)) {
            return false;
        }
        final AccumulatorPathVisitor other = (AccumulatorPathVisitor) obj;
        return Objects.equals(dirList, other.dirList) && Objects.equals(fileList, other.fileList);
    }

    /**
     * Gets the list of visited directories.
     *
     * @return the list of visited directories.
     */
    public List<Path> getDirList() {
        return dirList;
    }

    /**
     * Gets the list of visited files.
     *
     * @return the list of visited files.
     */
    public List<Path> getFileList() {
        return fileList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dirList, fileList);
        return result;
    }

    /**
     * Relativizes each directory path with {@link Path#relativize(Path)} against the given {@code parent}, optionally
     * sorting the result.
     *
     * @param parent A parent path
     * @param sort Whether to sort
     * @param comparator How to sort, null uses default sorting.
     * @return A new list
     */
    public List<Path> relativizeDirectories(final Path parent, final boolean sort,
        final Comparator<? super Path> comparator) {
        return PathUtils.relativize(getDirList(), parent, sort, comparator);
    }

    /**
     * Relativizes each file path with {@link Path#relativize(Path)} against the given {@code parent}, optionally
     * sorting the result.
     *
     * @param parent A parent path
     * @param sort Whether to sort
     * @param comparator How to sort, null uses default sorting.
     * @return A new list
     */
    public List<Path> relativizeFiles(final Path parent, final boolean sort,
        final Comparator<? super Path> comparator) {
        return PathUtils.relativize(getFileList(), parent, sort, comparator);
    }

    @Override
    protected void updateDirCounter(final Path dir, final IOException exc) {
        super.updateDirCounter(dir, exc);
        add(dirList, dir);
    }

    @Override
    protected void updateFileCounters(final Path file, final BasicFileAttributes attributes) {
        super.updateFileCounters(file, attributes);
        add(fileList, file);
    }

}
