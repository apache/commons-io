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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOExceptionList;

/**
 * Keep this code package-private for now.
 */
class IOStreams {

    /**
     * Accepts and throws an IOException.
     *
     * @param <T> The consumer type.
     * @param consumer The consumer to accept.
     * @param t the input argument.
     * @throws IOException if an I/O error occurs; erased for the compiler.
     */
    static <T> void accept(final IOConsumer<T> consumer, T t) {
        try {
            consumer.accept(t);
        } catch (IOException ex) {
            rethrow(ex);
        }
    }

    static <T> void forAll(final Stream<T> stream, final IOConsumer<T> action) throws IOExceptionList {
        forAll(stream, action, (i, e) -> e);
    }

    static <T> void forAll(final Stream<T> stream, final IOConsumer<T> action, final BiFunction<Integer, IOException, IOException> exSupplier)
        throws IOExceptionList {
        final AtomicReference<List<IOException>> causeList = new AtomicReference<>();
        final AtomicInteger index = new AtomicInteger();
        final IOConsumer<T> actualAction = toIOConsumer(action);
        of(stream).forEach(e -> {
            try {
                actualAction.accept(e);
            } catch (IOException ex) {
                if (causeList.get() == null) {
                    // Only allocate if required
                    causeList.set(new ArrayList<>());
                }
                if (exSupplier != null) {
                    causeList.get().add(exSupplier.apply(index.get(), ex));
                }
            }
            index.incrementAndGet();
        });
        IOExceptionList.checkEmpty(causeList.get(), null);
    }

    @SuppressWarnings("unused") // IOStreams.rethrow() throws
    static <T> void forEach(final Stream<T> stream, final IOConsumer<T> action) throws IOException {
        final IOConsumer<T> actualAction = toIOConsumer(action);
        of(stream).forEach(e -> accept(actualAction, e));
    }

    /**
     * Null-safe version of {@link StreamSupport#stream(java.util.Spliterator, boolean)}.
     *
     * Copied from Apache Commons Lang.
     *
     * @param <T> the type of stream elements.
     * @param values the elements of the new stream, may be {@code null}.
     * @return the new stream on {@code values} or {@link Stream#empty()}.
     */
    static <T> Stream<T> of(final Iterable<T> values) {
        return values == null ? Stream.empty() : StreamSupport.stream(values.spliterator(), false);
    }

    static <T> Stream<T> of(final Stream<T> stream) {
        return stream == null ? Stream.empty() : stream;
    }

    /**
     * Null-safe version of {@link Stream#of(Object[])}.
     *
     * Copied from Apache Commons Lang.
     *
     * @param <T> the type of stream elements.
     * @param values the elements of the new stream, may be {@code null}.
     * @return the new stream on {@code values} or {@link Stream#empty()}.
     */
    @SafeVarargs // Creating a stream from an array is safe
    static <T> Stream<T> of(final T... values) {
        return values == null ? Stream.empty() : Stream.of(values);
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
    static <T extends Throwable> RuntimeException rethrow(final Throwable throwable) throws T {
        throw (T) throwable; // hack
    }

    static <T> IOConsumer<T> toIOConsumer(final IOConsumer<T> action) {
        return action != null ? action : IOConsumer.noop();
    }
}
