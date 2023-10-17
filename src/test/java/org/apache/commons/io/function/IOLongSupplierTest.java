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
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOLongSupplier}.
 */
public class IOLongSupplierTest {

    private AtomicLong atomicLong;

    private long getThrowsIO(final IOLongSupplier supplier) throws IOException {
        return supplier.getAsLong();
    }

    private long getThrowsNone(final IOLongSupplier supplier) {
        return supplier.asSupplier().getAsLong();
    }

    @BeforeEach
    public void initEach() {
        atomicLong = new AtomicLong();
    }

    @Test
    public void testAsSupplier() {
        assertThrows(UncheckedIOException.class, () -> TestConstants.THROWING_IO_LONG_SUPPLIER.asSupplier().getAsLong());
        assertEquals(1L, getThrowsNone(() -> TestUtils.compareAndSetThrowsIO(atomicLong, 1L)));
        assertEquals(1L, atomicLong.get());
        assertNotEquals(TestConstants.THROWING_IO_LONG_SUPPLIER.asSupplier(), TestConstants.THROWING_IO_LONG_SUPPLIER.asSupplier());
    }

    @Test
    public void testGet() throws IOException {
        assertThrows(IOException.class, () -> TestConstants.THROWING_IO_LONG_SUPPLIER.getAsLong());
        assertThrows(IOException.class, () -> {
            throw new IOException();
        });
        assertEquals(1L, getThrowsIO(() -> TestUtils.compareAndSetThrowsIO(atomicLong, 1L)));
        assertEquals(1L, atomicLong.get());
    }

}