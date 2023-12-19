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
import java.util.function.Supplier;

/**
 * Always throws an {@link IOException} or a {@link RuntimeException} from all {@link Writer} methods where {@link IOException} is declared.
 * <p>
 * This class is mostly useful for testing error handling in code that uses a {@link Writer}.
 * </p>
 *
 * @since 2.0
 */
public class BrokenWriter extends Writer {

    /**
     * A singleton instance using a default IOException.
     *
     * @since 2.12.0
     */
    public static final BrokenWriter INSTANCE = new BrokenWriter();

    /**
     * Constructs a new writer that always throws a {@link RuntimeException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @return a new writer that always throws a {@link RuntimeException}.
     */
    public static BrokenWriter brokenWriter(final Supplier<RuntimeException> exceptionSupplier) {
        return new BrokenWriter(() -> {
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
     * Constructs a new writer that always throws an {@link IOException}.
     */
    public BrokenWriter() {
        this(() -> new IOException("Broken writer"));
    }

    /**
     * Constructs a new writer that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenWriter(final IOException exception) {
        this(() -> exception);
    }

    /**
     * Constructs a new writer that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenWriter(final RuntimeException exception) {
        this(() -> {
            throw exception;
        });
    }

    /**
     * Constructs a new writer that always throws an {@link IOException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @since 2.12.0
     */
    public BrokenWriter(final Supplier<IOException> exceptionSupplier) {
        this((ExceptionThrower) () -> {
            throw exceptionSupplier.get();
        });
    }

    private BrokenWriter(final ExceptionThrower exceptionThrower) {
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
     * @throws IOException as configured.
     */
    @Override
    public void flush() throws IOException {
        exceptionThrower.doThrow();
    }

    /**
     * Throws the configured exception.
     *
     * @param cbuf ignored.
     * @param off  ignored.
     * @param len  ignored.
     * @throws IOException as configured.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        exceptionThrower.doThrow();
    }

}
