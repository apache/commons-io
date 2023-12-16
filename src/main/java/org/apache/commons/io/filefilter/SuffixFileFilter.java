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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.file.PathUtils;

/**
 * Filters files based on the suffix (what the file name ends with).
 * This is used in retrieving all the files of a particular type.
 * <p>
 * For example, to retrieve and print all {@code *.java} files
 * in the current directory:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = FileUtils.current();
 * String[] files = dir.list(new SuffixFileFilter(".java"));
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = PathUtils.current();
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new SuffixFileFilter(".java"));
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
 * @since 1.0
 * @see FileFilterUtils#suffixFileFilter(String)
 * @see FileFilterUtils#suffixFileFilter(String, IOCase)
 */
public class SuffixFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -3389157631240246157L;

    /** The file name suffixes to search for */
    private final String[] suffixes;

    /** Whether the comparison is case-sensitive. */
    private final IOCase ioCase;

    /**
     * Constructs a new Suffix file filter for a list of suffixes.
     *
     * @param suffixes  the suffixes to allow, must not be null
     * @throws IllegalArgumentException if the suffix list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public SuffixFileFilter(final List<String> suffixes) {
        this(suffixes, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for a list of suffixes
     * specifying case-sensitivity.
     *
     * @param suffixes  the suffixes to allow, must not be null
     * @param ioCase  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the suffix list is null
     * @throws ClassCastException if the list does not contain Strings
     * @since 1.4
     */
    public SuffixFileFilter(final List<String> suffixes, final IOCase ioCase) {
        Objects.requireNonNull(suffixes, "suffixes");
        this.suffixes = suffixes.toArray(EMPTY_STRING_ARRAY);
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for a single extension.
     *
     * @param suffix  the suffix to allow, must not be null
     * @throws IllegalArgumentException if the suffix is null
     */
    public SuffixFileFilter(final String suffix) {
        this(suffix, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for an array of suffixes.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     *
     * @param suffixes  the suffixes to allow, must not be null
     * @throws NullPointerException if the suffix array is null
     */
    public SuffixFileFilter(final String... suffixes) {
        this(suffixes, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for a single extension
     * specifying case-sensitivity.
     *
     * @param suffix  the suffix to allow, must not be null
     * @param ioCase  how to handle case sensitivity, null means case-sensitive
     * @throws NullPointerException if the suffix is null
     * @since 1.4
     */
    public SuffixFileFilter(final String suffix, final IOCase ioCase) {
        Objects.requireNonNull(suffix, "suffix");
        this.suffixes = new String[] {suffix};
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for an array of suffixes
     * specifying case-sensitivity.
     *
     * @param suffixes  the suffixes to allow, must not be null
     * @param ioCase  how to handle case sensitivity, null means case-sensitive
     * @throws NullPointerException if the suffix array is null
     * @since 1.4
     */
    public SuffixFileFilter(final String[] suffixes, final IOCase ioCase) {
        Objects.requireNonNull(suffixes, "suffixes");
        this.suffixes = suffixes.clone();
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    /**
     * Checks to see if the file name ends with the suffix.
     *
     * @param file  the File to check
     * @return true if the file name ends with one of our suffixes
     */
    @Override
    public boolean accept(final File file) {
        return accept(file.getName());
    }

    /**
     * Checks to see if the file name ends with the suffix.
     *
     * @param file  the File directory
     * @param name  the file name
     * @return true if the file name ends with one of our suffixes
     */
    @Override
    public boolean accept(final File file, final String name) {
        return accept(name);
    }

    /**
     * Checks to see if the file name ends with the suffix.
     * @param path  the File to check
     *
     * @return true if the file name ends with one of our suffixes
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return toFileVisitResult(accept(PathUtils.getFileNameString(path)));
    }

    private boolean accept(final String name) {
        return Stream.of(suffixes).anyMatch(suffix -> ioCase.checkEndsWith(name, suffix));
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
        append(suffixes, buffer);
        buffer.append(")");
        return buffer.toString();
    }

}
