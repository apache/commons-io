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
 * A {@link FileFilter} providing conditional OR logic across a list of file filters. This filter returns
 * {@code true} if any filters in the list return {@code true}. Otherwise, it returns {@code false}. Checking of the
 * file filter list stops when the first filter returns {@code true}.
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 1.0
 * @see FileFilterUtils#or(IOFileFilter...)
 */
public class OrFileFilter extends AbstractFileFilter implements ConditionalFileFilter, Serializable {

    private static final long serialVersionUID = 5767770777065432721L;

    /** The list of file filters. */
    private final List<IOFileFilter> fileFilters;

    /**
     * Constructs a new instance of {@link OrFileFilter}.
     *
     * @since 1.1
     */
    public OrFileFilter() {
        this(0);
    }

    /**
     * Constructs a new instance with the given initial list.
     *
     * @param initialList the initial list.
     */
    private OrFileFilter(final ArrayList<IOFileFilter> initialList) {
        this.fileFilters = Objects.requireNonNull(initialList, "initialList");
    }

    /**
     * Constructs a new instance with the given initial capacity.
     *
     * @param initialCapacity the initial capacity.
     */
    private OrFileFilter(final int initialCapacity) {
        this(new ArrayList<>(initialCapacity));
    }

    /**
     * Constructs a new instance for the give filters.
     * @param fileFilters filters to OR.
     * @since 2.9.0
     */
    public OrFileFilter(final IOFileFilter... fileFilters) {
        this(Objects.requireNonNull(fileFilters, "fileFilters").length);
        addFileFilter(fileFilters);
    }

    /**
     * Constructs a new file filter that ORs the result of other filters.
     *
     * @param filter1 the first filter, must not be null
     * @param filter2 the second filter, must not be null
     * @throws IllegalArgumentException if either filter is null
     */
    public OrFileFilter(final IOFileFilter filter1, final IOFileFilter filter2) {
        this(2);
        addFileFilter(filter1);
        addFileFilter(filter2);
    }

    /**
     * Constructs a new instance of {@link OrFileFilter} with the specified filters.
     *
     * @param fileFilters the file filters for this filter, copied.
     * @since 1.1
     */
    public OrFileFilter(final List<IOFileFilter> fileFilters) {
        this(new ArrayList<>(Objects.requireNonNull(fileFilters, "fileFilters")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file) {
        return fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file, final String name) {
        return fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return toDefaultFileVisitResult(fileFilters.stream().anyMatch(fileFilter -> fileFilter.accept(file, attributes) == FileVisitResult.CONTINUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFileFilter(final IOFileFilter fileFilter) {
        this.fileFilters.add(Objects.requireNonNull(fileFilter, "fileFilter"));
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
        return Collections.unmodifiableList(this.fileFilters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeFileFilter(final IOFileFilter fileFilter) {
        return this.fileFilters.remove(fileFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFileFilters(final List<IOFileFilter> fileFilters) {
        this.fileFilters.clear();
        this.fileFilters.addAll(Objects.requireNonNull(fileFilters, "fileFilters"));
    }

    /**
     * Provide a String representation of this file filter.
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
