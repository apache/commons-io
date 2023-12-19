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

import static org.apache.commons.io.output.BrokenWriter.brokenWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BrokenWriter}.
 */
public class BrokenWriterTest {

    @Test
    public void testIOExceptionAppendChar() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).append('1')));
    }

    @Test
    public void testRuntimeExceptionAppendChar() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).append('1')));
    }

    @Test
    public void testIOExceptionAppendCharSequence() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).append("01")));
    }

    @Test
    public void testRuntimeExceptionAppendCharSequence() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).append("01")));
    }

    @Test
    public void testIOExceptionAppendCharSequenceIndexed() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).append("01", 0, 1)));
    }

    @Test
    public void testRuntimeExceptionAppendCharSequenceIndexed() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).append("01", 0, 1)));
    }

    @Test
    public void testIOExceptionClose() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).close()));
    }

    @Test
    public void testRuntimeExceptionClose() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).close()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testIOExceptionEquals() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).equals(null)));
    }

    @Test
    @Disabled("What should happen here?")
    public void testRuntimeExceptionEquals() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).equals(null)));
    }

    @Test
    public void testIOExceptionFlush() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).flush()));
    }

    @Test
    public void testRuntimeExceptionFlush() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).flush()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testIOExceptionHashCode() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).hashCode()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testRuntimeExceptionHashCode() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).hashCode()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testIOExceptionToString() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).toString()));
    }

    @Test
    @Disabled("What should happen here?")
    public void testRuntimeExceptionToString() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).toString()));
    }

    @Test
    public void testIOExceptionTryWithResources() {
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
    public void testRuntimeExceptionTryWithResources() {
        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (Writer newWriter = brokenWriter(() -> new RuntimeException("test exception"))) {
                newWriter.write(1);
            }
        });
        assertEquals("test exception", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(RuntimeException.class, suppressed[0].getClass());
        assertEquals("test exception", suppressed[0].getMessage());
    }

    @Test
    public void testIOExceptionWriteCharArray() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).write(new char[1])));
    }

    @Test
    public void testRuntimeExceptionWriteCharArray() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).write(new char[1])));
    }

    @Test
    public void testIOExceptionWriteCharArrayIndexed() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).write(new char[1], 0, 1)));
    }

    @Test
    public void testRuntimeExceptionWriteCharArrayIndexed() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).write(new char[1], 0, 1)));
    }

    @Test
    public void testIOExceptionWriteInt() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).write(1)));
    }

    @Test
    public void testRuntimeExceptionWriteInt() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).write(1)));
    }

    @Test
    public void testIOExceptionWriteString() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).write("01")));
    }

    @Test
    public void testRuntimeExceptionWriteString() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).write("01")));
    }

    @Test
    public void testIOExceptionWriteStringIndexed() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenWriter(exception).write("01", 0, 1)));
    }

    @Test
    public void testRuntimeExceptionWriteStringIndexed() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenWriter(exception).write("01", 0, 1)));
    }

}
