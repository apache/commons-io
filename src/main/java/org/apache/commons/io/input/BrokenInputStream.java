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
import java.util.function.Supplier;

/**
 * Always throws an {@link IOException} from all the {@link InputStream} methods where the exception is declared.
 * <p>
 * This class is mostly useful for testing error handling.
 * </p>
 *
 * @since 2.0
 */
public class BrokenInputStream extends InputStream {

    /**
     * The singleton instance.
     *
     * @since 2.12.0
     */
    public static final BrokenInputStream INSTANCE = new BrokenInputStream();

    /**
     * A supplier for the exception that is thrown by all methods of this class.
     */
    private final Supplier<IOException> exceptionSupplier;

    /**
     * Creates a new stream that always throws an {@link IOException}.
     */
    public BrokenInputStream() {
        this(() -> new IOException("Broken input stream"));
    }

    /**
     * Creates a new stream that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenInputStream(final IOException exception) {
        this(() -> exception);
    }

    /**
     * Creates a new stream that always throws an {@link IOException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @since 2.12.0
     */
    public BrokenInputStream(final Supplier<IOException> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    /**
     * Throws the configured exception.
     *
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public int available() throws IOException {
        throw exceptionSupplier.get();
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public void close() throws IOException {
        throw exceptionSupplier.get();
    }

    /**
     * Throws the configured exception.
     *
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public int read() throws IOException {
        throw exceptionSupplier.get();
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always thrown
     */
    @Override
    public synchronized void reset() throws IOException {
        throw exceptionSupplier.get();
    }

    /**
     * Throws the configured exception.
     *
     * @param n ignored
     * @return nothing
     * @throws IOException always thrown
     */
    @Override
    public long skip(final long n) throws IOException {
        throw exceptionSupplier.get();
    }

}
