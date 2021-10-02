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
import java.io.Reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link BrokenReader}.
 */
public class BrokenReaderTest {

    private IOException exception;

    private Reader brokenReader;

    @BeforeEach
    public void setUp() {
        exception = new IOException("test exception");
        brokenReader = new BrokenReader(exception);
    }

    @Test
    public void testClose() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.close()));
    }

    @Test
    public void testInstance() {
        assertNotNull(BrokenReader.INSTANCE);
    }

    @Test
    public void testMark() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.mark(1)));
    }

    @Test
    public void testRead() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.read()));
    }

    @Test
    public void testReadCharArray() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.read(new char[1])));
    }

    @Test
    public void testReadCharArrayIndexed() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.read(new char[1], 0, 1)));
    }

    @Test
    public void testReady() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.ready()));
    }

    @Test
    public void testReset() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.reset()));
    }

    @Test
    public void testSkip() {
        assertEquals(exception, assertThrows(IOException.class, () -> brokenReader.skip(1)));
    }

    @Test
    public void testTryWithResources() {
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
