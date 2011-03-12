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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * Tests the CountingInputStream.
 *
 * @version $Id$
 */
public class ClassLoaderObjectInputStreamTest extends TestCase {

    public ClassLoaderObjectInputStreamTest(String name) {
        super(name);
    }

    /* Note: This test case tests the simplest functionality of
     * ObjectInputStream.  IF we really wanted to test ClassLoaderObjectInputStream
     * we would probably need to create a transient Class Loader. -TO
     */

    
    public void testExpected() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject( Boolean.FALSE );

        InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ClassLoaderObjectInputStream clois = 
            new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        Boolean result = (Boolean) clois.readObject();

        assertTrue( !result.booleanValue() );
    }

    public void testResolveProxyClass() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject( Boolean.FALSE );
        InputStream bais = new ByteArrayInputStream(baos.toByteArray());

        ClassLoaderObjectInputStream clois = 
            new ClassLoaderObjectInputStream(getClass().getClassLoader(), bais);
        String[] interfaces = new String[] { Comparable.class.getName() };
        Class<?> result = clois.resolveProxyClass(interfaces);
        assertTrue("Assignable", Comparable.class.isAssignableFrom(result));
    }
    
}
