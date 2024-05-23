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

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.file.PathUtils;

/**
 * An output stream which will retain data in memory until a specified threshold is reached, and only then commit it to disk. If the stream is closed before the
 * threshold is reached, the data will not be written to disk at all.
 * <p>
 * To build an instance, use {@link Builder}.
 * </p>
 * <p>
 * The caller is responsible for deleting the output file ({@link #getFile()}, {@link #getPath()}) created by a DeferredFileOutputStream when the caller only
 * configured a prefix.
 * </p>
 * <p>
 * The caller is responsible for deleting the output file passed to a constructor or builder through {@link Builder#setOutputFile(File)} or
 * {@link Builder#setOutputFile(Path)}.
 * </p>
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not know in advance the size of the file being uploaded. If the file is small you
 * want to store it in memory (for speed), but if the file is large you want to store it to file (to avoid memory issues).
 * </p>
 *
 * @see Builder
 */
public class DeferredFileOutputStream extends ThresholdingOutputStream {

    // @formatter:off
    /**
     * Builds a new {@link DeferredFileOutputStream}.
     * <p>
     * For example:
     * </p>
     * <pre>{@code
     * DeferredFileOutputStream s = DeferredFileOutputStream.builder()
     *   .setBufferSize(4096)
     *   .setDirectory(dir)
     *   .setOutputFile(outputFile)
     *   .setPrefix(prefix)
     *   .setSuffix(suffix)
     *   .setThreshold(threshold)
     *   .get();}
     * </pre>
     * <p>
     * The only super's aspect used us buffer size.
     * </p>
     *
     * @see #get()
     * @since 2.12.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<DeferredFileOutputStream, Builder> {

        private int threshold;
        private Path outputFile;
        private String prefix;
        private String suffix;
        private Path directory;

        /**
         * Constructs a new builder.
         */
        public Builder() {
            setBufferSizeDefault(AbstractByteArrayOutputStream.DEFAULT_SIZE);
            setBufferSize(AbstractByteArrayOutputStream.DEFAULT_SIZE);
        }

        /**
         * Builds a new {@link DeferredFileOutputStream}.
         * <p>
         * This builder use the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getBufferSize()}</li>
         * <li>threshold</li>
         * <li>outputFile</li>
         * <li>prefix</li>
         * <li>suffix</li>
         * <li>directory</li>
         * </ul>
         *
         * @return a new instance.
         */
        @Override
        public DeferredFileOutputStream get() {
            return new DeferredFileOutputStream(threshold, outputFile, prefix, suffix, directory, getBufferSize());
        }

        /**
         * Sets the temporary file directory.
         *
         * @param directory Temporary file directory.
         * @return {@code this} instance.
         */
        public Builder setDirectory(final File directory) {
            this.directory = toPath(directory, null);
            return this;
        }

        /**
         * Sets the temporary file directory.
         *
         * @param directory Temporary file directory.
         * @return {@code this} instance.
         * @since 2.14.0
         */
        public Builder setDirectory(final Path directory) {
            this.directory = toPath(directory, null);
            return this;
        }

        /**
         * Sets the file to which data is saved beyond the threshold.
         *
         * @param outputFile The file to which data is saved beyond the threshold.
         * @return {@code this} instance.
         */
        public Builder setOutputFile(final File outputFile) {
            this.outputFile = toPath(outputFile, null);
            return this;
        }

        /**
         * Sets the file to which data is saved beyond the threshold.
         *
         * @param outputFile The file to which data is saved beyond the threshold.
         * @return {@code this} instance.
         * @since 2.14.0
         */
        public Builder setOutputFile(final Path outputFile) {
            this.outputFile = toPath(outputFile, null);
            return this;
        }

