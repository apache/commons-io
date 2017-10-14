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

import java.nio.ByteOrder;

/**
 * Converts Strings to {@link ByteOrder} instances.
 *
 * @since 2.6
 */
public final class ByteOrderParser {

    /**
     * ByteOrderUtils is a static utility class, so prevent construction with a private constructor.
     */
    private ByteOrderParser() {
    }

    /**
     * Parses the String argument as a {@link ByteOrder}.
     * <p>
     * Returns {@code ByteOrder.LITTLE_ENDIAN} if the given value is {@code "LITTLE_ENDIAN"}.
     * </p>
     * <p>
     * Returns {@code ByteOrder.BIG_ENDIAN} if the given value is {@code "BIG_ENDIAN"}.
     * </p>
     * Examples:
     * <ul>
     * <li>{@code ByteOrderParser.parseByteOrder("LITTLE_ENDIAN")} returns {@code ByteOrder.LITTLE_ENDIAN}</li>
     * <li>{@code ByteOrderParser.parseByteOrder("BIG_ENDIAN")} returns {@code ByteOrder.BIG_ENDIAN}</li>
     * </ul>
     *
     * @param value
     *            the {@code String} containing the ByteOrder representation to be parsed
     * @return the ByteOrder represented by the string argument
     * @throws IllegalArgumentException
     *             if the {@code String} containing the ByteOrder representation to be parsed is unknown.
     */
    public static ByteOrder parseByteOrder(final String value) {
        if (ByteOrder.BIG_ENDIAN.toString().equals(value)) {
            return ByteOrder.BIG_ENDIAN;
        }
        if (ByteOrder.LITTLE_ENDIAN.toString().equals(value)) {
            return ByteOrder.LITTLE_ENDIAN;
        }
        throw new IllegalArgumentException("Unsupported byte order setting: " + value + ", expeced one of " + ByteOrder.LITTLE_ENDIAN +
                 ", " + ByteOrder.BIG_ENDIAN);
    }

}
