/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link StringInputStream}.
 */
public class StringInputStreamTest {

    @Test
    public void testStrinConstructorString() throws IOException {
        try (final StringInputStream input = StringInputStream.on("01")) {
            assertEquals("01", IOUtils.toString(input, Charset.defaultCharset()));
        }
    }

    @Test
    public void testStrinConstructorStringCharset() throws IOException {
        try (final StringInputStream input = new StringInputStream("01", Charset.defaultCharset())) {
            assertEquals("01", IOUtils.toString(input, Charset.defaultCharset()));
        }
    }
}
