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

/**
 * <p>Navigate/search through a File Hierarchy.</p>
 *
 * <p>FileFinder can be used as it is to provide a list
 * of the files and directories in a file hierarchy,
 * starting from a specified point.</p>
 * 
 * <p>It can be used in conjunction with a <code>FileFilter</code>
 * to selectively filter the results produced. Commons IO
 * provides a number of useful
 * {@link org.apache.commons.io.filefilter.IOFileFilter} implementations
 * which can be used in conjunction with this class.</p>
 * 
 * <p>FileFinder can also be extended to provide custom implementations
 * that process the file hierarchy further (see example file cleaner below).</p>
 *
 * <h4>Example 1 - List all files and directories</h4>
 * <p>Example, showing how to list all files and directories
 * starting from the current directory:</p>
 *
 * <pre>
 * List files = FileFinder.ALL_FILES.find();
 * for (int i = 0; i < files.size(); i++) {
 *     File file = (File)files.get(i);
 *     System.out.println(file.getName());
 * }
 * </pre>
 *
 * <h4>Example 2 - Filtered list of files and directories</h4>
 * <p>Example, showing how to list all directories and
 * files ending in ".java" starting in a directory called
 * "src":</p>
 *
 * <pre>
 * IOFileFilter dirFilter  = DirectoryFileFilter.INSTANCE;
 * IOFileFilter fileFilter = new SuffixFileFilter(".java");
 * IOFileFilter filter     = new OrFileFilter(directories, txtFiles);
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
 * <p>Example, showing how to create an implementation that
 * deletes files and directories and returns a list of
 * what has been deleted.</p>
 *
 * <pre>
 *  public class FileDelete extends FileFinder {
 *
 *      public FileDelete() {
 *      }
 *      
 *      protected void handleDirectoryStart(File directory, int depth, List results) {
 *      }
 *
 *      protected void handleDirectoryEnd(File directory, int depth, List results) {
 *          directory.delete();
 *          results.add(directory);
 *      }
 *
 *      protected void handleFile(File file, int depth, List results) {
 *          file.delete();
 *          results.add(file);
 *      }
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
     * <p>Construct an instance with a filter.</p>
     *
     * @param filter Filter to limit the navigation/results
     */
    public FileFinder(FileFilter filter) {
        this(filter, -1);
    }

    /**
     * <p>Construct an instance limiting the <i>depth</i>
     * navigated to.</p>
     * 
     * @param depthLimit Controls how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(int depthLimit) {
        this(null, depthLimit);
    }

    /**
     * <p>Construct an instance with a filter and limit
     * the <i>depth</i> navigated to.</p>
     *
     * @param filter Filter to limit the navigation/results
     * @param depthLimit Controls how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    public FileFinder(FileFilter filter, int depthLimit) {
        this.filter = filter;
        this.depthLimit = depthLimit;
    }

    /**
     * <p>Walk the file hierarchy starting from the current
     * directory.</p>
     *
     * @return The collection of files found.
     */
    public List find() {
        return find(new File("."));
    }

    /**
     * <p>Walk the file hierarchy starting from the specified
     * directory.</p>
     *
     * @param startDirectory The directory to start from
     * @return The collection of files found.
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
        List results = new ArrayList();
        handleDirectory(startDirectory, 0, results);
        return results;
    }

    /**
     * <p>Process a directory.</p>
     *
     * @param directory The directory to process
     * @param depth The directory level (starting directory = 0)
     * @param results The collection of files found.
     */
    private void handleDirectory(File directory, int depth, List results) {
        handleDirectoryStart(directory, depth, results);
        int childDepth = depth + 1;
        if (depthLimit < 0 || childDepth <= depthLimit) {
            File[] files = (filter == null ? directory.listFiles() : directory.listFiles(filter));
            if (files == null) {
                handleRestricted(directory);
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        handleDirectory(files[i], childDepth, results);
                    } else {
                        handleFile(files[i], childDepth, results);
                    }
                }
            }
        }
        handleDirectoryEnd(directory, depth, results);
    }

    /**
     * <p>Initial directory processing.</p>
     *
     * <p>This implementation adds the directory to the
     * results collection.</p>
     *
     * @param directory The directory being processed
     * @param depth The directory level (starting directory = 0)
     * @param results The collection of files found.
     */
    protected void handleDirectoryStart(File directory, int depth, List results) {
        results.add(directory);
    }

    /**
     * <p>Final directory processing.</p>
     * 
     * <p>This implementation does nothing.</p>
     *
     * @param directory The directory being processed
     * @param depth The directory level (starting directory = 0)
     * @param results The collection of files found.
     */
    protected void handleDirectoryEnd(File directory, int depth, List results) {
    }


    /**
     * <p>File processing.</p>
     * 
     * <p>This implementation adds the file to the results
     * collection.</p>
     *
     * @param file The file being processed
     * @param depth The directory level (starting directory = 0)
     * @param results The collection of files found.
     */
    protected void handleFile(File file, int depth, List results) {
        results.add(file);
    }

    /**
     * <p>Handle directories which are restricted.</p>
     * 
     * @param directory Restricted directory
     */
    protected void handleRestricted(File directory) {
    }
}