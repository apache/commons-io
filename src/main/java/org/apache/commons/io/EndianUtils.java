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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helps with reading and writing primitive numeric types ({@code short},
 * {@code int}, {@code long}, {@code float}, and {@code double}) that are
 * encoded in little endian using two's complement or unsigned representations.
 * <p>
 * Different computer architectures have different conventions for
 * byte ordering. In "Little Endian" architectures (e.g. X86),
 * the low-order byte is stored in memory at the lowest address, and
 * subsequent bytes at higher addresses. In "Big Endian" architectures
 * (e.g. Motorola 680X0), the situation is reversed.
 * Most methods and classes throughout Java &mdash; e.g. {@code DataInputStream} and
 * {@code Double.longBitsToDouble()} &mdash; assume data is laid out
 * in big endian order with the most significant byte first.
 * The methods in this class read and write data in little endian order,
 * generally by reversing the bytes and then using the
 * regular Java methods to convert the swapped bytes to a primitive type.
 * </p>
 * <p>
 * Provenance: Excalibur
 * </p>
 *
 * @see org.apache.commons.io.input.SwappedDataInputStream
 */
public class EndianUtils {

    /**
     * Reads the next byte from the input stream.
     * @param input  the stream
     * @return the byte
     * @throws IOException if the end of file is reached
     */
    private static int read(final InputStream input) throws IOException {
        final int value = input.read();
        if (EOF == value) {
            throw new EOFException("Unexpected EOF reached");
        }
        return value;
    }

    /**
     * Reads a little endian {@code double} value from a byte array at a given offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 8 bytes
     */
    public static double readSwappedDouble(final byte[] data, final int offset) {
        return Double.longBitsToDouble(readSwappedLong(data, offset));
    }

    /**
     * Reads a little endian {@code double} value from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static double readSwappedDouble(final InputStream input) throws IOException {
        return Double.longBitsToDouble(readSwappedLong(input));
    }

    /**
     * Reads a little endian {@code float} value from a byte array at a given offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 4 bytes
     */
    public static float readSwappedFloat(final byte[] data, final int offset) {
        return Float.intBitsToFloat(readSwappedInteger(data, offset));
    }

    /**
     * Reads a little endian {@code float} value from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static float readSwappedFloat(final InputStream input) throws IOException {
        return Float.intBitsToFloat(readSwappedInteger(input));
    }

    /**
     * Reads a little endian {@code int} value from a byte array at a given offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 4 bytes
     */
    public static int readSwappedInteger(final byte[] data, final int offset) {
        validateByteArrayOffset(data, offset, Integer.SIZE / Byte.SIZE);
        return ((data[offset + 0] & 0xff) << 0) +
            ((data[offset + 1] & 0xff) << 8) +
            ((data[offset + 2] & 0xff) << 16) +
            ((data[offset + 3] & 0xff) << 24);
    }

    /**
     * Reads a little endian {@code int} value from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static int readSwappedInteger(final InputStream input) throws IOException {
        final int value1 = read(input);
        final int value2 = read(input);
        final int value3 = read(input);
        final int value4 = read(input);
        return ((value1 & 0xff) << 0) + ((value2 & 0xff) << 8) + ((value3 & 0xff) << 16) + ((value4 & 0xff) << 24);
    }

    /**
     * Reads a little endian {@code long} value from a byte array at a given offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 8 bytes
     */
    public static long readSwappedLong(final byte[] data, final int offset) {
        validateByteArrayOffset(data, offset, Long.SIZE / Byte.SIZE);
        final long low = readSwappedInteger(data, offset);
        final long high = readSwappedInteger(data, offset + 4);
        return (high << 32) + (0xffffffffL & low);
    }

    /**
     * Reads a little endian {@code long} value from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static long readSwappedLong(final InputStream input) throws IOException {
        final byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) read(input);
        }
        return readSwappedLong(bytes, 0);
    }

    /**
     * Reads a little endian {@code short} value from a byte array at a given offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 2 bytes
     */
    public static short readSwappedShort(final byte[] data, final int offset) {
        validateByteArrayOffset(data, offset, Short.SIZE / Byte.SIZE);
        return (short) (((data[offset + 0] & 0xff) << 0) + ((data[offset + 1] & 0xff) << 8));
    }

