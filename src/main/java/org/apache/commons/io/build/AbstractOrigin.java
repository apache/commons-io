/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.build;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IORandomAccessFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.channels.ByteArraySeekableByteChannel;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.RandomAccessFileOutputStream;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * Abstracts and wraps an <em>origin</em> for builders, where an origin is a {@code byte[]}, {@link Channel}, {@link CharSequence}, {@link File},
 * {@link InputStream}, {@link IORandomAccessFile}, {@link OutputStream}, {@link Path}, {@link RandomAccessFile}, {@link Reader}, {@link URI},
 * or {@link Writer}.
 * <p>
 * An origin represents where bytes/characters come from or go to. Concrete subclasses
 * expose only the operations that make sense for the underlying source or sink; invoking an unsupported operation
 * results in {@link UnsupportedOperationException} (see, for example, {@link #getFile()} and {@link #getPath()}).
 * </p>
 * <p>
 * An instance doesn't own its origin, it holds on to it to allow conversions. There are two use cases related to resource management for a Builder:
 * </p>
 * <ul>
 * <li>
 * A client allocates a {@linkplain Closeable} (or {@linkplain AutoCloseable}) resource, creates a Builder, and gives the Builder that resource by calling a
 * setter method. No matter what happens next, the client is responsible for releasing the resource ({@code Closeable.close()}). In this case, the origin
 * wraps but doesn't own the closeable resource. There is no transfer of ownership.
 * </li>
 * <li>
 * A client creates a Builder and gives it a non-Closeable object, like a File or a Path. The client then calls the Builder's factory method
 * (like {@linkplain #get()}), and that call returns a Closeable or a resource that requires releasing in some other way. No matter what happens next, the
 * client is responsible for releasing that resource. In this case, the origin doesn't wrap a closeable resource.
 * </li>
 * </ul>
 * <p>
 * In both cases, the client causes the allocation and is responsible for releasing the resource.
 * </p>
 * <p>
 * The table below summarizes which views and conversions are supported for each origin type.
 * Column headers show the target view; cells indicate whether that view is available from the origin in that row.
 * </p>
 *
 * <table>
 *   <caption>Supported Conversions</caption>
 *   <thead>
 *     <tr>
 *       <th>Origin Type</th>
 *       <th>byte[]</th>
 *       <th>CS</th>
 *       <th>File</th>
 *       <th>Path</th>
 *       <th>RAF</th>
 *       <th>IS</th>
 *       <th>Reader</th>
 *       <th>RBC</th>
 *       <th>OS</th>
 *       <th>Writer</th>
 *       <th>WBC</th>
 *       <th>Channel type<sup>2</sup></th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>byte[]</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>SBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link CharSequence} (CS)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>✔</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>SBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link File}</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>FC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Path}</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>FC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link IORandomAccessFile}</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>FC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link RandomAccessFile} (RAF)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>FC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link InputStream} (IS)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>RBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Reader}</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>✔</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>RBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ReadableByteChannel} (RBC)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>RBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link OutputStream} (OS)</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>WBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link Writer}</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>✔</td>
 *       <td>✔<sup>1</sup></td>
 *       <td>WBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WritableByteChannel} (WBC)</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>WBC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link URI} (FileSystem)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>FC</td>
 *     </tr>
 *     <tr>
 *       <td>{@link URI} (http/https)</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>✖</td>
 *       <td>RBC</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <p><strong>Legend</strong></p>
 * <ul>
 *   <li>✔ = Supported</li>
 *   <li>✖ = Not supported (throws {@link UnsupportedOperationException})</li>
 *   <li><sup>1</sup> = Characters are converted to bytes using the default {@link Charset}.</li>
 *   <li><sup>2</sup> Minimum channel type provided by the origin:
 *     <ul>
 *         <li>RBC = {@linkplain ReadableByteChannel}</li>
 *         <li>WBC = {@linkplain WritableByteChannel}</li>
 *         <li>SBC = {@linkplain SeekableByteChannel}</li>
 *         <li>FC = {@linkplain FileChannel}</li>
 *     </ul>
 *     The exact channel type may be a subtype of the minimum shown.
 *   </li>
 * </ul>
 *
 * @param <T> the type produced by the builder.
 * @param <B> the concrete builder subclass type.
 * @since 2.12.0
 */
public abstract class AbstractOrigin<T, B extends AbstractOrigin<T, B>> extends AbstractSupplier<T, B> {

