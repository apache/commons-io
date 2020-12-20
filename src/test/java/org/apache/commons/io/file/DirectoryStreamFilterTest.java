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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DirectoryStreamFilter}.
 */
public class DirectoryStreamFilterTest {

    private static final String PATH_FIXTURE = "NOTICE.txt";

    @Test
    public void testFilterByName() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        final DirectoryStreamFilter streamFilter = new DirectoryStreamFilter(pathFilter);
        assertEquals(pathFilter, streamFilter.getPathFilter());
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(PathUtils.current(), streamFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertEquals(PATH_FIXTURE, path.getFileName().toString());
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    public void testFilterByNameNot() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE).negate();
        final DirectoryStreamFilter streamFilter = new DirectoryStreamFilter(pathFilter);
        assertEquals(pathFilter, streamFilter.getPathFilter());
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(PathUtils.current(), streamFilter)) {
            for (final Path path : stream) {
                assertNotEquals(PATH_FIXTURE, path.getFileName().toString());
            }
        }
    }

}
