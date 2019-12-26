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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.Counters.PathCounters;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public final class PathUtils {

    /**
     * Accumulates file tree information in a {@link AccumulatorPathVisitor}.
     * 
     * @param directory The directory to accumulate information.
     * @param maxDepth See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @param linkOptions Options indicating how symbolic links are handled.
     * @param fileVisitOptions See {@link Files#walkFileTree(Path,Set,int,FileVisitor)}.
     * @throws IOException if an I/O error is thrown by a visitor method.
     * @return file tree information.
     */
    private static AccumulatorPathVisitor accumulate(final Path directory, final int maxDepth,
            final LinkOption[] linkOptions, final FileVisitOption[] fileVisitOptions) throws IOException {
        return visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory,
                toFileVisitOptionSet(fileVisitOptions), maxDepth);
    }

    /**
     * Private worker/holder that computes and tracks relative path names and their equality. We reuse the sorted
     * relative lists when comparing directories.
     */
    private static class RelativeSortedPaths {

        final boolean equals;
        final List<Path> relativeDirList1; // might need later?
        final List<Path> relativeDirList2; // might need later?
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
            List<Path> tmpRelativeDirList1 = null;
            List<Path> tmpRelativeDirList2 = null;
            List<Path> tmpRelativeFileList1 = null;
            List<Path> tmpRelativeFileList2 = null;
            if (dir1 == null && dir2 == null) {
                equals = true;
            } else if (dir1 == null ^ dir2 == null) {
                equals = false;
            } else {
                final boolean parentDirExists1 = Files.exists(dir1, linkOptions);
                final boolean parentDirExists2 = Files.exists(dir2, linkOptions);
                if (!parentDirExists1 || !parentDirExists2) {
                    equals = !parentDirExists1 && !parentDirExists2;
                } else {
                    AccumulatorPathVisitor visitor1 = accumulate(dir1, maxDepth, linkOptions, fileVisitOptions);
                    AccumulatorPathVisitor visitor2 = accumulate(dir2, maxDepth, linkOptions, fileVisitOptions);
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
            relativeDirList1 = tmpRelativeDirList1;
            relativeDirList2 = tmpRelativeDirList2;
            relativeFileList1 = tmpRelativeFileList1;
            relativeFileList2 = tmpRelativeFileList2;
        }
    }

    /**
     * Empty {@link FileVisitOption} array.
     */
    public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = new FileVisitOption[0];

    /**
     * Empty {@link LinkOption} array.
     */
    public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = new LinkOption[0];

    /**
     * Empty {@link OpenOption} array.
     */
    public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = new OpenOption[0];

    /**
     * Cleans a directory including sub-directories without deleting directories.
     *
     * @param directory directory to clean.
     * @return The visitation path counters.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters cleanDirectory(final Path directory) throws IOException {
        return visitFileTree(CleaningPathVisitor.withLongCounters(), directory).getPathCounters();
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
        return visitFileTree(
                new CopyDirectoryVisitor(Counters.longPathCounters(), sourceDirectory, targetDirectory, copyOptions),
                sourceDirectory).getPathCounters();
    }

    /**
     * Copies a URL to a directory.
     *
     * @param sourceFile The source URL.
     * @param targetFile The target file.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs
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
     * @throws IOException if an I/O error occurs
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
     * @throws IOException if an I/O error occurs
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
        return Files.isDirectory(path) ? deleteDirectory(path) : deleteFile(path);
    }

    /**
     * Deletes a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounters deleteDirectory(final Path directory) throws IOException {
        return visitFileTree(DeletingPathVisitor.withLongCounters(), directory).getPathCounters();
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException if an I/O error occurs.
     * @throws NotDirectoryException if the file is a directory.
     */
    public static PathCounters deleteFile(final Path file) throws IOException {
        if (Files.isDirectory(file)) {
            throw new NotDirectoryException(file.toString());
        }
        final PathCounters pathCounts = Counters.longPathCounters();
        final long size = Files.exists(file) ? Files.size(file) : 0;
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
        if (path1 == null ^ path2 == null) {
            return false;
        }
        if (!Files.exists(path1) && !Files.exists(path2)) {
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
        for (Path path : fileList1) {
            final int binarySearch = Collections.binarySearch(fileList2, path);
            if (binarySearch > -1) {
                if (!fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions)) {
                    return false;
                }
            } else {
                throw new IllegalStateException(String.format("Unexpected mismatch."));
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
            LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
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
        if (path1 == null ^ path2 == null) {
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
     * Returns whether the given file or directory is empty.
     *
     * @param path the the given file or directory to query.
     * @return whether the given file or directory is empty.
     * @throws IOException if an I/O error occurs
     */
    public static boolean isEmpty(final Path path) throws IOException {
        return Files.isDirectory(path) ? isEmptyDirectory(path) : isEmptyFile(path);
    }

    /**
     * Returns whether the directory is empty.
     *
     * @param directory the the given directory to query.
     * @return whether the given directory is empty.
     * @throws IOException if an I/O error occurs
     */
    public static boolean isEmptyDirectory(final Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            if (directoryStream.iterator().hasNext()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given file is empty.
     *
     * @param file the the given file to query.
     * @return whether the given file is empty.
     * @throws IOException if an I/O error occurs
     */
    public static boolean isEmptyFile(final Path file) throws IOException {
        return Files.size(file) <= 0;
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
    static List<Path> relativize(Collection<Path> collection, Path parent, boolean sort,
            Comparator<? super Path> comparator) {
        Stream<Path> stream = collection.stream().map(e -> parent.relativize(e));
        if (sort) {
            stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
        }
        return stream.collect(Collectors.toList());
    }

    /**
     * Converts an array of {@link FileVisitOption} to a {@link Set}.
     * 
     * @param fileVisitOptions input array.
     * @return a new Set.
     */
    static Set<FileVisitOption> toFileVisitOptionSet(final FileVisitOption... fileVisitOptions) {
        return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class)
                : Arrays.stream(fileVisitOptions).collect(Collectors.toSet());
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
    public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, Path start,
            Set<FileVisitOption> options, int maxDepth) throws IOException {
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
     * Does allow to instantiate.
     */
    private PathUtils() {
        // do not instantiate.
    }

}
