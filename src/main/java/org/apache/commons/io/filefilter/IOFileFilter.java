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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;

import org.apache.commons.io.file.PathFilter;

/**
 * An interface which brings the FileFilter, FilenameFilter, and PathFilter interfaces together.
 *
 * @since 1.0
 */
public interface IOFileFilter extends FileFilter, FilenameFilter, PathFilter {

    /**
     * An empty String array.
     */
    String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FileFilter}.
     * </p>
     *
     * @param file the File to check
     * @return true if this file matches the test
     */
    @Override
    boolean accept(File file);

    /**
     * Checks to see if the File should be accepted by this filter.
     * <p>
     * Defined in {@link java.io.FilenameFilter}.
     * </p>
     *
     * @param dir the directory File to check
     * @param name the file name within the directory to check
     * @return true if this file matches the test
     */
    @Override
    boolean accept(File dir, String name);

    /**
     * Checks to see if the Path should be accepted by this filter.
     *
     * @param path the Path to check
     * @return true if this path matches the test
     * @throws IOException if an I/O error occurs
     * @since 2.9.0
     */
    @Override
    default FileVisitResult accept(final Path path) throws IOException {
        return AbstractFileFilter.toFileVisitResult(accept(path.toFile()));
    }

    /**
     * Creates a new "and" filter with this filter and the given filters.
     *
     * @param fileFilter the filters to "and".
     * @return a new filter
     * @since 2.9.0
     */
    default AndFileFilter and(final IOFileFilter... fileFilter) {
        return new AndFileFilter(this, fileFilter);
    }

    /**
     * Creates a new "not" filter with this filter.
     *
     * @return a new filter
     * @since 2.9.0
     */
    default NotFileFilter not() {
        return new NotFileFilter(this);
    }

    /**
     * Creates a new "or" filter with this filter and the given filters.
     *
     * @param fileFilter the filters to "or".
     * @return a new filter
     * @since 2.9.0
     */
    default OrFileFilter or(final IOFileFilter... fileFilter) {
        return new OrFileFilter(this, fileFilter);
    }

}
