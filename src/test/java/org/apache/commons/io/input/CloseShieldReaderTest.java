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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Test Case for {@link CloseShieldReader}.
 */
public class CloseShieldReaderTest {

    private String data;

    private Reader original;

    private Reader shielded;

    private boolean closed;

    @Before
    public void setUp() {
        data = "xyz";
        original = new CharSequenceReader(data) {
            @Override
            public void close() {
                closed = true;
            }
        };
        shielded = new CloseShieldReader(original);
        closed = false;
    }

    @Test
    public void testClose() throws IOException {
        shielded.close();
        assertFalse("closed", closed);
        char[] cbuf = new char[10];
        assertEquals("read(cbuf, off, len)", -1, shielded.read(cbuf, 0, 10));
        assertEquals("read(cbuf, off, len)", data.length(), original.read(cbuf, 0, 10));
        assertEquals(data, new String(cbuf, 0, data.length()));
    }

}
