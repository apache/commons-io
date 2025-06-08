/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.file;

import org.apache.commons.io.file.Counters.Counter;
import org.apache.commons.io.file.Counters.PathCounters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CountersEqualsAndHashCodeTest {

    @Test
    void testBigIntegerCounterEquals() {
        testEquals(Counters.bigIntegerCounter(), Counters.bigIntegerCounter());
    }

    @Test
    void testBigIntegerHashCode() {
        testHashCodes(Counters.bigIntegerCounter(), Counters.bigIntegerCounter());
    }

    private void testEquals(final Counter counter1, final Counter counter2) {
        Assertions.assertEquals(counter1, counter2);
        counter1.increment();
        Assertions.assertNotEquals(counter1, counter2);
        counter2.increment();
        Assertions.assertEquals(counter1, counter2);
    }

    private void testEqualsByteCounters(final PathCounters counter1, final PathCounters counter2) {
        Assertions.assertEquals(counter1, counter2);
        counter1.getByteCounter().increment();
        Assertions.assertNotEquals(counter1, counter2);
        counter2.getByteCounter().increment();
        Assertions.assertEquals(counter1, counter2);
    }

    private void testEqualsDirectoryCounters(final PathCounters counter1, final PathCounters counter2) {
        Assertions.assertEquals(counter1, counter2);
        counter1.getDirectoryCounter().increment();
        Assertions.assertNotEquals(counter1, counter2);
        counter2.getDirectoryCounter().increment();
        Assertions.assertEquals(counter1, counter2);
    }

    private void testEqualsFileCounters(final PathCounters counter1, final PathCounters counter2) {
        Assertions.assertEquals(counter1, counter2);
        counter1.getFileCounter().increment();
        Assertions.assertNotEquals(counter1, counter2);
        counter2.getFileCounter().increment();
        Assertions.assertEquals(counter1, counter2);
    }

    private void testHashCodeFileCounters(final PathCounters counter1, final PathCounters counter2) {
        Assertions.assertEquals(counter1.hashCode(), counter2.hashCode());
        counter1.getFileCounter().increment();
        Assertions.assertNotEquals(counter1.hashCode(), counter2.hashCode());
        counter2.getFileCounter().increment();
        Assertions.assertEquals(counter1.hashCode(), counter2.hashCode());
    }

    private void testHashCodes(final Counter counter1, final Counter counter2) {
        Assertions.assertEquals(counter1.hashCode(), counter2.hashCode());
        counter1.increment();
        Assertions.assertNotEquals(counter1.hashCode(), counter2.hashCode());
        counter2.increment();
        Assertions.assertEquals(counter1.hashCode(), counter2.hashCode());
    }

    @Test
    void testLongCounterEquals() {
        testEquals(Counters.longCounter(), Counters.longCounter());
    }

    @Test
    void testLongCounterHashCodes() {
        testHashCodes(Counters.longCounter(), Counters.longCounter());
    }

    @Test
    void testLongCounterMixEquals() {
        testEquals(Counters.longCounter(), Counters.bigIntegerCounter());
        testEquals(Counters.bigIntegerCounter(), Counters.longCounter());
    }

    @Test
    void testLongPathCountersEqualsByteCounters() {
        testEqualsByteCounters(Counters.longPathCounters(), Counters.longPathCounters());
    }

    @Test
    void testLongPathCountersEqualsDirectoryCounters() {
        testEqualsDirectoryCounters(Counters.longPathCounters(), Counters.longPathCounters());
    }

    @Test
    void testLongPathCountersEqualsFileCounters() {
        testEqualsFileCounters(Counters.longPathCounters(), Counters.longPathCounters());
    }

    @Test
    void testLongPathCountersHashCodeFileCounters() {
        testHashCodeFileCounters(Counters.longPathCounters(), Counters.longPathCounters());
    }

    @Test
    void testMix() {
        testHashCodeFileCounters(Counters.longPathCounters(), Counters.bigIntegerPathCounters());
    }
}
