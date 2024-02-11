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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ProxyInputStream}.
 *
 * @param <T> The actual type tested.
 */
public class ProxyInputStreamTest<T extends ProxyInputStream> {

    private static final class ProxyInputStreamFixture extends ProxyInputStream {

        public ProxyInputStreamFixture(final InputStream proxy) {
            super(proxy);
        }

    }

    @SuppressWarnings({ "resource", "unused" }) // For subclasses
    protected T createFixture() throws IOException {
        return (T) new ProxyInputStreamFixture(createProxySource());
    }

    protected InputStream createProxySource() {
        return CharSequenceInputStream.builder().setCharSequence("abc").get();
    }

    protected void testEos(final T inputStream) {
        // empty
    }

    @Test
    public void testRead() throws IOException {
        try (T inputStream = createFixture()) {
            int found = inputStream.read();
            assertEquals('a', found);
            found = inputStream.read();
            assertEquals('b', found);
            found = inputStream.read();
            assertEquals('c', found);
            found = inputStream.read();
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadArrayAtMiddleFully() throws IOException {
        try (T inputStream = createFixture()) {
            final byte[] dest = new byte[5];
            int found = inputStream.read(dest, 2, 3);
            assertEquals(3, found);
            assertArrayEquals(new byte[] { 0, 0, 'a', 'b', 'c' }, dest);
            found = inputStream.read(dest, 2, 3);
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadArrayAtStartFully() throws IOException {
        try (T inputStream = createFixture()) {
            final byte[] dest = new byte[5];
            int found = inputStream.read(dest, 0, 5);
            assertEquals(3, found);
            assertArrayEquals(new byte[] { 'a', 'b', 'c', 0, 0 }, dest);
            found = inputStream.read(dest, 0, 5);
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadArrayAtStartPartial() throws IOException {
        try (T inputStream = createFixture()) {
            final byte[] dest = new byte[5];
            int found = inputStream.read(dest, 0, 2);
            assertEquals(2, found);
            assertArrayEquals(new byte[] { 'a', 'b', 0, 0, 0 }, dest);
            Arrays.fill(dest, (byte) 0);
            found = inputStream.read(dest, 0, 2);
            assertEquals(1, found);
            assertArrayEquals(new byte[] { 'c', 0, 0, 0, 0 }, dest);
            found = inputStream.read(dest, 0, 2);
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadArrayFully() throws IOException {
        try (T inputStream = createFixture()) {
            final byte[] dest = new byte[5];
            int found = inputStream.read(dest);
            assertEquals(3, found);
            assertArrayEquals(new byte[] { 'a', 'b', 'c', 0, 0 }, dest);
            found = inputStream.read(dest);
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadArrayPartial() throws IOException {
        try (T inputStream = createFixture()) {
            final byte[] dest = new byte[2];
            int found = inputStream.read(dest);
            assertEquals(2, found);
            assertArrayEquals(new byte[] { 'a', 'b' }, dest);
            Arrays.fill(dest, (byte) 0);
            found = inputStream.read(dest);
            assertEquals(1, found);
            assertArrayEquals(new byte[] { 'c', 0 }, dest);
            found = inputStream.read(dest);
            assertEquals(-1, found);
            testEos(inputStream);
        }
    }

    @Test
    public void testReadEof() throws Exception {
        final ByteArrayInputStream proxy = new ByteArrayInputStream(new byte[2]);
        try (ProxyInputStream inputStream = new ProxyInputStreamFixture(proxy)) {
            assertSame(proxy, inputStream.unwrap());
            int found = inputStream.read();
            assertEquals(0, found);
            found = inputStream.read();
            assertEquals(0, found);
            found = inputStream.read();
            assertEquals(-1, found);
        }
    }

}
