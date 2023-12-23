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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.attribute.FileTimes;

/**
 * The state of a file or directory, capturing the following {@link File} attributes at a point in time.
 * <ul>
 *   <li>File Name (see {@link File#getName()})</li>
 *   <li>Exists - whether the file exists or not (see {@link File#exists()})</li>
 *   <li>Directory - whether the file is a directory or not (see {@link File#isDirectory()})</li>
 *   <li>Last Modified Date/Time (see {@link FileUtils#lastModifiedUnchecked(File)})</li>
 *   <li>Length (see {@link File#length()}) - directories treated as zero</li>
 *   <li>Children - contents of a directory (see {@link File#listFiles(java.io.FileFilter)})</li>
 * </ul>
 *
 * <h2>Custom Implementations</h2>
 * <p>
 * If the state of additional {@link File} attributes is required then create a custom
 * {@link FileEntry} with properties for those attributes. Override the
 * {@link #newChildInstance(File)} to return a new instance of the appropriate type.
 * You may also want to override the {@link #refresh(File)} method.
 * </p>
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 * @see FileAlterationObserver
 * @since 2.0
 */
public class FileEntry implements Serializable {

    private static final long serialVersionUID = -2505664948818681153L;

    static final FileEntry[] EMPTY_FILE_ENTRY_ARRAY = {};

    /** The parent. */
    private final FileEntry parent;

    /** My children. */
    private FileEntry[] children;

    /** Monitored file. */
    private final File file;

    /** Monitored file name. */
    private String name;

    /** Whether the file exists. */
    private boolean exists;

    /** Whether the file is a directory or not. */
    private boolean directory;

    /** The file's last modified timestamp. */
    private SerializableFileTime lastModified = SerializableFileTime.EPOCH;

    /** The file's length. */
    private long length;

    /**
     * Constructs a new monitor for a specified {@link File}.
     *
     * @param file The file being monitored
     */
    public FileEntry(final File file) {
        this(null, file);
    }

    /**
     * Constructs a new monitor for a specified {@link File}.
     *
     * @param parent The parent.
     * @param file The file being monitored.
     */
    public FileEntry(final FileEntry parent, final File file) {
        this.file = Objects.requireNonNull(file, "file");
        this.parent = parent;
        this.name = file.getName();
    }

    /**
     * Gets the directory's files.
     *
     * @return This directory's files or an empty
     * array if the file is not a directory or the
     * directory is empty
     */
    public FileEntry[] getChildren() {
        return children != null ? children : EMPTY_FILE_ENTRY_ARRAY;
    }

    /**
     * Gets the file being monitored.
     *
     * @return the file being monitored
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the last modified time from the last time it
     * was checked.
     *
     * @return the last modified time in milliseconds.
     */
    public long getLastModified() {
        return lastModified.toMillis();
    }

    /**
     * Gets the last modified time from the last time it was checked.
     *
     * @return the last modified time.
     * @since 2.12.0
     */
    public FileTime getLastModifiedFileTime() {
        return lastModified.unwrap();
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * Gets the level
     *
     * @return the level
     */
    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parent entry.
     *
     * @return the parent entry
     */
    public FileEntry getParent() {
        return parent;
    }

    /**
     * Tests whether the file is a directory or not.
     *
     * @return whether the file is a directory or not
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Tests whether the file existed the last time it
     * was checked.
     *
     * @return whether the file existed
     */
    public boolean isExists() {
        return exists;
    }

    /**
     * Constructs a new child instance.
     * <p>
     * Custom implementations should override this method to return
     * a new instance of the appropriate type.
     * </p>
     *
     * @param file The child file
     * @return a new child instance
     */
    public FileEntry newChildInstance(final File file) {
        return new FileEntry(this, file);
    }

    /**
     * Refreshes the attributes from the {@link File}, indicating
     * whether the file has changed.
     * <p>
     * This implementation refreshes the {@code name}, {@code exists},
     * {@code directory}, {@code lastModified} and {@code length}
     * properties.
     * </p>
     * <p>
     * The {@code exists}, {@code directory}, {@code lastModified}
     * and {@code length} properties are compared for changes
     * </p>
     *
     * @param file the file instance to compare to
     * @return {@code true} if the file has changed, otherwise {@code false}
     */
    public boolean refresh(final File file) {
        // cache original values
        final boolean origExists = exists;
        final SerializableFileTime origLastModified = lastModified;
        final boolean origDirectory = directory;
        final long origLength = length;

        // refresh the values
        name = file.getName();
        exists = Files.exists(file.toPath());
        directory = exists && file.isDirectory();
        try {
            setLastModified(exists ? FileUtils.lastModifiedFileTime(file) : FileTimes.EPOCH);
        } catch (final IOException e) {
            setLastModified(SerializableFileTime.EPOCH);
        }
        length = exists && !directory ? file.length() : 0;

        // Return if there are changes
        return exists != origExists || !lastModified.equals(origLastModified) || directory != origDirectory
            || length != origLength;
    }

    /**
     * Sets the directory's files.
     *
     * @param children This directory's files, may be null
     */
    public void setChildren(final FileEntry... children) {
        this.children = children;
    }

    /**
     * Sets whether the file is a directory or not.
     *
     * @param directory whether the file is a directory or not
     */
    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }

    /**
     * Sets whether the file existed the last time it
     * was checked.
     *
     * @param exists whether the file exists or not
     */
    public void setExists(final boolean exists) {
        this.exists = exists;
    }

    /**
     * Sets the last modified time from the last time it was checked.
     *
     * @param lastModified The last modified time.
     * @since 2.12.0
     */
    public void setLastModified(final FileTime lastModified) {
        setLastModified(new SerializableFileTime(lastModified));
    }

    /**
     * Sets the last modified time from the last time it
     * was checked.
     *
     * @param lastModified The last modified time in milliseconds.
     */
    public void setLastModified(final long lastModified) {
        setLastModified(FileTime.fromMillis(lastModified));
    }

    void setLastModified(final SerializableFileTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the length.
     *
     * @param length the length
     */
    public void setLength(final long length) {
        this.length = length;
    }

    /**
     * Sets the file name.
     *
     * @param name the file name
     */
    public void setName(final String name) {
        this.name = name;
    }
}
