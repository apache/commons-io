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

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ValidatingObjectInputStream}.
 */
class ValidatingObjectInputStreamStrictTest {

    abstract static class AbtractFoo implements IFoo {

        private static final long serialVersionUID = 1L;

    }

    public static class FixtureObject implements IFoo {

        private static final long serialVersionUID = 1L;

        @Override
        public void foo() {
            // empty
        }
    }

    static class FooImpl extends AbtractFoo {

        private static final long serialVersionUID = 1L;

        @Override
        public void foo() {
            // empty
        }

    }

    @FunctionalInterface
    public interface IFoo extends Serializable {

        void foo();
    }

    @Test
    void testAcceptAbstractClass() throws IOException, ClassNotFoundException {
        final FooImpl object = new FooImpl();
        final byte[] serialized = SerializationUtils.serialize(object);
        final Class<IFoo> ifaceClass = IFoo.class;
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept(ifaceClass)
                .accept(Serializable.class)
                .accept(AbtractFoo.class)
                .accept(FooImpl.class)
                .get()) {
            // @formatter:on
            assertInstanceOf(ifaceClass, vois.readObject());
        }
    }

    @Test
    void testAcceptAll() throws IOException, ClassNotFoundException {
        final FixtureObject object = new FixtureObject();
        final byte[] serialized = SerializationUtils.serialize(object);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept("*")
                .get()) {
            // @formatter:on
            assertInstanceOf(IFoo.class, vois.readObject());
        }
    }

    @Test
    void testAcceptInterface() throws IOException, ClassNotFoundException {
        final FixtureObject object = new FixtureObject();
        final byte[] serialized = SerializationUtils.serialize(object);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .accept(IFoo.class)
                .get()) {
            // @formatter:on
            // not a feature
            assertThrows(InvalidClassException.class, vois::readObject);
        }
    }

    @Test
    void testRejectAnnotation() throws IOException, ClassNotFoundException {
        final FixtureObject object = new FixtureObject();
        final byte[] serialized = SerializationUtils.serialize(object);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .reject(FunctionalInterface.class)
                .accept("*")
                .setStrict(true)
                .get()) {
            // @formatter:on
            assertThrows(InvalidClassException.class, vois::readObject);
        }
    }

    @Test
    void testRejectInterface() throws IOException, ClassNotFoundException {
        final FixtureObject object = new FixtureObject();
        final byte[] serialized = SerializationUtils.serialize(object);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .reject(IFoo.class)
                .accept("*")
                .setStrict(true)
                .get()) {
            // @formatter:on
            assertThrows(InvalidClassException.class, vois::readObject);
        }
    }

    @Test
    void testRejectSuperClass() throws IOException, ClassNotFoundException {
        final FooImpl object = new FooImpl();
        final byte[] serialized = SerializationUtils.serialize(object);
        // @formatter:off
        try (ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                .setByteArray(serialized)
                .setStrict(true)
                .setPredicate(new ObjectStreamClassPredicate() {
                    @Override
                    public boolean test(String name) {
                        // System.out.println(name);
                        return super.test(name);
                    }
                })
                // after setting the debug predicate above.
                .reject(AbtractFoo.class)
                .accept("*")
                .get()) {
            // @formatter:on
            assertThrows(InvalidClassException.class, vois::readObject);
        }
    }
}
