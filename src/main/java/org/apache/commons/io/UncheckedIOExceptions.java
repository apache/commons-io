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
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Helps use {@link UncheckedIOException}.
 *
 * @since 2.12.0
 */
public class UncheckedIOExceptions {

    /**
     * Creates a new UncheckedIOException for the given detail message.
     * <p>
     * This method exists because there is no String constructor in {@link UncheckedIOException}.
     * </p>
     *
     * @param message the detail message.
     * @return a new {@link UncheckedIOException}.
     */
    public static UncheckedIOException create(final Object message) {
        final String string = Objects.toString(message);
        return new UncheckedIOException(string, new IOException(string));
    }

    /**
     * Creates a new UncheckedIOException for the given detail message.
     * <p>
     * This method exists because there is no String constructor in {@link UncheckedIOException}.
     * </p>
     *
     * @param message the detail message.
     * @param e cause the {@code IOException}.
     * @return a new {@link UncheckedIOException}.
     */
    public static UncheckedIOException create(final Object message, final IOException e) {
        return new UncheckedIOException(Objects.toString(message), e);
    }

}
