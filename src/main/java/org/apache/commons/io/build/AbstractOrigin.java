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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Abstracts the origin of data for builders like a {@link File}, {@link Path}, and so on.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractOrigin<T, B extends AbstractOrigin<T, B>> extends AbstractSupplier<T, B> {

    /**
     * A {@link File} origin.
     */
    public static class FileOrigin extends AbstractOrigin<File, FileOrigin> {

        public FileOrigin(final File origin) {
            super(origin);
        }

        @Override
        public File getFile() {
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
     * This origin cannot provide other aspects.
     * </p>
     */
    public static class InputStreamOrigin extends AbstractOrigin<InputStream, InputStreamOrigin> {

        public InputStreamOrigin(final InputStream origin) {
            super(origin);
        }

        @Override
        public InputStream getInputStream(final OpenOption... options) {
            return get();
        }

    }

    /**
     * An {@link OutputStream} origin.
     * <p>
     * This origin cannot provide other aspects.
     * </p>
     */
    public static class OutputStreamOrigin extends AbstractOrigin<OutputStream, OutputStreamOrigin> {

        public OutputStreamOrigin(final OutputStream origin) {
            super(origin);
        }

        @Override
        public OutputStream getOutputStream(final OpenOption... options) {
            return get();
        }

    }

    /**
     * A {@link Path} origin.
     */
    public static class PathOrigin extends AbstractOrigin<Path, PathOrigin> {

        public PathOrigin(final Path origin) {
            super(origin);
        }

        @Override
        public File getFile() {
            return get().toFile();
        }

        @Override
        public Path getPath() {
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

        public ReaderOrigin(final Reader origin) {
            super(origin);
        }

        @Override
        public Reader getReader(final Charset charset) throws IOException {
            return get();
        }
    }

    /**
     * A {@link URI} origin.
     */
    public static class URIOrigin extends AbstractOrigin<URI, URIOrigin> {

        public URIOrigin(final URI origin) {
            super(origin);
        }

        @Override
        public URI get() {
            return origin;
        }

        @Override
        public File getFile() {
            return getPath().toFile();
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

        public WriterOrigin(final Writer origin) {
            super(origin);
        }

        @Override
        public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
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
     * Gets a new Reader on the origin, buffered by default.
     *
     * @param charset the charset to use for decoding
     * @return a new Reader on the origin.
     * @throws IOException if an I/O error occurs opening the file.
     */
    public Reader getReader(final Charset charset) throws IOException {
        return Files.newBufferedReader(getPath(), charset);
    }

    /**
     * Gets a new Writer on the origin, buffered by default.
     *
     * @param charset the charset to use for encoding
     * @param options options specifying how the file is opened
     * @return a new Writer on the origin.
     * @throws IOException if an I/O error occurs opening or creating the file.
     */
    public Writer getWriter(final Charset charset, final OpenOption... options) throws IOException {
        return Files.newBufferedWriter(getPath(), charset, options);
    }

    /**
     * Gets this origin as a Path, if possible.
     *
     * @return this origin as a Path, if possible.
     */
    public File getFile() {
        throw new UnsupportedOperationException(origin.toString());
    }

    /**
     * Gets this origin as an InputStream, if possible.
     *
     * @param options options specifying how the file is opened
     * @return this origin as an InputStream, if possible.
     * @throws IOException if an I/O error occurs.
     */
    public InputStream getInputStream(final OpenOption... options) throws IOException {
        return Files.newInputStream(getPath(), options);
    }

    /**
     * Gets this origin as an OutputStream, if possible.
     *
     * @param options options specifying how the file is opened
     * @return this origin as an OutputStream, if possible.
     * @throws IOException if an I/O error occurs.
     */
    public OutputStream getOutputStream(final OpenOption... options) throws IOException {
        return Files.newOutputStream(getPath(), options);
    }

    /**
     * Gets this origin as a Path, if possible.
     *
     * @return this origin as a Path\, if possible.
     */
    public Path getPath() {
        throw new UnsupportedOperationException(origin.toString());
    }

    @Override
    public String toString() {
        return origin.toString();
    }
}
