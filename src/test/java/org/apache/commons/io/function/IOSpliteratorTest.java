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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOSpliterator}.
 */
public class IOSpliteratorTest {

    private IOSpliterator<Path> spliterator;

    @BeforeEach
    public void beforeEach() {
        spliterator = IOSpliterator.adapt(newPathList().spliterator());
    }

    private List<Path> newPathList() {
        return Arrays.asList(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B);
    }

    @Test
    public void testAdapt() {
        spliterator = IOSpliterator.adapt(newPathList().spliterator());
        assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testAsSpliterator() {
        assertEquals(2, spliterator.estimateSize());
        assertEquals(2, spliterator.asSpliterator().estimateSize());
    }

    @Test
    public void testCharacteristics() {
        assertEquals(spliterator.unwrap().characteristics(), spliterator.characteristics());
        assertEquals(spliterator.unwrap().characteristics(), spliterator.asSpliterator().characteristics());
    }

    @Test
    public void testEstimateSize() {
        assertEquals(2, spliterator.estimateSize());
        assertEquals(spliterator.unwrap().estimateSize(), spliterator.estimateSize());
        assertEquals(spliterator.unwrap().estimateSize(), spliterator.asSpliterator().estimateSize());
    }

    @Test
    public void testForEachRemaining() {
        final List<Path> list = new ArrayList<>();
        spliterator.forEachRemaining(list::add);
        assertEquals(2, list.size());
        assertEquals(newPathList(), list);
    }

    @Test
    public void testForEachRemainingAsSpliterator() {
        final List<Path> list = new ArrayList<>();
        spliterator.asSpliterator().forEachRemaining(list::add);
        assertEquals(2, list.size());
        assertEquals(newPathList(), list);
    }

    @Test
    public void testGetComparator() {
        if (spliterator.hasCharacteristics(Spliterator.SORTED)) {
            assertEquals(spliterator.unwrap().getComparator(), spliterator.getComparator());
            assertEquals(spliterator.unwrap().getComparator(), spliterator.asSpliterator().getComparator());
        } else {
            assertThrows(IllegalStateException.class, () -> spliterator.unwrap().getComparator());
            assertThrows(IllegalStateException.class, () -> spliterator.asSpliterator().getComparator());
        }
        final IOSpliterator<Path> adapted = IOSpliterator.adapt(new TreeSet<>(newPathList()).stream().sorted().spliterator());
        final IOComparator<? super Path> comparator = adapted.getComparator();
        assertNull(comparator);
    }

    @Test
    public void testGetExactSizeIfKnown() {
        assertEquals(2, spliterator.getExactSizeIfKnown());
        assertEquals(spliterator.unwrap().getExactSizeIfKnown(), spliterator.getExactSizeIfKnown());
        assertEquals(spliterator.unwrap().getExactSizeIfKnown(), spliterator.asSpliterator().getExactSizeIfKnown());
    }

    @Test
    public void testHasCharacteristics() {
        assertTrue(spliterator.hasCharacteristics(spliterator.characteristics()));
        assertEquals(spliterator.unwrap().hasCharacteristics(spliterator.unwrap().characteristics()),
                spliterator.hasCharacteristics(spliterator.characteristics()));
        assertEquals(spliterator.unwrap().hasCharacteristics(spliterator.unwrap().characteristics()),
                spliterator.asSpliterator().hasCharacteristics(spliterator.asSpliterator().characteristics()));
    }

    @Test
    public void testTryAdvance() {
        final AtomicReference<Path> ref = new AtomicReference<>();
        assertTrue(spliterator.tryAdvance(ref::set));
        assertEquals(TestConstants.ABS_PATH_A, ref.get());
    }

    @Test
    public void testTrySplit() {
        final IOSpliterator<Path> trySplit = spliterator.trySplit();
        assertNotNull(trySplit);
        assertTrue(spliterator.getExactSizeIfKnown() > 0);
    }

    @Test
    public void testUnwrap() {
        assertNotNull(spliterator.unwrap());
    }

}
