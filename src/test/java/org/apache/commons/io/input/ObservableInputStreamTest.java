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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ObservableInputStream.Observer;
import org.junit.jupiter.api.Test;

public class ObservableInputStreamTest {
    private static class LastByteKeepingObserver extends Observer {
        private int lastByteSeen = -1;
        private boolean finished;
        private boolean closed;

        @Override
        public void data(final int pByte) throws IOException {
            super.data(pByte);
            lastByteSeen = pByte;
        }

        @Override
        public void finished() throws IOException {
            super.finished();
            finished = true;
        }

        @Override
        public void closed() throws IOException {
            super.closed();
            closed = true;
        }
    }

    private static class LastBytesKeepingObserver extends Observer {
        private byte[] buffer = null;
        private int offset = -1;
        private int length = -1;

        @Override
        public void data(final byte[] pBuffer, final int pOffset, final int pLength) throws IOException {
            super.data(pBuffer, pOffset, pLength);
            buffer = pBuffer;
            offset = pOffset;
            length = pLength;
        }
    }

    /**
     * Tests that {@link Observer#data(int)} is called.
     */
    @Test
    public void testDataByteCalled() throws Exception {
        final byte[] buffer = MessageDigestCalculatingInputStreamTest
                .generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        final LastByteKeepingObserver lko = new LastByteKeepingObserver();
        try (final ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer))) {
            assertEquals(-1, lko.lastByteSeen);
            ois.read();
            assertEquals(-1, lko.lastByteSeen);
            assertFalse(lko.finished);
            assertFalse(lko.closed);
            ois.add(lko);
            for (int i = 1; i < buffer.length; i++) {
                final int result = ois.read();
                assertEquals((byte) result, buffer[i]);
                assertEquals(result, lko.lastByteSeen);
                assertFalse(lko.finished);
                assertFalse(lko.closed);
            }
            final int result = ois.read();
            assertEquals(-1, result);
            assertTrue(lko.finished);
            assertFalse(lko.closed);
            ois.close();
            assertTrue(lko.finished);
            assertTrue(lko.closed);
        }
    }

    /**
     * Tests that {@link Observer#data(byte[],int,int)} is called.
     */
    @Test
    public void testDataBytesCalled() throws Exception {
        final byte[] buffer = MessageDigestCalculatingInputStreamTest
                .generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        final ObservableInputStream ois = new ObservableInputStream(bais);
        final LastBytesKeepingObserver lko = new LastBytesKeepingObserver();
        final byte[] readBuffer = new byte[23];
        assertEquals(null, lko.buffer);
        ois.read(readBuffer);
        assertEquals(null, lko.buffer);
        ois.add(lko);
        for (;;) {
            if (bais.available() >= 2048) {
                final int result = ois.read(readBuffer);
                if (result == -1) {
                    ois.close();
                    break;
                }
                assertEquals(readBuffer, lko.buffer);
                assertEquals(0, lko.offset);
                assertEquals(readBuffer.length, lko.length);
            } else {
                final int res = Math.min(11, bais.available());
                final int result = ois.read(readBuffer, 1, 11);
                if (result == -1) {
                    ois.close();
                    break;
                }
                assertEquals(readBuffer, lko.buffer);
                assertEquals(1, lko.offset);
                assertEquals(res, lko.length);
            }
        }
    }

}
