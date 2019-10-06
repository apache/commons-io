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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BoundedInputStream}.
 *
 */
public class BoundedInputStreamTest {

    @Test
    public void testReadSingle() throws Exception {
        BoundedInputStream bounded;
        final byte[] helloWorld = "Hello World".getBytes();
        final byte[] hello      = "Hello".getBytes();

        // limit = length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length);
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit = length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit = length end");

        // limit > length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length + 1);
        for (int i = 0; i < helloWorld.length; i++) {
            assertEquals(helloWorld[i], bounded.read(), "limit > length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit > length end");

        // limit < length
        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), hello.length);
        for (int i = 0; i < hello.length; i++) {
            assertEquals(hello[i], bounded.read(), "limit < length byte[" + i + "]");
        }
        assertEquals(-1, bounded.read(), "limit < length end");
    }

    @Test
    public void testReadArray() throws Exception {

        BoundedInputStream bounded;
        final byte[] helloWorld = "Hello World".getBytes();
        final byte[] hello      = "Hello".getBytes();

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld));
        compare("limit = -1", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), 0);
        compare("limit = 0", new byte[0], IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length);
        compare("limit = length", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length + 1);
        compare("limit > length", helloWorld, IOUtils.toByteArray(bounded));

        bounded = new BoundedInputStream(new ByteArrayInputStream(helloWorld), helloWorld.length - 6);
        compare("limit < length", hello, IOUtils.toByteArray(bounded));
    }

    private void compare(final String msg, final byte[] expected, final byte[] actual) {
        assertEquals(expected.length, actual.length, msg + " length");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], msg + " byte[" + i + "]");
        }
    }
}
