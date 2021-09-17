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
 * <p>
 * An {@link InputStream} that utilizes memory mapped files to improve
 * performance. A sliding window of the file is mapped to memory to avoid
 * mapping the entire file to memory at one time. The size of the sliding buffer
 * is user configurable.
 * </p>
 * <p>
 * For most operating systems, mapping a file into memory is more expensive than
 * reading or writing a few tens of kilobytes of data. From the standpoint of
 * performance it is generally only worth mapping relatively large files into
 * memory.
 * </p>
 * Note: Use of this class does not necessarily obviate the need to use a
 * {@link BufferedInputStream}. Depending on the use case, the use of buffering
 * may still further improve performance. For example:
 *
 * <pre>
 * new BufferedInputStream(new GzipInputStream(new MemoryMappedFileInputStream(path))))
 * </pre>
 *
 * will greatly outperform:
 *
 * <pre>
 * new GzipInputStream(new MemoryMappedFileInputStream(path))
 * </pre>
 *
 * @since 2.12.0
 */
public class MemoryMappedFileInputStream extends InputStream {
    /**
     * Default size of the sliding memory mapped buffer. We use 256K, equal to 65536
     * pages (given a 4K page size). Increasing the value beyond the default size
     * will generally not provide any increase in throughput.
     */
    private static final int DEFAULT_BUFFER_SIZE = 256 * 1024;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();
    private static final boolean IS_CLEANING_SUPPORTED = ByteBufferCleaner.isSupported();
    private final int bufferSize;
    private final FileChannel channel;
    private ByteBuffer buffer = EMPTY_BUFFER;
    private boolean closed = false;
    /**
     * The starting position (within the file) of the next sliding buffer.
     */
    private long nextBufferPosition = 0;

    /** Constructs a {@link MemoryMappedFileInputStream} object.
     *
     * @param file The path of the file to to open.
     * @throws IOException If an I/O error occurs
     */
    public MemoryMappedFileInputStream(final Path file) throws IOException {
        this(file, DEFAULT_BUFFER_SIZE);
    }

    /**
     *  Constructs a {@link MemoryMappedFileInputStream} object.
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
                return -1;
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
        if (n <= this.buffer.remaining()) {
            this.buffer.position((int) (this.buffer.position() + n));
            return n;
        } else {
            long remainingInFile = this.channel.size() - nextBufferPosition;
            long skipped = this.buffer.remaining() + Math.min(remainingInFile, n - this.buffer.remaining());
            this.nextBufferPosition += skipped - this.buffer.remaining();
            nextBuffer();
            return skipped;
        }
    }

    @Override
    public int available() throws IOException {
        return this.buffer.remaining();
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            cleanBuffer();
            this.buffer = null;
            this.channel.close();
            this.closed = true;
        }
    }

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    private void nextBuffer() throws IOException {
        long remainingInFile = this.channel.size() - this.nextBufferPosition;
        if (remainingInFile > 0) {
            long amountToMap = Math.min(remainingInFile, bufferSize);
            cleanBuffer();
            this.buffer = this.channel.map(MapMode.READ_ONLY, nextBufferPosition, amountToMap);
            this.nextBufferPosition += amountToMap;
        } else {
            this.buffer = EMPTY_BUFFER;
        }
    }

    private void cleanBuffer() {
        if (IS_CLEANING_SUPPORTED && this.buffer.isDirect()) {
            ByteBufferCleaner.clean(this.buffer);
        }
    }

}
