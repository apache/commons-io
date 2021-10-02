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

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link BrokenInputStream}.
 */
public class BrokenInputStreamTest {

    private IOException exception;

    private InputStream stream;

    @BeforeEach
    public void setUp() {
        exception = new IOException("test exception");
        stream = new BrokenInputStream(exception);
    }

    @Test
    public void testAvailable() {
        assertEquals(exception, assertThrows(IOException.class, () -> stream.available()));
    }

    @Test
    public void testClose() {
        assertEquals(exception, assertThrows(IOException.class, () -> stream.close()));
    }

    @Test
    public void testRead() {
        assertEquals(exception, assertThrows(IOException.class, () -> stream.read()));
        assertEquals(exception, assertThrows(IOException.class, () -> stream.read(new byte[1])));
        assertEquals(exception, assertThrows(IOException.class, () -> stream.read(new byte[1], 0, 1)));
    }

    @Test
    public void testReset() {
        assertEquals(exception, assertThrows(IOException.class, () -> stream.reset()));
    }

    @Test
    public void testSkip() {
        assertEquals(exception, assertThrows(IOException.class, () -> stream.skip(1)));
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
