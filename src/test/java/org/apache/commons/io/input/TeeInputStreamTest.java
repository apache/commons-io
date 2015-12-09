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

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit Test Case for {@link TeeInputStream}.
 */
public class TeeInputStreamTest  {

    private final String ASCII = "US-ASCII";

    private InputStream tee;

    private ByteArrayOutputStream output;

    @Before
    public void setUp() throws Exception {
        final InputStream input = new ByteArrayInputStream("abc".getBytes(ASCII));
        output = new ByteArrayOutputStream();
        tee = new TeeInputStream(input, output);
    }

    @Test
    public void testReadNothing() throws Exception {
        assertEquals("", new String(output.toString(ASCII)));
    }

    @Test
    public void testReadOneByte() throws Exception {
        assertEquals('a', tee.read());
        assertEquals("a", new String(output.toString(ASCII)));
    }

    @Test
    public void testReadEverything() throws Exception {
        assertEquals('a', tee.read());
        assertEquals('b', tee.read());
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("abc", new String(output.toString(ASCII)));
    }

    @Test
    public void testReadToArray() throws Exception {
        final byte[] buffer = new byte[8];
        assertEquals(3, tee.read(buffer));
        assertEquals('a', buffer[0]);
        assertEquals('b', buffer[1]);
        assertEquals('c', buffer[2]);
        assertEquals(-1, tee.read(buffer));
        assertEquals("abc", new String(output.toString(ASCII)));
    }

    @Test
    public void testReadToArrayWithOffset() throws Exception {
        final byte[] buffer = new byte[8];
        assertEquals(3, tee.read(buffer, 4, 4));
        assertEquals('a', buffer[4]);
        assertEquals('b', buffer[5]);
        assertEquals('c', buffer[6]);
        assertEquals(-1, tee.read(buffer, 4, 4));
        assertEquals("abc", new String(output.toString(ASCII)));
    }

    @Test
    public void testSkip() throws Exception {
        assertEquals('a', tee.read());
        assertEquals(1, tee.skip(1));
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("ac", new String(output.toString(ASCII)));
    }

    @Test
    public void testMarkReset() throws Exception {
        assertEquals('a', tee.read());
        tee.mark(1);
        assertEquals('b', tee.read());
        tee.reset();
        assertEquals('b', tee.read());
        assertEquals('c', tee.read());
        assertEquals(-1, tee.read());
        assertEquals("abbc", new String(output.toString(ASCII)));
    }

}
