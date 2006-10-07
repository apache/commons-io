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
package org.apache.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

/**
 * Abstract class that walks through a directory hierarchy and provides
 * subclasses with convenient hooks to add specific behaviour.
 * <p>
 * This class operates with a {@link FileFilter} and maximum depth to
 * limit the files and direcories visited.
 * Commons IO supplies many common filter implementations in the 
 * <a href="filefilter/package-summary.html"> filefilter</a> package.
 *
 * <h3>Example Implementation</h3>
 *
 * There are many possible extensions, for example, to delete all
 * files and '.svn' directories, and return a list of deleted files:
 * <pre>
 *  public class FileCleaner extends DirectoryWalker {
 *
 *    public FileCleaner() {
 *      super(null, -1);
 *    }
 *
 *    public List clean(File startDirectory) {
 *      List results = new ArrayList();
 *      walk(startDirectory, results);
 *      return results;
 *    }
 *
 *    protected boolean handleDirectory(File directory, int depth, Collection results) {
 *      // delete svn directories and then skip
 *      if (".svn".equals(directory.getName())) {
 *        directory.delete();
 *        return false;
 *      } else {
 *        return true;
 *      }
 *
 *    }
 *
 *    protected void handleFile(File file, int depth, Collection results) {
 *      // delete file and add to list of deleted
 *      file.delete();
 *      results.add(file);
 *    }
 *  }
 * </pre>
 *
 * <h3>Filter Example</h3>
 *
 * If you wanted all directories which are not hidden
 * and files which end in ".txt" - you could build a composite filter
 * using the filter implementations in the Commons IO
 * <a href="filefilter/package-summary.html">filefilter</a> package
 * in the following way:
 *
 * <pre>
 *
 *    // Create a filter for Non-hidden directories
 *    IOFileFilter fooDirFilter = 
 *        FileFilterUtils.andFileFilter(FileFilterUtils.directoryFileFilter,
 *                                      HiddenFileFilter.VISIBLE);
 *
 *    // Create a filter for Files ending in ".txt"
 *    IOFileFilter fooFileFilter = 
 *        FileFilterUtils.andFileFilter(FileFilterUtils.fileFileFilter,
 *                                      FileFilterUtils.suffixFileFilter(".txt"));
 *
 *    // Combine the directory and file filters using an OR condition
 *    java.io.FileFilter fooFilter = 
 *        FileFilterUtils.orFileFilter(fooDirFilter, fooFileFilter);
 *
 *    // Use the filter to construct a DirectoryWalker implementation
 *    FooDirectoryWalker walker = new FooDirectoryWalker(fooFilter, -1);
 *
 * </pre>
 *
 * <h3>Cancellation</h3>
 *
 * The DirectoryWalker contains some of the logic required for cancel processing.
 * Subclasses must complete the implementation.
 * This is for performance and to ensure you think about the multihreaded implications.
 * <p>
 * Before any processing occurs on each file or directory the
 * <code>isCancelled()</code> method is called. If it returns <code>true</code>
 * then <code>handleCancelled()<code> is called. This method can decide whether
 * to accept or ignore the cancellation. If it accepts it then all further
 * processing is skipped and the operation returns. If it rejects it then
 * processing continues on as before. This is useful if a group of files has
 * meaning and cancellation cannot occur in the middle of the group.
 * <p>
 * The default implementation of <code>isCancelled()</code> always
 * returns <code>false</code> and it is down to the implementation
 * to fully implement the <code>isCancelled()</code> behaviour.
 * <p>
 * The following example uses the
 * <a href="http://java.sun.com/docs/books/jls/second_edition/html/classes.doc.html#36930">
 * volatile</a> keyword to (hopefully) ensure it will work properly in
 * a multi-threaded environment.
 *
 * <pre>
 *  public class FooDirectoryWalker extends DirectoryWalker {
 *
 *    private volatile boolean cancelled = false;
 *
 *    public void cancel() {
 *        cancelled = true;
 *    }
 *
 *    public boolean isCancelled() {
 *        return cancelled;
 *    }
 *
 *    protected boolean handleCancelled(File file, int depth, Collection results) {
 *       // implement any cancel processing here
 *       return true;  // accept cancellation
 *    }
 *  }
 * </pre>
 *
 * @since Commons IO 1.3
 * @version $Revision: 424748 $
 */
public abstract class DirectoryWalker {

    /**
     * The file filter to use to filter files and directories.
     */
    private final FileFilter filter;
    /**
     * The limit on the directory depth to walk.
     */
    private final int depthLimit;

    /**
     * Construct an instance with no filtering and unlimited <i>depth</i>.
     */
    protected DirectoryWalker() {
        this(null, -1);
    }

    /**
     * Construct an instance with a filter and limit the <i>depth</i> navigated to.
     *
     * @param filter  the filter to limit the navigation/results, may be null
     * @param depthLimit  controls how <i>deep</i> the hierarchy is
     *  navigated to (less than 0 means unlimited)
     */
    protected DirectoryWalker(FileFilter filter, int depthLimit) {
        this.filter = filter;
        this.depthLimit = depthLimit;
    }

