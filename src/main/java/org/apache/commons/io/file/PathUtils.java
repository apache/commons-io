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
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public final class PathUtils {

    /**
     * Counts aspects of a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @return The visitor used to count the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounts countDirectory(final Path directory) throws IOException {
        return visitFileTree(directory, new CountingPathFileVisitor()).getPathCounts();
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
    public static PathCounts delete(final Path path) throws IOException {
        return Files.isDirectory(path) ? deleteDirectory(path) : deleteFile(path);
    }

    /**
     * Deletes the given file.
     *
     * @param file The file to delete.
     * @return A visitor with path counts set to 1 file, 0 directories, and the size of the deleted file.
     * @throws IOException if an I/O error occurs.
     * @throws NotDirectoryException if the file is a directory.
     */
    public static PathCounts deleteFile(final Path file) throws IOException {
        if (Files.isDirectory(file)) {
            throw new NotDirectoryException(file.toString());
        }
        final PathCounts pathCounts = new PathCounts();
        final long size = Files.exists(file) ? Files.size(file) : 0;
        if (Files.deleteIfExists(file)) {
            pathCounts.fileCount.set(1);
            pathCounts.directoryCount.set(0);
            pathCounts.byteCount.set(size);
        }
        return pathCounts;
    }

    /**
     * Deletes a directory including sub-directories.
     *
     * @param directory directory to delete.
     * @return The visitor used to delete the given directory.
     * @throws IOException if an I/O error is thrown by a visitor method.
     */
    public static PathCounts deleteDirectory(final Path directory) throws IOException {
        return visitFileTree(directory, new DeletingPathFileVisitor()).getPathCounts();
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
     * @param directory See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param visitor See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @param <T> See {@link Files#walkFileTree(Path,FileVisitor)}.
     * @return the given visitor.
     *
     * @throws IOException if an I/O error is thrown by a visitor method
     */
    public static <T extends FileVisitor<? super Path>> T visitFileTree(final Path directory, final T visitor)
            throws IOException {
        Files.walkFileTree(directory, visitor);
        return visitor;
    }

    /**
     * Does allow to instantiate.
     */
    private PathUtils() {
        // do not instantiate.
    }

}
