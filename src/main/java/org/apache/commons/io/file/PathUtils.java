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
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.Counters.PathCounters;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public final class PathUtils {

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
     * Compares the contents of two Paths to determine if they are equal or not.
     * <p>
     * File content is accessed through {@link Files#newInputStream(Path,OpenOption...)}.
     * </p>
     *
     * @param path1 the first stream.
     * @param path2 the second stream.
     * @param options options specifying how the files are opened.
     * @return true if the content of the streams are equal or they both don't exist, false otherwise.
     * @throws NullPointerException if either input is null.
     * @throws IOException if an I/O error occurs.
     * @see org.apache.commons.io.FileUtils#contentEquals(java.io.File, java.io.File)
     */
    public static boolean fileContentEquals(final Path path1, final Path path2, final OpenOption... options) throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null ^ path2 == null) {
            return false;
        }
        final Path nPath1 = path1.normalize();
        final Path nPath2 = path2.normalize();
        final boolean path1Exists = Files.exists(nPath1);
        if (path1Exists != Files.exists(nPath2)) {
            return false;
        }
        if (!path1Exists) {
            // Two not existing files are equal?
            // Same as FileUtils
            return true;
        }
        if (Files.isDirectory(nPath1)) {
            // don't compare directory contents.
            throw new IOException("Can't compare directories, only files: " + nPath1);
        }
        if (Files.isDirectory(nPath2)) {
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
        try (final InputStream inputStream1 = Files.newInputStream(nPath1, options);
                final InputStream inputStream2 = Files.newInputStream(nPath2, options)) {
            return IOUtils.contentEquals(inputStream1, inputStream2);
        }
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
     * Copies a URL to a directory.
     *
     * @param sourceFile The source URL.
     * @param targetFile The target file.
     * @param copyOptions Specifies how the copying should be done.
     * @return The target file
     * @throws IOException if an I/O error occurs
     * @see Files#copy(InputStream, Path, CopyOption...)
     */
    public static Path copyFile(final URL sourceFile, final Path targetFile,
            final CopyOption... copyOptions) throws IOException {
        try (final InputStream inputStream = sourceFile.openStream()) {
            Files.copy(inputStream, targetFile, copyOptions);
            return targetFile;
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
     * Performs {@link Files#walkFileTree(Path,FileVisitor)} and returns the given visitor.
     *
     * Note that {@link Files#walkFileTree(Path,FileVisitor)} returns the given path.
     *
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param directory See {@link Files#walkFileTree(Path,FileVisitor)}.
     *
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
