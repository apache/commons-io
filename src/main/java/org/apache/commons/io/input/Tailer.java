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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

/**
 * Simple implementation of the unix "tail -f" functionality.
 *
 * <h2>1. Create a TailerListener implementation</h2>
 * <p>
 * First you need to create a {@link TailerListener} implementation
 * ({@link TailerListenerAdapter} is provided for convenience so that you don't have to
 * implement every method).
 * </p>
 *
 * <p>For example:</p>
 * <pre>
 *  public class MyTailerListener extends TailerListenerAdapter {
 *      public void handle(String line) {
 *          System.out.println(line);
 *      }
 *  }</pre>
 *
 * <h2>2. Using a Tailer</h2>
 *
 * <p>
 * You can create and use a Tailer in one of three ways:
 * </p>
 * <ul>
 *   <li>Using one of the static helper methods:
 *     <ul>
 *       <li>{@link Tailer#create(File, TailerListener)}</li>
 *       <li>{@link Tailer#create(File, TailerListener, long)}</li>
 *       <li>{@link Tailer#create(File, TailerListener, long, boolean)}</li>
 *     </ul>
 *   </li>
 *   <li>Using an {@link java.util.concurrent.Executor}</li>
 *   <li>Using an {@link Thread}</li>
 * </ul>
 *
 * <p>
 * An example of each of these is shown below.
 * </p>
 *
 * <h3>2.1 Using the static helper method</h3>
 *
 * <pre>
 *      TailerListener listener = new MyTailerListener();
 *      Tailer tailer = Tailer.create(file, listener, delay);</pre>
 *
 * <h3>2.2 Using an Executor</h3>
 *
 * <pre>
 *      TailerListener listener = new MyTailerListener();
 *      Tailer tailer = new Tailer(file, listener, delay);
 *
 *      // stupid executor impl. for demo purposes
 *      Executor executor = new Executor() {
 *          public void execute(Runnable command) {
 *              command.run();
 *           }
 *      };
 *
 *      executor.execute(tailer);
 * </pre>
 *
 *
 * <h3>2.3 Using a Thread</h3>
 * <pre>
 *      TailerListener listener = new MyTailerListener();
 *      Tailer tailer = new Tailer(file, listener, delay);
 *      Thread thread = new Thread(tailer);
 *      thread.setDaemon(true); // optional
 *      thread.start();</pre>
 *
 * <h2>3. Stopping a Tailer</h2>
 * <p>Remember to stop the tailer when you have done with it:</p>
 * <pre>
 *      tailer.stop();
 * </pre>
 *
 * <h2>4. Interrupting a Tailer</h2>
 * <p>You can interrupt the thread a tailer is running on by calling {@link Thread#interrupt()}.</p>
 * <pre>
 *      thread.interrupt();
 * </pre>
 * <p>If you interrupt a tailer, the tailer listener is called with the {@link InterruptedException}.</p>
 *
 * <p>The file is read using the default charset; this can be overridden if necessary</p>
 * @see TailerListener
 * @see TailerListenerAdapter
 * @version $Id$
 * @since 2.0
 * @since 2.5 Updated behavior and documentation for {@link Thread#interrupt()}
 */
public class Tailer implements Runnable {

    private static final int DEFAULT_DELAY_MILLIS = 1000;

    private static final String RAF_MODE = "r";

    private static final int DEFAULT_BUFSIZE = 4096;

    // The default charset used for reading files
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * Buffer on top of RandomAccessFile.
     */
    private final byte inbuf[];

    /**
     * The file which will be tailed.
     */
    private final Tailable tailable;

    /**
     * The character set that will be used to read the file.
     */
    private final Charset cset;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delayMillis;

