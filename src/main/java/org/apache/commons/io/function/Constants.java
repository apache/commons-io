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

/**
 * Defines package-private constants.
 */
final class Constants {

    /**
     * No-op singleton.
     */
    @SuppressWarnings("rawtypes")
    static final IOBiConsumer IO_BI_CONSUMER = (t, u) -> {/* No-op */};

    /**
     * No-op singleton.
     */
    static final IORunnable IO_RUNNABLE = () -> {/* No-op */};

    /**
     * No-op singleton.
     */
    @SuppressWarnings("rawtypes")
    static final IOBiFunction IO_BI_FUNCTION = (t, u) -> null;

    /**
     * No-op singleton.
     */
    @SuppressWarnings("rawtypes")
    static final IOFunction IO_FUNCTION_ID = t -> t;

    /**
     * Always false.
     */
    static final IOPredicate<Object> IO_PREDICATE_FALSE = t -> false;

    /**
     * Always true.
     */
    static final IOPredicate<Object> IO_PREDICATE_TRUE = t -> true;

    /**
     * No-op singleton.
     */
    @SuppressWarnings("rawtypes")
    static final IOTriConsumer IO_TRI_CONSUMER = (t, u, v) -> {/* No-op */};

    private Constants() {
        // We don't want instances
    }

}
