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
import java.io.OutputStream;

/**
 * Throws an IOException on all attempts to write to the stream.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept an output stream and acting as a sentinel value instead of a
 * {@code null} output stream.
 * </p>
 *
 * @since 1.4
 */
public class ClosedOutputStream extends OutputStream {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final ClosedOutputStream INSTANCE = new ClosedOutputStream();

    /**
     * The singleton instance.
     *
     * @deprecated Use {@link #INSTANCE}.
     */
    @Deprecated
    public static final ClosedOutputStream CLOSED_OUTPUT_STREAM = INSTANCE;

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
     * Throws an {@link IOException} to indicate that the stream is closed.
     *
     * @param b   ignored
     * @param off ignored
     * @param len ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException {
        throw new IOException("write(byte[], int, int) failed: stream is closed");
    }

    /**
     * Throws an {@link IOException} to indicate that the stream is closed.
     *
     * @param b ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(final int b) throws IOException {
        throw new IOException("write(int) failed: stream is closed");
    }
}
