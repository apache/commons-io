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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * This is used to test FileUtils.waitFor() method for correctness.
 *
 * @see FileUtils
 */
public class FileUtilsWaitForTest {
    // This class has been broken out from FileUtilsTestCase
    // to solve issues as per BZ 38927

    @Test
    public void testWaitFor0() {
        FileUtils.waitFor(FileUtils.current(), 0);
    }

    /**
     * TODO Fails randomly.
     */
    @Test
    public void testWaitForInterrupted() throws InterruptedException {
        final AtomicBoolean wasInterrupted = new AtomicBoolean();
        final CountDownLatch started = new CountDownLatch(1);
        final Thread thread1 = new Thread(() -> {
            started.countDown();
            assertTrue(FileUtils.waitFor(FileUtils.current(), 4));
            wasInterrupted.set(Thread.currentThread().isInterrupted());
        });
        thread1.start();
        thread1.interrupt();
        started.await();
        thread1.join();
        assertTrue(wasInterrupted.get());
    }

    @Test
    public void testWaitForNegativeDuration() {
        FileUtils.waitFor(FileUtils.current(), -1);
    }

}
