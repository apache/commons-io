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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;

import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThreadMonitor}.
 */
public class ThreadMonitorTest {

    /**
     * Test task completed before timeout.
     */
    @Test
    public void testCompletedWithoutTimeout() {
        try {
            final Thread monitor = ThreadMonitor.start(Duration.ofMillis(400));
            TestUtils.sleep(1);
            ThreadMonitor.stop(monitor);
        } catch (final InterruptedException e) {
            fail("Timed Out", e);
        }
    }

    /**
     * Test No timeout.
     */
    @Test
    public void testNoTimeoutMinus1() {
        // timeout = -1
        try {
            final Thread monitor = ThreadMonitor.start(Duration.ofMillis(-1));
            assertNull(monitor, "Timeout -1, Monitor should be null");
            TestUtils.sleep(100);
            ThreadMonitor.stop(monitor);
        } catch (final Exception e) {
            fail("Timeout -1, threw " + e, e);
        }
    }

    /**
     * Test No timeout.
     */
    @Test
    public void testNoTimeoutZero() {
        // timeout = 0
        try {
            final Thread monitor = ThreadMonitor.start(Duration.ZERO);
            assertNull(monitor, "Timeout 0, Monitor should be null");
            TestUtils.sleep(100);
            ThreadMonitor.stop(monitor);
        } catch (final Exception e) {
            fail("Timeout 0, threw " + e, e);
        }
    }

    /**
     * Test timeout.
     */
    @Test
    public void testTimeout() {
        assertThrows(InterruptedException.class, () -> {
            final Thread monitor = ThreadMonitor.start(Duration.ofMillis(100));
            TestUtils.sleep(400);
            ThreadMonitor.stop(monitor);
        });
    }
}
