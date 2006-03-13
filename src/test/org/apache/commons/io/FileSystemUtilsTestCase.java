/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
import java.io.InputStreamReader;
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
        
//        try {
//            System.out.println(FileSystemUtils.freeSpace("C:\\"));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
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
            // have to figure out unix block size
            Process proc = Runtime.getRuntime().exec(new String[] {"df", "/"});
            boolean kilobyteBlock = true;
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = r.readLine();
                if (line.toLowerCase().indexOf("512") >= 0) {
                    kilobyteBlock = false;
                }
            } finally {
                IOUtils.closeQuietly(r);
            }
            
            // now perform the test
            long free = FileSystemUtils.freeSpace("/");
            long kb = FileSystemUtils.freeSpaceKb("/");
            if (kilobyteBlock) {
                assertEquals((double) free, (double) kb, 256d);
            } else {
                assertEquals((double) free / 2d, (double) kb, 256d);
            }
        } else {
            long bytes = FileSystemUtils.freeSpace("");
            long kb = FileSystemUtils.freeSpaceKb("");
            assertEquals((double) bytes / 1024, (double) kb, 256d);
        }
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceOS_String_NullPath() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS(null, 1, false);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceOS(null, 1, true);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testGetFreeSpaceOS_String_InitError() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS("", -1, false);
            fail();
        } catch (IllegalStateException ex) {}
        try {
            fsu.freeSpaceOS("", -1, true);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Other() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS("", 0, false);
            fail();
        } catch (IllegalStateException ex) {}
        try {
            fsu.freeSpaceOS("", 0, true);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Windows() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            protected long freeSpaceWindows(String path) throws IOException {
                return 12345L;
            }
        };
        assertEquals(12345L, fsu.freeSpaceOS("", 1, false));
        assertEquals(12345L / 1024, fsu.freeSpaceOS("", 1, true));
    }

    public void testGetFreeSpaceOS_String_Unix() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            protected long freeSpaceUnix(String path, boolean kb) throws IOException {
                return (kb ? 12345L : 54321);
            }
        };
        assertEquals(54321L, fsu.freeSpaceOS("", 2, false));
        assertEquals(12345L, fsu.freeSpaceOS("", 2, true));
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceWindows_String_ParseCommaFormatBytes() throws Exception {
        // this is the format of response when calling dir /c
        // we have now switched to dir /-c, so we should never get this
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
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.freeSpaceWindows(""));
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
            "               7 File(s)         180260 bytes\n" +
            "              10 Dir(s)     41411551232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /-c ", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.freeSpaceWindows(""));
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
            "               7 File(s)         180260 bytes\n" +
            "              10 Dir(s)     41411551232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /-c C:", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.freeSpaceWindows("C:"));
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
            "               7 File(s)         180260 bytes\n" +
            "              10 Dir(s)     41411551232 bytes free";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                assertEquals("dir /-c C:", params[2]);
                return new BufferedReader(reader);
            }
        };
        assertEquals(41411551232L, fsu.freeSpaceWindows("C:\\somedir"));
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
            fsu.freeSpaceWindows("C:");
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
            fsu.freeSpaceUnix("", false);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceUnix("", true);
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
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", false));
    }

    public void testGetFreeSpaceUnix_String_NormalResponseKb() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   1472504  90% /home/users/s";
        final StringReader reader = new StringReader(lines);
        FileSystemUtils fsu = new FileSystemUtils() {
            protected BufferedReader openProcessStream(String[] params) {
                return new BufferedReader(reader);
            }
        };
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", true));
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
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", false));
    }

    public void testGetFreeSpaceUnix_String_LongResponseKb() throws Exception {
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
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", true));
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
            fsu.freeSpaceUnix("/home/users/s", false);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true);
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
            fsu.freeSpaceUnix("/home/users/s", false);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true);
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
            fsu.freeSpaceUnix("/home/users/s", false);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true);
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
            fsu.freeSpaceUnix("/home/users/s", false);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true);
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
            fsu.freeSpaceUnix("/home/users/s", false);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true);
            fail();
        } catch (IOException ex) {}
    }

}
