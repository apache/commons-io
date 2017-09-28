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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Tests the CountingInputStream.
 *
 */
public class ClassLoaderObjectInputStreamTest {

    /* Note: This test case tests the simplest functionality of
     * ObjectInputStream.  IF we really wanted to test ClassLoaderObjectInputStream
     * we would probably need to create a transient Class Loader. -TO
     */


    @org.junit.Test
    public void testExpected() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final Object input = Boolean.FALSE;
        oos.writeObject(input);

        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final Object result = clois.readObject();

        assertEquals(input, result);
        clois.close();
    }

    @org.junit.Test
    public void testLong() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final Object input = (long) 123;
        oos.writeObject(input);

        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final Object result = clois.readObject();

        assertEquals(input, result);
        clois.close();
    }

    @org.junit.Test
    public void testPrimitiveLong() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final long input = 12345L;
        oos.writeLong(input);
        oos.close();

        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final long result = clois.readLong();

        assertEquals(input, result);
        clois.close();
    }

    private static enum E {A, B, C}

    private static class Test implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int i;

        private final Object o;

        private final E e;

        Test(final int i, final Object o) {
            this.i = i;
            this.e = E.A;
            this.o = o;
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof Test) {
                final Test tother = (Test) other;
                return (this.i == tother.i)
                        & (this.e == tother.e)
                        & equalObject(tother.o);
            } else {
                return false;
            }
        }

        private boolean equalObject(final Object other) {
            if (this.o == null) {
                return other == null;
            }
            return o.equals(other);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    @org.junit.Test
    public void testObject1() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final Object input = new Test(123, null);
        oos.writeObject(input);
        oos.close();

        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final Object result = clois.readObject();

        assertEquals(input, result);
        clois.close();
    }

    @org.junit.Test
    public void testObject2() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final Object input = new Test(123, 0);
        oos.writeObject(input);
        oos.close();

        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final Object result = clois.readObject();

        assertEquals(input, result);
        clois.close();
    }

    @org.junit.Test
    public void testResolveProxyClass() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(Boolean.FALSE);
        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());

        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final String[] interfaces = new String[]{Comparable.class.getName()};
        final Class<?> result = clois.resolveProxyClass(interfaces);
        assertTrue("Assignable", Comparable.class.isAssignableFrom(result));
        clois.close();
    }

    @org.junit.Test
    public void testResolveProxyClassWithMultipleInterfaces() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(Boolean.FALSE);
        final InputStream bais = new ByteArrayInputStream(baos.toByteArray());

        final ClassLoaderObjectInputStream clois =
                new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        final String[] interfaces = new String[]{Comparable.class.getName(),
                                                 Serializable.class.getName(),
                                                 Runnable.class.getName()};
        final Class<?> result = clois.resolveProxyClass(interfaces);
        assertTrue("Assignable", Comparable.class.isAssignableFrom(result));
        assertTrue("Assignable", Runnable.class.isAssignableFrom(result));
        assertTrue("Assignable", Serializable.class.isAssignableFrom(result));
        assertFalse("Not Assignable", Flushable.class.isAssignableFrom(result));
        clois.close();
    }
}
