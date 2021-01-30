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

package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TimetampedObserver}.
 */
public class TimetampedObserverTest {

    @Test
    public void test() throws IOException, InterruptedException {
        final Instant before = Instant.now();
        Thread.sleep(20); // Some OS' clock granularity may be high.
        final TimetampedObserver timetampedObserver = new TimetampedObserver();
        assertTrue(timetampedObserver.getOpenInstant().isAfter(before));
        assertTrue(timetampedObserver.getOpenToNowDuration().toNanos() > 0);
        assertNull(timetampedObserver.getCloseInstant());
        final byte[] buffer = MessageDigestCalculatingInputStreamTest
            .generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        try (final ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer),
            timetampedObserver)) {
            assertTrue(timetampedObserver.getOpenInstant().isAfter(before));
            assertTrue(timetampedObserver.getOpenToNowDuration().toNanos() > 0);
        }
        assertTrue(timetampedObserver.getOpenInstant().isAfter(before));
        assertTrue(timetampedObserver.getOpenToNowDuration().toNanos() > 0);
        assertTrue(timetampedObserver.getCloseInstant().isAfter(timetampedObserver.getOpenInstant()));
        assertTrue(timetampedObserver.getOpenToCloseDuration().toNanos() > 0);
    }

    @Test
    public void testExample() throws IOException, InterruptedException {
        final TimetampedObserver timetampedObserver = new TimetampedObserver();
        final byte[] buffer = MessageDigestCalculatingInputStreamTest
            .generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        try (final ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer),
            timetampedObserver)) {
            //
        }
        System.out.printf("IO duration: %s%n", timetampedObserver.getOpenToCloseDuration());
    }

}
