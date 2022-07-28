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
import java.util.function.Predicate;

/**
 * Like {@link Predicate} but throws {@link IOException}.
 *
 * @param <T> the type of the input to the predicate
 * @since 2.12.0
 */
@FunctionalInterface
public interface IOPredicate<T> {

    /**
     * Always false.
     *
     * @param <T> the type of the input to the predicate
     * @return a constant predicate that tests always false.
     */
    @SuppressWarnings("unchecked")
    static <T> IOPredicate<T> alwaysFalse() {
        return (IOPredicate<T>) Constants.IO_PREDICATE_FALSE;
    }

    /**
     * Always true.
     *
     * @param <T> the type of the input to the predicate
     * @return a constant predicate that tests always true.
     */
    @SuppressWarnings("unchecked")
    static <T> IOPredicate<T> alwaysTrue() {
        return (IOPredicate<T>) Constants.IO_PREDICATE_TRUE;
    }

    /**
     * Creates a predicate that tests if two arguments are equal using {@link Objects#equals(Object, Object)}.
     *
     * @param <T> the type of arguments to the predicate
     * @param target the object to compare for equality, may be {@code null}
     * @return a predicate that tests if two arguments are equal using {@link Objects#equals(Object, Object)}
     */
    static <T> IOPredicate<T> isEqual(final Object target) {
        return null == target ? Objects::isNull : object -> target.equals(object);
    }

    /**
     * Creates a composed predicate that represents a short-circuiting logical AND of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code false}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     * </p>
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this predicate and the {@code other}
     *         predicate
     * @throws NullPointerException if other is null
     */
    default IOPredicate<T> and(final IOPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * Creates a {@link Predicate} for this instance that throws {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @return an UncheckedIOException Predicate.
     */
    default Predicate<T> asPredicate() {
        return t -> Uncheck.test(this, t);
    }

    /**
     * Creates a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default IOPredicate<T> negate() {
        return t -> !test(t);
    }

    /**
     * Creates a composed predicate that represents a short-circuiting logical OR of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     * </p>
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other}
     *         predicate
     * @throws NullPointerException if other is null
     */
    default IOPredicate<T> or(final IOPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     * @throws IOException if an I/O error occurs.
     */
    boolean test(T t) throws IOException;

}
