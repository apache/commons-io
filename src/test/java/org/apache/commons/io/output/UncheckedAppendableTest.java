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
import java.io.StringWriter;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link UncheckedAppendable}.
 */
public class UncheckedAppendableTest {

    private IOException exception;

    private UncheckedAppendable appendableBroken;
    private UncheckedAppendable appendableString;

    @SuppressWarnings("resource")
    @BeforeEach
    public void setUp() {
        exception = new IOException("test exception");
        appendableBroken = UncheckedAppendable.on(new BrokenWriter(exception));
        appendableString = UncheckedAppendable.on(new StringWriter());
    }

    @Test
    public void testAppendChar() {
        appendableString.append('a').append('b');
        assertEquals("ab", appendableString.toString());
    }

    @Test
    public void testAppendCharSequence() {
        appendableString.append("a").append("b");
        assertEquals("ab", appendableString.toString());
    }

    @Test
    public void testAppendCharSequenceIndexed() {
        appendableString.append("a", 0, 1).append("b", 0, 1);
        assertEquals("ab", appendableString.toString());
    }

    @Test
    public void testAppendCharSequenceIndexedThrows() {
        try {
            appendableBroken.append("a", 0, 1);
            fail("Expected exception not thrown.");
        } catch (final UncheckedIOException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void testAppendCharSequenceThrows() {
        try {
            appendableBroken.append("a");
            fail("Expected exception not thrown.");
        } catch (final UncheckedIOException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void testAppendCharThrows() {
        try {
            appendableBroken.append('a');
            fail("Expected exception not thrown.");
        } catch (final UncheckedIOException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void testToString() {
        assertEquals("ab", UncheckedAppendable.on(new StringWriter(2).append("ab")).toString());
    }

}
