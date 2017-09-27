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
package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.io.IOCase;

/**
 * Compare the <b>path</b> of two files for order (see {@link File#getPath()}).
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by their path either in a case-sensitive, case-insensitive or
 * system dependent case sensitive way. A number of singleton instances
 * are provided for the various case sensitivity options (using {@link IOCase})
 * and the reverse of those options.
 * <p>
 * Example of a <i>case-sensitive</i> file path sort using the
 * {@link #PATH_COMPARATOR} singleton instance:
 * <pre>
 *       List&lt;File&gt; list = ...
 *       ((AbstractFileComparator) PathFileComparator.PATH_COMPARATOR).sort(list);
 * </pre>
 * <p>
 * Example of a <i>reverse case-insensitive</i> file path sort using the
 * {@link #PATH_INSENSITIVE_REVERSE} singleton instance:
 * <pre>
 *       File[] array = ...
 *       ((AbstractFileComparator) PathFileComparator.PATH_INSENSITIVE_REVERSE).sort(array);
 * </pre>
 * <p>
 *
 * @since 1.4
 */
public class PathFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 6527501707585768673L;

    /** Case-sensitive path comparator instance (see {@link IOCase#SENSITIVE}) */
    public static final Comparator<File> PATH_COMPARATOR = new PathFileComparator();

    /** Reverse case-sensitive path comparator instance (see {@link IOCase#SENSITIVE}) */
    public static final Comparator<File> PATH_REVERSE = new ReverseComparator(PATH_COMPARATOR);

    /** Case-insensitive path comparator instance (see {@link IOCase#INSENSITIVE}) */
    public static final Comparator<File> PATH_INSENSITIVE_COMPARATOR = new PathFileComparator(IOCase.INSENSITIVE);

    /** Reverse case-insensitive path comparator instance (see {@link IOCase#INSENSITIVE}) */
    public static final Comparator<File> PATH_INSENSITIVE_REVERSE = new ReverseComparator(PATH_INSENSITIVE_COMPARATOR);

    /** System sensitive path comparator instance (see {@link IOCase#SYSTEM}) */
    public static final Comparator<File> PATH_SYSTEM_COMPARATOR = new PathFileComparator(IOCase.SYSTEM);

    /** Reverse system sensitive path comparator instance (see {@link IOCase#SYSTEM}) */
    public static final Comparator<File> PATH_SYSTEM_REVERSE = new ReverseComparator(PATH_SYSTEM_COMPARATOR);

    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Construct a case sensitive file path comparator instance.
     */
    public PathFileComparator() {
        this.caseSensitivity = IOCase.SENSITIVE;
    }

    /**
     * Construct a file path comparator instance with the specified case-sensitivity.
     *
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     */
    public PathFileComparator(final IOCase caseSensitivity) {
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Compare the paths of two files the specified case sensitivity.
     *
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return a negative value if the first file's path
     * is less than the second, zero if the paths are the
     * same and a positive value if the first files path
     * is greater than the second file.
     *
     */
    @Override
    public int compare(final File file1, final File file2) {
        return caseSensitivity.checkCompareTo(file1.getPath(), file2.getPath());
    }

    /**
     * String representation of this file comparator.
     *
     * @return String representation of this file comparator
     */
    @Override
    public String toString() {
        return super.toString() + "[caseSensitivity=" + caseSensitivity + "]";
    }
}