    /**
     * Reads a little endian {@code short} value from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static short readSwappedShort(final InputStream input) throws IOException {
        return (short) (((read(input) & 0xff) << 0) + ((read(input) & 0xff) << 8));
    }

    /**
     * Reads a little endian unsigned integer (32-bit) value from a byte array at a given
     * offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 4 bytes
    */
    public static long readSwappedUnsignedInteger(final byte[] data, final int offset) {
        validateByteArrayOffset(data, offset, Integer.SIZE / Byte.SIZE);
        final long low = ((data[offset + 0] & 0xff) << 0) +
                     ((data[offset + 1] & 0xff) << 8) +
                     ((data[offset + 2] & 0xff) << 16);
        final long high = data[offset + 3] & 0xff;
        return (high << 24) + (0xffffffffL & low);
    }

    /**
     * Reads a little endian unsigned integer (32-bit) from an InputStream.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static long readSwappedUnsignedInteger(final InputStream input) throws IOException {
        final int value1 = read(input);
        final int value2 = read(input);
        final int value3 = read(input);
        final int value4 = read(input);
        final long low = ((value1 & 0xff) << 0) + ((value2 & 0xff) << 8) + ((value3 & 0xff) << 16);
        final long high = value4 & 0xff;
        return (high << 24) + (0xffffffffL & low);
    }

    /**
     * Reads an unsigned short (16-bit) value from a byte array in little endian order at a given
     * offset.
     *
     * @param data source byte array
     * @param offset starting offset in the byte array
     * @return the value read
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 2 bytes
     */
    public static int readSwappedUnsignedShort(final byte[] data, final int offset) {
        validateByteArrayOffset(data, offset, Short.SIZE / Byte.SIZE);
        return ((data[offset + 0] & 0xff) << 0) + ((data[offset + 1] & 0xff) << 8);
    }

    /**
     * Reads an unsigned short (16-bit) from an InputStream in little endian order.
     *
     * @param input source InputStream
     * @return the value just read
     * @throws IOException in case of an I/O problem
     */
    public static int readSwappedUnsignedShort(final InputStream input) throws IOException {
        final int value1 = read(input);
        final int value2 = read(input);

        return ((value1 & 0xff) << 0) + ((value2 & 0xff) << 8);
    }

    /**
     * Converts a {@code double} value from big endian to little endian
     * and vice versa. That is, it converts the {@code double} to bytes,
     * reverses the bytes, and then reinterprets those bytes as a new {@code double}.
     * This can be useful if you have a number that was read from the
     * underlying source in the wrong endianness.
     *
     * @param value value to convert
     * @return the converted value
     */
    public static double swapDouble(final double value) {
        return Double.longBitsToDouble(swapLong(Double.doubleToLongBits(value)));
    }

    /**
     * Converts a {@code float} value from big endian to little endian and vice versa.
     *
     * @param value value to convert
     * @return the converted value
     */
    public static float swapFloat(final float value) {
        return Float.intBitsToFloat(swapInteger(Float.floatToIntBits(value)));
    }

    /**
     * Converts an {@code int} value from big endian to little endian and vice versa.
     *
     * @param value value to convert
     * @return the converted value
     */
    public static int swapInteger(final int value) {
        return
            ((value >> 0 & 0xff) << 24) +
            ((value >> 8 & 0xff) << 16) +
            ((value >> 16 & 0xff) << 8) +
            ((value >> 24 & 0xff) << 0);
    }

    /**
     * Converts a {@code long} value from big endian to little endian and vice versa.
     *
     * @param value value to convert
     * @return the converted value
     */
    public static long swapLong(final long value) {
        return
            ((value >> 0 & 0xff) << 56) +
            ((value >> 8 & 0xff) << 48) +
            ((value >> 16 & 0xff) << 40) +
            ((value >> 24 & 0xff) << 32) +
            ((value >> 32 & 0xff) << 24) +
            ((value >> 40 & 0xff) << 16) +
            ((value >> 48 & 0xff) << 8) +
            ((value >> 56 & 0xff) << 0);
    }

    /**
     * Converts a {@code short} value from big endian to little endian and vice versa.
     *
     * @param value value to convert
     * @return the converted value
     */
    public static short swapShort(final short value) {
        return (short) (((value >> 0 & 0xff) << 8) +
            ((value >> 8 & 0xff) << 0));
    }

    /**
     * Validates if the provided byte array has enough data.
     *
     * @param data the input byte array
     * @param offset the input offset
     * @param byteNeeded the needed number of bytes
     * @throws IllegalArgumentException if the byte array does not have enough data
     */
    private static void validateByteArrayOffset(final byte[] data, final int offset, final int byteNeeded) {
        if (data.length < offset + byteNeeded) {
            throw new IllegalArgumentException("Data only has " + data.length + "bytes, needed " + (offset + byteNeeded) + "bytes.");
        }
    }

