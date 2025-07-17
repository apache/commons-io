/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOBiConsumer}.
 */
class IOBiConsumerTest {

    @Test
    void testAccept() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOBiConsumer<String, Integer> biConsumer = (s, i) -> ref.set(s + i);
        biConsumer.accept("A", 1);
        assertEquals("A1", ref.get());
    }

    @Test
    void testAndThen() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOBiConsumer<String, Integer> biConsumer1 = (s, i) -> ref.set(s + i);
        final IOBiConsumer<String, Integer> biConsumer2 = (s, i) -> ref.set(ref.get() + i + s);
        biConsumer1.andThen(biConsumer2).accept("B", 2);
        assertEquals("B22B", ref.get());
    }

    @Test
    void testAsBiConsumer() {
        final Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        assertThrows(UncheckedIOException.class, () -> map.forEach(TestConstants.THROWING_IO_BI_CONSUMER.asBiConsumer()));
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOBiConsumer<String, Integer> consumer1 = (t, u) -> ref.set(t + u);
        map.forEach(consumer1.asBiConsumer());
        assertEquals("a1", ref.get());
    }

    @Test
    void testNoopIOConsumer() throws IOException {
        IOBiConsumer.noop().accept(null, null);
    }

}
