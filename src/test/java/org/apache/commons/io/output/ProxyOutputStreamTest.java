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

    private ByteArrayOutputStream original;

    private ProxyOutputStream proxied;

    private final AtomicBoolean hitByteArray = new AtomicBoolean();
    private final AtomicBoolean hitByteArrayAt = new AtomicBoolean();
    private final AtomicBoolean hitInt = new AtomicBoolean();

    @BeforeEach
    public void setUp() {
        original = new ByteArrayOutputStream() {

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
        proxied = new ProxyOutputStream(original);
    }

    @Test
    void testBuilder() throws Exception {
        assertSame(original, new ProxyOutputStream.Builder().setOutputStream(original).get().unwrap());
    }

    @SuppressWarnings("resource")
    @Test
    void testSetReference() throws Exception {
        assertFalse(hitByteArray.get());
        proxied.setReference(new ByteArrayOutputStream());
        proxied.write('y');
        assertFalse(hitByteArray.get());
        assertEquals(0, original.size());
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, original.toByteArray());
    }

    @Test
    void testWriteByteArray() throws Exception {
        assertFalse(hitByteArray.get());
        proxied.write(new byte[] { 'y', 'z' });
        assertTrue(hitByteArray.get());
        assertEquals(2, original.size());
        assertArrayEquals(new byte[] { 'y', 'z' }, original.toByteArray());
    }

    @Test
    void testWriteByteArrayAt() throws Exception {
        assertFalse(hitByteArrayAt.get());
        proxied.write(new byte[] { 'y', 'z' }, 1, 1);
        assertTrue(hitByteArrayAt.get());
        assertEquals(1, original.size());
        assertArrayEquals(new byte[] { 'z' }, original.toByteArray());
    }

    @Test
    void testWriteInt() throws Exception {
        assertFalse(hitInt.get());
        proxied.write('y');
        assertTrue(hitInt.get());
        assertEquals(1, original.size());
        assertEquals('y', original.toByteArray()[0]);
    }

    @Test
    void testWriteNullArrayProxiesToUnderlying() throws Exception {
        assertFalse(hitByteArray.get());
        final byte[] ba = null;
        assertThrows(NullPointerException.class, () -> original.write(ba));
        assertTrue(hitByteArray.get());
        assertThrows(NullPointerException.class, () -> proxied.write(ba));
        assertTrue(hitByteArray.get());
    }
}
