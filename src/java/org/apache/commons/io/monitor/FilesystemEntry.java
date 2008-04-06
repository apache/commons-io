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
import java.io.Serializable;

/**
 * {@link FilesystemEntry} represents the state of a file or directory, capturing
 * the following {@link File} attributes at a point in time:
 * <ul>
 *   <li>File Name (see {@link File#getName()})</li>
 *   <li>Exists - whether the file exists or not (see {@link File#exists()})</li>
 *   <li>Directory - whether the file is a directory or not (see {@link File#isDirectory()})</li>
 *   <li>Last Modified Date/Time (see {@link File#lastModified()})</li>
 *   <li>Children - contents of a directory (see {@link File#listFiles(java.io.FileFilter)})</li>
 * </ul>
 * <p>
 * <h3>Custom Implementations</h3>
 * If the state of additional {@link File} attributes is required then create a custom
 * {@link FilesystemEntry} with properties for those attributes. Override the
 * {@link #newChildInstance(File)} to return a new instance of the appropriate type.
 * You may also want to override the {@link #refresh()} and/or {@link #hasChanged()}
 * methods.
 * 
 */
public class FilesystemEntry implements Serializable {

    private FilesystemEntry parent;
    private FilesystemEntry[] children;
    private File file;
    private String name;
    private boolean exists;
    private boolean directory;
    private long lastModified;

    /**
     * Construct a new monitor for a specified {@link File}.
     *
     * @param file The file being monitored
     */
    public FilesystemEntry(File file) {
        this((FilesystemEntry)null, file);
    }

    /**
     * Construct a new monitor for a specified {@link File}.
     *
     * @param parent The parent
     * @param file The file being monitored
     */
    public FilesystemEntry(FilesystemEntry parent, File file) {
        if (file == null) {
            throw new IllegalArgumentException("File is missing");
        }
        this.file = file;
        this.parent = parent;
        this.name = file.getName();
    }

    /**
     * Refresh the attributes from the underlying {@link File}.
     * <p>
     * This implementation refreshes the <code>name</code>, <code>exists</code>
     * <code>directory</code> and <code>lastModified</code> properties.
     */
    public void refresh() {
        name = file.getName();
        exists = file.exists();
        if (exists) {
            directory = file.isDirectory();
            lastModified = file.lastModified();
        }
    }

    /**
     * Create a new child instance.
     * <p>
     * Custom implementations should override this method to return
     * a new instance of the appropriate type.
     *
     * @param file The child file
     * @return a new child instance
     */
    public FilesystemEntry newChildInstance(File file) {
        return new FilesystemEntry(this, file);
    }

    /**
     * Indicate whether the file has changed or not.
     * <p>
     * This implementation compares the <code>lastModified<code>
     * value of the {@link File} with the stored value.
     *
     * @return whether the file has changed or not
     */
    public boolean hasChanged() {
        return (lastModified != file.lastModified());
    }

    /**
     * Return the parent entry.
     *
     * @return the parent entry
     */
    public FilesystemEntry getParent() {
        return parent;
    }

    /**
     * Return the level
     *
     * @return the level
     */
    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    /**
     * Return the directory's files.
     *
     * @return This directory's files or an empty
     * array if the file is not a directory or the
     * directory is empty
     */
    public FilesystemEntry[] getChildren() {
        return children != null ? children : FilesystemObserver.EMPTY_ENTRIES;
    }

    /**
     * Set the directory's files.
     *
     * @param children This directory's files, may be null
     */
    public void setChildren(FilesystemEntry[] children) {
        this.children = children;
    }

    /**
     * Return the file being monitored.
     *
     * @return the file being monitored
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the file being monitored.
     *
     * @param file the file being monitored
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Return the file name.
     *
     * @return the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the file name.
     *
     * @param name the file name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the last modified time from the last time it
     * was checked.
     *
     * @return the last modified time
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Return the last modified time from the last time it
     * was checked.
     *
     * @param lastModified The last modified time
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Indicate whether the file existed the last time it
     * was checked.
     *
     * @return whether the file existed
     */
    public boolean isExists() {
        return exists;
    }

    /**
     * Set whether the file existed the last time it
     * was checked.
     *
     * @param exists whether the file exists or not
     */
    public void setExists(boolean exists) {
        this.exists = exists;
    }

    /**
     * Indicate whether the file is a directory or not.
     *
     * @return whether the file is a directory or not
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Set whether the file is a directory or not.
     *
     * @param directory whether the file is a directory or not
     */
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
}
