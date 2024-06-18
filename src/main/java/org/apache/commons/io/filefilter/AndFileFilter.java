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
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link FileFilter} providing conditional AND logic across a list of
 * file filters. This filter returns {@code true} if all filters in the
 * list return {@code true}. Otherwise, it returns {@code false}.
 * Checking of the file filter list stops when the first filter returns
 * {@code false}.
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 1.0
 * @see FileFilterUtils#and(IOFileFilter...)
 */
public class AndFileFilter extends AbstractFileFilter implements ConditionalFileFilter, Serializable {

    private static final long serialVersionUID = 7215974688563965257L;

    /** The list of file filters. */
    private final List<IOFileFilter> fileFilters;

    /**
     * Constructs a new empty instance.
     *
     * @since 1.1
     */
    public AndFileFilter() {
        this(0);
    }

    /**
     * Constructs a new instance with the given initial list.
     *
     * @param initialList the initial list.
     */
    private AndFileFilter(final ArrayList<IOFileFilter> initialList) {
        this.fileFilters = Objects.requireNonNull(initialList, "initialList");
    }

    /**
     * Constructs a new instance with the given initial capacity.
     *
     * @param initialCapacity the initial capacity.
     */
    private AndFileFilter(final int initialCapacity) {
        this(new ArrayList<>(initialCapacity));
    }

    /**
     * Constructs a new instance for the give filters.
     *
     * @param fileFilters filters to OR.
     * @since 2.9.0
     */
    public AndFileFilter(final IOFileFilter... fileFilters) {
        this(Objects.requireNonNull(fileFilters, "fileFilters").length);
        addFileFilter(fileFilters);
    }

    /**
     * Constructs a new file filter that ANDs the result of other filters.
     *
     * @param filter1  the first filter, must second be null
     * @param filter2  the first filter, must not be null
     * @throws IllegalArgumentException if either filter is null
     */
    public AndFileFilter(final IOFileFilter filter1, final IOFileFilter filter2) {
        this(2);
        addFileFilter(filter1);
        addFileFilter(filter2);
    }

    /**
     * Constructs a new instance of {@link AndFileFilter}
     * with the specified list of filters.
     *
     * @param fileFilters  a List of IOFileFilter instances, copied.
     * @since 1.1
     */
    public AndFileFilter(final List<IOFileFilter> fileFilters) {
        this(new ArrayList<>(Objects.requireNonNull(fileFilters, "fileFilters")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file) {
        return !isEmpty() && fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file, final String name) {
        return !isEmpty() && fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file, name));
    }

    /**
     * {@inheritDoc}
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return isEmpty() ? FileVisitResult.TERMINATE
                : toDefaultFileVisitResult(fileFilters.stream().allMatch(fileFilter -> fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFileFilter(final IOFileFilter fileFilter) {
        fileFilters.add(Objects.requireNonNull(fileFilter, "fileFilter"));
    }

    /**
     * Adds the given file filters.
     *
     * @param fileFilters the filters to add.
     * @since 2.9.0
     */
    public void addFileFilter(final IOFileFilter... fileFilters) {
        Stream.of(Objects.requireNonNull(fileFilters, "fileFilters")).forEach(this::addFileFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IOFileFilter> getFileFilters() {
        return Collections.unmodifiableList(fileFilters);
    }

    private boolean isEmpty() {
        return fileFilters.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeFileFilter(final IOFileFilter ioFileFilter) {
        return fileFilters.remove(ioFileFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileFilters(final List<IOFileFilter> fileFilters) {
        this.fileFilters.clear();
        this.fileFilters.addAll(fileFilters);
    }

    /**
     * Builds a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        append(fileFilters, buffer);
        buffer.append(")");
        return buffer.toString();
    }

}
