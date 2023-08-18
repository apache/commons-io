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

import static org.apache.commons.io.file.CounterAssertions.assertCounts;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

import org.apache.commons.io.file.Counters.PathCounters;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link CountingPathVisitor}.
 */
public class CopyDirectoryVisitorTest extends TestArguments {

    private static final CopyOption[] EXPECTED_COPY_OPTIONS = {StandardCopyOption.REPLACE_EXISTING};

    @TempDir
    private Path targetDir;

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryEmptyFolder(final PathCounters pathCounters) throws IOException {
        try (TempDirectory sourceDir = TempDirectory.create(getClass().getSimpleName())) {
            final Supplier<CopyDirectoryVisitor> supplier = () -> new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, EXPECTED_COPY_OPTIONS);
            final CopyDirectoryVisitor visitFileTree = PathUtils.visitFileTree(supplier.get(), sourceDir.get());
            assertCounts(1, 0, 0, visitFileTree);
            assertArrayEquals(EXPECTED_COPY_OPTIONS, visitFileTree.getCopyOptions());
            assertEquals(sourceDir.get(), ((AbstractPathWrapper) visitFileTree.getSourceDirectory()).get());
            assertEquals(sourceDir, visitFileTree.getSourceDirectory());
            assertEquals(targetDir, visitFileTree.getTargetDirectory());
            assertEquals(targetDir, visitFileTree.getTargetDirectory());
            //
            assertEquals(visitFileTree, supplier.get());
            assertEquals(visitFileTree.hashCode(), supplier.get().hashCode());
            assertEquals(visitFileTree, visitFileTree);
            assertEquals(visitFileTree.hashCode(), visitFileTree.hashCode());
            assertNotEquals(visitFileTree, "not");
            assertNotEquals(visitFileTree, CountingPathVisitor.withLongCounters());
        }
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryEmptyFolderFilters(final PathCounters pathCounters) throws IOException {
        try (TempDirectory sourceDir = TempDirectory.create(getClass().getSimpleName())) {
            final Supplier<CopyDirectoryVisitor> supplier = () -> new CopyDirectoryVisitor(pathCounters, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE,
                sourceDir, targetDir, EXPECTED_COPY_OPTIONS);
            final CopyDirectoryVisitor visitFileTree = PathUtils.visitFileTree(supplier.get(), sourceDir.get());
            assertCounts(1, 0, 0, visitFileTree);
            assertArrayEquals(EXPECTED_COPY_OPTIONS, visitFileTree.getCopyOptions());
            assertEquals(sourceDir, visitFileTree.getSourceDirectory());
            assertEquals(targetDir, visitFileTree.getTargetDirectory());
            //
            assertEquals(visitFileTree, supplier.get());
            assertEquals(visitFileTree.hashCode(), supplier.get().hashCode());
            assertEquals(visitFileTree, visitFileTree);
            assertEquals(visitFileTree.hashCode(), visitFileTree.hashCode());
        }
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders1FileSize0(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0");
        final Supplier<CopyDirectoryVisitor> supplier = () -> new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, EXPECTED_COPY_OPTIONS);
        final CopyDirectoryVisitor visitFileTree = PathUtils.visitFileTree(supplier.get(), sourceDir);
        assertCounts(1, 1, 0, visitFileTree);
        assertArrayEquals(EXPECTED_COPY_OPTIONS, visitFileTree.getCopyOptions());
        assertEquals(sourceDir, visitFileTree.getSourceDirectory());
        assertEquals(targetDir, visitFileTree.getTargetDirectory());
        //
        assertEquals(visitFileTree, supplier.get());
        assertEquals(visitFileTree.hashCode(), supplier.get().hashCode());
        assertEquals(visitFileTree, visitFileTree);
        assertEquals(visitFileTree.hashCode(), visitFileTree.hashCode());
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders1FileSize1(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1");
        final CopyDirectoryVisitor visitFileTree = PathUtils.visitFileTree(new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, EXPECTED_COPY_OPTIONS),
            sourceDir);
        assertCounts(1, 1, 1, visitFileTree);
        assertArrayEquals(EXPECTED_COPY_OPTIONS, visitFileTree.getCopyOptions());
        assertEquals(sourceDir, visitFileTree.getSourceDirectory());
        assertEquals(targetDir, visitFileTree.getTargetDirectory());
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testCopyDirectoryFolders2FileSize2(final PathCounters pathCounters) throws IOException {
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
        final CopyDirectoryVisitor visitFileTree = PathUtils.visitFileTree(new CopyDirectoryVisitor(pathCounters, sourceDir, targetDir, EXPECTED_COPY_OPTIONS),
            sourceDir);
        assertCounts(3, 2, 2, visitFileTree);
        assertArrayEquals(EXPECTED_COPY_OPTIONS, visitFileTree.getCopyOptions());
        assertEquals(sourceDir, visitFileTree.getSourceDirectory());
        assertEquals(targetDir, visitFileTree.getTargetDirectory());
    }

}
