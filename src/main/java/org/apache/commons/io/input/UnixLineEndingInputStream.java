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
public class UnixLineEndingInputStream extends AbstractLineEndingInputStream {

    /**
     * Constructs an input stream that filters another stream
     *
     * @param inputStream                        The input stream to wrap.
     * @param lineFeedAtEos true to ensure that the file ends with LF.
     */
    public UnixLineEndingInputStream(final InputStream inputStream, final boolean lineFeedAtEos) {
        super(inputStream, lineFeedAtEos);
    }

    /**
     * Handles the end of stream condition.
     *
     * @param previousWasSlashCr Indicates if the last seen was a {@code \r}.
     * @return The next char to output to the stream.
     */
    private int handleEos(final boolean previousWasSlashCr) {
        if (previousWasSlashCr || !lineFeedAtEos) {
            return EOF;
        }
        if (!atSlashLf) {
            atSlashLf = true;
            return LF;
        }
        return EOF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read() throws IOException {
        final boolean previousWasSlashR = atSlashCr;
        if (atEos) {
            return handleEos(previousWasSlashR);
        }
        final int target = readUpdate();
        if (atEos) {
            return handleEos(previousWasSlashR);
        }
        if (atSlashCr) {
            return LF;
        }

        if (previousWasSlashR && atSlashLf) {
            return read();
        }

        return target;
    }

    /**
     * Reads the next item from the target, updating internal flags in the process
     *
     * @return the next int read from the target stream.
     * @throws IOException If an I/O error occurs.
     */
    private int readUpdate() throws IOException {
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
