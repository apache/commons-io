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
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

import junit.framework.TestCase;

public class WriterOutputStreamTest extends TestCase {
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;

    static {
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private final Random random = new Random();

    private void testWithSingleByteWrite(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        final StringWriter writer = new StringWriter();
        final WriterOutputStream out = new WriterOutputStream(writer, charsetName);
        for (final byte b : bytes) {
            out.write(b);
        }
        out.close();
        assertEquals(testString, writer.toString());
    }

    private void testWithBufferedWrite(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        final StringWriter writer = new StringWriter();
        final WriterOutputStream out = new WriterOutputStream(writer, charsetName);
        int offset = 0;
        while (offset < expected.length) {
            final int length = Math.min(random.nextInt(128), expected.length-offset);
            out.write(expected, offset, length);
            offset += length;
        }
        out.close();
        assertEquals(testString, writer.toString());
    }

    public void testUTF8WithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, "UTF-8");
    }

    public void testLargeUTF8WithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(LARGE_TEST_STRING, "UTF-8");
    }

    public void testUTF8WithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, "UTF-8");
    }

    public void testLargeUTF8WithBufferedWrite() throws IOException {
        testWithBufferedWrite(LARGE_TEST_STRING, "UTF-8");
    }

    public void testUTF16WithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, "UTF-16");
    }

    public void testUTF16WithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, "UTF-16");
    }

    public void testUTF16BEWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, "UTF-16BE");
    }

    public void testUTF16BEWithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, "UTF-16BE");
    }

    public void testUTF16LEWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, "UTF-16LE");
    }

    public void testUTF16LEWithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, "UTF-16LE");
    }


    public void testFlush() throws IOException {
        final StringWriter writer = new StringWriter();
        final WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, false);
        out.write("abc".getBytes("us-ascii"));
        assertEquals(0, writer.getBuffer().length());
        out.flush();
        assertEquals("abc", writer.toString());
        out.close();
    }

    public void testWriteImmediately() throws IOException {
        final StringWriter writer = new StringWriter();
        final WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, true);
        out.write("abc".getBytes("us-ascii"));
        assertEquals("abc", writer.toString());
        out.close();
    }
}