    /**
     * Writes the 8 bytes of a {@code double} to a byte array at a given offset in little endian order.
     *
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 8 bytes
     */
    public static void writeSwappedDouble(final byte[] data, final int offset, final double value) {
        writeSwappedLong(data, offset, Double.doubleToLongBits(value));
    }

    /**
     * Writes the 8 bytes of a {@code double} to an output stream in little endian order.
     *
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedDouble(final OutputStream output, final double value) throws IOException {
        writeSwappedLong(output, Double.doubleToLongBits(value));
    }

    /**
     * Writes the 4 bytes of a {@code float} to a byte array at a given offset in little endian order.
     *
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 4 bytes
     */
    public static void writeSwappedFloat(final byte[] data, final int offset, final float value) {
        writeSwappedInteger(data, offset, Float.floatToIntBits(value));
    }

    /**
     * Writes the 4 bytes of a {@code float} to an output stream in little endian order.
     *
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
    */
    public static void writeSwappedFloat(final OutputStream output, final float value) throws IOException {
        writeSwappedInteger(output, Float.floatToIntBits(value));
    }

    /**
     * Writes the 4 bytes of an {@code int} to a byte array at a given offset in little endian order.
     *
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 4 bytes
     */
    public static void writeSwappedInteger(final byte[] data, final int offset, final int value) {
        validateByteArrayOffset(data, offset, Integer.SIZE / Byte.SIZE);
        data[offset + 0] = (byte) (value >> 0 & 0xff);
        data[offset + 1] = (byte) (value >> 8 & 0xff);
        data[offset + 2] = (byte) (value >> 16 & 0xff);
        data[offset + 3] = (byte) (value >> 24 & 0xff);
    }

    /**
     * Writes the 4 bytes of an {@code int} to an output stream in little endian order.
     *
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedInteger(final OutputStream output, final int value) throws IOException {
        output.write((byte) (value >> 0 & 0xff));
        output.write((byte) (value >> 8 & 0xff));
        output.write((byte) (value >> 16 & 0xff));
        output.write((byte) (value >> 24 & 0xff));
    }

    /**
     * Writes the 8 bytes of a {@code long} to a byte array at a given offset in little endian order.
     *
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 8 bytes
     */
    public static void writeSwappedLong(final byte[] data, final int offset, final long value) {
        validateByteArrayOffset(data, offset, Long.SIZE / Byte.SIZE);
        data[offset + 0] = (byte) (value >> 0 & 0xff);
        data[offset + 1] = (byte) (value >> 8 & 0xff);
        data[offset + 2] = (byte) (value >> 16 & 0xff);
        data[offset + 3] = (byte) (value >> 24 & 0xff);
        data[offset + 4] = (byte) (value >> 32 & 0xff);
        data[offset + 5] = (byte) (value >> 40 & 0xff);
        data[offset + 6] = (byte) (value >> 48 & 0xff);
        data[offset + 7] = (byte) (value >> 56 & 0xff);
    }

    /**
     * Writes the 8 bytes of a {@code long} to an output stream in little endian order.
     *
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedLong(final OutputStream output, final long value) throws IOException {
        output.write((byte) (value >> 0 & 0xff));
        output.write((byte) (value >> 8 & 0xff));
        output.write((byte) (value >> 16 & 0xff));
        output.write((byte) (value >> 24 & 0xff));
        output.write((byte) (value >> 32 & 0xff));
        output.write((byte) (value >> 40 & 0xff));
        output.write((byte) (value >> 48 & 0xff));
        output.write((byte) (value >> 56 & 0xff));
    }

    /**
     * Writes the 2 bytes of a {@code short} to a byte array at a given offset in little endian order.
     *
     * @param data target byte array
     * @param offset starting offset in the byte array
     * @param value value to write
     * @throws IllegalArgumentException if the part of the byte array starting at offset does not have at least 2 bytes
     */
    public static void writeSwappedShort(final byte[] data, final int offset, final short value) {
        validateByteArrayOffset(data, offset, Short.SIZE / Byte.SIZE);
        data[offset + 0] = (byte) (value >> 0 & 0xff);
        data[offset + 1] = (byte) (value >> 8 & 0xff);
    }

    /**
     * Writes the 2 bytes of a {@code short} to an output stream using little endian encoding.
     *
     * @param output target OutputStream
     * @param value value to write
     * @throws IOException in case of an I/O problem
     */
    public static void writeSwappedShort(final OutputStream output, final short value) throws IOException {
        output.write((byte) (value >> 0 & 0xff));
        output.write((byte) (value >> 8 & 0xff));
    }

    /**
     * Instances should NOT be constructed in standard programming.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public EndianUtils() {
        // empty
    }
}
