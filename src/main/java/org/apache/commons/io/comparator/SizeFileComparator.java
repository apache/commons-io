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

import org.apache.commons.io.FileUtils;

/**
 * Compare the <b>length/size</b> of two files for order (see
 * {@link File#length()} and {@link FileUtils#sizeOfDirectory(File)}).
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by their length/size.
 * <p>
 * Example of sorting a list of files using the
 * {@link #SIZE_COMPARATOR} singleton instance:
 * <pre>
 *       List&lt;File&gt; list = ...
 *       ((AbstractFileComparator) SizeFileComparator.SIZE_COMPARATOR).sort(list);
 * </pre>
 * <p>
 * Example of doing a <i>reverse</i> sort of an array of files using the
 * {@link #SIZE_REVERSE} singleton instance:
 * <pre>
 *       File[] array = ...
 *       ((AbstractFileComparator) SizeFileComparator.SIZE_REVERSE).sort(array);
 * </pre>
 * <p>
 * <strong>N.B.</strong> Directories are treated as <b>zero size</b> unless
 * <code>sumDirectoryContents</code> is {@code true}.
 *
 * @since 1.4
 */
public class SizeFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = -1201561106411416190L;

    /** Size comparator instance - directories are treated as zero size */
    public static final Comparator<File> SIZE_COMPARATOR = new SizeFileComparator();

    /** Reverse size comparator instance - directories are treated as zero size */
    public static final Comparator<File> SIZE_REVERSE = new ReverseComparator(SIZE_COMPARATOR);

    /**
     * Size comparator instance which sums the size of a directory's contents
     * using {@link FileUtils#sizeOfDirectory(File)}
     */
    public static final Comparator<File> SIZE_SUMDIR_COMPARATOR = new SizeFileComparator(true);

    /**
     * Reverse size comparator instance which sums the size of a directory's contents
     * using {@link FileUtils#sizeOfDirectory(File)}
     */
    public static final Comparator<File> SIZE_SUMDIR_REVERSE = new ReverseComparator(SIZE_SUMDIR_COMPARATOR);

    /** Whether the sum of the directory's contents should be calculated. */
    private final boolean sumDirectoryContents;

    /**
     * Construct a file size comparator instance (directories treated as zero size).
     */
    public SizeFileComparator() {
        this.sumDirectoryContents = false;
    }

    /**
     * Construct a file size comparator instance specifying whether the size of
     * the directory contents should be aggregated.
     * <p>
     * If the <code>sumDirectoryContents</code> is {@code true} The size of
     * directories is calculated using  {@link FileUtils#sizeOfDirectory(File)}.
     *
     * @param sumDirectoryContents {@code true} if the sum of the directories' contents
     *  should be calculated, otherwise {@code false} if directories should be treated
     *  as size zero (see {@link FileUtils#sizeOfDirectory(File)}).
     */
    public SizeFileComparator(final boolean sumDirectoryContents) {
        this.sumDirectoryContents = sumDirectoryContents;
    }

    /**
     * Compare the length of two files.
     *
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return a negative value if the first file's length
     * is less than the second, zero if the lengths are the
     * same and a positive value if the first files length
     * is greater than the second file.
     *
     */
    @Override
    public int compare(final File file1, final File file2) {
        long size1 = 0;
        if (file1.isDirectory()) {
            size1 = sumDirectoryContents && file1.exists() ? FileUtils.sizeOfDirectory(file1) : 0;
        } else {
            size1 = file1.length();
        }
        long size2 = 0;
        if (file2.isDirectory()) {
            size2 = sumDirectoryContents && file2.exists() ? FileUtils.sizeOfDirectory(file2) : 0;
        } else {
            size2 = file2.length();
        }
        final long result = size1 - size2;
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * String representation of this file comparator.
     *
     * @return String representation of this file comparator
     */
    @Override
    public String toString() {
        return super.toString() + "[sumDirectoryContents=" + sumDirectoryContents + "]";
    }
}
