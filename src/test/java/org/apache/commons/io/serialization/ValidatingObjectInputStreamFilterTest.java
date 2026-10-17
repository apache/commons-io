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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Tests resource filter composition and failures in isolated JVMs because global filters cannot be reset.
 */
class ValidatingObjectInputStreamFilterTest {

    /**
     * Runs filter scenarios in a separate JVM.
     */
    public static final class Probe {

        /**
         * Runs the requested scenario.
         *
         * @param args the scenario name.
         * @throws Exception if the scenario fails.
         */
        public static void main(final String[] args) throws Exception {
            final Class<?> filter = getFilterClass();
            final Class<?> config = Class.forName(filter.getName() + "$Config");
            final String pattern = "references".equals(args[0]) ? "maxrefs=5;maxarray=1000"
                    : "allowed".equals(args[0]) ? "java.lang.Object;java.util.HashMap;maxarray=1000" : "!java.util.HashMap;maxarray=2";
            final Object global = "throwing".equals(args[0])
                    ? Proxy.newProxyInstance(filter.getClassLoader(), new Class<?>[] { filter }, (proxy, method, values) -> {
                        if ("checkInput".equals(method.getName())) {
                            throw new IllegalArgumentException("Existing filter failed");
                        }
                        return "Throwing filter";
                    })
                    : config.getMethod("createFilter", String.class).invoke(null, pattern);
            config.getMethod("setSerialFilter", filter).invoke(null, global);
            if ("factory".equals(args[0]) || "security".equals(args[0]) || "error".equals(args[0])) {
                final BinaryOperator<Object> factory = (current, requested) -> {
                    if (current != null) {
                        if ("security".equals(args[0])) {
                            throw new SecurityException("Filter installation denied");
                        }
                        if ("error".equals(args[0])) {
                            throw new AssertionError("Filter installation denied");
                        }
                        throw new IllegalStateException("Filter installation denied");
                    }
                    return requested;
                };
                config.getMethod("setSerialFilterFactory", BinaryOperator.class).invoke(null, factory);
                final Class<? extends Throwable> expected = "security".equals(args[0]) ? SecurityException.class
                        : "error".equals(args[0]) ? AssertionError.class : IllegalStateException.class;
                final Throwable failure = assertThrows(expected, () -> read(new byte[1], -1, 5));
                assertEquals("Filter installation denied", failure.getMessage());
            } else if ("throwing".equals(args[0])) {
                final InvalidClassException failure = assertThrows(InvalidClassException.class, () -> read(new byte[1], -1, -1));
                assertEquals("Existing filter failed", failure.getCause().getMessage());
            } else if ("allowed".equals(args[0])) {
                assertEquals(new HashMap<>(), read(new HashMap<>(), -1, -1));
                // Both filters see Object[1]: the global class rule ALLOWS it, but the local length limit REJECTS it.
                assertEquals(1, ((Object[]) read(new Object[] { "value" }, 1, -1)).length);
                assertThrows(InvalidClassException.class, () -> read(new Object[] { "value" }, 0, -1));
            } else if ("references".equals(args[0])) {
                final Object[] repeated = new Object[100];
                Arrays.fill(repeated, "shared");
                assertThrows(InvalidClassException.class, () -> read(repeated, -1, -1));
            } else {
                assertEquals(1, ((byte[]) read(new byte[1], -1, -1)).length);
                assertThrows(InvalidClassException.class, () -> read(new byte[3], -1, -1));
                assertThrows(InvalidClassException.class, () -> read(new byte[1], 0, -1));
                assertThrows(InvalidClassException.class, () -> read(new HashMap<>(), -1, -1));
            }
        }

