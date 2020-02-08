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
import java.util.Objects;

import org.apache.commons.io.file.Counters.PathCounters;

/**
 * Counts files, directories, and sizes, as a visit proceeds.
 *
 * @since 2.7
 */
public class CountingPathVisitor extends SimplePathVisitor {

    static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Creates a new instance configured with a BigInteger {@link PathCounters}.
     *
     * @return a new instance configured with a BigInteger {@link PathCounters}.
     */
    public static CountingPathVisitor withBigIntegerCounters() {
        return new CountingPathVisitor(Counters.bigIntegerPathCounters());
    }

    /**
     * Creates a new instance configured with a long {@link PathCounters}.
     *
     * @return a new instance configured with a long {@link PathCounters}.
     */
    public static CountingPathVisitor withLongCounters() {
        return new CountingPathVisitor(Counters.longPathCounters());
    }

    private final PathCounters pathCounters;

    /**
     * Constructs a new instance.
     *
     * @param pathCounter How to count path visits.
     */
    public CountingPathVisitor(final PathCounters pathCounter) {
        super();
        this.pathCounters = Objects.requireNonNull(pathCounter, "pathCounter");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CountingPathVisitor)) {
            return false;
        }
        CountingPathVisitor other = (CountingPathVisitor) obj;
        return Objects.equals(pathCounters, other.pathCounters);
    }

    /**
     * Gets the visitation counts.
     *
     * @return the visitation counts.
     */
    public PathCounters getPathCounters() {
        return pathCounters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathCounters);
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        pathCounters.getDirectoryCounter().increment();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public String toString() {
        return pathCounters.toString();
    }

    /**
     * Updates the counters for visiting the given file.
     *
     * @param file the visited file.
     * @param attributes the visited file attributes.
     */
    protected void updateFileCounters(final Path file, final BasicFileAttributes attributes) {
        pathCounters.getFileCounter().increment();
        pathCounters.getByteCounter().add(attributes.size());
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
        if (Files.exists(file)) {
            updateFileCounters(file, attributes);
        }
        return FileVisitResult.CONTINUE;
    }

}
