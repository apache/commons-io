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

package org.apache.commons.io.input;

/**
 * Package-private factory for {@link UnsupportedOperationException} to provide messages with consistent formatting.
 *
 * <p>
 * TODO Consider making this public and use from LineIterator but this feels like it belongs in LANG rather than IO.
 * </p>
 */
class UnsupportedOperationExceptions {

    private static final String MARK_RESET = "mark/reset";

    /**
     * Creates a new instance of UnsupportedOperationException for a {@code mark} method.
     *
     * @return a new instance of UnsupportedOperationException
     */
    static UnsupportedOperationException mark() {
        // Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1.
        return method(MARK_RESET);
    }

    /**
     * Creates a new instance of UnsupportedOperationException for the given unsupported a {@code method} name.
     *
     * @param method A method name
     * @return a new instance of UnsupportedOperationException
     */
    static UnsupportedOperationException method(final String method) {
        return new UnsupportedOperationException(method + " not supported");
    }

    /**
     * Creates a new instance of UnsupportedOperationException for a {@code reset} method.
     *
     * @return a new instance of UnsupportedOperationException
     */
    static UnsupportedOperationException reset() {
        // Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1.
        return method(MARK_RESET);
    }
}
