/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import junit.framework.TestCase;

public class FilenameUtilsWildcardTestCase extends TestCase {

    public FilenameUtilsWildcardTestCase(String name) {
        super(name);
    }

    //-----------------------------------------------------------------------
    // Testing:
    //   FilenameUtils.wildcardMatch(String,String)

    public void testMatch() {
        assertTrue( FilenameUtils.wildcardMatch("Foo", "Foo") );
        assertTrue( FilenameUtils.wildcardMatch("", "") );
        assertTrue( FilenameUtils.wildcardMatch("Foo", "Fo*") );
        assertTrue( FilenameUtils.wildcardMatch("Foo", "Fo?") );
        assertTrue( FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*") );
        assertTrue( FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s") );
        assertFalse( FilenameUtils.wildcardMatch("Foo", "Bar") );
        assertTrue( FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*") );
        assertTrue( FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er") );
        assertTrue( FilenameUtils.wildcardMatch("Foo", "*Foo") );
        assertTrue( FilenameUtils.wildcardMatch("Foo", "Foo*") );
    }

    public void testSplitOnTokens() {
        assertArrayEquals( new String[] { "Ad", "*", "er" }, FilenameUtils.splitOnTokens("Ad*er") );
        assertArrayEquals( new String[] { "Ad", "?", "er" }, FilenameUtils.splitOnTokens("Ad?er") );
        assertArrayEquals( new String[] { "Test", "*", "?", "One" }, FilenameUtils.splitOnTokens("Test*?One") );
        assertArrayEquals( new String[] { "*", "*", "*", "*" }, FilenameUtils.splitOnTokens("****") );
        assertArrayEquals( new String[] { "*", "?", "?", "*" }, FilenameUtils.splitOnTokens("*??*") );
        assertArrayEquals( new String[] { "*", "?", "?", "*" }, FilenameUtils.splitOnTokens("*??*") );
        assertArrayEquals( new String[] { "h", "?", "?", "*" }, FilenameUtils.splitOnTokens("h??*") );
        assertArrayEquals( new String[] { "" }, FilenameUtils.splitOnTokens("") );
    }

    private void assertArrayEquals(Object[] a1, Object[] a2) {
        assertEquals(a1.length, a2.length);
        for(int i=0; i<a1.length; i++) {
            assertEquals(a1[i], a2[i]);
        }
    }

    private void assertMatch(String text, String wildcard, boolean expected) {
        assertEquals(text + " " + wildcard, expected, FilenameUtils.wildcardMatch(text, wildcard));
    }

    // A separate set of tests, added to this batch
    public void testMatch2() {
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
    }

}
