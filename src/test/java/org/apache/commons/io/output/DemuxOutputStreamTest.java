/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link DemuxOutputStream}.
 */
class DemuxOutputStreamTest {

    private static final class TrackingOutputStream extends OutputStream {

        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        private int bulkWriteCalls;

        private int singleByteWriteCalls;

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            bulkWriteCalls++;
            delegate.write(b, off, len);
        }

        @Override
        public void write(final int b) throws IOException {
            singleByteWriteCalls++;
            delegate.write(b);
        }
    }

    @Test
    void testWriteByteArrayAtDelegatesAsBulkWrite() throws IOException {
        final DemuxOutputStream demux = new DemuxOutputStream();
        final TrackingOutputStream output = new TrackingOutputStream();
        final byte[] bytes = { 1, 2, 3, 4 };
        demux.bindStream(output);

        demux.write(bytes, 1, 2);

        assertEquals(1, output.bulkWriteCalls);
        assertEquals(0, output.singleByteWriteCalls);
        assertArrayEquals(new byte[] { 2, 3 }, output.delegate.toByteArray());
    }

    @Test
    void testWriteByteArrayAtWithoutBoundStream() throws IOException {
        final DemuxOutputStream demux = new DemuxOutputStream();

        assertDoesNotThrow(() -> demux.write(new byte[] { 1, 2, 3 }, 1, 2));
        assertThrows(NullPointerException.class, () -> demux.write(null, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> demux.write(new byte[3], 2, 2));
    }
}
