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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters files using supplied regular expression(s).
 * <p/>
 * See java.util.regex.Pattern for regex matching rules
 * <p/>
 *
 * <p/>
 * e.g.
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new RegexFilter("^.*[tT]est(-\\d+)?\\.java$");
 * File[] files = dir.listFiles(fileFilter);
 * for (int i = 0; i < files.length; i++) {
 *   System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author Oliver Siegmar
 * @version $Revision$
 * @since Commons IO 1.4
 */
public class RegexFilter extends AbstractFileFilter {

    /** The regular expression patterns that will be used to match filenames */
    private Pattern[] patterns = null;

    /**
     * Construct a new regular expression filter for a single regular expression
     *
     * @param pattern regular string expression to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFilter(String pattern) {
        if (pattern == null) {
            throw new java.lang.IllegalArgumentException();
        }

        patterns = new Pattern[] { Pattern.compile(pattern) };
    }

    /**
     * Construct a new regular expression filter for a single regular expression
     *
     * @param pattern regular expression to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public RegexFilter(Pattern pattern) {
        if (pattern == null) {
            throw new java.lang.IllegalArgumentException();
        }

        patterns = new Pattern[] { pattern };
    }

    /**
     * Construct a new regular expression filter for an array of regular expressions
     *
     * @param patterns regular expression strings to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public RegexFilter(String[] patterns) {
        if (patterns == null) {
            throw new java.lang.IllegalArgumentException();
        }

        this.patterns = new Pattern[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            this.patterns[i] = Pattern.compile(patterns[i]);
        }
    }

    /**
     * Construct a new regular expression filter for an array of regular expressions
     *
     * @param patterns regular expression to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public RegexFilter(Pattern[] patterns) {
        if (patterns == null) {
            throw new java.lang.IllegalArgumentException();
        }

        this.patterns = patterns;
    }

    /**
     * Construct a new regular expression filter for a list of regular expressions
     *
     * @param patterns list of regular expressions (either list of type
     *                 <code>java.lang.String</code> or of type
     *                 <code>java.util.regex.Pattern</code>) to match
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public RegexFilter(List patterns) {
        if (patterns == null) {
            throw new java.lang.IllegalArgumentException();
        }

        if (patterns.size() == 0)
            return;

        this.patterns = new Pattern[patterns.size()];

        int i = 0;
        for (Iterator it = patterns.iterator(); it.hasNext(); i++) {
            Object pattern = it.next();
            this.patterns[i] = (pattern instanceof Pattern)
                ? (Pattern)pattern
                : Pattern.compile(pattern.toString());
        }
    }

    /**
     * Checks to see if the filename matches one of the regular expressions.
     *
     * @param dir   the file directory
     * @param name  the filename
     * @return true if the filename matches one of the regular expressions
     */
    public boolean accept(File dir, String name) {
        if (dir != null && new File(dir, name).isDirectory()) {
            return false;
        }

        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].matcher(name).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if the filename matches one of the regular expressions.
     *
     * @param file the file to check
     * @return true if the filename matches one of the regular expressions
     */
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return false;
        }

        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].matcher(file.getName()).matches()) {
                return true;
            }
        }

        return false;
    }

}
