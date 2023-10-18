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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.ThreadUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TimestampedObserver}.
 */
public class TimestampedObserverTest {

    @Test
    public void test() throws IOException, InterruptedException {
        final Instant before = Instant.now();
        // Some OS' clock granularity may be high.
        ThreadUtils.sleep(Duration.ofMillis(20));
        final TimestampedObserver timestampedObserver = new TimestampedObserver();
        assertFalse(timestampedObserver.isClosed());
        // Java 8 instant resolution is not great.
        ThreadUtils.sleep(Duration.ofMillis(20));
        // toString() should not blow up before close().
        assertNotNull(timestampedObserver.toString());
        assertTrue(timestampedObserver.getOpenInstant().isAfter(before));
        assertTrue(timestampedObserver.getOpenToNowDuration().toNanos() > 0);
        assertNull(timestampedObserver.getCloseInstant());
        assertFalse(timestampedObserver.isClosed());
        final byte[] buffer = MessageDigestInputStreamTest.generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        try (ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer), timestampedObserver)) {
            assertTrue(timestampedObserver.getOpenInstant().isAfter(before));
            assertTrue(timestampedObserver.getOpenToNowDuration().toNanos() > 0);
            assertFalse(timestampedObserver.isClosed());
        }
        assertTrue(timestampedObserver.isClosed());
        assertTrue(timestampedObserver.getOpenInstant().isAfter(before));
        assertTrue(timestampedObserver.getOpenToNowDuration().toNanos() > 0);
        assertTrue(timestampedObserver.getCloseInstant().isAfter(timestampedObserver.getOpenInstant()));
        assertTrue(timestampedObserver.getOpenToCloseDuration().toNanos() > 0);
        assertNotNull(timestampedObserver.toString());
    }

    @Test
    public void testExample() throws IOException {
        final TimestampedObserver timestampedObserver = new TimestampedObserver();
        final byte[] buffer = MessageDigestInputStreamTest
            .generateRandomByteStream(IOUtils.DEFAULT_BUFFER_SIZE);
        try (ObservableInputStream ois = new ObservableInputStream(new ByteArrayInputStream(buffer),
            timestampedObserver)) {
            //
        }
        // System.out.printf("IO duration: %s%n", timestampedObserver);
        // System.out.printf("IO duration: %s%n", timestampedObserver.getOpenToCloseDuration());
    }

}
