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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileFilter;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

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
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: FileUtils.java,v 1.31 2004/04/24 19:46:16 jeremias Exp $
 */
public class FileUtils {

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() { }

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * Returns a human-readable version of the file size (original is in
     * bytes).
     *
     * @param size The number of bytes.
     * @return     A human-readable display value (includes units).
     * @todo need for I18N?
     */
    public static String byteCountToDisplaySize(long size) {
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
     * Implements the same behaviour as the "touch" utility on Unix. It creates
     * a new file with size 0 or, if the file exists already, it is opened and
     * closed without modifying it, but updating the file date and time.
     * @param file the File to touch
     * @throws IOException If an I/O problem occurs
     */
    public static void touch(File file) throws IOException {
        OutputStream out = new java.io.FileOutputStream(file);
        IOUtils.closeQuietly(out);
    }


    private static void innerListFiles(Collection files, File directory, IOFileFilter filter) {
        File[] found = directory.listFiles((FileFilter)filter);
        for (int i = 0; i < found.length; i++) {
            if (found[i].isDirectory()) {
                innerListFiles(files, found[i], filter);
            } else {
                files.add(found[i]);
            }
        }
    }


    /**
     * Converts a Collection containing java.io.File instanced into array
     * representation. This is to account for the difference between
     * File.listFiles() and FileUtils.listFiles().
     * @param files a Collection containing java.io.File instances
     * @return an array of java.io.File
     */
    public static File[] convertFileCollectionToFileArray(Collection files) {
         return (File[])files.toArray(new File[files.size()]);
    }


    /**
     * <p>Finds files within a given directory (and optionally its 
     * subdirectories). All files found are filtered by an IOFileFilter.
     * </p>
     * <p>If your search should recurse into subdirectories you can pass in 
     * an IOFileFilter for directories. You don't need to bind a 
     * DirectoryFileFilter (via logical AND) to this filter. This method does 
     * that for you.
     * </p>
     * <p>An example: If you want to search through all directories called
     * "temp" you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
     * </p>
     * <p>Another common usage of this method is find files in a directory
     * tree but ignoring the directories generated CVS. You can simply pass
     * in <code>FileFilterUtils.makeCVSAware(null)</code>.
     * </p>  
     * @param directory the directory to search in
     * @param fileFilter filter to apply when finding files.
     * @param dirFilter optional filter to apply when finding subdirectories. 
     * If this parameter is null, subdirectories will not be included in the
     * search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an collection of java.io.File with the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     */
    public static Collection listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'directory' is not a directory");
        }
        if (fileFilter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
        
        //Setup effective file filter
        IOFileFilter effFileFilter = FileFilterUtils.andFileFilter(fileFilter, 
            FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
            
        //Setup effective directory filter
        IOFileFilter effDirFilter;
        if (dirFilter == null) {
            effDirFilter = FalseFileFilter.INSTANCE;
        } else {
            effDirFilter = FileFilterUtils.andFileFilter(dirFilter,
                DirectoryFileFilter.INSTANCE);
        }
        
        //Find files
        Collection files = new java.util.LinkedList();
        innerListFiles(files, directory, 
            FileFilterUtils.orFileFilter(effFileFilter, effDirFilter));
        return files;
    }
    

    /**
     * Converts an array of file extensions to suffixes for use
     * with IOFileFilters.
     * @param extensions an array of extensions. Format: {"java", "xml"}
     * @return an array of suffixes. Format: {".java", ".xml"}
     */
    private static String[] toSuffixes(String[] extensions) {
        String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }


    /**
     * Finds files within a given directory (and optionally its subdirectories)
     * which match an array of extensions. 
     * @param directory the directory to search in
     * @param extensions an array of extensions, ex. {"java","xml"}. If this
     * parameter is null, all files are returned.
     * @param recursive If true all subdirectories are searched, too.
     * @return an collection of java.io.File with the matching files
     */
    public static Collection listFiles(File directory, String[] extensions, boolean recursive) {
        IOFileFilter filter;
        if (extensions == null) {
            filter = TrueFileFilter.INSTANCE;
        } else {
            String[] suffixes = toSuffixes(extensions);
            filter = new SuffixFileFilter(suffixes);
        }
        return listFiles(directory, filter, 
            (recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE));
    }


    /**
     * <p>Compare the contents of two files to determine if they are equal or not.</p>
     * <p>Code origin: Avalon</p>
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     * @throws IOException in case of an I/O error
     */
    public static boolean contentEquals(File file1, File file2)
            throws IOException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // don't want to compare directory contents
            throw new IOException("Can't compare directories, only files");
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = new java.io.FileInputStream(file1);
            input2 = new java.io.FileInputStream(file2);
            return IOUtils.contentEquals(input1, input2);

        } finally {
            IOUtils.closeQuietly(input1);
            IOUtils.closeQuietly(input2);
        }
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * @param url File URL.
     * @return The equivalent <code>File</code> object, or <code>null</code> if the URL's protocol
     * is not <code>file</code>
     */
    public static File toFile(URL url) {
        if (url.getProtocol().equals("file") == false) {
            return null;
        } else {
            String filename =
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
    public static URL[] toURLs(File[] files) throws IOException {
        URL[] urls = new URL[files.length];

        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURL();
        }

        return urls;
    }


    /**
     * Copy file from source to destination. If <code>destinationDirectory</code> does not exist, it
     * (and any parent directories) will be created. If a file <code>source</code> in
     * <code>destinationDirectory</code> exists, it will be overwritten.
     * The copy will have the same file date as the original.
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
        File source,
        File destinationDirectory)
        throws IOException {
        if (destinationDirectory.exists()
            && !destinationDirectory.isDirectory()) {
            throw new IllegalArgumentException("Destination is not a directory");
        }

        copyFile(source, new File(destinationDirectory, source.getName()), true);
    }

    /**
     * Copy file from source to destination. The directories up to 
     * <code>destination</code> will be created if they don't already exist. 
     * <code>destination</code> will be overwritten if it already exists.
     * The copy will have the same file date as the original.
     *
     * @param source An existing non-directory <code>File</code> to copy 
     * bytes from.
     * @param destination A non-directory <code>File</code> to write bytes to 
     * (possibly overwriting).
     *
     * @throws IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     * written to, or an IO error occurs during copying.
     *
     * @throws FileNotFoundException if <code>destination</code> is a directory
     * (use {@link #copyFileToDirectory}).
     */
    public static void copyFile(File source, File destination)
                throws IOException {
        copyFile(source, destination, true);
    }
                
                
    /**
     * Copy file from source to destination. The directories up to 
     * <code>destination</code> will be created if they don't already exist. 
     * <code>destination</code> will be overwritten if it already exists.
     *
     * @param source An existing non-directory <code>File</code> to copy 
     * bytes from.
     * @param destination A non-directory <code>File</code> to write bytes to 
     * (possibly overwriting).
     * @param preserveFileDate True if the file date of the copy should be the
     * same as the original.
     *
     * @throws IOException if <code>source</code> does not exist, <code>destination</code> cannot be
     * written to, or an IO error occurs during copying.
     *
     * @throws FileNotFoundException if <code>destination</code> is a directory
     * (use {@link #copyFileToDirectory}).
     */
    public static void copyFile(File source, File destination, boolean preserveFileDate)
                throws IOException {
        //check source exists
        if (!source.exists()) {
            String message = "File " + source + " does not exist";
            throw new FileNotFoundException(message);
        }

        //does destinations directory exist ?
        if (destination.getParentFile() != null
            && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        //make sure we can write to destination
        if (destination.exists() && !destination.canWrite()) {
            String message =
                "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        }

        //makes sure it is not the same file        
        if (source.getCanonicalPath().equals(destination.getCanonicalPath())) {
            String message =
                "Unable to write file " + source + " on itself.";
            throw new IOException(message);
        }

        FileInputStream input = new FileInputStream(source);
        try {
            FileOutputStream output = new FileOutputStream(destination);
            try {
                CopyUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }

        if (source.length() != destination.length()) {
            String message =
                "Failed to copy full contents from "
                    + source
                    + " to "
                    + destination;
            throw new IOException(message);
        }
        
        if (preserveFileDate) {
            //file copy should preserve file date
            destination.setLastModified(source.lastModified());        
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
    public static void copyURLToFile(URL source, File destination)
                throws IOException {
        //does destination directory exist ?
        if (destination.getParentFile() != null
            && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        //make sure we can write to destination
        if (destination.exists() && !destination.canWrite()) {
            String message =
                "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        }

        InputStream input = source.openStream();
        try {
            FileOutputStream output = new FileOutputStream(destination);
            try {
                CopyUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * Recursively delete a directory.
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory)
        throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message =
                "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Clean a directory without deleting it.
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory)
        throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        IOException exception = null;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Waits for NFS to propagate a file creation, imposing a timeout.
     *
     * @param file The file
     * @param seconds The maximum time in seconds to wait.
     * @return True if file exists.
     * TODO Needs a clearer javadoc to see its real purpose for someone without
     *       NFS-knowledge.
     */
    public static boolean waitFor(File file, int seconds) {
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
     * <p>
     * Reads the contents of a file into a String.
     * </p>
     * <p>
     * There is no readFileToString method without encoding parameter because
     * the default encoding can differ between platforms and therefore results
     * in inconsistent results.
     * </p>
     *
     * @param file the file to read.
     * @param encoding the encoding to use
     * @return The file contents or null if read failed.
     * @throws IOException in case of an I/O error
     * @throws UnsupportedEncodingException if the encoding is not supported
     *   by the VM
     */
    public static String readFileToString(
            File file, String encoding) throws IOException {
        InputStream in = new java.io.FileInputStream(file);
        try {
            return IOUtils.toString(in, encoding);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * <p>
     * Writes data to a file. The file will be created if it does not exist.
     * </p>
     * <p>
     * There is no readFileToString method without encoding parameter because
     * the default encoding can differ between platforms and therefore results
     * in inconsistent results.
     * </p>
     *
     * @param file the file to write.
     * @param data The content to write to the file.
     * @param encoding encoding to use
     * @throws IOException in case of an I/O error
     * @throws UnsupportedEncodingException if the encoding is not supported
     *   by the VM
     */
    public static void writeStringToFile(File file, 
            String data, String encoding) throws IOException {
        OutputStream out = new java.io.FileOutputStream(file);
        try {
            out.write(data.getBytes(encoding));
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * <p>
     * Delete a file. If file is a directory, delete it and all sub-directories.
     * </p>
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted. 
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     * @param file file or directory to delete.
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.delete()) {
                String message =
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
    public static void forceDeleteOnExit(File file) throws IOException {
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
    private static void deleteDirectoryOnExit(File directory)
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
    private static void cleanDirectoryOnExit(File directory)
            throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        IOException exception = null;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
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
    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                String message =
                    "File "
                        + directory
                        + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (false == directory.mkdirs()) {
                String message =
                    "Unable to create directory " + directory;
                throw new IOException(message);
            }
        }
    }

    /**
     * Recursively count size of a directory (sum of the length of all files).
     *
     * @param directory directory to inspect
     * @return size of directory in bytes.
     */
    public static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

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
     public static boolean isFileNewer(File file, File reference) {
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
     public static boolean isFileNewer(File file, Date date) {
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
     public static boolean isFileNewer(File file, long timeMillis) {
         if (file == null) {
             throw new IllegalArgumentException("No specified file");
         }
         if (!file.exists()) {
             return false;
         }
 
         return file.lastModified() > timeMillis;
    }

}
