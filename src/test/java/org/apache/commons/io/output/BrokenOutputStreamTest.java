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

import static org.apache.commons.io.output.BrokenOutputStream.brokenOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BrokenOutputStream}.
 */
public class BrokenOutputStreamTest {

    @Test
    public void testIOExceptionClose() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenOutputStream(exception).close()));
    }

    @Test
    public void testRuntimeExceptionClose() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenOutputStream(exception).close()));
    }

    @Test
    public void testIOExceptionFlush() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenOutputStream(exception).flush()));
    }

    @Test
    public void testRuntimeExceptionFlush() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenOutputStream(exception).flush()));
    }

    @Test
    public void testInstance() {
        assertNotNull(BrokenOutputStream.INSTANCE);
    }

    @Test
    public void testIOExceptionTryWithResources() {
        final IOException thrown = assertThrows(IOException.class, () -> {
            try (OutputStream newStream = new BrokenOutputStream()) {
                newStream.write(1);
            }
        });
        assertEquals("Broken output stream", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(IOException.class, suppressed[0].getClass());
        assertEquals("Broken output stream", suppressed[0].getMessage());
    }

    @Test
    public void testRuntimeExceptionTryWithResources() {
        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (OutputStream newStream = brokenOutputStream(() -> new RuntimeException("test exception"))) {
                newStream.write(1);
            }
        });
        assertEquals("test exception", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(RuntimeException.class, suppressed[0].getClass());
        assertEquals("test exception", suppressed[0].getMessage());
    }

    @Test
    public void testIOExceptionWriteByteArray() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenOutputStream(exception).write(new byte[1])));
    }

    @Test
    public void testRuntimeExceptionWriteByteArray() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenOutputStream(exception).write(new byte[1])));
    }

    @Test
    public void testIOExceptionWriteByteArrayIndexed() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenOutputStream(exception).write(new byte[1], 0, 1)));
    }

    @Test
    public void testRuntimeExceptionWriteByteArrayIndexed() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenOutputStream(exception).write(new byte[1], 0, 1)));
    }

    @Test
    public void testIOExceptionWriteInt() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenOutputStream(exception).write(1)));
    }

    @Test
    public void testRuntimeExceptionWriteInt() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenOutputStream(exception).write(1)));
    }

}
