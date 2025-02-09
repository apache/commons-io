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
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Like {@link IntConsumer} but throws {@link IOException}.
 *
 * @since 2.18.0
 */
@FunctionalInterface
public interface IOIntConsumer {

    /**
     * The constant no-op consumer.
     */
    IOIntConsumer NOOP = i -> {
        // noop
    };

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     * @throws IOException if an I/O error occurs.
     */
    void accept(int value) throws IOException;

    /**
     * Returns a composed {@code IOIntConsumer} that performs, in sequence, this operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the composed operation. If performing this operation throws an exception, the {@code after}
     * operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code IOIntConsumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default IOIntConsumer andThen(final IOIntConsumer after) {
        Objects.requireNonNull(after);
        return (final int i) -> {
            accept(i);
            after.accept(i);
        };
    }

    /**
     * Creates a {@link Consumer} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException IntConsumer.
     */
    default Consumer<Integer> asConsumer() {
        return i -> Uncheck.accept(this, i);
    }

    /**
     * Creates an {@link IntConsumer} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException IntConsumer.
     */
    default IntConsumer asIntConsumer() {
        return i -> Uncheck.accept(this, i);
    }

}
