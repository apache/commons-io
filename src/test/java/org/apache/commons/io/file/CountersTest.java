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

import static org.apache.commons.io.file.CounterAssertions.assertCounter;
import static org.apache.commons.io.file.CounterAssertions.assertCounts;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.file.Counters.Counter;
import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CountersTest extends TestArguments {

    @ParameterizedTest
    @MethodSource("numberCounters")
    public void testInitialValue(final Counter counter) {
        assertCounter(0, counter, "");
    }

    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testInitialValues(final PathCounters pathCounter) {
        // Does not blow up
        assertCounts(0, 0, 0, pathCounter);
    }

    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testResetCounter(final PathCounters pathCounter) {
        final Counter byteCounter = pathCounter.getByteCounter();
        final long old = byteCounter.get();
        byteCounter.add(1);
        assertEquals(old + 1, byteCounter.get());
        byteCounter.reset();
        assertEquals(0, byteCounter.get());
    }

    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testResetPathCounter(final PathCounters pathCounter) {
        final Counter byteCounter = pathCounter.getByteCounter();
        final long old = byteCounter.get();
        byteCounter.add(1);
        assertEquals(old + 1, byteCounter.get());
        pathCounter.reset();
        assertEquals(0, byteCounter.get());
    }

    @ParameterizedTest
    @MethodSource("numberCounters")
    public void testToString(final Counter counter) {
        // Does not blow up
        counter.toString();
    }

    @ParameterizedTest
    @MethodSource("pathCounters")
    public void testToString(final PathCounters pathCounter) {
        // Does not blow up
        pathCounter.toString();
    }
}
