/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
package org.apache.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Finds files in a directory hierarchy applying a file filter.
 * <p>
 * FileFinder can be used without changes to provide a list of the files
 * and directories in a file hierarchy starting from a specified point.
 * This list can be filtered by hierarchy depth and using a
 * {@link IOFileFilter file filter}.
 * <p>
 * Commons IO supplies many common filter implementations in the
 * <code>filefilter</code> package, see {@link FileFilterUtils}.
 * <p>
 * In addition to using FileFinder directly, you can create more advanced
 * subclasses. FileFinder is a subclass of {@link DirectoryWalker} which
 * provides a structured approach to walking the directory structure of
 * a filing system. See that class for more details.
 *
 * <h4>Example 1 - List all files and directories</h4>
 * Example, showing how to list all files and directories starting from
 * the current directory:
 * <pre>
 * List files = FileFinder.ALL_FILES.find();
 * for (int i = 0; i < files.size(); i++) {
 *     File file = (File)files.get(i);
 *     System.out.println(file.getName());
 * }
 * </pre>
 *
 * <h4>Example 2 - Filtered list of files and directories</h4>
 * Example, showing how to list all files ending in ".java" starting in
 * a directory called "src":
 * <pre>
 * IOFileFilter filesFilter = FileFileFilter.FILE;
 * IOFileFilter javaFilter  = new SuffixFileFilter(".java");
 * IOFileFilter filter      = new AndFileFilter(filesFilter, javaFilter);
 * 
 * FileFinder finder = new FileFinder(filter);
 * 
 * List files = finder.find(new File("src"));
 * for (int i = 0; i < files.size(); i++) {
 *     File file = (File)files.get(i);
 *     System.out.println(file.getName());
 * }
 * </pre>
 *
 * @since Commons IO 1.3
 * @version $Revision$
 */
public class FileFinder extends DirectoryWalker {

    /** Singleton instance that finds all files */
    public static final FileFinder ALL_FILES = new FileFinder();

    /**
     * Restrictive consructor - use <code>ALL_FILES</code> singleton instance.
     */
    protected FileFinder() {
        super(null, -1);
    }

    /**
     * Construct an instance with a filter.
     *
     * @param filter  the filter to limit the navigation/results
     */
    public FileFinder(FileFilter filter) {
        super(filter, -1);
    }

    /**
     * Construct an instance limiting the <i>depth</i> navigated to.
     * 
     * @param depthLimit  cntrols how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(int depthLimit) {
        super(null, depthLimit);
    }

    /**
     * Construct an instance with a filter and limit the <i>depth</i> navigated to.
     *
     * @param filter  the filter to limit the navigation/results
     * @param depthLimit  controls how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(FileFilter filter, int depthLimit) {
        super(filter, depthLimit);
    }

    //-----------------------------------------------------------------------
    /**
     * Finds all the files and directories in the directory hierarchy starting
     * from the current directory.
     *
     * @return the collection of files found
     */
    public List find() {
        return find(new File("."));
    }

    /**
     * Finds all the files and directories in the directory hierarchy starting
     * from the specified directory.
     *
     * @param startDirectory  the directory to start from, must be valid
     * @return the collection of files found
     * @throws IllegalArgumentException if the start directory is null,
     * doesn't exist or isn't a directory
     */
    public List find(File startDirectory) {
        if (startDirectory == null || !startDirectory.exists()) {
            String message = "Directory does not exist: " + startDirectory;
            throw new IllegalArgumentException(message);
        }
        if (!startDirectory.isDirectory()) {
            String message = "Not a directory: " + startDirectory;
            throw new IllegalArgumentException(message);
        }
        return walk(startDirectory);
    }

    //-----------------------------------------------------------------------
    /**
     * Handles a directory start by adding the File to the result set.
     *
     * @param directory  the current directory being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     * @return true to process this directory, false to skip this directory
     */
    protected boolean handleDirectoryStart(File directory, int depth, List results) {
        results.add(directory);
        return true;
    }

    /**
     * Handles a file by adding the File to the result set.
     *
     * @param file  the current file being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleFile(File file, int depth, List results) {
        results.add(file);
    }

}
