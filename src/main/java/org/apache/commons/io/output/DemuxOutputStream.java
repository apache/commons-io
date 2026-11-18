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

package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Forwards data to a stream that has been associated with this thread.
 */
public class DemuxOutputStream extends OutputStream {

    private final InheritableThreadLocal<OutputStream> outputStreamThreadLocal = new InheritableThreadLocal<>();

    /**
     * Construct a new instance.
     */
    public DemuxOutputStream() {
        // empty
    }

    /**
     * Binds the specified stream to the current thread.
     *
     * @param output The stream to bind.
     * @return The OutputStream that was previously active.
     */
    public OutputStream bindStream(final OutputStream output) {
        final OutputStream stream = outputStreamThreadLocal.get();
        outputStreamThreadLocal.set(output);
        return stream;
    }

    /**
     * Closes stream associated with current thread.
     *
     * @throws IOException Thrown if an error occurs.
     */
    @SuppressWarnings("resource") // we actually close the stream here
    @Override
    public void close() throws IOException {
        IOUtils.close(outputStreamThreadLocal.get());
    }

    /**
     * Flushes stream associated with current thread.
     *
     * @throws IOException Thrown if an error occurs.
     */
    @Override
    public void flush() throws IOException {
        @SuppressWarnings("resource")
        final OutputStream output = outputStreamThreadLocal.get();
        if (output != null) {
            output.flush();
        }
    }

    /**
     * Writes bytes to the stream associated with the current thread.
     *
     * <p>
     * After validation, zero-length writes return without accessing the bound stream.
     * </p>
     *
     * <p>
     * Instances of this class delegate nonempty writes in a single call, or discard them when no stream is bound.
     * For compatibility, subclasses dispatch each byte through {@link #write(int)}.
     * </p>
     *
     * @param b   The bytes to write.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException               if an I/O error occurs.
     * @throws NullPointerException      if {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException if the offset or length is negative, or the range exceeds the array bounds.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        IOUtils.checkFromIndexSize(b, off, len);
        if (len == 0) {
            return;
        }
        if (getClass() != DemuxOutputStream.class) {
            // Preserve write(int) overrides in existing subclasses.
            super.write(b, off, len);
            return;
        }
        @SuppressWarnings("resource")
        final OutputStream output = outputStreamThreadLocal.get();
        if (output != null) {
            output.write(b, off, len);
        }
    }

    /**
     * Writes byte to stream associated with current thread.
     *
     * @param ch The byte to write to stream.
     * @throws IOException Thrown if an error occurs.
     */
    @Override
    public void write(final int ch) throws IOException {
        @SuppressWarnings("resource")
        final OutputStream output = outputStreamThreadLocal.get();
        if (output != null) {
            output.write(ch);
        }
    }
}
