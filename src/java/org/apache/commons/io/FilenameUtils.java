/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Common {@link java.io.File} manipulation routines through 
 * use of a filename.
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
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: FilenameUtils.java,v 1.5 2003/12/30 15:23:23 jeremias Exp $
 */
public class FilenameUtils {

    /**
     * Check if a file exits.
     *
     * @param fileName The name of the file to check.
     * @return true if file exists.
     */
    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }



    /**
     * Deletes a file.
     *
     * @param fileName The name of the file to delete.
     */
    public static void fileDelete(String fileName) {
        File file = new File(fileName);
        file.delete();
    }

    /**
     * Simple way to make a directory. It also creates the parent directories
     * if necessary.
     * @param dir directory to create
     */
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
     * </pre>
     *
     * @param filename the filename
     * @return the filename minus extension
     */
    public static String removeExtension( String filename) {
        int index = filename.lastIndexOf('.');

        if (-1 == index) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    /**
     * Get extension from filename.
     * ie
     * <pre>
     * foo.txt    --> "txt"
     * a\b\c.jpg --> "jpg"
     * a\b\c     --> ""
     * </pre>
     *
     * @param filename the filename
     * @return the extension of filename or "" if none
     */
    public static String getExtension( String filename) {
        int index = filename.lastIndexOf('.');

        if (-1 == index) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Remove path from filename. Equivalent to the unix command <code>basename</code>
     * ie.
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * </pre>
     *
     * @param filepath the filepath
     * @return the filename minus path
     */
    public static String removePath( String filepath) {
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
     * Get path from filename. Roughly equivalent to the unix command <code>dirname</code>.
     * ie.
     * <pre>
     * a/b/c.txt --> a/b
     * a.txt     --> ""
     * </pre>
     *
     * @param filepath the filepath
     * @return the filename minus path
     */
    public static String getPath( String filepath) {
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
     * Eliminates "/../" and "/./" in a string. Returns <code>null</code> if the ..'s went past the
     * root.
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
    public static String normalize( String path) {
        String normalized = path;
        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized =
                normalized.substring(0, index)
                    + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized =
                normalized.substring(0, index)
                    + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return null; // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized =
                normalized.substring(0, index2)
                    + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return normalized;
    }

    /**
     * Will concatenate 2 paths.  Paths with <code>..</code> will be
     * properly handled.
     * <p>Eg.,<br />
     * <code>/a/b/c</code> + <code>d</code> = <code>/a/b/d</code><br />
     * <code>/a/b/c</code> + <code>../d</code> = <code>/a/d</code><br />
     * </p>
     *
     * Thieved from Tomcat sources...
     *
     * @return The concatenated paths, or null if error occurs
     */
    public static String catPath( String lookupPath, String path) {
        // Cut off the last slash and everything beyond
        int index = lookupPath.lastIndexOf("/");
        String lookup = lookupPath.substring(0, index);
        String pth = path;

        // Deal with .. by chopping dirs off the lookup path
        while (pth.startsWith("../")) {
            if (lookup.length() > 0) {
                index = lookup.lastIndexOf("/");
                lookup = lookup.substring(0, index);
            } else {
                // More ..'s than dirs, return null
                return null;
            }

            index = pth.indexOf("../") + 3;
            pth = pth.substring(index);
        }

        return new StringBuffer(lookup).append("/").append(pth).toString();
    }

    /**
     * Resolve a file <code>filename</code> to it's canonical form. If <code>filename</code> is
     * relative (doesn't start with <code>/</code>), it will be resolved relative to
     * <code>baseFile</code>, otherwise it is treated as a normal root-relative path.
     *
     * @param baseFile Where to resolve <code>filename</code> from, if <code>filename</code> is
     * relative.
     * @param filename Absolute or relative file path to resolve.
     * @return The canonical <code>File</code> of <code>filename</code>.
     */
    public static File resolveFile( File baseFile, String filename) {
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
            } catch ( IOException ioe) {}

            return file;
        }
        // FIXME: I'm almost certain this // removal is unnecessary, as getAbsoluteFile() strips
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
        } catch ( IOException ioe) {}

        return file;
    }



    // ----------------------------------------------------------------
    // Deprecated methods
    // ----------------------------------------------------------------

    /**
     * Returns the filename portion of a file specification string.
     * Matches the equally named unix command.
     * @param filename filename to inspect
     * @return The filename string without extension.
     * @deprecated This method will be deleted before a 1.0 release
     * TODO DELETE before 1.0
     */
    public static String basename(String filename) {
        return basename(filename, extension(filename));
    }

    /**
     * Returns the filename portion of a file specification string.
     * Matches the equally named unix command.
     * @param filename filename to inspect
     * @param suffix additional remaining portion of name that if matches will
     * be removed
     * @return The filename string without the suffix.
     * @deprecated This method will be deleted.
     */
    public static String basename(String filename, String suffix) {
        int i = filename.lastIndexOf(File.separator) + 1;
        int lastDot =
            ((suffix != null) && (suffix.length() > 0))
                ? filename.lastIndexOf(suffix)
                : -1;

        if (lastDot >= 0) {
            return filename.substring(i, lastDot);
        } else if (i > 0) {
            return filename.substring(i);
        } else {
            return filename; // else returns all (no path and no extension)
        }
    }

    /**
     * Delete a file. If file is directory delete it and all sub-directories.
     * @param file file or directory to delete.
     * @throws IOException in case deletion is unsuccessful
     * @deprecated Use {@link FileUtils#forceDelete(File)}
     */
    public static void forceDelete( String file) throws IOException {
        FileUtils.forceDelete(new File(file));
    }



    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     * @deprecated Use {@link FileUtils#cleanDirectory(File)}
     */
    public static void cleanDirectory( String directory)
        throws IOException {
        FileUtils.cleanDirectory(new File(directory));
    }

    /**
     * Recursively count size of a directory (sum of the length of all files).
     *
     * @param directory directory to inspect
     * @return size of directory in bytes.
     * @deprecated Use {@link FileUtils#sizeOfDirectory(File)}
     */
    public static long sizeOfDirectory( String directory) {
        return FileUtils.sizeOfDirectory(new File(directory));
    }

    /**
     * Copy file from source to destination. If <code>destinationDirectory</code> does not exist, it
     * (and any parent directories) will be created. If a file <code>source</code> in
     * <code>destinationDirectory</code> exists, it will be overwritten.
     *
     * @param source An existing <code>File</code> to copy.
     * @param destinationDirectory A directory to copy <code>source</code> into.
     *
     * @throws FileNotFoundException if <code>source</code> isn't a normal file.
     * @throws IllegalArgumentException if <code>destinationDirectory</code> isn't a directory.
     * @throws IOException if <code>source</code> does not exist, the file in
     * <code>destinationDirectory</code> cannot be written to, or an IO error occurs during copying.
     *
     * @deprecated Use {@link FileUtils#copyFileToDirectory(File, File)}
     */
    public static void copyFileToDirectory(
        String source,
        String destinationDirectory)
        throws IOException, FileNotFoundException {
        FileUtils.copyFileToDirectory(new File(source), new File(destinationDirectory));
    }

    /**
     * Recursively delete a directory.
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     * @deprecated Use {@link FileUtils#deleteDirectory(File)}
     */
    public static void deleteDirectory( String directory)
        throws IOException {
        FileUtils.deleteDirectory(new File(directory));
    }

    /**
     * Returns the directory path portion of a file specification string.
     * Matches the equally named unix command.
     * @param filename filename to inspect
     * @return The directory portion excluding the ending file separator.
     * @deprecated Use {@link FileUtils#getPath(File)}
     * TODO DELETE before 1.0
     */
    public static String dirname(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0 ? filename.substring(0, i) : "");
    }

    /**
     * Returns the filename portion of a file specification string.
     * @param filename filename to inspect
     * @return The filename string with extension.
     * @deprecated Use {@link FileUtils#removeExtension(File)}
     * TODO DELETE before 1.0
     */
    public static String filename(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0 ? filename.substring(i + 1) : filename);
    }



    /**
     * Returns the extension portion of a file specification string.
     * This everything after the last dot '.' in the filename (NOT including
     * the dot).
     * @param filename filename to inspect
     * @return the extension
     * @deprecated Use {@link FileUtils#getExtension(File)}
     * TODO probably duplicate method. See getExtension
     */
    public static String extension(String filename) {
        int lastDot = filename.lastIndexOf('.');

        if (lastDot >= 0) {
            return filename.substring(lastDot + 1);
        } else {
            return "";
        }
    }

    /**
     * Creates a file handle.
     *
     * @param fileName The name of the file.
     * @return A <code>File</code> instance.
     * @deprecated Use {@link java.io.File#Constructor(String)}
     */
    public static File getFile(String fileName) {
        return new File(fileName);
    }

}
