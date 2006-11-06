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


import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * Test for the SwappedDataInputStream. This also 
 * effectively tests the underlying EndianUtils Stream methods.
 *
 * @version $Revision$ $Date$
 */

public class SwappedDataInputStreamTest extends TestCase {

    private SwappedDataInputStream sdis;
    private byte[] bytes;

    public SwappedDataInputStreamTest(String name) {
        super(name);
    }

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
        ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
        this.sdis = new SwappedDataInputStream( bais );
    }

    public void tearDown() {
        this.sdis = null;
    }

    public void testReadBoolean() throws IOException {
        assertEquals( false, this.sdis.readBoolean() );
    }

    public void testReadByte() throws IOException {
        assertEquals( 0x01, this.sdis.readByte() );
    }

    public void testReadChar() throws IOException {
        assertEquals( (char) 0x0201, this.sdis.readChar() );
    }

    public void testReadDouble() throws IOException {
        assertEquals( Double.longBitsToDouble(0x0807060504030201L), this.sdis.readDouble(), 0 );
    }

    public void testReadFloat() throws IOException {
        assertEquals( Float.intBitsToFloat(0x04030201), this.sdis.readFloat(), 0 );
    }

    public void testReadFully() throws IOException {
        byte[] bytesIn = new byte[8];
        this.sdis.readFully(bytesIn);
        for( int i=0; i<8; i++) {
            assertEquals( bytes[i], bytesIn[i] );
        }
    }

    public void testReadInt() throws IOException {
        assertEquals( (int) 0x04030201, this.sdis.readInt() );
    }

    public void testReadLine() throws IOException {
        try {
            String unexpected = this.sdis.readLine();
            fail("readLine should be unsupported. ");
        } catch(UnsupportedOperationException uoe) {
        }
    }

    public void testReadLong() throws IOException {
        assertEquals( 0x0807060504030201L, this.sdis.readLong() );
    }

    public void testReadShort() throws IOException {
        assertEquals( (short) 0x0201, this.sdis.readShort() );
    }

    public void testReadUnsignedByte() throws IOException {
        assertEquals( 0x01, this.sdis.readUnsignedByte() );
    }

    public void testReadUnsignedShort() throws IOException {
        assertEquals( (short) 0x0201, this.sdis.readUnsignedShort() );
    }

    public void testReadUTF() throws IOException {
        try {
            String unexpected = this.sdis.readUTF();
            fail("readUTF should be unsupported. ");
        } catch(UnsupportedOperationException uoe) {
        }
    }

    public void testSkipBytes() throws IOException {
        this.sdis.skipBytes(4);
        assertEquals( (int)0x08070605, this.sdis.readInt() );
    }

}
