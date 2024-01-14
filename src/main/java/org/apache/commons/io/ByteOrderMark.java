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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/**
 * Byte Order Mark (BOM) representation. See {@link org.apache.commons.io.input.BOMInputStream}.
 * <p>
 * We define the follow BOM constants:
 * </p>
 * <ul>
 * <li>{@link #UTF_16BE}</li>
 * <li>{@link #UTF_16LE}</li>
 * <li>{@link #UTF_32BE}</li>
 * <li>{@link #UTF_32LE}</li>
 * <li>{@link #UTF_8}</li>
 * </ul>
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @see org.apache.commons.io.input.BOMInputStream
 * @see <a href="https://en.wikipedia.org/wiki/Byte_order_mark">Wikipedia: Byte Order Mark</a>
 * @see <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#sec-guessing">W3C: Autodetection of Character Encodings
 *      (Non-Normative)</a>
 * @since 2.0
 */
public class ByteOrderMark implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * UTF-8 BOM.
     * <p>
     * This BOM is:
     * </p>
     * <pre>
     * 0xEF 0xBB 0xBF
     * </pre>
     */
    public static final ByteOrderMark UTF_8 = new ByteOrderMark(StandardCharsets.UTF_8.name(), 0xEF, 0xBB, 0xBF);

    /**
     * UTF-16BE BOM (Big-Endian).
     * <p>
     * This BOM is:
     * </p>
     * <pre>
     * 0xFE 0xFF
     * </pre>
     */
    public static final ByteOrderMark UTF_16BE = new ByteOrderMark(StandardCharsets.UTF_16BE.name(), 0xFE, 0xFF);

    /**
     * UTF-16LE BOM (Little-Endian).
     * <p>
     * This BOM is:
     * </p>
     * <pre>
     * 0xFF 0xFE
     * </pre>
     */
    public static final ByteOrderMark UTF_16LE = new ByteOrderMark(StandardCharsets.UTF_16LE.name(), 0xFF, 0xFE);

    /**
     * UTF-32BE BOM (Big-Endian).
     * <p>
     * This BOM is:
     * </p>
     * <pre>
     * 0x00 0x00 0xFE 0xFF
     * </pre>
     *
     * @since 2.2
     */
    public static final ByteOrderMark UTF_32BE = new ByteOrderMark("UTF-32BE", 0x00, 0x00, 0xFE, 0xFF);

    /**
     * UTF-32LE BOM (Little-Endian).
     * <p>
     * This BOM is:
     * </p>
     * <pre>
     * 0xFF 0xFE 0x00 0x00
     * </pre>
     *
     * @since 2.2
     */
    public static final ByteOrderMark UTF_32LE = new ByteOrderMark("UTF-32LE", 0xFF, 0xFE, 0x00, 0x00);

    /**
     * Unicode BOM character; external form depends on the encoding.
     *
     * @see <a href="https://unicode.org/faq/utf_bom.html#BOM">Byte Order Mark (BOM) FAQ</a>
     * @since 2.5
     */
    public static final char UTF_BOM = '\uFEFF';

    /**
     * Charset name.
     */
    private final String charsetName;

    /**
     * Bytes.
     */
    private final int[] bytes;

    /**
     * Constructs a new instance.
     *
     * @param charsetName The name of the charset the BOM represents
     * @param bytes The BOM's bytes
     * @throws IllegalArgumentException if the charsetName is zero length
     * @throws IllegalArgumentException if the bytes are zero length
     */
    public ByteOrderMark(final String charsetName, final int... bytes) {
        Objects.requireNonNull(charsetName, "charsetName");
        Objects.requireNonNull(bytes, "bytes");
        if (charsetName.isEmpty()) {
            throw new IllegalArgumentException("No charsetName specified");
        }
        if (bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified");
        }
        this.charsetName = charsetName;
        this.bytes = bytes.clone();
    }

    /**
     * Indicates if this instance's bytes equals another.
     *
     * @param obj The object to compare to
     * @return true if the bom's bytes are equal, otherwise
     * false
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ByteOrderMark)) {
            return false;
        }
        final ByteOrderMark bom = (ByteOrderMark) obj;
        if (bytes.length != bom.length()) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != bom.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the byte at the specified position.
     *
     * @param pos The position
     * @return The specified byte
     */
    public int get(final int pos) {
        return bytes[pos];
    }

    /**
     * Gets a copy of the BOM's bytes.
     *
     * @return a copy of the BOM's bytes
     */
    public byte[] getBytes() {
        final byte[] copy = IOUtils.byteArray(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            copy[i] = (byte) bytes[i];
        }
        return copy;
    }

    /**
     * Gets the name of the {@link java.nio.charset.Charset} the BOM represents.
     *
     * @return the character set name
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Computes the hash code for this BOM.
     *
     * @return the hash code for this BOM.
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = getClass().hashCode();
        for (final int b : bytes) {
            hashCode += b;
        }
        return hashCode;
    }

    /**
     * Gets the length of the BOM's bytes.
     *
     * @return the length of the BOM's bytes
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Converts this instance to a String representation of the BOM.
     *
     * @return the length of the BOM's bytes
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('[');
        builder.append(charsetName);
        builder.append(": ");
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("0x");
            builder.append(Integer.toHexString(0xFF & bytes[i]).toUpperCase(Locale.ROOT));
        }
        builder.append(']');
        return builder.toString();
    }

}
