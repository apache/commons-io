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

/**
 * Monitors a thread, interrupting it if it reaches the specified timeout.
 * <p>
 * This works by sleeping until the specified timeout amount and then
 * interrupting the thread being monitored. If the thread being monitored
 * completes its work before being interrupted, it should <code>interrupt()</code>
 * the <i>monitor</i> thread.
 * </p>
 *
 * <pre>
 *       long timeoutInMillis = 1000;
 *       try {
 *           Thread monitor = ThreadMonitor.start(timeoutInMillis);
 *           // do some work here
 *           ThreadMonitor.stop(monitor);
 *       } catch (InterruptedException e) {
 *           // timed amount was reached
 *       }
 * </pre>
 *
 */
class ThreadMonitor implements Runnable {

    private final Thread thread;
    private final long timeout;

    /**
     * Start monitoring the current thread.
     *
     * @param timeout The timeout amount in milliseconds
     * or no timeout if the value is zero or less
     * @return The monitor thread or {@code null}
     * if the timeout amount is not greater than zero
     */
    public static Thread start(final long timeout) {
        return start(Thread.currentThread(), timeout);
    }

    /**
     * Start monitoring the specified thread.
     *
     * @param thread The thread The thread to monitor
     * @param timeout The timeout amount in milliseconds
     * or no timeout if the value is zero or less
     * @return The monitor thread or {@code null}
     * if the timeout amount is not greater than zero
     */
    public static Thread start(final Thread thread, final long timeout) {
        Thread monitor = null;
        if (timeout > 0) {
            final ThreadMonitor timout = new ThreadMonitor(thread, timeout);
            monitor = new Thread(timout, ThreadMonitor.class.getSimpleName());
            monitor.setDaemon(true);
            monitor.start();
        }
        return monitor;
    }

    /**
     * Stop monitoring the specified thread.
     *
     * @param thread The monitor thread, may be {@code null}
     */
    public static void stop(final Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Construct and new monitor.
     *
     * @param thread The thread to monitor
     * @param timeout The timeout amount in milliseconds
     */
    private ThreadMonitor(final Thread thread, final long timeout) {
        this.thread = thread;
        this.timeout = timeout;
    }

    /**
     * Sleep until the specified timeout amount and then
     * interrupt the thread being monitored.
     *
     * @see Runnable#run()
     */
    @Override
    public void run() {
        try {
            sleep(timeout);
            thread.interrupt();
        } catch (final InterruptedException e) {
            // timeout not reached
        }
    }

    /**
     * Sleep for a guaranteed minimum number of milliseconds unless interrupted.
     *
     * This method exists because Thread.sleep(100) can sleep for 0, 70, 100 or 200ms or anything else
     * it deems appropriate. Read the docs on Thread.sleep for further interesting details.
     *
     * @param ms the number of milliseconds to sleep for
     * @throws InterruptedException if interrupted
     */
    private static void sleep(final long ms) throws InterruptedException {
        final long finishAt = System.currentTimeMillis() + ms;
        long remaining = ms;
        do {
            Thread.sleep(remaining);
            remaining = finishAt - System.currentTimeMillis();
        } while (remaining > 0);
    }


}