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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Test the chunked output stream
 */
public class ChunkedOutputStreamTest {

    @Test
    public void write_four_chunks() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        try (final ByteArrayOutputStream baos = newByteArrayOutputStream(numWrites);
            final ChunkedOutputStream chunked = new ChunkedOutputStream(baos, 10)) {
            chunked.write("0123456789012345678901234567891".getBytes());
            assertEquals(4, numWrites.get());
        }
    }

    @Test
    public void negative_chunksize_not_permitted() {
        assertThrows(IllegalArgumentException.class, () -> new ChunkedOutputStream(new ByteArrayOutputStream(), 0));
    }

    @Test
    public void defaultConstructor() throws IOException {
        final AtomicInteger numWrites = new AtomicInteger();
        try (final ByteArrayOutputStream baos = newByteArrayOutputStream(numWrites);
            final ChunkedOutputStream chunked = new ChunkedOutputStream(baos)) {
            chunked.write(new byte[1024 * 4 + 1]);
            assertEquals(2, numWrites.get());
        }
    }

    private ByteArrayOutputStream newByteArrayOutputStream(final AtomicInteger numWrites) {
        return new ByteArrayOutputStream() {
            @Override
            public void write(final byte[] b, final int off, final int len) {
                numWrites.incrementAndGet();
                super.write(b, off, len);
            }
        };
    }

}
