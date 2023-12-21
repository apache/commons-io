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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link BrokenWriter}.
 */
public class BrokenWriterTest {

    private static BrokenWriter createBrokenWriter(final Throwable exception) {
        if (exception instanceof IOException) {
            return new BrokenWriter((IOException) exception);
        }
        return new BrokenWriter(exception);
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testAppendChar(final Class<Exception> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.append('1')));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testAppendCharSequence(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.append("01")));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testAppendCharSequenceIndexed(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.append("01", 0, 1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testClose(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.close()));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testFlush(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.flush()));
    }

    @Test
    public void testInstance() {
        assertNotNull(BrokenWriter.INSTANCE);
    }

    @Test
    public void testTryWithResources() {
        final IOException thrown = assertThrows(IOException.class, () -> {
            try (Writer newWriter = new BrokenWriter()) {
                newWriter.write(1);
            }
        });
        assertEquals("Broken writer", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(IOException.class, suppressed[0].getClass());
        assertEquals("Broken writer", suppressed[0].getMessage());
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testWriteCharArray(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.write(new char[1])));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testWriteCharArrayIndexed(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.write(new char[1], 0, 1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testWriteInt(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.write(1)));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testWriteString(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.write("01")));
    }

    @ParameterizedTest
    @MethodSource("org.apache.commons.io.BrokenTestFactories#parameters")
    public void testWriteStringIndexed(final Class<Throwable> clazz) throws Exception {
        final Throwable exception = clazz.newInstance();
        @SuppressWarnings("resource")
        final BrokenWriter brokenWriter = createBrokenWriter(exception);
        assertEquals(exception, assertThrows(clazz, () -> brokenWriter.write("01", 0, 1)));
    }

}
