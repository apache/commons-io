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

import static org.apache.commons.io.IOUtils.CR;
import static org.apache.commons.io.IOUtils.EOF;
import static org.apache.commons.io.IOUtils.LF;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.ThreadUtils;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.attribute.FileTimes;

/**
 * Simple implementation of the UNIX "tail -f" functionality.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <h2>1. Create a TailerListener implementation</h2>
 * <p>
 * First you need to create a {@link TailerListener} implementation; ({@link TailerListenerAdapter} is provided for
 * convenience so that you don't have to implement every method).
 * </p>
 * <p>
 * For example:
 * </p>
 * <pre>
 * public class MyTailerListener extends TailerListenerAdapter {
 *     public void handle(String line) {
 *         System.out.println(line);
 *     }
 * }
 * </pre>
 * <h2>2. Using a Tailer</h2>
 * <p>
 * You can create and use a Tailer in one of three ways:
 * </p>
 * <ul>
 * <li>Using a {@link Builder}</li>
 * <li>Using an {@link java.util.concurrent.Executor}</li>
 * <li>Using a {@link Thread}</li>
 * </ul>
 * <p>
 * An example of each is shown below.
 * </p>
 * <h3>2.1 Using a Builder</h3>
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = Tailer.builder()
 *   .setFile(file)
 *   .setTailerListener(listener)
 *   .setDelayDuration(delay)
 *   .get();
 * </pre>
 * <h3>2.2 Using an Executor</h3>
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = new Tailer(file, listener, delay);
 *
 * // stupid executor impl. for demo purposes
 * Executor executor = new Executor() {
 *     public void execute(Runnable command) {
 *         command.run();
 *     }
 * };
 *
 * executor.execute(tailer);
 * </pre>
 * <h3>2.3 Using a Thread</h3>
 * <pre>
 * TailerListener listener = new MyTailerListener();
 * Tailer tailer = new Tailer(file, listener, delay);
 * Thread thread = new Thread(tailer);
 * thread.setDaemon(true); // optional
 * thread.start();
 * </pre>
 * <h2>3. Stopping a Tailer</h2>
 * <p>
 * Remember to stop the tailer when you have done with it:
 * </p>
 * <pre>
 * tailer.stop();
 * </pre>
 * <h2>4. Interrupting a Tailer</h2>
 * <p>
 * You can interrupt the thread a tailer is running on by calling {@link Thread#interrupt()}.
 * </p>
 * <pre>
 * thread.interrupt();
 * </pre>
 * <p>
 * If you interrupt a tailer, the tailer listener is called with the {@link InterruptedException}.
 * </p>
 * <p>
 * The file is read using the default Charset; this can be overridden if necessary.
 * </p>
 *
 * @see Builder
 * @see TailerListener
 * @see TailerListenerAdapter
 * @since 2.0
 * @since 2.5 Updated behavior and documentation for {@link Thread#interrupt()}.
 * @since 2.12.0 Add {@link Tailable} and {@link RandomAccessResourceBridge} interfaces to tail of files accessed using
 *        alternative libraries such as jCIFS or <a href="https://commons.apache.org/proper/commons-vfs/">Apache Commons
 *        VFS</a>.
 */
public class Tailer implements Runnable, AutoCloseable {

