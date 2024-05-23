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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link BrokenInputStream}.
 */
public class BrokenInputStreamTest {

    private static BrokenInputStream createBrokenInputStream(final Throwable exception) {
        if (exception instanceof IOException) {
            return new BrokenInputStream((IOException) exception);
        }
        return new BrokenInputStream(exception);
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testAvailable(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenInputStream stream = createBrokenInputStream(exception);
        assertEquals(exception, assertThrows(clazz, () -> stream.available()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testClose(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenInputStream stream = createBrokenInputStream(exception);
        assertEquals(exception, assertThrows(clazz, () -> stream.close()));
    }

    @Test
    public void testInstance() {
        assertNotNull(BrokenInputStream.INSTANCE);
    }

    @Test
    public void testIO469() throws Throwable {
        // The exception handling and nested blocks here look ugly.
        // Do NOT try to rationalize them by combining them, using try-with-resources or assertThrows,
        // or any similar improvements one would make in normal code. This tests
        // a very specific bug that comes up in unusual exception structures like this.
        // If this is improved, that bug will no longer be tested.
        final InputStream in = new BrokenInputStream();
        Throwable localThrowable2 = null;
        try {
            try {
                in.read();
            } catch (final Throwable localThrowable1) {
                localThrowable2 = localThrowable1;
                throw localThrowable1;
            } finally {
                try {
                    in.close();
                } catch (final Throwable x2) {
                    localThrowable2.addSuppressed(x2);
                }
            }
        } catch (final IOException expected) {
            final Throwable[] suppressed = expected.getSuppressed();
            assertEquals(1, suppressed.length);
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testRead(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenInputStream stream = createBrokenInputStream(exception);
        assertEquals(exception, assertThrows(clazz, () -> stream.read()));
        assertEquals(exception, assertThrows(clazz, () -> stream.read(new byte[1])));
        assertEquals(exception, assertThrows(clazz, () -> stream.read(new byte[1], 0, 1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testReset(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenInputStream stream = createBrokenInputStream(exception);
        assertEquals(exception, assertThrows(clazz, () -> stream.reset()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testSkip(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenInputStream stream = createBrokenInputStream(exception);
        assertEquals(exception, assertThrows(clazz, () -> stream.skip(1)));
    }

    @Test
    public void testTryWithResources() {
        final IOException thrown = assertThrows(IOException.class, () -> {
            try (InputStream newStream = new BrokenInputStream()) {
                newStream.read();
            }
        });
        assertEquals("Broken input stream", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(IOException.class, suppressed[0].getClass());
        assertEquals("Broken input stream", suppressed[0].getMessage());
    }

}
