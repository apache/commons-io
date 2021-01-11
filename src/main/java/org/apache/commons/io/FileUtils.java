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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.io.filefilter.FileEqualsFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * </p>
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible. Relying on the platform default means that the
 * code is Locale-dependent. Only use the default if the files are known to always use the platform default.
 * </p>
 * <p>
 * {@link SecurityException} are not documented in the Javadoc.
 * </p>
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 * </p>
 */
public class FileUtils {
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * The number of bytes in a gigabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    /**
     * The number of bytes in a terabyte.
     */
    public static final long ONE_TB = ONE_KB * ONE_GB;

    /**
     * The number of bytes in a terabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    /**
     * The number of bytes in a petabyte.
     */
    public static final long ONE_PB = ONE_KB * ONE_TB;

    /**
     * The number of bytes in a petabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    /**
     * The number of bytes in an exabyte.
     */
    public static final long ONE_EB = ONE_KB * ONE_PB;

    /**
     * The number of bytes in an exabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

    /**
     * The number of bytes in a zettabyte.
     */
    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

    /**
     * The number of bytes in a yottabyte.
     */
    public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

    /**
     * An empty array of type <code>File</code>.
     */
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @throws NullPointerException if the given {@code BigInteger} is {@code null}.
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     * @since 2.4
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(final BigInteger size) {
        Objects.requireNonNull(size, "size");
        final String displaySize;

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_EB_BI) + " EB";
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_PB_BI) + " PB";
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_TB_BI) + " TB";
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_GB_BI) + " GB";
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_MB_BI) + " MB";
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = size.divide(ONE_KB_BI) + " KB";
        } else {
            displaySize = size + " bytes";
        }
        return displaySize;
    }

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(final long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }

    /**
     * Computes the checksum of a file using the specified checksum object. Multiple files may be checked using one
     * <code>Checksum</code> instance if desired simply by reusing the same checksum object. For example:
     * 
     * <pre>
     * long checksum = FileUtils.checksum(file, new CRC32()).getValue();
     * </pre>
     *
     * @param file the file to checksum, must not be {@code null}
     * @param checksum the checksum object to be used, must not be {@code null}
     * @return the checksum specified, updated with the content of the file
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws NullPointerException if the given {@code Checksum} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a file.
     * @throws IOException if an IO error occurs reading the file.
     * @since 1.3
     */
    public static Checksum checksum(final File file, final Checksum checksum) throws IOException {
        requireExistsChecked(file, "file");
        requireFile(file, "file");
        Objects.requireNonNull(checksum, "checksum");
        try (InputStream in = new CheckedInputStream(new FileInputStream(file), checksum)) {
            IOUtils.consume(in);
        }
        return checksum;
    }

    /**
     * Computes the checksum of a file using the CRC32 checksum routine.
     * The value of the checksum is returned.
     *
     * @param file the file to checksum, must not be {@code null}
     * @return the checksum value
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a file.
     * @throws IOException              if an IO error occurs reading the file.
     * @since 1.3
     */
    public static long checksumCRC32(final File file) throws IOException {
        return checksum(file, new CRC32()).getValue();
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     * @see #forceDelete(File)
     */
    public static void cleanDirectory(final File directory) throws IOException {
        final File[] files = listFiles(directory, null);

        final List<Exception> causeList = new ArrayList<>();
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                causeList.add(ioe);
            }
        }

        if (!causeList.isEmpty()) {
            throw new IOExceptionList(causeList);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean, must not be {@code null}
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     * @see #forceDeleteOnExit(File)
     */
    private static void cleanDirectoryOnExit(final File directory) throws IOException {
        final File[] files = listFiles(directory, null);

        final List<Exception> causeList = new ArrayList<>();
        for (final File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (final IOException ioe) {
                causeList.add(ioe);
            }
        }

        if (!causeList.isEmpty()) {
            throw new IOExceptionList(causeList);
        }
    }

    /**
     * Tests whether the contents of two files are equal.
     * <p>
     * This method checks to see if the two files are different lengths or if they point to the same file, before
     * resorting to byte-by-byte comparison of the contents.
     * </p>
     * <p>
     * Code origin: Avalon
     * </p>
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     * @throws IllegalArgumentException when an input is not a file.
     * @throws IOException If an I/O error occurs.
     * @see org.apache.commons.io.file.PathUtils#fileContentEquals(Path,Path,java.nio.file.LinkOption[],java.nio.file.OpenOption...)
     */
    public static boolean contentEquals(final File file1, final File file2) throws IOException {
        if (file1 == null && file2 == null) {
            return true;
        }
        if (file1 == null || file2 == null) {
            return false;
        }
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        requireFile(file1, "file1");
        requireFile(file2, "file2");

        if (file1.length() != file2.length()) {
            // lengths differ, cannot be equal
            return false;
        }

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        try (InputStream input1 = new FileInputStream(file1); InputStream input2 = new FileInputStream(file2)) {
            return IOUtils.contentEquals(input1, input2);
        }
    }

    /**
     * Compares the contents of two files to determine if they are equal or not.
     * <p>
     * This method checks to see if the two files point to the same file,
     * before resorting to line-by-line comparison of the contents.
     * </p>
     *
     * @param file1       the first file
     * @param file2       the second file
     * @param charsetName the name of the requested charset.
     *                    May be null, in which case the platform default is used
     * @return true if the content of the files are equal or neither exists,
     * false otherwise
     * @throws IllegalArgumentException when an input is not a file.
     * @throws IOException in case of an I/O error.
     * @throws UnsupportedCharsetException If the named charset is unavailable (unchecked exception).
     * @see IOUtils#contentEqualsIgnoreEOL(Reader, Reader)
     * @since 2.2
     */
    public static boolean contentEqualsIgnoreEOL(final File file1, final File file2, final String charsetName)
            throws IOException {
        if (file1 == null && file2 == null) {
            return true;
        }
        if (file1 == null || file2 == null) {
            return false;
        }
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // two not existing files are equal
            return true;
        }

        requireFile(file1, "file1");
        requireFile(file2, "file2");

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        final Charset charset = Charsets.toCharset(charsetName);
        try (Reader input1 = new InputStreamReader(new FileInputStream(file1), charset);
             Reader input2 = new InputStreamReader(new FileInputStream(file2), charset)) {
            return IOUtils.contentEqualsIgnoreEOL(input1, input2);
        }
    }

    /**
     * Converts a Collection containing java.io.File instanced into array
     * representation. This is to account for the difference between
     * File.listFiles() and FileUtils.listFiles().
     *
     * @param files a Collection containing java.io.File instances
     * @return an array of java.io.File
     */
    public static File[] convertFileCollectionToFileArray(final Collection<File> files) {
        return files.toArray(EMPTY_FILE_ARRAY);
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child directories and files to the specified destination.
     * The destination is the new location and name of the directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that those operations will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.1
     */
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    /**
     * Copies a whole directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that those operations
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.1
     */
    public static void copyDirectory(final File srcDir, final File destDir, final boolean preserveFileDate)
        throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that those operations will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     * <b>Example: Copy directories only</b>
     * 
     * <pre>
     * // only copy the directory structure
     * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
     * </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * 
     * <pre>
     * // Create a filter for ".txt" files
     * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     * // Create a filter for either directories or ".txt" files
     * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     * // Copy using the filter
     * FileUtils.copyDirectory(srcDir, destDir, filter);
     * </pre>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @param filter the filter to apply, null means copy all directories and files should be the same as the original.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter)
        throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that those operations
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     * <b>Example: Copy directories only</b>
     * 
     * <pre>
     * // only copy the directory structure
     * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     * </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * 
     * <pre>
     * // Create a filter for ".txt" files
     * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     * // Create a filter for either directories or ".txt" files
     * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     * // Copy using the filter
     * FileUtils.copyDirectory(srcDir, destDir, filter, false);
     * </pre>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @param filter the filter to apply, null means copy all directories and files.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter,
        final boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, filter, preserveFileDate, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that those operations
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     * <b>Example: Copy directories only</b>
     * 
     * <pre>
     * // only copy the directory structure
     * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     * </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * 
     * <pre>
     * // Create a filter for ".txt" files
     * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     * // Create a filter for either directories or ".txt" files
     * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     * // Copy using the filter
     * FileUtils.copyDirectory(srcDir, destDir, filter, false);
     * </pre>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @param fileFilter the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy should be the same as the original
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 2.8.0
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter fileFilter,
        final boolean preserveFileDate, final CopyOption... copyOptions) throws IOException {
        requireFileCopy(srcDir, destDir);
        requireDirectory(srcDir, "srcDir");
        requireCanonicalPathsNotEquals(srcDir, destDir);

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        final String srcDirCanonicalPath = srcDir.getCanonicalPath();
        final String destDirCanonicalPath = destDir.getCanonicalPath();
        if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
            final File[] srcFiles = listFiles(srcDir, fileFilter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, fileFilter, preserveFileDate, exclusionList, copyOptions);
    }

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source directory and all its contents to a directory of the same name in the specified
     * destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that those operations will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param sourceDir an existing directory to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.2
     */
    public static void copyDirectoryToDirectory(final File sourceDir, final File destinationDir) throws IOException {
        requireDirectoryIfExists(sourceDir, "sourceDir");
        requireDirectoryIfExists(destinationDir, "destinationDir");
        copyDirectory(sourceDir, new File(destinationDir, sourceDir.getName()), true);
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that the operation will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @see #copyFileToDirectory(File, File)
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies an existing file to a new file location.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that the operation
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     * @see #copyFile(File, File, boolean, CopyOption...)
     */
    public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
        throws IOException {
        copyFile(srcFile, destFile, preserveFileDate, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that the operation
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}..
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IllegalArgumentException if source is not a file.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @throws IOException if an I/O error occurs, or setting the last-modified time didn't succeeded.
     * @see #copyFileToDirectory(File, File, boolean)
     * @since 2.8.0
     */
    public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate, final CopyOption... copyOptions)
        throws IOException {
        requireFileCopy(srcFile, destFile);
        requireFile(srcFile, "srcFile");
        requireCanonicalPathsNotEquals(srcFile, destFile);
        createParentDirectories(destFile);

        if (destFile.exists()) {
            requireCanWrite(destFile, "destFile");
        }
        doCopyFile(srcFile, destFile, preserveFileDate, copyOptions);
    }

    /**
     * Copy bytes from a <code>File</code> to an <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     * </p>
     *
     * @param input  the <code>File</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the File is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException          if an I/O error occurs
     * @since 2.1
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (FileInputStream fis = new FileInputStream(input)) {
            return IOUtils.copyLarge(fis, output);
        }
    }

    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified
     * destination directory. The destination directory is created if it does not exist. If the destination file exists,
     * then this method will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that the operation will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
     * Copies a file to a directory optionally preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified
     * destination directory. The destination directory is created if it does not exist. If the destination file exists,
     * then this method will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to {@code true} tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however it is not guaranteed that the operation
     * will succeed. If the modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param sourceFile an existing file to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @see #copyFile(File, File, boolean)
     * @since 1.3
     */
    public static void copyFileToDirectory(final File sourceFile, final File destinationDir, final boolean preserveFileDate)
            throws IOException {
        requireDirectoryIfExists(destinationDir, "destinationDir");
        final File destFile = new File(destinationDir, sourceFile.getName());
        copyFile(sourceFile, destFile, preserveFileDate);
    }

    /**
     * Copies bytes from an {@link InputStream} <code>source</code> to a file
     * <code>destination</code>. The directories up to <code>destination</code>
     * will be created if they don't already exist. <code>destination</code>
     * will be overwritten if it already exists.
     * <p>
     * <em>The {@code source} stream is closed.</em>
     * </p>
     * <p>
     * See {@link #copyToFile(InputStream, File)} for a method that does not close the input stream.
     * </p>
     *
     * @param source      the <code>InputStream</code> to copy bytes from, must not be {@code null}, will be closed
     * @param destination the non-directory <code>File</code> to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream in = source) {
            copyToFile(in, destination);
        }
    }

    /**
     * Copies a file or directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source file or directory, along all its contents, to a directory of the same name in the
     * specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that those operations will succeed. If the
     * modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param sourceFile an existing file or directory to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @see #copyDirectoryToDirectory(File, File)
     * @see #copyFileToDirectory(File, File)
     * @since 2.6
     */
    public static void copyToDirectory(final File sourceFile, final File destinationDir) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        if (sourceFile.isFile()) {
            copyFileToDirectory(sourceFile, destinationDir);
        } else if (sourceFile.isDirectory()) {
            copyDirectoryToDirectory(sourceFile, destinationDir);
        } else {
            throw new FileNotFoundException("The source " + sourceFile + " does not exist");
        }
    }

    /**
     * Copies a files to a directory preserving each file's date.
     * <p>
     * This method copies the contents of the specified source files
     * to a file of the same name in the specified destination directory.
     * The destination directory is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, the methods throws IOException.
     * </p>
     *
     * @param sourceIterable     a existing files to copy, must not be {@code null}.
     * @param destinationDir  the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @see #copyFileToDirectory(File, File)
     * @since 2.6
     */
    public static void copyToDirectory(final Iterable<File> sourceIterable, final File destinationDir) throws IOException {
        Objects.requireNonNull(sourceIterable, "sourceIterable");
        for (final File src : sourceIterable) {
            copyFileToDirectory(src, destinationDir);
        }
    }

    /**
     * Copies bytes from an {@link InputStream} <code>source</code> to a file
     * <code>destination</code>. The directories up to <code>destination</code>
     * will be created if they don't already exist. <code>destination</code>
     * will be overwritten if it already exists.
     * The {@code source} stream is left open, e.g. for use with {@link java.util.zip.ZipInputStream ZipInputStream}.
     * See {@link #copyInputStreamToFile(InputStream, File)} for a method that closes the input stream.
     *
     * @param source      the <code>InputStream</code> to copy bytes from, must not be {@code null}
     * @param destination the non-directory <code>File</code> to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.5
     */
    public static void copyToFile(final InputStream source, final File destination) throws IOException {
        try (OutputStream out = openOutputStream(destination)) {
            IOUtils.copy(source, out);
        }
    }

    /**
     * Copies bytes from the URL <code>source</code> to a file
     * <code>destination</code>. The directories up to <code>destination</code>
     * will be created if they don't already exist. <code>destination</code>
     * will be overwritten if it already exists.
     * <p>
     * Warning: this method does not set a connection or read timeout and thus
     * might block forever. Use {@link #copyURLToFile(URL, File, int, int)}
     * with reasonable timeouts to prevent this.
     * </p>
     *
     * @param source      the <code>URL</code> to copy bytes from, must not be {@code null}
     * @param destination the non-directory <code>File</code> to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if <code>source</code> URL cannot be opened
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyURLToFile(final URL source, final File destination) throws IOException {
        try (final InputStream stream = source.openStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }

    /**
     * Copies bytes from the URL <code>source</code> to a file <code>destination</code>. The directories up to
     * <code>destination</code> will be created if they don't already exist. <code>destination</code> will be
     * overwritten if it already exists.
     *
     * @param source the <code>URL</code> to copy bytes from, must not be {@code null}
     * @param destination the non-directory <code>File</code> to write bytes to (possibly overwriting), must not be
     *        {@code null}
     * @param connectionTimeoutMillis the number of milliseconds until this method will timeout if no connection could
     *        be established to the <code>source</code>
     * @param readTimeoutMillis the number of milliseconds until this method will timeout if no data could be read from
     *        the <code>source</code>
     * @throws IOException if <code>source</code> URL cannot be opened
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyURLToFile(final URL source, final File destination,
        final int connectionTimeoutMillis, final int readTimeoutMillis) throws IOException {
        final URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        try (final InputStream stream = connection.getInputStream()) {
            copyInputStreamToFile(stream, destination);
        }
    }


    /**
     * Creates all parent directories for a File object.
     *
     * @param file the File that may need parents, may be null.
     * @return The parent directory, or {@code null} if the given file does not name a parent
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not null and not a directory.
     * @since 2.9.0
     */
    public static File createParentDirectories(final File file) throws IOException {
        return mkdirs(getParentFile(file));
    }

    /**
     * Decodes the specified URL as per RFC 3986, i.e. transforms
     * percent-encoded octets to characters by decoding with the UTF-8 character
     * set. This function is primarily intended for usage with
     * {@link java.net.URL} which unfortunately does not enforce proper URLs. As
     * such, this method will leniently accept invalid characters or malformed
     * percent-encoded octets and simply pass them literally through to the
     * result string. Except for rare edge cases, this will make unencoded URLs
     * pass through unaltered.
     *
     * @param url The URL to decode, may be {@code null}.
     * @return The decoded URL or {@code null} if the input was
     * {@code null}.
     */
    static String decodeUrl(final String url) {
        String decoded = url;
        if (url != null && url.indexOf('%') >= 0) {
            final int n = url.length();
            final StringBuilder buffer = new StringBuilder();
            final ByteBuffer bytes = ByteBuffer.allocate(n);
            for (int i = 0; i < n; ) {
                if (url.charAt(i) == '%') {
                    try {
                        do {
                            final byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
                            bytes.put(octet);
                            i += 3;
                        } while (i < n && url.charAt(i) == '%');
                        continue;
                    } catch (final RuntimeException e) {
                        // malformed percent-encoded octet, fall through and
                        // append characters literally
                    } finally {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                            bytes.clear();
                        }
                    }
                }
                buffer.append(url.charAt(i++));
            }
            decoded = buffer.toString();
        }
        return decoded;
    }

    /**
     * Deletes the given File but throws an IOException if it cannot, unlike {@link File#delete()} which returns a
     * boolean.
     *
     * @param file The file to delete.
     * @return the given file.
     * @throws IOException if the file cannot be deleted.
     * @see File#delete()
     * @since 2.9.0
     */
    public static File delete(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.delete()) {
            throw new IOException("Unable to delete " + file);
        }
        return file;
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
     */
    public static void deleteDirectory(final File directory) throws IOException {
        Objects.requireNonNull(directory, "directory");
        if (!directory.exists()) {
            return;
        }
        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }
        delete(directory);
    }

    /**
     * Schedules a directory recursively for deletion on JVM exit.
     *
     * @param directory directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          in case deletion is unsuccessful
     */
    private static void deleteDirectoryOnExit(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     *
     * @param file file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise
     * {@code false}
     *
     * @since 1.4
     */
    public static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (final Exception ignored) {
            // ignore
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }

    /**
     * Determines whether the {@code parent} directory contains the {@code child} element (a file or directory).
     * <p>
     * Files are normalized before comparison.
     * </p>
     *
     * Edge cases:
     * <ul>
     * <li>A {@code directory} must not be null: if null, throw IllegalArgumentException</li>
     * <li>A {@code directory} must be a directory: if not a directory, throw IllegalArgumentException</li>
     * <li>A directory does not contain itself: return false</li>
     * <li>A null child file is not contained in any parent: return false</li>
     * </ul>
     *
     * @param directory the file to consider as the parent.
     * @param child     the file to consider as the child.
     * @return true is the candidate leaf is under by the specified composite. False otherwise.
     * @throws IOException              if an IO error occurs while checking the files.
     * @throws IllegalArgumentException if {@code directory} is not a directory.
     * @see FilenameUtils#directoryContains(String, String)
     * @since 2.2
     */
    public static boolean directoryContains(final File directory, final File child) throws IOException {
        requireDirectoryExists(directory, "directory");

        if (child == null) {
            return false;
        }

        if (!directory.exists() || !child.exists()) {
            return false;
        }

        // Canonicalize paths (normalizes relative paths)
        final String canonicalParent = directory.getCanonicalPath();
        final String canonicalChild = child.getCanonicalPath();

        return FilenameUtils.directoryContains(canonicalParent, canonicalChild);
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir the validated source directory, must not be {@code null}.
     * @param destDir the validated destination directory, must not be {@code null}.
     * @param fileFilter the filter to apply, null means copy all directories and files.
     * @param preserveFileDate whether to preserve the file date.
     * @param exclusionList List of files and directories to exclude from the copy, may be null.
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not a directory.
     */
    private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter fileFilter,
        final boolean preserveFileDate, final List<String> exclusionList, final CopyOption... copyOptions)
        throws IOException {
        // recurse
        final File[] srcFiles = listFiles(srcDir, fileFilter);
        requireDirectoryIfExists(destDir, "destDir");
        mkdirs(destDir);
        requireCanWrite(destDir, "destDir");
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, fileFilter, preserveFileDate, exclusionList, copyOptions);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate, copyOptions);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            setLastModified(srcDir, destDir);
        }
    }

    /**
     * Internal copy file method. This uses the original file length, and throws an IOException if the output file
     * length is different from the current input file length. So it may fail if the file changes size. It may also fail
     * with "IllegalArgumentException: Negative size" if the input file is truncated part way through copying the data
     * and the new file size is less than the current position.
     *
     * @param srcFile the validated source file, must not be {@code null}
     * @param destFile the validated destination file, must not be {@code null}
     * @param preserveFileDate whether to preserve the file date
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     * @throws IllegalArgumentException "Negative size" if the file is truncated so that the size is less than the
     *         position
     */
    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate, final CopyOption... copyOptions)
        throws IOException {
        Objects.requireNonNull(srcFile, "srcFile");
        requireFileIfExists(destFile, "destFile");

        final Path srcPath = srcFile.toPath();
        final Path destPath = destFile.toPath();
        // On Windows, the last modified time is copied by default.
        Files.copy(srcPath, destPath, copyOptions);

        // TODO IO-386: Do we still need this check?
        requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());

        if (preserveFileDate) {
            setLastModified(srcFile, destFile);
        }
    }

    /**
     * Deletes a file or directory. For a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>The directory does not have to be empty.</li>
     * <li>You get an exception when a file or directory cannot be deleted.</li>
     * </ul>
     *
     * @param file file or directory to delete, must not be {@code null}.
     * @throws NullPointerException  if the file is {@code null}.
     * @throws FileNotFoundException if the file was not found.
     * @throws IOException           in case deletion is unsuccessful.
     */
    public static void forceDelete(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        final Counters.PathCounters deleteCounters;
        try {
            deleteCounters = PathUtils.delete(file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY,
                StandardDeleteOption.OVERRIDE_READ_ONLY);
        } catch (final IOException e) {
            throw new IOException("Cannot delete file: " + file, e);
        }

        if (deleteCounters.getFileCounter().get() < 1 && deleteCounters.getDirectoryCounter().get() < 1) {
            // didn't find a file to delete.
            throw new FileNotFoundException("File does not exist: " + file);
        }
    }

    /**
     * Schedules a file to be deleted when JVM exits.
     * If file is directory delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be {@code null}.
     * @throws NullPointerException if the file is {@code null}.
     * @throws IOException          in case deletion is unsuccessful.
     */
    public static void forceDeleteOnExit(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Makes a directory, including any necessary but nonexistent parent
     * directories. If a file already exists with specified name but it is
     * not a directory then an IOException is thrown.
     * If the directory cannot be created (or the file already exists but is not a directory)
     * then an IOException is thrown.
     *
     * @param directory directory to create, must not be {@code null}.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     */
    public static void forceMkdir(final File directory) throws IOException {
        mkdirs(directory);
    }

    /**
     * Makes any necessary but nonexistent parent directories for a given File. If the parent directory cannot be
     * created then an IOException is thrown.
     *
     * @param file file with parent to create, must not be {@code null}.
     * @throws NullPointerException if the file is {@code null}.
     * @throws IOException          if the parent directory cannot be created.
     * @since 2.5
     */
    public static void forceMkdirParent(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        final File parent = getParentFile(file);
        if (parent == null) {
            return;
        }
        forceMkdir(parent);
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory.
     * @param names the name elements.
     * @return the new file.
     * @since 2.1
     */
    public static File getFile(final File directory, final String... names) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(names, "names");
        File file = directory;
        for (final String name : names) {
            file = new File(file, name);
        }
        return file;
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param names the name elements.
     * @return the file.
     * @since 2.1
     */
    public static File getFile(final String... names) {
        Objects.requireNonNull(names, "names");
        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    /**
     * Gets the parent of the given file. The given file may be bull and a file's parent may as well be null.
     * 
     * @param file The file to query.
     * @return The parent file or {@code null}.
     */
    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory.
     *
     * @since 2.0
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Returns the path to the system temporary directory.
     *
     * @return the path to the system temporary directory.
     *
     * @since 2.0
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     *
     * @since 2.0
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     *
     * @since 2.0
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code ChronoLocalDate}
     * at the current time.
     *
     * <p>Note: The input date is assumed to be in the system default time-zone with the time
     * part set to the current time. To use a non-default time-zone use the method
     * {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDate.atTime(LocalTime.now(zoneId)), zoneId)} where
     * {@code zoneId} is a valid {@link ZoneId}.
     *
     * @param file            the {@code File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @return true if the {@code File} exists and has been modified after the given
     * {@code ChronoLocalDate} at the current time.
     * @throws NullPointerException if the file or local date is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileNewer(file, chronoLocalDate, LocalTime.now());
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code ChronoLocalDate}
     * at the specified time.
     *
     * <p>Note: The input date and time are assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDate.atTime(localTime), zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file            the {@code File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @param localTime       the time reference.
     * @return true if the {@code File} exists and has been modified after the given
     * {@code ChronoLocalDate} at the given time.
     * @throws NullPointerException if the file, local date or zone ID is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileNewer(file, chronoLocalDate.atTime(localTime));
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code ChronoLocalDateTime}
     * at the system-default time zone.
     *
     * <p>Note: The input date and time is assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDateTime, zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file                the {@code File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @return true if the {@code File} exists and has been modified after the given
     * {@code ChronoLocalDateTime} at the system-default time zone.
     * @throws NullPointerException if the file or local date time is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime) {
        return isFileNewer(file, chronoLocalDateTime, ZoneId.systemDefault());
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code ChronoLocalDateTime}
     * at the specified {@code ZoneId}.
     *
     * @param file                the {@code File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @param zoneId              the time zone.
     * @return true if the {@code File} exists and has been modified after the given
     * {@code ChronoLocalDateTime} at the given {@code ZoneId}.
     * @throws NullPointerException if the file, local date time or zone ID is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileNewer(file, chronoLocalDateTime.atZone(zoneId));
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code ChronoZonedDateTime}.
     *
     * @param file                the {@code File} of which the modification date must be compared.
     * @param chronoZonedDateTime the date reference.
     * @return true if the {@code File} exists and has been modified after the given
     * {@code ChronoZonedDateTime}.
     * @throws NullPointerException if the file or zoned date time is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoZonedDateTime<?> chronoZonedDateTime) {
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return isFileNewer(file, chronoZonedDateTime.toInstant());
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code Date}.
     *
     * @param file the {@code File} of which the modification date must be compared.
     * @param date the date reference.
     * @return true if the {@code File} exists and has been modified
     * after the given {@code Date}.
     * @throws NullPointerException if the file or date is {@code null}.
     */
    public static boolean isFileNewer(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileNewer(file, date.getTime());
    }

    /**
     * Tests if the specified {@code File} is newer than the reference {@code File}.
     *
     * @param file      the {@code File} of which the modification date must be compared.
     * @param reference the {@code File} of which the modification date is used.
     * @return true if the {@code File} exists and has been modified more
     * recently than the reference {@code File}.
     * @throws NullPointerException if the file or reference file is {@code null}.
     * @throws IllegalArgumentException if the reference file doesn't exist.
     */
    public static boolean isFileNewer(final File file, final File reference) {
        requireExists(reference, "reference");
        return isFileNewer(file, reference.lastModified());
    }

    /**
     * Tests if the specified {@code File} is newer than the specified {@code Instant}.
     *
     * @param file    the {@code File} of which the modification date must be compared.
     * @param instant the date reference.
     * @return true if the {@code File} exists and has been modified after the given {@code Instant}.
     * @throws NullPointerException if the file or instant is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return isFileNewer(file, instant.toEpochMilli());
    }

    /**
     * Tests if the specified {@code File} is newer than the specified time reference.
     *
     * @param file       the {@code File} of which the modification date must be compared.
     * @param timeMillis the time reference measured in milliseconds since the
     *                   epoch (00:00:00 GMT, January 1, 1970).
     * @return true if the {@code File} exists and has been modified after the given time reference.
     * @throws NullPointerException if the file is {@code null}.
     */
    public static boolean isFileNewer(final File file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() > timeMillis;
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code ChronoLocalDate}
     * at the current time.
     *
     * <p>Note: The input date is assumed to be in the system default time-zone with the time
     * part set to the current time. To use a non-default time-zone use the method
     * {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDate.atTime(LocalTime.now(zoneId)), zoneId)} where
     * {@code zoneId} is a valid {@link ZoneId}.
     *
     * @param file            the {@code File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @return true if the {@code File} exists and has been modified before the given
     * {@code ChronoLocalDate} at the current time.
     * @throws NullPointerException if the file or local date is {@code null}.
     * @see ZoneId#systemDefault()
     * @see LocalTime#now()
     *
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileOlder(file, chronoLocalDate, LocalTime.now());
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code ChronoLocalDate}
     * at the specified {@code LocalTime}.
     *
     * <p>Note: The input date and time are assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDate.atTime(localTime), zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file            the {@code File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @param localTime       the time reference.
     * @return true if the {@code File} exists and has been modified before the
     * given {@code ChronoLocalDate} at the specified time.
     * @throws NullPointerException if the file, local date or local time is {@code null}.
     * @see ZoneId#systemDefault()
     *
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileOlder(file, chronoLocalDate.atTime(localTime));
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code ChronoLocalDateTime}
     * at the system-default time zone.
     *
     * <p>Note: The input date and time is assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDateTime, zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file                the {@code File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @return true if the {@code File} exists and has been modified before the given
     * {@code ChronoLocalDateTime} at the system-default time zone.
     * @throws NullPointerException if the file or local date time is {@code null}.
     * @see ZoneId#systemDefault()
     *
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime) {
        return isFileOlder(file, chronoLocalDateTime, ZoneId.systemDefault());
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code ChronoLocalDateTime}
     * at the specified {@code ZoneId}.
     *
     * @param file          the {@code File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @param zoneId        the time zone.
     * @return true if the {@code File} exists and has been modified before the given
     * {@code ChronoLocalDateTime} at the given {@code ZoneId}.
     * @throws NullPointerException if the file, local date time or zone ID is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileOlder(file, chronoLocalDateTime.atZone(zoneId));
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code ChronoZonedDateTime}.
     *
     * @param file                the {@code File} of which the modification date must be compared.
     * @param chronoZonedDateTime the date reference.
     * @return true if the {@code File} exists and has been modified before the given
     * {@code ChronoZonedDateTime}.
     * @throws NullPointerException if the file or zoned date time is {@code null}.
     *
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoZonedDateTime<?> chronoZonedDateTime) {
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return isFileOlder(file, chronoZonedDateTime.toInstant());
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code Date}.
     *
     * @param file the {@code File} of which the modification date must be compared.
     * @param date the date reference.
     * @return true if the {@code File} exists and has been modified before the given {@code Date}.
     * @throws NullPointerException if the file or date is {@code null}.
     */
    public static boolean isFileOlder(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileOlder(file, date.getTime());
    }

    /**
     * Tests if the specified {@code File} is older than the reference {@code File}.
     *
     * @param file      the {@code File} of which the modification date must be compared.
     * @param reference the {@code File} of which the modification date is used.
     * @return true if the {@code File} exists and has been modified before the reference {@code File}.
     * @throws NullPointerException if the file or reference file is {@code null}.
     * @throws IllegalArgumentException if the reference file doesn't exist.
     */
    public static boolean isFileOlder(final File file, final File reference) {
        requireExists(reference, "reference");
        return isFileOlder(file, reference.lastModified());
    }

    /**
     * Tests if the specified {@code File} is older than the specified {@code Instant}.
     *
     * @param file    the {@code File} of which the modification date must be compared.
     * @param instant the date reference.
     * @return true if the {@code File} exists and has been modified before the given {@code Instant}.
     * @throws NullPointerException if the file or instant is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return isFileOlder(file, instant.toEpochMilli());
    }

    /**
     * Tests if the specified {@code File} is older than the specified time reference.
     *
     * @param file       the {@code File} of which the modification date must be compared.
     * @param timeMillis the time reference measured in milliseconds since the
     *                   epoch (00:00:00 GMT, January 1, 1970).
     * @return true if the {@code File} exists and has been modified before the given time reference.
     * @throws NullPointerException if the file is {@code null}.
     */
    public static boolean isFileOlder(final File file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() < timeMillis;
    }

    /**
     * Tests whether the specified file is a symbolic link rather than an actual file.
     * <p>
     * This method delegates to {@link Files#isSymbolicLink(Path path)}
     * </p>
     * 
     * @param file the file to test.
     * @return true if the file is a symbolic link, see {@link Files#isSymbolicLink(Path path)}.
     * @since 2.0
     * @see Files#isSymbolicLink(Path)
     */
    public static boolean isSymlink(final File file) {
        return file != null ? Files.isSymbolicLink(file.toPath()) : false;
    }

    /**
     * Iterates over the files in given directory (and optionally
     * its subdirectories).
     * <p>
     * The resulting iterator MUST be consumed in its entirety in order to close its underlying stream.
     * </p>
     * <p>
     * All files found are filtered by an IOFileFilter.
     * </p>
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an iterator of java.io.File for the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 1.2
     */
    public static Iterator<File> iterateFiles(final File directory, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter) {
        return listFiles(directory, fileFilter, dirFilter).iterator();
    }

    /**
     * Iterates over the files in a given directory (and optionally
     * its subdirectories) which match an array of extensions.
     * <p>
     * The resulting iterator MUST be consumed in its entirety in order to close its underlying stream.
     * </p>
     * <p>
     *
     * @param directory  the directory to search in
     * @param extensions an array of extensions, ex. {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @param recursive  if true all subdirectories are searched as well
     * @return an iterator of java.io.File with the matching files
     * @since 1.2
     */
    public static Iterator<File> iterateFiles(final File directory, final String[] extensions,
        final boolean recursive) {
        try {
            return StreamIterator.iterator(streamFiles(directory, recursive, extensions));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Iterates over the files in given directory (and optionally
     * its subdirectories).
     * <p>
     * The resulting iterator MUST be consumed in its entirety in order to close its underlying stream.
     * </p>
     * <p>
     * All files found are filtered by an IOFileFilter.
     * </p>
     * <p>
     * The resulting iterator includes the subdirectories themselves.
     * </p>
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an iterator of java.io.File for the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 2.2
     */
    public static Iterator<File> iterateFilesAndDirs(final File directory, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter) {
        return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
    }

    /**
     * Returns an Iterator for the lines in a <code>File</code> using the default encoding for the VM.
     *
     * @param file the file to open for input, must not be {@code null}
     * @return an Iterator of the lines in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @see #lineIterator(File, String)
     * @since 1.3
     */
    public static LineIterator lineIterator(final File file) throws IOException {
        return lineIterator(file, null);
    }

    /**
     * Returns an Iterator for the lines in a <code>File</code>.
     * <p>
     * This method opens an <code>InputStream</code> for the file.
     * When you have finished with the iterator you should close the stream
     * to free internal resources. This can be done by calling the
     * {@link LineIterator#close()} or
     * {@link LineIterator#closeQuietly(LineIterator)} method.
     * </p>
     * <p>
     * The recommended usage pattern is:
     * </p>
     * <pre>
     * LineIterator it = FileUtils.lineIterator(file, "UTF-8");
     * try {
     *   while (it.hasNext()) {
     *     String line = it.nextLine();
     *     /// do something with line
     *   }
     * } finally {
     *   LineIterator.closeQuietly(iterator);
     * }
     * </pre>
     * <p>
     * If an exception occurs during the creation of the iterator, the
     * underlying stream is closed.
     * </p>
     *
     * @param file     the file to open for input, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return an Iterator of the lines in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.2
     */
    public static LineIterator lineIterator(final File file, final String charsetName) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = openInputStream(file);
            return IOUtils.lineIterator(inputStream, charsetName);
        } catch (final IOException | RuntimeException ex) {
            IOUtils.closeQuietly(inputStream, e -> ex.addSuppressed(e));
            throw ex;
        }
    }

    private static AccumulatorPathVisitor listAccumulate(final File directory, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter) throws IOException {
        final boolean isDirFilterSet = dirFilter != null;
        final FileEqualsFileFilter rootDirFilter = new FileEqualsFileFilter(directory);
        final PathFilter dirPathFilter = isDirFilterSet ? rootDirFilter.or(dirFilter) : rootDirFilter;
        final AccumulatorPathVisitor visitor = new AccumulatorPathVisitor(Counters.noopPathCounters(), fileFilter,
            dirPathFilter);
        Files.walkFileTree(directory.toPath(), Collections.emptySet(), toMaxDepth(isDirFilterSet), visitor);
        return visitor;
    }

    /**
     * Lists files in a directory, asserting that the supplied directory exists and is a directory.
     *
     * @param directory The directory to list
     * @param fileFilter Optional file filter, may be null.
     * @return The files in the directory, never {@code null}.
     * @throws NullPointerException if directory is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     */
    private static File[] listFiles(final File directory, FileFilter fileFilter) throws IOException {
        requireDirectoryExists(directory, "directory");
        final File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
        if (files == null) {
            // null if the directory does not denote a directory, or if an I/O error occurs.
            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
        }
        return files;
    }

    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     * <p>
     * If your search should recurse into subdirectories you can pass in
     * an IOFileFilter for directories. You don't need to bind a
     * DirectoryFileFilter (via logical AND) to this filter. This method does
     * that for you.
     * </p>
     * <p>
     * An example: If you want to search through all directories called
     * "temp" you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
     * </p>
     * <p>
     * Another common usage of this method is find files in a directory
     * tree but ignoring the directories generated CVS. You can simply pass
     * in <code>FileFilterUtils.makeCVSAware(null)</code>.
     * </p>
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files. Must not be {@code null},
     *                   use {@link TrueFileFilter#INSTANCE} to match all files in selected directories.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use {@link TrueFileFilter#INSTANCE} to match all directories.
     * @return a collection of java.io.File with the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     */
    public static Collection<File> listFiles(
        final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        try {
            final AccumulatorPathVisitor visitor = listAccumulate(directory, fileFilter, dirFilter);
            return visitor.getFileList().stream().map(Path::toFile).collect(Collectors.toList());
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories)
     * which match an array of extensions.
     *
     * @param directory  the directory to search in
     * @param extensions an array of extensions, ex. {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @param recursive  if true all subdirectories are searched as well
     * @return a collection of java.io.File with the matching files
     */
    public static Collection<File> listFiles(final File directory, final String[] extensions, final boolean recursive) {
        try {
            return toList(streamFiles(directory, recursive, extensions));
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     * <p>
     * The resulting collection includes the starting directory and
     * any subdirectories that match the directory filter.
     * </p>
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return a collection of java.io.File with the matching files
     * @see org.apache.commons.io.FileUtils#listFiles
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 2.2
     */
    public static Collection<File> listFilesAndDirs(
        final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        try {
            final AccumulatorPathVisitor visitor = listAccumulate(directory, fileFilter, dirFilter);
            final List<Path> list = visitor.getFileList();
            list.addAll(visitor.getDirList());
            return list.stream().map(Path::toFile).collect(Collectors.toList());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Calls {@link File#mkdirs()} and throws an exception on failure.
     *
     * @param directory the receiver for {@code mkdirs()}, may be null.
     * @return the given file, may be null.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see @see File#mkdirs()
     */
    private static File mkdirs(final File directory) throws IOException {
        if (directory != null) {
            if (!directory.mkdirs() && !directory.isDirectory()) {
                throw new IOException("Cannot create directory '" + directory + "'.");
            }
        }
        return directory;
    }

    /**
     * Moves a directory.
     * <p>
     * When the destination directory is on another file system, do a "copy and delete".
     * </p>
     *
     * @param srcDir the directory to be moved.
     * @param destDir the destination directory.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void moveDirectory(final File srcDir, final File destDir) throws IOException {
        validateMoveParameters(srcDir, destDir);
        requireDirectory(srcDir, "srcDir");
        requireAbsent(destDir, "destDir");
        if (!srcDir.renameTo(destDir)) {
            if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath() + File.separator)) {
                throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
            }
            copyDirectory(srcDir, destDir);
            deleteDirectory(srcDir);
            if (srcDir.exists()) {
                throw new IOException("Failed to delete original directory '" + srcDir +
                        "' after copy to '" + destDir + "'");
            }
        }
    }

    /**
     * Moves a directory to another directory.
     *
     * @param src the file to be moved.
     * @param destDir the destination file.
     * @param createDestDir If {@code true} create the destination directory, otherwise if {@code false} throw an
     *        IOException.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void moveDirectoryToDirectory(final File src, final File destDir, final boolean createDestDir)
            throws IOException {
        validateMoveParameters(src, destDir);
        if (!destDir.isDirectory()) {
            if (destDir.exists()) {
                throw new IOException("Destination '" + destDir + "' is not a directory");
            } else if (createDestDir) {
                mkdirs(destDir);
            } else {
                throw new FileNotFoundException("Destination directory '" + destDir +
                        "' does not exist [createDestDir=" + createDestDir + "]");
            }
        }
        moveDirectory(src, new File(destDir, src.getName()));
    }

    /**
     * Moves a file.
     * <p>
     * When the destination file is on another file system, do a "copy and delete".
     * </p>
     *
     * @param srcFile the file to be moved.
     * @param destFile the destination file.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileExistsException if the destination file exists.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void moveFile(final File srcFile, final File destFile) throws IOException {
        validateMoveParameters(srcFile, destFile);
        requireFile(srcFile, "srcFile");
        requireAbsent(destFile, null);
        final boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            copyFile(srcFile, destFile);
            if (!srcFile.delete()) {
                FileUtils.deleteQuietly(destFile);
                throw new IOException("Failed to delete original file '" + srcFile +
                        "' after copy to '" + destFile + "'");
            }
        }
    }

    /**
     * Moves a file to a directory.
     *
     * @param srcFile the file to be moved.
     * @param destDir the destination file.
     * @param createDestDir If {@code true} create the destination directory, otherwise if {@code false} throw an
     *        IOException.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileExistsException if the destination file exists.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void moveFileToDirectory(final File srcFile, final File destDir, final boolean createDestDir)
            throws IOException {
        validateMoveParameters(srcFile, destDir);
        if (!destDir.exists() && createDestDir) {
            mkdirs(destDir);
        }
        requireExistsChecked(destDir, "destDir");
        requireDirectory(destDir, "destDir");
        moveFile(srcFile, new File(destDir, srcFile.getName()));
    }

    /**
     * Moves a file or directory to the destination directory.
     * <p>
     * When the destination is on another file system, do a "copy and delete".
     * </p>
     *
     * @param src the file or directory to be moved.
     * @param destDir the destination directory.
     * @param createDestDir If {@code true} create the destination directory, otherwise if {@code false} throw an
     *        IOException.
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileExistsException if the directory or file exists in the destination directory.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeeded.
     * @since 1.4
     */
    public static void moveToDirectory(final File src, final File destDir, final boolean createDestDir)
            throws IOException {
        validateMoveParameters(src, destDir);
        if (src.isDirectory()) {
            moveDirectoryToDirectory(src, destDir, createDestDir);
        } else {
            moveFileToDirectory(src, destDir, createDestDir);
        }
    }

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better error messages than simply calling
     * <code>new FileInputStream(file)</code>.
     * <p>
     * At the end of the method either the stream will be successfully opened, or an exception will have been thrown.
     * </p>
     * <p>
     * An exception is thrown if the file does not exist. An exception is thrown if the file object exists but is a
     * directory. An exception is thrown if the file exists but cannot be read.
     * </p>
     *
     * @param file the file to open for input, must not be {@code null}
     * @return a new {@link FileInputStream} for the specified file
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException See FileNotFoundException above, FileNotFoundException is a subclass of IOException.
     * @since 1.3
     */
    public static FileInputStream openInputStream(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return new FileInputStream(file);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * </p>
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     * </p>
     *
     * @param file the file to open for output, must not be {@code null}
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 1.3
     */
    public static FileOutputStream openOutputStream(final File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * </p>
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     * </p>
     *
     * @param file   the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws NullPointerException if the file object is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.exists()) {
            requireFile(file, "file");
            requireCanWrite(file, "file");
        } else {
            createParentDirectories(file);
        }
        return new FileOutputStream(file, append);
    }

    /**
     * Reads the contents of a file into a byte array.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.1
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        try (InputStream in = openInputStream(file)) {
            final long fileLength = file.length();
            // file.length() may return 0 for system-dependent entities, treat 0 as unknown length - see IO-453
            return fileLength > 0 ? IOUtils.toByteArray(in, fileLength) : IOUtils.toByteArray(in);
        }
    }

    /**
     * Reads the contents of a file into a String using the default encoding for the VM.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.3.1
     * @deprecated 2.5 use {@link #readFileToString(File, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static String readFileToString(final File file) throws IOException {
        return readFileToString(file, Charset.defaultCharset());
    }

    /**
     * Reads the contents of a file into a String.
     * The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 2.3
     */
    public static String readFileToString(final File file, final Charset charsetName) throws IOException {
        try (InputStream in = openInputStream(file)) {
            return IOUtils.toString(in, Charsets.toCharset(charsetName));
        }
    }

    /**
     * Reads the contents of a file into a String. The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the named charset is unavailable.
     * @since 2.3
     */
    public static String readFileToString(final File file, final String charsetName) throws IOException {
        return readFileToString(file, Charsets.toCharset(charsetName));
    }

    /**
     * Reads the contents of a file line by line to a List of Strings using the default encoding for the VM.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.3
     * @deprecated 2.5 use {@link #readLines(File, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static List<String> readLines(final File file) throws IOException {
        return readLines(file, Charset.defaultCharset());
    }

    /**
     * Reads the contents of a file line by line to a List of Strings.
     * The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charset the charset to use, {@code null} means platform default
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 2.3
     */
    public static List<String> readLines(final File file, final Charset charset) throws IOException {
        try (InputStream in = openInputStream(file)) {
            return IOUtils.readLines(in, Charsets.toCharset(charset));
        }
    }

    /**
     * Reads the contents of a file line by line to a List of Strings. The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the named charset is unavailable.
     * @since 1.1
     */
    public static List<String> readLines(final File file, final String charsetName) throws IOException {
        return readLines(file, Charsets.toCharset(charsetName));
    }

    private static void requireAbsent(final File file, String name) throws FileExistsException {
        if (file.exists()) {
            throw new FileExistsException(
                String.format("File element in parameter '%s' already exists: '%s'", name, file));
        }
    }


    /**
     * Throws IllegalArgumentException if the given files' canonical representations are equal.
     * 
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @throws IllegalArgumentException if the given files' canonical representations are equal.
     */
    private static void requireCanonicalPathsNotEquals(final File file1, final File file2) throws IOException {
        final String canonicalPath = file1.getCanonicalPath();
        if (canonicalPath.equals(file2.getCanonicalPath())) {
            throw new IllegalArgumentException(String
                .format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the file is not writable.
     * 
     * @param file The file to test.
     * @param name The parameter name to use in the exception message.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the file is not writable.
     */
    private static void requireCanWrite(final File file, String name) {
        Objects.requireNonNull(file, "file");
        if (!file.canWrite()) {
            throw new IllegalArgumentException("File parameter '" + name + " is non-writable: '" + file + "'");
        }
    }

    /**
     * Requires that the given {@code File} is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name 
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectory(final File directory, String name) {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    /**
     * Requires that the given {@code File} exists and is a directory.
     *
     * @param directory The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireDirectoryExists(final File directory, String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    /**
     * Requires that the given {@code File} is a directory if it exists.
     *
     * @param directory The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} exists but is not a directory.
     */
    private static File requireDirectoryIfExists(final File directory, String name) {
        Objects.requireNonNull(directory, name);
        if (directory.exists()) {
            requireDirectory(directory, name);
        }
        return directory;
    }

    /**
     * Requires that two file lengths are equal.
     *
     * @param srcFile Source file.
     * @param destFile Destination file.
     * @param srcLen Source file length.
     * @param dstLen Destination file length
     * @throws IOException Thrown when the given sizes are not equal.
     */
    private static void requireEqualSizes(final File srcFile, final File destFile, final long srcLen, final long dstLen)
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link IllegalArgumentException} if it doesn't.
     *
     * @param file The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist.
     */
    private static File requireExists(final File file, String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException(
                "File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link FileNotFoundException} if it doesn't.
     *
     * @param file The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws FileNotFoundException if the given {@code File} does not exist.
     */
    private static File requireExistsChecked(final File file, String fileParamName) throws FileNotFoundException {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new FileNotFoundException(
                "File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    /**
     * Requires that the given {@code File} is a file.
     *
     * @param file The {@code File} to check.
     * @param name The parameter name to use in the exception message.
     * @return the given file.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireFile(final File file, String name) {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
        }
        return file;
    }

    /**
     * Requires parameter attributes for a file copy operation.
     *
     * @param source the source file
     * @param destination the destination
     * @throws NullPointerException if any of the given {@code File}s are {@code null}.
     * @throws FileNotFoundException if the source does not exist.
     */
    private static void requireFileCopy(final File source, final File destination) throws FileNotFoundException {
        requireExistsChecked(source, "source");
        Objects.requireNonNull(destination, "destination");
    }

    /**
     * Requires that the given {@code File} is a file if it exists.
     *
     * @param file The {@code File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @return the given directory.
     * @throws NullPointerException if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does exists but is not a directory.
     */
    private static File requireFileIfExists(final File file, String name) {
        Objects.requireNonNull(file, name);
        return file.exists() ? requireFile(file, name) : file;
    }

    /**
     * Sets the given {@code targetFile}'s last modified date to the value from {@code sourceFile}.
     *
     * @param sourceFile The source file to query.
     * @param targetFile The target file to set.
     * @throws NullPointerException if sourceFile is {@code null}.
     * @throws NullPointerException if targetFile is {@code null}.
     * @throws IOException if setting the last-modified time failed.
     */
    private static void setLastModified(final File sourceFile, final File targetFile) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        setLastModified(targetFile, sourceFile.lastModified());
    }

    /**
     * Sets the given {@code targetFile}'s last modified date to the given value.
     *
     * @param file The source file to query.
     * @param timeMillis The new last-modified time, measured in milliseconds since the epoch 01-01-1970 GMT.
     * @throws NullPointerException if file is {@code null}.
     * @throws IOException if setting the last-modified time failed.
     */
    private static void setLastModified(final File file, final long timeMillis) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.setLastModified(timeMillis)) {
            throw new IOException(String.format("Failed setLastModified(%s) on '%s'", timeMillis, file));
        }
    }

    /**
     * Returns the size of the specified file or directory. If the provided
     * {@link File} is a regular file, then the file's length is returned.
     * If the argument is a directory, then the size of the directory is
     * calculated recursively. If a directory or subdirectory is security
     * restricted, its size will not be included.
     * <p>
     * Note that overflow is not detected, and the return value may be negative if
     * overflow occurs. See {@link #sizeOfAsBigInteger(File)} for an alternative
     * method that does not overflow.
     * </p>
     *
     * @param file the regular file or directory to return the size
     *             of (must not be {@code null}).
     *
     * @return the length of the file, or recursive size of the directory,
     * provided (in bytes).
     *
     * @throws NullPointerException     if the file is {@code null}.
     * @throws IllegalArgumentException if the file does not exist.
     *
     * @since 2.0
     */
    public static long sizeOf(final File file) {
        requireExists(file, "file");
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }

    /**
     * Gets the size of a file.
     *
     * @param file the file to check.
     * @return the size of the file.
     * @throws NullPointerException if the file is {@code null}.
     */
    private static long sizeOf0(final File file) {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length(); // will be 0 if file does not exist
    }

    /**
     * Returns the size of the specified file or directory. If the provided
     * {@link File} is a regular file, then the file's length is returned.
     * If the argument is a directory, then the size of the directory is
     * calculated recursively. If a directory or subdirectory is security
     * restricted, its size will not be included.
     *
     * @param file the regular file or directory to return the size
     *             of (must not be {@code null}).
     *
     * @return the length of the file, or recursive size of the directory,
     * provided (in bytes).
     *
     * @throws NullPointerException     if the file is {@code null}.
     * @throws IllegalArgumentException if the file does not exist.
     *
     * @since 2.4
     */
    public static BigInteger sizeOfAsBigInteger(final File file) {
        requireExists(file, "file");
        return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
    }

    /**
     * Returns the size of a file or directory.
     *
     * @param file The file or directory.
     * @return the size
     */
    private static BigInteger sizeOfBig0(final File file) {
        Objects.requireNonNull(file, "fileOrDir");
        return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
    }

    /**
     * Counts the size of a directory recursively (sum of the length of all files).
     * <p>
     * Note that overflow is not detected, and the return value may be negative if
     * overflow occurs. See {@link #sizeOfDirectoryAsBigInteger(File)} for an alternative
     * method that does not overflow.
     * </p>
     *
     * @param directory directory to inspect, must not be {@code null}.
     * @return size of directory in bytes, 0 if directory is security restricted, a negative number when the real total
     * is greater than {@link Long#MAX_VALUE}.
     * @throws NullPointerException if the directory is {@code null}.
     */
    public static long sizeOfDirectory(final File directory) {
        return sizeOfDirectory0(requireDirectoryExists(directory, "directory"));
    }

    /**
     * Gets the size of a directory.
     *
     * @param directory the directory to check
     * @return the size
     * @throws NullPointerException if the directory is {@code null}.
     */
    private static long sizeOfDirectory0(final File directory) {
        Objects.requireNonNull(directory, "directory");
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size += sizeOf0(file);
                if (size < 0) {
                    break;
                }
            }
        }

        return size;
    }

    /**
     * Counts the size of a directory recursively (sum of the length of all files).
     *
     * @param directory directory to inspect, must not be {@code null}.
     * @return size of directory in bytes, 0 if directory is security restricted.
     * @throws NullPointerException if the directory is {@code null}.
     * @since 2.4
     */
    public static BigInteger sizeOfDirectoryAsBigInteger(final File directory) {
        return sizeOfDirectoryBig0(requireDirectoryExists(directory, "directory"));
    }

    /**
     * Computes the size of a directory.
     *
     * @param directory The directory.
     * @return the size.
     */
    private static BigInteger sizeOfDirectoryBig0(final File directory) {
        Objects.requireNonNull(directory, "directory");
        final File[] files = directory.listFiles();
        if (files == null) {
            // null if security restricted
            return BigInteger.ZERO;
        }
        BigInteger size = BigInteger.ZERO;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size = size.add(sizeOfBig0(file));
            }
        }

        return size;
    }

    /**
     * Streams over the files in a given directory (and optionally
     * its subdirectories) which match an array of extensions.
     *
     * @param directory  the directory to search in
     * @param recursive  if true all subdirectories are searched as well
     * @param extensions an array of extensions, ex. {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @return an iterator of java.io.File with the matching files
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     * @since 2.9.0
     */
    public static Stream<File> streamFiles(final File directory, final boolean recursive, final String... extensions)
        throws IOException {
        final IOFileFilter filter = extensions == null ? FileFileFilter.INSTANCE
            : FileFileFilter.INSTANCE.and(new SuffixFileFilter(toSuffixes(extensions)));
        return PathUtils.walk(directory.toPath(), filter, toMaxDepth(recursive), false).map(Path::toFile);
    }

    /**
     * Converts from a <code>URL</code> to a <code>File</code>.
     * <p>
     * From version 1.1 this method will decode the URL.
     * Syntax such as <code>file:///my%20docs/file.txt</code> will be
     * correctly decoded to <code>/my docs/file.txt</code>. Starting with version
     * 1.5, this method uses UTF-8 to decode percent-encoded octets to characters.
     * Additionally, malformed percent-encoded octets are handled leniently by
     * passing them through literally.
     * </p>
     *
     * @param url the file URL to convert, {@code null} returns {@code null}
     * @return the equivalent <code>File</code> object, or {@code null}
     * if the URL's protocol is not <code>file</code>
     */
    public static File toFile(final URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        final String filename = url.getFile().replace('/', File.separatorChar);
        return new File(decodeUrl(filename));
    }

    /**
     * Converts each of an array of <code>URL</code> to a <code>File</code>.
     * <p>
     * Returns an array of the same size as the input.
     * If the input is {@code null}, an empty array is returned.
     * If the input contains {@code null}, the output array contains {@code null} at the same
     * index.
     * </p>
     * <p>
     * This method will decode the URL.
     * Syntax such as <code>file:///my%20docs/file.txt</code> will be
     * correctly decoded to <code>/my docs/file.txt</code>.
     * </p>
     *
     * @param urls the file URLs to convert, {@code null} returns empty array
     * @return a non-{@code null} array of Files matching the input, with a {@code null} item
     * if there was a {@code null} at that index in the input array
     * @throws IllegalArgumentException if any file is not a URL file
     * @throws IllegalArgumentException if any file is incorrectly encoded
     * @since 1.1
     */
    public static File[] toFiles(final URL... urls) {
        if (urls == null || urls.length == 0) {
            return EMPTY_FILE_ARRAY;
        }
        final File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            final URL url = urls[i];
            if (url != null) {
                if (!"file".equalsIgnoreCase(url.getProtocol())) {
                    throw new IllegalArgumentException("Can only convert file URL to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    private static List<File> toList(final Stream<File> stream) {
        return stream.collect(Collectors.toList());
    }

    /**
     * Converts whether or not to recurse into a recursion max depth.
     *
     * @param recursive whether or not to recurse 
     * @return the recursion depth
     */
    private static int toMaxDepth(final boolean recursive) {
        return recursive ? Integer.MAX_VALUE : 1;
    }

    /**
     * Converts an array of file extensions to suffixes.
     *
     * @param extensions an array of extensions. Format: {"java", "xml"}
     * @return an array of suffixes. Format: {".java", ".xml"}
     * @throws NullPointerException
     */
    private static String[] toSuffixes(final String... extensions) {
        Objects.requireNonNull(extensions, "extensions");
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    /**
     * Implements the same behavior as the "touch" utility on Unix. It creates
     * a new file with size 0 or, if the file exists already, it is opened and
     * closed without modifying it, but updating the file date and time.
     * <p>
     * NOTE: As from v1.3, this method throws an IOException if the last
     * modified date of the file cannot be set. Also, as from v1.3 this method
     * creates parent directories if they do not exist.
     * </p>
     *
     * @param file the File to touch.
     * @throws IOException if an I/O problem occurs. 
     * @throws IOException if setting the last-modified time failed.
     */
    public static void touch(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!file.exists()) {
            openOutputStream(file).close();
        }
        setLastModified(file, System.currentTimeMillis());
    }

    /**
     * Converts each of an array of <code>File</code> to a <code>URL</code>.
     * <p>
     * Returns an array of the same size as the input.
     * </p>
     *
     * @param files the files to convert, must not be {@code null}
     * @return an array of URLs matching the input
     * @throws IOException          if a file cannot be converted
     * @throws NullPointerException if the parameter is null
     */
    public static URL[] toURLs(final File... files) throws IOException {
        Objects.requireNonNull(files, "files");
        final URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    /**
     * Validates the given arguments.
     * <ul>
     * <li>Throws {@link NullPointerException} if {@code source} is null</li>
     * <li>Throws {@link NullPointerException} if {@code destination} is null</li>
     * <li>Throws {@link FileNotFoundException} if {@code source} does not exist</li>
     * </ul>
     *
     * @param source      the file or directory to be moved
     * @param destination the destination file or directory
     * @throws FileNotFoundException if {@code source} file does not exist
     */
    private static void validateMoveParameters(final File source, final File destination) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }
    }

    /**
     * Waits for NFS to propagate a file creation, imposing a timeout.
     * <p>
     * This method repeatedly tests {@link File#exists()} until it returns
     * true up to the maximum time specified in seconds.
     * </p>
     *
     * @param file    the file to check, must not be {@code null}
     * @param seconds the maximum time in seconds to wait
     * @return true if file exists
     * @throws NullPointerException if the file is {@code null}
     */
    public static boolean waitFor(final File file, final int seconds) {
        Objects.requireNonNull(file, "file");
        final long finishAt = System.currentTimeMillis() + (seconds * 1000L);
        boolean wasInterrupted = false;
        try {
            while (!file.exists()) {
                final long remaining = finishAt -  System.currentTimeMillis();
                if (remaining < 0){
                    return false;
                }
                try {
                    Thread.sleep(Math.min(100, remaining));
                } catch (final InterruptedException ignore) {
                    wasInterrupted = true;
                } catch (final Exception ex) {
                    break;
                }
            }
        } finally {
            if (wasInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return true;
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 2.0
     * @deprecated 2.5 use {@link #write(File, CharSequence, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void write(final File file, final CharSequence data) throws IOException {
        write(file, data, Charset.defaultCharset(), false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file   the file to write
     * @param data   the content to write to the file
     * @param append if {@code true}, then the data will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     * @deprecated 2.5 use {@link #write(File, CharSequence, Charset, boolean)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void write(final File file, final CharSequence data, final boolean append) throws IOException {
        write(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the name of the requested charset, {@code null} means platform default
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void write(final File file, final CharSequence data, final Charset charset) throws IOException {
        write(file, data, charset, false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append   if {@code true}, then the data will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void write(final File file, final CharSequence data, final Charset charset, final boolean append)
            throws IOException {
        writeStringToFile(file, Objects.toString(data, null), charset, append);
    }

    // Private method, must be invoked will a directory parameter

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.0
     */
    public static void write(final File file, final CharSequence data, final String charsetName) throws IOException {
        write(file, data, charsetName, false);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append   if {@code true}, then the data will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                 in case of an I/O error
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void write(final File file, final CharSequence data, final String charsetName, final boolean append)
            throws IOException {
        write(file, data, Charsets.toCharset(charsetName), append);
    }

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 1.1
     */
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    // Must be called with a directory

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append)
            throws IOException {
        writeByteArrayToFile(file, data, 0, data.length, append);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting
     * at offset {@code off} to a file, creating the file if it does
     * not exist.
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @param off  the start offset in the data
     * @param len  the number of bytes to write
     * @throws IOException in case of an I/O error
     * @since 2.5
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len)
            throws IOException {
        writeByteArrayToFile(file, data, off, len, false);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting
     * at offset {@code off} to a file, creating the file if it does
     * not exist.
     *
     * @param file   the file to write to
     * @param data   the content to write to the file
     * @param off    the start offset in the data
     * @param len    the number of bytes to write
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.5
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len,
                                            final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data, off, len);
        }
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file  the file to write to
     * @param lines the lines to write, {@code null} entries produce blank lines
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null, false);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file   the file to write to
     * @param lines  the lines to write, {@code null} entries produce blank lines
     * @param append if {@code true}, then the lines will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final boolean append) throws IOException {
        writeLines(file, null, lines, null, append);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding)
            throws IOException {
        writeLines(file, null, lines, lineEnding, false);
    }


    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @param append     if {@code true}, then the lines will be added to the
     *                   end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding,
                                  final boolean append) throws IOException {
        writeLines(file, null, lines, lineEnding, append);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The specified character encoding and the default line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines    the lines to write, {@code null} entries produce blank lines
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines)
            throws IOException {
        writeLines(file, charsetName, lines, null, false);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line, optionally appending.
     * The specified character encoding and the default line ending will be used.
     *
     * @param file     the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines    the lines to write, {@code null} entries produce blank lines
     * @param append   if {@code true}, then the lines will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final boolean append) throws IOException {
        writeLines(file, charsetName, lines, null, append);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The specified character encoding and the line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file       the file to write to
     * @param charsetName   the name of the requested charset, {@code null} means platform default
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final String lineEnding) throws IOException {
        writeLines(file, charsetName, lines, lineEnding, false);
    }

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The specified character encoding and the line ending will be used.
     *
     * @param file       the file to write to
     * @param charsetName   the name of the requested charset, {@code null} means platform default
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @param append     if {@code true}, then the lines will be added to the
     *                   end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final String lineEnding, final boolean append) throws IOException {
        try (OutputStream out = new BufferedOutputStream(openOutputStream(file, append))) {
            IOUtils.writeLines(lines, lineEnding, out, charsetName);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @deprecated 2.5 use {@link #writeStringToFile(File, String, Charset)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void writeStringToFile(final File file, final String data) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file   the file to write
     * @param data   the content to write to the file
     * @param append if {@code true}, then the String will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     * @deprecated 2.5 use {@link #writeStringToFile(File, String, Charset, boolean)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void writeStringToFile(final File file, final String data, final boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.4
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset)
            throws IOException {
        writeStringToFile(file, data, charset, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset,
                                         final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            IOUtils.write(data, out, charset);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeStringToFile(final File file, final String data, final String charsetName) throws IOException {
        writeStringToFile(file, data, charsetName, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                 in case of an I/O error
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     * .UnsupportedEncodingException} in version 2.2 if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeStringToFile(final File file, final String data, final String charsetName,
                                         final boolean append) throws IOException {
        writeStringToFile(file, data, Charsets.toCharset(charsetName), append);
    }

    /**
     * Instances should NOT be constructed in standard programming.
     * @deprecated Will be private in 3.0.
     */
    @Deprecated
    public FileUtils() { //NOSONAR
        
    }
}
