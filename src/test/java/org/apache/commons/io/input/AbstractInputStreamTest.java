/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link InputStream} subclasses.
 * <p>
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was
 * called {@code GenericFileInputStreamSuite}.
 * </p>
 */
public abstract class AbstractInputStreamTest {

    static final String ARRAY_LENGTHS_NAME = "org.apache.commons.io.input.AbstractInputStreamTest#getArrayLengths";

    static final int[] ARRAY_LENGTHS = { 0, 1, 2, 4, 8, 16, 32, 64, 128 };

    static int[] getArrayLengths() {
        return ARRAY_LENGTHS;
    }

    protected static byte[] ActualBytes;
    protected static byte[] ExpectedBytes;
    protected static Path InputPath;

    /**
     * Set in subclasses.
     */
    protected InputStream[] inputStreams;

    @TempDir
    static Path tempDir;

    @BeforeAll
    public static void setUp() throws IOException {
        // Create a byte array of size 2 MB with random bytes
        ActualBytes = RandomUtils.insecure().randomBytes(2 * 1024 * 1024);
        ExpectedBytes = ActualBytes.clone();
        InputPath = tempDir.resolve(AbstractInputStreamTest.class.getSimpleName() + ".tmp");
        Files.write(InputPath, ActualBytes);
    }

    @AfterEach
    public void tearDown() throws IOException {
        IOUtils.close(inputStreams);
    }

    @Test
    public void testAvailableAfterClose() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            inputStream.close();
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testAvailableAfterOpen() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            assertTrue(inputStream.available() >= 0);
        }
    }

    @Test
    public void testAvailableAfterRead() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            assertNotEquals(IOUtils.EOF, inputStream.read());
            assertTrue(inputStream.available() > 0);
        }
    }

    @Test
    public void testAvailableAtEnd() throws Exception {
        for (final InputStream inputStream : inputStreams) {
            IOUtils.consume(inputStream);
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testBytesSkipped() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 1024; i < ExpectedBytes.length; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testBytesSkippedAfterEOF() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            assertEquals(ExpectedBytes.length, inputStream.skip(ActualBytes.length + 1));
            assertEquals(-1, inputStream.read());
        }
    }

    @Test
    public void testBytesSkippedAfterRead() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (int i = 0; i < 1024; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 2048; i < ExpectedBytes.length; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testNegativeBytesSkippedAfterRead() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (int i = 0; i < 1024; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
            // Skipping negative bytes should essential be a no-op
            assertEquals(0, inputStream.skip(-1));
            assertEquals(0, inputStream.skip(-1024));
            assertEquals(0, inputStream.skip(Long.MIN_VALUE));
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 2048; i < ExpectedBytes.length; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
    public void testReadMultipleBytes(final int bufferSize) throws IOException {
        for (final InputStream inputStream : inputStreams) {
            final byte[] readBytes = new byte[bufferSize];
            int i = 0;
            while (i < ExpectedBytes.length) {
                final int read = inputStream.read(readBytes, 0, readBytes.length);
                for (int j = 0; j < read; j++) {
                    assertEquals(ExpectedBytes[i], readBytes[j]);
                    i++;
                }
            }
        }
    }

    @Test
    public void testReadOneByOne() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (final byte randomByte : ExpectedBytes) {
                assertEquals(randomByte, (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testReadOneByOneCheckAvailable() throws IOException {
        final AtomicInteger refII = new AtomicInteger();
        for (int idxInputs = 0; idxInputs < inputStreams.length; idxInputs++) {
            refII.set(idxInputs);
            final AtomicInteger refIB = new AtomicInteger();
            @SuppressWarnings("resource")
            final InputStream inputStream = inputStreams[idxInputs];
            for (int idxBytes = 0; idxBytes < ExpectedBytes.length; idxBytes++) {
                refIB.set(idxBytes);
                final byte randomByte = ExpectedBytes[idxBytes];
                // Check that available() doesn't have a side effect on read()
                final int available = inputStream.available();
                final Supplier<String> messageSupplier = () -> String.format("idxInputs = %,d, idxBytes = %,d, available = %,d", refII.get(), refIB.get(),
                        available);
                assertTrue(available >= 0, messageSupplier);
                assertEquals(randomByte, (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testReadPastEof() throws IOException {
        final InputStream is = inputStreams[0];
        final byte[] buf = new byte[1024];
        while (is.read(buf, 0, buf.length) != -1) {
            // empty
        }
        final int readAfterEOF = is.read(buf, 0, buf.length);
        assertEquals(-1, readAfterEOF);
    }

    @Test
    public void testSkipFromFileChannel() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            // Since the buffer is smaller than the skipped bytes, this will guarantee
            // we skip from underlying file channel.
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 1024; i < 2048; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
            assertEquals(256, inputStream.skip(256));
            assertEquals(256, inputStream.skip(256));
            assertEquals(512, inputStream.skip(512));
            for (int i = 3072; i < ExpectedBytes.length; i++) {
                assertEquals(ExpectedBytes[i], (byte) inputStream.read());
            }
        }
    }
}
