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
package org.apache.commons.io.filefilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.CounterAssertions;
import org.apache.commons.io.file.Counters;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AgeFileFilter}.
 */
public class AgeFileFilterTest {

    /**
     * Javadoc example.
     *
     * System.out calls are commented out here but not in the Javadoc.
     */
    @Test
    public void testJavadocExampleUsingIo() {
        final File dir = new File(".");
        // We are interested in files older than one day
        final long cutoff = System.currentTimeMillis();
        final String[] files = dir.list(new AgeFileFilter(cutoff));
        for (final String file : files) {
            // System.out.println(files[i]);
        }
        // End of Javadoc example
        assertTrue(files.length > 0);
    }

    /**
     * Javadoc example.
     *
     * System.out calls are commented out here but not in the Javadoc.
     */
    @Test
    public void testJavadocExampleUsingNio() throws IOException {
        final Path dir = Paths.get("");
        // We are interested in files older than one day
        final long cutoff = System.currentTimeMillis();
        final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new AgeFileFilter(cutoff), TrueFileFilter.INSTANCE);
        //
        // Walk one dir
        Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
        // System.out.println(visitor.getPathCounters());
        // System.out.println(visitor.getFileList());
        //
        visitor.getPathCounters().reset();
        //
        // Walk dir tree
        Files.walkFileTree(dir, visitor);
        // System.out.println(visitor.getPathCounters());
        // System.out.println(visitor.getDirList());
        // System.out.println(visitor.getFileList());
        //
        // End of Javadoc example
        assertTrue(visitor.getPathCounters().getFileCounter().get() > 0);
        assertTrue(visitor.getPathCounters().getDirectoryCounter().get() > 0);
        assertTrue(visitor.getPathCounters().getByteCounter().get() > 0);
        // We counted and accumulated
        assertFalse(visitor.getDirList().isEmpty());
        assertFalse(visitor.getFileList().isEmpty());
        //
        assertNotEquals(Counters.noopPathCounters(), visitor.getPathCounters());
        visitor.getPathCounters().reset();
        CounterAssertions.assertZeroCounters(visitor.getPathCounters());
    }

    @Test
    public void testNoCounting() throws IOException {
        final Path dir = Paths.get("");
        final long cutoff = System.currentTimeMillis();
        final AccumulatorPathVisitor visitor = new AccumulatorPathVisitor(Counters.noopPathCounters(),
            new AgeFileFilter(cutoff), TrueFileFilter.INSTANCE);
        Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
        //
        CounterAssertions.assertZeroCounters(visitor.getPathCounters());
        // We did not count, but we still accumulated
        assertFalse(visitor.getDirList().isEmpty());
        assertFalse(visitor.getFileList().isEmpty());
    }
}
