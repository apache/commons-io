/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test FileSystemUtils.
 *
 * @author Stephen Colebourne
 * @version $Id$
 */
public class FileSystemUtilsTestCase extends FileBasedTestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FileSystemUtilsTestCase.class);
    }

    public FileSystemUtilsTestCase(String name) throws IOException {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpace_String() throws Exception {
        // test coverage, as we can't check value
        if (File.separatorChar == '/') {
            assertEquals(true, FileSystemUtils.getFreeSpace("~") > 0);
        } else {
            assertEquals(true, FileSystemUtils.getFreeSpace("") > 0);
        }
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceOS_String_NullPath() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.getFreeSpaceOS(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testGetFreeSpaceOS_String_InitError() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.getFreeSpaceOS("", -1);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Other() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.getFreeSpaceOS("", 0);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Windows() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            protected long getFreeSpaceWindows(String path) throws IOException {
                return 12345L;
            }
        };
        assertEquals(12345L, fsu.getFreeSpaceOS("", 1));
    }

    public void testGetFreeSpaceOS_String_Unix() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            protected long getFreeSpaceUnix(String path) throws IOException {
                return 12345L;
            }
        };
        assertEquals(12345L, fsu.getFreeSpaceOS("", 2));
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceWindows_String_EmptyPath() throws Exception {
        String lines =
            " Volume in drive C is HDD\n" +
            " Volume Serial Number is XXXX-YYYY\n" +
            "\n" +
            " Directory of C:\\Documents and Settings\\Xxxx\n" +
            "\n" +
            "19/08/2005  22:43    <DIR>          .\n" +
            "19/08/2005  22:43    <DIR>          ..\n" +
            "11/08/2005  01:07                81 build.properties\n" +
            "17/08/2005  21:44    <DIR>          Desktop\n" +
            "               7 File(s)        180,260 bytes\n" +
            "              10 Dir(s)  41,411,551,232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /c ", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.getFreeSpaceWindows(""));
    }

    public void testGetFreeSpaceWindows_String_NormalResponse() throws Exception {
        String lines =
            " Volume in drive C is HDD\n" +
            " Volume Serial Number is XXXX-YYYY\n" +
            "\n" +
            " Directory of C:\\Documents and Settings\\Xxxx\n" +
            "\n" +
            "19/08/2005  22:43    <DIR>          .\n" +
            "19/08/2005  22:43    <DIR>          ..\n" +
            "11/08/2005  01:07                81 build.properties\n" +
            "17/08/2005  21:44    <DIR>          Desktop\n" +
            "               7 File(s)        180,260 bytes\n" +
            "              10 Dir(s)  41,411,551,232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /c C:", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.getFreeSpaceWindows("C:"));
    }

    public void testGetFreeSpaceWindows_String_StripDrive() throws Exception {
        String lines =
            " Volume in drive C is HDD\n" +
            " Volume Serial Number is XXXX-YYYY\n" +
            "\n" +
            " Directory of C:\\Documents and Settings\\Xxxx\n" +
            "\n" +
            "19/08/2005  22:43    <DIR>          .\n" +
            "19/08/2005  22:43    <DIR>          ..\n" +
            "11/08/2005  01:07                81 build.properties\n" +
            "17/08/2005  21:44    <DIR>          Desktop\n" +
            "               7 File(s)        180,260 bytes\n" +
            "              10 Dir(s)  41,411,551,232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /c C:", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.getFreeSpaceWindows("C:\\somedir"));
    }

    public void testGetFreeSpaceWindows_String_EmptyResponse() throws Exception {
        String lines = "";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceWindows("C:");
            fail();
        } catch (IOException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceUnix_String_EmptyPath() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   1472504  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testGetFreeSpaceUnix_String_NormalResponse() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   1472504  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        assertEquals(1472504L, fsu.getFreeSpaceUnix("/home/users/s"));
    }

    public void testGetFreeSpaceUnix_String_LongResponse() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx-yyyyyyy-zzz:/home/users/s\n" +
            "                      14428928  12956424   1472504  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        assertEquals(1472504L, fsu.getFreeSpaceUnix("/home/users/s"));
    }

    public void testGetFreeSpaceUnix_String_EmptyResponse() throws Exception {
        String lines = "";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("/home/users/s");
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse1() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "                      14428928  12956424       100";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("/home/users/s");
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse2() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   nnnnnnn  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("/home/users/s");
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse3() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424        -1  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("/home/users/s");
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse4() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx-yyyyyyy-zzz:/home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        try {
            fsu.getFreeSpaceUnix("/home/users/s");
            fail();
        } catch (IOException ex) {}
    }

}
