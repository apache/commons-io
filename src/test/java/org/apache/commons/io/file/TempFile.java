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
 * A temporary file path that deletes its delegate on close.
 *
 * @since 2.12.0
 */
public class TempFile extends DeletablePath {

    /**
     * Creates a new instance for a new temporary file in the specified directory, using the given prefix to generate its
     * name.
     *
     * @param dir See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @param prefix See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @param suffix See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @param attrs See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @return a new instance for a new temporary directory
     * @throws IOException See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     */
    public static TempFile create(final Path dir, final String prefix, final String suffix, final FileAttribute<?>... attrs) throws IOException {
        return new TempFile(Files.createTempFile(dir, prefix, suffix, attrs));
    }

    /**
     * Creates a new instance for a new temporary file in the specified directory, using the given prefix to generate its
     * name.
     *
     * @param prefix See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @param suffix See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @param attrs See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     * @return a new instance for a new temporary directory
     * @throws IOException See {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
     */
    public static TempFile create(final String prefix, final String suffix, final FileAttribute<?>... attrs) throws IOException {
        return new TempFile(Files.createTempFile(prefix, suffix, attrs));
    }

    /**
     * Constructs a new instance wrapping the given delegate.
     *
     * @param path The delegate.
     */
    private TempFile(final Path path) {
        super(path);
    }

}
