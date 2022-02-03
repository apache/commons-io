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

package org.apache.commons.io.charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CharsetEncoders}.
 */
public class CharsetEncodersTest {

    @Test
    public void testToCharsetEncoders_default() {
        final CharsetEncoder charsetEncoder = CharsetEncoders.toCharsetEncoder(Charset.defaultCharset().newEncoder());
        assertNotNull(charsetEncoder);
        assertEquals(Charset.defaultCharset(), charsetEncoder.charset());
    }

    @Test
    public void testToCharsetEncoders_ISO_8859_1() {
        final CharsetEncoder charsetEncoder = CharsetEncoders.toCharsetEncoder(StandardCharsets.ISO_8859_1.newEncoder());
        assertNotNull(charsetEncoder);
        assertEquals(StandardCharsets.ISO_8859_1, charsetEncoder.charset());
    }

    @Test
    public void testToCharsetEncoders_null() {
        final CharsetEncoder charsetEncoder = CharsetEncoders.toCharsetEncoder(null);
        assertNotNull(charsetEncoder);
        assertEquals(Charset.defaultCharset(), charsetEncoder.charset());
    }
}
