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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests functionality of {@link BufferedFileChannelInputStream}.
 *
 * This class was ported and adapted from Apache Spark commit 933dc6cb7b3de1d8ccaf73d124d6eb95b947ed19 where it was
 * called {@code GenericFileInputStreamSuite}.
 */
public abstract class AbstractInputStreamTest {

    private byte[] randomBytes;

    protected File inputFile;

    protected InputStream[] inputStreams;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a byte array of size 2 MB with random bytes
        randomBytes = RandomUtils.nextBytes(2 * 1024 * 1024);
        inputFile = File.createTempFile("temp-file", ".tmp");
        FileUtils.writeByteArrayToFile(inputFile, randomBytes);
    }

    @AfterEach
    public void tearDown() throws IOException {
        inputFile.delete();

        for (final InputStream is : inputStreams) {
            is.close();
        }
    }

    @Test
    public void testBytesSkipped() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 1024; i < randomBytes.length; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testBytesSkippedAfterEOF() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            assertEquals(randomBytes.length, inputStream.skip(randomBytes.length + 1));
            assertEquals(-1, inputStream.read());
        }
    }

    @Test
    public void testBytesSkippedAfterRead() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (int i = 0; i < 1024; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 2048; i < randomBytes.length; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testNegativeBytesSkippedAfterRead() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (int i = 0; i < 1024; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
            // Skipping negative bytes should essential be a no-op
            assertEquals(0, inputStream.skip(-1));
            assertEquals(0, inputStream.skip(-1024));
            assertEquals(0, inputStream.skip(Long.MIN_VALUE));
            assertEquals(1024, inputStream.skip(1024));
            for (int i = 2048; i < randomBytes.length; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testReadMultipleBytes() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            final byte[] readBytes = new byte[8 * 1024];
            int i = 0;
            while (i < randomBytes.length) {
                final int read = inputStream.read(readBytes, 0, 8 * 1024);
                for (int j = 0; j < read; j++) {
                    assertEquals(randomBytes[i], readBytes[j]);
                    i++;
                }
            }
        }
    }

    @Test
    public void testReadOneByte() throws IOException {
        for (final InputStream inputStream : inputStreams) {
            for (final byte randomByte : randomBytes) {
                assertEquals(randomByte, (byte) inputStream.read());
            }
        }
    }

    @Test
    public void testReadPastEOF() throws IOException {
        final InputStream is = inputStreams[0];
        final byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf, 0, buf.length)) != -1) {
            
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
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
            assertEquals(256, inputStream.skip(256));
            assertEquals(256, inputStream.skip(256));
            assertEquals(512, inputStream.skip(512));
            for (int i = 3072; i < randomBytes.length; i++) {
                assertEquals(randomBytes[i], (byte) inputStream.read());
            }
        }
    }
}
