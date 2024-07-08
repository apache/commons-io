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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.MessageDigestCalculatingInputStream.Builder;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link MessageDigestCalculatingInputStream}.
 */
@SuppressWarnings("deprecation")
public class MessageDigestCalculatingInputStreamTest {

    private InputStream createInputStream() throws IOException {
        return MessageDigestCalculatingInputStream.builder()
                .setInputStream(new ByteArrayInputStream(MessageDigestInputStreamTest.generateRandomByteStream(256))).get();
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
    public void testNormalUse() throws Exception {
        for (int i = 256; i < 8192; i *= 2) {
            final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(i);
            final MessageDigest defaultMessageDigest = MessageDigestCalculatingInputStream.getDefaultMessageDigest();
            final byte[] defaultExpect = defaultMessageDigest.digest(buffer);
            // Defaults
            try (MessageDigestCalculatingInputStream messageDigestInputStream = new MessageDigestCalculatingInputStream(new ByteArrayInputStream(buffer))) {
                messageDigestInputStream.consume();
                assertArrayEquals(defaultExpect, messageDigestInputStream.getMessageDigest().digest());
            }
            try (MessageDigestCalculatingInputStream messageDigestInputStream = MessageDigestCalculatingInputStream.builder()
                    .setInputStream(new ByteArrayInputStream(buffer)).get()) {
                messageDigestInputStream.consume();
                assertArrayEquals(defaultExpect, messageDigestInputStream.getMessageDigest().digest());
            }
            try (MessageDigestCalculatingInputStream messageDigestInputStream = MessageDigestCalculatingInputStream.builder().setByteArray(buffer).get()) {
                messageDigestInputStream.consume();
                assertArrayEquals(defaultExpect, messageDigestInputStream.getMessageDigest().digest());
            }
            // SHA-512
            final byte[] sha512Expect = DigestUtils.sha512(buffer);
            {
                final Builder builder = MessageDigestCalculatingInputStream.builder();
                builder.setMessageDigest(MessageDigestAlgorithms.SHA_512);
                builder.setInputStream(new ByteArrayInputStream(buffer));
                try (MessageDigestCalculatingInputStream messageDigestInputStream = builder.get()) {
                    messageDigestInputStream.consume();
                    assertArrayEquals(sha512Expect, messageDigestInputStream.getMessageDigest().digest());
                }
            }
            {
                final Builder builder = MessageDigestCalculatingInputStream.builder();
                builder.setMessageDigest(MessageDigestAlgorithms.SHA_512);
                builder.setInputStream(new ByteArrayInputStream(buffer));
                try (MessageDigestCalculatingInputStream messageDigestInputStream = builder.get()) {
                    messageDigestInputStream.consume();
                    assertArrayEquals(sha512Expect, messageDigestInputStream.getMessageDigest().digest());
                }
            }
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

}
