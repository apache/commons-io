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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * FileAlterationObserver represents the state of files below a root directory, checking the file system and notifying listeners of create, change or delete
 * events.
 * <p>
 * To use this implementation:
 * </p>
 * <ul>
 * <li>Create {@link FileAlterationListener} implementation(s) that process the file/directory create, change and delete events</li>
 * <li>Register the listener(s) with a {@link FileAlterationObserver} for the appropriate directory.</li>
 * <li>Either register the observer(s) with a {@link FileAlterationMonitor} or run manually.</li>
 * </ul>
 * <h2>Basic Usage</h2> Create a {@link FileAlterationObserver} for the directory and register the listeners:
 * <pre>
 *      File directory = new File(FileUtils.current(), "src");
 *      FileAlterationObserver observer = new FileAlterationObserver(directory);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 * <p>
 * To manually observe a directory, initialize the observer and invoked the {@link #checkAndNotify()} method as required:
 * </p>
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
 * <p>
 * Alternatively, register the observer(s) with a {@link FileAlterationMonitor}, which creates a new thread, invoking the observer at the specified interval:
 * </p>
 * <pre>
 *      long interval = ...
 *      FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
 *      monitor.addObserver(observer);
 *      monitor.start();
 *      ...
 *      monitor.stop();
 * </pre>
 * <h2>File Filters</h2> This implementation can monitor portions of the file system by using {@link FileFilter}s to observe only the files and/or directories
 * that are of interest. This makes it more efficient and reduces the noise from <em>unwanted</em> file system events.
 * <p>
 * <a href="https://commons.apache.org/io/">Commons IO</a> has a good range of useful, ready-made <a href="../filefilter/package-summary.html">File Filter</a>
 * implementations for this purpose.
 * </p>
 * <p>
 * For example, to only observe 1) visible directories and 2) files with a ".java" suffix in a root directory called "src" you could set up a
 * {@link FileAlterationObserver} in the following way:
 * </p>
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
 * <h2>FileEntry</h2>
 * <p>
 * {@link FileEntry} represents the state of a file or directory, capturing {@link File} attributes at a point in time. Custom
 * implementations of {@link FileEntry} can be used to capture additional properties that the basic implementation does not support. The
 * {@link FileEntry#refresh(File)} method is used to determine if a file or directory has changed since the last check and stores the current state of the
 * {@link File}'s properties.
 * </p>
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @see FileAlterationListener
 * @see FileAlterationMonitor
 * @since 2.0
 */
public class FileAlterationObserver implements Serializable {

    private static final long serialVersionUID = 1185122225658782848L;

    private static Comparator<File> toComparator(final IOCase ioCase) {
        switch (IOCase.value(ioCase, IOCase.SYSTEM)) {
        case SYSTEM:
            return NameFileComparator.NAME_SYSTEM_COMPARATOR;
        case INSENSITIVE:
            return NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
        default:
            return NameFileComparator.NAME_COMPARATOR;
        }
    }

    /**
     * List of listeners.
     */
    private transient final List<FileAlterationListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * The root directory to observe.
     */
    private final FileEntry rootEntry;

    /**
     * The file filter or null if none.
     */
    private transient final FileFilter fileFilter;

    /**
     * Compares file names.
     */
    private final Comparator<File> comparator;

    /**
     * Constructs an observer for the specified directory.
     *
     * @param directory the directory to observe.
     */
    public FileAlterationObserver(final File directory) {
        this(directory, null);
    }

    /**
     * Constructs an observer for the specified directory and file filter.
     *
     * @param directory  the directory to observe.
     * @param fileFilter The file filter or null if none.
     */
    public FileAlterationObserver(final File directory, final FileFilter fileFilter) {
        this(directory, fileFilter, null);
    }

    /**
     * Constructs an observer for the specified directory, file filter and file comparator.
     *
     * @param directory  the directory to observe.
     * @param fileFilter The file filter or null if none.
     * @param ioCase     what case sensitivity to use comparing file names, null means system sensitive.
     */
    public FileAlterationObserver(final File directory, final FileFilter fileFilter, final IOCase ioCase) {
        this(new FileEntry(directory), fileFilter, ioCase);
    }

    /**
     * Constructs an observer for the specified directory, file filter and file comparator.
     *
     * @param rootEntry  the root directory to observe.
     * @param fileFilter The file filter or null if none.
     * @param comparator how to compare files.
     */
    private FileAlterationObserver(final FileEntry rootEntry, final FileFilter fileFilter, final Comparator<File> comparator) {
        Objects.requireNonNull(rootEntry, "rootEntry");
        Objects.requireNonNull(rootEntry.getFile(), "rootEntry.getFile()");
        this.rootEntry = rootEntry;
        this.fileFilter = fileFilter != null ? fileFilter : TrueFileFilter.INSTANCE;
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    /**
     * Constructs an observer for the specified directory, file filter and file comparator.
     *
     * @param rootEntry  the root directory to observe.
     * @param fileFilter The file filter or null if none.
     * @param ioCase     what case sensitivity to use comparing file names, null means system sensitive.
     */
    protected FileAlterationObserver(final FileEntry rootEntry, final FileFilter fileFilter, final IOCase ioCase) {
        this(rootEntry, fileFilter, toComparator(ioCase));
    }

    /**
     * Constructs an observer for the specified directory.
     *
     * @param directoryName the name of the directory to observe.
     */
    public FileAlterationObserver(final String directoryName) {
        this(new File(directoryName));
    }

    /**
     * Constructs an observer for the specified directory and file filter.
     *
     * @param directoryName the name of the directory to observe.
     * @param fileFilter    The file filter or null if none.
     */
    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    /**
     * Constructs an observer for the specified directory, file filter and file comparator.
     *
     * @param directoryName the name of the directory to observe.
     * @param fileFilter    The file filter or null if none.
     * @param ioCase        what case sensitivity to use comparing file names, null means system sensitive.
     */
    public FileAlterationObserver(final String directoryName, final FileFilter fileFilter, final IOCase ioCase) {
        this(new File(directoryName), fileFilter, ioCase);
    }

    /**
     * Adds a file system listener.
     *
     * @param listener The file system listener.
     */
    public void addListener(final FileAlterationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Compares two file lists for files which have been created, modified or deleted.
     *
     * @param parentEntry     The parent entry.
     * @param previousEntries The original list of file entries.
     * @param currentEntries  The current list of files entries.
     */
    private void checkAndFire(final FileEntry parentEntry, final FileEntry[] previousEntries, final File[] currentEntries) {
        int c = 0;
        final FileEntry[] actualEntries = currentEntries.length > 0 ? new FileEntry[currentEntries.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;
        for (final FileEntry previousEntry : previousEntries) {
            while (c < currentEntries.length && comparator.compare(previousEntry.getFile(), currentEntries[c]) > 0) {
                actualEntries[c] = createFileEntry(parentEntry, currentEntries[c]);
                fireOnCreate(actualEntries[c]);
                c++;
            }
            if (c < currentEntries.length && comparator.compare(previousEntry.getFile(), currentEntries[c]) == 0) {
                fireOnChange(previousEntry, currentEntries[c]);
                checkAndFire(previousEntry, previousEntry.getChildren(), listFiles(currentEntries[c]));
                actualEntries[c] = previousEntry;
                c++;
            } else {
                checkAndFire(previousEntry, previousEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
                fireOnDelete(previousEntry);
            }
        }
        for (; c < currentEntries.length; c++) {
            actualEntries[c] = createFileEntry(parentEntry, currentEntries[c]);
            fireOnCreate(actualEntries[c]);
        }
        parentEntry.setChildren(actualEntries);
    }

    /**
     * Checks whether the file and its children have been created, modified or deleted.
     */
    public void checkAndNotify() {

        // fire onStart()
        listeners.forEach(listener -> listener.onStart(this));

        // fire directory/file events
        final File rootFile = rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndFire(rootEntry, rootEntry.getChildren(), listFiles(rootFile));
        } else if (rootEntry.isExists()) {
            checkAndFire(rootEntry, rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
        }
        // Else: Didn't exist and still doesn't

        // fire onStop()
        listeners.forEach(listener -> listener.onStop(this));
    }

    /**
     * Creates a new file entry for the specified file.
     *
     * @param parent The parent file entry.
     * @param file   The file to wrap.
     * @return A new file entry.
     */
    private FileEntry createFileEntry(final FileEntry parent, final File file) {
        final FileEntry entry = parent.newChildInstance(file);
        entry.refresh(file);
        entry.setChildren(listFileEntries(file, entry));
        return entry;
    }

    /**
     * Final processing.
     *
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    public void destroy() throws Exception {
        // noop
    }

    /**
     * Fires directory/file change events to the registered listeners.
     *
     * @param entry The previous file system entry.
     * @param file  The current file.
     */
    private void fireOnChange(final FileEntry entry, final File file) {
        if (entry.refresh(file)) {
            listeners.forEach(listener -> {
                if (entry.isDirectory()) {
                    listener.onDirectoryChange(file);
                } else {
                    listener.onFileChange(file);
                }
            });
        }
    }

    /**
     * Fires directory/file created events to the registered listeners.
     *
     * @param entry The file entry.
     */
    private void fireOnCreate(final FileEntry entry) {
        listeners.forEach(listener -> {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        });
        Stream.of(entry.getChildren()).forEach(this::fireOnCreate);
    }

    /**
     * Fires directory/file delete events to the registered listeners.
     *
     * @param entry The file entry.
     */
    private void fireOnDelete(final FileEntry entry) {
        listeners.forEach(listener -> {
            if (entry.isDirectory()) {
                listener.onDirectoryDelete(entry.getFile());
            } else {
                listener.onFileDelete(entry.getFile());
            }
        });
    }

    /**
     * Returns the directory being observed.
     *
     * @return the directory being observed.
     */
    public File getDirectory() {
        return rootEntry.getFile();
    }

    /**
     * Returns the fileFilter.
     *
     * @return the fileFilter.
     * @since 2.1
     */
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * Returns the set of registered file system listeners.
     *
     * @return The file system listeners
     */
    public Iterable<FileAlterationListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    /**
     * Initializes the observer.
     *
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings("unused") // Possibly thrown from subclasses.
    public void initialize() throws Exception {
        rootEntry.refresh(rootEntry.getFile());
        rootEntry.setChildren(listFileEntries(rootEntry.getFile(), rootEntry));
    }

    /**
     * Lists the file entries in {@code file}.
     *
     * @param file  The directory to list.
     * @param entry the parent entry.
     * @return The child file entries.
     */
    private FileEntry[] listFileEntries(final File file, final FileEntry entry) {
        return Stream.of(listFiles(file)).map(f -> createFileEntry(entry, f)).toArray(FileEntry[]::new);
    }

    /**
     * Lists the contents of a directory.
     *
     * @param directory The directory to list.
     * @return the directory contents or a zero length array if the empty or the file is not a directory
     */
    private File[] listFiles(final File directory) {
        return directory.isDirectory() ? sort(directory.listFiles(fileFilter)) : FileUtils.EMPTY_FILE_ARRAY;
    }

    /**
     * Removes a file system listener.
     *
     * @param listener The file system listener.
     */
    public void removeListener(final FileAlterationListener listener) {
        if (listener != null) {
            listeners.removeIf(listener::equals);
        }
    }

    private File[] sort(final File[] files) {
        if (files == null) {
            return FileUtils.EMPTY_FILE_ARRAY;
        }
        if (files.length > 1) {
            Arrays.sort(files, comparator);
        }
        return files;
    }

    /**
     * Returns a String representation of this observer.
     *
     * @return a String representation of this observer.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        builder.append('\'');
        builder.append(", ");
        builder.append(fileFilter.toString());
        builder.append(", listeners=");
        builder.append(listeners.size());
        builder.append("]");
        return builder.toString();
    }

}
