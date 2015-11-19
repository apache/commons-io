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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class ValidatingObjectInputStreamTest extends ClosingBase {
    private MockSerializedClass testObject;
    private InputStream testStream;

    static private final ClassNameMatcher ALWAYS_TRUE = new ClassNameMatcher() {
        @Override
        public boolean matches(String className) {
            return true;
        }
    };

    @Before
    public void setup() throws IOException {
        testObject = new MockSerializedClass(UUID.randomUUID().toString());
        final ByteArrayOutputStream bos = willClose(new ByteArrayOutputStream());
        final ObjectOutputStream oos = willClose(new ObjectOutputStream(bos));
        oos.writeObject(testObject);
        testStream = willClose(new ByteArrayInputStream(bos.toByteArray()));
    }

    private void assertSerialization(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final MockSerializedClass result = (MockSerializedClass) (ois.readObject());
        assertEquals(testObject, result);
    }

    @Test(expected = InvalidClassException.class)
    public void noAccept() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream)));
    }

    @Test
    public void exceptionIncludesClassName() throws Exception {
        try {
            assertSerialization(
                    willClose(new ValidatingObjectInputStream(testStream)));
            fail("Expected an InvalidClassException");
        } catch(InvalidClassException ice) {
            final String name = MockSerializedClass.class.getName();
            assertTrue("Expecting message to contain " + name, ice.getMessage().contains(name));
        }
    }

    @Test
    public void acceptCustomMatcher() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(ALWAYS_TRUE)
        );
    }

    @Test(expected = InvalidClassException.class)
    public void rejectCustomMatcher() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(ALWAYS_TRUE)
        );
    }

    @Test
    public void acceptPattern() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(Pattern.compile(".*MockSerializedClass.*"))
        );
    }

    @Test(expected = InvalidClassException.class)
    public void rejectPattern() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(Pattern.compile("org.*"))
        );
    }

    @Test
    public void acceptWildcard() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept("org.apache.commons.io.*")
        );
    }

    @Test(expected = InvalidClassException.class)
    public void rejectWildcard() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject("org.*")
        );
    }

    @Test(expected = InvalidClassException.class)
    public void ourTestClassNotAccepted() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(Integer.class)
        );
    }

    @Test
    public void ourTestClassOnlyAccepted() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
        );
    }

    @Test
    public void ourTestClassAcceptedFirst() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class, Integer.class)
        );
    }

    @Test
    public void ourTestClassAcceptedSecond() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(Integer.class, MockSerializedClass.class)
        );
    }

    @Test
    public void ourTestClassAcceptedFirstWildcard() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept("*MockSerializedClass","*Integer")
        );
    }

    @Test
    public void ourTestClassAcceptedSecondWildcard() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept("*Integer","*MockSerializedClass")
        );
    }

    @Test(expected = InvalidClassException.class)
    public void reject() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(Long.class)
                .reject(MockSerializedClass.class, Integer.class)
        );
    }
    
    @Test(expected = InvalidClassException.class)
    public void rejectPrecedence() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .accept(MockSerializedClass.class)
                .reject(MockSerializedClass.class, Integer.class)
        );
    }
    
    @Test(expected = InvalidClassException.class)
    public void rejectOnly() throws Exception {
        assertSerialization(
                willClose(new ValidatingObjectInputStream(testStream))
                .reject(Integer.class)
        );
    }
    
    @Test(expected = RuntimeException.class)
    public void customInvalidMethod() throws Exception {
        class CustomVOIS extends ValidatingObjectInputStream {
            CustomVOIS(InputStream is) throws IOException {
                super(is);
            }

            @Override
            protected void invalidClassNameFound(String className) throws InvalidClassException {
                throw new RuntimeException("Custom exception");
            }
        };
        
        assertSerialization(
                willClose(new CustomVOIS(testStream))
                .reject(Integer.class)
        );
    }
}