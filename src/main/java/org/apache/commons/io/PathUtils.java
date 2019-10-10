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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * NIO Path utilities.
 *
 * @since 2.7
 */
public class PathUtils {

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

}
