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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@code IOIteratorAdapter}.
 */
public class IOIteratorAdapterTest {

    private IOIteratorAdapter<Path> iterator;

    @BeforeEach
    public void beforeEach() {
        iterator = IOIteratorAdapter.adapt(newPathList().iterator());
    }

    private List<Path> newPathList() {
        return Arrays.asList(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B);
    }

    @Test
    public void testAdapt() throws IOException {
        assertEquals(TestConstants.ABS_PATH_A, iterator.next());
    }

    @Test
    public void testAsIterator() {
        assertEquals(TestConstants.ABS_PATH_A, iterator.asIterator().next());
    }

    @Test
    public void testForEachRemaining() throws IOException {
        final List<Path> list = new ArrayList<>();
        iterator.forEachRemaining(p -> list.add(p.toRealPath()));
        assertFalse(iterator.hasNext());
        assertEquals(newPathList(), list);
    }

    @Test
    public void testHasNext() throws IOException {
        assertTrue(iterator.hasNext());
        iterator.forEachRemaining(Path::toRealPath);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNext() throws IOException {
        assertEquals(TestConstants.ABS_PATH_A, iterator.next());
    }

    @Test
    public void testRemove() throws IOException {
        final Class<? extends Exception> exClass = SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8) ? IllegalStateException.class
            : UnsupportedOperationException.class;
        assertThrows(exClass, iterator::remove);
        assertThrows(exClass, iterator::remove);
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertThrows(UnsupportedOperationException.class, iterator::remove);

    }

}
