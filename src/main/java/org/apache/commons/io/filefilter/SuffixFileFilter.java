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

import org.apache.commons.io.IOCase;

/**
 * Filters files based on the suffix (what the file name ends with).
 * This is used in retrieving all the files of a particular type.
 * <p>
 * For example, to retrieve and print all <code>*.java</code> files
 * in the current directory:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(new SuffixFileFilter(".java"));
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = Paths.get("");
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
 *
 * @since 1.0
 * @see FileFilterUtils#suffixFileFilter(String)
 * @see FileFilterUtils#suffixFileFilter(String, IOCase)
 */
public class SuffixFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -3389157631240246157L;

    /** The file name suffixes to search for */
    private final String[] suffixes;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

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
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the suffix list is null
     * @throws ClassCastException if the list does not contain Strings
     * @since 1.4
     */
    public SuffixFileFilter(final List<String> suffixes, final IOCase caseSensitivity) {
        if (suffixes == null) {
            throw new IllegalArgumentException("The list of suffixes must not be null");
        }
        this.suffixes = suffixes.toArray(EMPTY_STRING_ARRAY);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
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
     * @throws IllegalArgumentException if the suffix array is null
     */
    public SuffixFileFilter(final String... suffixes) {
        this(suffixes, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new Suffix file filter for a single extension
     * specifying case-sensitivity.
     *
     * @param suffix  the suffix to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the suffix is null
     * @since 1.4
     */
    public SuffixFileFilter(final String suffix, final IOCase caseSensitivity) {
        if (suffix == null) {
            throw new IllegalArgumentException("The suffix must not be null");
        }
        this.suffixes = new String[] {suffix};
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new Suffix file filter for an array of suffixes
     * specifying case-sensitivity.
     *
     * @param suffixes  the suffixes to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the suffix array is null
     * @since 1.4
     */
    public SuffixFileFilter(final String[] suffixes, final IOCase caseSensitivity) {
        if (suffixes == null) {
            throw new IllegalArgumentException("The array of suffixes must not be null");
        }
        this.suffixes = new String[suffixes.length];
        System.arraycopy(suffixes, 0, this.suffixes, 0, suffixes.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
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
     * @param file  the File to check
     *
     * @return true if the file name ends with one of our suffixes
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return toFileVisitResult(accept(Objects.toString(file.getFileName(), null)), file);
    }

    private boolean accept(final String name) {
        for (final String suffix : this.suffixes) {
            if (caseSensitivity.checkEndsWith(name, suffix)) {
                return true;
            }
        }
        return false;
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
        if (suffixes != null) {
            for (int i = 0; i < suffixes.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(suffixes[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
