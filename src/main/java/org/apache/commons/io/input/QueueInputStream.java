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
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.output.QueueOutputStream;

/**
 * Simple alternative to JDK {@link java.io.PipedInputStream}; queue input stream provides what's written in queue
 * output stream.
 *
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
 * Unlike JDK {@link PipedInputStream} and {@link PipedOutputStream}, queue input/output streams may be used safely in a
 * single thread or multiple threads. Also, unlike JDK classes, no special meaning is attached to initial or current
 * thread. Instances can be used longer after initial threads exited.
 * </p>
 * <p>
 * Closing a {@link QueueInputStream} has no effect. The methods in this class can be called after the stream has been
 * closed without generating an {@link IOException}.
 * </p>
 *
 * @see QueueOutputStream
 * @since 2.9.0
 */
public class QueueInputStream extends InputStream {

    final BlockingQueue<Integer> blockingQueue;

    /**
     * Constructs a new instance with no limit to its internal queue size
     */
    public QueueInputStream() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Constructs a new instance with given queue
     *
     * @param blockingQueue backing queue for the stream
     */
    public QueueInputStream(final BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    /**
     * Creates a blocking QueueOutputStream with no limit to its internal queue size. Its {@link #read()} method
     * will block or wait if necessary until a queue element becomes available.
     *
     * @return blocking QueueOutputStream
     */
    public static QueueInputStream createBlockingQueueInputStream() {
        return createBlockingQueueInputStream(new LinkedBlockingQueue<>());
    }

    /**
     * Creates a new QueueOutputStream with given queue. Its {@link #read()} method will block or wait if
     * necessary until a queue element becomes available.
     *
     * @param blockingQueue backing queue for the stream
     * @return blocking QueueOutputStream
     */
    public static QueueInputStream createBlockingQueueInputStream(final BlockingQueue<Integer> blockingQueue) {
        return new BlockingQueueInputStream(blockingQueue);
    }

    /**
     * Creates a new QueueOutputStream instance connected to this. Writes to the output stream will be visible to this
     * input stream.
     *
     * @return QueueOutputStream connected to this stream
     */
    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(blockingQueue);
    }

    /**
     * Reads and returns a single byte.
     *
     * @return either the byte read or {@code -1} immediately if the queue is empty.
     */
    @Override
    public int read() {
        final Integer value = blockingQueue.poll();
        return value == null ? EOF : 0xFF & value;
    }

    private static class BlockingQueueInputStream extends QueueInputStream {

        public BlockingQueueInputStream(final BlockingQueue<Integer> blockingQueue) {
            super(blockingQueue);
        }

        @Override
        public int read() {
            try {
                return blockingQueue.take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                final InterruptedIOException ioException = new InterruptedIOException();
                ioException.initCause(e);
                // throw runtime unchecked exception to maintain signature backward-compatibilty of
                // super class' read method, which does not declare any exception
                throw new UncheckedIOException(ioException);
            }
        }
    }
}
