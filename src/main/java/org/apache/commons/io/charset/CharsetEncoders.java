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

package org.apache.commons.io.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.function.Supplier;

/**
 * Works with {@link CharsetEncoder}.
 *
 * @since 2.12.0
 */
public final class CharsetEncoders {

    /**
     * Returns the given non-null CharsetEncoder or a new default CharsetEncoder.
     * <p>
     * Null input maps to the virtual machine's {@link Charset#defaultCharset() default charset} decoder.
     * </p>
     *
     * @param charsetEncoder The CharsetEncoder to test.
     * @return the given non-null CharsetEncoder or a new default CharsetEncoder.
     */
    public static CharsetEncoder toCharsetEncoder(final CharsetEncoder charsetEncoder) {
        return toCharsetEncoder(charsetEncoder, () -> Charset.defaultCharset().newEncoder());
    }

    /**
     * Returns the given non-null CharsetEncoder or a new default CharsetEncoder.
     *
     * @param charsetEncoder The CharsetEncoder to test.
     * @param defaultSupplier The CharsetEncoder supplier to get when charsetEncoder is null.
     * @return the given non-null CharsetEncoder or a new default CharsetEncoder.
     * @since 2.13.0
     */
    public static CharsetEncoder toCharsetEncoder(final CharsetEncoder charsetEncoder, final Supplier<CharsetEncoder> defaultSupplier) {
        return charsetEncoder != null ? charsetEncoder : defaultSupplier.get();
    }

    /** No instances. */
    private CharsetEncoders() {
        // No instances.
    }

}
