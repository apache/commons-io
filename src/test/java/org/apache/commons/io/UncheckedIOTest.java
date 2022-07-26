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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.function.IOTriFunction;
import org.apache.commons.io.function.Uncheck;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Uncheck}.
 */
public class UncheckedIOTest {

    private static final byte[] BYTES = {'a', 'b'};

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

    /**
     * Tests {@link Uncheck#get(IOSupplier)}.
     */
    @Test
    public void testGet() {
        assertEquals('a', Uncheck.get(() -> newInputStream().read()).intValue());
    }

    /**
     * Tests {@link Uncheck#run(IORunnable)}.
     */
    @Test
    public void testRun() {
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.run(() -> stream.skip(1));
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }
}
