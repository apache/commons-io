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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

/**
 * Test fixtures for this package.
 */
final class TestConstants {

    static final Path ABS_PATH_A = Paths.get("LICENSE.txt").toAbsolutePath();

    static final Path ABS_PATH_B = Paths.get("NOTICE.txt").toAbsolutePath();

    static IOBiConsumer<Object, Object> THROWING_IO_BI_CONSUMER = (t, u) -> throwIOException();

    static IOBiFunction<Object, Object, Object> THROWING_IO_BI_FUNCTION = (t, u) -> throwIOException();

    static IOBinaryOperator<?> THROWING_IO_BINARY_OPERATOR = (t, u) -> throwIOException();

    static IOBooleanSupplier THROWING_IO_BOOLEAN_SUPPLIER = TestConstants::throwIOException;

    static IOComparator<Object> THROWING_IO_COMPARATOR = (t, u) -> throwIOException();

    static IOConsumer<Object> THROWING_IO_CONSUMER = t -> throwIOException();

    static IOFunction<Object, Object> THROWING_IO_FUNCTION = t -> throwIOException();

    static IOIntConsumer THROWING_IO_INT_CONSUMER = t -> throwIOException();

    static IOIntSupplier THROWING_IO_INT_SUPPLIER = TestConstants::throwIOException;

    static IOLongSupplier THROWING_IO_LONG_SUPPLIER = TestConstants::throwIOException;

    static IOPredicate<Object> THROWING_IO_PREDICATE = t -> throwIOException();

    static IOQuadFunction<Object, Object, Object, Object, Object> THROWING_IO_QUAD_FUNCTION = (t, u, v, w) -> throwIOException();

    static IORunnable THROWING_IO_RUNNABLE = TestConstants::throwIOException;

    static IOSupplier<Object> THROWING_IO_SUPPLIER = TestConstants::throwIOException;

    static IOTriConsumer<Object, Object, Object> THROWING_IO_TRI_CONSUMER = (t, u, v) -> throwIOException();

    static IOTriFunction<Object, Object, Object, Object> THROWING_IO_TRI_FUNCTION = (t, u, v) -> throwIOException();

    static IOUnaryOperator<?> THROWING_IO_UNARY_OPERATOR = t -> throwIOException();

    static Predicate<Object> THROWING_PREDICATE = t -> {
        throw new UncheckedIOException(new IOException("Failure"));
    };

    static <T> T throwIOException() throws IOException {
        return throwIOException("Failure");
    }

    static <T> T throwIOException(final String message) throws IOException {
        throw new IOException(message);
    }

    static <T> T throwRuntimeException(final String message) {
        throw new RuntimeException(message);
    }

}
