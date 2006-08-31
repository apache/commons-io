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
import java.util.Collection;
import java.util.Vector;

/**
 * Keeps track of files awaiting deletion, and deletes them when an associated
 * marker object is reclaimed by the garbage collector.
 * <p>
 * This utility creates a background thread to handle file deletion.
 * Each file to be deleted is registered with a handler object.
 * When the handler object is garbage collected, the file is deleted.
 *
 * @author Noel Bergman
 * @author Martin Cooper
 * @version $Id$
 */
public class FileCleaner {

    /**
     * Queue of <code>Tracker</code> instances being watched.
     */
    private static ReferenceQueue /* Tracker */ q = new ReferenceQueue();

    /**
     * Collection of <code>Tracker</code> instances in existence.
     */
    private static Collection /* Tracker */ trackers = new Vector();

    /**
     * The thread that will clean up registered files.
     */
    private static Thread reaper = new Thread("File Reaper") {

        /**
         * Run the reaper thread that will delete files as their associated
         * marker objects are reclaimed by the garbage collector.
         */
        public void run() {
            for (;;) {
                Tracker tracker = null;
                try {
                    // Wait for a tracker to remove.
                    tracker = (Tracker) q.remove();
                } catch (Exception e) {
                    continue;
                }

                tracker.delete();
                tracker.clear();
                trackers.remove(tracker);
            }
        }
    };

    /**
     * The static initializer that starts the reaper thread.
     */
    static {
        reaper.setPriority(Thread.MAX_PRIORITY);
        reaper.setDaemon(true);
        reaper.start();
    }

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
    public static void track(File file, Object marker) {
        track(file, marker, (FileDeleteStrategy) null);
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The speified deletion strategy is used.
     *
     * @param file  the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     * @throws NullPointerException if the file is null
     */
    public static void track(File file, Object marker, FileDeleteStrategy deleteStrategy) {
        if (file == null) {
            throw new NullPointerException("The file must not be null");
        }
        trackers.add(new Tracker(file.getPath(), deleteStrategy, marker, q));
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
    public static void track(String path, Object marker) {
        track(path, marker, (FileDeleteStrategy) null);
    }

    /**
     * Track the specified file, using the provided marker, deleting the file
     * when the marker instance is garbage collected.
     * The speified deletion strategy is used.
     *
     * @param path  the full path to the file to be tracked, not null
     * @param marker  the marker object used to track the file, not null
     * @param deleteStrategy  the strategy to delete the file, null means normal
     * @throws NullPointerException if the path is null
     */
    public static void track(String path, Object marker, FileDeleteStrategy deleteStrategy) {
        if (path == null) {
            throw new NullPointerException("The path must not be null");
        }
        trackers.add(new Tracker(path, deleteStrategy, marker, q));
    }

    /**
     * Retrieve the number of files currently being tracked, and therefore
     * awaiting deletion.
     *
     * @return the number of files being tracked
     */
    public static int getTrackCount() {
        return trackers.size();
    }

    //-----------------------------------------------------------------------
    /**
     * Inner class which acts as the reference for a file pending deletion.
     */
    static class Tracker extends PhantomReference {

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
        Tracker(String path, FileDeleteStrategy deleteStrategy, Object marker, ReferenceQueue queue) {
            super(marker, queue);
            this.path = path;
            this.deleteStrategy = (deleteStrategy == null ? FileDeleteStrategy.NORMAL : deleteStrategy);
        }

        /**
         * Deletes the file associated with this tracker instance.
         *
         * @return <code>true</code> if the file was deleted successfully;
         *         <code>false</code> otherwise.
         */
        public boolean delete() {
            return deleteStrategy.deleteQuietly(new File(path));
        }
    }

}
