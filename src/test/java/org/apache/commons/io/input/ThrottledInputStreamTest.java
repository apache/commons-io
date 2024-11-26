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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ThrottledInputStream.Builder;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ThrottledInputStream}.
 */
public class ThrottledInputStreamTest extends ProxyInputStreamTest<ThrottledInputStream> {

    @Override
    @SuppressWarnings({ "resource" })
    protected ThrottledInputStream createFixture() throws IOException {
        return ThrottledInputStream.builder().setInputStream(createOriginInputStream()).get();
    }

    @Test
    public void testAfterReadConsumer() throws Exception {
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (InputStream bounded = ThrottledInputStream.builder()
                .setCharSequence("Hi")
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (InputStream inputStream = ThrottledInputStream.builder()
                .setCharSequence("Hi")
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(inputStream)).getMessage());
        }
        // @formatter:on
    }

    @Test
    public void testBuilder() throws IOException {
        final Builder builder = ThrottledInputStream.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.setMaxBytesPerSecond(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.setMaxBytesPerSecond(0));
        assertThrows(IllegalArgumentException.class, () -> builder.setMaxBytes(1, Duration.ZERO.minusMillis(1)));
        assertThrows(IllegalArgumentException.class, () -> builder.setMaxBytes(1, Duration.ZERO));
        assertThrows(NullPointerException.class, () -> builder.setMaxBytes(1, (Duration) null));
        assertThrows(NullPointerException.class, () -> builder.setMaxBytes(1, (ChronoUnit) null));
        //
        // 2 bytes per second
        builder.setMaxBytesPerSecond(2);
        assertEquals(2.0, builder.getMaxBytesPerSecond());
        // @formatter:off
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .get()) {
            assertEquals(2.0, builder.getMaxBytesPerSecond());
            assertEquals(2.0, inputStream.getMaxBytesPerSecond());
        }
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(2, ChronoUnit.SECONDS)
                .get()) {
            assertEquals(2.0, builder.getMaxBytesPerSecond());
            assertEquals(2.0, inputStream.getMaxBytesPerSecond());
        }
        // @formatter:on
        Duration maxBytesPer = Duration.ofSeconds(1);
        // @formatter:off
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(2, maxBytesPer)
                .get()) {
            assertEquals(2.0, builder.getMaxBytesPerSecond());
            assertEquals(2.0, inputStream.getMaxBytesPerSecond());
        }
        //
        // 1 bytes per 1/2 second (30_000 millis)
        // @formatter:on
        maxBytesPer = maxBytesPer.dividedBy(2);
        // @formatter:off
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(1, maxBytesPer)
                .get()) {
            assertEquals(0.5, inputStream.getMaxBytesPerSecond());
        }
        // 1 byte/millis
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(1, ChronoUnit.MILLIS)
                .get()) {
            assertEquals(0.001, inputStream.getMaxBytesPerSecond());
        }
        // @formatter:on
        // 1 byte per 10_0011 millis.
        maxBytesPer = Duration.ofSeconds(20).plusMillis(11);
        // @formatter:off
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(1, maxBytesPer)
                .get()) {
            assertEquals(20.011, inputStream.getMaxBytesPerSecond());
        }
        // @formatter:on
        // Javadoc example
        // @formatter:off
        try (ThrottledInputStream inputStream = builder
                .setInputStream(createOriginInputStream())
                .setMaxBytes(100_000, ChronoUnit.SECONDS)
                .get()) {
            assertEquals(100_000.0, inputStream.getMaxBytesPerSecond());
        }
        // @formatter:on
    }

    @Test
    public void testCalSleepTimeMs() {
        // case 0: initial - no read, no sleep
        assertEquals(0, ThrottledInputStream.toSleepMillis(0, 1_000, 10_000));
        // case 1: no threshold
        assertEquals(0, ThrottledInputStream.toSleepMillis(Long.MAX_VALUE, 1_000, 0));
        assertEquals(0, ThrottledInputStream.toSleepMillis(Long.MAX_VALUE, 1_000, -1));
        // case 2: too fast
        assertEquals(1500, ThrottledInputStream.toSleepMillis(5, 1_000, 2));
        assertEquals(500, ThrottledInputStream.toSleepMillis(5, 2_000, 2));
        assertEquals(6500, ThrottledInputStream.toSleepMillis(15, 1_000, 2));
        assertEquals(4000, ThrottledInputStream.toSleepMillis(5, 1_000, 1));
        assertEquals(9000, ThrottledInputStream.toSleepMillis(5, 1_000, 0.5));
        assertEquals(99000, ThrottledInputStream.toSleepMillis(5, 1_000, 0.05));
        // case 3: too slow, no sleep needed
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 1_000, 2));
        assertEquals(0, ThrottledInputStream.toSleepMillis(2, 2_000, 2));
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 1_000, 2));
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 1_000, 2.0));
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 1_000, 1));
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 1_000, 1.0));
    }

    @Test
    public void testCloseHandleIOException() throws IOException {
        ProxyInputStreamTest.testCloseHandleIOException(ThrottledInputStream.builder());
    }

    @Override
    protected void testEos(final ThrottledInputStream inputStream) {
        assertEquals(3, inputStream.getByteCount());
    }

    @Test
    public void testGet() throws IOException {
        try (ThrottledInputStream inputStream = createFixture()) {
            inputStream.read();
            assertEquals(Duration.ZERO, inputStream.getTotalSleepDuration());
        }
    }

}
