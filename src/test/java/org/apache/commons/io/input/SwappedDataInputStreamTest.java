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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test for the SwappedDataInputStream. This also
 * effectively tests the underlying EndianUtils Stream methods.
 *
 */

public class SwappedDataInputStreamTest {

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
        final ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
        this.sdis = new SwappedDataInputStream( bais );
    }

    @AfterEach
    public void tearDown() {
        this.sdis = null;
    }

    @Test
    public void testReadBoolean() throws IOException {
        bytes = new byte[] {0x00, 0x01, 0x02,};
        try (
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            final SwappedDataInputStream sdis = new SwappedDataInputStream(bais)
        ) {
            assertEquals(false, sdis.readBoolean());
            assertEquals(true, sdis.readBoolean());
            assertEquals(true, sdis.readBoolean());
        }
    }

    @Test
    public void testReadByte() throws IOException {
        assertEquals( 0x01, this.sdis.readByte() );
    }

    @Test
    public void testReadChar() throws IOException {
        assertEquals( (char) 0x0201, this.sdis.readChar() );
    }

    @Test
    public void testReadDouble() throws IOException {
        assertEquals( Double.longBitsToDouble(0x0807060504030201L), this.sdis.readDouble(), 0 );
    }

    @Test
    public void testReadFloat() throws IOException {
        assertEquals( Float.intBitsToFloat(0x04030201), this.sdis.readFloat(), 0 );
    }

    @Test
    public void testReadFully() throws IOException {
        final byte[] bytesIn = new byte[8];
        this.sdis.readFully(bytesIn);
        for( int i=0; i<8; i++) {
            assertEquals( bytes[i], bytesIn[i] );
        }
    }

    @Test
    public void testReadInt() throws IOException {
        assertEquals( 0x04030201, this.sdis.readInt() );
    }

    @Test
    public void testReadLine() {
        assertThrows(UnsupportedOperationException.class, () ->  this.sdis.readLine(),
                "readLine should be unsupported. ");
    }

    @Test
    public void testReadLong() throws IOException {
        assertEquals( 0x0807060504030201L, this.sdis.readLong() );
    }

    @Test
    public void testReadShort() throws IOException {
        assertEquals( (short) 0x0201, this.sdis.readShort() );
    }

    @Test
    public void testReadUnsignedByte() throws IOException {
        assertEquals( 0x01, this.sdis.readUnsignedByte() );
    }

    @Test
    public void testReadUnsignedShort() throws IOException {
        assertEquals( (short) 0x0201, this.sdis.readUnsignedShort() );
    }

    @Test
    public void testReadUTF() {
        assertThrows(UnsupportedOperationException.class, () ->  this.sdis.readUTF(),
                "readUTF should be unsupported. ");
    }

    @Test
    public void testSkipBytes() throws IOException {
        this.sdis.skipBytes(4);
        assertEquals( 0x08070605, this.sdis.readInt() );
    }

}
