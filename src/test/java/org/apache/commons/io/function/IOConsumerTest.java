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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.test.ThrowOnCloseReader;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOConsumer}.
 */
public class IOConsumerTest {

    @Test
    void testAccept() throws IOException {
        IOConsumer.noop().accept(null);
        IOConsumer.noop().accept(".");
        Uncheck.accept(Files::size, PathUtils.current());
        //
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOConsumer<String> consumer = s -> ref.set(s + "1");
        consumer.accept("A");
        assertEquals("A1", ref.get());
    }

    @Test
    void testAndThen() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOConsumer<String> consumer1 = s -> ref.set(s + "1");
        final IOConsumer<String> consumer2 = s -> ref.set(ref.get() + "2" + s);
        consumer1.andThen(consumer2).accept("B");
        assertEquals("B12B", ref.get());
    }

    @Test
    public void testAsConsumer() {
        assertThrows(UncheckedIOException.class, () -> Optional.of("a").ifPresent(TestUtils.throwingIOConsumer().asConsumer()));
        final AtomicReference<String> ref = new AtomicReference<>();
        final IOConsumer<String> consumer1 = s -> ref.set(s + "1");
        Optional.of("a").ifPresent(consumer1.asConsumer());
        assertEquals("a1", ref.get());
    }

    @Test
    public void testForAllArrayOf1() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (String[]) null);
        IOConsumer.forAll(null, (String[]) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), "1"));
        //
        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, "1");
        assertEquals("01", ref.get());
    }

    @Test
    public void testForAllArrayOf2() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (String[]) null);
        IOConsumer.forAll(null, (String[]) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), "1", "2"));
        //
        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, "1", "2");
        assertEquals("012", ref.get());
    }

    @Test
    public void testForAllIterableOf1() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (Iterable<Object>) null);
        IOConsumer.forAll(null, (Iterable<Object>) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), Arrays.asList("1")));

        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, Arrays.asList("1"));
        assertEquals("01", ref.get());
    }

    @Test
    public void testForAllIterableOf2() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (Iterable<Object>) null);
        IOConsumer.forAll(null, (Iterable<Object>) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), Arrays.asList("1", "2")));

        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, Arrays.asList("1", "2"));
        assertEquals("012", ref.get());
    }

    @Test
    public void testForAllStreamOf1() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (Stream<Object>) null);
        IOConsumer.forAll(null, (Stream<Object>) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), Arrays.asList("1").stream()));

        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, Arrays.asList("1").stream());
        assertEquals("01", ref.get());
    }

    @Test
    public void testForAllStreamOf2() throws IOException {
        IOConsumer.forAll(TestUtils.throwingIOConsumer(), (Stream<Object>) null);
        IOConsumer.forAll(null, (Stream<Object>) null);
        assertThrows(IOExceptionList.class, () -> IOConsumer.forAll(TestUtils.throwingIOConsumer(), Arrays.asList("1", "2").stream()));

        final AtomicReference<String> ref = new AtomicReference<>("0");
        final IOConsumer<String> consumer1 = s -> ref.set(ref.get() + s);
        IOConsumer.forAll(consumer1, Arrays.asList("1", "2").stream());
        assertEquals("012", ref.get());
    }

    @Test
    public void testNoop() {
        final Closeable nullCloseable = null;
        final IOConsumer<IOException> noopConsumer = IOConsumer.noop(); // noop consumer doesn't throw
        assertDoesNotThrow(() -> IOUtils.close(nullCloseable, noopConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new StringReader("s"), noopConsumer));
        assertDoesNotThrow(() -> IOUtils.close(new ThrowOnCloseReader(new StringReader("s")), noopConsumer));
    }

}
