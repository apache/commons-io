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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Charsets required of every implementation of the Java platform.
 *
 * From the Java documentation <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">
 * Standard charsets</a>:
 * <p>
 * <cite>Every implementation of the Java platform is required to support the following character encodings. Consult
 * the release documentation for your implementation to see if any other encodings are supported. Consult the release
 * documentation for your implementation to see if any other encodings are supported. </cite>
 * </p>
 *
 * <ul>
 * <li>{@code US-ASCII}<br>
 * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set.</li>
 * <li>{@code ISO-8859-1}<br>
 * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.</li>
 * <li>{@code UTF-8}<br>
 * Eight-bit Unicode Transformation Format.</li>
 * <li>{@code UTF-16BE}<br>
 * Sixteen-bit Unicode Transformation Format, big-endian byte order.</li>
 * <li>{@code UTF-16LE}<br>
 * Sixteen-bit Unicode Transformation Format, little-endian byte order.</li>
 * <li>{@code UTF-16}<br>
 * Sixteen-bit Unicode Transformation Format, byte order specified by a mandatory initial byte-order mark (either order
 * accepted on input, big-endian used on output.)</li>
 * </ul>
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
 * @since 2.3
 */
public class Charsets {

    //
    // This class should only contain Charset instances for required encodings. This guarantees that it will load
    // correctly and without delay on all Java platforms.
    //

    private static final SortedMap<String, Charset> STANDARD_CHARSET_MAP;

    static {
        final SortedMap<String, Charset> standardCharsetMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        standardCharsetMap.put(StandardCharsets.ISO_8859_1.name(), StandardCharsets.ISO_8859_1);
        standardCharsetMap.put(StandardCharsets.US_ASCII.name(), StandardCharsets.US_ASCII);
        standardCharsetMap.put(StandardCharsets.UTF_16.name(), StandardCharsets.UTF_16);
        standardCharsetMap.put(StandardCharsets.UTF_16BE.name(), StandardCharsets.UTF_16BE);
        standardCharsetMap.put(StandardCharsets.UTF_16LE.name(), StandardCharsets.UTF_16LE);
        standardCharsetMap.put(StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8);
        STANDARD_CHARSET_MAP = Collections.unmodifiableSortedMap(standardCharsetMap);
    }

    /**
     * CharEncodingISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    /**
     * <p>
     * Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block of the Unicode character set.
     * </p>
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset US_ASCII = StandardCharsets.US_ASCII;

    /**
     * <p>
     * Sixteen-bit Unicode Transformation Format, The byte order specified by a mandatory initial byte-order mark
     * (either order accepted on input, big-endian used on output)
     * </p>
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_16 = StandardCharsets.UTF_16;

    /**
     * <p>
     * Sixteen-bit Unicode Transformation Format, big-endian byte order.
     * </p>
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;

    /**
     * <p>
     * Sixteen-bit Unicode Transformation Format, little-endian byte order.
     * </p>
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;

    /**
     * <p>
     * Eight-bit Unicode Transformation Format.
     * </p>
     * <p>
     * Every implementation of the Java platform is required to support this character encoding.
     * </p>
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     * @deprecated Use Java 7's {@link java.nio.charset.StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Tests whether the given non-null Charset has an alias of the given name.
     *
     * @param charset a non-null Charset.
     * @param charsetName The name to test.
     * @return whether the given non-null charset name is a UTF-8 alias.
     * @since 2.20.0
     */
    public static boolean isAlias(final Charset charset, final String charsetName) {
        return charsetName != null && (charset.name().equalsIgnoreCase(charsetName) || charset.aliases().stream().anyMatch(charsetName::equalsIgnoreCase));
    }

    /**
     * Tests whether a given encoding is UTF-8. If the given charset is null, then check the platform's default encoding.
     *
     * @param charset If the given charset is null, then check the platform's default encoding.
     * @return whether a given encoding is UTF-8.
     * @since 2.20.0
     */
    public static boolean isUTF8(final Charset charset) {
        return isUTF8Alias(toCharset(charset).name());
    }

    /**
     * Tests whether the given non-null charset name is a UTF-8 alias.
     *
     * @param charsetName a non-null charset name.
     * @return whether the given non-null charset name is a UTF-8 alias.
     */
    private static boolean isUTF8Alias(final String charsetName) {
        return isAlias(StandardCharsets.UTF_8, charsetName);
    }

    /**
     * Constructs a sorted map from canonical charset names to charset objects required of every implementation of the
     * Java platform.
     * <p>
     * From the Java documentation <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">
     * Standard charsets</a>:
     * </p>
     *
     * @return An immutable, case-insensitive map from canonical charset names to charset objects.
     * @see Charset#availableCharsets()
     * @since 2.5
     */
    public static SortedMap<String, Charset> requiredCharsets() {
        return STANDARD_CHARSET_MAP;
    }

    /**
     * Returns the given Charset or the {@link Charset#defaultCharset() default Charset} if the given Charset is null.
     *
     * @param charset
     *            A charset or null.
     * @return the given Charset or the default Charset if the given Charset is null
     * @see Charset#defaultCharset()
     */
    public static Charset toCharset(final Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    /**
     * Returns the given charset if non-null, otherwise return defaultCharset.
     *
     * @param charset The charset to test, may be null.
     * @param defaultCharset The charset to return if charset is null, may be null.
     * @return a Charset.
     * @since 2.12.0
     */
    public static Charset toCharset(final Charset charset, final Charset defaultCharset) {
        return charset == null ? defaultCharset : charset;
    }

    /**
     * Returns a Charset for the named charset. If the name is null, return the {@link Charset#defaultCharset() default Charset}.
     *
     * @param charsetName The name of the requested charset, may be null.
     * @return a Charset for the named charset.
     * @throws UnsupportedCharsetException If the named charset is unavailable (unchecked exception).
     * @see Charset#defaultCharset()
     */
    public static Charset toCharset(final String charsetName) throws UnsupportedCharsetException {
        return toCharset(charsetName, Charset.defaultCharset());
    }

    /**
     * Returns a Charset for the named charset. If the name is null, return the given default Charset.
     *
     * @param charsetName The name of the requested charset, may be null.
     * @param defaultCharset The charset to return if charsetName is null, may be null.
     * @return a Charset for the named charset.
     * @throws UnsupportedCharsetException If the named charset is unavailable (unchecked exception).
     * @since 2.12.0
     */
    public static Charset toCharset(final String charsetName, final Charset defaultCharset) throws UnsupportedCharsetException {
        return charsetName == null ? defaultCharset : Charset.forName(charsetName);
    }

    /**
     * Returns a Charset for the named charset or the {@code defaultCharset}.
     * <p>
     * If {@code charsetName} cannot load a charset, return {@code defaultCharset}. Therefore, this method should never fail and always return a Charset.
     * </p>
     *
     * @param charsetName    The name of the requested charset, may be null.
     * @param defaultCharset The charset to return if charsetName is null or there is a problem, may be null which returns {@link Charset#defaultCharset()}.
     * @return a Charset for the named charset or {@code defaultCharset} if any errors occur.
     * @see Charset#defaultCharset()
     * @since 2.20.0
     */
    public static Charset toCharsetDefault(final String charsetName, final Charset defaultCharset) {
        try {
            return toCharset(charsetName);
        } catch (final RuntimeException ignored) {
            return toCharset(defaultCharset);
        }
    }

    /**
     * Construct a new instance.
     *
     * @deprecated Will be private in 4.0
     */
    @Deprecated
    public Charsets() {
        // empty
    }
}
