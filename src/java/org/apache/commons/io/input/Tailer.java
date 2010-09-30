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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.io.FileUtils;

/**
 * Simple implementation of the unix "tail -f" functionality.
 * <p>
 * Example Usage:
 * <pre>
 *      TailerListener listener = ...
 *      Tailer tailer = new Tailer(file, listener, delay);
 *      Thread thread = new Thread(tailer);
 *      thread.start();
 * </pre>
 *
 * @version $Id$
 * @since Commons IO 2.0
 */
public class Tailer implements Runnable {

    /**
     * The file which will be tailed.
     */
    private final File file;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delay;

    /**
     * Whether to tail from the end or start of file
     */
    private final boolean end;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener listener;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * The object which will be responsible for actually reading the file.
     */
    private RandomAccessFile reader;

    /**
     * The last position in the file that was read.
     */
    private long position;

    /**
     * The last time the file was checked for changes.
     */
    private long last;

    /**
     * Creates SimpleTailer for the given file.
     * @param file The file to follow.
     * @param listener The TailerListener which to use.
     */
    public Tailer(File file, TailerListener listener) {
        this(file, listener, 1000);
    }

    /**
     * Creates a SimpleTailer for the given file, with a delay other than the default 1.0s.
     * @param file The file to follow.
     * @param listener The TailerListener which to use.
     * @param delay The delay between checks of the file for new content in milliseconds.
     */
    public Tailer(File file, TailerListener listener, long delay) {
        this(file, listener, 1000, false);
    }

    /**
     * Creates a SimpleTailer for the given file, with a delay other than the default 1.0s.
     * @param file The file to follow.
     * @param listener The TailerListener which to use.
     * @param delay The delay between checks of the file for new content in milliseconds.
     * @param end Set to true to tail from the end of the file, false to tail from the beginning of the file.
     */
    public Tailer(File file, TailerListener listener, long delay, boolean end) {

        this.file = file;
        this.delay = delay;
        this.end = end;

        // Save and prepare the listener
        this.listener = listener;
        listener.init(this);
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Return the delay.
     *
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Follows changes in the file, calling the TailerListener's handle method for each new line.
     */
    public void run() {
        try {
            // Open the file
            while (run && reader == null) {
                try {
                    reader = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException e) {
                    listener.fileNotFound();
                }

                if (reader == null) {
                    Thread.sleep(delay);
                } else {
                    // The current position in the file
                    position = end ? file.length() : 0;
                    last = System.currentTimeMillis();
                    reader.seek(position);                    
                }
            }


            while (run) {

                // Check the file length to see if it was rotated
                long length = file.length();

                if (length < position) {

                    // File was rotated
                    listener.fileRotated();

                    // Reopen the reader after rotation
                    try {
                        reader = new RandomAccessFile(file, "r");
                        position = 0;
                    } catch (FileNotFoundException e) {
                        listener.fileNotFound();
                    }
                    continue;
                } else {

                    // File was not rotated

                    // See if the file needs to be read again
                    if (length > position) {

                        // The file has more content than it did last time
                        readLines();

                    } else if (FileUtils.isFileNewer(file, last)) {

                        /* This can happen if the file is truncated or overwritten
                         * with the exact same length of information. In cases like
                         * this, the file position needs to be reset
                         */
                        position = 0;
                        reader.seek(position);

                        // Now we can read new lines
                        readLines();
                    }
                }

                Thread.sleep(delay);
            }

        } catch (Exception e) {

            listener.handle(e);

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
     * @throws java.io.IOException if an I/O error occurs.
     */
    private void readLines() throws IOException {
        last = System.currentTimeMillis();
        String line = reader.readLine();
        while (line != null) {
            listener.handle(line);
            line = reader.readLine();
        }
        position = reader.getFilePointer();
    }

}
