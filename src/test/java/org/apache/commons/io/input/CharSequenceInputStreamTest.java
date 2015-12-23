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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private final Random random = new Random();

    private Set<String> getRequiredCharsetNames() {
        return Charsets.requiredCharsets().keySet();
    }

    private void testBufferedRead(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        final InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        try {
            final byte[] buffer = new byte[128];
            int offset = 0;
            while (true) {
                int bufferOffset = random.nextInt(64);
                final int bufferLength = random.nextInt(64);
                int read = in.read(buffer, bufferOffset, bufferLength);
                if (read == -1) {
                    assertEquals("EOF: offset should equal length for charset " + charsetName, expected.length, offset);
                    break;
                } else {
                    assertTrue("Read " + read + " <= " + bufferLength, read <= bufferLength);
                    while (read > 0) {
                        assertTrue("offset for " + charsetName +" " + offset + " < " + expected.length, offset < expected.length);
                        assertEquals("bytes should agree for " + charsetName, expected[offset], buffer[bufferOffset]);
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

//    Unfortunately checking canEncode does not seem to work for all charsets:
//    testBufferedRead_AvailableCharset(org.apache.commons.io.input.CharSequenceInputStreamTest)  Time elapsed: 0.682 sec  <<< ERROR!
//    java.lang.UnsupportedOperationException: null
//        at java.nio.CharBuffer.array(CharBuffer.java:940)
//        at sun.nio.cs.ext.COMPOUND_TEXT_Encoder.encodeLoop(COMPOUND_TEXT_Encoder.java:75)
//        at java.nio.charset.CharsetEncoder.encode(CharsetEncoder.java:544)
//        at org.apache.commons.io.input.CharSequenceInputStream.fillBuffer(CharSequenceInputStream.java:111)
    @Test
    public void testBufferedRead_AvailableCharset() throws IOException {
        for (final String csName : Charset.availableCharsets().keySet()) {
            // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
            if (isAvailabilityTestableForCharset(csName)) {
                testBufferedRead(TEST_STRING, csName);
            }
        }
    }

    @Test
    public void testBufferedRead_RequiredCharset() throws IOException {
        for (final String csName : getRequiredCharsetNames()) {
            testBufferedRead(TEST_STRING, csName);
        }
    }

    @Test
    public void testBufferedRead_UTF8() throws IOException {
        testBufferedRead(TEST_STRING, "UTF-8");
    }

    private void testCharsetMismatchInfiniteLoop(final String csName) throws IOException {
        // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
        final char[] inputChars = new char[] { (char) 0xE0, (char) 0xB2, (char) 0xA0 };
        final Charset charset = Charset.forName(csName); // infinite loop for US-ASCII, UTF-8 OK
        final InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512);
        try {
            while (stream.read() != -1) {
            }
        } finally {
            stream.close();
        }
    }

    @Test
    public void testCharsetMismatchInfiniteLoop_RequiredCharsets() throws IOException {
        for (final String csName : getRequiredCharsetNames()) {
            testCharsetMismatchInfiniteLoop(csName);
        }
    }

    // Test is broken if readFirst > 0
    // This is because the initial read fills the buffer from the CharSequence
    // so data1 gets the first buffer full; data2 will get the next buffer full
    private void testIO_356(final int bufferSize, final int dataSize, final int readFirst, final String csName) throws Exception {
        final CharSequenceInputStream is = new CharSequenceInputStream(ALPHABET, csName, bufferSize);

        for (int i = 0; i < readFirst; i++) {
            final int ch = is.read();
            assertFalse(ch == -1);
        }

        is.mark(dataSize);

        final byte[] data1 = new byte[dataSize];
        final int readCount1 = is.read(data1);
        assertEquals(dataSize, readCount1);

        is.reset(); // should allow data to be re-read

        final byte[] data2 = new byte[dataSize];
        final int readCount2 = is.read(data2);
        assertEquals(dataSize, readCount2);

        is.close();

        // data buffers should be identical
        assertArrayEquals("bufferSize=" + bufferSize + " dataSize=" + dataSize, data1, data2);
    }

    @Test
    public void testIO_356_B10_D10_S0_UTF16() throws Exception {
        testIO_356(10, 10, 0, "UTF-16");
    }

    @Test
    public void testIO_356_B10_D10_S0_UTF8() throws Exception {
        testIO_356(10, 10, 0, "UTF-8");
    }

    @Test
    public void testIO_356_B10_D10_S1_UTF8() throws Exception {
        testIO_356(10, 10, 1, "UTF-8");
    }

    @Test
    public void testIO_356_B10_D10_S2_UTF8() throws Exception {
        testIO_356(10, 10, 2, "UTF-8");
    }

    @Test
    public void testIO_356_B10_D13_S0_UTF8() throws Exception {
        testIO_356(10, 13, 0, "UTF-8");
    }

    @Test
    public void testIO_356_B10_D13_S1_UTF8() throws Exception {
        testIO_356(10, 13, 1, "UTF-8");
    }

    @Test
    public void testIO_356_B10_D20_S0_UTF8() throws Exception {
        testIO_356(10, 20, 0, "UTF-8");
    }

    private void testIO_356_Loop(final String csName, final int maxBytesPerChar) throws Exception {
        for (int bufferSize = maxBytesPerChar; bufferSize <= 10; bufferSize++) {
            for (int dataSize = 1; dataSize <= 20; dataSize++) {
                testIO_356(bufferSize, dataSize, 0, csName);
            }
        }
    }

    @Test
    public void testIO_356_Loop_UTF16() throws Exception {
        testIO_356_Loop("UTF-16", 4);
    }

    @Test
    public void testIO_356_Loop_UTF8() throws Exception {
        testIO_356_Loop("UTF-8", 4);
    }

    @Test
    public void testLargeBufferedRead_RequiredCharsets() throws IOException {
        for (final String csName : getRequiredCharsetNames()) {
            testBufferedRead(LARGE_TEST_STRING, csName);
        }
    }

    @Test
    public void testLargeBufferedRead_UTF8() throws IOException {
        testBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }

    @Test
    public void testLargeSingleByteRead_RequiredCharsets() throws IOException {
        for (final String csName : getRequiredCharsetNames()) {
            testSingleByteRead(LARGE_TEST_STRING, csName);
        }
    }

    @Test
    public void testLargeSingleByteRead_UTF8() throws IOException {
        testSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }

    // This test is broken for charsets that don't create a single byte for each char
    private void testMarkReset(final String csName) throws Exception {
        final InputStream r = new CharSequenceInputStream("test", csName);
        try {
            assertEquals(2, r.skip(2));
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
    @Ignore // Test broken for charsets that create multiple bytes for a single char
    public void testMarkReset_RequiredCharsets() throws Exception {
        for (final String csName : getRequiredCharsetNames()) {
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
        final InputStream r = new CharSequenceInputStream("test", "UTF-8");
        try {
            assertTrue(r.markSupported());
        } finally {
            r.close();
        }
    }

    private void testReadZero(final String csName) throws Exception {
        final InputStream r = new CharSequenceInputStream("test", csName);
        try {
            final byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        } finally {
            r.close();
        }
    }

    @Test
    public void testReadZero_EmptyString() throws Exception {
        final InputStream r = new CharSequenceInputStream("", "UTF-8");
        try {
            final byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        } finally {
            r.close();
        }
    }

    @Test
    public void testReadZero_RequiredCharsets() throws Exception {
        for (final String csName : getRequiredCharsetNames()) {
            testReadZero(csName);
        }
    }

    private void testSingleByteRead(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        final InputStream in = new CharSequenceInputStream(testString, charsetName, 512);
        try {
            for (final byte b : bytes) {
                final int read = in.read();
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
        for (final String csName : getRequiredCharsetNames()) {
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

    // This is broken for charsets that don't map each char to a byte
    private void testSkip(final String csName) throws Exception {
        final InputStream r = new CharSequenceInputStream("test", csName);
        try {
            assertEquals(1, r.skip(1));
            assertEquals(2, r.skip(2));
            assertEquals(csName, 't', r.read());
            r.skip(100);
            assertEquals(csName, -1, r.read());
        } finally {
            r.close();
        }
    }

    @Test
    @Ignore // test is broken for charsets that generate multiple bytes per char.
    public void testSkip_RequiredCharsets() throws Exception {
        for (final String csName : getRequiredCharsetNames()) {
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

    private int checkAvail(InputStream is, int min) throws Exception {
        int available = is.available();
        assertTrue("avail should be >= " + min + ", but was " + available, available >= min);
        return available;
    }

    private void testAvailableSkip(final String csName) throws Exception {
        final String input = "test";
        final InputStream r = new CharSequenceInputStream(input, csName);
        try {
            int available = checkAvail(r, input.length());
            assertEquals(available - 1, r.skip(available-1)); // skip all but one
            available = checkAvail(r, 1);
            assertEquals(1, r.skip(1));
            available = checkAvail(r, 0);
        } finally {
            r.close();
        }
    }

    private void testAvailableRead(final String csName) throws Exception {
        final String input = "test";
        final InputStream r = new CharSequenceInputStream(input, csName);
        try {
            int available = checkAvail(r, input.length());
            byte buff[] = new byte[available];
            assertEquals(available - 1, r.skip(available-1)); // skip all but one
            available = checkAvail(r, 1);
            buff = new byte[available];
            assertEquals(available, r.read(buff, 0, available));
        } finally {
            r.close();
        }
    }

    @Test
    public void testAvailable() throws Exception {
        for (final String csName : Charset.availableCharsets().keySet()) {
            // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
            // also try and avoid the following Effor on Continuum
//            java.lang.UnsupportedOperationException: null
//            at java.nio.CharBuffer.array(CharBuffer.java:940)
//            at sun.nio.cs.ext.COMPOUND_TEXT_Encoder.encodeLoop(COMPOUND_TEXT_Encoder.java:75)
//            at java.nio.charset.CharsetEncoder.encode(CharsetEncoder.java:544)
//            at org.apache.commons.io.input.CharSequenceInputStream.fillBuffer(CharSequenceInputStream.java:120)
//            at org.apache.commons.io.input.CharSequenceInputStream.read(CharSequenceInputStream.java:151)
//            at org.apache.commons.io.input.CharSequenceInputStreamTest.testAvailableRead(CharSequenceInputStreamTest.java:412)
//            at org.apache.commons.io.input.CharSequenceInputStreamTest.testAvailable(CharSequenceInputStreamTest.java:424)

            try {
                if (isAvailabilityTestableForCharset(csName)) {
                    testAvailableSkip(csName);
                    testAvailableRead(csName);
                }
            } catch (UnsupportedOperationException e){
                fail("Operation not supported for " + csName);
            }
        }
    }

    private boolean isAvailabilityTestableForCharset(final String csName) {
        return Charset.forName(csName).canEncode()
                && !"COMPOUND_TEXT".equalsIgnoreCase(csName) && !"x-COMPOUND_TEXT".equalsIgnoreCase(csName)
                && !isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(csName);
    }

    private boolean isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(String csName) {
        return "x-IBM1388".equalsIgnoreCase(csName) ||
                "ISO-2022-CN".equalsIgnoreCase(csName) ||
                "ISO-2022-JP".equalsIgnoreCase(csName) ||
                "Shift_JIS".equalsIgnoreCase(csName);
    }
}
