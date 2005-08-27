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
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
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
 * @since Commons IO 1.1
 */
public class FileSystemUtils {

    /** Singleton instance, used mainly for testing. */
    private static final FileSystemUtils INSTANCE = new FileSystemUtils();

    /** Operating system state flag for error. */
    private static final int INIT_PROBLEM = -1;
    /** Operating system state flag for neither Unix nor Windows. */
    private static final int OTHER = 0;
    /** Operating system state flag for Windows. */
    private static final int WINDOWS = 1;
    /** Operating system state flag for Unix. */
    private static final int UNIX = 2;

    /** The operating system flag. */
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
        super();
    }

    /**
     * Returns the free space on a drive or volume in a cross-platform manner.
     * Note that some OS's are NOT currently supported, including OS/390.
     * <pre>
     * FileSystemUtils.getFreeSpace("C:");  // Windows
     * FileSystemUtils.getFreeSpace("/volume");  // *nix
     * </pre>
     * The free space is calculated via the command line.
     * It uses 'dir /-c' on Windows and 'df' on *nix.
     *
     * @param path  the path to get free space for, not null, not empty on Unix
     * @return the amount of free drive space on the drive or volume
     * @throws IllegalArgumentException if the path is invalid
     * @throws IllegalStateException if an error occurred in initialisation
     * @throws IOException if an error occurs when finding the free space
     */
    public static long getFreeSpace(String path) throws IOException {
        return INSTANCE.getFreeSpaceOS(path, OS);
    }

    /**
     * Returns the free space on a drive or volume in a cross-platform manner.
     * Note that some OS's are NOT currently supported, including OS/390.
     * <pre>
     * FileSystemUtils.getFreeSpace("C:");  // Windows
     * FileSystemUtils.getFreeSpace("/volume");  // *nix
     * </pre>
     * The free space is calculated via the command line.
     * It uses 'dir /-c' on Windows and 'df' on *nix.
     *
     * @param path  the path to get free space for, not null, not empty on Unix
     * @return the amount of free drive space on the drive or volume
     * @throws IllegalArgumentException if the path is invalid
     * @throws IllegalStateException if an error occurred in initialisation
     * @throws IOException if an error occurs when finding the free space
     */
    protected long getFreeSpaceOS(String path, int os) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        switch (os) {
            case WINDOWS:
                return getFreeSpaceWindows(path);
            case UNIX:
                return getFreeSpaceUnix(path);
            case OTHER:
                throw new IllegalStateException("Unsupported operating system");
            default:
                throw new IllegalStateException(
                  "Exception caught when determining operating system");
        }
    }

    /**
     * Find free space on the Windows platform using the 'dir' command.
     *
     * @param path  the path to get free space for, including the colon
     * @return the amount of free drive space on the drive
     * @throws IOException if an error occurs
     */
    protected long getFreeSpaceWindows(String path) throws IOException {
        path = FilenameUtils.normalize(path);
        if (path.length() > 2 && path.charAt(1) == ':') {
            path = path.substring(0, 2);
        }

        // build and run the 'dir' command
        String[] cmdAttrbs = new String[] {"cmd.exe", "/C", "dir /c " + path};

        // read in the output of the command to an ArrayList
        BufferedReader in = null;
        String line = null;
        ArrayList lines = new ArrayList();
        try {
            in = openProcessStream(cmdAttrbs);
            line = in.readLine();
            while (line != null) {
                line = line.toLowerCase().trim();
                lines.add(line);
                line = in.readLine();
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        if (lines.size() == 0) {
            // unknown problem, throw exception
            throw new IOException(
                    "Command line 'dir /c' did not return any info " +
                    "for command '" + cmdAttrbs[2] + "'");
        }

        // now iterate over the lines we just read and find the LAST
        // non-empty line (the free space bytes should be in the last element
        // of the ArrayList anyway, but this will ensure it works even if it's
        // not, still assuming it is on the last non-blank line)
        long bytes = -1;
        int i = lines.size() - 1;
        int bytesStart = 0;
        int bytesEnd = 0;
        outerLoop: while (i > 0) {
            line = (String) lines.get(i);
            if (line.length() > 0) {
                // found it, so now read from the end of the line to find the
                // last numeric character on the line, then continue until we
                // find the first non-numeric character, and everything between
                // that and the last numeric character inclusive is our free
                // space bytes count
                int j = line.length() - 1;
                innerLoop1: while (j >= 0) {
                    char c = line.charAt(j);
                    if (Character.isDigit(c)) {
                      // found the last numeric character, this is the end of
                      // the free space bytes count
                      bytesEnd = j + 1;
                      break innerLoop1;
                    }
                    j--;
                }
                innerLoop2: while (j >= 0) {
                    char c = line.charAt(j);
                    if (!Character.isDigit(c) && c != ',' && c != '.') {
                      // found the next non-numeric character, this is the
                      // beginning of the free space bytes count
                      bytesStart = j + 1;
                      break innerLoop2;
                    }
                    j--;
                }
                break outerLoop;
            }
        }

        // remove commas and dots in the bytes count
        StringBuffer buf = new StringBuffer(line.substring(bytesStart, bytesEnd));
        for (int k = 0; k < buf.length(); k++) {
            if (buf.charAt(k) == ',' || buf.charAt(k) == '.') {
                buf.deleteCharAt(k--);
            }
        }
        bytes = Long.parseLong(buf.toString());
        return bytes;
    }

    /**
     * Find free space on the *nix platform using the 'df' command.
     *
     * @param path  the path to get free space for
     * @return the amount of free drive space on the volume
     * @throws IOException if an error occurs
     */
    protected long getFreeSpaceUnix(String path) throws IOException {
        if (path.length() == 0) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        path = FilenameUtils.normalize(path);

        // build and run the 'dir' command
        String[] cmdAttribs = new String[] {"df", path};

        // read the output from the command until we come to the second line
        long bytes = -1;
        BufferedReader in = null;
        try {
            in = openProcessStream(cmdAttribs);
            String line1 = in.readLine(); // header line (ignore it)
            String line2 = in.readLine(); // the line we're interested in
            String line3 = in.readLine(); // possibly interesting line
            if (line2 == null) {
                // unknown problem, throw exception
                throw new IOException(
                        "Command line 'df' did not return info as expected " +
                        "for path '" + path +
                        "'- response on first line was '" + line1 + "'");
            }
            line2 = line2.trim();

            // Now, we tokenize the string. The fourth element is what we want.
            StringTokenizer tok = new StringTokenizer(line2, " ");
            if (tok.countTokens() < 4) {
                // could be long Filesystem, thus data on third line
                if (tok.countTokens() == 1 && line3 != null) {
                    line3 = line3.trim();
                    tok = new StringTokenizer(line3, " ");
                } else {
                    throw new IOException(
                            "Command line 'df' did not return data as expected " +
                            "for path '" + path + "'- check path is valid");
                }
            } else {
                tok.nextToken(); // Ignore Filesystem
            }
            tok.nextToken(); // Ignore 1K-blocks
            tok.nextToken(); // Ignore Used
            String freeSpace = tok.nextToken();
            try {
                bytes = Long.parseLong(freeSpace);
            } catch (NumberFormatException ex) {
                throw new IOException(
                        "Command line 'df' did not return numeric data as expected " +
                        "for path '" + path + "'- check path is valid");
            }

        } finally {
            IOUtils.closeQuietly(in);
        }

        if (bytes < 0) {
            throw new IOException(
                    "Command line 'df' did not find free space in response " +
                    "for path '" + path + "'- check path is valid");
        }
        return bytes;
    }

    /**
     * Opens the stream to be operating system.
     *
     * @param params  the command parameters
     * @return a reader
     */
    protected BufferedReader openProcessStream(String[] params) throws IOException {
        Process proc = Runtime.getRuntime().exec(params);
        return new BufferedReader(
            new InputStreamReader(proc.getInputStream()));
    }

}
