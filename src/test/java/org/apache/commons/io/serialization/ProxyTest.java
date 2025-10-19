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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ValidatingObjectInputStream}.
 */
class ProxyTest {

    public interface IFoo extends Serializable {

        void foo();
    }

    public static class InvocationHandlerImpl implements InvocationHandler, Serializable {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            return "InvocationHandlerImpl.invoke()";
        }
    }

    Object newProxy() {
        return Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class<?>[] { IFoo.class }, new InvocationHandlerImpl());
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
}
