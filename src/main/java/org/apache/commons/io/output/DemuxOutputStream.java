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
 * Forwards data to a stream that has been associated with this thread.
 *
 */
public class DemuxOutputStream extends OutputStream {
    private final InheritableThreadLocal<OutputStream> outputStreamThreadLocal = new InheritableThreadLocal<>();

    /**
     * Binds the specified stream to the current thread.
     *
     * @param output
     *            the stream to bind
     * @return the OutputStream that was previously active
     */
    public OutputStream bindStream(final OutputStream output) {
        final OutputStream stream = outputStreamThreadLocal.get();
        outputStreamThreadLocal.set(output);
        return stream;
    }

    /**
     * Closes stream associated with current thread.
     *
     * @throws IOException
     *             if an error occurs
     */
    @Override
    public void close() throws IOException {
        final OutputStream output = outputStreamThreadLocal.get();
        if (null != output) {
            output.close();
        }
    }

    /**
     * Flushes stream associated with current thread.
     *
     * @throws IOException
     *             if an error occurs
     */
    @Override
    public void flush() throws IOException {
        @SuppressWarnings("resource")
        final OutputStream output = outputStreamThreadLocal.get();
        if (null != output) {
            output.flush();
        }
    }

    /**
     * Writes byte to stream associated with current thread.
     *
     * @param ch
     *            the byte to write to stream
     * @throws IOException
     *             if an error occurs
     */
    @Override
    public void write(final int ch) throws IOException {
        @SuppressWarnings("resource")
        final OutputStream output = outputStreamThreadLocal.get();
        if (null != output) {
            output.write(ch);
        }
    }
}
