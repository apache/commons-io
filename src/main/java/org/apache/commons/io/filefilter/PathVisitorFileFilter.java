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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.file.NoopPathVisitor;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.PathVisitor;

/**
 * A file filter backed by a path visitor.
 *
 * @since 2.9.0
 */
public class PathVisitorFileFilter extends AbstractFileFilter {

    private final PathVisitor pathVisitor;

    /**
     * Constructs a new instance that will forward calls to the given visitor.
     *
     * @param pathVisitor visit me.
     */
    public PathVisitorFileFilter(final PathVisitor pathVisitor) {
        this.pathVisitor = pathVisitor == null ? NoopPathVisitor.INSTANCE : pathVisitor;
    }

    @Override
    public boolean accept(final File file) {
        try {
            final Path path = file.toPath();
            return visitFile(path, file.exists() ? PathUtils.readBasicFileAttributes(path) : null) == FileVisitResult.CONTINUE;
        } catch (final IOException e) {
            return handle(e) == FileVisitResult.CONTINUE;
        }
    }

    @Override
    public boolean accept(final File dir, final String name) {
        try {
            final Path path = dir.toPath().resolve(name);
            return accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
        } catch (final IOException e) {
            return handle(e) == FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return get(() -> Files.isDirectory(path) ? pathVisitor.postVisitDirectory(path, null) : visitFile(path, attributes));
    }

    @Override
    public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
        return pathVisitor.visitFile(path, attributes);
    }

}
