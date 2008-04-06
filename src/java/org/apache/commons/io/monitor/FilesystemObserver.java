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
package org.apache.commons.io.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.comparator.NameFileComparator;

/**
 * FilesystemObserver represents the state of files below a root directory,
 * checking the filesystem and notifying listeners of create, change or
 * delete events.
 * <p>
 * To use this implementation:
 * <ul>
 *   <li>Create {@link FilesystemListener} implementation(s) that process
 *      the file/directory create, change and delete events</li>
 *   <li>Register the listener(s) with a {@link FilesystemObserver} for
 *       the appropriate directory.</li>
 *   <li>Either register the observer(s) with a {@link FilesystemMonitor} or
 *       run manually.</li>
 * </ul>
 *
 * <h2>Basic Usage</h2>
 * Create a {@link FilesystemObserver} for the directory and register the listeners:
 * <pre>
 *      File directory = new File(new File("."), "src");
 *      FilesystemObserver observer = new FilesystemObserver(directory);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 * To manually observe a directory, initialize the observer and invoked the
 * {@link #checkAndNotify()} method as required:
 * <pre>
 *      // intialize
 *      observer.init();
 *      ...
 *      // invoke as required
 *      observer.checkAndNotify();
 *      ...
 *      observer.checkAndNotify();
 *      ...
 *      // finished
 *      observer.finish();
 * </pre>
 * Alternatively, register the oberver(s) with a {@link FilesystemMonitor},
 * which creates a new thread, invoking the observer at the specified interval:
 * <pre>
 *      long interval = ...
 *      FilesystemMonitor monitor = new FilesystemMonitor(interval);
 *      monitor.addObserver(observer);
 *      monitor.start();
 *      ...
 *      monitor.stop();
 * </pre>
 *
 * <h2>File Filters</h2>
 * This implementation can monitor portions of the file system
 * by using {@link FileFilter}s to observe only the files and/or directories
 * that are of interest. This makes it more efficient and reduces the
 * noise from <i>unwanted</i> file system events.
 * <p>
 * <a href="http://commons.apache.org/io/">Commons IO</a> has a good range of
 * useful, ready made 
 * <a href="apidocs/org/apache/commons/io/filefilter/package-summary.html">File Filter</a>
 * implementations for this purpose.
 * <p>
 * For example, to only observe 1) visible directories and 2) files with a ".java" suffix
 * in a root directory called "src" you could set up a {@link FilesystemObserver} in the following
 * way:
 * <pre>
 *      // Create a FileFilter
 *      IOFileFilter directories = FileFilterUtils.directoryFileFilter();
 *      IOFileFilter visible     = HiddenFileFilter.VISIBLE;
 *      IOFileFilter dirFilter   = FileFilterUtils.andFileFilter(directories, visible);
 *      IOFileFilter files       = FileFilterUtils.fileFileFilter();
 *      IOFileFilter javaSuffix  = FileFilterUtils.suffixFileFilter(".java");
 *      IOFileFilter fileFilter  = FileFilterUtils.andFileFilter(files, javaSuffix);
 *      IOFileFilter filter = FileFilterUtils.orFileFilter(dirFilter, fileFilter);
 *
 *      // Create the File system observer and register File Listeners
 *      FilesystemObserver observer = new FilesystemObserver(new File("src"), filter);
 *      observer.addListener(...);
 *      observer.addListener(...);
 *
 *      //
 * </pre>
 *
 * <h2>File Comparator</h2>
 * This implementation works by comparing the file names of the current contents of
 * a directory with the previous contents using the <i>case-sensitive</i> 
 * {@link NameFileComparator#NAME_COMPARATOR} to determine which files have been created,
 * deleted or still exist. However a custom {@link Comparator} can be specified and
 * one example usage would be to compare file names in a <i>case-insensitive</i>
 * manner (@link {@link NameFileComparator#NAME_INSENSITIVE_COMPARATOR} could be used
 * to do that).
 *
 * <h2>FilesystemEntry</h2>
 * {@link FilesystemEntry} represents the state of a file or directory, capturing
 * {@link File} attributes at a point in time. Custom implementations of
 * {@link FilesystemEntry} can be used to capture additional properties that the
 * basic implementation does not support. The {@link FilesystemEntry#hasChanged()}
 * method is used to determine if a file or directory has changed since the last
 * check. {@link FilesystemEntry#refresh()} stores the current state of the
 * {@link File}'s properties.
 *
 * @see FilesystemListener
 * @see FilesystemMonitor
 * @version $Id$
 * @since Commons IO 2.0
 */
