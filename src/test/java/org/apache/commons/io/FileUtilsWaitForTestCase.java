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

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * This is used to test FileUtils.waitFor() method for correctness.
 *
 * @see FileUtils
 */
public class FileUtilsWaitForTestCase {
    // This class has been broken out from FileUtilsTestCase
    // to solve issues as per BZ 38927

    //-----------------------------------------------------------------------
    @Test
    public void testWaitFor() {
        FileUtils.waitFor(new File(""), -1);
        FileUtils.waitFor(new File(""), 2);
    }

    @Test
    public void testWaitForInterrupted() throws InterruptedException {
        final AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        final CountDownLatch started = new CountDownLatch(1);
        final Runnable thread = () -> {
            started.countDown();
            FileUtils.waitFor(new File(""), 2);
            wasInterrupted.set( Thread.currentThread().isInterrupted());
        };
        final Thread thread1 = new Thread(thread);
        thread1.start();
        started.await();
        thread1.interrupt();
        thread1.join();
        assertTrue( wasInterrupted.get() );
    }

}
