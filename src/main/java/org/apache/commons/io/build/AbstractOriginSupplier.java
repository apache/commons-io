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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.build.AbstractOrigin.FileOrigin;
import org.apache.commons.io.build.AbstractOrigin.InputStreamOrigin;
import org.apache.commons.io.build.AbstractOrigin.OutputStreamOrigin;
import org.apache.commons.io.build.AbstractOrigin.PathOrigin;
import org.apache.commons.io.build.AbstractOrigin.ReaderOrigin;
import org.apache.commons.io.build.AbstractOrigin.URIOrigin;
import org.apache.commons.io.build.AbstractOrigin.WriterOrigin;

/**
 * Abstracts building a typed instance of {@code T}.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractOriginSupplier<T, B extends AbstractOriginSupplier<T, B>> extends AbstractSupplier<T, B> {

    /**
     * Throws {@link IllegalArgumentException} if the initialBufferSize is less than zero.
     *
     * @param initialBufferSize the value to test.
     * @return the input value.
     */
    protected static int checkBufferSize(final int initialBufferSize) {
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be at least 0.");
        }
        return initialBufferSize;
    }

    /**
     * Creates a new file origin for a file.
     *
     * @param origin the file.
     * @return a new file origin
     */
    protected static FileOrigin newFileOrigin(final File origin) {
        return new FileOrigin(origin);
    }

    /**
     * Creates a new file origin for a file path.
     *
     * @param origin the file path.
     * @return a new file origin
     */
    protected static FileOrigin newFileOrigin(final String origin) {
        return new FileOrigin(new File(origin));
    }

    /**
     * Creates a new input stream origin for a file.
     *
     * @param origin the input stream.
     * @return a new input stream origin
     */
    protected static InputStreamOrigin newInputStreamOrigin(final InputStream origin) {
        return new InputStreamOrigin(origin);
    }

    /**
     * Creates a new output stream origin for a file.
     *
     * @param origin the output stream.
     * @return a new output stream origin
     */
    protected static OutputStreamOrigin newOutputStreamOrigin(final OutputStream origin) {
        return new OutputStreamOrigin(origin);
    }

    /**
     * Creates a new path origin for a file.
     *
     * @param origin the path.
     * @return a new path origin
     */
    protected static PathOrigin newPathOrigin(final Path origin) {
        return new PathOrigin(origin);
    }

    /**
     * Creates a new path name origin for a path name.
     *
     * @param origin the path name.
     * @return a new path name origin
     */
    protected static PathOrigin newPathOrigin(final String origin) {
        return new PathOrigin(Paths.get(origin));
    }

    /**
     * Creates a new reader origin for a reader.
     *
     * @param origin the reader.
     * @return a new reader origin
     */
    protected static ReaderOrigin newReaderOrigin(final Reader origin) {
        return new ReaderOrigin(origin);
    }

    /**
     * Creates a new reader origin for a URI.
     *
     * @param origin the URI.
     * @return a new URI origin
     */
    protected static URIOrigin newURIOrigin(final URI origin) {
        return new URIOrigin(origin);
    }

    /**
     * Creates a new writer origin for a file.
     *
     * @param origin the writer.
     * @return a new writer origin
     */
    protected static WriterOrigin newWriterOrigin(final Writer origin) {
        return new WriterOrigin(origin);
    }

    /**
     * The underlying origin.
     */
    private AbstractOrigin<?, ?> origin;

    /**
     * Checks whether the origin is null.
     *
     * @return the origin.
     * @throws NullPointerException if the {@code origin} is {@code null}
     */
    protected AbstractOrigin<?, ?> checkOrigin() {
        return Objects.requireNonNull(origin, "origin");
    }

    /**
     * Gets the origin.
     *
     * @return the origin.
     */
    protected AbstractOrigin<?, ?> getOrigin() {
        return origin;
    }

    /**
     * Tests whether the origin is null.
     *
     * @return whether the origin is null.
     */
    protected boolean hasOrigin() {
        return origin != null;
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setFile(final File origin) {
        return setOrigin(newFileOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setFile(final String origin) {
        return setOrigin(newFileOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setInputStream(final InputStream origin) {
        return setOrigin(newInputStreamOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    protected B setOrigin(final AbstractOrigin<?, ?> origin) {
        this.origin = origin;
        return asThis();
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setOutputStream(final OutputStream origin) {
        return setOrigin(newOutputStreamOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setPath(final Path origin) {
        return setOrigin(newPathOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setPath(final String origin) {
        return setOrigin(newPathOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setReader(final Reader origin) {
        return setOrigin(newReaderOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setURI(final URI origin) {
        return setOrigin(newURIOrigin(origin));
    }

    /**
     * Sets a new origin.
     *
     * @param origin the new origin.
     * @return this
     */
    public B setWriter(final Writer origin) {
        return setOrigin(newWriterOrigin(origin));
    }
}
