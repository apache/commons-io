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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileSystem.NameLengthStrategy.BYTES;
import static org.apache.commons.io.FileSystem.NameLengthStrategy.UTF16_CODE_UNITS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileSystem.NameLengthStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link FileSystem}.
 */
class FileSystemTest {

    /** A single UTF-8 character that encodes to 3 bytes. */
    private static final String UTF8_3BYTE_CHAR = "★";

    /** File name of 255 ASCII characters: 255 bytes == 255 UTF-16 chars. */
    private static final String FILE_NAME_255_ASCII = StringUtils.repeat("a", 255);

    /** File name of 85 UTF-16 chars, each 3 bytes in UTF-8: total 255 bytes. */
    private static final String FILE_NAME_255_UTF8_BYTES = StringUtils.repeat(UTF8_3BYTE_CHAR, 85);

    /** File name of 255 UTF-16 chars, each 3 bytes in UTF-8: total 765 bytes. */
    private static final String FILE_NAME_255_UTF16_CODE_UNITS = StringUtils.repeat(UTF8_3BYTE_CHAR, 255);

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

    static Stream<Arguments> testIsLegalName_Length() {
        return Stream.of(
                Arguments.of(FileSystem.GENERIC, StringUtils.repeat(FILE_NAME_255_ASCII, 4), UTF_8),
                Arguments.of(FileSystem.GENERIC, StringUtils.repeat(FILE_NAME_255_UTF8_BYTES, 4), UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_ASCII, UTF_8),
                Arguments.of(FileSystem.LINUX, FILE_NAME_255_UTF8_BYTES, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_ASCII, UTF_8),
                Arguments.of(FileSystem.MAC_OSX, FILE_NAME_255_UTF8_BYTES, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_ASCII, UTF_8),
                Arguments.of(FileSystem.WINDOWS, FILE_NAME_255_UTF16_CODE_UNITS, UTF_8));
    }

    @ParameterizedTest
    @MethodSource
    void testIsLegalName_Length(FileSystem fs, String nameAtLimit, Charset charset) {
        assertTrue(fs.isLegalFileName(nameAtLimit, charset), fs.name() + " length at limit");
        final String nameOverLimit = nameAtLimit + "a";
        assertFalse(fs.isLegalFileName(nameOverLimit, charset), fs.name() + " length over limit");
    }

    @Test
    void testIsLegalName_Encoding() {
        assertFalse(FileSystem.GENERIC.isLegalFileName(FILE_NAME_255_UTF8_BYTES, US_ASCII), "US-ASCII cannot represent all chars");
        assertTrue(FileSystem.GENERIC.isLegalFileName(FILE_NAME_255_UTF8_BYTES, UTF_8), "UTF-8 can represent all chars");
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
    void testMaxNameLength_MatchesRealSystem(@TempDir Path tempDir) {
        final FileSystem fs = FileSystem.getCurrent();
        final String[] validNames;
        switch (fs) {
            case LINUX:
                validNames = new String[] { FILE_NAME_255_ASCII, FILE_NAME_255_UTF8_BYTES };
                break;
            case MAC_OSX:
            case WINDOWS:
                validNames = new String[] { FILE_NAME_255_ASCII, FILE_NAME_255_UTF16_CODE_UNITS};
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + fs);
        }
        for (final String fileName : validNames) {
            assertDoesNotThrow(() -> testFileName(tempDir, fileName), "OS accepts max length name");
            assertTrue(fs.isLegalFileName(fileName, UTF_8), "Commons IO accepts max length name");
            final String tooLongName = fileName + "a";
            assertThrows(IOException.class, () -> testFileName(tempDir, tooLongName), "OS rejects too-long name");
            assertFalse(fs.isLegalFileName(tooLongName, UTF_8), "Commons IO rejects too-long name");
        }
    }

