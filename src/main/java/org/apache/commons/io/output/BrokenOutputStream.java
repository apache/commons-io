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
 * Broken output stream. This stream always throws an {@link IOException} from
 * all {@link OutputStream} methods.
 * <p>
 * This class is mostly useful for testing error handling in code that uses an
 * output stream.
 *
 * @since 2.0
 */
public class BrokenOutputStream extends OutputStream {

    /**
     * The exception that is thrown by all methods of this class.
     */
    private final IOException exception;

    /**
     * Creates a new stream that always throws the given exception.
     *
     * @param exception the exception to be thrown
     */
    public BrokenOutputStream(IOException exception) {
        this.exception = exception;
    }

    /**
     * Creates a new stream that always throws an {@link IOException}
     */
    public BrokenOutputStream() {
        this(new IOException("Broken output stream"));
    }

    /**
     * Throws the configured exception.
     *
     * @param b ignored
     * @throws IOException always thrown
     */
    @Override
    public void write(int b) throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public void flush() throws IOException {
        throw exception;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public void close() throws IOException {
        throw exception;
    }

}