    // @formatter:off
    /**
     * Builds a new {@link Tailer}.
     *
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * Tailer t = Tailer.builder()
     *   .setPath(path)
     *   .setCharset(StandardCharsets.UTF_8)
     *   .setDelayDuration(Duration.ofSeconds(1))
     *   .setExecutorService(Executors.newSingleThreadExecutor(Builder::newDaemonThread))
     *   .setReOpen(false)
     *   .setStartThread(true)
     *   .setTailable(tailable)
     *   .setTailerListener(tailerListener)
     *   .setTailFromEnd(false)
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<Tailer, Builder> {

        private static final Duration DEFAULT_DELAY_DURATION = Duration.ofMillis(DEFAULT_DELAY_MILLIS);

        /**
         * Creates a new daemon thread.
         *
         * @param runnable the thread's runnable.
         * @return a new daemon thread.
         */
        private static Thread newDaemonThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, "commons-io-tailer");
            thread.setDaemon(true);
            return thread;
        }

        private Tailable tailable;
        private TailerListener tailerListener;
        private Duration delayDuration = DEFAULT_DELAY_DURATION;
        private boolean tailFromEnd;
        private boolean reOpen;
        private boolean startThread = true;
        private ExecutorService executorService = Executors.newSingleThreadExecutor(Builder::newDaemonThread);

        /**
         * Builds a new {@link Tailer}.
         *
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getBufferSize()}</li>
         * <li>{@link #getCharset()}</li>
         * <li>{@link Tailable}</li>
         * <li>{@link TailerListener}</li>
         * <li>delayDuration</li>
         * <li>tailFromEnd</li>
         * <li>reOpen</li>
         * </ul>
         *
         * @return a new instance.
         */
        @Override
        public Tailer get() {
            final Tailer tailer = new Tailer(tailable, getCharset(), tailerListener, delayDuration, tailFromEnd, reOpen, getBufferSize());
            if (startThread) {
                executorService.submit(tailer);
            }
            return tailer;
        }

        /**
         * Sets the delay duration. null resets to the default delay of one second.
         *
         * @param delayDuration the delay between checks of the file for new content.
         * @return {@code this} instance.
         */
        public Builder setDelayDuration(final Duration delayDuration) {
            this.delayDuration = delayDuration != null ? delayDuration : DEFAULT_DELAY_DURATION;
            return this;
        }

        /**
         * Sets the executor service to use when startThread is true.
         *
         * @param executorService the executor service to use when startThread is true.
         * @return {@code this} instance.
         */
        public Builder setExecutorService(final ExecutorService executorService) {
            this.executorService = Objects.requireNonNull(executorService, "executorService");
            return this;
        }

        /**
         * Sets the origin.
         *
         * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
         */
        @Override
        protected Builder setOrigin(final AbstractOrigin<?, ?> origin) {
            setTailable(new TailablePath(origin.getPath()));
            return super.setOrigin(origin);
        }

        /**
         * Sets the re-open behavior.
         *
         * @param reOpen whether to close/reopen the file between chunks
         * @return {@code this} instance.
         */
        public Builder setReOpen(final boolean reOpen) {
            this.reOpen = reOpen;
            return this;
        }

        /**
         * Sets the daemon thread startup behavior.
         *
         * @param startThread whether to create a daemon thread automatically.
         * @return {@code this} instance.
         */
        public Builder setStartThread(final boolean startThread) {
            this.startThread = startThread;
            return this;
        }

        /**
         * Sets the tailable.
         *
         * @param tailable the tailable.
         * @return {@code this} instance.
         */
        public Builder setTailable(final Tailable tailable) {
            this.tailable = Objects.requireNonNull(tailable, "tailable");
            return this;
        }

        /**
         * Sets the listener.
         *
         * @param tailerListener the listener.
         * @return {@code this} instance.
         */
        public Builder setTailerListener(final TailerListener tailerListener) {
            this.tailerListener = Objects.requireNonNull(tailerListener, "tailerListener");
            return this;
        }

        /**
         * Sets the tail start behavior.
         *
         * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
         * @return {@code this} instance.
         */
        public Builder setTailFromEnd(final boolean end) {
            this.tailFromEnd = end;
            return this;
        }
    }

    /**
     * Bridges random access to a {@link RandomAccessFile}.
     */
    private static final class RandomAccessFileBridge implements RandomAccessResourceBridge {

        private final RandomAccessFile randomAccessFile;

        private RandomAccessFileBridge(final File file, final String mode) throws FileNotFoundException {
            randomAccessFile = new RandomAccessFile(file, mode);
        }

        @Override
        public void close() throws IOException {
            randomAccessFile.close();
        }

        @Override
        public long getPointer() throws IOException {
            return randomAccessFile.getFilePointer();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return randomAccessFile.read(b);
        }

        @Override
        public void seek(final long position) throws IOException {
            randomAccessFile.seek(position);
        }

    }

    /**
     * Bridges access to a resource for random access, normally a file. Allows substitution of remote files for example
     * using jCIFS.
     *
     * @since 2.12.0
     */
    public interface RandomAccessResourceBridge extends Closeable {

        /**
         * Gets the current offset in this tailable.
         *
         * @return the offset from the beginning of the tailable, in bytes, at which the next read or write occurs.
         * @throws IOException if an I/O error occurs.
         */
        long getPointer() throws IOException;

        /**
         * Reads up to {@code b.length} bytes of data from this tailable into an array of bytes. This method blocks until at
         * least one byte of input is available.
         *
         * @param b the buffer into which the data is read.
         * @return the total number of bytes read into the buffer, or {@code -1} if there is no more data because the end of
         *         this tailable has been reached.
         * @throws IOException If the first byte cannot be read for any reason other than end of tailable, or if the random
         *         access tailable has been closed, or if some other I/O error occurs.
         */
        int read(final byte[] b) throws IOException;

        /**
         * Sets the file-pointer offset, measured from the beginning of this tailable, at which the next read or write occurs.
         * The offset may be set beyond the end of the tailable. Setting the offset beyond the end of the tailable does not
         * change the tailable length. The tailable length will change only by writing after the offset has been set beyond the
         * end of the tailable.
         *
         * @param pos the offset position, measured in bytes from the beginning of the tailable, at which to set the tailable
         *        pointer.
         * @throws IOException if {@code pos} is less than {@code 0} or if an I/O error occurs.
         */
        void seek(final long pos) throws IOException;
    }

    /**
     * A tailable resource like a file.
     *
     * @since 2.12.0
     */
    public interface Tailable {

        /**
         * Creates a random access file stream to read.
         *
         * @param mode the access mode, by default this is for {@link RandomAccessFile}.
         * @return a random access file stream to read.
         * @throws FileNotFoundException if the tailable object does not exist.
         */
        RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException;

        /**
         * Tests if this tailable is newer than the specified {@link FileTime}.
         *
         * @param fileTime the file time reference.
         * @return true if the {@link File} exists and has been modified after the given {@link FileTime}.
         * @throws IOException if an I/O error occurs.
         */
        boolean isNewer(final FileTime fileTime) throws IOException;

        /**
         * Gets the last modification {@link FileTime}.
         *
         * @return See {@link java.nio.file.Files#getLastModifiedTime(Path, LinkOption...)}.
         * @throws IOException if an I/O error occurs.
         */
        FileTime lastModifiedFileTime() throws IOException;

        /**
         * Gets the size of this tailable.
         *
         * @return The size, in bytes, of this tailable, or {@code 0} if the file does not exist. Some operating systems may
         *         return {@code 0} for path names denoting system-dependent entities such as devices or pipes.
         * @throws IOException if an I/O error occurs.
         */
        long size() throws IOException;
    }

    /**
     * A tailable for a file {@link Path}.
     */
    private static final class TailablePath implements Tailable {

        private final Path path;
        private final LinkOption[] linkOptions;

        private TailablePath(final Path path, final LinkOption... linkOptions) {
            this.path = Objects.requireNonNull(path, "path");
            this.linkOptions = linkOptions;
        }

        Path getPath() {
            return path;
        }

        @Override
        public RandomAccessResourceBridge getRandomAccess(final String mode) throws FileNotFoundException {
            return new RandomAccessFileBridge(path.toFile(), mode);
        }

        @Override
        public boolean isNewer(final FileTime fileTime) throws IOException {
            return PathUtils.isNewer(path, fileTime, linkOptions);
        }

        @Override
        public FileTime lastModifiedFileTime() throws IOException {
            return Files.getLastModifiedTime(path, linkOptions);
        }

        @Override
        public long size() throws IOException {
            return Files.size(path);
        }

        @Override
        public String toString() {
            return "TailablePath [file=" + path + ", linkOptions=" + Arrays.toString(linkOptions) + "]";
        }
    }

    private static final int DEFAULT_DELAY_MILLIS = 1000;

    private static final String RAF_READ_ONLY_MODE = "r";

    // The default charset used for reading files
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * Constructs a new {@link Builder}.
     *
     * @return Creates a new {@link Builder}.
     * @since 2.12.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param charset the character set to use for reading the file.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks.
     * @param bufferSize buffer size.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final Charset charset, final TailerListener listener, final long delayMillis, final boolean end,
        final boolean reOpen, final int bufferSize) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setCharset(charset)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .setTailFromEnd(end)
                .setReOpen(reOpen)
                .setBufferSize(bufferSize)
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file with the default delay of 1.0s
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis, final boolean end) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .setTailFromEnd(end)
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis, final boolean end, final boolean reOpen) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .setTailFromEnd(end)
                .setReOpen(reOpen)
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks.
     * @param bufferSize buffer size.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis, final boolean end, final boolean reOpen,
        final int bufferSize) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .setTailFromEnd(end)
                .setReOpen(reOpen)
                .setBufferSize(bufferSize)
                .get();
        //@formatter:on
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufferSize buffer size.
     * @return The new tailer.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis, final boolean end, final int bufferSize) {
        //@formatter:off
        return builder()
                .setFile(file)
                .setTailerListener(listener)
                .setDelayDuration(Duration.ofMillis(delayMillis))
                .setTailFromEnd(end)
                .setBufferSize(bufferSize)
                .get();
        //@formatter:on
    }

    /**
     * Buffer on top of RandomAccessResourceBridge.
     */
    private final byte[] inbuf;

    /**
     * The file which will be tailed.
     */
    private final Tailable tailable;

    /**
     * The character set that will be used to read the file.
     */
    private final Charset charset;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final Duration delayDuration;

    /**
     * Whether to tail from the end or start of file
     */
    private final boolean tailAtEnd;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener listener;

    /**
     * Whether to close and reopen the file whilst waiting for more input.
     */
    private final boolean reOpen;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file the file to follow.
     * @param charset the Charset to be used for reading the file
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final Charset charset, final TailerListener listener, final long delayMillis, final boolean end, final boolean reOpen,
        final int bufSize) {
        this(new TailablePath(file.toPath()), charset, listener, Duration.ofMillis(delayMillis), end, reOpen, bufSize);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
     *
     * @param file The file to follow.
     * @param listener the TailerListener to use.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener) {
        this(file, listener, DEFAULT_DELAY_MILLIS);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener, final long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end) {
        this(file, listener, delayMillis, end, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end, final boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, IOUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufferSize Buffer size
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end, final boolean reOpen, final int bufferSize) {
        this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufferSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufferSize Buffer size
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}.
     */
    @Deprecated
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end, final int bufferSize) {
        this(file, listener, delayMillis, end, false, bufferSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param tailable the file to follow.
     * @param charset the Charset to be used for reading the file
     * @param listener the TailerListener to use.
     * @param delayDuration the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufferSize Buffer size
     */
    private Tailer(final Tailable tailable, final Charset charset, final TailerListener listener, final Duration delayDuration, final boolean end,
        final boolean reOpen, final int bufferSize) {
        this.tailable = Objects.requireNonNull(tailable, "tailable");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.delayDuration = delayDuration;
        this.tailAtEnd = end;
        this.inbuf = IOUtils.byteArray(bufferSize);

        // Save and prepare the listener
        listener.init(this);
        this.reOpen = reOpen;
        this.charset = charset;
    }

    /**
     * Requests the tailer to complete its current loop and return.
     */
    @Override
    public void close() {
        this.run = false;
    }

    /**
     * Gets the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     * @deprecated Use {@link #getDelayDuration()}.
     */
    @Deprecated
    public long getDelay() {
        return delayDuration.toMillis();
    }

    /**
     * Gets the delay Duration.
     *
     * @return the delay Duration.
     * @since 2.12.0
     */
    public Duration getDelayDuration() {
        return delayDuration;
    }

    /**
     * Gets the file.
     *
     * @return the file
     * @throws IllegalStateException if constructed using a user provided {@link Tailable} implementation
     */
    public File getFile() {
        if (tailable instanceof TailablePath) {
            return ((TailablePath) tailable).getPath().toFile();
        }
        throw new IllegalStateException("Cannot extract java.io.File from " + tailable.getClass().getName());
    }

    /**
     * Gets whether to keep on running.
     *
     * @return whether to keep on running.
     * @since 2.5
     */
    protected boolean getRun() {
        return run;
    }

    /**
     * Gets the Tailable.
     *
     * @return the Tailable
     * @since 2.12.0
     */
    public Tailable getTailable() {
        return tailable;
    }

    /**
     * Reads new lines.
     *
     * @param reader The file to read
     * @return The new position after the lines have been read
     * @throws IOException if an I/O error occurs.
     */
    private long readLines(final RandomAccessResourceBridge reader) throws IOException {
        try (ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64)) {
            long pos = reader.getPointer();
            long rePos = pos; // position to re-read
            int num;
            boolean seenCR = false;
            while (getRun() && (num = reader.read(inbuf)) != EOF) {
                for (int i = 0; i < num; i++) {
                    final byte ch = inbuf[i];
                    switch (ch) {
                    case LF:
                        seenCR = false; // swallow CR before LF
                        listener.handle(new String(lineBuf.toByteArray(), charset));
                        lineBuf.reset();
                        rePos = pos + i + 1;
                        break;
                    case CR:
                        if (seenCR) {
                            lineBuf.write(CR);
                        }
                        seenCR = true;
                        break;
                    default:
                        if (seenCR) {
                            seenCR = false; // swallow final CR
                            listener.handle(new String(lineBuf.toByteArray(), charset));
                            lineBuf.reset();
                            rePos = pos + i + 1;
                        }
                        lineBuf.write(ch);
                    }
                }
                pos = reader.getPointer();
            }

            reader.seek(rePos); // Ensure we can re-read if necessary

            if (listener instanceof TailerListenerAdapter) {
                ((TailerListenerAdapter) listener).endOfFileReached();
            }

            return rePos;
        }
    }

    /**
     * Follows changes in the file, calling {@link TailerListener#handle(String)} with each new line.
     */
    @Override
    public void run() {
        RandomAccessResourceBridge reader = null;
        try {
            FileTime last = FileTimes.EPOCH; // The last time the file was checked for changes
            long position = 0; // position within the file
            // Open the file
            while (getRun() && reader == null) {
                try {
                    reader = tailable.getRandomAccess(RAF_READ_ONLY_MODE);
                } catch (final FileNotFoundException e) {
                    listener.fileNotFound();
                }
                if (reader == null) {
                    ThreadUtils.sleep(delayDuration);
                } else {
                    // The current position in the file
                    position = tailAtEnd ? tailable.size() : 0;
                    last = tailable.lastModifiedFileTime();
                    reader.seek(position);
                }
            }
            while (getRun()) {
                final boolean newer = tailable.isNewer(last); // IO-279, must be done first
                // Check the file length to see if it was rotated
                final long length = tailable.size();
                if (length < position) {
                    // File was rotated
                    listener.fileRotated();
                    // Reopen the reader after rotation ensuring that the old file is closed iff we re-open it
                    // successfully
                    try (RandomAccessResourceBridge save = reader) {
                        reader = tailable.getRandomAccess(RAF_READ_ONLY_MODE);
                        // At this point, we're sure that the old file is rotated
                        // Finish scanning the old file and then we'll start with the new one
                        try {
                            readLines(save);
                        } catch (final IOException ioe) {
                            listener.handle(ioe);
                        }
                        position = 0;
                    } catch (final FileNotFoundException e) {
                        // in this case we continue to use the previous reader and position values
                        listener.fileNotFound();
                        ThreadUtils.sleep(delayDuration);
                    }
                    continue;
                }
                // File was not rotated
                // See if the file needs to be read again
                if (length > position) {
                    // The file has more content than it did last time
                    position = readLines(reader);
                    last = tailable.lastModifiedFileTime();
                } else if (newer) {
                    /*
                     * This can happen if the file is truncated or overwritten with the exact same length of information. In cases like
                     * this, the file position needs to be reset
                     */
                    position = 0;
                    reader.seek(position); // cannot be null here

                    // Now we can read new lines
                    position = readLines(reader);
                    last = tailable.lastModifiedFileTime();
                }
                if (reOpen && reader != null) {
                    reader.close();
                }
                ThreadUtils.sleep(delayDuration);
                if (getRun() && reOpen) {
                    reader = tailable.getRandomAccess(RAF_READ_ONLY_MODE);
                    reader.seek(position);
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.handle(e);
        } catch (final Exception e) {
            listener.handle(e);
        } finally {
            try {
                IOUtils.close(reader);
            } catch (final IOException e) {
                listener.handle(e);
            }
            close();
        }
    }

    /**
     * Requests the tailer to complete its current loop and return.
     *
     * @deprecated Use {@link #close()}.
     */
    @Deprecated
    public void stop() {
        close();
    }
}
