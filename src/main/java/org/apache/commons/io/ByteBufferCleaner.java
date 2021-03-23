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
package org.apache.commons.io;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Utility to manually clean a direct {@link ByteBuffer}. Without manual
 * intervention, direct ByteBuffers will be cleaned eventually upon garbage
 * collection. However, this should be be relied upon since it may not occur in
 * a timely fashion - especially since off heap ByeBuffers don't put pressure on
 * the garbage collector.
 * 
 * <p>
 * <b>Warning:</b> Do not attempt to use a direct {@link ByteBuffer} that has
 * been cleaned or bad things will happen. Don't use this class unless you can
 * ensure that the cleaned buffer will not be accessed anymore.
 * </p>
 * <p>
 * See <a href=https://bugs.openjdk.java.net/browse/JDK-4724038>JDK-4724038</a>
 * </p>
 * 
 * @since 2.9.0
 */
public class ByteBufferCleaner {

    private static final Cleaner cleaner = getCleaner();

    private static Cleaner getCleaner() {
        boolean isJava8;
        try {
            Class.forName("sun.misc.Cleaner");
            isJava8 = true;
        } catch (ClassNotFoundException e) {
            isJava8 = false;
        }
        Cleaner cleaner = null;
        try {
            cleaner = isJava8 ? new Java8Cleaner() : new Java9plusCleaner();
        } catch (Exception e) {
            System.err.println("Failed to load ByteBuffer cleaner");
            e.printStackTrace();
        }
        return cleaner;
    }

    /**
     * Release memory held by the given {@link ByteBuffer}
     * 
     * @param buffer to release
     */
    public static void clean(final ByteBuffer buffer) {
        cleaner.clean(buffer);
    }

    /**
     * Report if were able to load a suitable cleaner for the current JVM.
     * Attempting to call {@link #clean(ByteBuffer)} when this method returns false
     * will result in an exception
     * 
     * @return {@code true} if cleaning is supported, {@code false} otherwise
     */
    public static boolean isSupported() {
        return cleaner != null;
    }

    private static interface Cleaner {
        void clean(ByteBuffer buffer);
    }

    private static class Java8Cleaner implements Cleaner {
        private final Method cleanerMethod;
        private final Method cleanMethod;

        private Java8Cleaner() {
            try {
                Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
                this.cleanerMethod = directBufferClass.getMethod("cleaner");
                this.cleanMethod = cleanerClass.getMethod("clean");
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Failed to initialize Java8Cleaner", e);
            }
        }

        @Override
        public void clean(final ByteBuffer buffer) {
            try {
                Object cleaner = cleanerMethod.invoke(buffer);
                if (cleaner != null) {
                    cleanMethod.invoke(cleaner);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException("Failed to clean direct buffer", e);
            }

        }
    }

    private static class Java9plusCleaner implements Cleaner {
        private final Object theUnsafe;
        private final Method invokeCleaner;

        private Java9plusCleaner() {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field field = unsafeClass.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                this.theUnsafe = field.get(null);
                this.invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | SecurityException
                    | NoSuchMethodException e) {
                throw new IllegalStateException("Failed to initialize Java9plusCleaner", e);
            }
        }

        @Override
        public void clean(final ByteBuffer buffer) {
            try {
                this.invokeCleaner.invoke(theUnsafe, buffer);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException("Failed to clean direct buffer", e);
            }

        }
    }
}
