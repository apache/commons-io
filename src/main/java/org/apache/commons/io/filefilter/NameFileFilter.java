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
 * Filters file names for a certain name.
 * <p>
 * For example, to print all files and directories in the
 * current directory whose name is <code>Test</code>:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(new NameFileFilter("Test"));
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = Paths.get("");
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new NameFileFilter("Test"));
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
 * @see FileFilterUtils#nameFileFilter(String)
 * @see FileFilterUtils#nameFileFilter(String, IOCase)
 */
public class NameFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 176844364689077340L;

    /** The file names to search for */
    private final String[] names;

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Constructs a new case-sensitive name file filter for a list of names.
     *
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the name list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public NameFileFilter(final List<String> names) {
        this(names, null);
    }

    /**
     * Constructs a new name file filter for a list of names specifying case-sensitivity.
     *
     * @param names  the names to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the name list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public NameFileFilter(final List<String> names, final IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The list of names must not be null");
        }
        this.names = names.toArray(EMPTY_STRING_ARRAY);
        this.caseSensitivity = toIOCase(caseSensitivity);
    }

    /**
     * Constructs a new case-sensitive name file filter for a single name.
     *
     * @param name  the name to allow, must not be null
     * @throws IllegalArgumentException if the name is null
     */
    public NameFileFilter(final String name) {
        this(name, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new case-sensitive name file filter for an array of names.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     * </p>
     *
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the names array is null
     */
    public NameFileFilter(final String... names) {
        this(names, IOCase.SENSITIVE);
    }

    /**
     * Construct a new name file filter specifying case-sensitivity.
     *
     * @param name  the name to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the name is null
     */
    public NameFileFilter(final String name, final IOCase caseSensitivity) {
        if (name == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.names = new String[] {name};
        this.caseSensitivity = toIOCase(caseSensitivity);
    }

    /**
     * Constructs a new name file filter for an array of names specifying case-sensitivity.
     *
     * @param names  the names to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the names array is null
     */
    public NameFileFilter(final String[] names, final IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The array of names must not be null");
        }
        this.names = new String[names.length];
        System.arraycopy(names, 0, this.names, 0, names.length);
        this.caseSensitivity = toIOCase(caseSensitivity);
    }

    /**
     * Checks to see if the file name matches.
     *
     * @param file  the File to check
     * @return true if the file name matches
     */
    @Override
    public boolean accept(final File file) {
        return acceptBaseName(file.getName());
    }

    /**
     * Checks to see if the file name matches.
     *
     * @param dir  the File directory (ignored)
     * @param name  the file name
     * @return true if the file name matches
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return acceptBaseName(name);
    }

    /**
     * Checks to see if the file name matches.
     * @param file  the File to check
     *
     * @return true if the file name matches
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        return toFileVisitResult(acceptBaseName(Objects.toString(file.getFileName(), null)), file);
    }

    private boolean acceptBaseName(final String baseName) {
        for (final String testName : names) {
            if (caseSensitivity.checkEquals(baseName, testName)) {
                return true;
            }
        }
        return false;
    }

    private IOCase toIOCase(final IOCase caseSensitivity) {
        return caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
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
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(names[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
