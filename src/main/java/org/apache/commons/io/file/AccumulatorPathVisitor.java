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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Accumulates normalized paths during visitation.
 * <p>
 * Use with care on large file trees as each visited Path element is remembered.
 * </p>
 *
 * @since 2.7
 */
public class AccumulatorPathVisitor extends CountingPathVisitor {

    /**
     * Creates a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return a new instance configured with a BigInteger {@link PathCounters}.
     */
    public static AccumulatorPathVisitor withBigIntegerCounters() {
        return new AccumulatorPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Creates a new instance configured with a long {@link PathCounters}.
     *
     * @return a new instance configured with a long {@link PathCounters}.
     */
    public static AccumulatorPathVisitor withLongCounters() {
        return new AccumulatorPathVisitor(Counters.longPathCounters());
    }

    private final List<Path> dirList = new ArrayList<>();

    private final List<Path> fileList = new ArrayList<>();

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     */
    public AccumulatorPathVisitor(PathCounters pathCounter) {
        super(pathCounter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AccumulatorPathVisitor)) {
            return false;
        }
        AccumulatorPathVisitor other = (AccumulatorPathVisitor) obj;
        return Objects.equals(dirList, other.dirList) && Objects.equals(fileList, other.fileList);
    }

    /**
     * Gets the list of visited directories.
     * 
     * @return the list of visited directories.
     */
    public List<Path> getDirList() {
        return dirList;
    }

    /**
     * Gets the list of visited files.
     * 
     * @return the list of visited files.
     */
    public List<Path> getFileList() {
        return fileList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dirList, fileList);
        return result;
    }

    /**
     * Relativizes each directory path with {@link Path#relativize(Path)} against the given {@code parent}, optionally
     * sorting the result.
     * 
     * @param parent A parent path
     * @param sort Whether to sort
     * @param comparator How to sort, null uses default sorting.
     * @return A new list
     */
    public List<Path> relativizeDirectories(final Path parent, boolean sort, Comparator<? super Path> comparator) {
        return PathUtils.relativize(getDirList(), parent, sort, comparator);
    }

    /**
     * Relativizes each file path with {@link Path#relativize(Path)} against the given {@code parent}, optionally
     * sorting the result.
     * 
     * @param parent A parent path
     * @param sort Whether to sort
     * @param comparator How to sort, null uses default sorting.
     * @return A new list
     */
    public List<Path> relativizeFiles(final Path parent, boolean sort, Comparator<? super Path> comparator) {
        return PathUtils.relativize(getFileList(), parent, sort, comparator);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        ((Files.isDirectory(file)) ? dirList : fileList).add(file.normalize());
        return super.visitFile(file, attributes);
    }

}