        /**
         * Sets the prefix to use for the temporary file.
         *
         * @param prefix Prefix to use for the temporary file.
         * @return {@code this} instance.
         */
        public Builder setPrefix(final String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the suffix to use for the temporary file.
         *
         * @param suffix Suffix to use for the temporary file.
         * @return {@code this} instance.
         */
        public Builder setSuffix(final String suffix) {
            this.suffix = suffix;
            return this;
        }

        /**
         * Sets the number of bytes at which to trigger an event.
         *
         * @param threshold The number of bytes at which to trigger an event.
         * @return {@code this} instance.
         */
        public Builder setThreshold(final int threshold) {
            this.threshold = threshold;
            return this;
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

    private static int checkBufferSize(final int initialBufferSize) {
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
        return initialBufferSize;
    }

    private static Path toPath(final File file, final Supplier<Path> defaultPathSupplier) {
        return file != null ? file.toPath() : defaultPathSupplier == null ? null : defaultPathSupplier.get();
    }

    private static Path toPath(final Path file, final Supplier<Path> defaultPathSupplier) {
        return file != null ? file : defaultPathSupplier == null ? null : defaultPathSupplier.get();
    }

    /**
     * The output stream to which data will be written prior to the threshold being reached.
     */
    private ByteArrayOutputStream memoryOutputStream;

    /**
     * The output stream to which data will be written at any given time. This will always be one of {@code memoryOutputStream} or {@code diskOutputStream}.
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
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a file beyond that point. The initial
     * buffer size will default to {@value AbstractByteArrayOutputStream#DEFAULT_SIZE} bytes which is ByteArrayOutputStream's default buffer size.
     *
     * @param threshold  The number of bytes at which to trigger an event.
     * @param outputFile The file to which data is saved beyond the threshold.
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public DeferredFileOutputStream(final int threshold, final File outputFile) {
        this(threshold, outputFile, null, null, null, AbstractByteArrayOutputStream.DEFAULT_SIZE);
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data either to a file beyond that point.
     *
     * @param threshold         The number of bytes at which to trigger an event.
     * @param outputFile        The file to which data is saved beyond the threshold.
     * @param prefix            Prefix to use for the temporary file.
     * @param suffix            Suffix to use for the temporary file.
     * @param directory         Temporary file directory.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @throws IllegalArgumentException if initialBufferSize &lt; 0.
     */
    private DeferredFileOutputStream(final int threshold, final File outputFile, final String prefix, final String suffix, final File directory,
            final int initialBufferSize) {
        super(threshold);
        this.outputPath = toPath(outputFile, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = toPath(directory, PathUtils::getTempDirectory);
        this.memoryOutputStream = new ByteArrayOutputStream(checkBufferSize(initialBufferSize));
        this.currentOutputStream = memoryOutputStream;
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a file beyond that point.
     *
     * @param threshold         The number of bytes at which to trigger an event.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @param outputFile        The file to which data is saved beyond the threshold.
     * @since 2.5
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public DeferredFileOutputStream(final int threshold, final int initialBufferSize, final File outputFile) {
        this(threshold, outputFile, null, null, null, initialBufferSize);
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a temporary file beyond that point.
     *
     * @param threshold         The number of bytes at which to trigger an event.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @param prefix            Prefix to use for the temporary file.
     * @param suffix            Suffix to use for the temporary file.
     * @param directory         Temporary file directory.
     * @since 2.5
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public DeferredFileOutputStream(final int threshold, final int initialBufferSize, final String prefix, final String suffix, final File directory) {
        this(threshold, null, Objects.requireNonNull(prefix, "prefix"), suffix, directory, initialBufferSize);
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data either to a file beyond that point.
     *
     * @param threshold         The number of bytes at which to trigger an event.
     * @param outputFile        The file to which data is saved beyond the threshold.
     * @param prefix            Prefix to use for the temporary file.
     * @param suffix            Suffix to use for the temporary file.
     * @param directory         Temporary file directory.
     * @param initialBufferSize The initial size of the in memory buffer.
     * @throws IllegalArgumentException if initialBufferSize &lt; 0.
     */
    private DeferredFileOutputStream(final int threshold, final Path outputFile, final String prefix, final String suffix, final Path directory,
            final int initialBufferSize) {
        super(threshold);
        this.outputPath = toPath(outputFile, null);
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = toPath(directory, PathUtils::getTempDirectory);
        this.memoryOutputStream = new ByteArrayOutputStream(checkBufferSize(initialBufferSize));
        this.currentOutputStream = memoryOutputStream;
    }

    /**
     * Constructs an instance of this class which will trigger an event at the specified threshold, and save data to a temporary file beyond that point. The
     * initial buffer size will default to 32 bytes which is ByteArrayOutputStream's default buffer size.
     *
     * @param threshold The number of bytes at which to trigger an event.
     * @param prefix    Prefix to use for the temporary file.
     * @param suffix    Suffix to use for the temporary file.
     * @param directory Temporary file directory.
     * @since 1.4
     * @deprecated Use {@link #builder()}, {@link Builder}, and {@link Builder#get()}
     */
    @Deprecated
    public DeferredFileOutputStream(final int threshold, final String prefix, final String suffix, final File directory) {
        this(threshold, null, Objects.requireNonNull(prefix, "prefix"), suffix, directory, AbstractByteArrayOutputStream.DEFAULT_SIZE);
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
     * Gets the data for this output stream as an array of bytes, assuming that the data has been retained in memory. If the data was written to disk, this
     * method returns {@code null}.
     *
     * @return The data for this output stream, or {@code null} if no such data is available.
     */
    public byte[] getData() {
        return memoryOutputStream != null ? memoryOutputStream.toByteArray() : null;
    }

    /**
     * Gets either the output File specified in the constructor or the temporary File created or null.
     * <p>
     * If the constructor specifying the File is used then it returns that same output File, even when threshold has not been reached.
     * </p>
     * <p>
     * If constructor specifying a temporary File prefix/suffix is used then the temporary File created once the threshold is reached is returned if the
     * threshold was not reached then {@code null} is returned.
     * </p>
     *
     * @return The File for this output stream, or {@code null} if no such File exists.
     */
    public File getFile() {
        return outputPath != null ? outputPath.toFile() : null;
    }

    /**
     * Gets either the output Path specified in the constructor or the temporary Path created or null.
     * <p>
     * If the constructor specifying the file is used then it returns that same output Path, even when threshold has not been reached.
     * </p>
     * <p>
     * If constructor specifying a temporary Path prefix/suffix is used then the temporary Path created once the threshold is reached is returned if the
     * threshold was not reached then {@code null} is returned.
     * </p>
     *
     * @return The Path for this output stream, or {@code null} if no such Path exists.
     * @since 2.14.0
     */
    public Path getPath() {
        return outputPath;
    }

    /**
     * Gets the current output stream. This may be memory based or disk based, depending on the current state with respect to the threshold.
     *
     * @return The underlying output stream.
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
     * Switches the underlying output stream from a memory based stream to one that is backed by disk. This is the point at which we realize that too much data
     * is being written to keep in memory, so we elect to switch to disk-based storage.
     *
     * @throws IOException if an error occurs.
     */
    @Override
    protected void thresholdReached() throws IOException {
        if (prefix != null) {
            outputPath = Files.createTempFile(directory, prefix, suffix);
        }
        PathUtils.createParentDirectories(outputPath, null, PathUtils.EMPTY_FILE_ATTRIBUTE_ARRAY);
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
     * Converts the current contents of this byte stream to an {@link InputStream}. If the data for this output stream has been retained in memory, the returned
     * stream is backed by buffers of {@code this} stream, avoiding memory allocation and copy, thus saving space and time.<br>
     * Otherwise, the returned stream will be one that is created from the data that has been committed to disk.
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

    /**
     * Writes the data from this output stream to the specified output stream, after it has been closed.
     *
     * @param outputStream output stream to write to.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException          if this stream is not yet closed or an error occurs.
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
