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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * A temporary directory path that deletes its delegate on close.
 *
 * @since 2.12.0
 */
public class TempDirectory extends DeletablePath {

    /**
     * Creates a new instance for a new temporary directory in the specified directory, using the given prefix to generate
     * its name.
     *
     * @param dir See {@link Files#createTempDirectory(String, FileAttribute...)}.
     * @param prefix See {@link Files#createTempDirectory(String, FileAttribute...)}.
     * @param attrs See {@link Files#createTempDirectory(String, FileAttribute...)}.
     * @return a new instance for a new temporary directory
     * @throws IOException See {@link Files#createTempDirectory(String, FileAttribute...)}.
     */
    public static TempDirectory create(final Path dir, final String prefix, final FileAttribute<?>... attrs) throws IOException {
        return new TempDirectory(Files.createTempDirectory(dir, prefix, attrs));
    }

    /**
     * Creates a new instance for a new temporary directory in the specified directory, using the given prefix to generate
     * its name.
     *
     * @param prefix See {@link Files#createTempDirectory(String, FileAttribute...)}.
     * @param attrs See {@link Files#createTempDirectory(String, FileAttribute...)}.
     * @return a new instance for a new temporary directory
     * @throws IOException See {@link Files#createTempDirectory(String, FileAttribute...)}.
     */
    public static TempDirectory create(final String prefix, final FileAttribute<?>... attrs) throws IOException {
        return new TempDirectory(Files.createTempDirectory(prefix, attrs));
    }

    /**
     * Constructs a new instance wrapping the given delegate.
     *
     * @param path The delegate.
     */
    private TempDirectory(final Path path) {
        super(path);
    }

}
