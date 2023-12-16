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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files, but not directories, based on one or more wildcards
 * and using case-sensitive comparison.
 * </p>
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a
 * single or multiple wildcard characters.
 * This is the same as often found on DOS/Unix command lines.
 * The extension check is case-sensitive.
 * See {@link FilenameUtils#wildcardMatch(String, String)} for more information.
 * </p>
 * <p>
 * For example:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = FileUtils.current();
 * FileFilter fileFilter = new WildcardFilter("*test*.java~*~");
 * File[] files = dir.listFiles(fileFilter);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = PathUtils.current();
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new WildcardFilter("*test*.java~*~"));
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
 * @since 1.1
 * @deprecated Use WildcardFileFilter. Deprecated as this class performs directory
 * filtering which it shouldn't do, but that can't be removed due to compatibility.
 */
@Deprecated
public class WildcardFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -5037645902506953517L;

    /** The wildcards that will be used to match file names. */
    private final String[] wildcards;

    /**
     * Constructs a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards  the list of wildcards to match
     * @throws NullPointerException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFilter(final List<String> wildcards) {
        Objects.requireNonNull(wildcards, "wildcards");
        this.wildcards = wildcards.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Constructs a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard  the wildcard to match
     * @throws NullPointerException if the pattern is null
     */
    public WildcardFilter(final String wildcard) {
        Objects.requireNonNull(wildcard, "wildcard");
        this.wildcards = new String[] { wildcard };
    }

    /**
     * Constructs a new case-sensitive wildcard filter for an array of wildcards.
     *
     * @param wildcards  the array of wildcards to match
     * @throws NullPointerException if the pattern array is null
     */
    public WildcardFilter(final String... wildcards) {
        Objects.requireNonNull(wildcards, "wildcards");
        this.wildcards = wildcards.clone();
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param file the file to check
     * @return true if the file name matches one of the wildcards
     */
    @Override
    public boolean accept(final File file) {
        if (file.isDirectory()) {
            return false;
        }
        return Stream.of(wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(file.getName(), wildcard));
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     *
     * @param dir  the file directory
     * @param name  the file name
     * @return true if the file name matches one of the wildcards
     */
    @Override
    public boolean accept(final File dir, final String name) {
        if (dir != null && new File(dir, name).isDirectory()) {
            return false;
        }
        return Stream.of(wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(name, wildcard));
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     * @param path the file to check
     *
     * @return true if the file name matches one of the wildcards
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        if (Files.isDirectory(path)) {
            return FileVisitResult.TERMINATE;
        }
        return toDefaultFileVisitResult(
                Stream.of(wildcards).anyMatch(wildcard -> FilenameUtils.wildcardMatch(PathUtils.getFileNameString(path), wildcard)));

    }

}
