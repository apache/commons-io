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
import java.util.function.BinaryOperator;

/**
 * Like {@link BinaryOperator} but throws {@link IOException}.
 *
 * @param <T> the type of the operands and result of the operator.
 *
 * @see IOBiFunction
 * @see BinaryOperator
 * @since 2.12.0
 */
@FunctionalInterface
public interface IOBinaryOperator<T> extends IOBiFunction<T, T, T> {

    /**
     * Creates a {@link IOBinaryOperator} which returns the greater of two elements according to the specified
     * {@code Comparator}.
     *
     * @param <T> the type of the input arguments of the comparator
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code BinaryOperator} which returns the greater of its operands, according to the supplied
     *         {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> IOBinaryOperator<T> maxBy(final IOComparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    /**
     * Creates a {@link IOBinaryOperator} which returns the lesser of two elements according to the specified
     * {@code Comparator}.
     *
     * @param <T> the type of the input arguments of the comparator
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code BinaryOperator} which returns the lesser of its operands, according to the supplied
     *         {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> IOBinaryOperator<T> minBy(final IOComparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Creates a {@link BinaryOperator} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an unchecked BiFunction.
     */
    default BinaryOperator<T> asBinaryOperator() {
        return (t, u) -> Uncheck.apply(this, t, u);
    }
}
