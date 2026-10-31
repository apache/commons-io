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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the SwappedDataInputStream. This also
 * effectively tests the underlying EndianUtils Stream methods.
 */
class SwappedDataInputStreamTest {

    private SwappedDataInputStream sdis;
    private byte[] bytes;

    @BeforeEach
    public void setUp() {
        bytes = new byte[] {
            0x01,
            0x02,
            0x03,
            0x04,
            0x05,
            0x06,
            0x07,
            0x08
        };
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        this.sdis = new SwappedDataInputStream(bais);
    }

    @AfterEach
    public void tearDown() {
        this.sdis = null;
    }

    @SuppressWarnings({ "resource" })
    @Test
    void testCloseHandleIOException() throws IOException {
        ProxyInputStreamTest.testCloseHandleIOException(new SwappedDataInputStream(new BrokenInputStream((Throwable) new IOException())));
    }

    @Test
    void testReadAtEofThrowsEOFException() throws IOException {
        // A truncated stream must throw EOFException per the DataInput contract, never fabricate values.
        try (ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
                SwappedDataInputStream empty = new SwappedDataInputStream(bais)) {
            assertThrows(EOFException.class, empty::readByte);
            assertThrows(EOFException.class, empty::readUnsignedByte);
            assertThrows(EOFException.class, empty::readBoolean);
            assertThrows(EOFException.class, empty::readChar);
            assertThrows(EOFException.class, empty::readShort);
            assertThrows(EOFException.class, empty::readUnsignedShort);
            assertThrows(EOFException.class, empty::readInt);
            assertThrows(EOFException.class, empty::readLong);
            assertThrows(EOFException.class, empty::readFloat);
            assertThrows(EOFException.class, empty::readDouble);
        }
        // Exhausting the stream then reading again also throws.
        try (ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 0x01 });
                SwappedDataInputStream one = new SwappedDataInputStream(bais)) {
            assertEquals(0x01, one.readByte());
            assertThrows(EOFException.class, one::readByte);
            assertThrows(EOFException.class, one::readUnsignedByte);
            assertThrows(EOFException.class, one::readBoolean);
        }
    }

    @Test
    void testReadBoolean() throws IOException {
        bytes = new byte[] { 0x00, 0x01, 0x02, };
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                SwappedDataInputStream sdis = new SwappedDataInputStream(bais)) {
            assertFalse(sdis.readBoolean());
            assertTrue(sdis.readBoolean());
            assertTrue(sdis.readBoolean());
        }
    }

    @Test
    void testReadByte() throws IOException {
        assertEquals(0x01, this.sdis.readByte());
    }

    @Test
    void testReadByteValues() throws IOException {
        for (int value = 0; value <= 0xFF; value++) {
            final byte[] data = { (byte) value, (byte) value, (byte) value };
            try (SwappedDataInputStream input = new SwappedDataInputStream(new ByteArrayInputStream(data))) {
                assertEquals((byte) value, input.readByte());
                assertEquals(value, input.readUnsignedByte());
                assertEquals(value != 0, input.readBoolean());
                assertThrows(EOFException.class, input::readByte);
                assertThrows(EOFException.class, input::readUnsignedByte);
                assertThrows(EOFException.class, input::readBoolean);
            }
        }
    }

    @Test
    void testReadChar() throws IOException {
        assertEquals((char) 0x0201, this.sdis.readChar());
    }

    @Test
    void testReadDouble() throws IOException {
        assertEquals(Double.longBitsToDouble(0x0807060504030201L), this.sdis.readDouble(), 0);
    }

    @Test
    void testReadFloat() throws IOException {
        assertEquals(Float.intBitsToFloat(0x04030201), this.sdis.readFloat(), 0);
    }

    @Test
    void testReadFully() throws IOException {
        final byte[] bytesIn = new byte[8];
        this.sdis.readFully(bytesIn);
        for (int i = 0; i < 8; i++) {
            assertEquals(bytes[i], bytesIn[i]);
        }
    }

    @Test
    void testReadInt() throws IOException {
        assertEquals(0x04030201, this.sdis.readInt());
    }

    @Test
    void testReadLine() {
        assertThrows(UnsupportedOperationException.class, () -> this.sdis.readLine(), "readLine should be unsupported.");
    }

    @Test
    void testReadLong() throws IOException {
        assertEquals(0x0807060504030201L, this.sdis.readLong());
    }

    @Test
    void testReadShort() throws IOException {
        assertEquals((short) 0x0201, this.sdis.readShort());
    }

    @Test
    void testReadUnsignedByte() throws IOException {
        assertEquals(0x01, this.sdis.readUnsignedByte());
    }

    @Test
    void testReadUnsignedByteOverride() throws IOException {
        try (SwappedDataInputStream input = new SwappedDataInputStream(new ByteArrayInputStream(new byte[] { 0x2A, (byte) 0xFF, 0x01 })) {
            @Override
            public int readUnsignedByte() throws IOException {
                return Byte.toUnsignedInt(readByte());
            }
        }) {
            assertEquals(0x2A, input.readByte());
            assertEquals(0xFF, input.readUnsignedByte());
            assertTrue(input.readBoolean());
            assertThrows(EOFException.class, input::readByte);
            assertThrows(EOFException.class, input::readUnsignedByte);
            assertThrows(EOFException.class, input::readBoolean);
        }
    }

    @Test
    void testReadUnsignedShort() throws IOException {
        assertEquals((short) 0x0201, this.sdis.readUnsignedShort());
    }

    @Test
    void testReadUTF() {
        assertThrows(UnsupportedOperationException.class, () -> this.sdis.readUTF(), "readUTF should be unsupported.");
    }

    @Test
    void testSkipBytes() throws IOException {
        this.sdis.skipBytes(4);
        assertEquals(0x08070605, this.sdis.readInt());
    }

}
