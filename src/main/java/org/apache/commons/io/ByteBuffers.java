/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Manufactures {@link ByteBuffer} instances.
 *
 * @since 2.19.0
 */
public final class ByteBuffers {

    /**
     * Allocates a new byte buffer with little-endian byte order. The bytes of a multibyte value are ordered from least significant to most significant.
     * <p>
     * The new buffer's position is zero, the limit is its capacity, the mark is undefined, and each of element is initialized to zero. The new buffer has the
     * given backing {@code array}, and its {@link ByteBuffer#arrayOffset() array offset} is zero.
     * </p>
     *
     * @param array The array that will back the new byte buffer.
     * @return The new byte buffer.
     */
    public static ByteBuffer littleEndian(final byte[] array) {
        return littleEndian(ByteBuffer.wrap(array));
    }

    /**
     * Sets the give buffer to little-endian.
     *
     * @param allocate The buffer to set to little-endian.
     * @return the given buffer.
     */
    public static ByteBuffer littleEndian(final ByteBuffer allocate) {
        return allocate.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Allocates a new byte buffer with little-endian byte order. The bytes of a multibyte value are ordered from least significant to most significant.
     * <p>
     * The new buffer's position is zero, the limit is its capacity, the mark is undefined, and each of element is initialized to zero. The new buffer has a
     * {@link ByteBuffer#array() backing array}, and its {@link ByteBuffer#arrayOffset() array offset} is zero.
     * </p>
     *
     * @param capacity The new buffer's capacity, in bytes.
     * @return The new byte buffer.
     * @throws IllegalArgumentException If the <code>capacity</code> is negative.
     */
    public static ByteBuffer littleEndian(final int capacity) {
        return littleEndian(ByteBuffer.allocate(capacity));
    }

    private ByteBuffers() {
        // empty, no instance.
    }

}
