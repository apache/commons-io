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

import org.apache.commons.io.FilenameUtils;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files, but not directories, based on one or more wildcards
 * and using case-sensitive comparison.
 * </p>
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a
 * single or multiple wildcard characters.
 * This is the same as often found on Dos/Unix command lines.
 * The extension check is case-sensitive.
 * See {@link FilenameUtils#wildcardMatch(String, String)} for more information.
 * </p>
 * <p>
 * For example:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new WildcardFilter("*test*.java~*~");
 * File[] files = dir.listFiles(fileFilter);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = Paths.get("");
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
     * Construct a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards  the list of wildcards to match
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFilter(final List<String> wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard list must not be null");
        }
        this.wildcards = wildcards.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Construct a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard  the wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public WildcardFilter(final String wildcard) {
        if (wildcard == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.wildcards = new String[] { wildcard };
    }

    /**
     * Construct a new case-sensitive wildcard filter for an array of wildcards.
     *
     * @param wildcards  the array of wildcards to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public WildcardFilter(final String... wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard array must not be null");
        }
        this.wildcards = new String[wildcards.length];
        System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
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

        for (final String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(file.getName(), wildcard)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the file name matches one of the wildcards.
     * @param file the file to check
     *
     * @return true if the file name matches one of the wildcards
     * @since 2.9.0
     */
    @Override
    public FileVisitResult accept(final Path file, final BasicFileAttributes attributes) {
        if (Files.isDirectory(file)) {
            return FileVisitResult.TERMINATE;
        }

        for (final String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(Objects.toString(file.getFileName(), null), wildcard)) {
                return FileVisitResult.CONTINUE;
            }
        }

        return FileVisitResult.TERMINATE;
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

        for (final String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(name, wildcard)) {
                return true;
            }
        }

        return false;
    }

}
