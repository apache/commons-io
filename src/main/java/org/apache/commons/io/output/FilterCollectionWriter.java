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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.apache.commons.io.function.IOConsumer;

/**
 * Abstract class for writing filtered character streams to a {@link Collection} of writers. This is in contrast to
 * {@link FilterWriter} which is backed by a single {@link Writer}.
 * <p>
 * This abstract class provides default methods that pass all requests to the contained writers. Subclasses should
 * likely override some of these methods.
 * </p>
 * <p>
 * The class {@link Writer} defines method signatures with {@code throws} {@link IOException}, which in this class are
 * actually {@link IOExceptionList} containing a list of {@link IOIndexedException}.
 * </p>
 *
 * @since 2.7
 */
public class FilterCollectionWriter extends Writer {

    /**
     * Empty and immutable collection of writers.
     */
    protected final Collection<Writer> EMPTY_WRITERS = Collections.emptyList();

    /**
     * The underlying writers.
     */
    protected final Collection<Writer> writers;

    /**
     * Constructs a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    protected FilterCollectionWriter(final Collection<Writer> writers) {
        this.writers = writers == null ? EMPTY_WRITERS : writers;
    }

    /**
     * Constructs a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    protected FilterCollectionWriter(final Writer... writers) {
        this.writers = writers == null ? EMPTY_WRITERS : Arrays.asList(writers);
    }

    @Override
    public Writer append(final char c) throws IOException {
        return forAllWriters(w -> w.append(c));
    }

    @Override
    public Writer append(final CharSequence csq) throws IOException {
        return forAllWriters(w -> w.append(csq));
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        return forAllWriters(w -> w.append(csq, start, end));
    }

    @SuppressWarnings("resource") // no allocation
    @Override
    public void close() throws IOException {
        forAllWriters(Writer::close);
    }

    /**
     * Flushes the stream.
     *
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("resource") // no allocation
    @Override
    public void flush() throws IOException {
        forAllWriters(Writer::flush);
    }

    private FilterCollectionWriter forAllWriters(final IOConsumer<Writer> action) throws IOExceptionList {
        IOConsumer.forAll(action, writers());
        return this;
    }

    @SuppressWarnings("resource") // no allocation
    @Override
    public void write(final char[] cbuf) throws IOException {
        forAllWriters(w -> w.write(cbuf));
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param cbuf Buffer of characters to be written
     * @param off  Offset from which to start reading characters
     * @param len  Number of characters to be written
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("resource") // no allocation
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        forAllWriters(w -> w.write(cbuf, off, len));
    }

    /**
     * Writes a single character.
     *
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("resource") // no allocation
    @Override
    public void write(final int c) throws IOException {
        forAllWriters(w -> w.write(c));
    }

    @SuppressWarnings("resource") // no allocation
    @Override
    public void write(final String str) throws IOException {
        forAllWriters(w -> w.write(str));
    }

    /**
     * Writes a portion of a string.
     *
     * @param str String to be written
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("resource") // no allocation
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        forAllWriters(w -> w.write(str, off, len));
    }

    private Stream<Writer> writers() {
        return writers.stream().filter(Objects::nonNull);
    }

}
