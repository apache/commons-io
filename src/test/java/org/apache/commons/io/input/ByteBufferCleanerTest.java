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
package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.JRE.JAVA_23;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;

/**
 * Tests {@code ByteBufferCleaner}.
 */
class ByteBufferCleanerTest {

    @Test
    void testCleanEmpty() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        // There is no way verify that the buffer has been cleaned up, we are just verifying that
        // clean() doesn't blow up
        ByteBufferCleaner.clean(buffer);
        verifyCleared(buffer);
    }

    @Test
    void testCleanFull() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.putLong(Long.MAX_VALUE);
        verifyUncleared(buffer);
        // There is no way verify that the buffer has been cleaned up, we are just verifying that
        // clean() doesn't blow up
        ByteBufferCleaner.clean(buffer);
        verifyCleared(buffer);
    }

    @Test
    void testCleanNonDirectBuffer() {
        assertDoesNotThrow(() -> ByteBufferCleaner.clean(ByteBuffer.allocate(10)));
    }

    @Test
    @EnabledForJreRange(max = JAVA_23)
    void testCleanNullBuffer() {
        assertThrows(IllegalStateException.class, () -> ByteBufferCleaner.clean(null));
    }

    @Test
    @EnabledForJreRange(max = JAVA_23)
    void testSupported() {
        assertTrue(ByteBufferCleaner.isSupported(), "ByteBufferCleaner does not work on this platform, please investigate and fix");
    }

    @Test
    @EnabledForJreRange(min = JAVA_23)
    void testUnsupportedByDefaultOnJava23() {
        assertNull(ByteBufferCleaner.getCleaner());
        assertFalse(ByteBufferCleaner.isSupported(), "ByteBufferCleaner does not work on this platform, please investigate and fix");
    }

    private void verifyUncleared(final ByteBuffer buffer) {
        buffer.flip();
        assertEquals(Long.MAX_VALUE, buffer.getLong());
        buffer.flip();
    }

    private void verifyCleared(final ByteBuffer buffer) {
        assertEquals(0, buffer.getLong());
    }
}
