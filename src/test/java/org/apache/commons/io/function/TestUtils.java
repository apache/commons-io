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
import java.util.concurrent.atomic.AtomicReference;

class TestUtils {

    static <T> T compareAndSetThrows(final AtomicReference<T> ref, final T update) throws IOException {
        return compareAndSetThrows(ref, null, update);
    }

    static <T> T compareAndSetThrows(final AtomicReference<T> ref, final T expected, final T update) throws IOException {
        if (!ref.compareAndSet(expected, update)) {
            throw new IOException("Unexpected");
        }
        return ref.get(); // same as update
    }

    @SuppressWarnings("unchecked")
    static <T> IOBinaryOperator<T> throwingIOBinaryOperator() {
        return (IOBinaryOperator<T>) TestConstants.THROWING_IO_BINARY_OPERATOR;
    }

    @SuppressWarnings("unchecked")
    static <T> IOUnaryOperator<T> throwingIOUnaryOperator() {
        return (IOUnaryOperator<T>) TestConstants.THROWING_IO_UNARY_OPERATOR;
    }

}
