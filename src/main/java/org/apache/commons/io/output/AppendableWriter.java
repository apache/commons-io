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

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Writer implementation that writes the data to an {@link Appendable}
 * Object.
 * <p>
 * For example, can be used with a {@link java.lang.StringBuilder}
 * or {@link java.lang.StringBuffer}.
 * </p>
 *
 * @since 2.7
 * @see java.lang.Appendable
 *
 * @param <T> The type of the {@link Appendable} wrapped by this AppendableWriter.
 */
public class AppendableWriter <T extends Appendable> extends Writer {

    private final T appendable;

    /**
     * Constructs a new instance with the specified appendable.
     *
     * @param appendable the appendable to write to
     */
    public AppendableWriter(final T appendable) {
        this.appendable = appendable;
    }

    /**
     * Appends the specified character to the underlying appendable.
     *
     * @param c the character to append
     * @return this writer
     * @throws IOException upon error
     */
    @Override
    public Writer append(final char c) throws IOException {
        appendable.append(c);
        return this;
    }

    /**
     * Appends the specified character sequence to the underlying appendable.
     *
     * @param csq the character sequence to append
     * @return this writer
     * @throws IOException upon error
     */
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        appendable.append(csq);
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to the underlying appendable.
     *
     * @param csq the character sequence from which a subsequence will be appended
     * @param start the index of the first character in the subsequence
     * @param end the index of the character following the last character in the subsequence
     * @return this writer
     * @throws IOException upon error
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        appendable.append(csq, start, end);
        return this;
    }

    /**
     * Closes the stream. This implementation does nothing.
     *
     * @throws IOException upon error
     */
    @Override
    public void close() throws IOException {
        // noop
    }

    /**
     * Flushes the stream. This implementation does nothing.
     *
     * @throws IOException upon error
     */
    @Override
    public void flush() throws IOException {
        // noop
    }

    /**
     * Return the target appendable.
     *
     * @return the target appendable
     */
    public T getAppendable() {
        return appendable;
    }

    /**
     * Writes a portion of an array of characters to the underlying appendable.
     *
     * @param cbuf an array with the characters to write
     * @param off offset from which to start writing characters
     * @param len number of characters to write
     * @throws IOException upon error
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        Objects.requireNonNull(cbuf, "Character array is missing");
        if (len < 0 || (off + len) > cbuf.length) {
            throw new IndexOutOfBoundsException("Array Size=" + cbuf.length +
                    ", offset=" + off + ", length=" + len);
        }
        for (int i = 0; i < len; i++) {
            appendable.append(cbuf[off + i]);
        }
    }

    /**
     * Writes a character to the underlying appendable.
     *
     * @param c the character to write
     * @throws IOException upon error
     */
    @Override
    public void write(final int c) throws IOException {
        appendable.append((char)c);
    }

    /**
     * Writes a portion of a String to the underlying appendable.
     *
     * @param str a string
     * @param off offset from which to start writing characters
     * @param len number of characters to write
     * @throws IOException upon error
     */
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        // appendable.append will add "null" for a null String; add an explicit null check
        Objects.requireNonNull(str, "String is missing");
        appendable.append(str, off, off + len);
    }

}
