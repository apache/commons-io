/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ValidatingObjectInputStream}.
 */
public class ValidatingObjectInputStreamTest extends AbstractCloseableListTest {

    private static final ClassNameMatcher ALWAYS_TRUE = className -> true;
    private MockSerializedClass testObject;

    private InputStream testStream;

    private void assertSerialization(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final MockSerializedClass result = (MockSerializedClass) ois.readObject();
        assertEquals(testObject, result);
    }

    private ValidatingObjectInputStream newFixture() throws IOException {
        return ValidatingObjectInputStream.builder().setInputStream(testStream).get();
    }

    @BeforeEach
    public void setupMockSerializedClass() throws IOException {
        testObject = new MockSerializedClass(UUID.randomUUID().toString());
        final ByteArrayOutputStream bos = addCloseable(new ByteArrayOutputStream());
        final ObjectOutputStream oos = addCloseable(new ObjectOutputStream(bos));
        oos.writeObject(testObject);
        testStream = addCloseable(new ByteArrayInputStream(bos.toByteArray()));
    }

    @Test
    public void testAcceptCustomMatcher() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(ALWAYS_TRUE));
    }

    @Test
    public void testAcceptPattern() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(Pattern.compile(".*MockSerializedClass.*")));
    }

    @Test
    public void testAcceptWildcard() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept("org.apache.commons.io.*"));
    }

    @Test
    public void testAcceptOnePass() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class));
    }

    @Test
    public void testAcceptOneFail() throws Exception {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(Integer.class)));
    }

    /**
     * Javadoc example.
     */
    @SuppressWarnings({ "unchecked", "resource" })
    @Test
    public void testAcceptExample() throws Exception {
        // Data
        final HashMap<String, Integer> map1 = new HashMap<>();
        map1.put("1", 1);
        // Write
        final byte[] byteArray;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(map1);
            oos.flush();
            byteArray = baos.toByteArray();
        }
        // Read
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setInputStream(bais).get()) {
            // String.class is automatically accepted
            vois.accept(HashMap.class, Number.class, Integer.class);
            final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
            assertEquals(map1, map2);
        }
    }

    @Test
    public void testConstructor() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(ALWAYS_TRUE));
    }

    @Test
    public void testCustomInvalidMethod() {
        final class CustomVOIS extends ValidatingObjectInputStream {
            CustomVOIS(final InputStream is) throws IOException {
                super(is);
            }

            @Override
            protected void invalidClassNameFound(final String className) throws InvalidClassException {
                throw new RuntimeException("Custom exception");
            }
        }

        assertThrows(RuntimeException.class, () -> assertSerialization(addCloseable(new CustomVOIS(testStream)).reject(Integer.class)));
    }

    @Test
    public void testExceptionIncludesClassName() throws Exception {
        final InvalidClassException ice = assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture())));
        final String name = MockSerializedClass.class.getName();
        assertTrue(ice.getMessage().contains(name), "Expecting message to contain " + name);
    }

    @Test
    public void testNoAccept() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture())));
    }

    @Test
    public void testOurTestClassAcceptedFirst() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class, Integer.class));
    }

    @Test
    public void testOurTestClassAcceptedFirstWildcard() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept("*MockSerializedClass", "*Integer"));
    }

    @Test
    public void testOurTestClassAcceptedSecond() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(Integer.class, MockSerializedClass.class));
    }

    @Test
    public void testOurTestClassAcceptedSecondWildcard() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept("*Integer", "*MockSerializedClass"));
    }

    @Test
    public void testOurTestClassNotAccepted() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(Integer.class)));
    }

    @Test
    public void testOurTestClassOnlyAccepted() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class));
    }

    @Test
    public void testReject() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newFixture()).accept(Long.class).reject(MockSerializedClass.class, Integer.class)));
    }

    @Test
    public void testRejectCustomMatcher() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject(ALWAYS_TRUE)));
    }

    @Test
    public void testRejectOnly() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).reject(Integer.class)));
    }

    @Test
    public void testRejectPattern() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject(Pattern.compile("org.*"))));
    }

    @Test
    public void testRejectPrecedence() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject(MockSerializedClass.class, Integer.class)));
    }

    @Test
    public void testRejectWildcard() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject("org.*")));
    }
}
