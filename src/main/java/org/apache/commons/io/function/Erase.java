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

/**
 * Erases {@link IOException} for the compiler but still throws that exception at runtime.
 *
 * @since 2.16.0
 */
public final class Erase {

    /**
     * Delegates to the given {@link IOBiConsumer} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param <U> See delegate.
     * @param consumer See delegate.
     * @param t See delegate.
     * @param u See delegate.
     * @see IOBiConsumer
     */
    static <T, U> void accept(final IOBiConsumer<T, U> consumer, final T t, final U u) {
        try {
            consumer.accept(t, u);
        } catch (final IOException ex) {
            rethrow(ex); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOConsumer} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param consumer See delegate.
     * @param t See delegate.
     * @see IOConsumer
     */
    static <T> void accept(final IOConsumer<T> consumer, final T t) {
        try {
            consumer.accept(t);
        } catch (final IOException ex) {
            rethrow(ex); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOBiFunction} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param <U> See delegate.
     * @param <R> See delegate.
     * @param mapper See delegate.
     * @param t See delegate.
     * @param u See delegate.
     * @return See delegate.
     * @see IOBiFunction
     */
    static <T, U, R> R apply(final IOBiFunction<? super T, ? super U, ? extends R> mapper, final T t, final U u) {
        try {
            return mapper.apply(t, u);
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOFunction} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param <R> See delegate.
     * @param mapper See delegate.
     * @param t See delegate.
     * @return See delegate.
     * @see IOFunction
     */
    static <T, R> R apply(final IOFunction<? super T, ? extends R> mapper, final T t) {
        try {
            return mapper.apply(t);
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOComparator} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param comparator See delegate.
     * @param t See delegate.
     * @param u See delegate.
     * @return See delegate.
     * @see IOComparator
     */
    static <T> int compare(final IOComparator<? super T> comparator, final T t, final T u) {
        try {
            return comparator.compare(t, u);
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOSupplier} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param supplier See delegate.
     * @return See delegate.
     * @see IOSupplier
     */
    static <T> T get(final IOSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /**
     * Throws the given throwable.
     *
     * @param <T> The throwable cast type.
     * @param throwable The throwable to rethrow.
     * @return nothing because we throw.
     * @throws T Always thrown.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(final Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Delegates to the given {@link IORunnable} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param runnable See delegate.
     * @see IORunnable
     */
    static void run(final IORunnable runnable) {
        try {
            runnable.run();
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /**
     * Delegates to the given {@link IOPredicate} but erases its {@link IOException} for the compiler, while still throwing
     * the exception at runtime.
     *
     * @param <T> See delegate.
     * @param predicate See delegate.
     * @param t See delegate.
     * @return See delegate.
     * @see IOPredicate
     */
    static <T> boolean test(final IOPredicate<? super T> predicate, final T t) {
        try {
            return predicate.test(t);
        } catch (final IOException e) {
            throw rethrow(e); // throws IOException
        }
    }

    /** No instances. */
    private Erase() {
        // No instances.
    }

}
