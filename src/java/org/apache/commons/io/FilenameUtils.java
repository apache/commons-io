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

import java.io.File;
import java.io.IOException;

/**
 * Common {@link java.io.File} manipulation routines through
 * use of a filename/path.
 *
 * <h3>Path-related methods</h3>
 *
 * <p>Methods exist to retrieve the components of a typical file path. For
 * example <code>/www/hosted/mysite/index.html</code>, can be broken into:
 * <ul>
 *   <li><code>/www/hosted/mysite/</code> -- retrievable through
 *       {@link #getPath}</li>
 *   <li><code>index.html</code> -- retrievable through {@link #removePath}</li>
 *   <li><code>/www/hosted/mysite/index</code> -- retrievable through
 *       {@link #removeExtension}</li>
 *   <li><code>html</code> -- retrievable through {@link #getExtension}</li>
 * </ul>
 * There are also methods to {@link #catPath concatenate two paths},
 * {@link #resolveFile resolve a path relative to a File} and
 * {@link #normalize} a path.
 * </p>
 *
 * <h3>Origin of code</h3>
 * <ul>
 *   <li>commons-utils repo</li>
 *   <li>Alexandria's FileUtils.</li>
 *   <li>Avalon Excalibur's IO.</li>
 * </ul>
 *
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author Matthew Hawthorne
 * @author Martin Cooper
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: FilenameUtils.java,v 1.16 2004/10/29 18:53:56 bayard Exp $
 * @since Commons IO 1.1
 */
public class FilenameUtils {

    /**
     * Standard separator char used when internalizing paths.
     */
    private static final char INTERNAL_SEPARATOR_CHAR = '/';

    /**
     * Standard separator string used when internalizing paths.
     */
    // KILL
    private static final String INTERNAL_SEPARATOR = "/";

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FilenameUtils() { }

