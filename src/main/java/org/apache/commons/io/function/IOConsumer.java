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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.apache.commons.io.UncheckedIOExceptions;

/**
 * Like {@link Consumer} but throws {@link IOException}.
 *
 * @param <T> the type of the input to the operations.
 * @since 2.7
 */
@FunctionalInterface
public interface IOConsumer<T> {

    /**
     * Package private constant; consider private.
     */
    IOConsumer<?> NOOP_IO_CONSUMER = t -> {/* noop */};

    /**
     * Wraps an {@code IOConsumer} inside of a {@link Consumer}
     * that throws {@link UncheckedIOException} for any {@link IOException}s
     * that are thrown by the underlying {@code IOConsumer}.
     *
     * @param <T> The element type.
     * @param consumer The {@code IOConsumer} to wrap.
     * @return a {@code Consumer} that wraps the given {@code IOConsumer}.
     * @since 2.12.0
     */
    static <T> Consumer<T> asConsumer(IOConsumer<T> consumer) {
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                try {
                    consumer.accept(t);
                } catch (IOException e) {
                    throw UncheckedIOExceptions.create(String.format("%s thrown from %s", e.getClass().getName(), String.valueOf(consumer)), e);
                }
            }
        };
    }

    /**
     * Wraps a {@link Consumer} inside of a {@code IOConsumer}
     * that catches {@link UncheckedIOException}s that are thrown by the underlying
     * {@code IOConsumer} and rethrows them as {@link IOException}
     *
     * @param <T> The element type.
     * @param consumer The {@code Consumer} to wrap.
     * @return a {@code IOConsumer} that wraps the given {@code Consumer}.
     * @since 2.12.0
     */
    static <T> IOConsumer<T> wrap(Consumer<T> consumer) {
        return new IOConsumer<T>() {
            @Override
            public void accept(T t) throws IOException {
                try {
                    consumer.accept(t);
                } catch (UncheckedIOException e) {
                    throw e.getCause() == null ? new IOException(e) : e.getCause();
                }
            }
        };
    }

    /**
     * Performs an action for each element of this stream.
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
     * Performs an action for each element of this array, returning
     * a {@link Optional} that either contains an {@link IOException}
     * if one occurred, or {@link Optional#empty()}.
     *
     * @param <T> The element type.
     * @param array The input to stream.
     * @param action The action to apply to each input element.
     * @return a {@code Optional} that may wrap a {@code IOException}.
     * @since 2.12.0
     */
    static <T> Optional<IOException> forEachQuietly(final T[] array, final IOConsumer<T> action) {
        try {
            IOStreams.forEach(IOStreams.of(array), action);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    /**
     * Performs an action for each element of this stream, returning
     * a {@link Optional} that either contains an {@link IOExceptionList}
     * if one occurred, or {@link Optional#empty()}.
     *
     * @param <T> The element type.
     * @param stream The input to stream.
     * @param action The action to apply to each input element.
     * @return a {@code Optional} that may wrap a {@code IOExceptionList}.
     * @since 2.12.0
     */
    static <T> Optional<IOExceptionList> forEachIndexedQuietly(final Stream<T> stream, final IOConsumer<T> action) {
        try {
            IOStreams.forEachIndexed(stream, action, IOIndexedException::new);
            return Optional.empty();
        } catch (IOExceptionList e) {
            return Optional.of(e);
        }
    }

    /**
     * Performs an action for each element of this stream.
     *
     * @param <T> The element type.
     * @param stream The input to stream.
     * @param action The action to apply to each input element.
     * @throws IOExceptionList if an I/O error occurs.
     * @since 2.12.0
     */
    static <T> void forEachIndexed(final Stream<T> stream, final IOConsumer<T> action) throws IOExceptionList {
        IOStreams.forEachIndexed(stream, action, IOIndexedException::new);
    }

    /**
     * Returns a constant NOOP consumer.
     *
     * @param <T> Type consumer type.
     * @return a constant NOOP consumer.
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
     * Returns a composed {@code IOConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed operation.
     * If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code IOConsumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default IOConsumer<T> andThen(final IOConsumer<? super T> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
