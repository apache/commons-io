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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.ThreadUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.EmptyFileFilter;
import org.apache.commons.io.filefilter.PathVisitorFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Test;
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
            Arguments.of((Supplier<AccumulatorPathVisitor>) AccumulatorPathVisitor::withBigIntegerCounters),
            Arguments.of((Supplier<AccumulatorPathVisitor>) () ->
                AccumulatorPathVisitor.withBigIntegerCounters(TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)));
        // @formatter:on
    }

    static Stream<Arguments> testParametersIgnoreFailures() {
        // @formatter:off
        return Stream.of(
            Arguments.of((Supplier<AccumulatorPathVisitor>) () -> new AccumulatorPathVisitor(
                Counters.bigIntegerPathCounters(),
                CountingPathVisitor.defaultDirectoryFilter(),
                CountingPathVisitor.defaultFileFilter())));
        // @formatter:on
    }

    @TempDir
    Path tempDirPath;

    /**
     * Tests the 0-argument constructor.
     */
    @Test
    public void test0ArgConstructor() throws IOException {
        final AccumulatorPathVisitor accPathVisitor = new AccumulatorPathVisitor();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(tempDirPath, new AndFileFilter(countingFileFilter, DirectoryFileFilter.INSTANCE, EmptyFileFilter.EMPTY));
        assertCounts(0, 0, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertTrue(accPathVisitor.getFileList().isEmpty());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testEmptyFolder(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(tempDirPath, new AndFileFilter(countingFileFilter, DirectoryFileFilter.INSTANCE, EmptyFileFilter.EMPTY));
        assertCounts(1, 0, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertTrue(accPathVisitor.getFileList().isEmpty());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    @Test
    public void testEqualsHashCode() {
        final AccumulatorPathVisitor visitor0 = AccumulatorPathVisitor.withLongCounters();
        final AccumulatorPathVisitor visitor1 = AccumulatorPathVisitor.withLongCounters();
        assertEquals(visitor0, visitor0);
        assertEquals(visitor0, visitor1);
        assertEquals(visitor1, visitor0);
        assertEquals(visitor0.hashCode(), visitor0.hashCode());
        assertEquals(visitor0.hashCode(), visitor1.hashCode());
        assertEquals(visitor1.hashCode(), visitor0.hashCode());
        visitor0.getPathCounters().getByteCounter().increment();
        assertEquals(visitor0, visitor0);
        assertNotEquals(visitor0, visitor1);
        assertNotEquals(visitor1, visitor0);
        assertEquals(visitor0.hashCode(), visitor0.hashCode());
        assertNotEquals(visitor0.hashCode(), visitor1.hashCode());
        assertNotEquals(visitor1.hashCode(), visitor0.hashCode());
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders1FileSize0(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-0"), countingFileFilter);
        assertCounts(1, 1, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertEquals(1, accPathVisitor.getFileList().size());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders1FileSize1(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), countingFileFilter);
        assertCounts(1, 1, 1, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertEquals(1, accPathVisitor.getFileList().size());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    /**
     * Tests a directory with two subdirectories, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("testParameters")
    public void testFolders2FileSize2(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor);
        Files.walkFileTree(Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2"), countingFileFilter);
        assertCounts(3, 2, 2, accPathVisitor.getPathCounters());
        assertEquals(3, accPathVisitor.getDirList().size());
        assertEquals(2, accPathVisitor.getFileList().size());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    /**
     * Tests IO-755 with a directory with 100 files, and delete all of them midway through the visit.
     *
     * Random failure like:
     *
     * <pre>
     * ...?...
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("testParametersIgnoreFailures")
    public void testFolderWhileDeletingAsync(final Supplier<AccumulatorPathVisitor> supplier) throws IOException, InterruptedException {
        final int count = 10_000;
        final List<Path> files = new ArrayList<>(count);
        // Create "count" file fixtures
        for (int i = 1; i <= count; i++) {
            final Path tempFile = Files.createTempFile(tempDirPath, "test", ".txt");
            assertTrue(Files.exists(tempFile));
            files.add(tempFile);
        }
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor) {
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
                // Slow down the walking a bit to try and cause conflicts with the deletion thread
                try {
                    ThreadUtils.sleep(Duration.ofMillis(10));
                } catch (final InterruptedException ignore) {
                    // e.printStackTrace();
                }
                return super.visitFile(path, attributes);
            }
        };
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final AtomicBoolean deleted = new AtomicBoolean();
        try {
            executor.execute(() -> {
                for (final Path file : files) {
                    try {
                        // File deletion is slow compared to tree walking, so we go as fast as we can here
                        Files.delete(file);
                    } catch (final IOException ignored) {
                        // e.printStackTrace();
                    }
                }
                deleted.set(true);
            });
            Files.walkFileTree(tempDirPath, countingFileFilter);
        } finally {
            if (!deleted.get()) {
                ThreadUtils.sleep(Duration.ofMillis(1000));
            }
            if (!deleted.get()) {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
            executor.shutdownNow();
        }
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

    /**
     * Tests IO-755 with a directory with 100 files, and delete all of them midway through the visit.
     */
    @ParameterizedTest
    @MethodSource("testParametersIgnoreFailures")
    public void testFolderWhileDeletingSync(final Supplier<AccumulatorPathVisitor> supplier) throws IOException {
        final int count = 100;
        final int marker = count / 2;
        final Set<Path> files = new LinkedHashSet<>(count);
        for (int i = 1; i <= count; i++) {
            final Path tempFile = Files.createTempFile(tempDirPath, "test", ".txt");
            assertTrue(Files.exists(tempFile));
            files.add(tempFile);
        }
        final AccumulatorPathVisitor accPathVisitor = supplier.get();
        final AtomicInteger visitCount = new AtomicInteger();
        final PathVisitorFileFilter countingFileFilter = new PathVisitorFileFilter(accPathVisitor) {
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
                if (visitCount.incrementAndGet() == marker) {
                    // Now that we've visited half the files, delete them all
                    for (final Path file : files) {
                        Files.delete(file);
                    }
                }
                return super.visitFile(path, attributes);
            }
        };
        Files.walkFileTree(tempDirPath, countingFileFilter);
        assertCounts(1, marker - 1, 0, accPathVisitor.getPathCounters());
        assertEquals(1, accPathVisitor.getDirList().size());
        assertEquals(marker - 1, accPathVisitor.getFileList().size());
        assertEquals(accPathVisitor, accPathVisitor);
        assertEquals(accPathVisitor.hashCode(), accPathVisitor.hashCode());
    }

}