    /**
     * A {@link RandomAccessFile} origin.
     * <p>
     * This origin cannot support File and Path since you cannot query a RandomAccessFile for those attributes; Use {@link IORandomAccessFileOrigin}
     * instead.
     * </p>
     *
     * @param <T> the type of instances to build.
     * @param <B> the type of builder subclass.
     */
    public abstract static class AbstractRandomAccessFileOrigin<T extends RandomAccessFile, B extends AbstractRandomAccessFileOrigin<T, B>>
            extends AbstractOrigin<T, B> {

        /**
         * A {@link RandomAccessFile} origin.
         * <p>
         * Starting from this origin, you can everything except a Path and a File.
         * </p>
         *
         * @param origin The origin, not null.
         */
        public AbstractRandomAccessFileOrigin(final T origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            final long longLen = origin.length();
            if (longLen > Integer.MAX_VALUE) {
                throw new IllegalStateException("Origin too large.");
            }
            return RandomAccessFiles.read(origin, 0, (int) longLen);
        }

        @Override
        public byte[] getByteArray(final long position, final int length) throws IOException {
            return RandomAccessFiles.read(origin, position, length);
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return getRandomAccessFile(options).getChannel();
        }

        @Override
        public CharSequence getCharSequence(final Charset charset) throws IOException {
            return new String(getByteArray(), charset);
        }

        @SuppressWarnings("resource")
        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            return BufferedFileChannelInputStream.builder().setFileChannel(origin.getChannel()).get();
        }

        @Override
        public OutputStream getOutputStream(final OpenOption... options) throws IOException {
            return RandomAccessFileOutputStream.builder().setRandomAccessFile(origin).get();
        }

        @Override
        public T getRandomAccessFile(final OpenOption... openOption) {
            // No conversion
            return get();
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return new InputStreamReader(getInputStream(), Charsets.toCharset(charset));
        }

