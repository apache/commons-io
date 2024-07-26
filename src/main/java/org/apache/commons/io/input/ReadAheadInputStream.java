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

// import javax.annotation.concurrent.GuardedBy;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * Implements {@link InputStream} to asynchronously read ahead from an underlying input stream when a specified amount of data has been read from the current
 * buffer. It does so by maintaining two buffers: an active buffer and a read ahead buffer. The active buffer contains data which should be returned when a
 * read() call is issued. The read ahead buffer is used to asynchronously read from the underlying input stream. When the current active buffer is exhausted, we
 * flip the two buffers so that we can start reading from the read ahead buffer without being blocked by disk I/O.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19.
 * </p>
 *
 * @see Builder
 * @since 2.9.0
 */
public class ReadAheadInputStream extends FilterInputStream {

    // @formatter:off
    /**
     * Builds a new {@link ReadAheadInputStream}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * ReadAheadInputStream s = ReadAheadInputStream.builder()
     *   .setPath(path)
     *   .setExecutorService(Executors.newSingleThreadExecutor(ReadAheadInputStream::newThread))
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<ReadAheadInputStream, Builder> {

        private ExecutorService executorService;

        /**
         * Builds a new {@link ReadAheadInputStream}.
         * <p>
         * You must set input that supports {@link #getInputStream()}, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()}</li>
         * <li>{@link #getBufferSize()}</li>
         * <li>{@link ExecutorService}</li>
         * </ul>
         *
         * @return a new instance.
         * @throws IllegalStateException         if the {@code origin} is {@code null}.
         * @throws UnsupportedOperationException if the origin cannot be converted to an {@link InputStream}.
         * @throws IOException                   if an I/O error occurs.
         * @see #getInputStream()
         * @see #getBufferSize()
         */
        @SuppressWarnings("resource")
        @Override
        public ReadAheadInputStream get() throws IOException {
            return new ReadAheadInputStream(getInputStream(), getBufferSize(), executorService != null ? executorService : newExecutorService(),
                    executorService == null);
        }

        /**
         * Sets the executor service for the read-ahead thread.
         *
         * @param executorService the executor service for the read-ahead thread.
         * @return {@code this} instance.
         */
        public Builder setExecutorService(final ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

    }

