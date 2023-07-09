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
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Like {@link Spliterator} but throws {@link IOException}.
 *
 * @param <T> the type of elements returned by this IOSpliterator.
 * @since 2.12.0
 */
public interface IOSpliterator<T> {

    /**
     * Adapts the given Spliterator as an IOSpliterator.
     *
     * @param <E> the type of the stream elements.
     * @param iterator The iterator to adapt
     * @return A new IOSpliterator
     */
    static <E> IOSpliterator<E> adapt(final Spliterator<E> iterator) {
        return IOSpliteratorAdapter.adapt(iterator);
    }

    /**
     * Constructs a {@link Spliterator} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an {@link UncheckedIOException} {@link Spliterator}.
     */
    default Spliterator<T> asSpliterator() {
        return new UncheckedIOSpliterator<>(this);
    }

    /**
     * Like {@link Spliterator#characteristics()}.
     *
     * @return a representation of characteristics
     */
    default int characteristics() {
        return unwrap().characteristics();
    }

    /**
     * Like {@link Spliterator#estimateSize()}.
     *
     *
     * @return the estimated size, or {@code Long.MAX_VALUE} if infinite, unknown, or too expensive to compute.
     */
    default long estimateSize() {
        return unwrap().estimateSize();
    }

    /**
     * Like {@link Spliterator#forEachRemaining(Consumer)}.
     *
     * @param action The action
     * @throws NullPointerException if the specified action is null
     */
    default void forEachRemaining(final IOConsumer<? super T> action) {
        while (tryAdvance(action)) { // NOPMD
        }
    }

    /**
     * Like {@link Spliterator#getComparator()}.
     *
     * @return a Comparator, or {@code null} if the elements are sorted in the natural order.
     * @throws IllegalStateException if the spliterator does not report a characteristic of {@code SORTED}.
     */
    @SuppressWarnings("unchecked")
    default IOComparator<? super T> getComparator() {
        return (IOComparator<T>) unwrap().getComparator();
    }

    /**
     * Like {@link Spliterator#getExactSizeIfKnown()}.
     *
     * @return the exact size, if known, else {@code -1}.
     */
    default long getExactSizeIfKnown() {
        return unwrap().getExactSizeIfKnown();
    }

    /**
     * Like {@link Spliterator#hasCharacteristics(int)}.
     *
     * @param characteristics the characteristics to check for
     * @return {@code true} if all the specified characteristics are present, else {@code false}
     */
    default boolean hasCharacteristics(final int characteristics) {
        return unwrap().hasCharacteristics(characteristics);
    }

    /**
     * Like {@link Spliterator#tryAdvance(Consumer)}.
     *
     * @param action The action
     * @return {@code false} if no remaining elements existed upon entry to this method, else {@code true}.
     * @throws NullPointerException if the specified action is null
     */
    default boolean tryAdvance(final IOConsumer<? super T> action) {
        return unwrap().tryAdvance(Objects.requireNonNull(action, "action").asConsumer());
    }

    /**
     * Like {@link Spliterator#trySplit()}.
     *
     * @return a {@code Spliterator} covering some portion of the elements, or {@code null} if this spliterator cannot be
     *         split
     */
    default IOSpliterator<T> trySplit() {
        return adapt(unwrap().trySplit());
    }

    /**
     * Unwraps this instance and returns the underlying {@link Spliterator}.
     * <p>
     * Implementations may not have anything to unwrap and that behavior is undefined for now.
     * </p>
     *
     * @return the underlying Spliterator.
     */
    Spliterator<T> unwrap();

}
