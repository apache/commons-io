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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Like {@link Iterator} but throws {@link IOException}.
 *
 * @param <E> the type of elements returned by this iterator.
 * @since 2.12.0
 */
public interface IOIterator<E> {

    /**
     * Adapts the given Iterable as an IOIterator.
     *
     * @param <E> the type of the stream elements.
     * @param iterable The iterable to adapt
     * @return A new IOIterator
     * @since 2.17.0
     */
    static <E> IOIterator<E> adapt(final Iterable<E> iterable) {
        return IOIteratorAdapter.adapt(iterable.iterator());
    }

    /**
     * Adapts the given Iterator as an IOIterator.
     *
     * @param <E> the type of the stream elements.
     * @param iterator The iterator to adapt
     * @return A new IOIterator
     */
    static <E> IOIterator<E> adapt(final Iterator<E> iterator) {
        return IOIteratorAdapter.adapt(iterator);
    }

    /**
     * Creates an {@link Iterator} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an {@link UncheckedIOException} {@link Iterator}.
     */
    default Iterator<E> asIterator() {
        return new UncheckedIOIterator<>(this);
    }

    /**
     * Like {@link Iterator#forEachRemaining(Consumer)}.
     *
     * @param action See delegate.
     * @throws IOException if an I/O error occurs.
     */
    default void forEachRemaining(final IOConsumer<? super E> action) throws IOException {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.accept(next());
        }
    }

    /**
     * Like {@link Iterator#hasNext()}.
     *
     * @return See delegate.
     * @throws IOException if an I/O error occurs.
     */
    boolean hasNext() throws IOException;

    /**
     * Like {@link Iterator#next()}.
     *
     * @return See delegate.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchElementException if the iteration has no more elements
     */
    E next() throws IOException;

    /**
     * Like {@link Iterator#remove()}.
     *
     * @throws IOException if an I/O error occurs.
     */
    @SuppressWarnings("unused")
    default void remove() throws IOException {
        unwrap().remove();
    }

    /**
     * Unwraps this instance and returns the underlying {@link Iterator}.
     * <p>
     * Implementations may not have anything to unwrap and that behavior is undefined for now.
     * </p>
     * @return the underlying Iterator.
     */
    Iterator<E> unwrap();

}
