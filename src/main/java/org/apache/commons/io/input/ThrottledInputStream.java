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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Provides bandwidth throttling on an InputStream as a filter input stream. The throttling examines the number of bytes read from the underlying InputStream,
 * and sleeps for a time interval if the byte-transfer is found to exceed the specified maximum rate. Thus, while the read-rate might exceed the maximum for a
 * short interval, the average tends towards the specified maximum, overall.
 * <p>
 * To build an instance, call {@link #builder()}.
 * </p>
 * <p>
 * Inspired by Apache HBase's class of the same name.
 * </p>
 *
 * @see Builder
 * @since 2.16.0
 */
public final class ThrottledInputStream extends CountingInputStream {

    // @formatter:off
    /**
     * Builds a new {@link ThrottledInputStream}.
     *
     * <h2>Using NIO</h2>
     * <pre>{@code
     * ThrottledInputStream in = ThrottledInputStream.builder()
     *   .setPath(Paths.get("MyFile.xml"))
     *   .setMaxBytes(100_000, ChronoUnit.SECONDS)
     *   .get();
     * }
     * </pre>
     * <h2>Using IO</h2>
     * <pre>{@code
     * ThrottledInputStream in = ThrottledInputStream.builder()
     *   .setFile(new File("MyFile.xml"))
     *   .setMaxBytes(100_000, ChronoUnit.SECONDS)
     *   .get();
     * }
     * </pre>
     * <pre>{@code
     * ThrottledInputStream in = ThrottledInputStream.builder()
     *   .setInputStream(inputStream)
     *   .setMaxBytes(100_000, ChronoUnit.SECONDS)
     *   .get();
     * }
     * </pre>
     *
     * @see #get()
     */
    // @formatter:on
    public static class Builder extends AbstractBuilder<ThrottledInputStream, Builder> {

        /**
         * Effectively not throttled.
         */
        private double maxBytesPerSecond = Double.MAX_VALUE;

        /**
         * Constructs a new builder of {@link ThrottledInputStream}.
         */
        public Builder() {
            // empty
        }

        /**
         * Builds a new {@link ThrottledInputStream}.
         * <p>
         * You must set an aspect that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()} gets the target aspect.</li>
         * <li>maxBytesPerSecond</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link InputStream} using {@link #getInputStream()}.
         * @see #getInputStream()
         * @see #getUnchecked()
         */
        @Override
        public ThrottledInputStream get() throws IOException {
            return new ThrottledInputStream(this);
        }

        // package private for testing.
        double getMaxBytesPerSecond() {
            return maxBytesPerSecond;
        }

        /**
         * Sets the maximum bytes per time period unit.
         * <p>
         * For example, to throttle reading to 100K per second, use:
         * </p>
         * <pre>
         * builder.setMaxBytes(100_000, ChronoUnit.SECONDS)
         * </pre>
         * <p>
         * To test idle timeouts for example, use 1 byte per minute, 1 byte per 30 seconds, and so on.
         * </p>
         *
         * @param value the maximum bytes
         * @param chronoUnit a duration scale goal.
         * @return this instance.
         * @throws IllegalArgumentException Thrown if maxBytesPerSecond &lt;= 0.
         * @since 2.19.0
         */
        public Builder setMaxBytes(final long value, final ChronoUnit chronoUnit) {
            setMaxBytes(value, chronoUnit.getDuration());
            return asThis();
        }

        /**
         * Sets the maximum bytes per duration.
         * <p>
         * For example, to throttle reading to 100K per second, use:
         * </p>
         * <pre>
         * builder.setMaxBytes(100_000, Duration.ofSeconds(1))
         * </pre>
         * <p>
         * To test idle timeouts for example, use 1 byte per minute, 1 byte per 30 seconds, and so on.
         * </p>
         *
         * @param value the maximum bytes
         * @param duration a duration goal.
         * @return this instance.
         * @throws IllegalArgumentException Thrown if maxBytesPerSecond &lt;= 0.
         */
        // Consider making public in the future
        Builder setMaxBytes(final long value, final Duration duration) {
            setMaxBytesPerSecond((double) Objects.requireNonNull(duration, "duration").toMillis() / 1_000 * value);
            return asThis();
        }

        /**
         * Sets the maximum bytes per second.
         *
         * @param maxBytesPerSecond the maximum bytes per second.
         * @return this instance.
         * @throws IllegalArgumentException Thrown if maxBytesPerSecond &lt;= 0.
         */
        private Builder setMaxBytesPerSecond(final double maxBytesPerSecond) {
            if (maxBytesPerSecond <= 0) {
                throw new IllegalArgumentException("Bandwidth " + maxBytesPerSecond + " must be > 0.");
            }
            this.maxBytesPerSecond = maxBytesPerSecond;
            return asThis();
        }

        /**
         * Sets the maximum bytes per second.
         *
         * @param maxBytesPerSecond the maximum bytes per second.
         * @throws IllegalArgumentException Thrown if maxBytesPerSecond &lt;= 0.
         */
        public void setMaxBytesPerSecond(final long maxBytesPerSecond) {
            setMaxBytesPerSecond((double) maxBytesPerSecond);
            // TODO 3.0
            // return asThis();
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

    // package private for testing
    static long toSleepMillis(final long bytesRead, final long elapsedMillis, final double maxBytesPerSec) {
        if (bytesRead <= 0 || maxBytesPerSec <= 0 || elapsedMillis == 0) {
            return 0;
        }
        // We use this class to load the single source file, so the bytesRead
        // and maxBytesPerSec aren't greater than Double.MAX_VALUE.
        // We can get the precise sleep time by using the double value.
        final long millis = (long) (bytesRead / maxBytesPerSec * 1000 - elapsedMillis);
        if (millis <= 0) {
            return 0;
        }
        return millis;
    }

    private final double maxBytesPerSecond;
    private final long startTime = System.currentTimeMillis();
    private Duration totalSleepDuration = Duration.ZERO;

    private ThrottledInputStream(final Builder builder) throws IOException {
        super(builder);
        if (builder.maxBytesPerSecond <= 0) {
            throw new IllegalArgumentException("Bandwidth " + builder.maxBytesPerSecond + " is invalid.");
        }
        this.maxBytesPerSecond = builder.maxBytesPerSecond;
    }

    @Override
    protected void beforeRead(final int n) throws IOException {
        throttle();
    }

    /**
     * Gets the read-rate from this stream, since creation. Calculated as bytesRead/elapsedTimeSinceStart.
     *
     * @return Read rate, in bytes/sec.
     */
    private long getBytesPerSecond() {
        final long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSeconds == 0) {
            return getByteCount();
        }
        return getByteCount() / elapsedSeconds;
    }

    // package private for testing.
    double getMaxBytesPerSecond() {
        return maxBytesPerSecond;
    }

    private long getSleepMillis() {
        return toSleepMillis(getByteCount(), System.currentTimeMillis() - startTime, maxBytesPerSecond);
    }

    /**
     * Gets the total duration spent in sleep.
     *
     * @return Duration spent in sleep.
     */
    // package private for testing
    Duration getTotalSleepDuration() {
        return totalSleepDuration;
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
        return "ThrottledInputStream[bytesRead=" + getByteCount() + ", maxBytesPerSec=" + maxBytesPerSecond + ", bytesPerSec=" + getBytesPerSecond()
                + ", totalSleepDuration=" + totalSleepDuration + ']';
    }
}
