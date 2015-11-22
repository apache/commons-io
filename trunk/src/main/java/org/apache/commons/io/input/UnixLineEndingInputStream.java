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


import java.io.IOException;
import java.io.InputStream;

/**
 * A filtering input stream that ensures the content will have unix-style line endings, LF.
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
     * Create an input stream that filters another stream
     *
     * @param in                        The input stream to wrap
     * @param ensureLineFeedAtEndOfFile true to ensure that the file ends with LF
     */
    public UnixLineEndingInputStream( InputStream in, boolean ensureLineFeedAtEndOfFile ) {
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
        eofSeen = target == -1;
        if ( eofSeen ) {
            return target;
        }
        slashNSeen = target == '\n';
        slashRSeen = target == '\r';
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        boolean previousWasSlashR = slashRSeen;
        if ( eofSeen ) {
            return eofGame(previousWasSlashR);
        }
        else {
            int target = readWithUpdate();
            if ( eofSeen ) {
                return eofGame(previousWasSlashR);
            }
            if (slashRSeen)
            {
                return '\n';
            }

            if ( previousWasSlashR && slashNSeen){
                return read();
            }

            return target;
        }
    }

    /**
     * Handles the eof-handling at the end of the stream
     * @param previousWasSlashR Indicates if the last seen was a \r
     * @return The next char to output to the stream
     */
    private int eofGame(boolean previousWasSlashR) {
        if ( previousWasSlashR || !ensureLineFeedAtEndOfFile ) {
            return -1;
        }
        if ( !slashNSeen ) {
            slashNSeen = true;
            return '\n';
        } else {
            return -1;
        }
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
    public synchronized void mark( int readlimit ) {
        throw new UnsupportedOperationException( "Mark notsupported" );
    }
}
