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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link BrokenWriter}.
 */
public class BrokenWriterTest {

    private IOException exception;

    private Writer writer;

    @BeforeEach
    public void setUp() {
        exception = new IOException("test exception");
        writer = new BrokenWriter(exception);
    }

    @Test
    public void testWrite() {
        try {
            writer.write(1);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }

        try {
            writer.write(new char[1]);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }

        try {
            writer.write(new char[1], 0, 1);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

    @Test
    public void testFlush() {
        try {
            writer.flush();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

    @Test
    public void testClose() {
        try {
            writer.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

}
