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

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This is used to test FileSystemUtils for correctness.
 *
 * @author Stephen Colebourne
 * @version $Id$
 */
public class FileSystemUtilsChecker {

    public static void main(String[] args) {
        try {
            outputFreeSpaceCDrive();
            outputFreeSpaceUnix();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private static void outputFreeSpaceCDrive() throws IOException {
        System.out.println(FileSystemUtils.getFreeSpace("C:"));
    }

    private static void outputFreeSpaceUnix() throws Exception {
        // copied from FileSystemUtils, with some genuine UNIX data
        // as I run a Windows box
        String path = "/usr";
        long bytes = -1;
        
        String line1 = "Filesystem  1K-blocks    Used   Avail Capacity  Mounted on";
        String line2 = "/dev/da0s1g   8064542 2930306 4489074    39%    /usr";
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
        System.out.println("" + bytes);
    }
}
