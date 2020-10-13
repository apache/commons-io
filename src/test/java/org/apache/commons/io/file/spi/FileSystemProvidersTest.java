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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FileSystemProvidersTest {

    private static final String FILE_PATH = "file:///foo.txt";

    @Test
    public void testGetFileSystemProvider_all() throws URISyntaxException {
        final List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();
        for (final FileSystemProvider fileSystemProvider : installedProviders) {
            final String scheme = fileSystemProvider.getScheme();
            final URI uri = new URI(scheme, "ssp", "fragment");
            assertEquals(scheme, FileSystemProviders.installed().getFileSystemProvider(uri).getScheme());
        }
    }

    @Test
    public void testGetFileSystemProvider_filePath() {
        assertNotNull(FileSystemProviders.getFileSystemProvider(Paths.get(URI.create(FILE_PATH))));
    }

    @Test
    public void testGetFileSystemProvider_fileScheme() {
        assertNotNull(FileSystemProviders.installed().getFileSystemProvider("file"));
    }

    @Test
    public void testGetFileSystemProvider_fileURI() {
        assertNotNull(FileSystemProviders.installed().getFileSystemProvider(URI.create(FILE_PATH)));
    }

    @Test
    public void testGetFileSystemProvider_fileURL() throws MalformedURLException {
        assertNotNull(FileSystemProviders.installed().getFileSystemProvider(new URL(FILE_PATH)));
    }

}
