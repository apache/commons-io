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
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * This filter produces a logical NOT of the filters specified.
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 1.0
 * @see FileFilterUtils#notFileFilter(IOFileFilter)
 */
public class NotFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 6131563330944994230L;

    /** The filter */
    private final IOFileFilter filter;

    /**
     * Constructs a new file filter that NOTs the result of another filter.
     *
     * @param filter the filter, must not be null
     * @throws NullPointerException if the filter is null
     */
    public NotFileFilter(final IOFileFilter filter) {
        Objects.requireNonNull(filter, "filter");
        this.filter = filter;
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same File.
     *
     * @param file the File to check
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file) {
        return !filter.accept(file);
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same arguments.
     *
     * @param file the File directory
     * @param name the file name
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(final File file, final String name) {
        return !filter.accept(file, name);
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same File.
     * @param file the File to check
     *
     * @return true if the filter returns false
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return not(filter.accept(file, attributes));
    }

    private FileVisitResult not(final FileVisitResult accept) {
        return accept == FileVisitResult.CONTINUE ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
    }

    /**
     * Provide a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        return "NOT (" + filter.toString() + ")";
    }

}
