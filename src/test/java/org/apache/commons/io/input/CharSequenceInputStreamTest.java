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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.Random;

import org.apache.commons.io.CharsetsTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CharSequenceInputStreamTest {

    private static final String UTF_16 = StandardCharsets.UTF_16.name();
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
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

    private int checkAvail(final InputStream is, final int min) throws Exception {
        final int available = is.available();
        assertTrue(available >= min, "avail should be >= " + min + ", but was " + available);
        return available;
    }

    private boolean isAvailabilityTestableForCharset(final String csName) {
        return Charset.forName(csName).canEncode()
                && !"COMPOUND_TEXT".equalsIgnoreCase(csName) && !"x-COMPOUND_TEXT".equalsIgnoreCase(csName)
                && !isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(csName);
    }

    private boolean isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(final String csName) {
        return "x-IBM1388".equalsIgnoreCase(csName) ||
                "ISO-2022-CN".equalsIgnoreCase(csName) ||
                "ISO-2022-JP".equalsIgnoreCase(csName) ||
                "Shift_JIS".equalsIgnoreCase(csName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(CharsetsTest.AVAIL_CHARSETS)
    public void testAvailable(final String csName) throws Exception {
        // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
        // also try and avoid the following exception
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
        } catch (final UnsupportedOperationException e) {
            fail("Operation not supported for " + csName);
        }
    }

    @Test
    public void testAvailableAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = CharSequenceInputStream.builder().setCharSequence("Hi").get()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    /**
     * IO-781 available() returns 2 but only 1 byte is read afterwards.
     */
    @Test
    public void testAvailableAfterOpen() throws IOException {
        final Charset charset = Charset.forName("Big5");
        try (CharSequenceInputStream in = new CharSequenceInputStream("\uD800\uDC00", charset)) {
            final int available = in.available();
            final byte[] data = new byte[available];
            final int bytesRead = in.read(data);
            assertEquals(available, bytesRead);
        }
    }

    private void testAvailableRead(final String csName) throws Exception {
        final String input = "test";
        try (InputStream r = new CharSequenceInputStream(input, csName)) {
            int available = checkAvail(r, input.length());
            assertEquals(available - 1, r.skip(available - 1)); // skip all but one
            available = checkAvail(r, 1);
            final byte[] buff = new byte[available];
            assertEquals(available, r.read(buff, 0, available));
        }
    }

    private void testAvailableSkip(final String csName) throws Exception {
        final String input = "test";
        try (InputStream r = new CharSequenceInputStream(input, csName)) {
            int available = checkAvail(r, input.length());
            assertEquals(available - 1, r.skip(available - 1)); // skip all but one
            available = checkAvail(r, 1);
            assertEquals(1, r.skip(1));
            available = checkAvail(r, 0);
        }
    }

    private void testBufferedRead(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
            final byte[] buffer = new byte[128];
            int offset = 0;            while (true) {
                int bufferOffset = random.nextInt(64);
                final int bufferLength = random.nextInt(64);
                int read = in.read(buffer, bufferOffset, bufferLength);
                if (read == -1) {
                    assertEquals(expected.length, offset, "EOF: offset should equal length for charset " + charsetName);
                    break;
                }
                assertTrue(read <= bufferLength, "Read " + read + " <= " + bufferLength);
                while (read > 0) {
                    assertTrue(offset < expected.length,
                            "offset for " + charsetName + " " + offset + " < " + expected.length);
                    assertEquals(expected[offset], buffer[bufferOffset], "bytes should agree for " + charsetName);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
    }

    //    Unfortunately checking canEncode does not seem to work for all charsets:
//    testBufferedRead_AvailableCharset(org.apache.commons.io.input.CharSequenceInputStreamTest)  Time elapsed: 0.682 sec  <<< ERROR!
//    java.lang.UnsupportedOperationException: null
//        at java.nio.CharBuffer.array(CharBuffer.java:940)
//        at sun.nio.cs.ext.COMPOUND_TEXT_Encoder.encodeLoop(COMPOUND_TEXT_Encoder.java:75)
//        at java.nio.charset.CharsetEncoder.encode(CharsetEncoder.java:544)
//        at org.apache.commons.io.input.CharSequenceInputStream.fillBuffer(CharSequenceInputStream.java:111)
    @ParameterizedTest(name = "{0}")
    @MethodSource(CharsetsTest.AVAIL_CHARSETS)
    public void testBufferedRead_AvailableCharset(final String csName) throws IOException {
        // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
        if (isAvailabilityTestableForCharset(csName)) {
            testBufferedRead(TEST_STRING, csName);
        }
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testBufferedRead_RequiredCharset(final String csName) throws IOException {
        testBufferedRead(TEST_STRING, csName);
    }

    @Test
    public void testBufferedRead_UTF8() throws IOException {
        testBufferedRead(TEST_STRING, UTF_8);
    }

    @Test
    public void testCharacterCodingException() throws IOException {
        final Charset charset = StandardCharsets.US_ASCII;
        final CharSequenceInputStream in = CharSequenceInputStream.builder()
            .setCharsetEncoder(charset.newEncoder().onUnmappableCharacter(CodingErrorAction.REPORT))
            .setCharSequence("\u0080")
            .get();
        assertEquals(0, in.available());
        assertThrows(UnmappableCharacterException.class, in::read);
    }

    private void testCharsetMismatchInfiniteLoop(final String csName) throws IOException {
        // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
        final char[] inputChars = { (char) 0xE0, (char) 0xB2, (char) 0xA0 };
        final Charset charset = Charset.forName(csName); // infinite loop for US-ASCII, UTF-8 OK
        try (InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512)) {
            IOUtils.toCharArray(stream, charset);
        }
        try (InputStream stream = CharSequenceInputStream.builder().setCharSequence(new String(inputChars)).setCharset(charset).setBufferSize(512).get()) {
            IOUtils.toCharArray(stream, charset);
        }
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testCharsetMismatchInfiniteLoop_RequiredCharsets(final String csName) throws IOException {
        testCharsetMismatchInfiniteLoop(csName);
    }

    // Test is broken if readFirst > 0
    // This is because the initial read fills the buffer from the CharSequence
    // so data1 gets the first buffer full; data2 will get the next buffer full
    private void testIO_356(final int bufferSize, final int dataSize, final int readFirst, final String csName) throws Exception {
        final byte[] data1;
        final byte[] data2;
        try (CharSequenceInputStream is = new CharSequenceInputStream(ALPHABET, csName, bufferSize)) {
            for (int i = 0; i < readFirst; i++) {
                final int ch = is.read();
                assertNotEquals(-1, ch);
            }

            is.mark(dataSize);

            data1 = new byte[dataSize];
            final int readCount1 = is.read(data1);
            assertEquals(dataSize, readCount1);

            is.reset(); // should allow data to be re-read

            data2 = new byte[dataSize];
            final int readCount2 = is.read(data2);
            assertEquals(dataSize, readCount2);
        }

        // data buffers should be identical
        assertArrayEquals(data1, data2, "bufferSize=" + bufferSize + " dataSize=" + dataSize);
    }

    @Test
    public void testIO_356_B10_D10_S0_UTF16() throws Exception {
        testIO_356(10, 10, 0, UTF_16);
    }

    @Test
    public void testIO_356_B10_D10_S0_UTF8() throws Exception {
        testIO_356(10, 10, 0, UTF_8);
    }

    @Test
    public void testIO_356_B10_D10_S1_UTF8() throws Exception {
        testIO_356(10, 10, 1, UTF_8);
    }

    @Test
    public void testIO_356_B10_D10_S2_UTF8() throws Exception {
        testIO_356(10, 10, 2, UTF_8);
    }

    @Test
    public void testIO_356_B10_D13_S0_UTF8() throws Exception {
        testIO_356(10, 13, 0, UTF_8);
    }

    @Test
    public void testIO_356_B10_D13_S1_UTF8() throws Exception {
        testIO_356(10, 13, 1, UTF_8);
    }

    @Test
    public void testIO_356_B10_D20_S0_UTF8() throws Exception {
        testIO_356(10, 20, 0, UTF_8);
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
        final Charset charset = StandardCharsets.UTF_16;
        testIO_356_Loop(charset.displayName(), (int) ReaderInputStream.minBufferSize(charset.newEncoder()));
    }

    @Test
    public void testIO_356_Loop_UTF8() throws Exception {
        final Charset charset = StandardCharsets.UTF_8;
        testIO_356_Loop(charset.displayName(), (int) ReaderInputStream.minBufferSize(charset.newEncoder()));
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testLargeBufferedRead_RequiredCharsets(final String csName) throws IOException {
        testBufferedRead(LARGE_TEST_STRING, csName);
    }

    @Test
    public void testLargeBufferedRead_UTF8() throws IOException {
        testBufferedRead(LARGE_TEST_STRING, UTF_8);
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testLargeSingleByteRead_RequiredCharsets(final String csName) throws IOException {
        testSingleByteRead(LARGE_TEST_STRING, csName);
    }

    @Test
    public void testLargeSingleByteRead_UTF8() throws IOException {
        testSingleByteRead(LARGE_TEST_STRING, UTF_8);
    }

    // This test doesn't work for charsets that don't create a single byte for each char.
    // Use testMarkResetMultiByteChars() instead for those cases.
    private void testMarkReset(final String csName) throws Exception {
        try (InputStream r = new CharSequenceInputStream("test", csName)) {
            assertEquals(2, r.skip(2));
            r.mark(0);
            assertEquals('s', r.read(), csName);
            assertEquals('t', r.read(), csName);
            assertEquals(-1, r.read(), csName);
            r.reset();
            assertEquals('s', r.read(), csName);
            assertEquals('t', r.read(), csName);
            assertEquals(-1, r.read(), csName);
            r.reset();
            r.reset();
        }
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testMarkReset_RequiredCharsets(final String csName) throws Exception {
        testMarkResetMultiByteChars(csName);
    }

    @Test
    public void testMarkReset_USASCII() throws Exception {
        testMarkReset(StandardCharsets.US_ASCII.name());
    }

    @Test
    public void testMarkReset_UTF8() throws Exception {
        testMarkReset(UTF_8);
    }

    private void testMarkResetMultiByteChars(final String csName) throws IOException {
        // This test quietly skips Charsets that can't handle multibyte characters like ASCII.
        final String sequenceEnglish = "Test Sequence";
        final String sequenceCJK = "\u4e01\u4f23\u5045\u5167\u5289\u53ab"; // Kanji text
        final String[] sequences = {sequenceEnglish, sequenceCJK};
        for (final String testSequence : sequences) {
            final CharsetEncoder charsetEncoder = Charset.forName(csName).newEncoder();
            final ByteBuffer byteBuffer = ByteBuffer.allocate(testSequence.length() * 3);
            final CharBuffer charBuffer = CharBuffer.wrap(testSequence);
            final CoderResult result = charsetEncoder.encode(charBuffer, byteBuffer, true);
            if (result.isUnmappable()) {
                continue; // Skip character sets that can't handle multibyte characters.
            }
            final byte[] expectedBytes = byteBuffer.array();

            final int bLength = byteBuffer.position();
            final int skip = bLength - 4;
            try (InputStream r = new CharSequenceInputStream(testSequence, csName)) {
                assertEquals(skip, r.skip(skip));
                r.mark(0);
                assertEquals(expectedBytes[bLength - 4], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 3], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 2], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 1], (byte) r.read(), csName);
                assertEquals(-1, (byte) r.read(), csName);
                r.reset();
                assertEquals(expectedBytes[bLength - 4], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 3], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 2], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 1], (byte) r.read(), csName);
                assertEquals(-1, (byte) r.read(), csName);
                r.reset();
                assertEquals(expectedBytes[bLength - 4], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 3], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 2], (byte) r.read(), csName);
                assertEquals(expectedBytes[bLength - 1], (byte) r.read(), csName);
                assertEquals(-1, (byte) r.read(), csName);
            }
        }
    }

    @Test
    public void testMarkSupported() throws Exception {
        try (@SuppressWarnings("deprecation")
        InputStream r = new CharSequenceInputStream("test", UTF_8)) {
            assertTrue(r.markSupported());
        }
        try (InputStream r = CharSequenceInputStream.builder().setCharSequence("test").setCharset(UTF_8).get()) {
            assertTrue(r.markSupported());
        }
    }

    @Test
    public void testNullCharset() throws IOException {
        try (CharSequenceInputStream in = new CharSequenceInputStream("A", (Charset) null)) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
        try (CharSequenceInputStream in = CharSequenceInputStream.builder().setCharSequence("test").setCharset((Charset) null).get()) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
    }

    @Test
    public void testNullCharsetName() throws IOException {
        try (CharSequenceInputStream in = new CharSequenceInputStream("A", (String) null)) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
        try (CharSequenceInputStream in = CharSequenceInputStream.builder().setCharSequence("test").setCharset((String) null).get()) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
    }

    @Test
    public void testReadAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = CharSequenceInputStream.builder().setCharSequence("Hi").get()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(IOUtils.EOF, shadow.read());
    }

    private void testReadZero(final String csName) throws Exception {
        try (InputStream r = new CharSequenceInputStream("test", csName)) {
            final byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        }
    }

    @Test
    public void testReadZero_EmptyString() throws Exception {
        try (InputStream r = new CharSequenceInputStream("", UTF_8)) {
            final byte[] bytes = new byte[30];
            assertEquals(0, r.read(bytes, 0, 0));
        }
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testReadZero_RequiredCharsets(final String csName) throws Exception {
        testReadZero(csName);
    }

    private void testResetBeforeEnd(final CharSequenceInputStream inputStream) throws IOException {
        inputStream.mark(1);
        assertEquals('1', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
        assertEquals('2', inputStream.read());
        assertEquals('3', inputStream.read());
        assertEquals('4', inputStream.read());
        inputStream.reset();
        assertEquals('1', inputStream.read());
    }

    @Test
    public void testResetBeforeEndSetCharSequence() throws IOException {
        try (CharSequenceInputStream inputStream = CharSequenceInputStream.builder().setCharSequence("1234").get()) {
            testResetBeforeEnd(inputStream);
        }
    }

    @Test
    public void testResetCharset() {
        assertNotNull(CharSequenceInputStream.builder().setReader(new StringReader("\uD800")).setCharset((Charset) null).getCharset());
    }

    @Test
    public void testResetCharsetEncoder() {
        assertNotNull(CharSequenceInputStream.builder().setReader(new StringReader("\uD800")).setCharsetEncoder(null).getCharsetEncoder());
    }

    @Test
    public void testResetCharsetName() {
        assertNotNull(CharSequenceInputStream.builder().setReader(new StringReader("\uD800")).setCharset((String) null).getCharset());
    }

    private void testSingleByteRead(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
            for (final byte b : bytes) {
                final int read = in.read();
                assertTrue(read >= 0, "read " + read + " >=0 ");
                assertTrue(read <= 255, "read " + read + " <= 255");
                assertEquals(b, (byte) read, "Should agree with input");
            }
            assertEquals(-1, in.read());
        }
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testSingleByteRead_RequiredCharsets(final String csName) throws IOException {
        testSingleByteRead(TEST_STRING, csName);
    }

    @Test
    public void testSingleByteRead_UTF16() throws IOException {
        testSingleByteRead(TEST_STRING, UTF_16);
    }

    @Test
    public void testSingleByteRead_UTF8() throws IOException {
        testSingleByteRead(TEST_STRING, UTF_8);
    }

    @ParameterizedTest
    @MethodSource(CharsetsTest.REQUIRED_CHARSETS)
    public void testSkip_RequiredCharsets(final String csName) throws Exception {
        try (InputStream r = new CharSequenceInputStream("test", csName)) {
            assertEquals(1, r.skip(1));
            assertEquals(2, r.skip(2));
            r.skip(100);
            assertEquals(-1, r.read(), csName);
        }
    }
}
