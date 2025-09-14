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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileSystem.NameLengthStrategy.BYTES;
import static org.apache.commons.io.FileSystem.NameLengthStrategy.UTF16_CODE_UNITS;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileSystem.NameLengthStrategy;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests {@link FileSystem}.
 */
class FileSystemTest {

    /** A single ASCII character that encodes to 1 UTF-8 byte. */
    private static final String CHAR_UTF8_1B = "a";

    /** A single Unicode character that encodes to 2 UTF-8 bytes. */
    private static final String CHAR_UTF8_2B = "é";

    /** A single Unicode character that encodes to 3 UTF-8 bytes. */
    private static final String CHAR_UTF8_3B = "★";

    /** A single Unicode code point that encodes to 2 UTF-16 code units and 4 UTF-8 bytes. */
    private static final String CHAR_UTF8_4B = "😀";

    /**
     * A grapheme cluster that encodes to 69 UTF-8 bytes and 31 UTF-16 code units: 👩🏻‍🦰‍👨🏿‍🦲‍👧🏽‍🦱‍👦🏼‍🦳
     * <p>
     *     This should be treated as a single character in JDK 20+ for truncation purposes,
     *     even if it contains parts that have a meaning on their own.
     * </p>
     * <ul>
     *     <li>{@code 👩}: 4 UTF-8 bytes and 2 UTF-16 code points.</li>
     *     <li>{@code 👩🏻‍🦰}: 15 UTF-8 bytes and 7 UTF-16 code points.</li>
     * </ul>
     */
    private static final String CHAR_UTF8_69B =
            // woman + light skin + ZWJ + red hair = 15 bytes
            "\uD83D\uDC69\uD83C\uDFFB\u200D\uD83E\uDDB0"
                    // ZWJ = 3 bytes
                    + "\u200D"
                    // man + dark skin + ZWJ + bald = 15 bytes
                    + "\uD83D\uDC68\uD83C\uDFFF\u200D\uD83E\uDDB2"
                    // ZWJ = 3 bytes
                    + "\u200D"
                    // girl + medium skin + ZWJ + curly hair = 15 bytes
                    + "\uD83D\uDC67\uD83C\uDFFD\u200D\uD83E\uDDB1"
                    // ZWJ = 3 bytes
                    + "\u200D"
                    // boy + medium-light skin + ZWJ + white hair = 15 bytes
                    + "\uD83D\uDC66\uD83C\uDFFC\u200D\uD83E\uDDB3";

    /** File name of 255 bytes and 255 UTF-16 code units. */
    private static final String FILE_NAME_255_BYTES_UTF8_1B = repeat(CHAR_UTF8_1B, 255);

    /** File name of 255 bytes and 128 UTF-16 code units. */
    private static final String FILE_NAME_255_BYTES_UTF8_2B = repeat(CHAR_UTF8_2B, 127) + CHAR_UTF8_1B;

    /** File name of 255 bytes and 85 UTF-16 code units. */
    private static final String FILE_NAME_255_BYTES_UTF8_3B = repeat(CHAR_UTF8_3B, 85);

    /** File name of 255 bytes and 64 UTF-16 code units. */
    private static final String FILE_NAME_255_BYTES_UTF8_4B = repeat(CHAR_UTF8_4B, 63) + CHAR_UTF8_3B;

    /** File name of 255 bytes and 255 UTF-16 code units. */
    private static final String FILE_NAME_255_CHARS_UTF8_1B = FILE_NAME_255_BYTES_UTF8_1B;

    /** File name of 510 bytes and 255 UTF-16 code units. */
    private static final String FILE_NAME_255_CHARS_UTF8_2B = repeat(CHAR_UTF8_2B, 255);

    /** File name of 765 bytes and 255 UTF-16 code units. */
    private static final String FILE_NAME_255_CHARS_UTF8_3B = repeat(CHAR_UTF8_3B, 255);

    /** File name of 511 bytes and 255 UTF-16 code units. */
    private static final String FILE_NAME_255_CHARS_UTF8_4B = repeat(CHAR_UTF8_4B, 127) + CHAR_UTF8_3B;

