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

package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Provides bandwidth throttling on a specified InputStream. It is implemented as a wrapper on top of another InputStream instance. The throttling works by
 * examining the number of bytes read from the underlying InputStream from the beginning, and sleep()ing for a time interval if the byte-transfer is found
 * exceed the specified tolerable maximum. (Thus, while the read-rate might exceed the maximum for a given short interval, the average tends towards the
 * specified maximum, overall.)
 * <p>
 * Inspired by Apache HBase's class of the same name.
 * </p>
 *
 * @since 2.16.0
 */
public final class ThrottledInputStream extends CountingInputStream {

    /**
     * Builds a new {@link QueueInputStream} instance.
     * <h2>Using NIO</h2>
     *
     * <pre>{@code
     * ThrottledInputStream s = ThrottledInputStream.builder().setPath(Paths.get("MyFile.xml")).setMaxBytesPerSecond(100_000).get();
     * }
     * </pre>
     *
     * <h2>Using IO</h2>
     *
     * <pre>{@code
     * ThrottledInputStream s = ThrottledInputStream.builder().setFile(new File("MyFile.xml")).setMaxBytesPerSecond(100_000).get();
     * }
     * </pre>
     *
     * <pre>{@code
     * ThrottledInputStream s = ThrottledInputStream.builder().setInputStream(inputStream).setMaxBytesPerSecond(100_000).get();
     * }
     * </pre>
     */
    public static class Builder extends AbstractStreamBuilder<ThrottledInputStream, Builder> {

        /**
         * Effectively not throttled.
         */
        private long maxBytesPerSecond = Long.MAX_VALUE;

        @SuppressWarnings("resource")
        @Override
        public ThrottledInputStream get() throws IOException {
            return new ThrottledInputStream(getInputStream(), maxBytesPerSecond);
        }

        /**
         * Sets the maximum bytes per second.
         *
         * @param maxBytesPerSecond the maximum bytes per second.
         */
        public void setMaxBytesPerSecond(final long maxBytesPerSecond) {
            this.maxBytesPerSecond = maxBytesPerSecond;
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    static long toSleepMillis(final long bytesRead, final long maxBytesPerSec, final long elapsedMillis) {
        assert elapsedMillis >= 0 : "The elapsed time should be greater or equal to zero";
        if (bytesRead <= 0 || maxBytesPerSec <= 0 || elapsedMillis == 0) {
            return 0;
        }
        // We use this class to load the single source file, so the bytesRead
        // and maxBytesPerSec aren't greater than Double.MAX_VALUE.
        // We can get the precise sleep time by using the double value.
        final long millis = (long) ((double) bytesRead / (double) maxBytesPerSec * 1000 - elapsedMillis);
        if (millis <= 0) {
            return 0;
        }
        return millis;
    }

    private final long maxBytesPerSecond;
    private final long startTime = System.currentTimeMillis();
    private Duration totalSleepDuration = Duration.ZERO;

    private ThrottledInputStream(final InputStream proxy, final long maxBytesPerSecond) {
        super(proxy);
        assert maxBytesPerSecond > 0 : "Bandwidth " + maxBytesPerSecond + " is invalid.";
        this.maxBytesPerSecond = maxBytesPerSecond;
    }

    /**
     * Gets the read-rate from this stream, since creation. Calculated as bytesRead/elapsedTimeSinceStart.
     *
     * @return Read rate, in bytes/sec.
     */
    public long getBytesPerSecond() {
        final long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSeconds == 0) {
            return getByteCount();
        }
        return getByteCount() / elapsedSeconds;
    }

    private long getSleepMillis() {
        return toSleepMillis(getByteCount(), maxBytesPerSecond, System.currentTimeMillis() - startTime);
    }

    /**
     * Gets the total duration spent in sleep.
     *
     * @return Duration spent in sleep.
     */
    public Duration getTotalSleepDuration() {
        return totalSleepDuration;
    }

    @Override
    protected void beforeRead(final int n) throws IOException {
        throttle();
    }

    private void throttle() throws InterruptedIOException {
        final long sleepMillis = getSleepMillis();
        if (sleepMillis > 0) {
            totalSleepDuration = totalSleepDuration.plus(sleepMillis, ChronoUnit.MILLIS);
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            } catch (final InterruptedException e) {
                throw new InterruptedIOException("Thread aborted");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ThrottledInputStream[bytesRead=" + getCount() + ", maxBytesPerSec=" + maxBytesPerSecond + ", bytesPerSec=" + getBytesPerSecond()
                + ", totalSleepDuration=" + totalSleepDuration + ']';
    }
}
