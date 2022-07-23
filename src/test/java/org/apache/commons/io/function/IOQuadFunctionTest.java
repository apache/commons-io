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

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOQuadFunction}.
 */
public class IOQuadFunctionTest {

    /**
     * Tests {@link IOQuadFunction#apply(Object, Object, Object, Object)}.
     *
     * @throws IOException thrown on test failure
     */
    @Test
    public void testAccept() throws IOException {
        final AtomicReference<Byte> ref1 = new AtomicReference<>();
        final AtomicReference<Short> ref2 = new AtomicReference<>();
        final AtomicReference<String> ref3 = new AtomicReference<>();
        final AtomicReference<Long> ref4 = new AtomicReference<>();
        final IOQuadFunction<AtomicReference<Byte>, AtomicReference<Short>, AtomicReference<String>, AtomicReference<Long>, String> quad = (t, u, v, w) -> {
            ref1.set(Byte.valueOf("1"));
            ref2.set(Short.valueOf((short) 1));
            ref3.set("z");
            ref4.set(Long.valueOf(2));
            return "ABCD";
        };
        assertEquals("ABCD", quad.apply(ref1, ref2, ref3, ref4));
        assertEquals(Byte.valueOf("1"), ref1.get());
        assertEquals(Short.valueOf((short) 1), ref2.get());
        assertEquals("z", ref3.get());
        assertEquals(Long.valueOf(2), ref4.get());
    }

    /**
     * Tests {@link IOTriFunction#andThen(IOFunction)}.
     *
     * @throws IOException thrown on test failure
     */
    @Test
    public void testAndThenIOFunction() throws IOException {
        final AtomicReference<Byte> ref1 = new AtomicReference<>();
        final AtomicReference<Short> ref2 = new AtomicReference<>();
        final AtomicReference<String> ref3 = new AtomicReference<>();
        final AtomicReference<Long> ref4 = new AtomicReference<>();
        final IOQuadFunction<AtomicReference<Byte>, AtomicReference<Short>, AtomicReference<String>, AtomicReference<Long>, String> quad = (t, u, v, w) -> {
            ref1.set(Byte.valueOf("1"));
            ref2.set(Short.valueOf((short) 1));
            ref3.set("z");
            ref4.set(Long.valueOf(2));
            return "9";
        };
        final IOFunction<String, BigInteger> after = t -> {
            ref1.set(Byte.valueOf("2"));
            ref2.set(Short.valueOf((short) 2));
            ref3.set("zz");
            ref4.set(Long.valueOf(3));
            return BigInteger.valueOf(Long.parseLong(t)).add(BigInteger.ONE);
        };
        assertEquals(BigInteger.TEN, quad.andThen(after).apply(ref1, ref2, ref3, ref4));
        assertEquals(Byte.valueOf("2"), ref1.get());
        assertEquals(Short.valueOf((short) 2), ref2.get());
        assertEquals("zz", ref3.get());
        assertEquals(Long.valueOf(3), ref4.get());
    }

}
