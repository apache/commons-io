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

import org.junit.jupiter.api.Test;

import static org.apache.commons.io.input.UnsynchronizedByteArrayInputStream.END_OF_STREAM;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

/**
 * Basic unit tests for the alternative ByteArrayInputStream implementation.
 */
public class UnsynchronizedByteArrayInputStreamTest {

    @Test
    public void testConstructor1() throws IOException {
        final byte[] empty = new byte[0];
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty);
        assertEquals(empty.length, is.available());

        is.close();
        is = new UnsynchronizedByteArrayInputStream(one);
        assertEquals(one.length, is.available());

        is.close();
        is = new UnsynchronizedByteArrayInputStream(some);
        assertEquals(some.length, is.available());
        is.close();
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testConstructor2() {
        final byte[] empty = new byte[0];
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty, 0);
        assertEquals(empty.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(empty, 1);
        assertEquals(0, is.available());

        is = new UnsynchronizedByteArrayInputStream(one, 0);
        assertEquals(one.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 1);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 2);
        assertEquals(0, is.available());

        is = new UnsynchronizedByteArrayInputStream(some, 0);
        assertEquals(some.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 1);
        assertEquals(some.length - 1, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 10);
        assertEquals(some.length - 10, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, some.length);
        assertEquals(0, is.available());
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testConstructor3() {
        final byte[] empty = new byte[0];
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty, 0);
        assertEquals(empty.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(empty, 1);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(empty, 0,1);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(empty, 1,1);
        assertEquals(0, is.available());

        is = new UnsynchronizedByteArrayInputStream(one, 0);
        assertEquals(one.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 1);
        assertEquals(one.length - 1, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 2);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 0, 1);
        assertEquals(1, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 1, 1);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 0, 2);
        assertEquals(1, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 2, 1);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(one, 2, 2);
        assertEquals(0, is.available());

        is = new UnsynchronizedByteArrayInputStream(some, 0);
        assertEquals(some.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 1);
        assertEquals(some.length - 1, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 10);
        assertEquals(some.length - 10, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, some.length);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, some.length, some.length);
        assertEquals(0, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, some.length - 1, some.length);
        assertEquals(1, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 0, 7);
        assertEquals(7, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 7, 7);
        assertEquals(7, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, 0, some.length * 2);
        assertEquals(some.length, is.available());
        is = new UnsynchronizedByteArrayInputStream(some, some.length - 1, 7);
        assertEquals(1, is.available());
    }

    @Test
    public void testInvalidConstructor2OffsetUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(new byte[0], -1);
        });
    }

    @Test
    public void testInvalidConstructor3LengthUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(new byte[0], 0, -1);
        });
    }

    @Test
    public void testInvalidConstructor3OffsetUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(new byte[0], -1, 1);
        });
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testInvalidReadArrayExplicitLenUnder() {
        final byte[] buf = new byte[0];
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, 0, -1);
        });
    }

    @Test
    public void testInvalidReadArrayExplicitOffsetUnder() {
        final byte[] buf = new byte[0];
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, -1, 1);
        });
    }

    @Test
    public void testInvalidReadArrayExplicitRangeOver() {
        final byte[] buf = new byte[0];
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, 0, 1);
        });
    }

    @Test
    public void testInvalidReadArrayNull() {
        final byte[] buf = null;
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(NullPointerException.class, () -> {
            is.read(buf);
        });
    }

    @Test
    public void testInvalidSkipNUnder() {
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IllegalArgumentException.class, () -> {
            is.skip(-1);
        });
    }

    @Test
    public void testMarkReset() {
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertTrue(is.markSupported());
        assertEquals(0xa, is.read());
        assertTrue(is.markSupported());

        is.mark(10);

        assertEquals(0xb, is.read());
        assertEquals(0xc, is.read());

        is.reset();

        assertEquals(0xb, is.read());
        assertEquals(0xc, is.read());
        assertEquals(END_OF_STREAM, is.read());

        is.reset();

        assertEquals(0xb, is.read());
        assertEquals(0xc, is.read());
        assertEquals(END_OF_STREAM, is.read());
    }

    @Test
    public void testReadArray() {
        byte[] buf = new byte[10];
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[0]);
        int read = is.read(buf);
        assertEquals(END_OF_STREAM, read);
        assertArrayEquals(new byte[10], buf);

        buf = new byte[0];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertEquals(0, read);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertEquals(3, read);
        assertEquals(0xa, buf[0]);
        assertEquals(0xb, buf[1]);
        assertEquals(0xc, buf[2]);
        assertEquals(0, buf[3]);

        buf = new byte[2];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertEquals(2, read);
        assertEquals(0xa, buf[0]);
        assertEquals(0xb, buf[1]);
        read = is.read(buf);
        assertEquals(1, read);
        assertEquals(0xc, buf[0]);
    }

    @Test
    public void testReadArrayExplicit() {
        byte[] buf = new byte[10];
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[0]);
        int read = is.read(buf, 0, 10);
        assertEquals(END_OF_STREAM, read);
        assertArrayEquals(new byte[10], buf);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[0]);
        read = is.read(buf, 4, 2);
        assertEquals(END_OF_STREAM, read);
        assertArrayEquals(new byte[10], buf);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[0]);
        read = is.read(buf, 4, 6);
        assertEquals(END_OF_STREAM, read);
        assertArrayEquals(new byte[10], buf);

        buf = new byte[0];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf, 0,0);
        assertEquals(0, read);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf, 0, 2);
        assertEquals(2, read);
        assertEquals(0xa, buf[0]);
        assertEquals(0xb, buf[1]);
        assertEquals(0, buf[2]);
        read = is.read(buf, 0, 10);
        assertEquals(1, read);
        assertEquals(0xc, buf[0]);
    }

    @Test
    public void testReadSingle() {
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[0]);
        assertEquals(END_OF_STREAM, is.read());

        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(0xa, is.read());
        assertEquals(0xb, is.read());
        assertEquals(0xc, is.read());
        assertEquals(END_OF_STREAM, is.read());
    }

    @Test
    public void testSkip() {
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(3, is.available());

        is.skip(1);
        assertEquals(2, is.available());
        assertEquals(0xb, is.read());

        is.skip(1);
        assertEquals(0, is.available());
        assertEquals(END_OF_STREAM, is.read());


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(3, is.available());
        is.skip(0);
        assertEquals(3, is.available());
        assertEquals(0xa, is.read());


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(3, is.available());
        is.skip(2);
        assertEquals(1, is.available());
        assertEquals(0xc, is.read());
        assertEquals(END_OF_STREAM, is.read());


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(3, is.available());
        is.skip(3);
        assertEquals(0, is.available());
        assertEquals(END_OF_STREAM, is.read());


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertEquals(3, is.available());
        is.skip(999);
        assertEquals(0, is.available());
        assertEquals(END_OF_STREAM, is.read());
    }
}
