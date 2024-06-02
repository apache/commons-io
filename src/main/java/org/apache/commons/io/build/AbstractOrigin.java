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

package org.apache.commons.io.build;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.file.spi.FileSystemProviders;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * Abstracts the origin of data for builders like a {@link File}, {@link Path}, {@link Reader}, {@link Writer}, {@link InputStream}, {@link OutputStream}, and
 * {@link URI}.
 * <p>
 * Some methods may throw {@link UnsupportedOperationException} if that method is not implemented in a concrete subclass, see {@link #getFile()} and
 * {@link #getPath()}.
 * </p>
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractOrigin<T, B extends AbstractOrigin<T, B>> extends AbstractSupplier<T, B> {

    /**
     * A {@code byte[]} origin.
     */
    public static class ByteArrayOrigin extends AbstractOrigin<byte[], ByteArrayOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public ByteArrayOrigin(final byte[] origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() {
            // No conversion
            return get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@code byte[]} does not need an {@link OpenOption} to be read.
         * </p>
         */
        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            return new ByteArrayInputStream(origin);
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return new InputStreamReader(getInputStream(), charset);
        }

        @Override
        public long size() throws IOException {
            return origin.length;
        }

    }

    /**
     * A {@link CharSequence} origin.
     */
    public static class CharSequenceOrigin extends AbstractOrigin<CharSequence, CharSequenceOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public CharSequenceOrigin(final CharSequence origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return origin.toString().getBytes(Charset.defaultCharset());
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link CharSequence} does not need a {@link Charset} to be read.
         * </p>
         */
        @Override
        public CharSequence getCharSequence(final Charset charset) {
            // No conversion
            return get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link CharSequence} does not need an {@link OpenOption} to be read.
         * </p>
         */
        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return CharSequenceInputStream.builder().setCharSequence(getCharSequence(Charset.defaultCharset())).get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link CharSequence} does not need a {@link Charset} to be read.
         * </p>
         */
        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return new CharSequenceReader(get());
        }

        @Override
        public long size() throws IOException {
            return origin.length();
        }

    }

    /**
     * A {@link File} origin.
     * <p>
     * Starting from this origin, you can get a byte array, a file, an input stream, an output stream, a path, a reader, and a writer.
     * </p>
     */
    public static class FileOrigin extends AbstractOrigin<File, FileOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public FileOrigin(final File origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray(final long position, final int length) throws IOException {
            try (RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(origin)) {
                return RandomAccessFiles.read(raf, position, length);
            }
        }

        @Override
        public File getFile() {
            // No conversion
            return get();
        }

        @Override
        public Path getPath() {
            return get().toPath();
        }

    }

    /**
     * An {@link InputStream} origin.
     * <p>
     * This origin cannot provide some of the other aspects.
     * </p>
     */
    public static class InputStreamOrigin extends AbstractOrigin<InputStream, InputStreamOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public InputStreamOrigin(final InputStream origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            return IOUtils.toByteArray(origin);
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link InputStream} does not need an {@link OpenOption} to be read.
         * </p>
         */
        @Override
        public InputStream getInputStream(final OpenOption... options) {
            // No conversion
            return get();
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return new InputStreamReader(getInputStream(), charset);
        }

    }

    /**
     * An {@link OutputStream} origin.
     * <p>
     * This origin cannot provide some of the other aspects.
     * </p>
     */
    public static class OutputStreamOrigin extends AbstractOrigin<OutputStream, OutputStreamOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public OutputStreamOrigin(final OutputStream origin) {
            super(origin);
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link OutputStream} does not need an {@link OpenOption} to be written.
         * </p>
         */
        @Override
        public OutputStream getOutputStream(final OpenOption... options) {
            // No conversion
            return get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link OutputStream} does not need an {@link OpenOption} to be written.
         * </p>
         */
        @Override
        public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
            return new OutputStreamWriter(origin, charset);
        }
    }

    /**
     * A {@link Path} origin.
     * <p>
     * Starting from this origin, you can get a byte array, a file, an input stream, an output stream, a path, a reader, and a writer.
     * </p>
     */
    public static class PathOrigin extends AbstractOrigin<Path, PathOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public PathOrigin(final Path origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray(final long position, final int length) throws IOException {
            try (RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(origin)) {
                return RandomAccessFiles.read(raf, position, length);
            }
        }

        @Override
        public File getFile() {
            return get().toFile();
        }

        @Override
        public Path getPath() {
            // No conversion
            return get();
        }

    }

    /**
     * An {@link Reader} origin.
     * <p>
     * This origin cannot provide other aspects.
     * </p>
     */
    public static class ReaderOrigin extends AbstractOrigin<Reader, ReaderOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public ReaderOrigin(final Reader origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return IOUtils.toByteArray(origin, Charset.defaultCharset());
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link Reader} does not need a {@link Charset} to be read.
         * </p>
         */
        @Override
        public CharSequence getCharSequence(final Charset charset) throws IOException {
            return IOUtils.toString(origin);
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link Reader} does not need an {@link OpenOption} to be read.
         * </p>
         */
        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return ReaderInputStream.builder().setReader(origin).setCharset(Charset.defaultCharset()).get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link Reader} does not need a {@link Charset} to be read.
         * </p>
         */
        @Override
        public Reader getReader(final Charset charset) throws IOException {
            // No conversion
            return get();
        }
    }

    /**
     * A {@link URI} origin.
     */
    public static class URIOrigin extends AbstractOrigin<URI, URIOrigin> {

        private static final String SCHEME_HTTPS = "https";
        private static final String SCHEME_HTTP = "http";

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public URIOrigin(final URI origin) {
            super(origin);
        }

        @Override
        public File getFile() {
            return getPath().toFile();
        }

        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            final URI uri = get();
            final String scheme = uri.getScheme();
            final FileSystemProvider fileSystemProvider = FileSystemProviders.installed().getFileSystemProvider(scheme);
            if (fileSystemProvider != null) {
                return Files.newInputStream(fileSystemProvider.getPath(uri), options);
            }
            if (SCHEME_HTTP.equalsIgnoreCase(scheme) || SCHEME_HTTPS.equalsIgnoreCase(scheme)) {
                return uri.toURL().openStream();
            }
            return Files.newInputStream(getPath(), options);
        }

        @Override
        public Path getPath() {
            return Paths.get(get());
        }
    }

    /**
     * An {@link Writer} origin.
     * <p>
     * This origin cannot provide other aspects.
     * </p>
     */
    public static class WriterOrigin extends AbstractOrigin<Writer, WriterOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin.
         */
        public WriterOrigin(final Writer origin) {
            super(origin);
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link Writer} does not need an {@link OpenOption} to be written.
         * </p>
         */
        @Override
        public OutputStream getOutputStream(final OpenOption... options) throws IOException {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return WriterOutputStream.builder().setWriter(origin).setCharset(Charset.defaultCharset()).get();
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link Writer} does not need a {@link Charset} to be written.
         * </p>
         * <p>
         * The {@code options} parameter is ignored since a {@link Writer} does not need an {@link OpenOption} to be written.
         * </p>
         */
        @Override
        public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
            // No conversion
            return get();
        }
    }

    /**
     * The non-null origin.
     */
    final T origin;

    /**
     * Constructs a new instance for a subclass.
     *
     * @param origin The origin.
     */
    protected AbstractOrigin(final T origin) {
        this.origin = Objects.requireNonNull(origin, "origin");
    }

    /**
     * Gets the origin.
     *
     * @return the origin.
     */
    @Override
    public T get() {
        return origin;
    }

    /**
     * Gets this origin as a byte array, if possible.
     *
     * @return this origin as a byte array, if possible.
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     */
    public byte[] getByteArray() throws IOException {
        return Files.readAllBytes(getPath());
    }

    /**
     * Gets this origin as a byte array, if possible.
     *
     * @param position the initial index of the range to be copied, inclusive.
     * @param length   How many bytes to copy.
     * @return this origin as a byte array, if possible.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     * @throws ArithmeticException           if the {@code position} overflows an int
     * @throws IOException                   if an I/O error occurs.
     * @since 2.13.0
     */
    public byte[] getByteArray(final long position, final int length) throws IOException {
        final byte[] bytes = getByteArray();
        // Checks for int overflow.
        final int start = Math.toIntExact(position);
        if (start < 0 || length < 0 || start + length < 0 || start + length > bytes.length) {
            throw new IllegalArgumentException("Couldn't read array (start: " + start + ", length: " + length + ", data length: " + bytes.length + ").");
        }
        return Arrays.copyOfRange(bytes, start, start + length);
    }

    /**
     * Gets this origin as a byte array, if possible.
     *
     * @param charset The charset to use if conversion from bytes is needed.
     * @return this origin as a byte array, if possible.
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     */
    public CharSequence getCharSequence(final Charset charset) throws IOException {
        return new String(getByteArray(), charset);
    }

    /**
     * Gets this origin as a Path, if possible.
     *
     * @return this origin as a Path, if possible.
     * @throws UnsupportedOperationException if this method is not implemented in a concrete subclass.
     */
    public File getFile() {
        throw new UnsupportedOperationException(
                String.format("%s#getFile() for %s origin %s", getSimpleClassName(), origin.getClass().getSimpleName(), origin));
    }

    /**
     * Gets this origin as an InputStream, if possible.
     *
     * @param options options specifying how the file is opened
     * @return this origin as an InputStream, if possible.
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     */
    public InputStream getInputStream(final OpenOption... options) throws IOException {
        return Files.newInputStream(getPath(), options);
    }

    /**
     * Gets this origin as an OutputStream, if possible.
     *
     * @param options options specifying how the file is opened
     * @return this origin as an OutputStream, if possible.
     * @throws IOException                   if an I/O error occurs.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     */
    public OutputStream getOutputStream(final OpenOption... options) throws IOException {
        return Files.newOutputStream(getPath(), options);
    }

    /**
     * Gets this origin as a Path, if possible.
     *
     * @return this origin as a Path, if possible.
     * @throws UnsupportedOperationException if this method is not implemented in a concrete subclass.
     */
    public Path getPath() {
        throw new UnsupportedOperationException(
                String.format("%s#getPath() for %s origin %s", getSimpleClassName(), origin.getClass().getSimpleName(), origin));
    }

    /**
     * Gets a new Reader on the origin, buffered by default.
     *
     * @param charset the charset to use for decoding
     * @return a new Reader on the origin.
     * @throws IOException if an I/O error occurs opening the file.
     */
    public Reader getReader(final Charset charset) throws IOException {
        return Files.newBufferedReader(getPath(), charset);
    }

    private String getSimpleClassName() {
        return getClass().getSimpleName();
    }

    /**
     * Gets a new Writer on the origin, buffered by default.
     *
     * @param charset the charset to use for encoding
     * @param options options specifying how the file is opened
     * @return a new Writer on the origin.
     * @throws IOException                   if an I/O error occurs opening or creating the file.
     * @throws UnsupportedOperationException if the origin cannot be converted to a Path.
     */
    public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
        return Files.newBufferedWriter(getPath(), charset, options);
    }

    /**
     * Gets the size of the origin, if possible.
     *
     * @return the size of the origin in bytes or characters.
     * @throws IOException if an I/O error occurs.
     * @since 2.13.0
     */
    public long size() throws IOException {
        return Files.size(getPath());
    }

    @Override
    public String toString() {
        return getSimpleClassName() + "[" + origin.toString() + "]";
    }
}
