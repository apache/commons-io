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
import java.util.Date;

import org.apache.commons.io.FileUtils;

/**
 * Filters files based on a cutoff time, can filter either older or newer files.
 * <p>
 * For example, to print all files and directories in the
 * current directory older than one day:
 *
 * <pre>
 * File dir = new File(".");
 * // We are interested in files older than one day
 * long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * String[] files = dir.list( new AgeFileFilter(cutoff) );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author Rahul Akolkar
 * @version $Id$
 * @since Commons IO 1.2
 */
public class AgeFileFilter extends AbstractFileFilter {

    /** The cutoff time threshold. */
    private long cutoff;
    /** Whether the files accepted will be older or newer. */
    private boolean acceptOlder;

    /**
     * Constructs a new age file filter for files older than a certain cutoff.
     *
     * @param age  the threshold age of the files
     */
    public AgeFileFilter(long cutoff) {
        this(cutoff, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain cutoff.
     *
     * @param age  the threshold age of the files
     * @param acceptOlder  if true, older files are accepted, else newer ones
     */
    public AgeFileFilter(long cutoff, boolean acceptOlder) {
        this.acceptOlder = acceptOlder;
        this.cutoff = cutoff;
    }

    /**
     * Constructs a new age file filter for files older than a certain
     * cutoff date.
     *
     * @param age  the threshold age of the files
     */
    public AgeFileFilter(Date cutoffDate) {
        this(cutoffDate, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain cutoff date.
     *
     * @param age  the threshold age of the files
     * @param acceptOlder  if true, older files are accepted, else newer ones
     */
    public AgeFileFilter(Date cutoffDate, boolean acceptOlder) {
        this(cutoffDate.getTime(), acceptOlder);
    }

    /**
     * Constructs a new age file filter for files older than a certain
     * File (whose last modification time will be used as reference).
     *
     * @param age  the threshold age of the files
     */
    public AgeFileFilter(File cutoffReference) {
        this(cutoffReference, true);
    }

    /**
     * Constructs a new age file filter for files on any one side
     * of a certain File (whose last modification time will be used as
     * reference).
     *
     * @param age  the threshold age of the files
     * @param acceptOlder  if true, older files are accepted, else newer ones
     */
    public AgeFileFilter(File cutoffReference, boolean acceptOlder) {
        this(cutoffReference.lastModified(), acceptOlder);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the last modification of the file matches cutoff
     * favorably.
     * If last modification time equals cutoff, file is not selected.
     *
     * @param file  the File to check
     * @return true if the filename matches
     */
    public boolean accept(File file) {
        boolean newer = FileUtils.isFileNewer(file, cutoff);
        return acceptOlder ? !newer : newer;
    }

}
