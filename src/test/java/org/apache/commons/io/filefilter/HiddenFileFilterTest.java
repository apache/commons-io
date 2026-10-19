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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link HiddenFileFilter}.
 */
class HiddenFileFilterTest {

    /**
     * Tests that hidden and visible filters reject a path when its hidden status cannot be determined.
     *
     * <p>
     * Subsequent successful checks must use the path's hidden status independently of the earlier I/O error.
     * </p>
     *
     * @throws IOException if configuring the filesystem provider fails.
     */
    @Test
    void testIOException() throws IOException {
        final Path path = mock(Path.class);
        final FileSystem fileSystem = mock(FileSystem.class);
        final FileSystemProvider provider = mock(FileSystemProvider.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(provider);
        when(provider.isHidden(path)).thenThrow(new IOException("Cannot determine hidden status"));
        assertEquals(FileVisitResult.TERMINATE, HiddenFileFilter.HIDDEN.accept(path, null));
        assertEquals(FileVisitResult.TERMINATE, HiddenFileFilter.VISIBLE.accept(path, null));
        assertFalse(HiddenFileFilter.HIDDEN.matches(path));
        assertFalse(HiddenFileFilter.VISIBLE.matches(path));
        // A later successful check must use the file's status rather than the earlier error.
        doReturn(false).when(provider).isHidden(path);
        assertEquals(FileVisitResult.CONTINUE, HiddenFileFilter.VISIBLE.accept(path, null));
        assertTrue(HiddenFileFilter.VISIBLE.matches(path));
        assertEquals(FileVisitResult.TERMINATE, HiddenFileFilter.HIDDEN.accept(path, null));
        doReturn(true).when(provider).isHidden(path);
        assertEquals(FileVisitResult.TERMINATE, HiddenFileFilter.VISIBLE.accept(path, null));
        assertEquals(FileVisitResult.CONTINUE, HiddenFileFilter.HIDDEN.accept(path, null));
    }
}
