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

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOIntConsumer}.
 */
public class IOIntConsumerTest {

    @Test
    void testAccept() throws IOException {
        assertDoesNotThrow(() -> IOIntConsumer.NOOP.accept(0));
        assertDoesNotThrow(() -> IOIntConsumer.NOOP.accept('.'));
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOIntConsumer consumer = s -> ref.set(s + "-");
        consumer.accept(65);
        assertEquals(65 + "-", ref.get());
    }

    @Test
    void testAndThen() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOIntConsumer consumer1 = s -> ref.set(s + "-");
        final IOIntConsumer consumer2 = s -> ref.set(ref.get() + "=" + s);
        consumer1.andThen(consumer2).accept(66);
        assertEquals(66 + "-=" + 66, ref.get());
    }

    @Test
    void testAsConsumer() {
        assertThrows(UncheckedIOException.class, () -> Optional.of(65).ifPresent(TestUtils.throwingIOIntConsumer().asConsumer()));
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOIntConsumer consumer1 = s -> ref.set(s + "A");
        Optional.of(2).ifPresent(consumer1.asConsumer());
        assertEquals("2A", ref.get());
    }

    @Test
    void testasIntConsumer() {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOIntConsumer consumer1 = s -> ref.set(s + "A");
        consumer1.asIntConsumer().accept(2);
        assertEquals("2A", ref.get());
    }

    @Test
    void testNOOP() {
        // nothing happens:
        assertDoesNotThrow(() -> IOIntConsumer.NOOP.accept(0));
    }

}
