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
package org.apache.commons.io.output;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit Test Case for {@link BrokenOutputStream}.
 */
public class BrokenOutputStreamTest {

    private IOException exception;

    private OutputStream stream;

    @Before
    public void setUp() {
        exception = new IOException("test exception");
        stream = new BrokenOutputStream(exception);
    }

    @Test
    public void testWrite() {
        try {
            stream.write(1);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }

        try {
            stream.write(new byte[1]);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }

        try {
            stream.write(new byte[1], 0, 1);
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

    @Test
    public void testFlush() {
        try {
            stream.flush();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

    @Test
    public void testClose() {
        try {
            stream.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertEquals(exception, e);
        }
    }

}
