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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Like {@link Function} but throws {@link IOException}.
 *
 * @param <T> the type of the input to the operations.
 * @param <R> the return type of the operations.
 * @since 2.7
 */
@FunctionalInterface
public interface IOFunction<T, R> {

    /**
     * Returns a {@link IOFunction} that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    @SuppressWarnings("unchecked")
    static <T> IOFunction<T, T> identity() {
        return Constants.IO_FUNCTION_ID;
    }

    /**
     * Returns a composed {@link IOFunction} that first applies this function to its input, and then applies the
     * {@code after} consumer to the result. If evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param after the consumer to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} consumer
     * @throws NullPointerException if after is null
     *
     * @see #compose(IOFunction)
     */
    default IOConsumer<T> andThen(final Consumer<? super R> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.accept(apply(t));
    }

    /**
     * Returns a composed {@link IOFunction} that first applies this function to its input, and then applies the
     * {@code after} function to the result. If evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     * @see #compose(IOFunction)
     */
    default <V> IOFunction<T, V> andThen(final Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.apply(apply(t));
    }

    /**
     * Returns a composed {@link IOFunction} that first applies this function to its input, and then applies the
     * {@code after} consumer to the result. If evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param after the consumer to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} consumer
     * @throws NullPointerException if after is null
     *
     * @see #compose(IOFunction)
     */
    default IOConsumer<T> andThen(final IOConsumer<? super R> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.accept(apply(t));
    }

    /**
     * Returns a composed {@link IOFunction} that first applies this function to its input, and then applies the
     * {@code after} function to the result. If evaluation of either function throws an exception, it is relayed to the
     * caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     * @see #compose(IOFunction)
     */
    default <V> IOFunction<T, V> andThen(final IOFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after");
        return (final T t) -> after.apply(apply(t));
    }

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws IOException if an I/O error occurs.
     */
    R apply(final T t) throws IOException;

    /**
     * Creates a {@link Function} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException Function.
     * @since 2.12.0
     */
    default Function<T, R> asFunction() {
        return t -> Uncheck.apply(this, t);
    }

    /**
     * Returns a composed {@link IOFunction} that first applies the {@code before} function to its input, and then applies
     * this function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
     * composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before} function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(IOFunction)
     */
    default <V> IOFunction<V, R> compose(final Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before, "before");
        return (final V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed {@link IOFunction} that first applies the {@code before} function to its input, and then applies
     * this function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
     * composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before} function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(IOFunction)
     */
    default <V> IOFunction<V, R> compose(final IOFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before, "before");
        return (final V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed {@link IOFunction} that first applies the {@code before} function to its input, and then applies
     * this function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
     * composed function.
     *
     * @param before the supplier which feeds the application of this function
     * @return a composed function that first applies the {@code before} function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(IOFunction)
     */
    default IOSupplier<R> compose(final IOSupplier<? extends T> before) {
        Objects.requireNonNull(before, "before");
        return () -> apply(before.get());
    }

    /**
     * Returns a composed {@link IOFunction} that first applies the {@code before} function to its input, and then applies
     * this function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
     * composed function.
     *
     * @param before the supplier which feeds the application of this function
     * @return a composed function that first applies the {@code before} function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(IOFunction)
     */
    default IOSupplier<R> compose(final Supplier<? extends T> before) {
        Objects.requireNonNull(before, "before");
        return () -> apply(before.get());
    }
}
