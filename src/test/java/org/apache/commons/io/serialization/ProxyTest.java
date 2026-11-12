/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.io.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ValidatingObjectInputStream}.
 */
class ProxyTest {

    @FunctionalInterface
    public interface IAnnotated extends Serializable {

        void call();
    }

    public interface IAnnotatedChild extends IAnnotated {
        // Inherits the annotated interface.
    }

    public interface IBar extends IEvil {

        void bar();
    }

    public interface IEvil extends Serializable {
        // Marker super-interface used to test strict-mode recursive rejection.
    }

    public interface IFoo extends Serializable {

        void foo();
    }

    public static class InvocationHandlerImpl implements InvocationHandler, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            return "InvocationHandlerImpl.invoke()";
        }
    }

    private void assertStrictRejectAnnotation(final Class<?> iface) throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(iface)).accept("*")
                .reject(FunctionalInterface.class).setStrict(true).get()) {
            assertEquals("Class name not accepted: " + FunctionalInterface.class.getName(),
                    assertThrows(InvalidClassException.class, vois::readObject).getMessage());
        }
    }

    Object newProxy() {
        return Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class<?>[] { IFoo.class }, new InvocationHandlerImpl());
    }

    private byte[] serializeProxy(final Class<?>... interfaces) {
        return SerializationUtils.serialize((Serializable) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), interfaces,
                new InvocationHandlerImpl()));
    }

    @Test
    void testAcceptProxy() throws IOException, ClassNotFoundException {
        final Object proxy = newProxy();
        final byte[] serialized = SerializationUtils.serialize((Serializable) proxy);
        final Class<IFoo> ifaceClass = IFoo.class;
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept("*")
                .get()) {
            // @formatter:on
            assertTrue(assertInstanceOf(ifaceClass, vois.readObject()).toString().endsWith("InvocationHandlerImpl.invoke()"));
        }
    }

    @Test
    void testNonStrictAcceptRejectedAnnotation() throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IAnnotated.class)).accept("*")
                .reject(FunctionalInterface.class).setStrict(false).get()) {
            assertInstanceOf(IAnnotated.class, vois.readObject());
        }
    }

    @Test
    void testNonStrictAcceptRejectedSuperInterface() throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IBar.class)).accept("*")
                .reject(IEvil.class).setStrict(false).get()) {
            assertInstanceOf(IBar.class, vois.readObject());
        }
    }

    @Test
    void testRejectProxy() throws IOException, ClassNotFoundException {
        final Object proxy = newProxy();
        final byte[] serialized = SerializationUtils.serialize((Serializable) proxy);
        final Class<IFoo> ifaceClass = IFoo.class;
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept("*")
                .reject(ifaceClass)
                .get()) {
            // @formatter:on
            assertThrows(InvalidClassException.class, vois::readObject);
        }
    }

    @Test
    void testStrictAcceptAnnotatedProxy() throws IOException, ClassNotFoundException {
        // Standard meta-annotations contain cycles, including Documented annotating itself.
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IAnnotated.class)).accept("*")
                .setStrict(true).get()) {
            assertInstanceOf(IAnnotated.class, vois.readObject());
        }
    }

    @Test
    void testStrictAcceptInheritedAnnotatedProxy() throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IAnnotatedChild.class)).accept("*")
                .setStrict(true).get()) {
            assertInstanceOf(IAnnotatedChild.class, vois.readObject());
        }
    }

    @Test
    void testStrictAcceptProxy() throws IOException, ClassNotFoundException {
        final Object proxy = newProxy();
        final byte[] serialized = SerializationUtils.serialize((Serializable) proxy);
        final Class<IFoo> ifaceClass = IFoo.class;
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept("*")
                .setStrict(true)
                .get()) {
            // @formatter:on
            assertTrue(assertInstanceOf(ifaceClass, vois.readObject()).toString().endsWith("InvocationHandlerImpl.invoke()"));
        }
    }

    @Test
    void testStrictAcceptProxyExplicitTypes() throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IBar.class))
                .accept(IBar.class, IEvil.class, Serializable.class, Proxy.class, InvocationHandlerImpl.class, InvocationHandler.class).setStrict(true).get()) {
            assertInstanceOf(IBar.class, vois.readObject());
        }
    }

    @Test
    void testStrictRejectProxyAnnotation() throws IOException, ClassNotFoundException {
        assertStrictRejectAnnotation(IAnnotated.class);
    }

    @Test
    void testStrictRejectProxyInheritedAnnotation() throws IOException, ClassNotFoundException {
        assertStrictRejectAnnotation(IAnnotatedChild.class);
    }

    @Test
    void testStrictRejectProxyMetaAnnotation() throws IOException, ClassNotFoundException {
        // Cycle detection must still visit other annotations in the graph.
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IAnnotated.class)).accept("*")
                .reject(Retention.class).setStrict(true).get()) {
            assertEquals("Class name not accepted: " + Retention.class.getName(), assertThrows(InvalidClassException.class, vois::readObject).getMessage());
        }
    }

    @Test
    void testStrictRejectProxySecondInterfaceHierarchy() throws IOException, ClassNotFoundException {
        // Accepting the first interface must not skip validation of the second interface's hierarchy.
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IFoo.class, IBar.class)).accept("*")
                .reject(IEvil.class).setStrict(true).get()) {
            assertEquals("Class name not accepted: " + IEvil.class.getName(), assertThrows(InvalidClassException.class, vois::readObject).getMessage());
        }
    }

    @Test
    void testStrictRejectProxySuperInterface() throws IOException, ClassNotFoundException {
        // The proxy declares only IBar; IBar extends IEvil. In strict mode, rejecting IEvil must reject the
        // proxy, exactly as it would reject a plain class implementing IBar (recursive interface check).
        final Object proxy = Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class<?>[] { IBar.class }, new InvocationHandlerImpl());
        final byte[] serialized = SerializationUtils.serialize((Serializable) proxy);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept("*")
                .reject(IEvil.class)
                .setStrict(true)
                .get()) {
            // @formatter:on
            assertEquals("Class name not accepted: " + IEvil.class.getName(), assertThrows(InvalidClassException.class, vois::readObject).getMessage());
        }
    }

    @Test
    void testStrictRejectProxyUnacceptedSuperInterface() throws IOException, ClassNotFoundException {
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setByteArray(serializeProxy(IBar.class))
                .accept(IBar.class, Serializable.class, Proxy.class, InvocationHandlerImpl.class, InvocationHandler.class).setStrict(true).get()) {
            assertEquals("Class name not accepted: " + IEvil.class.getName(), assertThrows(InvalidClassException.class, vois::readObject).getMessage());
        }
    }
}
