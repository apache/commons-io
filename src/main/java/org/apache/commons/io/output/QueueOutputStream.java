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
package org.apache.commons.io.output;

import org.apache.commons.io.input.QueueInputStream;

import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple alternative to JDK {@link java.io.PipedOutputStream}; queue input stream provides what's written in queue
 * output stream.
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * QueueOutputStream outputStream = new QueueOutputStream();
 * QueueInputStream inputStream = outputStream.newPipeInputStream();
 * 
 * outputStream.write("hello world".getBytes(UTF_8));
 * inputStream.read();
 * </pre>
 * 
 * Unlike JDK {@link PipedInputStream} and {@link PipedOutputStream}, queue input/output streams may be used safely in a
 * single thread or multiple threads. Also, unlike JDK classes, no special meaning is attached to initial or current
 * thread. Instances can be used longer after initial threads exited.
 * <p>
 * Closing a {@code QueueOutputStream} has no effect. The methods in this class can be called after the stream has been
 * closed without generating an {@code IOException}.
 * </p>
 * 
 * @see QueueInputStream
 * @since 2.9.0
 */
public class QueueOutputStream extends OutputStream {

    private final BlockingQueue<Integer> blockingQueue;

    /**
     * Constructs a new instance with no limit to internal buffer size.
     */
    public QueueOutputStream() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Constructs a new instance with given buffer.
     * 
     * @param blockingQueue backing queue for the stream
     */
    public QueueOutputStream(final BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    /**
     * Creates a new QueueInputStream instance connected to this. Writes to this output stream will be visible to the
     * input stream.
     * 
     * @return QueueInputStream connected to this stream
     */
    public QueueInputStream newQueueInputStream() {
        return new QueueInputStream(blockingQueue);
    }

    /**
     * Writes a single byte.
     *
     * @throws InterruptedIOException if the thread is interrupted while writing to the queue.
     */
    @Override
    public void write(final int b) throws InterruptedIOException {
        try {
            blockingQueue.put(0xFF & b);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            final InterruptedIOException interruptedIoException = new InterruptedIOException();
            interruptedIoException.initCause(e);
            throw interruptedIoException;
        }
    }
}
