/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TaggedIOException}.
 */
public class TaggedIOExceptionTest {

    @Test
    public void testTaggedIOException() {
        final Serializable tag = UUID.randomUUID();
        final IOException exception = new IOException("Test exception");
        final TaggedIOException tagged = new TaggedIOException(exception, tag);
        assertTrue(TaggedIOException.isTaggedWith(tagged, tag));
        assertFalse(TaggedIOException.isTaggedWith(tagged, UUID.randomUUID()));
        assertEquals(exception, tagged.getCause());
        assertEquals(exception.getMessage(), tagged.getMessage());
    }

}
