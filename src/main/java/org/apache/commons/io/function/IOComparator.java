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
import java.util.Comparator;

/**
 * Like {@link Comparator} but throws {@link IOException}.
 *
 * @param <T> the type of objects that may be compared by this comparator
 *
 * @see Comparator
 * @since 2.12.0
 */
@FunctionalInterface
public interface IOComparator<T> {

    /**
     * Creates a {@link Comparator} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an UncheckedIOException BiFunction.
     */
    default Comparator<T> asComparator() {
        return (t, u) -> Uncheck.compare(this, t, u);
    }

    /**
     * Like {@link Comparator#compare(Object, Object)} but throws {@link IOException}.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than
     *         the second.
     * @throws NullPointerException if an argument is null and this comparator does not permit null arguments
     * @throws ClassCastException if the arguments' types prevent them from being compared by this comparator.
     * @throws IOException if an I/O error occurs.
     */
    int compare(T o1, T o2) throws IOException;
}
