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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link CloseShieldReader}.
 */
public class CloseShieldReaderTest {

    private String data;

    private Reader original;

    private Reader shielded;

    @BeforeEach
    public void setUp() {
        data = "xyz";
        original = spy(new CharSequenceReader(data));
        shielded = CloseShieldReader.wrap(original);
    }

    @Test
    public void testClose() throws IOException {
        shielded.close();
        verify(original, never()).close();
        final char[] cbuf = new char[10];
        assertEquals(-1, shielded.read(cbuf, 0, 10), "read(cbuf, off, len)");
        assertEquals(data.length(), original.read(cbuf, 0, 10), "read(cbuf, off, len)");
        assertEquals(data, new String(cbuf, 0, data.length()));
    }

}
