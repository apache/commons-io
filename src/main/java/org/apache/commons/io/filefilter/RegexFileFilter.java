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
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;

/**
 * Filters files using supplied regular expression(s).
 * <p>
 * See java.util.regex.Pattern for regex matching rules
 * </p>
 *
 * <p>
 * e.g.
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new RegexFileFilter("^.*[tT]est(-\\d+)?\\.java$");
 * File[] files = dir.listFiles(fileFilter);
 * for (int i = 0; i &lt; files.length; i++) {
 *   System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.4
 */
public class RegexFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 4269646126155225062L;
    /** The regular expression pattern that will be used to match filenames */
    private final Pattern pattern;

    /**
     * Construct a new regular expression filter.
     *
     * @param pattern regular string expression to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }

        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Construct a new regular expression filter with the specified flags case sensitivity.
     *
     * @param pattern regular string expression to match
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern, final IOCase caseSensitivity) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        int flags = 0;
        if (caseSensitivity != null && !caseSensitivity.isCaseSensitive()) {
            flags = Pattern.CASE_INSENSITIVE;
        }
        this.pattern = Pattern.compile(pattern, flags);
    }

    /**
     * Construct a new regular expression filter with the specified flags.
     *
     * @param pattern regular string expression to match
     * @param flags pattern flags - e.g. {@link Pattern#CASE_INSENSITIVE}
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final String pattern, final int flags) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = Pattern.compile(pattern, flags);
    }

    /**
     * Construct a new regular expression filter for a compiled regular expression
     *
     * @param pattern regular expression to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFileFilter(final Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }

        this.pattern = pattern;
    }

    /**
     * Checks to see if the filename matches one of the regular expressions.
     *
     * @param dir   the file directory (ignored)
     * @param name  the filename
     * @return true if the filename matches one of the regular expressions
     */
    @Override
    public boolean accept(final File dir, final String name) {
        return pattern.matcher(name).matches();
    }

}
