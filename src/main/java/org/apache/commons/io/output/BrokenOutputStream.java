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
import java.util.function.Supplier;

/**
 * Broken output stream. This stream always throws an {@link IOException} or a {@link RuntimeException} from
 * all {@link OutputStream} methods.
 * <p>
 * This class is mostly useful for testing error handling in code that uses an
 * output stream.
 * </p>
 *
 * @since 2.0
 */
public class BrokenOutputStream extends OutputStream {

    /**
     * A singleton instance.
     *
     * @since 2.12.0
     */
    public static final BrokenOutputStream INSTANCE = new BrokenOutputStream();

    /**
     * Constructs a new stream that always throws a {@link RuntimeException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @return a new stream that always throws a {@link RuntimeException}.
     */
    public static BrokenOutputStream brokenOutputStream(final Supplier<RuntimeException> exceptionSupplier) {
        return new BrokenOutputStream(() -> {
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
     * Constructs a new stream that always throws an {@link IOException}.
     */
    public BrokenOutputStream() {
        this(() -> new IOException("Broken output stream"));
    }

    /**
     * Constructs a new stream that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenOutputStream(final IOException exception) {
        this(() -> exception);
    }

    /**
     * Constructs a new stream that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     */
    public BrokenOutputStream(final RuntimeException exception) {
        this(() -> {
            throw exception;
        });
    }

    /**
     * Constructs a new stream that always throws an {@link IOException}.
     *
     * @param exceptionSupplier a supplier for the exception to be thrown.
     * @since 2.12.0
     */
    public BrokenOutputStream(final Supplier<IOException> exceptionSupplier) {
        this((ExceptionThrower) () -> {
            throw exceptionSupplier.get();
        });
    }

    private BrokenOutputStream(final ExceptionThrower exceptionThrower) {
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
     * @param b ignored
     * @throws IOException as configured.
     */
    @Override
    public void write(final int b) throws IOException {
        exceptionThrower.doThrow();
    }

}
