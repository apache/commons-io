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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link CloseShieldInputStream}.
 */
public class CloseShieldInputStreamTest {

    private byte[] data;

    private InputStream original;

    private InputStream shielded;

    private boolean closed;

    @BeforeEach
    public void setUp() {
        data = new byte[] { 'x', 'y', 'z' };
        original = new ByteArrayInputStream(data) {
            @Override
            public void close() {
                closed = true;
            }
        };
        shielded = new CloseShieldInputStream(original);
        closed = false;
    }

    @Test
    public void testClose() throws IOException {
        shielded.close();
        assertFalse(closed, "closed");
        assertEquals(-1, shielded.read(), "read()");
        assertEquals(data[0], original.read(), "read()");
    }

}
