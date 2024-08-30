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

import java.io.BufferedInputStream;
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
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.Uncheck;

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
 * Provenance: Excalibur, Alexandria, Commons-Utils
 * </p>
 */
public class FileUtils {

    private static final String PROTOCOL_FILE = "file";

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
     * An empty array of type {@link File}.
     */
    public static final File[] EMPTY_FILE_ARRAY = {};

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
     * @throws NullPointerException if the given {@link BigInteger} is {@code null}.
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
     * @since 2.12.0
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(final Number size) {
        return byteCountToDisplaySize(size.longValue());
    }

    /**
     * Requires that the given {@link File} object
     * points to an actual file (not a directory) in the file system,
     * and throws a {@link FileNotFoundException} if it doesn't.
     * It throws an IllegalArgumentException if the object points to a directory.
     *
     * @param file The {@link File} to check.
     * @param name The parameter name to use in the exception message.
     * @throws FileNotFoundException if the file does not exist
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if the given {@link File} is not a file.
     */
    private static void checkFileExists(final File file, final String name) throws FileNotFoundException {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            if (file.exists()) {
                throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
            }
            if (!Files.isSymbolicLink(file.toPath())) {
                throw new FileNotFoundException("Source '" + file + "' does not exist");
            }
        }
    }

    private static File checkIsFile(final File file, final String name) {
        if (file.isFile()) {
            return file;
        }
        throw new IllegalArgumentException(String.format("Parameter '%s' is not a file: %s", name, file));
    }

    /**
     * Computes the checksum of a file using the specified checksum object. Multiple files may be checked using one
     * {@link Checksum} instance if desired simply by reusing the same checksum object. For example:
     *
     * <pre>
     * long checksum = FileUtils.checksum(file, new CRC32()).getValue();
     * </pre>
     *
     * @param file the file to checksum, must not be {@code null}
     * @param checksum the checksum object to be used, must not be {@code null}
     * @return the checksum specified, updated with the content of the file
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws NullPointerException if the given {@link Checksum} is {@code null}.
     * @throws IllegalArgumentException if the given {@link File} is not a file.
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if an IO error occurs reading the file.
     * @since 1.3
     */
    public static Checksum checksum(final File file, final Checksum checksum) throws IOException {
        checkFileExists(file, PROTOCOL_FILE);
        Objects.requireNonNull(checksum, "checksum");
        try (InputStream inputStream = new CheckedInputStream(Files.newInputStream(file.toPath()), checksum)) {
            IOUtils.consume(inputStream);
        }
        return checksum;
    }

    /**
     * Computes the checksum of a file using the CRC32 checksum routine.
     * The value of the checksum is returned.
     *
     * @param file the file to checksum, must not be {@code null}
     * @return the checksum value
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if the given {@link File} does not exist or is not a file.
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
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     * @see #forceDelete(File)
     */
    public static void cleanDirectory(final File directory) throws IOException {
        IOConsumer.forAll(FileUtils::forceDelete, listFiles(directory, null));
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean, must not be {@code null}
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if directory does not exist or is not a directory.
     * @throws IOException if an I/O error occurs.
     * @see #forceDeleteOnExit(File)
     */
    private static void cleanDirectoryOnExit(final File directory) throws IOException {
        IOConsumer.forAll(FileUtils::forceDeleteOnExit, listFiles(directory, null));
    }

    /**
     * Tests whether the contents of two files are equal.
     * <p>
     * This method checks to see if the two files are different lengths or if they point to the same file, before
     * resorting to byte-by-byte comparison of the contents.
     * </p>
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return true if the content of the files are equal or they both don't exist, false otherwise
     * @throws IllegalArgumentException when an input is not a file.
     * @throws IOException If an I/O error occurs.
     * @see PathUtils#fileContentEquals(Path,Path)
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

        checkIsFile(file1, "file1");
        checkIsFile(file2, "file2");

        if (file1.length() != file2.length()) {
            // lengths differ, cannot be equal
            return false;
        }

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        return PathUtils.fileContentEquals(file1.toPath(), file2.toPath());
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

        checkFileExists(file1, "file1");
        checkFileExists(file2, "file2");

        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            // same file
            return true;
        }

        final Charset charset = Charsets.toCharset(charsetName);
        try (Reader input1 = new InputStreamReader(Files.newInputStream(file1.toPath()), charset);
             Reader input2 = new InputStreamReader(Files.newInputStream(file2.toPath()), charset)) {
            return IOUtils.contentEqualsIgnoreEOL(input1, input2);
        }
    }

    /**
     * Converts a Collection containing {@link File} instances into array
     * representation. This is to account for the difference between
     * File.listFiles() and FileUtils.listFiles().
     *
     * @param files a Collection containing {@link File} instances
     * @return an array of {@link File}
     */
    public static File[] convertFileCollectionToFileArray(final Collection<File> files) {
        return files.toArray(EMPTY_FILE_ARRAY);
    }

    /**
     * Copies a whole directory to a new location, preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child directories and files to the specified destination.
     * The destination is the new location and name of the directory. That is, copying /home/bar to /tmp/bang
     * copies the contents of /home/bar into /tmp/bang. It does not create /tmp/bang/bar.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However it is
     * not guaranteed that the operation will succeed. If the modification operation fails, it falls back to
     * {@link File#setLastModified(long)}. If that fails, the method throws IOException.
     * </p>
     * <p>
     * Symbolic links in the source directory are copied to new symbolic links in the destination
     * directory that point to the original target. The target of the link is not copied unless
     * it is also under the source directory. Even if it is under the source directory, the new symbolic
     * link in the destination points to the original target in the source directory, not to the
     * newly created copy of the target.
     * </p>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory,
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time didn't succeed
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
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}. However it is not guaranteed that those operations
     * will succeed. If the modification operation fails, the method throws IOException.
     * </p>
     *
     * @param srcDir an existing directory to copy, must not be {@code null}.
     * @param destDir the new directory, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory, or
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time didn't succeed
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
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last modified date/times using
     * {@link File#setLastModified(long)}. However it is not guaranteed that those operations will succeed. If the
     * modification operation fails, the method throws IOException.
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
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.INSTANCE, txtSuffixFilter);
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
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory, or
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time didn't succeed
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
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)}. If that fails, the method throws IOException.
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
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.INSTANCE, txtSuffixFilter);
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
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory,
     *     the source and the destination directory are the same, or the destination is not writable
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @since 1.4
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter filter, final boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, filter, preserveFileDate, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)}. If that fails, the method throws IOException.
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
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.INSTANCE, txtSuffixFilter);
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
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory, or
     *     the source and the destination directory are the same
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time didn't succeed
     * @since 2.8.0
     */
    public static void copyDirectory(final File srcDir, final File destDir, final FileFilter fileFilter, final boolean preserveFileDate,
        final CopyOption... copyOptions) throws IOException {
        Objects.requireNonNull(destDir, "destination");
        requireDirectoryExists(srcDir, "srcDir");
        requireCanonicalPathsNotEquals(srcDir, destDir);

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        final String srcDirCanonicalPath = srcDir.getCanonicalPath();
        final String destDirCanonicalPath = destDir.getCanonicalPath();
        if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
            final File[] srcFiles = listFiles(srcDir, fileFilter);
            if (srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    exclusionList.add(new File(destDir, srcFile.getName()).getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, fileFilter, exclusionList, preserveFileDate, copyOptions);
    }

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source directory and all its contents to a directory of the same name in the specified
     * destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory does exist, then this
     * method merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)} and if that fails, the method throws IOException.
     * </p>
     *
     * @param sourceDir an existing directory to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs, the destination is not writable, or setting the last-modified time didn't succeed
     * @since 1.2
     */
    public static void copyDirectoryToDirectory(final File sourceDir, final File destinationDir) throws IOException {
        Objects.requireNonNull(sourceDir, "sourceDir");
        requireDirectoryIfExists(destinationDir, "destinationDir");
        copyDirectory(sourceDir, new File(destinationDir, sourceDir.getName()), true);
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * overwrites it. A symbolic link is resolved before copying so the new file is not a link.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last modified date/times using
     * {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is not guaranteed that the
     * operation will succeed. If the modification operation fails, it falls back to
     * {@link File#setLastModified(long)}, and if that fails, the method throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @see #copyFileToDirectory(File, File)
     * @see #copyFile(File, File, boolean)
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies an existing file to a new file location.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, then this method
     * overwrites it. A symbolic link is resolved before copying so the new file is not a link.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails, it falls back to
     * {@link File#setLastModified(long)}, and if that fails, the method throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     * @see #copyFile(File, File, boolean, CopyOption...)
     */
    public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate) throws IOException {
        copyFile(srcFile, destFile, preserveFileDate, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies the contents of a file to a new location.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, you can overwrite
     * it with {@link StandardCopyOption#REPLACE_EXISTING}.
     * </p>
     *
     * <p>
     * By default, a symbolic link is resolved before copying so the new file is not a link.
     * To copy symbolic links as links, you can pass {@code LinkOption.NO_FOLLOW_LINKS} as the last argument.
     * </p>
     *
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails, it falls back to
     * {@link File#setLastModified(long)}, and if that fails, the method throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IllegalArgumentException if {@code srcFile} or {@code destFile} is not a file
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @throws IOException if an I/O error occurs, setting the last-modified time didn't succeed,
     *     or the destination is not writable
     * @see #copyFileToDirectory(File, File, boolean)
     * @since 2.8.0
     */
    public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate, final CopyOption... copyOptions) throws IOException {
        Objects.requireNonNull(destFile, "destination");
        checkFileExists(srcFile, "srcFile");
        requireCanonicalPathsNotEquals(srcFile, destFile);
        createParentDirectories(destFile);
        if (destFile.exists()) {
            checkFileExists(destFile, "destFile");
        }

        final Path srcPath = srcFile.toPath();

        Files.copy(srcPath, destFile.toPath(), copyOptions);

        // On Windows, the last modified time is copied by default.
        if (preserveFileDate && !Files.isSymbolicLink(srcPath) && !setTimes(srcFile, destFile)) {
            throw new IOException("Cannot set the file time.");
        }
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory
     * holding the destination file is created if it does not exist. If the destination file exists, you can overwrite
     * it if you use {@link StandardCopyOption#REPLACE_EXISTING}.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destFile the new file, must not be {@code null}.
     * @param copyOptions options specifying how the copy should be done, for example {@link StandardCopyOption}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IllegalArgumentException if source is not a file.
     * @throws IOException if an I/O error occurs.
     * @see StandardCopyOption
     * @since 2.9.0
     */
    public static void copyFile(final File srcFile, final File destFile, final CopyOption... copyOptions) throws IOException {
        copyFile(srcFile, destFile, true, copyOptions);
    }

    /**
     * Copies bytes from a {@link File} to an {@link OutputStream}.
     * <p>
     * This method buffers the input internally, so there is no need to use a {@link BufferedInputStream}.
     * </p>
     *
     * @param input  the {@link File} to read.
     * @param output the {@link OutputStream} to write.
     * @return the number of bytes copied
     * @throws NullPointerException if the File is {@code null}.
     * @throws NullPointerException if the OutputStream is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.1
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (InputStream fis = Files.newInputStream(input.toPath())) {
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
     * {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is not guaranteed that the
     * operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)} and if that fails, the method throws IOException.
     * </p>
     *
     * @param srcFile an existing file to copy, must not be {@code null}.
     * @param destDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
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
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)} and if that fails, the method throws IOException.
     * </p>
     *
     * @param sourceFile an existing file to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @param preserveFileDate true if the file date of the copy should be the same as the original.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @throws IOException if the output file length is not the same as the input file length after the copy completes.
     * @see #copyFile(File, File, CopyOption...)
     * @since 1.3
     */
    public static void copyFileToDirectory(final File sourceFile, final File destinationDir, final boolean preserveFileDate) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        requireDirectoryIfExists(destinationDir, "destinationDir");
        copyFile(sourceFile, new File(destinationDir, sourceFile.getName()), preserveFileDate);
    }

    /**
     * Copies bytes from an {@link InputStream} {@code source} to a file
     * {@code destination}. The directories up to {@code destination}
     * will be created if they don't already exist. {@code destination}
     * will be overwritten if it already exists.
     * <p>
     * <em>The {@code source} stream is closed.</em>
     * </p>
     * <p>
     * See {@link #copyToFile(InputStream, File)} for a method that does not close the input stream.
     * </p>
     *
     * @param source      the {@link InputStream} to copy bytes from, must not be {@code null}, will be closed
     * @param destination the non-directory {@link File} to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream inputStream = source) {
            copyToFile(inputStream, destination);
        }
    }

    /**
     * Copies a file or directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source file or directory, along with all its contents, to a directory of the same name in the
     * specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist. If the destination directory does exist, then this method
     * merges the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting {@code preserveFileDate} to {@code true} tries to preserve the file's last
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)} and if that fails, the method throws IOException.
     * </p>
     *
     * @param sourceFile an existing file or directory to copy, must not be {@code null}.
     * @param destinationDir the directory to place the copy in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
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
     * modified date/times using {@link BasicFileAttributeView#setTimes(FileTime, FileTime, FileTime)}. However, it is
     * not guaranteed that the operation will succeed. If the modification operation fails it falls back to
     * {@link File#setLastModified(long)} and if that fails, the method throws IOException.
     * </p>
     *
     * @param sourceIterable  existing files to copy, must not be {@code null}.
     * @param destinationDir  the directory to place the copies in, must not be {@code null}.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
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
     * Copies bytes from an {@link InputStream} source to a {@link File} destination. The directories
     * up to {@code destination} will be created if they don't already exist. {@code destination} will be
     * overwritten if it already exists. The {@code source} stream is left open, e.g. for use with
     * {@link java.util.zip.ZipInputStream ZipInputStream}. See {@link #copyInputStreamToFile(InputStream, File)} for a
     * method that closes the input stream.
     *
     * @param inputStream the {@link InputStream} to copy bytes from, must not be {@code null}
     * @param file the non-directory {@link File} to write bytes to (possibly overwriting), must not be
     *        {@code null}
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws NullPointerException if the File is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory.
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
     * @throws IOException if an IO error occurs during copying.
     * @since 2.5
     */
    public static void copyToFile(final InputStream inputStream, final File file) throws IOException {
        try (OutputStream out = newOutputStream(file, false)) {
            IOUtils.copy(inputStream, out);
        }
    }

    /**
     * Copies bytes from the URL {@code source} to a file
     * {@code destination}. The directories up to {@code destination}
     * will be created if they don't already exist. {@code destination}
     * will be overwritten if it already exists.
     * <p>
     * Warning: this method does not set a connection or read timeout and thus
     * might block forever. Use {@link #copyURLToFile(URL, File, int, int)}
     * with reasonable timeouts to prevent this.
     * </p>
     *
     * @param source      the {@link URL} to copy bytes from, must not be {@code null}
     * @param destination the non-directory {@link File} to write bytes to
     *                    (possibly overwriting), must not be {@code null}
     * @throws IOException if {@code source} URL cannot be opened
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyURLToFile(final URL source, final File destination) throws IOException {
        final Path path = destination.toPath();
        PathUtils.createParentDirectories(path);
        PathUtils.copy(source::openStream, path, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies bytes from the URL {@code source} to a file {@code destination}. The directories up to
     * {@code destination} will be created if they don't already exist. {@code destination} will be
     * overwritten if it already exists.
     *
     * @param source the {@link URL} to copy bytes from, must not be {@code null}
     * @param destination the non-directory {@link File} to write bytes to (possibly overwriting), must not be
     *        {@code null}
     * @param connectionTimeoutMillis the number of milliseconds until this method will time out if no connection could
     *        be established to the {@code source}
     * @param readTimeoutMillis the number of milliseconds until this method will time out if no data could be read from
     *        the {@code source}
     * @throws IOException if {@code source} URL cannot be opened
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     * @since 2.0
     */
    public static void copyURLToFile(final URL source, final File destination, final int connectionTimeoutMillis, final int readTimeoutMillis)
        throws IOException {
        try (CloseableURLConnection urlConnection = CloseableURLConnection.open(source)) {
            urlConnection.setConnectTimeout(connectionTimeoutMillis);
            urlConnection.setReadTimeout(readTimeoutMillis);
            try (InputStream stream = urlConnection.getInputStream()) {
                copyInputStreamToFile(stream, destination);
            }
        }
    }

    /**
     * Creates all parent directories for a File object, including any necessary but non-existent parent directories. If a parent directory already exists or
     * is null, nothing happens.
     *
     * @param file the File that may need parents, may be null.
     * @return The parent directory, or {@code null} if the given File does have a parent.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @since 2.9.0
     */
    public static File createParentDirectories(final File file) throws IOException {
        return mkdirs(getParentFile(file));
    }

    /**
     * Gets the current directory.
     *
     * @return the current directory.
     * @since 2.12.0
     */
    public static File current() {
        return PathUtils.current().toFile();
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
            final StringBuilder builder = new StringBuilder();
            final ByteBuffer byteBuffer = ByteBuffer.allocate(n);
            for (int i = 0; i < n; ) {
                if (url.charAt(i) == '%') {
                    try {
                        do {
                            final byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
                            byteBuffer.put(octet);
                            i += 3;
                        } while (i < n && url.charAt(i) == '%');
                        continue;
                    } catch (final IndexOutOfBoundsException | NumberFormatException ignored) {
                        // malformed percent-encoded octet, fall through and
                        // append characters literally
                    } finally {
                        if (byteBuffer.position() > 0) {
                            byteBuffer.flip();
                            builder.append(StandardCharsets.UTF_8.decode(byteBuffer).toString());
                            byteBuffer.clear();
                        }
                    }
                }
                builder.append(url.charAt(i++));
            }
            decoded = builder.toString();
        }
        return decoded;
    }

    /**
     * Deletes the given File but throws an IOException if it cannot, unlike {@link File#delete()} which returns a
     * boolean.
     *
     * @param file The file to delete.
     * @return the given file.
     * @throws NullPointerException     if the parameter is {@code null}
     * @throws IOException              if the file cannot be deleted.
     * @see File#delete()
     * @since 2.9.0
     */
    public static File delete(final File file) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        Files.delete(file.toPath());
        return file;
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException              in case deletion is unsuccessful
     * @throws NullPointerException     if the parameter is {@code null}
     * @throws IllegalArgumentException if {@code directory} is not a directory
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
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all subdirectories.
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
     * <li>A {@code directory} must not be null: if null, throw NullPointerException</li>
     * <li>A {@code directory} must be a directory: if not a directory, throw IllegalArgumentException</li>
     * <li>A directory does not contain itself: return false</li>
     * <li>A null child file is not contained in any parent: return false</li>
     * </ul>
     *
     * @param directory the file to consider as the parent.
     * @param child     the file to consider as the child.
     * @return true is the candidate leaf is under by the specified composite. False otherwise.
     * @throws IOException              if an IO error occurs while checking the files.
     * @throws NullPointerException if the parent is {@code null}.
     * @throws IllegalArgumentException if the parent is not a directory.
     * @see FilenameUtils#directoryContains(String, String)
     * @since 2.2
     */
    public static boolean directoryContains(final File directory, final File child) throws IOException {
        requireDirectoryExists(directory, "directory");

        if (child == null || !child.exists()) {
            return false;
        }

        // Canonicalize paths (normalizes relative paths)
        return FilenameUtils.directoryContains(directory.getCanonicalPath(), child.getCanonicalPath());
    }

    /**
     * Internal copy directory method. Creates all destination parent directories,
     * including any necessary but non-existent parent directories.
     *
     * @param srcDir the validated source directory, must not be {@code null}.
     * @param destDir the validated destination directory, must not be {@code null}.
     * @param fileFilter the filter to apply, null means copy all directories and files.
     * @param exclusionList List of files and directories to exclude from the copy, may be null.
     * @param preserveDirDate preserve the directories last modified dates.
     * @param copyOptions options specifying how the copy should be done, see {@link StandardCopyOption}.
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IllegalArgumentException if {@code destDir} is not writable
     * @throws SecurityException See {@link File#mkdirs()}.
     */
    private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter fileFilter, final List<String> exclusionList,
        final boolean preserveDirDate, final CopyOption... copyOptions) throws IOException {
        // recurse dirs, copy files.
        final File[] srcFiles = listFiles(srcDir, fileFilter);
        requireDirectoryIfExists(destDir, "destDir");
        mkdirs(destDir);
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, fileFilter, exclusionList, preserveDirDate, copyOptions);
                } else {
                    copyFile(srcFile, dstFile, preserveDirDate, copyOptions);
                }
            }
        }
        // Do this last, as the above has probably affected directory metadata
        if (preserveDirDate) {
            setTimes(srcDir, destDir);
        }
    }

    /**
     * Deletes a file or directory. For a directory, delete it and all subdirectories.
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
        Objects.requireNonNull(file, PROTOCOL_FILE);

        final Counters.PathCounters deleteCounters;
        try {
            deleteCounters = PathUtils.delete(
                    file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY,
                    StandardDeleteOption.OVERRIDE_READ_ONLY);
        } catch (final IOException ex) {
            throw new IOException("Cannot delete file: " + file, ex);
        }
        if (deleteCounters.getFileCounter().get() < 1 && deleteCounters.getDirectoryCounter().get() < 1) {
            // didn't find a file to delete.
            throw new FileNotFoundException("File does not exist: " + file);
        }
    }

    /**
     * Schedules a file to be deleted when JVM exits.
     * If file is directory delete it and all subdirectories.
     *
     * @param file file or directory to delete, must not be {@code null}.
     * @throws NullPointerException if the file is {@code null}.
     * @throws IOException          in case deletion is unsuccessful.
     */
    public static void forceDeleteOnExit(final File file) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * Creates all directories for a File object, including any necessary but non-existent parent directories. If the {@code directory} already exists or is
     * null, nothing happens.
     * <p>
     * Calls {@link File#mkdirs()} and throws an {@link IOException} on failure.
     * </p>
     *
     * @param directory the receiver for {@code mkdirs()}. If the {@code directory} already exists or is null, nothing happens.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws IOException       if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see File#mkdirs()
     */
    public static void forceMkdir(final File directory) throws IOException {
        mkdirs(directory);
    }

    /**
     * Creates all directories for a File object, including any necessary but non-existent parent directories. If the parent directory already exists or is
     * null, nothing happens.
     * <p>
     * Calls {@link File#mkdirs()} for the parent of {@code file}.
     * </p>
     *
     * @param file file with parents to create, must not be {@code null}.
     * @throws NullPointerException if the file is {@code null}.
     * @throws IOException          if the directory was not created along with all its parent directories.
     * @throws SecurityException    See {@link File#mkdirs()}.
     * @see File#mkdirs()
     * @since 2.5
     */
    public static void forceMkdirParent(final File file) throws IOException {
        forceMkdir(getParentFile(Objects.requireNonNull(file, PROTOCOL_FILE)));
    }

    /**
     * Constructs a file from the set of name elements.
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
     * Constructs a file from the set of name elements.
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
     * Gets the parent of the given file. The given file may be null. Note that a file's parent may be null as well.
     *
     * @param file The file to query, may be null.
     * @return The parent file or {@code null}. Note that a file's parent may be null as well.
     */
    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory as a File
     * @since 2.0
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Returns the path to the system temporary directory.
     *
     * WARNING: this method relies on the Java system property 'java.io.tmpdir'
     * which may or may not have a trailing file separator.
     * This can affect code that uses String processing to manipulate pathnames rather
     * than the standard libary methods in classes such as {@link File}
     *
     * @return the path to the system temporary directory as a String
     * @since 2.0
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     * @since 2.0
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     * @since 2.0
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Tests whether the specified {@link File} is a directory or not. Implemented as a
     * null-safe delegate to {@link Files#isDirectory(Path path, LinkOption... options)}.
     *
     * @param   file the path to the file.
     * @param   options options indicating how symbolic links are handled
     * @return  {@code true} if the file is a directory; {@code false} if
     *          the path is null, the file does not exist, is not a directory, or it cannot
     *          be determined if the file is a directory or not.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the
     *                               {@link SecurityManager#checkRead(String) checkRead} method is invoked to check read
     *                               access to the directory.
     * @since 2.9.0
     */
    public static boolean isDirectory(final File file, final LinkOption... options) {
        return file != null && Files.isDirectory(file.toPath(), options);
    }

    /**
     * Tests whether the directory is empty.
     *
     * @param directory the directory to query.
     * @return whether the directory is empty.
     * @throws IOException if an I/O error occurs.
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not a directory
     *                               <em>(optional specific exception)</em>.
     * @since 2.9.0
     */
    public static boolean isEmptyDirectory(final File directory) throws IOException {
        return PathUtils.isEmptyDirectory(directory.toPath());
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoLocalDate}
     * at the end of day.
     *
     * <p>Note: The input date is assumed to be in the system default time-zone with the time
     * part set to the current time. To use a non-default time-zone use the method
     * {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDate.atTime(LocalTime.now(zoneId)), zoneId)} where
     * {@code zoneId} is a valid {@link ZoneId}.
     *
     * @param file            the {@link File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @return true if the {@link File} exists and has been modified after the given
     * {@link ChronoLocalDate} at the current time.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file or local date is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileNewer(file, chronoLocalDate, LocalTime.MAX);
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoLocalDate}
     * at the specified time.
     *
     * <p>Note: The input date and time are assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDate.atTime(localTime), zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file            the {@link File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @param localTime       the time reference.
     * @return true if the {@link File} exists and has been modified after the given
     * {@link ChronoLocalDate} at the given time.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file, local date or zone ID is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileNewer(file, chronoLocalDate.atTime(localTime));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoLocalDate} at the specified
     * {@link OffsetTime}.
     *
     * @param file the {@link File} of which the modification date must be compared
     * @param chronoLocalDate the date reference
     * @param offsetTime the time reference
     * @return true if the {@link File} exists and has been modified after the given {@link ChronoLocalDate} at the given
     *         {@link OffsetTime}.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file, local date or zone ID is {@code null}
     * @since 2.12.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDate chronoLocalDate, final OffsetTime offsetTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(offsetTime, "offsetTime");
        return isFileNewer(file, chronoLocalDate.atTime(offsetTime.toLocalTime()));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoLocalDateTime}
     * at the system-default time zone.
     *
     * <p>Note: The input date and time is assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileNewer(File, ChronoLocalDateTime, ZoneId)
     * isFileNewer(file, chronoLocalDateTime, zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file                the {@link File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @return true if the {@link File} exists and has been modified after the given
     * {@link ChronoLocalDateTime} at the system-default time zone.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file or local date time is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime) {
        return isFileNewer(file, chronoLocalDateTime, ZoneId.systemDefault());
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoLocalDateTime}
     * at the specified {@link ZoneId}.
     *
     * @param file                the {@link File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @param zoneId              the time zone.
     * @return true if the {@link File} exists and has been modified after the given
     * {@link ChronoLocalDateTime} at the given {@link ZoneId}.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file, local date time or zone ID is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileNewer(file, chronoLocalDateTime.atZone(zoneId));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link ChronoZonedDateTime}.
     *
     * @param file                the {@link File} of which the modification date must be compared.
     * @param chronoZonedDateTime the date reference.
     * @return true if the {@link File} exists and has been modified after the given
     * {@link ChronoZonedDateTime}.
     * @throws NullPointerException if the file or zoned date time is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final ChronoZonedDateTime<?> chronoZonedDateTime) {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return Uncheck.get(() -> PathUtils.isNewer(file.toPath(), chronoZonedDateTime));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link Date}.
     *
     * @param file the {@link File} of which the modification date must be compared.
     * @param date the date reference.
     * @return true if the {@link File} exists and has been modified
     * after the given {@link Date}.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file or date is {@code null}.
     */
    public static boolean isFileNewer(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileNewer(file, date.getTime());
    }

    /**
     * Tests if the specified {@link File} is newer than the reference {@link File}.
     *
     * @param file      the {@link File} of which the modification date must be compared.
     * @param reference the {@link File} of which the modification date is used.
     * @return true if the {@link File} exists and has been modified more
     * recently than the reference {@link File}.
     * @throws NullPointerException if the file or reference file is {@code null}.
     * @throws UncheckedIOException if the reference file doesn't exist.
     */
    public static boolean isFileNewer(final File file, final File reference) {
        return Uncheck.get(() -> PathUtils.isNewer(file.toPath(), reference.toPath()));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link FileTime}.
     *
     * @param file the {@link File} of which the modification date must be compared.
     * @param fileTime the file time reference.
     * @return true if the {@link File} exists and has been modified after the given {@link FileTime}.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the file or local date is {@code null}.
     * @since 2.12.0
     */
    public static boolean isFileNewer(final File file, final FileTime fileTime) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return PathUtils.isNewer(file.toPath(), fileTime);
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link Instant}.
     *
     * @param file the {@link File} of which the modification date must be compared.
     * @param instant the date reference.
     * @return true if the {@link File} exists and has been modified after the given {@link Instant}.
     * @throws NullPointerException if the file or instant is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.8.0
     */
    public static boolean isFileNewer(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return Uncheck.get(() -> PathUtils.isNewer(file.toPath(), instant));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified time reference.
     *
     * @param file       the {@link File} of which the modification date must be compared.
     * @param timeMillis the time reference measured in milliseconds since the
     *                   epoch (00:00:00 GMT, January 1, 1970).
     * @return true if the {@link File} exists and has been modified after the given time reference.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file is {@code null}.
     */
    public static boolean isFileNewer(final File file, final long timeMillis) {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return Uncheck.get(() -> PathUtils.isNewer(file.toPath(), timeMillis));
    }

    /**
     * Tests if the specified {@link File} is newer than the specified {@link OffsetDateTime}.
     *
     * @param file the {@link File} of which the modification date must be compared
     * @param offsetDateTime the date reference
     * @return true if the {@link File} exists and has been modified before the given {@link OffsetDateTime}.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file or zoned date time is {@code null}
     * @since 2.12.0
     */
    public static boolean isFileNewer(final File file, final OffsetDateTime offsetDateTime) {
        Objects.requireNonNull(offsetDateTime, "offsetDateTime");
        return isFileNewer(file, offsetDateTime.toInstant());
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoLocalDate}
     * at the end of day.
     *
     * <p>Note: The input date is assumed to be in the system default time-zone with the time
     * part set to the current time. To use a non-default time-zone use the method
     * {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDate.atTime(LocalTime.now(zoneId)), zoneId)} where
     * {@code zoneId} is a valid {@link ZoneId}.
     *
     * @param file            the {@link File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @return true if the {@link File} exists and has been modified before the given
     * {@link ChronoLocalDate} at the current time.
     * @throws NullPointerException if the file or local date is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @see ZoneId#systemDefault()
     * @see LocalTime#now()
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate) {
        return isFileOlder(file, chronoLocalDate, LocalTime.MAX);
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoLocalDate}
     * at the specified {@link LocalTime}.
     *
     * <p>Note: The input date and time are assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDate.atTime(localTime), zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file            the {@link File} of which the modification date must be compared.
     * @param chronoLocalDate the date reference.
     * @param localTime       the time reference.
     * @return true if the {@link File} exists and has been modified before the
     * given {@link ChronoLocalDate} at the specified time.
     * @throws UncheckedIOException if an I/O error occurs
     * @throws NullPointerException if the file, local date or local time is {@code null}.
     * @see ZoneId#systemDefault()
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate, final LocalTime localTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(localTime, "localTime");
        return isFileOlder(file, chronoLocalDate.atTime(localTime));
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoLocalDate} at the specified
     * {@link OffsetTime}.
     *
     * @param file the {@link File} of which the modification date must be compared
     * @param chronoLocalDate the date reference
     * @param offsetTime the time reference
     * @return true if the {@link File} exists and has been modified after the given {@link ChronoLocalDate} at the given
     *         {@link OffsetTime}.
     * @throws NullPointerException if the file, local date or zone ID is {@code null}
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.12.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDate chronoLocalDate, final OffsetTime offsetTime) {
        Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
        Objects.requireNonNull(offsetTime, "offsetTime");
        return isFileOlder(file, chronoLocalDate.atTime(offsetTime.toLocalTime()));
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoLocalDateTime}
     * at the system-default time zone.
     *
     * <p>Note: The input date and time is assumed to be in the system default time-zone. To use a
     * non-default time-zone use the method {@link #isFileOlder(File, ChronoLocalDateTime, ZoneId)
     * isFileOlder(file, chronoLocalDateTime, zoneId)} where {@code zoneId} is a valid
     * {@link ZoneId}.
     *
     * @param file                the {@link File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @return true if the {@link File} exists and has been modified before the given
     * {@link ChronoLocalDateTime} at the system-default time zone.
     * @throws NullPointerException if the file or local date time is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @see ZoneId#systemDefault()
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime) {
        return isFileOlder(file, chronoLocalDateTime, ZoneId.systemDefault());
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoLocalDateTime}
     * at the specified {@link ZoneId}.
     *
     * @param file          the {@link File} of which the modification date must be compared.
     * @param chronoLocalDateTime the date reference.
     * @param zoneId        the time zone.
     * @return true if the {@link File} exists and has been modified before the given
     * {@link ChronoLocalDateTime} at the given {@link ZoneId}.
     * @throws NullPointerException if the file, local date time or zone ID is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoLocalDateTime<?> chronoLocalDateTime, final ZoneId zoneId) {
        Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
        Objects.requireNonNull(zoneId, "zoneId");
        return isFileOlder(file, chronoLocalDateTime.atZone(zoneId));
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link ChronoZonedDateTime}.
     *
     * @param file                the {@link File} of which the modification date must be compared.
     * @param chronoZonedDateTime the date reference.
     * @return true if the {@link File} exists and has been modified before the given
     * {@link ChronoZonedDateTime}.
     * @throws NullPointerException if the file or zoned date time is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final ChronoZonedDateTime<?> chronoZonedDateTime) {
        Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
        return isFileOlder(file, chronoZonedDateTime.toInstant());
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link Date}.
     *
     * @param file the {@link File} of which the modification date must be compared.
     * @param date the date reference.
     * @return true if the {@link File} exists and has been modified before the given {@link Date}.
     * @throws NullPointerException if the file or date is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     */
    public static boolean isFileOlder(final File file, final Date date) {
        Objects.requireNonNull(date, "date");
        return isFileOlder(file, date.getTime());
    }

    /**
     * Tests if the specified {@link File} is older than the reference {@link File}.
     *
     * @param file      the {@link File} of which the modification date must be compared.
     * @param reference the {@link File} of which the modification date is used.
     * @return true if the {@link File} exists and has been modified before the reference {@link File}.
     * @throws NullPointerException if the file or reference file is {@code null}.
     * @throws FileNotFoundException if the reference file doesn't exist.
     * @throws UncheckedIOException if an I/O error occurs
     */
    public static boolean isFileOlder(final File file, final File reference) throws FileNotFoundException {
        return Uncheck.get(() -> PathUtils.isOlder(file.toPath(), reference.toPath()));
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link FileTime}.
     *
     * @param file the {@link File} of which the modification date must be compared.
     * @param fileTime the file time reference.
     * @return true if the {@link File} exists and has been modified before the given {@link FileTime}.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the file or local date is {@code null}.
     * @since 2.12.0
     */
    public static boolean isFileOlder(final File file, final FileTime fileTime) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return PathUtils.isOlder(file.toPath(), fileTime);
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link Instant}.
     *
     * @param file    the {@link File} of which the modification date must be compared.
     * @param instant the date reference.
     * @return true if the {@link File} exists and has been modified before the given {@link Instant}.
     * @throws NullPointerException if the file or instant is {@code null}.
     * @since 2.8.0
     */
    public static boolean isFileOlder(final File file, final Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return Uncheck.get(() -> PathUtils.isOlder(file.toPath(), instant));
    }

    /**
     * Tests if the specified {@link File} is older than the specified time reference.
     *
     * @param file       the {@link File} of which the modification date must be compared.
     * @param timeMillis the time reference measured in milliseconds since the
     *                   epoch (00:00:00 GMT, January 1, 1970).
     * @return true if the {@link File} exists and has been modified before the given time reference.
     * @throws NullPointerException if the file is {@code null}.
     * @throws UncheckedIOException if an I/O error occurs
     */
    public static boolean isFileOlder(final File file, final long timeMillis) {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return Uncheck.get(() -> PathUtils.isOlder(file.toPath(), timeMillis));
    }

    /**
     * Tests if the specified {@link File} is older than the specified {@link OffsetDateTime}.
     *
     * @param file the {@link File} of which the modification date must be compared
     * @param offsetDateTime the date reference
     * @return true if the {@link File} exists and has been modified before the given {@link OffsetDateTime}.
     * @throws NullPointerException if the file or zoned date time is {@code null}
     * @since 2.12.0
     */
    public static boolean isFileOlder(final File file, final OffsetDateTime offsetDateTime) {
        Objects.requireNonNull(offsetDateTime, "offsetDateTime");
        return isFileOlder(file, offsetDateTime.toInstant());
    }

    /**
     * Tests whether the given URL is a file URL.
     *
     * @param url The URL to test.
     * @return Whether the given URL is a file URL.
     */
    private static boolean isFileProtocol(final URL url) {
        return PROTOCOL_FILE.equalsIgnoreCase(url.getProtocol());
    }

    /**
     * Tests whether the specified {@link File} is a regular file or not. Implemented as a
     * null-safe delegate to {@link Files#isRegularFile(Path path, LinkOption... options)}.
     *
     * @param   file the path to the file.
     * @param   options options indicating how symbolic links are handled
     * @return  {@code true} if the file is a regular file; {@code false} if
     *          the path is null, the file does not exist, is not a regular file, or it cannot
     *          be determined if the file is a regular file or not.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the
     *                               {@link SecurityManager#checkRead(String) checkRead} method is invoked to check read
     *                               access to the directory.
     * @since 2.9.0
     */
    public static boolean isRegularFile(final File file, final LinkOption... options) {
        return file != null && Files.isRegularFile(file.toPath(), options);
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
        return file != null && Files.isSymbolicLink(file.toPath());
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
     * @return an iterator of {@link File} for the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 1.2
     */
    public static Iterator<File> iterateFiles(final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        return listFiles(directory, fileFilter, dirFilter).iterator();
    }

    /**
     * Iterates over the files in a given directory (and optionally
     * its subdirectories) which match an array of extensions.
     * <p>
     * The resulting iterator MUST be consumed in its entirety in order to close its underlying stream.
     * </p>
     *
     * @param directory  the directory to search in
     * @param extensions an array of extensions, for example, {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @param recursive  if true all subdirectories are searched as well
     * @return an iterator of {@link File} with the matching files
     * @since 1.2
     */
    public static Iterator<File> iterateFiles(final File directory, final String[] extensions, final boolean recursive) {
        return StreamIterator.iterator(Uncheck.get(() -> streamFiles(directory, recursive, extensions)));
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
     * @return an iterator of {@link File} for the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 2.2
     */
    public static Iterator<File> iterateFilesAndDirs(final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
    }

    /**
     * Returns the last modification time in milliseconds via
     * {@link java.nio.file.Files#getLastModifiedTime(Path, LinkOption...)}.
     * <p>
     * For the best precision, use {@link #lastModifiedFileTime(File)}.
     * </p>
     * <p>
     * Use this method to avoid issues with {@link File#lastModified()} like
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8177809">JDK-8177809</a> where {@link File#lastModified()} is
     * losing milliseconds (always ends in 000). This bug exists in OpenJDK 8 and 9, and is fixed in 10.
     * </p>
     *
     * @param file The File to query.
     * @return See {@link java.nio.file.attribute.FileTime#toMillis()}.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long lastModified(final File file) throws IOException {
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        // File.lastModified() is losing milliseconds (always ends in 000)
        // This bug is in OpenJDK 8 and 9, and fixed in 10.
        return lastModifiedFileTime(file).toMillis();
    }

    /**
     * Returns the last modification {@link FileTime} via
     * {@link java.nio.file.Files#getLastModifiedTime(Path, LinkOption...)}.
     * <p>
     * Use this method to avoid issues with {@link File#lastModified()} like
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8177809">JDK-8177809</a> where {@link File#lastModified()} is
     * losing milliseconds (always ends in 000). This bug exists in OpenJDK 8 and 9, and is fixed in 10.
     * </p>
     *
     * @param file The File to query.
     * @return See {@link java.nio.file.Files#getLastModifiedTime(Path, LinkOption...)}.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    public static FileTime lastModifiedFileTime(final File file) throws IOException {
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        // File.lastModified() is losing milliseconds (always ends in 000)
        // This bug is in OpenJDK 8 and 9, and fixed in 10.
        return Files.getLastModifiedTime(Objects.requireNonNull(file, PROTOCOL_FILE).toPath());
    }

    /**
     * Returns the last modification time in milliseconds via
     * {@link java.nio.file.Files#getLastModifiedTime(Path, LinkOption...)}.
     * <p>
     * For the best precision, use {@link #lastModifiedFileTime(File)}.
     * </p>
     * <p>
     * Use this method to avoid issues with {@link File#lastModified()} like
     * <a href="https://bugs.openjdk.java.net/browse/JDK-8177809">JDK-8177809</a> where {@link File#lastModified()} is
     * losing milliseconds (always ends in 000). This bug exists in OpenJDK 8 and 9, and is fixed in 10.
     * </p>
     *
     * @param file The File to query.
     * @return See {@link java.nio.file.attribute.FileTime#toMillis()}.
     * @throws UncheckedIOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static long lastModifiedUnchecked(final File file) {
        // https://bugs.openjdk.java.net/browse/JDK-8177809
        // File.lastModified() is losing milliseconds (always ends in 000)
        // This bug is in OpenJDK 8 and 9, and fixed in 10.
        return Uncheck.apply(FileUtils::lastModified, file);
    }

    /**
     * Returns an Iterator for the lines in a {@link File} using the default encoding for the VM.
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
     * Returns an Iterator for the lines in a {@link File}.
     * <p>
     * This method opens an {@link InputStream} for the file.
     * When you have finished with the iterator you should close the stream
     * to free internal resources. This can be done by using a try-with-resources block or calling the
     * {@link LineIterator#close()} method.
     * </p>
     * <p>
     * The recommended usage pattern is:
     * </p>
     * <pre>
     * LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name());
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
     * @return a LineIterator for lines in the file, never {@code null}; MUST be closed by the caller.
     * @throws NullPointerException if file is {@code null}.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some
     *         other reason cannot be opened for reading.
     * @throws IOException if an I/O error occurs.
     * @since 1.2
     */
    @SuppressWarnings("resource") // Caller closes the result LineIterator.
    public static LineIterator lineIterator(final File file, final String charsetName) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(file.toPath());
            return IOUtils.lineIterator(inputStream, charsetName);
        } catch (final IOException | RuntimeException ex) {
            IOUtils.closeQuietly(inputStream, ex::addSuppressed);
            throw ex;
        }
    }

    private static AccumulatorPathVisitor listAccumulate(final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter,
            final FileVisitOption... options) throws IOException {
        final boolean isDirFilterSet = dirFilter != null;
        final FileEqualsFileFilter rootDirFilter = new FileEqualsFileFilter(directory);
        final PathFilter dirPathFilter = isDirFilterSet ? rootDirFilter.or(dirFilter) : rootDirFilter;
        final AccumulatorPathVisitor visitor = new AccumulatorPathVisitor(Counters.noopPathCounters(), fileFilter, dirPathFilter,
                (p, e) -> FileVisitResult.CONTINUE);
        final Set<FileVisitOption> optionSet = new HashSet<>();
        if (options != null) {
            Collections.addAll(optionSet, options);
        }
        Files.walkFileTree(directory.toPath(), optionSet, toMaxDepth(isDirFilterSet), visitor);
        return visitor;
    }

    /**
     * Lists files in a directory, asserting that the supplied directory exists and is a directory.
     *
     * @param directory The directory to list
     * @param fileFilter Optional file filter, may be null.
     * @return The files in the directory, never {@code null}.
     * @throws NullPointerException if directory is {@code null}.
     * @throws IllegalArgumentException if {@link directory} exists but is not a directory
     * @throws IOException if an I/O error occurs.
     */
    private static File[] listFiles(final File directory, final FileFilter fileFilter) throws IOException {
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
     * "temp" you pass in {@code FileFilterUtils.NameFileFilter("temp")}
     * </p>
     * <p>
     * Another common usage of this method is find files in a directory
     * tree but ignoring the directories generated CVS. You can simply pass
     * in {@code FileFilterUtils.makeCVSAware(null)}.
     * </p>
     *
     * @param directory  the directory to search in
     * @param fileFilter filter to apply when finding files. Must not be {@code null},
     *                   use {@link TrueFileFilter#INSTANCE} to match all files in selected directories.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     *                   If this parameter is {@code null}, subdirectories will not be included in the
     *                   search. Use {@link TrueFileFilter#INSTANCE} to match all directories.
     * @return a collection of {@link File} with the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     */
    public static Collection<File> listFiles(final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        final AccumulatorPathVisitor visitor = Uncheck
            .apply(d -> listAccumulate(d, FileFileFilter.INSTANCE.and(fileFilter), dirFilter, FileVisitOption.FOLLOW_LINKS), directory);
        return toList(visitor.getFileList().stream().map(Path::toFile));
    }

    /**
     * Lists files within a given directory (and optionally its subdirectories)
     * which match an array of extensions.
     *
     * @param directory  the directory to search in
     * @param extensions an array of extensions, for example, {"java","xml"}. If this
     *                   parameter is {@code null}, all files are returned.
     * @param recursive  if true all subdirectories are searched as well
     * @return a collection of {@link File} with the matching files
     */
    public static Collection<File> listFiles(final File directory, final String[] extensions, final boolean recursive) {
        try (Stream<File> fileStream = Uncheck.get(() -> streamFiles(directory, recursive, extensions))) {
            return toList(fileStream);
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
     * @return a collection of {@link File} with the matching files
     * @see org.apache.commons.io.FileUtils#listFiles
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 2.2
     */
    public static Collection<File> listFilesAndDirs(final File directory, final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
        final AccumulatorPathVisitor visitor = Uncheck.apply(d -> listAccumulate(d, fileFilter, dirFilter, FileVisitOption.FOLLOW_LINKS),
            directory);
        final List<Path> list = visitor.getFileList();
        list.addAll(visitor.getDirList());
        return toList(list.stream().map(Path::toFile));
    }

    /**
     * Calls {@link File#mkdirs()} and throws an {@link IOException} on failure.
     * <p>
     * Creates all directories for a File object, including any necessary but non-existent parent directories. If the {@code directory} already exists or is
     * null, nothing happens.
     * </p>
     *
     * @param directory the receiver for {@code mkdirs()}. If the {@code directory} already exists or is null, nothing happens.
     * @return the given directory.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws IOException       if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see File#mkdirs()
     */
    private static File mkdirs(final File directory) throws IOException {
        if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Cannot create directory '" + directory + "'.");
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
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if {@code srcDir} exists but is not a directory
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @since 1.4
     */
    public static void moveDirectory(final File srcDir, final File destDir) throws IOException {
        Objects.requireNonNull(destDir, "destination");
        requireDirectoryExists(srcDir, "srcDir");
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
     * <p>
     * If {@code createDestDir} is true, creates all destination parent directories, including any necessary but non-existent parent directories.
     * </p>
     *
     * @param source the directory to be moved.
     * @param destDir the destination file.
     * @param createDestDir If {@code true} create the destination directory, otherwise if {@code false} throw an
     *        IOException.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws IllegalArgumentException if the source or destination is invalid.
     * @throws FileNotFoundException if the source does not exist.
     * @throws IOException if the directory was not created along with all its parent directories, if enabled.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @since 1.4
     */
    public static void moveDirectoryToDirectory(final File source, final File destDir, final boolean createDestDir) throws IOException {
        validateMoveParameters(source, destDir);
        if (!destDir.isDirectory()) {
            if (destDir.exists()) {
                throw new IOException("Destination '" + destDir + "' is not a directory");
            }
            if (!createDestDir) {
                throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + false + "]");
            }
            mkdirs(destDir);
        }
        moveDirectory(source, new File(destDir, source.getName()));
    }

    /**
     * Moves a file preserving attributes.
     * <p>
     * Shorthand for {@code moveFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES)}.
     * </p>
     * <p>
     * When the destination file is on another file system, do a "copy and delete".
     * </p>
     *
     * @param srcFile the file to be moved.
     * @param destFile the destination file.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileExistsException if the destination file exists.
     * @throws FileNotFoundException if the source file does not exist.
     * @throws IllegalArgumentException if {@code srcFile} is a directory
     * @throws IOException if an error occurs.
     * @since 1.4
     */
    public static void moveFile(final File srcFile, final File destFile) throws IOException {
        moveFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * Moves a file.
     * <p>
     * When the destination file is on another file system, do a "copy and delete".
     * </p>
     *
     * @param srcFile the file to be moved.
     * @param destFile the destination file.
     * @param copyOptions Copy options.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileExistsException if the destination file exists.
     * @throws FileNotFoundException if the source file does not exist.
     * @throws IllegalArgumentException if {@code srcFile} is a directory
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @since 2.9.0
     */
    public static void moveFile(final File srcFile, final File destFile, final CopyOption... copyOptions) throws IOException {
        Objects.requireNonNull(destFile, "destination");
        checkFileExists(srcFile, "srcFile");
        requireAbsent(destFile, "destFile");
        final boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            // Don't interfere with file date on move, handled by StandardCopyOption.COPY_ATTRIBUTES
            copyFile(srcFile, destFile, false, copyOptions);
            if (!srcFile.delete()) {
                deleteQuietly(destFile);
                throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
            }
        }
    }

    /**
     * Moves a file into a directory.
     * <p>
     * If {@code createDestDir} is true, creates all destination parent directories, including any necessary but non-existent parent directories.
     * </p>
     *
     * @param srcFile the file to be moved.
     * @param destDir the directory to move the file into
     * @param createDestDir if {@code true} create the destination directory. If {@code false} throw an
     *        IOException if the destination directory does not already exist.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileExistsException if the destination file exists.
     * @throws FileNotFoundException if the source file does not exist.
     * @throws IOException if source or destination is invalid.
     * @throws IOException if the directory was not created along with all its parent directories, if enabled.
     * @throws IOException if an error occurs or setting the last-modified time didn't succeed.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @throws IllegalArgumentException if {@code destDir} exists but is not a directory
     * @since 1.4
     */
    public static void moveFileToDirectory(final File srcFile, final File destDir, final boolean createDestDir) throws IOException {
        validateMoveParameters(srcFile, destDir);
        if (!destDir.exists() && createDestDir) {
            mkdirs(destDir);
        }
        requireDirectoryExists(destDir, "destDir");
        moveFile(srcFile, new File(destDir, srcFile.getName()));
    }

    /**
     * Moves a file or directory into a destination directory.
     * <p>
     * If {@code createDestDir} is true, creates all destination parent directories, including any necessary but non-existent parent directories.
     * </p>
     * <p>
     * When the destination is on another file system, do a "copy and delete".
     * </p>
     *
     * @param src           the file or directory to be moved.
     * @param destDir       the destination directory.
     * @param createDestDir if {@code true} create the destination directory. If {@code false} throw an
     *        IOException if the destination directory does not already exist.
     * @throws NullPointerException  if any of the given {@link File}s are {@code null}.
     * @throws FileExistsException   if the directory or file exists in the destination directory.
     * @throws FileNotFoundException if the source file does not exist.
     * @throws IOException           if source or destination is invalid.
     * @throws IOException           if an error occurs or setting the last-modified time didn't succeed.
     * @since 1.4
     */
    public static void moveToDirectory(final File src, final File destDir, final boolean createDestDir) throws IOException {
        validateMoveParameters(src, destDir);
        if (src.isDirectory()) {
            moveDirectoryToDirectory(src, destDir, createDestDir);
        } else {
            moveFileToDirectory(src, destDir, createDestDir);
        }
    }

    /**
     * Creates a new OutputStream by opening or creating a file, returning an output stream that may be used to write bytes
     * to the file.
     *
     * @param append Whether or not to append.
     * @param file the File.
     * @return a new OutputStream.
     * @throws IOException if an I/O error occurs.
     * @see PathUtils#newOutputStream(Path, boolean)
     * @since 2.12.0
     */
    public static OutputStream newOutputStream(final File file, final boolean append) throws IOException {
        return PathUtils.newOutputStream(Objects.requireNonNull(file, PROTOCOL_FILE).toPath(), append);
    }

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better error messages than simply calling
     * {@code new FileInputStream(file)}.
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
        Objects.requireNonNull(file, PROTOCOL_FILE);
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
     * @throws NullPointerException if the file object is {@code null}.
     * @throws IllegalArgumentException if the file object is a directory
     * @throws IllegalArgumentException if the file is not writable.
     * @throws IOException if the directories could not be created.
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
     * @throws IOException if the directories could not be created, or the file is not writable
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        if (file.exists()) {
            checkIsFile(file, PROTOCOL_FILE);
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
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @since 1.1
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Reads the contents of a file into a String using the default encoding for the VM.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @since 1.3.1
     * @deprecated Use {@link #readFileToString(File, Charset)} instead (and specify the appropriate encoding)
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
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @since 2.3
     */
    public static String readFileToString(final File file, final Charset charsetName) throws IOException {
        return IOUtils.toString(() -> Files.newInputStream(file.toPath()), Charsets.toCharset(charsetName));
    }

    /**
     * Reads the contents of a file into a String. The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the file contents, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @throws java.nio.charset.UnsupportedCharsetException if the named charset is unavailable.
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
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @since 1.3
     * @deprecated Use {@link #readLines(File, Charset)} instead (and specify the appropriate encoding)
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
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @since 2.3
     */
    public static List<String> readLines(final File file, final Charset charset) throws IOException {
        return Files.readAllLines(file.toPath(), charset);
    }

    /**
     * Reads the contents of a file line by line to a List of Strings. The file is always closed.
     *
     * @param file     the file to read, must not be {@code null}
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @return the list of Strings representing each line in the file, never {@code null}
     * @throws NullPointerException if file is {@code null}.
     * @throws IOException if an I/O error occurs, including when the file does not exist, is a directory rather than a
     *         regular file, or for some other reason why the file cannot be opened for reading.
     * @throws java.nio.charset.UnsupportedCharsetException if the named charset is unavailable.
     * @since 1.1
     */
    public static List<String> readLines(final File file, final String charsetName) throws IOException {
        return readLines(file, Charsets.toCharset(charsetName));
    }

    private static void requireAbsent(final File file, final String name) throws FileExistsException {
        if (file.exists()) {
            throw new FileExistsException(String.format("File element in parameter '%s' already exists: '%s'", name, file));
        }
    }

    /**
     * Throws IllegalArgumentException if the given files' canonical representations are equal.
     *
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @throws IOException if an I/O error occurs.
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
     * Requires that the given {@link File} exists and is a directory.
     *
     * @param directory The {@link File} to check.
     * @param name The parameter name to use in the exception message in case of null input or if the file is not a directory.
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws FileNotFoundException if the given {@link File} does not exist
     * @throws IllegalArgumentException if the given {@link File} exists but is not a directory.
     */
    private static void requireDirectoryExists(final File directory, final String name) throws FileNotFoundException {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            if (directory.exists()) {
                throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
            }
            throw new FileNotFoundException("Directory '" + directory + "' does not exist.");
        }
    }

    /**
     * Requires that the given {@link File} is a directory if it exists.
     *
     * @param directory The {@link File} to check.
     * @param name The parameter name to use in the exception message in case of null input.
     * @throws NullPointerException if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if the given {@link File} exists but is not a directory.
     */
    private static void requireDirectoryIfExists(final File directory, final String name) {
        Objects.requireNonNull(directory, name);
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
    }

    /**
     * Sets file lastModifiedTime, lastAccessTime and creationTime to match source file
     *
     * @param sourceFile The source file to query.
     * @param targetFile The target file or directory to set.
     * @return {@code true} if and only if the operation succeeded;
     *          {@code false} otherwise
     * @throws NullPointerException if sourceFile is {@code null}.
     * @throws NullPointerException if targetFile is {@code null}.
     */
    private static boolean setTimes(final File sourceFile, final File targetFile) {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(targetFile, "targetFile");
        try {
            // Set creation, modified, last accessed to match source file
            final BasicFileAttributes srcAttr = Files.readAttributes(sourceFile.toPath(), BasicFileAttributes.class);
            final BasicFileAttributeView destAttrView = Files.getFileAttributeView(targetFile.toPath(), BasicFileAttributeView.class);
            // null guards are not needed; BasicFileAttributes.setTimes(...) is null safe
            destAttrView.setTimes(srcAttr.lastModifiedTime(), srcAttr.lastAccessTime(), srcAttr.creationTime());
            return true;
        } catch (final IOException ignored) {
            // Fallback: Only set modified time to match source file
            return targetFile.setLastModified(sourceFile.lastModified());
        }

        // TODO: (Help!) Determine historically why setLastModified(File, File) needed PathUtils.setLastModifiedTime() if
        //  sourceFile.isFile() was true, but needed setLastModifiedTime(File, long) if sourceFile.isFile() was false
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
     * @throws UncheckedIOException if an IO error occurs.
     * @since 2.0
     */
    public static long sizeOf(final File file) {
        return Uncheck.get(() -> PathUtils.sizeOf(file.toPath()));
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
     * @throws UncheckedIOException if an IO error occurs.
     * @since 2.4
     */
    public static BigInteger sizeOfAsBigInteger(final File file) {
        return Uncheck.get(() -> PathUtils.sizeOfAsBigInteger(file.toPath()));
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
     * @throws IllegalArgumentException if the given {@link File} exists but is not a directory
     * @throws NullPointerException if the directory is {@code null}.
     * @throws UncheckedIOException if an IO error occurs.
     */
    public static long sizeOfDirectory(final File directory) {
        try {
            requireDirectoryExists(directory, "directory");
        } catch (final FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return Uncheck.get(() -> PathUtils.sizeOfDirectory(directory.toPath()));
    }

    /**
     * Counts the size of a directory recursively (sum of the length of all files).
     *
     * @param directory directory to inspect, must not be {@code null}.
     * @return size of directory in bytes, 0 if directory is security restricted.
     * @throws IllegalArgumentException if the given {@link File} exists but is not a directory
     * @throws NullPointerException if the directory is {@code null}.
     * @throws UncheckedIOException if an IO error occurs.
     * @since 2.4
     */
    public static BigInteger sizeOfDirectoryAsBigInteger(final File directory) {
        try {
            requireDirectoryExists(directory, "directory");
        } catch (final FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return Uncheck.get(() -> PathUtils.sizeOfDirectoryAsBigInteger(directory.toPath()));
    }

    /**
     * Streams over the files in a given directory (and optionally its subdirectories) which match an array of extensions.
     * <p>
     * The returned {@link Stream} may wrap one or more {@link DirectoryStream}s. When you require timely disposal of file system resources, use a
     * {@code try}-with-resources block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed. Calling a
     * closed stream causes a {@link IllegalStateException}.
     * </p>
     *
     * @param directory  the directory to search in
     * @param recursive  if true all subdirectories are searched as well
     * @param extensions an array of extensions, for example, {"java","xml"}. If this parameter is {@code null}, all files are returned.
     * @return a Stream of {@link File} for matching files.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     * @since 2.9.0
     */
    public static Stream<File> streamFiles(final File directory, final boolean recursive, final String... extensions) throws IOException {
        // @formatter:off
        final IOFileFilter filter = extensions == null
            ? FileFileFilter.INSTANCE
            : FileFileFilter.INSTANCE.and(new SuffixFileFilter(toSuffixes(extensions)));
        // @formatter:on
        return PathUtils.walk(directory.toPath(), filter, toMaxDepth(recursive), false, FileVisitOption.FOLLOW_LINKS).map(Path::toFile);
    }

    /**
     * Converts from a {@link URL} to a {@link File}.
     * <p>
     * Syntax such as {@code file:///my%20docs/file.txt} will be
     * correctly decoded to {@code /my docs/file.txt}.
     * UTF-8 is used to decode percent-encoded octets to characters.
     * Additionally, malformed percent-encoded octets are handled leniently by
     * passing them through literally.
     * </p>
     *
     * @param url the file URL to convert, {@code null} returns {@code null}
     * @return the equivalent {@link File} object, or {@code null}
     * if the URL's protocol is not {@code file}
     */
    public static File toFile(final URL url) {
        if (url == null || !isFileProtocol(url)) {
            return null;
        }
        final String fileName = url.getFile().replace('/', File.separatorChar);
        return new File(decodeUrl(fileName));
    }

    /**
     * Converts each of an array of {@link URL} to a {@link File}.
     * <p>
     * Returns an array of the same size as the input.
     * If the input is {@code null}, an empty array is returned.
     * If the input contains {@code null}, the output array contains {@code null} at the same
     * index.
     * </p>
     * <p>
     * This method will decode the URL.
     * Syntax such as {@code file:///my%20docs/file.txt} will be
     * correctly decoded to {@code /my docs/file.txt}.
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
        if (IOUtils.length(urls) == 0) {
            return EMPTY_FILE_ARRAY;
        }
        final File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            final URL url = urls[i];
            if (url != null) {
                if (!isFileProtocol(url)) {
                    throw new IllegalArgumentException("Can only convert file URL to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    /**
     * Consumes all of the given stream.
     * <p>
     * When called from a FileTreeWalker, the walker <em>closes</em> the stream because {@link FileTreeWalker#next()} calls {@code top.stream().close()}.
     * </p>
     *
     * @param stream The stream to consume.
     * @return a new List.
     */
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
     * @throws NullPointerException if the parameter is null
     */
    private static String[] toSuffixes(final String... extensions) {
        return Stream.of(Objects.requireNonNull(extensions, "extensions")).map(e -> "." + e).toArray(String[]::new);
    }

    /**
     * Implements behavior similar to the UNIX "touch" utility. Creates a new file with size 0, or, if the file exists, just
     * updates the file's modified time. This method throws an IOException if the last modified date
     * of the file cannot be set. It creates parent directories if they do not exist.
     *
     * @param file the File to touch.
     * @throws NullPointerException if the parameter is {@code null}.
     * @throws IOException if setting the last-modified time failed or an I/O problem occurs.
     */
    public static void touch(final File file) throws IOException {
        PathUtils.touch(Objects.requireNonNull(file, PROTOCOL_FILE).toPath());
    }

    /**
     * Converts each element of an array of {@link File} to a {@link URL}.
     * <p>
     * Returns an array of the same size as the input.
     * </p>
     *
     * @param files the files to convert, must not be {@code null}
     * @return an array of URLs matching the input
     * @throws IOException          if a file cannot be converted
     * @throws NullPointerException if any argument is null
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
     * @param source      the file or directory to be moved.
     * @param destination the destination file or directory.
     * @throws NullPointerException if any of the given {@link File}s are {@code null}.
     * @throws FileNotFoundException if the source file does not exist.
     */
    private static void validateMoveParameters(final File source, final File destination) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }
    }

    /**
     * Waits for the file system to detect a file's presence, with a timeout.
     * <p>
     * This method repeatedly tests {@link Files#exists(Path, LinkOption...)} until it returns
     * true up to the maximum time specified in seconds.
     * </p>
     *
     * @param file    the file to check, must not be {@code null}
     * @param seconds the maximum time in seconds to wait
     * @return true if file exists
     * @throws NullPointerException if the file is {@code null}
     */
    public static boolean waitFor(final File file, final int seconds) {
        Objects.requireNonNull(file, PROTOCOL_FILE);
        return PathUtils.waitFor(file.toPath(), Duration.ofSeconds(seconds), PathUtils.EMPTY_LINK_OPTION_ARRAY);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 2.0
     * @deprecated Use {@link #write(File, CharSequence, Charset)} instead (and specify the appropriate encoding)
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
     * @deprecated Use {@link #write(File, CharSequence, Charset, boolean)} instead (and specify the appropriate encoding)
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
    public static void write(final File file, final CharSequence data, final Charset charset, final boolean append) throws IOException {
        writeStringToFile(file, Objects.toString(data, null), charset, append);
    }

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
     * @throws java.nio.charset.UnsupportedCharsetException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void write(final File file, final CharSequence data, final String charsetName, final boolean append) throws IOException {
        write(file, data, Charsets.toCharset(charsetName), append);
    }

    // Must be called with a directory

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file the file to write to
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @since 1.1
     */
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

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
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append) throws IOException {
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
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len) throws IOException {
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
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len, final boolean append) throws IOException {
        try (OutputStream out = newOutputStream(file, append)) {
            out.write(data, off, len);
        }
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
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
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
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
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding) throws IOException {
        writeLines(file, null, lines, lineEnding, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
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
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding, final boolean append) throws IOException {
        writeLines(file, null, lines, lineEnding, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
     * The specified character encoding and the default line ending will be used.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file     the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines    the lines to write, {@code null} entries produce blank lines
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines) throws IOException {
        writeLines(file, charsetName, lines, null, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line, optionally appending.
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
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines, final boolean append) throws IOException {
        writeLines(file, charsetName, lines, null, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
     * The specified character encoding and the line ending will be used.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file       the file to write to
     * @param charsetName   the name of the requested charset, {@code null} means platform default
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines, final String lineEnding) throws IOException {
        writeLines(file, charsetName, lines, lineEnding, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@link File} line by line.
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
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines, final String lineEnding, final boolean append)
        throws IOException {
        try (OutputStream out = new BufferedOutputStream(newOutputStream(file, append))) {
            IOUtils.writeLines(lines, lineEnding, out, charsetName);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     * @deprecated Use {@link #writeStringToFile(File, String, Charset)} instead (and specify the appropriate encoding)
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
     * @deprecated Use {@link #writeStringToFile(File, String, Charset, boolean)} instead (and specify the appropriate encoding)
     */
    @Deprecated
    public static void writeStringToFile(final File file, final String data, final boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * The parent directories of the file will be created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.4
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset) throws IOException {
        writeStringToFile(file, data, charset, false);
    }

    /**
     * Writes a String to a file, creating the file if it does not exist.
     * The parent directories of the file are created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset, final boolean append) throws IOException {
        try (OutputStream out = newOutputStream(file, append)) {
            IOUtils.write(data, out, charset);
        }
    }

    /**
     * Writes a String to a file, creating the file if it does not exist.
     * The parent directories of the file are created if they do not exist.
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
     * Writes a String to a file, creating the file if it does not exist.
     * The parent directories of the file are created if they do not exist.
     *
     * @param file     the file to write
     * @param data     the content to write to the file
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param append   if {@code true}, then the String will be added to the
     *                 end of the file rather than overwriting
     * @throws IOException                 in case of an I/O error
     * @throws java.nio.charset.UnsupportedCharsetException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeStringToFile(final File file, final String data, final String charsetName, final boolean append) throws IOException {
        writeStringToFile(file, data, Charsets.toCharset(charsetName), append);
    }

    /**
     * Instances should NOT be constructed in standard programming.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public FileUtils() { //NOSONAR
        // empty
    }

}
