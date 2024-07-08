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
package org.apache.commons.io.input;

import static org.apache.commons.io.IOUtils.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ClosedInputStream}.
 */
public class ClosedInputStreamTest {

    private void assertEof(final ClosedInputStream cis) {
        assertEquals(EOF, cis.read(), "read()");
    }

    @Test
    public void testAvailableAfterClose() throws Exception {
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        final InputStream shadow;
        try (InputStream in = new ClosedInputStream()) {
            assertEquals(0, in.available());
            shadow = in;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    public void testAvailableAfterOpen() throws Exception {
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        try (ClosedInputStream cis = new ClosedInputStream()) {
            assertEquals(0, cis.available());
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testNonNull() throws Exception {
        assertSame(ClosedInputStream.INSTANCE, ClosedInputStream.ifNull(null));
        assertSame(ClosedInputStream.INSTANCE, ClosedInputStream.ifNull(ClosedInputStream.INSTANCE));
        assertSame(System.in, ClosedInputStream.ifNull(System.in));
    }

    @Test
    public void testRead() throws Exception {
        try (ClosedInputStream cis = new ClosedInputStream()) {
            assertEof(cis);
        }
    }

    @Test
    public void testReadAfterCose() throws Exception {
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        assertEquals(0, ClosedInputStream.INSTANCE.available());
        final InputStream shadow;
        try (InputStream in = new ClosedInputStream()) {
            assertEquals(0, in.available());
            shadow = in;
        }
        assertEquals(EOF, shadow.read());
    }

    @Test
    public void testReadArray() throws Exception {
        try (ClosedInputStream cis = new ClosedInputStream()) {
            assertEquals(EOF, cis.read(new byte[4096]));
            assertEquals(EOF, cis.read(new byte[1]));
            assertEquals(EOF, cis.read(new byte[0]));
        }
    }

    @Test
    public void testReadArrayIndex() throws Exception {
        try (ClosedInputStream cis = new ClosedInputStream()) {
            assertEquals(EOF, cis.read(new byte[4096], 0, 1));
            assertEquals(EOF, cis.read(new byte[1], 0, 1));
            assertEquals(EOF, cis.read(new byte[0], 0, 0));
        }
    }

    @Test
    public void testSingleton() throws Exception {
        try (@SuppressWarnings("deprecation")
        ClosedInputStream cis = ClosedInputStream.CLOSED_INPUT_STREAM) {
            assertEof(cis);
        }
        try (ClosedInputStream cis = ClosedInputStream.INSTANCE) {
            assertEof(cis);
        }
    }

}
