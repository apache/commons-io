/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * {@link InputStream} implementation which uses direct buffer to read a file to avoid extra copy of data between Java and native memory which happens when
 * using {@link java.io.BufferedInputStream}. Unfortunately, this is not something already available in JDK, {@code sun.nio.ch.ChannelInputStream} supports
 * reading a file using NIO, but does not support buffering.
 * <p>
 * To build an instance, see {@link Builder}.
 * </p>
 * <p>
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was called {@code NioBufferedFileInputStream}.
 * </p>
 *
 * @since 2.9.0
 */
public final class BufferedFileChannelInputStream extends InputStream {

    /**
     * Builds a new {@link BufferedFileChannelInputStream} instance.
     * <p>
     * Using File IO:
     * </p>
     *
     * <pre>{@code
     * BufferedFileChannelInputStream s = BufferedFileChannelInputStream.builder()
     *   .setFile(file)
     *   .setBufferSize(4096)
     *   .get();}
     * </pre>
     * <p>
     * Using NIO Path:
     * </p>
     *
     * <pre>{@code
     * BufferedFileChannelInputStream s = BufferedFileChannelInputStream.builder()
     *   .setPath(path)
     *   .setBufferSize(4096)
     *   .get();}
     * </pre>
     *
     * @since 2.12.0
     */
    public static class Builder extends AbstractStreamBuilder<BufferedFileChannelInputStream, Builder> {

        /**
         * Constructs a new instance.
         * <p>
         * This builder use the aspects Path and buffer size.
         * </p>
         * <p>
         * You must provide an origin that can be converted to a Path by this builder, otherwise, this call will throw an
         * {@link UnsupportedOperationException}.
         * </p>
         *
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a Path.
         * @see AbstractOrigin#getPath()
         */
        @Override
        public BufferedFileChannelInputStream get() throws IOException {
            return new BufferedFileChannelInputStream(getPath(), getBufferSize());
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

    private final ByteBuffer byteBuffer;

    private final FileChannel fileChannel;

    /**
     * Constructs a new instance for the given File.
     *
     * @param file The file to stream.
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BufferedFileChannelInputStream(final File file) throws IOException {
        this(file, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance for the given File and buffer size.
     *
     * @param file       The file to stream.
     * @param bufferSize buffer size.
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BufferedFileChannelInputStream(final File file, final int bufferSize) throws IOException {
        this(file.toPath(), bufferSize);
    }

    /**
     * Constructs a new instance for the given Path.
     *
     * @param path The path to stream.
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BufferedFileChannelInputStream(final Path path) throws IOException {
        this(path, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new instance for the given Path and buffer size.
     *
     * @param path       The path to stream.
     * @param bufferSize buffer size.
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public BufferedFileChannelInputStream(final Path path, final int bufferSize) throws IOException {
        Objects.requireNonNull(path, "path");
        fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        byteBuffer.flip();
    }

    @Override
    public synchronized int available() throws IOException {
        return byteBuffer.remaining();
    }

    /**
     * Attempts to clean up a ByteBuffer if it is direct or memory-mapped. This uses an *unsafe* Sun API that will cause errors if one attempts to read from the
     * disposed buffer. However, neither the bytes allocated to direct buffers nor file descriptors opened for memory-mapped buffers put pressure on the garbage
     * collector. Waiting for garbage collection may lead to the depletion of off-heap memory or huge numbers of open files. There's unfortunately no standard
     * API to manually dispose of these kinds of buffers.
     *
     * @param buffer the buffer to clean.
     */
    private void clean(final ByteBuffer buffer) {
        if (buffer.isDirect()) {
            cleanDirectBuffer(buffer);
        }
    }

    /**
     * In Java 8, the type of {@code sun.nio.ch.DirectBuffer.cleaner()} was {@code sun.misc.Cleaner}, and it was possible to access the method
     * {@code sun.misc.Cleaner.clean()} to invoke it. The type changed to {@code jdk.internal.ref.Cleaner} in later JDKs, and the {@code clean()} method is not
     * accessible even with reflection. However {@code sun.misc.Unsafe} added an {@code invokeCleaner()} method in JDK 9+ and this is still accessible with
     * reflection.
     *
     * @param buffer the buffer to clean. must be a DirectBuffer.
     */
    private void cleanDirectBuffer(final ByteBuffer buffer) {
        if (ByteBufferCleaner.isSupported()) {
            ByteBufferCleaner.clean(buffer);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            fileChannel.close();
        } finally {
            clean(byteBuffer);
        }
    }

    @Override
    public synchronized int read() throws IOException {
        if (!refill()) {
            return EOF;
        }
        return byteBuffer.get() & 0xFF;
    }

    @Override
    public synchronized int read(final byte[] b, final int offset, int len) throws IOException {
        if (offset < 0 || len < 0 || offset + len < 0 || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (!refill()) {
            return EOF;
        }
        len = Math.min(len, byteBuffer.remaining());
        byteBuffer.get(b, offset, len);
        return len;
    }

    /**
     * Checks whether data is left to be read from the input stream.
     *
     * @return true if data is left, false otherwise
     * @throws IOException if an I/O error occurs.
     */
    private boolean refill() throws IOException {
        if (!byteBuffer.hasRemaining()) {
            byteBuffer.clear();
            int nRead = 0;
            while (nRead == 0) {
                nRead = fileChannel.read(byteBuffer);
            }
            byteBuffer.flip();
            return nRead >= 0;
        }
        return true;
    }

    @Override
    public synchronized long skip(final long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        }
        if (byteBuffer.remaining() >= n) {
            // The buffered content is enough to skip
            byteBuffer.position(byteBuffer.position() + (int) n);
            return n;
        }
        final long skippedFromBuffer = byteBuffer.remaining();
        final long toSkipFromFileChannel = n - skippedFromBuffer;
        // Discard everything we have read in the buffer.
        byteBuffer.position(0);
        byteBuffer.flip();
        return skippedFromBuffer + skipFromFileChannel(toSkipFromFileChannel);
    }

    private long skipFromFileChannel(final long n) throws IOException {
        final long currentFilePosition = fileChannel.position();
        final long size = fileChannel.size();
        if (n > size - currentFilePosition) {
            fileChannel.position(size);
            return size - currentFilePosition;
        }
        fileChannel.position(currentFilePosition + n);
        return n;
    }

}
