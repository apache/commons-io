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

import org.apache.commons.io.IOCase;

/**
 * FileAlterationObserver represents the state of files below a root directory,
 * checking the file system and notifying listeners of create, change or
 * delete events.
 * <p>
 * To use this implementation:
 * <ul>
 *   <li>Create {@link FileAlterationListener} implementation(s) that process
 *      the file/directory create, change and delete events</li>
 *   <li>Register the listener(s) with a {@link FileAlterationObserver} for
 *       the appropriate directory.</li>
 *   <li>Either register the observer(s) with a {@link FileAlterationMonitor} or
 *       run manually.</li>
 * </ul>
 *
 * <h2>Basic Usage</h2>
 * Create a {@link FileAlterationObserver} for the directory and register the listeners:
 * <pre>
 *      File directory = new File(new File("."), "src");
 *      FileAlterationObserver observer = new FileAlterationObserver(directory);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 * To manually observe a directory, initialize the observer and invoked the
 * {@link #checkAndNotify()} method as required:
 * <pre>
 *      // initialize
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
 * Alternatively, register the observer(s) with a {@link FileAlterationMonitor},
 * which creates a new thread, invoking the observer at the specified interval:
 * <pre>
 *      long interval = ...
 *      FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
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
 * <a href="https://commons.apache.org/io/">Commons IO</a> has a good range of
 * useful, ready made
 * <a href="../filefilter/package-summary.html">File Filter</a>
 * implementations for this purpose.
 * <p>
 * For example, to only observe 1) visible directories and 2) files with a ".java" suffix
 * in a root directory called "src" you could set up a {@link FileAlterationObserver} in the following
 * way:
 * <pre>
 *      // Create a FileFilter
 *      IOFileFilter directories = FileFilterUtils.and(
 *                                      FileFilterUtils.directoryFileFilter(),
 *                                      HiddenFileFilter.VISIBLE);
 *      IOFileFilter files       = FileFilterUtils.and(
 *                                      FileFilterUtils.fileFileFilter(),
 *                                      FileFilterUtils.suffixFileFilter(".java"));
 *      IOFileFilter filter = FileFilterUtils.or(directories, files);
 *
 *      // Create the File system observer and register File Listeners
 *      FileAlterationObserver observer = new FileAlterationObserver(new File("src"), filter);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 *
 * <h2>FileEntry</h2>
 * {@link FileEntry} represents the state of a file or directory, capturing
 * {@link File} attributes at a point in time. Custom implementations of
 * {@link FileEntry} can be used to capture additional properties that the
 * basic implementation does not support. The {@link FileEntry#refresh(File)}
 * method is used to determine if a file or directory has changed since the last
 * check and stores the current state of the {@link File}'s properties.
 *
 * @see FileAlterationListener
 * @see FileAlterationMonitor
 *
 * @since 2.0
 */
public class FileAlterationObserver extends AbstractFileAlterationObserver<File, File> implements Serializable {

    private static final long serialVersionUID = 1185122225658782848L;

    /**
     * Constructs an observer for the specified directory.
     *
     * @param directoryName the name of the directory to observe
     */
    public FileAlterationObserver(final String directoryName) {
        this(new File(directoryName));
    }

    /**
     * Constructs an observer for the specified directory and file filter.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter,
                                  final IOCase caseSensitivity) {
        this(new File(directoryName), fileFilter, caseSensitivity);
    }

    /**
     * Constructs an observer for the specified directory.
     *
     * @param directory the directory to observe
     */
    public FileAlterationObserver(final File directory) {
        this(directory, null);
    }

    /**
     * Constructs an observer for the specified directory and file filter.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FileAlterationObserver(final File directory, final FileFilter fileFilter) {
        this(directory, fileFilter, null);
    }

    /**
     * Constructs an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    public FileAlterationObserver(final File directory, final FileFilter fileFilter, final IOCase caseSensitivity) {
        this(new FileEntry(new FileAdapter(directory)), fileFilter, caseSensitivity);
    }

    /**
     * Constructs an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param rootEntry the root directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    protected FileAlterationObserver(final FileEntry rootEntry, final FileFilter fileFilter,
                                     final IOCase caseSensitivity) {
        super(rootEntry, fileFilter, caseSensitivity);
    }

    /**
     * Returns the directory being observed.
     *
     * @return the directory being observed
     */
    public File getDirectory() {
    	return unwrapDirectory(rootEntry.getFile());
    }

    /**
     * 
     * @param wrapper
     * @return the underlying {@link File} object
     */
    protected File unwrapFile(IFile wrapper) {
    	if(wrapper instanceof FileAdapter) {
    		return ((FileAdapter) wrapper).getFile();
    	}
    	return null;
    }

    /**
     * Initialize the observer.
     *
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    public void initialize() throws Exception {
        rootEntry.refresh(rootEntry.getFile());
        final FileEntry[] children = doListFiles(rootEntry.getFile(), rootEntry);
        rootEntry.setChildren(children);
    }

    /**
     * Final processing.
     *
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    public void destroy() throws Exception {
        // noop
    }

    /**
     * Checks whether the file and its children have been created, modified or deleted.
     */
    public void checkAndNotify() {

        /* fire onStart() */
        for (final FileAlterationListener<File, File> listener : listeners) {
            listener.onStart((IFileAlterationObserver<File, File>) this);
        }

        /* fire directory/file events */
        final IFile rootFile = rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), listFiles(rootFile));
        } else if (rootEntry.isExists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), EMPTY_FILE_ARRAY);
        } else {
            // Didn't exist and still doesn't
        }

        /* fire onStop() */
        for (final FileAlterationListener<File, File> listener : listeners) {
            listener.onStop((IFileAlterationObserver<File, File>) this);
        }
    }

    /**
     * Provide a String representation of this observer.
     *
     * @return a String representation of this observer
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        builder.append('\'');
        if (fileFilter != null) {
            builder.append(", ");
            builder.append(fileFilter.toString());
        }
        builder.append(", listeners=");
        builder.append(listeners.size());
        builder.append("]");
        return builder.toString();
    }

	@Override
	protected File unwrapDirectory(IFile directory){
		return unwrapFile(directory);
	}

}
