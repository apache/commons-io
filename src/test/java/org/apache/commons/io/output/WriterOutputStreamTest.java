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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.charset.CharsetDecoders;
import org.apache.commons.lang3.SystemProperties;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link WriterOutputStream}.
 */
public class WriterOutputStreamTest {

    private static final String UTF_16LE = StandardCharsets.UTF_16LE.name();
    private static final String UTF_16BE = StandardCharsets.UTF_16BE.name();
    private static final String UTF_16 = StandardCharsets.UTF_16.name();
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";
    private static final String LARGE_TEST_STRING;

    static {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            buffer.append(TEST_STRING);
        }
        LARGE_TEST_STRING = buffer.toString();
    }

    private final Random random = new Random();

    @Test
    public void testFlush() throws IOException {
        final StringWriter writer = new StringWriter();
        try (WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, false)) {
            out.write("abc".getBytes(StandardCharsets.US_ASCII));
            assertEquals(0, writer.getBuffer().length());
            out.flush();
            assertEquals("abc", writer.toString());
        }
    }

    @Test
    public void testLargeUTF8CharsetWithBufferedWrite() throws IOException {
        testWithBufferedWrite(LARGE_TEST_STRING, UTF_8);
    }

    @Test
    public void testLargeUTF8CharsetWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(LARGE_TEST_STRING, StandardCharsets.UTF_8);
    }

    @Test
    public void testLargeUTF8WithBufferedWrite() throws IOException {
        testWithBufferedWrite(LARGE_TEST_STRING, UTF_8);
    }

    @Test
    public void testLargeUTF8WithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(LARGE_TEST_STRING, UTF_8);
    }

    @Test
    public void testNullCharsetDecoderWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, (CharsetDecoder) null);
    }

    @Test
    public void testNullCharsetNameWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, (String) null);
    }

    @Test
    public void testNullCharsetWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, (Charset) null);
    }

    @Test
    public void testUTF16BEWithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, UTF_16BE);
    }

    @Test
    public void testUTF16BEWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, UTF_16BE);
    }

    @Test
    public void testUTF16LEWithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, UTF_16LE);
    }

    @Test
    public void testUTF16LEWithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, UTF_16LE);
    }

    @Test
    public void testUTF16WithBufferedWrite() throws IOException {
        try {
            testWithBufferedWrite(TEST_STRING, UTF_16);
        } catch (final UnsupportedOperationException e) {
            if (!SystemProperties.getJavaVendor().contains("IBM")) {
                fail("This test should only throw UOE on IBM JDKs with broken UTF-16");
            }
        }
    }

    @Test
    public void testUTF16WithSingleByteWrite() throws IOException {
        try {
            testWithSingleByteWrite(TEST_STRING, UTF_16);
        } catch (final UnsupportedOperationException e) {
            if (!SystemProperties.getJavaVendor().contains("IBM")) {
                fail("This test should only throw UOE on IBM JDKs with broken UTF-16");
            }
        }
    }

    @Test
    public void testUTF8WithBufferedWrite() throws IOException {
        testWithBufferedWrite(TEST_STRING, UTF_8);
    }

    @Test
    public void testUTF8WithSingleByteWrite() throws IOException {
        testWithSingleByteWrite(TEST_STRING, UTF_8);
    }

    private void testWithBufferedWrite(final String testString, final String charsetName) throws IOException {
        final byte[] expected = testString.getBytes(charsetName);
        final StringWriter writer = new StringWriter();
        try (WriterOutputStream out = WriterOutputStream.builder().setWriter(writer).setCharset(charsetName).get()) {
            int offset = 0;
            while (offset < expected.length) {
                final int length = Math.min(random.nextInt(128), expected.length - offset);
                out.write(expected, offset, length);
                offset += length;
            }
        }
        assertEquals(testString, writer.toString());
    }

    private void testWithSingleByteWrite(final String testString, final Charset charset) throws IOException {
        final byte[] bytes = testString.getBytes(Charsets.toCharset(charset));
        StringWriter writer = new StringWriter();
        try (WriterOutputStream out = new WriterOutputStream(writer, charset)) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
        //
        writer = new StringWriter();
        try (WriterOutputStream out = WriterOutputStream.builder().setWriter(writer).setCharset(charset).get()) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
    }

    private void testWithSingleByteWrite(final String testString, final CharsetDecoder charsetDecoder) throws IOException {
        final byte[] bytes = testString.getBytes(CharsetDecoders.toCharsetDecoder(charsetDecoder).charset());
        StringWriter writer = new StringWriter();
        try (WriterOutputStream out = new WriterOutputStream(writer, charsetDecoder)) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
        //
        writer = new StringWriter();
        try (WriterOutputStream out = WriterOutputStream.builder().setWriter(writer).setCharsetDecoder(charsetDecoder).get()) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
    }

    private void testWithSingleByteWrite(final String testString, final String charsetName) throws IOException {
        final byte[] bytes = testString.getBytes(Charsets.toCharset(charsetName));
        StringWriter writer = new StringWriter();
        try (WriterOutputStream out = new WriterOutputStream(writer, charsetName)) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
        //
        writer = new StringWriter();
        try (WriterOutputStream out = WriterOutputStream.builder().setWriter(writer).setCharset(charsetName).get()) {
            writeOneAtATime(bytes, out);
        }
        assertEquals(testString, writer.toString());
    }

    @Test
    public void testWriteImmediately() throws IOException {
        final StringWriter writer = new StringWriter();
        try (WriterOutputStream out = new WriterOutputStream(writer, "us-ascii", 1024, true)) {
            out.write("abc".getBytes(StandardCharsets.US_ASCII));
            assertEquals("abc", writer.toString());
        }
        // @formatter:off
        try (WriterOutputStream out = WriterOutputStream.builder()
                .setWriter(writer)
                .setCharset("us-ascii")
                .setBufferSize(1024)
                .setWriteImmediately(true)
                .setOpenOptions(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)
                .get()) {
            // @formatter:on
            out.write("abc".getBytes(StandardCharsets.US_ASCII));
            assertEquals("abcabc", writer.toString());
        }
    }

    private void writeOneAtATime(final byte[] bytes, final WriterOutputStream out) throws IOException {
        for (final byte b : bytes) {
            out.write(b);
        }
    }
}
