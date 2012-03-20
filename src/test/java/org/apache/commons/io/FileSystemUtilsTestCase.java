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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.commons.io.testtools.FileBasedTestCase;

/**
 * This is used to test FileSystemUtils.
 *
 * @version $Id$
 */
public class FileSystemUtilsTestCase extends FileBasedTestCase {

    public FileSystemUtilsTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpace_String() throws Exception {
        // test coverage, as we can't check value
        if (File.separatorChar == '/') {
            // have to figure out unix block size
            String[] cmd = null;
            String osName = System.getProperty("os.name");
            osName = osName.toLowerCase(Locale.ENGLISH);

            if (osName.indexOf("hp-ux") >= 0 || osName.indexOf("aix") >= 0) {
                cmd = new String[] {"df", "-P", "/"};
            } else if (osName.indexOf("sunos") >= 0 || osName.indexOf("sun os") >= 0
                       || osName.indexOf("solaris") >= 0) {
                cmd = new String[] {"/usr/xpg4/bin/df", "-P", "/"};
            } else {
                cmd = new String[] {"df", "/"};
            }
            Process proc = Runtime.getRuntime().exec(cmd);
            boolean kilobyteBlock = true;
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line = r.readLine();
                Assert.assertNotNull("Unexpected null line", line);
                if (line.indexOf("512") >= 0) {
                    kilobyteBlock = false;
                }
            } finally {
                IOUtils.closeQuietly(r);
            }
            
            // now perform the test
            @SuppressWarnings("deprecation")
            long free = FileSystemUtils.freeSpace("/");
            long kb = FileSystemUtils.freeSpaceKb("/");
            if (kilobyteBlock) {
                assertEquals(free, kb, 256d);
            } else {
                assertEquals(free / 2d, kb, 256d);
            }
        } else {
            @SuppressWarnings("deprecation")
            long bytes = FileSystemUtils.freeSpace("");
            long kb = FileSystemUtils.freeSpaceKb("");
            assertEquals((double) bytes / 1024, kb, 256d);
        }
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceOS_String_NullPath() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS(null, 1, false, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceOS(null, 1, true, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    public void testGetFreeSpaceOS_String_InitError() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS("", -1, false, -1);
            fail();
        } catch (IllegalStateException ex) {}
        try {
            fsu.freeSpaceOS("", -1, true, -1);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Other() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils();
        try {
            fsu.freeSpaceOS("", 0, false, -1);
            fail();
        } catch (IllegalStateException ex) {}
        try {
            fsu.freeSpaceOS("", 0, true, -1);
            fail();
        } catch (IllegalStateException ex) {}
    }

    public void testGetFreeSpaceOS_String_Windows() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            @Override
            protected long freeSpaceWindows(String path, long timeout) throws IOException {
                return 12345L;
            }
        };
        assertEquals(12345L, fsu.freeSpaceOS("", 1, false, -1));
        assertEquals(12345L / 1024, fsu.freeSpaceOS("", 1, true, -1));
    }

    public void testGetFreeSpaceOS_String_Unix() throws Exception {
        FileSystemUtils fsu = new FileSystemUtils() {
            @Override
            protected long freeSpaceUnix(String path, boolean kb, boolean posix, long timeout) throws IOException {
                return kb ? 12345L : 54321;
            }
        };
        assertEquals(54321L, fsu.freeSpaceOS("", 2, false, -1));
        assertEquals(12345L, fsu.freeSpaceOS("", 2, true, -1));
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
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(41411551232L, fsu.freeSpaceWindows("", -1));
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
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines, "dir /a /-c ");
        assertEquals(41411551232L, fsu.freeSpaceWindows("", -1));
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
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines, "dir /a /-c \"C:\"");
        assertEquals(41411551232L, fsu.freeSpaceWindows("C:", -1));
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
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines, "dir /a /-c \"C:\\somedir\"");
        assertEquals(41411551232L, fsu.freeSpaceWindows("C:\\somedir", -1));
    }

    public void testGetFreeSpaceWindows_String_quoted() throws Exception {
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
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines, "dir /a /-c \"C:\\somedir\"");
        assertEquals(41411551232L, fsu.freeSpaceWindows("\"C:\\somedir\"", -1));
    }

    public void testGetFreeSpaceWindows_String_EmptyResponse() throws Exception {
        String lines = "";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceWindows("C:", -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceWindows_String_EmptyMultiLineResponse() throws Exception {
        String lines = "\n\n";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceWindows("C:", -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceWindows_String_InvalidTextResponse() throws Exception {
        String lines = "BlueScreenOfDeath";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceWindows("C:", -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceWindows_String_NoSuchDirectoryResponse() throws Exception {
        String lines =
            " Volume in drive C is HDD\n" +
            " Volume Serial Number is XXXX-YYYY\n" +
            "\n" +
            " Directory of C:\\Documents and Settings\\empty" +
            "\n";
        FileSystemUtils fsu = new MockFileSystemUtils(1, lines);
        try {
            fsu.freeSpaceWindows("C:", -1);
            fail();
        } catch (IOException ex) {}
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceUnix_String_EmptyPath() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   1472504  90% /home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("", false, false, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceUnix("", true, false, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceUnix("", true, true, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            fsu.freeSpaceUnix("", false, true, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        
    }

    public void testGetFreeSpaceUnix_String_NormalResponseLinux() throws Exception {
        // from Sourceforge 'GNU bash, version 2.05b.0(1)-release (i386-redhat-linux-gnu)'
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "/dev/xxx                497944    308528    189416  62% /";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(189416L, fsu.freeSpaceUnix("/", false, false, -1));
    }

    public void testGetFreeSpaceUnix_String_NormalResponseFreeBSD() throws Exception {
        // from Apache 'FreeBSD 6.1-RELEASE (SMP-turbo)'
        String lines =
            "Filesystem  1K-blocks      Used    Avail Capacity  Mounted on\n" +
            "/dev/xxxxxx    128990    102902    15770    87%    /";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(15770L, fsu.freeSpaceUnix("/", false, false, -1));
    }

    //-----------------------------------------------------------------------
    public void testGetFreeSpaceUnix_String_NormalResponseKbLinux() throws Exception {
        // from Sourceforge 'GNU bash, version 2.05b.0(1)-release (i386-redhat-linux-gnu)'
        // df, df -k and df -kP are all identical
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "/dev/xxx                497944    308528    189416  62% /";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(189416L, fsu.freeSpaceUnix("/", true, false, -1));
    }

    public void testGetFreeSpaceUnix_String_NormalResponseKbFreeBSD() throws Exception {
        // from Apache 'FreeBSD 6.1-RELEASE (SMP-turbo)'
        // df and df -k are identical, but df -kP uses 512 blocks (not relevant as not used)
        String lines =
            "Filesystem  1K-blocks      Used    Avail Capacity  Mounted on\n" +
            "/dev/xxxxxx    128990    102902    15770    87%    /";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(15770L, fsu.freeSpaceUnix("/", true, false, -1));
    }

    public void testGetFreeSpaceUnix_String_NormalResponseKbSolaris() throws Exception {
        // from IO-91 - ' SunOS et 5.10 Generic_118822-25 sun4u sparc SUNW,Ultra-4'
        // non-kb response does not contain free space - see IO-91
        String lines =
            "Filesystem            kbytes    used   avail capacity  Mounted on\n" +
            "/dev/dsk/x0x0x0x0    1350955  815754  481163    63%";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(481163L, fsu.freeSpaceUnix("/dev/dsk/x0x0x0x0", true, false, -1));
    }

    public void testGetFreeSpaceUnix_String_LongResponse() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx-yyyyyyy-zzz:/home/users/s\n" +
            "                      14428928  12956424   1472504  90% /home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", false, false, -1));
    }

    public void testGetFreeSpaceUnix_String_LongResponseKb() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx-yyyyyyy-zzz:/home/users/s\n" +
            "                      14428928  12956424   1472504  90% /home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        assertEquals(1472504L, fsu.freeSpaceUnix("/home/users/s", true, false, -1));
    }

    public void testGetFreeSpaceUnix_String_EmptyResponse() throws Exception {
        String lines = "";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("/home/users/s", false, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", false, true, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, true, -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse1() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "                      14428928  12956424       100";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("/home/users/s", false, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", false, true, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, true, -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse2() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424   nnnnnnn  90% /home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("/home/users/s", false, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", false, true, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, true, -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse3() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx:/home/users/s     14428928  12956424        -1  90% /home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("/home/users/s", false, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", false, true, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, true, -1);
            fail();
        } catch (IOException ex) {}
    }

    public void testGetFreeSpaceUnix_String_InvalidResponse4() throws Exception {
        String lines =
            "Filesystem           1K-blocks      Used Available Use% Mounted on\n" +
            "xxx-yyyyyyy-zzz:/home/users/s";
        FileSystemUtils fsu = new MockFileSystemUtils(0, lines);
        try {
            fsu.freeSpaceUnix("/home/users/s", false, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, false, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", false, true, -1);
            fail();
        } catch (IOException ex) {}
        try {
            fsu.freeSpaceUnix("/home/users/s", true, true, -1);
            fail();
        } catch (IOException ex) {}
    }

    //-----------------------------------------------------------------------
    static class MockFileSystemUtils extends FileSystemUtils {
        private final int exitCode;
        private final byte[] bytes;
        private final String cmd;
        public MockFileSystemUtils(int exitCode, String lines) {
            this(exitCode, lines, null);
        }
        public MockFileSystemUtils(int exitCode, String lines, String cmd) {
            this.exitCode = exitCode;
            this.bytes = lines.getBytes();
            this.cmd = cmd;
        }
        @Override
        Process openProcess(String[] params) {
            if (cmd != null) {
                assertEquals(cmd, params[params.length - 1]);
            }
            return new Process() {
                @Override
                public InputStream getErrorStream() {
                    return null;
                }
                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(bytes);
                }
                @Override
                public OutputStream getOutputStream() {
                    return null;
                }
                @Override
                public int waitFor() throws InterruptedException {
                    return exitCode;
                }
                @Override
                public int exitValue() {
                    return exitCode;
                }
                @Override
                public void destroy() {
                }
            };
        }
    }

}
