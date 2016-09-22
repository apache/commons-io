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
import java.util.Locale;

/**
 * Converts Strings to {@link ByteOrder} instances.
 *
 * @since 2.6
 */
public class ByteOrderFactory {

    private static final Locale ComparisonLocale = Locale.ROOT;

    /**
     * Big endian.
     */
    public static final String BIG_ENDIAN = "Big";

    /**
     * Little endian.
     */
    public static final String LITTLE_ENDIAN = "Little";

    /**
     * Parses the String argument as a {@link ByteOrder}, ignoring case.
     * <p>
     * Returns {@code ByteOrder.LITTLE_ENDIAN} if the given value is {@code "little"} or {@code "LITTLE_ENDIAN"}.
     * </p>
     * <p>
     * Returns {@code ByteOrder.BIG_ENDIAN} if the given value is {@code "big"} or {@code "BIG_ENDIAN"}.
     * </p>
     * Examples:
     * <ul>
     * <li>{@code ByteOrderFactory.parseByteOrder("little")} returns {@code ByteOrder.LITTLE_ENDIAN}</li>
     * <li>{@code ByteOrderFactory.parseByteOrder("big")} returns {@code ByteOrder.BIG_ENDIAN}</li>
     * </ul>
     * 
     * @param value
     *            the {@code String} containing the ByteOrder representation to be parsed
     * @return the ByteOrder represented by the string argument
     * @throws IllegalArgumentException
     *             if the {@code String} containing the ByteOrder representation to be parsed is unknown.
     */
    public static ByteOrder parseByteOrder(final String value) {
        final String valueUp = value.toUpperCase(ComparisonLocale);
        final String bigEndianUp = BIG_ENDIAN.toUpperCase(ComparisonLocale);
        final String littleEndianUp = LITTLE_ENDIAN.toUpperCase(ComparisonLocale);
        if (bigEndianUp.equals(valueUp) || ByteOrder.BIG_ENDIAN.toString().equals(valueUp)) {
            return ByteOrder.BIG_ENDIAN;
        }
        if (littleEndianUp.equals(valueUp) || ByteOrder.LITTLE_ENDIAN.toString().equals(valueUp)) {
            return ByteOrder.LITTLE_ENDIAN;
        }
        throw new IllegalArgumentException("Unsupported byte order setting: " + value + ", expeced one of " + ByteOrder.LITTLE_ENDIAN + ", " +
                LITTLE_ENDIAN + ", " + ByteOrder.BIG_ENDIAN + ", " + bigEndianUp);
    }

}
