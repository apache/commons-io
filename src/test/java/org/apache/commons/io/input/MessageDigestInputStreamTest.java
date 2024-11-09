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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link MessageDigestInputStream}.
 */
public class MessageDigestInputStreamTest {

    static byte[] generateRandomByteStream(final int pSize) {
        final byte[] buffer = new byte[pSize];
        final Random rnd = new Random();
        rnd.nextBytes(buffer);
        return buffer;
    }

    private InputStream createInputStream() throws IOException, NoSuchAlgorithmException {
        return createInputStream(new ByteArrayInputStream(MessageDigestInputStreamTest.generateRandomByteStream(256)));
    }

    private InputStream createInputStream(final InputStream origin) throws IOException, NoSuchAlgorithmException {
        // @formatter:off
        return MessageDigestInputStream.builder()
                .setMessageDigest(MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512))
                .setInputStream(origin)
                .get();
        // @formatter:on
    }

    @Test
    public void testAfterReadConsumer() throws Exception {
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = MessageDigestInputStream.builder()
                .setMessageDigest(MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512))
                .setCharSequence("Hi")
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream bounded = MessageDigestInputStream.builder()
                .setMessageDigest(MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512))
                .setCharSequence("Hi")
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertTrue(assertThrowsExactly(IOExceptionList.class, () -> IOUtils.consume(bounded)).getMessage().contains(message));
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
            assertNotEquals(IOUtils.EOF, in.read());
            assertTrue(in.available() > 0);
        }
    }

    @Test
    public void testNoDefault() throws Exception {
        // No default by design, call MUST set a message digest
        // Fail-fast, no need to try to process any input origin
        assertThrows(NullPointerException.class, () -> MessageDigestInputStream.builder().get());
        assertThrows(NullPointerException.class, () -> MessageDigestInputStream.builder().setInputStream(new ByteArrayInputStream(new byte[] { 1 })).get());
    }

    @Test
    public void testNormalUse() throws Exception {
        for (int i = 256; i < 8192; i *= 2) {
            final byte[] buffer = generateRandomByteStream(i);
            final byte[] expect = DigestUtils.sha512(buffer);
            try (MessageDigestInputStream messageDigestInputStream = MessageDigestInputStream.builder().setMessageDigest(MessageDigestAlgorithms.SHA_512)
                    .setInputStream(new ByteArrayInputStream(buffer)).get()) {
                messageDigestInputStream.consume();
                assertArrayEquals(expect, messageDigestInputStream.getMessageDigest().digest());
            }
            try (MessageDigestInputStream messageDigestInputStream = MessageDigestInputStream.builder().setByteArray(buffer)
                    .setMessageDigest(DigestUtils.getSha512Digest()).get()) {
                messageDigestInputStream.consume();
                assertArrayEquals(expect, messageDigestInputStream.getMessageDigest().digest());
            }
        }
    }

    @Test
    public void testReadAfterClose_ByteArrayInputStream() throws Exception {
        try (InputStream in = createInputStream()) {
            in.close();
            assertNotEquals(IOUtils.EOF, in.read());
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testReadAfterClose_ChannelInputStream() throws Exception {
        try (InputStream in = createInputStream(Files.newInputStream(Paths.get("src/test/resources/org/apache/commons/io/abitmorethan16k.txt")))) {
            in.close();
            // ChannelInputStream throws when closed
            assertThrows(IOException.class, in::read);
        }
    }

}
