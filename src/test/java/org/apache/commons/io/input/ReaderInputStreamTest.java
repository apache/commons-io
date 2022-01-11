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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReaderInputStreamTest {
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;

    static {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    static Stream<Arguments> charsetData() {
        // @formatter:off
        return Stream.of(
                Arguments.of("Cp930", "\u0391"),
                Arguments.of("ISO_8859_1", "A"),
                Arguments.of("UTF-8", "\u0391"));
        // @formatter:on
    }

    private final Random random = new Random();

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testBufferSmallest() throws IOException {
        final Charset charset = StandardCharsets.UTF_8;
        try (InputStream in = new ReaderInputStream(new StringReader("\uD800"), charset, (int) ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            in.read();
        }
    }

    @Test
    public void testBufferTooSmall() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, -1));
        assertThrows(IllegalArgumentException.class, () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, 0));
        assertThrows(IllegalArgumentException.class, () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, 1));
    }

    @ParameterizedTest
    @MethodSource("charsetData")
    public void testCharsetEncoderFlush(final String charsetName, final String data) throws IOException {
        final Charset charset = Charset.forName(charsetName);
        final byte[] expected = data.getBytes(charset);
        try (InputStream in = new ReaderInputStream(new StringReader(data), charset)) {
            final byte[] actual = IOUtils.toByteArray(in);
            assertEquals(Arrays.toString(expected), Arrays.toString(actual));
        }
    }

    /*
     * Tests https://issues.apache.org/jira/browse/IO-277
     */
    @Test
    public void testCharsetMismatchInfiniteLoop() throws IOException {
        // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
        final char[] inputChars = {(char) 0xE0, (char) 0xB2, (char) 0xA0};
        // Charset charset = Charset.forName("UTF-8"); // works
        final Charset charset = StandardCharsets.US_ASCII; // infinite loop
        try (ReaderInputStream stream = new ReaderInputStream(new CharArrayReader(inputChars), charset)) {
            IOUtils.toCharArray(stream, charset);
        }
    }

    /**
     * Tests IO-717 to avoid infinite loops.
     *
     * ReaderInputStream does not throw exception with {@link CodingErrorAction#REPORT}.
     */
    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testCodingErrorAction() throws IOException {
        final Charset charset = StandardCharsets.UTF_8;
        final CharsetEncoder encoder = charset.newEncoder().onMalformedInput(CodingErrorAction.REPORT);
        try (InputStream in = new ReaderInputStream(new StringReader("\uD800aa"), encoder, (int) ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            assertThrows(CharacterCodingException.class, in::read);
        }
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testConstructNullCharset() throws IOException {
        final Charset charset = Charset.defaultCharset();
        final Charset encoder = null;
        try (ReaderInputStream in = new ReaderInputStream(new StringReader("ABC"), encoder, (int) ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testConstructNullCharsetEncoder() throws IOException {
        final Charset charset = Charset.defaultCharset();
        final CharsetEncoder encoder = null;
        try (ReaderInputStream in = new ReaderInputStream(new StringReader("ABC"), encoder, (int) ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testConstructNullCharsetNameEncoder() throws IOException {
        final Charset charset = Charset.defaultCharset();
        final String encoder = null;
        try (ReaderInputStream in = new ReaderInputStream(new StringReader("ABC"), encoder, (int) ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            IOUtils.toByteArray(in);
            assertEquals(Charset.defaultCharset(), in.getCharsetEncoder().charset());
        }
    }

    @Test
    public void testLargeUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(LARGE_TEST_STRING, "UTF-8");
    }

    @Test
    public void testLargeUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(LARGE_TEST_STRING, "UTF-8");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testReadZero() throws Exception {
        final String inStr = "test";
        try (final ReaderInputStream inputStream = new ReaderInputStream(new StringReader(inStr))) {
            final byte[] bytes = new byte[30];
            assertEquals(0, inputStream.read(bytes, 0, 0));
            assertEquals(inStr.length(), inputStream.read(bytes, 0, inStr.length() + 1));
            // Should always return 0 for length == 0
            assertEquals(0, inputStream.read(bytes, 0, 0));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testReadZeroEmptyString() throws Exception {
        try (final ReaderInputStream inputStream = new ReaderInputStream(new StringReader(""))) {
            final byte[] bytes = new byte[30];
            // Should always return 0 for length == 0
            assertEquals(0, inputStream.read(bytes, 0, 0));
            assertEquals(-1, inputStream.read(bytes, 0, 1));
            assertEquals(0, inputStream.read(bytes, 0, 0));
            assertEquals(-1, inputStream.read(bytes, 0, 1));
        }
    }

    @Test
    public void testUTF16WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-16");
    }

    @Test
    public void testUTF8WithBufferedRead() throws IOException {
        testWithBufferedRead(TEST_STRING, "UTF-8");
    }

    @Test
    public void testUTF8WithSingleByteRead() throws IOException {
        testWithSingleByteRead(TEST_STRING, "UTF-8");
    }

    private void testWithBufferedRead(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        try (final ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName)) {
            final byte[] buffer = new byte[128];
            int offset = 0;
            while (true) {
                int bufferOffset = random.nextInt(64);
                final int bufferLength = random.nextInt(64);
                int read = in.read(buffer, bufferOffset, bufferLength);
                if (read == -1) {
                    assertEquals(offset, expected.length);
                    break;
                }
                assertTrue(read <= bufferLength);
                while (read > 0) {
                    assertTrue(offset < expected.length);
                    assertEquals(expected[offset], buffer[bufferOffset]);
                    offset++;
                    bufferOffset++;
                    read--;
                }
            }
        }
    }

    private void testWithSingleByteRead(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(charsetName);
        try (final ReaderInputStream in = new ReaderInputStream(new StringReader(testString), charsetName)) {
            for (final byte b : bytes) {
                final int read = in.read();
                assertTrue(read >= 0);
                assertTrue(read <= 255);
                assertEquals(b, (byte) read);
            }
            assertEquals(-1, in.read());
        }
    }
}
