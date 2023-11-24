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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests {@code Erase}.
 */
final class EraseTest {

    private final AtomicInteger intRef = new AtomicInteger();
    private final AtomicBoolean boolRef = new AtomicBoolean();

    @Test
    void testAcceptIOBiConsumerOfTUTU() {
        Erase.accept((e, f) -> boolRef.set(intRef.compareAndSet(0, e)), 1, true);
        assertEquals(1, intRef.get());
        assertTrue(boolRef.get());
        assertThrows(IOException.class, () -> Erase.accept(TestUtils.throwingIOBiConsumer(), null, 1));
    }

    @Test
    void testAcceptIOConsumerOfTT() {
        Erase.accept(e -> intRef.compareAndSet(0, e), 1);
        assertEquals(1, intRef.get());
        assertThrows(IOException.class, () -> Erase.accept(TestUtils.throwingIOConsumer(), 1));
    }

    @Test
    void testApplyIOBiFunctionOfQsuperTQsuperUQextendsRTU() {
        assertTrue(Erase.<Integer, Boolean, Boolean>apply((i, b) -> boolRef.compareAndSet(false, intRef.compareAndSet(0, i.intValue())), 1, Boolean.TRUE));
        assertThrows(IOException.class, () -> Erase.apply(TestUtils.throwingIOBiFunction(), 1, Boolean.TRUE));
    }

    @Test
    void testApplyIOFunctionOfQsuperTQextendsRT() {
        assertTrue(Erase.<Integer, Boolean>apply(e -> intRef.compareAndSet(0, e), 1));
        assertThrows(IOException.class, () -> Erase.apply(TestUtils.throwingIOFunction(), 1));
    }

    @Test
    void testCompare() {
        assertEquals(0, Erase.compare(String::compareTo, "A", "A"));
        assertEquals(-1, Erase.compare(String::compareTo, "A", "B"));
        assertEquals(1, Erase.compare(String::compareTo, "B", "A"));
        assertThrows(IOException.class, () -> Erase.compare(TestUtils.throwingIOComparator(), null, null));
    }

    @Test
    void testGet() {
        assertEquals(0, Erase.get(() -> intRef.get()));
        assertThrows(IOException.class, () -> Erase.get(TestUtils.throwingIOSupplier()));
    }

    @Test
    void testRethrow() {
        assertThrows(IOException.class, () -> Erase.rethrow(new IOException()));
    }

    @Test
    void testRun() {
        Erase.run(() -> intRef.set(1));
        assertEquals(1, intRef.get());
        assertThrows(IOException.class, () -> Erase.run(TestUtils.throwingIORunnable()));
    }

    @Test
    void testTest() {
        assertTrue(Erase.test(e -> intRef.compareAndSet(0, e), 1));
        assertThrows(IOException.class, () -> Erase.test(TestUtils.throwingIOPredicate(), 1));
    }

}
