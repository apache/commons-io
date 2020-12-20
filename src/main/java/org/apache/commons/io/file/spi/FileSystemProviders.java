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

package org.apache.commons.io.file.spi;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Objects;

/**
 * Helps working with {@link FileSystemProvider}.
 *
 * @since 2.9.0
 */
public class FileSystemProviders {

    private static final FileSystemProviders INSTALLED = new FileSystemProviders(FileSystemProvider.installedProviders());

    /**
     * Gets the {@link FileSystemProvider} for the given Path.
     *
     * @param path The Path to query
     * @return the {@link FileSystemProvider} for the given Path.
     */
    @SuppressWarnings("resource") // FileSystem is not allocated here.
    public static FileSystemProvider getFileSystemProvider(final Path path) {
        return Objects.requireNonNull(path, "path").getFileSystem().provider();
    }

    /**
     * Returns the instance for the installed providers.
     *
     * @return the instance for the installed providers.
     * @see FileSystemProvider#installedProviders()
     */
    public static FileSystemProviders installed() {
        return INSTALLED;
    }

    private final List<FileSystemProvider> providers;

    /*
     * Might make public later.
     */
    private FileSystemProviders(final List<FileSystemProvider> providers) {
        this.providers = providers;
    }

    /**
     * Gets the {@link FileSystemProvider} for the given scheme.
     *
     * @param scheme The scheme to query.
     * @return the {@link FileSystemProvider} for the given URI or null.
     */
    @SuppressWarnings("resource") // FileSystems.getDefault() returns a constant.
    public FileSystemProvider getFileSystemProvider(final String scheme) {
        Objects.requireNonNull(scheme, "scheme");
        // Check default provider first to avoid loading of installed providers.
        if (scheme.equalsIgnoreCase("file")) {
            return FileSystems.getDefault().provider();
        }
        // Find provider.
        if (providers != null) {
            for (final FileSystemProvider provider : providers) {
                if (provider.getScheme().equalsIgnoreCase(scheme)) {
                    return provider;
                }
            }
        }
        return null;
    }

    /**
     * Gets the {@link FileSystemProvider} for the given URI.
     *
     * @param uri The URI to query
     * @return the {@link FileSystemProvider} for the given URI or null.
     */
    public FileSystemProvider getFileSystemProvider(final URI uri) {
        return getFileSystemProvider(Objects.requireNonNull(uri, "uri").getScheme());
    }

    /**
     * Gets the {@link FileSystemProvider} for the given URL.
     *
     * @param url The URL to query
     * @return the {@link FileSystemProvider} for the given URI or null.
     */
    public FileSystemProvider getFileSystemProvider(final URL url) {
        return getFileSystemProvider(Objects.requireNonNull(url, "url").getProtocol());
    }

}
