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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link BrokenReader}.
 */
class BrokenReaderTest {

    private static BrokenReader createBrokenReader(final Throwable exception) {
        if (exception instanceof IOException) {
            return new BrokenReader((IOException) exception);
        }
        return new BrokenReader(exception);
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testClose(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.close()));
    }

    @Test
    void testInstance() {
        assertNotNull(BrokenReader.INSTANCE);
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testMark(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.mark(1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testRead(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.read()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testReadCharArray(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.read(new char[1])));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testReadCharArrayIndexed(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.read(new char[1], 0, 1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testReady(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.ready()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testReset(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.reset()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    void testSkip(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenReader brokenReader = createBrokenReader(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenReader.skip(1)));
    }

    @Test
    void testTryWithResources() {
        final IOException thrown = assertThrows(IOException.class, () -> {
            try (Reader newReader = new BrokenReader()) {
                newReader.read();
            }
        });
        assertEquals("Broken reader", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(IOException.class, suppressed[0].getClass());
        assertEquals("Broken reader", suppressed[0].getMessage());
    }

}
