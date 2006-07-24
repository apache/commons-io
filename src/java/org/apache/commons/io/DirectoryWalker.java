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
 * <p>
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
 *    protected boolean handleDirectoryStart(File directory, int depth, Collection results) {
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
     * Internal method that walks the directory hierarchy.
     * <p>
     * Most users of this class do not need to call this method. This method will
     * be called automatically by another (public) method on the specific subclass.
     * <p>
     * Writers of subclasses should call this method to start the directory walk.
     * Once called, this method will emit events as it walks the hierarchy.
     * The event methods have the prefix <code>handle</code>.
     *
     * @param startDirectory  the directory to start from
     * @param results  the collection of result objects, may be updated
     */
    protected void walk(File startDirectory, Collection results) {
        handleStart(startDirectory, results);
        walk(startDirectory, 0, results);
        handleEnd(results);
    }

    /**
     * Main recursive method to examine the directory hierarchy.
     *
     * @param directory  the directory to examine
     * @param depth  the directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     */
    private void walk(File directory, int depth, Collection results) {
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
                            walk(files[i], childDepth, results);
                        } else {
                            handleFile(files[i], childDepth, results);
                        }
                    }
                }
            }
            handleDirectoryEnd(directory, depth, results);
        }
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
     * Overridable callback method invoked at the start of processing each directory.
     * <p>
     * This method returns a boolean to indicate if the directory should be examined
     * or not. If you return false, the next event received will be the
     * {@link #handleDirectoryEnd} for that directory. Note that this functionality
     * is in addition to the filtering by file filter.
     * <p>
     * This implementation does nothing and returns true.
     *
     * @param directory  the current directory being processed
     * @param depth  the current directory level (starting directory = 0)
     * @param results  the collection of result objects, may be updated
     * @return true to process this directory, false to skip this directory
     */
    protected boolean handleDirectoryStart(File directory, int depth, Collection results) {
        // do nothing - overridable by subclass
        return true;
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

}
