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
 * @version $Id: FilenameUtilsTestCase.java,v 1.20 2004/11/23 00:04:29 scolebourne Exp $
 * @see FilenameUtils
 */
public class FilenameUtilsTestCase extends FileBasedTestCase {
    
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

    // catPath

    public void testCatPath() {
        // TODO StringIndexOutOfBoundsException thrown if file doesn't contain slash.
        // Is this acceptable?
        //assertEquals("", FilenameUtils.catPath("a", "b"));

        assertEquals("/a" + File.separator + "c", FilenameUtils.catPath("/a/b", "c"));
        assertEquals("/a" + File.separator + "d", FilenameUtils.catPath("/a/b/c", "../d"));
        assertEquals("C:\\a" + File.separator + "c", FilenameUtils.catPath("C:\\a\\b", "c"));
        assertEquals("C:\\a" + File.separator + "d", FilenameUtils.catPath("C:\\a\\b\\c", "../d"));
    }

    // resolveFile

    public void testResolveFileDotDot() throws Exception {
        File file = FilenameUtils.resolveFile(getTestDirectory(), "..");
        assertEquals(
            "Check .. operator",
            file,
            getTestDirectory().getParentFile());
    }

    public void testResolveFileDot() throws Exception {
        File file = FilenameUtils.resolveFile(getTestDirectory(), ".");
        assertEquals("Check . operator", file, getTestDirectory());
    }

    // normalize

    public void testNormalize() throws Exception {
        String[] src =
            {
                null,
                "",
                "/",
                "///",
                "/foo",
                "/foo//",
                "/./",
                "/foo/./",
                "/foo/./bar",
                "/foo/../bar",
                "/foo/../bar/../baz",
                "/foo/bar/../../baz",
                "/././",
                "/foo/./../bar",
                "/foo/.././bar/",
                "//foo//./bar",
                "/../",
                "/foo/../../",
                "../foo",
                "foo/../../bar",
                "foo/../bar" };

        String[] dest =
            {
                null,
                "",
                "/",
                "/",
                "/foo",
                "/foo/",
                "/",
                "/foo/",
                "/foo/bar",
                "/bar",
                "/baz",
                "/baz",
                "/",
                "/bar",
                "/bar/",
                "/foo/bar",
                null,
                null,
                null,
                null,
                "bar" };

        assertEquals("Oops, test writer goofed", src.length, dest.length);

        for (int i = 0; i < src.length; i++) {
            String destStr = FilenameUtils.separatorsToSystem(dest[i]);
            String resultStr = FilenameUtils.normalize(src[i]);
            assertEquals(
                "Check if '" + src[i] + "' normalized to '" + destStr + "', was '" + resultStr + "'",
                destStr, resultStr);
        }
    }

