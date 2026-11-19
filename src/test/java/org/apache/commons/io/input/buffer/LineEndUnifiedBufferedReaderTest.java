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
package org.apache.commons.io.input.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * modified from NonThreadSafeButFastBufferedReaderTest
 */
public class LineEndUnifiedBufferedReaderTest {
    /**
     * Always using the same seed should ensure a reproducable test.
     */
    private final Random rnd = new Random(1530960934483L);

    @Test
    public void testRandomRead() throws Exception {
        final char[] inputBuffer = newInputBuffer();
        final char[] bufferCopy = new char[inputBuffer.length];
        final CharArrayReader bais = new CharArrayReader(inputBuffer);
        @SuppressWarnings("resource") final LineEndUnifiedBufferedReader cbis =
                new LineEndUnifiedBufferedReader(bais, 253);
        int offset = 0;
        final char[] readBuffer = new char[256];
        while (offset < bufferCopy.length) {
            switch (rnd.nextInt(2)) {
                case 0: {
                    final int res = cbis.read();
                    if (res == IOUtils.EOF) {
                        throw new IllegalStateException("Unexpected EOF at offset " + offset);
                    }
                    if (inputBuffer[offset] != res) {
                        throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset +
                                ", got " + res);
                    }
                    ++offset;
                    break;
                }
                case 1: {
                    final int res = cbis.read(readBuffer, 0, rnd.nextInt(readBuffer.length + 1));
                    if (res == IOUtils.EOF) {
                        throw new IllegalStateException("Unexpected EOF at offset " + offset);
                    } else if (res == 0) {
                        throw new IllegalStateException("Unexpected zero-byte-result at offset " + offset);
                    } else {
                        for (int i = 0; i < res; i++) {
                            if (inputBuffer[offset] != readBuffer[i]) {
                                throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset + ", got " + readBuffer[i]);
                            }
                            ++offset;
                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected random choice value");
            }
        }
        bais.close();
        cbis.close();
    }

    @Test
    public void testClose() throws Exception {
        final LineEndUnifiedBufferedReader b = new LineEndUnifiedBufferedReader(null);
        closeSeveralTimes(b);
        final LineEndUnifiedBufferedReader b2 =
                new LineEndUnifiedBufferedReader(new StringReader(""));
        closeSeveralTimes(b2);
    }

    private void closeSeveralTimes(LineEndUnifiedBufferedReader b) throws IOException {
        b.close();
        b.close();
        b.close();
        b.close();
        b.close();
    }

    @Test
    public void testFullRead() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader("aaaaa"));
        while (b.read() != IOUtils.EOF) {
        }
    }

    @Test
    public void testFullReadArray() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader("aaaaa"));
        final char[] buffer = new char[5];
        while (true) {
            final int res = b.read(buffer, 0, buffer.length);
            if (res == IOUtils.EOF) {
                break;
            }
        }
    }

    @Test
    public void testWeirdReadArray() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader("aaaaa"));
        final char[] buffer = new char[5];
        int res;
        res = b.read(buffer, 0, 0);
        assertEquals(res, 0);
        res = b.read(buffer, 0, -20);
        assertEquals(res, 0);
    }

    /**
     * Create a large, but random input buffer.
     * Do not test `\r` problems in this test.
     * `\r` problems are specially tested in IOUtilsTestCase.testContentEqualsIgnoreEOL
     * @see org.apache.commons.io.IOUtilsTest#testContentEqualsIgnoreEOL()
     */
    private char[] newInputBuffer() {
        final char[] buffer = new char[16 * 512 + rnd.nextInt(512)];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (char) rnd.nextInt();
            while (buffer[i] == '\r') {
                buffer[i] = (char) rnd.nextInt();
            }
        }
        return buffer;
    }

    @Test
    public void testCachedCR_ReadArray() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader(""));
        b.setCachedCR(true);
        final char[] chars = new char[5];
        Assertions.assertEquals(b.read(chars), 1);
        assertEquals('\n', chars[0]);
    }

    @Test
    public void testCachedCR_Read() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader(""));
        b.setCachedCR(true);
        Assertions.assertEquals('\n', b.read());
    }

    @Test
    public void testCR_ReadArray() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader("\r"));
        final char[] chars = new char[5];
        Assertions.assertEquals(0, b.read(chars));
        Assertions.assertTrue(b.isCachedCR());
        Assertions.assertEquals(1, b.read(chars));
        assertEquals('\n', chars[0]);
    }

    @Test
    public void testCR_Read() throws Exception {
        final LineEndUnifiedBufferedReader b =
                new LineEndUnifiedBufferedReader(new StringReader("\r"));
        Assertions.assertEquals('\n', b.read());
    }
}
