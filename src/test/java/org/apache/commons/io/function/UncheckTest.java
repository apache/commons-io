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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Uncheck}.
 */
public class UncheckTest {

    private static final byte[] BYTES = { 'a', 'b' };
    private static final String CAUSE_MESSAGE = "CauseMessage";
    private static final String CUSTOM_MESSAGE = "Custom message";

    private AtomicInteger atomicInt;
    private AtomicLong atomicLong;
    private AtomicBoolean atomicBoolean;
    private AtomicReference<String> ref1;
    private AtomicReference<String> ref2;
    private AtomicReference<String> ref3;
    private AtomicReference<String> ref4;

    private void assertUncheckedIOException(final IOException expected, final UncheckedIOException e) {
        assertEquals(CUSTOM_MESSAGE, e.getMessage());
        final IOException cause = e.getCause();
        assertEquals(expected.getClass(), cause.getClass());
        assertEquals(CAUSE_MESSAGE, cause.getMessage());
    }

    @BeforeEach
    public void beforeEach() {
        ref1 = new AtomicReference<>();
        ref2 = new AtomicReference<>();
        ref3 = new AtomicReference<>();
        ref4 = new AtomicReference<>();
        atomicInt = new AtomicInteger();
        atomicLong = new AtomicLong();
        atomicBoolean = new AtomicBoolean();
    }

    private ByteArrayInputStream newInputStream() {
        return new ByteArrayInputStream(BYTES);
    }

    /**
     * Tests {@link Uncheck#accept(IOConsumer, Object)}.
     */
    @Test
    public void testAccept() {
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.accept(n -> stream.skip(n), 1);
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }

    @Test
    public void testAcceptIOBiConsumerOfTUTU() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept((t, u) -> {
            throw new IOException();
        }, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(TestConstants.THROWING_IO_BI_CONSUMER, null, null));
        Uncheck.accept((t, u) -> {
            TestUtils.compareAndSetThrowsIO(ref1, t);
            TestUtils.compareAndSetThrowsIO(ref2, u);
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
        Uncheck.accept(t -> TestUtils.compareAndSetThrowsIO(ref1, t), "new1");
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testAcceptIOTriConsumerOfTUVTUV() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept((t, u, v) -> {
            throw new IOException();
        }, null, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.accept(TestConstants.THROWING_IO_TRI_CONSUMER, null, null, null));
        Uncheck.accept((t, u, v) -> {
            TestUtils.compareAndSetThrowsIO(ref1, t);
            TestUtils.compareAndSetThrowsIO(ref2, u);
            TestUtils.compareAndSetThrowsIO(ref3, v);
        }, "new1", "new2", "new3");
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
        assertEquals("new3", ref3.get());
    }

    /**
     * Tests {@link Uncheck#apply(IOFunction, Object)}.
     */
    @Test
    public void testApply1() {
        final ByteArrayInputStream stream = newInputStream();
        assertEquals(1, Uncheck.apply(n -> stream.skip(n), 1).intValue());
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }

    /**
     * Tests {@link Uncheck#apply(IOBiFunction, Object, Object)}.
     */
    @Test
    public void testApply2() {
        final ByteArrayInputStream stream = newInputStream();
        final byte[] buf = new byte[BYTES.length];
        assertEquals(1, Uncheck.apply((o, l) -> stream.read(buf, o, l), 0, 1).intValue());
        assertEquals('a', buf[0]);
    }

    /**
     * Tests {@link Uncheck#apply(IOTriFunction, Object, Object, Object)}.
     */
    @Test
    public void testApply3() {
        final ByteArrayInputStream stream = newInputStream();
        final byte[] buf = new byte[BYTES.length];
        assertEquals(1, Uncheck.apply((b, o, l) -> stream.read(b, o, l), buf, 0, 1).intValue());
        assertEquals('a', buf[0]);
    }

    @Test
    public void testApplyIOBiFunctionOfTURTU() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply((t, u) -> {
            throw new IOException();
        }, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_BI_FUNCTION, null, null));
        assertEquals("new0", Uncheck.apply((t, u) -> {
            TestUtils.compareAndSetThrowsIO(ref1, t);
            TestUtils.compareAndSetThrowsIO(ref2, u);
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
        Uncheck.apply(t -> TestUtils.compareAndSetThrowsIO(ref1, t), "new1");
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testApplyIOQuadFunctionOfTUVWRTUVW() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply((t, u, v, w) -> {
            throw new IOException();
        }, null, null, null, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.apply(TestConstants.THROWING_IO_QUAD_FUNCTION, null, null, null, null));
        assertEquals("new0", Uncheck.apply((t, u, v, w) -> {
            TestUtils.compareAndSetThrowsIO(ref1, t);
            TestUtils.compareAndSetThrowsIO(ref2, u);
            TestUtils.compareAndSetThrowsIO(ref3, v);
            TestUtils.compareAndSetThrowsIO(ref4, w);
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
            TestUtils.compareAndSetThrowsIO(ref1, t);
            TestUtils.compareAndSetThrowsIO(ref2, u);
            TestUtils.compareAndSetThrowsIO(ref3, v);
            return "new0";
        }, "new1", "new2", "new3"));
        assertEquals("new1", ref1.get());
        assertEquals("new2", ref2.get());
        assertEquals("new3", ref3.get());
    }

    /**
     * Tests {@link Uncheck#get(IOSupplier)}.
     */
    @Test
    public void testGet() {
        assertEquals('a', Uncheck.get(() -> newInputStream().read()).intValue());
        assertThrows(UncheckedIOException.class, () -> Uncheck.get(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.get(TestConstants.THROWING_IO_SUPPLIER));
        assertEquals("new1", Uncheck.get(() -> TestUtils.compareAndSetThrowsIO(ref1, "new1")));
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testGetAsBoolean() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsBoolean(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsBoolean(TestConstants.THROWING_IO_BOOLEAN_SUPPLIER));
        assertTrue(Uncheck.getAsBoolean(() -> TestUtils.compareAndSetThrowsIO(atomicBoolean, true)));
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void testGetAsInt() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsInt(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsInt(TestConstants.THROWING_IO_INT_SUPPLIER));
        assertEquals(1, Uncheck.getAsInt(() -> TestUtils.compareAndSetThrowsIO(atomicInt, 1)));
        assertEquals(1, atomicInt.get());
    }

    @Test
    public void testGetAsIntMessage() {
        // No exception
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsInt(() -> {
            throw new IOException();
        }, () -> CUSTOM_MESSAGE));
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsInt(TestConstants.THROWING_IO_INT_SUPPLIER, () -> CUSTOM_MESSAGE));
        assertEquals(1, Uncheck.getAsInt(() -> TestUtils.compareAndSetThrowsIO(atomicInt, 1), () -> CUSTOM_MESSAGE));
        assertEquals(1, atomicInt.get());
        // exception
        final IOException expected = new IOException(CAUSE_MESSAGE);
        try {
            Uncheck.getAsInt(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }

    @Test
    public void testGetAsLong() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsLong(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsLong(TestConstants.THROWING_IO_LONG_SUPPLIER));
        assertEquals(1L, Uncheck.getAsLong(() -> TestUtils.compareAndSetThrowsIO(atomicLong, 1L)));
        assertEquals(1L, atomicLong.get());
    }

    @Test
    public void testGetAsLongMessage() {
        // No exception
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsLong(() -> {
            throw new IOException();
        }, () -> CUSTOM_MESSAGE));
        assertThrows(UncheckedIOException.class, () -> Uncheck.getAsLong(TestConstants.THROWING_IO_LONG_SUPPLIER, () -> CUSTOM_MESSAGE));
        assertEquals(1L, Uncheck.getAsLong(() -> TestUtils.compareAndSetThrowsIO(atomicLong, 1L), () -> CUSTOM_MESSAGE));
        assertEquals(1L, atomicLong.get());
        // exception
        final IOException expected = new IOException(CAUSE_MESSAGE);
        try {
            Uncheck.getAsLong(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }

    /**
     * Tests {@link Uncheck#get(IOSupplier, Supplier)}.
     */
    @Test
    public void testGetMessage() {
        // No exception
        assertEquals('a', Uncheck.get(() -> newInputStream().read()).intValue(), () -> CUSTOM_MESSAGE);
        // Exception
        final IOException expected = new IOException(CAUSE_MESSAGE);
        try {
            Uncheck.get(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }

    /**
     * Tests {@link Uncheck#run(IORunnable)}.
     */
    @Test
    public void testRun() {
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.run(() -> stream.skip(1));
        assertEquals('b', Uncheck.get(stream::read).intValue());
        //
        assertThrows(UncheckedIOException.class, () -> Uncheck.run(() -> {
            throw new IOException();
        }));
        assertThrows(UncheckedIOException.class, () -> Uncheck.run(TestConstants.THROWING_IO_RUNNABLE));
        Uncheck.run(() -> TestUtils.compareAndSetThrowsIO(ref1, "new1"));
        assertEquals("new1", ref1.get());
    }

    /**
     * Tests {@link Uncheck#run(IORunnable, Supplier))}.
     *
     * @throws IOException
     */
    @Test
    public void testRunMessage() throws IOException {
        // No exception
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.run(() -> stream.skip(1), () -> CUSTOM_MESSAGE);
        assertEquals('b', Uncheck.get(stream::read).intValue());
        final IOException expected = new IOException(CAUSE_MESSAGE);
        // Exception
        try {
            Uncheck.run(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }

    @Test
    public void testTest() {
        assertThrows(UncheckedIOException.class, () -> Uncheck.test(t -> {
            throw new IOException();
        }, null));
        assertThrows(UncheckedIOException.class, () -> Uncheck.test(TestConstants.THROWING_IO_PREDICATE, null));
        assertTrue(Uncheck.test(t -> TestUtils.compareAndSetThrowsIO(ref1, t).equals(t), "new1"));
        assertEquals("new1", ref1.get());
    }

}