public class FilesystemObserver implements Serializable {

    private static final File[] EMPTY_FILES = new File[0];
    static final FilesystemEntry[] EMPTY_ENTRIES = new FilesystemEntry[0];

    private final List<FilesystemListener> listeners = new CopyOnWriteArrayList<FilesystemListener>();
    private final FilesystemEntry rootEntry;
    private final FileFilter fileFilter;
    private final Comparator<File> comparator;

    /**
     * Construct an observer for the specified directory.
     *
     * @param directoryName the name of the directory to observe
     */
    public FilesystemObserver(String directoryName) {
        this(new File(directoryName));
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FilesystemObserver(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     * @param comparator The comparator to use when comparing file names, may be null
     */
    public FilesystemObserver(String directoryName, FileFilter fileFilter, Comparator<File> comparator) {
        this(new File(directoryName), fileFilter, comparator);
    }

    /**
     * Construct an observer for the specified directory.
     *
     * @param directory the directory to observe
     */
    public FilesystemObserver(File directory) {
        this(directory, (FileFilter)null);
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FilesystemObserver(File directory, FileFilter fileFilter) {
        this(directory, fileFilter, (Comparator<File>)null);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     * @param comparator The comparator to use when comparing file names, may be null
     */
    public FilesystemObserver(File directory, FileFilter fileFilter, Comparator<File> comparator) {
        this(new FilesystemEntry(directory), fileFilter, comparator);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param rootEntry the root directory to observe
     * @param fileFilter The file filter or null if none
     * @param comparator The comparator to use when comparing file names, may be null
     */
    protected FilesystemObserver(FilesystemEntry rootEntry, FileFilter fileFilter, Comparator<File> comparator) {
        if (rootEntry == null) {
            throw new IllegalArgumentException("Root entry is missing");
        }
        if (rootEntry.getFile() == null) {
            throw new IllegalArgumentException("Root directory is missing");
        }
        this.rootEntry = rootEntry;
        this.fileFilter = fileFilter;
        if (comparator == null) {
            this.comparator = NameFileComparator.NAME_COMPARATOR;
        } else {
            this.comparator = comparator;
        }
    }

    /**
     * Return the entry for the root directory.
     *
     * @return the entry for the root directory
     */
    public FilesystemEntry getRootEntry() {
        return rootEntry;
    }

    /**
     * Return the directory being observed.
     *
     * @return the directory being observed
     */
    public File getDirectory() {
        return rootEntry.getFile();
    }

    /**
     * Return the file filter, if any.
     *
     * @return the file filteror <code>null</code> if none
     */
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * Return the comparator.
     *
     * @return the comparator
     */
    public Comparator<File> getComparator() {
        return comparator;
    }

    /**
     * Add a file system listener.
     *
     * @param listener The file system listener
     */
    public void addListener(final FilesystemListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a file system listener.
     *
     * @param listener The file system listener
     */
    public void removeListener(final FilesystemListener listener) {
        if (listener != null) {
            while (listeners.remove(listener)) {
            }
        }
    }

    /**
     * Returns the set of registered file system listeners.
     *
     * @return The file system listeners
     */
    public Iterable<FilesystemListener> getListeners() {
        return listeners;
    }

    /**
     * Initialize the observer.
     *
     * @throws Exception if an error occurs
     */
    public void initialize() throws Exception {
        rootEntry.refresh();
        File[] files = listFiles(rootEntry.getFile());
        FilesystemEntry[] children = files.length > 0 ? new FilesystemEntry[files.length] : EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(rootEntry, files[i]);
        }
        rootEntry.setChildren(children);
    }

    /**
     * Final processing.
     *
     * @throws Exception if an error occurs
     */
    public void destroy() throws Exception {
    }

    /**
     * Check whether the file and its chlidren have been created, modified or deleted.
     */
    public void checkAndNotify() {

        /* fire onStart() */
        for (FilesystemListener listener : listeners) {
            listener.onStart(this);
        }

        /* fire directory/file events */
        File rootFile = rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), listFiles(rootFile));
        } else if (rootEntry.isExists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), EMPTY_FILES);
        } else {
            // Didn't exist and still doesn't
        }

        /* fire onStop() */
        for (FilesystemListener listener : listeners) {
            listener.onStop(this);
        }
    }

