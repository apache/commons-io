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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Abstracts an OS' file system details, currently supporting the single use case of converting a file name String to a
 * legal file name with {@link #toLegalFileName(String, char)}.
 * <p>
 * The starting point of any operation is {@link #getCurrent()} which gets you the enum for the file system that matches
 * the OS hosting the running JVM.
 * </p>
 *
 * @since 2.7
 */
public enum FileSystem {

    /**
     * Generic file system.
     */
    GENERIC(4096, false, false, 1020, 1024 * 1024, new int[] {
            // @formatter:off
            // ASCII NUL
            0
            // @formatter:on
    }, new String[] {}, false, false, '/', NameLengthStrategy.BYTES),

    /**
     * Linux file system.
     */
    LINUX(8192, true, true, 255, 4096, new int[] {
            // KEEP THIS ARRAY SORTED!
            // @formatter:off
            // ASCII NUL
            0,
             '/'
            // @formatter:on
    }, new String[] {}, false, false, '/', NameLengthStrategy.BYTES),

    /**
     * MacOS file system.
     */
    MAC_OSX(4096, true, true, 255, 1024, new int[] {
            // KEEP THIS ARRAY SORTED!
            // @formatter:off
            // ASCII NUL
            0,
            '/',
             ':'
            // @formatter:on
    }, new String[] {}, false, false, '/', NameLengthStrategy.BYTES),

    /**
     * Windows file system.
     * <p>
     * The reserved characters are defined in the
     * <a href="https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file">Naming Conventions
     * (microsoft.com)</a>.
     * </p>
     *
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file">Naming Conventions
     *      (microsoft.com)</a>
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilea#consoles">
     *      CreateFileA function - Consoles (microsoft.com)</a>
     */
    // @formatter:off
    WINDOWS(4096, false, true,
            255, 32767, // KEEP THIS ARRAY SORTED!
            new int[] {
                    // KEEP THIS ARRAY SORTED!
                    // ASCII NUL
                    0,
                    // 1-31 may be allowed in file streams
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                    29, 30, 31,
                    '"', '*', '/', ':', '<', '>', '?', '\\', '|'
            }, new String[] {
                    "AUX",
                    "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                    "COM\u00b2", "COM\u00b3", "COM\u00b9", // Superscript 2 3 1 in that order
                    "CON", "CONIN$", "CONOUT$",
                    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
                    "LPT\u00b2", "LPT\u00b3", "LPT\u00b9", // Superscript 2 3 1 in that order
                    "NUL", "PRN"
            }, true, true, '\\', NameLengthStrategy.UTF16_CODE_UNITS);
    // @formatter:on

    /**
     * <p>
     * Is {@code true} if this is Linux.
     * </p>
     * <p>
     * The field will return {@code false} if {@code OS_NAME} is {@code null}.
     * </p>
     */
    private static final boolean IS_OS_LINUX = getOsMatchesName("Linux");

    /**
     * <p>
     * Is {@code true} if this is Mac.
     * </p>
     * <p>
     * The field will return {@code false} if {@code OS_NAME} is {@code null}.
     * </p>
     */
    private static final boolean IS_OS_MAC = getOsMatchesName("Mac");

    /**
     * The prefix String for all Windows OS.
     */
    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

    /**
     * <p>
     * Is {@code true} if this is Windows.
     * </p>
     * <p>
     * The field will return {@code false} if {@code OS_NAME} is {@code null}.
     * </p>
     */
    private static final boolean IS_OS_WINDOWS = getOsMatchesName(OS_NAME_WINDOWS_PREFIX);

    /**
     * The current FileSystem.
     */
    private static final FileSystem CURRENT = current();

    /**
     * Gets the current file system.
     *
     * @return the current file system
     */
    private static FileSystem current() {
        if (IS_OS_LINUX) {
            return LINUX;
        }
        if (IS_OS_MAC) {
            return MAC_OSX;
        }
        if (IS_OS_WINDOWS) {
            return WINDOWS;
        }
        return GENERIC;
    }

    /**
     * Gets the current file system.
     *
     * @return the current file system
     */
    public static FileSystem getCurrent() {
        return CURRENT;
    }

    /**
     * Decides if the operating system matches.
     *
     * @param osNamePrefix
     *            the prefix for the os name
     * @return true if matches, or false if not or can't determine
     */
    private static boolean getOsMatchesName(final String osNamePrefix) {
        return isOsNameMatch(getSystemProperty("os.name"), osNamePrefix);
    }

    /**
     * <p>
     * Gets a System property, defaulting to {@code null} if the property cannot be read.
     * </p>
     * <p>
     * If a {@link SecurityException} is caught, the return value is {@code null} and a message is written to
     * {@code System.err}.
     * </p>
     *
     * @param property
     *            the system property name
     * @return the system property value or {@code null} if a security problem occurs
     */
    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            System.err.println("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

    /*
     * Finds the index of the first dot in a CharSequence.
     */
    private static int indexOfFirstDot(final CharSequence cs) {
        if (cs instanceof String) {
            return ((String) cs).indexOf('.');
        }
        for (int i = 0; i < cs.length(); i++) {
            if (cs.charAt(i) == '.') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Decides if the operating system matches.
     * <p>
     * This method is package private instead of private to support unit test invocation.
     * </p>
     *
     * @param osName
     *            the actual OS name
     * @param osNamePrefix
     *            the prefix for the expected OS name
     * @return true if matches, or false if not or can't determine
     */
    private static boolean isOsNameMatch(final String osName, final String osNamePrefix) {
        if (osName == null) {
            return false;
        }
        return osName.toUpperCase(Locale.ROOT).startsWith(osNamePrefix.toUpperCase(Locale.ROOT));
    }

    /**
     * Null-safe replace.
     *
     * @param path the path to be changed, null ignored.
     * @param oldChar the old character.
     * @param newChar the new character.
     * @return the new path.
     */
    private static String replace(final String path, final char oldChar, final char newChar) {
        return path == null ? null : path.replace(oldChar, newChar);
    }

    private final int blockSize;
    private final boolean casePreserving;
    private final boolean caseSensitive;
    private final int[] illegalFileNameChars;
    private final int maxFileNameLength;
    private final int maxPathLength;
    private final String[] reservedFileNames;
    private final boolean reservedFileNamesExtensions;
    private final boolean supportsDriveLetter;
    private final char nameSeparator;
    private final char nameSeparatorOther;
    private final NameLengthStrategy nameLengthStrategy;

    /**
     * Constructs a new instance.
     *
     * @param blockSize file allocation block size in bytes.
     * @param caseSensitive Whether this file system is case-sensitive.
     * @param casePreserving Whether this file system is case-preserving.
     * @param maxFileLength The maximum length for file names. The file name does not include folders.
     * @param maxPathLength The maximum length of the path to a file. This can include folders.
     * @param illegalFileNameChars Illegal characters for this file system.
     * @param reservedFileNames The reserved file names.
     * @param reservedFileNamesExtensions TODO
     * @param supportsDriveLetter Whether this file system support driver letters.
     * @param nameSeparator The name separator, '\\' on Windows, '/' on Linux.
     * @param nameLengthStrategy The strategy for measuring and truncating file and path names.
     */
    FileSystem(final int blockSize, final boolean caseSensitive, final boolean casePreserving,
        final int maxFileLength, final int maxPathLength, final int[] illegalFileNameChars,
        final String[] reservedFileNames, final boolean reservedFileNamesExtensions, final boolean supportsDriveLetter,
        final char nameSeparator, final NameLengthStrategy nameLengthStrategy) {
        this.blockSize = blockSize;
        this.maxFileNameLength = maxFileLength;
        this.maxPathLength = maxPathLength;
        this.illegalFileNameChars = Objects.requireNonNull(illegalFileNameChars, "illegalFileNameChars");
        this.reservedFileNames = Objects.requireNonNull(reservedFileNames, "reservedFileNames");
        //Arrays.sort(this.reservedFileNames);
        this.reservedFileNamesExtensions = reservedFileNamesExtensions;
        this.caseSensitive = caseSensitive;
        this.casePreserving = casePreserving;
        this.supportsDriveLetter = supportsDriveLetter;
        this.nameSeparator = nameSeparator;
        this.nameSeparatorOther = FilenameUtils.flipSeparator(nameSeparator);
        this.nameLengthStrategy = nameLengthStrategy;
    }

    /**
     * Gets the file allocation block size in bytes.
     * @return the file allocation block size in bytes.
     * @since 2.12.0
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Gets a cloned copy of the illegal characters for this file system.
     *
     * @return the illegal characters for this file system.
     */
    public char[] getIllegalFileNameChars() {
        final char[] chars = new char[illegalFileNameChars.length];
        for (int i = 0; i < illegalFileNameChars.length; i++) {
            chars[i] = (char) illegalFileNameChars[i];
        }
        return chars;
    }

    /**
     * Gets a cloned copy of the illegal code points for this file system.
     *
     * @return the illegal code points for this file system.
     * @since 2.12.0
     */
    public int[] getIllegalFileNameCodePoints() {
        return this.illegalFileNameChars.clone();
    }

    /**
     * Gets the maximum length for file names (excluding any folder path).
     *
     * <p>This limit applies only to the file name itself, excluding any parent
     * directories.</p>
     *
     * <p>The value is expressed in Java {@code char} units (UTF-16 code units).</p>
     *
     * <p><strong>Note:</strong> Because many file systems enforce limits in
     * <em>bytes</em> using a specific encoding rather than in UTF-16 code
     * units, a name that fits this limit may still be rejected by the
     * underlying file system.</p>
     *
     * <p>Use {@link #isLegalFileName} to check whether a given name is valid
     * for the current file system and charset.</p>
     *
     * <p>However, any file name longer than this limit is guaranteed to be
     * invalid on the current file system.</p>
     *
     * @return the maximum file name length in characters.
     */
    public int getMaxFileNameLength() {
        return maxFileNameLength;
    }

    /**
     * Gets the maximum length for file paths (may include folders).
     *
     * <p>This value is inclusive of all path components and separators.
     * For a limit of each path component see {@link #getMaxFileNameLength()}.</p>
     *
     * <p>The value is expressed in Java {@code char} units (UTF-16 code units)
     * and represents the longest path that can be safely passed to Java
     * {@link java.io.File} and {@link java.nio.file.Path} APIs.</p>
     *
     * <p><strong>Note:</strong> many operating systems and file systems enforce
     * path length limits in <em>bytes</em> using a specific encoding, rather than
     * in UTF-16 code units. As a result, a path that fits within this limit may
     * still be rejected by the underlying platform.</p>
     *
     * <p>Conversely, any path longer than this limit is guaranteed to fail with
     * at least some operating system API calls.</p>
     *
     * @return the maximum file path length in characters.
     */
    public int getMaxPathLength() {
        return maxPathLength;
    }

    /**
     * Gets the name separator, '\\' on Windows, '/' on Linux.
     *
     * @return '\\' on Windows, '/' on Linux.
     * @since 2.12.0
     */
    public char getNameSeparator() {
        return nameSeparator;
    }

    /**
     * Gets a cloned copy of the reserved file names.
     *
     * @return the reserved file names.
     */
    public String[] getReservedFileNames() {
        return reservedFileNames.clone();
    }

    /**
     * Tests whether this file system preserves case.
     *
     * @return Whether this file system preserves case.
     */
    public boolean isCasePreserving() {
        return casePreserving;
    }

    /**
     * Tests whether this file system is case-sensitive.
     *
     * @return Whether this file system is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Tests if the given character is illegal in a file name, {@code false} otherwise.
     *
     * @param c
     *            the character to test
     * @return {@code true} if the given character is illegal in a file name, {@code false} otherwise.
     */
    private boolean isIllegalFileNameChar(final int c) {
        return Arrays.binarySearch(illegalFileNameChars, c) >= 0;
    }

    /**
     * Tests if a candidate file name (without a path) is a legal file name.
     *
     * <p>Takes a file name like {@code "filename.ext"} or {@code "filename"} and checks:</p>
     * <ul>
     * <li>if the file name length is legal</li>
     * <li>if the file name is not a reserved file name</li>
     * <li>if the file name does not contain illegal characters</li>
     * </ul>
     *
     * @param candidate
     *            A candidate file name (without a path) like {@code "filename.ext"} or {@code "filename"}
     * @return {@code true} if the candidate name is legal
     */
    public boolean isLegalFileName(final CharSequence candidate) {
        return isLegalFileName(candidate, Charset.defaultCharset());
    }

    /**
     * Tests if a candidate file name (without a path) is a legal file name.
     *
     * <p>Takes a file name like {@code "filename.ext"} or {@code "filename"} and checks:</p>
     * <ul>
     * <li>if the file name length is legal</li>
     * <li>if the file name is not a reserved file name</li>
     * <li>if the file name does not contain illegal characters</li>
     * </ul>
     *
     * @param candidate
     *            A candidate file name (without a path) like {@code "filename.ext"} or {@code "filename"}
     * @param charset
     *            The charset to use when the file name length is measured in bytes
     * @return {@code true} if the candidate name is legal
     * @since 2.21.0
     */
    public boolean isLegalFileName(final CharSequence candidate, final Charset charset) {
        return candidate != null
                && candidate.length() != 0
                && nameLengthStrategy.isWithinLimit(candidate, getMaxFileNameLength(), charset)
                && !isReservedFileName(candidate)
                && candidate.chars().noneMatch(this::isIllegalFileNameChar);
    }

    /**
     * Tests whether the given string is a reserved file name.
     *
     * @param candidate
     *            the string to test
     * @return {@code true} if the given string is a reserved file name.
     */
    public boolean isReservedFileName(final CharSequence candidate) {
        final CharSequence test = reservedFileNamesExtensions ? trimExtension(candidate) : candidate;
        return Arrays.binarySearch(reservedFileNames, test) >= 0;
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     * @since 2.12.0
     */
    public String normalizeSeparators(final String path) {
        return replace(path, nameSeparatorOther, nameSeparator);
    }

    /**
     * Tests whether this file system support driver letters.
     * <p>
     * Windows supports driver letters as do other operating systems. Whether these other OS's still support Java like
     * OS/2, is a different matter.
     * </p>
     *
     * @return whether this file system support driver letters.
     * @since 2.9.0
     * @see <a href="https://en.wikipedia.org/wiki/Drive_letter_assignment">Operating systems that use drive letter
     *      assignment</a>
     */
    public boolean supportsDriveLetter() {
        return supportsDriveLetter;
    }

    /**
     * Converts a candidate file name (without a path) to a legal file name.
     *
     * <p>Takes a file name like {@code "filename.ext"} or {@code "filename"} and:</p>
     * <ul>
     *     <li>replaces illegal characters by the given replacement character</li>
     *     <li>truncates the name to {@link #getMaxFileNameLength()} if necessary</li>
     * </ul>
     *
     * @param candidate
     *            A candidate file name (without a path) like {@code "filename.ext"} or {@code "filename"}
     * @param replacement
     *            Illegal characters in the candidate name are replaced by this character
     * @return a String without illegal characters
     */
    public String toLegalFileName(final String candidate, final char replacement) {
        return toLegalFileName(candidate, replacement, Charset.defaultCharset());
    }

    /**
     * Converts a candidate file name (without a path) to a legal file name.
     *
     * <p>Takes a file name like {@code "filename.ext"} or {@code "filename"} and:</p>
     * <ul>
     *     <li>replaces illegal characters by the given replacement character</li>
     *     <li>truncates the name to {@link #getMaxFileNameLength()} if necessary</li>
     * </ul>
     *
     * @param candidate
     *            A candidate file name (without a path) like {@code "filename.ext"} or {@code "filename"}
     * @param replacement
     *            Illegal characters in the candidate name are replaced by this character
     * @param charset
     *            The charset to use when the file name length is measured in bytes
     * @return a String without illegal characters
     * @since 2.21.0
     */
    public String toLegalFileName(final String candidate, final char replacement, final Charset charset) {
        Objects.requireNonNull(candidate, "candidate");
        if (candidate.isEmpty()) {
            throw new IllegalArgumentException("The candidate file name is empty");
        }
        if (isIllegalFileNameChar(replacement)) {
            // %s does not work properly with NUL
            throw new IllegalArgumentException(String.format("The replacement character '%s' cannot be one of the %s illegal characters: %s",
                replacement == '\0' ? "\\0" : replacement, name(), Arrays.toString(illegalFileNameChars)));
        }
        final CharSequence truncated = nameLengthStrategy.truncate(candidate, getMaxFileNameLength(), charset);
        final int[] array = truncated.chars().map(i -> isIllegalFileNameChar(i) ? replacement : i).toArray();
        return new String(array, 0, array.length);
    }

    CharSequence trimExtension(final CharSequence cs) {
        final int index = indexOfFirstDot(cs);
        // An initial dot is not an extension
        return index < 1 ? cs : cs.subSequence(0, index);
    }

    /**
     * Strategy for measuring and truncating file or path names in different units.
     * Implementations measure length and can truncate to a specified limit.
     */
    enum NameLengthStrategy {
        /** Length measured as encoded bytes. */
        BYTES {
            @Override
            int getLength(final CharSequence value, final Charset charset) {
                final CharsetEncoder enc = charset.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
                try {
                    return enc.encode(CharBuffer.wrap(value)).remaining();
                } catch (CharacterCodingException e) {
                    // Unencodable, does not fit any byte limit.
                    return Integer.MAX_VALUE;
                }
            }

            @Override
            CharSequence truncate(final CharSequence value, final int limit, final Charset charset) {
                final CharsetEncoder encoder = charset.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);

                if (!encoder.canEncode(value)) {
                    throw new IllegalArgumentException(
                            "The value " + value + " cannot be encoded using " + charset.name());
                }

                // Fast path: if even the worst-case expansion fits, we're done.
                if (value.length() <= Math.floor(limit / encoder.maxBytesPerChar())) {
                    return value;
                }

                // Slow path: encode into a fixed-size byte buffer.
                // 1. Compute length of extension in bytes (if any).
                final int extensionStart = indexOfFirstDot(value);
                final boolean hasExtension = extensionStart > 0;
                final int extensionLength = hasExtension ? getLength(value.subSequence(extensionStart, value.length()), charset) : 0;
                if (hasExtension && extensionLength >= limit) {
                    // Extension itself does not fit
                    throw new IllegalArgumentException(
                            "The extension of " + value + " is too long to fit within " + limit + " bytes");
                }

                // 2. Compute the byte size of the non-extension part.
                final ByteBuffer byteBuffer = ByteBuffer.allocate(limit - extensionLength);
                final CharBuffer charBuffer = CharBuffer.allocate(value.length());

                // Encode until the first character that would exceed the byte budget.
                charBuffer.append(value, 0, hasExtension ? extensionStart : value.length());
                charBuffer.rewind();
                final CoderResult cr = encoder.encode(charBuffer, byteBuffer, true);

                if (cr.isUnderflow()) {
                    // Entire candidate fit within maxFileNameLength bytes.
                    return value;
                }

                // We ran out of space mid-encode: append the extension (if any) and return the charBuffer.
                if (hasExtension) {
                    charBuffer.append(value, extensionStart, value.length());
                }
                charBuffer.flip();
                return charBuffer;
            }
        },

        /** Length measured as UTF-16 code units (i.e., {@code CharSequence.length()}). */
        UTF16_CODE_UNITS {
            @Override
            int getLength(final CharSequence value, final Charset charset) {
                return value.length();
            }

            @Override
            CharSequence truncate(final CharSequence value, final int limit, final Charset charset) {
                // Fast path: no truncation needed.
                if (value.length() <= limit) {
                    return value;
                }
                // Slow path: truncate to limit.
                // 1. Compute length of extension in chars (if any).
                final int extensionStart = indexOfFirstDot(value);
                final int extensionLength = extensionStart > 0 ? value.length() - extensionStart : 0;
                // 2. Truncate the non-extension part and append the extension (if any).
                if (extensionLength >= limit) {
                    // Extension itself does not fit
                    throw new IllegalArgumentException("The extension of " + value + " is too long to fit within " + limit + " characters");
                }
                final int safeLimit = safeCutPoint(value, limit - extensionLength);
                if (extensionLength == 0) {
                    return value.subSequence(0, safeLimit);
                }
                return value.subSequence(0, safeLimit).toString() + value.subSequence(extensionStart, value.length());
            }

            private int safeCutPoint(final CharSequence value, final int limit) {
                // Ensure we do not cut a surrogate pair in half.
                if (Character.isHighSurrogate(value.charAt(limit - 1)) && Character.isLowSurrogate(value.charAt(limit))) {
                    return limit - 1;
                }
                return limit;
            }
        };

        /**
         * Gets the measured length in this strategy’s unit.
         *
         * @param value The value to measure, not null.
         * @param charset The charset to use when measuring in bytes.
         * @return The length in this strategy’s unit.
         */
        abstract int getLength(CharSequence value, Charset charset);

        /**
         * Tests if the measured length is less or equal the {@code limit}.
         *
         * @param value The value to measure, not null.
         * @param limit The limit to compare to.
         * @param charset The charset to use when measuring in bytes.
         * @return {@code true} if the measured length is less or equal the {@code limit}, {@code false} otherwise.
         */
        final boolean isWithinLimit(final CharSequence value, final int limit, final Charset charset) {
            return getLength(value, charset) <= limit;
        }

        /**
         * Truncates to {@code limit} in this strategy’s unit (no-op if already within limit).
         *
         * @param value The value to truncate, not null.
         * @param limit The limit to truncate to.
         * @param charset The charset to use when measuring in bytes.
         * @return The truncated value, not null.
         */
        abstract CharSequence truncate(CharSequence value, int limit, Charset charset);
    }

}
