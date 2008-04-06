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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A runnable that spawns a monitoring thread triggering any
 * registered {@link FilesystemObserver} at a specified interval.
 * 
 * @see FilesystemObserver
 * @see FilesystemTimerTask
 * @version $Id$
 * @since Commons IO 2.0
 */
public final class FilesystemMonitor implements Runnable {

    private final long interval;
    private final List<FilesystemObserver> observers = new CopyOnWriteArrayList<FilesystemObserver>();
    private Thread thread = null;
    private volatile boolean running = false;

    /**
     * Construct a monitor with a default interval of 10 seconds.
     */
    public FilesystemMonitor() {
        this(10000);
    }

    /**
     * Construct a monitor with the specified interval.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     */
    public FilesystemMonitor(long interval) {
        this.interval = interval;
    }

    /**
     * Construct a monitor with the specified interval and set of observers.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     * @param observers The set of observers to add to the monitor.
     */
    public FilesystemMonitor(long interval, FilesystemObserver... observers) {
        this(interval);
        if (observers != null) {
            for (int i = 0; i < observers.length; i++) {
                addObserver(observers[i]);
            }
        }
    }

    /**
     * Add a file system observer to this monitor.
     *
     * @param observer The file system observer to add
     */
    public void addObserver(final FilesystemObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * Remove a file system observer from this monitor.
     *
     * @param observer The file system observer to remove
     */
    public void removeObserver(final FilesystemObserver observer) {
        if (observer != null) {
            while (observers.remove(observer)) {
            }
        }
    }

    /**
     * Returns the set of {@link FilesystemObserver} registered with
     * this monitor. 
     *
     * @return The set of {@link FilesystemObserver}
     */
    public Iterable<FilesystemObserver> getObservers() {
        return observers;
    }

    /**
     * Start monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public void start() throws Exception {
        for (FilesystemObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Stop monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public void stop() throws Exception {
        running = false;
        try {
            thread.join(interval);
        } catch (InterruptedException e) {
        }
        for (FilesystemObserver observer : observers) {
            observer.destroy();
        }
    }

    /**
     * Run.
     */
    public void run() {
        while (true) {
            if (!running) {
                break;
            }
            for (FilesystemObserver observer : observers) {
                observer.checkAndNotify();
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException e) {
            }
        }
    }
}
