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
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Uncheck}.
 */
public class UncheckTest {

    private AtomicReference<String> ref1;
    private AtomicReference<String> ref2;
    private AtomicReference<String> ref3;

    private AtomicReference<String> ref4;

    @BeforeEach
    public void initEach() {
        ref1 = new AtomicReference<>();
        ref2 = new AtomicReference<>();
        ref3 = new AtomicReference<>();
        ref4 = new AtomicReference<>();
    }

    @Test
    public void testAcceptIOBiConsumerOfTUTU() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept((t, u) -> {
            throw new IOException();
        }, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(TestConstants.THROWING_IO_BI_CONSUMER, null, null));
        Uncheck.accept((t, u) -> {
            TestUtils.compareAndSetThrows(ref1, t);
            TestUtils.compareAndSetThrows(ref2, u);
        }, "new1", "new2");
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
    }

    @Test
    public void testAcceptIOConsumerOfTT() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(t -> {
            throw new IOException();
        }, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(TestUtils.throwingIOConsumer(), null));
        Uncheck.accept(t -> TestUtils.compareAndSetThrows(ref1, t), "new1");
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testAcceptIOTriConsumerOfTUVTUV() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept((t, u, v) -> {
            throw new IOException();
        }, null, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(TestConstants.THROWING_IO_TRI_CONSUMER, null, null, null));
        Uncheck.accept((t, u, v) -> {
            TestUtils.compareAndSetThrows(ref1, t);
            TestUtils.compareAndSetThrows(ref2, u);
            TestUtils.compareAndSetThrows(ref3, v);
        }, "new1", "new2", "new3");
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
        assertEquals("new3", ref3.get());
    }

    @Test
    public void testApplyIOBiFunctionOfTURTU() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply((t, u) -> {
            throw new IOException();
        }, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_BI_FUNCTION, null, null));
        assertEquals("new0", Uncheck.apply((t, u) -> {
            TestUtils.compareAndSetThrows(ref1, t);
            TestUtils.compareAndSetThrows(ref2, u);
            return "new0";
        }, "new1", "new2"));
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
    }

    @Test
    public void testApplyIOFunctionOfTRT() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(t -> {
            throw new IOException();
        }, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_FUNCTION, null));
        Uncheck.apply(t -> TestUtils.compareAndSetThrows(ref1, t), "new1");
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testApplyIOQuadFunctionOfTUVWRTUVW() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply((t, u, v, w) -> {
            throw new IOException();
        }, null, null, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_QUAD_FUNCTION, null, null, null, null));
        assertEquals("new0", Uncheck.apply((t, u, v, w) -> {
            TestUtils.compareAndSetThrows(ref1, t);
            TestUtils.compareAndSetThrows(ref2, u);
            TestUtils.compareAndSetThrows(ref3, v);
            TestUtils.compareAndSetThrows(ref4, w);
            return "new0";
        }, "new1", "new2", "new3", "new4"));
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
        assertEquals("new3", ref3.get());
        assertEquals("new4", ref4.get());
    }

    @Test
    public void testApplyIOTriFunctionOfTUVRTUV() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply((t, u, v) -> {
            throw new IOException();
        }, null, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_TRI_FUNCTION, null, null, null));
        assertEquals("new0", Uncheck.apply((t, u, v) -> {
            TestUtils.compareAndSetThrows(ref1, t);
            TestUtils.compareAndSetThrows(ref2, u);
            TestUtils.compareAndSetThrows(ref3, v);
            return "new0";
        }, "new1", "new2", "new3"));
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
        assertEquals("new3", ref3.get());
    }

    @Test
    public void testGet() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.get(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.get(TestConstants.THROWING_IO_SUPPLIER));
        assertEquals("new1", Uncheck.get(() -> TestUtils.compareAndSetThrows(ref1, "new1")));
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testRun() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.run(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.run(TestConstants.THROWING_IO_RUNNABLE));
        Uncheck.run(() -> TestUtils.compareAndSetThrows(ref1, "new1"));
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testTest() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.test(t -> {
            throw new IOException();
        }, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.test(TestConstants.THROWING_IO_PREDICATE, null));
        assertTrue(Uncheck.test(t -> TestUtils.compareAndSetThrows(ref1, t).equals(t), "new1"));
        assertEquals("new1", ref1.get());
    }

}
