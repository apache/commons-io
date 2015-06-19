/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;

/**
 * A reader that imposes a limit to the number of characters that can be read from
 * an underlying reader, returning eof when this limit is reached -regardless of state of
 * underlying reader.
 *
 * <p>
 * One use case is to avoid overrunning the readAheadLimit supplied to
 * java.io.Reader#mark(int), since reading too many characters removes the
 * ability to do a successful reset.
 * </p>
 *
 * @since 2.5
 */
public class BoundedReader
    extends Reader
{

    private static final int INVALID = -1;

    private final Reader target;

    private int charsRead = 0;

    private int markedAt = INVALID;

    private int readAheadLimit; // Internally, this value will never exceed the allowed size

    private final int maxCharsFromTargetReader;

    /**
     * Constructs a bounded reader
     *
     * @param target                   The target stream that will be used
     * @param maxCharsFromTargetReader The maximum number of characters that can be read from target
     * @throws IOException if mark fails
     */
    public BoundedReader( Reader target, int maxCharsFromTargetReader ) throws IOException {
        this.target = target;
        this.maxCharsFromTargetReader = maxCharsFromTargetReader;
    }

    /**
     * Closes the target
     *
     * @throws IOException If an I/O error occurs while calling the underlying reader's close method
     */
    @Override
    public void close() throws IOException {
        target.close();
    }

    /**
     * Resets the target to the latest mark, @see java.io.Reader#reset()
     *
     * @throws IOException If an I/O error occurs while calling the underlying reader's reset method
     */
    @Override
    public void reset() throws IOException {
        charsRead = markedAt;
        target.reset();
    }

    /**
     * marks the target stream, @see java.io.Reader#mark(int).
     *
     * @param readAheadLimit The number of characters that can be read while
     *                       still retaining the ability to do #reset().
     *                       Note that this parameter is not validated with respect to
     *                       maxCharsFromTargetReader. There is no way to pass
     *                       past maxCharsFromTargetReader, even if this value is
     *                       greater.
     *
     * @throws IOException If an I/O error occurs while calling the underlying reader's mark method
     */
    @Override
    public void mark( int readAheadLimit ) throws IOException {
        this.readAheadLimit = readAheadLimit - charsRead;

        markedAt = charsRead;

        target.mark( readAheadLimit );
    }

    /**
     * Reads a single character, @see java.io.Reader#read()
     *
     * @return -1 on eof or the character read
     * @throws IOException If an I/O error occurs while calling the underlying reader's read method
     */
    @Override
    public int read() throws IOException {

        if ( charsRead >= maxCharsFromTargetReader ) {
            return -1;
        }

        if ( markedAt >= 0 && ( charsRead - markedAt ) >= readAheadLimit ) {
            return -1;
        }
        charsRead++;
        return target.read();
    }

    /**
     * Reads into an array, @see java.io.Reader#read(char[], int, int)
     *
     * @param cbuf The buffer to fill
     * @param off  The offset
     * @param len  The number of chars to read
     * @return the number of chars read
     * @throws IOException If an I/O error occurs while calling the underlying reader's read method
     */
    @Override
    public int read( char[] cbuf, int off, int len ) throws IOException {
        int c;
        for ( int i = 0; i < len; i++ ) {
            c = read();
            if ( c == -1 ) {
                return i;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
    }
}
