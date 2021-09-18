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
 * Throws an IOException on all attempts to write with {@link #close()} implemented as a noop.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept a writer and acting as a
 * sentinel value instead of a {@code null} writer.
 * </p>
 *
 * @since 2.7
 */
public class ClosedWriter extends Writer {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final ClosedWriter INSTANCE = new ClosedWriter();

    /**
     * The singleton instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public static final ClosedWriter CLOSED_WRITER = INSTANCE;

    @Override
    public void close() throws IOException {
        // noop
    }

    /**
     * Throws an {@link IOException} to indicate that the stream is closed.
     *
     * @throws IOException always thrown
     */
    @Override
    public void flush() throws IOException {
        throw new IOException("flush() failed: stream is closed");
    }

    /**
     * Throws an {@link IOException} to indicate that the writer is closed.
     *
     * @param cbuf ignored
     * @param off ignored
     * @param len ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        throw new IOException("write(" + new String(cbuf) + ", " + off + ", " + len + ") failed: stream is closed");
    }
}
