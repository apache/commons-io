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

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit Test Case for {@link AutoCloseInputStream}.
 */
public class AutoCloseInputStreamTest {

    private byte[] data;

    private InputStream stream;

    private boolean closed;

    @Before
    public void setUp() {
        data = new byte[] { 'x', 'y', 'z' };
        stream = new AutoCloseInputStream(new ByteArrayInputStream(data) {
            @Override
            public void close() {
                closed = true;
            }
        });
        closed = false;
    }

    @Test
    public void testClose() throws IOException {
        stream.close();
        assertTrue("closed", closed);
        assertEquals("read()", -1, stream.read());
    }


    @Test
    public void testRead() throws IOException {
        for (final byte element : data) {
            assertEquals("read()", element, stream.read());
            assertFalse("closed", closed);
        }
        assertEquals("read()", -1, stream.read());
        assertTrue("closed", closed);
    }

    @Test
    public void testReadBuffer() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b)) {
            assertFalse("closed", closed);
            for (int i = 0; i < n; i++) {
                assertEquals("read(b)", data[total + i], b[i]);
            }
            total += n;
        }
        assertEquals("read(b)", data.length, total);
        assertTrue("closed", closed);
        assertEquals("read(b)", -1, stream.read(b));
    }

    @Test
    public void testReadBufferOffsetLength() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b, total, b.length - total)) {
            assertFalse("closed", closed);
            total += n;
        }
        assertEquals("read(b, off, len)", data.length, total);
        for (int i = 0; i < data.length; i++) {
            assertEquals("read(b, off, len)", data[i], b[i]);
        }
        assertTrue("closed", closed);
        assertEquals("read(b, off, len)", -1, stream.read(b, 0, b.length));
    }

}
