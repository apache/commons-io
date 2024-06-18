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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * General File System utilities.
 * <p>
 * This class provides static utility methods for general file system functions not provided before Java 6's {@link File File} class.
 * </p>
 * <p>
 * The current functions provided are:
 * </p>
 * <ul>
 * <li>Get the free space on a drive</li>
 * </ul>
 *
 * @since 1.1
 * @deprecated As of 2.6 deprecated without replacement. Use equivalent methods in {@link java.nio.file.FileStore} instead,
 *             {@code Files.getFileStore(Paths.get("/home")).getUsableSpace()} or iterate over {@code FileSystems.getDefault().getFileStores()}
 */
@Deprecated
public class FileSystemUtils {

    /**
     * Gets the number of kibibytes (1024 bytes) available to this Java virtual machine on the given file store.
     * <p>
     * Note that some OS's are NOT currently supported, including OS/390, OpenVMS.
     * </p>
     *
     * <pre>
     * FileSystemUtils.freeSpace("C:"); // Windows
     * FileSystemUtils.freeSpace("/volume"); // *nix
     * </pre>
     *
     * @param path the path to get free space for, not null, not empty on UNIX
     * @return the amount of free drive space on the drive or volume
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     * @since 1.1, enhanced OS support in 1.2 and 1.3
     * @deprecated Use freeSpaceKb(String) Deprecated from 1.3, may be removed in 2.0
     */
    @Deprecated
    public static long freeSpace(final String path) throws IOException {
        return getFreeSpace(path);
    }

    /**
     * Gets the number of kibibytes (1024 bytes) available to this Java virtual machine on the current file store.
     * <p>
     * Identical to:
     * </p>
     *
     * <pre>
     * freeSpaceKb(FileUtils.current().getAbsolutePath())
     * </pre>
     *
     * @return the amount of free drive space on the drive or volume in kilobytes
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     * @since 2.0
     * @deprecated As of 2.6 deprecated without replacement. Please use {@link java.nio.file.FileStore#getUsableSpace()}.
     */
    @Deprecated
    public static long freeSpaceKb() throws IOException {
        return freeSpaceKb(-1);
    }

    /**
     * Gets the number of kibibytes (1024 bytes) available to this Java virtual machine on the current file store.
     * <p>
     * Identical to:
     * </p>
     *
     * <pre>
     * freeSpaceKb(FileUtils.current().getAbsolutePath())
     * </pre>
     *
     * @param timeout ignored.
     * @return the amount of free drive space on the drive or volume in kilobytes
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     * @since 2.0
     * @deprecated As of 2.6 deprecated without replacement. Please use {@link java.nio.file.FileStore#getUsableSpace()}.
     */
    @Deprecated
    public static long freeSpaceKb(final long timeout) throws IOException {
        return freeSpaceKb(FileUtils.current().getAbsolutePath(), timeout);
    }

    /**
     * Gets the number of kibibytes (1024 bytes) available to this Java virtual machine on the given file store.
     *
     * <pre>
     * FileSystemUtils.freeSpaceKb("C:"); // Windows
     * FileSystemUtils.freeSpaceKb("/volume"); // *nix
     * </pre>
     *
     * @param path the path to get free space for, not null, not empty on UNIX
     * @return the amount of free drive space on the drive or volume in kilobytes
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     * @since 1.2, enhanced OS support in 1.3
     * @deprecated As of 2.6 deprecated without replacement. Please use {@link java.nio.file.FileStore#getUsableSpace()}.
     */
    @Deprecated
    public static long freeSpaceKb(final String path) throws IOException {
        return freeSpaceKb(path, -1);
    }

    /**
     * Gets the number of kibibytes (1024 bytes) available to this Java virtual machine on the given file store.
     *
     * <pre>
     * FileSystemUtils.freeSpaceKb("C:"); // Windows
     * FileSystemUtils.freeSpaceKb("/volume"); // *nix
     * </pre>
     *
     * @param path    the path to get free space for, not null, not empty on UNIX
     * @param timeout ignored.
     * @return the amount of free drive space on the drive or volume in kilobytes
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     * @since 2.0
     * @deprecated As of 2.6 deprecated without replacement. Please use {@link java.nio.file.FileStore#getUsableSpace()}.
     */
    @Deprecated
    public static long freeSpaceKb(final String path, final long timeout) throws IOException {
        return getFreeSpace(path) / FileUtils.ONE_KB;
    }

    /**
     * Gets the number of bytes available to this Java virtual machine on the given file store.
     *
     * <pre>
     * FileSystemUtils.freeSpace("C:"); // Windows
     * FileSystemUtils.freeSpace("/volume"); // *nix
     * </pre>
     *
     * @param pathStr the path to get free space for, not null, not empty on UNIX
     * @return the amount of free drive space on the drive or volume
     * @throws IOException              if an I/O error occurs.
     * @throws IllegalArgumentException if the path is invalid.
     */
    static long getFreeSpace(final String pathStr) throws IOException {
        final Path path = Paths.get(Objects.requireNonNull(pathStr, "pathStr"));
        if (Files.exists(path)) {
            // Need an absolute path for input like "" to work
            return Files.getFileStore(path.toAbsolutePath()).getUsableSpace();
            // return path.toAbsolutePath().toFile().getUsableSpace();
        }
        throw new IllegalArgumentException(path.toString());
    }

    /**
     * Instances should NOT be constructed in standard programming.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public FileSystemUtils() {
        // empty
    }

}
