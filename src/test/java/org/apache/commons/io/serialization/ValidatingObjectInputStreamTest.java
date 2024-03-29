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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    @BeforeEach
    public void setupMockSerializedClass() throws IOException {
        testObject = new MockSerializedClass(UUID.randomUUID().toString());
        final ByteArrayOutputStream bos = closeAfterEachTest(new ByteArrayOutputStream());
        final ObjectOutputStream oos = closeAfterEachTest(new ObjectOutputStream(bos));
        oos.writeObject(testObject);
        testStream = closeAfterEachTest(new ByteArrayInputStream(bos.toByteArray()));
    }

    @Test
    public void testAcceptCustomMatcher() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(ALWAYS_TRUE)
        );
    }

    @Test
    public void testAcceptPattern() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(Pattern.compile(".*MockSerializedClass.*"))
        );
    }

    @Test
    public void testAcceptWildcard() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept("org.apache.commons.io.*")
        );
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

        assertThrows(RuntimeException.class,
                () -> assertSerialization(
                closeAfterEachTest(new CustomVOIS(testStream))
                .reject(Integer.class)
        ));
    }

    @Test
    public void testExceptionIncludesClassName() throws Exception {
        try {
            assertSerialization(
                    closeAfterEachTest(new ValidatingObjectInputStream(testStream)));
            fail("Expected an InvalidClassException");
        } catch (final InvalidClassException ice) {
            final String name = MockSerializedClass.class.getName();
            assertTrue(ice.getMessage().contains(name), "Expecting message to contain " + name);
        }
    }

    @Test
    public void testNoAccept() {
        assertThrows(InvalidClassException.class, () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))));
    }

    @Test
    public void testOurTestClassAcceptedFirst() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class, Integer.class)
        );
    }

    @Test
    public void testOurTestClassAcceptedFirstWildcard() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept("*MockSerializedClass", "*Integer")
        );
    }

    @Test
    public void testOurTestClassAcceptedSecond() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(Integer.class, MockSerializedClass.class)
        );
    }

    @Test
    public void testOurTestClassAcceptedSecondWildcard() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept("*Integer", "*MockSerializedClass")
        );
    }

    @Test
    public void testOurTestClassNotAccepted() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(Integer.class)
        ));
    }

    @Test
    public void testOurTestClassOnlyAccepted() throws Exception {
        assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
        );
    }

    @Test
    public void testReject() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(Long.class)
                .reject(MockSerializedClass.class, Integer.class)
        ));
    }

    @Test
    public void testRejectCustomMatcher() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(ALWAYS_TRUE)
        ));
    }

    @Test
    public void testRejectOnly() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .reject(Integer.class)
        ));
    }

    @Test
    public void testRejectPattern() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(Pattern.compile("org.*"))
        ));
    }

    @Test
    public void testRejectPrecedence() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(MockSerializedClass.class, Integer.class)
        ));
    }

    @Test
    public void testRejectWildcard() {
        assertThrows(InvalidClassException.class,
                () -> assertSerialization(
                closeAfterEachTest(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject("org.*")
        ));
    }
}