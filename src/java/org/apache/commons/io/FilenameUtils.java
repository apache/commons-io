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
 * Utility class that provides methods to manipulate filenames and filepaths.
 * <p>
 * This class defines three basic components within a filename (example C:\dev\file.txt):
 * <ul>
 * <li>the path - C:\dev
 * <li>the name - file.txt
 * <li>the extension - txt
 * </ul>
 * The class only supports Unix and Windows style names.
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
 * @author Stephen Colebourne
 * @version $Id: FilenameUtils.java,v 1.23 2004/10/30 23:23:54 scolebourne Exp $
 * @since Commons IO 1.1
 */
public class FilenameUtils {

    /**
     * The extension separator character.
     */
    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FilenameUtils() { }

    //-----------------------------------------------------------------------
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
     * @todo Make this non-unix specific
     */
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
     // TODO UNIX/Windows only. Is this a problem?
    public static String catPath(String lookupPath, String path) {
        // Cut off the last slash and everything beyond
        int index = indexOfLastSeparator(lookupPath);
        String lookup = lookupPath.substring(0, index);
        String pth = path;

        // Deal with .. by chopping dirs off the lookup path
        while (pth.startsWith("../") || pth.startsWith("..\\")) {
            if (lookup.length() > 0) {
                index = indexOfLastSeparator(lookup);
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
     // KILL? Decide whether this is worth keeping?
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

    //-----------------------------------------------------------------------
    /**
     * Converts all separators to the Unix separator of forward slash.
     * 
     * @param path  the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     * 
     * @param path  the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    /**
     * Converts all separators to the system separator.
     * 
     * @param path  the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
            return separatorsToWindows(path);
        } else {
            return separatorsToUnix(path);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the index of the last directory separator character.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The position of the last forward or backslash is returned.
     * 
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character.
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Returns the index of the last extension separator character, which is a dot.
     * <p>
     * This method also checks that there is no directory separator after the last dot.
     * To do this it uses {@link #indexOfLastSeparator(String)} which will
     * handle a file in either Unix or Windows format.
     * 
     * @param filename  the filename to find the last path separator in, null returns -1
     * @return the index of the last separator character, or -1 if there
     * is no such character.
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the path from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text before the last forward or backslash is returned.
     * This method is roughly equivalent to the unix command <code>dirname</code>.
     * <pre>
     * a/b/c.txt --> a/b
     * a.txt     --> ""
     * a/b/c     --> a/b
     * a/b/c/    --> a/b/c
     * </pre>
     *
     * @param filename  the filename to query, null returns null
     * @return the path of the file, or an empty string if none exists
     */
    public static String getPath(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(0, index);
        }
    }

    /**
     * Gets the name minus the path from a full filename.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The text after the last forward or backslash is returned.
     * This method is roughly equivalent to the unix command <code>basename</code>.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     *
     * @param filename  the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists
     */
    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    /**
     * Gets the extension of a filename.
     * <p>
     * This method returns the textual part of the filename after the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b.txt/c    --> ""
     * a/b/c        --> ""
     * </pre>
     *
     * @param filename the filename to retrieve the extension of.
     * @return the extension of the file or an empty string if none exists.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Removes the extension from a filename.
     * <p>
     * This method returns the textual part of the filename before the last dot.
     * There must be no directory separator after the dot.
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg --> a\b\c
     * a\b\c     --> a\b\c
     * a.b\c        --> a.b\c
     * </pre>
     *
     * @param filename  the filename to query, null returns null
     * @return the filename minus the extension
     */
    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

}
