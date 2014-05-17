package org.apache.commons.io.input;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

class TailerRun implements Runnable {

    private static final String RAF_MODE = "r";
    /**
     * The character set that will be used to read the file.
     */
    private final Charset cset;
    /**
     * Whether to tail from the end or start of file
     */
    private final boolean end;

    /**
     * The file which will be tailed.
     */
    private final File file;

    /**
     * Buffer on top of RandomAccessFile.
     */
    private final byte inbuf[];

    /**
     * The last time the file was checked for changes.
     */
    private long last = 0;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener listener;

    /**
     * position within the file
     */
    private long position = 0;

    private RandomAccessFile reader = null;
    /**
     * Whether to close and reopen the file whilst waiting for more input.
     */
    private final boolean reOpen;

    public TailerRun(final File file, final Charset cset, final TailerListener listener, final boolean end,
            final boolean reOpen, final int bufSize) {
        this.file = file;
        this.cset = cset;
        this.end = end;
        this.reOpen = reOpen;
        this.inbuf = new byte[bufSize];
        this.listener = listener;
    }

    public void cleanup() {
        IOUtils.closeQuietly(this.reader);
        this.reader = null;
    }

    /**
     * Read new lines.
     *
     * @param reader
     *            The file to read
     * @return The new position after the lines have been read
     * @throws java.io.IOException
     *             if an I/O error occurs.
     */
    private long readLines(final RandomAccessFile reader) throws IOException {
        final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);
        long pos = reader.getFilePointer();
        long rePos = pos; // position to re-read
        int num;
        boolean seenCR = false;
        while ((num = reader.read(this.inbuf)) != IOUtils.EOF) {
            for (int i = 0; i < num; i++) {
                final byte ch = this.inbuf[i];
                switch (ch) {
                    case '\n':
                        seenCR = false; // swallow CR before LF
                        this.listener.handle(new String(lineBuf.toByteArray(), this.cset));
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
                            this.listener.handle(new String(lineBuf.toByteArray(), this.cset));
                            lineBuf.reset();
                            rePos = pos + i + 1;
                        }
                        lineBuf.write(ch);
                }
            }
            pos = reader.getFilePointer();
        }
        IOUtils.closeQuietly(lineBuf); // not strictly necessary
        reader.seek(rePos); // Ensure we can re-read if necessary
        return rePos;
    }

    @Override
    public void run() {
        try {
            // Open the file
            if (this.reader == null) {
                try {
                    this.reader = new RandomAccessFile(this.file, TailerRun.RAF_MODE);
                } catch (final FileNotFoundException e) {
                    this.listener.fileNotFound();
                    return;
                }
                // The current position in the file
                this.position = this.end ? this.file.length() : 0;
                this.last = this.file.lastModified();
                this.reader.seek(this.position);
            }
            final boolean newer = FileUtils.isFileNewer(this.file, this.last); // IO-279,
                                                                               // must
                                                                               // be
                                                                               // done
                                                                               // first
            // Check the file length to see if it was rotated
            final long length = this.file.length();
            if (length < this.position) {
                // File was rotated
                this.listener.fileRotated();
                // Reopen the reader after rotation
                try {
                    // Ensure that the old file is closed iff we re-open it
                    // successfully
                    final RandomAccessFile save = this.reader;
                    this.reader = new RandomAccessFile(this.file, TailerRun.RAF_MODE);
                    // At this point, we're sure that the old file is rotated
                    // Finish scanning the old file and then we'll start with
                    // the new one
                    try {
                        this.readLines(save);
                    } catch (final IOException ioe) {
                        this.listener.handle(ioe);
                    }
                    this.position = 0;
                    // close old file explicitly rather than relying on GC
                    // picking up previous RAF
                    IOUtils.closeQuietly(save);
                } catch (final FileNotFoundException e) {
                    // in this case we continue to use the previous reader and
                    // position values
                    this.listener.fileNotFound();
                }
            } else {
                // File was not rotated
                // See if the file needs to be read again
                if (length > this.position) {
                    // The file has more content than it did last time
                    this.position = this.readLines(this.reader);
                    this.last = this.file.lastModified();
                } else if (newer) {
                    /*
                     * This can happen if the file is truncated or overwritten
                     * with the exact same length of information. In cases like
                     * this, the file position needs to be reset
                     */
                    this.position = 0;
                    this.reader.seek(this.position); // cannot be null here

                    // Now we can read new lines
                    this.position = this.readLines(this.reader);
                    this.last = this.file.lastModified();
                }
            }
            if (this.reOpen) {
                IOUtils.closeQuietly(this.reader);
                this.reader = new RandomAccessFile(this.file, TailerRun.RAF_MODE);
                this.reader.seek(this.position);
            }
        } catch (final Exception e) {
            this.listener.handle(e);
        }
    }

}
