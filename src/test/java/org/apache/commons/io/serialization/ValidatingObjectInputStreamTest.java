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

import org.apache.commons.io.serialization.ValidatingObjectInputStream.Builder;
import org.apache.commons.lang3.SerializationUtils;
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

    private Builder newBuilder() {
        return ValidatingObjectInputStream.builder().setInputStream(testStream);
    }

    private ValidatingObjectInputStream newFixture() throws IOException {
        return newBuilder().get();
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
    public void testAcceptCustomMatcherBuilder() throws Exception {
        assertSerialization(addCloseable(newBuilder().accept(ALWAYS_TRUE).get()));
    }

    @Test
    public void testAcceptCustomMatcherInstance() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(ALWAYS_TRUE));
    }

    @Test
    public void testAcceptOneFail() throws Exception {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(Integer.class)));
    }

    @Test
    public void testAcceptOnePassBuilder() throws Exception {
        assertSerialization(addCloseable(newBuilder().accept(MockSerializedClass.class).get()));
    }

    @Test
    public void testAcceptOnePassInstance() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class));
    }

    @Test
    public void testAcceptPatternBuilder() throws Exception {
        assertSerialization(addCloseable(newBuilder().accept(Pattern.compile(".*MockSerializedClass.*")).get()));
    }

    @Test
    public void testAcceptPatternInstance() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept(Pattern.compile(".*MockSerializedClass.*")));
    }

    @Test
    public void testAcceptWildcardBuilder() throws Exception {
        assertSerialization(addCloseable(newBuilder().accept("org.apache.commons.io.*").get()));
    }

    @Test
    public void testAcceptWildcardInstance() throws Exception {
        assertSerialization(addCloseable(newFixture()).accept("org.apache.commons.io.*"));
    }

    @Test
    public void testBuildDefault() throws Exception {
        final byte[] serialized = SerializationUtils.serialize("");
        try (InputStream is = newBuilder().setInputStream(new ByteArrayInputStream(serialized)).get()) {
            // empty
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

    /**
     * Javadoc example.
     */
    @SuppressWarnings({ "unchecked" })
    @Test
    public void testJavadocExample() throws Exception {
        // @formatter:off
        // Defining Object fixture
        final HashMap<String, Integer> map1 = new HashMap<>();
        map1.put("1", 1);
        // Writing serialized fixture
        final byte[] byteArray;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(map1);
            oos.flush();
            byteArray = baos.toByteArray();
        }
        // Reading
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                        .accept(HashMap.class, Number.class, Integer.class)
                        .setInputStream(bais)
                        .get()) {
            // String.class is automatically accepted
            final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
            assertEquals(map1, map2);
        }
        // Reusing a configuration
        final ObjectStreamClassPredicate predicate = new ObjectStreamClassPredicate()
                .accept(HashMap.class, Number.class, Integer.class);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
                        .setPredicate(predicate)
                        .setInputStream(bais)
                        .get()) {
            // String.class is automatically accepted
            final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
            assertEquals(map1, map2);
        }
        // @formatter:on
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
    public void testRejectBuilder() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newBuilder().accept(Long.class).reject(MockSerializedClass.class, Integer.class).get())));
    }

    @Test
    public void testRejectCustomMatcherBuilder() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newBuilder().accept(MockSerializedClass.class).reject(ALWAYS_TRUE).get())));
    }

    @Test
    public void testRejectCustomMatcherInstance() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject(ALWAYS_TRUE)));
    }

    @Test
    public void testRejectInstance() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newFixture()).accept(Long.class).reject(MockSerializedClass.class, Integer.class)));
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
    public void testRejectPrecedenceBuilder() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newBuilder().accept(MockSerializedClass.class).reject(MockSerializedClass.class, Integer.class).get())));
    }

    @Test
    public void testRejectPrecedenceInstance() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject(MockSerializedClass.class, Integer.class)));
    }

    @Test
    public void testRejectWildcardBuilder() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(addCloseable(newBuilder().accept(MockSerializedClass.class).reject("org.*").get())));
    }

    @Test
    public void testRejectWildcardInstance() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(addCloseable(newFixture()).accept(MockSerializedClass.class).reject("org.*")));
    }

    @Test
    public void testReuseConfiguration() throws Exception {
        // Defining Object fixture
        final HashMap<String, Integer> map1 = new HashMap<>();
        map1.put("1", 1);
        // Writing serialized fixture
        final byte[] byteArray;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(map1);
            oos.flush();
            byteArray = baos.toByteArray();
        }
        // Reusing a configuration: ObjectStreamClassPredicate
        final ObjectStreamClassPredicate predicate = new ObjectStreamClassPredicate().accept(HashMap.class, Number.class, Integer.class);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setPredicate(predicate).setInputStream(bais).get()) {
            // String.class is automatically accepted
            assertEquals(map1, vois.readObjectCast());
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder().setPredicate(predicate).setInputStream(bais).get()) {
            // String.class is automatically accepted
            assertEquals(map1, vois.readObjectCast());
        }
        // Reusing a configuration: Builder and ObjectStreamClassPredicate
        final Builder builder = ValidatingObjectInputStream.builder().setPredicate(predicate);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = builder.setInputStream(bais).get()) {
            // String.class is automatically accepted
            assertEquals(map1, vois.readObjectCast());
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ValidatingObjectInputStream vois = builder.setInputStream(bais).get()) {
            // String.class is automatically accepted
            assertEquals(map1, vois.readObjectCast());
        }
    }
}
