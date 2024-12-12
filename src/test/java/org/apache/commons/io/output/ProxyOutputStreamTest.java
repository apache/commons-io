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
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CloseShieldOutputStream}.
 */
public class ProxyOutputStreamTest {

    private final AtomicBoolean hit = new AtomicBoolean();

    private ByteArrayOutputStream original;

    private OutputStream proxied;

    @BeforeEach
    public void setUp() {
        original = new ByteArrayOutputStream() {

            @Override
            public void write(final byte[] ba) {
                hit.set(true);
                super.write(ba);
            }

            @Override
            public synchronized void write(final int ba) {
                hit.set(true);
                super.write(ba);
            }
        };
        proxied = new ProxyOutputStream(original);
    }

    @Test
    public void testWrite() throws Exception {
        assertFalse(hit.get());
        proxied.write('y');
        assertTrue(hit.get());
        assertEquals(1, original.size());
        assertEquals('y', original.toByteArray()[0]);
    }

    @Test
    public void testWriteNullArrayProxiesToUnderlying() throws Exception {
        assertFalse(hit.get());
        final byte[] ba = null;
        assertThrows(NullPointerException.class, () -> original.write(ba));
        assertTrue(hit.get());
        assertThrows(NullPointerException.class, () -> proxied.write(ba));
        assertTrue(hit.get());
    }
}
