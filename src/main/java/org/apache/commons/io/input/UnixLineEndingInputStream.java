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
 * A filtering input stream that ensures the content will have UNIX-style line endings, LF.
 *
 * @since 2.5
 */
public class UnixLineEndingInputStream extends InputStream {

    private boolean slashNSeen = false;

    private boolean slashRSeen = false;

    private boolean eofSeen = false;

    private final InputStream target;

    private final boolean ensureLineFeedAtEndOfFile;

    /**
     * Creates an input stream that filters another stream
     *
     * @param in                        The input stream to wrap
     * @param ensureLineFeedAtEndOfFile true to ensure that the file ends with LF
     */
    public UnixLineEndingInputStream(final InputStream in, final boolean ensureLineFeedAtEndOfFile) {
        this.target = in;
        this.ensureLineFeedAtEndOfFile = ensureLineFeedAtEndOfFile;
    }

    /**
     * Reads the next item from the target, updating internal flags in the process
     * @return the next int read from the target stream
     * @throws IOException upon error
     */
    private int readWithUpdate() throws IOException {
        final int target = this.target.read();
        eofSeen = target == EOF;
        if (eofSeen) {
            return target;
        }
        slashNSeen = target == LF;
        slashRSeen = target == CR;
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        final boolean previousWasSlashR = slashRSeen;
        if (eofSeen) {
            return eofGame(previousWasSlashR);
        }
        final int target = readWithUpdate();
        if (eofSeen) {
            return eofGame(previousWasSlashR);
        }
        if (slashRSeen) {
            return LF;
        }

        if (previousWasSlashR && slashNSeen) {
            return read();
        }

        return target;
    }

    /**
     * Handles the EOF-handling at the end of the stream
     * @param previousWasSlashR Indicates if the last seen was a \r
     * @return The next char to output to the stream
     */
    private int eofGame(final boolean previousWasSlashR) {
        if (previousWasSlashR || !ensureLineFeedAtEndOfFile) {
            return EOF;
        }
        if (!slashNSeen) {
            slashNSeen = true;
            return LF;
        }
        return EOF;
    }

    /**
     * Closes the stream. Also closes the underlying stream.
     * @throws IOException upon error
     */
    @Override
    public void close() throws IOException {
        super.close();
        target.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mark(final int readlimit) {
        throw new UnsupportedOperationException("Mark not supported");
    }
}
