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

import java.time.Duration;
import java.time.Instant;

/**
 * Helps work with threads.
 *
 * @since 2.12.0
 */
public final class ThreadUtils {

    private static int getNanosOfMilli(final Duration duration) {
        return duration.getNano() % 1_000_000;
    }

    /**
     * Sleeps for a guaranteed minimum duration unless interrupted.
     * <p>
     * This method exists because Thread.sleep(100) can sleep for 0, 70, 100 or 200ms or anything else it deems appropriate. Read
     * {@link Thread#sleep(long, int)}} for further interesting details.
     * </p>
     *
     * @param duration the sleep duration.
     * @throws InterruptedException if interrupted
     * @see Thread#sleep(long, int)
     */
    public static void sleep(final Duration duration) throws InterruptedException {
        // Using this method avoids depending on the vagaries of the precision and accuracy of system timers and schedulers.
        final Instant finishInstant = Instant.now().plus(duration);
        Duration remainingDuration = duration;
        do {
            Thread.sleep(remainingDuration.toMillis(), getNanosOfMilli(remainingDuration));
            remainingDuration = Duration.between(Instant.now(), finishInstant);
        } while (!remainingDuration.isNegative());
    }

    /**
     * Make private in 3.0.
     *
     * @deprecated TODO Make private in 3.0.
     */
    @Deprecated
    public ThreadUtils() {
        // empty
    }
}
