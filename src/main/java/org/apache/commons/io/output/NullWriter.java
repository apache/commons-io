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
package org.apache.commons.io.output;

import java.io.Writer;

import org.apache.commons.io.IOUtils;

/**
 * Never writes data. Calls never go beyond this class.
 * <p>
 * This {@link Writer} has no destination (file/socket etc.) and all characters written to it are ignored and lost.
 * </p>
 */
public class NullWriter extends Writer {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final NullWriter INSTANCE = new NullWriter();

    /**
     * The singleton instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public static final NullWriter NULL_WRITER = INSTANCE;

    /**
     * Constructs a new NullWriter.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public NullWriter() {
    }

    /**
     * Does nothing, like writing to {@code /dev/null}.
     *
     * @param c The character to write.
     * @return this writer.
     * @since 2.0
     */
    @Override
    public Writer append(final char c) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing, like writing to {@code /dev/null}.
     *
     * @param csq The character sequence to write.
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing except argument validation, like writing to {@code /dev/null}.
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.
     *              If {@code csq} is {@code null}, it is treated as if it were
     *              {@code "null"}.
     * @param start The index of the first character in the subsequence.
     * @param end   The index of the character following the last character in the
     *              subsequence.
     * @return {@code this} instance.
     * @throws IndexOutOfBoundsException If {@code start} or {@code end} are negative, {@code end} is
     *                                   greater than {@code csq.length()}, or {@code start} is greater
     *                                   than {@code end}.
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) {
        IOUtils.checkFromToIndex(csq, start, end);
        return this;
    }

    /** @see Writer#close() */
    @Override
    public void close() {
        //to /dev/null
    }

    /** @see Writer#flush() */
    @Override
    public void flush() {
        //to /dev/null
    }

    /**
     * Does nothing except argument validation, like writing to {@code /dev/null}.
     *
     * @param chr The characters to write, not {@code null}.
     * @throws NullPointerException if {@code chr} is {@code null}.
     */
    @Override
    public void write(final char[] chr) {
        write(chr, 0, chr.length);
        //to /dev/null
    }

    /**
     * Does nothing except argument validation, like writing to {@code /dev/null}.
     *
     * @param cbuf The characters to write, not {@code null}.
     * @param off  The start offset.
     * @param len  The number of characters to write.
     * @throws NullPointerException      if {@code chr} is {@code null}.
     * @throws IndexOutOfBoundsException If ({@code off} or {@code len} are negative, or {@code off + len} is greater than {@code cbuf.length}.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) {
        IOUtils.checkFromIndexSize(cbuf, off, len);
        //to /dev/null
    }

    /**
     * Does nothing, like writing to {@code /dev/null}.
     *
     * @param b The character to write.
     */
    @Override
    public void write(final int b) {
        //to /dev/null
    }

    /**
     * Does nothing except argument validation, like writing to {@code /dev/null}.
     *
     * @param str The string to write, not {@code null}.
     * @throws NullPointerException if {@code str} is {@code null}.
     */
    @Override
    public void write(final String str) {
        write(str, 0, str.length());
        //to /dev/null
    }

    /**
     * Does nothing except argument validation, like writing to {@code /dev/null}.
     *
     * @param str The string to write, not {@code null}.
     * @param off The start offset.
     * @param len The number of characters to write.
     * @throws NullPointerException      If {@code str} is {@code null}.
     * @throws IndexOutOfBoundsException If ({@code off} or {@code len} are negative, or {@code off + len} is greater than {@code str.length()}.
     */
    @Override
    public void write(final String str, final int off, final int len) {
        IOUtils.checkFromIndexSize(str, off, len);
        //to /dev/null
    }

}
