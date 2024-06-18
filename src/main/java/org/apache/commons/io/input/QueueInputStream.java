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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.output.QueueOutputStream;

/**
 * Simple alternative to JDK {@link PipedInputStream}; queue input stream provides what's written in queue output stream.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * QueueInputStream inputStream = new QueueInputStream();
 * QueueOutputStream outputStream = inputStream.newQueueOutputStream();
 *
 * outputStream.write("hello world".getBytes(UTF_8));
 * inputStream.read();
 * </pre>
 * <p>
 * Unlike JDK {@link PipedInputStream} and {@link PipedOutputStream}, queue input/output streams may be used safely in a single thread or multiple threads.
 * Also, unlike JDK classes, no special meaning is attached to initial or current thread. Instances can be used longer after initial threads exited.
 * </p>
 * <p>
 * Closing a {@link QueueInputStream} has no effect. The methods in this class can be called after the stream has been closed without generating an
 * {@link IOException}.
 * </p>
 *
 * @see Builder
 * @see QueueOutputStream
 * @since 2.9.0
 */
public class QueueInputStream extends InputStream {

    // @formatter:off
    /**
     * Builds a new {@link QueueInputStream}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * QueueInputStream s = QueueInputStream.builder()
     *   .setBlockingQueue(new LinkedBlockingQueue<>())
     *   .setTimeout(Duration.ZERO)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<QueueInputStream, Builder> {

        private BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>();
        private Duration timeout = Duration.ZERO;

        /**
         * Builds a new {@link QueueInputStream}.
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #setBlockingQueue(BlockingQueue)}</li>
         * <li>timeout</li>
         * </ul>
         *
         * @return a new instance.
         */
        @Override
        public QueueInputStream get() {
            return new QueueInputStream(blockingQueue, timeout);
        }

        /**
         * Sets backing queue for the stream.
         *
         * @param blockingQueue backing queue for the stream.
         * @return {@code this} instance.
         */
        public Builder setBlockingQueue(final BlockingQueue<Integer> blockingQueue) {
            this.blockingQueue = blockingQueue != null ? blockingQueue : new LinkedBlockingQueue<>();
            return this;
        }

        /**
         * Sets the polling timeout.
         *
         * @param timeout the polling timeout.
         * @return {@code this} instance.
         */
        public Builder setTimeout(final Duration timeout) {
            if (timeout != null && timeout.toNanos() < 0) {
                throw new IllegalArgumentException("timeout must not be negative");
            }
            this.timeout = timeout != null ? timeout : Duration.ZERO;
            return this;
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final BlockingQueue<Integer> blockingQueue;

    private final long timeoutNanos;

    /**
     * Constructs a new instance with no limit to its internal queue size and zero timeout.
     */
    public QueueInputStream() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Constructs a new instance with given queue and zero timeout.
     *
     * @param blockingQueue backing queue for the stream.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public QueueInputStream(final BlockingQueue<Integer> blockingQueue) {
        this(blockingQueue, Duration.ZERO);
    }

    /**
     * Constructs a new instance with given queue and timeout.
     *
     * @param blockingQueue backing queue for the stream.
     * @param timeout       how long to wait before giving up when polling the queue.
     */
    private QueueInputStream(final BlockingQueue<Integer> blockingQueue, final Duration timeout) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
        this.timeoutNanos = Objects.requireNonNull(timeout, "timeout").toNanos();
    }

    /**
     * Gets the blocking queue.
     *
     * @return the blocking queue.
     */
    BlockingQueue<Integer> getBlockingQueue() {
        return blockingQueue;
    }

    /**
     * Gets the timeout duration.
     *
     * @return the timeout duration.
     */
    Duration getTimeout() {
        return Duration.ofNanos(timeoutNanos);
    }

    /**
     * Constructs a new QueueOutputStream instance connected to this. Writes to the output stream will be visible to this input stream.
     *
     * @return QueueOutputStream connected to this stream.
     */
    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(blockingQueue);
    }

    /**
     * Reads and returns a single byte.
     *
     * @return the byte read, or {@code -1} if a timeout occurs before a queue element is available.
     * @throws IllegalStateException if thread is interrupted while waiting.
     */
    @Override
    public int read() {
        try {
            final Integer value = blockingQueue.poll(timeoutNanos, TimeUnit.NANOSECONDS);
            return value == null ? EOF : 0xFF & value;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            // throw runtime unchecked exception to maintain signature backward-compatibility of
            // this read method, which does not declare IOException
            throw new IllegalStateException(e);
        }
    }

}
