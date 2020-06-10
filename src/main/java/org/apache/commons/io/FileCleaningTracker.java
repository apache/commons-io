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
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Keeps track of files awaiting deletion, and deletes them when an associated
 * marker object is reclaimed by the garbage collector.
 * <p>
 * This utility creates a background thread to handle file deletion.
 * Each file to be deleted is registered with a handler object.
 * When the handler object is garbage collected, the file is deleted.
 * <p>
 * In an environment with multiple class loaders (a servlet container, for
 * example), you should consider stopping the background thread if it is no
 * longer needed. This is done by invoking the method
 * {@link #exitWhenFinished}, typically in
 * {@code javax.servlet.ServletContextListener.contextDestroyed(javax.servlet.ServletContextEvent)} or similar.
 *
 */
public class FileCleaningTracker {

    // Note: fields are package protected to allow use by test cases

    /**
     * Queue of <code>Tracker</code> instances being watched.
     */
    ReferenceQueue<Object> q = new ReferenceQueue<>();
    /**
     * Collection of <code>Tracker</code> instances in existence.
     */
    final Collection<Tracker> trackers = Collections.synchronizedSet(new HashSet<Tracker>()); // synchronized
    /**
     * Collection of File paths that failed to delete.
     */
    final List<String> deleteFailures = Collections.synchronizedList(new ArrayList<String>());
    /**
     * Whether to terminate the thread when the tracking is complete.
     */
    volatile boolean exitWhenFinished = false;
    /**
     * The thread that will clean up registered files.
     */
    Thread reaper;

    //-----------------------------------------------------------------------
    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The {@link FileDeleteStrategy#NORMAL normal} deletion strategy will be used.
     *
     * @param file  the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @throws NullPointerException if the file is null
     */
    public void track(final File file, final Object marker) {
        track(file, marker, null);
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The specified deletion strategy is used.
     *
     * @param file  the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     * @throws NullPointerException if the file is null
     */
    public void track(final File file, final Object marker, final FileDeleteStrategy deleteStrategy) {
        Objects.requireNonNull(file, "file");
        addTracker(file.getPath(), marker, deleteStrategy);
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The {@link FileDeleteStrategy#NORMAL normal} deletion strategy will be used.
     *
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @throws NullPointerException if the path is null
     */
    public void track(final String path, final Object marker) {
        track(path, marker, null);
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The specified deletion strategy is used.
     *
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     * @throws NullPointerException if the path is null
     */
    public void track(final String path, final Object marker, final FileDeleteStrategy deleteStrategy) {
        Objects.requireNonNull(path, "path");
        addTracker(path, marker, deleteStrategy);
    }

    /**
     * Adds a tracker to the list of trackers.
     *
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     */
    private synchronized void addTracker(final String path, final Object marker, final FileDeleteStrategy
            deleteStrategy) {
        // synchronized block protects reaper
        if (exitWhenFinished) {
            throw new IllegalStateException("No new trackers can be added once exitWhenFinished() is called");
        }
        if (reaper == null) {
            reaper = new Reaper();
            reaper.start();
        }
        trackers.add(new Tracker(path, deleteStrategy, marker, q));
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve the number of files currently being tracked, and therefore
     * awaiting deletion.
     *
     * @return the number of files being tracked
     */
    public int getTrackCount() {
        return trackers.size();
    }

    /**
     * Return the file paths that failed to delete.
     *
     * @return the file paths that failed to delete
     * @since 2.0
     */
    public List<String> getDeleteFailures() {
        return deleteFailures;
    }

    /**
     * Call this method to cause the file cleaner thread to terminate when
     * there are no more objects being tracked for deletion.
     * <p>
     * In a simple environment, you don't need this method as the file cleaner
     * thread will simply exit when the JVM exits. In a more complex environment,
     * with multiple class loaders (such as an application server), you should be
     * aware that the file cleaner thread will continue running even if the class
     * loader it was started from terminates. This can constitute a memory leak.
     * <p>
     * For example, suppose that you have developed a web application, which
     * contains the commons-io jar file in your WEB-INF/lib directory. In other
     * words, the FileCleaner class is loaded through the class loader of your
     * web application. If the web application is terminated, but the servlet
     * container is still running, then the file cleaner thread will still exist,
     * posing a memory leak.
     * <p>
     * This method allows the thread to be terminated. Simply call this method
     * in the resource cleanup code, such as
     * {@code javax.servlet.ServletContextListener.contextDestroyed(javax.servlet.ServletContextEvent)}.
     * Once called, no new objects can be tracked by the file cleaner.
     */
    public synchronized void exitWhenFinished() {
        // synchronized block protects reaper
        exitWhenFinished = true;
        if (reaper != null) {
            synchronized (reaper) {
                reaper.interrupt();
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * The reaper thread.
     */
    private final class Reaper extends Thread {
        /** Construct a new Reaper */
        Reaper() {
            super("File Reaper");
            setPriority(Thread.MAX_PRIORITY);
            setDaemon(true);
        }

        /**
         * Run the reaper thread that will delete files as their associated
         * marker objects are reclaimed by the garbage collector.
         */
        @Override
        public void run() {
            // thread exits when exitWhenFinished is true and there are no more tracked objects
            while (exitWhenFinished == false || trackers.size() > 0) {
                try {
                    // Wait for a tracker to remove.
                    final Tracker tracker = (Tracker) q.remove(); // cannot return null
                    trackers.remove(tracker);
                    if (!tracker.delete()) {
                        deleteFailures.add(tracker.getPath());
                    }
                    tracker.clear();
                } catch (final InterruptedException e) {
                    continue;
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Inner class which acts as the reference for a file pending deletion.
     */
    private static final class Tracker extends PhantomReference<Object> {

        /**
         * The full path to the file being tracked.
         */
        private final String path;
        /**
         * The strategy for deleting files.
         */
        private final FileDeleteStrategy deleteStrategy;

        /**
         * Constructs an instance of this class from the supplied parameters.
         *
         * @param path  the full path to the file to be tracked, not null
         * @param deleteStrategy  the strategy to delete the file, null means normal
         * @param marker  the marker object used to track the file, not null
         * @param queue  the queue on to which the tracker will be pushed, not null
         */
        Tracker(final String path, final FileDeleteStrategy deleteStrategy, final Object marker,
                final ReferenceQueue<? super Object> queue) {
            super(marker, queue);
            this.path = path;
            this.deleteStrategy = deleteStrategy == null ? FileDeleteStrategy.NORMAL : deleteStrategy;
        }

        /**
         * Return the path.
         *
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * Deletes the file associated with this tracker instance.
         *
         * @return {@code true} if the file was deleted successfully;
         *         {@code false} otherwise.
         */
        public boolean delete() {
            return deleteStrategy.deleteQuietly(new File(path));
        }
    }

}
