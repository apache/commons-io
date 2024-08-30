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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests FileUtils.waitFor().
 * <p>
 * This class has been broken out from FileUtilsTestCase to solve issues as per BZ 38927
 * </p>
 *
 * @see FileUtils
 */
public class FileUtilsWaitForTest {

    // Assume that this file does not exist
    private final File NOSUCHFILE = new File("a.b.c.d." + System.currentTimeMillis());

    @Test
    public void testIO_488() throws InterruptedException {
        final long start = System.currentTimeMillis();
        final AtomicBoolean wasInterrupted = new AtomicBoolean();
        final int seconds = 3;
        final Thread thread1 = new Thread(() -> {
            // This will wait (assuming the file is not found)
            assertFalse(FileUtils.waitFor(NOSUCHFILE, seconds), "Should not find file");
            wasInterrupted.set(Thread.currentThread().isInterrupted());
        });
        thread1.start();
        Thread.sleep(500); // This should be enough to ensure the waitFor loop has been entered
        thread1.interrupt(); // Try to interrupt waitFor
        thread1.join();
        assertTrue(wasInterrupted.get(), "Should have been interrupted");
        final long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= seconds * 1000, "Should wait for n seconds, actual: " + elapsed);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MILLISECONDS) // Should complete quickly as the path is present
    public void testWaitFor0() {
        assertTrue(FileUtils.waitFor(FileUtils.current(), 0));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MILLISECONDS) // Should complete quickly even though the path is missing
    public void testWaitFor0Absent() {
        assertFalse(FileUtils.waitFor(NOSUCHFILE, 0));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MILLISECONDS) // Should complete quickly as the path is present
    public void testWaitFor10() {
        assertTrue(FileUtils.waitFor(FileUtils.current(), 10));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MILLISECONDS) // Should complete quickly as the path is present
    public void testWaitFor100() {
        assertTrue(FileUtils.waitFor(FileUtils.current(), 100));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS) // Allow for timeout waiting for non-existent file
    public void testWaitFor5Absent() {
        final long start = System.currentTimeMillis();
        assertFalse(FileUtils.waitFor(NOSUCHFILE, 2));
        final long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 2000, "Must reach timeout - expected 2000, actual: " + elapsed);
    }

    @Test
    @Timeout(value = 300, unit = TimeUnit.MILLISECONDS) // Should complete quickly as the path is present
    public void testWaitForNegativeDuration() {
        assertTrue(FileUtils.waitFor(FileUtils.current(), -1));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MILLISECONDS) // Should complete quickly even though the path is missing
    public void testWaitForNegativeDurationAbsent() {
        assertFalse(FileUtils.waitFor(NOSUCHFILE, -1));
    }

}
