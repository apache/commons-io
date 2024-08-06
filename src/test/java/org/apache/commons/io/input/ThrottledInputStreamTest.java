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

import java.io.IOException;
import java.time.Duration;

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
    public void testCalSleepTimeMs() {
        // case 0: initial - no read, no sleep
        assertEquals(0, ThrottledInputStream.toSleepMillis(0, 10_000, 1_000));

        // case 1: no threshold
        assertEquals(0, ThrottledInputStream.toSleepMillis(Long.MAX_VALUE, 0, 1_000));
        assertEquals(0, ThrottledInputStream.toSleepMillis(Long.MAX_VALUE, -1, 1_000));

        // case 2: too fast
        assertEquals(1500, ThrottledInputStream.toSleepMillis(5, 2, 1_000));
        assertEquals(500, ThrottledInputStream.toSleepMillis(5, 2, 2_000));
        assertEquals(6500, ThrottledInputStream.toSleepMillis(15, 2, 1_000));

        // case 3: too slow
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 2, 1_000));
        assertEquals(0, ThrottledInputStream.toSleepMillis(2, 2, 2_000));
        assertEquals(0, ThrottledInputStream.toSleepMillis(1, 2, 1_000));
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
