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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * General File System utilities.
 * <p>
 * This class provides static utility methods for general file system
 * functions.
 *
 * @author Frank W. Zammetti
 * @author Stephen Colebourne
 * @version $Id$
 */
public final class FileSystemUtils {

    private static final int INIT_PROBLEM = -1;
    private static final int OTHER = 0;
    private static final int WINDOWS = 1;
    private static final int UNIX = 2;

    private static final int OS;
    static {
        int os = OTHER;
        try {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                throw new IOException("os.name not found");
            }
            osName = osName.toLowerCase();
            // match
            if (osName.indexOf("windows") != -1) {
                os = WINDOWS;
            } else if (osName.indexOf("linux") != -1 ||
                osName.indexOf("sun os") != -1 ||
                osName.indexOf("sunos") != -1 ||
                osName.indexOf("solaris") != -1 ||
                osName.indexOf("mpe/ix") != -1 ||
                osName.indexOf("hp-ux") != -1 ||
                osName.indexOf("aix") != -1 ||
                osName.indexOf("freebsd") != -1 ||
                osName.indexOf("irix") != -1 ||
                osName.indexOf("digital unix") != -1 ||
                osName.indexOf("unix") != -1 ||
                osName.indexOf("mac os x") != -1) {
                os = UNIX;
            } else {
                os = OTHER;
            }
            
        } catch (Exception ex) {
            os = INIT_PROBLEM;
        }
        OS = os;
    }

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileSystemUtils() {
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the free space on a drive or volume in a cross-platform manner.
     * Note that some OS's are NOT currently supported, including OS/390.
     * <pre>
     * FileSystemUtils.getFreeSpace("C:");  // Windows
     * FileSystemUtils.getFreeSpace("/volume");  // *nix
     * </pre>
     * The free space is calculated via the command line.
     * It uses 'dir /-c' on Windows and 'df' on Unix.
     *
     * @param path  the path to get free space for
     * @return the amount of free drive space on the drive or volume
     * @throws IllegalArgumentException if the path is invalid
     * @throws IllegalStateException if an error occurred in initialisation
     * @throws IOException if an error occurs when finding the free space
     */
    public static long getFreeSpace(String path) throws IOException {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        switch (OS) {
            case WINDOWS:
                return getFreeSpaceWindows(path);
            case UNIX:
                return getFreeSpaceUnix(path);
            case OTHER:
                throw new IllegalStateException("Unsupported operating system");
            default:
                throw new IllegalStateException("Exception caught when determining operating system");
        }
    }

    /**
     * Find free space on the Windows platform using the 'dir' command.
     *
     * @param path  the path to get free space for, including the colon
     * @return the amount of free drive space on the drive
     * @throws IOException if an error occurs
     */
    private static long getFreeSpaceWindows(String path) throws IOException {
        // build and run the 'dir' command
        String line = null;
        String[] cmdAttrbs = new String[3];
        cmdAttrbs[0] = "cmd.exe";
        cmdAttrbs[1] = "/C";
        cmdAttrbs[2] = "dir /-c " + path;
        Process proc = Runtime.getRuntime().exec(cmdAttrbs);

        // read the output until we find the line with 'bytes free'
        long bytes = -1;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            line = in.readLine();
            while (line != null) {
                line = line.toLowerCase();
                int bfl = line.indexOf("bytes free");
                if (bfl != -1) {
                    // found "bytes free"
                    // chop off everything AFTER the actual byte count
                    line = line.substring(0, bfl);
                    line = line.trim();
                    // find the LAST space in the string, should be right before the
                    // byte count
                    int lsl = line.lastIndexOf(' ');
                    // now get rid of everything BEFORE that space, and line will
                    // then contain just the byte count
                    line = line.substring(lsl + 1);
                    bytes = Long.parseLong(line);
                }
                line = in.readLine();
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        if (bytes == -1) {
            throw new IOException(
                    "Command line 'dir' did not find text 'bytes free' in response for path '" +
                    path + "'- check path is of the form 'C:'");
        }
        return bytes;
    }

    /**
     * Find free space on the Nix platform using the 'df' command.
     *
     * @param path  the path to get free space for
     * @return the amount of free drive space on the volume
     * @throws IOException if an error occurs
     */
    private static long getFreeSpaceUnix(String path) throws IOException {
        // build and run the 'dir' command
        String[] cmdAttrbs = new String[3];
        cmdAttrbs[0] = "cmd.exe";
        cmdAttrbs[1] = "/C";
        cmdAttrbs[2] = "df " + path;
        Process proc = Runtime.getRuntime().exec(cmdAttrbs);

        // read the output from the command until we come to the second line
        long bytes = -1;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line1 = in.readLine(); // header line (ignore it)
            String line2 = in.readLine(); // the line we're interested in
            if (line2 == null) {
                // unknown problem, throw exception
                throw new IOException(
                        "Command line 'df' did not return info as expected for path '" +
                        path + "'- response on first line was '" + line1 + '"');
            }
            line2 = line2.trim();

            // Now, we tokenize the string. The fourth element is what we want.
            StringTokenizer tok = new StringTokenizer(line2, " ");
            if (tok.countTokens() < 4) {
                throw new IOException(
                        "Command line 'df' did not return data as expected for path '" +
                        path + "'- check path is valid");
            }
            tok.nextToken(); // Ignore Filesystem
            tok.nextToken(); // Ignore 1K-blocks
            tok.nextToken(); // Ignore Used
            String freeSpace = tok.nextToken();
            bytes = Long.parseLong(freeSpace);

        } finally {
            IOUtils.closeQuietly(in);
        }

        if (bytes == -1) {
            throw new IOException(
                    "Command line 'df' did not find free space in response for path '" +
                    path + "'- check path is valid");
        }
        return bytes;
    }

}
