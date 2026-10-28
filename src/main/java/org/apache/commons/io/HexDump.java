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
package org.apache.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.io.output.CloseShieldOutputStream;

/**
 * Dumps data in hexadecimal format.
 * <p>
 * Provides a single function to take an array of bytes and display it
 * in hexadecimal form.
 * </p>
 * <p>
 * Provenance: POI.
 * </p>
 */
public class HexDump {

    /**
     * The line-separator (initializes to "line.separator" system property).
     *
     * @deprecated Use {@link System#lineSeparator()}.
     */
    @Deprecated
    public static final String EOL = System.lineSeparator();

    private static final char[] HEX_CODES =
            {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'
            };

    private static final int[] SHIFTS =
            {
                28, 24, 20, 16, 12, 8, 4, 0
            };

    /**
     * Dumps an array of bytes to an Appendable. The output is formatted
     * for human inspection, with a hexadecimal offset followed by the
     * hexadecimal values of the next 16 bytes of data and the printable ASCII
     * characters (if any) that those bytes represent printed per each line
     * of output.
     *
     * @param data  The byte array to be dumped.
     * @param appendable  The Appendable to which the data is to be written.
     * @throws IOException is thrown if anything goes wrong writing
     *         the data to appendable.
     * @throws NullPointerException if the output appendable is null.
     * @since 2.12.0
     */
    public static void dump(final byte[] data, final Appendable appendable)
            throws IOException {
        dump(data, 0, appendable, 0, data.length);
    }

    /**
     * Dumps an array of bytes to an Appendable. The output is formatted
     * for human inspection, with a hexadecimal offset followed by the
     * hexadecimal values of the next 16 bytes of data and the printable ASCII
     * characters (if any) that those bytes represent printed per each line
     * of output.
     * <p>
     * The offset argument specifies the start offset of the data array
     * within a larger entity like a file or an incoming stream. For example,
     * if the data array contains the third kibibyte of a file, then the
     * offset argument should be set to 2048. The offset value printed
     * at the beginning of each line indicates where in that larger entity
     * the first byte on that line is located.
     * </p>
     *
     * @param data  The byte array to be dumped.
     * @param offset  offset of the byte array within a larger entity.
     * @param appendable  The Appendable to which the data is to be written.
     * @param index initial index into the byte array.
     * @param length number of bytes to dump from the array.
     * @throws IOException is thrown if anything goes wrong writing
     *         the data to appendable.
     * @throws ArrayIndexOutOfBoundsException if the index or length is
     *         outside the data array's bounds.
     * @throws NullPointerException if the output appendable is null.
     * @since 2.12.0
     */
    public static void dump(final byte[] data, final long offset,
                            final Appendable appendable, final int index,
                            final int length)
            throws IOException, ArrayIndexOutOfBoundsException {
        Objects.requireNonNull(appendable, "appendable");
        if (index < 0 || index >= data.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "illegal index: " + index + " into array of length "
                    + data.length);
        }
        long displayOffset = offset + index;
        final StringBuilder buffer = new StringBuilder(74);

        // TODO Use Objects.checkFromIndexSize(index, length, data.length) when upgrading to JDK9
        if (length < 0 || index + length > data.length) {
            throw new ArrayIndexOutOfBoundsException(String.format("Range [%s, %<s + %s) out of bounds for length %s", index, length, data.length));
        }

        final int endIndex = index + length;

        for (int lineStart = index; lineStart < endIndex; lineStart += 16) {
            int bytesInLine = endIndex - lineStart;

            if (bytesInLine > 16) {
                bytesInLine = 16;
            }
            appendHex(buffer, displayOffset).append(' ');
            for (int column = 0; column < 16; column++) {
                if (column < bytesInLine) {
                    appendHex(buffer, data[column + lineStart]);
                } else {
                    buffer.append("  ");
                }
                buffer.append(' ');
            }
            for (int column = 0; column < bytesInLine; column++) {
                if (data[column + lineStart] >= ' ' && data[column + lineStart] < 127) {
                    buffer.append((char) data[column + lineStart]);
                } else {
                    buffer.append('.');
                }
            }
            buffer.append(System.lineSeparator());
            appendable.append(buffer);
            buffer.setLength(0);
            displayOffset += bytesInLine;
        }
    }

    /**
     * Dumps an array of bytes to an OutputStream. The output is formatted
     * for human inspection, with a hexadecimal offset followed by the
     * hexadecimal values of the next 16 bytes of data and the printable ASCII
     * characters (if any) that those bytes represent printed per each line
     * of output.
     * <p>
     * The offset argument specifies the start offset of the data array
     * within a larger entity like a file or an incoming stream. For example,
     * if the data array contains the third kibibyte of a file, then the
     * offset argument should be set to 2048. The offset value printed
     * at the beginning of each line indicates where in that larger entity
     * the first byte on that line is located.
     * </p>
     * <p>
     * All bytes between the given index (inclusive) and the end of the
     * data array are dumped.
     * </p>
     * <p>
     * This method uses the virtual machine's {@linkplain Charset#defaultCharset() default charset}.
     * </p>
     *
     * @param data  The byte array to be dumped.
     * @param offset  offset of the byte array within a larger entity.
     * @param stream  The OutputStream to which the data is to be
     *               written.
     * @param index initial index into the byte array.
     * @throws IOException is thrown if anything goes wrong writing
     *         the data to stream.
     * @throws ArrayIndexOutOfBoundsException if the index is
     *         outside the data array's bounds.
     * @throws NullPointerException if the output stream is null.
     */
    @SuppressWarnings("resource") // Caller closes stream
    public static void dump(final byte[] data, final long offset,
                            final OutputStream stream, final int index)
            throws IOException, ArrayIndexOutOfBoundsException {
        Objects.requireNonNull(stream, "stream");

        try (OutputStreamWriter out = new OutputStreamWriter(CloseShieldOutputStream.wrap(stream), Charset.defaultCharset())) {
            dump(data, offset, out, index, data.length - index);
        }
    }

    /**
     * Appends a byte as two hexadecimal digits to a StringBuilder.
     *
     * @param builder The StringBuilder to dump the value in.
     * @param value  The byte value to be dumped.
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder appendHex(final StringBuilder builder, final byte value) {
        for (int digit = 0; digit < 2; digit++) {
            builder.append(HEX_CODES[value >> SHIFTS[digit + 6] & 15]);
        }
        return builder;
    }

    /**
     * Appends the 32 least significant bits of a long as eight hexadecimal digits to a StringBuilder.
     *
     * @param builder The StringBuilder to dump the value in.
     * @param value  The long value to be dumped.
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder appendHex(final StringBuilder builder, final long value) {
        for (int digit = 0; digit < 8; digit++) {
            builder.append(HEX_CODES[(int) (value >> SHIFTS[digit]) & 15]);
        }
        return builder;
    }

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public HexDump() {
    }

}
