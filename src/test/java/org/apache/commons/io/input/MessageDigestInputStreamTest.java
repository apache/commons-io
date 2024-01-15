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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
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

    @Test
    public void testNoDefault() throws Exception {
        assertThrows(IllegalStateException.class, () -> MessageDigestInputStream.builder().get());
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

}
