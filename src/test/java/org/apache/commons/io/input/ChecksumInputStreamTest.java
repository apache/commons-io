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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ChecksumInputStream}.
 */
public class ChecksumInputStreamTest {

    private ChecksumInputStream createInputStream() throws IOException {
        return ChecksumInputStream.builder().setCharSequence("Hi").setChecksum(new CRC32()).get();
    }

    @Test
    public void testAfterReadConsumer() throws Exception {
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = ChecksumInputStream.builder()
                .setCharSequence("Hi")
                .setChecksum(new CRC32())
                .setExpectedChecksumValue(1293356558)
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream bounded = ChecksumInputStream.builder()
                .setCharSequence("Hi")
                .setChecksum(new CRC32())
                .setExpectedChecksumValue(1293356558)
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(bounded)).getMessage());
        }
        // @formatter:on
    }

    @SuppressWarnings("resource")
    @Test
    public void testAvailableAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = createInputStream()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    public void testAvailableAfterOpen() throws Exception {
        try (InputStream in = createInputStream()) {
            assertTrue(in.available() > 0);
            assertEquals('H', in.read());
            assertTrue(in.available() > 0);
        }
    }

    @Test
    public void testDefaultThresholdFailure() throws IOException {
        final byte[] byteArray = new byte[3];
        final Adler32 adler32 = new Adler32();
        try (ChecksumInputStream checksum = ChecksumInputStream.builder()
        // @formatter:off
                .setByteArray(byteArray)
                .setChecksum(adler32)
                .setExpectedChecksumValue((byte) -68)
                .get()) {
                // @formatter:on
            assertEquals(0, checksum.getByteCount());
            assertEquals(-1, checksum.getRemaining());
            // Ask to read one more byte than there is, we get the correct byte count.
            assertEquals(byteArray.length, checksum.read(new byte[byteArray.length + 1]));
            // Next read is at EOF
            assertThrows(IOException.class, () -> checksum.read(new byte[1]));
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(-4, checksum.getRemaining());
        }
    }

    @Test
    public void testDefaultThresholdSuccess() throws IOException {
        // sanity-check
        final Adler32 sanityCheck = new Adler32();
        final byte[] byteArray = new byte[3];
        sanityCheck.update(byteArray);
        final long expectedChecksum = sanityCheck.getValue();
        // actual
        final Adler32 adler32 = new Adler32();
        try (ChecksumInputStream checksum = ChecksumInputStream.builder()
        // @formatter:off
                .setByteArray(byteArray)
                .setChecksum(adler32)
                .setExpectedChecksumValue(expectedChecksum)
                .get()) {
                // @formatter:on
            assertEquals(0, checksum.getByteCount());
            assertEquals(-1, checksum.getRemaining());
            assertEquals(3, checksum.read(byteArray));
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(-4, checksum.getRemaining());
            assertEquals(-1, checksum.read(byteArray));
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(-4, checksum.getRemaining());
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testReadAfterClose() throws Exception {
        final InputStream shadow;
        try (InputStream in = createInputStream()) {
            assertTrue(in.available() > 0);
            shadow = in;
        }
        assertEquals(IOUtils.EOF, shadow.read());
    }

    @Test
    public void testReadTakingByteArrayThrowsException() throws IOException {
        final Adler32 adler32 = new Adler32();
        final byte[] byteArray = new byte[3];
        final long sizeThreshold = -1859L;
        try (ChecksumInputStream checksum = ChecksumInputStream.builder()
        // @formatter:off
                .setByteArray(byteArray)
                .setChecksum(adler32)
                .setExpectedChecksumValue((byte) -68)
                .setCountThreshold(sizeThreshold)
                .get()) {
                // @formatter:on
            assertEquals(0, checksum.getByteCount());
            assertEquals(sizeThreshold, checksum.getRemaining());
            // Ask to read one more byte than there is.
            assertEquals(byteArray.length, checksum.read(new byte[byteArray.length + 1]));
            // Next read is at EOF
            assertThrows(IOException.class, () -> checksum.read(new byte[1]));
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(sizeThreshold - byteArray.length, checksum.getRemaining());
        }
    }

    @Test
    public void testReadTakingNoArgumentsThrowsException() throws IOException {
        final CRC32 crc32 = new CRC32();
        final byte[] byteArray = new byte[9];
        try (ChecksumInputStream checksum = ChecksumInputStream.builder()
        // @formatter:off
                .setByteArray(byteArray)
                .setChecksum(crc32)
                .setExpectedChecksumValue((byte) 1)
                .setCountThreshold(1)
                .get()) {
                // @formatter:on
            assertEquals(0, checksum.getByteCount());
            assertEquals(1, checksum.getRemaining());
            assertThrows(IOException.class, () -> checksum.read());
            assertEquals(1, checksum.getByteCount());
            assertEquals(0, checksum.getRemaining());
        }
    }

    @Test
    public void testSkip() throws IOException {
        // sanity-check
        final CRC32 sanityCheck = new CRC32();
        final byte[] byteArray = new byte[4];
        sanityCheck.update(byteArray);
        final long expectedChecksum = sanityCheck.getValue();
        // actual
        final CRC32 crc32 = new CRC32();
        final InputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        try (ChecksumInputStream checksum = ChecksumInputStream.builder()
        // @formatter:off
                .setInputStream(byteArrayInputStream)
                .setChecksum(crc32)
                .setExpectedChecksumValue(expectedChecksum)
                .setCountThreshold(33)
                .get()) {
                // @formatter:on
            assertEquals(0, checksum.getByteCount());
            assertEquals(4, checksum.read(byteArray));
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(29, checksum.getRemaining());
            final long skipReturnValue = checksum.skip((byte) 1);
            assertEquals(byteArray.length, checksum.getByteCount());
            assertEquals(29, checksum.getRemaining());
            assertEquals(558161692L, crc32.getValue());
            assertEquals(0, byteArrayInputStream.available());
            assertArrayEquals(new byte[4], byteArray);
            assertEquals(0L, skipReturnValue);
            assertEquals(29, checksum.getRemaining());
        }
    }

}
