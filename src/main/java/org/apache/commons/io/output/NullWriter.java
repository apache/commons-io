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
     * Does nothing - output to {@code /dev/null}.
     * @param c The character to write
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(final char c) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param csq The character sequence to write
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq) {
        //to /dev/null
        return this;
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param csq The character sequence to write
     * @param start The index of the first character to write
     * @param end  The index of the first character to write (exclusive)
     * @return this writer
     * @since 2.0
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) {
        //to /dev/null
        return this;
    }

    /** @see java.io.Writer#close() */
    @Override
    public void close() {
        //to /dev/null
    }

    /** @see java.io.Writer#flush() */
    @Override
    public void flush() {
        //to /dev/null
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param chr The characters to write
     */
    @Override
    public void write(final char[] chr) {
        //to /dev/null
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param chr The characters to write
     * @param st The start offset
     * @param end The number of characters to write
     */
    @Override
    public void write(final char[] chr, final int st, final int end) {
        //to /dev/null
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param b The character to write.
     */
    @Override
    public void write(final int b) {
        //to /dev/null
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param str The string to write
     */
    @Override
    public void write(final String str) {
        //to /dev/null
    }

    /**
     * Does nothing - output to {@code /dev/null}.
     * @param str The string to write
     * @param st The start offset
     * @param end The number of characters to write
     */
    @Override
    public void write(final String str, final int st, final int end) {
        //to /dev/null
    }

}
