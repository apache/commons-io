/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.file;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Set;

/**
 * Helps tests use {@link FileSystem}s.
 */
public final class NioFileSystem {
    // macOS:
    // jshell> FileSystems.getDefault().supportedFileAttributeViews()
    // $1 ==> [owner, basic, posix, user, unix]

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String BASIC = "basic";

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String DOS = "dos";

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String OWNER = "owner";

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String POSIX = "posix";

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String UNIX = "unix";

    /**
     * The {@value} name from {@link FileSystem#supportedFileAttributeViews()}.
     */
    public static final String USER = "user";

    /**
     * Tests whether the given view names contains the {@link #DOS} name.
     *
     * @param views The names to test.
     * @return whether the given view names contains the {@link #DOS} name.
     */
    public static boolean isDos(final Set<String> views) {
        return views.contains(DOS);
    }

    /**
     * Tests whether the given FileSystem contains the {@link #POSIX} file attribute view name.
     *
     * @param fileSystem The FileSystem to test.
     * @return whether the given FileSystem contains the {@link #POSIX} file attribute view name.
     */
    public static boolean isPosix(final FileSystem fileSystem) {
        return supportsFileAttributeView(fileSystem, POSIX);
    }

    /**
     * Tests whether the given Path contains the {@link #POSIX} file attribute view name.
     *
     * @param path The Path to test.
     * @return whether the given FileSystem contains the {@link #POSIX} file attribute view name.
     */
    public static boolean isPosix(final Path path) {
        return supportsFileAttributeView(path.getFileSystem(), POSIX);
    }

    /**
     * Tests whether the given view names contains the {@link #POSIX} name.
     *
     * @param views The names to test.
     * @return whether the given view names contains the {@link #POSIX} name.
     */
    public static boolean isPosix(final Set<String> views) {
        return views.contains(POSIX);
    }

    /**
     * Tests whether the given view names contains the {@link #UNIX} name.
     *
     * @param views The names to test.
     * @return whether the given view names contains the {@link #UNIX} name.
     */
    public static boolean isUnix(final Set<String> views) {
        return views.contains(UNIX);
    }

    /**
     * Tests whether the given FileSystem contains the {@code view} file attribute view name.
     *
     * @param fileSystem The FileSystem to test.
     * @param view The view name to test.
     * @return whether the given FileSystem contains the {@code view} file attribute view name.
     */
    public static boolean supportsFileAttributeView(final FileSystem fileSystem, final String view) {
        return fileSystem.supportedFileAttributeViews().contains(view);
    }

    /**
     * No instances needed.
     */
    private NioFileSystem() {
        // empty
    }
}
