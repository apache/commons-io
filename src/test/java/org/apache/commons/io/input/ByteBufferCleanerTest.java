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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        // There is no way verify that the buffer has been cleaned up by inspecting it because a cleaned buffer should
        // not be used. We are just verifying that clean() doesn't blow up
        ByteBufferCleaner.clean(buffer, true);
    }

    @Test
    void testCleanFull() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.putLong(Long.MAX_VALUE);
        verifyUncleared(buffer);
        // There is no way verify that the buffer has been cleaned up by inspecting it because a cleaned buffer should
        // not be used. We are just verifying that clean() doesn't blow up
        ByteBufferCleaner.clean(buffer, true);
    }

    @Test
    void testCleanNonDirectBuffer() {
        assertDoesNotThrow(() -> ByteBufferCleaner.clean(ByteBuffer.allocate(10), true));
    }

    @Test
    @EnabledForJreRange(max = JAVA_23)
    void testCleanNullBuffer() {
        assertThrows(IllegalStateException.class, () -> ByteBufferCleaner.clean(null, true));
    }

    @Test
    void testCleanWithSunCleanFalseZerosBuffer() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.putLong(Long.MAX_VALUE);
        // clean with sunClean false should still zero the buffer via Buffers.clearWritable
        ByteBufferCleaner.clean(buffer, false);
        // Buffers.clearWritable resets position to zero and limit to capacity
        buffer.position(0);
        // The buffer should be zeroed, not contain the original value
        assertEquals(0L, buffer.getLong());
    }

    private void verifyUncleared(final ByteBuffer buffer) {
        buffer.flip();
        assertEquals(Long.MAX_VALUE, buffer.getLong());
        buffer.flip();
    }
}
