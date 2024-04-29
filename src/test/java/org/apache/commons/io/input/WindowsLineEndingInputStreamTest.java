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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WindowsLineEndingInputStreamTest {
    private String roundtrip(final String msg) throws IOException {
        return roundtrip(msg, true);
    }

    private String roundtrip(final String msg, final boolean ensure) throws IOException {
        try (WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream(
                CharSequenceInputStream.builder().setCharSequence(msg).setCharset(StandardCharsets.UTF_8).get(), ensure)) {
            final byte[] buf = new byte[100];
            final int read = lf.read(buf);
            return new String(buf, 0, read, StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testInTheMiddleOfTheLine() throws Exception {
        assertEquals("a\r\nbc\r\n", roundtrip("a\r\nbc"));
    }

    @Test
    public void testLinuxLineFeeds() throws Exception {
        final String roundtrip = roundtrip("ab\nc", false);
        assertEquals("ab\r\nc", roundtrip);
    }

    @Test
    public void testMalformed() throws Exception {
        assertEquals("a\rbc", roundtrip("a\rbc", false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMark(final boolean ensureLineFeedAtEndOfFile) {
        assertThrows(UnsupportedOperationException.class, () -> new WindowsLineEndingInputStream(NullInputStream.INSTANCE, true).mark(1));
    }

    @Test
    public void testMultipleBlankLines() throws Exception {
        assertEquals("a\r\n\r\nbc\r\n", roundtrip("a\r\n\r\nbc"));
    }

    @Test
    public void testRetainLineFeed() throws Exception {
        assertEquals("a\r\n\r\n", roundtrip("a\r\n\r\n", false));
        assertEquals("a", roundtrip("a", false));
    }

    @Test
    public void testSimpleString() throws Exception {
        assertEquals("abc\r\n", roundtrip("abc"));
    }

    @Test
    public void testTwoLinesAtEnd() throws Exception {
        assertEquals("a\r\n\r\n", roundtrip("a\r\n\r\n"));
    }
}
