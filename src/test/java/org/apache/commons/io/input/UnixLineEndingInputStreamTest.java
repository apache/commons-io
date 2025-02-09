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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class UnixLineEndingInputStreamTest {

    private String roundtrip(final String msg) throws IOException {
        return roundtrip(msg, true, 0);
    }

    private String roundtrip(final String msg, final boolean ensureLineFeedAtEndOfFile, final int minBufferLen) throws IOException {
        final String string;
        // read(byte[])
        try (ByteArrayInputStream baos = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
                UnixLineEndingInputStream in = new UnixLineEndingInputStream(baos, ensureLineFeedAtEndOfFile)) {
            // read into a buffer larger than the fixture.
            final byte[] buf = new byte[minBufferLen + msg.length() * 10];
            string = new String(buf, 0, in.read(buf), StandardCharsets.UTF_8);
        }
        // read(byte[], int, int)
        try (ByteArrayInputStream baos = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
                UnixLineEndingInputStream in = new UnixLineEndingInputStream(baos, ensureLineFeedAtEndOfFile)) {
            // read into a buffer larger than the fixture.
            final byte[] buf = new byte[minBufferLen + msg.length() * 10];
            assertEquals(string, new String(buf, 0, in.read(buf, 0, buf.length), StandardCharsets.UTF_8));
        }
        // read
        try (ByteArrayInputStream baos = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
                UnixLineEndingInputStream in = new UnixLineEndingInputStream(baos, ensureLineFeedAtEndOfFile)) {
            // read into a buffer larger than the fixture.
            final int[] buf = new int[minBufferLen + msg.length() * 10];
            if (buf.length > 0) {
                int b;
                int i = 0;
                while ((b = in.read()) != -1) {
                    buf[i++] = b;
                }
                assertEquals(string, new String(buf, 0, i));
            }
        }
        return string;
    }

    @Test
    public void testCrAtEnd() throws Exception {
        assertEquals("a\n", roundtrip("a\r"));
    }

    @Test
    public void testCrOnlyEnsureAtEof() throws Exception {
        assertEquals("a\nb\n", roundtrip("a\rb"));
    }

    @Test
    public void testCrOnlyNotAtEof() throws Exception {
        assertEquals("a\nb", roundtrip("a\rb", false, 0));
    }

    @Test
    public void testEmpty() throws Exception {
        assertEquals("", roundtrip(""));
    }

    @Test
    public void testInTheMiddleOfTheLine() throws Exception {
        assertEquals("a\nbc\n", roundtrip("a\r\nbc"));
    }

    @Test
    public void testMultipleBlankLines() throws Exception {
        assertEquals("a\n\nbc\n", roundtrip("a\r\n\r\nbc"));
    }

    @Test
    public void testRetainLineFeed() throws Exception {
        assertEquals("a\n\n", roundtrip("a\r\n\r\n", false, 0));
        assertEquals("a", roundtrip("a", false, 0));
    }

    @Test
    public void testSimpleString() throws Exception {
        assertEquals("abc\n", roundtrip("abc"));
    }

    @Test
    public void testTwoLinesAtEnd() throws Exception {
        assertEquals("a\n\n", roundtrip("a\r\n\r\n"));
    }

}
