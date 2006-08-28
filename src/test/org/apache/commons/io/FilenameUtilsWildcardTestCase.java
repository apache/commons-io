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

import java.io.File;

import junit.framework.TestCase;

public class FilenameUtilsWildcardTestCase extends TestCase {

    private static final boolean WINDOWS = (File.separatorChar == '\\');

    public FilenameUtilsWildcardTestCase(String name) {
        super(name);
    }

    //-----------------------------------------------------------------------
    // Testing:
    //   FilenameUtils.wildcardMatch(String,String)

    public void testMatch() {
        assertEquals(false, FilenameUtils.wildcardMatch(null, "Foo"));
        assertEquals(false, FilenameUtils.wildcardMatch("Foo", null));
        assertEquals(true, FilenameUtils.wildcardMatch(null, null));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Foo"));
        assertEquals(true, FilenameUtils.wildcardMatch("", ""));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Fo*"));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Fo?"));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*"));
        assertEquals(true, FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s"));
        assertEquals(false, FilenameUtils.wildcardMatch("Foo", "Bar"));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*"));
        assertEquals(true, FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er"));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "*Foo"));
        assertEquals(true, FilenameUtils.wildcardMatch("BarFoo", "*Foo"));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Foo*"));
        assertEquals(true, FilenameUtils.wildcardMatch("FooBar", "Foo*"));
        assertEquals(false, FilenameUtils.wildcardMatch("FOO", "*Foo"));
        assertEquals(false, FilenameUtils.wildcardMatch("BARFOO", "*Foo"));
        assertEquals(false, FilenameUtils.wildcardMatch("FOO", "Foo*"));
        assertEquals(false, FilenameUtils.wildcardMatch("FOOBAR", "Foo*"));
    }

    public void testMatchOnSystem() {
        assertEquals(false, FilenameUtils.wildcardMatchOnSystem(null, "Foo"));
        assertEquals(false, FilenameUtils.wildcardMatchOnSystem("Foo", null));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem(null, null));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo", "Foo"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("", ""));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo", "Fo*"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo", "Fo?"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo Bar and Catflap", "Fo*"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("New Bookmarks", "N?w ?o?k??r?s"));
        assertEquals(false, FilenameUtils.wildcardMatchOnSystem("Foo", "Bar"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo Bar Foo", "F*o Bar*"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Adobe Acrobat Installer", "Ad*er"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo", "*Foo"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("BarFoo", "*Foo"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("Foo", "Foo*"));
        assertEquals(true, FilenameUtils.wildcardMatchOnSystem("FooBar", "Foo*"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOO", "*Foo"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("BARFOO", "*Foo"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOO", "Foo*"));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatchOnSystem("FOOBAR", "Foo*"));
    }

    public void testMatchCaseSpecified() {
        assertEquals(false, FilenameUtils.wildcardMatch(null, "Foo", IOCase.SENSITIVE));
        assertEquals(false, FilenameUtils.wildcardMatch("Foo", null, IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch(null, null, IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Foo", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("", "", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Fo*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Fo?", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s", IOCase.SENSITIVE));
        assertEquals(false, FilenameUtils.wildcardMatch("Foo", "Bar", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("BarFoo", "*Foo", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("FooBar", "Foo*", IOCase.SENSITIVE));
        
        assertEquals(false, FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SENSITIVE));
        assertEquals(false, FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SENSITIVE));
        assertEquals(false, FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SENSITIVE));
        assertEquals(false, FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.INSENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.INSENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.INSENSITIVE));
        assertEquals(true, FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.INSENSITIVE));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SYSTEM));
        assertEquals(WINDOWS, FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SYSTEM));
    }

    public void testSplitOnTokens() {
        assertArrayEquals( new String[] { "Ad", "*", "er" }, FilenameUtils.splitOnTokens("Ad*er") );
        assertArrayEquals( new String[] { "Ad", "?", "er" }, FilenameUtils.splitOnTokens("Ad?er") );
        assertArrayEquals( new String[] { "Test", "*", "?", "One" }, FilenameUtils.splitOnTokens("Test*?One") );
        assertArrayEquals( new String[] { "*" }, FilenameUtils.splitOnTokens("****") );
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

}
