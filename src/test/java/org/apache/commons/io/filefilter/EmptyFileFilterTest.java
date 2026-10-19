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

package org.apache.commons.io.filefilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link EmptyFileFilter}.
 */
class EmptyFileFilterTest extends AbstractFilterTest {

    /**
     * Tests that both empty and non-empty filters reject a directory when closing its stream fails.
     *
     * @param hasEntries whether the directory contains an entry.
     * @throws IOException if configuring the filesystem provider fails.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testDirectoryCloseIOException(final boolean hasEntries) throws IOException {
        final Path path = mock(Path.class);
        final FileSystem fileSystem = mock(FileSystem.class);
        final FileSystemProvider provider = mock(FileSystemProvider.class, CALLS_REAL_METHODS);
        final BasicFileAttributes attributes = mock(BasicFileAttributes.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.readAttributes(path, BasicFileAttributes.class)).thenReturn(attributes);
        when(attributes.isDirectory()).thenReturn(true);
        when(provider.newDirectoryStream(eq(path), any())).thenReturn(new DirectoryStream<Path>() {

            @Override
            public void close() throws IOException {
                throw new IOException("Cannot close directory");
            }

            @Override
            public Iterator<Path> iterator() {
                return (hasEntries ? Collections.singleton(path) : Collections.<Path>emptyList()).iterator();
            }
        });
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.EMPTY.accept(path, attributes));
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.NOT_EMPTY.accept(path, attributes));
        assertFalse(EmptyFileFilter.EMPTY.matches(path));
        assertFalse(EmptyFileFilter.NOT_EMPTY.matches(path));
    }

    /**
     * Tests that both empty and non-empty filters reject a directory when opening its stream fails.
     *
     * @throws IOException if configuring the filesystem provider fails.
     */
    @Test
    void testDirectoryIOException() throws IOException {
        final Path path = mock(Path.class);
        final FileSystem fileSystem = mock(FileSystem.class);
        final FileSystemProvider provider = mock(FileSystemProvider.class, CALLS_REAL_METHODS);
        final BasicFileAttributes attributes = mock(BasicFileAttributes.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.readAttributes(path, BasicFileAttributes.class)).thenReturn(attributes);
        when(attributes.isDirectory()).thenReturn(true);
        when(provider.newDirectoryStream(eq(path), any())).thenThrow(new IOException("Cannot list directory"));
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.EMPTY.accept(path, attributes));
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.NOT_EMPTY.accept(path, attributes));
        assertFalse(EmptyFileFilter.EMPTY.matches(path));
        assertFalse(EmptyFileFilter.NOT_EMPTY.matches(path));
    }

    /**
     * Tests that both empty and non-empty filters reject a missing path whose size cannot be determined.
     */
    @Test
    void testMissingPath() {
        final Path path = temporaryFolder.toPath().resolve("missing");
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.EMPTY.accept(path, null));
        assertEquals(FileVisitResult.TERMINATE, EmptyFileFilter.NOT_EMPTY.accept(path, null));
        assertFalse(EmptyFileFilter.EMPTY.matches(path));
        assertFalse(EmptyFileFilter.NOT_EMPTY.matches(path));
    }
}
