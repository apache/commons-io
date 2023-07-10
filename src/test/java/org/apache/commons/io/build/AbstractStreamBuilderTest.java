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

package org.apache.commons.io.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link AbstractStreamBuilder}.
 */
public class AbstractStreamBuilderTest {

    public static class Builder extends AbstractStreamBuilder<char[], Builder> {

        @Override
        public char[] get() {
            final char[] arr = new char[getBufferSize()];
            Arrays.fill(arr, 'a');
            return arr;
        }

    }

    private void assertResult(final char[] arr, final int size) {
        assertNotNull(arr);
        assertEquals(size, arr.length);
        for (final char c : arr) {
            assertEquals('a', c);
        }
    }

    protected Builder builder() {
        return new Builder();
    }

    @Test
    public void testBufferSizeChecker() {
        // sanity
        final Builder builder = builder();
        assertResult(builder.get(), builder.getBufferSize());
        // basic failure
        assertThrows(IllegalArgumentException.class, () -> builder().setBufferSizeMax(2).setBufferSize(3));
        // reset
        assertResult(builder.setBufferSizeMax(2).setBufferSizeMax(0).setBufferSize(3).get(), 3);
        // resize
        assertResult(builder().setBufferSizeMax(2).setBufferSizeChecker(i -> 100).setBufferSize(3).get(), 100);
    }
}
