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
import java.util.function.UnaryOperator;

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

    /**
     * Builds instances of {@link CountingPathVisitor}.
     *
     * @param <T> The CountingPathVisitor type.
     * @param <B> The AbstractBuilder type.
     * @since 2.19.0
     */
    public abstract static class AbstractBuilder<T, B extends AbstractBuilder<T, B>> extends SimplePathVisitor.AbstractBuilder<T, B> {

        private PathCounters pathCounters = defaultPathCounters();
        private PathFilter fileFilter = defaultFileFilter();
        private PathFilter directoryFilter = defaultDirectoryFilter();
        private UnaryOperator<Path> directoryPostTransformer = defaultDirectoryTransformer();

        /**
         * Constructs a new builder for subclasses.
         */
        public AbstractBuilder() {
            // empty.
        }

        PathFilter getDirectoryFilter() {
            return directoryFilter;
        }

        UnaryOperator<Path> getDirectoryPostTransformer() {
            return directoryPostTransformer;
        }

        PathFilter getFileFilter() {
            return fileFilter;
        }

        PathCounters getPathCounters() {
            return pathCounters;
        }

        /**
         * Sets how to filter directories.
         *
         * @param directoryFilter how to filter files.
         * @return this instance.
         */
        public B setDirectoryFilter(final PathFilter directoryFilter) {
            this.directoryFilter = directoryFilter != null ? directoryFilter : defaultDirectoryFilter();
            return asThis();
        }

        /**
         * Sets how to transform directories, defaults to {@link UnaryOperator#identity()}.
         *
         * @param directoryTransformer how to filter files.
         * @return this instance.
         */
        public B setDirectoryPostTransformer(final UnaryOperator<Path> directoryTransformer) {
            this.directoryPostTransformer = directoryTransformer != null ? directoryTransformer : defaultDirectoryTransformer();
            return asThis();
        }

        /**
         * Sets how to filter files.
         *
         * @param fileFilter how to filter files.
         * @return this instance.
         */
        public B setFileFilter(final PathFilter fileFilter) {
            this.fileFilter = fileFilter != null ? fileFilter : defaultFileFilter();
            return asThis();
        }

        /**
         * Sets how to count path visits.
         *
         * @param pathCounters How to count path visits.
         * @return this instance.
         */
        public B setPathCounters(final PathCounters pathCounters) {
            this.pathCounters = pathCounters != null ? pathCounters : defaultPathCounters();
            return asThis();
        }
    }

    /**
     * Builds instances of {@link CountingPathVisitor}.
     *
     * @since 2.18.0
     */
    public static class Builder extends AbstractBuilder<CountingPathVisitor, Builder> {

        /**
         * Constructs a new builder.
         */
        public Builder() {
            // empty.
        }

        @Override
        public CountingPathVisitor get() {
            return new CountingPathVisitor(this);
        }
    }

    static final String[] EMPTY_STRING_ARRAY = {};

    static IOFileFilter defaultDirectoryFilter() {
        return TrueFileFilter.INSTANCE;
    }

    static UnaryOperator<Path> defaultDirectoryTransformer() {
        return UnaryOperator.identity();
    }

    static IOFileFilter defaultFileFilter() {
        return new SymbolicLinkFileFilter(FileVisitResult.TERMINATE, FileVisitResult.CONTINUE);
    }

    static PathCounters defaultPathCounters() {
        return Counters.longPathCounters();
    }

    /**
     * Constructs a new instance configured with a {@link BigInteger} {@link PathCounters}.
     *
     * @return a new instance configured with a {@link BigInteger} {@link PathCounters}.
     */
    public static CountingPathVisitor withBigIntegerCounters() {
        return new Builder().setPathCounters(Counters.bigIntegerPathCounters()).get();
    }

    /**
     * Constructs a new instance configured with a {@code long} {@link PathCounters}.
     *
     * @return a new instance configured with a {@code long} {@link PathCounters}.
     */
    public static CountingPathVisitor withLongCounters() {
        return new Builder().setPathCounters(Counters.longPathCounters()).get();
    }

    private final PathCounters pathCounters;
    private final PathFilter fileFilter;
    private final PathFilter directoryFilter;
    private final UnaryOperator<Path> directoryPostTransformer;

    CountingPathVisitor(final AbstractBuilder<?, ?> builder) {
        super(builder);
        this.pathCounters = builder.getPathCounters();
        this.fileFilter = builder.getFileFilter();
        this.directoryFilter = builder.getDirectoryFilter();
        this.directoryPostTransformer = builder.getDirectoryPostTransformer();
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounters How to count path visits.
     * @see Builder
     */
    public CountingPathVisitor(final PathCounters pathCounters) {
        this(new Builder().setPathCounters(pathCounters));
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounters    How to count path visits.
     * @param fileFilter      Filters which files to count.
     * @param directoryFilter Filters which directories to count.
     * @see Builder
     * @since 2.9.0
     */
    public CountingPathVisitor(final PathCounters pathCounters, final PathFilter fileFilter, final PathFilter directoryFilter) {
        this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
        this.directoryPostTransformer = UnaryOperator.identity();
    }

    /**
     * Constructs a new instance.
     *
     * @param pathCounters    How to count path visits.
     * @param fileFilter      Filters which files to count.
     * @param directoryFilter Filters which directories to count.
     * @param visitFileFailed Called on {@link #visitFileFailed(Path, IOException)}.
     * @since 2.12.0
     * @deprecated Use {@link Builder}.
     */
    @Deprecated
    public CountingPathVisitor(final PathCounters pathCounters, final PathFilter fileFilter, final PathFilter directoryFilter,
            final IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed) {
        super(visitFileFailed);
        this.pathCounters = Objects.requireNonNull(pathCounters, "pathCounters");
        this.fileFilter = Objects.requireNonNull(fileFilter, "fileFilter");
        this.directoryFilter = Objects.requireNonNull(directoryFilter, "directoryFilter");
        this.directoryPostTransformer = UnaryOperator.identity();
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
        updateDirCounter(directoryPostTransformer.apply(dir), exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        final FileVisitResult accept = directoryFilter.accept(dir, attributes);
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
     * @param file       the visited file.
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
