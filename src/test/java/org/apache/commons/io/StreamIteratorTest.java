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

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link StreamIterator}.
 */
public class StreamIteratorTest {

    @Test
    public void testForEachRemaining() {
        final AtomicBoolean closed = new AtomicBoolean();
        final Iterator<Integer> iter = StreamIterator.iterator(Stream.of(1, 2, 3).onClose(() -> closed.set(true)));
        final AtomicInteger sum = new AtomicInteger();

        iter.forEachRemaining(sum::addAndGet);

        assertEquals(6, sum.get());
        assertTrue(closed.get());
    }

    @Test
    public void testHasNext() {
        final AtomicBoolean closed = new AtomicBoolean();
        final Iterator<Integer> iter = StreamIterator.iterator(Stream.of(1, 2, 3).onClose(() -> closed.set(true)));
        int sum = 0;

        while (iter.hasNext()) {
            sum += iter.next();
        }

        assertEquals(6, sum);
        assertTrue(closed.get());
    }
}
