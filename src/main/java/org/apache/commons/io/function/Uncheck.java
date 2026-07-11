/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.function.Supplier;

/**
 * Unchecks calls by throwing {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @since 2.12.0
 */
public final class Uncheck {

    /**
     * Accepts an IO consumer with the given arguments.
     *
     * @param <T> The first input type.
     * @param <U> The second input type.
     * @param t The first input argument.
     * @param u The second input argument.
     * @param consumer Consumes the value.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, U> void accept(final IOBiConsumer<T, U> consumer, final T t, final U u) {
        try {
            consumer.accept(t, u);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Accepts an IO consumer with the given argument.
     *
     * @param <T> The input type.
     * @param t The input argument.
     * @param consumer Consumes the value.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T> void accept(final IOConsumer<T> consumer, final T t) {
        try {
            consumer.accept(t);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Accepts an IO consumer with the given argument.
     *
     * @param i The input argument.
     * @param consumer Consumes the value.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.18.0
     */
    public static void accept(final IOIntConsumer consumer, final int i) {
        try {
            consumer.accept(i);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Accepts an IO consumer with the given arguments.
     *
     * @param <T> The first input type.
     * @param <U> The second input type.
     * @param <V> The third input type.
     * @param t The first input argument.
     * @param u The second input argument.
     * @param v The third input argument.
     * @param consumer Consumes the value.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, U, V> void accept(final IOTriConsumer<T, U, V> consumer, final T t, final U u, final V v) {
        try {
            consumer.accept(t, u, v);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Applies an IO function with the given arguments.
     *
     * @param <T> The first function argument type.
     * @param <U> The second function argument type.
     * @param <R> The return type.
     * @param function The function.
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The function result.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, U, R> R apply(final IOBiFunction<T, U, R> function, final T t, final U u) {
        try {
            return function.apply(t, u);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Applies an IO function with the given arguments.
     *
     * @param function The function.
     * @param <T> The first function argument type.
     * @param <R> The return type.
     * @param t The first function argument.
     * @return The function result.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, R> R apply(final IOFunction<T, R> function, final T t) {
        try {
            return function.apply(t);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Applies an IO quad-function with the given arguments.
     *
     * @param function The function.
     * @param <T> The first function argument type.
     * @param <U> The second function argument type.
     * @param <V> The third function argument type.
     * @param <W> The fourth function argument type.
     * @param <R> The return type.
     * @param t The first function argument.
     * @param u The second function argument.
     * @param v The third function argument.
     * @param w The fourth function argument.
     * @return The function result.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, U, V, W, R> R apply(final IOQuadFunction<T, U, V, W, R> function, final T t, final U u, final V v, final W w) {
        try {
            return function.apply(t, u, v, w);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Applies an IO tri-function with the given arguments.
     *
     * @param <T> The first function argument type.
     * @param <U> The second function argument type.
     * @param <V> The third function argument type.
     * @param <R> The return type.
     * @param function The function.
     * @param t The first function argument.
     * @param u The second function argument.
     * @param v The third function argument.
     * @return The function result.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T, U, V, R> R apply(final IOTriFunction<T, U, V, R> function, final T t, final U u, final V v) {
        try {
            return function.apply(t, u, v);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Compares the arguments with the comparator.
     *
     * @param <T> The first function argument type.
     * @param comparator The function.
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The comparator result.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T> int compare(final IOComparator<T> comparator, final T t, final T u) {
        try {
            return comparator.compare(t, u);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets the result from an IO supplier.
     *
     * @param <T> The return type of the operations.
     * @param supplier Supplies the return value.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T> T get(final IOSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets the result from an IO supplier.
     *
     * @param <T> The return type of the operations.
     * @param supplier Supplies the return value.
     * @param message The UncheckedIOException message if an I/O error occurs.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static <T> T get(final IOSupplier<T> supplier, final Supplier<String> message) {
        try {
            return supplier.get();
        } catch (final IOException e) {
            throw wrap(e, message);
        }
    }

    /**
     * Gets the result from an IO boolean supplier.
     *
     * @param supplier Supplies the return value.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.19.0
     */
    public static boolean getAsBoolean(final IOBooleanSupplier supplier) {
        try {
            return supplier.getAsBoolean();
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets the result from an IO int supplier.
     *
     * @param supplier Supplies the return value.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.14.0
     */
    public static int getAsInt(final IOIntSupplier supplier) {
        try {
            return supplier.getAsInt();
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets the result from an IO int supplier.
     *
     * @param supplier Supplies the return value.
     * @param message The UncheckedIOException message if an I/O error occurs.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.14.0
     */
    public static int getAsInt(final IOIntSupplier supplier, final Supplier<String> message) {
        try {
            return supplier.getAsInt();
        } catch (final IOException e) {
            throw wrap(e, message);
        }
    }

    /**
     * Gets the result from an IO long supplier.
     *
     * @param supplier Supplies the return value.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.14.0
     */
    public static long getAsLong(final IOLongSupplier supplier) {
        try {
            return supplier.getAsLong();
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets the result from an IO long supplier.
     *
     * @param supplier Supplies the return value.
     * @param message The UncheckedIOException message if an I/O error occurs.
     * @return result from the supplier.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.14.0
     */
    public static long getAsLong(final IOLongSupplier supplier, final Supplier<String> message) {
        try {
            return supplier.getAsLong();
        } catch (final IOException e) {
            throw wrap(e, message);
        }
    }

    /**
     * Runs an IO runnable.
     *
     * @param runnable The runnable to run.
     * @throws UncheckedIOException if an I/O error occurs.
     */
    public static void run(final IORunnable runnable) {
        try {
            runnable.run();
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Runs an IO runnable.
     *
     * @param runnable The runnable to run.
     * @param message The UncheckedIOException message if an I/O error occurs.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.14.0
     */
    public static void run(final IORunnable runnable, final Supplier<String> message) {
        try {
            runnable.run();
        } catch (final IOException e) {
            throw wrap(e, message);
        }
    }

    /**
     * Tests an IO predicate.
     *
     * @param <T> The type of the input to the predicate.
     * @param predicate The predicate.
     * @param t The input to the predicate.
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}.
     */
    public static <T> boolean test(final IOPredicate<T> predicate, final T t) {
        try {
            return predicate.test(t);
        } catch (final IOException e) {
            throw wrap(e);
        }
    }

    /**
     * Constructs a new {@link UncheckedIOException} for the given exception.
     *
     * @param e The exception to wrap.
     * @return A new {@link UncheckedIOException}.
     */
    private static UncheckedIOException wrap(final IOException e) {
        return new UncheckedIOException(e);
    }

    /**
     * Constructs a new {@link UncheckedIOException} for the given exception and detail message.
     *
     * @param e The exception to wrap.
     * @param message The UncheckedIOException message if an I/O error occurs.
     * @return A new {@link UncheckedIOException}.
     */
    private static UncheckedIOException wrap(final IOException e, final Supplier<String> message) {
        return new UncheckedIOException(message.get(), e);
    }

    /**
     * No instances needed.
     */
    private Uncheck() {
        // no instances needed.
    }
}
