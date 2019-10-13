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

import java.math.BigInteger;

import org.apache.commons.io.file.Counters.Counter;
import org.apache.commons.io.file.Counters.PathCounters;

class CounterAssertions {

    static void assertCounter(final long expected, final Counter actual, String message) {
        assertEquals(expected, actual.get(), message);
        assertEquals(Long.valueOf(expected), actual.getLong(), message);
        assertEquals(BigInteger.valueOf(expected), actual.getBigInteger(), message);
    }

    static void assertCounts(final long expectedDirCount, final long expectedFileCount, final long expectedByteCount,
            final CountingPathVisitor actualVisitor) {
        assertCounts(expectedDirCount, expectedFileCount, expectedByteCount, actualVisitor.getPathCounters());
    }

    static void assertCounts(final long expectedDirCount, final long expectedFileCount, final long expectedByteCount,
            final PathCounters actualPathCounters) {
        assertCounter(expectedDirCount, actualPathCounters.getDirectoryCounter(), "getDirectoryCounter");
        assertCounter(expectedFileCount, actualPathCounters.getFileCounter(), "getFileCounter");
        assertCounter(expectedByteCount, actualPathCounters.getByteCounter(), "getByteCounter");
    }

}
