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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.EOFException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOIndexedException}.
 *
 * @since 2.7
 */
public class IOIndexedExceptionTestCase {

    @Test
    public void testEdge() {
        final IOIndexedException exception = new IOIndexedException(-1, null);
        assertEquals(-1, exception.getIndex());
        assertNull(exception.getCause());
        assertNotNull(exception.getMessage());
    }

    @Test
    public void testPlain() {
        final EOFException e = new EOFException("end");
        final IOIndexedException exception = new IOIndexedException(0, e);
        assertEquals(0, exception.getIndex());
        assertEquals(e, exception.getCause());
        assertNotNull(exception.getMessage());
    }
}
