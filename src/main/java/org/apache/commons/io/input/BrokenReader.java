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
import java.io.Reader;
import java.util.function.Supplier;

/**
 * Always throws an {@link IOException} or a {@link RuntimeException} from all {@link Reader} methods where {@link IOException} is declared.
 * <p>
 * This class is mostly useful for testing error handling in code that uses a {@link Reader}.
 * </p>
 *
 * @since 2.7
 */
public class BrokenReader extends Reader {

    /**
     * A singleton instance using a default IOException.
     *
     * @since 2.12.0
     */
    public static final BrokenReader INSTANCE = new BrokenReader();

    /**
     * Constructs a new reader that always throws a {@link RuntimeException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @return a new reader that always throws a {@link RuntimeException}.
     */
    public static BrokenReader brokenReader(final Supplier<RuntimeException> exceptionSupplier) {
        return new BrokenReader(() -> {
            throw exceptionSupplier.get();
        });
    }

    @FunctionalInterface
    private interface ExceptionThrower {
        void doThrow() throws IOException;
    }

    /**
     * A function that throws the exception that is thrown by all methods of this class.
     */
    private final ExceptionThrower exceptionThrower;

    /**
     * Constructs a new reader that always throws an {@link IOException}.
     */
    public BrokenReader() {
        this(() -> new IOException("Broken reader"));
    }

    /**
     * Constructs a new reader that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenReader(final IOException exception) {
        this(() -> exception);
    }

    /**
     * Constructs a new reader that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenReader(final RuntimeException exception) {
        this(() -> {
            throw exception;
        });
    }

    /**
     * Constructs a new reader that always throws an {@link IOException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @since 2.12.0
     */
    public BrokenReader(final Supplier<IOException> exceptionSupplier) {
        this((ExceptionThrower) () -> {
            throw exceptionSupplier.get();
        });
    }

    private BrokenReader(final ExceptionThrower exceptionThrower) {
        this.exceptionThrower = exceptionThrower;
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException as configured.
     */
    @Override
    public void close() throws IOException {
        exceptionThrower.doThrow();
    }

    /**
     * Throws the configured exception.
     *
     * @param readAheadLimit ignored.
     * @throws IOException as configured.
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        exceptionThrower.doThrow();
    }

    /**
     * Throws the configured exception.
     *
     * @param cbuf ignored.
     * @param off  ignored.
     * @param len  ignored.
     * @return nothing.
     * @throws IOException as configured.
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        exceptionThrower.doThrow();
        return 0;
    }

    /**
     * Throws the configured exception.
     *
     * @return nothing.
     * @throws IOException as configured.
     */
    @Override
    public boolean ready() throws IOException {
        exceptionThrower.doThrow();
        return super.ready();
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException as configured.
     */
    @Override
    public void reset() throws IOException {
        exceptionThrower.doThrow();
    }

    /**
     * Throws the configured exception.
     *
     * @param n ignored.
     * @return nothing.
     * @throws IOException as configured.
     */
    @Override
    public long skip(final long n) throws IOException {
        exceptionThrower.doThrow();
        return super.skip(n);
    }

}
