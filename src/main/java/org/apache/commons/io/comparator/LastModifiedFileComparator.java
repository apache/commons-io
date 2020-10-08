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

/**
 * Compare the <b>last modified date/time</b> of two files for order
 * (see {@link File#lastModified()}).
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by their last modified date/time.
 * <p>
 * Example of sorting a list of files using the
 * {@link #LASTMODIFIED_COMPARATOR} singleton instance:
 * <pre>
 *       List&lt;File&gt; list = ...
 *       ((AbstractFileComparator) LastModifiedFileComparator.LASTMODIFIED_COMPARATOR).sort(list);
 * </pre>
 * <p>
 * Example of doing a <i>reverse</i> sort of an array of files using the
 * {@link #LASTMODIFIED_REVERSE} singleton instance:
 * <pre>
 *       File[] array = ...
 *       ((AbstractFileComparator) LastModifiedFileComparator.LASTMODIFIED_REVERSE).sort(array);
 * </pre>
 * <p>
 *
 * @since 1.4
 */
public class LastModifiedFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = 7372168004395734046L;

    /** Last modified comparator instance. */
    public static final Comparator<File> LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();

    /** Reverse last modified comparator instance. */
    public static final Comparator<File> LASTMODIFIED_REVERSE = new ReverseFileComparator(LASTMODIFIED_COMPARATOR);

    /**
     * Compares the last the last modified date/time of two files.
     *
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @return a negative value if the first file's last modified date/time is less than the second, zero if the last
     *         modified date/time are the same and a positive value if the first files last modified date/time is
     *         greater than the second file.
     */
    @Override
    public int compare(final File file1, final File file2) {
        final long result = file1.lastModified() - file2.lastModified();
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
