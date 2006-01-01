/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/**
 * Filters files based on size, can filter either larger or smaller files
 * as compared to a given threshold.
 * <p>
 * For example, to print all files and directories in the
 * current directory whose size is greater than 1 MB:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( new SizeFileFilter(1024 * 1024) );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author Rahul Akolkar
 * @version $Id$
 * @since Commons IO 1.2
 */
public class SizeFileFilter extends AbstractFileFilter {

    /** The size threshold. */
    private long size;
    /** Whether the files accepted will be larger or smaller. */
    private boolean acceptLarger;

    /**
     * Constructs a new size file filter for files larger than a certain size.
     *
     * @param size  the threshold size of the files
     * @throws IllegalArgumentException if the size is negative
     */
    public SizeFileFilter(long size) {
        this(size, true);
    }

    /**
     * Constructs a new size file filter for files based on a certain size
     * threshold.
     *
     * @param size  the threshold size of the files
     * @param acceptLarger  if true, larger files are accepted, else smaller ones
     * @throws IllegalArgumentException if the size is negative
     */
    public SizeFileFilter(long size, boolean acceptLarger) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must be non-negative");
        }
        this.size = size;
        this.acceptLarger = acceptLarger;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the size of the file is favorable.
     * If size equals threshold, file is not selected.
     *
     * @param file  the File to check
     * @return true if the filename matches
     */
    public boolean accept(File file) {
        boolean smaller = file.length() < size;
        return acceptLarger ? !smaller : smaller;
    }

}
