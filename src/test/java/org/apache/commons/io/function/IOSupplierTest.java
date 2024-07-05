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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOSupplier}.
 */
public class IOSupplierTest {

    private AtomicReference<String> ref1;

    private String getThrowsIO(final IOSupplier<String> supplier) throws IOException {
        return supplier.get();
    }

    private String getThrowsNoneAsSupplier(final IOSupplier<String> supplier) {
        return supplier.asSupplier().get();
    }

    private String getThrowsNoneGetUnchecked(final IOSupplier<String> supplier) {
        return supplier.getUnchecked();
    }

    @BeforeEach
    public void initEach() {
        ref1 = new AtomicReference<>();
    }

    @Test
    public void testAsSupplier() {
        assertThrows(UncheckedIOException.class, () -> TestConstants.THROWING_IO_SUPPLIER.asSupplier().get());
        final String s1 = "string1";
        final String s2 = "string2";
        assertEquals(s1, getThrowsNoneAsSupplier(() -> TestUtils.compareAndSetThrowsIO(ref1, null, s1)));
        assertEquals(s1, ref1.get());
        assertEquals(s2, getThrowsNoneAsSupplier(() -> TestUtils.compareAndSetThrowsIO(ref1, s1, s2)));
        assertEquals(s2, ref1.get());
        assertNotEquals(TestConstants.THROWING_IO_SUPPLIER.asSupplier(), TestConstants.THROWING_IO_SUPPLIER.asSupplier());
    }

    @Test
    public void testGet() throws IOException {
        assertThrows(IOException.class, () -> TestConstants.THROWING_IO_SUPPLIER.get());
        assertThrows(IOException.class, () -> {
            throw new IOException();
        });
        assertEquals("new1", getThrowsIO(() -> TestUtils.compareAndSetThrowsIO(ref1, "new1")));
        assertEquals("new1", ref1.get());
    }

    @Test
    public void testGetUnchecked() {
        assertThrows(UncheckedIOException.class, () -> TestConstants.THROWING_IO_SUPPLIER.asSupplier().get());
        final String s1 = "string1";
        final String s2 = "string2";
        assertEquals(s1, getThrowsNoneGetUnchecked(() -> TestUtils.compareAndSetThrowsIO(ref1, null, s1)));
        assertEquals(s1, ref1.get());
        assertEquals(s2, getThrowsNoneGetUnchecked(() -> TestUtils.compareAndSetThrowsIO(ref1, s1, s2)));
        assertEquals(s2, ref1.get());
        assertNotEquals(TestConstants.THROWING_IO_SUPPLIER.asSupplier(), TestConstants.THROWING_IO_SUPPLIER.asSupplier());
    }

}