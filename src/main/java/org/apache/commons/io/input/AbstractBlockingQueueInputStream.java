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
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.output.QueueOutputStream;

/**
 * Alternative to {@link java.io.PipedInputStream}, where this queue input stream provides what's written in a queue output stream.
 * <p>
 * Example usage, see {@link PollingQueueInputStream} and {@link TakingQueueInputStream}.
 * </p>
 *
 * @see PollingQueueInputStream
 * @see TakingQueueInputStream
 * @since 2.12.0
 */
public abstract class AbstractBlockingQueueInputStream extends InputStream {

    /**
     * Simple alternative to JDK {@link java.io.PipedInputStream}; queue input stream provides what's written in queue output stream.
     *
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * BlockingQueueInputStream inputStream = new BlockingQueueInputStream();
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
     * Closing a {@link PollingQueueInputStream} has no effect. The methods in this class can be called after the stream has been closed without generating an
     * {@link IOException}.
     * </p>
     *
     * @see QueueOutputStream
     * @since 2.12.0
     */
    public static class PollingQueueInputStream extends AbstractBlockingQueueInputStream {

        /**
         * Constructs a new instance.
         */
        public PollingQueueInputStream() {
        }

        /**
         * Constructs a new instance with given queue.
         *
         * @param blockingQueue backing queue for the stream.
         */
        public PollingQueueInputStream(final BlockingQueue<Integer> blockingQueue) {
            super(blockingQueue);
        }

        @Override
        public int read() throws IOException {
            final Integer value = getBlockingQueue().poll();
            return value == null ? EOF : 0xFF & value;
        }
    }

    /**
     * Simple alternative to JDK {@link java.io.PipedInputStream}; queue input stream provides what's written in queue output stream.
     *
     * <p>
     * Example usage:
     * </p>
     *
     * <pre>
     * TakingQueueInputStream inputStream = new TakingQueueInputStream();
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
     * Closing a {@link TakingQueueInputStream} has no effect. The methods in this class can be called after the stream has been closed without generating an
     * {@link IOException}.
     * </p>
     *
     * @see QueueOutputStream
     * @since 2.12.0
     */
    public static class TakingQueueInputStream extends AbstractBlockingQueueInputStream {

        /**
         * Constructs a new instance.
         */
        public TakingQueueInputStream() {
        }

        /**
         * Constructs a new instance with given queue.
         *
         * @param blockingQueue backing queue for the stream.
         */
        public TakingQueueInputStream(final BlockingQueue<Integer> blockingQueue) {
            super(blockingQueue);
        }

        /**
         * Reads and returns a single byte.
         *
         * @return either the byte read or {@code -1} if the end of the stream has been reached
         * @throws InterruptedIOException if interrupted while waiting to take.
         */
        @Override
        public int read() throws InterruptedIOException {
            try {
                return getBlockingQueue().take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                final InterruptedIOException ioException = new InterruptedIOException();
                ioException.initCause(e);
                throw ioException;
            }
        }
    }

    private final BlockingQueue<Integer> blockingQueue;

    /**
     * Constructs a new instance with no limit to its internal buffer size.
     */
    protected AbstractBlockingQueueInputStream() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Constructs a new instance with given buffer
     *
     * @param blockingQueue backing queue for the stream
     */
    protected AbstractBlockingQueueInputStream(final BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    /**
     * Gets the underlying BlockingQueue.
     *
     * @return the underlying BlockingQueue.
     */
    protected BlockingQueue<Integer> getBlockingQueue() {
        return blockingQueue;
    }

    /**
     * Creates a new QueueOutputStream instance connected to this. Writes to the output stream will be visible to this input stream.
     *
     * @return QueueOutputStream connected to this stream
     */
    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(blockingQueue);
    }

}
