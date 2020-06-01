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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * modified from CircularBufferInputStreamTest
 * @see CircularBufferInputStreamTest
 */
public class UnsynchronizedBufferedInputStreamTest {
    /**
     * Always using the same seed should ensure a reproducable test.
     */
    private final Random rnd = new Random(1530960934483L);

    @Test
    public void testRandomRead() throws Exception {
        final byte[] inputBuffer = newInputBuffer();
        final byte[] bufferCopy = new byte[inputBuffer.length];
        final ByteArrayInputStream bais = new ByteArrayInputStream(inputBuffer);
        @SuppressWarnings("resource") final UnsynchronizedBufferedInputStream cbis =
                new UnsynchronizedBufferedInputStream(bais, 253);
        int offset = 0;
        final byte[] readBuffer = new byte[256];
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
        UnsynchronizedBufferedInputStream b = new UnsynchronizedBufferedInputStream(null);
        closeSeveralTimes(b);
        UnsynchronizedBufferedInputStream b2 =
                new UnsynchronizedBufferedInputStream(new ReaderInputStream(new StringReader("")));
        closeSeveralTimes(b2);
    }

    private void closeSeveralTimes(UnsynchronizedBufferedInputStream b) throws IOException {
        b.close();
        b.close();
        b.close();
        b.close();
        b.close();
    }

    @Test
    public void testFullRead() throws Exception {
        UnsynchronizedBufferedInputStream b =
                new UnsynchronizedBufferedInputStream(new ReaderInputStream(new StringReader("aaaaa")));
        while (b.read() != IOUtils.EOF) {
        }
    }

    @Test
    public void testFullReadArray() throws Exception {
        UnsynchronizedBufferedInputStream b =
                new UnsynchronizedBufferedInputStream(new ReaderInputStream(new StringReader("aaaaa")));
        final byte[] buffer = new byte[5];
        while (true) {
            final int res = b.read(buffer, 0, buffer.length);
            if (res == IOUtils.EOF) {
                break;
            }
        }
    }

    @Test
    public void testWeirdReadArray() throws Exception {
        UnsynchronizedBufferedInputStream b =
                new UnsynchronizedBufferedInputStream(new ReaderInputStream(new StringReader("aaaaa")));
        final byte[] buffer = new byte[5];
        int res;
        res = b.read(buffer, 0, 0);
        assertEquals(res, 0);
        res = b.read(buffer, 0, -20);
        assertEquals(res, 0);
    }

    /**
     * Create a large, but random input buffer.
     */
    private byte[] newInputBuffer() {
        final byte[] buffer = new byte[16 * 512 + rnd.nextInt(512)];
        rnd.nextBytes(buffer);
        return buffer;
    }
}
