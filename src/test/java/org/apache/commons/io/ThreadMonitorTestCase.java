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

import org.apache.commons.io.testtools.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ThreadMonitor}.
 */
public class ThreadMonitorTestCase {

    /**
     * Test timeout.
     */
    @Test
    public void testTimeout() {
        try {
            final Thread monitor = ThreadMonitor.start(100);
            TestUtils.sleep(200);
            ThreadMonitor.stop(monitor);
            fail("Expected InterruptedException");
        } catch (final InterruptedException e) {
            // expected result - timeout
        }
    }

    /**
     * Test task completed before timeout.
     */
    @Test
    public void testCompletedWithoutTimeout() {
        try {
            final Thread monitor = ThreadMonitor.start(200);
            TestUtils.sleep(100);
            ThreadMonitor.stop(monitor);
        } catch (final InterruptedException e) {
            fail("Timed Out");
        }
    }

    /**
     * Test No timeout.
     */
    @Test
    public void testNoTimeout() {

        // timeout = -1
        try {
            final Thread monitor = ThreadMonitor.start(-1);
            assertNull("Timeout -1, Monitor should be null", monitor);
            TestUtils.sleep(100);
            ThreadMonitor.stop(monitor);
        } catch (final Exception e) {
            fail("Timeout -1, threw " + e);
        }

        // timeout = 0
        try {
            final Thread monitor = ThreadMonitor.start(0);
            assertNull("Timeout 0, Monitor should be null", monitor);
            TestUtils.sleep(100);
            ThreadMonitor.stop(monitor);
        } catch (final Exception e) {
            fail("Timeout 0, threw " + e);
        }
    }
}