    private void testFileName(Path tempDir, String fileName) throws IOException {
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

    static Stream<Arguments> testNameLengthStrategyTruncate() {
        return Stream.of(
                Arguments.of(BYTES, 255, "simple.txt", "simple.txt"),
                Arguments.of(BYTES, 255, "." + FILE_NAME_255_ASCII, "." + FILE_NAME_255_ASCII.substring(0, 254)),
                Arguments.of(BYTES, 255, FILE_NAME_255_ASCII + "aaaa", FILE_NAME_255_ASCII),
                Arguments.of(BYTES, 255, FILE_NAME_255_ASCII + ".txt", FILE_NAME_255_ASCII.substring(0, 251) + ".txt"),
                Arguments.of(BYTES, 255, FILE_NAME_255_UTF8_BYTES + "aaaa", FILE_NAME_255_UTF8_BYTES),
                Arguments.of(
                        BYTES,
                        255,
                        FILE_NAME_255_UTF8_BYTES + ".txt",
                        FILE_NAME_255_UTF8_BYTES.substring(0, 83) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 255, "simple.txt", "simple.txt"),
                Arguments.of(UTF16_CODE_UNITS, 255, "." + FILE_NAME_255_ASCII, "." + FILE_NAME_255_ASCII.substring(0, 254)),
                Arguments.of(
                        UTF16_CODE_UNITS, 255, FILE_NAME_255_ASCII + ".txt", FILE_NAME_255_ASCII.substring(0, 251) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 255, FILE_NAME_255_ASCII + "aaaa", FILE_NAME_255_ASCII),
                Arguments.of(
                        UTF16_CODE_UNITS,
                        255,
                        FILE_NAME_255_UTF16_CODE_UNITS + ".txt",
                        FILE_NAME_255_UTF16_CODE_UNITS.substring(0, 251) + ".txt"),
                Arguments.of(UTF16_CODE_UNITS, 255, FILE_NAME_255_UTF16_CODE_UNITS + "aaaa", FILE_NAME_255_UTF16_CODE_UNITS),
                Arguments.of(
                        UTF16_CODE_UNITS,
                        7,
                        "😀😀.txt" // each emoji is 2 UTF-16 chars
                        ,
                        "😀.txt"),
                // High surrogate not followed by low surrogate (invalid UTF-16 sequence)
                Arguments.of(UTF16_CODE_UNITS, 5, "\uD83Da.txt", "\uD83D.txt"));
    }

    @ParameterizedTest(name = "{index}: {0} truncates {1} to {2}")
    @MethodSource
    void testNameLengthStrategyTruncate(NameLengthStrategy strategy, int limit, String input, String expected) {
        final CharSequence out = strategy.truncate(input, limit, UTF_8);
        assertEquals(expected, out.toString(), strategy.name() + " truncates to limit");
    }

    static Stream<Arguments> testTruncateByBytes_Succeeds() {
        return Stream.of(
                // ASCII (UTF-8) — fits
                Arguments.of("ASCII fits (UTF-8)", "hello", UTF_8, 5, "hello"),
                // ASCII (UTF-8) — truncate
                Arguments.of("ASCII truncated (UTF-8)", "hello", UTF_8, 4, "hell"),
                // Empty input
                Arguments.of("Empty input", "", UTF_8, 10, ""),
                // Zero budget → empty
                Arguments.of("Zero-byte limit", "anything", UTF_8, 0, ""),
                // UTF-8: 2-byte char exact fit
                Arguments.of("UTF-8: 2-byte char exact fit", "é", UTF_8, 2, "é"),
                // UTF-8: 2-byte + ASCII; 3 fits both, 2 fits only 2-byte, 1 fits neither
                Arguments.of("UTF-8: 2-byte + ASCII, both fit", "éa", UTF_8, 3, "éa"),
                Arguments.of("UTF-8: 2-byte + ASCII, truncate before ASCII", "éa", UTF_8, 2, "é"),
                Arguments.of("UTF-8: 2-byte + ASCII, truncate all", "éa", UTF_8, 1, ""),
                // UTF-8: emoji + ASCII; 5 fits both, 4 fits only emoji, 3 fits neither
                Arguments.of("UTF-8: emoji + ASCII, both fit", "😀a", UTF_8, 5, "😀a"),
                Arguments.of("UTF-8: emoji + ASCII, truncate before ASCII", "😀a", UTF_8, 4, "😀"),
                Arguments.of("UTF-8: emoji + ASCII, truncate all", "😀a", UTF_8, 3, ""),
                // Large limit (fast-path should accept)
                Arguments.of("Large limit fast-path (UTF-8)", "ok", UTF_8, 8, "ok"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource
    void testTruncateByBytes_Succeeds(String caseName, String input, Charset charset, int maxBytes, String expected) {
        final CharSequence out = BYTES.truncate(input, maxBytes, charset);
        // If your contract returns null for null input, this still works; otherwise adjust.
        assertEquals(expected, Objects.toString(out, null), caseName);
    }

    @Test
    void testTruncateByBytes_UnmappableAsciiThrows() {
        final String in = "café"; // contains 'é' (not in ASCII)
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> BYTES.truncate(in, 100, US_ASCII));
        assertTrue(ex.getMessage().contains(US_ASCII.name()), "ex message contains charset name");
    }

    @ParameterizedTest
    @EnumSource(NameLengthStrategy.class)
    void testNameLengthStrategyTruncate_ExtensionTooLong(NameLengthStrategy strategy) {
        final String in = "a.txt"; // ".txt" is 4 chars
        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> strategy.truncate(in, 4, UTF_8));
        assertTrue(ex.getMessage().contains("extension"), "ex message contains 'extension'");
    }
}
