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

package org.apache.commons.io.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOBooleanSupplier}.
 */
public class IOBooleanSupplierTest {

    private AtomicBoolean atomicBoolean;

    @BeforeEach
    public void beforeEach() {
        atomicBoolean = new AtomicBoolean();
    }

    private boolean getThrowsIO(final IOBooleanSupplier supplier) throws IOException {
        return supplier.getAsBoolean();
    }

    private boolean getThrowsNone(final IOBooleanSupplier supplier) {
        return supplier.asBooleanSupplier().getAsBoolean();
    }

    @Test
    public void testAsSupplier() {
        assertThrows(UncheckedIOException.class, () -> TestConstants.THROWING_IO_BOOLEAN_SUPPLIER.asBooleanSupplier().getAsBoolean());
        assertEquals(true, getThrowsNone(() -> TestUtils.compareAndSetThrowsIO(atomicBoolean, true)));
        assertEquals(true, atomicBoolean.get());
        assertNotEquals(TestConstants.THROWING_IO_BOOLEAN_SUPPLIER.asBooleanSupplier(), TestConstants.THROWING_IO_BOOLEAN_SUPPLIER.asBooleanSupplier());
    }

    @Test
    public void testGet() throws IOException {
        assertThrows(IOException.class, () -> TestConstants.THROWING_IO_BOOLEAN_SUPPLIER.getAsBoolean());
        assertThrows(IOException.class, () -> {
            throw new IOException();
        });
        assertEquals(true, getThrowsIO(() -> TestUtils.compareAndSetThrowsIO(atomicBoolean, true)));
        assertEquals(true, atomicBoolean.get());
    }
}
