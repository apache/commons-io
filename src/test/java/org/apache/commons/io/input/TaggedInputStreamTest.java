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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.TaggedIOException;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TaggedInputStream}.
 */
public class TaggedInputStreamTest  {

    @Test
    public void testEmptyStream() throws IOException {
        final InputStream stream = new TaggedInputStream(ClosedInputStream.CLOSED_INPUT_STREAM);
        assertEquals(0, stream.available());
        assertEquals(-1, stream.read());
        assertEquals(-1, stream.read(new byte[1]));
        assertEquals(-1, stream.read(new byte[1], 0, 1));
        stream.close();
    }

    @Test
    public void testNormalStream() throws IOException {
        final InputStream stream = new TaggedInputStream(
                new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));
        assertEquals(3, stream.available());
        assertEquals('a', stream.read());
        final byte[] buffer = new byte[1];
        assertEquals(1, stream.read(buffer));
        assertEquals('b', buffer[0]);
        assertEquals(1, stream.read(buffer, 0, 1));
        assertEquals('c', buffer[0]);
        assertEquals(-1, stream.read());
        stream.close();
    }

    @Test
    public void testBrokenStream() {
        final IOException exception = new IOException("test exception");
        final TaggedInputStream stream =
            new TaggedInputStream(new BrokenInputStream(exception));

        // Test the available() method
        try {
            stream.available();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(stream.isCauseOf(e));
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the read() method
        try {
            stream.read();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(stream.isCauseOf(e));
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the close() method
        try {
            stream.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(stream.isCauseOf(e));
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }
    }

    @Test
    public void testOtherException() throws Exception {
        final IOException exception = new IOException("test exception");
        final TaggedInputStream stream = new TaggedInputStream(ClosedInputStream.CLOSED_INPUT_STREAM);

        assertFalse(stream.isCauseOf(exception));
        assertFalse(stream.isCauseOf(
                new TaggedIOException(exception, UUID.randomUUID())));

        stream.throwIfCauseOf(exception);

        stream.throwIfCauseOf(
                    new TaggedIOException(exception, UUID.randomUUID()));
        stream.close();
    }

}
