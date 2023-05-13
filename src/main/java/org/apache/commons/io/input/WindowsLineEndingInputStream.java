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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.CR;
import static org.apache.commons.io.IOUtils.EOF;
import static org.apache.commons.io.IOUtils.LF;

import java.io.IOException;
import java.io.InputStream;

/**
 * A filtering input stream that ensures the content will have Windows line endings, CRLF.
 *
 * @since 2.5
 */
public class WindowsLineEndingInputStream  extends InputStream {

    private boolean atEos;

    private boolean atSlashCr;

    private boolean atSlashLf;

    private final InputStream in;

    private boolean injectSlashLf;

    private final boolean lineFeedAtEndOfFile;

    /**
     * Creates an input stream that filters another stream
     *
     * @param in                        The input stream to wrap
     * @param ensureLineFeedAtEndOfFile true to ensure that the file ends with CRLF
     */
    public WindowsLineEndingInputStream(final InputStream in, final boolean ensureLineFeedAtEndOfFile) {
        this.in = in;
        this.lineFeedAtEndOfFile = ensureLineFeedAtEndOfFile;
    }

    /**
     * Closes the stream. Also closes the underlying stream.
     *
     * @throws IOException upon error
     */
    @Override
    public void close() throws IOException {
        super.close();
        in.close();
    }

    /**
     * Handles the end of stream condition.
     *
     * @return The next char to output to the stream
     */
    private int handleEos() {
        if (!lineFeedAtEndOfFile) {
            return EOF;
        }
        if (!atSlashLf && !atSlashCr) {
            atSlashCr = true;
            return CR;
        }
        if (!atSlashLf) {
            atSlashCr = false;
            atSlashLf = true;
            return LF;
        }
        return EOF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mark(final int readlimit) {
        throw UnsupportedOperationExceptions.mark();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (atEos) {
            return handleEos();
        }
        if (injectSlashLf) {
            injectSlashLf = false;
            return LF;
        }
        final boolean prevWasSlashR = atSlashCr;
        final int target = readWithUpdate();
        if (atEos) {
            return handleEos();
        }
        if (target == LF && !prevWasSlashR) {
            injectSlashLf = true;
            return CR;
        }
        return target;
    }

    /**
     * Reads the next item from the target, updating internal flags in the process
     * @return the next int read from the target stream
     * @throws IOException upon error
     */
    private int readWithUpdate() throws IOException {
        final int target = this.in.read();
        atEos = target == EOF;
        if (atEos) {
            return target;
        }
        atSlashCr = target == CR;
        atSlashLf = target == LF;
        return target;
    }
}
