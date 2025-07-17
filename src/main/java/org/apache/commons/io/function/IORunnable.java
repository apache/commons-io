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

/**
 * Like {@link Runnable} but throws {@link IOException}.
 *
 * @since 2.12.0
 */
@FunctionalInterface
public interface IORunnable {

    /**
     * Returns the constant no-op runnable.
     *
     * @return a constant no-op runnable.
     * @since 2.16.0
     */
    static IORunnable noop() {
        return Constants.IO_RUNNABLE;
    }

    /**
     * Creates a {@link Runnable} for this instance that throws {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @return an UncheckedIOException Predicate.
     */
    default Runnable asRunnable() {
        return () -> Uncheck.run(this);
    }

    /**
     * Like {@link Runnable#run()} but throws {@link IOException}.
     *
     * @throws IOException if an I/O error occurs.
     */
    void run() throws IOException;
}
