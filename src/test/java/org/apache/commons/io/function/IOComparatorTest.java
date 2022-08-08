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

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOComparator}.
 */
public class IOComparatorTest {

    /** {@link Files#size(Path)} throws IOException */
    static final IOComparator<Path> PATH_SIZE_COMP = (final Path t, final Path u) -> Long.compare(Files.size(t), Files.size(u));

    /** {@link Path#toRealPath(java.nio.file.LinkOption...)} throws IOException */
    static final IOComparator<Path> REAL_PATH_COMP = (final Path t, final Path u) -> t.toRealPath().compareTo(u);

    @Test
    public void testAsComparator() {
        assertEquals(0, REAL_PATH_COMP.asComparator().compare(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A));
        assertThrows(UncheckedIOException.class,
            () -> TestConstants.THROWING_IO_COMPARATOR.asComparator().compare(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
    }

    @Test
    public void testCompareLong() throws IOException {
        assertEquals(0, REAL_PATH_COMP.compare(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A));
    }

    @Test
    public void testComparePath() throws IOException {
        assertEquals(0, PATH_SIZE_COMP.compare(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_A));
    }

    @Test
    public void testThrowing() {
        assertThrows(IOException.class, () -> TestConstants.THROWING_IO_COMPARATOR.compare(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B));
    }

}
