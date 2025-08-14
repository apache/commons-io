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
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ProxyOutputStream}.
 */
class ProxyOutputStreamTest {

    private ByteArrayOutputStream target;

    private ProxyOutputStream proxied;

    private final AtomicBoolean hitByteArray = new AtomicBoolean();
    private final AtomicBoolean hitByteArrayAt = new AtomicBoolean();
    private final AtomicBoolean hitInt = new AtomicBoolean();

    @BeforeEach
    public void setUp() {
        target = new ByteArrayOutputStream() {

            @Override
            public void write(final byte[] ba) {
                hitByteArray.set(true);
                super.write(ba);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) {
                hitByteArrayAt.set(true);
                super.write(b, off, len);
            }

            @Override
            public synchronized void write(final int ba) {
                hitInt.set(true);
                super.write(ba);
            }
        };
        proxied = new ProxyOutputStream(target);
    }

    @Test
    void testBuilder() throws Exception {
        assertSame(target, new ProxyOutputStream.Builder().setOutputStream(target).get().unwrap());
    }

    @SuppressWarnings("resource")
    @Test
    void testSetReference() throws Exception {
        assertFalse(hitByteArray.get());
        proxied.setReference(new ByteArrayOutputStream());
        proxied.write('y');
        assertFalse(hitByteArray.get());
        assertEquals(0, target.size());
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, target.toByteArray());
    }

    @Test
    void testWriteByteArray() throws Exception {
        assertFalse(hitByteArray.get());
        proxied.write(new byte[] { 'y', 'z' });
        assertTrue(hitByteArray.get());
        assertEquals(2, target.size());
        assertArrayEquals(new byte[] { 'y', 'z' }, target.toByteArray());
    }

    @Test
    void testWriteByteArrayAt() throws Exception {
        assertFalse(hitByteArrayAt.get());
        proxied.write(new byte[] { 'y', 'z' }, 1, 1);
        assertTrue(hitByteArrayAt.get());
        assertEquals(1, target.size());
        assertArrayEquals(new byte[] { 'z' }, target.toByteArray());
    }

    @Test
    void testWriteByteArrayAtRepeat() throws Exception {
        // repeat -1
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 1, 1, 0);
        assertFalse(hitByteArrayAt.get());
        hitByteArray.set(false);
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 0
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 1, 1, 0);
        assertFalse(hitByteArrayAt.get());
        hitByteArray.set(false);
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 1
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 1, 1, 1);
        assertTrue(hitByteArrayAt.get());
        hitByteArray.set(false);
        assertEquals(1, target.size());
        assertArrayEquals(new byte[] { 'z' }, target.toByteArray());
        // repeat 2
        proxied.writeRepeat(new byte[] { 'y', 'x' }, 1, 1, 2);
        assertTrue(hitByteArrayAt.get());
        assertEquals(3, target.size());
        assertArrayEquals(new byte[] { 'z', 'x', 'x' }, target.toByteArray());
    }

    @Test
    void testWriteByteArrayRepeat() throws Exception {
        // repeat -1
        proxied.writeRepeat(new byte[] { 'y', 'z' }, -1);
        assertFalse(hitByteArray.get());
        hitByteArray.set(false);
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 0
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 0);
        assertFalse(hitByteArray.get());
        hitByteArray.set(false);
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 1
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 1);
        assertTrue(hitByteArray.get());
        hitByteArray.set(false);
        assertEquals(2, target.size());
        assertArrayEquals(new byte[] { 'y', 'z' }, target.toByteArray());
        // repeat 2
        proxied.writeRepeat(new byte[] { 'y', 'z' }, 2);
        assertTrue(hitByteArray.get());
        assertEquals(6, target.size());
        assertArrayEquals(new byte[] { 'y', 'z', 'y', 'z' , 'y', 'z' }, target.toByteArray());
    }

    @Test
    void testWriteInt() throws Exception {
        assertFalse(hitInt.get());
        proxied.write('y');
        assertTrue(hitInt.get());
        assertEquals(1, target.size());
        assertEquals('y', target.toByteArray()[0]);
    }

    @Test
    void testWriteIntRepeat() throws Exception {
        // repeat -1
        assertFalse(hitInt.get());
        proxied.writeRepeat('y', -1);
        assertFalse(hitInt.get());
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 0
        assertFalse(hitInt.get());
        proxied.writeRepeat('y', 0);
        assertFalse(hitInt.get());
        assertEquals(0, target.size());
        assertArrayEquals(new byte[] {}, target.toByteArray());
        // repeat 1
        assertFalse(hitInt.get());
        proxied.writeRepeat('y', 1);
        assertTrue(hitInt.get());
        hitInt.set(false);
        assertEquals(1, target.size());
        assertArrayEquals(new byte[] { 'y' }, target.toByteArray());
        // repeat 2
        assertFalse(hitInt.get());
        proxied.writeRepeat('z', 2);
        assertTrue(hitInt.get());
        hitInt.set(false);
        assertEquals(3, target.size());
        assertArrayEquals(new byte[] { 'y', 'z', 'z' }, target.toByteArray());
    }

    @Test
    void testWriteNullArrayProxiesToUnderlying() throws Exception {
        assertFalse(hitByteArray.get());
        final byte[] ba = null;
        assertThrows(NullPointerException.class, () -> target.write(ba));
        assertTrue(hitByteArray.get());
        assertThrows(NullPointerException.class, () -> proxied.write(ba));
        assertTrue(hitByteArray.get());
    }
}
