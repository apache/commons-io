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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

final class TestUtils {

    static boolean compareAndSetThrowsIO(final AtomicBoolean ref, final boolean update) throws IOException {
        return compareAndSetThrowsIO(ref, false, update);
    }

    static boolean compareAndSetThrowsIO(final AtomicBoolean ref, final boolean expected, final boolean update) throws IOException {
        if (!ref.compareAndSet(expected, update)) {
            throw new IOException("Unexpected");
        }
        return ref.get(); // same as update
    }

    static int compareAndSetThrowsIO(final AtomicInteger ref, final int update) throws IOException {
        return compareAndSetThrowsIO(ref, 0, update);
    }

    static int compareAndSetThrowsIO(final AtomicInteger ref, final int expected, final int update) throws IOException {
        if (!ref.compareAndSet(expected, update)) {
            throw new IOException("Unexpected");
        }
        return ref.get(); // same as update
    }

    static long compareAndSetThrowsIO(final AtomicLong ref, final long update) throws IOException {
        return compareAndSetThrowsIO(ref, 0, update);
    }

    static long compareAndSetThrowsIO(final AtomicLong ref, final long expected, final long update) throws IOException {
        if (!ref.compareAndSet(expected, update)) {
            throw new IOException("Unexpected");
        }
        return ref.get(); // same as update
    }

    static <T> T compareAndSetThrowsIO(final AtomicReference<T> ref, final T update) throws IOException {
        return compareAndSetThrowsIO(ref, null, update);
    }

    static <T> T compareAndSetThrowsIO(final AtomicReference<T> ref, final T expected, final T update) throws IOException {
        if (!ref.compareAndSet(expected, update)) {
            throw new IOException("Unexpected");
        }
        return ref.get(); // same as update
    }

    static <T> T compareAndSetThrowsRE(final AtomicReference<T> ref, final T expected, final T update) {
        if (!ref.compareAndSet(expected, update)) {
            throw new RuntimeException("Unexpected");
        }
        return ref.get(); // same as update
    }

    @SuppressWarnings("unchecked")
    static <T, U> IOBiConsumer<T, U> throwingIOBiConsumer() {
        return (IOBiConsumer<T, U>) TestConstants.THROWING_IO_BI_CONSUMER;
    }

    @SuppressWarnings("unchecked")
    static <T, U, V> IOBiFunction<T, U, V> throwingIOBiFunction() {
        return (IOBiFunction<T, U, V>) TestConstants.THROWING_IO_BI_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    static <T> IOBinaryOperator<T> throwingIOBinaryOperator() {
        return (IOBinaryOperator<T>) TestConstants.THROWING_IO_BINARY_OPERATOR;
    }

    @SuppressWarnings("unchecked")
    static <T> IOComparator<T> throwingIOComparator() {
        return (IOComparator<T>) TestConstants.THROWING_IO_COMPARATOR;
    }

    @SuppressWarnings("unchecked")
    static <T> IOConsumer<T> throwingIOConsumer() {
        return (IOConsumer<T>) TestConstants.THROWING_IO_CONSUMER;
    }

    @SuppressWarnings("unchecked")
    static <T, U> IOFunction<T, U> throwingIOFunction() {
        return (IOFunction<T, U>) TestConstants.THROWING_IO_FUNCTION;
    }

    static IOIntConsumer throwingIOIntConsumer() {
        return TestConstants.THROWING_IO_INT_CONSUMER;
    }

    @SuppressWarnings("unchecked")
    static <T> IOPredicate<T> throwingIOPredicate() {
        return (IOPredicate<T>) TestConstants.THROWING_IO_PREDICATE;
    }

    static IORunnable throwingIORunnable() {
        return TestConstants.THROWING_IO_RUNNABLE;
    }

    @SuppressWarnings("unchecked")
    static <T> IOSupplier<T> throwingIOSupplier() {
        return (IOSupplier<T>) TestConstants.THROWING_IO_SUPPLIER;
    }

    @SuppressWarnings("unchecked")
    static <T> IOUnaryOperator<T> throwingIOUnaryOperator() {
        return (IOUnaryOperator<T>) TestConstants.THROWING_IO_UNARY_OPERATOR;
    }

}
