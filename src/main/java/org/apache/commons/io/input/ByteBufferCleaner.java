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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.apache.commons.io.Buffers;

/**
 * Cleans {@link ByteBuffer} instances. Without manual intervention, direct ByteBuffers are eventually cleaned on garbage collection. However, this should not
 * be relied upon since it may not occur in a timely fashion, especially since off heap ByeBuffers don't put pressure on the garbage collector.
 * <p>
 * <strong>Warning:</strong> Do not attempt to use a direct {@link ByteBuffer} that has been cleaned or bad things will happen. Don't use this class unless you
 * can ensure that the cleaned buffer will not be accessed anymore.
 * </p>
 * <p>
 * See <a href=https://bugs.openjdk.java.net/browse/JDK-4724038>JDK-4724038</a>
 * </p>
 */
final class ByteBufferCleaner {

    private interface Cleaner {

        void clean(ByteBuffer buffer) throws ReflectiveOperationException;
    }

    private static final class RuntimeDependentCleaner implements Cleaner {
        private final Method cleanerMethod;
        private Method cleanMethod;

        private RuntimeDependentCleaner() throws SecurityException {
            try {
                try { // for Java 8
                    cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");
                } catch (final Exception e) { // for Java 9+
                    cleanMethod = Class.forName("jdk.internal.ref.Cleaner").getMethod("clean");
                }
                cleanerMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to create RuntimeDependentCleaner", e);
            }
        }

        @Override
        public void clean(final ByteBuffer buffer) throws ReflectiveOperationException {
            final Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner != null) {
                cleanMethod.invoke(cleaner);
            }
        }
    }

    private static final Cleaner CLEANER = new RuntimeDependentCleaner();

    /**
     * Releases memory held by the given {@link ByteBuffer}.
     * <p>
     * If the buffer is writable, it is cleared by filling it with zeros, the position is set to zero, the limit is set to the capacity, and the mark is
     * discarded.
     * </p>
     * <p>
     * If the buffer is direct and {@code clean} is true, it cleaned using the "sun." package.
     * </p>
     *
     * @param buffer   The buffer to release.
     * @param sunClean Perform additional cleaning using the "sun." package.
     * @throws IllegalStateException on internal failure.
     */
    static void clean(final ByteBuffer buffer, final boolean sunClean) {
        try {
            Buffers.clearWritable(buffer);
            if (sunClean && buffer.isDirect()) {
                sunClean(buffer);
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to clean a direct ByteBuffer.", e);
        }
    }

    static void sunClean(final ByteBuffer buffer) throws ReflectiveOperationException {
        CLEANER.clean(buffer);
    }
}
