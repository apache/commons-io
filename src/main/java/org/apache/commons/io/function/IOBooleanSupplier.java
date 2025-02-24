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
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Like {@link BooleanSupplier} but throws {@link IOException}.
 *
 * @since 2.19.0
 */
@FunctionalInterface
public interface IOBooleanSupplier {

    /**
     * Creates a {@link Supplier} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException Supplier.
     */
    default BooleanSupplier asBooleanSupplier() {
        return () -> Uncheck.getAsBoolean(this);
    }

    /**
     * Gets a result.
     *
     * @return a result
     * @throws IOException if an I/O error occurs.
     */
    boolean getAsBoolean() throws IOException;
}
