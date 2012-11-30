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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.junit.Ignore;
import org.junit.Test;

public class CharSequenceInputStreamTest {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LARGE_TEST_STRING;

    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";

    static {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private Random random = new Random();

    private Set<String> getRequiredCharsetNames() {
        return Charsets.requiredCharsets().keySet();
    }

    private void testBufferedRead(String testString, String charsetName) throws IOException {
        byte[] expected = testString.getBytes(charsetName);
        InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        try {
            byte[] buffer = new byte[128];
            int offset = 0;
            while (true) {
                int bufferOffset = random.nextInt(64);
                int bufferLength = random.nextInt(64);
                int read = in.read(buffer, bufferOffset, bufferLength);
                if (read == -1) {
                    assertEquals("EOF: offset should equal length", expected.length, offset);
                    break;
                } else {
                    assertTrue("Read " + read + " <= " + bufferLength, read <= bufferLength);
                    while (read > 0) {
                        assertTrue("offset " + offset + " < " + expected.length, offset < expected.length);
                        assertEquals("bytes should agree", expected[offset], buffer[bufferOffset]);
                        offset++;
                        bufferOffset++;
                        read--;
                    }
                }
            }
        } finally {
            in.close();
        }
    }

    /**
     * Fails with java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder(Unknown Source).
     * Probably not a good idea to use {@link Charset#availableCharsets()} because any encoding can fail for any
     * reason.
     */
    @Test
    @Ignore
    public void testBufferedRead_AvailableCharset() throws IOException {
        for (String csName : Charset.availableCharsets().keySet()) {
            testBufferedRead(TEST_STRING, csName);
        }
    }

    @Test
    public void testBufferedRead_RequiredCharset() throws IOException {
        for (String csName : getRequiredCharsetNames()) {
            testBufferedRead(TEST_STRING, csName);
        }
    }

    @Test
    public void testBufferedRead_UTF8() throws IOException {
        testBufferedRead(TEST_STRING, "UTF-8");
    }

    @Test
    public void testCharsetMismatchInfiniteLoop_RequiredCharsets() throws IOException {
        for (String csName : getRequiredCharsetNames()) {
            testCharsetMismatchInfiniteLoop(csName);
        }
    }

    private void testCharsetMismatchInfiniteLoop(String csName) throws IOException {
        // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
        char[] inputChars = new char[] { (char) 0xE0, (char) 0xB2, (char) 0xA0 };
        Charset charset = Charset.forName(csName); // infinite loop for US-ASCII, UTF-8 OK
        InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512);
        try {
            while (stream.read() != -1) {
            }
        } finally {
            stream.close();
        }
    }

    @Test
    public void testIO_356() throws Exception {
        testIO_356(10, 10); // OK
        testIO_356(10, 20); // OK
        testIO_356(10, 13); // fails
        for (int bs = 1; bs <= 10; bs++) {
            for (int ds = 1; ds <= 20; ds++) {
                testIO_356(bs, ds);
            }
        }
    }

    private void testIO_356(int bufferSize, int dataSize) throws Exception {
        CharSequenceInputStream is = new CharSequenceInputStream(ALPHABET, "UTF-8", bufferSize);

        is.mark(dataSize);

        byte[] data1 = new byte[dataSize];
        is.read(data1);

        is.reset(); // should allow data to be re-read

        byte[] data2 = new byte[dataSize];
        is.read(data2);

        is.close();
        assertArrayEquals("bufferSize=" + bufferSize + " dataSize=" + dataSize, data1, data2); // data buffers should
                                                                                               // be identical
    }

    @Test
    public void testLargeBufferedRead_RequiredCharsets() throws IOException {
        for (String csName : getRequiredCharsetNames()) {
            testBufferedRead(LARGE_TEST_STRING, csName);
        }
    }

    @Test
    public void testLargeBufferedRead_UTF8() throws IOException {
        testBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }

    @Test
    public void testLargeSingleByteRead_RequiredCharsets() throws IOException {
        for (String csName : getRequiredCharsetNames()) {
            testSingleByteRead(LARGE_TEST_STRING, csName);
        }
    }

    @Test
    public void testLargeSingleByteRead_UTF8() throws IOException {
        testSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }

    private void testMarkReset(String csName) throws Exception {
        InputStream r = new CharSequenceInputStream("test", csName);
        try {
            r.skip(2);
            r.mark(0);
            assertEquals(csName, 's', r.read());
            assertEquals(csName, 't', r.read());
            assertEquals(csName, -1, r.read());
            r.reset();
            assertEquals(csName, 's', r.read());
            assertEquals(csName, 't', r.read());
            assertEquals(csName, -1, r.read());
            r.reset();
            r.reset();
        } finally {
            r.close();
        }
    }

    @Test
    @Ignore
    public void testMarkReset_RequiredCharsets() throws Exception {
        for (String csName : getRequiredCharsetNames()) {
            testMarkReset(csName);
        }
    }

    @Test
    public void testMarkReset_USASCII() throws Exception {
        testMarkReset("US-ASCII");
    }

    @Test
    public void testMarkReset_UTF8() throws Exception {
        testMarkReset("UTF-8");
    }

    @Test
    public void testMarkSupported() throws Exception {
        InputStream r = new CharSequenceInputStream("test", "UTF-8");
        try {
            assertTrue(r.markSupported());
        } finally {
            r.close();
        }
    }

    public void testReadZero(String csName) throws Exception {
        InputStream r = new CharSequenceInputStream("test", csName);
        try {
            byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        } finally {
            r.close();
        }
    }

    @Test
    public void testReadZero_EmptyString() throws Exception {
        InputStream r = new CharSequenceInputStream("", "UTF-8");
        try {
            byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        } finally {
            r.close();
        }
    }

    @Test
    public void testReadZero_RequiredCharsets() throws Exception {
        for (String csName : getRequiredCharsetNames()) {
            testReadZero(csName);
        }
    }

    private void testSingleByteRead(String testString, String charsetName) throws IOException {
        byte[] bytes = testString.getBytes(charsetName);
        InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        try {
            for (byte b : bytes) {
                int read = in.read();
                assertTrue("read " + read + " >=0 ", read >= 0);
                assertTrue("read " + read + " <= 255", read <= 255);
                assertEquals("Should agree with input", b, (byte) read);
            }
            assertEquals(-1, in.read());
        } finally {
            in.close();
        }
    }

    @Test
    public void testSingleByteRead_RequiredCharsets() throws IOException {
        for (String csName : getRequiredCharsetNames()) {
            testSingleByteRead(TEST_STRING, csName);
        }
    }

    @Test
    public void testSingleByteRead_UTF16() throws IOException {
        testSingleByteRead(TEST_STRING, "UTF-16");
    }

    @Test
    public void testSingleByteRead_UTF8() throws IOException {
        testSingleByteRead(TEST_STRING, "UTF-8");
    }

    public void testSkip(String csName) throws Exception {
        InputStream r = new CharSequenceInputStream("test", csName);
        try {
            r.skip(1);
            r.skip(2);
            assertEquals(csName, 't', r.read());
            r.skip(100);
            assertEquals(csName, -1, r.read());
        } finally {
            r.close();
        }
    }

    @Test
    @Ignore
    public void testSkip_RequiredCharsets() throws Exception {
        for (String csName : getRequiredCharsetNames()) {
            testSkip(csName);
        }
    }

    @Test
    public void testSkip_USASCII() throws Exception {
        testSkip("US-ASCII");
    }

    @Test
    public void testSkip_UTF8() throws Exception {
        testSkip("UTF-8");
    }
}
