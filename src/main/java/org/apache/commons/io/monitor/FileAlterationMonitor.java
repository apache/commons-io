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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * A runnable that spawns a monitoring thread triggering any
 * registered {@link FileAlterationObserver} at a specified interval.
 *
 * @see FileAlterationObserver
 * @since 2.0
 */
public final class FileAlterationMonitor implements Runnable {

    private static final FileAlterationObserver[] EMPTY_ARRAY = {};

    private final long interval;
    private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<>();
    private Thread thread;
    private ThreadFactory threadFactory;
    private volatile boolean running;

    /**
     * Constructs a monitor with a default interval of 10 seconds.
     */
    public FileAlterationMonitor() {
        this(10000);
    }

    /**
     * Constructs a monitor with the specified interval.
     *
     * @param interval The amount of time in milliseconds to wait between
     * checks of the file system.
     */
    public FileAlterationMonitor(final long interval) {
        this.interval = interval;
    }

    /**
     * Constructs a monitor with the specified interval and collection of observers.
     *
     * @param interval The amount of time in milliseconds to wait between
     * checks of the file system.
     * @param observers The collection of observers to add to the monitor.
     * @since 2.9.0
     */
    public FileAlterationMonitor(final long interval, final Collection<FileAlterationObserver> observers) {
        // @formatter:off
        this(interval,
            Optional
                .ofNullable(observers)
                .orElse(Collections.emptyList())
                .toArray(EMPTY_ARRAY)
        );
        // @formatter:on
    }

    /**
     * Constructs a monitor with the specified interval and set of observers.
     *
     * @param interval The amount of time in milliseconds to wait between
     * checks of the file system.
     * @param observers The set of observers to add to the monitor.
     */
    public FileAlterationMonitor(final long interval, final FileAlterationObserver... observers) {
        this(interval);
        if (observers != null) {
            for (final FileAlterationObserver observer : observers) {
                addObserver(observer);
            }
        }
    }

    /**
     * Returns the interval.
     *
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Sets the thread factory.
     *
     * @param threadFactory the thread factory
     */
    public synchronized void setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /**
     * Adds a file system observer to this monitor.
     *
     * @param observer The file system observer to add
     */
    public void addObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * Removes a file system observer from this monitor.
     *
     * @param observer The file system observer to remove
     */
    public void removeObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            while (observers.remove(observer)) {
                // empty
            }
        }
    }

    /**
     * Returns the set of {@link FileAlterationObserver} registered with
     * this monitor.
     *
     * @return The set of {@link FileAlterationObserver}
     */
    public Iterable<FileAlterationObserver> getObservers() {
        return observers;
    }

    /**
     * Starts monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        for (final FileAlterationObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        if (threadFactory != null) {
            thread = threadFactory.newThread(this);
        } else {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * Stops monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void stop() throws Exception {
        stop(interval);
    }

    /**
     * Stops monitoring.
     *
     * @param stopInterval the amount of time in milliseconds to wait for the thread to finish.
     * A value of zero will wait until the thread is finished (see {@link Thread#join(long)}).
     * @throws Exception if an error occurs initializing the observer
     * @since 2.1
     */
    public synchronized void stop(final long stopInterval) throws Exception {
        if (!running) {
            throw new IllegalStateException("Monitor is not running");
        }
        running = false;
        try {
            thread.interrupt();
            thread.join(stopInterval);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (final FileAlterationObserver observer : observers) {
            observer.destroy();
        }
    }

    /**
     * Runs this monitor.
     */
    @Override
    public void run() {
        while (running) {
            for (final FileAlterationObserver observer : observers) {
                observer.checkAndNotify();
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException ignored) {
                // ignore
            }
        }
    }
}
