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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AppendableWriter}.
 *
 */
public class AppendableWriterTest {

    private AppendableWriter<StringBuilder> out;

    @BeforeEach
    public void setUp() throws Exception {
        out = new AppendableWriter<>(new StringBuilder());
    }

    @Test
    public void testWriteInt() throws Exception {
        out.write('F');

        assertEquals("F", out.getAppendable().toString());
    }

    @Test
    public void testWriteChars() throws Exception {
        final String testData = "ABCD";

        out.write(testData.toCharArray());

        assertEquals(testData, out.getAppendable().toString());
    }

    @Test
    public void testWriteString() throws Exception {
        final String testData = "ABCD";

        out.write(testData);

        assertEquals(testData, out.getAppendable().toString());
    }

    @Test
    public void testAppendCharSequence() throws Exception {
        final String testData = "ABCD";

        out.append(testData);
        out.append(null);

        assertEquals(testData + "null", out.getAppendable().toString());
    }

    @Test
    public void testAppendSubSequence() throws Exception {
        final String testData = "ABCD";

        out.append(testData, 1, 3);
        out.append(null, 1, 3);

        assertEquals(testData.substring(1, 3) + "ul", out.getAppendable().toString());
    }

    @Test
    public void testAppendChar() throws Exception {
        out.append('F');

        assertEquals("F", out.getAppendable().toString());
    }
}
