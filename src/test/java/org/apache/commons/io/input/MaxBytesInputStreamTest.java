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
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Tests the MaxBytesInputStream.
 *
 * @version $Id$
 */
public class MaxBytesInputStreamTest extends TestCase {

    public MaxBytesInputStreamTest(final String name) {
        super(name);
    }

    public void testCounting() throws Exception {
        final String text = "A piece of text";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, text.length());

        // have to declare this larger as we're going to read
        // off the end of the stream and input stream seems
        // to do bounds checking
        final byte[] result = new byte[21];

        final byte[] ba = new byte[5];
        int found = cis.read(ba);
        System.arraycopy(ba, 0, result, 0, 5);
        assertEquals( found, cis.getCount() );

        final int value = cis.read();
        found++;
        result[5] = (byte)value;
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 6, 5);
        assertEquals( found, cis.getCount() );

        found += cis.read(result, 11, 10); // off the end
        assertEquals( found, cis.getCount() );

        // trim to get rid of the 6 empty values
        final String textResult = new String(result).trim();
        assertEquals(textResult, text);
        cis.close();
    }


    /**
     * Test for files > 2GB in size - see issue IO-84
     */
    public void testLargeFiles_IO84() throws Exception {
        final long size = (long)Integer.MAX_VALUE + (long)1;
        final NullInputStream mock    = new NullInputStream(size);
        final MaxBytesInputStream cis = new MaxBytesInputStream(mock, size);
        final OutputStream out        = new NullOutputStream();

        // Test integer methods
        IOUtils.copyLarge(cis, out);
        try {
            cis.getCount();
            fail("Expected getCount() to throw an ArithmeticException");
        } catch (final ArithmeticException ae) {
            // expected result
        }
        try {
            cis.resetCount();
            fail("Expected resetCount() to throw an ArithmeticException");
        } catch (final ArithmeticException ae) {
            // expected result
        }

        mock.close();

        // Test long methods
        IOUtils.copyLarge(cis, out);
        assertEquals("getByteCount()",   size, cis.getByteCount());
        assertEquals("resetByteCount()", size, cis.resetByteCount());
    }

    public void testResetting() throws Exception {
        final String text = "A piece of text";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, bytes.length);

        final byte[] result = new byte[bytes.length];

        int found = cis.read(result, 0, 5);
        assertEquals( found, cis.getCount() );

        final int count = cis.resetCount();
        found = cis.read(result, 6, 5);
        assertEquals( found, count );
        cis.close();
    }

    public void testZeroLength1() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 0);

        final int found = cis.read();
        assertEquals(-1, found);
        assertEquals(0, cis.getCount());
        cis.close();
    }

    public void testZeroLength2() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 0);

        final byte[] result = new byte[10];

        final int found = cis.read(result);
        assertEquals(-1, found);
        assertEquals(0, cis.getCount());
        cis.close();
    }

    public void testZeroLength3() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 0);

        final byte[] result = new byte[10];

        final int found = cis.read(result, 0, 5);
        assertEquals(-1, found);
        assertEquals(0, cis.getCount());
        cis.close();
    }

    public void testEOF1() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 2);

        int found = cis.read();
        assertEquals(0, found);
        assertEquals(1, cis.getCount());
        found = cis.read();
        assertEquals(0, found);
        assertEquals(2, cis.getCount());
        found = cis.read();
        assertEquals(-1, found);
        assertEquals(2, cis.getCount());
        cis.close();
    }

    public void testEOF2() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 2);

        final byte[] result = new byte[10];

        final int found = cis.read(result);
        assertEquals(2, found);
        assertEquals(2, cis.getCount());
        cis.close();
    }

    public void testEOF3() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 2);

        final byte[] result = new byte[10];

        final int found = cis.read(result, 0, 5);
        assertEquals(2, found);
        assertEquals(2, cis.getCount());
        cis.close();
    }

    public void testMaxZero() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 0);

        final byte[] result = new byte[10];

        final int found = cis.read(result, 0, 5);
        assertEquals(-1, found);
        assertEquals(0, cis.getCount());
        cis.close();
    }

    public void testMax1() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[20]);
        final int max = 4;
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, max);

        final byte[] result = new byte[10];

        final int found = cis.read(result, 0, 5);
        assertEquals(max, found);
        assertEquals(max, cis.getCount());
        cis.close();
    }

    public void testMax1b() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[20]);
        final int max = 4;
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, max);

        final byte[] result = new byte[10];

        final int found = cis.read(result);
        assertEquals(max, found);
        assertEquals(max, cis.getCount());
        cis.close();
    }

    public void testSkipping() throws IOException {
        final String text = "Hello World!";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final MaxBytesInputStream cis = new MaxBytesInputStream(bais, 100);

        assertEquals(6,cis.skip(6));
        assertEquals(6,cis.getCount());
        final byte[] result = new byte[6];
        cis.read(result);

        assertEquals("World!",new String(result));
        assertEquals(12,cis.getCount());
        cis.close();
    }

}
