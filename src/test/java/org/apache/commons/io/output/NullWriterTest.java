/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Really not a lot to do here, but checking that no
 * Exceptions are thrown.
 */
class NullWriterTest {

    private static final String TEST_STRING = "ABC";
    private static final char[] TEST_CHARS = TEST_STRING.toCharArray();

    @Test
    void testAppendChar() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            assertSame(writer, writer.append('X'));
        }
    }

    @Test
    void testAppendCharSequence() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            assertSame(writer, writer.append(TEST_STRING));
            assertSame(writer, writer.append(null));
        }
    }

    @Test
    void testAppendCharSequenceWithRange() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            assertSame(writer, writer.append(TEST_STRING, 1, 2));
            assertSame(writer, writer.append(null, 0, 4));
            // Test argument validation
            assertThrows(IndexOutOfBoundsException.class, () -> writer.append(TEST_STRING, -1, 2));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.append(TEST_STRING, 1, 5));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.append(TEST_STRING, 2, 1));
        }
    }

    @Test
    void testCloseNoOp() {
        final NullWriter writer = NullWriter.INSTANCE;
        writer.close();
        writer.write(TEST_CHARS);
    }

    @Test
    void testFlush() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.flush();
        }
    }

    @Test
    void testWriteCharArray() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.write(TEST_CHARS);
            // Test argument validation
            assertThrows(NullPointerException.class, () -> writer.write((char[]) null));
        }
    }

    @Test
    void testWriteCharArrayWithOffset() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.write(TEST_CHARS, 1, 2);
            // Test argument validation
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_CHARS, -1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_CHARS, 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_CHARS, 0, 4));
            assertThrows(NullPointerException.class, () -> writer.write((char[]) null, 0, 0));
        }
    }

    @Test
    void testWriteInt() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.write(42);
        }
    }

    @Test
    void testWriteString() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.write(TEST_STRING);
            // Test argument validation
            assertThrows(NullPointerException.class, () -> writer.write((String) null));
        }
    }

    @Test
    void testWriteStringWithOffset() {
        try (NullWriter writer = NullWriter.INSTANCE) {
            writer.write(TEST_STRING, 1, 1);
            // Test argument validation
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_STRING, -1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_STRING, 0, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> writer.write(TEST_STRING, 0, 4));
            assertThrows(NullPointerException.class, () -> writer.write((String) null, 0, 0));
        }
    }
}
