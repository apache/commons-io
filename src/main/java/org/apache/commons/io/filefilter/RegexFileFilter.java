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
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;

/**
 * Filters files using supplied regular expression(s).
 * <p>
 * See java.util.regex.Pattern for regex matching rules.
 * </p>
 * <h2>Using Classic IO</h2>
 * <p>
 * e.g.
 *
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$");
 * File[] files = dir.listFiles(fileFilter);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 *
 * <pre>
 * final Path dir = Paths.get("");
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$"));
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
 * @since 1.4
 */
public class RegexFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 4269646126155225062L;

    /**
     * Compiles the given pattern source.
     *
     * @param pattern the source pattern.
     * @param flags the compilation flags.
     * @return a new Pattern.
     */
    private static Pattern compile(final String pattern, final int flags) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        return Pattern.compile(pattern, flags);
    }

    /**
     * Converts IOCase to Pattern compilation flags.
     *
     * @param caseSensitivity case-sensitivity.
     * @return Pattern compilation flags.
     */
    private static int toFlags(final IOCase caseSensitivity) {
        return IOCase.isCaseSensitive(caseSensitivity) ? Pattern.CASE_INSENSITIVE : 0;
    }

    /** The regular expression pattern that will be used to match file names. */
    private final Pattern pattern;
    
    /** How convert a path to a string. */
    private final Function<Path, String> pathToString;

    /**
     * Constructs a new regular expression filter for a compiled regular expression
     *
     * @param pattern regular expression to match.
     * @throws IllegalArgumentException if the pattern is null.
     */
    public RegexFileFilter(final Pattern pattern) {
        this(pattern, p -> p.getFileName().toString());
    }

    /**
     * Constructs a new regular expression filter for a compiled regular expression
     *
     * @param pattern regular expression to match.
     * @param pathToString How convert a path to a string.
     * @throws IllegalArgumentException if the pattern is null.
     * @since 2.10.0
     */
    public RegexFileFilter(final Pattern pattern, final Function<Path, String> pathToString) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = pattern;
        this.pathToString = pathToString;
    }

    /**
     * Constructs a new regular expression filter.
     *
     * @param pattern regular string expression to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern) {
        this(pattern, 0);
    }

    /**
     * Constructs a new regular expression filter with the specified flags.
     *
     * @param pattern regular string expression to match
     * @param flags pattern flags - e.g. {@link Pattern#CASE_INSENSITIVE}
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern, final int flags) {
        this(compile(pattern, flags));
    }

    /**
     * Constructs a new regular expression filter with the specified flags case sensitivity.
     *
     * @param pattern regular string expression to match
     * @param caseSensitivity how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern, final IOCase caseSensitivity) {
        this(compile(pattern, toFlags(caseSensitivity)));
    }

    /**
     * Checks to see if the file name matches one of the regular expressions.
     *
     * @param dir the file directory (ignored)
     * @param name the file name
     * @return true if the file name matches one of the regular expressions
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return pattern.matcher(name).matches();
    }

    /**
     * Checks to see if the file name matches one of the regular expressions.
     *
     * @param path the path
     * @param attributes the path attributes
     * @return true if the file name matches one of the regular expressions
     */
    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return toFileVisitResult(pattern.matcher(pathToString.apply(path)).matches(), path);
    }

    /**
     * Returns a debug string.
     *
     * @since 2.10.0
     */
    @Override
    public String toString() {
        return "RegexFileFilter [pattern=" + pattern + "]";
    }

}
