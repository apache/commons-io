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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOStream}.
 */
public class IOStreamTest {

    private static final boolean AT_LEAST_JAVA_11 = SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_11);
    private static final boolean AT_LEAST_JAVA_17 = SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_17);

    private void compareAndSetIO(final AtomicReference<String> ref, final String expected, final String update) throws IOException {
        TestUtils.compareAndSetThrowsIO(ref, expected, update);
    }

    private void compareAndSetRE(final AtomicReference<String> ref, final String expected, final String update) {
        TestUtils.compareAndSetThrowsRE(ref, expected, update);
    }

    private void ioExceptionOnNull(final Object test) throws IOException {
        if (test == null) {
            throw new IOException("Unexpected");
        }
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testAdapt() {
        assertEquals(0, IOStream.adapt((Stream<?>) null).count());
        assertEquals(0, IOStream.adapt(Stream.empty()).count());
        assertEquals(1, IOStream.adapt(Stream.of("A")).count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testAllMatch() throws IOException {
        assertThrows(IOException.class, () -> IOStream.of("A", "B").allMatch(TestConstants.THROWING_IO_PREDICATE));
        assertTrue(IOStream.of("A", "B").allMatch(IOPredicate.alwaysTrue()));
        assertFalse(IOStream.of("A", "B").allMatch(IOPredicate.alwaysFalse()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testAnyMatch() throws IOException {
        assertThrows(IOException.class, () -> IOStream.of("A", "B").anyMatch(TestConstants.THROWING_IO_PREDICATE));
        assertTrue(IOStream.of("A", "B").anyMatch(IOPredicate.alwaysTrue()));
        assertFalse(IOStream.of("A", "B").anyMatch(IOPredicate.alwaysFalse()));
    }

    @Test
    public void testClose() {
        IOStream.of("A", "B").close();
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testCollectCollectorOfQsuperTAR() {
        // TODO IOCollector?
        IOStream.of("A", "B").collect(Collectors.toList());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testCollectSupplierOfRBiConsumerOfRQsuperTBiConsumerOfRR() throws IOException {
        // TODO Need an IOCollector?
        IOStream.of("A", "B").collect(() -> "A", (t, u) -> {
        }, (t, u) -> {
        });
        assertEquals("AB", Stream.of("A", "B").collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString());
        assertEquals("AB", IOStream.of("A", "B").collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString());
        // Exceptions
        assertThrows(IOException.class, () -> IOStream.of("A", "B").collect(TestUtils.throwingIOSupplier(), (t, u) -> {
        }, (t, u) -> {
        }));
        assertThrows(IOException.class, () -> IOStream.of("A", "B").collect(() -> "A", TestUtils.throwingIOBiConsumer(), (t, u) -> {
        }));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testCount() {
        assertEquals(0, IOStream.of().count());
        assertEquals(1, IOStream.of("A").count());
        assertEquals(2, IOStream.of("A", "B").count());
        assertEquals(3, IOStream.of("A", "B", "C").count());
        assertEquals(3, IOStream.of("A", "A", "A").count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testDistinct() {
        assertEquals(0, IOStream.of().distinct().count());
        assertEquals(1, IOStream.of("A").distinct().count());
        assertEquals(2, IOStream.of("A", "B").distinct().count());
        assertEquals(3, IOStream.of("A", "B", "C").distinct().count());
        assertEquals(1, IOStream.of("A", "A", "A").distinct().count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testEmpty() throws IOException {
        assertEquals(0, Stream.empty().count());
        assertEquals(0, IOStream.empty().count());
        IOStream.empty().forEach(TestUtils.throwingIOConsumer());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFilter() throws IOException {
        IOStream.of("A").filter(TestConstants.THROWING_IO_PREDICATE);
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").filter(TestConstants.THROWING_IO_PREDICATE).count());
        // compile vs inline lambda
        assertThrows(IOException.class, () -> IOStream.of("A").filter(e -> {
            throw new IOException("Failure");
        }).count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFindAny() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").filter(TestConstants.THROWING_IO_PREDICATE).findAny());
        // compile vs inline lambda
        assertThrows(IOException.class, () -> IOStream.of("A").filter(e -> {
            throw new IOException("Failure");
        }).findAny());

        assertTrue(IOStream.of("A", "B").filter(IOPredicate.alwaysTrue()).findAny().isPresent());
        assertFalse(IOStream.of("A", "B").filter(IOPredicate.alwaysFalse()).findAny().isPresent());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFindFirst() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").filter(TestConstants.THROWING_IO_PREDICATE).findFirst());
        // compile vs inline lambda
        assertThrows(IOException.class, () -> IOStream.of("A").filter(e -> {
            throw new IOException("Failure");
        }).findAny());

        assertTrue(IOStream.of("A", "B").filter(IOPredicate.alwaysTrue()).findFirst().isPresent());
        assertFalse(IOStream.of("A", "B").filter(IOPredicate.alwaysFalse()).findFirst().isPresent());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFlatMap() throws IOException {
        assertEquals(Arrays.asList("A", "B", "C", "D"),
                IOStream.of(IOStream.of("A", "B"), IOStream.of("C", "D")).flatMap(IOFunction.identity()).collect(Collectors.toList()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFlatMapToDouble() throws IOException {
        assertEquals('A' + 'B', IOStream.of("A", "B").flatMapToDouble(e -> DoubleStream.of(e.charAt(0))).sum());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFlatMapToInt() throws IOException {
        assertEquals('A' + 'B', IOStream.of("A", "B").flatMapToInt(e -> IntStream.of(e.charAt(0))).sum());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testFlatMapToLong() throws IOException {
        assertEquals('A' + 'B', IOStream.of("A", "B").flatMapToLong(e -> LongStream.of(e.charAt(0))).sum());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testForaAllIOConsumer() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").forAll(TestUtils.throwingIOConsumer()));
        // compile vs inline
        assertThrows(IOException.class, () -> IOStream.of("A").forAll(e -> {
            throw new IOException("Failure");
        }));
        assertThrows(IOException.class, () -> IOStream.of("A", "B").forAll(TestUtils.throwingIOConsumer()));
        final StringBuilder sb = new StringBuilder();
        IOStream.of("A", "B").forAll(sb::append);
        assertEquals("AB", sb.toString());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testForaAllIOConsumerBiFunction() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").forAll(TestUtils.throwingIOConsumer(), (i, e) -> e));
        // compile vs inline
        assertThrows(IOException.class, () -> IOStream.of("A").forAll(e -> {
            throw new IOException("Failure");
        }, (i, e) -> e));
        assertThrows(IOException.class, () -> IOStream.of("A", "B").forAll(TestUtils.throwingIOConsumer(), (i, e) -> e));
        final StringBuilder sb = new StringBuilder();
        IOStream.of("A", "B").forAll(sb::append, (i, e) -> e);
        assertEquals("AB", sb.toString());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testForaAllIOConsumerBiFunctionNull() throws IOException {
        // compile vs type
        assertDoesNotThrow(() -> IOStream.of("A").forAll(TestUtils.throwingIOConsumer(), null));
        // compile vs inline
        assertDoesNotThrow(() -> IOStream.of("A").forAll(e -> {
            throw new IOException("Failure");
        }, null));
        assertDoesNotThrow(() -> IOStream.of("A", "B").forAll(TestUtils.throwingIOConsumer(), null));
        final StringBuilder sb = new StringBuilder();
        IOStream.of("A", "B").forAll(sb::append, null);
        assertEquals("AB", sb.toString());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testForEachIOConsumerOfQsuperT() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").forEach(TestUtils.throwingIOConsumer()));
        // compile vs inline
        assertThrows(IOException.class, () -> IOStream.of("A").forEach(e -> {
            throw new IOException("Failure");
        }));
        assertThrows(IOException.class, () -> IOStream.of("A", "B").forEach(TestUtils.throwingIOConsumer()));
        final StringBuilder sb = new StringBuilder();
        IOStream.of("A", "B").forEachOrdered(sb::append);
        assertEquals("AB", sb.toString());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testForEachOrdered() throws IOException {
        // compile vs type
        assertThrows(IOException.class, () -> IOStream.of("A").forEach(TestUtils.throwingIOConsumer()));
        // compile vs inline
        assertThrows(IOException.class, () -> IOStream.of("A").forEach(e -> {
            throw new IOException("Failure");
        }));
        assertThrows(IOException.class, () -> IOStream.of("A", "B").forEach(TestUtils.throwingIOConsumer()));
        final StringBuilder sb = new StringBuilder();
        IOStream.of("A", "B").forEachOrdered(sb::append);
        assertEquals("AB", sb.toString());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testIsParallel() {
        assertFalse(IOStream.of("A", "B").isParallel());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testIterateException() throws IOException {
        final IOStream<Long> stream = IOStream.iterate(1L, TestUtils.throwingIOUnaryOperator());
        final IOIterator<Long> iterator = stream.iterator();
        assertEquals(1L, iterator.next());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testIterateLong() throws IOException {
        final IOStream<Long> stream = IOStream.iterate(1L, i -> i + 1);
        final IOIterator<Long> iterator = stream.iterator();
        assertEquals(1L, iterator.next());
        assertEquals(2L, iterator.next());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testIterator() throws IOException {
        final AtomicInteger ref = new AtomicInteger();
        IOStream.of("A", "B").iterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testLimit() {
        assertEquals(1, IOStream.of("A", "B").limit(1).count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMap() throws IOException {
        assertEquals(Arrays.asList("AC", "BC"), IOStream.of("A", "B").map(e -> e + "C").collect(Collectors.toList()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMapToDouble() {
        assertArrayEquals(new double[] { Double.parseDouble("1"), Double.parseDouble("2") }, IOStream.of("1", "2").mapToDouble(Double::parseDouble).toArray());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMapToInt() {
        assertArrayEquals(new int[] { 1, 2 }, IOStream.of("1", "2").mapToInt(Integer::parseInt).toArray());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMapToLong() {
        assertArrayEquals(new long[] { 1L, 2L }, IOStream.of("1", "2").mapToLong(Long::parseLong).toArray());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMax() throws IOException {
        assertEquals("B", IOStream.of("A", "B").max(String::compareTo).get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testMin() throws IOException {
        assertEquals("A", IOStream.of("A", "B").min(String::compareTo).get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testNoneMatch() throws IOException {
        assertThrows(IOException.class, () -> IOStream.of("A", "B").noneMatch(TestConstants.THROWING_IO_PREDICATE));
        assertFalse(IOStream.of("A", "B").noneMatch(IOPredicate.alwaysTrue()));
        assertTrue(IOStream.of("A", "B").noneMatch(IOPredicate.alwaysFalse()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testOfArray() {
        assertEquals(0, IOStream.of((String[]) null).count());
        assertEquals(0, IOStream.of().count());
        assertEquals(2, IOStream.of("A", "B").count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testOfIterable() {
        assertEquals(0, IOStream.of((Iterable<?>) null).count());
        assertEquals(0, IOStream.of(Collections.emptyList()).count());
        assertEquals(0, IOStream.of(Collections.emptySet()).count());
        assertEquals(0, IOStream.of(Collections.emptySortedSet()).count());
        assertEquals(1, IOStream.of(Arrays.asList("a")).count());
        assertEquals(2, IOStream.of(Arrays.asList("a", "b")).count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testOfOne() {
        assertEquals(1, IOStream.of("A").count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testOnClose() throws IOException {
        assertThrows(IOException.class, () -> IOStream.of("A").onClose(TestConstants.THROWING_IO_RUNNABLE).close());
        final AtomicReference<String> ref = new AtomicReference<>();
        IOStream.of("A").onClose(() -> compareAndSetIO(ref, null, "new1")).close();
        assertEquals("new1", ref.get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testOnCloseMultipleHandlers() {
        //
        final AtomicReference<String> ref = new AtomicReference<>();
        // Sanity check
        ref.set(null);
        final RuntimeException thrownRE = assertThrows(RuntimeException.class, () -> {
            // @formatter:off
            final Stream<String> stream = Stream.of("A")
                .onClose(() -> compareAndSetRE(ref, null, "new1"))
                .onClose(() -> TestConstants.throwRuntimeException("Failure 2"));
            // @formatter:on
            stream.close();
        });
        assertEquals("new1", ref.get());
        assertEquals("Failure 2", thrownRE.getMessage());
        assertEquals(0, thrownRE.getSuppressed().length);
        // Test
        ref.set(null);
        final IOException thrownIO = assertThrows(IOException.class, () -> {
            // @formatter:off
            final IOStream<String> stream = IOStream.of("A")
                .onClose(() -> compareAndSetIO(ref, null, "new1"))
                .onClose(() -> TestConstants.throwIOException("Failure 2"));
            // @formatter:on
            stream.close();
        });
        assertEquals("new1", ref.get());
        assertEquals("Failure 2", thrownIO.getMessage());
        assertEquals(0, thrownIO.getSuppressed().length);
        //
        final IOException thrownB = assertThrows(IOException.class, () -> {
            // @formatter:off
            final IOStream<String> stream = IOStream.of("A")
                .onClose(TestConstants.throwIOException("Failure 1"))
                .onClose(TestConstants.throwIOException("Failure 2"));
            // @formatter:on
            stream.close();
        });
        assertEquals("Failure 1", thrownB.getMessage());
        assertEquals(0, thrownB.getSuppressed().length);
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testParallel() {
        assertEquals(2, IOStream.of("A", "B").parallel().count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testPeek() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        // Stream sanity check
        assertEquals(1, Stream.of("A").peek(e -> compareAndSetRE(ref, null, e)).count());
        // TODO Resolve, abstract or document these differences?
        assertEquals(AT_LEAST_JAVA_11 ? null : "A", ref.get());
        if (AT_LEAST_JAVA_11) {
            assertEquals(1, IOStream.of("B").peek(e -> compareAndSetRE(ref, null, e)).count());
            assertEquals(1, IOStream.of("B").peek(e -> compareAndSetIO(ref, null, e)).count());
            assertNull(ref.get());
        } else {
            // Java 8
            assertThrows(RuntimeException.class, () -> IOStream.of("B").peek(e -> compareAndSetRE(ref, null, e)).count());
            assertThrows(IOException.class, () -> IOStream.of("B").peek(e -> compareAndSetIO(ref, null, e)).count());
            assertEquals("A", ref.get());
        }
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testReduceBinaryOperatorOfT() throws IOException {
        assertEquals("AB", IOStream.of("A", "B").reduce((t, u) -> t + u).get());
        assertEquals(TestConstants.ABS_PATH_A.toRealPath(),
                IOStream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B).reduce((t, u) -> t.toRealPath()).get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testReduceTBinaryOperatorOfT() throws IOException {
        assertEquals("_AB", IOStream.of("A", "B").reduce("_", (t, u) -> t + u));
        assertEquals(TestConstants.ABS_PATH_A.toRealPath(),
                IOStream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B).reduce(TestConstants.ABS_PATH_A, (t, u) -> t.toRealPath()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() throws IOException {
        assertEquals("_AB", IOStream.of("A", "B").reduce("_", (t, u) -> t + u, (t, u) -> t + u));
        assertEquals(TestConstants.ABS_PATH_A.toRealPath(), IOStream.of(TestConstants.ABS_PATH_A, TestConstants.ABS_PATH_B).reduce(TestConstants.ABS_PATH_A,
                (t, u) -> t.toRealPath(), (t, u) -> u.toRealPath()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testSequential() {
        assertEquals(2, IOStream.of("A", "B").sequential().count());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testSkip() throws IOException {
        final AtomicReference<String> ref = new AtomicReference<>();
        assertEquals(1, Stream.of("A", "B").skip(1).peek(e -> compareAndSetRE(ref, null, e)).count());
        // TODO Resolve, abstract or document these differences?
        assertEquals(AT_LEAST_JAVA_17 ? null : "B", ref.get());
        if (AT_LEAST_JAVA_17) {
            assertEquals(1, IOStream.of("C", "D").skip(1).peek(e -> compareAndSetRE(ref, null, e)).count());
            assertEquals(1, IOStream.of("C", "D").skip(1).peek(e -> compareAndSetIO(ref, null, e)).count());
            assertNull(ref.get());
        } else {
            if (AT_LEAST_JAVA_11) {
                assertThrows(RuntimeException.class, () -> IOStream.of("C", "D").skip(1).peek(e -> compareAndSetRE(ref, null, e)).count());
                assertThrows(IOException.class, () -> IOStream.of("C", "D").skip(1).peek(e -> compareAndSetIO(ref, null, e)).count());
            } else {
                assertThrows(RuntimeException.class, () -> IOStream.of("C", "D").skip(1).peek(e -> compareAndSetRE(ref, null, e)).count());
                assertThrows(IOException.class, () -> IOStream.of("C", "D").skip(1).peek(e -> compareAndSetIO(ref, null, e)).count());
            }
            assertEquals("B", ref.get());
        }
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testSorted() throws IOException {
        assertEquals(Arrays.asList("A", "B", "C", "D"), IOStream.of("D", "A", "B", "C").sorted().collect(Collectors.toList()));
        assertEquals(Arrays.asList("A", "B", "C", "D"), IOStream.of("D", "A", "B", "C").sorted().peek(this::ioExceptionOnNull).collect(Collectors.toList()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testSortedComparatorOfQsuperT() throws IOException {
        assertEquals(Arrays.asList("A", "B", "C", "D"), IOStream.of("D", "A", "B", "C").sorted(String::compareTo).collect(Collectors.toList()));
        assertEquals(Arrays.asList("A", "B", "C", "D"),
                IOStream.of("D", "A", "B", "C").sorted(String::compareTo).peek(this::ioExceptionOnNull).collect(Collectors.toList()));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testSpliterator() {
        final AtomicInteger ref = new AtomicInteger();
        IOStream.of("A", "B").spliterator().forEachRemaining(e -> ref.incrementAndGet());
        assertEquals(2, ref.get());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testToArray() {
        assertArrayEquals(new String[] { "A", "B" }, IOStream.of("A", "B").toArray());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testToArrayIntFunctionOfA() {
        assertArrayEquals(new String[] { "A", "B" }, IOStream.of("A", "B").toArray(String[]::new));
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testUnordered() {
        // Sanity check
        assertArrayEquals(new String[] { "A", "B" }, Stream.of("A", "B").unordered().toArray());
        // Test
        assertArrayEquals(new String[] { "A", "B" }, IOStream.of("A", "B").unordered().toArray());
    }

    @SuppressWarnings("resource") // custom stream not recognized by compiler warning machinery
    @Test
    public void testUnwrap() {
        final Stream<String> unwrap = IOStream.of("A", "B").unwrap();
        assertNotNull(unwrap);
        assertEquals(2, unwrap.count());
    }

}
