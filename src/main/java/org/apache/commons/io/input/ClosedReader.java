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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.IOException;
import java.io.Reader;

/**
 * Closed reader. This reader returns EOF to all attempts to read something from it.
 * <p>
 * Typically uses of this class include testing for corner cases in methods that accept readers and acting as a sentinel
 * value instead of a {@code null} reader.
 * </p>
 *
 * @since 2.7
 */
public class ClosedReader extends Reader {

    /**
     * A singleton.
     */
    public static final ClosedReader CLOSED_READER = new ClosedReader();

    /**
     * Returns -1 to indicate that the stream is closed.
     *
     * @param cbuf ignored
     * @param off  ignored
     * @param len  ignored
     * @return always -1
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) {
        return EOF;
    }

    @Override
    public void close() throws IOException {
        // noop
    }

}
