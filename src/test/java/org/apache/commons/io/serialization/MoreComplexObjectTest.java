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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This is more an example than a test - deserialize our {@link MoreComplexObject}
 * to verify which settings it requires, as the object uses a number of primitive
 * and java.* member objects.
 */
class MoreComplexObjectTest extends AbstractCloseableListTest {

    private InputStream inputStream;
    private MoreComplexObject original;

    private void assertSerialization(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final MoreComplexObject copy = (MoreComplexObject) ois.readObject();
        assertEquals(original.toString(), copy.toString(), "Expecting same data after deserializing");
    }

    @BeforeEach
    public void setupMoreComplexObject() throws IOException {
        original = new MoreComplexObject();
        final ByteArrayOutputStream bos = addCloseable(new ByteArrayOutputStream());
        final ObjectOutputStream oos = addCloseable(new ObjectOutputStream(bos));
        oos.writeObject(original);
        inputStream = addCloseable(new ByteArrayInputStream(bos.toByteArray()));
    }

    /**
     * Trusting java.* is probably reasonable and avoids having to be too detailed in the accepts.
     */
    @Test
    void testTrustJavaIncludingArrays() throws IOException, ClassNotFoundException {
        // @formatter:off
        assertSerialization(addCloseable(
                ValidatingObjectInputStream.builder()
                .setInputStream(inputStream)
                .accept(MoreComplexObject.class)
                .accept("java.*", "[Ljava.*")
                .get()
        ));
        // @formatter:on
    }

    /**
     * Trusting java.lang.* and the array variants of that means we have to define a number of accept classes explicitly. Quite safe but might become a bit
     * verbose.
     */
    @Test
    void testTrustJavaLang() throws IOException, ClassNotFoundException {
        // @formatter:off
        assertSerialization(addCloseable(
                ValidatingObjectInputStream.builder()
                .setInputStream(inputStream)
                .accept(MoreComplexObject.class, ArrayList.class, Random.class)
                .accept("java.lang.*", "[Ljava.lang.*")
                .get()
        ));
        // @formatter:on
    }

    /**
     * Here we accept everything but reject specific classes, using a pure blacklist mode.
     *
     * That's not as safe as it's hard to get an exhaustive blacklist, but might be ok in controlled environments.
     */
    @Test
    void testUseBlacklist() throws IOException, ClassNotFoundException {
        final String [] blacklist = {
                "org.apache.commons.collections.functors.InvokerTransformer",
                "org.codehaus.groovy.runtime.ConvertedClosure",
                "org.codehaus.groovy.runtime.MethodClosure",
                "org.springframework.beans.factory.ObjectFactory"
        };
        // @formatter:off
        assertSerialization(addCloseable(
                ValidatingObjectInputStream.builder()
                .setInputStream(inputStream)
                .accept("*")
                .reject(blacklist)
                .get()
        ));
        // @formatter:on
    }
}