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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.input.ObservableInputStream.Observer;

/**
 * An observer with timestamps.
 * <p>
 * For example:
 * </p>
 *
 * <pre>
 * final TimestampedObserver timetampedObserver = new TimestampedObserver();
 * try (final ObservableInputStream inputStream = new ObservableInputStream(...),
 *     timetampedObserver)) {
 *     ...
 * }
 * System.out.printf("IO duration: %s%n", timetampedObserver.getOpenToCloseDuration());
 * </pre>
 *
 * @since 2.9.0
 */
public class TimestampedObserver extends Observer {

    private volatile Instant closeInstant;
    private final Instant openInstant = Instant.now();

    @Override
    public void closed() throws IOException {
        closeInstant = Instant.now();
    }

    /**
     * Gets the instant for when this instance was closed.
     *
     * @return the instant for when closed was called.
     */
    public Instant getCloseInstant() {
        return closeInstant;
    }

    /**
     * Gets the Duration between creation and close.
     *
     * @return the Duration between creation and close.
     */
    public Duration getOpenToCloseDuration() {
        return Duration.between(openInstant, closeInstant);
    }

    /**
     * Gets the Duration between creation and now.
     *
     * @return the Duration between creation and now.
     */
    public Duration getOpenToNowDuration() {
        return Duration.between(openInstant, Instant.now());
    }

    /**
     * Gets the instant for when this instance was created.
     *
     * @return the instant for when this instance was created.
     */
    public Instant getOpenInstant() {
        return openInstant;
    }

    @Override
    public String toString() {
        return "TimestampedObserver [openInstant=" + openInstant + ", closeInstant=" + closeInstant + "]";
    }

}