    private static void createAndDelete(final Path tempDir, final String fileName) throws IOException {
        final Path filePath = tempDir.resolve(fileName);
        Files.createFile(filePath);
        try (Stream<Path> files = Files.list(tempDir)) {
            final boolean found = files.anyMatch(filePath::equals);
            if (!found) {
                throw new FileNotFoundException(fileName + " not found in " + tempDir);
            }
        }
        Files.delete(filePath);
    }

    static Stream<Arguments> testIsLegalName_Length() {
        return Stream.of(
                Arguments.of(FileSystem.GENERIC, repeat(FILE_NAME_255_BYTES_UTF8_1B, 4), UTF_8),
                Arguments.of(FileSystem.GENERIC, repeat(FILE_NAME_255_BYTES_UTF8_2B, 4), UTF_8),
                Arguments.of(FileSystem.GENERIC, repeat(FILE_NAME_255_BYTES_UTF8_3B, 4), UTF_8),
                Arguments.of(FileSystem.GENERIC, repeat(FILE_NAME_255_BYTES_UTF8_4B, 4), UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_BYTES_UTF8_1B, UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_BYTES_UTF8_2B, UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_BYTES_UTF8_3B, UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_BYTES_UTF8_4B, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_BYTES_UTF8_1B, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_BYTES_UTF8_2B, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_BYTES_UTF8_3B, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_BYTES_UTF8_4B, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_CHARS_UTF8_1B, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_CHARS_UTF8_2B, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_CHARS_UTF8_3B, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_CHARS_UTF8_4B, UTF_8),
                // Repeat some tests with other encodings for GENERIC and LINUX
                Arguments.of(FileSystem.GENERIC, repeat(FILE_NAME_255_BYTES_UTF8_1B, 4), US_ASCII),
                Arguments.of(FileSystem.GENERIC, repeat(CHAR_UTF8_2B, 1020), ISO_8859_1),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_BYTES_UTF8_1B, US_ASCII),
                Arguments.of(FileSystem.LINUX, repeat(CHAR_UTF8_2B, 255), ISO_8859_1));
    }

    static Stream<Arguments> testNameLengthStrategyTruncate_Succeeds() {
        // The grapheme cluster CHAR_UTF8_69B is treated as a single character in JDK 20+,
        final String woman;
        final String redHeadWoman;
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_19)) {
            woman = CHAR_UTF8_69B.substring(0, 2); // 👩
            redHeadWoman = CHAR_UTF8_69B.substring(0, 7); // 👩🏻‍🦰
        } else {
            woman = "";
            redHeadWoman = "";
        }
        return Stream.of(
                // Truncation by bytes
                // -------------------
                //
                // Empty
                Arguments.of(BYTES, 0, "", ""),
                // Simple name without truncation
                Arguments.of(BYTES, 10, "simple.txt", "simple.txt"),
                // Name starting with dot
                Arguments.of(BYTES, 10, "." + repeat(CHAR_UTF8_1B, 10), "." + repeat(CHAR_UTF8_1B, 9)),
                Arguments.of(BYTES, 20, "." + repeat(CHAR_UTF8_2B, 10), "." + repeat(CHAR_UTF8_2B, 9)),
                Arguments.of(BYTES, 30, "." + repeat(CHAR_UTF8_3B, 10), "." + repeat(CHAR_UTF8_3B, 9)),
                Arguments.of(BYTES, 40, "." + repeat(CHAR_UTF8_4B, 10), "." + repeat(CHAR_UTF8_4B, 9)),
                // Names with extensions
                Arguments.of(BYTES, 13, repeat(CHAR_UTF8_1B, 10) + ".txt", repeat(CHAR_UTF8_1B, 9) + ".txt"),
                Arguments.of(BYTES, 23, repeat(CHAR_UTF8_2B, 10) + ".txt", repeat(CHAR_UTF8_2B, 9) + ".txt"),
                Arguments.of(BYTES, 33, repeat(CHAR_UTF8_3B, 10) + ".txt", repeat(CHAR_UTF8_3B, 9) + ".txt"),
                Arguments.of(BYTES, 43, repeat(CHAR_UTF8_4B, 10) + ".txt", repeat(CHAR_UTF8_4B, 9) + ".txt"),
                // Names without extensions
                Arguments.of(BYTES, 1, CHAR_UTF8_1B, CHAR_UTF8_1B),
                Arguments.of(BYTES, 2, CHAR_UTF8_2B, CHAR_UTF8_2B),
                Arguments.of(BYTES, 3, CHAR_UTF8_3B, CHAR_UTF8_3B),
                Arguments.of(BYTES, 4, CHAR_UTF8_4B, CHAR_UTF8_4B),
                Arguments.of(BYTES, 9, repeat(CHAR_UTF8_1B, 10), repeat(CHAR_UTF8_1B, 9)),
                Arguments.of(BYTES, 19, repeat(CHAR_UTF8_2B, 10), repeat(CHAR_UTF8_2B, 9)),
                Arguments.of(BYTES, 29, repeat(CHAR_UTF8_3B, 10), repeat(CHAR_UTF8_3B, 9)),
                Arguments.of(BYTES, 39, repeat(CHAR_UTF8_4B, 10), repeat(CHAR_UTF8_4B, 9)),
                // Grapheme cluster
                Arguments.of(BYTES, 69, CHAR_UTF8_69B, CHAR_UTF8_69B),
                // Will not cut 4 or 15 bytes of the grapheme cluster
                Arguments.of(BYTES, 69 + 4, repeat(CHAR_UTF8_69B, 2), CHAR_UTF8_69B + woman),
                Arguments.of(BYTES, 69 + 15, repeat(CHAR_UTF8_69B, 2), CHAR_UTF8_69B + redHeadWoman),
                // Truncation by UTF-16 code units
                // -------------------------------
                // Empty
                Arguments.of(UTF16_CODE_UNITS, 0, "", ""),
                // Simple name without truncation
                Arguments.of(UTF16_CODE_UNITS, 10, "simple.txt", "simple.txt"),
                // Name starting with dot
                Arguments.of(UTF16_CODE_UNITS, 10, "." + repeat(CHAR_UTF8_1B, 10), "." + repeat(CHAR_UTF8_1B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 10, "." + repeat(CHAR_UTF8_2B, 10), "." + repeat(CHAR_UTF8_2B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 10, "." + repeat(CHAR_UTF8_3B, 10), "." + repeat(CHAR_UTF8_3B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 20, "." + repeat(CHAR_UTF8_4B, 10), "." + repeat(CHAR_UTF8_4B, 9)),
                // Names with extensions
                Arguments.of(UTF16_CODE_UNITS, 13, repeat(CHAR_UTF8_1B, 10) + ".txt", repeat(CHAR_UTF8_1B, 9) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 13, repeat(CHAR_UTF8_2B, 10) + ".txt", repeat(CHAR_UTF8_2B, 9) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 13, repeat(CHAR_UTF8_3B, 10) + ".txt", repeat(CHAR_UTF8_3B, 9) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 23, repeat(CHAR_UTF8_4B, 10) + ".txt", repeat(CHAR_UTF8_4B, 9) + ".txt"),
                // Names without extensions
                Arguments.of(UTF16_CODE_UNITS, 1, CHAR_UTF8_1B, CHAR_UTF8_1B),
                Arguments.of(UTF16_CODE_UNITS, 1, CHAR_UTF8_2B, CHAR_UTF8_2B),
                Arguments.of(UTF16_CODE_UNITS, 1, CHAR_UTF8_3B, CHAR_UTF8_3B),
                Arguments.of(UTF16_CODE_UNITS, 2, CHAR_UTF8_4B, CHAR_UTF8_4B),
                Arguments.of(UTF16_CODE_UNITS, 9, repeat(CHAR_UTF8_1B, 10), repeat(CHAR_UTF8_1B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 9, repeat(CHAR_UTF8_2B, 10), repeat(CHAR_UTF8_2B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 9, repeat(CHAR_UTF8_3B, 10), repeat(CHAR_UTF8_3B, 9)),
                Arguments.of(UTF16_CODE_UNITS, 19, repeat(CHAR_UTF8_4B, 10), repeat(CHAR_UTF8_4B, 9)),
                // Grapheme cluster
                Arguments.of(UTF16_CODE_UNITS, 31, CHAR_UTF8_69B, CHAR_UTF8_69B),
                // Will not cut 2 or 7 UTF-16 code units of the grapheme cluster
                Arguments.of(UTF16_CODE_UNITS, 31 + 2, repeat(CHAR_UTF8_69B, 2), CHAR_UTF8_69B + woman),
                Arguments.of(UTF16_CODE_UNITS, 31 + 7, repeat(CHAR_UTF8_69B, 2), CHAR_UTF8_69B + redHeadWoman));
    }

    static Stream<Arguments> testNameLengthStrategyTruncate_Throws() {
        final Stream<Arguments> common = Stream.of(
                // Encoding issues
                Arguments.of(BYTES, 10, "café", US_ASCII, "US-ASCII"),
                Arguments.of(UTF16_CODE_UNITS, 10, "\uD800.txt", UTF_8, "UTF-16"),
                Arguments.of(UTF16_CODE_UNITS, 10, "\uDC00.txt", UTF_8, "UTF-16"),
                // Extension too long
                Arguments.of(BYTES, 4, "a.txt", UTF_8, "extension"),
                Arguments.of(UTF16_CODE_UNITS, 4, "a.txt", UTF_8, "extension"),
                // Limit too small
                Arguments.of(BYTES, 3, CHAR_UTF8_4B, UTF_8, "truncated to 1 character"),
                Arguments.of(UTF16_CODE_UNITS, 1, CHAR_UTF8_4B, UTF_8, "truncated to 1 character"));
        return SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_19)
                ? common
                : Stream.concat(
                        common,
                        // In JDK 20+ the grapheme cluster CHAR_UTF8_69B is treated as a single character,
                        // so cannot be truncated to 2 or 7 code units
                        Stream.of(
                                Arguments.of(BYTES, 68, CHAR_UTF8_69B, UTF_8, "truncated to 29 characters"),
                                Arguments.of(UTF16_CODE_UNITS, 30, CHAR_UTF8_69B, UTF_8, "truncated to 30 characters")));
    }

    private String parseXmlRootValue(final Path xmlPath, final Charset charset) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (BufferedReader reader = Files.newBufferedReader(xmlPath, charset)) {
            final Document document = builder.parse(new InputSource(reader));
            return document.getDocumentElement().getTextContent();
        }
    }

    private String parseXmlRootValue(final String xmlString) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = builder.parse(new InputSource(new StringReader(xmlString)));
        return document.getDocumentElement().getTextContent();
    }

    @Test
    void testGetBlockSize() {
        assertTrue(FileSystem.getCurrent().getBlockSize() >= 0);
    }

    @Test
    void testGetCurrent() {
        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals(FileSystem.WINDOWS, FileSystem.getCurrent());
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertEquals(FileSystem.LINUX, FileSystem.getCurrent());
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            assertEquals(FileSystem.MAC_OSX, FileSystem.getCurrent());
        }
    }

    @Test
    void testGetIllegalFileNameChars() {
        final FileSystem current = FileSystem.getCurrent();
        assertNotSame(current.getIllegalFileNameChars(), current.getIllegalFileNameChars());
    }

    @Test
    void testGetNameSeparator() {
        final FileSystem current = FileSystem.getCurrent();
        assertEquals(SystemProperties.getFileSeparator(), Character.toString(current.getNameSeparator()));
    }

    @ParameterizedTest
    @EnumSource(FileSystem.class)
    void testIsLegalName(final FileSystem fs) {
        assertFalse(fs.isLegalFileName(""), fs.name()); // Empty is always illegal
        assertFalse(fs.isLegalFileName(null), fs.name()); // null is always illegal
        assertFalse(fs.isLegalFileName("\0"), fs.name()); // Assume NUL is always illegal
        assertTrue(fs.isLegalFileName("0"), fs.name()); // Assume simple name always legal
        for (final String candidate : fs.getReservedFileNames()) {
            // Reserved file names are not legal
            assertFalse(fs.isLegalFileName(candidate), candidate);
        }
    }

    @Test
    void testIsLegalName_Encoding() {
        assertFalse(FileSystem.GENERIC.isLegalFileName(FILE_NAME_255_BYTES_UTF8_3B, US_ASCII), "US-ASCII cannot represent all chars");
        assertTrue(FileSystem.GENERIC.isLegalFileName(FILE_NAME_255_BYTES_UTF8_3B, UTF_8), "UTF-8 can represent all chars");
    }

    @ParameterizedTest(name = "{index}: {0} with charset {2}")
    @MethodSource
    void testIsLegalName_Length(final FileSystem fs, final String nameAtLimit, final Charset charset) {
        assertTrue(fs.isLegalFileName(nameAtLimit, charset), fs.name() + " length at limit");
        final String nameOverLimit = nameAtLimit + "a";
        assertFalse(fs.isLegalFileName(nameOverLimit, charset), fs.name() + " length over limit");
    }

    @Test
    void testIsReservedFileName() {
        for (final FileSystem fs : FileSystem.values()) {
            for (final String candidate : fs.getReservedFileNames()) {
                assertTrue(fs.isReservedFileName(candidate));
            }
        }
    }

    @Test
    void testIsReservedFileNameOnWindows() {
        final FileSystem fs = FileSystem.WINDOWS;
        for (final String candidate : fs.getReservedFileNames()) {
            // System.out.printf("Reserved %s exists: %s%n", candidate, Files.exists(Paths.get(candidate)));
            assertTrue(fs.isReservedFileName(candidate));
            assertTrue(fs.isReservedFileName(candidate + ".txt"), candidate);
        }

// This can hang when trying to create files for some reserved names, but it is interesting to keep
//
//        for (final String candidate : fs.getReservedFileNames()) {
//            System.out.printf("Testing %s%n", candidate);
//            assertTrue(fs.isReservedFileName(candidate));
//            final Path path = Paths.get(candidate);
//            final boolean exists = Files.exists(path);
//            try {
//                PathUtils.writeString(path, "Hello World!", StandardCharsets.UTF_8);
//            } catch (IOException ignored) {
//                // Asking to create a reserved file either:
//                // - Throws an exception, for example "AUX"
//                // - Is a NOOP, for example "COM3"
//            }
//            assertEquals(exists, Files.exists(path), path.toString());
//        }
    }

    @Test
    void testMaxNameLength_MatchesRealSystem(@TempDir final Path tempDir) {
        final FileSystem fs = FileSystem.getCurrent();
        final String[] validNames;
        switch (fs) {
        case MAC_OSX:
        case LINUX:
            // Names with 255 UTF-8 bytes are legal
            // @formatter:off
                validNames = new String[] {
                    FILE_NAME_255_BYTES_UTF8_1B,
                    FILE_NAME_255_BYTES_UTF8_2B,
                    FILE_NAME_255_BYTES_UTF8_3B,
                    FILE_NAME_255_BYTES_UTF8_4B
                };
                // @formatter:on
            break;
        case WINDOWS:
            // Names with 255 UTF-16 code units are legal
            // @formatter:off
                validNames = new String[] {
                    FILE_NAME_255_CHARS_UTF8_1B,
                    FILE_NAME_255_CHARS_UTF8_2B,
                    FILE_NAME_255_CHARS_UTF8_3B,
                    FILE_NAME_255_CHARS_UTF8_4B
                };
                // @formatter:on
            break;
        default:
            throw new IllegalStateException("Unexpected value: " + fs);
        }
        int failures = 0;
        for (final String fileName : validNames) {
            // 1) OS should accept names at the documented limit.
            assertDoesNotThrow(() -> createAndDelete(tempDir, fileName), "OS should accept max-length name: " + fileName);
            // 2) Library should consider them legal.
            assertTrue(fs.isLegalFileName(fileName, UTF_8), "Commons IO should accept max-length name: " + fileName);
            // 3) For “one over” the limit: Commons IO must reject; OS may or may not enforce strictly.
            final String tooLongName = fileName + "a";
            // Library contract: must be illegal.
            assertFalse(fs.isLegalFileName(tooLongName, UTF_8), "Commons IO should reject too-long name: " + tooLongName);
            // OS behavior: may or may not reject.
            try {
                createAndDelete(tempDir, tooLongName);
            } catch (final Throwable e) {
                failures++;
                assertInstanceOf(IOException.class, e, "OS rejects too-long name");
            }
        }
        // On Linux and Windows the API and the filesystem measure name length
        // in the same unit as the underlying limit (255 bytes on Linux/most POSIX,
        // 255 UTF-16 code units on Windows).
        // So all “too-long” variants should fail.
        //
        // macOS is trickier because the API and filesystem limits don’t always match:
        //
        // - POSIX API layer (getdirentries/readdir): 1023 bytes per component since macOS 10.5.
        // https://man.freebsd.org/cgi/man.cgi?query=dir&sektion=5&apropos=0&manpath=macOS+15.6
        // - HFS+: enforces 255 UTF-16 code units per component.
        // - APFS: enforces 255 UTF-8 bytes per component.
        //
        // Because of this mismatch, depending on which filesystem is mounted,
        // either all or only FILE_NAME_255BYTES_UTF8_1B + "a" will be rejected.
        if (SystemUtils.IS_OS_MAC_OSX) {
            assertTrue(failures >= 1, "Expected at least one too-long name rejected, got " + failures);
        } else {
            assertEquals(4, failures, "All too-long names were rejected");
        }
    }

    @ParameterizedTest(name = "{index}: {0} truncates {1} to {2}")
    @MethodSource
    void testNameLengthStrategyTruncate_Succeeds(final NameLengthStrategy strategy, final int limit, final String input, final String expected) {
        final CharSequence out = strategy.truncate(input, limit, UTF_8);
        assertEquals(expected, out.toString(), strategy.name() + " truncates to limit");
    }

    @ParameterizedTest(name = "{index}: {0} truncates {2} with limit {1} throws")
    @MethodSource
    void testNameLengthStrategyTruncate_Throws(final NameLengthStrategy strategy, final int limit, final String input, final Charset charset,
            final String message) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> strategy.truncate(input, limit, charset));
        final String exMessage = ex.getMessage();
        assertTrue(exMessage.contains(message), "ex message contains " + message + ": " + exMessage);
    }

    @Test
    void testReplacementWithNUL() {
        for (final FileSystem fs : FileSystem.values()) {
            try {
                fs.toLegalFileName("Test", '\0'); // Assume NUL is always illegal
            } catch (final IllegalArgumentException iae) {
                assertTrue(iae.getMessage().startsWith("The replacement character '\\0'"), iae.getMessage());
            }
        }
    }

    @Test
    void testSorted() {
        for (final FileSystem fs : FileSystem.values()) {
            final char[] chars = fs.getIllegalFileNameChars();
            for (int i = 0; i < chars.length - 1; i++) {
                assertTrue(chars[i] < chars[i + 1], fs.name());
            }
        }
    }

    @Test
    void testSupportsDriveLetter() {
        assertTrue(FileSystem.WINDOWS.supportsDriveLetter());
        assertFalse(FileSystem.GENERIC.supportsDriveLetter());
        assertFalse(FileSystem.LINUX.supportsDriveLetter());
        assertFalse(FileSystem.MAC_OSX.supportsDriveLetter());
    }

    @Test
    void testToLegalFileNameWindows() {
        final FileSystem fs = FileSystem.WINDOWS;
        final char replacement = '-';
        for (char i = 0; i < 32; i++) {
            assertEquals(replacement, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        final char[] illegal = { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        for (char i = 0; i < illegal.length; i++) {
            assertEquals(replacement, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = 'a'; i < 'z'; i++) {
            assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = 'A'; i < 'Z'; i++) {
            assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        for (char i = '0'; i < '9'; i++) {
            assertEquals(i, fs.toLegalFileName(String.valueOf(i), replacement).charAt(0));
        }
        // Null and empty
        assertThrows(NullPointerException.class, () -> fs.toLegalFileName(null, '_'));
        assertThrows(IllegalArgumentException.class, () -> fs.toLegalFileName("", '_'));
        // Illegal replacement
        assertThrows(IllegalArgumentException.class, () -> fs.toLegalFileName("test", '\0'));
        assertThrows(IllegalArgumentException.class, () -> fs.toLegalFileName("test", ':'));
    }

    @ParameterizedTest
    @EnumSource(FileSystem.class)
    void testXmlRoundtrip(final FileSystem fs, @TempDir final Path tempDir) throws Exception {
        final Charset charset = Charset.defaultCharset();
        assertEquals("a", fs.toLegalFileName("a", '_', charset));
        assertEquals("abcdefghijklmno", fs.toLegalFileName("abcdefghijklmno", '_', charset));
        assertEquals("\u4F60\u597D\u55CE", fs.toLegalFileName("\u4F60\u597D\u55CE", '_', charset));
        assertEquals("\u2713\u2714", fs.toLegalFileName("\u2713\u2714", '_', charset));
        assertEquals("\uD83D\uDE80\u2728\uD83C\uDF89", fs.toLegalFileName("\uD83D\uDE80\u2728\uD83C\uDF89", '_', charset));
        assertEquals("\uD83D\uDE03", fs.toLegalFileName("\uD83D\uDE03", '_', charset));
        assertEquals("\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03",
                fs.toLegalFileName("\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03\uD83D\uDE03", '_', charset));
        for (int i = 1; i <= 100; i++) {
            final String name1 = fs.toLegalFileName(StringUtils.repeat("🦊", i), '_', charset);
            assertNotNull(name1);
            final byte[] name1Bytes = name1.getBytes();
            final String xmlString1 = toXmlString(name1, charset);
            final Path path = tempDir.resolve(name1);
            Files.write(path, xmlString1.getBytes(charset));
            final String xmlFromPath = parseXmlRootValue(path, charset);
            assertEquals(name1, xmlFromPath, "i =  " + i);
            final String name2 = new String(name1Bytes, charset);
            assertEquals(name1, name2);
            final String xmlString2 = toXmlString(name2, charset);
            assertEquals(xmlString1, xmlString2);
            final String parsedValue = Objects.toString(parseXmlRootValue(xmlString2), "");
            assertEquals(name1, parsedValue, "i =  " + i);
            assertEquals(name2, parsedValue, "i =  " + i);
        }
        for (int i = 1; i <= 100; i++) {
            final String name1 = fs.toLegalFileName(fs.getNameLengthStrategy().truncate(
                    "👩🏻‍👨🏻‍👦🏻‍👦🏻👩🏼‍👨🏼‍👦🏼‍👦🏼👩🏽‍👨🏽‍👦🏽‍👦🏽👩🏾‍👨🏾‍👦🏾‍👦🏾👩🏿‍👨🏿‍👦🏿‍👦🏿👩🏻‍👨🏻‍👦🏻‍👦🏻👩🏼‍👨🏼‍👦🏼‍👦🏼👩🏽‍👨🏽‍👦🏽‍👦🏽👩🏾‍👨🏾‍👦🏾‍👦🏾👩🏿‍👨🏿‍👦🏿‍👦🏿",
                    // TODO hack 100: truncate blows up when it can't.
                    100 + i, charset), '_', charset);
            assertNotNull(name1);
            final byte[] name1Bytes = name1.getBytes();
            final String xmlString1 = toXmlString(name1, charset);
            final Path path = tempDir.resolve(name1);
            Files.write(path, xmlString1.getBytes(charset));
            final String xmlFromPath = parseXmlRootValue(path, charset);
            assertEquals(name1, xmlFromPath, "i =  " + i);
            final String name2 = new String(name1Bytes, charset);
            assertEquals(name1, name2);
            final String xmlString2 = toXmlString(name2, charset);
            assertEquals(xmlString1, xmlString2);
            final String parsedValue = Objects.toString(parseXmlRootValue(xmlString2), "");
            assertEquals(name1, parsedValue, "i =  " + i);
            assertEquals(name2, parsedValue, "i =  " + i);
        }
    }

    private String toXmlString(final String s, final Charset charset) {
        return String.format("<?xml version=\"1.0\" encoding=\"%s\"?><data>%s</data>", charset.name(), s);
    }

}