        private static Object read(final Object value, final long maxArray, final long maxRefs) throws Exception {
            try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder()
                    .accept(byte[].class, Object[].class, HashMap.class)
                    .setMaxDepth(100).setMaxArrayLength(maxArray).setMaxRefs(maxRefs)
                    .setInputStream(new ByteArrayInputStream(SerializationUtils.serialize((java.io.Serializable) value))).get()) {
                return stream.readObject();
            }
        }
    }

    private static Class<?> getFilterClass() throws ClassNotFoundException {
        try {
            return Class.forName("java.io.ObjectInputFilter");
        } catch (final ClassNotFoundException e) {
            return Class.forName("sun.misc.ObjectInputFilter");
        }
    }

    private void runProbe(final String scenario, final boolean needsFactory) throws Exception {
        try {
            final Class<?> config = Class.forName(getFilterClass().getName() + "$Config");
            if (needsFactory) {
                config.getMethod("setSerialFilterFactory", BinaryOperator.class);
            }
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            Assumptions.assumeTrue(false, "Platform filter API unavailable: " + e);
        }
        final String executable = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();
        final List<String> command = new ArrayList<>();
        command.add(executable);
        // Include forked filter scenarios in the same coverage report as the parent test JVM.
        for (final String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:") && argument.contains("jacoco")) {
                command.add(argument);
            }
        }
        command.add("-cp");
        command.add(System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
        command.add(Probe.class.getName());
        command.add(scenario);
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Filter probe timed out");
            assertEquals(0, process.exitValue(), IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
        } finally {
            process.destroyForcibly();
        }
    }

    @Test
    void testCombinedLimitsAndBoundaries() throws Exception {
        final byte[] bytes = SerializationUtils.serialize(new byte[1]);
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(byte[].class)
                .setMaxDepth(1).setMaxArrayLength(1).setMaxRefs(2)
                .setInputStream(new ByteArrayInputStream(bytes)).get()) {
            assertEquals(1, ((byte[]) stream.readObject()).length);
        }
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(byte[].class)
                .setMaxDepth(0).setInputStream(new ByteArrayInputStream(bytes)).get()) {
            assertThrows(InvalidClassException.class, stream::readObject);
        }
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(byte[].class)
                .setMaxRefs(0).setInputStream(new ByteArrayInputStream(bytes)).get()) {
            assertThrows(InvalidClassException.class, stream::readObject);
        }
    }

    @Test
    void testDeclaredArrayLengthRejectedBeforeContents() throws Exception {
        final byte[] serialized = SerializationUtils.serialize(new byte[1000]);
        final byte[] header = Arrays.copyOf(serialized, serialized.length - 1000);
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(byte[].class).setMaxArrayLength(999)
                .setInputStream(new ByteArrayInputStream(header)).get()) {
            assertThrows(InvalidClassException.class, stream::readObject);
        }
    }

    @Test
    void testExistingAllowCannotOverrideLocalRejection() throws Exception {
        runProbe("allowed", false);
    }

    @Test
    void testExistingFilterAndLocalLimits() throws Exception {
        runProbe("composition", false);
    }

    @Test
    void testExistingFilterExceptionStillRejects() throws Exception {
        runProbe("throwing", false);
    }

    @Test
    void testExistingReferenceLimit() throws Exception {
        runProbe("references", false);
    }

    @Test
    void testFactoryErrorPropagated() throws Exception {
        runProbe("error", true);
    }

    @Test
    void testFactoryFailurePropagated() throws Exception {
        runProbe("factory", true);
    }

    @Test
    void testFactorySecurityFailurePropagated() throws Exception {
        runProbe("security", true);
    }

    @Test
    void testIncompleteApiFailsClosed() throws Exception {
        final ClassLoader incomplete = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (name.endsWith("ObjectInputFilter$Config")) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }
        };
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(SerializationUtils.serialize("value")))) {
            final IOException failure = assertThrows(IOException.class,
                    () -> ValidatingObjectInputStream.setObjectInputFilter(stream, "maxrefs=5", incomplete));
            assertTrue(failure.getCause() instanceof ClassNotFoundException);
        }
    }

    @Test
    void testNegativeLimitsDisablePreviouslyConfiguredLimits() throws Exception {
        final byte[] bytes = SerializationUtils.serialize(new byte[1]);
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(byte[].class)
                .setMaxDepth(0).setMaxArrayLength(0).setMaxRefs(0)
                .setMaxDepth(-2).setMaxArrayLength(-2).setMaxRefs(-2)
                .setInputStream(new ByteArrayInputStream(bytes)).get()) {
            assertEquals(1, ((byte[]) stream.readObject()).length);
        }
    }

    @Test
    void testRepeatedReferencesRejected() throws Exception {
        final Object[] repeated = new Object[100];
        Arrays.fill(repeated, "shared");
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(Object[].class).setMaxRefs(5)
                .setInputStream(new ByteArrayInputStream(SerializationUtils.serialize(repeated))).get()) {
            assertThrows(InvalidClassException.class, stream::readObject);
        }
    }

    @Test
    void testResourceLimitsDoNotBypassClassRejection() throws Exception {
        final byte[] bytes = SerializationUtils.serialize(new HashMap<>());
        try (ValidatingObjectInputStream stream = ValidatingObjectInputStream.builder().accept(HashMap.class).reject(HashMap.class)
                .setMaxDepth(100).setMaxArrayLength(1000).setMaxRefs(1000)
                .setInputStream(new ByteArrayInputStream(bytes)).get()) {
            assertThrows(InvalidClassException.class, stream::readObject);
        }
    }

    @Test
    void testUnavailableApiFailsClosedForEveryLimit() throws Exception {
        final ClassLoader unavailable = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if ("java.io.ObjectInputFilter".equals(name) || "sun.misc.ObjectInputFilter".equals(name)) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }
        };
        for (final String pattern : new String[] {"maxrefs=5", "maxdepth=5", "maxarray=5"}) {
            try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(SerializationUtils.serialize("value")))) {
                assertThrows(UnsupportedOperationException.class, () -> ValidatingObjectInputStream.setObjectInputFilter(stream, pattern, unavailable));
            }
        }
    }
}
