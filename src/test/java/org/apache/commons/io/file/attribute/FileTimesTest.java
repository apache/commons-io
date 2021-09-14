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

package org.apache.commons.io.file.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileTimes}.
 */
public class FileTimesTest {

    @Test
    public void PlusMinusMillis() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.plusMillis(millis), FileTimes.plusMillis(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusMillis(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testEpoch() {
        assertEquals(0, FileTimes.EPOCH.toMillis());
    }

    @Test
    public void testMinusMillis() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.minusMillis(millis), FileTimes.minusMillis(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusMillis(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testMinusNanos() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.minusNanos(millis), FileTimes.minusNanos(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusNanos(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testMinusSeconds() {
        final int seconds = 2;
        assertEquals(Instant.EPOCH.minusSeconds(seconds), FileTimes.minusSeconds(FileTimes.EPOCH, seconds).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.minusSeconds(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testPlusNanos() {
        final int millis = 2;
        assertEquals(Instant.EPOCH.plusNanos(millis), FileTimes.plusNanos(FileTimes.EPOCH, millis).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusNanos(FileTimes.EPOCH, 0).toInstant());
    }

    @Test
    public void testPlusSeconds() {
        final int seconds = 2;
        assertEquals(Instant.EPOCH.plusSeconds(seconds), FileTimes.plusSeconds(FileTimes.EPOCH, seconds).toInstant());
        assertEquals(Instant.EPOCH, FileTimes.plusSeconds(FileTimes.EPOCH, 0).toInstant());
    }

}
