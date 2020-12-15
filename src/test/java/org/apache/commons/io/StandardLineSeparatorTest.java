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

package org.apache.commons.io;

import static org.apache.commons.io.StandardLineSeparator.CR;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import static org.apache.commons.io.StandardLineSeparator.LF;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link StandardLineSeparator}.
 */
public class StandardLineSeparatorTest {

    @Test
    public void testCR() {
        assertEquals("\r", CR.getString());
    }

    @Test
    public void testCR_getBytes() {
        assertArrayEquals("\r".getBytes(StandardCharsets.ISO_8859_1), CR.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testCRLF() {
        assertEquals("\r\n", CRLF.getString());
    }

    @Test
    public void testCRLF_getBytes() {
        assertArrayEquals("\r\n".getBytes(StandardCharsets.ISO_8859_1), CRLF.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testLF() {
        assertEquals("\n", LF.getString());
    }

    @Test
    public void testLF_getBytes() {
        assertArrayEquals("\n".getBytes(StandardCharsets.ISO_8859_1), LF.getBytes(StandardCharsets.ISO_8859_1));
    }

}
