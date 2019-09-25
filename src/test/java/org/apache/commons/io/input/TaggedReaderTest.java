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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

import org.apache.commons.io.TaggedIOException;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TaggedReader}.
 */
public class TaggedReaderTest {

    @Test
    public void testEmptyReader() throws IOException {
        final Reader reader = new TaggedReader(new ClosedReader());
        assertFalse(reader.ready());
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read(new char[1]));
        assertEquals(-1, reader.read(new char[1], 0, 1));
        reader.close();
    }

    @Test
    public void testNormalReader() throws IOException {
        final Reader reader = new TaggedReader(new StringReader("abc"));
        assertTrue(reader.ready());
        assertEquals('a', reader.read());
        final char[] buffer = new char[1];
        assertEquals(1, reader.read(buffer));
        assertEquals('b', buffer[0]);
        assertEquals(1, reader.read(buffer, 0, 1));
        assertEquals('c', buffer[0]);
        assertEquals(-1, reader.read());
        reader.close();
    }

    @Test
    public void testBrokenReader() {
        final IOException exception = new IOException("test exception");
        final TaggedReader reader = new TaggedReader(new BrokenReader(exception));

        // Test the ready() method
        try {
            reader.ready();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(reader.isCauseOf(e));
            try {
                reader.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the read() method
        try {
            reader.read();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(reader.isCauseOf(e));
            try {
                reader.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the close() method
        try {
            reader.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(reader.isCauseOf(e));
            try {
                reader.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }
    }

    @Test
    public void testOtherException() throws Exception {
        final IOException exception = new IOException("test exception");
        final Reader closed = new ClosedReader();
        final TaggedReader reader = new TaggedReader(closed);

        assertFalse(reader.isCauseOf(exception));
        assertFalse(reader.isCauseOf(new TaggedIOException(exception, UUID.randomUUID())));

        reader.throwIfCauseOf(exception);

        reader.throwIfCauseOf(new TaggedIOException(exception, UUID.randomUUID()));
        reader.close();
    }

}
