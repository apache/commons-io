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
import java.io.FilenameFilter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.file.PathFilter;

/**
 * An interface which brings the {@link FileFilter}, {@link FilenameFilter}, {@link PathFilter}, and {@link PathMatcher} interfaces together.
 *
 * @since 1.0
 */
public interface IOFileFilter extends FileFilter, FilenameFilter, PathFilter, PathMatcher {

    /**
     * An empty String array.
     */
    String[] EMPTY_STRING_ARRAY = {};

    /**
     * Tests if a File should be accepted by this filter.
     * <p>
     * Defined in {@link FileFilter}.
     * </p>
     *
     * @param file the File to check.
     * @return true if this file matches the test.
     */
    @Override
    boolean accept(File file);

    /**
     * Tests if a File should be accepted by this filter.
     * <p>
     * Defined in {@link FilenameFilter}.
     * </p>
     *
     * @param dir  the directory File to check.
     * @param name the file name within the directory to check.
     * @return true if this file matches the test.
     */
    @Override
    boolean accept(File dir, String name);

    /**
     * Checks to see if a Path should be accepted by this filter.
     *
     * @param path the Path to check.
     * @return true if this path matches the test.
     * @since 2.9.0
     */
    @Override
    default FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return AbstractFileFilter.toDefaultFileVisitResult(path != null && accept(path.toFile()));
    }

    /**
     * Constructs a new "and" filter with this filter.
     *
     * @param fileFilter the filter to "and".
     * @return a new filter.
     * @since 2.9.0
     */
    default IOFileFilter and(final IOFileFilter fileFilter) {
        return new AndFileFilter(this, fileFilter);
    }

    /**
     * Tests if a Path should be accepted by this filter.
     *
     * @param path the Path to check.
     * @return true if this path matches the test.
     * @since 2.14.0
     */
    @Override
    default boolean matches(final Path path) {
        return accept(path, null) != FileVisitResult.TERMINATE;
    }

    /**
     * Constructs a new "not" filter with this filter.
     *
     * @return a new filter.
     * @since 2.9.0
     */
    default IOFileFilter negate() {
        return new NotFileFilter(this);
    }

    /**
     * Constructs a new "or" filter with this filter.
     *
     * @param fileFilter the filter to "or".
     * @return a new filter.
     * @since 2.9.0
     */
    default IOFileFilter or(final IOFileFilter fileFilter) {
        return new OrFileFilter(this, fileFilter);
    }

}