    //-----------------------------------------------------------------------
    /**
     * Internal method that walks the directory hierarchy in a depth-first manner.
     * <p>
     * Most users of this class do not need to call this method. This method will
     * be called automatically by another (public) method on the specific subclass.
     * <p>
     * Writers of subclasses should call this method to start the directory walk.
     * Once called, this method will emit events as it walks the hierarchy.
     * The event methods have the prefix <code>handle</code>.
     *
     * @param startDirectory  the directory to start from, not null
     * @param results  the collection of result objects, may be updated
     * @return true if completed, false if cancelled
     * @throws NullPointerException if the start directory is null
     */
    protected boolean walk(File startDirectory, Collection results) {
        handleStart(startDirectory, results);
        if (walk(startDirectory, 0, results) == false) {
            return false;  // cancelled
        }
        handleEnd(results);
        return true;
    }

    /**
     * Main recursive method to examine the directory hierarchy.
     *
     * @param directory  the directory to examine, not null
     * @param depth  the directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     * @return false if cancelled
     */
    private boolean walk(File directory, int depth, Collection results) {
        if (isCancelled() && handleCancelled(directory, depth, results)) {
            return false;  // cancelled
        }
        if (handleDirectory(directory, depth, results)) {
            handleDirectoryStart(directory, depth, results);
            int childDepth = depth + 1;
            if (depthLimit < 0 || childDepth <= depthLimit) {
                File[] files = (filter == null ? directory.listFiles() : directory.listFiles(filter));
                if (files == null) {
                    handleRestricted(directory, childDepth, results);
                } else {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            if (walk(files[i], childDepth, results) == false) {
                                return false;  // cancelled
                            }
                        } else {
                            if (isCancelled() && handleCancelled(files[i], childDepth, results)) {
                                return false;  // cancelled
                            }
                            handleFile(files[i], childDepth, results);
                        }
                    }
                }
            }
            handleDirectoryEnd(directory, depth, results);
        }
        return true;
    }

    //-----------------------------------------------------------------------
    /**
     * Overridable callback method invoked at the start of processing.
     * <p>
     * This implementation does nothing.
     *
     * @param startDirectory  the directory to start from
     * @param results  the collection of result objects, may be updated
     */
    protected void handleStart(File startDirectory, Collection results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked to determine if a directory should be processed.
     * <p>
     * This method returns a boolean to indicate if the directory should be examined or not.
     * If you return false, the entire directory and any subdirectories will be skipped.
     * Note that this functionality is in addition to the filtering by file filter.
     * <p>
     * This implementation does nothing and returns true.
     *
     * @param directory  the current directory being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     * @return true to process this directory, false to skip this directory
     */
    protected boolean handleDirectory(File directory, int depth, Collection results) {
        // do nothing - overridable by subclass
        return true;  // process directory
    }

    /**
     * Overridable callback method invoked at the start of processing each directory.
     * <p>
     * This implementation does nothing.
     *
     * @param directory  the current directory being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleDirectoryStart(File directory, int depth, Collection results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked for each (non-directory) file.
     * <p>
     * This implementation does nothing.
     *
     * @param file  the current file being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleFile(File file, int depth, Collection results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked for each restricted directory.
     * <p>
     * This implementation does nothing.
     *
     * @param directory  the restricted directory
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleRestricted(File directory, int depth, Collection results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing each directory.
     * <p>
     * This implementation does nothing.
     *
     * @param directory  the directory being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    protected void handleDirectoryEnd(File directory, int depth, Collection results) {
        // do nothing - overridable by subclass
    }

    /**
     * Overridable callback method invoked at the end of processing.
     * <p>
     * This implementation does nothing.
     *
     * @param results  the collection of result objects, may be updated
     */
    protected void handleEnd(Collection results) {
        // do nothing - overridable by subclass
    }

    //-----------------------------------------------------------------------
    /**
     * Indicates whether the operation has been cancelled or not.
     * <p>
     * This implementation always returns <code>false</code>.
     *
     * @return true if the operation has been cancelled
     */
    protected boolean isCancelled() {
        return false;
    }

    /**
     * Overridable callback method invoked when the operation is cancelled.
     * <p>
     * This method returns a boolean to indicate if the cancellation is being
     * accepted or rejected. This could be useful if you need to finish processing
     * all the files in a directory before accepting the cancellation request.
     * For example, this only accepts the cancel when the current directory is complete:
     * <pre>
     * protected boolean handleCancelled(File file, int depth, Collection results) {
     *   return file.isDirectory();
     * }
     * </pre>
     * If you return true, then the whole operation is cancelled and no more event
     * methods will be called.
     * <p>
     * If you return false, then normal processing will continue until the next time
     * the <code>isCancelled()</code> method returns false.
     * <p>
     * This implementation returns true, accepting the cancellation.
     *
     * @param file  the file about to be processed which may be a file or a directory
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     * @return true to accept the cancellation, false to reject it
     */
    protected boolean handleCancelled(File file, int depth, Collection results) {
        // do nothing - overridable by subclass
        return true;  // accept cancellation
    }

}
