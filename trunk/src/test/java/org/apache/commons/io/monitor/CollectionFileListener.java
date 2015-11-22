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
import java.util.ArrayList;
import java.util.Collection;

/**
 * {@link FileAlterationListener} implementation that adds created, changed and deleted
 * files/directories to a set of {@link Collection}s.
 */
public class CollectionFileListener implements FileAlterationListener, Serializable {

    private static final long serialVersionUID = 939724715678693963L;
    private final boolean clearOnStart;
    private final Collection<File> createdFiles = new ArrayList<File>();
    private final Collection<File> changedFiles = new ArrayList<File>();
    private final Collection<File> deletedFiles = new ArrayList<File>();
    private final Collection<File> createdDirectories = new ArrayList<File>();
    private final Collection<File> changedDirectories = new ArrayList<File>();
    private final Collection<File> deletedDirectories = new ArrayList<File>();

    /**
     * Create a new observer.
     *
     * @param clearOnStart true if clear() should be called by onStart().
     */
    public CollectionFileListener(final boolean clearOnStart) {
        this.clearOnStart = clearOnStart;
    }

    /**
     * File system observer started checking event.
     *
     * @param observer The file system observer
     */
    public void onStart(final FileAlterationObserver observer) {
        if (clearOnStart) {
            clear();
        }
    }

    /**
     * Clear file collections.
     */
    public void clear() {
        createdFiles.clear();
        changedFiles.clear();
        deletedFiles.clear();
        createdDirectories.clear();
        changedDirectories.clear();
        deletedDirectories.clear();
    }

    /**
     * Return the set of changed directories.
     *
     * @return Directories which have changed
     */
    public Collection<File> getChangedDirectories() {
        return changedDirectories;
    }

    /**
     * Return the set of changed files.
     *
     * @return Files which have changed
     */
    public Collection<File> getChangedFiles() {
        return changedFiles;
    }

    /**
     * Return the set of created directories.
     *
     * @return Directories which have been created
     */
    public Collection<File> getCreatedDirectories() {
        return createdDirectories;
    }

    /**
     * Return the set of created files.
     *
     * @return Files which have been created
     */
    public Collection<File> getCreatedFiles() {
        return createdFiles;
    }

    /**
     * Return the set of deleted directories.
     *
     * @return Directories which been deleted
     */
    public Collection<File> getDeletedDirectories() {
        return deletedDirectories;
    }

    /**
     * Return the set of deleted files.
     *
     * @return Files which been deleted
     */
    public Collection<File> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * Directory created Event.
     * 
     * @param directory The directory created
     */
    public void onDirectoryCreate(final File directory) {
        createdDirectories.add(directory);
    }

    /**
     * Directory changed Event.
     * 
     * @param directory The directory changed
     */
    public void onDirectoryChange(final File directory) {
        changedDirectories.add(directory);
    }

    /**
     * Directory deleted Event.
     * 
     * @param directory The directory deleted
     */
    public void onDirectoryDelete(final File directory) {
        deletedDirectories.add(directory);
    }

    /**
     * File created Event.
     * 
     * @param file The file created
     */
    public void onFileCreate(final File file) {
        createdFiles.add(file);
    }

    /**
     * File changed Event.
     * 
     * @param file The file changed
     */
    public void onFileChange(final File file) {
        changedFiles.add(file);
    }

    /**
     * File deleted Event.
     * 
     * @param file The file deleted
     */
    public void onFileDelete(final File file) {
        deletedFiles.add(file);
    }

    /**
     * File system observer finished checking event.
     *
     * @param observer The file system observer
     */
    public void onStop(final FileAlterationObserver observer) {
    }

}
