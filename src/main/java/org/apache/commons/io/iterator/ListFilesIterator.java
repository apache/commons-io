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
package org.apache.commons.io.iterator;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A true Iterator implementation to traverse a directory tree.
 * This class is meant to to be used by utility methods on
 * {@link org.apache.commons.io.FileUtils}
 */
public class ListFilesIterator implements Iterator<File> {

    private final File directory;
    private final IOFileFilter effFileFilter;
    private final IOFileFilter effDirFilter;
    private final boolean includeSubDirectories;
    private final boolean recursive;

    private File next = null;
    private LinkedList<IteratorPosition> stack = new LinkedList<>();

    /**
     * Creates a new instance for traversing the given directory
     * @param directory to be traversed
     * @param effFileFilter the effective file filter to be used. Must be provided by {@link org.apache.commons.io.FileUtils}
     * @param effDirFilter the effective directory filter to be used. Must be provided by {@link org.apache.commons.io.FileUtils}
     * @param includeSubDirectories indicates if should include the directories
     * @param recursive indicates if should process recursively
     */
    public ListFilesIterator(final File directory, final IOFileFilter effFileFilter, final IOFileFilter effDirFilter,
                             final boolean includeSubDirectories, final boolean recursive) {

        this.directory = directory;
        this.effFileFilter = effFileFilter;
        this.effDirFilter = effDirFilter;
        this.includeSubDirectories = includeSubDirectories;
        this.recursive = recursive;

        final File[] found = this.directory.listFiles((FileFilter) FileFilterUtils.or(effFileFilter, effDirFilter));

        if (includeSubDirectories) {
            stack.push(new IteratorPosition(new File[] {directory}));
        } else {
            if (found.length > 0) {
                stack.push(new IteratorPosition(found));
            }
        }
    }

    @Override
    public boolean hasNext() {
        this.next = null;
        if (stack.isEmpty()) {
            return false;
        }

        IteratorPosition iteratorPosition = removeFinishedDirs(stack.peekFirst());

        if (iteratorPosition == null) {
            return false;
        }

        moveNext(iteratorPosition);

        if (this.next.isDirectory() && this.includeSubDirectories) {
            return true;
        }

        while (this.next.isDirectory() && !this.includeSubDirectories) {
            iteratorPosition = removeFinishedDirs(iteratorPosition);

            if (iteratorPosition == null) {
                return false;
            }

            moveNext(iteratorPosition);
        }

        return true;
    }

    /**
     * Remove item from stack when there is no more elements to process
     * @param iteratorPosition the current position on the stack
     * @return the item to be processed or null if the process is finished
     */
    private IteratorPosition removeFinishedDirs(IteratorPosition iteratorPosition) {
        while (iteratorPosition != null && iteratorPosition.hasNext()) {
            iteratorPosition = stack.peekFirst();

            if (iteratorPosition != null && iteratorPosition.hasNext()) {
                stack.pop();
            }
        }
        return iteratorPosition;
    }

    /**
     * Moves to the next element and if it's a directory updates the stack
     * @param iteratorPosition the current position on the stack
     */
    private void moveNext(IteratorPosition iteratorPosition) {
        this.next = iteratorPosition.next();
        if (this.next.isDirectory() && this.recursive) {
            final File[] found = this.next.listFiles((FileFilter) FileFilterUtils.or(effFileFilter, effDirFilter));

            if (found.length > 0) {
                stack.push(new IteratorPosition(found));
            }
        }
    }

    @Override
    public File next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        return this.next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Utility class representing the actual stack's position
     */
    private static class IteratorPosition {
        public File[] found;
        public int nextIndex = 0;

        /**
         * Creates a new item for the stack
         * @param found the array of files found
         */
        public IteratorPosition(File[] found) {
            this.found = found;
        }

        /**
         * Checks if the current item has more files to be processed
         * @return true if there are more items to process or false otherwise
         */
        public boolean hasNext() {
            return this.nextIndex >= this.found.length;
        }

        /**
         * Returns the next file for this item
         * @return
         */
        public File next() {
            return found[this.nextIndex++];
        }
    }
}
