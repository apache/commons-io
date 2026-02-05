/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
     * @return {@code Charset.availableCharsets().values()}.
     */
    public static Collection<Charset> availableCharsetsValues() {
        return Charset.availableCharsets().values();
    }

    static Stream<Arguments> charsetAliasProvider() {
        return Charset.availableCharsets().entrySet().stream()
                .flatMap(entry -> entry.getValue().aliases().stream().map(a -> Arguments.of(entry.getValue(), a)));
    }

    /**
     * For parameterized tests.
     *
     * @return {@code Charset.requiredCharsets().keySet()}.
     */
    public static Set<String> getRequiredCharsetNames() {
        return Charsets.requiredCharsets().keySet();
    }

    @ParameterizedTest
    @MethodSource("charsetAliasProvider")
    void testIsAlias(final Charset charset, final String charsetAlias) {
        assertTrue(Charsets.isAlias(charset, charsetAlias));
        assertTrue(Charsets.isAlias(charset, charsetAlias.toLowerCase()));
        assertTrue(Charsets.isAlias(charset, charsetAlias.toUpperCase()));
        assertTrue(Charsets.isAlias(charset, charset.name()));
        assertFalse(Charsets.isAlias(charset, null));
    }

    @Test
    void testIso8859_1() {
        assertEquals("ISO-8859-1", Charsets.ISO_8859_1.name());
    }

    @ParameterizedTest
    @MethodSource("availableCharsetsValues")
    void testIsUTF8Charset(final Charset charset) {
        assumeFalse(StandardCharsets.UTF_8.equals(charset));
        charset.aliases().forEach(n -> assertFalse(Charsets.isUTF8(Charset.forName(n))));
    }

    void testIsUTF8CharsetUTF8() {
        assertTrue(Charsets.isUTF8(StandardCharsets.UTF_8));
        StandardCharsets.UTF_8.aliases().forEach(n -> assertTrue(Charsets.isUTF8(Charset.forName(n))));
    }

    @Test
    void testRequiredCharsets() {
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
    void testToCharset_String() {
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((String) null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((Charset) null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset(Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8));
    }

    @Test
    void testToCharsetDefault() {
        assertEquals(Charset.defaultCharset(), Charsets.toCharsetDefault((String) null, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharsetDefault(StringUtils.EMPTY, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharsetDefault(".", null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharsetDefault(null, Charset.defaultCharset()));
        assertEquals(Charset.defaultCharset(), Charsets.toCharsetDefault(Charset.defaultCharset().name(), Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharsetDefault(StandardCharsets.UTF_8.name(), Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharsetDefault(StandardCharsets.UTF_8.name(), null));
    }

    @Test
    void testToCharsetWithStringCharset() {
        assertNull(Charsets.toCharset((String) null, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((String) null, Charset.defaultCharset()));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset((Charset) null, Charset.defaultCharset()));
        assertNull(Charsets.toCharset((Charset) null, null));
        assertEquals(Charset.defaultCharset(), Charsets.toCharset(Charset.defaultCharset(), Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8, Charset.defaultCharset()));
        assertEquals(StandardCharsets.UTF_8, Charsets.toCharset(StandardCharsets.UTF_8, null));
    }

    @Test
    void testUsAscii() {
        assertEquals(StandardCharsets.US_ASCII.name(), Charsets.US_ASCII.name());
    }

    @Test
    void testUtf16() {
        assertEquals(StandardCharsets.UTF_16.name(), Charsets.UTF_16.name());
    }

    @Test
    void testUtf16Be() {
        assertEquals(StandardCharsets.UTF_16BE.name(), Charsets.UTF_16BE.name());
    }

    @Test
    void testUtf16Le() {
        assertEquals(StandardCharsets.UTF_16LE.name(), Charsets.UTF_16LE.name());
    }

    @Test
    void testUtf8() {
        assertEquals(StandardCharsets.UTF_8.name(), Charsets.UTF_8.name());
    }

}
