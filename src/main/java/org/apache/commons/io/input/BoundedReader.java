/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;

/**
 * A reader that imposes a limit to the number of characters that can be read from an underlying reader, returning EOF
 * when this limit is reached, regardless of state of underlying reader.
 *
 * <p>
 * One use case is to avoid overrunning the readAheadLimit supplied to {@link Reader#mark(int)}, since reading
 * too many characters removes the ability to do a successful reset.
 * </p>
 *
 * @since 2.5
 */
public class BoundedReader extends Reader {

    private static final int INVALID = -1;

    private final Reader target;

    private int charsRead;

    private int markedAt = INVALID;

    private int readAheadLimit; // Internally, this value will never exceed the allowed size

    private final int maxCharsFromTargetReader;

    /**
     * Constructs a bounded reader
     *
     * @param target                   The target stream that will be used
     * @param maxCharsFromTargetReader The maximum number of characters that can be read from target
     */
    public BoundedReader(final Reader target, final int maxCharsFromTargetReader) {
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
     * marks the target stream
     *
     * @param readAheadLimit The number of characters that can be read while still retaining the ability to do #reset().
     *                       Note that this parameter is not validated with respect to maxCharsFromTargetReader. There
     *                       is no way to pass past maxCharsFromTargetReader, even if this value is greater.
     *
     * @throws IOException If an I/O error occurs while calling the underlying reader's mark method
     * @see java.io.Reader#mark(int)
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        this.readAheadLimit = readAheadLimit - charsRead;

        markedAt = charsRead;

        target.mark(readAheadLimit);
    }

    /**
     * Reads a single character
     *
     * @return -1 on EOF or the character read
     * @throws IOException If an I/O error occurs while calling the underlying reader's read method
     * @see java.io.Reader#read()
     */
    @Override
    public int read() throws IOException {

        if (charsRead >= maxCharsFromTargetReader) {
            return EOF;
        }

        if (markedAt >= 0 && charsRead - markedAt >= readAheadLimit) {
            return EOF;
        }
        charsRead++;
        return target.read();
    }

    /**
     * Reads into an array
     *
     * @param cbuf The buffer to fill
     * @param off  The offset
     * @param len  The number of chars to read
     * @return the number of chars read
     * @throws IOException If an I/O error occurs while calling the underlying reader's read method
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        int c;
        for (int i = 0; i < len; i++) {
            c = read();
            if (c == EOF) {
                return i == 0 ? EOF : i;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
    }

    /**
     * Resets the target to the latest mark,
     *
     * @throws IOException If an I/O error occurs while calling the underlying reader's reset method
     * @see java.io.Reader#reset()
     */
    @Override
    public void reset() throws IOException {
        charsRead = markedAt;
        target.reset();
    }
}
