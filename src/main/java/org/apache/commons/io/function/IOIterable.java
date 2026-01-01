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
import java.util.Objects;

/**
 * Like {@link Iterable} but throws {@link IOException}.
 *
 * @param <T> the type of elements returned by the iterable.
 * @since 2.19.0
 */
public interface IOIterable<T> {

    /**
     * Creates an {@link Iterable} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an {@link UncheckedIOException} {@link Iterable}.
     * @since 2.21.0
     */
    default Iterable<T> asIterable() {
        return new UncheckedIOIterable<>(this);
    }

    /**
     * Like {@link Iterable#iterator()}.
     *
     * @param action The action to be performed for each element.
     * @throws NullPointerException Thrown if the specified action is null.
     * @throws IOException          Thrown if an I/O error occurs for a remaining element, or the given action throws.
     * @see Iterable#iterator()
     */
    default void forEach(final IOConsumer<? super T> action) throws IOException {
        iterator().forEachRemaining(Objects.requireNonNull(action));
    }

    /**
     * Like {@link Iterable#iterator()}.
     *
     * @return See {@link Iterable#iterator() delegate}.
     * @see Iterable#iterator()
     */
    IOIterator<T> iterator();

    /**
     * Like {@link Iterable#spliterator()}.
     *
     * @return See {@link Iterable#spliterator() delegate}.
     * @see Iterable#spliterator()
     */
    default IOSpliterator<T> spliterator() {
        return IOSpliteratorAdapter.adapt(new UncheckedIOIterable<>(this).spliterator());
    }

    /**
     * Unwraps this instance and returns the underlying {@link Iterable}.
     * <p>
     * Implementations may not have anything to unwrap and that behavior is undefined for now.
     * </p>
     *
     * @return the underlying Iterable.
     */
    Iterable<T> unwrap();

}