    private static final ThreadLocal<byte[]> BYTE_ARRAY_1 = ThreadLocal.withInitial(() -> new byte[1]);

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs a new daemon thread.
     *
     * @param r the thread's runnable.
     * @return a new daemon thread.
     */
    private static Thread newDaemonThread(final Runnable r) {
        final Thread thread = new Thread(r, "commons-io-read-ahead");
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Constructs a new daemon executor service.
     *
     * @return a new daemon executor service.
     */
    private static ExecutorService newExecutorService() {
        return Executors.newSingleThreadExecutor(ReadAheadInputStream::newDaemonThread);
    }

    private final ReentrantLock stateChangeLock = new ReentrantLock();

    // @GuardedBy("stateChangeLock")
    private ByteBuffer activeBuffer;

    // @GuardedBy("stateChangeLock")
    private ByteBuffer readAheadBuffer;

    // @GuardedBy("stateChangeLock")
    private boolean endOfStream;

    // @GuardedBy("stateChangeLock")
    // true if async read is in progress
    private boolean readInProgress;

    // @GuardedBy("stateChangeLock")
    // true if read is aborted due to an exception in reading from underlying input stream.
    private boolean readAborted;

    // @GuardedBy("stateChangeLock")
    private Throwable readException;

    // @GuardedBy("stateChangeLock")
    // whether the close method is called.
    private boolean isClosed;

    // @GuardedBy("stateChangeLock")
    // true when the close method will close the underlying input stream. This is valid only if
    // `isClosed` is true.
    private boolean isUnderlyingInputStreamBeingClosed;

    // @GuardedBy("stateChangeLock")
    // whether there is a read ahead task running,
    private boolean isReading;

    // Whether there is a reader waiting for data.
    private final AtomicBoolean isWaiting = new AtomicBoolean();

    private final ExecutorService executorService;

    private final boolean shutdownExecutorService;

    private final Condition asyncReadComplete = stateChangeLock.newCondition();

    /**
     * Constructs an instance with the specified buffer size and read-ahead threshold
     *
     * @param inputStream       The underlying input stream.
     * @param bufferSizeInBytes The buffer size.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReadAheadInputStream(final InputStream inputStream, final int bufferSizeInBytes) {
        this(inputStream, bufferSizeInBytes, newExecutorService(), true);
    }

    /**
     * Constructs an instance with the specified buffer size and read-ahead threshold
     *
     * @param inputStream       The underlying input stream.
     * @param bufferSizeInBytes The buffer size.
     * @param executorService   An executor service for the read-ahead thread.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public ReadAheadInputStream(final InputStream inputStream, final int bufferSizeInBytes, final ExecutorService executorService) {
        this(inputStream, bufferSizeInBytes, executorService, false);
    }

    /**
     * Constructs an instance with the specified buffer size and read-ahead threshold
     *
     * @param inputStream             The underlying input stream.
     * @param bufferSizeInBytes       The buffer size.
     * @param executorService         An executor service for the read-ahead thread.
     * @param shutdownExecutorService Whether or not to shut down the given ExecutorService on close.
     */
    private ReadAheadInputStream(final InputStream inputStream, final int bufferSizeInBytes, final ExecutorService executorService,
            final boolean shutdownExecutorService) {
        super(Objects.requireNonNull(inputStream, "inputStream"));
        if (bufferSizeInBytes <= 0) {
            throw new IllegalArgumentException("bufferSizeInBytes should be greater than 0, but the value is " + bufferSizeInBytes);
        }
        this.executorService = Objects.requireNonNull(executorService, "executorService");
        this.shutdownExecutorService = shutdownExecutorService;
        this.activeBuffer = ByteBuffer.allocate(bufferSizeInBytes);
        this.readAheadBuffer = ByteBuffer.allocate(bufferSizeInBytes);
        this.activeBuffer.flip();
        this.readAheadBuffer.flip();
    }

    @Override
    public int available() throws IOException {
        stateChangeLock.lock();
        // Make sure we have no integer overflow.
        try {
            return (int) Math.min(Integer.MAX_VALUE, (long) activeBuffer.remaining() + readAheadBuffer.remaining());
        } finally {
            stateChangeLock.unlock();
        }
    }

    private void checkReadException() throws IOException {
        if (readAborted) {
            if (readException instanceof IOException) {
                throw (IOException) readException;
            }
            throw new IOException(readException);
        }
    }

    @Override
    public void close() throws IOException {
        boolean isSafeToCloseUnderlyingInputStream = false;
        stateChangeLock.lock();
        try {
            if (isClosed) {
                return;
            }
            isClosed = true;
            if (!isReading) {
                // Nobody is reading, so we can close the underlying input stream in this method.
                isSafeToCloseUnderlyingInputStream = true;
                // Flip this to make sure the read ahead task will not close the underlying input stream.
                isUnderlyingInputStreamBeingClosed = true;
            }
        } finally {
            stateChangeLock.unlock();
        }

        if (shutdownExecutorService) {
            try {
                executorService.shutdownNow();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                final InterruptedIOException iio = new InterruptedIOException(e.getMessage());
                iio.initCause(e);
                throw iio;
            } finally {
                if (isSafeToCloseUnderlyingInputStream) {
                    super.close();
                }
            }
        }
    }

    private void closeUnderlyingInputStreamIfNecessary() {
        boolean needToCloseUnderlyingInputStream = false;
        stateChangeLock.lock();
        try {
            isReading = false;
            if (isClosed && !isUnderlyingInputStreamBeingClosed) {
                // close method cannot close underlyingInputStream because we were reading.
                needToCloseUnderlyingInputStream = true;
            }
        } finally {
            stateChangeLock.unlock();
        }
        if (needToCloseUnderlyingInputStream) {
            try {
                super.close();
            } catch (final IOException ignored) {
                // TODO Rethrow as UncheckedIOException?
            }
        }
    }

    private boolean isEndOfStream() {
        return !activeBuffer.hasRemaining() && !readAheadBuffer.hasRemaining() && endOfStream;
    }

    @Override
    public int read() throws IOException {
        if (activeBuffer.hasRemaining()) {
            // short path - just get one byte.
            return activeBuffer.get() & 0xFF;
        }
        final byte[] oneByteArray = BYTE_ARRAY_1.get();
        oneByteArray[0] = 0;
        return read(oneByteArray, 0, 1) == EOF ? EOF : oneByteArray[0] & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int offset, int len) throws IOException {
        if (offset < 0 || len < 0 || len > b.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }

        if (!activeBuffer.hasRemaining()) {
            // No remaining in active buffer - lock and switch to write ahead buffer.
            stateChangeLock.lock();
            try {
                waitForAsyncReadComplete();
                if (!readAheadBuffer.hasRemaining()) {
                    // The first read.
                    readAsync();
                    waitForAsyncReadComplete();
                    if (isEndOfStream()) {
                        return EOF;
                    }
                }
                // Swap the newly read ahead buffer in place of empty active buffer.
                swapBuffers();
                // After swapping buffers, trigger another async read for read ahead buffer.
                readAsync();
            } finally {
                stateChangeLock.unlock();
            }
        }
        len = Math.min(len, activeBuffer.remaining());
        activeBuffer.get(b, offset, len);

        return len;
    }

    /**
     * Read data from underlyingInputStream to readAheadBuffer asynchronously.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void readAsync() throws IOException {
        stateChangeLock.lock();
        final byte[] arr;
        try {
            arr = readAheadBuffer.array();
            if (endOfStream || readInProgress) {
                return;
            }
            checkReadException();
            readAheadBuffer.position(0);
            readAheadBuffer.flip();
            readInProgress = true;
        } finally {
            stateChangeLock.unlock();
        }
        executorService.execute(() -> {
            stateChangeLock.lock();
            try {
                if (isClosed) {
                    readInProgress = false;
                    return;
                }
                // Flip this so that the close method will not close the underlying input stream when we
                // are reading.
                isReading = true;
            } finally {
                stateChangeLock.unlock();
            }

            // Please note that it is safe to release the lock and read into the read ahead buffer
            // because either of following two conditions will hold:
            //
            // 1. The active buffer has data available to read so the reader will not read from the read ahead buffer.
            //
            // 2. This is the first time read is called or the active buffer is exhausted, in that case the reader waits
            // for this async read to complete.
            //
            // So there is no race condition in both the situations.
            int read = 0;
            int off = 0, len = arr.length;
            Throwable exception = null;
            try {
                // try to fill the read ahead buffer.
                // if a reader is waiting, possibly return early.
                do {
                    read = in.read(arr, off, len);
                    if (read <= 0) {
                        break;
                    }
                    off += read;
                    len -= read;
                } while (len > 0 && !isWaiting.get());
            } catch (final Throwable ex) {
                exception = ex;
                if (ex instanceof Error) {
                    // `readException` may not be reported to the user. Rethrow Error to make sure at least
                    // The user can see Error in UncaughtExceptionHandler.
                    throw (Error) ex;
                }
            } finally {
                stateChangeLock.lock();
                try {
                    readAheadBuffer.limit(off);
                    if (read < 0 || exception instanceof EOFException) {
                        endOfStream = true;
                    } else if (exception != null) {
                        readAborted = true;
                        readException = exception;
                    }
                    readInProgress = false;
                    signalAsyncReadComplete();
                } finally {
                    stateChangeLock.unlock();
                }
                closeUnderlyingInputStreamIfNecessary();
            }
        });
    }

    private void signalAsyncReadComplete() {
        stateChangeLock.lock();
        try {
            asyncReadComplete.signalAll();
        } finally {
            stateChangeLock.unlock();
        }
    }

    @Override
    public long skip(final long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        }
        if (n <= activeBuffer.remaining()) {
            // Only skipping from active buffer is sufficient
            activeBuffer.position((int) n + activeBuffer.position());
            return n;
        }
        stateChangeLock.lock();
        final long skipped;
        try {
            skipped = skipInternal(n);
        } finally {
            stateChangeLock.unlock();
        }
        return skipped;
    }

    /**
     * Internal skip function which should be called only from skip(). The assumption is that the stateChangeLock is already acquired in the caller before
     * calling this function.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    private long skipInternal(final long n) throws IOException {
        if (!stateChangeLock.isLocked()) {
            throw new IllegalStateException("Expected stateChangeLock to be locked");
        }
        waitForAsyncReadComplete();
        if (isEndOfStream()) {
            return 0;
        }
        if (available() >= n) {
            // we can skip from the internal buffers
            int toSkip = (int) n;
            // We need to skip from both active buffer and read ahead buffer
            toSkip -= activeBuffer.remaining();
            if (toSkip <= 0) { // skipping from activeBuffer already handled.
                throw new IllegalStateException("Expected toSkip > 0, actual: " + toSkip);
            }
            activeBuffer.position(0);
            activeBuffer.flip();
            readAheadBuffer.position(toSkip + readAheadBuffer.position());
            swapBuffers();
            // Trigger async read to emptied read ahead buffer.
            readAsync();
            return n;
        }
        final int skippedBytes = available();
        final long toSkip = n - skippedBytes;
        activeBuffer.position(0);
        activeBuffer.flip();
        readAheadBuffer.position(0);
        readAheadBuffer.flip();
        final long skippedFromInputStream = in.skip(toSkip);
        readAsync();
        return skippedBytes + skippedFromInputStream;
    }

    /**
     * Flips the active and read ahead buffer
     */
    private void swapBuffers() {
        final ByteBuffer temp = activeBuffer;
        activeBuffer = readAheadBuffer;
        readAheadBuffer = temp;
    }

    private void waitForAsyncReadComplete() throws IOException {
        stateChangeLock.lock();
        try {
            isWaiting.set(true);
            // There is only one reader, and one writer, so the writer should signal only once,
            // but a while loop checking the wake-up condition is still needed to avoid spurious wakeups.
            while (readInProgress) {
                asyncReadComplete.await();
            }
        } catch (final InterruptedException e) {
            final InterruptedIOException iio = new InterruptedIOException(e.getMessage());
            iio.initCause(e);
            throw iio;
        } finally {
            try {
                isWaiting.set(false);
            } finally {
                stateChangeLock.unlock();
            }
        }
        checkReadException();
    }
}
