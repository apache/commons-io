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
 * WITHOUProxyInputStreamFixture WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.test.CustomIOException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ProxyInputStream}.
 *
 * @param <T> The actual type tested.
 */
public class ProxyInputStreamTest<T extends ProxyInputStream> {

    private static final class ProxyInputStreamFixture extends ProxyInputStream {

        static class Builder extends ProxyInputStream.AbstractBuilder<ProxyInputStreamFixture, Builder> {

            @Override
            public ProxyInputStreamFixture get() throws IOException {
                return new ProxyInputStreamFixture(this);
            }

        }

        static Builder builder() {
            return new Builder();
        }

        ProxyInputStreamFixture(final Builder builder) throws IOException {
            super(builder);
        }

        ProxyInputStreamFixture(final InputStream proxy) {
            super(proxy);
        }
    }

    @SuppressWarnings("resource")
    static <T, B extends AbstractStreamBuilder<T, B>> void testCloseHandleIOException(final AbstractStreamBuilder<T, B> builder) throws IOException {
        final IOException exception = new IOException();
        testCloseHandleIOException((ProxyInputStream) builder.setInputStream(new BrokenInputStream(() -> exception)).get());
    }

    @SuppressWarnings("resource")
    static void testCloseHandleIOException(final ProxyInputStream inputStream) throws IOException {
        assertFalse(inputStream.isClosed(), "closed");
        final ProxyInputStream spy = spy(inputStream);
        assertThrows(IOException.class, spy::close);
        final BrokenInputStream unwrap = (BrokenInputStream) inputStream.unwrap();
        verify(spy).handleIOException((IOException) unwrap.getThrowable());
        assertFalse(spy.isClosed(), "closed");
    }

    /**
     * Asserts that a ProxyInputStream's markSupported() equals the proxied value.
     *
     * @param inputStream The stream to test.
     */
    @SuppressWarnings("resource") // unwrap() is a getter
    protected void assertMarkSupportedEquals(final ProxyInputStream inputStream) {
        assertNotNull(inputStream, "inputStream");
        assertEquals(inputStream.unwrap().markSupported(), inputStream.markSupported());
    }

    @SuppressWarnings({ "resource", "unused", "unchecked" }) // For subclasses
    protected T createFixture() throws IOException {
        return (T) new ProxyInputStreamFixture(createOriginInputStream());
    }

    @SuppressWarnings("unchecked")
    protected T createFixture(final InputStream proxy) {
        return (T) new ProxyInputStreamFixture(proxy);
    }

    protected InputStream createOriginInputStream() {
        return CharSequenceInputStream.builder().setCharSequence("abc").get();
    }

    @SuppressWarnings("resource")
    @Test
    public void testAvailableAfterClose() throws IOException {
        final T shadow;
        try (T inputStream = createFixture()) {
            shadow = inputStream;
        }
        assertEquals(0, shadow.available());
    }

    @Test
    public void testAvailableAfterOpen() throws IOException {
        try (T inputStream = createFixture()) {
            assertEquals(3, inputStream.available());
        }
    }

    @Test
    public void testAvailableAll() throws IOException {
        try (T inputStream = createFixture()) {
            assertEquals(3, inputStream.available());
            IOUtils.toByteArray(inputStream);
            assertEquals(0, inputStream.available());
        }
    }

    @Test
    public void testAvailableNull() throws IOException {
        try (T inputStream = createFixture(null)) {
            assertEquals(0, inputStream.available());
            inputStream.setReference(createFixture());
            assertEquals(3, inputStream.available());
            IOUtils.toByteArray(inputStream);
            assertEquals(0, inputStream.available());
            inputStream.setReference(null);
            assertEquals(0, inputStream.available());
        }
    }

    protected void testEos(final T inputStream) {
        // empty
    }

    //@Test
    public void testMarkOnNull() throws IOException {
        try (T inputStream = createFixture(null)) {
            inputStream.mark(1);
            inputStream.setReference(createFixture());
            inputStream.mark(1);
            IOUtils.toByteArray(inputStream);
            inputStream.mark(1);
            inputStream.setReference(null);
            inputStream.mark(1);
        }
    }

    @Test
    public void testMarkSupported() throws IOException {
        try (T inputStream = createFixture()) {
            assertMarkSupportedEquals(inputStream);
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testMarkSupportedAfterClose() throws IOException {
        final T shadow;
        try (T inputStream = createFixture()) {
            shadow = inputStream;
        }
        assertMarkSupportedEquals(shadow);
    }

    @Test
    public void testMarkSupportedOnNull() throws IOException {
        try (ProxyInputStream fixture = createFixture()) {
            assertMarkSupportedEquals(fixture);
            fixture.setReference(null);
            assertFalse(fixture.markSupported());
        }
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
    public void testReadAfterClose_ByteArrayInputStream() throws IOException {
        try (InputStream inputStream = new ProxyInputStreamFixture(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)))) {
            inputStream.close();
            // ByteArrayInputStream does not throw on a closed stream.
            assertNotEquals(IOUtils.EOF, inputStream.read());
        }
    }

    @Test
    public void testReadAfterClose_ChannelInputStream() throws IOException {
        try (InputStream inputStream = new ProxyInputStreamFixture(
                Files.newInputStream(Paths.get("src/test/resources/org/apache/commons/io/abitmorethan16k.txt")))) {
            inputStream.close();
            // ChannelInputStream throws when closed
            assertThrows(IOException.class, inputStream::read);
        }
    }

    @Test
    public void testReadAfterClose_CharSequenceInputStream() throws IOException {
        try (InputStream inputStream = createFixture()) {
            inputStream.close();
            // CharSequenceInputStream (like ByteArrayInputStream) does not throw on a closed stream.
            assertEquals(IOUtils.EOF, inputStream.read());
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

    @Test
    public void testSubclassAfterReadConsumer() throws Exception {
        final byte[] hello = "Hello".getBytes(StandardCharsets.UTF_8);
        final AtomicBoolean boolRef = new AtomicBoolean();
        // @formatter:off
        try (ProxyInputStreamFixture bounded = ProxyInputStreamFixture.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setAfterRead(null) // should not blow up
                .setAfterRead(i -> boolRef.set(true))
                .get()) {
            IOUtils.consume(bounded);
        }
        // @formatter:on
        assertTrue(boolRef.get());
        // Throwing
        final String message = "test exception message";
        // @formatter:off
        try (ProxyInputStreamFixture bounded = ProxyInputStreamFixture.builder()
                .setInputStream(new ByteArrayInputStream(hello))
                .setAfterRead(i -> {
                    throw new CustomIOException(message);
                })
                .get()) {
            assertEquals(message, assertThrowsExactly(CustomIOException.class, () -> IOUtils.consume(bounded)).getMessage());
        }
        // @formatter:on
    }

}
