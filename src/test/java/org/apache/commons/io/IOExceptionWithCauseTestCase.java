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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests IOExceptionWithCause
 *
 */
public class IOExceptionWithCauseTestCase {

    /**
     * Tests the {@link IOExceptionWithCause#IOExceptionWithCause(String,Throwable)} constructor.
     */
    @Test
    public void testIOExceptionStringThrowable() {
        final Throwable cause = new IllegalArgumentException("cause");
        final IOException exception = new IOException("message", cause);
        this.validate(exception, cause, "message");
    }

    /**
     * Tests the {@link IOExceptionWithCause#IOExceptionWithCause(Throwable)} constructor.
     */

    @Test
    public void testIOExceptionThrowable() {
        final Throwable cause = new IllegalArgumentException("cause");
        final IOException exception = new IOException(cause);
        this.validate(exception, cause, "java.lang.IllegalArgumentException: cause");
    }

    void validate(final Throwable throwable, final Throwable expectedCause, final String expectedMessage) {
        assertEquals(expectedMessage, throwable.getMessage());
        assertEquals(expectedCause, throwable.getCause());
        assertSame(expectedCause, throwable.getCause());
    }
}