    private String replaceAll(
        String text,
        String lookFor,
        String replaceWith) {
        StringBuffer sb = new StringBuffer(text);
        while (true) {
            int idx = sb.toString().indexOf(lookFor);
            if (idx < 0) {
                break;
            }
            sb.replace(idx, idx + lookFor.length(), replaceWith);
        }
        return sb.toString();
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
        if (WINDOWS) {
            assertEquals(-1, FilenameUtils.getPrefixLength("1:\\a\\b\\c.txt"));
            assertEquals(-1, FilenameUtils.getPrefixLength("1:"));
            assertEquals(-1, FilenameUtils.getPrefixLength("1:a"));
            assertEquals(-1, FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt"));
            assertEquals(-1, FilenameUtils.getPrefixLength("\\\\a"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
            assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
            assertEquals(3, FilenameUtils.getPrefixLength("C:\\a\\b\\c.txt"));
            assertEquals(9, FilenameUtils.getPrefixLength("\\\\server\\a\\b\\c.txt"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("a/b/c.txt"));
            assertEquals(1, FilenameUtils.getPrefixLength("/a/b/c.txt"));
            assertEquals(3, FilenameUtils.getPrefixLength("C:/a/b/c.txt"));
            assertEquals(9, FilenameUtils.getPrefixLength("//server/a/b/c.txt"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("~/a/b/c.txt"));
            assertEquals(0, FilenameUtils.getPrefixLength("~user/a/b/c.txt"));
        } else {
            assertEquals(-1, FilenameUtils.getPrefixLength("~"));
            assertEquals(-1, FilenameUtils.getPrefixLength("~user"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("a/b/c.txt"));
            assertEquals(1, FilenameUtils.getPrefixLength("/a/b/c.txt"));
            assertEquals(2, FilenameUtils.getPrefixLength("~/a/b/c.txt"));
            assertEquals(6, FilenameUtils.getPrefixLength("~user/a/b/c.txt"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("a\\b\\c.txt"));
            assertEquals(1, FilenameUtils.getPrefixLength("\\a\\b\\c.txt"));
            assertEquals(2, FilenameUtils.getPrefixLength("~\\a\\b\\c.txt"));
            assertEquals(6, FilenameUtils.getPrefixLength("~user\\a\\b\\c.txt"));
            
            assertEquals(0, FilenameUtils.getPrefixLength("C:\\a\\b\\c.txt"));
            assertEquals(1, FilenameUtils.getPrefixLength("\\\\server\\a\\b\\c.txt"));
        }
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
        if (WINDOWS) {
            assertEquals(null, FilenameUtils.getPrefix("1:\\a\\b\\c.txt"));
            assertEquals(null, FilenameUtils.getPrefix("1:"));
            assertEquals(null, FilenameUtils.getPrefix("1:a"));
            assertEquals(null, FilenameUtils.getPrefix("\\\\\\a\\b\\c.txt"));
            assertEquals(null, FilenameUtils.getPrefix("\\\\a"));
            
            assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
            assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
            assertEquals("C:\\", FilenameUtils.getPrefix("C:\\a\\b\\c.txt"));
            assertEquals("\\\\server\\", FilenameUtils.getPrefix("\\\\server\\a\\b\\c.txt"));
            
            assertEquals("", FilenameUtils.getPrefix("a/b/c.txt"));
            assertEquals("/", FilenameUtils.getPrefix("/a/b/c.txt"));
            assertEquals("C:/", FilenameUtils.getPrefix("C:/a/b/c.txt"));
            assertEquals("//server/", FilenameUtils.getPrefix("//server/a/b/c.txt"));
            
            assertEquals("", FilenameUtils.getPrefix("~/a/b/c.txt"));
            assertEquals("", FilenameUtils.getPrefix("~user/a/b/c.txt"));
        } else {
            assertEquals(null, FilenameUtils.getPrefix("~"));
            assertEquals(null, FilenameUtils.getPrefix("~user"));
            
            assertEquals("", FilenameUtils.getPrefix("a/b/c.txt"));
            assertEquals("/", FilenameUtils.getPrefix("/a/b/c.txt"));
            assertEquals("~/", FilenameUtils.getPrefix("~/a/b/c.txt"));
            assertEquals("~user/", FilenameUtils.getPrefix("~user/a/b/c.txt"));
            
            assertEquals("", FilenameUtils.getPrefix("a\\b\\c.txt"));
            assertEquals("\\", FilenameUtils.getPrefix("\\a\\b\\c.txt"));
            assertEquals("~\\", FilenameUtils.getPrefix("~\\a\\b\\c.txt"));
            assertEquals("~user\\", FilenameUtils.getPrefix("~user\\a\\b\\c.txt"));
            
            assertEquals("", FilenameUtils.getPrefix("C:\\a\\b\\c.txt"));
            assertEquals("\\", FilenameUtils.getPrefix("\\\\server\\a\\b\\c.txt"));
        }
    }

    public void testGetPath() {
        assertEquals(null, FilenameUtils.getPath(null));
        assertEquals("", FilenameUtils.getPath("noseperator.inthispath"));
        assertEquals("a/b", FilenameUtils.getPath("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getPath("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getPath("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getPath("a\\b\\c"));
        if (WINDOWS) {
            assertEquals(null, FilenameUtils.getPath("1:/a/b/c.txt"));
            assertEquals(null, FilenameUtils.getPath("1:"));
            assertEquals(null, FilenameUtils.getPath("1:a"));
            assertEquals(null, FilenameUtils.getPath("///a/b/c.txt"));
            assertEquals(null, FilenameUtils.getPath("//a"));
            
            assertEquals("a/b", FilenameUtils.getPath("a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("/a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("C:/a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("//server/a/b/c.txt"));
            
            assertEquals("~/a/b", FilenameUtils.getPath("~/a/b/c.txt"));
            assertEquals("~user/a/b", FilenameUtils.getPath("~user/a/b/c.txt"));
        } else {
            assertEquals(null, FilenameUtils.getPath("~"));
            assertEquals(null, FilenameUtils.getPath("~user"));
            
            assertEquals("a/b", FilenameUtils.getPath("a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("/a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("~/a/b/c.txt"));
            assertEquals("a/b", FilenameUtils.getPath("~user/a/b/c.txt"));
            
            assertEquals("C:/a/b", FilenameUtils.getPath("C:/a/b/c.txt"));
            assertEquals("/server/a/b", FilenameUtils.getPath("//server/a/b/c.txt"));
        }
    }

    public void testGetFullPath() {
        assertEquals(null, FilenameUtils.getFullPath(null));
        assertEquals("", FilenameUtils.getFullPath("noseperator.inthispath"));
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c"));
        assertEquals("a/b/c", FilenameUtils.getFullPath("a/b/c/"));
        assertEquals("a\\b", FilenameUtils.getFullPath("a\\b\\c"));
        if (WINDOWS) {
            assertEquals(null, FilenameUtils.getFullPath("1:/a/b/c.txt"));
            assertEquals(null, FilenameUtils.getFullPath("1:"));
            assertEquals(null, FilenameUtils.getFullPath("1:a"));
            assertEquals(null, FilenameUtils.getFullPath("///a/b/c.txt"));
            assertEquals(null, FilenameUtils.getFullPath("//a"));
            
            assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
            assertEquals("/a/b", FilenameUtils.getFullPath("/a/b/c.txt"));
            assertEquals("C:/a/b", FilenameUtils.getFullPath("C:/a/b/c.txt"));
            assertEquals("//server/a/b", FilenameUtils.getFullPath("//server/a/b/c.txt"));
            
            assertEquals("~/a/b", FilenameUtils.getFullPath("~/a/b/c.txt"));
            assertEquals("~user/a/b", FilenameUtils.getFullPath("~user/a/b/c.txt"));
        } else {
            assertEquals(null, FilenameUtils.getFullPath("~"));
            assertEquals(null, FilenameUtils.getFullPath("~user"));
            
            assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
            assertEquals("/a/b", FilenameUtils.getFullPath("/a/b/c.txt"));
            assertEquals("~/a/b", FilenameUtils.getFullPath("~/a/b/c.txt"));
            assertEquals("~user/a/b", FilenameUtils.getFullPath("~user/a/b/c.txt"));
            
            assertEquals("C:/a/b", FilenameUtils.getFullPath("C:/a/b/c.txt"));
            assertEquals("//server/a/b", FilenameUtils.getFullPath("//server/a/b/c.txt"));
        }
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
        
        if (WINDOWS) {
            assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", "TXT"));
        } else {
            assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", "TXT"));
        }
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
        
        if (WINDOWS) {
            assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT"}));
            assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT", "RTF"}));
        } else {
            assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT"}));
            assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new String[] {"TXT", "RTF"}));
        }
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
        
        if (WINDOWS) {
            assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT"}))));
            assertEquals(true, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT", "RTF"}))));
        } else {
            assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT"}))));
            assertEquals(false, FilenameUtils.isExtension("a.b\\file.txt", new ArrayList(Arrays.asList(new String[] {"TXT", "RTF"}))));
        }
    }

}