        @Override
        public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
            return new OutputStreamWriter(getOutputStream(options), Charsets.toCharset(charset));
        }

        @Override
        public long size() throws IOException {
            return origin.length();
        }
    }

    /**
     * A {@code byte[]} origin.
     */
    public static class ByteArrayOrigin extends AbstractOrigin<byte[], ByteArrayOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin, not null.
         */
        public ByteArrayOrigin(final byte[] origin) {
            super(origin);
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * No conversion should occur when calling this method.
         * </p>
         */
        @Override
        public byte[] getByteArray() {
            // No conversion
            return get();
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            for (final OpenOption option : options) {
                if (option == StandardOpenOption.WRITE) {
                    throw new UnsupportedOperationException("Only READ is supported for byte[] origins: " + Arrays.toString(options));
                }
            }
            return ByteArraySeekableByteChannel.wrap(getByteArray());
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
            return new InputStreamReader(getInputStream(), Charsets.toCharset(charset));
        }

        @Override
        public long size() throws IOException {
            return origin.length;
        }

    }

    /**
     * A {@link Channel} origin.
     *
     * @since 2.21.0
     */
    public static class ChannelOrigin extends AbstractOrigin<Channel, ChannelOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin, not null.
         */
        public ChannelOrigin(final Channel origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            return IOUtils.toByteArray(getInputStream());
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * No conversion should occur when calling this method.
         * </p>
         */
        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            // No conversion
            return get();
        }

        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            return Channels.newInputStream(getChannel(ReadableByteChannel.class, options));
        }

        @Override
        public OutputStream getOutputStream(final OpenOption... options) throws IOException {
            return Channels.newOutputStream(getChannel(WritableByteChannel.class, options));
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return Channels.newReader(
                    getChannel(ReadableByteChannel.class),
                    Charsets.toCharset(charset).newDecoder(),
                    -1);
        }

        @Override
        public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
            return Channels.newWriter(getChannel(WritableByteChannel.class, options), Charsets.toCharset(charset).newEncoder(), -1);
        }

        @Override
        public long size() throws IOException {
            if (origin instanceof SeekableByteChannel) {
                return ((SeekableByteChannel) origin).size();
            }
            throw unsupportedOperation("size");
        }
    }

    /**
     * A {@link CharSequence} origin.
     */
    public static class CharSequenceOrigin extends AbstractOrigin<CharSequence, CharSequenceOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin, not null.
         */
        public CharSequenceOrigin(final CharSequence origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return origin.toString().getBytes(Charset.defaultCharset());
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            for (final OpenOption option : options) {
                if (option == StandardOpenOption.WRITE) {
                    throw new UnsupportedOperationException("Only READ is supported for CharSequence origins: " + Arrays.toString(options));
                }
            }
            return ByteArraySeekableByteChannel.wrap(getByteArray());
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code charset} parameter is ignored since a {@link CharSequence} does not need a {@link Charset} to be read.
         * </p>
         * <p>
         * No conversion should occur when calling this method.
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
         * @param origin The origin, not null.
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
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Files.newByteChannel(getPath(), options);
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * No conversion should occur when calling this method.
         * </p>
         */
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
         * @param origin The origin, not null.
         */
        public InputStreamOrigin(final InputStream origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            return IOUtils.toByteArray(origin);
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Channels.newChannel(getInputStream(options));
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link InputStream} does not need an {@link OpenOption} to be read.
         * </p>
         * <p>
         * No conversion should occur when calling this method.
         * </p>
         */
        @Override
        public InputStream getInputStream(final OpenOption... options) {
            // No conversion
            return get();
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return new InputStreamReader(getInputStream(), Charsets.toCharset(charset));
        }

        @Override
        public long size() throws IOException {
            if (origin instanceof FileInputStream) {
                return ((FileInputStream) origin).getChannel().size();
            }
            throw unsupportedOperation("size");
        }
    }

    /**
     * An {@link IORandomAccessFile} origin.
     *
     * @since 2.18.0
     */
    public static class IORandomAccessFileOrigin extends AbstractRandomAccessFileOrigin<IORandomAccessFile, IORandomAccessFileOrigin> {

        /**
         * A {@link RandomAccessFile} origin.
         *
         * @param origin The origin, not null.
         */
        public IORandomAccessFileOrigin(final IORandomAccessFile origin) {
            super(origin);
        }

        @SuppressWarnings("resource")
        @Override
        public File getFile() {
            return get().getFile();
        }

        @Override
        public Path getPath() {
            return getFile().toPath();
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
         * @param origin The origin, not null.
         */
        public OutputStreamOrigin(final OutputStream origin) {
            super(origin);
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Channels.newChannel(getOutputStream(options));
        }

        /**
         * {@inheritDoc}
         * <p>
         * The {@code options} parameter is ignored since a {@link OutputStream} does not need an {@link OpenOption} to be written.
         * </p>
         * <p>
         * No conversion should occur when calling this method.
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
            return new OutputStreamWriter(origin, Charsets.toCharset(charset));
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
         * @param origin The origin, not null.
         */
        public PathOrigin(final Path origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray(final long position, final int length) throws IOException {
            return RandomAccessFileMode.READ_ONLY.apply(origin, raf -> RandomAccessFiles.read(raf, position, length));
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Files.newByteChannel(getPath(), options);
        }

        @Override
        public File getFile() {
            return get().toFile();
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * No conversion should occur when calling this method.
         * </p>
         */
        @Override
        public Path getPath() {
            // No conversion
            return get();
        }
    }

    /**
     * A {@link RandomAccessFile} origin.
     * <p>
     * This origin cannot support File and Path since you cannot query a RandomAccessFile for those attributes; Use {@link IORandomAccessFileOrigin}
     * instead.
     * </p>
     */
    public static class RandomAccessFileOrigin extends AbstractRandomAccessFileOrigin<RandomAccessFile, RandomAccessFileOrigin> {

        /**
         * A {@link RandomAccessFile} origin.
         * <p>
         * Starting from this origin, you can everything except a Path and a File.
         * </p>
         *
         * @param origin The origin, not null.
         */
        public RandomAccessFileOrigin(final RandomAccessFile origin) {
            super(origin);
        }

    }

    /**
     * A {@link Reader} origin.
     * <p>
     * This origin cannot provide conversions to other aspects.
     * </p>
     */
    public static class ReaderOrigin extends AbstractOrigin<Reader, ReaderOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin, not null.
         */
        public ReaderOrigin(final Reader origin) {
            super(origin);
        }

        @Override
        public byte[] getByteArray() throws IOException {
            // TODO Pass in a Charset? Consider if call sites actually need this.
            return IOUtils.toByteArray(origin, Charset.defaultCharset());
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Channels.newChannel(getInputStream());
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
         * <p>
         * No conversion should occur when calling this method.
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
         * @param origin The origin, not null.
         */
        public URIOrigin(final URI origin) {
            super(origin);
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            final URI uri = get();
            final String scheme = uri.getScheme();
            if (SCHEME_HTTP.equalsIgnoreCase(scheme) || SCHEME_HTTPS.equalsIgnoreCase(scheme)) {
                return Channels.newChannel(uri.toURL().openStream());
            }
            return Files.newByteChannel(getPath(), options);
        }

        @Override
        public File getFile() {
            return getPath().toFile();
        }

        @Override
        public InputStream getInputStream(final OpenOption... options) throws IOException {
            final URI uri = get();
            final String scheme = uri.getScheme();
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
     * A {@link Writer} origin.
     * <p>
     * This origin cannot provide conversions to other aspects.
     * </p>
     */
    public static class WriterOrigin extends AbstractOrigin<Writer, WriterOrigin> {

        /**
         * Constructs a new instance for the given origin.
         *
         * @param origin The origin, not null.
         */
        public WriterOrigin(final Writer origin) {
            super(origin);
        }

        @Override
        protected Channel getChannel(final OpenOption... options) throws IOException {
            return Channels.newChannel(getOutputStream());
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
         * <p>
         * No conversion should occur when calling this method.
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
     * Constructs a new instance for subclasses.
     *
     * @param origin The origin, not null.
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
     * Gets a portion of this origin as a byte array, if possible.
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
     * Gets this origin as a Channel of the given type, if possible.
     *
     * @param channelType The type of channel to return.
     * @param options Options specifying how a file-based origin is opened, ignored otherwise.
     * @return A new Channel on the origin of the given type.
     * @param <C> The type of channel to return.
     * @throws IOException                   If an I/O error occurs.
     * @throws UnsupportedOperationException If this origin cannot be converted to a channel of the given type.
     * @since 2.21.0
     */
    public final <C extends Channel> C getChannel(final Class<C> channelType, final OpenOption... options) throws IOException {
        Objects.requireNonNull(channelType, "channelType");
        final Channel channel = getChannel(options);
        if (channelType.isInstance(channel)) {
            return channelType.cast(channel);
        }
        throw unsupportedChannelType(channelType);
    }

    /**
     * Gets this origin as a Channel, if possible.
     *
     * @param options Options specifying how a file-based origin is opened, ignored otherwise.
     * @return A new Channel on the origin.
     * @throws IOException                   If an I/O error occurs.
     * @throws UnsupportedOperationException If this origin cannot be converted to a channel.
     * @since 2.21.0
     */
    protected Channel getChannel(final OpenOption... options) throws IOException {
        throw unsupportedOperation("getChannel");
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
     * Gets this origin as a File, if possible.
     *
     * @return this origin as a File, if possible.
     * @throws UnsupportedOperationException if this method is not implemented in a concrete subclass.
     */
    public File getFile() {
        throw unsupportedOperation("getFile");
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
        throw unsupportedOperation("getPath");
    }

    /**
     * Gets this origin as a RandomAccessFile, if possible.
     *
     * @param openOption options like {@link StandardOpenOption}.
     * @return this origin as a RandomAccessFile, if possible.
     * @throws FileNotFoundException         See {@link RandomAccessFile#RandomAccessFile(File, String)}.
     * @throws UnsupportedOperationException if this method is not implemented in a concrete subclass.
     * @since 2.18.0
     */
    public RandomAccessFile getRandomAccessFile(final OpenOption... openOption) throws FileNotFoundException {
        return RandomAccessFileMode.valueOf(openOption).create(getFile());
    }

    /**
     * Gets a new Reader on the origin, buffered by default.
     *
     * @param charset the charset to use for decoding, null maps to the default Charset.
     * @return a new Reader on the origin.
     * @throws IOException if an I/O error occurs opening the file.
     */
    public Reader getReader(final Charset charset) throws IOException {
        return Files.newBufferedReader(getPath(), Charsets.toCharset(charset));
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
        return Files.newBufferedWriter(getPath(), Charsets.toCharset(charset), options);
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

    UnsupportedOperationException unsupportedChannelType(final Class<? extends Channel> channelType) {
        return new UnsupportedOperationException(String.format(
                "%s#getChannel(%s) for %s origin %s",
                getSimpleClassName(),
                channelType.getSimpleName(),
                origin.getClass().getSimpleName(),
                origin));
    }

    UnsupportedOperationException unsupportedOperation(final String method) {
        return new UnsupportedOperationException(String.format(
                "%s#%s() for %s origin %s",
                getSimpleClassName(), method, origin.getClass().getSimpleName(), origin));
    }
}
