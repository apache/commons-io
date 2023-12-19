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

import static org.apache.commons.io.input.BrokenReader.brokenReader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BrokenReader}.
 */
public class BrokenReaderTest {

    @Test
    public void testIOExceptionClose() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).close()));
    }

    @Test
    public void testRuntimeExceptionClose() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).close()));
    }

    @Test
    public void testInstance() {
        assertNotNull(BrokenReader.INSTANCE);
    }

    @Test
    public void testIOExceptionMark() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).mark(1)));
    }

    @Test
    public void testRuntimeExceptionMark() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).mark(1)));
    }

    @Test
    public void testIOExceptionRead() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).read()));
    }

    @Test
    public void testRuntimeExceptionRead() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).read()));
    }

    @Test
    public void testIOExceptionReadCharArray() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).read(new char[1])));
    }

    @Test
    public void testRuntimeExceptionReadCharArray() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).read(new char[1])));
    }

    @Test
    public void testIOExceptionReadCharArrayIndexed() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).read(new char[1], 0, 1)));
    }

    @Test
    public void testRuntimeExceptionReadCharArrayIndexed() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).read(new char[1], 0, 1)));
    }

    @Test
    public void testIOExceptionReady() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).ready()));
    }

    @Test
    public void testRuntimeExceptionReady() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).ready()));
    }

    @Test
    public void testIOExceptionReset() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).reset()));
    }

    @Test
    public void testRuntimeExceptionReset() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).reset()));
    }

    @Test
    public void testIOExceptionSkip() {
        final IOException exception = new IOException("test exception");
        assertEquals(exception, assertThrows(IOException.class, () -> new BrokenReader(exception).skip(1)));
    }

    @Test
    public void testRuntimeExceptionSkip() {
        final RuntimeException exception = new RuntimeException("test exception");
        assertEquals(exception, assertThrows(RuntimeException.class, () -> new BrokenReader(exception).skip(1)));
    }

    @Test
    public void testIOExceptionTryWithResources() {
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

    @Test
    public void testRuntimeExceptionTryWithResources() {
        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try (Reader newReader = brokenReader(() -> new RuntimeException("test exception"))) {
                newReader.read();
            }
        });
        assertEquals("test exception", thrown.getMessage());

        final Throwable[] suppressed = thrown.getSuppressed();
        assertEquals(1, suppressed.length);
        assertEquals(RuntimeException.class, suppressed[0].getClass());
        assertEquals("test exception", suppressed[0].getMessage());
    }

}
