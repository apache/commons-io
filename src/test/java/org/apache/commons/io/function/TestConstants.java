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
import java.util.function.Predicate;

/**
 * Test fixtures for this package.
 */
class TestConstants {

    static IOConsumer<Object> THROWING_IO_CONSUMER = t -> {
        throw new IOException("Failure");
    };

    static IOFunction<Object, Object> THROWING_IO_FUNCTION = t -> {
        throw new IOException("Failure");
    };

    static IOBiFunction<Object, Object, Object> THROWING_IO_BI_FUNCTION = (t, u) -> {
        throw new IOException("Failure");
    };

    static IOTriFunction<Object, Object, Object, Object> THROWING_IO_TRI_FUNCTION = (t, u, v) -> {
        throw new IOException("Failure");
    };

    static IOQuadFunction<Object, Object, Object, Object, Object> THROWING_IO_QUAD_FUNCTION = (t, u, v, w) -> {
        throw new IOException("Failure");
    };

    static IOSupplier<Object> THROWING_IO_SUPPLIER = () -> {
        throw new IOException("Failure");
    };

    static IORunnable THROWING_IO_RUNNABLE = () -> {
        throw new IOException("Failure");
    };

    static IOBiConsumer<Object, Object> THROWING_IO_BI_CONSUMER = (t, u) -> {
        throw new IOException("Failure");
    };

    static IOTriConsumer<Object, Object, Object> THROWING_IO_TRI_CONSUMER = (t, u, v) -> {
        throw new IOException("Failure");
    };

    static IOPredicate<Object> THROWING_IO_PREDICATE = t -> {
        throw new IOException("Failure");
    };

    static Predicate<Object> THROWING_PREDICATE = t -> {
        throw new UncheckedIOException(new IOException("Failure"));
    };

}
