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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

/**
 * This filter accepts files or directories that are empty.
 * <p>
 * If the {@link File} is a directory it checks that it contains no files.
 * </p>
 * <p>
 * Example, showing how to print out a list of the current directory's empty files/directories:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = FileUtils.current();
 * String[] files = dir.list(EmptyFileFilter.EMPTY);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <p>
 * Example, showing how to print out a list of the current directory's non-empty files/directories:
 * </p>
 *
 * <pre>
 * File dir = FileUtils.current();
 * String[] files = dir.list(EmptyFileFilter.NOT_EMPTY);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = PathUtils.current();
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(EmptyFileFilter.EMPTY);
 * //
 * // Walk one dir
 * Files.<b>walkFileTree</b>(dir, Collections.emptySet(), 1, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getFileList());
 * //
 * visitor.getPathCounters().reset();
 * //
 * // Walk dir tree
 * Files.<b>walkFileTree</b>(dir, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getDirList());
 * System.out.println(visitor.getFileList());
 * </pre>
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 1.3
 */
public class EmptyFileFilter extends AbstractFileFilter implements Serializable {

    /** Singleton instance of <em>empty</em> filter */
    public static final IOFileFilter EMPTY = new EmptyFileFilter();

    /** Singleton instance of <em>not-empty</em> filter */
    public static final IOFileFilter NOT_EMPTY = EMPTY.negate();

    private static final long serialVersionUID = 3631422087512832211L;

    /**
     * Restrictive constructor.
     */
    protected EmptyFileFilter() {
    }

    /**
     * Checks to see if the file is empty.
     *
     * @param file the file or directory to check
     * @return {@code true} if the file or directory is <em>empty</em>, otherwise {@code false}.
     */
    @Override
    public boolean accept(final File file) {
        if (file == null) {
            return true;
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            return IOUtils.length(files) == 0;
        }
        return file.length() == 0;
    }

    /**
     * Checks to see if the file is empty.
     * @param file the file or directory to check
     *
     * @return {@code true} if the file or directory is <em>empty</em>, otherwise {@code false}.
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        if (file == null) {
            return toFileVisitResult(true);
        }
        return get(() -> {
            if (Files.isDirectory(file)) {
                try (Stream<Path> stream = Files.list(file)) {
                    return toFileVisitResult(!stream.findFirst().isPresent());
                }
            }
            return toFileVisitResult(Files.size(file) == 0);
        });
    }

}
