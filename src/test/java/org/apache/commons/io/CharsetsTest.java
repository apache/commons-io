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

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Charsets}.
 */
@SuppressWarnings("deprecation") // testing deprecated code
public class CharsetsTest {

    /**
     * For parameterized tests.
     */
    public static final String AVAIL_CHARSETS = "org.apache.commons.io.CharsetsTest#availableCharsetsKeySet";
    /**
     * For parameterized tests.
     */
    public static final String REQUIRED_CHARSETS = "org.apache.commons.io.CharsetsTest#getRequiredCharsetNames";

    /**
     * For parameterized tests.
     *
     * @return {@code Charset.availableCharsets().keySet()}.
     */
    public static Set<String> availableCharsetsKeySet() {
        return Charset.availableCharsets().keySet();
    }

    /**
     * For parameterized tests.
     *
     * @return {@code Charset.requiredCharsets().keySet()}.
     */
    public static Set<String> getRequiredCharsetNames() {
        return Charsets.requiredCharsets().keySet();
    }

    @Test
    public void testIso8859_1() {
        assertEquals("ISO-8859-1", Charsets.ISO_8859_1.name());
    }

    @Test
    public void testRequiredCharsets() {
        final SortedMap<String, Charset> requiredCharsets = Charsets.requiredCharsets();
        // test for what we expect to be there as of Java 6
        // Make sure the object at the given key is the right one
        assertEquals(requiredCharsets.get("US-ASCII").name(), "US-ASCII");
        assertEquals(requiredCharsets.get("ISO-8859-1").name(), "ISO-8859-1");
        assertEquals(requiredCharsets.get("UTF-8").name(), "UTF-8");
        assertEquals(requiredCharsets.get("UTF-16").name(), "UTF-16");
        assertEquals(requiredCharsets.get("UTF-16BE").name(), "UTF-16BE");
        assertEquals(requiredCharsets.get("UTF-16LE").name(), "UTF-16LE");
    }

    @Test
    public void testToCharset_String() {
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((String) null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((Charset) null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset(Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8));
    }

    @Test
    public void testToCharset_String_Charset() {
        assertNull(Charsets.toCharset((String) null, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((String) null, Charset.defaultCharset()));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((Charset) null, Charset.defaultCharset()));
        assertNull(Charsets.toCharset((Charset) null, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset(Charset.defaultCharset(), Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8, Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8, null));
    }

    @Test
    public void testUsAscii() {
        assertEquals(StandardCharsets.US_ASCII.name(), Charsets.US_ASCII.name());
    }

    @Test
    public void testUtf16() {
        assertEquals(StandardCharsets.UTF_16.name(), Charsets.UTF_16.name());
    }

    @Test
    public void testUtf16Be() {
        assertEquals(StandardCharsets.UTF_16BE.name(), Charsets.UTF_16BE.name());
    }

    @Test
    public void testUtf16Le() {
        assertEquals(StandardCharsets.UTF_16LE.name(), Charsets.UTF_16LE.name());
    }

    @Test
    public void testUtf8() {
        assertEquals(StandardCharsets.UTF_8.name(), Charsets.UTF_8.name());
    }

}
