/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test FilenameUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @author Martin Cooper
 * @version $Id$
 * @see FilenameUtils
 */
public class FilenameUtilsTestCase extends FileBasedTestCase {
    
    private static final String SEP = "" + File.separatorChar;
    private static final boolean WINDOWS = (File.separatorChar == '\\');

    private File testFile1;
    private File testFile2;

    private static int testFile1Size;
    private static int testFile2Size;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FilenameUtilsTestCase.class);
    }

    public FilenameUtilsTestCase(String name) throws IOException {
        super(name);

        testFile1 = new File(getTestDirectory(), "file1-test.txt");
        testFile2 = new File(getTestDirectory(), "file1a-test.txt");

        testFile1Size = (int)testFile1.length();
        testFile2Size = (int)testFile2.length();
    }

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
        FileUtils.deleteDirectory(getTestDirectory());
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
    }

    /** @see junit.framework.TestCase#tearDown() */
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(getTestDirectory());
    }

    //-----------------------------------------------------------------------
    public void testNormalize() throws Exception {
        assertEquals(null, FilenameUtils.normalize(null));
        assertEquals(null, FilenameUtils.normalize(":"));
        assertEquals(null, FilenameUtils.normalize("1:\\a\\b\\c.txt"));
        assertEquals(null, FilenameUtils.normalize("1:"));
        assertEquals(null, FilenameUtils.normalize("1:a"));
        assertEquals(null, FilenameUtils.normalize("\\\\\\a\\b\\c.txt"));
        assertEquals(null, FilenameUtils.normalize("\\\\a"));
        assertEquals(null, FilenameUtils.normalize("~"));
        assertEquals(null, FilenameUtils.normalize("~user"));
        
        assertEquals("a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("a\\b/c.txt"));
        assertEquals("" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\a\\b/c.txt"));
        assertEquals("C:" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("C:\\a\\b/c.txt"));
        assertEquals("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("\\\\server\\a\\b/c.txt"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("~\\a\\b/c.txt"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt", FilenameUtils.normalize("~user\\a\\b/c.txt"));
        
        assertEquals("a" + SEP + "c", FilenameUtils.normalize("a/b/../c"));
        assertEquals("c", FilenameUtils.normalize("a/b/../../c"));
        assertEquals("c", FilenameUtils.normalize("a/b/../../c/"));
        assertEquals(null, FilenameUtils.normalize("a/b/../../../c"));
        assertEquals("a", FilenameUtils.normalize("a/b/.."));
        assertEquals("a", FilenameUtils.normalize("a/b/../"));
        assertEquals("", FilenameUtils.normalize("a/b/../.."));
        assertEquals("", FilenameUtils.normalize("a/b/../../"));
        assertEquals(null, FilenameUtils.normalize("a/b/../../.."));
        assertEquals("a" + SEP + "d", FilenameUtils.normalize("a/b/../c/../d"));
        assertEquals("a" + SEP + "d", FilenameUtils.normalize("a/b/../c/../d/"));
        assertEquals("a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("a/b//d"));
        assertEquals("a" + SEP + "b", FilenameUtils.normalize("a/b/././."));
        assertEquals("a" + SEP + "b", FilenameUtils.normalize("a/b/./././"));
        assertEquals("a", FilenameUtils.normalize("./a/"));
        assertEquals("a", FilenameUtils.normalize("./a"));
        assertEquals("", FilenameUtils.normalize("./"));
        assertEquals("", FilenameUtils.normalize("."));
        assertEquals(null, FilenameUtils.normalize("../a"));
        assertEquals(null, FilenameUtils.normalize(".."));
        assertEquals("", FilenameUtils.normalize(""));
        
        assertEquals(SEP + "a" + SEP + "c", FilenameUtils.normalize("/a/b/../c"));
        assertEquals(SEP + "c", FilenameUtils.normalize("/a/b/../../c"));
        assertEquals(null, FilenameUtils.normalize("/a/b/../../../c"));
        assertEquals(SEP + "a", FilenameUtils.normalize("/a/b/.."));
        assertEquals(SEP + "", FilenameUtils.normalize("/a/b/../.."));
        assertEquals(null, FilenameUtils.normalize("/a/b/../../.."));
        assertEquals(SEP + "a" + SEP + "d", FilenameUtils.normalize("/a/b/../c/../d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("/a/b//d"));
        assertEquals(SEP + "a" + SEP + "b", FilenameUtils.normalize("/a/b/././."));
        assertEquals(SEP + "a", FilenameUtils.normalize("/./a"));
        assertEquals(SEP + "", FilenameUtils.normalize("/./"));
        assertEquals(SEP + "", FilenameUtils.normalize("/."));
        assertEquals(null, FilenameUtils.normalize("/../a"));
        assertEquals(null, FilenameUtils.normalize("/.."));
        assertEquals(SEP + "", FilenameUtils.normalize("/"));
        
        assertEquals("~" + SEP + "a" + SEP + "c", FilenameUtils.normalize("~/a/b/../c"));
        assertEquals("~" + SEP + "c", FilenameUtils.normalize("~/a/b/../../c"));
        assertEquals(null, FilenameUtils.normalize("~/a/b/../../../c"));
        assertEquals("~" + SEP + "a", FilenameUtils.normalize("~/a/b/.."));
        assertEquals("~" + SEP + "", FilenameUtils.normalize("~/a/b/../.."));
        assertEquals(null, FilenameUtils.normalize("~/a/b/../../.."));
        assertEquals("~" + SEP + "a" + SEP + "d", FilenameUtils.normalize("~/a/b/../c/../d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("~/a/b//d"));
        assertEquals("~" + SEP + "a" + SEP + "b", FilenameUtils.normalize("~/a/b/././."));
        assertEquals("~" + SEP + "a", FilenameUtils.normalize("~/./a"));
        assertEquals("~" + SEP + "", FilenameUtils.normalize("~/./"));
        assertEquals("~" + SEP + "", FilenameUtils.normalize("~/."));
        assertEquals(null, FilenameUtils.normalize("~/../a"));
        assertEquals(null, FilenameUtils.normalize("~/.."));
        assertEquals("~" + SEP + "", FilenameUtils.normalize("~/"));
        
        assertEquals("~user" + SEP + "a" + SEP + "c", FilenameUtils.normalize("~user/a/b/../c"));
        assertEquals("~user" + SEP + "c", FilenameUtils.normalize("~user/a/b/../../c"));
        assertEquals(null, FilenameUtils.normalize("~user/a/b/../../../c"));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalize("~user/a/b/.."));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/a/b/../.."));
        assertEquals(null, FilenameUtils.normalize("~user/a/b/../../.."));
        assertEquals("~user" + SEP + "a" + SEP + "d", FilenameUtils.normalize("~user/a/b/../c/../d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("~user/a/b//d"));
        assertEquals("~user" + SEP + "a" + SEP + "b", FilenameUtils.normalize("~user/a/b/././."));
        assertEquals("~user" + SEP + "a", FilenameUtils.normalize("~user/./a"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/./"));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/."));
        assertEquals(null, FilenameUtils.normalize("~user/../a"));
        assertEquals(null, FilenameUtils.normalize("~user/.."));
        assertEquals("~user" + SEP + "", FilenameUtils.normalize("~user/"));
        
        assertEquals("C:" + SEP + "a" + SEP + "c", FilenameUtils.normalize("C:/a/b/../c"));
        assertEquals("C:" + SEP + "c", FilenameUtils.normalize("C:/a/b/../../c"));
        assertEquals(null, FilenameUtils.normalize("C:/a/b/../../../c"));
        assertEquals("C:" + SEP + "a", FilenameUtils.normalize("C:/a/b/.."));
        assertEquals("C:" + SEP + "", FilenameUtils.normalize("C:/a/b/../.."));
        assertEquals(null, FilenameUtils.normalize("C:/a/b/../../.."));
        assertEquals("C:" + SEP + "a" + SEP + "d", FilenameUtils.normalize("C:/a/b/../c/../d"));
        assertEquals("C:" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("C:/a/b//d"));
        assertEquals("C:" + SEP + "a" + SEP + "b", FilenameUtils.normalize("C:/a/b/././."));
        assertEquals("C:" + SEP + "a", FilenameUtils.normalize("C:/./a"));
        assertEquals("C:" + SEP + "", FilenameUtils.normalize("C:/./"));
        assertEquals("C:" + SEP + "", FilenameUtils.normalize("C:/."));
        assertEquals(null, FilenameUtils.normalize("C:/../a"));
        assertEquals(null, FilenameUtils.normalize("C:/.."));
        assertEquals("C:" + SEP + "", FilenameUtils.normalize("C:/"));
        
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "c", FilenameUtils.normalize("//server/a/b/../c"));
        assertEquals(SEP + SEP + "server" + SEP + "c", FilenameUtils.normalize("//server/a/b/../../c"));
        assertEquals(null, FilenameUtils.normalize("//server/a/b/../../../c"));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalize("//server/a/b/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/a/b/../.."));
        assertEquals(null, FilenameUtils.normalize("//server/a/b/../../.."));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "d", FilenameUtils.normalize("//server/a/b/../c/../d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d", FilenameUtils.normalize("//server/a/b//d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b", FilenameUtils.normalize("//server/a/b/././."));
        assertEquals(SEP + SEP + "server" + SEP + "a", FilenameUtils.normalize("//server/./a"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/./"));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/."));
        assertEquals(null, FilenameUtils.normalize("//server/../a"));
        assertEquals(null, FilenameUtils.normalize("//server/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", FilenameUtils.normalize("//server/"));
    }

    //-----------------------------------------------------------------------
    public void testConcat() {
        assertEquals(null, FilenameUtils.concat("", null));
        assertEquals(null, FilenameUtils.concat(null, null));
        assertEquals(null, FilenameUtils.concat(null, ""));
        assertEquals(null, FilenameUtils.concat(null, "a"));
        assertEquals(SEP + "a", FilenameUtils.concat(null, "/a"));
        
        assertEquals(null, FilenameUtils.concat("", ":")); // invalid prefix
        assertEquals(null, FilenameUtils.concat(":", "")); // invalid prefix
        
        assertEquals("f", FilenameUtils.concat("", "f/"));
        assertEquals("f", FilenameUtils.concat("", "f"));
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a/", "f/"));
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a", "f"));
        assertEquals("a" + SEP + "b" + SEP + "f", FilenameUtils.concat("a/b/", "f/"));
        assertEquals("a" + SEP + "b" + SEP + "f", FilenameUtils.concat("a/b", "f"));
        
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a/b/", "../f/"));
        assertEquals("a" + SEP + "f", FilenameUtils.concat("a/b", "../f"));
        assertEquals("a" + SEP + "c" + SEP + "g", FilenameUtils.concat("a/b/../c/", "f/../g/"));
        assertEquals("a" + SEP + "c" + SEP + "g", FilenameUtils.concat("a/b/../c", "f/../g"));
        
        assertEquals("a" + SEP + "c.txt" + SEP + "f", FilenameUtils.concat("a/c.txt", "f"));
        
        assertEquals(SEP + "f", FilenameUtils.concat("", "/f/"));
        assertEquals(SEP + "f", FilenameUtils.concat("", "/f"));
        assertEquals(SEP + "f", FilenameUtils.concat("a/", "/f/"));
        assertEquals(SEP + "f", FilenameUtils.concat("a", "/f"));
    }

    //-----------------------------------------------------------------------
    public void testSeparatorsToUnix() {
        assertEquals(null, FilenameUtils.separatorsToUnix(null));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b/c"));
        assertEquals("/a/b/c.txt", FilenameUtils.separatorsToUnix("/a/b/c.txt"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("/a/b\\c"));
        assertEquals("/a/b/c", FilenameUtils.separatorsToUnix("\\a\\b\\c"));
        assertEquals("D:/a/b/c", FilenameUtils.separatorsToUnix("D:\\a\\b\\c"));
    }

    public void testSeparatorsToWindows() {
        assertEquals(null, FilenameUtils.separatorsToWindows(null));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b\\c"));
        assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToWindows("\\a\\b\\c.txt"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("\\a\\b/c"));
        assertEquals("\\a\\b\\c", FilenameUtils.separatorsToWindows("/a/b/c"));
        assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToWindows("D:/a/b/c"));
    }

    public void testSeparatorsToSystem() {
        if (WINDOWS) {
            assertEquals(null, FilenameUtils.separatorsToSystem(null));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("\\a\\b\\c.txt", FilenameUtils.separatorsToSystem("\\a\\b\\c.txt"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("\\a\\b/c"));
            assertEquals("\\a\\b\\c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("D:\\a\\b\\c", FilenameUtils.separatorsToSystem("D:/a/b/c"));
        } else {
            assertEquals(null, FilenameUtils.separatorsToSystem(null));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b/c"));
            assertEquals("/a/b/c.txt", FilenameUtils.separatorsToSystem("/a/b/c.txt"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("/a/b\\c"));
            assertEquals("/a/b/c", FilenameUtils.separatorsToSystem("\\a\\b\\c"));
            assertEquals("D:/a/b/c", FilenameUtils.separatorsToSystem("D:\\a\\b\\c"));
        }
    }

    //-----------------------------------------------------------------------
    public void testGetPrefixLength() {
        assertEquals(-1, FilenameUtils.getPrefixLength(null));
        assertEquals(-1, FilenameUtils.getPrefixLength(":"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:"));
        assertEquals(-1, FilenameUtils.getPrefixLength("1:a"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt"));
        assertEquals(-1, FilenameUtils.getPrefixLength("\\\\a"));
        assertEquals(-1, FilenameUtils.getPrefixLength("~"));
        assertEquals(-1, FilenameUtils.getPrefixLength("~user"));
        
        assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("C:a\\b\\c.txt"));
        assertEquals(3, FilenameUtils.getPrefixLength("C:\\a\\b\\c.txt"));
        assertEquals(9, FilenameUtils.getPrefixLength("\\\\server\\a\\b\\c.txt"));
        
        assertEquals(0, FilenameUtils.getPrefixLength("a/b/c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("/a/b/c.txt"));
        assertEquals(3, FilenameUtils.getPrefixLength("C:/a/b/c.txt"));
        assertEquals(9, FilenameUtils.getPrefixLength("//server/a/b/c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("~/a/b/c.txt"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user/a/b/c.txt"));
        
        assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
        assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
        assertEquals(2, FilenameUtils.getPrefixLength("~\\a\\b\\c.txt"));
        assertEquals(6, FilenameUtils.getPrefixLength("~user\\a\\b\\c.txt"));
    }
    
    public void testIndexOfLastSeparator() {
        assertEquals(-1, FilenameUtils.indexOfLastSeparator(null));
        assertEquals(-1, FilenameUtils.indexOfLastSeparator("noseperator.inthispath"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a/b/c"));
        assertEquals(3, FilenameUtils.indexOfLastSeparator("a\\b\\c"));
    }

    public void testIndexOfExtension() {
        assertEquals(-1, FilenameUtils.indexOfExtension(null));
        assertEquals(-1, FilenameUtils.indexOfExtension("file"));
        assertEquals(4, FilenameUtils.indexOfExtension("file.txt"));
        assertEquals(13, FilenameUtils.indexOfExtension("a.txt/b.txt/c.txt"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b\\c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a/b.notextension/c"));
        assertEquals(-1, FilenameUtils.indexOfExtension("a\\b.notextension\\c"));
    }

    //-----------------------------------------------------------------------
    public void testGetPrefix() {
        assertEquals(null, FilenameUtils.getPrefix(null));
        assertEquals(null, FilenameUtils.getPrefix(":"));
        assertEquals(null, FilenameUtils.getPrefix("1:\\a\\b\\c.txt"));
        assertEquals(null, FilenameUtils.getPrefix("1:"));
        assertEquals(null, FilenameUtils.getPrefix("1:a"));
        assertEquals(null, FilenameUtils.getPrefix("\\\\\\a\\b\\c.txt"));
        assertEquals(null, FilenameUtils.getPrefix("\\\\a"));
        assertEquals(null, FilenameUtils.getPrefix("~"));
        assertEquals(null, FilenameUtils.getPrefix("~user"));
        
        assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
        assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
        assertEquals("C:\\", FilenameUtils.getPrefix("C:\\a\\b\\c.txt"));
        assertEquals("\\\\server\\", FilenameUtils.getPrefix("\\\\server\\a\\b\\c.txt"));
        
        assertEquals("", FilenameUtils.getPrefix("a/b/c.txt"));
        assertEquals("/", FilenameUtils.getPrefix("/a/b/c.txt"));
        assertEquals("C:/", FilenameUtils.getPrefix("C:/a/b/c.txt"));
        assertEquals("//server/", FilenameUtils.getPrefix("//server/a/b/c.txt"));
        assertEquals("~/", FilenameUtils.getPrefix("~/a/b/c.txt"));
        assertEquals("~user/", FilenameUtils.getPrefix("~user/a/b/c.txt"));
        
        assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
        assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
        assertEquals("~\\", FilenameUtils.getPrefix("~\\a\\b\\c.txt"));
        assertEquals("~user\\", FilenameUtils.getPrefix("~user\\a\\b\\c.txt"));
    }

    public void testGetPath() {
        assertEquals(null, FilenameUtils.getPath(null));
        assertEquals("", FilenameUtils.getPath("noseperator.inthispath"));
        assertEquals("a/b", FilenameUtils.getPath("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getPath("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getPath("a\\b\\c"));
        
        assertEquals(null, FilenameUtils.getPath(":"));
        assertEquals(null, FilenameUtils.getPath("1:/a/b/c.txt"));
        assertEquals(null, FilenameUtils.getPath("1:"));
        assertEquals(null, FilenameUtils.getPath("1:a"));
        assertEquals(null, FilenameUtils.getPath("///a/b/c.txt"));
        assertEquals(null, FilenameUtils.getPath("//a"));
        assertEquals(null, FilenameUtils.getPath("~"));
        assertEquals(null, FilenameUtils.getPath("~user"));
        
        assertEquals("a/b", FilenameUtils.getPath("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("C:/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("//server/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("~/a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("~user/a/b/c.txt"));
    }

    public void testGetFullPath() {
        assertEquals(null, FilenameUtils.getFullPath(null));
        assertEquals("", FilenameUtils.getFullPath("noseperator.inthispath"));
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getFullPath("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getFullPath("a\\b\\c"));
        
        assertEquals(null, FilenameUtils.getFullPath(":"));
        assertEquals(null, FilenameUtils.getFullPath("1:/a/b/c.txt"));
        assertEquals(null, FilenameUtils.getFullPath("1:"));
        assertEquals(null, FilenameUtils.getFullPath("1:a"));
        assertEquals(null, FilenameUtils.getFullPath("///a/b/c.txt"));
        assertEquals(null, FilenameUtils.getFullPath("//a"));
        assertEquals(null, FilenameUtils.getFullPath("~"));
        assertEquals(null, FilenameUtils.getFullPath("~user"));
        
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("/a/b", FilenameUtils.getFullPath("/a/b/c.txt"));
        assertEquals("C:/a/b", FilenameUtils.getFullPath("C:/a/b/c.txt"));
        assertEquals("//server/a/b", FilenameUtils.getFullPath("//server/a/b/c.txt"));
        assertEquals("~/a/b", FilenameUtils.getFullPath("~/a/b/c.txt"));
        assertEquals("~user/a/b", FilenameUtils.getFullPath("~user/a/b/c.txt"));
    }

    public void testGetName() {
        assertEquals(null, FilenameUtils.getName(null));
        assertEquals("noseperator.inthispath", FilenameUtils.getName("noseperator.inthispath"));
        assertEquals("c.txt", FilenameUtils.getName("a/b/c.txt"));
        assertEquals("c", FilenameUtils.getName("a/b/c"));
        assertEquals("", FilenameUtils.getName("a/b/c/"));
        assertEquals("c", FilenameUtils.getName("a\\b\\c"));
    }

    public void testGetBaseName() {
        assertEquals(null, FilenameUtils.getBaseName(null));
        assertEquals("noseperator", FilenameUtils.getBaseName("noseperator.inthispath"));
        assertEquals("c", FilenameUtils.getBaseName("a/b/c.txt"));
        assertEquals("c", FilenameUtils.getBaseName("a/b/c"));
        assertEquals("", FilenameUtils.getBaseName("a/b/c/"));
        assertEquals("c", FilenameUtils.getBaseName("a\\b\\c"));
        assertEquals("file.txt", FilenameUtils.getBaseName("file.txt.bak"));
    }

    public void testGetExtension() {
        assertEquals(null, FilenameUtils.getExtension(null));
        assertEquals("ext", FilenameUtils.getExtension("file.ext"));
        assertEquals("", FilenameUtils.getExtension("README"));
        assertEquals("com", FilenameUtils.getExtension("domain.dot.com"));
        assertEquals("jpeg", FilenameUtils.getExtension("image.jpeg"));
        assertEquals("", FilenameUtils.getExtension("a.b/c"));
        assertEquals("txt", FilenameUtils.getExtension("a.b/c.txt"));
        assertEquals("", FilenameUtils.getExtension("a/b/c"));
        assertEquals("", FilenameUtils.getExtension("a.b\\c"));
        assertEquals("txt", FilenameUtils.getExtension("a.b\\c.txt"));
        assertEquals("", FilenameUtils.getExtension("a\\b\\c"));
        assertEquals("", FilenameUtils.getExtension("C:\\temp\\foo.bar\\README"));
        assertEquals("ext", FilenameUtils.getExtension("../filename.ext"));
    }

    public void testRemoveExtension() {
        assertEquals(null, FilenameUtils.removeExtension(null));
        assertEquals("file", FilenameUtils.removeExtension("file.ext"));
        assertEquals("README", FilenameUtils.removeExtension("README"));
        assertEquals("domain.dot", FilenameUtils.removeExtension("domain.dot.com"));
        assertEquals("image", FilenameUtils.removeExtension("image.jpeg"));
        assertEquals("a.b/c", FilenameUtils.removeExtension("a.b/c"));
        assertEquals("a.b/c", FilenameUtils.removeExtension("a.b/c.txt"));
        assertEquals("a/b/c", FilenameUtils.removeExtension("a/b/c"));
        assertEquals("a.b\\c", FilenameUtils.removeExtension("a.b\\c"));
        assertEquals("a.b\\c", FilenameUtils.removeExtension("a.b\\c.txt"));
        assertEquals("a\\b\\c", FilenameUtils.removeExtension("a\\b\\c"));
        assertEquals("C:\\temp\\foo.bar\\README", FilenameUtils.removeExtension("C:\\temp\\foo.bar\\README"));
        assertEquals("../filename", FilenameUtils.removeExtension("../filename.ext"));
    }

    //-----------------------------------------------------------------------
    public void testEquals() {
        assertEquals(true, FilenameUtils.equals(null, null));
        assertEquals(false, FilenameUtils.equals(null, ""));
        assertEquals(false, FilenameUtils.equals("", null));
        assertEquals(true, FilenameUtils.equals("", ""));
        assertEquals(true, FilenameUtils.equals("file.txt", "file.txt"));
        assertEquals(false, FilenameUtils.equals("file.txt", "FILE.TXT"));
        assertEquals(false, FilenameUtils.equals("a\\b\\file.txt", "a/b/file.txt"));
    }

    public void testEqualsOnSystem() {
        assertEquals(true, FilenameUtils.equalsOnSystem(null, null));
        assertEquals(false, FilenameUtils.equalsOnSystem(null, ""));
        assertEquals(false, FilenameUtils.equalsOnSystem("", null));
        assertEquals(true, FilenameUtils.equalsOnSystem("", ""));
        assertEquals(true, FilenameUtils.equalsOnSystem("file.txt", "file.txt"));
        assertEquals(WINDOWS, FilenameUtils.equalsOnSystem("file.txt", "FILE.TXT"));
        assertEquals(false, FilenameUtils.equalsOnSystem("a\\b\\file.txt", "a/b/file.txt"));
    }

    //-----------------------------------------------------------------------
    public void testEqualsNormalized() {
        assertEquals(true, FilenameUtils.equalsNormalized(null, null));
        assertEquals(false, FilenameUtils.equalsNormalized(null, ""));
        assertEquals(false, FilenameUtils.equalsNormalized("", null));
        assertEquals(true, FilenameUtils.equalsNormalized("", ""));
        assertEquals(true, FilenameUtils.equalsNormalized("file.txt", "file.txt"));
        assertEquals(false, FilenameUtils.equalsNormalized("file.txt", "FILE.TXT"));
        assertEquals(true, FilenameUtils.equalsNormalized("a\\b\\file.txt", "a/b/file.txt"));
        assertEquals(true, FilenameUtils.equalsNormalized("a\\b\\", "a/b"));
    }

    public void testEqualsNormalizedOnSystem() {
        assertEquals(true, FilenameUtils.equalsNormalizedOnSystem(null, null));
        assertEquals(false, FilenameUtils.equalsNormalizedOnSystem(null, ""));
        assertEquals(false, FilenameUtils.equalsNormalizedOnSystem("", null));
        assertEquals(true, FilenameUtils.equalsNormalizedOnSystem("", ""));
        assertEquals(true, FilenameUtils.equalsNormalizedOnSystem("file.txt", "file.txt"));
        assertEquals(WINDOWS, FilenameUtils.equalsNormalizedOnSystem("file.txt", "FILE.TXT"));
        assertEquals(true, FilenameUtils.equalsNormalizedOnSystem("a\\b\\file.txt", "a/b/file.txt"));
        assertEquals(true, FilenameUtils.equalsNormalizedOnSystem("a\\b\\", "a/b"));
    }

    //-----------------------------------------------------------------------
    public void testIsExtension() {
        assertEquals(false, FilenameUtils.isExtension(null, (String) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", (String) null));
        assertEquals(true, FilenameUtils.isExtension("file", (String) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", ""));
        assertEquals(true, FilenameUtils.isExtension("file", ""));
        assertEquals(true, FilenameUtils.isExtension("file.txt", "txt"));
        assertEquals(false, FilenameUtils.isExtension("file.txt", "rtf"));
        
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", (String) null));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", ""));
        assertEquals(true, FilenameUtils.isExtension("a/b/file.txt", "txt"));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", "rtf"));
        
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", (String) null));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", ""));
        assertEquals(true, FilenameUtils.isExtension("a.b/file.txt", "txt"));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", "rtf"));
        
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", (String) null));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", ""));
        assertEquals(true, FilenameUtils.isExtension("a\\b\\file.txt", "txt"));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", "rtf"));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", (String) null));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", ""));
        assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", "txt"));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", "rtf"));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", "TXT"));
    }

    public void testIsExtensionArray() {
        assertEquals(false, FilenameUtils.isExtension(null, (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", (String[]) null));
        assertEquals(true, FilenameUtils.isExtension("file", (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", new String[0]));
        assertEquals(true, FilenameUtils.isExtension("file.txt", new String[] {"txt"}));
        assertEquals(false, FilenameUtils.isExtension("file.txt", new String[] {"rtf"}));
        assertEquals(true, FilenameUtils.isExtension("file", new String[] {"rtf", ""}));
        assertEquals(true, FilenameUtils.isExtension("file.txt", new String[] {"rtf", "txt"}));
        
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", new String[0]));
        assertEquals(true, FilenameUtils.isExtension("a/b/file.txt", new String[] {"txt"}));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", new String[] {"rtf"}));
        assertEquals(true, FilenameUtils.isExtension("a/b/file.txt", new String[] {"rtf", "txt"}));
        
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", new String[0]));
        assertEquals(true, FilenameUtils.isExtension("a.b/file.txt", new String[] {"txt"}));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", new String[] {"rtf"}));
        assertEquals(true, FilenameUtils.isExtension("a.b/file.txt", new String[] {"rtf", "txt"}));
        
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", new String[0]));
        assertEquals(true, FilenameUtils.isExtension("a\\b\\file.txt", new String[] {"txt"}));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", new String[] {"rtf"}));
        assertEquals(true, FilenameUtils.isExtension("a\\b\\file.txt", new String[] {"rtf", "txt"}));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", (String[]) null));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[0]));
        assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"txt"}));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"rtf"}));
        assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"rtf", "txt"}));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT"}));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT", "RTF"}));
    }

    public void testIsExtensionCollection() {
        assertEquals(false, FilenameUtils.isExtension(null, (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", (Collection) null));
        assertEquals(true, FilenameUtils.isExtension("file", (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("file.txt", new ArrayList()));
        assertEquals(true, FilenameUtils.isExtension("file.txt", new ArrayList(Arrays.asList(new String[] {"txt"}))));
        assertEquals(false, FilenameUtils.isExtension("file.txt", new ArrayList(Arrays.asList(new String[] {"rtf"}))));
        assertEquals(true, FilenameUtils.isExtension("file", new ArrayList(Arrays.asList(new String[] {"rtf", ""}))));
        assertEquals(true, FilenameUtils.isExtension("file.txt", new ArrayList(Arrays.asList(new String[] {"rtf", "txt"}))));
        
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", new ArrayList()));
        assertEquals(true, FilenameUtils.isExtension("a/b/file.txt", new ArrayList(Arrays.asList(new String[] {"txt"}))));
        assertEquals(false, FilenameUtils.isExtension("a/b/file.txt", new ArrayList(Arrays.asList(new String[] {"rtf"}))));
        assertEquals(true, FilenameUtils.isExtension("a/b/file.txt", new ArrayList(Arrays.asList(new String[] {"rtf", "txt"}))));
        
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", new ArrayList()));
        assertEquals(true, FilenameUtils.isExtension("a.b/file.txt", new ArrayList(Arrays.asList(new String[] {"txt"}))));
        assertEquals(false, FilenameUtils.isExtension("a.b/file.txt", new ArrayList(Arrays.asList(new String[] {"rtf"}))));
        assertEquals(true, FilenameUtils.isExtension("a.b/file.txt", new ArrayList(Arrays.asList(new String[] {"rtf", "txt"}))));
        
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList()));
        assertEquals(true, FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList(Arrays.asList(new String[] {"txt"}))));
        assertEquals(false, FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList(Arrays.asList(new String[] {"rtf"}))));
        assertEquals(true, FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList(Arrays.asList(new String[] {"rtf", "txt"}))));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", (Collection) null));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList()));
        assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"txt"}))));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"rtf"}))));
        assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"rtf", "txt"}))));
        
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT"}))));
        assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT", "RTF"}))));
    }

}
