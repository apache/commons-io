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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilenameUtils} {@code wildcardMatch()} methods.
 */
class FilenameUtilsWildcardTest {

    private static final boolean WINDOWS = File.separatorChar == '\\';

    private static List<String> generateStrings(final String alphabet, final int maxLength) {
        final List<String> strings = new ArrayList<>();
        strings.add("");
        for (int i = 0; i < strings.size(); i++) {
            final String prefix = strings.get(i);
            if (prefix.length() < maxLength) {
                for (int j = 0; j < alphabet.length(); j++) {
                    strings.add(prefix + alphabet.charAt(j));
                }
            }
        }
        return strings;
    }

    // Independent dynamic programming reference, without the production matcher's tokenization or backtracking.
    private static boolean referenceMatch(final String text, final String pattern, final IOCase ioCase) {
        final boolean[][] matches = new boolean[text.length() + 1][pattern.length() + 1];
        matches[0][0] = true;
        for (int i = 0; i <= text.length(); i++) {
            for (int j = 1; j <= pattern.length(); j++) {
                final char token = pattern.charAt(j - 1);
                if (token == '*') {
                    matches[i][j] = matches[i][j - 1] || i > 0 && matches[i - 1][j];
                } else if (i > 0 && (token == '?' || ioCase.checkRegionMatches(text, i - 1, String.valueOf(token)))) {
                    matches[i][j] = matches[i - 1][j - 1];
                }
            }
        }
        return matches[text.length()][pattern.length()];
    }

    private void assertMatch(final String text, final String wildcard, final boolean expected) {
        assertEquals(expected, FilenameUtils.wildcardMatch(text, wildcard), text + " " + wildcard);
    }

    /**
     * See https://issues.apache.org/jira/browse/IO-246
     */
    @Test
    void test_IO_246() {

        // Tests for "*?"
        assertMatch("aaa", "aa*?", true);
        assertMatch("aaa", "a*?", true);
        assertMatch("aaa", "*?", true);

        // Tests for "?*"
        assertMatch("",    "?*",   false);
        assertMatch("a",   "a?*",  false);
        assertMatch("aa",  "aa?*", false);
        assertMatch("a",   "?*",   true);
        assertMatch("aa",  "?*",   true);
        assertMatch("aaa", "?*",   true);

        // Test ending on "?"
        assertMatch("",    "?", false);
        assertMatch("a",   "a?", false);
        assertMatch("aa",  "aa?", false);
        assertMatch("aab", "aa?", true);
        assertMatch("aaa", "*a", true);
    }

