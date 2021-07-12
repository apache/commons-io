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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UncheckedFilterInputStream}.
 */
public class UncheckedFilterInputStreamTest {

    private UncheckedFilterInputStream stringInputStream;
    private UncheckedFilterInputStream brokenInputStream;
    private IOException exception = new IOException("test exception");

    @SuppressWarnings("resource")
    @BeforeEach
    public void beforeEach() {
        stringInputStream = UncheckedFilterInputStream.on(new BufferedInputStream(new StringInputStream("01")));
        exception = new IOException("test exception");
        brokenInputStream = UncheckedFilterInputStream.on(new BrokenInputStream(exception));
    }

    @Test
    public void testClose() {
        stringInputStream.close();
        assertThrows(UncheckedIOException.class, () -> brokenInputStream.read());
    }

    @Test
    public void testCloseThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenInputStream.close()).getCause());
    }

    @Test
    public void testMarkReset() {
        stringInputStream.mark(10);
        final int c = stringInputStream.read();
        stringInputStream.reset();
        assertEquals(c, stringInputStream.read());
    }

    @Test
    public void testRead() {
        try (final UncheckedFilterInputStream uncheckedReader = UncheckedFilterInputStream.on(stringInputStream)) {
            assertEquals('0', uncheckedReader.read());
            assertEquals('1', uncheckedReader.read());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
            assertEquals(IOUtils.EOF, uncheckedReader.read());
        }
    }

    @Test
    public void testReadByteArray() {
        try (final UncheckedFilterInputStream uncheckedReader = UncheckedFilterInputStream.on(stringInputStream)) {
            final byte[] array = new byte[1];
            assertEquals(1, uncheckedReader.read(array));
            assertEquals('0', array[0]);
            array[0] = 0;
            assertEquals(1, uncheckedReader.read(array));
            assertEquals('1', array[0]);
            array[0] = 0;
            assertEquals(IOUtils.EOF, uncheckedReader.read(array));
            assertEquals(0, array[0]);
            assertEquals(IOUtils.EOF, uncheckedReader.read(array));
            assertEquals(0, array[0]);
        }
    }

    @Test
    public void testReadByteArrayIndexed() {
        try (final UncheckedFilterInputStream uncheckedReader = UncheckedFilterInputStream.on(stringInputStream)) {
            final byte[] array = new byte[1];
            assertEquals(1, uncheckedReader.read(array, 0, 1));
            assertEquals('0', array[0]);
            array[0] = 0;
            assertEquals(1, uncheckedReader.read(array, 0, 1));
            assertEquals('1', array[0]);
            array[0] = 0;
            assertEquals(IOUtils.EOF, uncheckedReader.read(array, 0, 1));
            assertEquals(0, array[0]);
            assertEquals(IOUtils.EOF, uncheckedReader.read(array, 0, 1));
            assertEquals(0, array[0]);
        }
    }

    @Test
    public void testReadByteArrayIndexedThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenInputStream.read(new byte[1], 0, 1)).getCause());
    }

    @Test
    public void testReadByteArrayThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenInputStream.read(new byte[1])).getCause());
    }

    @Test
    public void testReadThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenInputStream.read()).getCause());
    }

    @Test
    public void testResetThrows() {
        try (UncheckedFilterInputStream closedReader = UncheckedFilterInputStream.on(ClosedInputStream.INSTANCE)) {
            closedReader.close();
            assertThrows(UncheckedIOException.class, () -> brokenInputStream.reset());
        }
    }

    @Test
    public void testSkip() {
        assertEquals(1, stringInputStream.skip(1));
    }

    @Test
    public void testSkipThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenInputStream.skip(1)).getCause());
    }

}
