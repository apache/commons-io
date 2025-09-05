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
    }, new String[] {}, false, false, '/', LengthUnit.BYTES),

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
    }, new String[] {}, false, false, '/', LengthUnit.BYTES),

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
    }, new String[] {}, false, false, '/', LengthUnit.CHARS),

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
            }, true, true, '\\', LengthUnit.CHARS);
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

    /**
     * Copied from Apache Commons Lang CharSequenceUtils.
     *
     * Returns the index within {@code cs} of the first occurrence of the
     * specified character, starting the search at the specified index.
     * <p>
     * If a character with value {@code searchChar} occurs in the
     * character sequence represented by the {@code cs}
     * object at an index no smaller than {@code start}, then
     * the index of the first such occurrence is returned. For values
     * of {@code searchChar} in the range from 0 to 0xFFFF (inclusive),
     * this is the smallest value <em>k</em> such that:
     * </p>
     * <blockquote><pre>
     * (this.charAt(<em>k</em>) == searchChar) &amp;&amp; (<em>k</em> &gt;= start)
     * </pre></blockquote>
     * is true. For other values of {@code searchChar}, it is the
     * smallest value <em>k</em> such that:
     * <blockquote><pre>
     * (this.codePointAt(<em>k</em>) == searchChar) &amp;&amp; (<em>k</em> &gt;= start)
     * </pre></blockquote>
     * <p>
     * is true. In either case, if no such character occurs in {@code cs}
     * at or after position {@code start}, then
     * {@code -1} is returned.
     * </p>
     * <p>
     * There is no restriction on the value of {@code start}. If it
     * is negative, it has the same effect as if it were zero: the entire
     * {@link CharSequence} may be searched. If it is greater than
     * the length of {@code cs}, it has the same effect as if it were
     * equal to the length of {@code cs}: {@code -1} is returned.
     * </p>
     * <p>All indices are specified in {@code char} values
     * (Unicode code units).
     * </p>
     *
     * @param cs  the {@link CharSequence} to be processed, not null
     * @param searchChar  the char to be searched for
     * @param start  the start index, negative starts at the string start
     * @return the index where the search char was found, -1 if not found
     * @since 3.6 updated to behave more like {@link String}
     */
    private static int indexOf(final CharSequence cs, final int searchChar, int start) {
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar, start);
        }
        final int sz = cs.length();
        if (start < 0) {
            start = 0;
        }
        if (searchChar < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            for (int i = start; i < sz; i++) {
                if (cs.charAt(i) == searchChar) {
                    return i;
                }
            }
            return -1;
        }
        //supplementary characters (LANG1300)
        if (searchChar <= Character.MAX_CODE_POINT) {
            final char[] chars = Character.toChars(searchChar);
            for (int i = start; i < sz - 1; i++) {
                final char high = cs.charAt(i);
                final char low = cs.charAt(i + 1);
                if (high == chars[0] && low == chars[1]) {
                    return i;
                }
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
    private final LengthUnit lengthUnit;

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
     * @param lengthUnit The unit of measurement for length limits.
     */
    FileSystem(final int blockSize, final boolean caseSensitive, final boolean casePreserving,
        final int maxFileLength, final int maxPathLength, final int[] illegalFileNameChars,
        final String[] reservedFileNames, final boolean reservedFileNamesExtensions, final boolean supportsDriveLetter,
        final char nameSeparator, final LengthUnit lengthUnit) {
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
        this.lengthUnit = lengthUnit;
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
     * <p><strong>Note:</strong> This excludes any folder path. The unit depends on the
     * filesystem or OS; see {@link #getLengthUnit()} to check whether the value is in
     * bytes or UTF-16 characters.</p>
     *
     * @return the maximum file name length.
     */
    public int getMaxFileNameLength() {
        return maxFileNameLength;
    }

    /**
     * Gets the maximum length for file paths (may include folders).
     *
     * <p><strong>Note:</strong> This may include folder names as well as the file name.
     * The unit is the same as {@link #getMaxFileNameLength()} and can be obtained
     * from {@link #getLengthUnit()}.</p>
     *
     * @return the maximum file path length.
     */
    public int getMaxPathLength() {
        return maxPathLength;
    }

    /**
     * Gets the unit of measurement for length limits.
     *
     * <p>Depending on the platform, limits may be expressed in bytes or in UTF-16
     * characters.</p>
     *
     * @return the unit for file name and path length limits.
     * @since 2.21.0
     */
    public LengthUnit getLengthUnit() {
        return lengthUnit;
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
        if (!isLegalFileLength(candidate, charset)) {
            return false;
        }
        if (isReservedFileName(candidate)) {
            return false;
        }
        return candidate.chars().noneMatch(this::isIllegalFileNameChar);
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
        final CharSequence truncated = truncateFileName(candidate, charset);
        final int[] array = truncated.chars().map(i -> isIllegalFileNameChar(i) ? replacement : i).toArray();
        return new String(array, 0, array.length);
    }

    CharSequence trimExtension(final CharSequence cs) {
        final int index = indexOf(cs, '.', 0);
        return index < 0 ? cs : cs.subSequence(0, index);
    }

    private boolean isLegalFileLength(final CharSequence candidate, final Charset charset) {
        if (candidate == null || candidate.length() == 0) {
            return false;
        }
        if (lengthUnit == LengthUnit.CHARS) {
            return candidate.length() <= getMaxFileNameLength();
        }
        final CharsetEncoder encoder = charset.newEncoder();
        try {
            final ByteBuffer buffer = encoder.encode(CharBuffer.wrap(candidate));
            return buffer.remaining() <= getMaxFileNameLength();
        } catch (CharacterCodingException e) {
            // If we can't encode, it's not legal
            return false;
        }
    }

    CharSequence truncateFileName(final CharSequence candidate, final Charset charset) {
        final int maxFileNameLength = getMaxFileNameLength();
        // Character-based limit: simple substring if needed.
        if (lengthUnit == LengthUnit.CHARS) {
            return candidate.length() <= maxFileNameLength ? candidate : candidate.subSequence(0, maxFileNameLength);
        }

        // Byte-based limit
        return truncateByBytes(candidate, charset, maxFileNameLength);
    }

    static CharSequence truncateByBytes(final CharSequence candidate, final Charset charset, final int maxBytes) {
        // Byte-based limit
        final CharsetEncoder encoder = charset.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        if (!encoder.canEncode(candidate)) {
            throw new IllegalArgumentException(
                    "File name contains characters that cannot be encoded with charset " + charset.name());
        }

        // Fast path: if even the worst-case expansion fits, we're done.
        if (candidate.length() <= Math.floor(maxBytes / encoder.maxBytesPerChar())) {
            return candidate;
        }

        // Slow path: encode into a fixed-size byte buffer.
        final ByteBuffer out = ByteBuffer.allocate(maxBytes);
        final CharBuffer in = CharBuffer.wrap(candidate);

        // Encode until the first character that would exceed the byte budget.
        final CoderResult cr = encoder.encode(in, out, true);

        if (cr.isUnderflow()) {
            // Entire candidate fit within maxFileNameLength bytes.
            return candidate;
        }

        // We ran out of space mid-encode: truncate BEFORE the offending character.
        return candidate.subSequence(0, in.position());
    }

    /**
     * Units of length for the file name and path length limits.
     *
     * @since 2.21.0
     */
    public enum LengthUnit {
        /** Length in bytes. */
        BYTES,
        /** Length in UTF-16 characters. */
        CHARS;
    }
}
