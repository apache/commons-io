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

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

public class UncheckedIOExceptionsTest {

    @Test
    public void testCreate() {
        final Object message = "test";
        try {
            throw UncheckedIOExceptions.create(message);
        } catch (final UncheckedIOException e) {
            assertEquals(message, e.getMessage());
            assertEquals(message, e.getCause().getMessage());
        }

    }

    @Test
    public void testCreateWithException() {
        final Object message1 = "test1";
        final Object message2 = "test2";
        final IOException ioe = new IOException(message2.toString());
        try {
            throw UncheckedIOExceptions.create(message1, ioe);
        } catch (final UncheckedIOException e) {
            assertEquals(message1, e.getMessage());
            assertEquals(message2, e.getCause().getMessage());
        }

    }
}
