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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WindowsLineEndingInputStreamTest {

    private String roundtripReadByte(final String msg) throws IOException {
        return roundtripReadByte(msg, true);
    }

    private String roundtripReadByte(final String msg, final boolean ensure) throws IOException {
        // read(byte[])
        try (WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream(
                CharSequenceInputStream.builder().setCharSequence(msg).setCharset(StandardCharsets.UTF_8).get(), ensure)) {
            final byte[] buf = new byte[100];
            int i = 0;
            while (i < buf.length) {
                final int read = lf.read();
                if (read < 0) {
                    break;
                }
                buf[i++] = (byte) read;
            }
            return new String(buf, 0, i, StandardCharsets.UTF_8);
        }
    }

    private String roundtripReadByteArray(final String msg) throws IOException {
        return roundtripReadByteArray(msg, true);
    }

    private String roundtripReadByteArray(final String msg, final boolean ensure) throws IOException {
        // read(byte[])
        try (WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream(
                CharSequenceInputStream.builder().setCharSequence(msg).setCharset(StandardCharsets.UTF_8).get(), ensure)) {
            final byte[] buf = new byte[100];
            final int read = lf.read(buf);
            return new String(buf, 0, read, StandardCharsets.UTF_8);
        }
    }

    private String roundtripReadByteArrayIndex(final String msg) throws IOException {
        return roundtripReadByteArrayIndex(msg, true);
    }

    private String roundtripReadByteArrayIndex(final String msg, final boolean ensure) throws IOException {
        // read(byte[])
        try (WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream(
                CharSequenceInputStream.builder().setCharSequence(msg).setCharset(StandardCharsets.UTF_8).get(), ensure)) {
            final byte[] buf = new byte[100];
            final int read = lf.read(buf, 0, 100);
            return new String(buf, 0, read, StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testInTheMiddleOfTheLine_Byte() throws Exception {
        assertEquals("a\r\nbc\r\n", roundtripReadByte("a\r\nbc"));
    }

    @Test
    public void testInTheMiddleOfTheLine_ByteArray() throws Exception {
        assertEquals("a\r\nbc\r\n", roundtripReadByteArray("a\r\nbc"));
    }

    @Test
    public void testInTheMiddleOfTheLine_ByteArrayIndex() throws Exception {
        assertEquals("a\r\nbc\r\n", roundtripReadByteArrayIndex("a\r\nbc"));
    }

    @Test
    public void testLinuxLineFeeds_Byte() throws Exception {
        assertEquals("ab\r\nc", roundtripReadByte("ab\nc", false));
    }

    @Test
    public void testLinuxLineFeeds_ByteArray() throws Exception {
        assertEquals("ab\r\nc", roundtripReadByteArray("ab\nc", false));
    }

    @Test
    public void testLinuxLineFeeds_ByteArrayIndex() throws Exception {
        assertEquals("ab\r\nc", roundtripReadByteArrayIndex("ab\nc", false));
    }

    @Test
    public void testMalformed_Byte() throws Exception {
        assertEquals("a\rbc", roundtripReadByte("a\rbc", false));
    }

    @Test
    public void testMalformed_ByteArray() throws Exception {
        assertEquals("a\rbc", roundtripReadByteArray("a\rbc", false));
    }

    @Test
    public void testMalformed_ByteArrayIndex() throws Exception {
        assertEquals("a\rbc", roundtripReadByteArrayIndex("a\rbc", false));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testMark(final boolean ensureLineFeedAtEndOfFile) {
        assertThrows(UnsupportedOperationException.class, () -> new WindowsLineEndingInputStream(new NullInputStream(), true).mark(1));
    }

    @SuppressWarnings("resource")
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testMarkSupported(final boolean ensureLineFeedAtEndOfFile) {
        assertFalse(new WindowsLineEndingInputStream(new NullInputStream(), true).markSupported());
    }

    @Test
    public void testMultipleBlankLines_Byte() throws Exception {
        assertEquals("a\r\n\r\nbc\r\n", roundtripReadByte("a\r\n\r\nbc"));
    }

    @Test
    public void testMultipleBlankLines_ByteArray() throws Exception {
        assertEquals("a\r\n\r\nbc\r\n", roundtripReadByteArray("a\r\n\r\nbc"));
    }

    @Test
    public void testMultipleBlankLines_ByteArrayIndex() throws Exception {
        assertEquals("a\r\n\r\nbc\r\n", roundtripReadByteArrayIndex("a\r\n\r\nbc"));
    }

    @Test
    public void testRetainLineFeed_Byte() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByte("a\r\n\r\n", false));
        assertEquals("a", roundtripReadByte("a", false));
    }

    @Test
    public void testRetainLineFeed_ByteArray() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByteArray("a\r\n\r\n", false));
        assertEquals("a", roundtripReadByteArray("a", false));
    }

    @Test
    public void testRetainLineFeed_ByteArrayIndex() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByteArray("a\r\n\r\n", false));
        assertEquals("a", roundtripReadByteArrayIndex("a", false));
    }

    @Test
    public void testSimpleString_Byte() throws Exception {
        assertEquals("abc\r\n", roundtripReadByte("abc"));
    }

    @Test
    public void testSimpleString_ByteArray() throws Exception {
        assertEquals("abc\r\n", roundtripReadByteArray("abc"));
    }

    @Test
    public void testSimpleString_ByteArrayIndex() throws Exception {
        assertEquals("abc\r\n", roundtripReadByteArrayIndex("abc"));
    }

    @Test
    public void testTwoLinesAtEnd_Byte() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByte("a\r\n\r\n"));
    }

    @Test
    public void testTwoLinesAtEnd_ByteArray() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByteArray("a\r\n\r\n"));
    }

    @Test
    public void testTwoLinesAtEnd_ByteArrayIndex() throws Exception {
        assertEquals("a\r\n\r\n", roundtripReadByteArrayIndex("a\r\n\r\n"));
    }
}
