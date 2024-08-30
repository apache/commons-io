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

package org.apache.commons.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.ThreadUtils;
import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOSupplier;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public final class PathUtils {

    /**
     * Private worker/holder that computes and tracks relative path names and their equality. We reuse the sorted relative lists when comparing directories.
     */
    private static final class RelativeSortedPaths {

        final boolean equals;
        // final List<Path> relativeDirList1; // might need later?
        // final List<Path> relativeDirList2; // might need later?
        final List<Path> relativeFileList1;
        final List<Path> relativeFileList2;

        /**
         * Constructs and initializes a new instance by accumulating directory and file info.
         *
         * @param dir1             First directory to compare.
         * @param dir2             Seconds directory to compare.
         * @param maxDepth         See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
         * @param linkOptions      Options indicating how symbolic links are handled.
         * @param fileVisitOptions See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
         * @throws IOException if an I/O error is thrown by a visitor method.
         */
        private RelativeSortedPaths(final Path dir1, final Path dir2, final int maxDepth, final LinkOption[] linkOptions,
                final FileVisitOption[] fileVisitOptions) throws IOException {
            final List<Path> tmpRelativeDirList1;
            final List<Path> tmpRelativeDirList2;
            List<Path> tmpRelativeFileList1 = null;
            List<Path> tmpRelativeFileList2 = null;
            if (dir1 == null && dir2 == null) {
                equals = true;
            } else if (dir1 == null ^ dir2 == null) {
                equals = false;
            } else {
                final boolean parentDirNotExists1 = Files.notExists(dir1, linkOptions);
                final boolean parentDirNotExists2 = Files.notExists(dir2, linkOptions);
                if (parentDirNotExists1 || parentDirNotExists2) {
                    equals = parentDirNotExists1 && parentDirNotExists2;
                } else {
                    final AccumulatorPathVisitor visitor1 = accumulate(dir1, maxDepth, fileVisitOptions);
                    final AccumulatorPathVisitor visitor2 = accumulate(dir2, maxDepth, fileVisitOptions);
                    if (visitor1.getDirList().size() != visitor2.getDirList().size() || visitor1.getFileList().size() != visitor2.getFileList().size()) {
                        equals = false;
                    } else {
                        tmpRelativeDirList1 = visitor1.relativizeDirectories(dir1, true, null);
                        tmpRelativeDirList2 = visitor2.relativizeDirectories(dir2, true, null);
                        if (!tmpRelativeDirList1.equals(tmpRelativeDirList2)) {
                            equals = false;
                        } else {
                            tmpRelativeFileList1 = visitor1.relativizeFiles(dir1, true, null);
                            tmpRelativeFileList2 = visitor2.relativizeFiles(dir2, true, null);
                            equals = tmpRelativeFileList1.equals(tmpRelativeFileList2);
                        }
                    }
                }
            }
            // relativeDirList1 = tmpRelativeDirList1;
            // relativeDirList2 = tmpRelativeDirList2;
            relativeFileList1 = tmpRelativeFileList1;
            relativeFileList2 = tmpRelativeFileList2;
        }
    }

    private static final OpenOption[] OPEN_OPTIONS_TRUNCATE = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

    private static final OpenOption[] OPEN_OPTIONS_APPEND = { StandardOpenOption.CREATE, StandardOpenOption.APPEND };

    /**
     * Empty {@link CopyOption} array.
     *
     * @since 2.8.0
     */
    public static final CopyOption[] EMPTY_COPY_OPTIONS = {};

    /**
     * Empty {@link DeleteOption} array.
     *
     * @since 2.8.0
     */
    public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = {};

    /**
     * Empty {@link FileAttribute} array.
     *
     * @since 2.13.0
     */
    public static final FileAttribute<?>[] EMPTY_FILE_ATTRIBUTE_ARRAY = {};

    /**
     * Empty {@link FileVisitOption} array.
     */
    public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = {};

    /**
     * Empty {@link LinkOption} array.
     */
    public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = {};

    /**
     * {@link LinkOption} array for {@link LinkOption#NOFOLLOW_LINKS}.
     *
     * @since 2.9.0
     * @deprecated Use {@link #noFollowLinkOptionArray()}.
     */
    @Deprecated
    public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY = { LinkOption.NOFOLLOW_LINKS };

    /**
     * A LinkOption used to follow link in this class, the inverse of {@link LinkOption#NOFOLLOW_LINKS}.
     *
     * @since 2.12.0
     */
    static final LinkOption NULL_LINK_OPTION = null;

    /**
     * Empty {@link OpenOption} array.
     */
    public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = {};

    /**
     * Empty {@link Path} array.
     *
     * @since 2.9.0
     */
    public static final Path[] EMPTY_PATH_ARRAY = {};

    /**
     * Accumulates file tree information in a {@link AccumulatorPathVisitor}.
     *
     * @param directory        The directory to accumulate information.
     * @param maxDepth         See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param fileVisitOptions See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @return file tree information.
     */
    private static AccumulatorPathVisitor accumulate(final Path directory, final int maxDepth, final FileVisitOption[] fileVisitOptions) throws IOException {
        return visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory, toFileVisitOptionSet(fileVisitOptions), maxDepth);
    }

    /**
     * Cleans a directory including subdirectories without deleting directories.
     *
     * @param directory directory to clean.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters cleanDirectory(final Path directory) throws IOException {
        return cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Cleans a directory including subdirectories without deleting directories.
     *
     * @param directory     directory to clean.
     * @param deleteOptions How to handle deletion.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.8.0
     */
    public static PathCounters cleanDirectory(final Path directory, final DeleteOption... deleteOptions) throws IOException {
        return visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions), directory).getPathCounters();
    }

    /**
     * Compares the given {@link Path}'s last modified time to the given file time.
     *
     * @param file     the {@link Path} to test.
     * @param fileTime the time reference.
     * @param options  options indicating how to handle symbolic links.
     * @return See {@link FileTime#compareTo(FileTime)}
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     */
    private static int compareLastModifiedTimeTo(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        return getLastModifiedTime(file, options).compareTo(fileTime);
    }

    /**
     * Copies the InputStream from the supplier with {@link Files#copy(InputStream, Path, CopyOption...)}.
     *
     * @param in          Supplies the InputStream.
     * @param target      See {@link Files#copy(InputStream, Path, CopyOption...)}.
     * @param copyOptions See {@link Files#copy(InputStream, Path, CopyOption...)}.
     * @return See {@link Files#copy(InputStream, Path, CopyOption...)}
     * @throws IOException See {@link Files#copy(InputStream, Path, CopyOption...)}
     * @since 2.12.0
     */
    public static long copy(final IOSupplier<InputStream> in, final Path target, final CopyOption... copyOptions) throws IOException {
        try (InputStream inputStream = in.get()) {
            return Files.copy(inputStream, target, copyOptions);
        }
    }

    /**
     * Copies a directory to another directory.
     *
     * @param sourceDirectory The source directory.
     * @param targetDirectory The target directory.
     * @param copyOptions     Specifies how the copying should be done.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters copyDirectory(final Path sourceDirectory, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        final Path absoluteSource = sourceDirectory.toAbsolutePath();
        return visitFileTree(new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions), absoluteSource)
                .getPathCounters();
    }

    /**
     * Copies a URL to a directory.
     *
     * @param sourceFile  The source URL.
     * @param targetFile  The target file.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(InputStream, Path, CopyOption...)
     */
    public static Path copyFile(final URL sourceFile, final Path targetFile, final CopyOption... copyOptions) throws IOException {
        copy(sourceFile::openStream, targetFile, copyOptions);
        return targetFile;
    }

    /**
     * Copies a file to a directory.
     *
     * @param sourceFile      The source file.
     * @param targetDirectory The target directory.
     * @param copyOptions     Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(Path, Path, CopyOption...)
     */
    public static Path copyFileToDirectory(final Path sourceFile, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        return Files.copy(sourceFile, targetDirectory.resolve(sourceFile.getFileName()), copyOptions);
    }

    /**
     * Copies a URL to a directory.
     *
     * @param sourceFile      The source URL.
     * @param targetDirectory The target directory.
     * @param copyOptions     Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(InputStream, Path, CopyOption...)
     */
    public static Path copyFileToDirectory(final URL sourceFile, final Path targetDirectory, final CopyOption... copyOptions) throws IOException {
        final Path resolve = targetDirectory.resolve(FilenameUtils.getName(sourceFile.getFile()));
        copy(sourceFile::openStream, resolve, copyOptions);
        return resolve;
    }

    /**
     * Counts aspects of a directory including subdirectories.
     *
     * @param directory directory to delete.
     * @return The visitor used to count the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters countDirectory(final Path directory) throws IOException {
        return visitFileTree(CountingPathVisitor.withLongCounters(), directory).getPathCounters();
    }

    /**
     * Counts aspects of a directory including subdirectories.
     *
     * @param directory directory to count.
     * @return The visitor used to count the given directory.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    public static PathCounters countDirectoryAsBigInteger(final Path directory) throws IOException {
        return visitFileTree(CountingPathVisitor.withBigIntegerCounters(), directory).getPathCounters();
    }

    /**
     * Creates the parent directories for the given {@code path}.
     * <p>
     * If the parent directory already exists, then return it.
     * </p>
     *
     * @param path  The path to a file (or directory).
     * @param attrs An optional list of file attributes to set atomically when creating the directories.
     * @return The Path for the {@code path}'s parent directory or null if the given path has no parent.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static Path createParentDirectories(final Path path, final FileAttribute<?>... attrs) throws IOException {
        return createParentDirectories(path, LinkOption.NOFOLLOW_LINKS, attrs);
    }

    /**
     * Creates the parent directories for the given {@code path}.
     * <p>
     * If the parent directory already exists, then return it.
     * </p>
     *
     * @param path       The path to a file (or directory).
     * @param linkOption A {@link LinkOption} or null.
     * @param attrs      An optional list of file attributes to set atomically when creating the directories.
     * @return The Path for the {@code path}'s parent directory or null if the given path has no parent.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    public static Path createParentDirectories(final Path path, final LinkOption linkOption, final FileAttribute<?>... attrs) throws IOException {
        Path parent = getParent(path);
        parent = linkOption == LinkOption.NOFOLLOW_LINKS ? parent : readIfSymbolicLink(parent);
        if (parent == null) {
            return null;
        }
        final boolean exists = linkOption == null ? Files.exists(parent) : Files.exists(parent, linkOption);
        return exists ? parent : Files.createDirectories(parent, attrs);
    }

    /**
     * Gets the current directory.
     *
     * @return the current directory.
     *
     * @since 2.9.0
     */
    public static Path current() {
        return Paths.get(".");
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all subdirectories.
     * <p>
     * The difference between {@link File#delete()} and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link File#delete()} returns a boolean.
     * </ul>
     *
     * @param path file or directory to delete, must not be {@code null}
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          if an I/O error is thrown by a visitor method or if an I/O error occurs.
     */
    public static PathCounters delete(final Path path) throws IOException {
        return delete(path, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all subdirectories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link File#delete()} returns a boolean.
     * </ul>
     *
     * @param path          file or directory to delete, must not be {@code null}
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          if an I/O error is thrown by a visitor method or if an I/O error occurs.
     * @since 2.8.0
     */
    public static PathCounters delete(final Path path, final DeleteOption... deleteOptions) throws IOException {
        // File deletion through Files deletes links, not targets, so use LinkOption.NOFOLLOW_LINKS.
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? deleteDirectory(path, deleteOptions) : deleteFile(path, deleteOptions);
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all subdirectories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link File#delete()} returns a boolean.
     * </ul>
     *
     * @param path          file or directory to delete, must not be {@code null}
     * @param linkOptions   How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException          if an I/O error is thrown by a visitor method or if an I/O error occurs.
     * @since 2.9.0
     */
    public static PathCounters delete(final Path path, final LinkOption[] linkOptions, final DeleteOption... deleteOptions) throws IOException {
        // File deletion through Files deletes links, not targets, so use LinkOption.NOFOLLOW_LINKS.
        return Files.isDirectory(path, linkOptions) ? deleteDirectory(path, linkOptions, deleteOptions) : deleteFile(path, linkOptions, deleteOptions);
    }

    /**
     * Deletes a directory including subdirectories.
     *
     * @param directory directory to delete.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters deleteDirectory(final Path directory) throws IOException {
        return deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes a directory including subdirectories.
     *
     * @param directory     directory to delete.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.8.0
     */
    public static PathCounters deleteDirectory(final Path directory, final DeleteOption... deleteOptions) throws IOException {
        final LinkOption[] linkOptions = noFollowLinkOptionArray();
        // POSIX ops will noop on non-POSIX.
        return withPosixFileAttributes(getParent(directory), linkOptions, overrideReadOnly(deleteOptions),
                pfa -> visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters());
    }

    /**
     * Deletes a directory including subdirectories.
     *
     * @param directory     directory to delete.
     * @param linkOptions   How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.9.0
     */
    public static PathCounters deleteDirectory(final Path directory, final LinkOption[] linkOptions, final DeleteOption... deleteOptions) throws IOException {
        return visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters();
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException         if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     */
    public static PathCounters deleteFile(final Path file) throws IOException {
        return deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes the given file.
     *
     * @param file          The file to delete.
     * @param deleteOptions How to handle deletion.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException         if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     * @since 2.8.0
     */
    public static PathCounters deleteFile(final Path file, final DeleteOption... deleteOptions) throws IOException {
        // Files.deleteIfExists() never follows links, so use LinkOption.NOFOLLOW_LINKS in other calls to Files.
        return deleteFile(file, noFollowLinkOptionArray(), deleteOptions);
    }

    /**
     * Deletes the given file.
     *
     * @param file          The file to delete.
     * @param linkOptions   How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException         if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     * @since 2.9.0
     */
    public static PathCounters deleteFile(final Path file, final LinkOption[] linkOptions, final DeleteOption... deleteOptions)
            throws NoSuchFileException, IOException {
        //
        // TODO Needs clean up
        //
        if (Files.isDirectory(file, linkOptions)) {
            throw new NoSuchFileException(file.toString());
        }
        final PathCounters pathCounts = Counters.longPathCounters();
        boolean exists = exists(file, linkOptions);
        long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0;
        try {
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
                return pathCounts;
            }
        } catch (final AccessDeniedException ignored) {
            // Ignore and try again below.
        }
        final Path parent = getParent(file);
        PosixFileAttributes posixFileAttributes = null;
        try {
            if (overrideReadOnly(deleteOptions)) {
                posixFileAttributes = readPosixFileAttributes(parent, linkOptions);
                setReadOnly(file, false, linkOptions);
            }
            // Read size _after_ having read/execute access on POSIX.
            exists = exists(file, linkOptions);
            size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0;
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
            }
        } finally {
            if (posixFileAttributes != null) {
                Files.setPosixFilePermissions(parent, posixFileAttributes.permissions());
            }
        }
        return pathCounts;
    }

    /**
     * Delegates to {@link File#deleteOnExit()}.
     *
     * @param path the path to delete.
     * @since 3.13.0
     */
    public static void deleteOnExit(final Path path) {
        Objects.requireNonNull(path).toFile().deleteOnExit();
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not while considering file contents. The comparison includes all files in all
     * subdirectories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @return Whether the two directories contain the same files while considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static boolean directoryAndFileContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not while considering file contents. The comparison includes all files in all
     * subdirectories.
     *
     * @param path1           The first directory.
     * @param path2           The second directory.
     * @param linkOptions     options to follow links.
     * @param openOptions     options to open files.
     * @param fileVisitOption options to configure traversal.
     * @return Whether the two directories contain the same files while considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static boolean directoryAndFileContentEquals(final Path path1, final Path path2, final LinkOption[] linkOptions, final OpenOption[] openOptions,
            final FileVisitOption[] fileVisitOption) throws IOException {
        // First walk both file trees and gather normalized paths.
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        if (notExists(path1) && notExists(path2)) {
            return true;
        }
        final RelativeSortedPaths relativeSortedPaths = new RelativeSortedPaths(path1, path2, Integer.MAX_VALUE, linkOptions, fileVisitOption);
        // If the normalized path names and counts are not the same, no need to compare contents.
        if (!relativeSortedPaths.equals) {
            return false;
        }
        // Both visitors contain the same normalized paths, we can compare file contents.
        final List<Path> fileList1 = relativeSortedPaths.relativeFileList1;
        final List<Path> fileList2 = relativeSortedPaths.relativeFileList2;
        for (final Path path : fileList1) {
            final int binarySearch = Collections.binarySearch(fileList2, path);
            if (binarySearch <= -1) {
                throw new IllegalStateException("Unexpected mismatch.");
            }
            if (!fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not without considering file contents. The comparison includes all files in all
     * subdirectories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @return Whether the two directories contain the same files without considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static boolean directoryContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not without considering file contents. The comparison includes all files in all
     * subdirectories.
     *
     * @param path1            The first directory.
     * @param path2            The second directory.
     * @param maxDepth         See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param linkOptions      options to follow links.
     * @param fileVisitOptions options to configure the traversal
     * @return Whether the two directories contain the same files without considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static boolean directoryContentEquals(final Path path1, final Path path2, final int maxDepth, final LinkOption[] linkOptions,
            final FileVisitOption[] fileVisitOptions) throws IOException {
        return new RelativeSortedPaths(path1, path2, maxDepth, linkOptions, fileVisitOptions).equals;
    }

    private static boolean exists(final Path path, final LinkOption... options) {
        return path != null && (options != null ? Files.exists(path, options) : Files.exists(path));
    }

    /**
     * Compares the file contents of two Paths to determine if they are equal or not.
     * <p>
     * File content is accessed through {@link Files#newInputStream(Path,OpenOption...)}.
     * </p>
     *
     * @param path1 the first stream.
     * @param path2 the second stream.
     * @return true if the content of the streams are equal or they both don't exist, false otherwise.
     * @throws NullPointerException if either input is null.
     * @throws IOException          if an I/O error occurs.
     * @see org.apache.commons.io.FileUtils#contentEquals(java.io.File, java.io.File)
     */
    public static boolean fileContentEquals(final Path path1, final Path path2) throws IOException {
        return fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
    }

    /**
     * Compares the file contents of two Paths to determine if they are equal or not.
     * <p>
     * File content is accessed through {@link RandomAccessFileMode#create(Path)}.
     * </p>
     *
     * @param path1       the first stream.
     * @param path2       the second stream.
     * @param linkOptions options specifying how files are followed.
     * @param openOptions ignored.
     * @return true if the content of the streams are equal or they both don't exist, false otherwise.
     * @throws NullPointerException if openOptions is null.
     * @throws IOException          if an I/O error occurs.
     * @see org.apache.commons.io.FileUtils#contentEquals(java.io.File, java.io.File)
     */
    public static boolean fileContentEquals(final Path path1, final Path path2, final LinkOption[] linkOptions, final OpenOption[] openOptions)
            throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        final Path nPath1 = path1.normalize();
        final Path nPath2 = path2.normalize();
        final boolean path1Exists = exists(nPath1, linkOptions);
        if (path1Exists != exists(nPath2, linkOptions)) {
            return false;
        }
        if (!path1Exists) {
            // Two not existing files are equal?
            // Same as FileUtils
            return true;
        }
        if (Files.isDirectory(nPath1, linkOptions)) {
            // don't compare directory contents.
            throw new IOException("Can't compare directories, only files: " + nPath1);
        }
        if (Files.isDirectory(nPath2, linkOptions)) {
            // don't compare directory contents.
            throw new IOException("Can't compare directories, only files: " + nPath2);
        }
        if (Files.size(nPath1) != Files.size(nPath2)) {
            // lengths differ, cannot be equal
            return false;
        }
        if (path1.equals(path2)) {
            // same file
            return true;
        }
        // Faster:
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(path1.toRealPath(linkOptions));
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(path2.toRealPath(linkOptions))) {
            return RandomAccessFiles.contentEquals(raf1, raf2);
        } catch (final UnsupportedOperationException e) {
            // Slower:
            // Handle
            // java.lang.UnsupportedOperationException
            // at com.sun.nio.zipfs.ZipPath.toFile(ZipPath.java:656)
            try (InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);
                    InputStream inputStream2 = Files.newInputStream(nPath2, openOptions)) {
                return IOUtils.contentEquals(inputStream1, inputStream2);
            }
        }
    }

    /**
     * <p>
     * Applies an {@link IOFileFilter} to the provided {@link File} objects. The resulting array is a subset of the original file list that matches the provided
     * filter.
     * </p>
     *
     * <p>
     * The {@link Set} returned by this method is not guaranteed to be thread safe.
     * </p>
     *
     * <pre>
     * Set&lt;File&gt; allFiles = ...
     * Set&lt;File&gt; javaFiles = FileFilterUtils.filterSet(allFiles,
     *     FileFilterUtils.suffixFileFilter(".java"));
     * </pre>
     *
     * @param filter the filter to apply to the set of files.
     * @param paths  the array of files to apply the filter to.
     *
     * @return a subset of {@code files} that is accepted by the file filter.
     * @throws NullPointerException     if the filter is {@code null}
     * @throws IllegalArgumentException if {@code files} contains a {@code null} value.
     *
     * @since 2.9.0
     */
    public static Path[] filter(final PathFilter filter, final Path... paths) {
        Objects.requireNonNull(filter, "filter");
        if (paths == null) {
            return EMPTY_PATH_ARRAY;
        }
        return filterPaths(filter, Stream.of(paths), Collectors.toList()).toArray(EMPTY_PATH_ARRAY);
    }

    private static <R, A> R filterPaths(final PathFilter filter, final Stream<Path> stream, final Collector<? super Path, A, R> collector) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(collector, "collector");
        if (stream == null) {
            return Stream.<Path>empty().collect(collector);
        }
        return stream.filter(p -> {
            try {
                return p != null && filter.accept(p, readBasicFileAttributes(p)) == FileVisitResult.CONTINUE;
            } catch (final IOException e) {
                return false;
            }
        }).collect(collector);
    }

    /**
     * Reads the access control list from a file attribute view.
     *
     * @param sourcePath the path to the file.
     * @return a file attribute view of the given type, or null if the attribute view type is not available.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static List<AclEntry> getAclEntryList(final Path sourcePath) throws IOException {
        final AclFileAttributeView fileAttributeView = getAclFileAttributeView(sourcePath);
        return fileAttributeView == null ? null : fileAttributeView.getAcl();
    }

    /**
     * Shorthand for {@code Files.getFileAttributeView(path, AclFileAttributeView.class)}.
     *
     * @param path    the path to the file.
     * @param options how to handle symbolic links.
     * @return a AclFileAttributeView, or {@code null} if the attribute view type is not available.
     * @since 2.12.0
     */
    public static AclFileAttributeView getAclFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, AclFileAttributeView.class, options);
    }

    /**
     * Gets the base name (the part up to and not including the last ".") of the last path segment of a file name.
     * <p>
     * Will return the file name itself if it doesn't contain any dots. All leading directories of the {@code file name} parameter are skipped.
     * </p>
     *
     * @return the base name of file name
     * @param path the path of the file to obtain the base name of.
     * @since 2.16.0
     */
    public static String getBaseName(final Path path) {
        if (path == null) {
            return null;
        }
        final Path fileName = path.getFileName();
        return fileName != null ? FilenameUtils.removeExtension(fileName.toString()) : null;
    }

    /**
     * Shorthand for {@code Files.getFileAttributeView(path, DosFileAttributeView.class, options)}.
     *
     * @param path    the path to the file.
     * @param options how to handle symbolic links.
     * @return a DosFileAttributeView, or {@code null} if the attribute view type is not available.
     * @since 2.12.0
     */
    public static DosFileAttributeView getDosFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, DosFileAttributeView.class, options);
    }

    /**
     * Gets the extension of a Path.
     * <p>
     * This method returns the textual part of the Path after the last dot.
     * </p>
     * <pre>
     * foo.txt      --&gt; "txt"
     * a/b/c.jpg    --&gt; "jpg"
     * a/b.txt/c    --&gt; ""
     * a/b/c        --&gt; ""
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * </p>
     *
     * @param path the path to query.
     * @return the extension of the file or an empty string if none exists or {@code null} if the fileName is {@code null}.
     * @since 2.16.0
     */
    public static String getExtension(final Path path) {
        final String fileName = getFileNameString(path);
        return fileName != null ? FilenameUtils.getExtension(fileName) : null;
    }

    /**
     * Gets the Path's file name and apply the given function if the file name is non-null.
     *
     * @param <R> The function's result type.
     * @param path the path to query.
     * @param function function to apply to the file name.
     * @return the Path's file name as a string or null.
     * @see Path#getFileName()
     * @since 2.16.0
     */
    public static <R> R getFileName(final Path path, final Function<Path, R> function) {
        final Path fileName = path != null ? path.getFileName() : null;
        return fileName != null ? function.apply(fileName) : null;
    }

    /**
     * Gets the Path's file name as a string.
     *
     * @param path the path to query.
     * @return the Path's file name as a string or null.
     * @see Path#getFileName()
     * @since 2.16.0
     */
    public static String getFileNameString(final Path path) {
        return getFileName(path, Path::toString);
    }

    /**
     * Gets the file's last modified time or null if the file does not exist.
     * <p>
     * The method provides a workaround for bug <a href="https://bugs.openjdk.java.net/browse/JDK-8177809">JDK-8177809</a> where {@link File#lastModified()}
     * looses milliseconds and always ends in 000. This bug is in OpenJDK 8 and 9, and fixed in 11.
     * </p>
     *
     * @param file the file to query.
     * @return the file's last modified time.
     * @throws IOException Thrown if an I/O error occurs.
     * @since 2.12.0
     */
    public static FileTime getLastModifiedFileTime(final File file) throws IOException {
        return getLastModifiedFileTime(file.toPath(), null, EMPTY_LINK_OPTION_ARRAY);
    }

    /**
     * Gets the file's last modified time or null if the file does not exist.
     *
     * @param path            the file to query.
     * @param defaultIfAbsent Returns this file time of the file does not exist, may be null.
     * @param options         options indicating how symbolic links are handled.
     * @return the file's last modified time.
     * @throws IOException Thrown if an I/O error occurs.
     * @since 2.12.0
     */
    public static FileTime getLastModifiedFileTime(final Path path, final FileTime defaultIfAbsent, final LinkOption... options) throws IOException {
        return Files.exists(path) ? getLastModifiedTime(path, options) : defaultIfAbsent;
    }

    /**
     * Gets the file's last modified time or null if the file does not exist.
     *
     * @param path    the file to query.
     * @param options options indicating how symbolic links are handled.
     * @return the file's last modified time.
     * @throws IOException Thrown if an I/O error occurs.
     * @since 2.12.0
     */
    public static FileTime getLastModifiedFileTime(final Path path, final LinkOption... options) throws IOException {
        return getLastModifiedFileTime(path, null, options);
    }

    /**
     * Gets the file's last modified time or null if the file does not exist.
     *
     * @param uri the file to query.
     * @return the file's last modified time.
     * @throws IOException Thrown if an I/O error occurs.
     * @since 2.12.0
     */
    public static FileTime getLastModifiedFileTime(final URI uri) throws IOException {
        return getLastModifiedFileTime(Paths.get(uri), null, EMPTY_LINK_OPTION_ARRAY);
    }

    /**
     * Gets the file's last modified time or null if the file does not exist.
     *
     * @param url the file to query.
     * @return the file's last modified time.
     * @throws IOException        Thrown if an I/O error occurs.
     * @throws URISyntaxException if the URL is not formatted strictly according to RFC2396 and cannot be converted to a URI.
     * @since 2.12.0
     */
    public static FileTime getLastModifiedFileTime(final URL url) throws IOException, URISyntaxException {
        return getLastModifiedFileTime(url.toURI());
    }

    private static FileTime getLastModifiedTime(final Path path, final LinkOption... options) throws IOException {
        return Files.getLastModifiedTime(Objects.requireNonNull(path, "path"), options);
    }

    private static Path getParent(final Path path) {
        return path == null ? null : path.getParent();
    }

    /**
     * Shorthand for {@code Files.getFileAttributeView(path, PosixFileAttributeView.class)}.
     *
     * @param path    the path to the file.
     * @param options how to handle symbolic links.
     * @return a PosixFileAttributeView, or {@code null} if the attribute view type is not available.
     * @since 2.12.0
     */
    public static PosixFileAttributeView getPosixFileAttributeView(final Path path, final LinkOption... options) {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class, options);
    }

    /**
     * Gets a {@link Path} representing the system temporary directory.
     *
     * @return the system temporary directory.
     * @since 2.12.0
     */
    public static Path getTempDirectory() {
        return Paths.get(FileUtils.getTempDirectoryPath());
    }

    /**
     * Tests whether the given {@link Path} is a directory or not. Implemented as a null-safe delegate to
     * {@code Files.isDirectory(Path path, LinkOption... options)}.
     *
     * @param path    the path to the file.
     * @param options options indicating how to handle symbolic links
     * @return {@code true} if the file is a directory; {@code false} if the path is null, the file does not exist, is not a directory, or it cannot be
     *         determined if the file is a directory or not.
     * @throws SecurityException In the case of the default provider, and a security manager is installed, the {@link SecurityManager#checkRead(String)
     *                           checkRead} method is invoked to check read access to the directory.
     * @since 2.9.0
     */
    public static boolean isDirectory(final Path path, final LinkOption... options) {
        return path != null && Files.isDirectory(path, options);
    }

    /**
     * Tests whether the given file or directory is empty.
     *
     * @param path the file or directory to query.
     * @return whether the file or directory is empty.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean isEmpty(final Path path) throws IOException {
        return Files.isDirectory(path) ? isEmptyDirectory(path) : isEmptyFile(path);
    }

    /**
     * Tests whether the directory is empty.
     *
     * @param directory the directory to query.
     * @return whether the directory is empty.
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not a directory <em>(optional specific exception)</em>.
     * @throws IOException           if an I/O error occurs.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the {@link SecurityManager#checkRead(String)
     *                               checkRead} method is invoked to check read access to the directory.
     */
    public static boolean isEmptyDirectory(final Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            return !directoryStream.iterator().hasNext();
        }
    }

    /**
     * Tests whether the given file is empty.
     *
     * @param file the file to query.
     * @return whether the file is empty.
     * @throws IOException       if an I/O error occurs.
     * @throws SecurityException In the case of the default provider, and a security manager is installed, its {@link SecurityManager#checkRead(String)
     *                           checkRead} method denies read access to the file.
     */
    public static boolean isEmptyFile(final Path file) throws IOException {
        return Files.size(file) <= 0;
    }

    /**
     * Tests if the given {@link Path} is newer than the given time reference.
     *
     * @param file    the {@link Path} to test.
     * @param czdt    the time reference.
     * @param options options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified after the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isNewer(final Path file, final ChronoZonedDateTime<?> czdt, final LinkOption... options) throws IOException {
        Objects.requireNonNull(czdt, "czdt");
        return isNewer(file, czdt.toInstant(), options);
    }

    /**
     * Tests if the given {@link Path} is newer than the given time reference.
     *
     * @param file     the {@link Path} to test.
     * @param fileTime the time reference.
     * @param options  options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified after the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isNewer(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        if (notExists(file)) {
            return false;
        }
        return compareLastModifiedTimeTo(file, fileTime, options) > 0;
    }

    /**
     * Tests if the given {@link Path} is newer than the given time reference.
     *
     * @param file    the {@link Path} to test.
     * @param instant the time reference.
     * @param options options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified after the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isNewer(final Path file, final Instant instant, final LinkOption... options) throws IOException {
        return isNewer(file, FileTime.from(instant), options);
    }

    /**
     * Tests if the given {@link Path} is newer than the given time reference.
     *
     * @param file       the {@link Path} to test.
     * @param timeMillis the time reference measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @param options    options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified after the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.9.0
     */
    public static boolean isNewer(final Path file, final long timeMillis, final LinkOption... options) throws IOException {
        return isNewer(file, FileTime.fromMillis(timeMillis), options);
    }

    /**
     * Tests if the given {@link Path} is newer than the reference {@link Path}.
     *
     * @param file      the {@link File} to test.
     * @param reference the {@link File} of which the modification date is used.
     * @return true if the {@link File} exists and has been modified more recently than the reference {@link File}.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    public static boolean isNewer(final Path file, final Path reference) throws IOException {
        return isNewer(file, getLastModifiedTime(reference));
    }

    /**
     * Tests if the given {@link Path} is older than the given time reference.
     *
     * @param file     the {@link Path} to test.
     * @param fileTime the time reference.
     * @param options  options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified before the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isOlder(final Path file, final FileTime fileTime, final LinkOption... options) throws IOException {
        if (notExists(file)) {
            return false;
        }
        return compareLastModifiedTimeTo(file, fileTime, options) < 0;
    }

    /**
     * Tests if the given {@link Path} is older than the given time reference.
     *
     * @param file    the {@link Path} to test.
     * @param instant the time reference.
     * @param options options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified before the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isOlder(final Path file, final Instant instant, final LinkOption... options) throws IOException {
        return isOlder(file, FileTime.from(instant), options);
    }

    /**
     * Tests if the given {@link Path} is older than the given time reference.
     *
     * @param file       the {@link Path} to test.
     * @param timeMillis the time reference measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @param options    options indicating how to handle symbolic links.
     * @return true if the {@link Path} exists and has been modified before the given time reference.
     * @throws IOException          if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean isOlder(final Path file, final long timeMillis, final LinkOption... options) throws IOException {
        return isOlder(file, FileTime.fromMillis(timeMillis), options);
    }

    /**
     * Tests if the given {@link Path} is older than the reference {@link Path}.
     *
     * @param file      the {@link File} to test.
     * @param reference the {@link File} of which the modification date is used.
     * @return true if the {@link File} exists and has been modified before than the reference {@link File}.
     * @throws IOException if an I/O error occurs.
     * @since 2.12.0
     */
    public static boolean isOlder(final Path file, final Path reference) throws IOException {
        return isOlder(file, getLastModifiedTime(reference));
    }

    /**
     * Tests whether the given path is on a POSIX file system.
     *
     * @param test    The Path to test.
     * @param options options indicating how to handle symbolic links.
     * @return true if test is on a POSIX file system.
     * @since 2.12.0
     */
    public static boolean isPosix(final Path test, final LinkOption... options) {
        return exists(test, options) && readPosixFileAttributes(test, options) != null;
    }

    /**
     * Tests whether the given {@link Path} is a regular file or not. Implemented as a null-safe delegate to
     * {@code Files.isRegularFile(Path path, LinkOption... options)}.
     *
     * @param path    the path to the file.
     * @param options options indicating how to handle symbolic links.
     * @return {@code true} if the file is a regular file; {@code false} if the path is null, the file does not exist, is not a directory, or it cannot be
     *         determined if the file is a regular file or not.
     * @throws SecurityException In the case of the default provider, and a security manager is installed, the {@link SecurityManager#checkRead(String)
     *                           checkRead} method is invoked to check read access to the directory.
     * @since 2.9.0
     */
    public static boolean isRegularFile(final Path path, final LinkOption... options) {
        return path != null && Files.isRegularFile(path, options);
    }

    /**
     * Creates a new DirectoryStream for Paths rooted at the given directory.
     * <p>
     * If you don't use the try-with-resources construct, then you must call the stream's {@link Stream#close()} method after iteration is complete to free any
     * resources held for the open directory.
     * </p>
     *
     * @param dir        the path to the directory to stream.
     * @param pathFilter the directory stream filter.
     * @return a new instance.
     * @throws IOException if an I/O error occurs.
     */
    public static DirectoryStream<Path> newDirectoryStream(final Path dir, final PathFilter pathFilter) throws IOException {
        return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
    }

    /**
     * Creates a new OutputStream by opening or creating a file, returning an output stream that may be used to write bytes to the file.
     *
     * @param path   the Path.
     * @param append Whether or not to append.
     * @return a new OutputStream.
     * @throws IOException if an I/O error occurs.
     * @see Files#newOutputStream(Path, OpenOption...)
     * @since 2.12.0
     */
    public static OutputStream newOutputStream(final Path path, final boolean append) throws IOException {
        return newOutputStream(path, EMPTY_LINK_OPTION_ARRAY, append ? OPEN_OPTIONS_APPEND : OPEN_OPTIONS_TRUNCATE);
    }

    static OutputStream newOutputStream(final Path path, final LinkOption[] linkOptions, final OpenOption... openOptions) throws IOException {
        if (!exists(path, linkOptions)) {
            createParentDirectories(path, linkOptions != null && linkOptions.length > 0 ? linkOptions[0] : NULL_LINK_OPTION);
        }
        final List<OpenOption> list = new ArrayList<>(Arrays.asList(openOptions != null ? openOptions : EMPTY_OPEN_OPTION_ARRAY));
        list.addAll(Arrays.asList(linkOptions != null ? linkOptions : EMPTY_LINK_OPTION_ARRAY));
        return Files.newOutputStream(path, list.toArray(EMPTY_OPEN_OPTION_ARRAY));
    }

    /**
     * Copy of the {@link LinkOption} array for {@link LinkOption#NOFOLLOW_LINKS}.
     *
     * @return Copy of the {@link LinkOption} array for {@link LinkOption#NOFOLLOW_LINKS}.
     */
    public static LinkOption[] noFollowLinkOptionArray() {
        return NOFOLLOW_LINK_OPTION_ARRAY.clone();
    }

    private static boolean notExists(final Path path, final LinkOption... options) {
        return Files.notExists(Objects.requireNonNull(path, "path"), options);
    }

    /**
     * Returns true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     *
     * @param deleteOptions the array to test
     * @return true if the given options contain {@link StandardDeleteOption#OVERRIDE_READ_ONLY}.
     */
    private static boolean overrideReadOnly(final DeleteOption... deleteOptions) {
        if (deleteOptions == null) {
            return false;
        }
        return Stream.of(deleteOptions).anyMatch(e -> e == StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

    /**
     * Reads the BasicFileAttributes from the given path. Returns null if the attributes can't be read.
     *
     * @param <A>     The {@link BasicFileAttributes} type
     * @param path    The Path to test.
     * @param type    the {@link Class} of the file attributes required to read.
     * @param options options indicating how to handle symbolic links.
     * @return the file attributes or null if the attributes can't be read.
     * @see Files#readAttributes(Path, Class, LinkOption...)
     * @since 2.12.0
     */
    public static <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) {
        try {
            return path == null ? null : Files.readAttributes(path, type, options);
        } catch (final UnsupportedOperationException | IOException e) {
            // For example, on Windows.
            return null;
        }
    }

    /**
     * Reads the BasicFileAttributes from the given path.
     *
     * @param path the path to read.
     * @return the path attributes.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static BasicFileAttributes readBasicFileAttributes(final Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class);
    }

    /**
     * Reads the BasicFileAttributes from the given path. Returns null if the attributes can't be read.
     *
     * @param path    the path to read.
     * @param options options indicating how to handle symbolic links.
     * @return the path attributes.
     * @since 2.12.0
     */
    public static BasicFileAttributes readBasicFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, BasicFileAttributes.class, options);
    }

    /**
     * Reads the BasicFileAttributes from the given path. Returns null if the attributes can't be read.
     *
     * @param path the path to read.
     * @return the path attributes.
     * @since 2.9.0
     * @deprecated Use {@link #readBasicFileAttributes(Path, LinkOption...)}.
     */
    @Deprecated
    public static BasicFileAttributes readBasicFileAttributesUnchecked(final Path path) {
        return readBasicFileAttributes(path, EMPTY_LINK_OPTION_ARRAY);
    }

    /**
     * Reads the DosFileAttributes from the given path. Returns null if the attributes can't be read.
     *
     * @param path    the path to read.
     * @param options options indicating how to handle symbolic links.
     * @return the path attributes.
     * @since 2.12.0
     */
    public static DosFileAttributes readDosFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, DosFileAttributes.class, options);
    }

    private static Path readIfSymbolicLink(final Path path) throws IOException {
        return path != null ? Files.isSymbolicLink(path) ? Files.readSymbolicLink(path) : path : null;
    }

    /**
     * Reads the PosixFileAttributes or DosFileAttributes from the given path. Returns null if the attributes can't be read.
     *
     * @param path    The Path to read.
     * @param options options indicating how to handle symbolic links.
     * @return the file attributes.
     * @since 2.12.0
     */
    public static BasicFileAttributes readOsFileAttributes(final Path path, final LinkOption... options) {
        final PosixFileAttributes fileAttributes = readPosixFileAttributes(path, options);
        return fileAttributes != null ? fileAttributes : readDosFileAttributes(path, options);
    }

    /**
     * Reads the PosixFileAttributes from the given path. Returns null instead of throwing {@link UnsupportedOperationException}.
     *
     * @param path    The Path to read.
     * @param options options indicating how to handle symbolic links.
     * @return the file attributes.
     * @since 2.12.0
     */
    public static PosixFileAttributes readPosixFileAttributes(final Path path, final LinkOption... options) {
        return readAttributes(path, PosixFileAttributes.class, options);
    }

    /**
     * Reads the file contents at the given path as a String using the Charset.
     *
     * @param path    The source path.
     * @param charset How to convert bytes to a String, null uses the default Charset.
     * @return the file contents as a new String.
     * @throws IOException if an I/O error occurs reading from the stream.
     * @see Files#readAllBytes(Path)
     * @see Charsets#toCharset(Charset)
     * @since 2.12.0
     */
    public static String readString(final Path path, final Charset charset) throws IOException {
        return new String(Files.readAllBytes(path), Charsets.toCharset(charset));
    }

    /**
     * Relativizes all files in the given {@code collection} against a {@code parent}.
     *
     * @param collection The collection of paths to relativize.
     * @param parent     relativizes against this parent path.
     * @param sort       Whether to sort the result.
     * @param comparator How to sort.
     * @return A collection of relativized paths, optionally sorted.
     */
    static List<Path> relativize(final Collection<Path> collection, final Path parent, final boolean sort, final Comparator<? super Path> comparator) {
        Stream<Path> stream = collection.stream().map(parent::relativize);
        if (sort) {
            stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * Requires that the given {@link File} exists and throws an {@link IllegalArgumentException} if it doesn't.
     *
     * @param file          The {@link File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @param options       options indicating how to handle symbolic links.
     * @return the given file.
     * @throws NullPointerException     if the given {@link File} is {@code null}.
     * @throws IllegalArgumentException if the given {@link File} does not exist.
     */
    private static Path requireExists(final Path file, final String fileParamName, final LinkOption... options) {
        Objects.requireNonNull(file, fileParamName);
        if (!exists(file, options)) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    private static boolean setDosReadOnly(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        final DosFileAttributeView dosFileAttributeView = getDosFileAttributeView(path, linkOptions);
        if (dosFileAttributeView != null) {
            dosFileAttributeView.setReadOnly(readOnly);
            return true;
        }
        return false;
    }

    /**
     * Sets the given {@code targetFile}'s last modified time to the value from {@code sourceFile}.
     *
     * @param sourceFile The source path to query.
     * @param targetFile The target path to set.
     * @throws NullPointerException if sourceFile is {@code null}.
     * @throws NullPointerException if targetFile is {@code null}.
     * @throws IOException          if setting the last-modified time failed.
     * @since 2.12.0
     */
    public static void setLastModifiedTime(final Path sourceFile, final Path targetFile) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Files.setLastModifiedTime(targetFile, getLastModifiedTime(sourceFile));
    }

    /**
     * To delete a file in POSIX, you need Write and Execute permissions on its parent directory.
     *
     * @param parent               The parent path for a file element to delete which needs RW permissions.
     * @param enableDeleteChildren true to set permissions to delete.
     * @param linkOptions          options indicating how handle symbolic links.
     * @return true if the operation was attempted and succeeded, false if parent is null.
     * @throws IOException if an I/O error occurs.
     */
    private static boolean setPosixDeletePermissions(final Path parent, final boolean enableDeleteChildren, final LinkOption... linkOptions)
            throws IOException {
        // To delete a file in POSIX, you need write and execute permissions on its parent directory.
        // @formatter:off
        return setPosixPermissions(parent, enableDeleteChildren, Arrays.asList(
            PosixFilePermission.OWNER_WRITE,
            //PosixFilePermission.GROUP_WRITE,
            //PosixFilePermission.OTHERS_WRITE,
            PosixFilePermission.OWNER_EXECUTE
            //PosixFilePermission.GROUP_EXECUTE,
            //PosixFilePermission.OTHERS_EXECUTE
            ), linkOptions);
        // @formatter:on
    }

    /**
     * Low-level POSIX permission operation to set permissions.
     * <p>
     * If the permissions to update are already set, then make no additional calls.
     * </p>
     *
     * @param path              Set this path's permissions.
     * @param addPermissions    true to add, false to remove.
     * @param updatePermissions the List of PosixFilePermission to add or remove.
     * @param linkOptions       options indicating how handle symbolic links.
     * @return true if the operation was attempted and succeeded, false if parent is null.
     * @throws IOException if an I/O error occurs.
     */
    private static boolean setPosixPermissions(final Path path, final boolean addPermissions, final List<PosixFilePermission> updatePermissions,
            final LinkOption... linkOptions) throws IOException {
        if (path != null) {
            final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
            final Set<PosixFilePermission> newPermissions = new HashSet<>(permissions);
            if (addPermissions) {
                newPermissions.addAll(updatePermissions);
            } else {
                newPermissions.removeAll(updatePermissions);
            }
            if (!newPermissions.equals(permissions)) {
                Files.setPosixFilePermissions(path, newPermissions);
            }
            return true;
        }
        return false;
    }

    private static void setPosixReadOnlyFile(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        // Not Windows 10
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
        // @formatter:off
        final List<PosixFilePermission> readPermissions = Arrays.asList(
                PosixFilePermission.OWNER_READ
                //PosixFilePermission.GROUP_READ,
                //PosixFilePermission.OTHERS_READ
            );
        final List<PosixFilePermission> writePermissions = Arrays.asList(
                PosixFilePermission.OWNER_WRITE
                //PosixFilePermission.GROUP_WRITE,
                //PosixFilePermission.OTHERS_WRITE
            );
        // @formatter:on
        if (readOnly) {
            // RO: We can read, we cannot write.
            permissions.addAll(readPermissions);
            permissions.removeAll(writePermissions);
        } else {
            // Not RO: We can read, we can write.
            permissions.addAll(readPermissions);
            permissions.addAll(writePermissions);
        }
        Files.setPosixFilePermissions(path, permissions);
    }

    /**
     * Sets the given Path to the {@code readOnly} value.
     * <p>
     * This behavior is OS dependent.
     * </p>
     *
     * @param path        The path to set.
     * @param readOnly    true for read-only, false for not read-only.
     * @param linkOptions options indicating how to handle symbolic links.
     * @return The given path.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static Path setReadOnly(final Path path, final boolean readOnly, final LinkOption... linkOptions) throws IOException {
        try {
            // Windows is simplest
            if (setDosReadOnly(path, readOnly, linkOptions)) {
                return path;
            }
        } catch (final IOException ignored) {
            // Retry with POSIX below.
        }
        final Path parent = getParent(path);
        if (!isPosix(parent, linkOptions)) { // Test parent because we may not the permissions to test the file.
            throw new IOException(String.format("DOS or POSIX file operations not available for '%s', linkOptions %s", path, Arrays.toString(linkOptions)));
        }
        // POSIX
        if (readOnly) {
            // RO
            // File, then parent dir (if any).
            setPosixReadOnlyFile(path, readOnly, linkOptions);
            setPosixDeletePermissions(parent, false, linkOptions);
        } else {
            // RE
            // Parent dir (if any), then file.
            setPosixDeletePermissions(parent, true, linkOptions);
        }
        return path;
    }

    /**
     * Returns the size of the given file or directory. If the provided {@link Path} is a regular file, then the file's size is returned. If the argument is a
     * directory, then the size of the directory is calculated recursively.
     * <p>
     * Note that overflow is not detected, and the return value may be negative if overflow occurs. See {@link #sizeOfAsBigInteger(Path)} for an alternative
     * method that does not overflow.
     * </p>
     *
     * @param path the regular file or directory to return the size of, must not be {@code null}.
     * @return the length of the file, or recursive size of the directory, in bytes.
     * @throws NullPointerException     if the file is {@code null}.
     * @throws IllegalArgumentException if the file does not exist.
     * @throws IOException              if an I/O error occurs.
     * @since 2.12.0
     */
    public static long sizeOf(final Path path) throws IOException {
        requireExists(path, "path");
        return Files.isDirectory(path) ? sizeOfDirectory(path) : Files.size(path);
    }

    /**
     * Returns the size of the given file or directory. If the provided {@link Path} is a regular file, then the file's size is returned. If the argument is a
     * directory, then the size of the directory is calculated recursively.
     *
     * @param path the regular file or directory to return the size of (must not be {@code null}).
     * @return the length of the file, or recursive size of the directory, provided (in bytes).
     * @throws NullPointerException     if the file is {@code null}.
     * @throws IllegalArgumentException if the file does not exist.
     * @throws IOException              if an I/O error occurs.
     * @since 2.12.0
     */
    public static BigInteger sizeOfAsBigInteger(final Path path) throws IOException {
        requireExists(path, "path");
        return Files.isDirectory(path) ? sizeOfDirectoryAsBigInteger(path) : BigInteger.valueOf(Files.size(path));
    }

    /**
     * Counts the size of a directory recursively (sum of the size of all files).
     * <p>
     * Note that overflow is not detected, and the return value may be negative if overflow occurs. See {@link #sizeOfDirectoryAsBigInteger(Path)} for an
     * alternative method that does not overflow.
     * </p>
     *
     * @param directory directory to inspect, must not be {@code null}.
     * @return size of directory in bytes, 0 if directory is security restricted, a negative number when the real total is greater than {@link Long#MAX_VALUE}.
     * @throws NullPointerException if the directory is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.12.0
     */
    public static long sizeOfDirectory(final Path directory) throws IOException {
        return countDirectory(directory).getByteCounter().getLong();
    }

    /**
     * Counts the size of a directory recursively (sum of the size of all files).
     *
     * @param directory directory to inspect, must not be {@code null}.
     * @return size of directory in bytes, 0 if directory is security restricted.
     * @throws NullPointerException if the directory is {@code null}.
     * @throws IOException          if an I/O error occurs.
     * @since 2.12.0
     */
    public static BigInteger sizeOfDirectoryAsBigInteger(final Path directory) throws IOException {
        return countDirectoryAsBigInteger(directory).getByteCounter().getBigInteger();
    }

    /**
     * Converts an array of {@link FileVisitOption} to a {@link Set}.
     *
     * @param fileVisitOptions input array.
     * @return a new Set.
     */
    static Set<FileVisitOption> toFileVisitOptionSet(final FileVisitOption... fileVisitOptions) {
        return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class) : Stream.of(fileVisitOptions).collect(Collectors.toSet());
    }

    /**
     * Implements behavior similar to the UNIX "touch" utility. Creates a new file with size 0, or, if the file exists, just updates the file's modified time.
     * this method creates parent directories if they do not exist.
     *
     * @param file the file to touch.
     * @return The given file.
     * @throws NullPointerException if the parameter is {@code null}.
     * @throws IOException          if setting the last-modified time failed or an I/O problem occurs.\
     * @since 2.12.0
     */
    public static Path touch(final Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file)) {
            createParentDirectories(file);
            Files.createFile(file);
        } else {
            FileTimes.setLastModifiedTime(file);
        }
        return file;
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor   See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param directory See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param <T>       See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws NoSuchFileException  if the directory does not exist.
     * @throws IOException          if an I/O error is thrown by a visitor method.
     * @throws NullPointerException if the directory is {@code null}.
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path directory) throws IOException {
        Files.walkFileTree(directory, visitor);
        return visitor;
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param start    See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param options  See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param visitor  See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param <T>      See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path start, final Set<FileVisitOption> options,
            final int maxDepth) throws IOException {
        Files.walkFileTree(start, options, maxDepth, visitor);
        return visitor;
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param first   See {@link Paths#get(String,String[])}.
     * @param more    See {@link Paths#get(String,String[])}.
     * @param <T>     See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final String first, final String... more) throws IOException {
        return visitFileTree(visitor, Paths.get(first, more));
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param uri     See {@link Paths#get(URI)}.
     * @param <T>     See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final URI uri) throws IOException {
        return visitFileTree(visitor, Paths.get(uri));
    }

    /**
     * Waits for the file system to detect a file's presence, with a timeout.
     * <p>
     * This method repeatedly tests {@link Files#exists(Path,LinkOption...)} until it returns true up to the maximum time given.
     * </p>
     *
     * @param file    the file to check, must not be {@code null}.
     * @param timeout the maximum time to wait.
     * @param options options indicating how to handle symbolic links.
     * @return true if file exists.
     * @throws NullPointerException if the file is {@code null}.
     * @since 2.12.0
     */
    public static boolean waitFor(final Path file, final Duration timeout, final LinkOption... options) {
        Objects.requireNonNull(file, "file");
        final Instant finishInstant = Instant.now().plus(timeout);
        boolean interrupted = false;
        final long minSleepMillis = 100;
        try {
            while (!exists(file, options)) {
                final Instant now = Instant.now();
                if (now.isAfter(finishInstant)) {
                    return false;
                }
                try {
                    ThreadUtils.sleep(Duration.ofMillis(Math.min(minSleepMillis, finishInstant.minusMillis(now.toEpochMilli()).toEpochMilli())));
                } catch (final InterruptedException ignore) {
                    interrupted = true;
                } catch (final Exception ex) {
                    break;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return exists(file, options);
    }

    /**
     * Returns a stream of filtered paths.
     * <p>
     * The returned {@link Stream} may wrap one or more {@link DirectoryStream}s. When you require timely disposal of file system resources, use a
     * {@code try}-with-resources block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed. Calling a
     * closed stream causes a {@link IllegalStateException}.
     * </p>
     *
     * @param start          the start path
     * @param pathFilter     the path filter
     * @param maxDepth       the maximum depth of directories to walk.
     * @param readAttributes whether to call the filters with file attributes (false passes null).
     * @param options        the options to configure the walk.
     * @return a filtered stream of paths.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     * @since 2.9.0
     */
    @SuppressWarnings("resource") // Caller closes
    public static Stream<Path> walk(final Path start, final PathFilter pathFilter, final int maxDepth, final boolean readAttributes,
            final FileVisitOption... options) throws IOException {
        return Files.walk(start, maxDepth, options)
                .filter(path -> pathFilter.accept(path, readAttributes ? readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE);
    }

    private static <R> R withPosixFileAttributes(final Path path, final LinkOption[] linkOptions, final boolean overrideReadOnly,
            final IOFunction<PosixFileAttributes, R> function) throws IOException {
        final PosixFileAttributes posixFileAttributes = overrideReadOnly ? readPosixFileAttributes(path, linkOptions) : null;
        try {
            return function.apply(posixFileAttributes);
        } finally {
            if (posixFileAttributes != null && path != null && Files.exists(path, linkOptions)) {
                Files.setPosixFilePermissions(path, posixFileAttributes.permissions());
            }
        }
    }

    /**
     * Writes the given character sequence to a file at the given path.
     *
     * @param path         The target file.
     * @param charSequence The character sequence text.
     * @param charset      The Charset to encode the text.
     * @param openOptions  options How to open the file.
     * @return The given path.
     * @throws IOException          if an I/O error occurs writing to or creating the file.
     * @throws NullPointerException if either {@code path} or {@code charSequence} is {@code null}.
     * @since 2.12.0
     */
    public static Path writeString(final Path path, final CharSequence charSequence, final Charset charset, final OpenOption... openOptions)
            throws IOException {
        // Check the text is not null before opening file.
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(charSequence, "charSequence");
        Files.write(path, String.valueOf(charSequence).getBytes(Charsets.toCharset(charset)), openOptions);
        return path;
    }

    /**
     * Prevents instantiation.
     */
    private PathUtils() {
        // do not instantiate.
    }

}
