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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.apache.commons.io.function.Uncheck;

/**
 * Delegates to {@link Files} to uncheck calls by throwing {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @see Files
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public final class FilesUncheck {

    /**
     * Delegates to {@link Files#copy(InputStream, Path, CopyOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param in See delegate.
     * @param target See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     * @see Files#copy(InputStream, Path,CopyOption...)
     */
    public static long copy(final InputStream in, final Path target, final CopyOption... options) {
        return Uncheck.apply(Files::copy, in, target, options);
    }

    /**
     * Delegates to {@link Files#copy(Path, OutputStream)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param source See delegate. See delegate.
     * @param out See delegate. See delegate.
     * @return See delegate. See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static long copy(final Path source, final OutputStream out) {
        return Uncheck.apply(Files::copy, source, out);
    }

    /**
     * Delegates to {@link Files#copy(Path, Path, CopyOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param source See delegate.
     * @param target See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path copy(final Path source, final Path target, final CopyOption... options) {
        return Uncheck.apply(Files::copy, source, target, options);
    }

    /**
     * Delegates to {@link Files#createDirectories(Path, FileAttribute...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param dir See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createDirectories(final Path dir, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createDirectories, dir, attrs);
    }

    /**
     * Delegates to {@link Files#createDirectory(Path, FileAttribute...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param dir See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createDirectory(final Path dir, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createDirectory, dir, attrs);
    }

    /**
     * Delegates to {@link Files#createFile(Path, FileAttribute...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createFile(final Path path, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createFile, path, attrs);
    }

    /**
     * Delegates to {@link Files#createLink(Path, Path)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param link See delegate.
     * @param existing See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createLink(final Path link, final Path existing) {
        return Uncheck.apply(Files::createLink, link, existing);
    }

    /**
     * Delegates to {@link Files#createSymbolicLink(Path, Path, FileAttribute...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param link See delegate.
     * @param target See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createSymbolicLink(final Path link, final Path target, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createSymbolicLink, link, target, attrs);
    }

    /**
     * Delegates to {@link Files#createTempDirectory(Path, String, FileAttribute...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param dir See delegate.
     * @param prefix See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createTempDirectory(final Path dir, final String prefix, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createTempDirectory, dir, prefix, attrs);
    }

    /**
     * Delegates to {@link Files#createTempDirectory(String, FileAttribute...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param prefix See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createTempDirectory(final String prefix, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createTempDirectory, prefix, attrs);
    }

    /**
     * Delegates to {@link Files#createTempFile(Path, String, String, FileAttribute...)} throwing
     * {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param dir See delegate.
     * @param prefix See delegate.
     * @param suffix See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createTempFile(final Path dir, final String prefix, final String suffix, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createTempFile, dir, prefix, suffix, attrs);
    }

    /**
     * Delegates to {@link Files#createTempFile(String, String, FileAttribute...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param prefix See delegate.
     * @param suffix See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path createTempFile(final String prefix, final String suffix, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::createTempFile, prefix, suffix, attrs);
    }

    /**
     * Delegates to {@link Files#delete(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static void delete(final Path path) {
        Uncheck.accept(Files::delete, path);
    }

    /**
     * Delegates to {@link Files#deleteIfExists(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static boolean deleteIfExists(final Path path) {
        return Uncheck.apply(Files::deleteIfExists, path);
    }

    /**
     * Delegates to {@link Files#find(Path, int, BiPredicate, FileVisitOption...)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} wraps a {@link DirectoryStream}. When you require timely disposal of file system resources, use a {@code try}-with-resources
     * block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed.
     * </p>
     *
     * @param start    See delegate.
     * @param maxDepth See delegate.
     * @param matcher  See delegate.
     * @param options  See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     * @since 2.14.0
     */
    public static Stream<Path> find(final Path start, final int maxDepth, final BiPredicate<Path, BasicFileAttributes> matcher,
            final FileVisitOption... options) {
        return Uncheck.apply(Files::find, start, maxDepth, matcher, options);
    }

    /**
     * Delegates to {@link Files#getAttribute(Path, String, LinkOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param attribute See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Object getAttribute(final Path path, final String attribute, final LinkOption... options) {
        return Uncheck.apply(Files::getAttribute, path, attribute, options);
    }

    /**
     * Delegates to {@link Files#getFileStore(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static FileStore getFileStore(final Path path) {
        return Uncheck.apply(Files::getFileStore, path);
    }

    /**
     * Delegates to {@link Files#getLastModifiedTime(Path, LinkOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static FileTime getLastModifiedTime(final Path path, final LinkOption... options) {
        return Uncheck.apply(Files::getLastModifiedTime, path, options);
    }

    /**
     * Delegates to {@link Files#getOwner(Path, LinkOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static UserPrincipal getOwner(final Path path, final LinkOption... options) {
        return Uncheck.apply(Files::getOwner, path, options);
    }

    /**
     * Delegates to {@link Files#getPosixFilePermissions(Path, LinkOption...)} throwing {@link UncheckedIOException} instead
     * of {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Set<PosixFilePermission> getPosixFilePermissions(final Path path, final LinkOption... options) {
        return Uncheck.apply(Files::getPosixFilePermissions, path, options);
    }

    /**
     * Delegates to {@link Files#isHidden(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static boolean isHidden(final Path path) {
        return Uncheck.apply(Files::isHidden, path);
    }

    /**
     * Delegates to {@link Files#isSameFile(Path, Path)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param path2 See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static boolean isSameFile(final Path path, final Path path2) {
        return Uncheck.apply(Files::isSameFile, path, path2);
    }

    /**
     * Delegates to {@link Files#lines(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} wraps a {@link Reader}. When you require timely disposal of file system resources, use a {@code try}-with-resources block to
     * ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed.
     * </p>
     *
     * @param path See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Stream<String> lines(final Path path) {
        return Uncheck.apply(Files::lines, path);
    }

    /**
     * Delegates to {@link Files#lines(Path, Charset)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} wraps a {@link Reader}. When you require timely disposal of file system resources, use a {@code try}-with-resources block to
     * ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed.
     * </p>
     *
     * @param path See delegate.
     * @param cs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Stream<String> lines(final Path path, final Charset cs) {
        return Uncheck.apply(Files::lines, path, cs);
    }

    /**
     * Delegates to {@link Files#list(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} wraps a {@link DirectoryStream}. When you require timely disposal of file system resources, use a {@code try}-with-resources
     * block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed.
     * </p>
     *
     * @param dir See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Stream<Path> list(final Path dir) {
        return Uncheck.apply(Files::list, dir);
    }

    /**
     * Delegates to {@link Files#move(Path, Path, CopyOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param source See delegate.
     * @param target See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static Path move(final Path source, final Path target, final CopyOption... options) {
        return Uncheck.apply(Files::move, source, target, options);
    }

    /**
     * Delegates to {@link Files#newBufferedReader(Path)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static BufferedReader newBufferedReader(final Path path) {
        return Uncheck.apply(Files::newBufferedReader, path);
    }

    /**
     * Delegates to {@link Files#newBufferedReader(Path, Charset)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param cs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static BufferedReader newBufferedReader(final Path path, final Charset cs) {
        return Uncheck.apply(Files::newBufferedReader, path, cs);
    }

    /**
     * Delegates to {@link Files#newBufferedWriter(Path, Charset, OpenOption...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param path See delegate.
     * @param cs See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static BufferedWriter newBufferedWriter(final Path path, final Charset cs, final OpenOption... options) {
        return Uncheck.apply(Files::newBufferedWriter, path, cs, options);
    }

    /**
     * Delegates to {@link Files#newBufferedWriter(Path, OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static BufferedWriter newBufferedWriter(final Path path, final OpenOption... options) {
        return Uncheck.apply(Files::newBufferedWriter, path, options);
    }

    /**
     * Delegates to {@link Files#newByteChannel(Path, OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static SeekableByteChannel newByteChannel(final Path path, final OpenOption... options) {
        return Uncheck.apply(Files::newByteChannel, path, options);
    }

    /**
     * Delegates to {@link Files#newByteChannel(Path, Set, FileAttribute...)} throwing {@link UncheckedIOException} instead
     * of {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @param attrs See delegate.
     * @return See delegate.
     * @throws UncheckedIOException Wraps an {@link IOException}.
     */
    public static SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) {
        return Uncheck.apply(Files::newByteChannel, path, options, attrs);
    }

    /**
     * Delegates to {@link Files#newDirectoryStream(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * If you don't use the try-with-resources construct, then you must call the stream's {@link Stream#close()} method after iteration is complete to free any
     * resources held for the open directory.
     * </p>
     *
     * @param dir See delegate.
     * @return See delegate.
     */
    public static DirectoryStream<Path> newDirectoryStream(final Path dir) {
        return Uncheck.apply(Files::newDirectoryStream, dir);
    }

    /**
     * Delegates to {@link Files#newDirectoryStream(Path, java.nio.file.DirectoryStream.Filter)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     * <p>
     * If you don't use the try-with-resources construct, then you must call the stream's {@link Stream#close()} method after iteration is complete to free any
     * resources held for the open directory.
     * </p>
     *
     * @param dir    See delegate.
     * @param filter See delegate.
     * @return See delegate.
     */
    public static DirectoryStream<Path> newDirectoryStream(final Path dir, final DirectoryStream.Filter<? super Path> filter) {
        return Uncheck.apply(Files::newDirectoryStream, dir, filter);
    }

    /**
     * Delegates to {@link Files#newDirectoryStream(Path, String)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     * <p>
     * The returned {@link Stream} wraps a {@link DirectoryStream}. When you require timely disposal of file system resources, use a {@code try}-with-resources
     * block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed.
     * </p>
     *
     * @param dir See delegate.
     * @param glob See delegate.
     * @return See delegate.
     */
    public static DirectoryStream<Path> newDirectoryStream(final Path dir, final String glob) {
        return Uncheck.apply(Files::newDirectoryStream, dir, glob);
    }

    /**
     * Delegates to {@link Files#newInputStream(Path, OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static InputStream newInputStream(final Path path, final OpenOption... options) {
        return Uncheck.apply(Files::newInputStream, path, options);
    }

    /**
     * Delegates to {@link Files#newOutputStream(Path, OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static OutputStream newOutputStream(final Path path, final OpenOption... options) {
        return Uncheck.apply(Files::newOutputStream, path, options);
    }

    /**
     * Delegates to {@link Files#probeContentType(Path)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     */
    public static String probeContentType(final Path path) {
        return Uncheck.apply(Files::probeContentType, path);
    }

    /**
     * Delegates to {@link Files#readAllBytes(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     */
    public static byte[] readAllBytes(final Path path) {
        return Uncheck.apply(Files::readAllBytes, path);
    }

    /**
     * Delegates to {@link Files#readAllLines(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     */
    public static List<String> readAllLines(final Path path) {
        return Uncheck.apply(Files::readAllLines, path);
    }

    /**
     * Delegates to {@link Files#readAllLines(Path, Charset)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param cs See delegate.
     * @return See delegate.
     */
    public static List<String> readAllLines(final Path path, final Charset cs) {
        return Uncheck.apply(Files::readAllLines, path, cs);
    }

    /**
     * Delegates to {@link Files#readAttributes(Path, Class, LinkOption...)} throwing {@link UncheckedIOException} instead
     * of {@link IOException}.
     *
     * @param <A> See delegate.
     * @param path See delegate.
     * @param type See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options) {
        return Uncheck.apply(Files::readAttributes, path, type, options);
    }

    /**
     * Delegates to {@link Files#readAttributes(Path, String, LinkOption...)} throwing {@link UncheckedIOException} instead
     * of {@link IOException}.
     *
     * @param path See delegate.
     * @param attributes See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options) {
        return Uncheck.apply(Files::readAttributes, path, attributes, options);
    }

    /**
     * Delegates to {@link Files#readSymbolicLink(Path)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param link See delegate.
     * @return See delegate.
     */
    public static Path readSymbolicLink(final Path link) {
        return Uncheck.apply(Files::readSymbolicLink, link);
    }

    /**
     * Delegates to {@link Files#setAttribute(Path, String, Object, LinkOption...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param path See delegate.
     * @param attribute See delegate.
     * @param value See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Path setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options) {
        return Uncheck.apply(Files::setAttribute, path, attribute, value, options);
    }

    /**
     * Delegates to {@link Files#setLastModifiedTime(Path, FileTime)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param time See delegate.
     * @return See delegate.
     */
    public static Path setLastModifiedTime(final Path path, final FileTime time) {
        return Uncheck.apply(Files::setLastModifiedTime, path, time);
    }

    /**
     * Delegates to {@link Files#setOwner(Path, UserPrincipal)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param owner See delegate.
     * @return See delegate.
     */
    public static Path setOwner(final Path path, final UserPrincipal owner) {
        return Uncheck.apply(Files::setOwner, path, owner);
    }

    /**
     * Delegates to {@link Files#setPosixFilePermissions(Path, Set)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param perms See delegate.
     * @return See delegate.
     */
    public static Path setPosixFilePermissions(final Path path, final Set<PosixFilePermission> perms) {
        return Uncheck.apply(Files::setPosixFilePermissions, path, perms);
    }

    /**
     * Delegates to {@link Files#size(Path)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     *
     * @param path See delegate.
     * @return See delegate.
     */
    public static long size(final Path path) {
        return Uncheck.apply(Files::size, path);
    }

    /**
     * Delegates to {@link Files#walk(Path, FileVisitOption...)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} may wrap one or more {@link DirectoryStream}s. When you require timely disposal of file system resources, use a
     * {@code try}-with-resources block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed. Calling a
     * closed stream causes a {@link IllegalStateException}.
     * </p>
     *
     * @param start   See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Stream<Path> walk(final Path start, final FileVisitOption... options) {
        return Uncheck.apply(Files::walk, start, options);
    }

    /**
     * Delegates to {@link Files#walk(Path, int, FileVisitOption...)} throwing {@link UncheckedIOException} instead of {@link IOException}.
     * <p>
     * The returned {@link Stream} may wrap one or more {@link DirectoryStream}s. When you require timely disposal of file system resources, use a
     * {@code try}-with-resources block to ensure invocation of the stream's {@link Stream#close()} method after the stream operations are completed. Calling a
     * closed stream causes a {@link IllegalStateException}.
     * </p>
     *
     * @param start    See delegate.
     * @param maxDepth See delegate.
     * @param options  See delegate.
     * @return See delegate.
     */
    public static Stream<Path> walk(final Path start, final int maxDepth, final FileVisitOption... options) {
        return Uncheck.apply(Files::walk, start, maxDepth, options);
    }

    /**
     * Delegates to {@link Files#walkFileTree(Path, FileVisitor)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param start See delegate.
     * @param visitor See delegate.
     * @return See delegate.
     */
    public static Path walkFileTree(final Path start, final FileVisitor<? super Path> visitor) {
        return Uncheck.apply(Files::walkFileTree, start, visitor);
    }

    /**
     * Delegates to {@link Files#walkFileTree(Path, Set, int, FileVisitor)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param start See delegate.
     * @param options See delegate.
     * @param maxDepth See delegate.
     * @param visitor See delegate.
     * @return See delegate.
     */
    public static Path walkFileTree(final Path start, final Set<FileVisitOption> options, final int maxDepth, final FileVisitor<? super Path> visitor) {
        return Uncheck.apply(Files::walkFileTree, start, options, maxDepth, visitor);
    }

    /**
     * Delegates to {@link Files#write(Path, byte[], OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param bytes See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Path write(final Path path, final byte[] bytes, final OpenOption... options) {
        return Uncheck.apply(Files::write, path, bytes, options);
    }

    /**
     * Delegates to {@link Files#write(Path, Iterable, Charset, OpenOption...)} throwing {@link UncheckedIOException}
     * instead of {@link IOException}.
     *
     * @param path See delegate.
     * @param lines See delegate.
     * @param cs See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Path write(final Path path, final Iterable<? extends CharSequence> lines, final Charset cs, final OpenOption... options) {
        return Uncheck.apply(Files::write, path, lines, cs, options);
    }

    /**
     * Delegates to {@link Files#write(Path, Iterable, OpenOption...)} throwing {@link UncheckedIOException} instead of
     * {@link IOException}.
     *
     * @param path See delegate.
     * @param lines See delegate.
     * @param options See delegate.
     * @return See delegate.
     */
    public static Path write(final Path path, final Iterable<? extends CharSequence> lines, final OpenOption... options) {
        return Uncheck.apply(Files::write, path, lines, options);
    }

    /**
     * No instances.
     */
    private FilesUncheck() {
        // No instances
    }
}
