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

import java.time.Duration;

/**
 * Monitors a thread, interrupting it if it reaches the specified timeout.
 * <p>
 * This works by sleeping until the specified timeout amount and then interrupting the thread being monitored. If the
 * thread being monitored completes its work before being interrupted, it should {@code interrupt()} the <em>monitor</em>
 * thread.
 * </p>
 *
 * <pre>
 * Duration timeout = Duration.ofSeconds(1);
 * try {
 *     Thread monitor = ThreadMonitor.start(timeout);
 *     // do some work here
 *     ThreadMonitor.stop(monitor);
 * } catch (InterruptedException e) {
 *     // timed amount was reached
 * }
 * </pre>
 */
final class ThreadMonitor implements Runnable {

    /**
     * Starts monitoring the current thread.
     *
     * @param timeout The timeout amount. or no timeout if the value is zero or less.
     * @return The monitor thread or {@code null} if the timeout amount is not greater than zero.
     */
    static Thread start(final Duration timeout) {
        return start(Thread.currentThread(), timeout);
    }

    /**
     * Starts monitoring the specified thread.
     *
     * @param thread The thread to monitor
     * @param timeout The timeout amount. or no timeout if the value is zero or less.
     * @return The monitor thread or {@code null} if the timeout amount is not greater than zero.
     */
    static Thread start(final Thread thread, final Duration timeout) {
        if (timeout.isZero() || timeout.isNegative()) {
            return null;
        }
        final Thread monitor = new Thread(new ThreadMonitor(thread, timeout), ThreadMonitor.class.getSimpleName());
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
    }

    /**
     * Stops monitoring the specified thread.
     *
     * @param thread The monitor thread, may be {@code null}.
     */
    static void stop(final Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private final Thread thread;

    private final Duration timeout;

    /**
     * Constructs a new monitor.
     *
     * @param thread The thread to monitor.
     * @param timeout The timeout amount.
     */
    private ThreadMonitor(final Thread thread, final Duration timeout) {
        this.thread = thread;
        this.timeout = timeout;
    }

    /**
     * Sleeps until the specified timeout amount and then interrupt the thread being monitored.
     *
     * @see Runnable#run()
     */
    @Override
    public void run() {
        try {
            ThreadUtils.sleep(timeout);
            thread.interrupt();
        } catch (final InterruptedException ignored) {
            // timeout not reached
        }
    }
}
