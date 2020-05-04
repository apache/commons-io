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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

import org.apache.commons.io.TaggedIOException;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TaggedWriter}.
 */
public class TaggedWriterTest  {

    @Test
    public void testNormalWriter() {
        try (final StringBuilderWriter buffer = new StringBuilderWriter()) {
            try (final Writer writer = new TaggedWriter(buffer)) {
                writer.write('a');
                writer.write(new char[] { 'b' });
                writer.write(new char[] { 'c' }, 0, 1);
                writer.flush();
            }
            assertEquals(3, buffer.getBuilder().length());
            assertEquals('a', buffer.getBuilder().charAt(0));
            assertEquals('b', buffer.getBuilder().charAt(1));
            assertEquals('c', buffer.getBuilder().charAt(2));
        } catch (final IOException e) {
            fail("Unexpected exception thrown");
        }
    }

    @Test
    public void testBrokenWriter() {
        final IOException exception = new IOException("test exception");
        final TaggedWriter writer =
            new TaggedWriter(new BrokenWriter(exception));

        // Test the write() method
        try {
            writer.write(new char[] { 'x' }, 0, 1);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(writer.isCauseOf(e));
            try {
                writer.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the flush() method
        try {
            writer.flush();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(writer.isCauseOf(e));
            try {
                writer.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }

        // Test the close() method
        try {
            writer.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertTrue(writer.isCauseOf(e));
            try {
                writer.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertEquals(exception, e2);
            }
        }
    }

    @Test
    public void testOtherException() throws Exception {
        final IOException exception = new IOException("test exception");
        try (final TaggedWriter writer = new TaggedWriter(ClosedWriter.CLOSED_WRITER)) {

            assertFalse(writer.isCauseOf(exception));
            assertFalse(writer.isCauseOf(new TaggedIOException(exception, UUID.randomUUID())));

            try {
                writer.throwIfCauseOf(exception);
            } catch (final IOException e) {
                fail("Unexpected exception thrown");
            }

            try {
                writer.throwIfCauseOf(new TaggedIOException(exception, UUID.randomUUID()));
            } catch (final IOException e) {
                fail("Unexpected exception thrown");
            }
        }
    }

}
