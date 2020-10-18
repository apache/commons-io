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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.commons.io.filefilter.PathVisitorFileFilter;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests both {@link AccumulatorPathVisitor} and {@link PathVisitorFileFilter}.
 */
public class AccumulatorPathVisitorTest {

    static Stream<Arguments> testParameters() {
        // @formatter:off
        return Stream.of(
            Arguments.of((Supplier<AccumulatorPathVisitor>) AccumulatorPathVisitor::withLongCounters),
            Arguments.of((Supplier<AccumulatorPathVisitor>) AccumulatorPathVisitor::withBigIntegerCounters));
        // @formatter:on
    }

    @TempDir
    File tempDirFile;

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testEmptyFolder(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final Path tempDir = tempDirFile.toPath();
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(tempDir,
            new AndFileFilter(countingFileFilter, DirectoryFileFilter.INSTANCE, EmptyFileFilter.EMPTY));
        assertCounts(1, 0, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertTrue(accPathVisitor.getFileList().isEmpty());
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders1FileSize0(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"),
            countingFileFilter);
        assertCounts(1, 1, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertEquals(1, accPathVisitor.getFileList().size());
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders1FileSize1(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"),
            countingFileFilter);
        assertCounts(1, 1, 1, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertEquals(1, accPathVisitor.getFileList().size());
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders2FileSize2(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"),
            countingFileFilter);
        assertCounts(3, 2, 2, accPathVisitor.getPathCounters());
        assertEquals(3, accPathVisitor.getDirList().size());
        assertEquals(2, accPathVisitor.getFileList().size());
    }

}
