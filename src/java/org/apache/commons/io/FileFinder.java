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
import java.util.ArrayList;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Finds files in a directory hierarchy, with the potential for subclasses
 * to add additiional behaviour.
 * <p>
 * FileFinder can be used without changes to provide a list of the files
 * and directories in a file hierarchy starting from a specified point.
 * This list can be filtered by hierarchy depth and using a
 * {@link IOFileFilter file filter}.
 * <p>
 * Commons IO supplies many common filter implementations in the
 * <code>filefilter</code> package, see {@link FileFilterUtils}.
 * You can also create your own custom implementation, such as in the
 * file cleaner example below.
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
 * <h4>Example 3 - Custom Implementation</h4>
 * Example, showing how to create an implementation that deletes files
 * and directories and returns a list of what has been deleted.
 *
 * <pre>
 *  public class FileDelete extends FileFinder {
 *
 *    public FileDelete() {
 *    }
 *
 *    protected void handleDirectoryStart(File directory, int depth, List results) {
 *    }
 *
 *    protected void handleDirectoryEnd(File directory, int depth, List results) {
 *      directory.delete();
 *      results.add(directory);
 *    }
 *
 *    protected void handleFile(File file, int depth, List results) {
 *      file.delete();
 *      results.add(file);
 *    }
 *  }
 * </pre>
 *
 * @since Commons IO 1.3
 * @version $Revision$
 */
public class FileFinder {

    /** Singleton instance that finds all files */
    public static final FileFinder ALL_FILES = new FileFinder();

    private FileFilter filter;
    private int depthLimit = -1;

    /**
     * Restrictive consructor - use <code>ALL_FILES</code> singleton instance.
     */
    protected FileFinder() {
    }

    /**
     * Construct an instance with a filter.
     *
     * @param filter  the filter to limit the navigation/results
     */
    public FileFinder(FileFilter filter) {
        this(filter, -1);
    }

    /**
     * Construct an instance limiting the <i>depth</i> navigated to.
     * 
     * @param depthLimit  cntrols how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(int depthLimit) {
        this(null, depthLimit);
    }

    /**
     * Construct an instance with a filter and limit the <i>depth</i> navigated to.
     *
     * @param filter  the filter to limit the navigation/results
     * @param depthLimit  controls how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(FileFilter filter, int depthLimit) {
        this.filter = filter;
        this.depthLimit = depthLimit;
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
        return examine(startDirectory);
    }

    //-----------------------------------------------------------------------
    /**
     * Examines the directory hierarchy.
     *
     * @param startDirectory  the directory to start from
     * @return the collection of result objects
     */
    private List examine(File startDirectory) {
        List results = new ArrayList();
        handleStart(startDirectory, results);
        examine(startDirectory, 0, results);
        handleEnd(results);
        return results;
    }

    /**
     * Main recursive method to examine the directory hierarchy.
     *
     * @param directory  the directory to examine
     * @param depth  the directory level (starting directory = 0)
     * @return the collection of result objects
     */
    private void examine(File directory, int depth, List results) {
        boolean process = handleDirectoryStart(directory, depth, results);
        if (process) {
            int childDepth = depth + 1;
            if (depthLimit < 0 || childDepth <= depthLimit) {
                File[] files = (filter == null ? directory.listFiles() : directory.listFiles(filter));
                if (files == null) {
                    handleRestricted(directory);
                } else {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            examine(files[i], childDepth, results);
                        } else {
                            handleFile(files[i], childDepth, results);
                        }
                    }
                }
            }
        }
        handleDirectoryEnd(directory, depth, results);
    }

    //-----------------------------------------------------------------------
    /**
     * Overridable callback method invoked at the start of processing.
     * <p>
     * This implementation does nohting.
     *
     * @param startDirectory  the directory to start from
     * @param results  the collection of result objects, may be updated
     */
    protected void handleStart(File startDirectory, List results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the start of processing each directory.
     * <p>
     * This method returns a boolean to indicate if the directory should be examined
     * or not. If you return false, the next event received will be the
     * {@link #handleDirectoryEnd} for that directory. Note that this functionality
     * is in addition to the filtering by file filter.
     * <p>
     * This implementation adds the directory to the results collection.
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
     * Overridable callback method invoked for each (non-directory) file.
     * <p>
     * This implementation adds the file to the results collection.
     *
     * @param file  the current file being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleFile(File file, int depth, List results) {
        results.add(file);
    }

    /**
     * Overridable callback method invoked for each restricted directory.
     *
     * @param directory  the restricted directory
     */
    protected void handleRestricted(File directory) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing each directory.
     * <p>
     * This implementation does nothing.
     *
     * @param directory The directory being processed
     * @param depth The directory level (starting directory = 0)
     * @param results The collection of files found.
     */
    protected void handleDirectoryEnd(File directory, int depth, List results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing.
     * <p>
     * This implementation does nothing.
     *
     * @param results  the collection of result objects, may be updated
     */
    protected void handleEnd(List results) {
        // do nothing - overridable by subclass
    }

}
