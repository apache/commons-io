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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link ClosedWriter}.
 */
public class ClosedWriterTest {

    /**
     * Test the <code>write(cbuf, off, len)</code> method.
     * @throws Exception
     */
    @Test
    public void testWrite() throws Exception {
        try (ClosedWriter cw = new ClosedWriter()) {
            cw.write(new char[0], 0, 0);
            fail("write(cbuf, off, len)");
        } catch (final IOException e) {
            // expected
        }
    }

    /**
     * Test the <code>flush()</code> method.
     * @throws Exception
     */
    @Test
    public void testFlush() throws Exception {
        try (ClosedWriter cw = new ClosedWriter()) {
            cw.flush();
            fail("flush()");
        } catch (final IOException e) {
            // expected
        }
    }

}
