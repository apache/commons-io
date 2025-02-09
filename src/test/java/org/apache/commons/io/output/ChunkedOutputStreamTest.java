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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.TempFile;
import org.apache.commons.lang3.ArrayFill;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ChunkedOutputStream}.
 */
public class ChunkedOutputStreamTest {

    private ByteArrayOutputStream newByteArrayOutputStream(final AtomicInteger numWrites) {
        return new ByteArrayOutputStream() {
            @Override
            public void write(final byte[] b, final int off, final int len) {
                numWrites.incrementAndGet();
                super.write(b, off, len);
            }
        };
    }

    /**
     * Tests the default chunk size with a ByteArrayOutputStream.
     *
     * @throws IOException
     */
    @Test
    public void testBuildSetByteArrayOutputStream() throws IOException {
        final AtomicInteger numWrites = new AtomicInteger();
        try (ByteArrayOutputStream baos = newByteArrayOutputStream(numWrites);
                ChunkedOutputStream chunked = ChunkedOutputStream.builder().setOutputStream(baos).get()) {
            chunked.write(new byte[IOUtils.DEFAULT_BUFFER_SIZE + 1]);
            assertEquals(2, numWrites.get());
        }
        assertThrows(IllegalStateException.class, () -> ChunkedOutputStream.builder().get());
    }

    /**
     * Tests the default chunk size with a Path.
     *
     * @throws IOException
     */
    @Test
    public void testBuildSetPath() throws IOException {
        try (TempFile tempFile = TempFile.create("test-", ".txt")) {
            final byte[] fill = ArrayFill.fill(new byte[IOUtils.DEFAULT_BUFFER_SIZE + 1], (byte) 'a');
            final Path tempPath = tempFile.get();
            try (ChunkedOutputStream chunked = ChunkedOutputStream.builder().setPath(tempPath).get()) {
                chunked.write(fill);
            }
            assertArrayEquals(fill, Files.readAllBytes(tempPath));
        }
    }

    @Test
    public void testDefaultConstructor() throws IOException {
        final AtomicInteger numWrites = new AtomicInteger();
        try (ByteArrayOutputStream baos = newByteArrayOutputStream(numWrites);
                ChunkedOutputStream chunked = new ChunkedOutputStream(baos)) {
            chunked.write(new byte[IOUtils.DEFAULT_BUFFER_SIZE + 1]);
            assertEquals(2, numWrites.get());
        }
    }

    @Test
    public void testNegativeChunkSize() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new ChunkedOutputStream(new ByteArrayOutputStream(), -1));
        // Builder resets invalid input to the default.
        try (ChunkedOutputStream os = ChunkedOutputStream.builder().setOutputStream(new ByteArrayOutputStream()).setBufferSize(-1).get()) {
            assertEquals(IOUtils.DEFAULT_BUFFER_SIZE, os.getChunkSize());
        }
    }

    @Test
    public void testWriteFourChunks() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        try (ByteArrayOutputStream baos = newByteArrayOutputStream(numWrites);
                ChunkedOutputStream chunked = new ChunkedOutputStream(baos, 10)) {
            chunked.write("0123456789012345678901234567891".getBytes());
            assertEquals(4, numWrites.get());
        }
    }

    @Test
    public void testZeroChunkSize() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new ChunkedOutputStream(new ByteArrayOutputStream(), 0));
        // Builder resets invalid input to the default.
        try (ChunkedOutputStream os = ChunkedOutputStream.builder().setOutputStream(new ByteArrayOutputStream()).setBufferSize(0).get()) {
            assertEquals(IOUtils.DEFAULT_BUFFER_SIZE, os.getChunkSize());
        }
    }

}
