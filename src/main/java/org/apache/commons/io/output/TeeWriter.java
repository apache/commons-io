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

/**
 * Classic splitter of Writer. Named after the unix 'tee'
 * command. It allows a writer to be branched off so there
 * are now two writers.
 *
 * @since 2.7
 */
public class TeeWriter extends ProxyWriter {

    /** the second Writer to write to */
    protected Writer branch; //TODO consider making this private

    /**
     * Constructs a TeeWriter.
     * @param out the main Writer
     * @param branch the second Writer
     */
    public TeeWriter(final Writer out, final Writer branch) {
        super(out);
        this.branch = branch;
    }

    /**
     * Appends a character to both writers.
     * @param c the character to append
     * @return this writer
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized Writer append(final char c) throws IOException {
        super.append(c);
        this.branch.append(c);
        return this;
    }

    /**
     * Appends a character sequence to both writers.
     * @param csq the character sequence to append
     * @return this writer
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Writer append(final CharSequence csq) throws IOException {
        super.append(csq);
        this.branch.append(csq);
        return this;
    }

    /**
     * Appends a portion of a character sequence to both writers.
     * @param csq the character sequence to append
     * @param start the index of the first character to append
     * @param end the index of the last character to write (exclusive)
     * @return this writer
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        super.append(csq, start, end);
        this.branch.append(csq, start, end);
        return this;
    }

    /**
     * Writes a character to both writers.
     * @param c the character to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(final int c) throws IOException {
        super.write(c);
        this.branch.write(c);
    }

    /**
     * Write the characters to both writers.
     * @param cbuf the characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(final char[] cbuf) throws IOException {
        super.write(cbuf);
        this.branch.write(cbuf);
    }

    /**
     * Write the specified characters to both writers.
     * @param cbuf the characters to write
     * @param off the start offset
     * @param len the number of characters to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(final char[] cbuf, final int off, final int len) throws IOException {
        super.write(cbuf, off, len);
        this.branch.write(cbuf, off, len);
    }

    /**
     * Flushes both writers.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        super.flush();
        this.branch.flush();
    }

    /**
     * Closes both output writers.
     *
     * If closing the main writer throws an exception, attempt to close the branch writer.
     *
     * If closing the main and branch writers both throw exceptions, which exceptions is thrown by this method is
     * currently unspecified and subject to change.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            this.branch.close();
        }
    }

}
