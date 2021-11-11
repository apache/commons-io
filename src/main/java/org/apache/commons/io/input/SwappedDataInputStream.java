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

import static org.apache.commons.io.IOUtils.EOF;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.EndianUtils;

/**
 * DataInput for systems relying on little endian data formats. When read, values will be changed from little endian to
 * big endian formats for internal usage.
 * <p>
 * <b>Origin of code: </b>Avalon Excalibur (IO)
 * </p>
 *
 */
public class SwappedDataInputStream extends TaggedInputStream implements DataInput {

    /**
     * Constructs a SwappedDataInputStream.
     *
     * @param input InputStream to read from
     */
    public SwappedDataInputStream(final InputStream input) {
        super(input);
    }

    /**
     * Return <code>{@link #readByte()} != 0</code>
     *
     * @return false if the byte read is zero, otherwise true
     * @throws IOException if an I/O error occurs.
     * @throws EOFException if an end of file is reached unexpectedly
     */
    @Override
    public boolean readBoolean() throws IOException, EOFException {
        return 0 != readByte();
    }

    /**
     * Invokes the delegate's {@code read()} method.
     *
     * @return the byte read or -1 if the end of stream
     * @throws IOException if an I/O error occurs.
     * @throws EOFException if an end of file is reached unexpectedly
     */
    @Override
    public byte readByte() throws IOException, EOFException {
        final int returnValue = super.read();
        if (returnValue == EOF) {
            handleIOException(new EOFException("Unexpected EOF reached"));
        }
        return (byte) returnValue;
    }

    /**
     * Reads a character delegating to {@link #readShort()}.
     *
     * @return the byte read or -1 if the end of stream
     * @throws IOException if an I/O error occurs.
     * @throws EOFException if an end of file is reached unexpectedly
     */
    @Override
    public char readChar() throws IOException, EOFException {
        return (char) readShort();
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedDouble(InputStream)}.
     *
     * @return the read long
     * @throws IOException if an I/O error occurs.
     * @throws EOFException if an end of file is reached unexpectedly
     */
    @Override
    public double readDouble() throws IOException, EOFException {
        double returnValue;
        try {
            returnValue = EndianUtils.readSwappedDouble(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedFloat(InputStream)}.
     *
     * @return the read long
     * @throws IOException if an I/O error occurs.
     * @throws EOFException if an end of file is reached unexpectedly
     */
    @Override
    public float readFloat() throws IOException, EOFException {
        float returnValue;
        try {
            returnValue = EndianUtils.readSwappedFloat(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Invokes the delegate's {@code read(byte[] data, int, int)} method.
     *
     * @param data the buffer to read the bytes into
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void readFully(final byte[] data) throws IOException, EOFException {
        readFully(data, 0, data.length);
    }

    /**
     * Invokes the delegate's {@code read(byte[] data, int, int)} method.
     *
     * @param data the buffer to read the bytes into
     * @param offset The start offset
     * @param length The number of bytes to read
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void readFully(final byte[] data, final int offset, final int length) throws IOException, EOFException {
        int remaining = length;

        while (remaining > 0) {
            final int location = offset + length - remaining;
            final int count = read(data, location, remaining);

            if (EOF == count) {
                handleIOException(new EOFException("Unexpected EOF reached"));
                break; // Never reached, exception is thrown.
            }

            remaining -= count;
        }
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedInteger(InputStream)}.
     *
     * @return the read long
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int readInt() throws IOException, EOFException {
        int returnValue;
        try {
            returnValue = EndianUtils.readSwappedInteger(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Not currently supported - throws {@link UnsupportedOperationException}.
     *
     * @return the line read
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public String readLine() throws IOException, EOFException {
        throw UnsupportedOperationExceptions.method("readLine");
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedLong(InputStream)}.
     *
     * @return the read long
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long readLong() throws IOException, EOFException {
        long returnValue;
        try {
            returnValue = EndianUtils.readSwappedLong(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedShort(InputStream)}.
     *
     * @return the read long
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public short readShort() throws IOException, EOFException {
        short returnValue;
        try {
            returnValue = EndianUtils.readSwappedShort(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Invokes the delegate's {@code read()} method.
     *
     * @return the byte read or -1 if the end of stream
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int readUnsignedByte() throws IOException, EOFException {
        final int returnValue = super.read();
        if (returnValue == EOF) {
            handleIOException(new EOFException("Unexpected EOF reached"));
        }
        return returnValue;
    }

    /**
     * Delegates to {@link EndianUtils#readSwappedUnsignedShort(InputStream)}.
     *
     * @return the read long
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int readUnsignedShort() throws IOException, EOFException {
        int returnValue;
        try {
            returnValue = EndianUtils.readSwappedUnsignedShort(this);
        } catch (IOException e) {
            handleIOException(e);
            returnValue = EOF;
        }
        return returnValue;
    }

    /**
     * Not currently supported - throws {@link UnsupportedOperationException}.
     *
     * @return UTF String read
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public String readUTF() throws IOException, EOFException {
        throw UnsupportedOperationExceptions.method("readUTF");
    }

    /**
     * Invokes the delegate's {@code skip(int)} method.
     *
     * @param count the number of bytes to skip
     * @return the number of bytes to skipped or -1 if the end of stream
     * @throws EOFException if an end of file is reached unexpectedly
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int skipBytes(final int count) throws IOException, EOFException {
        return (int) super.skip(count);
    }

}