    /**
     * Compare two file lists for files which have been created, modified or deleted.
     *
     * @param parent The parent entry
     * @param previous The original list of files
     * @param files  The current list of files
     */
    private void checkAndNotify(FilesystemEntry parent, FilesystemEntry[] previous, File[] files) {
        int c = 0;
        FilesystemEntry[] current = files.length > 0 ? new FilesystemEntry[files.length] : EMPTY_ENTRIES;
        for (int p = 0; p < previous.length; p++) {
            while (c < files.length &&  comparator.compare(previous[p].getFile(), files[c]) > 0) {
                current[c] = createFileEntry(parent, files[c]);
                doCreate(current[c]);
                c++;
            }
            if (c < files.length && comparator.compare(previous[p].getFile(), files[c]) == 0) {
                doMatch(previous[p], files[c]);
                checkAndNotify(previous[p], previous[p].getChildren(), listFiles(files[c]));
                current[c] = previous[p];
                c++;
            } else {
                checkAndNotify(previous[p], previous[p].getChildren(), EMPTY_FILES);
                doDelete(previous[p]);
            }
        }
        for (; c < files.length; c++) {
            current[c] = createFileEntry(parent, files[c]);
            doCreate(current[c]);
        }
        parent.setChildren(current);
    }

    /**
     * Create a new file entry for the specified file.
     *
     * @param parent The parent file entry
     * @param file The file to create an entry for
     * @return A new file entry
     */
    private FilesystemEntry createFileEntry(FilesystemEntry parent, File file) {
        FilesystemEntry entry = parent.newChildInstance(file);
        entry.refresh();
        File[] files = listFiles(file);
        FilesystemEntry[] children = files.length > 0 ? new FilesystemEntry[files.length] : EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(entry, files[i]);
        }
        entry.setChildren(children);
        return entry;
    }

    /**
     * Fire directory/file created events to the registered listeners.
     *
     * @param entry The file entry
     */
    private void doCreate(FilesystemEntry entry) {
        for (FilesystemListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        }
        FilesystemEntry[] children = entry.getChildren();
        for (int i = 0; i < children.length; i++) {
            doCreate(children[i]);
        }
    }

    /**
     * Fire directory/file change events to the registered listeners.
     *
     * @param entry The previous file system entry
     * @param file The current file
     */
    private void doMatch(FilesystemEntry entry, File file) {
        if (entry.hasChanged()) {
            for (FilesystemListener listener : listeners) {
                if (entry.isDirectory()) {
                    listener.onDirectoryChange(entry.getFile());
                } else {
                    listener.onFileChange(entry.getFile());
                }
            }
            entry.refresh();
        }
        entry.setFile(file);
    }

    /**
     * Fire directory/file delete events to the registered listeners.
     *
     * @param entry The file entry
     */
    private void doDelete(FilesystemEntry entry) {
        for (FilesystemListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryDelete(entry.getFile());
            } else {
                listener.onFileDelete(entry.getFile());
            }
        }
    }

    /**
     * List the contents of a directory
     *
     * @param file The file to list the contents of
     * @return the directory contents or a zero length array if
     * the empty or the file is not a directory
     */
    private File[] listFiles(File file) {
        File[] children = null;
        if (file.isDirectory()) {
            children = (fileFilter == null) ? file.listFiles() : file.listFiles(fileFilter);
        }
        if (children == null) {
            children = EMPTY_FILES;
        }
        if (comparator != null && children.length > 1) {
            Arrays.sort(children, comparator);
        }
        return children;
    }

    /**
     * Provide a String representation of this observer.
     *
     * @return a String representation of this observer
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        if (fileFilter != null) {
            builder.append(", ");
            builder.append(fileFilter.toString());
        }
        builder.append(", listeners=");
        builder.append(listeners.size());
        builder.append("]");
        return builder.toString();
    }

}
