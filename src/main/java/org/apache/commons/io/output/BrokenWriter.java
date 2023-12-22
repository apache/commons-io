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

import org.apache.commons.io.function.Erase;

/**
 * Always throws an exception from all {@link Writer} methods where {@link IOException} is declared.
 * <p>
 * This class is mostly useful for testing error handling.
 * </p>
 *
 * @since 2.0
 */
public class BrokenWriter extends Writer {

    /**
     * The singleton instance using a default IOException.
     *
     * @since 2.12.0
     */
    public static final BrokenWriter INSTANCE = new BrokenWriter();

    /**
     * Supplies the exception that is thrown by all methods of this class.
     */
    private final Supplier<Throwable> exceptionSupplier;

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
     * @deprecated Use {@link #BrokenWriter(Throwable)}.
     */
    @Deprecated
    public BrokenWriter(final IOException exception) {
        this(() -> exception);
    }

    /**
     * Constructs a new writer that always throws the supplied exception.
     *
     * @param exceptionSupplier a supplier for the IOException or RuntimeException to be thrown.
     * @since 2.12.0
     */
    public BrokenWriter(final Supplier<Throwable> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    /**
     * Constructs a new writer that always throws the given exception.
     *
     * @param exception the exception to be thrown.
     * @since 2.16.0
     */
    public BrokenWriter(final Throwable exception) {
        this(() -> exception);
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always throws the exception configured in a constructor.
     */
    @Override
    public void close() throws IOException {
        throw rethrow();
    }

    /**
     * Throws the configured exception.
     *
     * @throws IOException always throws the exception configured in a constructor.
     */
    @Override
    public void flush() throws IOException {
        throw rethrow();
    }

    /**
     * Throws the configured exception from its supplier.
     *
     * @return Throws the configured exception from its supplier.
     */
    private RuntimeException rethrow() {
        return Erase.rethrow(exceptionSupplier.get());
    }

    /**
     * Throws the configured exception.
     *
     * @param cbuf ignored.
     * @param off  ignored.
     * @param len  ignored.
     * @throws IOException always throws the exception configured in a constructor.
     */
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        throw rethrow();
    }

}