    /**
     * Whether to tail from the end or start of file
     */
    private final boolean end;

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
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
     * @param file The file to follow.
     * @param listener the TailerListener to use.
     */
    public Tailer(final File file, final TailerListener listener) {
        this(file, listener, DEFAULT_DELAY_MILLIS);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
     * @param file The file to follow.
     * @param listener the TailerListener to use.
     */
    public Tailer(final Tailable file, final TailerListener listener) {
        this(file, listener, DEFAULT_DELAY_MILLIS);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     */
    public Tailer(final Tailable file, final TailerListener listener, final long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end) {
        this(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     */
    public Tailer(final Tailable file, final TailerListener listener, final long delayMillis, final boolean end) {
        this(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     */
    public Tailer(final Tailable file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize Buffer size
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final int bufSize) {
        this(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize Buffer size
     */
    public Tailer(final Tailable file, final TailerListener listener, final long delayMillis, final boolean end,
                  final int bufSize) {
        this(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen, final int bufSize) {
        this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     */
    public Tailer(final Tailable file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen, final int bufSize) {
        this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param file the file to follow.
     * @param cset the Charset to be used for reading the file
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     */
    public Tailer(final File file, final Charset cset, final TailerListener listener, final long delayMillis,
                  final boolean end, final boolean reOpen
            , final int bufSize) {
        this.tailable = new FileTailable(file);
        this.delayMillis = delayMillis;
        this.end = end;

        this.inbuf = new byte[bufSize];

        // Save and prepare the listener
        this.listener = listener;
        listener.init(this);
        this.reOpen = reOpen;
        this.cset = cset;
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     * @param tailable the file to follow.
     * @param cset the Charset to be used for reading the file
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen if true, close and reopen the file between reading chunks
     * @param bufSize Buffer size
     */
    public Tailer(final Tailable tailable, final Charset cset, final TailerListener listener, final long delayMillis,
                  final boolean end, final boolean reOpen
            , final int bufSize) {
        this.tailable = tailable;
        this.delayMillis = delayMillis;
        this.end = end;

        this.inbuf = new byte[bufSize];

        // Save and prepare the listener
        this.listener = listener;
        listener.init(this);
        this.reOpen = reOpen;
        this.cset = cset;
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final int bufSize) {
        return create(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener, final long delayMillis,
                                final boolean end, final int bufSize) {
        return create(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen,
            final int bufSize) {
        return create(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen
            ,final int bufSize) {
        return create(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param charset the character set to use for reading the file
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final File file, final Charset charset, final TailerListener listener,
                                final long delayMillis, final boolean end, final boolean reOpen
            ,final int bufSize) {
        final Tailer tailer = new Tailer(file, charset, listener, delayMillis, end, reOpen, bufSize);
        final Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file the file to follow.
     * @param charset the character set to use for reading the file
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @param bufSize buffer size.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final Charset charset, final TailerListener listener,
                                final long delayMillis, final boolean end, final boolean reOpen
            , final int bufSize) {
        final Tailer tailer = new Tailer(file, charset, listener, delayMillis, end, reOpen, bufSize);
        final Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        return tailer;
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end) {
        return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener, final long delayMillis,
                                final boolean end) {
        return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen) {
        return create(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen whether to close/reopen the file between chunks
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen) {
        return create(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis) {
        return create(file, listener, delayMillis, false);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener, final long delayMillis) {
        return create(file, listener, delayMillis, false);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     * with the default delay of 1.0s
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @return The new tailer
     */
    public static Tailer create(final File file, final TailerListener listener) {
        return create(file, listener, DEFAULT_DELAY_MILLIS, false);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     * with the default delay of 1.0s
     *
     * @param file the file to follow.
     * @param listener the TailerListener to use.
     * @return The new tailer
     */
    public static Tailer create(final Tailable file, final TailerListener listener) {
        return create(file, listener, DEFAULT_DELAY_MILLIS, false);
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public Tailable getTailable() {
        return tailable;
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public File getFile() {
        if (tailable instanceof FileTailable) {
            return ((FileTailable) tailable).getFile();
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
     * Return the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    public long getDelay() {
        return delayMillis;
    }

    /**
     * Follows changes in the file, calling the TailerListener's handle method for each new line.
     */
    @Override
    public void run() {
        RandomAccessTailable reader = null;
        try {
            long last = 0; // The last time the file was checked for changes
            long position = 0; // position within the file
            // Open the file
            while (getRun() && reader == null) {
                try {
                    reader = tailable.getRandomAccess(RAF_MODE);
                } catch (final FileNotFoundException e) {
                    listener.fileNotFound();
                }
                if (reader == null) {
                    Thread.sleep(delayMillis);
                } else {
                    // The current position in the file
                    position = end ? tailable.length() : 0;
                    last = tailable.lastModified();
                    reader.seek(position);
                }
            }
            while (getRun()) {
                final boolean newer = isFileNewer(tailable, last); // IO-279, must be done first
                // Check the file length to see if it was rotated
                final long length = tailable.length();
                if (length < position) {
                    // File was rotated
                    listener.fileRotated();
                    // Reopen the reader after rotation ensuring that the old file is closed iff we re-open it
                    // successfully
                    try (RandomAccessTailable save = reader) {
                        reader = tailable.getRandomAccess(RAF_MODE);
                        // At this point, we're sure that the old file is rotated
                        // Finish scanning the old file and then we'll start with the new one
                        try {
                            readLines(save);
                        } catch (IOException ioe) {
                            listener.handle(ioe);
                        }
                        position = 0;
                    } catch (final FileNotFoundException e) {
                        // in this case we continue to use the previous reader and position values
                        listener.fileNotFound();
                        Thread.sleep(delayMillis);
                    }
                    continue;
                } else {
                    // File was not rotated
                    // See if the file needs to be read again
                    if (length > position) {
                        // The file has more content than it did last time
                        position = readLines(reader);
                        last = tailable.lastModified();
                    } else if (newer) {
                        /*
                         * This can happen if the file is truncated or overwritten with the exact same length of
                         * information. In cases like this, the file position needs to be reset
                         */
                        position = 0;
                        reader.seek(position); // cannot be null here

                        // Now we can read new lines
                        position = readLines(reader);
                        last = tailable.lastModified();
                    }
                }
                if (reOpen && reader != null) {
                    reader.close();
                }
                Thread.sleep(delayMillis);
                if (getRun() && reOpen) {
                    reader = tailable.getRandomAccess(RAF_MODE);
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
                if (reader != null) {
                    reader.close();
                }
            } catch (final IOException e) {
                listener.handle(e);
            }
            stop();
        }
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop() {
        this.run = false;
    }

    /**
     * Read new lines.
     *
     * @param reader The file to read
     * @return The new position after the lines have been read
     * @throws java.io.IOException if an I/O error occurs.
     */
    private long readLines(final RandomAccessTailable reader) throws IOException {
        try (ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64)) {
            long pos = reader.getFilePointer();
            long rePos = pos; // position to re-read
            int num;
            boolean seenCR = false;
            while (getRun() && ((num = reader.read(inbuf)) != EOF)) {
                for (int i = 0; i < num; i++) {
                    final byte ch = inbuf[i];
                    switch ( ch ) {
                        case '\n':
                            seenCR = false; // swallow CR before LF
                            listener.handle(new String(lineBuf.toByteArray(), cset));
                            lineBuf.reset();
                            rePos = pos + i + 1;
                            break;
                        case '\r':
                            if (seenCR) {
                                lineBuf.write('\r');
                            }
                            seenCR = true;
                            break;
                        default:
                            if (seenCR) {
                                seenCR = false; // swallow final CR
                                listener.handle(new String(lineBuf.toByteArray(), cset));
                                lineBuf.reset();
                                rePos = pos + i + 1;
                            }
                            lineBuf.write(ch);
                    }
                }
                pos = reader.getFilePointer();
            }

            reader.seek(rePos); // Ensure we can re-read if necessary

            if (listener instanceof TailerListenerAdapter) {
                ((TailerListenerAdapter) listener).endOfFileReached();
            }

            return rePos;
        }
    }

    /**
     * @see FileUtils#isFileNewer(File, long)
     */
    private static boolean isFileNewer(final Tailable file, final long timeMillis) {
        if (file == null) {
            throw new IllegalArgumentException("No specified file");
        }
        return file.exists() && file.lastModified() > timeMillis;
    }

    /**
     * Abstraction on {@link java.io.File} which allows substitution of remote files for example using jCIFS
     */
    public interface Tailable {
        /**
         * @return  The name of the file denoted by this tailable
         */
        String getName();

        /**
         * @return  The full path of this tailable
         */
        String getPath();

        /**
         * Returns the length of this tailable
         *
         * @return  The length, in bytes, of this tailable, or <code>0L</code>
         *          if the file does not exist.  Some operating systems may
         *          return <code>0L</code> for pathnames denoting system-dependent
         *          entities such as devices or pipes.
         */
        long length();

        /**
         * Returns the time that this tailable was last modified.
         *
         * @return  A <code>long</code> value representing the time this tailable
         *          was last modified, measured in milliseconds since the epoch
         *          (00:00:00 GMT, January 1, 1970), or <code>0L</code> if the
         *          tailable does not exist or if an I/O error occurs
         */
        long lastModified();

        /**
         * Tests whether this tailable exists.
         *
         * @return  <code>true</code> if and only if the tailable exists;
         *          <code>false</code> otherwise
         */
        boolean exists();

        /**
         * @param mode the access mode {@link RandomAccessFile}
         * @return a random access file stream to read from
         * @throws FileNotFoundException if the tailable object does not exist
         */
        RandomAccessTailable getRandomAccess(final String mode) throws FileNotFoundException;
    }

    /**
     * Abstraction on {@link java.io.RandomAccessFile} which allows substitution of remote files for example using jCIFS
     */
    public interface RandomAccessTailable extends Closeable {
        /**
         * Returns the current offset in this tailable.
         *
         * @return     the offset from the beginning of the tailable, in bytes,
         *             at which the next read or write occurs.
         * @exception  IOException  if an I/O error occurs.
         */
        long getFilePointer() throws IOException;

        /**
         * Sets the file-pointer offset, measured from the beginning of this
         * tailable, at which the next read or write occurs.  The offset may be
         * set beyond the end of the tailable. Setting the offset beyond the end
         * of the tailable does not change the tailable length.  The tailable
         * length will change only by writing after the offset has been set beyond
         * the end of the tailable.
         *
         * @param      pos   the offset position, measured in bytes from the
         *                   beginning of the tailable, at which to set the
         *                   tailable pointer.
         * @exception  IOException  if {@code pos} is less than
         *                          {@code 0} or if an I/O error occurs.
         */
        void seek(final long pos) throws IOException;

        /**
         * Reads up to {@code b.length} bytes of data from this tailable
         * into an array of bytes. This method blocks until at least one byte
         * of input is available.
         *
         * @param      b   the buffer into which the data is read.
         * @return     the total number of bytes read into the buffer, or
         *             {@code -1} if there is no more data because the end of
         *             this tailable has been reached.
         * @exception  IOException If the first byte cannot be read for any reason
         * other than end of tailable, or if the random access tailable has been
         * closed, or if some other I/O error occurs.
         */
        int read(final byte[] b) throws IOException;
    }

    private static final class FileTailable implements Tailable {
        private final File file;

        private FileTailable(final File file) {
            this.file = file;
        }

        private File getFile() {
            return file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getPath() {
            return file.getPath();
        }

        @Override
        public long length() {
            return file.length();
        }

        @Override
        public long lastModified() {
            return file.lastModified();
        }

        @Override
        public boolean exists() {
            return file.exists();
        }

        @Override
        public RandomAccessTailable getRandomAccess(final String mode) throws FileNotFoundException {
            return new RandomAccessTailable() {
                private final RandomAccessFile reader = new RandomAccessFile(file, mode);

                @Override
                public long getFilePointer() throws IOException {
                    return reader.getFilePointer();
                }

                @Override
                public void seek(final long position) throws IOException {
                    reader.seek(position);
                }

                @Override
                public int read(final byte[] b) throws IOException {
                    return reader.read(b);
                }

                @Override
                public void close() throws IOException {
                    reader.close();
                }
            };
        }
    }
}
