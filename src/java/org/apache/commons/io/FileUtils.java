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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This class provides basic facilities for manipulating files and file paths.
 *
 * <h3>Path-related methods</h3>
 *
 * <p>Methods exist to retrieve the components of a typical file path. For example
 * <code>/www/hosted/mysite/index.html</code>, can be broken into:
 * <ul>
 *   <li><code>/www/hosted/mysite/</code> -- retrievable through {@link #getPath}</li>
 *   <li><code>index.html</code> -- retrievable through {@link #removePath}</li>
 *   <li><code>/www/hosted/mysite/index</code> -- retrievable through {@link #removeExtension}</li>
 *   <li><code>html</code> -- retrievable through {@link #getExtension}</li>
 * </ul>
 * There are also methods to {@link #catPath concatenate two paths}, {@link #resolveFile resolve a
 * path relative to a File} and {@link #normalize} a path.
 * </p>
 *
 * <h3>File-related methods</h3>
 * <p>
 * There are methods to  create a {@link #toFile File from a URL}, copy a
 * {@link #copyFileToDirectory File to a directory},
 * copy a {@link #copyFile File to another File},
 * copy a {@link #copyURLToFile URL's contents to a File},
 * as well as methods to {@link #deleteDirectory(File) delete} and {@link #cleanDirectory(File)
 * clean} a directory.
 * </p>
 *
 * Common {@link java.io.File} manipulation routines.
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
 * @version $Id: FileUtils.java,v 1.16 2003/10/17 20:15:46 matth Exp $
 */
public class FileUtils {

    /**
     * The number of bytes in a kilobyte.
     */
    public static final int ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final int ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final int ONE_GB = ONE_KB * ONE_MB;

    /**
     * Returns a human-readable version of the file size (original is in
     * bytes).
     *
     * @param size The number of bytes.
     * @return     A human-readable display value (includes units).
     */
    public static String byteCountToDisplaySize(int size) {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }

        return displaySize;
    }



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
     * Reads the contents of a file (using the default encoding).
     *
     * @param fileName The name of the file to read.
     * @return The file contents or null if read failed.
     * @throws IOException in case of an I/O error
     * TODO This method should probably be removed or rethought.
     * Because it uses the default encoding only it should probably not be
     * used at all (platform-dependency)
     */
    public static String fileRead(final String fileName) throws IOException {
        FileInputStream in = new FileInputStream(fileName);
        try {
            return IOUtils.toString(in);
        } finally {
            IOUtils.shutdownStream(in);
        }
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param fileName The name of the file to write.
     * @param data The content to write to the file.
     * @throws IOException in case of an I/O error
     * TODO This method should probably be removed or rethought.
     * Because it uses the default encoding only it should probably not be
     * used at all (platform-dependency)
     */
    public static void fileWrite(String fileName, String data)
        throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        try {
            out.write(data.getBytes());
        } finally {
            IOUtils.shutdownStream(out);
        }
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
     * Waits for NFS to propagate a file creation, imposing a timeout.
     *
     * @param fileName The name of the file.
     * @param seconds The maximum time in seconds to wait.
     * @return True if file exists.
     * TODO Does this method make sense? Does it behave as it should?
     */
    public static boolean waitFor(String fileName, int seconds) {
        File file = new File(fileName);
        int timeout = 0;
        int tick = 0;
        while (!file.exists()) {
            if (tick++ >= 10) {
                tick = 0;
                if (timeout++ > seconds) {
                    return false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {} catch (Exception ex) {
                break;
            }
        }
        return true;
    }

    /**
     * Given a directory and an array of extensions... return an array of
     * compliant files.
     *
     * TODO Should an ignore list be passed in?
     * TODO Should a recurse flag be passed in?
     * TODO Should be rewritten using the filefilter package.
     *
     * The given extensions should be like "java" and not like ".java"
     */
    public static String[] getFilesFromExtension(
        String directory,
        String[] extensions) {

        Collection files = new ArrayList();

        File currentDir = new File(directory);

        String[] unknownFiles = currentDir.list();

        if (unknownFiles == null) {
            return new String[0];
        }

        for (int i = 0; i < unknownFiles.length; ++i) {
            String currentFileName =
                directory
                    + System.getProperty("file.separator")
                    + unknownFiles[i];
            File currentFile = new java.io.File(currentFileName);

            if (currentFile.isDirectory()) {

                //ignore all CVS directories...
                if (currentFile.getName().equals("CVS")) {
                    continue;
                }

                //ok... transverse into this directory and get all the files... then combine
                //them with the current list.

                String[] fetchFiles =
                    getFilesFromExtension(currentFileName, extensions);
                files = blendFiles(files, fetchFiles);

            } else {
                //ok... add the file

                String add = currentFile.getAbsolutePath();
                if (isValidFile(add, extensions)) {
                    files.add(add);

                }

            }
        }

        //ok... move the Vector into the files list...

        String[] foundFiles = new String[files.size()];
        files.toArray(foundFiles);

        return foundFiles;

    }

    /**
     * Private hepler method for getFilesFromExtension()
     */
    private static Collection blendFiles(Collection c, String[] files) {

        for (int i = 0; i < files.length; ++i) {
            c.add(files[i]);
        }

        return c;
    }

    /**
     * Checks to see if a file is of a particular type(s).
     * Note that if the file does not have an extension, an empty string
     * (&quot;&quot;) is matched for.
     *
     */
    private static boolean isValidFile(String file, String[] extensions) {

        String extension = extension(file);
        if (extension == null) {
            extension = "";
        }

        //ok.. now that we have the "extension" go through the current know
        //excepted extensions and determine if this one is OK.

        for (int i = 0; i < extensions.length; ++i) {
            if (extensions[i].equals(extension))
                return true;
        }

        return false;

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

    /* *** AVALON CODE *** */

    /**
     * Compare the contents of two files to determine if they are equal or not.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     * @throws IOException in case of an I/O error
     */
    public static boolean contentEquals(final File file1, final File file2)
        throws IOException {
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // don't want to compare directory contents
            return false;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = new FileInputStream(file1);
            input2 = new FileInputStream(file2);
            return IOUtils.contentEquals(input1, input2);

        } finally {
            IOUtils.shutdownStream(input1);
            IOUtils.shutdownStream(input2);
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * @param url File URL.
     * @return The equivalent <code>File</code> object, or <code>null</code> if the URL's protocol
     * is not <code>file</code>
     */
    public static File toFile(final URL url) {
        if (url.getProtocol().equals("file") == false) {
            return null;
        } else {
            final String filename =
                url.getFile().replace('/', File.separatorChar);
            return new File(filename);
        }
    }

    /**
     * Convert the array of Files into a list of URLs.
     *
     * @param files the array of files
     * @return the array of URLs
     * @throws IOException if an error occurs
     */
    public static URL[] toURLs(final File[] files) throws IOException {
        final URL[] urls = new URL[files.length];

        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURL();
        }

        return urls;
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
    public static String removeExtension(final String filename) {
        final int index = filename.lastIndexOf('.');

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
    public static String getExtension(final String filename) {
        final int index = filename.lastIndexOf('.');

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
    public static String removePath(final String filepath) {
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
        final String filepath,
        final char fileSeparatorChar) {
        final int index = filepath.lastIndexOf(fileSeparatorChar);

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
    public static String getPath(final String filepath) {
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
        final String filepath,
        final char fileSeparatorChar) {
        final int index = filepath.lastIndexOf(fileSeparatorChar);
        if (-1 == index) {
            return "";
        } else {
            return filepath.substring(0, index);
        }
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
     */
    public static void copyFileToDirectory(
        final File source,
        final File destinationDirectory)
        throws IOException {
        if (destinationDirectory.exists()
            && !destinationDirectory.isDirectory()) {
            throw new IllegalArgumentException("Destination is not a directory");
        }

        copyFile(source, new File(destinationDirectory, source.getName()));
    }

    /**
     * Copy file from source to destination. The directories up to <code>destination</code> will be
     * created if they don't already exist. <code>destination</code> will be overwritten if it
     * already exists.
     *
     * @param source An existing non-directory <code>File</code> to copy bytes from.
     * @param destination A non-directory <code>File</code> to write bytes to (possibly
     * overwriting).
     *
     * @throws IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     * written to, or an IO error occurs during copying.
     *
     * @throws FileNotFoundException if <code>destination</code> is a directory
     * (use {@link #copyFileToDirectory}).
     */
    public static void copyFile(final File source, final File destination)
        throws IOException {
        //check source exists
        if (!source.exists()) {
            final String message = "File " + source + " does not exist";
            throw new IOException(message);
        }

        //does destinations directory exist ?
        if (destination.getParentFile() != null
            && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        //make sure we can write to destination
        if (destination.exists() && !destination.canWrite()) {
            final String message =
                "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        }

        final FileInputStream input = new FileInputStream(source);
        try {
            final FileOutputStream output = new FileOutputStream(destination);
            try {
                CopyUtils.copy(input, output);
            } finally {
                IOUtils.shutdownStream(output);
            }
        } finally {
            IOUtils.shutdownStream(input);
        }

        if (source.length() != destination.length()) {
            final String message =
                "Failed to copy full contents from "
                    + source
                    + " to "
                    + destination;
            throw new IOException(message);
        }
    }

    /**
     * Copies bytes from the URL <code>source</code> to a file <code>destination</code>.
     * The directories up to <code>destination</code> will be created if they don't already exist.
     * <code>destination</code> will be overwritten if it already exists.
     *
     * @param source A <code>URL</code> to copy bytes from.
     * @param destination A non-directory <code>File</code> to write bytes to (possibly
     * overwriting).
     *
     * @throws IOException if
     * <ul>
     *  <li><code>source</code> URL cannot be opened</li>
     *  <li><code>destination</code> cannot be written to</li>
     *  <li>an IO error occurs during copying</li>
     * </ul>
     */
    public static void copyURLToFile(final URL source, final File destination)
                throws IOException {
        //does destination directory exist ?
        if (destination.getParentFile() != null
            && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        //make sure we can write to destination
        if (destination.exists() && !destination.canWrite()) {
            final String message =
                "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        }

        final InputStream input = source.openStream();
        try {
            final FileOutputStream output = new FileOutputStream(destination);
            try {
                CopyUtils.copy(input, output);
            } finally {
                IOUtils.shutdownStream(output);
            }
        } finally {
            IOUtils.shutdownStream(input);
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
    public static String normalize(final String path) {
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
    public static String catPath(final String lookupPath, final String path) {
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
    public static File resolveFile(final File baseFile, String filename) {
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
            } catch (final IOException ioe) {}

            return file;
        }
        // FIXME: I'm almost certain this // removal is unnecessary, as getAbsoluteFile() strips
        // them. However, I'm not sure about this UNC stuff. (JT)
        final char[] chars = filename.toCharArray();
        final StringBuffer sb = new StringBuffer();

        //remove duplicate file separators in succession - except
        //on win32 at start of filename as UNC filenames can
        //be \\AComputer\AShare\myfile.txt
        int start = 0;
        if ('\\' == File.separatorChar) {
            sb.append(filenm.charAt(0));
            start++;
        }

        for (int i = start; i < chars.length; i++) {
            final boolean doubleSeparator =
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
        } catch (final IOException ioe) {}

        return file;
    }



    /**
     * Delete a file. If file is directory delete it and all sub-directories.
     * @param file file or directory to delete.
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.delete()) {
                final String message =
                    "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Schedule a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     * @param file file or directory to delete.
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDeleteOnExit(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Recursively schedule directory for deletion on JVM exit.
     * @param directory directory to delete.
     * @throws IOException in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(final File directory)
        throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectoryOnExit(directory);
        directory.deleteOnExit();
    }

    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean.
     * @throws IOException in case cleaning is unsuccessful
     */
    private static void cleanDirectoryOnExit(final File directory)
        throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        IOException exception = null;

        final File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            try {
                forceDeleteOnExit(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Make a directory. If there already exists a file with specified name or
     * the directory cannot be created then an exception is thrown.
     * @param directory directory to create
     * @throws IOException if the directory cannot be created.
     */
    public static void forceMkdir(final File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                final String message =
                    "File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (false == directory.mkdirs()) {
                final String message =
                    "Unable to create directory " + directory;
                throw new IOException(message);
            }
        }
    }

    /**
     * Recursively delete a directory.
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(final File directory)
        throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            final String message =
                "Directory " + directory + " unable to be deleted.";
            throw new IOException(message);
        }
    }

    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(final File directory)
        throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        IOException exception = null;

        final File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Recursively count size of a directory (sum of the length of all files).
     *
     * @param directory directory to inspect
     * @return size of directory in bytes.
     */
    public static long sizeOfDirectory(final File directory) {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        final File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];

            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }

        return size;
    }
   
     /**
      * Tests if the specified <code>File</code> is newer than the reference 
      * <code>File</code>.
      *
      * @param file the <code>File</code> of which the modification date must be compared
      * @param reference the <code>File</code> of which the modification date is used 
      * like reference
      * @return true if the <code>File</code> exists and has been modified more recently
      * than the reference <code>File</code>.
      */
     public static boolean isFileNewer(final File file, final File reference) {
         if (reference == null) {
             throw new IllegalArgumentException("No specified reference file");
         }
         if (!reference.exists()) {
             throw new IllegalArgumentException("The reference file '" + file + "' doesn't exist");
         }
 
         return isFileNewer(file, reference.lastModified());
     }
 
     /**
      * Tests if the specified <code>File</code> is newer than the specified 
      * <code>Date</code>
      *
      * @param file the <code>File</code> of which the modification date must be compared
      * @param date the date reference
      * @return true if the <code>File</code> exists and has been modified after
      * the given <code>Date</code>.
      */
     public static boolean isFileNewer(final File file, final Date date) {
         if (date == null) {
             throw new IllegalArgumentException("No specified date");
         }
         return isFileNewer(file, date.getTime());
     }
 
     /**
      * Tests if the specified <code>File</code> is newer than the specified 
      * time reference.
      *
      * @param file the <code>File</code> of which the modification date must be compared.
      * @param timeMillis the time reference measured in milliseconds since the epoch 
      * (00:00:00 GMT, January 1, 1970)
      * @return true if the <code>File</code> exists and has been modified after
      * the given time reference.
      */
     public static boolean isFileNewer(final File file, final long timeMillis) {
         if (file == null) {
             throw new IllegalArgumentException("No specified file");
         }
         if (!file.exists()) {
             return false;
         }
 
         return file.lastModified() > timeMillis;
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
     * @deprecated Use {@link #forceDelete(File)}
     */
    public static void forceDelete(final String file) throws IOException {
        forceDelete(new File(file));
    }



    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     * @deprecated Use {@link #cleanDirectory(File)}
     */
    public static void cleanDirectory(final String directory)
        throws IOException {
        cleanDirectory(new File(directory));
    }

    /**
     * Recursively count size of a directory (sum of the length of all files).
     *
     * @param directory directory to inspect
     * @return size of directory in bytes.
     * @deprecated Use {@link #sizeOfDirectory(File)}
     */
    public static long sizeOfDirectory(final String directory) {
        return sizeOfDirectory(new File(directory));
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
     * @deprecated Use {@link #copyFileToDirectory(File, File)}
     */
    public static void copyFileToDirectory(
        final String source,
        final String destinationDirectory)
        throws IOException {
        copyFileToDirectory(new File(source), new File(destinationDirectory));
    }

    /**
     * Recursively delete a directory.
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     * @deprecated Use {@link #deleteDirectory(File)}
     */
    public static void deleteDirectory(final String directory)
        throws IOException {
        deleteDirectory(new File(directory));
    }

    /**
     * Returns the directory path portion of a file specification string.
     * Matches the equally named unix command.
     * @param filename filename to inspect
     * @return The directory portion excluding the ending file separator.
     * @deprecated Use {@link #getPath(File)}
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
     * @deprecated Use {@link #removeExtension(File)}
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
     * @deprecated Use {@link #getExtension(File)}
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
     * Copy a file. The new file will be created if it does not exist. This is
     * an inefficient method, which just calls {@link #fileRead(String)} and
     * then {@link #fileWrite(String,String)}
     *
     * @param inFileName the file to copy
     * @param outFileName the file to copy to
     * @throws Exception if fileRead or fileWrite throw it
     * @deprecated This method will be deleted.
     *
     * TODO This method is not a good idea. It doesn't do a binary copy. DELETE.
     */
    public static void fileCopy(String inFileName, String outFileName)
        throws Exception {
        String content = fileRead(inFileName);
        fileWrite(outFileName, content);
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
