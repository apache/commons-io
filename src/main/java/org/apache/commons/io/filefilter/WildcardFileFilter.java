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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.build.AbstractSupplier;
import org.apache.commons.io.file.PathUtils;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files and directories based on one or more wildcards. Testing is case-sensitive by default, but this can be configured.
 * </p>
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a single or multiple wildcard characters. This is the same as often found on DOS/Unix
 * command lines. The check is case-sensitive by default. See {@link FilenameUtils#wildcardMatchOnSystem(String,String)} for more information.
 * </p>
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * For example:
 * </p>
 * <h2>Using Classic IO</h2>
 *
 * <pre>
 * File dir = FileUtils.current();
 * FileFilter fileFilter = WildcardFileFilter.builder().setWildcards("*test*.java~*~").get();
 * File[] files = dir.listFiles(fileFilter);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 *
 * <pre>
 * final Path dir = PathUtils.current();
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(
 *     WildcardFileFilter.builder().setWildcards("*test*.java~*~").get());
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
public class WildcardFileFilter extends AbstractFileFilter implements Serializable {

    /**
     * Builds a new {@link WildcardFileFilter} instance.
     *
     * @since 2.12.0
     */
    public static class Builder extends AbstractSupplier<WildcardFileFilter, Builder> {

        /** The wildcards that will be used to match file names. */
        private String[] wildcards;

        /** Whether the comparison is case-sensitive. */
        private IOCase ioCase = IOCase.SENSITIVE;

        @Override
        public WildcardFileFilter get() {
            return new WildcardFileFilter(ioCase, wildcards);
        }

        /**
         * Sets how to handle case sensitivity, null means case-sensitive.
         *
         * @param ioCase how to handle case sensitivity, null means case-sensitive.
         * @return {@code this} instance.
         */
        public Builder setIoCase(final IOCase ioCase) {
            this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
            return this;
        }

        /**
         * Sets the list of wildcards to match, not null.
         *
         * @param wildcards the list of wildcards to match, not null.
         * @return {@code this} instance.
         */
        public Builder setWildcards(final List<String> wildcards) {
            setWildcards(requireWildcards(wildcards).toArray(EMPTY_STRING_ARRAY));
            return this;
        }

        /**
         * Sets the wildcards to match, not null.
         *
         * @param wildcards the wildcards to match, not null.
         * @return {@code this} instance.
         */
        public Builder setWildcards(final String... wildcards) {
            this.wildcards = requireWildcards(wildcards);
            return this;
        }

    }

    private static final long serialVersionUID = -7426486598995782105L;

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private static <T> T requireWildcards(final T wildcards) {
        return Objects.requireNonNull(wildcards, "wildcards");
    }

    /** The wildcards that will be used to match file names. */
    private final String[] wildcards;

    /** Whether the comparison is case-sensitive. */
    private final IOCase ioCase;

    /**
     * Constructs a new wildcard filter for an array of wildcards specifying case-sensitivity.
     *
     * @param wildcards the array of wildcards to match, not null
     * @param ioCase    how to handle case sensitivity, null means case-sensitive
     * @throws NullPointerException if the pattern array is null
     */
    private WildcardFileFilter(final IOCase ioCase, final String... wildcards) {
        this.wildcards = requireWildcards(wildcards).clone();
        this.ioCase = IOCase.value(ioCase, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards the list of wildcards to match, not null
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException       if the list does not contain Strings
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final List<String> wildcards) {
        this(wildcards, IOCase.SENSITIVE);
    }

    /**
     * Constructs a new wildcard filter for a list of wildcards specifying case-sensitivity.
     *
     * @param wildcards the list of wildcards to match, not null
     * @param ioCase    how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException       if the list does not contain Strings
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final List<String> wildcards, final IOCase ioCase) {
        this(ioCase, requireWildcards(wildcards).toArray(EMPTY_STRING_ARRAY));
    }

    /**
     * Constructs a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard the wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final String wildcard) {
        this(IOCase.SENSITIVE, requireWildcards(wildcard));
    }

    /**
     * Constructs a new case-sensitive wildcard filter for an array of wildcards.
     *
     * @param wildcards the array of wildcards to match
     * @throws NullPointerException if the pattern array is null
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final String... wildcards) {
        this(IOCase.SENSITIVE, wildcards);
    }

    /**
     * Constructs a new wildcard filter for a single wildcard specifying case-sensitivity.
     *
     * @param wildcard the wildcard to match, not null
     * @param ioCase   how to handle case sensitivity, null means case-sensitive
     * @throws NullPointerException if the pattern is null
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final String wildcard, final IOCase ioCase) {
        this(ioCase, wildcard);
    }

    /**
     * Constructs a new wildcard filter for an array of wildcards specifying case-sensitivity.
     *
     * @param wildcards the array of wildcards to match, not null
     * @param ioCase    how to handle case sensitivity, null means case-sensitive
     * @throws NullPointerException if the pattern array is null
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public WildcardFileFilter(final String[] wildcards, final IOCase ioCase) {
        this(ioCase, wildcards);
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param file the file to check
     * @return true if the file name matches one of the wildcards
     */
    @Override
    public boolean accept(final File file) {
        return accept(file.getName());
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param dir  the file directory (ignored)
     * @param name the file name
     * @return true if the file name matches one of the wildcards
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return accept(name);
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param path the file to check
     *
     * @return true if the file name matches one of the wildcards.
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return toFileVisitResult(accept(PathUtils.getFileNameString(path)));
    }

    private boolean accept(final String name) {
        return Stream.of(wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(name, wildcard, ioCase));
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
        append(wildcards, buffer);
        buffer.append(")");
        return buffer.toString();
    }
}
