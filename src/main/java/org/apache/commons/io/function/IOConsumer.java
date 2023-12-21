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

package org.apache.commons.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;

/**
 * Like {@link Consumer} but throws {@link IOException}.
 *
 * @param <T> the type of the input to the operations.
 * @since 2.7
 */
@FunctionalInterface
public interface IOConsumer<T> {

    /**
     * Consider private.
     */
    IOConsumer<?> NOOP_IO_CONSUMER = t -> {/* noop */};

    /**
     * Performs an action for each element of the collection gathering any exceptions.
     *
     * @param action The action to apply to each input element.
     * @param iterable The input to stream.
     * @param <T> The element type.
     * @throws IOExceptionList if any I/O errors occur.
     * @since 2.12.0
     */
    static <T> void forAll(final IOConsumer<T> action, final Iterable<T> iterable) throws IOExceptionList {
        IOStreams.forAll(IOStreams.of(iterable), action);
    }

    /**
     * Performs an action for each element of the collection gathering any exceptions.
     *
     * @param action The action to apply to each input element.
     * @param stream The input to stream.
     * @param <T> The element type.
     * @throws IOExceptionList if any I/O errors occur.
     * @since 2.12.0
     */
    static <T> void forAll(final IOConsumer<T> action, final Stream<T> stream) throws IOExceptionList {
        IOStreams.forAll(stream, action, IOIndexedException::new);
    }

    /**
     * Performs an action for each element of the array, gathering any exceptions.
     *
     * @param action The action to apply to each input element.
     * @param array The input to stream.
     * @param <T> The element type.
     * @throws IOExceptionList if any I/O errors occur.
     * @since 2.12.0
     */
    @SafeVarargs
    static <T> void forAll(final IOConsumer<T> action, final T... array) throws IOExceptionList {
        IOStreams.forAll(IOStreams.of(array), action);
    }

    /**
     * Performs an action for each element of the collection, stopping at the first exception.
     *
     * @param <T> The element type.
     * @param iterable The input to stream.
     * @param action The action to apply to each input element.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    static <T> void forEach(final Iterable<T> iterable, final IOConsumer<T> action) throws IOException {
        IOStreams.forEach(IOStreams.of(iterable), action);
    }

    /**
     * Performs an action for each element of the stream, stopping at the first exception.
     *
     * @param <T> The element type.
     * @param stream The input to stream.
     * @param action The action to apply to each input element.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    static <T> void forEach(final Stream<T> stream, final IOConsumer<T> action) throws IOException {
        IOStreams.forEach(stream, action);
    }

    /**
     * Performs an action for each element of this array, stopping at the first exception.
     *
     * @param <T> The element type.
     * @param array The input to stream.
     * @param action The action to apply to each input element.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    static <T> void forEach(final T[] array, final IOConsumer<T> action) throws IOException {
        IOStreams.forEach(IOStreams.of(array), action);
    }

    /**
     * Returns the constant no-op consumer.
     *
     * @param <T> Type consumer type.
     * @return a constant no-op consumer.
     * @since 2.9.0
     */
    @SuppressWarnings("unchecked")
    static <T> IOConsumer<T> noop() {
        return (IOConsumer<T>) NOOP_IO_CONSUMER;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws IOException if an I/O error occurs.
     */
    void accept(T t) throws IOException;

    /**
     * Returns a composed {@link IOConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@link Consumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default IOConsumer<T> andThen(final IOConsumer<? super T> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }

    /**
     * Creates a {@link Consumer} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException Consumer.
     * @since 2.12.0
     */
    default Consumer<T> asConsumer() {
        return t -> Uncheck.accept(this, t);
    }

}
