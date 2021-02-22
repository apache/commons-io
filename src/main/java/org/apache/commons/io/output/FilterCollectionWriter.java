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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;

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
     * Creates a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    protected FilterCollectionWriter(final Collection<Writer> writers) {
        this.writers = writers == null ? EMPTY_WRITERS : writers;
    }

    /**
     * Creates a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    protected FilterCollectionWriter(final Writer... writers) {
        this.writers = writers == null ? EMPTY_WRITERS : Arrays.asList(writers);
    }

    /**
     * Adds an indexed exception to the list.
     *
     * @param causeList The target list.
     * @param i The index.
     * @param e The cause.
     * @return the given list or a new list on null input.
     */
    private List<Exception> add(List<Exception> causeList, final int i, final IOException e) {
        if (causeList == null) {
            causeList = new ArrayList<>();
        }
        causeList.add(new IOIndexedException(i, e));
        return causeList;
    }

    @Override
    public Writer append(final char c) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.append(c);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("append", causeList);
        }
        return this;
    }

    @Override
    public Writer append(final CharSequence csq) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.append(csq);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("append", causeList);
        }
        return this;
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {

        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.append(csq, start, end);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("append", causeList);
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.close();
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("close", causeList);
        }

    }

    /**
     * Flushes the stream.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.flush();
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("flush", causeList);
        }

    }

    /**
     * Tests if the given list is empty in a null-safe manner.
     *
     * @param causeList the list to test.
     * @return true if empty or null.
     */
    private boolean notEmpty(final List<Exception> causeList) {
        return causeList != null && !causeList.isEmpty();
    }

    @Override
    public void write(final char[] cbuf) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.write(cbuf);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("write", causeList);
        }
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param cbuf Buffer of characters to be written
     * @param off  Offset from which to start reading characters
     * @param len  Number of characters to be written
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.write(cbuf, off, len);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("write", causeList);
        }
    }

    /**
     * Writes a single character.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(final int c) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.write(c);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("write", causeList);
        }
    }

    @Override
    public void write(final String str) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.write(str);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("write", causeList);
        }

    }

    /**
     * Writes a portion of a string.
     *
     * @param str String to be written
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        List<Exception> causeList = null;
        int i = 0;
        for (final Writer w : writers) {
            if (w != null) {
                try {
                    w.write(str, off, len);
                } catch (final IOException e) {
                    causeList = add(causeList, i, e);
                }
            }
            i++;
        }
        if (notEmpty(causeList)) {
            throw new IOExceptionList("write", causeList);
        }

    }

}
