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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOIterable}.
 */
public class IOIterableTest {

    private static class Fixture implements IOIterable<Path> {

        List<Path> list = Arrays.asList(Paths.get("a"), Paths.get("b"));

        @Override
        public IOIterator<Path> iterator() {
            return IOIterator.adapt(list);
        }

        @Override
        public Iterable<Path> unwrap() {
            return list;
        }

    }

    private IOIterable<Path> iterable;
    private Fixture fixture;

    @BeforeEach
    public void beforeEach() {
        fixture = new Fixture();
        iterable = fixture;
    }

    @Test
    public void testForEach() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        assertThrows(NullPointerException.class, () -> iterable.forEach(null));
        iterable.forEach(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
    }

    @Test
    public void testIterator() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        iterable.iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
    }

    @Test
    public void testSpliterator() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        iterable.spliterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
    }

    @Test
    public void testUnrwap() throws IOException {
        assertSame(fixture.list, iterable.unwrap());
        assertSame(fixture.unwrap(), iterable.unwrap());
    }
}
