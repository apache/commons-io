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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BrokenWriter}.
 */
class UncheckedFilterWriterTest {

    private IOException exception;

    private UncheckedFilterWriter brokenWriter;
    private UncheckedFilterWriter stringWriter;

    @SuppressWarnings("resource")
    @BeforeEach
    public void setUp() throws IOException {
        exception = new IOException("test exception");
        brokenWriter = UncheckedFilterWriter.builder().setWriter(new BrokenWriter(exception)).get();
        stringWriter = UncheckedFilterWriter.builder().setWriter(new StringWriter()).get();
    }

    @SuppressWarnings("resource")
    @Test
    void testAppendChar() {
        stringWriter.append('1');
    }

    @SuppressWarnings("resource")
    @Test
    void testAppendCharSequence() {
        stringWriter.append("01");
    }

    @SuppressWarnings("resource")
    @Test
    void testAppendCharSequenceIndexed() {
        stringWriter.append("01", 0, 1);
    }

    @Test
    void testAppendCharSequenceIndexedThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.append("01", 0, 1)).getCause());
    }

    @Test
    void testAppendCharSequenceThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.append("01")).getCause());
    }

    @Test
    void testAppendCharThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.append('1')).getCause());
    }

    @Test
    void testClose() {
        stringWriter.close();
    }

    @Test
    void testCloseThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.close()).getCause());
    }

    @Test
    void testEquals() {
        stringWriter.equals(null);
    }

    @Test
    @Disabled("What should happen here?")
    void testEqualsThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.equals(null)).getCause());
    }

    @Test
    void testFlush() {
        stringWriter.flush();
    }

    @Test
    void testFlushThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.flush()).getCause());
    }

    @Test
    void testHashCode() {
        stringWriter.hashCode();
    }

    @Test
    @Disabled("What should happen here?")
    void testHashCodeThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.hashCode()).getCause());
    }

    @Test
    void testToString() {
        stringWriter.toString();
    }

    @Test
    @Disabled("What should happen here?")
    void testToStringThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.toString()).getCause());
    }

    @Test
    void testWriteCharArray() {
        stringWriter.write(new char[1]);
    }

    @Test
    void testWriteCharArrayIndexed() {
        stringWriter.write(new char[1], 0, 1);
    }

    @Test
    void testWriteCharArrayIndexedThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.write(new char[1], 0, 1)).getCause());
    }

    @Test
    void testWriteCharArrayThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.write(new char[1])).getCause());
    }

    @Test
    void testWriteInt() {
        stringWriter.write(1);
    }

    @Test
    void testWriteIntThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.write(1)).getCause());
    }

    @Test
    void testWriteString() {
        stringWriter.write("01");
    }

    @Test
    void testWriteStringIndexed() {
        stringWriter.write("01", 0, 1);
    }

    @Test
    void testWriteStringIndexedThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.write("01", 0, 1)).getCause());
    }

    @Test
    void testWriteStringThrows() {
        assertEquals(exception, assertThrows(UncheckedIOException.class, () -> brokenWriter.write("01")).getCause());
    }
}
