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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@code ByteBufferCleaner}.
 */
public class ByteBufferCleanerTest {

    @Test
    void testCleanEmpty() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(10);
        // There is no way verify that the buffer has been cleaned up, we are just verifying that
        // clean() doesn't blow up
        ByteBufferCleaner.clean(buffer);
    }

    @Test
    void testCleanFull() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(10);
        buffer.put(RandomUtils.nextBytes(10), 0, 10);
        // There is no way verify that the buffer has been cleaned up, we are just verifying that
        // clean() doesn't blow up
        ByteBufferCleaner.clean(buffer);
    }

    @Test
    void testSupported() {
        assertTrue(ByteBufferCleaner.isSupported(), "ByteBufferCleaner does not work on this platform, please investigate and fix");
    }

}