    @Test
    void testLocaleIndependence() {
        final Locale orig = Locale.getDefault();

        final Locale[] locales = Locale.getAvailableLocales();

        final String[][] data = {
            { "I", "i"},
            { "i", "I"},
            { "i", "\u0130"},
            { "i", "\u0131"},
            { "\u03A3", "\u03C2"},
            { "\u03A3", "\u03C3"},
            { "\u03C2", "\u03C3"},
        };

        try {
            for (int i = 0; i < data.length; i++) {
                for (final Locale locale : locales) {
                    Locale.setDefault(locale);
                    assertTrue(data[i][0].equalsIgnoreCase(data[i][1]), "Test data corrupt: " + i);
                    final boolean match = FilenameUtils.wildcardMatch(data[i][0], data[i][1], IOCase.INSENSITIVE);
                    assertTrue(match, Locale.getDefault().toString() + ": " + i);
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

    @Test
    void testMatch() {
        assertFalse(FilenameUtils.wildcardMatch(null, "Foo"));
        assertFalse(FilenameUtils.wildcardMatch("Foo", null));
        assertTrue(FilenameUtils.wildcardMatch(null, null));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Foo"));
        assertTrue(FilenameUtils.wildcardMatch("", ""));
        assertTrue(FilenameUtils.wildcardMatch("", "*"));
        assertFalse(FilenameUtils.wildcardMatch("", "?"));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Fo*"));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Fo?"));
        assertTrue(FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*"));
        assertTrue(FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s"));
        assertFalse(FilenameUtils.wildcardMatch("Foo", "Bar"));
        assertTrue(FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*"));
        assertTrue(FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er"));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "*Foo"));
        assertTrue(FilenameUtils.wildcardMatch("BarFoo", "*Foo"));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Foo*"));
        assertTrue(FilenameUtils.wildcardMatch("FooBar", "Foo*"));
        assertFalse(FilenameUtils.wildcardMatch("FOO", "*Foo"));
        assertFalse(FilenameUtils.wildcardMatch("BARFOO", "*Foo"));
        assertFalse(FilenameUtils.wildcardMatch("FOO", "Foo*"));
        assertFalse(FilenameUtils.wildcardMatch("FOOBAR", "Foo*"));
    }

    // A separate set of tests, added to this batch
    @Test
    void testMatch2() {
        assertMatch("log.txt", "log.txt", true);
        assertMatch("log.txt1", "log.txt", false);

        assertMatch("log.txt", "log.txt*", true);
        assertMatch("log.txt", "log.txt*1", false);
        assertMatch("log.txt", "*log.txt*", true);

        assertMatch("log.txt", "*.txt", true);
        assertMatch("txt.log", "*.txt", false);
        assertMatch("config.ini", "*.ini", true);

        assertMatch("config.txt.bak", "con*.txt", false);

        assertMatch("log.txt9", "*.txt?", true);
        assertMatch("log.txt", "*.txt?", false);

        assertMatch("progtestcase.java~5~", "*test*.java~*~", true);
        assertMatch("progtestcase.java;5~", "*test*.java~*~", false);
        assertMatch("progtestcase.java~5", "*test*.java~*~", false);

        assertMatch("log.txt", "log.*", true);

        assertMatch("log.txt", "log?*", true);

        assertMatch("log.txt12", "log.txt??", true);

        assertMatch("log.log", "log**log", true);
        assertMatch("log.log", "log**", true);
        assertMatch("log.log", "log.**", true);
        assertMatch("log.log", "**.log", true);
        assertMatch("log.log", "**log", true);

        assertMatch("log.log", "log*log", true);
        assertMatch("log.log", "log*", true);
        assertMatch("log.log", "log.*", true);
        assertMatch("log.log", "*.log", true);
        assertMatch("log.log", "*log", true);

        assertMatch("log.log", "*log?", false);
        assertMatch("log.log", "*log?*", true);
        assertMatch("log.log.abc", "*log?abc", true);
        assertMatch("log.log.abc.log.abc", "*log?abc", true);
        assertMatch("log.log.abc.log.abc.d", "*log?abc?d", true);
    }

    @Test
    void testMatchCaseSpecified() {
        assertFalse(FilenameUtils.wildcardMatch(null, "Foo", IOCase.SENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("Foo", null, IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch(null, null, IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Foo", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("", "", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Fo*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Fo?", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s", IOCase.SENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("Foo", "Bar", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("BarFoo", "*Foo", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("FooBar", "Foo*", IOCase.SENSITIVE));

        assertFalse(FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.INSENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.INSENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.INSENSITIVE));
        assertTrue(FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.INSENSITIVE));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SYSTEM));
    }

    @Test
    void testMatchCombinatorialBacktracking() {
        final String text = StringUtils.repeat("ab", 60) + "ac";
        final String pattern = StringUtils.repeat("*ab", 30) + "*ac*ad";
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertFalse(FilenameUtils.wildcardMatch(text, pattern));
            assertFalse(FilenameUtils.wildcardMatch(text, pattern, IOCase.INSENSITIVE));
            assertFalse(FilenameUtils.wildcardMatchOnSystem(text, pattern));
            assertTrue(FilenameUtils.wildcardMatch(text + "ad", pattern));
            assertFalse(FilenameUtils.wildcardMatch(text, pattern.toUpperCase(Locale.ROOT), IOCase.INSENSITIVE));
            assertTrue(FilenameUtils.wildcardMatch(text + "ad", pattern.toUpperCase(Locale.ROOT), IOCase.INSENSITIVE));
        });
    }

    @Test
    void testMatchLongInput() {
        final String text = StringUtils.repeat('a', 65_535);
        final String pattern = StringUtils.repeat("*a", 64);
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertFalse(FilenameUtils.wildcardMatch(text, "*a*b"));
            assertFalse(FilenameUtils.wildcardMatch(text, pattern + "*b"));
            assertTrue(FilenameUtils.wildcardMatch(text, pattern));
        });
    }

    @Test
    void testMatchLongQuestionPattern() {
        final String text = StringUtils.repeat('a', 131_072);
        final String pattern = StringUtils.repeat('?', text.length());
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertTrue(FilenameUtils.wildcardMatch(text, pattern));
            assertFalse(FilenameUtils.wildcardMatch(text + "a", pattern));
            assertFalse(FilenameUtils.wildcardMatch(text, "Z" + pattern.substring(1)));
            assertTrue(FilenameUtils.wildcardMatch(text, "a*"));
        });
    }

