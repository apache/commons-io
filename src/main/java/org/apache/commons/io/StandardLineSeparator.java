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

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Enumerates standard line separators: {@link #CR}, {@link #CRLF}, {@link #LF}.
 *
 * @since 2.9.0
 */
public enum StandardLineSeparator {

    /**
     * Carriage return. This is the line ending used on Macos 9 and earlier.
     */
    CR("\r"),

    /**
     * Carriage return followed by line feed. This is the line ending used on Windows.
     */
    CRLF("\r\n"),

    /**
     * Line feed. This is the line ending used on Linux and Macos X and later.
     */
    LF("\n");

    private final String lineSeparator;

    /**
     * Constructs a new instance for a non-null line separator.
     *
     * @param lineSeparator a non-null line separator.
     */
    StandardLineSeparator(final String lineSeparator) {
        this.lineSeparator = Objects.requireNonNull(lineSeparator);
    }

    /**
     * Gets the bytes for this instance encoded using the given Charset.
     *
     * @param charset the encoding Charset.
     * @return the bytes for this instance encoded using the given Charset.
     */
    public byte[] getBytes(final Charset charset) {
        return lineSeparator.getBytes(charset);
    }

    /**
     * Gets the String value of this instance.
     *
     * @return the String value of this instance.
     */
    public String getString() {
        return lineSeparator;
    }
}
