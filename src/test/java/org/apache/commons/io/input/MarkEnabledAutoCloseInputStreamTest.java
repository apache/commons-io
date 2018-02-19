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

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit Test Case for {@link AutoCloseInputStream} with enabled mark support (the default).
 */
public class MarkEnabledAutoCloseInputStreamTest extends AbstractAutoCloseInputStreamTest {

    public MarkEnabledAutoCloseInputStreamTest() {
        super(true);
    }

    @Test
    public void testMark() throws IOException {
        assertTrue(targetStream.markSupported());

        // Make sure mark is disabled
        assertTrue(stream.markSupported());

        // Check that mark() does not fail
        stream.mark(1);
        assertTrue("not marked", marked);

        assertEquals('x', this.stream.read());

        // Check that reset() works
        stream.reset();
        assertTrue("not reseted", reseted);

        assertEquals('x', this.stream.read());
    }
}
