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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.file.PathVisitor;
import org.apache.commons.io.function.IOSupplier;

/**
 * Abstracts the implementation of the {@link FileFilter} (IO), {@link FilenameFilter} (IO), {@link PathFilter} (NIO)
 * interfaces via our own {@link IOFileFilter} interface.
 * <p>
 * Note that a subclass MUST override one of the {@code accept} methods, otherwise that subclass will infinitely loop.
 * </p>
 *
 * @since 1.0
 */
public abstract class AbstractFileFilter implements IOFileFilter, PathVisitor {

    static FileVisitResult toDefaultFileVisitResult(final boolean accept) {
        return accept ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
    }

    /**
     * What to do when this filter accepts.
     */
    private final FileVisitResult onAccept;

    /**
     * What to do when this filter rejects.
     */
    private final FileVisitResult onReject;

    /**
     * Constructs a new instance.
     */
    public AbstractFileFilter() {
        this(FileVisitResult.CONTINUE, FileVisitResult.TERMINATE);
    }

    /**
     * Constructs a new instance.
     *
     * @param onAccept What to do on acceptance.
     * @param onReject What to do on rejection.
     * @since 2.12.0.
     */
    protected AbstractFileFilter(final FileVisitResult onAccept, final FileVisitResult onReject) {
        this.onAccept = onAccept;
        this.onReject = onReject;
    }

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     * @param file the File to check
     * @return true if this file matches the test
     */
    @Override
    public boolean accept(final File file) {
        Objects.requireNonNull(file, "file");
        return accept(file.getParentFile(), file.getName());
    }

    /**
     * Checks to see if the File should be accepted by this filter.
     *
     * @param dir the directory File to check
     * @param name the file name within the directory to check
     * @return true if this file matches the test
     */
    @Override
    public boolean accept(final File dir, final String name) {
        Objects.requireNonNull(name, "name");
        return accept(new File(dir, name));
    }

    void append(final List<?> list, final StringBuilder buffer) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(list.get(i));
        }
    }

    void append(final Object[] array, final StringBuilder buffer) {
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(array[i]);
        }
    }

    FileVisitResult get(final IOSupplier<FileVisitResult> supplier) {
        try {
            return supplier.get();
        } catch (final IOException e) {
            return handle(e);
        }
    }

    /**
     * Handles exceptions caught while accepting.
     *
     * @param t the caught Throwable.
     * @return the given Throwable.
     * @since 2.9.0
     */
    protected FileVisitResult handle(final Throwable t) {
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
        return accept(dir, attributes);
    }

    /**
     * Converts a boolean into a FileVisitResult.
     *
     * @param accept accepted or rejected.
     * @return a FileVisitResult.
     */
    FileVisitResult toFileVisitResult(final boolean accept) {
        return accept ? onAccept : onReject;
    }

    /**
     * Provides a String representation of this file filter.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        return accept(file, attributes);
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

}
