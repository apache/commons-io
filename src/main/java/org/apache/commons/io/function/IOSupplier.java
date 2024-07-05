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
import java.util.function.Supplier;

/**
 * Like {@link Supplier} but throws {@link IOException}.
 * <p>
 * Using an IOSupplier allows you to compose usage of checked and unchecked exceptions as you best see fit.
 * </p>
 *
 * @param <T> the return type of the operations.
 * @since 2.7
 */
@FunctionalInterface
public interface IOSupplier<T> {

    /**
     * Creates a {@link Supplier} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException Supplier.
     * @since 2.12.0
     */
    default Supplier<T> asSupplier() {
        return this::getUnchecked;
    }

    /**
     * Gets a result.
     *
     * @return a result.
     * @throws IOException if an I/O error occurs.
     */
    T get() throws IOException;

    /**
     * Gets a result.
     *
     * @return a result.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.17.0
     */
    default T getUnchecked() throws UncheckedIOException {
        return Uncheck.get(this);
    }
}
