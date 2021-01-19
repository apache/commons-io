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
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A {@link java.nio.file.DirectoryStream.Filter DirectoryStream.Filter} that delegates to a {@link PathFilter}.
 * <p>
 * You pass this filter to {@link java.nio.file.Files#newDirectoryStream(Path, DirectoryStream.Filter)
 * Files#newDirectoryStream(Path, DirectoryStream.Filter)}.
 * </p>
 *
 * @since 2.9.0
 */
public class DirectoryStreamFilter implements DirectoryStream.Filter<Path> {

    private final PathFilter pathFilter;

    /**
     * Constructs a new instance for the given path filter.
     *
     * @param pathFilter How to filter paths.
     */
    public DirectoryStreamFilter(final PathFilter pathFilter) {
        // TODO Instead of NPE, we could map null to FalseFileFilter.
        this.pathFilter = Objects.requireNonNull(pathFilter, "pathFilter");
    }

    @Override
    public boolean accept(final Path path) throws IOException {
        return pathFilter.accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
    }

    /**
     * Gets the path filter.
     *
     * @return the path filter.
     */
    public PathFilter getPathFilter() {
        return pathFilter;
    }

}
