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

import java.io.IOException;

import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * JUnit Test Case for {@link ClosedOutputStream}.
 */
public class ClosedOutputStreamTest {

    /**
     * Test the <code>write(b)</code> method.
     * @throws Exception 
     */
    @Test
    public void testRead() throws Exception {
        ClosedOutputStream cos = null;
        try {
            cos = new ClosedOutputStream();
            cos.write('x');
            fail("write(b)");
        } catch (final IOException e) {
            // expected
        } finally {
            cos.close();
        }
    }

}
