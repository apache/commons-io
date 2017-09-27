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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files and directories based on one or more wildcards.
 * Testing is case-sensitive by default, but this can be configured.
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a
 * single or multiple wildcard characters.
 * This is the same as often found on Dos/Unix command lines.
 * The check is case-sensitive by default.
 * See {@link FilenameUtils#wildcardMatchOnSystem} for more information.
 * <p>
 * For example:
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new WildcardFileFilter("*test*.java~*~");
 * File[] files = dir.listFiles(fileFilter);
 * for (int i = 0; i &lt; files.length; i++) {
 *   System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.3
 */
public class WildcardFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -7426486598995782105L;
    /** The wildcards that will be used to match filenames. */
    private final String[] wildcards;
    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Construct a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard  the wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public WildcardFileFilter(final String wildcard) {
        this(wildcard, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for a single wildcard specifying case-sensitivity.
     *
     * @param wildcard  the wildcard to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern is null
     */
    public WildcardFileFilter(final String wildcard, final IOCase caseSensitivity) {
        if (wildcard == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.wildcards = new String[] { wildcard };
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Construct a new case-sensitive wildcard filter for an array of wildcards.
     * <p>
     *
     * @param wildcards  the array of wildcards to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public WildcardFileFilter(final String[] wildcards) {
        this(wildcards, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for an array of wildcards specifying case-sensitivity.
     * <p>
     *
     * @param wildcards  the array of wildcards to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern array is null
     */
    public WildcardFileFilter(final String[] wildcards, final IOCase caseSensitivity) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard array must not be null");
        }
        this.wildcards = new String[wildcards.length];
        System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Construct a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards  the list of wildcards to match, not null
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFileFilter(final List<String> wildcards) {
        this(wildcards, IOCase.SENSITIVE);
    }

    /**
     * Construct a new wildcard filter for a list of wildcards specifying case-sensitivity.
     *
     * @param wildcards  the list of wildcards to match, not null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFileFilter(final List<String> wildcards, final IOCase caseSensitivity) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard list must not be null");
        }
        this.wildcards = wildcards.toArray(new String[wildcards.size()]);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param dir  the file directory (ignored)
     * @param name  the filename
     * @return true if the filename matches one of the wildcards
     */
    @Override
    public boolean accept(final File dir, final String name) {
        for (final String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(name, wildcard, caseSensitivity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param file  the file to check
     * @return true if the filename matches one of the wildcards
     */
    @Override
    public boolean accept(final File file) {
        final String name = file.getName();
        for (final String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(name, wildcard, caseSensitivity)) {
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
        if (wildcards != null) {
            for (int i = 0; i < wildcards.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(wildcards[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
