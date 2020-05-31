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

package org.apache.commons.io;

import java.io.IOException;

/**
 * A IOException associated with a source index.
 *
 * @since 2.7
 */
public class IOIndexedException extends IOException {

    private static final long serialVersionUID = 1L;
    private final int index;

    /**
     * Creates a new exception.
     *
     * @param index index of this exception.
     * @param cause cause exceptions.
     */
    public IOIndexedException(final int index, final Throwable cause) {
        super(toMessage(index, cause), cause);
        this.index = index;
    }

    /**
     * Converts input to a suitable String for exception message.
     *
     * @param index An index into a source collection.
     * @param cause A cause.
     * @return A message.
     */
    protected static String toMessage(final int index, final Throwable cause) {
        // Letting index be any int
        final String unspecified = "Null";
        final String name = cause == null ? unspecified : cause.getClass().getSimpleName();
        final String msg = cause == null ? unspecified : cause.getMessage();
        return String.format("%s #%,d: %s", name, index, msg);
    }

    /**
     * The index of this exception.
     *
     * @return index of this exception.
     */
    public int getIndex() {
        return index;
    }

}
