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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.io.file.PathUtils;

/**
 * An output stream which will retain data in memory until a specified threshold is reached, and only then commit it to
 * disk. If the stream is closed before the threshold is reached, the data will not be written to disk at all.
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not know in advance the size of the file
 * being uploaded. If the file is small you want to store it in memory (for speed), but if the file is large you want to
 * store it to file (to avoid memory issues).
 * </p>
 */
public class DeferredFileOutputStream extends ThresholdingOutputStream {

    /**
     * The output stream to which data will be written prior to the threshold being reached.
     */
    private ByteArrayOutputStream memoryOutputStream;

    /**
     * The output stream to which data will be written at any given time. This will always be one of
     * {@code memoryOutputStream} or {@code diskOutputStream}.
     */
    private OutputStream currentOutputStream;

    /**
     * The file to which output will be directed if the threshold is exceeded.
     */
    private Path outputPath;

    /**
     * The temporary file prefix.
     */
    private final String prefix;

    /**
     * The temporary file suffix.
     */
    private final String suffix;

    /**
     * The directory to use for temporary files.
     */
    private final Path directory;

    /**
     * True when close() has been called successfully.
     */
    private boolean closed;

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a
     * file beyond that point. The initial buffer size will default to
     * {@value AbstractByteArrayOutputStream#DEFAULT_SIZE} bytes which is ByteArrayOutputStream's default buffer size.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param outputFile The file to which data is saved beyond the threshold.
     */
    public DeferredFileOutputStream(final int threshold, final File outputFile) {
        this(threshold, outputFile, null, null, null, AbstractByteArrayOutputStream.DEFAULT_SIZE);
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data either
     * to a file beyond that point.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param outputFile The file to which data is saved beyond the threshold.
     * @param prefix Prefix to use for the temporary file.
     * @param suffix Suffix to use for the temporary file.
     * @param directory Temporary file directory.
     * @param initialBufferSize The initial size of the in memory buffer.
     */
    private DeferredFileOutputStream(final int threshold, final File outputFile, final String prefix,
            final String suffix, final File directory, final int initialBufferSize) {
        super(threshold);
        this.outputPath = toPath(outputFile, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = toPath(directory, PathUtils::getTempDirectory);

        memoryOutputStream = new ByteArrayOutputStream(initialBufferSize);
        currentOutputStream = memoryOutputStream;
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a
     * file beyond that point.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @param outputFile The file to which data is saved beyond the threshold.
     *
     * @since 2.5
     */
    public DeferredFileOutputStream(final int threshold, final int initialBufferSize, final File outputFile) {
        this(threshold, outputFile, null, null, null, initialBufferSize);
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a
     * temporary file beyond that point.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @param prefix Prefix to use for the temporary file.
     * @param suffix Suffix to use for the temporary file.
     * @param directory Temporary file directory.
     *
     * @since 2.5
     */
    public DeferredFileOutputStream(final int threshold, final int initialBufferSize, final String prefix,
        final String suffix, final File directory) {
        this(threshold, null, prefix, suffix, directory, initialBufferSize);
        Objects.requireNonNull(prefix, "prefix");
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a
     * temporary file beyond that point. The initial buffer size will default to 32 bytes which is
     * ByteArrayOutputStream's default buffer size.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param prefix Prefix to use for the temporary file.
     * @param suffix Suffix to use for the temporary file.
     * @param directory Temporary file directory.
     *
     * @since 1.4
     */
    public DeferredFileOutputStream(final int threshold, final String prefix, final String suffix,
        final File directory) {
        this(threshold, null, prefix, suffix, directory, AbstractByteArrayOutputStream.DEFAULT_SIZE);
        Objects.requireNonNull(prefix, "prefix");
    }

    /**
     * Closes underlying output stream, and mark this as closed
     *
     * @throws IOException if an error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }

    /**
     * Gets the data for this output stream as an array of bytes, assuming that the data has been retained in memory.
     * If the data was written to disk, this method returns {@code null}.
     *
     * @return The data for this output stream, or {@code null} if no such data is available.
     */
    public byte[] getData() {
        return memoryOutputStream != null ? memoryOutputStream.toByteArray() : null;
    }

    /**
     * Gets either the output file specified in the constructor or the temporary file created or null.
     * <p>
     * If the constructor specifying the file is used then it returns that same output file, even when threshold has not
     * been reached.
     * <p>
     * If constructor specifying a temporary file prefix/suffix is used then the temporary file created once the
     * threshold is reached is returned If the threshold was not reached then {@code null} is returned.
     *
     * @return The file for this output stream, or {@code null} if no such file exists.
     */
    public File getFile() {
        return outputPath != null ? outputPath.toFile() : null;
    }

    /**
     * Gets the current output stream. This may be memory based or disk based, depending on the current state with
     * respect to the threshold.
     *
     * @return The underlying output stream.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    protected OutputStream getStream() throws IOException {
        return currentOutputStream;
    }

    /**
     * Tests whether or not the data for this output stream has been retained in memory.
     *
     * @return {@code true} if the data is available in memory; {@code false} otherwise.
     */
    public boolean isInMemory() {
        return !isThresholdExceeded();
    }

    /**
     * Switches the underlying output stream from a memory based stream to one that is backed by disk. This is the point
     * at which we realize that too much data is being written to keep in memory, so we elect to switch to disk-based
     * storage.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    protected void thresholdReached() throws IOException {
        if (prefix != null) {
            outputPath = Files.createTempFile(directory, prefix, suffix);
        }
        PathUtils.createParentDirectories(outputPath);
        final OutputStream fos = Files.newOutputStream(outputPath);
        try {
            memoryOutputStream.writeTo(fos);
        } catch (final IOException e) {
            fos.close();
            throw e;
        }
        currentOutputStream = fos;
        memoryOutputStream = null;
    }

    /**
     * Converts the current contents of this byte stream to an {@link InputStream}.
     * If the data for this output stream has been retained in memory, the
     * returned stream is backed by buffers of {@code this} stream,
     * avoiding memory allocation and copy, thus saving space and time.<br>
     * Otherwise, the returned stream will be one that is created from the data
     * that has been committed to disk.
     *
     * @return the current contents of this output stream.
     * @throws IOException if this stream is not yet closed or an error occurs.
     * @see org.apache.commons.io.output.ByteArrayOutputStream#toInputStream()
     *
     * @since 2.9.0
     */
    public InputStream toInputStream() throws IOException {
        // we may only need to check if this is closed if we are working with a file
        // but we should force the habit of closing whether we are working with
        // a file or memory.
        if (!closed) {
            throw new IOException("Stream not closed");
        }

        if (isInMemory()) {
            return memoryOutputStream.toInputStream();
        }
        return Files.newInputStream(outputPath);
    }

    private Path toPath(final File file, final Supplier<Path> defaultPathSupplier) {
        return file != null ? file.toPath() : defaultPathSupplier == null ? null : defaultPathSupplier.get();
    }

    /**
     * Writes the data from this output stream to the specified output stream, after it has been closed.
     *
     * @param outputStream output stream to write to.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException if this stream is not yet closed or an error occurs.
     */
    public void writeTo(final OutputStream outputStream) throws IOException {
        // we may only need to check if this is closed if we are working with a file
        // but we should force the habit of closing whether we are working with
        // a file or memory.
        if (!closed) {
            throw new IOException("Stream not closed");
        }

        if (isInMemory()) {
            memoryOutputStream.writeTo(outputStream);
        } else {
            Files.copy(outputPath, outputStream);
        }
    }
}
