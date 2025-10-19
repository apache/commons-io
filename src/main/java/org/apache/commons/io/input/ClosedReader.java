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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

/**
 * Always returns {@link IOUtils#EOF} to all attempts to read something from it.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept readers and acting as a sentinel
 * value instead of a {@code null} reader.
 * </p>
 *
 * @since 2.7
 */
public class ClosedReader extends Reader {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final ClosedReader INSTANCE = new ClosedReader();

    /**
     * The singleton instance.
     *
     * @deprecated {@link #INSTANCE}.
     */
    @Deprecated
    public static final ClosedReader CLOSED_READER = INSTANCE;

    /**
     * Construct a new instance.
     */
    public ClosedReader() {
        // empty
    }

    @Override
    public void close() throws IOException {
        // noop
    }

    /**
     * A no-op read method that always indicates end-of-stream.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>If {@code len == 0}, returns {@code 0} immediately (no characters are read).</li>
     *   <li>Otherwise, always returns {@value IOUtils#EOF} to signal that the stream is closed or at end-of-stream.</li>
     * </ul>
     *
     * @param cbuf The destination buffer.
     * @param off  The offset at which to start storing characters.
     * @param len  The maximum number of characters to read.
     * @return {@code 0} if {@code len == 0}; otherwise always {@value IOUtils#EOF}.
     * @throws IndexOutOfBoundsException If {@code off < 0}, {@code len < 0}, or {@code off + len > cbuf.length}.
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) {
        IOUtils.checkFromIndexSize(cbuf, off, len);
        if (len == 0) {
            return 0;
        }
        return EOF;
    }

}
