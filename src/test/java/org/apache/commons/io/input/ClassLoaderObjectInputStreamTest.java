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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ClassLoaderObjectInputStream}.
 */
public class ClassLoaderObjectInputStreamTest {

    /**
     * Note: This test case tests the simplest functionality of ObjectInputStream. IF we really wanted to test
     * ClassLoaderObjectInputStream we would probably need to create a transient Class Loader. -TO
     */
    private enum E {
        A, B, C
    }

    private static final class TestFixture implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int i;

        private final Object o;

        private final E e;

        TestFixture(final int i, final Object o) {
            this.i = i;
            this.e = E.A;
            this.o = o;
        }

        private boolean equalObject(final Object other) {
            if (this.o == null) {
                return other == null;
            }
            return o.equals(other);
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof TestFixture) {
                final TestFixture tOther = (TestFixture) other;
                return this.i == tOther.i & this.e == tOther.e & equalObject(tOther.o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @Test
    public void testExpected() throws Exception {
        final Boolean input = Boolean.FALSE;
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(input));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final Object result = clois.readObject();
            assertEquals(input, result);
        }
    }

    @Test
    public void testLong() throws Exception {
        final Long input = 123L;
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(input));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final Object result = clois.readObject();
            assertEquals(input, result);
        }
    }

    @Test
    public void testObject1() throws Exception {
        final TestFixture input = new TestFixture(123, null);
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(input));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final Object result = clois.readObject();
            assertEquals(input, result);
        }
    }

    @Test
    public void testObject2() throws Exception {
        final TestFixture input = new TestFixture(123, 0);
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(input));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final Object result = clois.readObject();
            assertEquals(input, result);
        }
    }

    @Test
    public void testPrimitiveLong() throws Exception {
        final long input = 12345L;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeLong(input);
        }
        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final long result = clois.readLong();
            assertEquals(input, result);
        }
    }

    @Test
    public void testResolveProxyClass() throws Exception {
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(Boolean.FALSE));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final String[] interfaces = {Comparable.class.getName()};
            final Class<?> result = clois.resolveProxyClass(interfaces);
            assertTrue(Comparable.class.isAssignableFrom(result), "Assignable");
        }
    }

    @Test
    public void testResolveProxyClassWithMultipleInterfaces() throws Exception {
        final InputStream bais = new ByteArrayInputStream(SerializationUtils.serialize(Boolean.FALSE));
        try (ClassLoaderObjectInputStream clois = new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais)) {
            final String[] interfaces = {Comparable.class.getName(), Serializable.class.getName(), Runnable.class.getName()};
            final Class<?> result = clois.resolveProxyClass(interfaces);
            assertTrue(Comparable.class.isAssignableFrom(result), "Assignable");
            assertTrue(Runnable.class.isAssignableFrom(result), "Assignable");
            assertTrue(Serializable.class.isAssignableFrom(result), "Assignable");
            assertFalse(Flushable.class.isAssignableFrom(result), "Not Assignable");
        }
    }
}
