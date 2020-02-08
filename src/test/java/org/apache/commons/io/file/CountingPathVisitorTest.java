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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link CountingPathVisitor}.
 */
public class CountingPathVisitorTest extends TestArguments {

    /**
     * Tests an empty folder.
     */
    @ParameterizedTest
    @MethodSource("countingPathVisitors")
    public void testCountEmptyFolder(final CountingPathVisitor visitor) throws IOException {
        checkZeroCounts(visitor);
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            assertCounts(1, 0, 0, PathUtils.visitFileTree(visitor, tempDir));
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }

    /**
     * Tests a directory with one file of size 0.
     */
    @ParameterizedTest
    @MethodSource("countingPathVisitors")
    public void testCountFolders1FileSize0(final CountingPathVisitor visitor) throws IOException {
        checkZeroCounts(visitor);
        assertCounts(1, 1, 0, PathUtils.visitFileTree(visitor,
                "src/test/resources/org/apache/commons/io/dirs-1-file-size-0"));
    }

    /**
     * Tests a directory with one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("countingPathVisitors")
    public void testCountFolders1FileSize1(final CountingPathVisitor visitor) throws IOException {
        checkZeroCounts(visitor);
        assertCounts(1, 1, 1, PathUtils.visitFileTree(visitor,
                "src/test/resources/org/apache/commons/io/dirs-1-file-size-1"));
    }

    /**
     * Tests a directory with two subdirectorys, each containing one file of size 1.
     */
    @ParameterizedTest
    @MethodSource("countingPathVisitors")
    public void testCountFolders2FileSize2(final CountingPathVisitor visitor) throws IOException {
        checkZeroCounts(visitor);
        assertCounts(3, 2, 2, PathUtils.visitFileTree(visitor,
                "src/test/resources/org/apache/commons/io/dirs-2-file-size-2"));
    }

    private void checkZeroCounts(final CountingPathVisitor visitor) {
        Assertions.assertEquals(CountingPathVisitor.withLongCounters(), visitor);
        Assertions.assertEquals(CountingPathVisitor.withBigIntegerCounters(), visitor);
    }

    @ParameterizedTest
    @MethodSource("countingPathVisitors")
    void testToString(final CountingPathVisitor visitor) {
        // Make sure it does not blow up
        visitor.toString();
    }
}
