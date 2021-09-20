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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * An {@link InputStream} that utilizes memory mapped files to improve performance. A sliding window of the file is
 * mapped to memory to avoid mapping the entire file to memory at one time. The size of the sliding buffer is
 * configurable.
 * <p>
 * For most operating systems, mapping a file into memory is more expensive than reading or writing a few tens of
 * kilobytes of data. From the standpoint of performance. it is generally only worth mapping relatively large files into
 * memory.
 * </p>
 * <p>
 * Note: Use of this class does not necessarily obviate the need to use a {@link BufferedInputStream}. Depending on the
 * use case, the use of buffering may still further improve performance. For example:
 * </p>
 * <pre>
 * new BufferedInputStream(new GzipInputStream(new MemoryMappedFileInputStream(path))))
 * </pre>
 * <p>
 * should outperform:
 * </p>
 * <pre>
 * new GzipInputStream(new MemoryMappedFileInputStream(path))
 * </pre>
 *
 * @since 2.12.0
 */
public class MemoryMappedFileInputStream extends InputStream {

    /**
     * Default size of the sliding memory mapped buffer. We use 256K, equal to 65536 pages (given a 4K page size).
     * Increasing the value beyond the default size will generally not provide any increase in throughput.
     */
    private static final int DEFAULT_BUFFER_SIZE = 256 * 1024;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();
    private final int bufferSize;
    private final FileChannel channel;
    private ByteBuffer buffer = EMPTY_BUFFER;
    private boolean closed;

    /**
     * The starting position (within the file) of the next sliding buffer.
     */
    private long nextBufferPosition;

    /**
     * Constructs a new instance.
     *
     * @param file The path of the file to to open.
     * @throws IOException If an I/O error occurs
     */
    public MemoryMappedFileInputStream(final Path file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param file The path of the file to to open.
     * @param bufferSize Size of the sliding buffer.
     * @throws IOException If an I/O error occurs.
     */
    public MemoryMappedFileInputStream(final Path file, final int bufferSize) throws IOException {
        this.bufferSize = bufferSize;
        this.channel = FileChannel.open(file, StandardOpenOption.READ);
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }

    private void cleanBuffer() {
        if (ByteBufferCleaner.isSupported() && buffer.isDirect()) {
            ByteBufferCleaner.clean(buffer);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            cleanBuffer();
            buffer = null;
            channel.close();
            closed = true;
        }
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    private void nextBuffer() throws IOException {
        final long remainingInFile = channel.size() - nextBufferPosition;
        if (remainingInFile > 0) {
            final long amountToMap = Math.min(remainingInFile, bufferSize);
            cleanBuffer();
            buffer = channel.map(MapMode.READ_ONLY, nextBufferPosition, amountToMap);
            nextBufferPosition += amountToMap;
        } else {
            buffer = EMPTY_BUFFER;
        }
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (!buffer.hasRemaining()) {
            nextBuffer();
            if (!buffer.hasRemaining()) {
                return EOF;
            }
        }
        return Short.toUnsignedInt(buffer.get());
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        ensureOpen();
        if (!buffer.hasRemaining()) {
            nextBuffer();
            if (!buffer.hasRemaining()) {
                return EOF;
            }
        }
        final int numBytes = Math.min(buffer.remaining(), len);
        buffer.get(b, off, numBytes);
        return numBytes;
    }

    @Override
    public long skip(final long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }
        if (n <= buffer.remaining()) {
            buffer.position((int) (buffer.position() + n));
            return n;
        }
        final long remainingInFile = channel.size() - nextBufferPosition;
        final long skipped = buffer.remaining() + Math.min(remainingInFile, n - buffer.remaining());
        nextBufferPosition += skipped - buffer.remaining();
        nextBuffer();
        return skipped;
    }

}
