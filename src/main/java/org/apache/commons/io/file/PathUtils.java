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
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
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
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public final class PathUtils {

    /**
     * Private worker/holder that computes and tracks relative path names and their equality. We reuse the sorted
     * relative lists when comparing directories.
     */
    private static class RelativeSortedPaths {

        final boolean equals;
        // final List<Path> relativeDirList1; // might need later?
        // final List<Path> relativeDirList2; // might need later?
        final List<Path> relativeFileList1;
        final List<Path> relativeFileList2;

        /**
         * Constructs and initializes a new instance by accumulating directory and file info.
         *
         * @param dir1 First directory to compare.
         * @param dir2 Seconds directory to compare.
         * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
         * @param linkOptions Options indicating how symbolic links are handled.
         * @param fileVisitOptions See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
         * @throws IOException if an I/O error is thrown by a visitor method.
         */
        private RelativeSortedPaths(final Path dir1, final Path dir2, final int maxDepth,
            final LinkOption[] linkOptions, final FileVisitOption[] fileVisitOptions) throws IOException {
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
                    if (visitor1.getDirList().size() != visitor2.getDirList().size()
                        || visitor1.getFileList().size() != visitor2.getFileList().size()) {
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

    /**
     * Empty {@link CopyOption} array.
     *
     * @since 2.8.0
     */
    public static final CopyOption[] EMPTY_COPY_OPTIONS = {};

    /**
     * Empty {@link LinkOption} array.
     *
     * @since 2.8.0
     */
    public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = {};

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
     */
    public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY = {LinkOption.NOFOLLOW_LINKS};

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
     * @param directory The directory to accumulate information.
     * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param fileVisitOptions See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @return file tree information.
     */
    private static AccumulatorPathVisitor accumulate(final Path directory, final int maxDepth,
        final FileVisitOption[] fileVisitOptions) throws IOException {
        return visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory,
            toFileVisitOptionSet(fileVisitOptions), maxDepth);
    }

    /**
     * Cleans a directory including sub-directories without deleting directories.
     *
     * @param directory directory to clean.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters cleanDirectory(final Path directory) throws IOException {
        return cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Cleans a directory including sub-directories without deleting directories.
     *
     * @param directory directory to clean.
     * @param deleteOptions How to handle deletion.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.8.0
     */
    public static PathCounters cleanDirectory(final Path directory, final DeleteOption... deleteOptions)
        throws IOException {
        return visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions), directory)
            .getPathCounters();
    }

    /**
     * Copies a directory to another directory.
     *
     * @param sourceDirectory The source directory.
     * @param targetDirectory The target directory.
     * @param copyOptions Specifies how the copying should be done.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters copyDirectory(final Path sourceDirectory, final Path targetDirectory,
        final CopyOption... copyOptions) throws IOException {
        final Path absoluteSource = sourceDirectory.toAbsolutePath();
        return visitFileTree(
            new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions),
            absoluteSource).getPathCounters();
    }

    /**
     * Copies a URL to a directory.
     *
     * @param sourceFile The source URL.
     * @param targetFile The target file.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(InputStream, Path, CopyOption...)
     */
    public static Path copyFile(final URL sourceFile, final Path targetFile, final CopyOption... copyOptions)
        throws IOException {
        try (final InputStream inputStream = sourceFile.openStream()) {
            Files.copy(inputStream, targetFile, copyOptions);
            return targetFile;
        }
    }

    /**
     * Copies a file to a directory.
     *
     * @param sourceFile The source file.
     * @param targetDirectory The target directory.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(Path, Path, CopyOption...)
     */
    public static Path copyFileToDirectory(final Path sourceFile, final Path targetDirectory,
        final CopyOption... copyOptions) throws IOException {
        return Files.copy(sourceFile, targetDirectory.resolve(sourceFile.getFileName()), copyOptions);
    }

    /**
     * Copies a URL to a directory.
     *
     * @param sourceFile The source URL.
     * @param targetDirectory The target directory.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs.
     * @see Files#copy(InputStream, Path, CopyOption...)
     */
    public static Path copyFileToDirectory(final URL sourceFile, final Path targetDirectory,
        final CopyOption... copyOptions) throws IOException {
        try (final InputStream inputStream = sourceFile.openStream()) {
            Files.copy(inputStream, targetDirectory.resolve(sourceFile.getFile()), copyOptions);
            return targetDirectory;
        }
    }

    /**
     * Counts aspects of a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @return The visitor used to count the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters countDirectory(final Path directory) throws IOException {
        return visitFileTree(new CountingPathVisitor(Counters.longPathCounters()), directory).getPathCounters();
    }

    /**
     * Creates the parent directories for the given {@code path}.
     *
     * @param path The path to a file (or directory).
     * @param attrs An optional list of file attributes to set atomically when creating the directories.
     * @return The Path for the {@code path}'s parent directory or null if the given path has no parent.
     * @throws IOException if an I/O error occurs.
     * @since 2.9.0
     */
    public static Path createParentDirectories(final Path path, final FileAttribute<?>... attrs) throws IOException {
        final Path parent = path.getParent();
        if (parent == null) {
            return null;
        }
        return Files.createDirectories(parent, attrs);
    }

    /**
     * Gets the current directory.
     *
     * @return the current directory.
     *
     * @since 2.9.0
     */
    public static Path current() {
        return Paths.get("");
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link java.io.File#delete()} returns a
     * boolean.
     * </ul>
     *
     * @param path file or directory to delete, must not be {@code null}
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException if an I/O error is thrown by a visitor method or if an I/O error occurs.
     */
    public static PathCounters delete(final Path path) throws IOException {
        return delete(path, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link java.io.File#delete()} returns a
     * boolean.
     * </ul>
     *
     * @param path file or directory to delete, must not be {@code null}
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException if an I/O error is thrown by a visitor method or if an I/O error occurs.
     * @since 2.8.0
     */
    public static PathCounters delete(final Path path, final DeleteOption... deleteOptions) throws IOException {
        // File deletion through Files deletes links, not targets, so use LinkOption.NOFOLLOW_LINKS.
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? deleteDirectory(path, deleteOptions)
            : deleteFile(path, deleteOptions);
    }

    /**
     * Deletes a file or directory. If the path is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * </p>
     * <ul>
     * <li>A directory to delete does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted; {@link java.io.File#delete()} returns a
     * boolean.
     * </ul>
     *
     * @param path file or directory to delete, must not be {@code null}
     * @param linkOptions How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws NullPointerException if the directory is {@code null}
     * @throws IOException if an I/O error is thrown by a visitor method or if an I/O error occurs.
     * @since 2.9.0
     */
    public static PathCounters delete(final Path path, final LinkOption[] linkOptions,
        final DeleteOption... deleteOptions) throws IOException {
        // File deletion through Files deletes links, not targets, so use LinkOption.NOFOLLOW_LINKS.
        return Files.isDirectory(path, linkOptions) ? deleteDirectory(path, linkOptions, deleteOptions)
            : deleteFile(path, linkOptions, deleteOptions);
    }

    /**
     * Deletes a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters deleteDirectory(final Path directory) throws IOException {
        return deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.8.0
     */
    public static PathCounters deleteDirectory(final Path directory, final DeleteOption... deleteOptions)
        throws IOException {
        return visitFileTree(
            new DeletingPathVisitor(Counters.longPathCounters(), PathUtils.NOFOLLOW_LINK_OPTION_ARRAY, deleteOptions),
            directory).getPathCounters();
    }

    /**
     * Deletes a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @param linkOptions How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @since 2.9.0
     */
    public static PathCounters deleteDirectory(final Path directory, final LinkOption[] linkOptions,
        final DeleteOption... deleteOptions) throws IOException {
        return visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions),
            directory).getPathCounters();
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     */
    public static PathCounters deleteFile(final Path file) throws IOException {
        return deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @param deleteOptions How to handle deletion.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     * @since 2.8.0
     */
    public static PathCounters deleteFile(final Path file, final DeleteOption... deleteOptions) throws IOException {
        // Files.deleteIfExists() never follows links, so use LinkOption.NOFOLLOW_LINKS in other calls to Files.
        return deleteFile(file, NOFOLLOW_LINK_OPTION_ARRAY, deleteOptions);
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @param linkOptions How to handle symbolic links.
     * @param deleteOptions How to handle deletion.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchFileException if the file is a directory.
     * @since 2.9.0
     */
    public static PathCounters deleteFile(final Path file, final LinkOption[] linkOptions,
        final DeleteOption... deleteOptions) throws NoSuchFileException, IOException {
        if (Files.isDirectory(file, linkOptions)) {
            throw new NoSuchFileException(file.toString());
        }
        final PathCounters pathCounts = Counters.longPathCounters();
        final boolean exists = Files.exists(file, linkOptions);
        final long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0;
        if (overrideReadOnly(deleteOptions) && exists) {
            setReadOnly(file, false, linkOptions);
        }
        if (Files.deleteIfExists(file)) {
            pathCounts.getFileCounter().increment();
            pathCounts.getByteCounter().add(size);
        }
        return pathCounts;
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not while considering file contents. The
     * comparison includes all files in all sub-directories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @return Whether the two directories contain the same files while considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static boolean directoryAndFileContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY,
            EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not while considering file contents. The
     * comparison includes all files in all sub-directories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @param linkOptions options to follow links.
     * @param openOptions options to open files.
     * @param fileVisitOption options to configure traversal.
     * @return Whether the two directories contain the same files while considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static boolean directoryAndFileContentEquals(final Path path1, final Path path2,
        final LinkOption[] linkOptions, final OpenOption[] openOptions, final FileVisitOption[] fileVisitOption)
        throws IOException {
        // First walk both file trees and gather normalized paths.
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        if (Files.notExists(path1) && Files.notExists(path2)) {
            return true;
        }
        final RelativeSortedPaths relativeSortedPaths = new RelativeSortedPaths(path1, path2, Integer.MAX_VALUE,
            linkOptions, fileVisitOption);
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
     * Compares the file sets of two Paths to determine if they are equal or not without considering file contents. The
     * comparison includes all files in all sub-directories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @return Whether the two directories contain the same files without considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static boolean directoryContentEquals(final Path path1, final Path path2) throws IOException {
        return directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY,
            EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    /**
     * Compares the file sets of two Paths to determine if they are equal or not without considering file contents. The
     * comparison includes all files in all sub-directories.
     *
     * @param path1 The first directory.
     * @param path2 The second directory.
     * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param linkOptions options to follow links.
     * @param fileVisitOptions options to configure the traversal
     * @return Whether the two directories contain the same files without considering file contents.
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static boolean directoryContentEquals(final Path path1, final Path path2, final int maxDepth,
        final LinkOption[] linkOptions, final FileVisitOption[] fileVisitOptions) throws IOException {
        return new RelativeSortedPaths(path1, path2, maxDepth, linkOptions, fileVisitOptions).equals;
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
     * @throws IOException if an I/O error occurs.
     * @see org.apache.commons.io.FileUtils#contentEquals(java.io.File, java.io.File)
     */
    public static boolean fileContentEquals(final Path path1, final Path path2) throws IOException {
        return fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
    }

    /**
     * Compares the file contents of two Paths to determine if they are equal or not.
     * <p>
     * File content is accessed through {@link Files#newInputStream(Path,OpenOption...)}.
     * </p>
     *
     * @param path1 the first stream.
     * @param path2 the second stream.
     * @param linkOptions options specifying how files are followed.
     * @param openOptions options specifying how files are opened.
     * @return true if the content of the streams are equal or they both don't exist, false otherwise.
     * @throws NullPointerException if either input is null.
     * @throws IOException if an I/O error occurs.
     * @see org.apache.commons.io.FileUtils#contentEquals(java.io.File, java.io.File)
     */
    public static boolean fileContentEquals(final Path path1, final Path path2, final LinkOption[] linkOptions,
        final OpenOption[] openOptions) throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        final Path nPath1 = path1.normalize();
        final Path nPath2 = path2.normalize();
        final boolean path1Exists = Files.exists(nPath1, linkOptions);
        if (path1Exists != Files.exists(nPath2, linkOptions)) {
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
        try (final InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);
            final InputStream inputStream2 = Files.newInputStream(nPath2, openOptions)) {
            return IOUtils.contentEquals(inputStream1, inputStream2);
        }
    }

    /**
     * <p>
     * Applies an {@link IOFileFilter} to the provided {@link File} objects. The resulting array is a subset of the
     * original file list that matches the provided filter.
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
     * @param paths the array of files to apply the filter to.
     *
     * @return a subset of {@code files} that is accepted by the file filter.
     * @throws IllegalArgumentException if the filter is {@code null} or {@code files} contains a {@code null}
     *         value.
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

    private static <R, A> R filterPaths(final PathFilter filter, final Stream<Path> stream,
        final Collector<? super Path, A, R> collector) {
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
     * @return a file attribute view of the specified type, or null if the attribute view type is not available.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static List<AclEntry> getAclEntryList(final Path sourcePath) throws IOException {
        final AclFileAttributeView fileAttributeView = Files.getFileAttributeView(sourcePath,
            AclFileAttributeView.class);
        return fileAttributeView == null ? null : fileAttributeView.getAcl();
    }

    /**
     * Tests whether the specified {@code Path} is a directory or not. Implemented as a
     * null-safe delegate to {@code Files.isDirectory(Path path, LinkOption... options)}.
     *
     * @param   path the path to the file.
     * @param   options options indicating how symbolic links are handled
     * @return  {@code true} if the file is a directory; {@code false} if
     *          the path is null, the file does not exist, is not a directory, or it cannot
     *          be determined if the file is a directory or not.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the
     *                               {@link SecurityManager#checkRead(String) checkRead} method is invoked to check read
     *                               access to the directory.
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
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not a directory
     *                               <i>(optional specific exception)</i>.
     * @throws IOException           if an I/O error occurs.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the
     *                               {@link SecurityManager#checkRead(String) checkRead} method is invoked to check read
     *                               access to the directory.
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
     * @throws SecurityException In the case of the default provider, and a security manager is installed, its
     *                           {@link SecurityManager#checkRead(String) checkRead} method denies read access to the
     *                           file.
     */
    public static boolean isEmptyFile(final Path file) throws IOException {
        return Files.size(file) <= 0;
    }

    /**
     * Tests if the specified {@code Path} is newer than the specified time reference.
     *
     * @param file the {@code Path} of which the modification date must be compared
     * @param timeMillis the time reference measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @param options options indicating how symbolic links are handled * @return true if the {@code Path} exists and
     *        has been modified after the given time reference.
     * @return true if the {@code Path} exists and has been modified after the given time reference.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException if the file is {@code null}
     * @since 2.9.0
     */
    public static boolean isNewer(final Path file, final long timeMillis, final LinkOption... options)
        throws IOException {
        Objects.requireNonNull(file, "file");
        if (Files.notExists(file)) {
            return false;
        }
        return Files.getLastModifiedTime(file, options).toMillis() > timeMillis;
    }

    /**
     * Tests whether the specified {@code Path} is a regular file or not. Implemented as a
     * null-safe delegate to {@code Files.isRegularFile(Path path, LinkOption... options)}.
     *
     * @param   path the path to the file.
     * @param   options options indicating how symbolic links are handled
     * @return  {@code true} if the file is a regular file; {@code false} if
     *          the path is null, the file does not exist, is not a directory, or it cannot
     *          be determined if the file is a regular file or not.
     * @throws SecurityException     In the case of the default provider, and a security manager is installed, the
     *                               {@link SecurityManager#checkRead(String) checkRead} method is invoked to check read
     *                               access to the directory.
     * @since 2.9.0
     */
    public static boolean isRegularFile(final Path path, final LinkOption... options) {
        return path != null && Files.isRegularFile(path, options);
    }

    /**
     * Creates a new DirectoryStream for Paths rooted at the given directory.
     *
     * @param dir the path to the directory to stream.
     * @param pathFilter the directory stream filter.
     * @return a new instance.
     * @throws IOException if an I/O error occurs.
     */
    public static DirectoryStream<Path> newDirectoryStream(final Path dir, final PathFilter pathFilter)
        throws IOException {
        return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
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
     * Shorthand for {@code Files.readAttributes(path, BasicFileAttributes.class)}
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
     * Shorthand for {@code Files.readAttributes(path, BasicFileAttributes.class)} while wrapping {@link IOException}
     * as {@link UncheckedIOException}.
     *
     * @param path the path to read.
     * @return the path attributes.
     * @throws UncheckedIOException if an I/O error occurs
     * @since 2.9.0
     */
    public static BasicFileAttributes readBasicFileAttributesUnchecked(final Path path) {
        try {
            return readBasicFileAttributes(path);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Relativizes all files in the given {@code collection} against a {@code parent}.
     *
     * @param collection The collection of paths to relativize.
     * @param parent relativizes against this parent path.
     * @param sort Whether to sort the result.
     * @param comparator How to sort.
     * @return A collection of relativized paths, optionally sorted.
     */
    static List<Path> relativize(final Collection<Path> collection, final Path parent, final boolean sort,
        final Comparator<? super Path> comparator) {
        Stream<Path> stream = collection.stream().map(parent::relativize);
        if (sort) {
            stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * Sets the given Path to the {@code readOnly} value.
     * <p>
     * This behavior is OS dependent.
     * </p>
     *
     * @param path The path to set.
     * @param readOnly true for read-only, false for not read-only.
     * @param linkOptions options indicating how symbolic links are handled.
     * @return The given path.
     * @throws IOException if an I/O error occurs.
     * @since 2.8.0
     */
    public static Path setReadOnly(final Path path, final boolean readOnly, final LinkOption... linkOptions)
        throws IOException {
        final List<Exception> causeList = new ArrayList<>(2);
        final DosFileAttributeView fileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class,
            linkOptions);
        if (fileAttributeView != null) {
            try {
                fileAttributeView.setReadOnly(readOnly);
                return path;
            } catch (final IOException e) {
                // ignore for now, retry with PosixFileAttributeView
                causeList.add(e);
            }
        }
        final PosixFileAttributeView posixFileAttributeView = Files.getFileAttributeView(path,
            PosixFileAttributeView.class, linkOptions);
        if (posixFileAttributeView != null) {
            // Works on Windows but not on Ubuntu:
            // Files.setAttribute(path, "unix:readonly", readOnly, options);
            // java.lang.IllegalArgumentException: 'unix:readonly' not recognized
            final PosixFileAttributes readAttributes = posixFileAttributeView.readAttributes();
            final Set<PosixFilePermission> permissions = readAttributes.permissions();
            permissions.remove(PosixFilePermission.OWNER_WRITE);
            permissions.remove(PosixFilePermission.GROUP_WRITE);
            permissions.remove(PosixFilePermission.OTHERS_WRITE);
            try {
                return Files.setPosixFilePermissions(path, permissions);
            } catch (final IOException e) {
                causeList.add(e);
            }
        }
        if (!causeList.isEmpty()) {
            throw new IOExceptionList(path.toString(), causeList);
        }
        throw new IOException(
            String.format("No DosFileAttributeView or PosixFileAttributeView for '%s' (linkOptions=%s)", path,
                Arrays.toString(linkOptions)));
    }

    /**
     * Converts an array of {@link FileVisitOption} to a {@link Set}.
     *
     * @param fileVisitOptions input array.
     * @return a new Set.
     */
    static Set<FileVisitOption> toFileVisitOptionSet(final FileVisitOption... fileVisitOptions) {
        return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class)
            : Stream.of(fileVisitOptions).collect(Collectors.toSet());
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param directory See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param <T> See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path directory)
        throws IOException {
        Files.walkFileTree(directory, visitor);
        return visitor;
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param start See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param options See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param visitor See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param <T> See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final Path start,
        final Set<FileVisitOption> options, final int maxDepth) throws IOException {
        Files.walkFileTree(start, options, maxDepth, visitor);
        return visitor;
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param first See {@link Paths#get(String,String[])}.
     * @param more See {@link Paths#get(String,String[])}.
     * @param <T> See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final String first,
        final String... more) throws IOException {
        return visitFileTree(visitor, Paths.get(first, more));
    }

    /**
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param uri See {@link Paths#get(URI)}.
     * @param <T> See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final T visitor, final URI uri)
        throws IOException {
        return visitFileTree(visitor, Paths.get(uri));
    }

    /**
     * Returns a stream of filtered paths.
     *
     * @param start the start path
     * @param pathFilter the path filter
     * @param maxDepth the maximum depth of directories to walk.
     * @param readAttributes whether to call the filters with file attributes (false passes null).
     * @param options the options to configure the walk.
     * @return a filtered stream of paths.
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     * @since 2.9.0
     */
    public static Stream<Path> walk(final Path start, final PathFilter pathFilter, final int maxDepth,
        final boolean readAttributes, final FileVisitOption... options) throws IOException {
        return Files.walk(start, maxDepth, options).filter(path -> pathFilter.accept(path,
            readAttributes ? readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE);
    }

    /**
     * Does allow to instantiate.
     */
    private PathUtils() {
        // do not instantiate.
    }

}
