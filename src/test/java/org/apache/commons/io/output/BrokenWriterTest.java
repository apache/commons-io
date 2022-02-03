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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link BrokenWriter}.
 */
public class BrokenWriterTest {

    private IOException exception;

    private Writer brokenWriter;

    @BeforeEach
    public void setUp() {
        exception = new IOException("test exception");
        brokenWriter = new BrokenWriter(exception);
    }

    @Test
    public void testAppendChar() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.append('1')));
    }

    @Test
    public void testAppendCharSequence() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.append("01")));
    }

    @Test
    public void testAppendCharSequenceIndexed() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.append("01", 0, 1)));
    }

    @Test
    public void testClose() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.close()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testEquals() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.equals(null)));
    }

    @Test
    public void testFlush() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.flush()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testHashCode() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.hashCode()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testToString() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.toString()));
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

    @Test
    public void testWriteCharArray() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.write(new char[1])));
    }

    @Test
    public void testWriteCharArrayIndexed() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.write(new char[1], 0, 1)));
    }

    @Test
    public void testWriteInt() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.write(1)));
    }

    @Test
    public void testWriteString() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.write("01")));
    }

    @Test
    public void testWriteStringIndexed() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenWriter.write("01", 0, 1)));
    }

}