    @Test
    void testMatchOnSystem() {
        assertFalse(FilenameUtils.wildcardMatchOnSystem(null, "Foo"));
        assertFalse(FilenameUtils.wildcardMatchOnSystem("Foo", null));
        assertTrue(FilenameUtils.wildcardMatchOnSystem(null, null));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo", "Foo"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("", ""));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo", "Fo*"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo", "Fo?"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo Bar and Catflap", "Fo*"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("New Bookmarks", "N?w ?o?k??r?s"));
        assertFalse(FilenameUtils.wildcardMatchOnSystem("Foo", "Bar"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo Bar Foo", "F*o Bar*"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Adobe Acrobat Installer", "Ad*er"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo", "*Foo"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("BarFoo", "*Foo"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("Foo", "Foo*"));
        assertTrue(FilenameUtils.wildcardMatchOnSystem("FooBar", "Foo*"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOO", "*Foo"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("BARFOO", "*Foo"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOO", "Foo*"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOOBAR", "Foo*"));
    }

    @Test
    void testMatchOverlappingLiterals() {
        assertMatch("ababb", "*ab*ab*b", true);
        assertMatch("abab", "*ab*ab*b", false);
        assertMatch("aaaaab", "*aa*ab", true);
        assertMatch("aaaab", "*aaa*ab", true);
        assertMatch("aaab", "*aaa*ab", false);
        assertTrue(FilenameUtils.wildcardMatch("aAaAAb", "*AA*AB", IOCase.INSENSITIVE));
        assertFalse(FilenameUtils.wildcardMatch("aAaAAb", "*AA*AB", IOCase.SENSITIVE));
    }

    @Test
    void testMatchStarQuestion() {
        assertMatch("", "*?", false);
        assertMatch("a", "*?", true);
        assertMatch("a", "*??", false);
        assertMatch("ab", "*??", true);
        assertMatch("a.b", "*?", true);
        assertMatch("a/b", "*?", true);
        assertMatch("ab", "*?", true);
        assertMatch("ab", "**?", true);
        assertMatch("ab", "a*?", true);
        assertMatch("abc", "a*?c", true);
        assertMatch("abxc", "a*?c", true);
        assertMatch("abc", "*?*", true);
        assertMatch("", "*?*", false);
        assertMatch("aab", "*a*?", true);
        assertMatch("ab", "*?*?", true);
        assertMatch("abc", "*?*?", true);
    }

    @Test
    void testMatchStarQuestionBacktracking() {
        final String text = StringUtils.repeat('a', 4096);
        final String pattern = StringUtils.repeat("*?", 64);
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertTrue(FilenameUtils.wildcardMatch(text, pattern));
            assertFalse(FilenameUtils.wildcardMatch(text, pattern + "b"));
            assertFalse(FilenameUtils.wildcardMatch(text, pattern + "B", IOCase.INSENSITIVE));
            assertFalse(FilenameUtils.wildcardMatchOnSystem(text, pattern + "b"));
        });
    }

    @Test
    void testMatchSurrogates() {
        final String high = "\uD801";
        final String low = "\uDC28";
        final String upper = "\uD801\uDC00";
        final String lower = high + low;
        assertMatch(lower, "??", true);
        assertMatch(lower, "?", false);
        assertMatch(lower, "*" + high + "?", true);
        assertMatch(lower, "?" + low, true);
        assertMatch(high, "*" + high, true);
        assertMatch(low, "*" + low, true);
        assertMatch(high, "*" + lower, false);
        assertMatch(lower, "*" + lower, true);
        assertMatch(lower, "*" + upper, false);
        // Compare a whole literal run, even when its UTF-16 contains an unpaired surrogate.
        assertEquals(IOCase.INSENSITIVE.checkEquals(high + upper, upper + high),
                FilenameUtils.wildcardMatch(high + upper, upper + high + "*", IOCase.INSENSITIVE));
        assertEquals(upper.equalsIgnoreCase(lower), FilenameUtils.wildcardMatch(lower, "*" + upper, IOCase.INSENSITIVE));
        assertEquals(upper.equalsIgnoreCase(lower), FilenameUtils.wildcardMatch("x" + lower + "y", "?" + upper + "*", IOCase.INSENSITIVE));
    }

    @Test
    void testMatchWithReference() {
        final List<String> texts = generateStrings("aAb.", 3);
        final List<String> patterns = generateStrings("aAb.*?", 4);
        for (final String text : texts) {
            for (final String pattern : patterns) {
                for (final IOCase ioCase : IOCase.values()) {
                    final boolean expected = referenceMatch(text, pattern, ioCase);
                    assertEquals(expected, FilenameUtils.wildcardMatch(text, pattern, ioCase),
                            () -> "text=" + text + ", pattern=" + pattern + ", case=" + ioCase);
                }
                final boolean expected = referenceMatch(text, pattern, IOCase.SENSITIVE);
                assertEquals(expected, FilenameUtils.wildcardMatch(text, pattern), () -> "text=" + text + ", pattern=" + pattern);
                assertEquals(expected, FilenameUtils.wildcardMatch(text, pattern, null), () -> "text=" + text + ", pattern=" + pattern);
                assertEquals(referenceMatch(text, pattern, IOCase.SYSTEM), FilenameUtils.wildcardMatchOnSystem(text, pattern),
                        () -> "text=" + text + ", pattern=" + pattern);
            }
        }
    }

    @Test
    void testSplitOnTokens() {
        assertArrayEquals(new String[] { "Ad", "*", "er" }, FilenameUtils.splitOnTokens("Ad*er"));
        assertArrayEquals(new String[] { "Ad", "?", "er" }, FilenameUtils.splitOnTokens("Ad?er"));
        assertArrayEquals(new String[] { "Test", "*", "?", "One" }, FilenameUtils.splitOnTokens("Test*?One"));
        assertArrayEquals(new String[] { "Test", "?", "*", "One" }, FilenameUtils.splitOnTokens("Test?*One"));
        assertArrayEquals(new String[] { "*" }, FilenameUtils.splitOnTokens("****"));
        assertArrayEquals(new String[] { "*", "?", "?", "*" }, FilenameUtils.splitOnTokens("*??*"));
        assertArrayEquals(new String[] { "*", "?", "*", "?", "*" }, FilenameUtils.splitOnTokens("*?**?*"));
        assertArrayEquals(new String[] { "*", "?", "*", "?", "*" }, FilenameUtils.splitOnTokens("*?***?*"));
        assertArrayEquals(new String[] { "h", "?", "?", "*" }, FilenameUtils.splitOnTokens("h??*"));
        assertArrayEquals(new String[] { "" }, FilenameUtils.splitOnTokens(""));
    }

}