    /**
     * Check if a file exits.
     *
     * @param fileName The name of the file to check.
     * @return true if file exists.
     */
    // KILL: Not filename based
    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }



    /**
     * Deletes a file.
     *
     * @param fileName The name of the file to delete.
     */
    // KILL: Not filename based
    public static void fileDelete(String fileName) {
        File file = new File(fileName);
        file.delete();
    }

    /**
     * Simple way to make a directory. It also creates the parent directories
     * if necessary.
     * @param dir directory to create
     */
    // KILL: Not filename based
    public static void mkdir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * Remove extension from filename.
     * ie
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg --> a\b\c
     * a\b\c     --> a\b\c
     * a.b\c        --> a.b\c
     * </pre>
     *
     * @param filename the filename
     * @return the filename minus extension
     */
    public static String removeExtension(String filename) {
        String ext = getExtension(filename);
        int index = ext.length();
        if (index > 0) {
            // include the . in the count
            index++;
        }
        index = filename.length() - index;
        return filename.substring(0, index);
    }

   /**
     * Gets the extension of a filename.
     * <p>
     * eg
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b/c        --> ""
     * a.b/c.txt    --> "txt"
     * a.b/c        --> ""
     * </pre>
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of filename or an empty string if none exists.
     */
    public static String getExtension(String filename) {
        String suffix = "";
        String shortFilename = filename;
        String ifilename = internalize(filename);

        int lastDirSeparator = ifilename.lastIndexOf(INTERNAL_SEPARATOR_CHAR);
        if (lastDirSeparator > 0) {
            shortFilename = ifilename.substring(lastDirSeparator + 1);
        }

        int index = shortFilename.lastIndexOf('.');

        if (index > 0 && index < shortFilename.length() - 1) {
            suffix = shortFilename.substring(index + 1);
        }

        return suffix;
    }

    /**
     * Remove path from filename. Equivalent to the unix command
     * <code>basename</code>.
     * ie.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * </pre>
     *
     * @param filepath the filepath
     * @return the filename minus path
     */
    // KILL? Just use StringUtils?
    public static String removePath(String filepath) {
        return removePath(filepath, File.separatorChar);
    }

    /**
     * Remove path from filename.
     * ie.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * </pre>
     *
     * @param filepath the filepath
     * @param fileSeparatorChar the file separator character to use
     * @return the filename minus path
     */
    // KILL: Why allow the char to be specified?
    public static String removePath(
        String filepath,
        char fileSeparatorChar) {
        int index = filepath.lastIndexOf(fileSeparatorChar);

        if (-1 == index) {
            return filepath;
        } else {
            return filepath.substring(index + 1);
        }
    }

    /**
     * Get path from filename. Roughly equivalent to the unix command
     * <code>dirname</code>.
     * ie.
     * <pre>
     * a/b/c.txt --> a/b
     * a.txt     --> ""
     * </pre>
     *
     * @param filepath the filepath
     * @return the filename minus path
     */
    // KILL? Just use StringUtils?
    public static String getPath(String filepath) {
        return getPath(filepath, File.separatorChar);
    }

    /**
     * Get path from filename.
     * ie.
     * <pre>
     * a/b/c.txt --> a/b
     * a.txt     --> ""
     * </pre>
     *
     * @param filepath the filepath
     * @param fileSeparatorChar the file separator character to use
     * @return the filename minus path
     */
    // KILL: Why allow the char to be specified?
    public static String getPath(
        String filepath,
        char fileSeparatorChar) {
        int index = filepath.lastIndexOf(fileSeparatorChar);
        if (-1 == index) {
            return "";
        } else {
            return filepath.substring(0, index);
        }
    }



    /**
     * Normalize a path.
     * Eliminates "/../" and "/./" in a string. Returns <code>null</code> if
     * the ..'s went past the root.
     * Eg:
     * <pre>
     * /foo//               -->     /foo/
     * /foo/./              -->     /foo/
     * /foo/../bar          -->     /bar
     * /foo/../bar/         -->     /bar/
     * /foo/../bar/../baz   -->     /baz
     * //foo//./bar         -->     /foo/bar
     * /../                 -->     null
     * </pre>
     *
     * @param path the path to normalize
     * @return the normalized String, or <code>null</code> if too many ..'s.
     */
    // TODO: Make this non-unix specific
    public static String normalize(String path) {
        String normalized = path;
        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0) {
                break;
            }
            normalized =
                normalized.substring(0, index)
                    + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0) {
                break;
            }
            normalized =
                normalized.substring(0, index)
                    + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0) {
                break;
            }
            if (index == 0) {
                return null; // Trying to go outside our context
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized =
                normalized.substring(0, index2)
                    + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return normalized;
    }

    /**
     * Will concatenate 2 paths. Paths with <code>..</code> will be
     * properly handled. The path separator between the 2 paths is the
     * system default path separator.
     *
     * <p>Eg. on UNIX,<br />
     * <code>/a/b/c</code> + <code>d</code> = <code>/a/b/d</code><br />
     * <code>/a/b/c</code> + <code>../d</code> = <code>/a/d</code><br />
     * </p>
     *
     * <p>Eg. on Microsoft Windows,<br />
     * <code>C:\a\b\c</code> + <code>d</code> = <code>C:\a\b\d</code><br />
     * <code>C:\a\b\c</code> + <code>..\d</code> = <code>C:\a\d</code><br />
     * <code>/a/b/c</code> + <code>d</code> = <code>/a/b\d</code><br />
     * </p>
     *
     * Thieved from Tomcat sources...
     *
     * @param lookupPath the base path to attach to
     * @param path path the second path to attach to the first
     * @return The concatenated paths, or null if error occurs
     */
    // TODO: UNIX/Windows only. Is this a problem?
    public static String catPath(String lookupPath, String path) {
        // Cut off the last slash and everything beyond
        int index = indexOfLastPathSeparator(lookupPath);
        String lookup = lookupPath.substring(0, index);
        String pth = path;

        // Deal with .. by chopping dirs off the lookup path
        while (pth.startsWith("../") || pth.startsWith("..\\")) {
            if (lookup.length() > 0) {
                index = indexOfLastPathSeparator(lookup);
                lookup = lookup.substring(0, index);
            } else {
                // More ..'s than dirs, return null
                return null;
            }

            pth = pth.substring(3);
        }

        return new StringBuffer(lookup).
                append(File.separator).append(pth).toString();
    }

    /**
     * Return the index of the last 'path separator' character. The 'path
     * separator' character is '/' for UNIX systems and '\' for Microsoft
     * Windows systems.
     *
     * @param path The path to find the last path separator in
     * @return The index of the last 'path separator' character, or -1 if there
     * is no such character.
     */
    // KILL: Inline into above method
    public static int indexOfLastPathSeparator(String path) {
        int lastUnixPos = path.lastIndexOf('/');
        int lastWindowsPos = path.lastIndexOf('\\');
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Resolve a file <code>filename</code> to it's canonical form. If
     * <code>filename</code> is relative (doesn't start with <code>/</code>),
     * it will be resolved relative to <code>baseFile</code>, otherwise it is
     * treated as a normal root-relative path.
     *
     * @param baseFile Where to resolve <code>filename</code> from, if
     * <code>filename</code> is relative.
     * @param filename Absolute or relative file path to resolve.
     * @return The canonical <code>File</code> of <code>filename</code>.
     */
    // TODO: Decide whether this is worth keeping?
    public static File resolveFile(File baseFile, String filename) {
        String filenm = filename;
        if ('/' != File.separatorChar) {
            filenm = filename.replace('/', File.separatorChar);
        }

        if ('\\' != File.separatorChar) {
            filenm = filename.replace('\\', File.separatorChar);
        }

        // deal with absolute files
        if (filenm.startsWith(File.separator)) {
            File file = new File(filenm);

            try {
                file = file.getCanonicalFile();
            } catch (IOException ioe) {
                // ignore
            }

            return file;
        }
        // FIXME: I'm almost certain this // removal is unnecessary, as
        // getAbsoluteFile() strips
        // them. However, I'm not sure about this UNC stuff. (JT)
        char[] chars = filename.toCharArray();
        StringBuffer sb = new StringBuffer();

        //remove duplicate file separators in succession - except
        //on win32 at start of filename as UNC filenames can
        //be \\AComputer\AShare\myfile.txt
        int start = 0;
        if ('\\' == File.separatorChar) {
            sb.append(filenm.charAt(0));
            start++;
        }

        for (int i = start; i < chars.length; i++) {
            boolean doubleSeparator =
                File.separatorChar == chars[i]
                    && File.separatorChar == chars[i - 1];

            if (!doubleSeparator) {
                sb.append(chars[i]);
            }
        }

        filenm = sb.toString();

        //must be relative
        File file = (new File(baseFile, filenm)).getAbsoluteFile();

        try {
            file = file.getCanonicalFile();
        } catch ( IOException ioe) {
            ;
        }

        return file;
    }

    /**
     * Convert all separators to the internal form. This allows manipulation
     * of paths without concern for which separators are used within them.
     * @param path The path to be internalized.
     * @return The internalized path.
     */
    // KILL: Inline into the one place this is used
    private static String internalize(String path) {
        return path.replace('\\', INTERNAL_SEPARATOR_CHAR);
    }

    /**
     * Convert all separators to their external form. That is, ensure that all
     * separators are the same as File.separator.
     * @param path The path to be externalized.
     * @return The externalized path.
     */
    // KILL: Nothing uses this
    private static String externalize(String path) {
        if (INTERNAL_SEPARATOR_CHAR != File.separatorChar) {
            path = path.replace(INTERNAL_SEPARATOR_CHAR, File.separatorChar);
        }
        return path;
    }

}
